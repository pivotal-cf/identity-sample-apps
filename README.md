# Pivotal Single Sign-On Service Sample Applications

This repo holds separate sample applications for each one of the four OAuth 2.0 grant types supported by the Pivotal Single Sign-On Service. The grant type specific environment variables are configured to their relevant values in the manifests of sample application. Each grant type maps to an Application Type as seen in the Pivotal Single Sign-On Service Dashboard.

Application Type  | Grant Type | Uses Spring Cloud SSO starter library
------------- | -------------- | ---------------------
[Web App](/authcode)  | authorization_code | yes
[Native Mobile App](/password)  | password | no
[Service-to-Service App](/client_credentials) | client_credentials | yes
[Single Page JavaScript App](/implicit) | implicit | no

The latest version of this repository supports the following dependencies:

Dependency | Version
------------- | ---------- 
[Spring Boot](https://github.com/spring-projects/spring-boot/tree/2.1.x) | 2.1.1+
[Spring Security](https://github.com/spring-projects/spring-security/tree/5.1.x) | 5.0+ 
[Java CfEnv Boot Pivotal SSO](https://github.com/pivotal-cf/java-cfenv/tree/master/java-cfenv-boot-pivotal-sso) | 1.0.2+ 

The last version to support Spring Boot 1.5.5+ is tagged at [spring-boot/1.5](https://github.com/pivotal-cf/identity-sample-apps/releases/tag/spring-boot%2F1.5).

The sample applications for the corresponding grant types are located in subdirectories of this project:  

## <a name="step-1">Step 1</a>: Prerequisites

1. Login as a Space Developer into the required Org and Space on your PCF Foundation

       cf login -a api.<your-domain>
        
1. Ensure that an SSO (p-identity) [Service Plan](https://docs.pivotal.io/p-identity/manage-service-plans.html) exists for your Org

       cf marketplace | grep p-identity

1. Create a [Service Instance](https://docs.pivotal.io/p-identity/manage-service-instances.html) named 'p-identity-instance' from the 'p-identity' service using an available Service Plan

       cf create-service p-identity <plan_tier> p-identity-instance

1. Preparing a test user with sufficient scopes

     Contact your cloud administrator to determine whether your Service Plan is has configured the "Internal User Store" as an Identity Provider or an external Identity Provider (like LDAP).

     - If your SSO Service plans is configured with the 'Internal User Store' option, you can use the instruction in [Manage Users in an Internal User Store](https://docs.pivotal.io/p-identity/manage-users.html) documentation to create a user to `todo.read` and `todo.write` scopes.

     - If your plan is configured with an alternative Identity Provider (like LDAP), your administrator will need to provide you credentials with memberships to the `todo.read` and `todo.write` scopes.

You can then test the applications by creating test users with the `todo.read` and `todo.write` scopes for your plan using the steps [here](https://docs.pivotal.io/p-identity/configure-id-providers.html#add-to-int).

## <a name="quick-start">Quick Start</a>: Authcode Sample App and Resource Server on SSO

You can deploy the authcode and resource server sample applications using application bootstrapping with the steps below. You can read more about these topics in the following sections.

1. Navigate to the *resource-server* directory

1. Update the `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUERURI` <PLAN_AUTH_DOMAIN> and <YOUR_DOMAIN> placeholders in the *resource-service* manifest. 

1. Build (`./gradlew build`) and push (`cf push`) the *resource-server* application.

1. Navigate to the *authcode* directory

1. Update the `RESOURCE_URL` value in the *authcode* manifest to the route of the deployed *resource-server*.

1. Build (`./gradlew build`) and push (`cf push`) the *authcode* project.
   
The sample application and resource server will be immediately bound to the SSO Service on start-up.

You may have to use `--random-route` flag when cf pushing your application if a route already exists with your application name.

# Bootstrap Application Client Configurations for the Pivotal Single Sign-On Service Instance
Beginning in SSO 1.4.0, you can set environment variables in your application's manifest to bootstrap client configurations for your applications automatically when binding or rebinding your application to the service instance. These values will be automatically populated to the client configurations for your application through CF environment variables.

**NOTE:** These configurations are only applied at the initial service binding time. Subsequent `cf push` of the application will **NOT** update the configurations. You will either need to manually update the configurations via the SSO dashboard or unbind and rebind the service instance.

When you specify your own scopes and authorities, consider including openid for scopes on auth code, implicit, and password grant type applications, and uaa.resource for client credentials grant type applications, as these will not be provided if they are not specified.

The table in [SSO Documentation - Configure SSO Properties with Environment Variables](https://docs.pivotal.io/p-identity/configure-apps/index.html#configure) provides a description and the default values. Further details and examples are provided in the sample application manifests.

To remove any variables set through bootstrapping, you must use `cf unset-env <APP_NAME> <PROPERTY_NAME>` and rebind the application.
