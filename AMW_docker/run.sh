#!/bin/bash
set -e

docker run -p 8080:8080 -p 9990:9990 -i liima/liima:snapshot
