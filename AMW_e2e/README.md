# Liima End to End Tests

This module contains end-to-end tests using [playwright](https://playwright.dev/) 

## Getting started

Install all dependencies:

```bash
pnpm install
npx playwright install --with-deps
```

## Build the application

The playwright tests run against a docker-build of the liima-application. Therefore you have to build it for the first time.

```bash
pnpm run docker:build
pnpm start
```

## Run tests

Run all tests with: 

```bash
pnpm run test
```
With this command the liima-container is started and the test run against it.

## Using the playwright ui

You can run the playwright ui with the following command:

```bash
pnpm run test:ui
```

This will start the playwright ui that lets you run individual tests and inspect the runs. 

## Run tests on CI

Start the docker container, wait for the application startup and execute all tests with:

```bash
pnpm run test:ci
```

Notes: 
* the container will not shut down when the tests are finished.
* you may need to build a new docker image if changes to your application where made.


