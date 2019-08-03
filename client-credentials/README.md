# Deploying the Client Credentials Sample Application

##Introduction

This sample application integrates with the [UAA](https://github.com/cloudfoundry/uaa) using the [Client Credentials](https://tools.ietf.org/html/rfc6749#section-4.4) 
OAuth2 grant type. This sample application relies on the [Pivotal Single Sign-On Service](https://docs.pivotal.io/p-identity/index.html) 
to automatically register this sample application as an OAuth2 client of the UAA and the 
[SSO CFEnv Processor](https://github.com/pivotal-cf/java-cfenv/tree/master/java-cfenv-boot-pivotal-sso) to automatically consume those configurations.

App-specific OAuth2 client configurations are made using the environment variables section of the sample app's [`manifest.yml`](./manifest.yml) 
file.

##Use Case for Using Client Credentials

The Client Credentials OAuth2 grant type is most commonly used for web applications which:

1. Do not require a human user to authenticate
1. The backend needs to authenticate as itself, not on behalf of any particular human user,
   to perform requests to another service (service-to-service auth).

## Prerequisites:

1. An operator must have installed the [Pivotal Single Sign-On Service](https://docs.pivotal.io/p-identity/index.html)
1. An operator must have [configured at least one plan](https://docs.pivotal.io/p-identity/manage-service-plans.html) for the SSO Service that is visible to your Org.

### Step 0: Deploy a sample resource server

The goal of applications obtaining tokens using the OAuth2 Client Credentials grant type is to be able to use those tokens to perform privileged 
actions on another service fulfilling the role of a resource server.  This sample client-credentials application is meant to obtain tokens for use with
the [sample resource server application](../resource-server) which implements a simple TODO application.

As a result of this relationship between the client-credentials application and the resource server, having pushed a sample resource server 
app is a required prerequesite for working through the rest of this tutorial. [Follow these instructions](../resource-server/README.md) to 
deploy a sample resource server if you have not already done so.

### Step 1: Create an identity service instance

Using the CF CLI, login and target the space where you'd like the sample app to reside.

Using the plan created as part of the Prerequisites, create a service instance in your space if you have not done so already

    cf create-service p-identity <plan-name> p-identity-instance

### Step 2: Update `client-credentials/manifest.yml` with the location of the sample resource server

The [`manifest.yml`](./manifest.yml) includes [a configuration block](https://docs.cloudfoundry.org/devguide/deploy-apps/manifest.html#env-block) 
called `env`. This section is used to list environment variables that will be available to the deployed application.

In the `env` section of [`manifest.yml`](./manifest.yml), you must update the value of `RESOURCE_URL` with the location of your deployed 
resource server application. Replace `RESOURCE_URL: https://resource-server-sample.<your-domain>.com` with a real url.

NOTE: You must leave off the trailing slash (`/`) in the `RESOURCE_URL`.

### Step 3: Update `client-credentials/manifest.yml` with the name of your p-identity service instance

The [`manifest.yml`](./manifest.yml) includes [a configuration block](https://docs.cloudfoundry.org/devguide/deploy-apps/manifest.html#services-block) 
called `services`. Your app will be bound to any service instances you list in this section when it is pushed.

Make sure that the `services` block includes the name of the service instance created in Step 1.

### Step 4: Deploy Sample Application to Pivotal Cloud Foundry
    
Build the jar for our sample application:

    ./gradlew clean build
    
This should result in the creation of an artifact `build/libs/client-credentials.jar`. Next push the client-credentials sample app:

    cf push --random-route

Running `cf push` should result in
 
  - The app being bound to the p-identity service instance, which results in the creation of a new client registration for the sample app in the UAA.
  - The OAuth client id and client secret from the UAA are provided to your application through the `VCAP_SERVICES` environment variable. You can view these values yourself with `cf env client-credentials-sample`.
  - When the app starts, the SSO CFEnv Processor reads `VCAP_SERVICES` and translates configuration from `p-identity` into the configuration needed by `org.springframework.security.oauth` to make the sample application OAuth-aware.

You can verify the app is successfully running by viewing the output of `cf apps`. You can visit the client-credentials app by navigating to `https://<client-credentials-app-url>` where `client-credentials-app-url` is the route output from `cf push` or `cf apps`.

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
Creating app client-credentials-sample...
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

This means that the `todo` resource is a cross-space resource, namely, a resource that has already been created on the same SSO service plan in a different space. Since SSO enforces some extra restrictions on using cross-space resource, you have to perform some additional steps as a workaround. For more information see the [SSO Documentation](https://docs.pivotal.io/p-identity/manage-resources.html#space-protection).

#### Solution:

1. Comment out the `SSO_RESOURCES` in the `manifest.yml`

1. Unset the `SSO_RESOURCES` env var on the deployed `client-credentials-sample` application.
     ```
     cf unset-env client-credentials-sample SSO_RESOURCES
     ```
1. Push the `client-credentials-sample` application again.
     ```
     cf push
     ```   
     
Removing the `SSO_RESOURCES` env var will result in a call to bind the application to the SSO Service Instance without an attempt to create any additional Groups in UAA. However, the SSO Client created for your application will be allowed to use these Groups and Scopes.

** Important Authorization Followup **

For Client Credentials applications, there will be some additional work to allow your SSO Application access to the proper cross-space `authorities` which will result in the `todo.read` and `todo.wrote` scopes on your token. Client Crednetials `authorities` are handled a bit differently than User permissions that are used to authorize token scopes in the Authorization Code Flow. Since there is no intersection of User permissions involved in the Client Credentials grant type token scopes, you will need SSO Plan Admin intervention to ultimately receive a token with the appropriate cross-space scopes. We have attempted to outline some suggestions on how to correctly authorize your SSO Client Credentials application that was created as a result of this sample app binding to an SSO service.

You may choose one of the follow remediation steps:

* You will need to contact your SSO Plan Administrator to whitelist the Authorites for your SSO Application created from the binding of `client-credentials-sample`. CF System Operator with access to OpsManager can manage and identify SSO Plan Admistrators through the [SSO Service Plans UI](https://docs.pivotal.io/p-identity/manage-service-plans.html#create-svc-plan). An SSO Plan Administrator will then need to navigate to the [SSO Dev Dashboard](https://docs.pivotal.io/p-identity/manage-service-instances.html#access-svc-instance-developer-dashboard), navigate to the SSO Application with the name `client-credentails-sample` and then check the `todo.read` and `todo.write` checkboxes.
* Ask a CF System Operator with access to OpsManager to create a new SSO Service Plan that you can reference in a `create-service` command to give you a clean namespace for resources.
