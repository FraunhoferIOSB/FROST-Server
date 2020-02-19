---
layout: default
title: Docker deployment
category: Deployment
order: 2
---

# Docker

There's also the possibility to run FROST-Server and the needed database inside one or multiple Docker containers.
There are three docker images available:

* [fraunhoferiosb/frost-server](https://hub.docker.com/r/fraunhoferiosb/frost-server/) The all-in-one package
* [fraunhoferiosb/frost-server-http](https://hub.docker.com/r/fraunhoferiosb/frost-server-http/) The HTTP-only package
* [fraunhoferiosb/frost-server-mqtt](https://hub.docker.com/r/fraunhoferiosb/frost-server-mqtt/) The MQTT-only package

To make deployment of these images easier, two example docker-compose files are provided.
The `docker-compose.yaml` file uses the all-in-one server, and adds the required postgresql / postgis database.
The `docker-compose-separated.yaml` file uses the separated HTTP and MQTT packages,
adds the required postgresql / postgis database, and adds a mosquitto server as a message bus.

You can start the all-in-one docker image by running `docker-compose up`, or you can start the separated docker images using `docker-compose -f docker-compose-separated.yaml up`.
This will download the latest version of the specified FROST-Server packages, and any dependencies, and starts it all.
You can access the server by opening `http://localhost:8080/FROST-Server/` in your browser.

If you want to build your own docker images, you can do this by calling:

```
mvn dockerfile:build -pl FROST-Server.HTTP,FROST-Server.MQTT,FROST-Server.MQTTP
```

All data is stored inside the PostGIS database. To keep this state there's a volume automatically mapped to the PostGIS container.
For more information see the `docker-compose.yaml` file and the https://hub.docker.com/r/mdillon/postgis/[PostGIS container documentation]

You can override all [configuration settings](settings.adoc) by using environment variables in the docker-compose files.

