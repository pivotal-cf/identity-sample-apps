# Pivotal Single Sign-On Service Sample Applications

This repo holds separate sample applications for each one of the four OAuth 2.0 grant types supported by the Pivotal Single Sign-On Service. The GRANT_TYPE environment variable is already set to the relevant value mentioned below for each sample application. Each grant type maps to an Application Type as seen in the Pivotal Single Sign-On Service Dashboard. 

Application Type  | Grant Type
------------- | -------------
[Web App](https://github.com/pivotal-cf/identity-sample-apps/tree/master/authcode)  | authorization_code
[Native Mobile App](https://github.com/pivotal-cf/identity-sample-apps/tree/master/password)  | password
[Service-to-Service App](https://github.com/pivotal-cf/identity-sample-apps/tree/master/client_credentials) | client_credentials
[Single Page JavaScript App](https://github.com/pivotal-cf/identity-sample-apps/tree/master/implicit) | implicit

## Step 1: Deploy Sample Application to Pivotal Cloud Foundry

Set the correct CF API target in the CF CLI and login as a Space Developer into the required Org and Space

    cf api api.<your-domain>
    
Go to your application directory and push the app.

    ./gradlew build
    cf push

## Step 2: Bind the Application with the Pivotal Single Sign-On Service Instance
Follow the steps [here] (http://docs.pivotal.io/p-identity/index.html#create-instance) to bind your application to the service instance.

Restart your application after binding the service using Apps Manager or CF CLI.
