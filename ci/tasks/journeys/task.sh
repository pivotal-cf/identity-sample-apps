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
        echo ">>> Sample resource-server app is starting"
        export SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUERURI="http://localhost:8080/uaa/oauth/token"
        export SPRING_APPLICATION_JSON='{"server.port": 8889}'
        ./gradlew --project-cache-dir="${workspace_dir}/.gradle" -p resource-server clean bootRun &
        until nc -vz localhost 8889 >/dev/null 2>&1; do
            echo ">>> Waiting for resource-server to start"
            sleep 15
        done
        echo ">>> Sample resource-server app is up"
    )

    (
        export RESOURCE_URL="http://localhost:8889"
        export SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_SSO_AUTHORIZATIONGRANTTYPE="authorization_code"
        export SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_SSO_SCOPE="openid, email, profile, roles, user_attributes, todo.read, todo.write"
        export SPRING_APPLICATION_JSON='{"server.port": 8888}'
        export VCAP_SERVICES="$(cat journeys/src/test/resources/vcap_services.json)"
        export VCAP_APPLICATION="$(cat journeys/src/test/resources/vcap_application.json)"
        echo ">>> Sample authcode app is starting"
        ./gradlew --project-cache-dir="${workspace_dir}/.gradle" -p authcode clean bootRun &
        until nc -vz localhost 8888 >/dev/null 2>&1; do
            echo ">>> Waiting for authcode to start"
            sleep 15
        done
        echo ">>> Sample authcode app is up"
    )

#    echo ">>>> ABOUT TO GO TO SLEEP"
#    sleep 1800

    ./gradlew --project-cache-dir="${workspace_dir}/.gradle" test
    ./gradlew --project-cache-dir="${workspace_dir}/.gradle" build  #TODO: is this build step necessary? won't the test step have already built the jars?
popd

cp "identity-sample-apps/resource-server/build/libs/resource-server.jar" sample-app-jars/resource-server.jar
cp "identity-sample-apps/authcode/build/libs/authcode.jar" sample-app-jars/authcode.jar
cp "identity-sample-apps/client-credentials/build/libs/client-credentials.jar" sample-app-jars/client-credentials.jar
