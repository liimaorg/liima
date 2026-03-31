---
trigger: always_on
description: 
globs: 
---

# Clean Architecture Guidelines

## Overview
These guidelines define clean architecture patterns for the Liima application, focusing on separation of concerns between REST endpoints (AMW_rest), use case interfaces (AMW_business/boundary), and use case implementations (AMW_business/control).

## Core Principles

### 1. Dependency Direction
- **REST layer** depends on **boundary interfaces** (use cases)
- **Control layer** implements **boundary interfaces**
- **Boundary layer** defines contracts and command objects
- Dependencies flow inward: REST → Boundary → Control → Domain

### 2. Exception-Driven Error Handling
- Business logic and REST endpoints throw domain exceptions
- Exception mappers translate exceptions to HTTP responses
- No manual HTTP Response building for error cases
- Let the framework handle error responses via exception mappers

### 3. Command Objects for Input
- Use case methods for **write operations** (POST, PUT, DELETE) accept command objects
- Use case methods for **simple queries** may accept validated primitives/objects directly
- Commands encapsulate and validate complex input data
- Commands are immutable (final fields, no setters)
- Validation happens in command constructors using JSR-303/Bean Validation

### 4. Query Operations and Validation

**Simple Queries** (single parameter):
- Use JAX-RS validation annotations (`@NotNull`, `@PathParam`, `@QueryParam`)
- JAX-RS automatically validates and throws `BadRequestException` if validation fails
- Pass validated parameter directly to use case
- No command object needed

**Complex Queries** (multiple parameters, optional filters):
- Use command objects to encapsulate parameters
- Validate in command constructor
- Provides type safety and clear intent

**Decision Guide**:
- **1 parameter** (e.g., `GET /resources/{id}`) → Use `@PathParam` with `@NotNull`
- **2-3 required parameters** → Consider command object for clarity
- **Multiple optional parameters** → Use command object
- **Complex filtering/pagination** → Use command object

## Layer Responsibilities

### REST Layer (AMW_rest)
**Location**: `/AMW_rest/src/main/java/ch/mobi/itc/mobiliar/rest/`

**Responsibilities**:
- Define JAX-RS endpoints (@Path, @GET, @POST, @PUT, @DELETE)
- Map HTTP requests to use case calls
- Convert request DTOs to command objects (for write operations)
- Convert domain objects to response DTOs
- Inject and call use case interfaces from boundary packages
- Validate path/query parameters using JAX-RS annotations (@NotNull, @PathParam, @QueryParam)
- Return successful responses (200, 201, 204)
- Throw exceptions for error cases (let exception mappers handle them)

**Rules**:
- ✅ **DO**: Inject use case interfaces from `*.boundary` packages
- ✅ **DO**: Create command objects from request DTOs for write operations
- ✅ **DO**: Use JAX-RS validation (@NotNull, @Valid) for path/query parameters
- ✅ **DO**: Map domain objects to response DTOs in REST layer
- ✅ **DO**: Keep DTOs in REST layer (not in boundary/control)
- ✅ **DO**: Throw exceptions for validation and business errors
- ✅ **DO**: Return `Response.ok()` or `Response.status(CREATED)` for success
- ❌ **DON'T**: Inject services from `*.control` packages directly
- ❌ **DON'T**: Implement business logic in REST classes
- ❌ **DON'T**: Build error responses manually (use exception mappers)
- ❌ **DON'T**: Pass DTOs to use cases (convert to commands/primitives first)
- ❌ **DON'T**: Return domain entities directly (map to DTOs)

**Write Operation Example (POST/PUT/DELETE)**:
```java
@RequestScoped
@Path("/apps")
@Consumes(APPLICATION_JSON)
@Produces(APPLICATION_JSON)
public class AppsRest {

    @Inject
    private AddAppUseCase addAppUseCase;

    @POST
    public Response addApp(@Valid AddAppDTO dto) {
        // Convert DTO to command (validation happens in command constructor)
        AddAppCommand command = new AddAppCommand(dto.getAppName(), dto.getReleaseId());
        
        // Call use case (throws exceptions on error)
        Integer appId = addAppUseCase.add(command);
        
        // Return success response
        return Response.status(CREATED).entity(appId).build();
    }
}
```

