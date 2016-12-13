# how to deploy on openshift v3

oc new-project mobi-amw-test

## database
add postgresql Database to project, via webconsole or

```
oc new-app -e \
    POSTGRESQL_USER=amw,POSTGRESQL_PASSWORD=amw,POSTGRESQL_DATABASE=amw \
    registry.access.redhat.com/rhscl/postgresql-94-rhel7
```


## create binary build

```
oc new-build --docker-image=registry.access.redhat.com/jboss-eap-6/eap64-openshift  --binary=true --name=amw-eap
```

## configure the deployment

```
oc env dc amw-eap \
     -e OPENSHIFT_POSTGRESQL_DB_HOST=amw \
     -e OPENSHIFT_POSTGRESQL_DB_PORT=5432 \
     -e OPENSHIFT_POSTGRESQL_DB_NAME=amw \
     -e OPENSHIFT_POSTGRESQL_DB_USERNAME=amw \
     -e OPENSHIFT_POSTGRESQL_DB_PASSWORD=amw \
     -e AMW_ENCRYPTIONKEY=6B8F084BC13DEE7177613E8D90BB331 \
     -e AMW_LOGOUTURL=www.google.ch \
     -e AMW_GENERATORPATH=/tmp/gen \
     -e AMW_LOGSPATH=/tmp/log \
     -e AMW_TESTRESULTPATH=/tmp/test \
     -e AMW_STMPATH=/tmp/amw/stm.jar \
     -e AMW_STMREPO=/tmp/amw/stps \
     -e AMW_DEPLOYMENTSCHEDULERDISABLED=false \
     -e AMW_DELIVERMAIL=false \
     -e AMW_MAILDOMAIN=puzzle.ch 

```


## create initial Build
copy the generated artefact, you need to run ``mvn clean install`` first
```
cp -a ../AMW_ear/target/AMW.ear ./deployments/
```
start binary build from 
```
oc start-build amw-eap --from-dir=.
```
create App and deploy it
```
oc new-app amw-eap
```

## redeploy

```
cp -a ../AMW_ear/target/AMW.ear ./deployments/
oc start-build amw-eap --from-dir=.
```






