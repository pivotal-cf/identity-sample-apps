# Sample Applications for Pivotal Single Sign-On

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
    $ cf create-org sample-org
    Creating org sample-org as admin...
    OK
    
    Assigning role OrgManager to user admin in org sample-org ...
    OK
    
    TIP: Use 'cf target -o "sample-org"' to target new org
    
    $ cf target -o sample-org
    api endpoint:   https://api.sys.example.com
    api version:    2.103.0
    user:           admin
    org:            sample-org
    No space targeted, use 'cf target -s SPACE'
    
    $ cf create-space sample-space
    Creating space sample-space in org sample-org as admin...
    OK
    Assigning role RoleSpaceManager to user admin in org sample-org / space sample-space as admin...
    OK
    Assigning role RoleSpaceDeveloper to user admin in org sample-org / space sample-space as admin...
    OK
    
    TIP: Use 'cf target -o "sample-org" -s "sample-space"' to target new space
    
    $  cf target -s sample-space
    api endpoint:   https://api.sys.example.com
    api version:    2.103.0
    user:           admin
    org:            sample-org
    space:          sample-space
    ```

- Access to a Single Sign-On `Plan` (e.g. `sample-plan`)  
   - Create a new Single Sign-On `Plan` in the Single Sign-On dashboard; usually at https://p-identity.sys.example.com/
 
### PCF Deployment Steps

```
$ ./gradlew clean build -x test
  
BUILD SUCCESSFUL in 4s
8 actionable tasks: 8 executed
  
$ cf push authorization_code --no-start -p authorization_code/build/libs/authorization_code-0.0.1-SNAPSHOT.jar
Pushing app authorization_code to org sample-org / space sample-space as admin...
Getting app info...
Creating app with these attributes...
+ name:       authorization_code
  path:       /path/to/identity-sample-apps/authorization_code/build/libs/authorization_code-0.0.1-SNAPSHOT.jar
  routes:
+   authorizationcode.apps.example.com

Creating app authorization_code...
Mapping routes...
Comparing local files to remote cache...
Packaging files to upload...
Uploading files...
 19.18 MiB / 19.18 MiB [=========================================================================================] 100.00% 1s

Waiting for API to complete processing files...

name:              authorization_code
requested state:   stopped
instances:         0/1
usage:             1G x 1 instances
routes:            authorizationcode.apps.example.com
last uploaded:     Fri 23 Mar 12:02:45 PDT 2018
stack:             cflinuxfs2
buildpack:
start command:

There are no running instances of this app.

$ cf create-service p-identity sample-plan sample-plan-service-instance
Creating service instance sample-plan-service-instance in org sample-org / space sample-space as admin...
OK

$ cf bind-service authorization_code sample-plan-service-instance
Binding service sample-plan-service-instance to app authorization_code in org sample-org / space sample-space as admin...
OK
TIP: Use 'cf restage authorization_code' to ensure your env variable changes take effect

$ cf start authorization_code
...

```

NOTE: The `cf start` above will fail if the UAA has a self-signed SSL certificate (example error below). Our 
recommendation is to use an SSL certificate signed by a trusted CA instead.

### Local Development

UAA configuration can be done by running the following commands (requires the [`yq`](https://yq.readthedocs.io/en/latest/) command for manipulating yaml):

```
git clone https://github.com/cloudfoundry/uaa.git
git clone https://github.com/pivotal-cf/identity-sample-apps.git
yq merge --inplace uaa/uaa/src/main/resources/uaa.yml identity-sample-apps/journeys/src/test/resources/uaa-customizations.yml
```

Then, startup UAA server:

```
pushd /path/to/uaa
    ./gradlew run
popd
```

and finally the auth server:

```
pushd /path/to/identity-sample-apps
    export VCAP_SERVICES="$(cat journeys/src/test/resources/vcap_services.json)"
    export VCAP_APPLICATION="$(cat journeys/src/test/resources/vcap_application.json)"
    ./gradlew -p authorization_code clean bootRun
popd
```

Now you can visit the server at `http://localhost:8888/secured/access_token`

---

#### Running in Unsafe Environment with Self-signed Certificates

⚠️⚠️⚠️ **WARNING** ⚠️⚠️⚠️ Do not use the following steps in your production environments; instead, use trusted certificates within your environment.

If necessary to push the sample apps to an unsafe environment with self-signed certificates, you can add the [cloudfoundry-certificate-truster](https://github.com/pivotal-cf/cloudfoundry-certificate-truster) dependency to the gradle file. Follow the instructions from the cloudfoundry-certificate-truster readme.
