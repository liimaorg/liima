#!/bin/bash
set -e

docker run -i -p 8080:8080 -p 9990:9990 liimaorg/liima:snapshot
