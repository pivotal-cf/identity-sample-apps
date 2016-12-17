# Pivotal Single Sign-On Service Sample Applications

This repo holds separate sample applications for each one of the four OAuth 2.0 grant types supported by the Pivotal Single Sign-On Service. The GRANT_TYPE environment variable is already set to the relevant value mentioned below for each sample application. Each grant type maps to an Application Type as seen in the Pivotal Single Sign-On Service Dashboard. 

Application Type  | Grant Type
------------- | -------------
[Web App](/authcode)  | authorization_code
[Native Mobile App](/password)  | password
[Service-to-Service App](/client_credentials) | client_credentials
[Single Page JavaScript App](/implicit) | implicit

## <a name="step-1">Step 1</a>: Deploy Sample Application to Pivotal Cloud Foundry

Set the correct CF API target in the CF CLI and login as a Space Developer into the required Org and Space

    cf api api.<your-domain>
    
Go to your application directory and push the app.

    ./gradlew build
    cf push

NOTE: If you are using a public IP, you will need to update the internal_proxies variable in application.yml to your public IP.

## <a name="step-2">Step 2</a>: Bind the Application with the Pivotal Single Sign-On Service Instance
Follow the steps [here](http://docs.pivotal.io/p-identity/configure-apps/index.html#bind) to bind your application to the service instance.

Restart your application after binding the service using Apps Manager or CF CLI.


# Resource Server Sample Application

## Deploying Resource Server

### Setup
The resource server needs to know the Auth Server (or UAA) location in order to retrieve the token key to validate the tokens. 
Set the Auth Server location as the value of the auth_domain environment variable for the authcode sample app.

`cf set-env <RESOURCE_SERVER_APP_NAME> AUTH_SERVER <AUTH_SERVER_LOCATION>`

It has three API endpoints:
 * `GET /todo` to list TODO items. Requires the user to have `todo.read` scope.
 * `POST /todo` to create a TODO item. Requires `todo.write` scope. Example body: `{"todo":"<content>"}`
 * `DELETE /todo/{id}` to delete a TODO item. Requires `todo.write` scope.

To push the app, follow steps [1](#step-1) and [2](#step-2) of the previous section.

## Setting up Authcode Sample App to use Resource Server

Currently, only the authcode sample app uses the resource server, but the other grant types should be similar.
The authcode sample app needs to know the resource server location in order to manage TODO resources.

`cf set-env <AUTHCODE_APP_NAME> RESOURCE_URL <RESOURCE_SERVER_URL>`

NOTE: You must remove the trailing slash ('/') from the URL.

For the sample app to work you need to go to the Resource dashboard and create a Resource with name `todo` and `todo.read` and `todo.write` permissions.
After creating the resource, you need to update the authcode-sample app with the previously created scopes on the App dashboard.
Follow the steps [here] (http://docs.pivotal.io/p-identity/manage-resources.html) to create the resource and permissions.

The authenticated user should also have the scopes `todo.read` and `todo.write`.

NOTE: If a user doesn't have these scopes, contact your local admin to grant these scopes to that user.
