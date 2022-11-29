---
layout: default
title: Logging
category: settings
order: 23
---

# Logging

FROST-Server uses SLF4J and Logback as logging framework. Each component (HTTP,
MQTT and combined) comes with a default logback.xml configuration file.
You can override the logback configuration file by specifying a different configuration
file in a JVM parameter `-Dlogback.configurationFile`.
Of course this is not trivial in a Docker environment. Therefore there are some
environment variables that influence basic logging options.

## Environment variables

* **logSensitiveData:**  
  If false, sensitive data like passwords and database connection URLs are not logged when loading settings. Default: `false`.
* **queueLoggingInterval:**  
  If non-zero, log queue statistics ever x milliseconds. Default: 0 (off)
* **FROST_LL:**  
  The "root" log level. Default: INFO
* **FROST_LL_parser:**  
  The log level for the `de.fraunhofer.iosb.ilt.frostserver.parser` package. Default: INFO
* **FROST_LL_queries:**  
  The log level for the `de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.QueryBuilder` package. Default: INFO
* **FROST_LL_requests:**  
  The log level for the request decoder. When set to DEBUG each request with its headers will be logged. Default: INFO
* **FROST_LL_service:**  
  The log level for the `de.fraunhofer.iosb.ilt.frostserver.service` package.
  When set to DEBUG, 4xx error details and 5xx stack traces will be logged. Default: INFO
* **FROST_LL_settings:**  
  The log level for the `de.fraunhofer.iosb.ilt.frostserver.settings` package. Default: INFO
* **FROST_LL_io_moquette:**  
  The log level for the `io.moquette` package. Default: WARN
* **FROST_LL_liquibase:**  
  The log level for the `liquibase` package. Default: INFO
* **FROST_LL_org_jooq:**  
  The log level for the `org.jooq` package. Default: INFO

