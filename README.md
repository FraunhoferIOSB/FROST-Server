# FROST-Server [![Build Status](https://travis-ci.org/FraunhoferIOSB/FROST-Server.svg?branch=master)](https://travis-ci.org/FraunhoferIOSB/FROST-Server)

![FROST-Server Logo](https://raw.githubusercontent.com/FraunhoferIOSB/FROST-Server/master/images/FROST-Server-darkgrey.png)
<a href="http://www.opengeospatial.org/resource/products/details/?pid=1371"><img align="right" src ="https://raw.githubusercontent.com/FraunhoferIOSB/FROST-Server/master/images/Certified_OGC_Compliant_Logo_Web.png"/></a>

A Server implementation of the OGC SensorThings API. The **FR**aunhofer **O**pensource **S**ensor**T**hings-Server
is the first complete, open-source implementation of the OGC SensorThings API Part 1: Sensing.


## Compliance Testing Status:

| Conformance Class                     | Reference | Implemented |Test Status |
|:--------------------------------------|:---------:|:-----------:|-----------:|
| Sensing Core                          | A.1       | Yes         |   6 /  6   |
| Filtering Extension                   | A.2       | Yes         |  42 / 42   |
| Create-Update-Delete                  | A.3       | Yes         |  14 / 14   |
| Batch Request                         | A.4       | Yes         |   0 /  0   |
| Sensing MultiDatastream Extension     | A.5       | Yes         |  18 / 18   |
| Sensing Data Array Extension          | A.6       | Yes         |   2 /  2   |
| MQTT Extension for Create and Update  | A.7       | Yes         |   4 /  4   |
| MQTT Extension for Receiving Updates  | A.8       | Yes         |  13 / 13   |

We have extended the official test suit with extra tests that can be found [here](https://github.com/FraunhoferIOSB/ets-sta10).
The official test suit is fully passed.
See the wiki page [features](https://github.com/FraunhoferIOSB/FROST-Server/wiki/Features) for more details.

## The very short and crude installation instructions

See the [wiki](https://github.com/FraunhoferIOSB/FROST-Server/wiki) for longer installation instructions.

### Compiling

1. Checkout the project from github: `git clone https://github.com/FraunhoferIOSB/FROST-Server.git`
2. Go to the project root (The top-most directory with a pom.xml) `cd FROST-Server`
3. Give the command `mvn clean install`
   This should build the war file in `FROST-Server.HTTP/target/`


### Database installation

1. create PostgreSQL database for the data
2. install the postgis extensions in this database (CREATE EXTENSION postgis;)

### Database configuration

The database connection is configured from the [Context](http://tomcat.apache.org/tomcat-8.0-doc/config/context.html)
entry either in server.xml, in `$CATALINA_BASE/conf/[enginename]/[hostname]/appname.xml`
or in `/META-INF/context.xml` inside the war file. If you are running the application
from your IDE, it is easiest to change the context.xml file in the war file.

1. Copy the [Postgres JDBC jar](http://repo.maven.apache.org/maven2/org/postgresql/postgresql/9.4.1212/postgresql-9.4.1212.jar)
and the [postgis jar](http://repo.maven.apache.org/maven2/net/postgis/postgis-jdbc/2.2.1/postgis-jdbc-2.2.1.jar)
to `$CATALINA_HOME/lib`.
2. Configure the database resource. Either in the Context, or elsewhere in server.xml:

        <Resource
            name="jdbc/sensorThings" auth="Container"
            type="javax.sql.DataSource" driverClassName="org.postgresql.Driver"
            url="jdbc:postgresql://localhost:5432/sensorthings"
            username="sensorthings" password="ChangeMe"
            maxTotal="20" maxIdle="10" maxWaitMillis="-1"/>


### Database initialisation or upgrade

1. Choose which backend to use, by configuring the persistence.persistenceManagerImplementationClass option:
    * Default value, using Long values for entity ids, generated in sequence: de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.PostgresPersistenceManagerLong
    * Using String values for entity ids, with new values generated using uuid_generate_v1mc(): de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.PostgresPersistenceManagerString
    * Using uuid values for entity ids, with new values generated using uuid_generate_v1mc(): de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.PostgresPersistenceManagerUuid
2. Browse to http://localhost:8080/FROST/DatabaseStatus

This should initialise/update the database to the latest version and the service
should be ready for use.


### Performance and Indices

By default, only primary and foreign keys have indices on them. If your database grows large
and you notice a significant slowdown, you should check which queries you use most, and
add indices for those queries. A very common one is probably for
Datastreams(x)/observations?$orderby=phenomenonTime desc

```
CREATE INDEX "OBS-DS_ID-PHTIME_SE-O_ID"
  ON "OBSERVATIONS"
  USING btree
  ("DATASTREAM_ID", "PHENOMENON_TIME_START" DESC, "PHENOMENON_TIME_END" DESC, "ID");
```


### Configuration options

The server is configured from the [Context](http://tomcat.apache.org/tomcat-8.0-doc/config/context.html)
entry either in server.xml, in `$CATALINA_BASE/conf/[enginename]/[hostname]/appname.xml`
or in `/META-INF/context.xml` inside the war file. If you are running the application
from your IDE, it is easiest to change the context.xml file in the war file. It has
the following options:

* SensorThings API settings
  * `serviceRootUrl`: The base URL of the SensorThings Server without version.
  * `defaultCount`: The default value for the $count query option.
  * `defaultTop`: The default value for the $top query option.
  * `maxTop`: The maximum allowed value for the $top query option.
  * `useAbsoluteNavigationLinks`: If true, navigationLinks are absolute, otherwise relative.
* MQTT settings
  * `mqtt.mqttServerImplementationClass`: The java class used for running the MQTT server (must implement MqttServer interface)
  * `mqtt.Enabled`: Specifies wether MQTT support will be enabled or not.
  * `mqtt.Port`: The port the MQTT server runs on.
  * `mqtt.QoS`: Quality of Service Level for MQTT messages.
  * `mqtt.SubscribeMessageQueueSize`: Queue size for messages to be pubslihed via MQTT.
  * `mqtt.SubscribeThreadPoolSize`: Number of threads use to dispatch MQTT notifications.
  * `mqtt.CreateMessageQueueSize`: Queue size for create observation requests via MQTT .
  * `mqtt.CreateThreadPoolSize`: Number of threads use to dispatch observation creation requests.
  * `mqtt.Host`: The external IP address or host name the MQTT server should listen on. Set to 0.0.0.0 to listen on all interfaces.
  * `mqtt.internalHost`: The internal host name of the MQTT server.
  * `mqtt.WebsocketPort`: The port the MQTT server is reachable via WebSocket.
* persistence settings
  * `persistence.persistenceManagerImplementationClass`: The java class used for persistence (must implement PersistenceManaher interface)
  * `persistence.alwaysOrderbyId`: Always add an 'orderby=id asc' to queries to ensure consistent paging.
  * `persistence.db_jndi_datasource`: JNDI data source name

## Docker support

There's also the possibility to run FROST-Server and the needed database inside a Docker container.
This dependency is specified inside the ```docker-compose.yaml``` file.

You can use the prebuild [docker image](https://hub.docker.com/r/fraunhoferiosb/frost-server/) by
running ```docker-compose up```. This will download the latest version of the FROST-Server and starts it
together with the needed database. You can access the server by opening ```http://localhost:8080/FROST-Server/``` in your browser.

If you want to build your own docker image, you can do this by calling ```mvn dockerfile:build -pl FROST-Server```.

All data is stored inside the PostGIS database. To keep this state there's a volume automatically mapped to the PostGIS container.
For more information see the ```docker-compose.yaml``` file and the [PostGIS container documentation](https://hub.docker.com/r/mdillon/postgis/)

To have a proper configuration of your server you need to create your own `context.xml` file.
You can use `FROST-Server.HTTP/src/main/webapp/META-INF/context.xml` as template.
Make sure your adapted file is located in `$CATALINA_HOME/conf/Catalina/localhost/FROST-Server.xml`.

## Standalone Spring Boot

If you prefer to not use Tomcat, [Kinota Server](https://github.com/kinota/kinota-server) is a
 Spring Boot application that makes it easy to run Fraunhofer IOSB FROST-Server in cloud environments.


## Wildfly 10

If you prefer not to use Tomcat but [Wildfly](https://github.com/wildfly/wildfly) it is possible to run FROST-Server on it.

Create a directory $WILDFLY_HOME/modules/org/postgresql/main and add both [Postgres JDBC jar](http://repo.maven.apache.org/maven2/org/postgresql/postgresql/9.4.1212/postgresql-9.4.1212.jar)
and the [postgis jar](http://repo.maven.apache.org/maven2/net/postgis/postgis-jdbc/2.2.1/postgis-jdbc-2.2.1.jar) to it. Create a file named module.xml and add the following:

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

Add a datasource to $WILDFLY_HOME/standalone/configuration/[standalone.xml]
Example:

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


# Authors

Hylke van der Schaaf
hylke.vanderschaaf@iosb.fraunhofer.de

Michael Jacoby
michael.jacoby@iosb.fraunhofer.de


# License

Copyright (C) 2016 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
Karlsruhe, Germany.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
