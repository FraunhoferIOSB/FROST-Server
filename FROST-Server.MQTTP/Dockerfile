From tomcat:8.5-jdk11

COPY target/docker_deps/ ${CATALINA_HOME}/lib/

# Copy to images tomcat path
ARG WAR_FILE
COPY target/${WAR_FILE} ${CATALINA_HOME}/webapps/FROST-Server.war

RUN addgroup --system --gid 1000 tomcat \
    && adduser --system --uid 1000 --gid 1000 tomcat \
    && chown -R tomcat $CATALINA_HOME

USER tomcat