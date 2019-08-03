#!/bin/bash
set -euo pipefail
IFS=$'\n\t'

# Set SKIP_TESTS=true to use this script to run local apps without running tests, so you can run tests in your IDE
SKIP_TESTS=${SKIP_TESTS:-false} # defaults to "false"

workspace_dir="$(pwd)"
uaayml=uaa/uaa/src/main/resources/uaa.yml

if [[ ! -x ${uaayml}.merged ]]; then
    yq merge --inplace ${uaayml} identity-sample-apps/journeys/src/test/resources/uaa-customizations.yml
    # Don't merge again if already merged to allow us to run this script repeatedly on our dev workstations
    touch ${uaayml}.merged
fi

pushd "uaa"
    echo ">>> UAA is starting"
    ./gradlew --console=plain --project-cache-dir="${workspace_dir}/.gradle" run &
    echo ">>> Waiting for UAA to start"
    # UAA isn't ready until its /healthz endpoint returns successfully
    until curl -f http://localhost:8080/uaa/healthz >/dev/null 2>&1; do
       sleep 1
    done
    echo ">>> UAA is up"
popd

pushd "identity-sample-apps"
    (
        echo ">>> Sample resource-server app is starting"
        export SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUERURI="http://localhost:8080/uaa/oauth/token"
        export SPRING_APPLICATION_JSON='{"server.port": 8889}'
        ./gradlew --console=plain --project-cache-dir="${workspace_dir}/.gradle" -p resource-server clean bootRun &
        echo ">>> Waiting for resource-server to start"
        until nc -vz localhost 8889 >/dev/null 2>&1; do
            sleep 1
        done
        echo ">>> Sample resource-server app is up"
    )

    (
        export RESOURCE_URL="http://localhost:8889"
        export SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_SSO_SCOPE="openid, email, profile, roles, user_attributes, todo.read, todo.write"
        export SPRING_APPLICATION_JSON='{"server.port": 8888}'
        export VCAP_SERVICES="$(cat journeys/src/test/resources/vcap_services_authcode.json)"

        echo ">>> Sample authcode app is starting"
        ./gradlew --console=plain --project-cache-dir="${workspace_dir}/.gradle" -p authcode clean bootRun &
        echo ">>> Waiting for authcode to start"
        until nc -vz localhost 8888 >/dev/null 2>&1; do
            sleep 1
        done
        echo ">>> Sample authcode app is up"
    )

    (
        export RESOURCE_URL="http://localhost:8889"
        export SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_SSO_SCOPE="uaa.resource, todo.read, todo.write"
        export SPRING_APPLICATION_JSON='{"server.port": 8887}'
        export VCAP_SERVICES="$(cat journeys/src/test/resources/vcap_services_client_credentials.json)"

        echo ">>> Sample client-credentials app is starting"
        ./gradlew --console=plain --project-cache-dir="${workspace_dir}/.gradle" -p client-credentials clean bootRun &
        echo ">>> Waiting for client-credentials to start"
        until nc -vz localhost 8887 >/dev/null 2>&1; do
            sleep 1
        done
        echo ">>> Sample client-credentials app is up"
    )

    (
        export RESOURCE_URL="http://localhost:8889"
        export SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_SSOCLIENTCREDENTIALS_SCOPE="uaa.resource, todo.read, todo.write"
        export SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_SSOAUTHORIZATIONCODE_SCOPE="openid, email, profile, roles, user_attributes, todo.read, todo.write"
        export SPRING_APPLICATION_JSON='{"server.port": 8890}'
        export VCAP_SERVICES="$(cat journeys/src/test/resources/vcap_services_multi_grant_authcode_client_credentials.json)"

        echo ">>> Sample authcode-client-credentials app is starting"
        ./gradlew --console=plain --project-cache-dir="${workspace_dir}/.gradle" -p authcode-client-credentials clean bootRun &
        echo ">>> Waiting for authcode-client-credentials to start"
        until nc -vz localhost 8890 >/dev/null 2>&1; do
            sleep 1
        done
        echo ">>> Sample authcode-client-credentials app is up"
    )

popd

if [[ "$SKIP_TESTS" = "false" ]]; then
    pushd "identity-sample-apps"
        ./gradlew --project-cache-dir="${workspace_dir}/.gradle" assemble test
        cp "resource-server/build/libs/resource-server.jar" ../sample-app-jars/resource-server.jar
        cp "authcode/build/libs/authcode.jar" ../sample-app-jars/authcode.jar
        cp "client-credentials/build/libs/client-credentials.jar" ../sample-app-jars/client-credentials.jar
    popd
fi
