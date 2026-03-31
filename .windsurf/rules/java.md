---
trigger: always_on
description: 
globs: 
---

# Java Development Rules

## Java Version
- **Target**: Java 11
- **Compiler**: Maven compiler plugin with release=11

## Framework & Libraries
- **Java EE**: JBoss Java EE Web 8.0 specification
- **ORM**: Hibernate 5.3.20.Final (provided by application server)
- **Validation**: Hibernate Validator 6.2.0.Final (JSR-303)
- **Utilities**: Apache Commons Lang3 3.20.0
- **JSON**: Jackson 2.x (provided by application server)
- **XML Binding**: JAXB 2.3.x (provided by application server)

## Code Generation
- **Lombok**: Version 1.18.42
- Lombok is required in the IDE for proper development
- Common annotations: @Data, @Getter, @Setter, @Builder, @NoArgsConstructor, @AllArgsConstructor
- Use Lombok to reduce boilerplate code

## Module Structure
- **AMW_business**: EJB 3.2 modules with business logic
- **AMW_commons**: Shared utilities and domain objects
- **AMW_web**: WAR module with JSF views
- **AMW_rest**: WAR module with REST endpoints
- **AMW_ear**: EAR packaging combining all modules

## Dependency Management
- All dependency versions are managed in the parent pom.xml
- Use `<scope>provided</scope>` for application server-provided libraries
- Use `<scope>test</scope>` for test dependencies
- Never add dependencies without checking if they're already managed

## Testing
- **Framework**: JUnit 5 (Jupiter)
- **Mocking**: Mockito 5.21.0
- **Assertions**: AssertJ 3.27.7, Hamcrest 3.0
- Test configuration: `useSystemClassLoader=false`, `forkCount=1`, `reuseForks=true`
- Maximum heap size for tests: 4096m

## Build Configuration
- Maven plugins are configured in parent pom.xml pluginManagement
- JAR/WAR/EAR manifests include build metadata
- Archive indexing is enabled
- Source encoding: UTF-8

## Code Quality
- JaCoCo for code coverage reporting
- Sonar analysis available (configure in ~/.m2/settings.xml)
- Follow Java EE best practices
- Use CDI for dependency injection
- Use EJB for transactional business logic

## Naming Conventions
- Follow standard Java naming conventions
- Use meaningful variable and method names
- Keep method names descriptive and action-oriented
- Use proper package structure following reverse domain naming

## Error Handling
- Use appropriate exception types
- Don't catch generic Exception unless necessary
- Log errors appropriately
- Provide meaningful error messages
