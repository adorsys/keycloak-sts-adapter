#!/bin/bash

cd .. && mvn clean package
cd user-secret-adapter-embedded
# docker-compose -f target/docker-compose.yml build
# docker-compose -f target/docker-compose.yml up
cd target/
tar xzf ~/Downloads/keycloak-3.4.3.Final.tar.gz
cd keycloak-3.4.3.Final/
tar xzf ../user-secret-adapter-embedded-0.2.0-SNAPSHOT-keycloak-3.4.3.Final.tar.gz
cd cli/
../bin/jboss-cli.sh --file=init_keycloak.cli 
cd ..
./bin/add-user-keycloak.sh --user admin --password admin123
./bin/standalone.sh --debug