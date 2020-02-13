From tomcat:8-jre8

ADD https://repo.maven.apache.org/maven2/org/postgresql/postgresql/42.2.5/postgresql-42.2.5.jar /usr/local/tomcat/lib/
ADD https://repo.maven.apache.org/maven2/net/postgis/postgis-jdbc/2.3.0/postgis-jdbc-2.3.0.jar /usr/local/tomcat/lib/

# Copy to images tomcat path
ARG WAR_FILE
ADD target/${WAR_FILE} /usr/local/tomcat/webapps/FROST-Server.war
