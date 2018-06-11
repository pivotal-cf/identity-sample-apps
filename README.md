# Sample Applications for Pivotal Single Sign-On

The authorization_code app serves the following endpoints:

Endpoint | Description | Required scopes
-------- | ----------- | ----------------
/secured/access_token | Displays your JWT access token | at least 1 scope
/secured/userinfo | Displays your openid userinfo | `openid`
/secured/abc | Displays a resource requiring a custom scope | `acme.abc`
/secured/xyz | Displays a resource requiring a custom scope | `acme.xyz`

## Authorization Code Grant Type

To push to Pivotal Cloud Foundry:

### PCF Prerequisites

- Access to a PCF environment with the following installed
  - Pivotal Cloud Foundry Ops Manager 2.0
  - Pivotal Application Service 2.0.x
  - Pivotal Single Sign-On 1.6.0
  - Check your environment like this:
  
    ```
    $ cf api api.example.com
    Setting api endpoint to api.sys.example.com...
    OK
    
    api endpoint:   https://api.sys.example.com
    api version:    2.82.0
    
    $ cf login
    API endpoint: https://api.sys.example.com
    
    Email> admin
    
    Password>
    Authenticating...
    OK
    
    Targeted org system
    
    Targeted space identity-service-space
    
    
    
    API endpoint:   https://api.sys.example.com (API version: 2.103.0)
    User:           admin
    Org:            system
    Space:          identity-service-space
    
    $ cf marketplace
    Getting services from marketplace in org sample-org / space sample-space as admin...
    OK
    
    service      plans              description
    p-identity   sample-plan, uaa   Provides identity capabilities via UAA as a Service
    
    TIP:  Use 'cf marketplace -s SERVICE' to view descriptions of individual plans of a given service.
    ```
    
- Access to an `Org` and `Space` to deploy these sample applications (e.g. `sample-org` & `sample-space`)

```
cf create-org sample-org
cf target -o sample-org
cf create-space sample-space
cf target -s sample-space
```

- Access to a Single Sign-On `Plan` (e.g. `sample-plan`)  
   - Create a new Single Sign-On `Plan` in the Single Sign-On dashboard; usually at https://p-identity.sys.example.com/
 
### PCF Deployment Steps

1. Build all samples

```
./gradlew clean build -x test
```

2. Push `resource_server`

```
cf push resource_server --no-start -p resource_server/build/libs/resource_server-0.0.1-SNAPSHOT.jar
cf set-env resource_server SPRING_APPLICATION_JSON '{"authDomain":"https://sample-plan.login.sys.example.com"}'
cf start resource_server
```

NOTE: The `cf start` above will fail if the UAA has a self-signed SSL certificate. Our recommendation is to use an SSL
certificate signed by a trusted CA instead.

3. Push `authorization_code`

```
cf push authorization_code --no-start -p authorization_code/build/libs/authorization_code-0.0.1-SNAPSHOT.jar
cf create-service p-identity sample-plan sample-plan-service-instance
cf bind-service authorization_code sample-plan-service-instance \
  -c '{"resources":{"acme.abc":"ABC","acme.xyz":"XYZ"},"scopes":["openid","acme.abc","acme.xyz"],"authorities":["uaa.resource"],"redirect_uris":["https://authorization-code.sfo.identity.team/**"]}'
cf set-env authorization_code SPRING_APPLICATION_JSON '{"resourceServerUrl":"https://resource_server.apps.example.com"}'
cf start authorization_code
```

NOTE: The `cf start` above will fail if the UAA has a self-signed SSL certificate. Our recommendation is to use an SSL
certificate signed by a trusted CA instead.

4. Create a user

```
uaac --zone sample-plan user add sample-user --password sample-password --emails sample-user@example.com
uaac --zone sample-plan member add acme.abc sample-user
uaac --zone sample-plan member add acme.xyz sample-user
```

### Local Development

UAA configuration can be done by running the following commands (requires the [`yq`](https://yq.readthedocs.io/en/latest/) command for manipulating yaml):

1. Clone UAA and identity-sample-apps projects

```
git clone https://github.com/cloudfoundry/uaa.git
git clone https://github.com/pivotal-cf/identity-sample-apps.git
```

2. Configure UAA for identity-sample-apps

```
yq merge --inplace uaa/uaa/src/main/resources/uaa.yml identity-sample-apps/journeys/src/test/resources/uaa-customizations.yml
```

3. Start UAA:

```
pushd /path/to/uaa
    ./gradlew run
popd
```

4. Build samples:

```
./gradlew clean build -x test
```

5. Start `resource_server`

```
pushd /path/to/identity-sample-apps
    export SPRING_APPLICATION_JSON='{"authDomain":"http://localhost:8080/uaa"}'
    ./gradlew -p resource_server clean bootRun
popd
```

6. Start `authorization_code`

```
pushd /path/to/identity-sample-apps
    export VCAP_SERVICES="$(cat journeys/src/test/resources/vcap_services.json)"
    export VCAP_APPLICATION="$(cat journeys/src/test/resources/vcap_application.json)"
    export SPRING_APPLICATION_JSON='{"resourceServerUrl":"http://localhost:8889"}'
    ./gradlew -p authorization_code clean bootRun
popd
```

Now you can visit the server at `http://localhost:8888/secured/access_token`

---

#### Running in Unsafe Environment with Self-signed Certificates

⚠️⚠️⚠️ **WARNING** ⚠️⚠️⚠️ Do not use the following steps in your production environments; instead, use trusted certificates within your environment.

If necessary to push the sample apps to an unsafe environment with self-signed certificates, you can add the [cloudfoundry-certificate-truster](https://github.com/pivotal-cf/cloudfoundry-certificate-truster) dependency to the gradle file. Follow the instructions from the cloudfoundry-certificate-truster readme.
