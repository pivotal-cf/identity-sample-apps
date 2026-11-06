# Goal

Keep the identity sample apps buildable and their tests passing. Before considering a
code change complete, run both the root unit/build checks and the end-to-end journey
tests on Java 25 when the local environment supports them. If either test layer cannot
be run, state why and report the remaining validation clearly.

## Local validation

- Unit tests and app builds: `./gradlew clean build` from the repository root with Java 25.
- End-to-end tests with Podman: `COMPOSE_PROVIDER=podman ./docker/run-e2e-tests.sh`.
- If host port 8080 is occupied, set `UAA_HOST_PORT` to an available port, for example
  `COMPOSE_PROVIDER=podman UAA_HOST_PORT=18080 ./docker/run-e2e-tests.sh`.
- See `README.md` and `docker/README.md` for prerequisites, reports, and troubleshooting.
