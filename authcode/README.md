# Deploying the Authorization Code (Authcode) Sample Application

##Introduction

This sample application integrates with the [UAA](https://github.com/cloudfoundry/uaa) using the [authorization code](https://tools.ietf.org/html/rfc6749#section-4.1) 
OAuth2 grant type. This sample application relies on the [Pivotal Single Sign-On Service](https://docs.pivotal.io/p-identity/index.html)
to automatically register this sample application as an OAuth2 client of the UAA and the 
[SSO CFEnv Processor](https://github.com/pivotal-cf/java-cfenv/tree/master/java-cfenv-boot-pivotal-sso) to automatically consume those configurations.

App-specific OAuth2 client configurations are made using the environment variables section of the sample app's [`manifest.yml`](./manifest.yml) 
file.

##Use Case for Using Authcode

The Authorization Code OAuth2 grant type is most commonly used for web applications which:

1. Requires a human user to authenticate for authentication and/or authorization protection of your app's endpoints
   and/or to access other services using the identity and the level of authorization permissions of that user.
1. Have a backend which can receive the auth code and exchange it for a access token. In this sample app,
   the Spring Security library performs this step.

## Prerequisites:

1. An operator must have installed the [Pivotal Single Sign-On Service](https://docs.pivotal.io/p-identity/index.html)
1. An operator must have [configured at least one plan](https://docs.pivotal.io/p-identity/manage-service-plans.html) for the SSO Service that is visible to your Org.
1. The person using this sample app must know login credentials for a user in this plan. For new plans, an operator may need to [create a user](http://docs.pivotal.io/p-identity/manage-users.html).

### Step 0: Deploy a sample resource server

The goal of applications obtaining tokens using the OAuth2 authcode grant type is to be able to use those tokens to perform privileged 
actions on another service fulfilling the role of a resource server.  This sample authcode application is meant to obtain tokens for use with
the [sample resource server application](../resource-server) which implements a simple TODO application.

As a result of this relationship between the authcode client application and the resource server, having pushed a sample resource server 
app is a required prerequesite for working through the rest of this tutorial. [Follow these instructions](../resource-server/README.md) to 
deploy a sample resource server if you have not already done so.

### Step 1: Create an identity service instance

Using the CF CLI, login and target the space where you'd like the sample app to reside.

Using the plan created as part of the Prerequisites, create a service instance in your space

    cf create-service p-identity <plan-name> p-identity-instance

### Step 2: Update `authcode/manifest.yml` with the location of the sample resource server

The [`manifest.yml`](./manifest.yml) includes [a configuration block](https://docs.cloudfoundry.org/devguide/deploy-apps/manifest.html#env-block) 
called `env`. This section is used to list environment variables that will be available to the deployed application.

In the `env` section of [`manifest.yml`](./manifest.yml), you must update the value of `RESOURCE_URL` with the location of your deployed 
resource server application. Replace `RESOURCE_URL: https://resource-server-sample.<your-domain>.com` with a real url.

NOTE: You must leave off the trailing slash (`/`) in the `RESOURCE_URL`.

### Step 3: Update `authcode/manifest.yml` with the name of your identity service instance

The [`manifest.yml`](./manifest.yml) includes [a configuration block](https://docs.cloudfoundry.org/devguide/deploy-apps/manifest.html#services-block) 
called `services`. Your app will be bound to any service instances you list in this section when it is pushed.

Make sure that the `services` block includes the name of the service instance created in Step 1.

### Step 4: Deploy Sample Application to Pivotal Cloud Foundry
    
Build the jar for our sample application:

    ./gradlew clean build
    
This should result in the creation of an artifact `build/libs/authcode.jar`. Next push the authcode sample app:

    cf push --random-route

Running `cf push` should result in
 
  - The app being bound to the p-identity service instance, which results in the creation of a new client registration for the sample app in the UAA.
  - The OAuth client id and client secret from the UAA are provided to your application through the `VCAP_SERVICES` environment variable. You can view these values yourself with `cf env authcode-sample`.
  - When the app starts, the spring-cloud-sso-connector reads `VCAP_SERVICES` and translates configuration from `p-identity` into the configuration needed by `org.springframework.security.oauth` to make the sample application OAuth-aware.

You can verify the app is successfully running by viewing the output of `cf apps`. You can visit the authcode app by navigating to `https://<authcode-app-url>` where `authcode-app-url` is the route output from `cf push` or `cf apps`.

# Bootstrap Application Client Configurations for the Pivotal Single Sign-On Service Instance
Beginning in SSO 1.4.0, you can use the following values your application's manifest to bootstrap client configurations for your applications automatically when binding or rebinding your application to the service instance. These values will be automatically populated to the client configurations for your application through CF environment variables.

The [SSO Documentation](https://docs.pivotal.io/p-identity/configure-apps/index.html#configure-app-manifest) provides descriptions and default values for these SSO properties. Further details and examples are provided in the sample application manifests.

When you specify your own `SSO_SCOPES` and `SSO_AUTHORITIES` values, consider including `openid` for Authorization Code grant type applications, and `uaa.resource` for Client Credentials grant type applications, as these will not be provided if they are not specified.

To remove any variables set through bootstrapping, you must use `cf unset-env <APP_NAME> <PROPERTY_NAME>` and rebind the application.

## Troubleshooting

#### Scenario:

You have received an error during `cf push`, or an explict `cf bind-service` call, to the SSO Service that looks like this:

```
Binding Failure:
Creating app authcode-sample...
Mapping routes...
Binding services...
Unexpected Response
Response code: 502
CC code:       0
CC error code:
Request ID:    78c53a54-8e03-4922-4833-8b51156e6078::fd0c88e1-8e2e-4eb3-85db-cfea8e078e01
Description:   {
  "description": "Service broker error: The resource name \"todo\" already exists in another space. Please enter a unique resource name before saving",
  "error_code": "CF-ServiceBrokerBadResponse",
  "code": 10001,
  "http": {
    "uri": "https://p-identity-broker.<CF_DOMAIN>/v2/service_instances/bc9a7563-e8b0-4dce-aef2-d7d50b486d9d/service_bindings/80adcbdf-7832-4d45-a58f-76fde5056c8f",
    "method": "PUT",
    "status": 500
  }
}
```

This means that `todo` resource has already been created on the same SSO service plan in a different space. For more information see the [SSO Documentation](https://docs.pivotal.io/p-identity/manage-resources.html#space-protection).

#### Solution:

1. Comment out the `SSO_RESOURCES` in the `manifest.yml`

1. Unset the `SSO_RESOURCES` env var on the deployed authcode-sample application.
     ```
     cf unset-env authcode-sample SSO_RESOURCES
     ```
1. Push the authcode-sample application again.
     ```
     cf push
     ```   
     
Removing the `SSO_RESOURCES` env var will result in a call to bind the application to the SSO Service Instance without an attempt to create any additional Groups in UAA. However, the SSO Client created for your application will be allowed to use these Groups and Scopes. The User will still have to have memberships to the `todo.read` and `todo.write` Groups to receive a token with those scopes.
