---
layout: default
title: Deployment - Docker
category: deploymentTutorial
topCategory: STA
order: 1
---

# Deploying an OGC SensorThings API Instance

There are several server implementations of the OGC SensorThings API.
For this workshop we are using FROST-Server.

## Deploying FROST-Server using Docker

One easy way to get a STA server running is by using docker-compose.
Download the example [docker-compose.yaml](docker-compose.yaml) and run the command `docker-compose up` to start.
This will start a FROST-Server instance at:
> [http://localhost:8080/FROST-Server/](http://localhost:8080/FROST-Server/)

The docker compose file arranges for:
- A PostgreSQL server with PostGIS extensions to be started
- This PostgreSQL server to have a persistent data volume for the database, so the data is not lost when restarting
- A FROST-Server all-in-one package, connected to this PostgreSQL server

## Deploying FROST-Server directly on Tomcat

If you already have a Tomcat and PostgreSQL server running, you can also deploy FROST-Server directly on Tomcat.
The manual for how to do this can be found on the [FROST-Server documentation site](https://fraunhoferiosb.github.io/FROST-Server/deployment/tomcat.html).

## Following this tutorial without deploying FROST-Server

If you have nether Tomcat/PostgreSQL, nor Docker, you can also follow this tutorial by using our demonstration
FROST instance, at:

> [https://ogc-demo.k8s.ilt-dmz.iosb.fraunhofer.de/](https://ogc-demo.k8s.ilt-dmz.iosb.fraunhofer.de/)

This server is open for everyone to read, write, update and delete.
Most servers use some form of authentication for edit actions, but on this demo server everyone can delete everything!



