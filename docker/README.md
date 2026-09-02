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

The individual stages are also available as subcommands, for when the stack needs to
outlive a single test run — starting it once and then running several filtered passes
against it, which is what the GitHub Actions job does (see "CI" below):

```
./docker/run-e2e-tests.sh up                  # build + start uaa, the apps, selenium
./docker/run-e2e-tests.sh test [gradle args]  # run journeys; extra args go to `gradle test`
./docker/run-e2e-tests.sh logs                # dump container logs to the artifacts dir
./docker/run-e2e-tests.sh down                # tear down
```

`test` bind-mounts the `journeys` build directory out to
`docker/artifacts/$RESULTS_NAME/` (default `journeys`) so the JUnit XML and HTML
reports survive the container being removed. To run a subset, pass Gradle's
`--tests` filters straight through:

```
./docker/run-e2e-tests.sh test --tests 'io.pivotal.cf.identity.samples.journeys.ClientCredentialsTest'
```

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
  canonical fixture also used by the Concourse CI flow), with the OAuth client
  `redirect-uri` hosts repointed from `localhost` to compose service names. Keep both
  files in sync if clients/users/scopes change — including the exact `redirect-uri`
  entries, which are load-bearing (see "The `redirect_uri` wildcard trap" below).
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
  local `ChromeDriver` exactly as before. Both images are pinned to grid `4.16.1`,
  matching the `journeys` module's Selenium Java client version, rather than `:latest`
  — `selenium/standalone-chrome:4.16.1-20231219` and
  `seleniarm/standalone-chromium:4.16.1-20231230`. The dates differ because the two
  repositories publish independently; don't copy a date (or a tag shape) from one to the
  other, or the architecture you don't develop on will fail to pull.
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

## The `redirect_uri` wildcard trap (fixed — read before editing UAA client config)

All 12 journeys pass. They didn't used to: `AuthorizationCodeTest`'s 4 tests and
`MutiGrantAuththorizationCodeClientCredentialsTest`'s `step03`-`step06` — every test
needing a browser-driven `authorization_code` OIDC login — failed until the client
`redirect-uri` registrations in `uaa/uaa.yml` were given **exact** URIs.

**Cause.** UAA v78.16.0 resolves `redirect_uri` with `NormalizedRedirectResolver`, which
matches *literally*. An Ant-style `http://authcode:8888/**` registration matches nothing —
not even `http://authcode:8888/` itself. UAA does ship a wildcard-capable
`LegacyRedirectResolver`, selected by `RedirectResolverFactoryBean` when its
`allowUnsafeMatching` flag is set, but **nothing in the shipped v78.16.0 artifacts
references that factory bean** (the string `unsafe` occurs in exactly one class in
`cloudfoundry-identity-server-v78.16.0.jar`: the unused factory itself). So there is no
config property that re-enables wildcards — exact URIs are the only option.

Verified by registering a family of clients differing only in pattern shape and calling
`/oauth/authorize` post-login for each:

| Registered | Requested | Result |
| --- | --- | --- |
| `http://authcode:8888/login/oauth2/code/sso` | same | **200 accepted** |
| `http://authcode:8888/**` | `…/login/oauth2/code/sso` | 400 invalid redirect |
| `http://authcode:8888/login/**` | `…/login/oauth2/code/sso` | 400 invalid redirect |
| `http://authcode:8888/*` | `…/foo` | 400 invalid redirect |
| `http://localhost:8888/**` | `…/login/oauth2/code/sso` | 400 invalid redirect |
| `http://app.example.com:8888/**` | `…/login/oauth2/code/sso` | 400 invalid redirect |

**Why it looked like a Selenium bug.** UAA validates the redirect only *after*
authentication, so login itself succeeds (`POST /uaa/login.do` → 302) and only the
following `/oauth/authorize` fails (400). The browser lands on a UAA error page with no
`h1` and no `.user_info`, and FluentLenium then burns two 30s waits — which reads as a
~62s "element never appeared" timeout rather than an OAuth error. An earlier round of
investigation attributed this to Selenium's window handling across the cross-origin
redirect and ruled out wait timeouts, session-slot contention, Selenium/Chromium version
skew, and compose networking. Those were all correctly ruled out, and the conclusion that
the Spring Boot 4.1 upgrade was not responsible was right — a bad redirect registration
fails identically on both Boot versions. The mechanism was simply misdiagnosed. The tell
is in the UAA log, not the browser:

```
grep -a "did not match" docker/artifacts/compose.log
```

**Two exact URIs are needed per browser-login app**, both already in `uaa/uaa.yml`:

- the `authorization_code` callback — `/login/oauth2/code/<registrationId>`, so
  `/login/oauth2/code/sso` for `authcode` and `/login/oauth2/code/ssoauthorizationcode`
  for `authcode-client-credentials`
- the app root, with and without a trailing slash — `UaaLogoutSuccessHandler` sends the
  browser to UAA's `/logout.do?client_id=…&redirect=http://<host>:<port>` (path stripped),
  and UAA validates that `redirect` against the *same* client list. Register only the
  callback and login passes but `#logout` fails.

The `/**` entries were left in place alongside the exact ones, harmlessly, for older UAA
versions that do honour them.

`journeys/src/test/resources/uaa-customizations.yml` — the fixture the Concourse flow
merges into UAA's own config — had the identical defect on `localhost` and got the same
fix. Note that today's pipeline pins UAA to `branch: master`, so it was very likely
affected too; that has not been re-verified here.

## CI

`.github/workflows/ci.yml` runs both test layers on pushes and pull requests
targeting `spring-boot-4.1`, and on manual dispatch:

- **`unit-tests`** — `./gradlew build` on JDK 17, covering the four sample app
  modules in the root `settings.gradle`. `journeys` is excluded from that build on
  purpose (it needs Selenium and a browser), so it runs in the job below instead.
- **`e2e-tests`** — brings this compose harness up on the runner, then runs all 12
  journeys against it in two separately-reported steps: the `client_credentials`
  journeys, then the browser-login ones. Both gate the job. Both steps' JUnit reports
  and the container logs are uploaded as the `e2e-artifacts` bundle.

The split is purely for signal granularity — a failure in the first step points at
token/resource handling, one in the second at the browser-driven OIDC login.

Docker Hub pulls from GitHub-hosted runners are anonymous and therefore rate-limited;
if the UAA/Selenium image pulls start failing with 429s, add a `docker/login-action`
step with a Docker Hub token.

The Concourse pipeline (`ci/pipeline.yml`, `ci/tasks/journeys/task.sh`) is left
untouched — it orchestrates the same journeys suite bare-metal, independently of this
harness. Separately: today's pipeline pins UAA to `branch: master`
(bleeding-edge) — the same root cause that produced the Java 25 dual-JDK conflict
this harness avoids by pinning `v78.16.0`. Whenever the pipeline is revisited, it
should be repointed at a `v78.*` tag rather than `master`/`develop`.
