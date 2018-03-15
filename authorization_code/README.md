UAA configuration can be done by running the following commands (requires the `yq` command):

```$xslt
git clone https://github.com/cloudfoundry/uaa.git
git clone https://github.com/pivotal-cf/identity-sample-apps.git
yq merge --inplace uaa/uaa/src/main/resources/uaa.yml \
                   identity-sample-apps/journeys/src/test/resources/uaa-customizations.yml
```

Then, startup UAA server:
```
cd uaa
./gradlew run
```
and finally the auth server:
```$xslt
cd identity-sample-apps
./gradlew -p authorization_code clean bootRun
```

Now you can visit the server at `https://localhost:8888/secured/token`