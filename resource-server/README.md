# Resource Server Sample Application

## Deploying Resource Server

The resource server needs to know the Auth Server (or UAA) location in order to retrieve the token key to validate the tokens.
Change `AUTH_SERVER` in `manifest.yml` to point to your UAA instance. For example, for a given SSO service plan/UAA identity zone, the location would be `https://subdomain.login.my-domain.org`

To push the app, ensure a CF space is targeted. Go to `./resource-server` and run:

    ./gradlew build
    cf push

### What is the Resource Server

It has three API endpoints:
 * `GET /todo` to list TODO items. Requires the user to have `todo.read` scope.
 * `POST /todo` to create a TODO item. Requires `todo.write` scope. Example body: `{"todo":"<content>"}`
 * `DELETE /todo/{id}` to delete a TODO item. Requires `todo.write` scope.
