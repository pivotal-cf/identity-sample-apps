#!/usr/bin/env bash
set -euo pipefail

wait_for() {
  local host="$1" port="$2"
  until (exec 3<>"/dev/tcp/${host}/${port}") 2>/dev/null; do
    echo "waiting for ${host}:${port}..."
    sleep 2
  done
  exec 3<&- 3>&- || true
}

wait_for uaa 8080
wait_for resource-server 8889
wait_for authcode 8888
wait_for client-credentials 8887
wait_for authcode-client-credentials 8890

exec "$@"
