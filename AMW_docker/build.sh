#!/bin/bash
set -e

mkdir -p AMW_docker/tmp
cp AMW_ear/target/AMW.ear AMW_docker/tmp
cp AMW_business/src/test/resources/integration-test/testdb/amwFileDbIntegrationEmpty.mv.db AMW_docker/tmp

docker build --pull ${DOCKER_ARGS} -t liimaorg/liima:snapshot AMW_docker
