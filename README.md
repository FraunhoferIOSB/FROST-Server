# FROST-Server [![Build Status](https://github.com/FraunhoferIOSB/FROST-Server/workflows/Maven%20Build/badge.svg)](https://github.com/FraunhoferIOSB/FROST-Server/actions) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/da22d7347eb7450a955274916ed2d336)](https://www.codacy.com/gh/FraunhoferIOSB/FROST-Server?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=FraunhoferIOSB/FROST-Server&amp;utm_campaign=Badge_Grade) [![Docker badge](https://img.shields.io/docker/pulls/fraunhoferiosb/frost-server-http.svg)](https://hub.docker.com/r/fraunhoferiosb/frost-server-http/)

![FROST-Server Logo](https://raw.githubusercontent.com/FraunhoferIOSB/FROST-Server/master/docs/images/FROST-Server-darkgrey.png)
<a href="http://www.opengeospatial.org/resource/products/details/?pid=1371"><img align="right" src ="https://raw.githubusercontent.com/FraunhoferIOSB/FROST-Server/master/docs/images/Certified_OGC_Compliant_Logo_Web.png"/></a>

A Server implementation of the OGC SensorThings API. The **FR**aunhofer **O**pensource **S**ensor**T**hings-Server
is the first complete, open-source implementation of the OGC SensorThings API Part 1: Sensing. It now also includes
preliminary actuation support.


## Downloading

Instead of compiling the server yourself, you can also download pre-built war and jar files from:
* [FROST-Server.MQTTP](https://repo1.maven.org/maven2/de/fraunhofer/iosb/ilt/FROST-Server/FROST-Server.MQTTP/1.14.2/FROST-Server.MQTTP-1.14.2.war)
* [FROST-Server.HTTP](https://repo1.maven.org/maven2/de/fraunhofer/iosb/ilt/FROST-Server/FROST-Server.HTTP/1.14.2/FROST-Server.HTTP-1.14.2.war)
* [FROST-Server.MQTT](https://repo1.maven.org/maven2/de/fraunhofer/iosb/ilt/FROST-Server/FROST-Server.MQTT/1.14.2/FROST-Server.MQTT-1.14.2-jar-with-dependencies.jar)

Or you can [use Docker](https://fraunhoferiosb.github.io/FROST-Server/deployment/docker.html).

## Documentation

Documentation can be found on the [documentation site](https://fraunhoferiosb.github.io/FROST-Server/)

## Kubernetes (Helm) support

See [helm/frost-server/README.md](helm/frost-server/README.md) for how to use the FROST-Server Helm chart for a Kubernetes deployment.

## Standalone Spring Boot

If you prefer to not use Tomcat, [Kinota Server](https://github.com/kinota/kinota-server) is a
Spring Boot application that makes it easy to run Fraunhofer IOSB FROST-Server in cloud environments.

## Compiling

To compile FROST-Server you need to have a JDK and Maven installed.

1. Checkout the project from github: `git clone https://github.com/FraunhoferIOSB/FROST-Server.git`
2. Go to the project root (The top-most directory with a pom.xml) `cd FROST-Server`
3. Give the command `mvn clean install`. This should build the war and jar files in:
  * `FROST-Server.MQTTP/target/`
  * `FROST-Server.HTTP/target/`
  * `FROST-Server.MQTT/target/`

## Authors

Hylke van der Schaaf
hylke.vanderschaaf@iosb.fraunhofer.de

Michael Jacoby
michael.jacoby@iosb.fraunhofer.de


## License

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
