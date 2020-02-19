---
layout: default
title: All Settings
category: Settings
order: 1
---

# Configuration Options

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

* **serviceRootUrl:**  
  The base URL of the SensorThings Server without version.
* **defaultCount:**  
  The default value for the $count query option.
* **defaultTop:**  
  The default value for the $top query option.
* **maxTop:**  
  The maximum allowed value for the $top query option.
* **maxDataSize:**  
  The number of bytes that can be loaded before the server stops loading more entities and returns the result. The default is 25000000 (25 MB).
* **useAbsoluteNavigationLinks:**  
  If true, navigationLinks are absolute, otherwise relative.
* **enableActuation:**  
  If false, actuation entities are hidden from the index page, and navigation links to the actuation entities are
  not shown. The entities can be accessed regardless of the setting. Defaults: `false`.
* **enableMultiDatastream:**  
  If false, MultiDatastream entities are hidden from the index page, and navigation links to the MultiDatastream entities are
  not shown. The entities can be accessed regardless of the setting. Defaults: `true`.


## HTTP settings

These are settings for the HTTP package.

* **http.cors.enable:**  
  If true, a filter is added to allow cross-site-scripting. Defaults: `false`.
* **http.cors.allowed.origins:**  
  A list of origins that are allowed to access the resource. A * can be specified to enable access to resource
  from any origin. Otherwise, a whitelist of comma separated origins can be provided. Eg: `http://www.w3.org, https://www.apache.org`.
  Defaults: `*`.
* **http.cors.allowed.methods:**  
  A comma separated list of HTTP methods that can be used to access the resource, using cross-origin requests.
  These are the methods which will also be included as part of Access-Control-Allow-Methods header in pre-flight response.
  Eg: `GET, POST`. Defaults: `GET, HEAD, OPTIONS`.
* **http.cors.exposed.headers:**  
  A comma separated list of headers other than simple response headers that browsers are allowed to access.
  These are the headers which will also be included as part of Access-Control-Expose-Headers header in the pre-flight response.
  Eg: `X-CUSTOM-HEADER-PING,X-CUSTOM-HEADER-PONG`. Default: `Location`.
* **http.cors.allowed.headers:**  
  A comma separated list of request headers that can be used when making an actual request. These headers will
  also be returned as part of Access-Control-Allow-Headers header in a pre-flight response. Eg: `Origin,Accept`.
  Defaults: `Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers, Authorization`.
* **http.cors.support.credentials:**  
  A flag that indicates whether the resource supports user credentials. This flag is exposed as part of
  Access-Control-Allow-Credentials header in a pre-flight response. It helps browser determine whether or not an actual request can
  be made using credentials. Defaults: `false`.
* **http.cors.preflight.maxage:**  
  The amount of seconds, browser is allowed to cache the result of the pre-flight request. This will be included
  as part of Access-Control-Max-Age header in the pre-flight response. A negative value will prevent CORS Filter from adding this
  response header to pre-flight response. Defaults: `1800`.
* **http.cors.request.decorate:**  
  A flag to control if CORS specific attributes should be added to HttpServletRequest object or not. Defaults: `true`.


## Auth settings

See [auth](auth.md) for more information.



## MQTT settings

These are settings for the MQTT package.

* **mqtt.mqttServerImplementationClass:**  
  The java class used for running the MQTT server (must implement MqttServer interface)
* **mqtt.Enabled:**  
  Specifies wether MQTT support will be enabled or not.
* **mqtt.Host:**  
  The external IP address or host name the MQTT server should listen on. Set to 0.0.0.0 to listen on all interfaces.
* **mqtt.internalHost:**  
  The internal host name of the MQTT server.
* **mqtt.Port:**  
  The port the MQTT server runs on.
* **mqtt.sslPort:**  
  The port the MQTT server runs on, using ssl.
* **mqtt.QoS:**  
  Quality of Service Level for MQTT messages.
* **mqtt.SubscribeMessageQueueSize:**  
  Queue size for messages to be pubslihed via MQTT.
* **mqtt.SubscribeThreadPoolSize:**  
  Number of threads use to dispatch MQTT notifications.
* **mqtt.CreateMessageQueueSize:**  
  Queue size for create observation requests via MQTT .
* **mqtt.CreateThreadPoolSize:**  
  Number of threads use to dispatch observation creation requests.
* **mqtt.WebsocketPort:**  
  The port the MQTT server is reachable via WebSocket.
* **mqtt.secureWebsocketPort:**  
  The port the MQTT server is reachable via secure WebSocket.
* **mqtt.javaKeystorePath:**  
  When using SSL (sslPort or secureWebsocketPort) this is the path to the key store with your certificates.
* **mqtt.keyStorePassword:**  
  The password for the certificate keystore.
* **mqtt.keyManagerPassword:**  
  The password for the certificate itself.
* **mqtt.maxInFlight:**  
  The maximum number of "in-flight" messages to allow when sending notifications.
* **mqtt.WaitForEnter:**  
  When true, and running in an interactive console, the FROST-MQTT component will read the keyboard input, and exit
  when the enter key is pressed. When false, the FROST-MQTT component has to be stopped by sending it a TERM Signal.
* **mqtt.exposedEndpoints:**  
  A comma separated list of MQTT endpoints to list on the index page.
  when not present, FROST will try to generate this list itself.


## Persistence Settings

