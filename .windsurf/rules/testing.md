---
trigger: always_on
description: 
globs: 
---

# Testing Guidelines

## General Testing Principles
- **Never delete or weaken tests without explicit direction**
- Tests should be isolated and repeatable
- Each test should test one thing
- Use descriptive test names that explain what is being tested
- Follow AAA pattern: Arrange, Act, Assert

## Java Testing

### Framework
- **JUnit 5** (Jupiter) - version 5.14.1
- **Mockito** - version 5.21.0 for mocking
- **AssertJ** - version 3.27.7 for fluent assertions
- **Hamcrest** - version 3.0 for matchers

### Test Configuration
- Tests run with `useSystemClassLoader=false`
- Fork count: 1
- Reuse forks: true
- Max heap size: 4096m

### Test Structure
```java
@Test
void shouldDoSomething() {
    // Arrange
    // Set up test data and mocks
    
    // Act
    // Execute the code under test
    
    // Assert
    // Verify the results
}
```

### Mocking with Mockito
```java
@ExtendWith(MockitoExtension.class)
class MyServiceTest {
    @Mock
    private Dependency dependency;
    
    @InjectMocks
    private MyService service;
    
    @Test
    void testMethod() {
        when(dependency.method()).thenReturn(value);
        // test logic
        verify(dependency).method();
    }
}
```

### Assertions
- Prefer AssertJ for fluent assertions
- Use meaningful assertion messages
- Test both happy path and error cases
- Test edge cases and boundary conditions

### Coverage
- Code coverage tracked via JaCoCo
- Generate reports: `mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent install`
- Coverage reports in `target/site/jacoco/`

## Angular Testing

### Framework
- **Vitest** - version 4.0.16
- **jsdom** - version 28.1.0 for DOM simulation

### Test Commands
```bash
# Run tests once
npm test
ng test --watch=false

# Watch mode
npm run test:watch
ng test

# Maven integration
npm run maventest
```

### Test Structure
- Unit tests for components
- Unit tests for services
- Integration tests where appropriate
- Mock HTTP calls and external dependencies

### Best Practices
- Test component behavior, not implementation details
- Use TestBed for component testing
- Mock services and dependencies
- Test user interactions
- Test error handling
- Test edge cases

## Test Organization
- Keep tests close to the code they test
- Use descriptive test file names (*.test.ts, *Test.java)
- Group related tests in describe/nested blocks
- Use setup and teardown methods appropriately

## Continuous Integration
- All tests must pass before merging
- Tests run automatically in CI/CD pipeline
- Failed tests block the build
- Coverage reports generated on each build

## When to Write Tests
- Write tests for new features
- Write tests when fixing bugs (regression tests)
- Update tests when changing existing functionality
- Add tests for edge cases discovered during development

## What to Test
- Business logic
- Data transformations
- Error handling
- Edge cases and boundary conditions
- Integration points
- User interactions (in UI)

## What Not to Test
- Third-party library internals
- Framework internals
- Trivial getters/setters (unless they have logic)
- Auto-generated code

## Debugging Tests
- Use descriptive test names
- Add meaningful assertion messages
- Use debugger when needed
- Isolate failing tests
- Check test data and mocks
- Verify test environment setup
