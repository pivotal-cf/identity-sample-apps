# Local end-to-end test harness

This directory contains a docker/podman-compose environment that runs a real UAA
server plus all four sample apps, and drives them with the existing `journeys`
Selenium suite — locally, without needing Concourse.

## Prerequisites

- podman (with the compose extension: `podman compose`, backed by a `docker-compose`
  binary on PATH) or Docker Desktop, with a running machine/daemon.
- On macOS, initialize a podman machine with enough memory for a JVM-based UAA
  server plus four Spring Boot apps and a browser, e.g.:

  ```
  podman machine init --now -m 8192 --disk-size 60 --import-native-ca
  ```

  `--import-native-ca` is required if your network intercepts TLS (e.g. a corporate
  proxy) — without it, image pulls from Docker Hub/quay.io will fail with certificate
  errors.

## Running

```
./docker/run-e2e-tests.sh
```

This builds the four sample apps' images, starts UAA + the apps + a Selenium
container, runs `journeys`, dumps logs to `docker/artifacts/`, and tears everything
down. Run it once against an unmodified baseline to confirm the harness itself
works, then again after making code changes to confirm identical behavior.

## Notable design decisions

- **UAA image**: `docker.io/cfidentity/uaa:v78.16.0`, the last UAA release before
  it moved to Java 25 / Spring Boot 4.1 internally (`v79.0.0`). Pinning an older,
  stable UAA avoids a dual-JDK conflict entirely — UAA runs in its own container
  off a pre-built image, independent of whatever JDK the sample apps target.
  `platform: linux/amd64` is set because this image has no arm64 variant; on Apple
  Silicon it runs under emulation (functionally correct, just slower to start).
- **UAA config**: `uaa/uaa.yml` is bind-mounted into the container and picked up
  natively via `CLOUDFOUNDRY_CONFIG_PATH`/`SECRETS_DIR` env vars — no `yq merge`/
  image-rebuild step needed. Its `oauth.clients`/`scim.users`/`jwt.token` sections
  are copied from `../journeys/src/test/resources/uaa-customizations.yml` (the
  canonical fixture also used by the Concourse CI flow), with only the OAuth
  client `redirect-uri` hosts widened to compose service names. Keep both files in
  sync if clients/users/scopes change.
- **UAA SAML key**: `uaa/uaa.yml` also configures a throwaway self-signed
  `login.saml` signing key. UAA v78.16.0 eagerly builds a self-referential SAML
  "relying party registration" at startup, and its default metadata generation
  fails hard (`Metadata response is missing verification certificates`) without a
  configured SAML key — even though this project's sample apps only use OAuth2
  and never touch SAML. This key exists purely to let UAA boot; it does not affect
  any sample app behavior.
- **UAA memory**: the container is limited to `mem_limit: 3g` so Paketo's buildpack
  memory calculator (which otherwise sizes the JVM heap off the whole host/VM's
  visible memory) doesn't over-allocate and trigger an OOM exit.
- **VCAP_SERVICES fixtures**: `vcap/*.json` are copies of the `journeys` module's
  fixtures with `auth_domain` repointed from `localhost:8080` to the compose
  service name `uaa:8080`. `RESOURCE_URL` is passed directly as an env var (the
  apps read it directly, not via VCAP), so no fixture rewriting was needed there.
- **Networking**: every service talks to every other service by its compose
  service name, never `localhost` — required because `resource-server` validates
  JWTs by making a real server-side call to UAA's OIDC discovery/JWKS endpoints.
