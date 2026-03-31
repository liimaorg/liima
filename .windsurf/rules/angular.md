---
trigger: always_on
description: 
globs: 
---

# Angular Development Rules

## Angular Version
- **Angular**: 21.1.5
- **TypeScript**: 5.9.3
- **Node.js**: Managed via nvm (see .tool-versions)

## Important: Node Setup
**ALWAYS run `nvm use` before executing any npm or Angular CLI commands!**
This ensures the correct Node.js version is active.

## Project Location
- Angular application is in: `AMW_angular/io/`
- Working directory for Angular commands: `cd AMW_angular/io`

## UI Framework
- **Bootstrap**: 5.3.8
- **ng-bootstrap**: 20.0.0
- **Bootstrap Icons**: 1.13.1
- **@ng-select/ng-select**: 21.1.3
- **@popperjs/core**: 2.11.8

## Code Editor
- **CodeMirror**: 6.0.2
- **@codemirror/merge**: 6.11.2
- **@codemirror/theme-one-dark**: 6.1.3

## Development Commands
```bash
# Always run first!
nvm use

# Install dependencies
npm install

# Start dev server
npm start
# or
ng serve

# Build for development
npm run build
# or
ng build

# Build for production (Maven integration)
npm run mavenbuild
# or
ng build --configuration production

# Run tests
npm test
# or
ng test

# Run tests once (Maven integration)
npm run maventest
# or
ng test --watch=false

# Watch tests
npm run test:watch

# Lint code
npm run lint
# or
ng lint

# Format code
npm run prettier
```

## Backend Integration
```bash
# Build backend
npm run backend:build

# Start backend (Docker)
npm run backend:start

# Stop backend
npm run backend:stop
```

## Code Quality Tools
- **Linting**: ESLint 9.39.3 with Angular ESLint
- **Formatting**: Prettier 3.7.4
- **Pre-commit**: Husky 9.1.7 with git-format-staged
- **Testing**: Vitest 4.0.16

## Angular Best Practices
- Use standalone components (Angular 21+ default)
- Prefer reactive forms over template-driven forms
- Use Angular signals for state management where appropriate
- Follow Angular style guide
- Use dependency injection properly
- Implement proper error handling
- Use RxJS operators efficiently

## Code Style
- Follow ESLint configuration (eslint-config-prettier)
- Use Prettier for consistent formatting
- TypeScript strict mode enabled
- Use proper typing (avoid `any` unless necessary)

## Module Structure
- Use feature modules for organization
- Lazy load routes where appropriate
- Keep components focused and single-purpose
- Use services for business logic and API calls

## Testing
- Write unit tests for components and services
- Use Vitest for testing
- Mock dependencies appropriately
- Aim for good test coverage
- Tests should be isolated and repeatable

## Build Integration
- Maven calls `npm run mavenbuild` for production builds
- Maven calls `npm run maventest` for test execution
- Build output is packaged into WAR file
- Production builds are optimized and minified

## Dependencies
- Use npm for package management
- Keep dependencies up to date
- Check for security vulnerabilities regularly
- Use exact versions in package.json where stability is critical