**Simple Query Example (GET with path parameter)**:
```java
@RequestScoped
@Path("/resources")
@Produces(APPLICATION_JSON)
public class ResourcesRest {

    @Inject
    private GetResourceUseCase getResourceUseCase;

    @GET
    @Path("/{resourceId}")
    public Response getResource(@PathParam("resourceId") @NotNull Integer resourceId) {
        // JAX-RS validates @NotNull, throws BadRequestException if null
        // Call use case directly with validated parameter
        Resource resource = getResourceUseCase.getById(resourceId);
        
        // Map domain object to DTO
        ResourceDTO dto = ResourceDTO.from(resource);
        
        // Return success response
        return Response.ok(dto).build();
    }
}
```

**Complex Query Example (GET with multiple parameters)**:
```java
@RequestScoped
@Path("/deployments")
@Produces(APPLICATION_JSON)
public class DeploymentsRest {

    @Inject
    private ListDeploymentsUseCase listDeploymentsUseCase;

    @GET
    public Response listDeployments(
            @QueryParam("environmentId") Integer environmentId,
            @QueryParam("appServerId") Integer appServerId,
            @QueryParam("state") String state,
            @QueryParam("limit") @DefaultValue("50") Integer limit) {
        
        // For complex queries with multiple optional parameters, use a command
        ListDeploymentsCommand command = new ListDeploymentsCommand(
            environmentId, appServerId, state, limit
        );
        
        List<Deployment> deployments = listDeploymentsUseCase.list(command);
        
        // Map to DTOs
        List<DeploymentDTO> dtos = deployments.stream()
            .map(DeploymentDTO::from)
            .collect(Collectors.toList());
        
        return Response.ok(dtos).build();
    }
}
```

### Boundary Layer (AMW_business/*/boundary)
**Location**: `/AMW_business/src/main/java/ch/puzzle/itc/mobiliar/business/*/boundary/`

**Responsibilities**:
- Define use case interfaces
- Define command objects with validation annotations
- Define result/response objects (if needed)
- Declare checked exceptions in method signatures

**Rules**:
- ✅ **DO**: Create one interface per use case (Single Responsibility)
- ✅ **DO**: Use descriptive use case names (e.g., `AddAppUseCase`, `ListAppsUseCase`)
- ✅ **DO**: Accept command objects for write operations and complex queries
- ✅ **DO**: Accept simple validated parameters (Integer id, String name) for simple queries
- ✅ **DO**: Return domain objects or primitives (not DTOs, not Response objects)
- ✅ **DO**: Declare all exceptions that can be thrown
- ✅ **DO**: Keep interfaces focused and cohesive
- ❌ **DON'T**: Put implementation logic in boundary package
- ❌ **DON'T**: Accept or return DTOs (DTOs belong in REST layer)
- ❌ **DON'T**: Return Response objects (that's REST layer concern)
- ❌ **DON'T**: Use multiple primitive parameters for complex operations (use commands)

**Write Operation Use Case Example**:
```java
package ch.puzzle.itc.mobiliar.business.apps.boundary;

public interface AddAppUseCase {
    Integer add(AddAppCommand command) throws NotFoundException, IllegalStateException;
}
```

**Simple Query Use Case Example**:
```java
package ch.puzzle.itc.mobiliar.business.resources.boundary;

import ch.puzzle.itc.mobiliar.business.resourcegroup.entity.Resource;

public interface GetResourceUseCase {
    Resource getById(Integer resourceId) throws NotFoundException;
}
```

