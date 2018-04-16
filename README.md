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
| Sensing Data Array Extension          | A.6       | Yes         |   3 /  3   |
| MQTT Extension for Create and Update  | A.7       | Yes         |   4 /  4   |
| MQTT Extension for Receiving Updates  | A.8       | Yes         |  13 / 13   |

We have extended the official test suit with extra tests that can be found [here](https://github.com/FraunhoferIOSB/ets-sta10).
The official test suit is fully passed.
See the wiki page [features](https://github.com/FraunhoferIOSB/FROST-Server/wiki/Features) for more details.

## Compiling

See the [wiki](https://github.com/FraunhoferIOSB/FROST-Server/wiki) for longer installation instructions.

To compile FROST-Server you need to have a JDK and Maven installed.

1. Checkout the project from github: `git clone https://github.com/FraunhoferIOSB/FROST-Server.git`
2. Go to the project root (The top-most directory with a pom.xml) `cd FROST-Server`
3. Give the command `mvn clean install`. This should build the war and jar files in:
  * `FROST-Server.MQTTP/target/`
  * `FROST-Server.HTTP/target/`
  * `FROST-Server.MQTT/target/`

## Database setup

See [docs/postgresql.adoc](docs/postgresql.adoc) for an overview on how to configure PostgreSQL and Tomcat/Wildfly.

## Configuration options

See [docs/settings.adoc](docs/settings.adoc) for an overview of all the the configuration settings.

## Docker support

See [docs/docker.adoc](docs/docker.adoc) for how to use the FROST-Server docker images.

## Standalone Spring Boot

If you prefer to not use Tomcat, [Kinota Server](https://github.com/kinota/kinota-server) is a
Spring Boot application that makes it easy to run Fraunhofer IOSB FROST-Server in cloud environments.

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
