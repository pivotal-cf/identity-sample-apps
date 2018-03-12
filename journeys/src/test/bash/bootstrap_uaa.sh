#!/usr/bin/env bash -e

uaac target http://localhost:8080/uaa --skip-ssl-validation
uaac token client get admin -s adminsecret

uaac group add sample.scope

uaac client add sample-client \
   --name sample-client \
   --scope sample.scope \
   -s sample-client-secret \
   --authorized_grant_types authorization_code \
   --authorities uaa.resource \
   --redirect_uri http://localhost:8888/**

uaac user add sample-user -p sample-password --emails user@example.com

uaac member add sample.scope sample-user
