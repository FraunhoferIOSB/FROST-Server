# FROST-Server [![Build Status](https://github.com/FraunhoferIOSB/FROST-Server/workflows/Maven%20Build/badge.svg)](https://github.com/FraunhoferIOSB/FROST-Server/actions) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/da22d7347eb7450a955274916ed2d336)](https://www.codacy.com/gh/FraunhoferIOSB/FROST-Server?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=FraunhoferIOSB/FROST-Server&amp;utm_campaign=Badge_Grade) [![Docker badge](https://img.shields.io/docker/pulls/fraunhoferiosb/frost-server.svg)](https://hub.docker.com/r/fraunhoferiosb/frost-server/)

![FROST-Server Logo](https://raw.githubusercontent.com/FraunhoferIOSB/FROST-Server/v2.x/docs/images/FROST-Server-darkgrey.png)
<a href="https://www.ogc.org/resource/products/details/?pid=1751"><img align="right" src="https://portal.ogc.org/public_ogc/compliance/OGC_Certified_Badge.png" alt="Certified OGC Compliant Logo" width="206" height="130"/></a>

A Server implementation of the [OGC SensorThings API](https://github.com/opengeospatial/sensorthings). The **FR**aunhofer **O**pensource **S**ensor**T**hings-Server
is the first complete, open-source official reference implementation of the [OGC SensorThings API Part 1: Sensing 1.0](https://docs.ogc.org/is/15-078r6/15-078r6.html). It also implements [OGC SensorThings API Part 1: Sensing 1.1](https://docs.ogc.org/is/18-088/18-088.html) and [OGC SensorThings API Part 2: Tasking Core 1.1](https://docs.ogc.org/is/17-079r1/17-079r1.html).


## Downloading

Instead of compiling the server yourself, you can also download pre-built war and jar files from:
* [FROST-Server.MQTTP](https://repo1.maven.org/maven2/de/fraunhofer/iosb/ilt/FROST-Server/FROST-Server.MQTTP/2.1.0/FROST-Server.MQTTP-2.1.0.war)
* [FROST-Server.HTTP](https://repo1.maven.org/maven2/de/fraunhofer/iosb/ilt/FROST-Server/FROST-Server.HTTP/2.1.0/FROST-Server.HTTP-2.1.0.war)
* [FROST-Server.MQTT](https://repo1.maven.org/maven2/de/fraunhofer/iosb/ilt/FROST-Server/FROST-Server.MQTT/2.1.0/FROST-Server.MQTT-2.1.0-jar-with-dependencies.jar)

Or you can [use Docker](https://fraunhoferiosb.github.io/FROST-Server/deployment/docker.html).


## Extendable Data Model and pluggable APIs

FROST-Server implements the SensorThings API data model and API, but is not limited to these.
Using plugins the data model can be extended or even completely replaced depending on your specific requirements.
APIs and result formats can also be added using plugins.
By default, FROST-Server comes with experimental plugins for the OData 4.0 and 4.01 APIs and for CSV and GeoJSON result formats.


## Fine-grained Authorisation

Authorisation rules can either be simple with Read, Create, Update and Delete on a service level,
or they can be very fine-grained with, for instance, certain users being able to read or create only Observations in Datastreams of certain Things.


## Documentation

Documentation can be found on the [documentation site](https://fraunhoferiosb.github.io/FROST-Server/)


## Changes and Updates

See the [Change Log](CHANGELOG.md).


## Kubernetes (Helm) support

See [helm/frost-server/README.md](helm/frost-server/README.md) for how to use the FROST-Server Helm chart for a Kubernetes deployment.


## Compiling

To compile FROST-Server you need to have a JDK and Maven installed.

1. Checkout the project from github: `git clone https://github.com/FraunhoferIOSB/FROST-Server.git`
2. Go to the project root (The top-most directory with a pom.xml) `cd FROST-Server`
3. Give the command `mvn clean install`. This should build the war and jar files in:
   * `FROST-Server.MQTTP/target/`
   * `FROST-Server.HTTP/target/`
   * `FROST-Server.MQTT/target/`


## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create.
Any contributions are greatly appreciated.
You can read more in our [contribution guidelines](CONTRIBUTING.md).


## Authors

Hylke van der Schaaf, 
Michael Jacoby

frost@iosb.fraunhofer.de


## License

Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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

