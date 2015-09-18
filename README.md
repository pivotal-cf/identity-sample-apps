# Pivotal Single Sign-On Service Sample Applications

This repo holds separate sample applications for each one of the four OAuth 2.0 grant types supported by the Pivotal Single Sign-On Service. Each grant type maps to an Application Type as seen in the Pivotal Single Sign-On Service Dashboard.

Application Type  | Grant Type
------------- | -------------
Web App  | authorization_code
Native Mobile App  | password
Service-to-Service App | client_credentials
Single Page JavaScript App | implicit

## Step 1: Deploy Sample Application to Pivotal Cloud Foundry

Set the correct CF environment in the CF CLI and login as a Space Developer into the required Org and Space

    cf api api.<your-domain>
    
Go to your application directory and push the app

    ./gradlew build
    cf push --no-start


## Step 2: Configure your application to use Pivotal Single Sign-On Service
This step may vary depending on your application type. Please refer the README.md for each application type aka grant type.
All four sample apps in this repo uses the spring-cloud-sso-connector to auto-configure the sso service bound to the app.

## Step 3: Set grant type for your Application

### Using Apps Manager

Go to ```https://console.<your-domain>```
Add or edit `GRANT_TYPE` environment variable in the app with the of before mentioned grant type [here] (https://github.com/pivotal-cf/identity-sample-apps#pivotal-single-sign-on-service-sample-applications). Restart the app.

### Using CF CLI

Update the GRANT_TYPE environment variable for the app:

    cf set-env <app-name> GRANT_TYPE <grant_type>

Start your app

    cf restart <app-name>
    
Now your app is ready to be used.
