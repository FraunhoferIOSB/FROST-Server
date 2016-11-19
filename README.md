# SensorThingsServer
A Server implementation of the OGC SensorThings API.

## Compliance Testing Status:

| Conformance Class                     | Reference | Implemented |Test Status         |
|---------------------------------------|-----------|-------------|--------------------|
| Sensing Core                          | A.1       | Yes         | 6 / 6              |
| Filtering Extension                   | A.2       | Yes         | 23 / 23            |
| Create-Update-Delete                  | A.3       | Yes         | 9 / 9              |
| Batch Request                         | A.4       | No          | No tests available |
| Sensing MultiDatastream Extension     | A.5       | Yes         | 16 / 16            |
| Sensing Data Array Extension          | A.6       | Yes         | 2 / 2              |
| MQTT Extension for Create and Update  | A.7       | Yes         | 4 / 4              |
| MQTT Extension for Receiving Updates  | A.8       | Yes         | 13 / 13            |

We have extended the official test suit with extra tests. The official test suit is fully passed.
See the [features](FEATURES.md) for more details.

## The very short and crude installation instructions

### Database installation

1. create PostgreSQL database for the data
2. install the postgis extensions in this database (CREATE EXTENSION postgis;)

### Database configuration

The database connection is configured from the [Context](http://tomcat.apache.org/tomcat-8.0-doc/config/context.html)
entry either in server.xml, in `$CATALINA_BASE/conf/[enginename]/[hostname]/appname.xml`
or in `/META-INF/context.xml` inside the war file. If you are running the application
from your IDE, it is easiest to change the context.xml file in the war file.

There are two ways to configure the database: Using [JNDI](http://tomcat.apache.org/tomcat-8.0-doc/jndi-datasource-examples-howto.html#PostgreSQL)
or directly.

#### JNDI

This method uses connection pooling and is faster.

1. Copy the [Postgres JDBC jar](http://repo.maven.apache.org/maven2/org/postgresql/postgresql/9.4.1209.jre7/postgresql-9.4.1209.jre7.jar)
and the [postgis jar](http://repo.maven.apache.org/maven2/net/postgis/postgis-jdbc/2.2.0/postgis-jdbc-2.2.0.jar)
to `$CATALINA_HOME/lib`.
2. Configure the database resource. Either in the Context, or elsewhere in server.xml:

        <Resource
            name="jdbc/sensorThings" auth="Container"
            type="javax.sql.DataSource" driverClassName="org.postgresql.Driver"
            url="jdbc:postgresql://localhost:5432/sensorthings"
            username="sensorthings" password="ChangeMe"
            maxTotal="20" maxIdle="10" maxWaitMillis="-1"/>

3. Tell the application how to find the datasource in the Context:

        <Parameter name="db_jndi_datasource" value="jdbc/sensorThings" description="JNDI data source name"/>

   The value of the Parameter and the name of the Resource have to be the same, but
   can be anything you like.

#### Direct database connection

This method does not support connection pooling.

1. Copy the [Postgres JDBC jar](http://repo.maven.apache.org/maven2/org/postgresql/postgresql/9.4.1209.jre7/postgresql-9.4.1209.jre7.jar)
   and the [postgis jar](http://repo.maven.apache.org/maven2/net/postgis/postgis-jdbc/2.2.0/postgis-jdbc-2.2.0.jar)
   to `WEB-INF/lib` or `$CATALINA_HOME/lib`.
2. Configure the database resource in the Context:

        <Parameter name="db_driver" value="org.postgresql.Driver" description="Database driver classname"/>
        <Parameter name="db_url" value="jdbc:postgresql://localhost:5432/sta" description="Database connection URL"/>
        <Parameter name="db_username" value="postgres" description="Database username"/>
        <Parameter name="db_password" value="1qay!QAY" description="Database password"/>


### Compiling

1. Go to the project root (The top-most directory with a pom.xml)
2. mvn clean install
   This should build the war file in SensorThingsServer/target/


### Database initialisation or upgrade:

1. Browse to http://localhost:8080/SensorThingsService/DatabaseStatus

This should initialise/update the database to the latest version and the service
should be ready for use.


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


