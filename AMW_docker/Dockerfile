FROM quay.io/wildfly/wildfly:26.0.0.Final
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
    mkdir -p $JBOSS_HOME/standalone/data/amw/logs $JBOSS_HOME/standalone/data/amw/generator $JBOSS_HOME/standalone/data/amw/shakedown

# add jsf 2.2 for RichFaces
RUN mkdir -p $JBOSS_HOME/modules/com/sun/jsf-impl/mojarra-2.2 && \
    curl https://repo1.maven.org/maven2/com/sun/faces/jsf-impl/2.2.20/jsf-impl-2.2.20.jar > $JBOSS_HOME/modules/com/sun/jsf-impl/mojarra-2.2/jsf-impl.jar && \
    mkdir -p $JBOSS_HOME/modules/javax/faces/api/mojarra-2.2 && \
    curl https://repo1.maven.org/maven2/com/sun/faces/jsf-api/2.2.20/jsf-api-2.2.20.jar> $JBOSS_HOME/modules/javax/faces/api/mojarra-2.2/jsf-api.jar && \
    mkdir -p $JBOSS_HOME/modules/org/jboss/as/jsf-injection/mojarra-2.2 && \
    cp $JBOSS_HOME/modules/system/layers/base/org/jboss/as/jsf-injection/main/wildfly-jsf-injection*.jar $JBOSS_HOME/modules/org/jboss/as/jsf-injection/mojarra-2.2/wildfly-jsf-injection.jar && \
    cp $JBOSS_HOME/modules/system/layers/base/org/jboss/as/jsf-injection/main/weld-jsf*.jar $JBOSS_HOME/modules/org/jboss/as/jsf-injection/mojarra-2.2/weld-jsf.jar
COPY jsf/impl_module.xml $JBOSS_HOME/modules/com/sun/jsf-impl/mojarra-2.2/module.xml
COPY jsf/api_module.xml $JBOSS_HOME/modules/javax/faces/api/mojarra-2.2/module.xml
COPY jsf/injection_module.xml $JBOSS_HOME/modules/org/jboss/as/jsf-injection/mojarra-2.2/module.xml

COPY --chown=jboss:jboss tmp/AMW.ear $JBOSS_HOME/standalone/deployments/AMW.ear
COPY --chown=jboss:jboss tmp/amwFileDbIntegrationEmpty.mv.db $JBOSS_HOME/standalone/data/amw/amwFileDbIntegrationEmpty.mv.db

CMD ["bin/standalone.sh", "-P", "standalone/configuration/props/amw-system.properties", "-b=0.0.0.0", "-bmanagement=0.0.0.0", "--debug", "*:8787"]
