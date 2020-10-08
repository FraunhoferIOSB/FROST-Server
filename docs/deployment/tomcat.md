---
layout: default
title: Tomcat deployment
category: deployment
order: 12
---

# Tomcat or Wildfly

There are several ways to deploy the different packages the make up the FROST-Server.
The HTTP and all-in-one MQTTP packages can be run in Tomcat or Wildfly (see below), or as a [docker image](docker.md).


## Tomcat

When deploying on a new or existing Tomcat or Wildfly installation, the following steps can be folowed.
You will also need a PostgreSQL server with PostGIS extensions, see [PostgreSQL Setup](postgresql.md).

1. Copy the [Postgres JDBC jar](https://repo.maven.apache.org/maven2/org/postgresql/postgresql/42.2.16/postgresql-42.2.16.jar)
   and the [postgis jar](https://repo.maven.apache.org/maven2/net/postgis/postgis-jdbc/2.4.0/postgis-jdbc-2.4.0.jar) to `$CATALINA_HOME/lib`.
2. Configure the database resource. Either in the Context, or elsewhere in server.xml:

  ```xml
        <Resource
            name="jdbc/sensorThings" auth="Container"
            type="javax.sql.DataSource" driverClassName="org.postgresql.Driver"
            url="jdbc:postgresql://localhost:5432/sensorthings"
            username="sensorthings" password="ChangeMe"
            maxTotal="20" maxIdle="10" maxWaitMillis="-1"/>
  ```

For tomcat, configuration options like the `persistence.persistenceManagerImplementationClass` option mentioned below, are taken from (in order of priority):

1. Enviroment variables
1. The [Context](http://tomcat.apache.org/tomcat-8.0-doc/config/context.html) entry either in
   * server.xml
   * `$CATALINA_BASE/conf/[enginename]/[hostname]/appname.xml`
   * `/META-INF/context.xml` inside the war file.
1. web.xml

Next, read below for Database initialisation or upgrade.

## Wildfly

* Create a directory `$WILDFLY_HOME/modules/org/postgresql/main`
* add both [Postgres JDBC jar](https://repo.maven.apache.org/maven2/org/postgresql/postgresql/42.2.16/postgresql-42.2.16.jar)
  and the [postgis jar](https://repo.maven.apache.org/maven2/net/postgis/postgis-jdbc/2.4.0/postgis-jdbc-2.4.0.jar) to it.
* Create a file named module.xml and add the following:

  ```xml
  <?xml version="1.0" encoding="UTF-8"?>
  <module xmlns="urn:jboss:module:1.0" name="org.postgresql">
    <resources>
      <resource-root path="postgresql-42.2.16.jar"/>
      <resource-root path="postgis-jdbc-2.4.0.jar"/>
    </resources>
    <dependencies>
      <module name="javax.api"/>
      <module name="javax.transaction.api"/>
    </dependencies>
  </module>
  ```
* Add a datasource to `$WILDFLY_HOME/standalone/configuration/[standalone.xml]`
  ```xml
  <subsystem xmlns="urn:jboss:domain:datasources:4.0">
    <datasources>
      <datasource jta="true" jndi-name="java:/comp/env/jdbc/sensorThings" pool-name="Sensorthings" enabled="true" use-ccm="true">
          <connection-url>jdbc:postgresql://localhost:5432/sensorthings</connection-url>
          <driver-class>org.postgresql.Driver</driver-class>
          <driver>postgres</driver>
          <security>
            <user-name>sensorthings</user-name>
            <password>ChangeMe</password>
          </security>
          <validation>
          <valid-connection-checker class-name="org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLValidConnectionChecker"/>
            <background-validation>true</background-validation>
            <exception-sorter class-name="org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLExceptionSorter"/>
          </validation>
      </datasource>
    </datasources>
  </subsystem>
  ```

For Wildfly, configuration options like the `persistence.persistenceManagerImplementationClass` option mentioned below, are taken from (in order of priority):

1. Enviroment variables
1. web.xml


## Database initialisation or upgrade

1. Choose which backend to use, by configuring the `persistence.persistenceManagerImplementationClass` option:
   * `de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.PostgresPersistenceManagerLong`  
     Default value, using Long values for entity ids, generated in sequence
   * `de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.PostgresPersistenceManagerString`  
     Using String values for entity ids, with new values generated using uuid_generate_v1mc()
   * `de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.PostgresPersistenceManagerUuid`  
     Using uuid values for entity ids, with new values generated using uuid_generate_v1mc()
2. Browse to http://localhost:8080/FROST/DatabaseStatus
3. Click the upgrade button

This should initialise/update the database to the latest version and the service is ready for use.

Now you may want to have a look at [PostgreSQL Setup](postgresql.md) or [Performance tips for PostgreSQL and PostGIS](db_performance.md).

