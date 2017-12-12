#!/usr/bin/env bash

mvn compile -Pliquibase,h2 -Ddb=h2.test