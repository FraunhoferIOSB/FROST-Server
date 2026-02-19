---
layout: default
title: Logging & Metrics
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
* **FROST_LOG_maxtotalsize:** Since 2.2.0, 2.1.1, 2.0.7, Default: `200MB`  
  The maximum total size for all log files. See the [logback docs](https://logback.qos.ch/manual/appenders.html#tbrpTotalSizeCap).
* **FROST_LOG_maxfilesize:** Since 2.2.0, 2.1.1, 2.0.7, Default: `50MB`  
  The maximum individual log file size.  See the [logback docs](https://logback.qos.ch/manual/appenders.html#SizeAndTimeBasedRollingPolicy).
* **FROST_LOG_maxfilecount:** Since 2.2.0, 2.1.1, 2.0.7, Default: `5`  
  The maximum number of log file to keep. See the [logback docs](https://logback.qos.ch/manual/appenders.html#tbrpMaxHistory).
* **FROST_LL:** Since 2.0.0, Default: INFO  
  The "root" log level.
* **FROST_LL_parser:** Since 2.0.0, Default: INFO  
  The log level for the `de.fraunhofer.iosb.ilt.frostserver.parser` package.
* **FROST_LL_queries:** Since 2.0.0, Default: INFO  
  The log level for the `de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.QueryBuilder` package.
* **FROST_LL_requests:** Since 2.0.0, Default: INFO  
  The log level for the request decoder. When set to DEBUG each request with its headers will be logged.
* **FROST_LL_service:** Since 2.0.0, Default: INFO  
  The log level for the `de.fraunhofer.iosb.ilt.frostserver.service` package.
  When set to DEBUG, 4xx error details and 5xx stack traces will be logged.
* **FROST_LL_settings:** Since 2.0.0, Default: INFO  
  The log level for the `de.fraunhofer.iosb.ilt.frostserver.settings` package.
* **FROST_LL_io_moquette:** Since 2.0.0, Default: WARN  
  The log level for the `io.moquette` package.
* **FROST_LL_liquibase:** Since 2.0.0, Default: INFO  
  The log level for the `liquibase` package.
* **FROST_LL_org_jooq:** Since 2.0.0, Default: INFO  
  The log level for the `org.jooq` package.


# Metrics

FROST-Server uses prometheus to gather metrics.
For the HTTP component, the metrics end point can be made available on the same port as the normal service by setting the `useServlet` variable,
or on a separate port by setting the `useInternalHttpServer` variable.
If both `useServlet` and `useInternalHttpServer` are false, then metrics gathering is disabled.


## Environment variables

* **metrics.useServlet:** Since 2.6.0, Default: `false`  
  For the HTTP and All-In-One components, use a servlet to expose the metrics.
  This makes `/FROST-Server/metrics` available, next to the other end-points.
* **metrics.useInternalHttpServer:** Since 2.6.0, Default: `false`  
  Use a separate HTTP service, on its own port, to expose the metrics.
  This makes a `/metrics` URL available on a configurable port.
* **metrics.endpointPort:** Since 2.6.0, Default: `9400`  
  The port to use for the separate HTTP service that exposes the metrics.

## Gathered metrics

Besides the FROST-Server specific metrics listed below, the JVM metrics are also gathered, as described in the [Prometheus Documentation](https://prometheus.github.io/client_java/instrumentation/jvm/)

### HTTP Pods

One HTTP request may result in multiple service requests, which in turn may result in may SQL queries.

- **http_request_duration_seconds** (histogram) HTTP request service time in seconds. Split along the HTTP Method used. (GET, PATCH, POST, ...)
  - **http_request_duration_seconds_bucket** (counter) Cumulative counters for the observation buckets.
  - **http_request_duration_seconds_count** (counter) Count of events that have been observed for the histogram metric.
  - **http_request_duration_seconds_sum** (counter) Total sum of all observed values for the histogram metric.

- **service_request_duration_seconds** (histogram) Service request service time in seconds. Split along the service request type. (read, create, getCapabilities)
  - **service_request_duration_seconds_bucket** (counter) Cumulative counters for the observation buckets.
  - **service_request_duration_seconds_count** (counter) Count of events that have been observed for the histogram metric.
  - **service_request_duration_seconds_sum** (counter) Total sum of all observed values for the histogram metric.

- **sql_query_duration_seconds** (histogram) SQL query execution time in seconds. Split along the entity type.
  - **sql_query_duration_seconds_bucket** (counter) Cumulative counters for the observation buckets.
  - **sql_query_duration_seconds_count** (counter) Count of events that have been observed for the histogram metric.
  - **sql_query_duration_seconds_sum** (counter) Total sum of all observed values for the histogram metric.

### MQTT Pods

The MQTT Manager is the part of the FROST-MQTT component that sits between the Data Model and the MQTT Broker.
It tracks which changes are made to the data, and determines which MQTT Topics need to be notified of these changes.

- **mqtt_manager_queue_fill** (gauge) Fill level of the Queue (0 - 1). Split into _Create_, _Changed_.
  - _Create_: Queue for entities being created over MQTT.
  - _Changed_: Queue for entities that have changed and may require messages to be sent to users.
- **mqtt_manager_queue_fill_max** (gauge) Maximum fill level of the Queue since last scrape call (0 - 1). Split into _Create_, _Changed_.
- **mqtt_manager_queue_overruns_total** (gauge) Total number of actions dropped because a queue was full. If this increases, the queue is too small, the worker count too low, or the system is overloaded.
- **mqtt_manager_topics** (gauge) Number of distinct topics that have subscriptions.
- **mqtt_manager_worker_status** (gauge) Overview of what workers do Overview of what workers do. Split along two dimensions: _Waiting_, _Working_, _Dead_ and _Create_, _Changed_.
  - _Waiting_: Number of workers that are waiting for work.
  - _Working_: Number of workers that are actively working.
  - _Dead_: Number of workers that have not responded for several seconds. If this is non-zero ther is a problem.

Moquette is the MQTT broker that distributes messages to the clients that listen to the topics those messages are sent to.

- **moquette_open_sessions** (gauge) The number of open sessions in the broker.
- **moquette_publishes_total** (counter) The number of messages publised to the broker.
- **moquette_session_messages_total** (counter) The number of messages publised by the broker, per Session Queue. Split along two dimensions: The session queue and the QualityOfService level of the message. The number of Session Queues equals the number of CPUs detected.
- **moquette_session_queue_fill** (gauge) Number of items in each Session Queue.
- **moquette_session_queue_fill_max** (gauge) Maximum number of items in each Session Queue since the last scrape call.
- **moquette_session_queue_overruns_total** (counter) Total number of actions dropped because the session queue was full. If this increases, the queue is too small, or the system is overloaded.


### Message Bus

- **message_bus_queue_fill** (gauge) Fill level of the Queue (0 - 1). Split into _Send_ and _Receive_ sub-gauges.
  - _Send_: The outgoing queue, items being sent over the bus.
  - _Receive_: The incoming queue, items being received over the bus.
- **message_bus_queue_fill_max** (gauge) Maximum fill level of the Queue since last scrape call (0 - 1). Also split into _Send_ and _Receive_.
- **message_bus_queue_overruns_total** (gauge) Total number of messages that were dropped because the queue was full. If this increases, either the queue is too small, or the number of workers is too low. Also split into _Send_ and _Receive_.
- **message_bus_worker_status** (gauge) Overview of what workers do. Split along two dimensions: _Waiting_, _Working_, _Dead_ and _Send_, _Receive_.
  - _Waiting_: Number of workers that are waiting for work.
  - _Working_: Number of workers that are actively working.
  - _Dead_: Number of workers that have not responded for several seconds. If this is non-zero ther is a problem.

