FROM jboss/keycloak:${keycloak.version}

ENV DB_VENDOR POSTGRES

ADD ${project.build.finalName}-${project.version}-keycloak-${keycloak.version}.tar.gz /opt/jboss/keycloak

RUN cd /opt/jboss/keycloak/cli/ && /opt/jboss/keycloak/bin/jboss-cli.sh --file=init_keycloak.cli

RUN rm -rf /opt/jboss/keycloak/standalone/configuration/standalone_xml_history
