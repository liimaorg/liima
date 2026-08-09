FROM quay.io/wildfly/wildfly:26.1.3.Final-jdk11
LABEL MAINTAINER="liimaorg <amw-dev@puzzle.ch>"

ARG DB_CLI_SCRIPT=wildfly_h2.cli 

WORKDIR $JBOSS_HOME
# deploy oracle driver
# COPY installs/ojdbc6.jar $JBOSS_HOME/standalone/deployments/ojdbc6.jar

# add configuration
COPY --chown=jboss:jboss configuration $JBOSS_HOME/standalone/configuration

RUN $JBOSS_HOME/bin/jboss-cli.sh --file=$JBOSS_HOME/standalone/configuration/wildfly_base.cli && \
    $JBOSS_HOME/bin/jboss-cli.sh --file=$JBOSS_HOME/standalone/configuration/$DB_CLI_SCRIPT && \
    rm -rf $JBOSS_HOME/standalone/configuration/standalone_xml_history && \
    mkdir -p $JBOSS_HOME/standalone/data/amw/logs $JBOSS_HOME/standalone/data/amw/generator

COPY --chown=jboss:jboss tmp/AMW.ear $JBOSS_HOME/standalone/deployments/AMW.ear
COPY --chown=jboss:jboss tmp/amwFileDbIntegrationEmpty.mv.db $JBOSS_HOME/standalone/data/amw/amwFileDbIntegrationEmpty.mv.db

CMD ["bin/standalone.sh", "-P", "standalone/configuration/props/amw-system.properties", "-b=0.0.0.0", "-bmanagement=0.0.0.0", "--debug", "*:8787"]
