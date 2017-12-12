FROM registry.access.redhat.com/redhat-sso-7/sso71-openshift:1.2-7

COPY keycloak-storage-provider/target/keycloak-storage-provider.jar /opt/eap/standalone/deployments/keycloak-storage-provider.jar

RUN rm -rf /opt/eap/standalone/configuration/standalone_xml_history

COPY ./docker/openshift-launch.sh /opt/eap/bin/openshift-launch.sh