- **Selenium**: the compose network runs a `selenium/standalone-chrome` (x86_64)
  or `seleniarm/standalone-chromium` (arm64) container, driven via
  `RemoteWebDriver` from the `journeys-runner` container (see
  `HeadlessChromeTest.java`'s `selenium.remote.url` support). Concourse's existing
  bare-metal flow is untouched — it never sets that property, so it keeps using a
  local `ChromeDriver` exactly as before. Pinned to the `120.0-chromedriver-120.0-
  grid-4.16.1-*` tag (matching the `journeys` module's Selenium Java client version,
  4.16.1) rather than `:latest` — see "Known limitation" below.
- **Selenium session slots**: `SE_NODE_MAX_SESSIONS=2` (with
  `SE_NODE_OVERRIDE_MAX_SESSIONS=true`) so a standalone node isn't limited to a
  single concurrent session by default, which caused later tests to hang
  indefinitely if an earlier test's session wasn't cleanly closed (e.g. after a
  forcefully-killed run).
- **journeys as a standalone Gradle project**: `journeys` has its own
  `settings.gradle`/Gradle wrapper (mirroring the four sample app modules) so it
  can be built/run independently inside the `journeys-runner` image. It remains
  excluded from the root `settings.gradle` on purpose, so the default
  `./gradlew build` at repo root stays fast and independent of Selenium/Chrome.

## Proving an upgrade didn't change the HTTP contract

`compare-contract.sh` diffs every reachable endpoint of a candidate build against a
reference build — status, `Location`, `WWW-Authenticate`, `Content-Type` and body,
normalising only values that legitimately vary per request (JWT `exp`/`iat`/`jti`,
session ids, CSRF tokens, generated todo ids).

To use it, run the reference build on the offset ports (default: standard port minus
10, with resource-server on 8899) and the candidate on the standard ports, then:

```
./docker/compare-contract.sh
```

This is how the Spring Boot 4.1 upgrade was verified. It caught two changes that the
in-process tests had been too loosely written to detect — Spring Security 7 adding an
RFC 9728 `resource_metadata` parameter to resource-server's `WWW-Authenticate`
challenge, and newly serving `/.well-known/oauth-protected-resource` — both since
pinned back to their pre-upgrade behavior. A convenient way to produce the reference
build is `git worktree add <dir> <pre-upgrade-commit>` and `gradle -p <dir> bootJar`.

## Known limitation: the browser-login journeys fail locally (pre-existing)

`AuthorizationCodeTest`'s 4 tests and `MutiGrantAuththorizationCodeClientCredentialsTest`'s
`step03`-`step06` — the ones requiring a full browser-driven `authorization_code`
OIDC login — fail when run locally, both inside this harness and against apps run
directly on the host. The other 8 tests (everything using `client_credentials`, which
never needs a browser login) pass reliably.

**This is not caused by the Spring Boot 4.1 upgrade.** Verified by A/B test: the same
4 tests fail identically when run against jars built from the pre-upgrade Spring Boot
3.5.12 commit (`ea40d73`) in the same environment, with only the Boot version differing.

Ruled out during investigation:
- Wait timeouts — FluentLenium's `@Wait` was raised from the 5s default to 30s; no change.
- Selenium session-slot contention — real, and fixed (see `SE_NODE_MAX_SESSIONS` above),
  but not the cause of these failures.
- Selenium/Chromium version skew — retested with an image whose bundled grid version
  exactly matches the Selenium Java client (4.16.1); no change.
- Compose networking — the failures reproduce with everything on real `localhost`
  and a real local Chrome, outside containers entirely.
- The app and the UAA login page — raw WebDriver protocol calls (bypassing
  FluentLenium/JUnit) show the login page rendering correctly with
  `input[name=username]` instantly findable. A temporary diagnostic inside the failing
  test printed `pageSource()` to the JUnit XML report and showed the element present in
  the DOM moments before that same session's `$(...).fill()` timed out.

Current best guess: something in the Selenium Java client's window/context handling
across the cross-origin redirect this flow requires (the app's origin -> UAA's origin),
not yet isolated from FluentLenium's wrapper layer. The next diagnostic step would be a
minimal standalone Java class using only `RemoteWebDriver` (no FluentLenium, no JUnit)
to determine which layer owns the bug.

The Concourse CI flow is a separate environment and is unaffected by the harness itself;
whether these tests pass there has not been re-checked as part of this work.

## CI

The existing Concourse pipeline (`ci/pipeline.yml`, `ci/tasks/journeys/task.sh`) is
left untouched for now — this harness's job is a fast local verification loop.
A future option is to replace that pipeline's bare-metal orchestration with this
same compose setup (in a Concourse task image with podman/docker available, or a
GitHub Actions job). Separately: today's pipeline pins UAA to `branch: master`
(bleeding-edge) — the same root cause that produced the Java 25 dual-JDK conflict
this harness avoids by pinning `v78.16.0`. Whenever the pipeline is revisited, it
should be repointed at a `v78.*` tag rather than `master`/`develop`.
