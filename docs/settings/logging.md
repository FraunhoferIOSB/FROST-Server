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
* **FROST_LOG_path:** Since 2.2.0, 2.1.1, 2.0.7  
  The path to log to. Defaults to `${catalina.base}/logs`  
* **FROST_LOG_filename:** Since 2.2.0, 2.1.1, 2.0.7  
  The base filename to use for log files. Defaults to `FROST-Server`.
* **FROST_LOG_maxtotalsize:** Since 2.2.0, 2.1.1, 2.0.7  
  The maximum total size for all log files. See the [logback docs](https://logback.qos.ch/manual/appenders.html#tbrpTotalSizeCap). Default: `200MB`.
* **FROST_LOG_maxfilesize:** Since 2.2.0, 2.1.1, 2.0.7  
  The maximum individual log file size.  See the [logback docs](https://logback.qos.ch/manual/appenders.html#SizeAndTimeBasedRollingPolicy). Default: `50MB`.
* **FROST_LOG_maxfilecount:** Since 2.2.0, 2.1.1, 2.0.7  
  The maximum number of log file to keep. See the [logback docs](https://logback.qos.ch/manual/appenders.html#tbrpMaxHistory). Default: `5`.
* **FROST_LL:** 2.0.0  
  The "root" log level. Default: INFO
* **FROST_LL_parser:** 2.0.0  
  The log level for the `de.fraunhofer.iosb.ilt.frostserver.parser` package. Default: INFO
* **FROST_LL_queries:** 2.0.0  
  The log level for the `de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.QueryBuilder` package. Default: INFO
* **FROST_LL_requests:** 2.0.0  
  The log level for the request decoder. When set to DEBUG each request with its headers will be logged. Default: INFO
* **FROST_LL_service:** 2.0.0  
  The log level for the `de.fraunhofer.iosb.ilt.frostserver.service` package.
  When set to DEBUG, 4xx error details and 5xx stack traces will be logged. Default: INFO
* **FROST_LL_settings:** 2.0.0  
  The log level for the `de.fraunhofer.iosb.ilt.frostserver.settings` package. Default: INFO
* **FROST_LL_io_moquette:** 2.0.0  
  The log level for the `io.moquette` package. Default: WARN
* **FROST_LL_liquibase:** 2.0.0  
  The log level for the `liquibase` package. Default: INFO
* **FROST_LL_org_jooq:** 2.0.0  
  The log level for the `org.jooq` package. Default: INFO

