# Client Credentials Sample Application

This application is a sample for how you can set up your own application that uses a client credentials grant type. The application is written in java and uses the Spring framework.
Client Credentials grant is typically used for service to service applications.

## Creating Client Credentials client in an Identity Zone

1. From Apps Manager click on the manage service button to go to the Pivotal SSO application dashboard.
2. On the application dashboard click on `NEW APP` and fill out the following field as described:
    * App Name (required)
    * Application Type (select `Service-to-Service App` for client credentials grant type)
    * Scopes (`uaa.resource` and `clients.read`)
3. Click on Create App button.
4. Go back to Apps Manager and follow the steps [here] (../README.md)

Note: If any scope change is performed in the application after it's been started up the same needs to be restarted.
