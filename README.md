# Liima
[![test and build](https://github.com/liimaorg/liima/actions/workflows/test.yml/badge.svg?branch=master)](https://github.com/liimaorg/liima/actions/workflows/test.yml)

Liima - (AMW) Automated Middleware allows you to manage the configurations of
your Java EE applications on an unlimited number of different environments
with various versions, including the automated deployment of those apps.

## Docs

Liima Docs are available under https://github.com/liimaorg/docs

### Property validation patterns

Property type validation patterns and server-side property validation use
[RE2/J](https://github.com/google/re2j) to avoid catastrophic regex backtracking.
Matches must cover the entire property value. Java-specific constructs such as
lookarounds, backreferences, atomic groups, and possessive quantifiers are not
supported. Before upgrading, check existing validation patterns for compatibility
with [RE2 syntax](https://github.com/google/re2/wiki/Syntax).
Unsupported patterns are rejected by the property type API. Existing unsupported
patterns are reported as property validation errors and logged; there is no
fallback to the Java regex engine.

## Run Liima locally on docker

Preconditions:
- install docker 
- install docker-compose

Follow [ReadMe file](./AMW_docker/README.md) in order to run Liima locally on docker.

## Build and create Release 
Build the Liima ear

Preconditions:
- JDK 11 or newer (Java 11 remains the runtime target)
- Maven 3.6.3+
- [Angular CLI](https://cli.angular.io/) is required for Angular development
- [Lombok](https://projectlombok.org/) is required in the IDE

```
mvn clean install
```

Or build Liima inside a container:

```
docker run -it -u $(id -u):$(id -g) -e MAVEN_CONFIG=$HOME/.m2 -e HOME=$HOME -v "$PWD":/usr/src/amw -v "$HOME":"$HOME" -w /usr/src/amw maven:3.9.12-eclipse-temurin-11-noble mvn -Duser.home=$HOME clean install
```

### Dependency compatibility

The supported baseline is JDK 11 and WildFly 26.1.3.Final. The parent POM imports
`org.wildfly.bom:wildfly-jakartaee8:26.1.3.Final`. Keep `version.wildfly` aligned
with the Docker image. Keep server-provided dependencies in `provided` scope without
individual version overrides. The Jakarta EE 8 artifacts still contain `javax.*`
packages; they do not require a Jakarta EE 9+ source migration.
Some BOM-managed artifacts, such as JAXB, require the public JBoss repository.
If Maven uses a repository mirror, it must proxy that repository or exclude
`jboss-public` from its `mirrorOf` pattern (for example, `*,!jboss-public`).

Application-owned libraries and test tools are managed separately. Builds may use
newer JDKs with `--release 11`; CI verifies compatibility on JDK 11. Maven Enforcer
rejects dependencies with bytecode newer than Java 11, including provided and test
dependencies. Dependabot groups minor/patch Maven
updates and leaves major updates separate. WildFly BOM updates have their own
group and are limited to 26.x; WildFly 27+ requires a Jakarta EE migration.
The Java EE API, JUnit 6+, and Liquibase 5+ remain excluded from automatic updates.

GitHub integration tests use the public WildFly 26.1.3 image on JDK 11.

### Create new Release

1. Update the release-changelog.md
1. Push a tag to git:
    ```
    git tag v1.17.23
    git push origin v1.17.23
    ```
The release will then be built and published by the release GitHub action: https://github.com/liimaorg/liima/actions

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

## Copying

Please check [Copying file](COPYING)

## License

Liima is licensed under the GNU AGPLv3 License, see [LICENSE file](LICENSE)

## Contribution

Please have a look at the [contribution guide](CONTRIBUTING.md)

## Authors

The contributors who are working on Liima or did contribute to Liima are listed in the [AUTHORS file](AUTHORS)

## Declaration

Contributions to the codebase have been made with the support of Codeium. Codeium is AI-powered code completion tool, that is trained exclusively on natural language and source code data with [permissive licenses](https://codeium.com/blog/copilot-trains-on-gpl-codeium-does-not ). 



