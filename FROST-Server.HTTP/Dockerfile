From tomcat:8.5-jdk11

COPY target/docker_deps/ /usr/local/tomcat/lib/

# Copy to images tomcat path
ARG WAR_FILE
COPY target/${WAR_FILE} /usr/local/tomcat/webapps/FROST-Server.war
