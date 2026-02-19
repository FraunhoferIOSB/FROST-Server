---
layout: default
title: Base Settings
category: settings
order: 20
---

# Base Configuration Options

There are several ways to deploy the different packages the make up the FROST-Server.
The HTTP and all-in-one MQTTP packages can be run in Tomcat or Wildfly, or as a docker image.
The MQTT package is a stand-alone application that can be run directly from the command line, or as a docker image.
For each option, the configuration is taken from (in order of priority):

* Tomcat
  1. Enviroment variables
  1. The [Context](http://tomcat.apache.org/tomcat-8.0-doc/config/context.html) entry either in
     * server.xml
     * `$CATALINA_BASE/conf/[enginename]/[hostname]/appname.xml`
     * `/META-INF/context.xml` inside the war file.
  1. web.xml
* Wildfly
  1. Enviroment variables
  1. web.xml
* Standalone MQTT
  1. Enviroment variables
  1. The config file: FrostMqtt.properties

**Important when using Environment variables:** Environment variables are not allowed to have the dot (`.`) character in the name. You must replace all of the dots in the names with an underscore (`_`).


## General Settings

These are settings affecting both the MQTT and HTTP packages.

* **serviceRootUrl:** Default: empty  
  The base URL of the SensorThings Server without version.
* **defaultCount:** Default: `false`  
  The default value for the $count query option.
* **defaultTop:** Default: `100`  
  The default value for the $top query option.
* **maxTop:** Default: `10000`  
  The maximum allowed value for the $top query option.
* **maxDataSize:** Default: `25000000` (25 MB)  
  The number of bytes that can be loaded before the server stops loading more entities and returns the result.
* **useAbsoluteNavigationLinks:** Default: `true`  
  If true, navigationLinks are absolute, otherwise relative.
* **alwaysOrderbyId:** Default: `true`  
  Always add an 'orderby=id asc' to queries to ensure consistent paging.
* **logSensitiveData:** Default: `false`  
  If false, sensitive data like passwords and database connection URLs are not logged when loading settings.
* **queueLoggingInterval:** Default: `0` (off)  
  If non-zero, log queue statistics ever x milliseconds.


## HTTP settings

These are settings for the HTTP package.

* **http.requestDecoder.autodetectRootUrl:** Since 2.4.0  
  If true, FROST will try to automatically detect the serviceRootUrl.
        Default `false` if the serviceRootUrl is not set, otherwise `true`.
* **http.requestDecoder.useXForwardedHeaders:** Since 2.4.0, Default `false`  
  If true, FROST will use the X-Forwarded headers to detect the serviceRootUrl.
* **http.requestDecoder.XForwardedHostHeader:** Since 2.4.0, Default `X-Forwarded-Host`  
  The name of the X-Forwarded-Host equivalent header.
* **http.requestDecoder.XForwardedPathHeader:** Since 2.4.0, Default `X-Forwarded-Path`  
  The name of the X-Forwarded-Path equivalent header.
* **http.requestDecoder.XForwardedPortHeader:** Since 2.4.0, Default `X-Forwarded-Port`  
  The name of the X-Forwarded-Port equivalent header.
* **http.requestDecoder.XForwardedProtoHeader:** Since 2.4.0, Default `X-Forwarded-Proto`  
  The name of the X-Forwarded-Proto equivalent header.
* **http.cors.enable:** Default: `false`  
  If true, a filter is added to allow cross-site-scripting.
* **http.cors.allowed.origins:** Default: `*`  
  A list of origins that are allowed to access the resource. A * can be specified to enable access to resource
  from any origin. Otherwise, a whitelist of comma separated origins can be provided. Eg: `http://www.w3.org, https://www.apache.org`.
* **http.cors.allowed.methods:** Default: `GET, HEAD, OPTIONS`  
  A comma separated list of HTTP methods that can be used to access the resource, using cross-origin requests.
  These are the methods which will also be included as part of Access-Control-Allow-Methods header in pre-flight response.
  Eg: `GET, POST`.
* **http.cors.exposed.headers:** Default: `Location`  
  A comma separated list of headers other than simple response headers that browsers are allowed to access.
  These are the headers which will also be included as part of Access-Control-Expose-Headers header in the pre-flight response.
  Eg: `X-CUSTOM-HEADER-PING,X-CUSTOM-HEADER-PONG`.
* **http.cors.allowed.headers:**  
  A comma separated list of request headers that can be used when making an actual request. These headers will
  also be returned as part of Access-Control-Allow-Headers header in a pre-flight response. Eg: `Origin,Accept`.
        Default: `Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers, Authorization`.
* **http.cors.support.credentials:** Default: `false`  
  A flag that indicates whether the resource supports user credentials. This flag is exposed as part of
  Access-Control-Allow-Credentials header in a pre-flight response. It helps browser determine whether or not an actual request can
  be made using credentials.
* **http.cors.preflight.maxage:** Default: `1800`  
  The amount of seconds, browser is allowed to cache the result of the pre-flight request. This will be included
  as part of Access-Control-Max-Age header in the pre-flight response. A negative value will prevent CORS Filter from adding this
  response header to pre-flight response.
* **http.cors.request.decorate:** Default: `true`  
  A flag to control if CORS specific attributes should be added to HttpServletRequest object or not.
* **http.sessionCookiePath:** Since 2.4.0, Default: empty  
  If set, overrides the session cookie path of Tomcat. Usefull when FROST is behind a path-rewriting reverse proxy.
* **http.remoteIpFilter.enable:** Since 2.4.0, Default: `false`  
  If true, a [remoteIpFilter](https://tomcat.apache.org/tomcat-9.0-doc/config/filter.html#Remote_IP_Filter) is added.
  All configuration parameters of the filter are exposed as `http.remoteIpFilter.*`.


## Auth settings

See [auth](auth.md) for more information.



## MQTT settings

These are settings for the MQTT package.

* **mqtt.mqttServerImplementationClass:**  
  The java class used for running the MQTT server (must implement MqttServer interface).
        Default: `de.fraunhofer.iosb.ilt.frostserver.mqtt.moquette.MoquetteMqttServer`
* **mqtt.Enabled:** Default: `true`  
  Specifies wether MQTT support will be enabled or not.
* **mqtt.Host:** Default: `0.0.0.0`  
  The external IP address or host name the MQTT server should listen on. Set to 0.0.0.0 to listen on all interfaces.
* **mqtt.Port:** Default: `1883`  
  The port the MQTT server runs on.
* **mqtt.sslPort:** Default: `8883`  
  The port the MQTT server runs on, using ssl.
* **mqtt.QoS:** Default: `2` (exactly once)  
  Maximum Quality of Service Level for MQTT messages. Allowed values: 0, 1, 2. Setting this to 1 or 2 makes it so messages
  are guaranteed to be delivered, but also severely reduces performance and throughput.
* **mqtt.SubscribeMessageQueueSize:** Default: `10`  
  Queue size for messages to be pubslihed via MQTT.
* **mqtt.SubscribeThreadPoolSize:** Default: `10`  
  Number of threads use to dispatch MQTT notifications.
* **mqtt.CreateMessageQueueSize:** Default: `10`  
  Queue size for create observation requests via MQTT.
* **mqtt.CreateThreadPoolSize:** Default: `5`  
  Number of threads use to dispatch observation creation requests.
* **mqtt.WebsocketPort:** Default: `9876`  
  The port the MQTT server is reachable via WebSocket.
* **mqtt.secureWebsocketPort:** Default: `443`  
  The port the MQTT server is reachable via secure WebSocket.
* **mqtt.javaKeystorePath:** Default: empty  
  When using SSL (sslPort or secureWebsocketPort) this is the path to the key store with your certificates.
* **mqtt.keyStorePassword:** Default: empty  
  The password for the certificate keystore.
* **mqtt.keyManagerPassword:** Default: empty  
  The password for the certificate itself.
* **mqtt.persistentStoreType:** Default: `memory`  
  The way the MQTT server keeps track of subscriptions, either in-memory (`memory`) or using an H2 database (`h2`).
* **mqtt.session.queue.size:** Default: `1024`  
  The size of the internal queue the mqtt broker uses per CPU core.
* **mqtt.persistent.client.expiration:** Default: `3600s`  
  The maximum lifetime of disconnected sessions. For example `100s` for 100 seconds.
* **mqtt.maxInFlight:** Default: `50`  
  The maximum number of "in-flight" messages to allow when sending notifications.
* **mqtt.netty.mqtt.message_size:** Default: `8092` (Bytes)  
  The maximum size of MQTT messages.
* **mqtt.WaitForEnter:** Default: `false`  
  When true, and running in an interactive console, the FROST-MQTT component will read the keyboard input, and exit
  when the enter key is pressed. When false, the FROST-MQTT component has to be stopped by sending it a TERM Signal.
* **mqtt.exposedEndpoints:** Default: empty  
  A comma separated list of MQTT endpoints to list on the index page.
  when not present, FROST will try to generate this list itself.
* **mqtt.allowFilter:** Since 2.3.2, Default: `false`  
  When true, MQTT topics may contain `$filter` query parameters.


## Persistence Settings

These settings deal with the database connection, for both the HTTP and MQTT packages.

* **persistence.persistenceManagerImplementationClass:**  
  The java class used for persistence (must implement PersistenceManager interface). Current implementations are:
  * **`de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager`:**  
    Default value, for PostgreSQL.
* **persistence.autoUpdateDatabase:** Default: `false`  
  Automatically apply database updates.
* **persistence.idGenerationMode:** Default: `ServerGeneratedOnly`  
  Determines how entity ids are generated. This can be overridden for each Entity Type. The three allowed values are:
  * **`ServerGeneratedOnly`:**  
    Default value, no client defined ids allowed, database generates ids.
  * **`ServerAndClientGenerated`:**  
    Both, server and client generated ids, are allowed.
  * **`ClientGeneratedOnly`:**  
    Client has to provide @iot.id to create entities.
* **persistence.idGenerationMode.<EntityTypeName>:** Since 2.2.0  
  Determines how entity ids are generated for this Entity Type. This overrides **persistence.idGenerationMode**.
* **persistence.transactionRole:** Default: `false`  
  If true, [SET LOCAL ROLE](https://www.postgresql.org/docs/current/sql-set-role.html)
  is used for each query and set as HTTP user name or `anonymous` for anonymous HTTP users,
  to be used typically with [Row-Level Security](https://www.postgresql.org/docs/current/ddl-rowsecurity.html).
  The PostgreSQL role is not related to the AuthProvider roles security (auth.role.* settings) which is also applied.
  This currently has no effect on MQTT since MQTT subscriptions do not query the database directly.
* **persistence.db.jndi.datasource:** Default: empty  
  JNDI data source name, used when running in Tomcat/Wildfly.
* **persistence.db.driver:** Default: empty  
  The Database driver to use when not using JNDI. For PostgreSQL this should be: `org.postgresql.Driver`
* **persistence.db.url:** Default: empty  
  The database connection url when not using JNDI. Example: `jdbc:postgresql://localhost:5432/sensorthings`
* **persistence.db.username:** Default: empty  
  The username to use when connecting to the database when not using JNDI.
* **persistence.db.password:** Default: empty  
  The password to use when connecting to the database when not using JNDI.
* **persistence.db.conn.max:**  
  The maximum number of database connections to use, when not using JNDI.
  The default is driver dependent.
* **persistence.db.conn.idle.max:**  
  The maximum number of idle database connections to keep open, when not using JNDI.
  The default is driver dependent.
* **persistence.db.conn.idle.min:**  
  The minimum number of idle database connections to keep open, when not using JNDI.
  The default is driver dependent.
* **persistence.db.conn.testOnBorrow:** Default: `false`  
  If true, connections are validated before being borrowed from the pool. Default: `false` (for backward compatibility).
* **persistence.db.conn.testWhileIdle:** Default: `false`  
  If true, connections are validated during idle periods. Default: `false` (for backward compatibility).
* **persistence.db.conn.validationQuery:** Default: empty  
  The SQL query used to validate connections (e.g., `SELECT 1` for PostgreSQL).
* **persistence.db.conn.timeBetweenEvictionRunsMillis:** Default: `0`  
  The time in milliseconds between eviction runs for idle connections. Set to 0 to disable.
* **persistence.db.schemaPriority:** Since 2.2.0, Default: empty  
  When searching table definitions, if a table with a given name is found in multiple schemas,
  use this comma-separated list of schemas to determine which table to use.
* **persistence.slowQueryThreshold:** Default: `200`  
  The duration threshold in ms after which queries are considered slow and are logged. Default 200, set to 0 to disable.
* **persistence.queryTimeout:** Default `0` (no timeout)  
  The maximum duration, in seconds, that a query is allowed to take.
  If your FROST instance is behind a reverse proxy that will abort the connection after a certain time, set this to the same duration.
* **persistence.countMode:** Since 2.0.0, Default `FULL`  
  The way to count entities. Usually the best value is LIMIT_ESTIMATE, but for backward compatibility, FULL is the default.
  Allowed values:
  * **`FULL`:** (default) Fully count all entities. Can be very slow on large result sets, but always gives accurate results.
  * **`LIMIT_SAMPLE`:** First do a count, with a limit of `countEstimateThreshold`. If the limit is reached, do an
    estimate using [`TABLESAMPLE (1)`](https://www.postgresql.org/docs/current/sql-select.html). For large result sets
    this is much faster than a full count, but is still guaranteed to give accurate results for low counts.
  * **`SAMPLE_LIMIT`:** First do an estimate using `TABLESAMPLE (1)` and if the estimate is below the threshold, do a
    count with a limit of countEstimateThreshold. This is faster than the above option for large result sets, but
    if the estimate is inaccurate, it can give an incorrect estimate for low counts.
  * **`LIMIT_ESTIMATE`:** First do a count, with a limit of `countEstimateThreshold`. If the limit is reached, do an
    estimate using [`EXPLAIN`](https://wiki.postgresql.org/wiki/Count_estimate). For large result sets
    this is even faster than the TABLESAMPLE estimate, but estimates can be wildly inaccurate for fields that are
    not backed by in index. For low counts this method is still guaranteed to give accurate results.
  * **`ESTIMATE_LIMIT`:** First do an estimate using `EXPLAIN` and if the estimate is below the threshold, do a
    count with a limit of countEstimateThreshold. This is the fastest method, but
    if the estimate is inaccurate, it can give an incorrect estimate for low counts.
* **persistence.countEstimateThreshold:** Since 2.0.0, Default: `10000`  
  When to switch from counting to estimating. Detailed behaviour depends on the value of
  `persistence.countMode`.


## message bus settings

These settings configure the way the HTTP and MQTT packages communicate with each other.

* **bus.busImplementationClass:**  
  The java class that is used to connect to the message bus. Current implementations:
  * **`de.fraunhofer.iosb.ilt.sta.messagebus.InternalMessageBus`:** (default)  
    This internal message bus can be used when all components run in the same JVM, as is the case with the all-in-one MQTTP package.
  * **`de.fraunhofer.iosb.ilt.sta.messagebus.MqttMessageBus`:**  
    This message bus implementation connects to an MQTT server to exchange messages.


### Settings for the Message bus classes

* **`de.fraunhofer.iosb.ilt.sta.messagebus.InternalMessageBus`**  
  This internal message bus can be used when all components run in the same JVM, as is the case with the all-in-one MQTTP package.

  * **bus.workerPoolSize:** Default: `2`  
    The number of worker threads to handle sending messages to the bus.
  * **bus.queueSize:** Default: `100`  
    The size of the message queue to buffer messages to be sent to the bus.

* **`de.fraunhofer.iosb.ilt.sta.messagebus.MqttMessageBus`**  
  This message bus implementation connects to an MQTT server to exchange messages.

  * **bus.mqttBroker:** Default: `tcp://127.0.0.1:1884`  
    The MQTT broker to use as a message bus.
  * **bus.sendWorkerPoolSize:** Default: `2`  
    The number of worker threads to handle sending messages to the bus.
  * **bus.sendQueueSize:** Default: `100`  
    The size of the message queue to buffer messages to be sent to the bus.
  * **bus.recvWorkerPoolSize:** Default: `2`  
    The number of worker threads to handle messages coming from the bus.
  * **bus.recvQueueSize:** Default: `100`  
    The size of the message queue to buffer messages coming from the bus.
  * **bus.topicName:** Default: `FROST-Bus`  
    The MQTT topic to use as a message bus.
  * **bus.qosLevel:** Default: `2`  
    The Quality of Service Level for the MQTT bus.
  * **bus.maxInFlight:** Default: `50`  
    The maximum number of "in-flight" messages to allow on the MQTT bus.


## Extension Settings

These settings control various non-standard extensions.

* **extension.customLinks.enable:** Default: `false`  
  Enables the EntityLinking extension described in:
  [https://github.com/INSIDE-information-systems/SensorThingsAPI/blob/master/EntityLinking/Linking.md](https://github.com/INSIDE-information-systems/SensorThingsAPI/blob/master/EntityLinking/Linking.md)
* **extension.customLinks.recurseDepth:** Default: `0` (only top level)  
  The depth to search for custom links in properties.
* **extension.filterDelete.enable:** Default: `false`  
  Enables DELETE on EntitySets, with filters. By default only individual Entities can be deleted.
* **mqtt.allowExpand:** Since 2.3.2, Default: `true`  
  When true, MQTT topics may contain `$expand` query parameters.
* **mqtt.allowFilter:** Since 2.3.2, Default: `false`  
  When true, MQTT topics may contain `$filter` query parameters.


