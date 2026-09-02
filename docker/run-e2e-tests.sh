#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")"

usage() {
  cat >&2 <<'EOF'
usage: run-e2e-tests.sh [command] [args...]

With no command, runs the full local cycle: up, test, logs, down.

Commands (for callers that need the stack to outlive a single test run, e.g. CI
running several filtered test passes and reporting them as separate steps):

  up                  build and start uaa + the four sample apps + selenium
  test [gradle args]  run the journeys suite against the running stack; extra
                      args are appended to `gradle test` (e.g. --tests filters).
                      Set RESULTS_NAME to name this run's artifacts subdirectory.
  logs                dump compose logs to $ARTIFACTS_DIR/compose.log
  down                tear the stack down

Environment:
  ARTIFACTS_DIR   where logs and journeys reports are written (default ./artifacts)
  RESULTS_NAME    artifacts subdirectory for a `test` run's reports (default journeys)
  SELENIUM_IMAGE  override the pinned browser image
EOF
  exit 2
}

# COMPOSE_PROVIDER forces a provider instead of autodetecting. Worth setting in CI:
# GitHub-hosted runners ship podman *and* a docker-compose binary, so `podman compose`
# satisfies the probe below and would be preferred over the runner's first-class Docker.
case "${COMPOSE_PROVIDER:-auto}" in
  docker)         COMPOSE=(docker compose) ;;
  docker-compose) COMPOSE=(docker-compose) ;;
  podman)         COMPOSE=(podman compose) ;;
  podman-compose) COMPOSE=(podman-compose) ;;
  auto)
    if command -v podman >/dev/null 2>&1 && podman compose version >/dev/null 2>&1; then
      COMPOSE=(podman compose)
    elif command -v podman-compose >/dev/null 2>&1; then
      COMPOSE=(podman-compose)
    elif command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
      COMPOSE=(docker compose)
    elif command -v docker-compose >/dev/null 2>&1; then
      COMPOSE=(docker-compose)
    else
      echo "No podman-compose/docker-compose provider found on PATH" >&2
      exit 1
    fi
    ;;
  *)
    echo "Unknown COMPOSE_PROVIDER '$COMPOSE_PROVIDER' (want: docker|docker-compose|podman|podman-compose|auto)" >&2
    exit 1
    ;;
esac
echo ">>> compose provider: ${COMPOSE[*]} ($("${COMPOSE[@]}" version 2>&1 | head -1))"

# Pinned to match the journeys module's Selenium Java client version (4.16.1) exactly --
# newer browser/grid images paired with this older client can cause findElement calls to
# hang/fail after cross-origin navigation (observed with :latest during harness development).
#
# Both use the `<grid-version>-<date>` tag form. Do NOT copy a date suffix from one repo to
# the other: the two publish on independent dates (hence 20231219 vs 20231230), and
# seleniarm additionally offers a longer `<chrome>-chromedriver-<v>-grid-<v>-<date>` form
# that selenium/standalone-chrome does not -- borrowing that shape for the x86 image
# produced a `manifest unknown` pull failure that only showed up on CI, since developers
# on Apple Silicon never take this branch.
case "$(uname -m)" in
  arm64|aarch64)
    export SELENIUM_IMAGE="${SELENIUM_IMAGE:-seleniarm/standalone-chromium:4.16.1-20231230}"
    ;;
  *)
    export SELENIUM_IMAGE="${SELENIUM_IMAGE:-selenium/standalone-chrome:4.16.1-20231219}"
    ;;
esac

ARTIFACTS_DIR="${ARTIFACTS_DIR:-./artifacts}"
mkdir -p "$ARTIFACTS_DIR"

stack_up() {
  echo ">>> Building and starting uaa + sample apps + selenium"
  "${COMPOSE[@]}" up -d --build uaa resource-server authcode client-credentials authcode-client-credentials selenium
}

# Runs the journeys suite in a throwaway container against the already-running stack.
# The journeys module's whole build directory is bind-mounted out so the JUnit XML and
# HTML reports survive the container being removed (--rm) and can be uploaded by CI.
run_tests() {
  # A bind mount needs an absolute source path, so resolve a relative ARTIFACTS_DIR
  # against this script's directory while leaving an absolute one alone.
  local results_dir
  case "$ARTIFACTS_DIR" in
    /*) results_dir="$ARTIFACTS_DIR/${RESULTS_NAME:-journeys}" ;;
    *)  results_dir="$(pwd)/${ARTIFACTS_DIR#./}/${RESULTS_NAME:-journeys}" ;;
  esac
  mkdir -p "$results_dir"

  # Don't ask compose for a TTY when there isn't one to hand out (CI), which it
  # otherwise tries to allocate and fails on. Seeded with --rm rather than left
  # empty so expanding it stays safe under `set -u` on bash 3.2 (macOS).
  local run_flags=(--rm)
  [ -t 1 ] || run_flags+=(-T)

  echo ">>> Running journeys test suite${*:+ ($*)}"
  "${COMPOSE[@]}" --profile test run "${run_flags[@]}" \
    -v "$results_dir:/workspace/journeys/build" \
    journeys-runner gradle --no-daemon test "$@"
}

dump_logs() {
  echo ">>> Dumping compose logs to $ARTIFACTS_DIR/compose.log"
  "${COMPOSE[@]}" logs --no-color > "$ARTIFACTS_DIR/compose.log" 2>&1 || true
}

stack_down() {
  echo ">>> Tearing down"
  "${COMPOSE[@]}" down -v --remove-orphans || true
}

case "${1:-}" in
  up)
    stack_up
    ;;
  test)
    shift
    run_tests "$@"
    ;;
  logs)
    dump_logs
    ;;
  down)
    stack_down
    ;;
  "")
    cleanup() {
      local status=$?
      dump_logs
      stack_down
      exit $status
    }
    trap cleanup EXIT
    stack_up
    run_tests
    ;;
  *)
    usage
    ;;
esac
