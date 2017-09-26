# Liima Docker image

The images includes the h2 test database with sample data. The ear file has to be built beforehand.  
User/PW is found in configuration/props/application-users.properties  
This images is meant for demo and testing purposes and is not production ready, data is stored in the embedded h2 database in the container and will be lost if the container is removed.

## Building

    AMW_docker/build.sh

## Start the container

    AMW_docker/run.sh
    