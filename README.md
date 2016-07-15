# SensorThingsServer
A Server implementation of the OGC SensorThings API

# The very short and crude installation instructions

## When installing from Source:

1. Edit SensorThingsService/src/main/webapp/META-INF/context.xml
   and set the correct database url, username and password.
2. mvn clean install
   This should build the war file in SensorThingsService/target/
   If your editor is properly configured you should be able to run the sub-project SensorThingsServer directly,
   Otherwise you can install "from war"


## When installing from WAR:

1. create PostgreSQL database for the data
2. install the postgis extensions in this database (CREATE EXTENSION postgis;)
3. copy the WAR to tomcat.
4. edit $CATALINA_BASE/conf/[enginename]/[hostname]/SensorThingsService.xml
   and set the correct database url, username and password.
5. Browse to http://localhost:8080/SensorThingsService/DatabaseStatus
   This will initialise or update the database.

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


