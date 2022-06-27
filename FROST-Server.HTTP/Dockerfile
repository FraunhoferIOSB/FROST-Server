From tomcat:9-jdk17

COPY target/docker_deps/ ${CATALINA_HOME}/lib/

# Copy to images tomcat path
ARG ARTIFACT_FILE
COPY target/${ARTIFACT_FILE} /tmp/FROST-Server.war

RUN apt-get update && apt-get install unzip && apt-get clean \
    && unzip -d ${CATALINA_HOME}/webapps/FROST-Server /tmp/FROST-Server.war \
    && rm /tmp/FROST-Server.war \
    && addgroup --system --gid 1000 tomcat \
    && adduser --system --uid 1000 --gid 1000 tomcat \
    && chgrp -R 0 $CATALINA_HOME \
    && chmod -R g=u $CATALINA_HOME

USER tomcat