These settings deal with the database connection, for both the HTTP and MQTT packages. There are currently two versions of the PostgreSQL
database persistence manager, one using QueryDSL, and one using JOOQ.

* **persistence.persistenceManagerImplementationClass:**  
  The java class used for persistence (must implement PersistenceManager interface). Current implementations are:
  * **`de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.imp.PostgresPersistenceManagerLong`:**  
    Default value, using Long values for entity ids, generated in sequence.
  * **`de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.imp.PostgresPersistenceManagerString`:**  
    Using String values for entity ids, with new values generated using `uuid_generate_v1mc()`.
  * **`de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.imp.PostgresPersistenceManagerUuid`:**  
    Using uuid values for entity ids, with new values generated using `uuid_generate_v1mc()`.
* **persistence.alwaysOrderbyId:**  
  Always add an 'orderby=id asc' to queries to ensure consistent paging.
* **persistence.autoUpdateDatabase:**  
  Automatically apply database updates.
* **persistence.idGenerationMode:**  
  Determines how entity ids are generated. The three allowed values are:
  * **`ServerGeneratedOnly`:**  
    Default value, no client defined ids allowed, database generates ids.
  * **`ServerAndClientGenerated`:**  
    Both, server and client generated ids, are allowed.
  * **`ClientGeneratedOnly`:**  
    Client has to provide @iot.id to create entities.
* **persistence.db.jndi.datasource:**  
  JNDI data source name, used when running in Tomcat/Wildfly.
* **persistence.db.driver:**  
  The Database driver to use when not using JNDI. For PostgreSQL this should be: `org.postgresql.Driver`
* **persistence.db.url:**  
  The database connection url when not using JNDI. Example: `jdbc:postgresql://localhost:5432/sensorthings`
* **persistence.db.username:**  
  The username to use when connecting to the database when not using JNDI.
* **persistence.db.password:**  
  The password to use when connecting to the database when not using JNDI.
* **persistence.db.conn.max:**  
  The maximum number of database connections to use, when not using JNDI.
* **persistence.db.conn.idle.max:**  
  The maximum number of idle database connections to keep open, when not using JNDI.
* **persistence.db.conn.idle.min:**  
  The minimum number of idle database connections to keep open, when not using JNDI.
* **persistence.slowQueryThreshold:**  
  The duration threshold in ms after which queries are considered slow and are logged. Default 200, set to 0 to disable.
* **persistence.queryTimeout:**  
  The maximum duration, in seconds, that a query is allowed to take. Default 0 (no timeout). If
  your FROST instance is behind a reverse proxy that will abort the connection after a certain time, set this to the
  same duration.


## message bus settings

These settings configure the way the HTTP and MQTT packages communicate with each other.

* **bus.busImplementationClass:**  
  The java class that is used to connect to the message bus. Current implementations:
  * **`de.fraunhofer.iosb.ilt.sta.messagebus.InternalMessageBus`:**  
    This internal message bus can be used when all components run in the same JVM, as is the case with the all-in-one MQTTP package.
  * **`de.fraunhofer.iosb.ilt.sta.messagebus.MqttMessageBus`:**  
    This message bus implementation connects to an MQTT server to exchange messages.


### Settings for the Message bus classes

* **`de.fraunhofer.iosb.ilt.sta.messagebus.InternalMessageBus`**  
  This internal message bus can be used when all components run in the same JVM, as is the case with the all-in-one MQTTP package.

  * **bus.workerPoolSize:**  
    The number of worker threads to handle sending messages to the bus.
  * **bus.queueSize:**  
    The size of the message queue to buffer messages to be sent to the bus.

* **`de.fraunhofer.iosb.ilt.sta.messagebus.MqttMessageBus`**  
  This message bus implementation connects to an MQTT server to exchange messages.

  * **bus.mqttBroker:**  
    The MQTT broker to use as a message bus.
  * **bus.sendWorkerPoolSize:**  
    The number of worker threads to handle sending messages to the bus.
  * **bus.sendQueueSize:**  
    The size of the message queue to buffer messages to be sent to the bus.
  * **bus.recvWorkerPoolSize:**  
    The number of worker threads to handle messages coming from the bus.
  * **bus.recvQueueSize:**  
    The size of the message queue to buffer messages coming from the bus.
  * **bus.topicName:**  
    The MQTT topic to use as a message bus.
  * **bus.qosLevel:**  
    The Quality of Service Level for the MQTT bus.
  * **bus.maxInFlight:**  
    The maximum number of "in-flight" messages to allow on the MQTT bus.


## Plugin Settings

These settings control which plugins are loaded.

* **plugins.providedPlugins:**  
  A comma-separated list of class names, listing the plugins provided
  with FROST by default. Normally there should be no need to change this.
* **plugins.plugins:**  
  A comma-separated list of class names, listing additional plugins to load.
* **plugins.dataArray.enable:**  
  Toggle indicating the ResultFormat dataArray should be enabled. Defaults: `true`.
* **plugins.csv.enable:**  
  Toggle indicating the ResultFormat CSV should be enabled. Defaults: `true`.


## Experimental Settings

These settings control non-standard, experimental behaviour.

* **experimental.exposeServerSettings:**  
  Adds a serverSettings element to the v1.0 index page, as discussed on the SensorThings API
  GitHub page in [issue 4](https://github.com/opengeospatial/sensorthings/issues/4).
  You should probably use v1.1 instead.


