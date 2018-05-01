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
    (
        echo ">>> Sample resource_server app is starting"
        export SPRING_APPLICATION_JSON='{"authDomain": "http://localhost:8080"}'
        ./gradlew --project-cache-dir="${workspace_dir}/.gradle" -p resource_server clean bootRun &
        until nc -vz localhost 8889 >/dev/null 2>&1; do
            echo ">>> Waiting for resource_server to start"
            sleep 15
        done
        echo ">>> Sample resource_server app is up"
    )

    (
        export SPRING_APPLICATION_JSON='{"resourceServerUrl": "http://localhost:8889"}'
        export VCAP_SERVICES="$(cat journeys/src/test/resources/vcap_services.json)"
        export VCAP_APPLICATION="$(cat journeys/src/test/resources/vcap_application.json)"
        echo ">>> Sample authorization_code app is starting"
        ./gradlew --project-cache-dir="${workspace_dir}/.gradle" -p authorization_code clean bootRun &
        until nc -vz localhost 8888 >/dev/null 2>&1; do
            echo ">>> Waiting for authorization_code to start"
            sleep 15
        done
        echo ">>> Sample authorization_code app is up"
    )

    ./gradlew --project-cache-dir="${workspace_dir}/.gradle" test
    ./gradlew --project-cache-dir="${workspace_dir}/.gradle" build
popd

cp "identity-sample-apps/authorization_code/build/libs/authorization_code.jar" sample-app-jars/authorization_code.jar
cp "identity-sample-apps/resource_server/build/libs/resource_server.jar" sample-app-jars/resource_server.jar