**Complex Query Use Case Example**:
```java
package ch.puzzle.itc.mobiliar.business.deployments.boundary;

import ch.puzzle.itc.mobiliar.business.deploy.entity.Deployment;
import java.util.List;

public interface ListDeploymentsUseCase {
    List<Deployment> list(ListDeploymentsCommand command);
}
```

**Command Object Example**:
```java
package ch.puzzle.itc.mobiliar.business.apps.boundary;

import lombok.Getter;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import static ch.puzzle.itc.mobiliar.business.utils.Validation.validate;

@Getter
public class AddAppCommand {

    @ValidAppName
    private final String appName;

    @NotNull
    private final Integer releaseId;

    public AddAppCommand(String appName, Integer releaseId) throws ValidationException {
        this.appName = appName;
        this.releaseId = releaseId;
        validate(this);  // Validate on construction
    }
}
```

**Command Object Rules**:
- ✅ **DO**: Use `@Getter` from Lombok
- ✅ **DO**: Make all fields `private final`
- ✅ **DO**: Validate in constructor using `validate(this)`
- ✅ **DO**: Use JSR-303 validation annotations (@NotNull, @Size, etc.)
- ✅ **DO**: Create custom validators for domain-specific rules
- ✅ **DO**: Throw `ValidationException` from constructor
- ❌ **DON'T**: Add setters (commands are immutable)
- ❌ **DON'T**: Add business logic to commands
- ❌ **DON'T**: Use default/no-arg constructors

### Control Layer (AMW_business/*/control)
**Location**: `/AMW_business/src/main/java/ch/puzzle/itc/mobiliar/business/*/control/`

**Responsibilities**:
- Implement use case interfaces
- Contain business logic
- Orchestrate domain operations
- Interact with repositories and other services
- Handle transactions (@Transactional)
- Enforce security (@HasPermission)

**Rules**:
- ✅ **DO**: Implement one or more related use case interfaces
- ✅ **DO**: Use `@Stateless` or `@ApplicationScoped` for EJBs
- ✅ **DO**: Inject repositories and other services
- ✅ **DO**: Throw domain exceptions (not HTTP exceptions)
- ✅ **DO**: Apply security annotations (@HasPermission)
- ✅ **DO**: Keep methods focused on single responsibility
- ❌ **DON'T**: Return JAX-RS Response objects
- ❌ **DON'T**: Catch exceptions and build error responses
- ❌ **DON'T**: Perform input validation (commands already validated)
- ❌ **DON'T**: Depend on REST layer classes

**Example**:
```java
@Stateless
public class AppsService implements AddAppUseCase, ListAppsUseCase {

    @Inject
    private ResourceBoundary resourceBoundary;

    @Inject
    private PermissionBoundary permissionBoundary;

    @Override
    @HasPermission(permission = Permission.RESOURCE, action = CREATE)
    public Integer add(AddAppCommand command) throws NotFoundException, IllegalStateException {
        try {
            Application app = resourceBoundary.createNewApplicationWithoutAppServerByName(
                command.getAppName(), 
                command.getReleaseId(), 
                false
            );
            permissionBoundary.createAutoAssignedRestrictions(app.getEntity());
            return app.getId();
        } catch (ElementAlreadyExistsException e) {
            throw new IllegalStateException(e.getMessage());
        } catch (AMWException e) {
            throw new IllegalStateException("Failed to create app: " + command.getAppName(), e);
        }
    }
}
```

## Exception Handling

### Exception Mappers (AMW_rest/exceptions)
**Location**: `/AMW_rest/src/main/java/ch/mobi/itc/mobiliar/rest/exceptions/`

**Purpose**: Translate domain/business exceptions to HTTP responses

**Rules**:
- ✅ **DO**: Create one mapper per exception type
- ✅ **DO**: Use `@Provider` annotation
- ✅ **DO**: Implement `ExceptionMapper<T>`
- ✅ **DO**: Return appropriate HTTP status codes
- ✅ **DO**: Include error details in response body (ExceptionDto)
- ❌ **DON'T**: Add business logic to mappers
- ❌ **DON'T**: Catch exceptions in REST endpoints (let mappers handle them)

