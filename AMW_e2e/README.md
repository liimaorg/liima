# Liima End to End Tests

This module contains end-to-end tests using [cypress](https://www.cypress.io/) 

## Getting started

Install all dependencies:

```bash
npm install
```

## Run the application 

To run the e2e test, you need a running application on http://localhost:8080/AMW_web

You can use the docker image to get an application with a fresh h2 database:

```bash
npm run docker:build
```

Start and stop the application manually:

```bash
npm start
npm stop
```

Note: there are other ways to start the application. Using docker is the most convenient way if you want to focus on written end-to-end tests.

## Using the cypress ui

You can run the cypress ui with the following command:

```bash
npm run test:ui
```

This will start the cypress ui that lets you run individual tests and inspect the runs. You have to choose the test type and environment first. 
Note: only e2e tests are setup up for this project, no component testing.

See https://docs.cypress.io/guides/getting-started/opening-the-app for more information.


## Run tests in headless mode

To just run all tests, without the ui use:

```bash
npm run test
```

## Run tests on CI

Start the docker container, wait for the application startup and execute all tests with:

```bash
npm run test:ci
```

Notes: 
* the container will not shut down when the tests are finished.
* you may need to build a new docker image if changes to your application where made.


