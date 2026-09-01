#!/usr/bin/env bash
set -euo pipefail

until curl -sf "http://uaa:8080/uaa/healthz" > /dev/null 2>&1; do
  echo "waiting for uaa..."
  sleep 2
done

if [[ -n "${VCAP_SERVICES_FILE:-}" ]]; then
  export VCAP_SERVICES="$(cat "$VCAP_SERVICES_FILE")"
fi

exec "$@"
