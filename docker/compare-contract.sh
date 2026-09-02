#!/usr/bin/env bash
#
# Compare every reachable endpoint of a candidate build against a reference build,
# to prove an upgrade did not change the apps' HTTP contract.
#
# Both stacks must already be running: the candidate on the standard ports and the
# reference on those ports minus a fixed offset. See docker/README.md for how the
# Spring Boot 4.1 upgrade was verified with this.
#
#   ./docker/compare-contract.sh
#
# Ports may be overridden with NEW_PORTS / OLD_PORTS (space-separated, in the order
# authcode client-credentials authcode-client-credentials resource-server).
set -uo pipefail

read -r -a NEWP <<< "${NEW_PORTS:-8888 8887 8890 8889}"
read -r -a OLDP <<< "${OLD_PORTS:-8898 8897 8896 8899}"

PATHS_AUTHCODE="/ /info /todos /todos/"
PATHS_CC="/ /info /todos /todos/"
PATHS_ACC="/ /info /user/todos /client/todos"
PATHS_RS="/todos /todos/ /.well-known/oauth-protected-resource /actuator/health"

# Values that legitimately differ between two runs: per-request JWT claims, session
# ids, CSRF tokens and generated todo ids. Everything else must match exactly.
normalise() {
  sed -E \
    -e "s/$1/PORT/g" \
    -e 's/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/UUID/g' \
    -e 's/value="[A-Za-z0-9_-]{20,}"/value="TOKEN"/g' \
    -e 's/[A-Za-z0-9_-]{40,}/BLOB/g' \
    -e 's/\b[0-9]{10}\b/EPOCH/g' \
    -e 's/\b[0-9a-f]{32}\b/HEX32/g' \
    -e 's/\b[0-9a-f]{7,8}\b/HEX/g'
}

header() { grep -i "^$2:" "$1" | sed -E "s/$3/PORT/g" | tr -d '\r'; }

fail=0
for i in 0 1 2 3; do
  n=${NEWP[$i]}; o=${OLDP[$i]}
  case $i in
    0) paths=$PATHS_AUTHCODE ;;
    1) paths=$PATHS_CC ;;
    2) paths=$PATHS_ACC ;;
    3) paths=$PATHS_RS ;;
  esac
  for p in $paths; do
    ns=$(curl -s -o /tmp/_nb -D /tmp/_nh -w '%{http_code}' "http://localhost:$n$p")
    os=$(curl -s -o /tmp/_ob -D /tmp/_oh -w '%{http_code}' "http://localhost:$o$p")
    d=""
    [ "$ns" != "$os" ] && d="$d status($os->$ns)"
    for h in location www-authenticate content-type; do
      nv=$(header /tmp/_nh "$h" "$n"); ov=$(header /tmp/_oh "$h" "$o")
      [ "$nv" != "$ov" ] && d="$d $h($ov -> $nv)"
    done
    normalise "$n" < /tmp/_nb > /tmp/_nbn; normalise "$o" < /tmp/_ob > /tmp/_obn
    cmp -s /tmp/_nbn /tmp/_obn || d="$d body"
    if [ -n "$d" ]; then echo "DIFF  :$n$p ->$d"; fail=1; else echo "same  :$n$p  [$ns]"; fi
  done
done

echo
if [ $fail -eq 0 ]; then echo "All endpoints identical."; else echo "Differences found - see DIFF lines above."; fi
exit $fail
