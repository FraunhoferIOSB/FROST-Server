---
layout: default
title: PostgreSQL Setup
category: deployment
order: 13
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


## Database initialisation or upgrade

After setting up FROST-Server in Docker, Tomcat or Wildfly:

1. Browse to http://localhost:8080/FROST/DatabaseStatus
2. Click the upgrade button

This should initialise/update the database to the latest version and the service is ready for use.

Now you may want to have a look at  [Performance tips for PostgreSQL and PostGIS](db_performance.md).