**Common Exception to HTTP Status Mappings**:
- `ValidationException` → 400 Bad Request
- `IllegalArgumentException` → 400 Bad Request
- `NotFoundException` / `NoResultException` → 404 Not Found
- `NotAuthorizedException` → 403 Forbidden
- `IllegalStateException` → 409 Conflict (or 400 depending on context)
- `ElementAlreadyExistsException` → 409 Conflict
- `OptimisticLockException` → 409 Conflict
- `ConcurrentModificationException` → 409 Conflict
- Uncaught exceptions → 500 Internal Server Error

**Example Mapper**:
```java
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {
    @Override
    public Response toResponse(ValidationException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                      .entity(new ExceptionDto(exception))
                      .build();
    }
}
```

### Exception Strategy
- **Validation errors**: Throw `ValidationException` (400)
- **Not found**: Throw `NotFoundException` (404)
- **Authorization**: Throw `NotAuthorizedException` (403)
- **Business rule violations**: Throw `IllegalStateException` (409)
- **Conflicts**: Throw `ElementAlreadyExistsException` or `OptimisticLockException` (409)
- **Unexpected errors**: Let them bubble up as 500

## DTO Mapping Strategy

### Overview
DTOs (Data Transfer Objects) are JSON serialization contracts that belong exclusively in the REST layer. They should never leak into the business layer (boundary/control packages).

### DTO Location and Responsibility
**Location**: `/AMW_rest/src/main/java/ch/mobi/itc/mobiliar/rest/dtos/`

**Purpose**:
- Define JSON structure for HTTP requests and responses
- Provide stable API contracts for clients
- Decouple REST API from internal domain model
- Allow API evolution without changing domain

### Mapping Direction

#### Request Flow (Client → Server)
```
HTTP Request → Request DTO → Command Object → Use Case → Domain Logic
```

**Example**:
```java
// 1. Request DTO (in AMW_rest/dtos)
@Getter
@Setter
public class CreateAppRequestDTO {
    @NotNull
    private String appName;
    
    @NotNull
    private Integer releaseId;
}

// 2. REST endpoint maps DTO to Command
@POST
public Response createApp(@Valid CreateAppRequestDTO dto) {
    // Map DTO → Command (in REST layer)
    AddAppCommand command = new AddAppCommand(dto.getAppName(), dto.getReleaseId());
    
    Integer appId = addAppUseCase.add(command);
    return Response.status(CREATED).entity(appId).build();
}
```

#### Response Flow (Server → Client)
```
Domain Entity → Response DTO → HTTP Response
```

**Example**:
```java
// 1. Domain entity (in AMW_business domain)
public class Resource {
    private Integer id;
    private String name;
    private ResourceType type;
    // ... many other fields
}

// 2. Response DTO (in AMW_rest/dtos)
@Getter
@AllArgsConstructor
public class ResourceDTO {
    private Integer id;
    private String name;
    private String typeName;
    
    // Factory method for mapping
    public static ResourceDTO from(Resource resource) {
        return new ResourceDTO(
            resource.getId(),
            resource.getName(),
            resource.getType().getName()
        );
    }
}

// 3. REST endpoint maps Entity → DTO
@GET
@Path("/{resourceId}")
public Response getResource(@PathParam("resourceId") @NotNull Integer resourceId) {
    Resource resource = getResourceUseCase.getById(resourceId);
    
    // Map Domain → DTO (in REST layer)
    ResourceDTO dto = ResourceDTO.from(resource);
    
    return Response.ok(dto).build();
}
```

### DTO Mapping Rules

