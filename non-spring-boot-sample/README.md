# Non-Spring Boot Sample Application With SSO Connector

This application runs only on Cloud Foundry and it needs to be bound to the SSO service.

The `SsoServiceInfoCreatorInitializer` triggers the `SsoServiceInfoCreator` in the connector and adds the binding credentials to the environment.

Once this bean is initialized, other beans can access these credentials as environment variables.

## Deploying this app to CF

`./gradlew clean build`

`cf push [app-name] -p build/libs/non-spring-boot-sample-1.0-SNAPSHOT.war --no-start`

Bind the app to the SSO service

`cf restart [app-name]`

Note: In order to make the SSO flow work, please ensure that the OAuth client for this application has the `uaa.resource` authority.
