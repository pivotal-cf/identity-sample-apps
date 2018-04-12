#!/bin/bash
set -euo pipefail
IFS=$'\n\t'

workspace_dir="$(pwd)"

yq merge --inplace uaa/uaa/src/main/resources/uaa.yml \
                   identity-sample-apps/journeys/src/test/resources/uaa-customizations.yml

pushd "uaa"
    echo ">>> UAA is starting"
    ./gradlew --project-cache-dir="${workspace_dir}/.gradle" run &
    until nc -vz localhost 8080 >/dev/null 2>&1; do
       echo ">>> Waiting for UAA to start"
       sleep 15
    done
    echo ">>> UAA is up"
popd


pushd "identity-sample-apps"
    export VCAP_SERVICES="$(cat journeys/src/test/resources/vcap_services.json)"
    export VCAP_APPLICATION="$(cat journeys/src/test/resources/vcap_application.json)"

    echo ">>> Sample app is starting"
    ./gradlew --project-cache-dir="${workspace_dir}/.gradle" -p authorization_code clean bootRun &
    until nc -vz localhost 8888 >/dev/null 2>&1; do
        echo ">>> Waiting for Authcode to start"
        sleep 15
    done
    echo ">>> Sample app is up"

    ./gradlew --project-cache-dir="${workspace_dir}/.gradle" test
    ./gradlew --project-cache-dir="${workspace_dir}/.gradle" build
popd

cp "identity-sample-apps/authorization_code/build/libs/authorization_code-0.0.1-SNAPSHOT.jar" sample-app-jars/authorization_code.jar