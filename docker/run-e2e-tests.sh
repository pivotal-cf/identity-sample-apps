#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")"

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

# Pinned to match the journeys module's Selenium Java client version (4.16.1) exactly --
# newer browser/grid images paired with this older client can cause findElement calls to
# hang/fail after cross-origin navigation (observed with :latest during harness development).
case "$(uname -m)" in
  arm64|aarch64)
    export SELENIUM_IMAGE="${SELENIUM_IMAGE:-seleniarm/standalone-chromium:120.0-chromedriver-120.0-grid-4.16.1-20231230}"
    ;;
  *)
    export SELENIUM_IMAGE="${SELENIUM_IMAGE:-selenium/standalone-chrome:120.0-chromedriver-120.0-grid-4.16.1-20231230}"
    ;;
esac

ARTIFACTS_DIR="${ARTIFACTS_DIR:-./artifacts}"
mkdir -p "$ARTIFACTS_DIR"

cleanup() {
  local status=$?
  echo ">>> Dumping compose logs to $ARTIFACTS_DIR/compose.log"
  "${COMPOSE[@]}" logs --no-color > "$ARTIFACTS_DIR/compose.log" 2>&1 || true
  echo ">>> Tearing down"
  "${COMPOSE[@]}" down -v --remove-orphans || true
  exit $status
}
trap cleanup EXIT

echo ">>> Building and starting uaa + sample apps + selenium"
"${COMPOSE[@]}" up -d --build uaa resource-server authcode client-credentials authcode-client-credentials selenium

echo ">>> Running journeys test suite"
"${COMPOSE[@]}" --profile test run --rm journeys-runner
