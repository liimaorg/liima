# Liima Docker image

This image is meant to run liima code quickly on wildfly server provided on docker. 
Data is stored in the embedded h2 database in the container and it will be lost if the container is removed.

To run latest code on docker, build the latest ear package:
```
mvn clean install
```  

## Building docker image

Create docker image:
```
AMW_docker/build.sh
```

## Start the application

Start the container:
```
docker-compose -f ./AMW_docker/docker-compose/docker-compose.yml up -d
```
Follow application logs:
```
docker-compose -f AMW_docker/docker-compose/docker-compose.yml logs -f
```
Access web application on: http://localhost:8080/AMW_web/

and swagger UI on: http://localhost:8080/AMW_rest/

User/PW for app login can be found in:
```
configuration/props/application-users.properties
```

## Debug the application

From *Run/Debug configurations* in IntelliJ select ```Debug application remotely on docker``` and click on debug (Shift+F9) in order to debug application from IDE.
If debug is successfully started, following command will appear in Debug Console: 
```
Connected to the target VM, address: 'localhost:8787', transport: 'socket'
```


## Connect to the h2 database

Download the h2.jar (https://mvnrepository.com/artifact/com.h2database/h2) and start it:
```
java -jar h2-1.4.197.jar
```

Start the Docker container and mount the folder with the h2 database:
```
docker run -it -p 8080:8080 -v $PWD/AMW_business/src/test/resources/integration-test/testdb/:/opt/jboss/wildfly/standalone/data/amw/ -v $PWD/AMW_ear/target/AMW.ear:/opt/jboss/wildfly/standalone/deployments/AMW.ear liimaorg/liima:snapshot
```

Connect to the h2 database:
* H2 console: http://localhost:8082/login.jsp
* H2 JDBC url: jdbc:h2:<path to liima folder>/AMW_business/src/test/resources/integration-test/testdb/amwFileDbIntegrationEmpty;AUTO_SERVER=TRUE;

