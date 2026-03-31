---
trigger: always_on
description: 
globs: 
---

# Liima Project Rules

## Project Overview
Liima (AMW - Automated Middleware) is a Java EE application for managing configurations of Java EE applications across multiple environments with automated deployment capabilities.

## Technology Stack
- **Java**: JDK 11
- **Build Tool**: Maven 3.5+
- **Application Server**: WildFly/JBoss EAP
- **Frontend**: Angular 21+ (in AMW_angular/io)
- **ORM**: Hibernate 5.3.20 with Envers
- **Database Migrations**: Liquibase 4.33.0
- **Code Generation**: Lombok 1.18.42
- **Testing**: JUnit 5, Mockito 5, AssertJ

## Project Structure
- **AMW_business**: EJB business logic layer (core application logic)
- **AMW_web**: JSF web layer (legacy web interface)
- **AMW_angular**: Angular frontend (main modern UI)
- **AMW_rest**: REST API layer (backend services)
- **AMW_commons**: Shared utilities and common code
- **AMW_db_scripts**: Liquibase database migration scripts
- **AMW_ear**: EAR packaging module
- **AMW_docker**: Docker configuration for local development
- **AMW_e2e**: End-to-end tests

## Build Commands
- **Full build**: `mvn clean install`
- **Build without tests**: `mvn clean install -DskipTests`
- **Code coverage**: `mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent install -Dmaven.test.failure.ignore=true`
- **Docker build**: See AMW_docker/README.md
- **Container build**: `docker run -it -u $(id -u):$(id -g) -e MAVEN_CONFIG=$HOME/.m2 -e HOME=$HOME -v "$PWD":/usr/src/amw -v "$HOME":"$HOME" -w /usr/src/amw maven:3.9.12-eclipse-temurin-11-noble mvn -Duser.home=$HOME clean install`

## Important Requirements
- Lombok is required in the IDE for development
- Angular development requires Node.js/npm (managed via nvm)
- Always run `nvm use` before executing npm or Angular CLI commands
- Docker and docker-compose are needed for local development

## Code Style Guidelines
- Follow existing code patterns and conventions
- Never remove existing comments or documentation unless explicitly requested
- Maintain consistent formatting with the existing codebase
- Use Lombok annotations appropriately (@Data, @Getter, @Setter, etc.)
- Follow Java EE best practices for EJBs and CDI

## Testing Guidelinestag
- Use JUnit 5 for all new tests
- Use Mockito for mocking dependencies
- Use AssertJ for fluent assertions
- Never delete or weaken existing tests without explicit direction
- Ensure tests are isolated and repeatable
- Test coverage is tracked via JaCoCo

## Release Process
1. Update release-changelog.md
2. Push a tag: `git tag v1.17.23 && git push origin v1.17.23`
3. GitHub Actions will build and publish the release

## Development Workflow
- Use feature branches for development
- Follow the contribution guidelines in CONTRIBUTING.md
- Ensure all tests pass before committing
- Code contributions may use AI assistance (Codeium/similar tools)


## Misc
- General Date format is yyyy-mm-dd hh:MM.ss
- never us US locales - e.g. mm/dd/yyyy for dates