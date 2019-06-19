# Pivotal Single Sign-On Service Sample Applications

This repo holds separate sample applications for each one of the four OAuth 2.0 grant types supported by the Pivotal Single Sign-On Service. The grant type specific environment variables are configured to their relevant values in the manifests of sample application. Each grant type maps to an Application Type as seen in the Pivotal Single Sign-On Service Dashboard. For more information about how to determine SSO Application Type, please read [PCF SSO Documentation](https://docs.pivotal.io/p-identity/determine-type.html).

Application Type  | Grant Type | Uses Spring Cloud SSO starter library
------------- | -------------- | ---------------------
[Web App](/authcode)  | authorization_code | yes
[Service-to-Service App](/client-credentials) | client_credentials | yes
[Resource Server App](/resource-server) | n/a | no

The latest version of this repository supports the following dependencies:

Dependency | Version
------------- | ---------- 
[Spring Boot](https://github.com/spring-projects/spring-boot/tree/2.1.x) | 2.1.1+
[Spring Security](https://github.com/spring-projects/spring-security/tree/5.1.x) | 5.0+ 
[Spring Cloud SSO Starter library](https://github.com/pivotal-cf/java-cfenv/tree/master/java-cfenv-boot-pivotal-sso) | 1.1.0.RELEASE

The last version to support Spring Boot 1.5.5+ is tagged at [spring-boot/1.5](https://github.com/pivotal-cf/identity-sample-apps/releases/tag/spring-boot%2F1.5).

The sample applications for the corresponding grant types are located in subdirectories of this project:  

## Prerequisites

1. Login as a Space Developer into the required Org and Space on your PCF Foundation

       cf login -a api.<your-domain>
        
1. Ensure that an SSO (p-identity) [Service Plan](https://docs.pivotal.io/p-identity/manage-service-plans.html) exists for your Org. Record the name of the plan you wish to select to be used as the `<plan_tier>` value for the next step.

       cf marketplace | grep p-identity

1. Create a [Service Instance](https://docs.pivotal.io/p-identity/manage-service-instances.html) named 'p-identity-instance' from the 'p-identity' service using an available Service Plan

       cf create-service p-identity <plan_tier> p-identity-instance

## <a name="quick-start">Quick Start</a>

You can deploy the authcode and resource server sample applications using application bootstrapping with the steps below. You can read more about these topics in the following sections.

### Deploying Resource Server Sample App

1. Navigate to the *resource-server* directory

1. Update the `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUERURI` <PLAN_AUTH_DOMAIN> and <YOUR_DOMAIN> placeholders in the *resource-service* manifest. 

1. Build (`./gradlew build`) and push (`cf push`) the *resource-server* application. (You may have to use `--random-route` flag when cf pushing your application if a route already exists with your application name.)

### Deploying Authorization Code Sample App

1. Navigate to the *authcode* directory

1. Update the `RESOURCE_URL` value in the *authcode* manifest.yml file to the route of the deployed *resource-server* (which you can find by running `cf apps`).

1. Build (`./gradlew build`) and push (`cf push`) the *authcode* project. (You may have to use `--random-route` flag when cf pushing your application if a route already exists with your application name.) The sample application will be immediately bound to the SSO Service after `cf push`.

### Deploying Client Credentials Sample App

1. Navigate to the *client-credentials* directory

1. Update the `RESOURCE_URL` value in the *client-credentials* manifest.yml file to the route of the deployed *resource-server* (which you can find by running `cf apps`).

1. Build (`./gradlew build`) and push (`cf push`) the *client-credentials* project. (You may have to use `--random-route` flag when cf pushing your application if a route already exists with your application name.) The sample application will be immediately bound to the SSO Service after `cf push`.

## Testing the Sample Apps

1. Preparing a test user with sufficient scopes

     Contact your cloud administrator to determine whether your Service Plan is has configured the "Internal User Store" as an Identity Provider or an external Identity Provider (like LDAP).

     - If your SSO Service plans is configured with the 'Internal User Store' option, you can use the instruction in [Manage Users in an Internal User Store](https://docs.pivotal.io/p-identity/manage-users.html) documentation to create a user to `todo.read` and `todo.write` scopes.

     - If your plan is configured with an alternative Identity Provider (like LDAP), your administrator will need to provide you credentials with memberships to the `todo.read` and `todo.write` scopes.

1. Visit the deployed Authorization Code and Client Credentials sample apps by entering the urls of the apps (which you can find by running `cf apps`). (The Resource Server sample app is a backend API and not intended to be accessed through a browser.)

# Unsupported Grant Types

### Implicit Grant Type:

The Implicit Grant Type is supported by Spring Security 5, but has generally fallen out of favor as an architectural pattern for SPAs. It has been determined that we will not provide Sample Apps to demonstrate this grant type moving forward. The current recommendation for SPAs is to use the Authorization Code Flow in conjuntion with the [Proof Key for Code Exchange](https://tools.ietf.org/html/rfc7636) to protect the Authorization Code in the client's browser. For more information, please see the Okta developers blog article: [Is The OAuth Implict Flow Dead](https://developer.okta.com/blog/2019/05/01/is-the-oauth-implicit-flow-dead#the-oauth-authorization-code-flow-is-better).

### Resource Owner Password Credentials (i.e. Password) Type:

The Resource Owner Password Credentials grant type is no longer supported by Spring Security 5 (see the Grant Type [Support Matrix](projects/spring-security/wiki/OAuth-2.0-Features-Matrix#client-support)). The Password grant type is more commonly used with programs, like CLIs, that are unlikley to be dependendant on Spring or other Web based software libraries. For more information, see the [OAuth 2 Password Grant specification](https://tools.ietf.org/html/rfc6749#section-4.3.2). 

If your use cases require the Password grant type for a Spring application, you will need to implement the access token request on your own. However, if your Java based CF application is bound to an SSO service instance and using the [Spring Boot SSO Starter Library](https://github.com/pivotal-cf/java-cfenv/tree/master/java-cfenv-boot-pivotal-sso), you may find it useful to reference the table of [Spring Security 5 Java properties](https://github.com/pivotal-cf/java-cfenv/tree/master/java-cfenv-boot-pivotal-sso#spring-applications) built from VCAP_SERVICES to help craft your request. 

# Bootstrap Application Client Configurations for the Pivotal Single Sign-On Service Instance
Beginning in SSO 1.4.0, you can set environment variables in your application's manifest to bootstrap client configurations for your applications automatically when binding or rebinding your application to the service instance. These values will be automatically populated to the client configurations for your application through CF environment variables.

**NOTE:** These configurations are only applied at the initial service binding time. Subsequent `cf push` of the application will **NOT** update the configurations. You will either need to manually update the configurations via the SSO dashboard or unbind and rebind the service instance.

When you specify your own scopes and authorities, consider including openid for scopes on auth code, implicit, and password grant type applications, and uaa.resource for client credentials grant type applications, as these will not be provided if they are not specified.

The table in [SSO Documentation - Configure SSO Properties with Environment Variables](https://docs.pivotal.io/p-identity/configure-apps/index.html#configure) provides a description and the default values. Further details and examples are provided in the sample application manifests.

To remove any variables set through bootstrapping, you must use `cf unset-env <APP_NAME> <PROPERTY_NAME>` and rebind the application.

---

#### Running in Unsafe Environment with Self-signed Certificates

⚠️⚠️⚠️ **WARNING** ⚠️⚠️⚠️ Do not use the following steps in your production environments; instead, use trusted certificates within your environment.

If necessary to push the sample apps to an unsafe environment with self-signed certificates, you can add the [cloudfoundry-certificate-truster](https://github.com/pivotal-cf/cloudfoundry-certificate-truster) dependency to the gradle file. Follow the instructions from the cloudfoundry-certificate-truster readme.
