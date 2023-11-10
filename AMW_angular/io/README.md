# Liima

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 8.3.3.

## Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.

The backend must be up and running. The simplest way is to run it in a docker container - in order to build the image, you must have docker, docker-compose, maven and java 11 installed - build the image with:

```bash
npm run backend:build
```

Note: this will take some time!

Start/ stop the backend container with:

```bash
npm run backend:start
npm run backend:stop
```

## Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory. Use the `--prod` flag for a production build.

## Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Running end-to-end tests

See `AMW_e2e` for end-to-end tests

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI README](https://github.com/angular/angular-cli/blob/master/README.md).

# Icons

The project uses bootstrap 4 and [bootstrap icons](https://icons.getbootstrap.com/).
You can use any icon from the boostrap icon set with the custom `<app-icon>` component:

```
<app-icon icon="clock"></app-icon>
```
