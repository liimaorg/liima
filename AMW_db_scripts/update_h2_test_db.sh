#!/usr/bin/env bash

mvn compile -Pliquibase -Ddb=h2.test -Dgoal=update