**✅ DO**:
- Keep all DTOs in `AMW_rest/src/main/java/ch/mobi/itc/mobiliar/rest/dtos/`
- Map DTOs to commands in REST endpoints (for requests)
- Map domain objects to DTOs in REST endpoints (for responses)
- Use static factory methods (`from()`, `toDTO()`) in DTOs for mapping
- Use `@JsonProperty` for field name customization
- Use `@JsonIgnore` to exclude sensitive fields
- Version DTOs when making breaking changes (e.g., `ResourceDTOV2`)
- Keep DTOs simple and focused on serialization

**❌ DON'T**:
- Pass DTOs to use cases (convert to commands first)
- Return DTOs from use cases (return domain objects)
- Put DTOs in boundary or control packages
- Add business logic to DTOs
- Use domain entities as DTOs (expose internal structure)
- Let DTOs reference domain entities directly
- Use the same DTO for request and response if they differ significantly

### Mapping Patterns

#### Pattern 1: Simple Mapping (Static Factory Method)
```java
@Getter
@AllArgsConstructor
public class AppDTO {
    private Integer id;
    private String name;
    
    public static AppDTO from(Application app) {
        return new AppDTO(app.getId(), app.getName());
    }
}
```

#### Pattern 2: Complex Mapping (Dedicated Mapper Class)
For complex mappings with dependencies:
```java
@RequestScoped
public class DeploymentMapper {
    
    @Inject
    private EnvironmentLocator environmentLocator;
    
    public DeploymentDTO toDTO(Deployment deployment) {
        return new DeploymentDTO(
            deployment.getId(),
            deployment.getDeploymentDate(),
            deployment.getState().name(),
            environmentLocator.getEnvironmentName(deployment.getEnvironmentId())
        );
    }
}

// Usage in REST endpoint
@Inject
private DeploymentMapper deploymentMapper;

@GET
@Path("/{id}")
public Response getDeployment(@PathParam("id") Integer id) {
    Deployment deployment = getDeploymentUseCase.getById(id);
    DeploymentDTO dto = deploymentMapper.toDTO(deployment);
    return Response.ok(dto).build();
}
```

#### Pattern 3: Collection Mapping
```java
@GET
public Response listApps() {
    List<Application> apps = listAppsUseCase.list();
    
    List<AppDTO> dtos = apps.stream()
        .map(AppDTO::from)
        .collect(Collectors.toList());
    
    return Response.ok(dtos).build();
}
```

#### Pattern 4: Nested DTOs
```java
@Getter
@AllArgsConstructor
public class AppServerDTO {
    private Integer id;
    private String name;
    private List<AppDTO> applications;
    
    public static AppServerDTO from(AppServer appServer) {
        List<AppDTO> appDTOs = appServer.getApplications().stream()
            .map(AppDTO::from)
            .collect(Collectors.toList());
            
        return new AppServerDTO(
            appServer.getId(),
            appServer.getName(),
            appDTOs
        );
    }
}
```

### When to Use Commands vs DTOs

**Use Commands** (in boundary package):
- For write operations (POST, PUT, DELETE)
- When validation is required
- When input represents a business operation
- When you need immutability and self-validation

**Use DTOs** (in REST package):
- For all HTTP request/response serialization
- When you need Jackson annotations (@JsonProperty, @JsonIgnore)
- When API contract differs from domain model
- For versioning and backward compatibility

**Example - Both Together**:
```java
// Request DTO (REST layer - Jackson serialization)
@Getter
@Setter
public class UpdateResourceRequestDTO {
    @JsonProperty("resource_name")
    private String name;
    
    @JsonProperty("release_id")
    private Integer releaseId;
}

// Command (Boundary layer - validation & business logic)
@Getter
public class UpdateResourceCommand {
    @ValidResourceName
    private final String name;
    
    @NotNull
    private final Integer releaseId;
    
    @NotNull
    private final Integer resourceId;
    
    public UpdateResourceCommand(Integer resourceId, String name, Integer releaseId) {
        this.resourceId = resourceId;
        this.name = name;
        this.releaseId = releaseId;
        validate(this);
    }
}

// REST endpoint - maps DTO to Command
@PUT
@Path("/{resourceId}")
public Response updateResource(
        @PathParam("resourceId") @NotNull Integer resourceId,
        @Valid UpdateResourceRequestDTO dto) {
    
    // DTO → Command conversion
    UpdateResourceCommand command = new UpdateResourceCommand(
        resourceId,
        dto.getName(),
        dto.getReleaseId()
    );
    
    updateResourceUseCase.update(command);
    return Response.ok().build();
}
```

