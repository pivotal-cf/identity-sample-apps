# Client Credentials Sample Application

This application is a sample for how you can set up your own application that uses a client credentials grant type. The application is written in java and uses the Spring framework.

## Creating client credentials aka Service-to-Service client in Pivotal Single Sign-On Service

1. From Apps Manager click on the manage service button to go to the Pivotal SSO application dashboard.
2. On the application dashboard click on `NEW APP` and fill out the following field as described:
    * App Name (required)
    * Application Type (select `Service-to-Service App` for client credentials grant type)
    * Scopes (`uaa.resource` and `clients.read`)
3. Click on Create App button.
4. Make note of the environment variables to be set.
5. Go back to Apps Manager and follow the steps [here] (https://github.com/pivotal-cf/identity-sample-apps#step-3-update-oauth-client-information-in-the-application)

Note: If scopes are updated in the application definition above after it's been started up the same needs to be restarted.
