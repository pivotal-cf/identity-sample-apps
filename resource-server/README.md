# Resource Server Sample Application

## Description
The resource server is an OAuth2 protected resource that designed to be consumed by the grant type sample applications in this repository.

## Deploying Resource Server
The resource server needs to know the Auth Server (or UAA) location in order to retrieve the token key to validate the tokens.
Change `SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUERURI` in `manifest.yml` to point to your UAA instance. For example, for a given SSO service plan/UAA identity zone, the location would be `https://<PLAN_AUTH_DOMAIN>.uaa.<YOUR_DOMAIN>/oauth/token`. 

To discover the `issuer` URI, you can vist the [well-known](https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderConfig) endpoint of the UAA application and parse the `issuer` value. To parse the `issuer` using [jq](https://stedolan.github.io/jq/) library run:

    curl https://<PLAN_AUTH_DOMAIN>.uaa.<YOUR_DOMAIN>/.well-known/openid-configuration | jq '.issuer'

To push the app, ensure a CF space is targeted. Go to `./resource-server` and run:

    ./gradlew build
    cf push --random-route

## Endpoints on the Resource Server

It has three API endpoints:
 * `GET /todo` to list TODO items. Requires the user to have `todo.read` scope.
 * `POST /todo` to create a TODO item. Requires `todo.write` scope. Example body: `{"todo":"<content>"}`
 * `DELETE /todo/{id}` to delete a TODO item. Requires `todo.write` scope.
 
