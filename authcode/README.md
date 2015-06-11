# Authorization Code Sample Application

This application is a sample for how you can set up your own application that uses an authcode grant type. The application is written in java and uses Spring Cloud Security for the SSO flow. The authcode grant type is the most common OAuth flow.

## Creating authcode aka Web App client in Pivotal Single Sign-On Service

1. From Apps Manager click on the manage service button of your service instance to go to the service dashboard.
2. On the service dashboard click on `NEW APP` and fill out the following field as described:
    * App Name (required)
    * Application Type (select `Web App` for authorization code grant type)
    * User Store Connections (select `Internal User Store` or create a New SAML User Store Connection)
    * Redirect URIs (Give the url of your application i.e. `https://<app-route>`)
    * Scopes (`openid`)
3. Click on Create App button.
4. Make note of the environment variables to be set
4. Go back to Apps Manager and follow the steps [here] (https://github.com/pivotal-cf/identity-sample-apps#step-3-update-oauth-client-information-in-the-application)
