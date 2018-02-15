From tomcat:8-jre8

ADD http://repo.maven.apache.org/maven2/org/postgresql/postgresql/9.4.1212/postgresql-9.4.1212.jar /usr/local/tomcat/lib/
ADD http://repo.maven.apache.org/maven2/net/postgis/postgis-jdbc/2.2.1/postgis-jdbc-2.2.1.jar /usr/local/tomcat/lib/

# Copy to images tomcat path
ARG WAR_FILE
ADD target/${WAR_FILE} /usr/local/tomcat/webapps/FROST-Server.war
