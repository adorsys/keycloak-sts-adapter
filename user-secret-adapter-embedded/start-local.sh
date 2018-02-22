#!/bin/bash

# Download the keycloak distribution and set the corresponding env variable
# export KEYCLOAK_DIST=~/Downloads/keycloak-3.4.3.Final.tar.gz

if [ "y$KEYCLOAK_DIST" = "y" ];
        then
                echo "Missing param KEYCLOAK_DIST"
                echo "Download the keycloak distribution and set the corresponding env variable"
                exit 1
fi

echo "KEYCLOAK_DIST $KEYCLOAK_DIST"

export WORK_DIR=$(pwd)

cd .. && mvn clean package

cd $WORK_DIR/target && tar xzf $KEYCLOAK_DIST

export KEYCLOACK_HOME=$WORK_DIR/target/keycloak-3.4.3.Final

cd $KEYCLOACK_HOME && tar xzf $WORK_DIR/target/user-secret-adapter-embedded-0.2.0-SNAPSHOT-keycloak-3.4.3.Final.tar.gz

cd $KEYCLOACK_HOME/cli && $KEYCLOACK_HOME/bin/jboss-cli.sh --file=init_keycloak.cli 

$KEYCLOACK_HOME/bin/add-user-keycloak.sh --user admin --password admin123

$KEYCLOACK_HOME/bin/standalone.sh --debug -DSTS_RESOURCE_SERVERS_CONFIG_FILE=$KEYCLOACK_HOME/cli/SAMPLE_STS_RESOURCE_SERVERS_CONFIG_FILE.properties
