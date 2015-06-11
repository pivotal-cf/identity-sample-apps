# Cloud Foundry Pivotal Single Sign-On Service Sample Applications

This repo holds separate sample applications for each one of the four OAuth 2.0 grant types supported by the Pivotal Single Sign-On Service. Each grant type maps to an Application Type as seen in the Pivotal Single Sign-On Service Dashboard.

Application Type  | Grant Type
------------- | -------------
Web App  | authcode
Native Mobile App  | password
Service-to-Service App | client_credentials
Single Page JavaScript App | implicit

## Step 1: Deploy Sample Application to Pivotal Cloud Foundry

Set the correct CF environment in the CF CLI and login as a Space Developer into the required Org and Space

    cf api api.<your-domain>
    
Go to your application directory and push the app

    ./gradlew build
    cf push --no-start


## Step 2: Register an OAuth Client with Pivotal Single Sign-On Service
This step varies depending on the application type. Please refer the README.md for each application type aka grant type

## Step 3: Update OAuth Client information in the Application

### Using Apps Manager

Go to ```https://console.<your-domain>```
Edit the `CLIENT_ID`, `CLIENT_SECRET` and `ID_SERVICE_URL` environment variable in your app with the new values and start the app.

### Using CF CLI

Run ```cf env <appName>``` (where appName is, for example, authcode-sample).
Copy from the output the `client_id`, `client_secret` and `auth_domain` properties under the bound service.

Use those three values to set three environment variables:

    cf set-env <app-name> CLIENT_ID <client_id>
    cf set-env <app-name> CLIENT_SECRET <client_secret>
    cf set-env <app-name> ID_SERVICE_URL <auth_domain>
    
Start your app

    cf restart <app-name>
    
Now your app is ready to be used.