### Summary

| Aspect | DTO | Command |
|--------|-----|---------|
| **Location** | `AMW_rest/dtos/` | `AMW_business/*/boundary/` |
| **Purpose** | HTTP serialization | Business operation input |
| **Validation** | JAX-RS (@Valid, @NotNull) | JSR-303 in constructor |
| **Mutability** | Mutable (setters for Jackson) | Immutable (final fields) |
| **Direction** | Request & Response | Request only |
| **Layer** | REST only | Boundary & Control |
| **Annotations** | Jackson (@JsonProperty) | Validation (@NotNull, custom) |

## Migration Strategy

When refactoring existing code to follow these patterns:

### Step 1: Identify the Use Case
- What business operation is being performed?
- Name it clearly (e.g., `UpdateResourcePropertyUseCase`)

### Step 2: Create Command Object
- Extract all input parameters into a command class
- Add validation annotations
- Place in `boundary` package
- Validate in constructor

### Step 3: Create Use Case Interface
- Define interface in `boundary` package
- Method accepts command, returns result
- Declare exceptions

### Step 4: Implement Use Case
- Create or update service in `control` package
- Implement the use case interface
- Move business logic here
- Throw exceptions (don't build responses)

### Step 5: Update REST Endpoint
- Inject use case interface (not service)
- Create command from request
- Call use case
- Return success response
- Remove manual error handling

### Step 6: Ensure Exception Mappers Exist
- Check if exception mappers exist for all thrown exceptions
- Create missing mappers if needed
- Register in `RESTApplication.java` if required

## Benefits

1. **Testability**: Use cases can be tested independently of HTTP layer
2. **Maintainability**: Clear separation of concerns
3. **Flexibility**: Easy to add new interfaces (GraphQL, gRPC) using same use cases
4. **Consistency**: Standardized error handling across all endpoints
5. **Type Safety**: Commands provide compile-time validation of inputs
6. **Documentation**: Use case interfaces serve as clear API contracts
7. **Security**: Centralized permission checks in use case implementations

## Anti-Patterns to Avoid

❌ **God Services**: Services implementing too many unrelated use cases
❌ **Anemic Commands**: Commands with no validation
❌ **Manual Response Building**: Building error responses in REST or business layer
❌ **Primitive Obsession**: Using primitives instead of command objects
❌ **Layer Violation**: REST calling control directly, or control depending on REST
❌ **Exception Swallowing**: Catching exceptions and returning error responses
❌ **Validation in Multiple Places**: Validate once in command constructor

## Checklist for New Features

When implementing a new REST endpoint:

- [ ] Created command object in `boundary` package with validation
- [ ] Created use case interface in `boundary` package
- [ ] Implemented use case in `control` package (EJB)
- [ ] REST endpoint injects use case interface (not implementation)
- [ ] REST endpoint creates command from request
- [ ] REST endpoint calls use case and returns success response
- [ ] REST endpoint throws exceptions (no manual error handling)
- [ ] Exception mappers exist for all thrown exceptions
- [ ] Use case registered in `RESTApplication.java` (if needed)
- [ ] Tests written for command validation
- [ ] Tests written for use case logic
- [ ] Tests written for REST endpoint

## Related Documentation

- See `@/home/mburri/git/mobi/liima/.windsurf/rules/java.md` for Java development rules
- See `@/home/mburri/git/mobi/liima/.windsurf/rules/testing.md` for testing guidelines
- See `