---
layout: default
title: PostgreSQL Setup
category: Deployment
order: 3
---

# Database: PostgreSQL with PostGIS

The FROST-Server needs a database to store its data. Currently only [PostgreSQL](https://www.postgresql.org/) with the [PostGIS](https://postgis.net/) extensions is supported.

## Database installation

1. create PostgreSQL database for the data
2. install the postgis extensions in this database (`CREATE EXTENSION postgis;`)
3. Optional: If you want to use UUIDs as entity ids, you need the uuid-ossp extension (`CREATE EXTENSION "uuid-ossp";`)

## Database configuration

The HTTP and all-in-one MQTTP packages use JNDI to get a database resource from Tomcat or Wildfly. You have to configure
a resource in Tomcat/Wildfly, and then tell the FROST-Server what name you gave this resource.
The default name is `jdbc/sensorThings`.

### Tomcat

1. Copy the [Postgres JDBC jar](https://repo.maven.apache.org/maven2/org/postgresql/postgresql/9.4.1212/postgresql-9.4.1212.jar)
   and the [postgis jar](https://repo.maven.apache.org/maven2/net/postgis/postgis-jdbc/2.2.1/postgis-jdbc-2.2.1.jar) to `$CATALINA_HOME/lib`.
2. Configure the database resource. Either in the Context, or elsewhere in server.xml:

        <Resource
            name="jdbc/sensorThings" auth="Container"
            type="javax.sql.DataSource" driverClassName="org.postgresql.Driver"
            url="jdbc:postgresql://localhost:5432/sensorthings"
            username="sensorthings" password="ChangeMe"
            maxTotal="20" maxIdle="10" maxWaitMillis="-1"/>

### Wildfly

* Create a directory `$WILDFLY_HOME/modules/org/postgresql/main`
* add both [Postgres JDBC jar](https://repo.maven.apache.org/maven2/org/postgresql/postgresql/9.4.1212/postgresql-9.4.1212.jar)
  and the [postgis jar](https://repo.maven.apache.org/maven2/net/postgis/postgis-jdbc/2.2.1/postgis-jdbc-2.2.1.jar) to it.
* Create a file named module.xml and add the following:

  ```xml
  <?xml version="1.0" encoding="UTF-8"?>
  <module xmlns="urn:jboss:module:1.0" name="org.postgresql">
    <resources>
      <resource-root path="postgresql-9.4.1212.jar"/>
      <resource-root path="postgis-jdbc-2.2.1.jar"/>
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

### Performance and Indices

By default, only primary and foreign keys have indices on them. If your database grows large
and you notice a significant slowdown, you should check which queries you use most, and
add indices for those queries. A very common one is probably for
Datastreams(x)/observations?$orderby=phenomenonTime asc

```sql
CREATE INDEX "OBS-DS_ID-PHTIME_SE-O_ID"
  ON "OBSERVATIONS"
  USING btree
  ("DATASTREAM_ID", "PHENOMENON_TIME_START" ASC, "PHENOMENON_TIME_END" ASC);
```


