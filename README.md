# Liima
[![Build Status](https://travis-ci.org/liimaorg/liima.svg?branch=master)](https://travis-ci.org/liimaorg/liima)

Liima - (AMW) Automated Middleware allows you to manage the configurations of
your Java EE applications on an unlimited number of different environments
with various versions, including the automated deployment of those apps.

## Docs

Liima Docs are available under https://github.com/liimaorg/docs

## Run Liima locally on docker

Preconditions:
- install docker 
- install docker-compose

Follow [ReadMe file](./AMW_docker/README.md) in order to run Liima locally on docker.

## Build and create Release 
Build the Liima ear

**Note:** when using Oracle JDK to build Liima, make sure to install the Java Cryptography Extension (JCE) Unlimited Strength. Otherwise the decryption tests are going to fail.  

```
mvn clean install
```

### Create new Release

Version Updates are done with the maven release plugin. 
```
mvn release:clean release:prepare -Darguments="-DskipTests"
```

* Choose the version number of the new release as well as the following version number when asked. Please make sure, that the following version number is postfixed with "SNAPSHOT"
* This will create a new Git Tag, update the version numbers and commit the changes.
* The new Tag creates a release in GitHub and Travis will automatically add the binary to the release page on GitHub once it's done building. You can then add the release notes.
* The `*.releaseBackup` files can be removed with `mvn release:clean`
* Push the changes
* We do not use `mvn release:perform` as the ear is not uploaded to a maven repository.

You also can rollback your release-step with
```
mvn release:rollback
```

### Create Code Coverage Report
```
mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent install -Dmaven.test.failure.ignore=true
```

Execute Sonar analysis
----------------------

```
mvn sonar:sonar
```
The Sonar properties must be confiured in ~/.m2/settings.xml
```
<settings>
    <profiles>
        <profile>
            <id>sonar</id>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <properties>
                <!-- Example for MySQL-->
                <sonar.jdbc.url>
                        jdbc:h2:tcp://localhost/sonar
                </sonar.jdbc.url>
                <sonar.jdbc.username>sonar</sonar.jdbc.username>
                <sonar.jdbc.password>sonar</sonar.jdbc.password>

                <!-- Optional URL to server. Default value is http://localhost:9000 -->
                <sonar.host.url>
                  http://localhost:9000
                </sonar.host.url>
            </properties>
        </profile>
     </profiles>
</settings>
```
## Toggle Angular

The Angular-UI can be disabled by setting the following property to true
```
amw.feature.disableAngularGui
``` 

## Copying

please check [Copying file](COPYING)

## License

Liima is licensed under the GNU AGPLv3 License, see [LICENSE file](LICENSE)

## Contribution

Please have a look at the [contribution guide](CONTRIBUTING.md)

## Authors

The contributors who are working on Liima or did contribute to Liima are listed in the [AUTHORS file](AUTHORS)



