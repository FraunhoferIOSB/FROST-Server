FROM tomcat:10-jre21

RUN apt-get update && apt-get install unzip && apt-get clean

# Copy to images tomcat path
ARG ARTIFACT_FILE
COPY target/${ARTIFACT_FILE} /tmp/FROST-Server.war

RUN unzip -d ${CATALINA_HOME}/webapps/FROST-Server /tmp/FROST-Server.war \
    && rm /tmp/FROST-Server.war \
    && groupadd --system --gid 1001 tomcat \
    && useradd --system --uid 1001 --gid 1001 tomcat \
    && chgrp -R 0 $CATALINA_HOME \
    && chmod -R g=u $CATALINA_HOME \
    && chown tomcat:tomcat $JAVA_HOME/lib/security/cacerts

COPY target/docker_deps/ ${CATALINA_HOME}/webapps/FROST-Server/WEB-INF/lib/

USER tomcat
