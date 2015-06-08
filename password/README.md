# Password Grant Sample Application

This application is a sample for how you can set up your own application that uses a password grant type. The application is written in java uses the Spring framework.
Password grant is typically used when there is a high degree of trust between the resource owner and the client.

## Creating Password client in an Identity Zone

1. From Apps Manager click on the manage service button to go to the Pivotal SSO application dashboard.
2. On the application dashboard click on `NEW APP` and fill out the following field as described:
    * App Name (required)
    * Application Type (select `Native Mobile App` for resource owner password credentials grant type)
    * User Store Connections (select `Internal User Store` to use UAA as authentication server)
    * Scopes (`openid`)
3. Click on Create App button.
4. Go back to Apps Manager and follow the steps [here] (../README.md)
