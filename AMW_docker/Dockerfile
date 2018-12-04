FROM jboss/wildfly:13.0.0.Final
MAINTAINER liimaorg <amw-dev@puzzle.ch>

# adds datasource, mail and security-domain
ARG cli_script=wildfly_h2.cli 
WORKDIR $JBOSS_HOME

# deploy oracle driver
# COPY installs/ojdbc6.jar $JBOSS_HOME/standalone/deployments/ojdbc6.jar

# add configuration
COPY configuration $JBOSS_HOME/standalone/configuration
COPY tmp/AMW.ear $JBOSS_HOME/standalone/deployments/AMW.ear
# to be replaced with COPY --chown=jboss:jboss with docker 17.09
COPY tmp/amwFileDbIntegrationEmpty.h2.db /tmp/amwFileDbIntegrationEmpty.h2.db

RUN $JBOSS_HOME/bin/jboss-cli.sh --file=$JBOSS_HOME/standalone/configuration/${cli_script} && \
    rm -rf $JBOSS_HOME/standalone/configuration/standalone_xml_history && \
    mkdir -p $JBOSS_HOME/standalone/data/amw/logs $JBOSS_HOME/standalone/data/amw/generator $JBOSS_HOME/standalone/data/amw/shakedown && \
    cp /tmp/amwFileDbIntegrationEmpty.h2.db $JBOSS_HOME/standalone/data/amw


CMD ["bin/standalone.sh", "-P", "standalone/configuration/props/amw-system.properties", "-b=0.0.0.0", "-bmanagement=0.0.0.0"]
