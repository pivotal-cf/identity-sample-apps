# Authorization Code Sample Application

This application is a sample for how you can set up your own application that uses an authorization code grant type. The application is written in java and uses Spring Cloud Security for the SSO flow.
Authorization code grant is the most common OAuth flow.

## Creating Authcode client in an Identity Zone

1. From Apps Manager click on the manage service button to go to the Pivotal SSO application dashboard.
2. On the application dashboard click on `NEW APP` and fill out the following field as described:
    * App Name (required)
    * Application Type (select `Web App` for authorization code grant type)
    * User Store Connections (select `Internal User Store` to use UAA as authentication server)
    * Redirect URIs (Give the url of your application i.e. `https://<app-route>.login.<domain>`)
    * Scopes (`openid`)
3. Click on Create App button.
4. Go back to Apps Manager and follow the steps [here] (../README.md)
