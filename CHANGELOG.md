# Changelog

## Development version 2.4.1

**Internal changes & Bugfixes**
* Set limit to 1 for entity-by-primary-key requests.
* Added a validator that sets a time to now(), unless otherwise set.
* Improved OpenAPI generator, better property types.
* Fixed substring not working on PostgreSQL due to failing cast.
* Fixed substring function to be 0-based. substring(name,1) should return name after
  the first character, but it returned name including the first character.
* Fixed #2016: MultiPolygon not supported in spatial queries.
* Improved insert efficiency by not doing a separate select to get the full inserted entity. (georghas)
* Improved update efficiency by not doing a separate select to get the full updated entity. (georghas)


## Release version 2.4.0

**New Features**
* Implemented OData any() filters.
* Added options for adding extra meta data to the landing page using the modelLoader plugin.
* Implemented $filter for MQTT topics. Can be enabled using the `mqtt.allowFilter` setting.
* Plugins can be loaded in any order.
* [HELM] Improved method for setting extra env variables on HTTP and MQTT pods.
* [HELM] Add database connection to Keycloak provider.
* Added setting for sessionCookiePath.
* Added support for [Tomcat Remote IP Filter](https://tomcat.apache.org/tomcat-9.0-doc/config/filter.html#Remote_IP_Filter).
* Added option to auto-detect the `serviceRootUrl`, optionally using X-Forwarded headers.
* Improved Batch Requests extension to stream results to the client, while processing the batch.
* Added option to Batch Request extension, to allow back-references to GET results.
* Added option to Batch Request extension, to allow skipping requests based on back references.
* Added 'Projects' plugin with fine-grained authorisation capabilities.

**Internal changes & Bugfixes**
* Improved generated queries when fetching entities over a one-to-many relation.
* Fixed deep select for complex properties in GeoJSON and CSV output.
* Fixed automatic generation of Features when Feature and Location use different idTypes.
* Fixed GeoJSON output not containing geometries.
* Fixed $select not properly working on unitOfMeasurement sub-properties.
* Fixed #1934, incorrect URL parameter in docs.
* Changed header handling to allow for duplicate headers.
* Fixed MultiDatastream not picking up ID type of Thing and Sensor.
* Improved settings handling code, added annotation for sensitive values.


## Release version 2.3.0

NOTE: The helm option `frost.http.ingress.rewriteTarget` had a typo in it
and has been changed (was: `rewriteTraget`). If you use this HELM option,
make sure to check and update your HELM settings.

**New Features**
* Added support for headers in JSON-Batch requets, enabling JSON-Patch requests in JSON-Batch requests.
* Added option for KeycloakAuthProvider to locally register user.
* Implemented expand option for MQTT Topics: `v1.1/Observations?$select=id&$expand=Datastream($expand=Thing)`.
* Fixed ordering of object properties on JSONB columns (Aplhabetic instead of by key-length).
* Changed the default orderby for Observations to `phenomenonTime, id`.

**Internal changes & Bugfixes**
* Fixed #1754: resultTime/phenomenonTime mixup in MultiDatastream.
* Fixed ordering of object properties on JSONB columns.
* Improved internal message bus when MQTT is not in use.
* Improved loading speed of custom data models.
* Fixed a typo in the helm variable rewriteTarget.
* Fixed security queries running as normal user, resulting in too narrow access.
* Improved the memory efficiency of the DataArray resultFormat.
* Added checks for maximum username and password lengths.
* Added cache to basic authentication module to avoid calling crypt for users that are previously authenticated.
* Improved generated queries when filtering or ordering on ID over one-to-many relations.


## Release version 2.2.0

**New Features**
* Improved configuration options for logging.
* Implemented comparing to null: `$filter=property eq null` and `$filter=property ne null`.
* Improved queue-status logging.
* Implemented FineGrained Authorization.
* Many-to-Many Self-Relations can be symmetrical.
* Improved handling of sub-properties of complex properties.
* Fixed subQueries on expands on custom links.
* Disallow wild-cards in MQTT topics.
* Added DrawIO output to OData metadata page using '$metadata?$format=drawio'.
* Added option to specify schema priority if the same table name exists in multiple schemas.
* Added support for OData `in` keyword.
* Return a 400 error when navigation properties have conflicting values.
* Changed ManyToMany relations to use onConflictDoNothing to avoid 500 when adding duplicates.

**Internal changes & Bugfixes**
* Fixed performance degradation caused by HTTP-instances listening on the message bus.
* Fixed #1541: times not serialised correctly in GeoJSON resultFormat.
* Fixed #1542: fixed regression in GeoJSON id property.
* Fixed #1564: NavLinks can have names that are not the name of the target entity type.
* Fixed cross-entity-type custom links breaking $expand.
* Fixed #1554: Count incorrect after following nextLink.
* Fixed unknown version giving 200 instead of 404.
* Fixed JSON-Patch that changed nothing giving an error back.
* Fixed memory leak when creating Entities using MQTT.


## Release version 2.1.0

FROST-Server version 2.1 and higher requires Java 17. This is because some libraries that are used now require Java 17.

**New Features**
* The new setting `plugins.coreModel.editableDsProperties` to control if generated
  (Multi)Datastream properties can be directly edited.
* Allow BasicAuthProvider to use hashed passwords. (pbaumard)
* Add FROST_LL_service env variable for log level. (pbaumard)
* Added option to set PostgreSQL role to enable Row-Level Security for HTTP. (pbaumard)
* Implemented [unlinking many-to-many relations](https://docs.oasis-open.org/odata/odata/v4.01/os/part2-url-conventions/odata-v4.01-os-part2-url-conventions.html#sec_AddressingReferencesbetweenEntities).

**Internal changes & Bugfixes**
* Fixed DateTimeConstants sometimes being generated with decimal comma.
* Fixed DateTimeConstants failing to parse when using decimal comma.
* Updated version of moquette allows persistent sessions to time out, avoiding memory leaks.
* Improved OpenAPI spec generator.
* Improved handling of required, nullable and non-editable properties.
* Fixed #1069: GeoJSON output is missing the `type` attribute for the feature elements.
* Changed build system to Java 17.
* Added Maven wrapper to build without locally installed Maven.
* (OData) Add support for metadata sub parameter in format parameter.
* Fixed #1246: phenomenonTime not serialised in CSV ResultFormatter.
* Fixed NPE in auth module on DatabaseStatus path.
* Improved consistency of 4xx logging. (pbaumard)
* Fixed handling of default settings.


## Release version 2.0.0

Version 2.0 contains many large updates to the FROST-Server codebase, greatly increasing its flexibility.
Much functionality is now controlled by plugins, from the data model to result formats.


**Upgrading from 1.x**

First update to 1.14.x or 1.15.x and run the Update Database script to ensure the database schema is fully up to date.
After this version 2.x can be used on the same Database.

FROST 2.x is more strict when it comes to relation names when filtering. Frost 1.x would accept
 `/ObservedProperties?$filter=Datastream/name` even though an ObservedProperty has a `Datastreams`
 (plural) relation and not a `Datastream` (singular) relation. Version 2.x will give an error in this case.

FROST 2.x does not allow `$resultMetadata` in `$expand`.

**New Features**
* The data model provided by FROST-Server is now completely defined in plugins.
  This means it is possible to use FROST-Server with a totally different data
  model, or to create plugins that extend the existing core SensorThings API
  data model.
* API endpoints are defined by plugins.
* New plugin: _CoreModel_ supplies the core data model from the _OGC SensorThings
  API Part 1: Sensing_ version 1.0 and 1.1 and supplies the `/v1.0` and `/v1.1`
  end points.
* New plugin: _MultiDatastream_ supplies the MultiDatastream extension.
* New plugin: _Actuation_ supplies the Actuation data model from the
  _OGC SensorThings API Part 2 – Tasking Core_.
* New experimental plugin: _OData_ adds the OData 4.0 compatible end point `/ODATA_4.0` and
  the OData 4.01 end point `/ODATA_4.01`.
* PersistenceManagers are no longer tied to the ID type. There is now only one
  `persistenceManagerImplementationClass`, used for all ID types. The ID type is
  now set in the data model plugin.
* Types of primary keys can be set on a per-table basis.
* Data is directly streamed from the database to the http response instead of
  to a full intermediate model and String, greatly improving memory use.
* The option alwaysOrderById now works directly on the query instead of the
  persistenceManager and is enabled by default.
* Faster Paging: instead of always using $skip, FROST-Server now generates a filter
  to improve paging speed if it is possible to do so. The query must have an (automatic)
  orderby on the primary key.
* Faster Counting: There are four options for faster, though less accurate, ways
  to count entities. Using either [TABLESAMPLE (1)](https://www.postgresql.org/docs/current/sql-select.html) or 
  [EXPLAIN](https://wiki.postgresql.org/wiki/Count_estimate) in combination with
  a limit on the SQL Count.
* Generated Features Of Interest now use the name, description and properties of
  the Location they are generated from.


## Release Version 1.14.0

**New Features**
* Added support for Batch Requests in JSON format instead of MultiPart
  format. This makes Batch Requests much easier to create. (Thanks pbaumard)

**Internal changes & Bugfixes**
* Increased the performance of the Datastream-update triggers that run on Observation
  insert/update/delete by up to 20%.
* Fixed deep-select not working for GeoJSON and CSV ResultFormats.
* Fixed newest mosquitto not working in docker-compose demo.
* Fix CME when getting parallel requests directly after startup.
* Regularly check if connection with message bus is there, connect if not
* Fixed the search_path if Liquibase changes it.
* Fixed double URLEncode in $select of nextLinks.
* Fixed Content-ID referencing in batch requests.
* Fixed batch request with encoded characters in the URL or relative resource paths.
* Fixed newlines in batch-request response.


## Release Version 1.13.0

**New Features**
* Added HTTP tool to index page, for simple POST/PATCH/DELETE commands.
* Implemented deep & distinct select. It is now possible to $select into properties
  and other JSON fields, to only get parts of the properties, and to get all the
  distinct values of properties.
  https://fraunhoferiosb.github.io/FROST-Server/extensions/DeepSelect.html
  https://fraunhoferiosb.github.io/FROST-Server/extensions/SelectDistinct.html

**Internal changes & Bugfixes**
* Updated the Moquette MQTT broker for increased performance and stability.
* Fixed handling of Long query parameters.
* Fixed #352: Date function not working in filters.
* Fixed #346: Added missing commit/rollback to batch-request transactions.
* Fixed #337: sensitive data shown in logs.
* Updated java version in Docker images.
* Improved performance of subscription matching in MQTT Manager.
* Added optional periodic logging of queue fill status.
* Fixed typo in trigger when deleting Observations from MultiDatastreams.
* Fixed #331: BasicAuth plugin did not properly use DB connection pool.
* Fixed #291: Change default request encoding to utf-8.
* Fixed conformance class URLs.
* Reduced logging output and how long logfiles are kept
* [HELM]: Fixed Ingress rewrite rules to work with nginx ingress version 0.22.0 and above.


## Release Version 1.12.0

**Caution**: The default values for the HELM-Chart have been changed.
This changes the default behaviour if values are not overwritten.
Keep this in mind when updating an existing instance.
It mainly affects installations without an enabled ingress resource.
* `frost.http.ingress.enabled` is now enabled by default
* `frost.mqtt.ports.mqtt.nodePort` doesn't contain a default value, so Kubernetes will choose a free port
* `frost.mqtt.ports.websocket.nodePort` doesn't contain a default value, so Kubernetes will choose a free port
* Database is changed from `mdillon/postgis:latest` to `postgis/postgis:11-2.5-alpine`.

**New Features**
* Added a resultFormat=GeoJSON, as described in: [GeoJSON-ResultFormat.md](https://fraunhoferiosb.github.io/FROST-Server/extensions/GeoJSON-ResultFormat.md).
* Added a custom entity linking extension, as described in: [EntityLinking.md](https://github.com/INSIDE-information-systems/SensorThingsAPI/blob/master/EntityLinking/Linking.md).
* The safe_cast_to_ functions in PostgreSQL are now IMMUTABLE so they can be used in indices.
* Converted JSON-holding columns to JsonB.
  **Caution**: On large databases this conversion can take considerable time, during
  which the table being converted is locked.

**Internal changes & Bugfixes**
* The JSON writer component is now about 5 times as fast.
* Fixed PostgreSQL triggers not running for MultiDatastreams.


## Release Version 1.11.0

**New Features**
* Initial support for SensorThings API version 1.1 was added. This is still subject
  to change, since version 1.1 is not officially released yet.
* ResultFormatters can now be supplied as plugins.
* Added a resultFormat=CSV, as described in: [CSV-ResultFormat](https://github.com/INSIDE-information-systems/SensorThingsAPI/blob/master/CSV-ResultFormat/CSV-ResultFormat.md).
* Added a plugin that generates an OpenAPI description in v1.x/api, disabled by default.

**Internal changes & Bugfixes**
* Fixed #132: HistoricalLocations not notified for auto-generated HLs.
* Fixed creating Tasks via MQTT not working.
* Fixed #125: TaskingCapabilities not created when posting a Thing.
* Refactored the data + path models and generic utils into separate packages.
* Adding support for creating Tasks via MQTT.
* Fixed TaskingCapabilities not created when posting a Thing.
* Fixed (Multi)Datastream times not being corrected correctly.
* Removed QueryDSL based persistence manager implementations.


## Release Version 1.10

**New Features**
* Actuation support. By default the actuation entities are hidden from the index,
  and from navigationLinks. By turning on the setting `enableActuation` the entities
  and navigationLinks are shown. Validation of the taskingParameters is not
  implemented yet.
* MultiDatastreams can be hidden from the index page and from navigationLinks by
  setting enableMultiDatastream to false. By default MultiDatastreams are enabled.
* Added experimental support for a serverSettings element to the index page, as
  discussed on the SensorThings API GitHub page: <https://github.com/opengeospatial/sensorthings/issues/4>.
* Added database persistence manager implementations using JOOQ instead of QueryDSL.

**Bugfixes**
* Fixed #110, paths with multiple ids (v1.0/Things(1)/Datastreams(2)/...) not
  always properly validated.
* Fixed #118, durations with year, month or week values were not parsed correctly.
* Fixed comparing JSON properties with each other in filters
* Fixed #116, filtering on Datastream/(phenomenon|result)Time returns Datastreams
  that have no such Time.


## Release Version 1.9

**New Features**
* Added experimental DELETE on Collections, with filters. Allows easier data cleanup.
  See <https://github.com/opengeospatial/sensorthings/issues/44>
* Added experimental way to change the location of a Thing, without generating a
  HistoricalLocation with a time of now(). See #66 and <https://github.com/opengeospatial/sensorthings/issues/30>.
* Added authentication support with two backends:
  * Basic: Using an internal user database and Basic authentication for HTTP.
  * Keycloak: Authenticating against an external Keycloak server.
* Added support for JSON-Patch [RFC6902](https://tools.ietf.org/html/rfc6902) updates.
  This allows users to specify specific changes to be made to the properties object,
  without replacing the entire object.


## Release Version 1.8

**New Features**
* Upgraded moquette to v0.11.
* Allow setting of the moquette persistent store path and storage class.
* Enabling the tomcat CorsFilter to allow cross-site-scripting can be done from environment variables.
* Added option to automatically run the liquibase database upgrade.

**Bugfixes**
* Fixed #59, incorrect nextLink when filtering on unitOfMeasurement/name.
* Fixed `MultiDatastream.observationType` being required even though we set it automatically.
* Prioritise `persistence_db_url` over `persistence_db_jndi_datasource`. This way
  there is no longer the need to add an empty environment variable `persistence_db_jndi_datasource`
  for the HTTP and MQTTP component when configuring using environment variables.
* Fixed string ids in next- and selfLink not being urlEncoded.


## Release Version 1.7

**New Features**
* Observation.result can be explicitly set to null. This is useful in cases where
  an observation did not produce a value, but the fact that an observation was attempted
  must still be recorded.
* Exposed database connection options `persistence.db.conn.max`, `persistence.db.conn.idle.max`,
  `persistence.db.conn.idle.min`

**Bugfixes**
* Fixed #53: Query parser not Unicode aware.
* Fixed #52: Generating FeatureOfInterest did not work for Things with multiple
  Location entities when some of these entities were not geoJSON.
* Fixed the 'year' function not working on interval properties.


## Release Version 1.6

**New Features**
* User-defined-ids. FROST-Server can not be configured to allow the user to specify
  the id of created enitites.
  The new setting `persistence.idGenerationMode` has three allowed values:
  * `ServerGeneratedOnly`: No client defined ids allowed, database generates ids.
  * `ServerAndClientGenerated`: Both, server and client generated ids, are allowed.
  * `ClientGeneratedOnly`: Client has to provide @iot.id to create entities.

  Thanks to Marcel Köpke for the patch.

* Improved time handling in queries. FROST-Server can now calculate with times:

    ```/Observations?$filter=phenomenonTime gt now() sub duration'P1D' mul Datastream/properties/days```

* Separated the MQTT and HTTP parts of the server.
  The MQTT and HTTP parts of the server are now separated in to stand-alone programs:
  * FROST-Server.HTTP: contains a web-app handling the HTTP part of the server.
  * FROST-Server.MQTT: contains a java application handling the MQTT part of the server.
  * FROST-Server.MQTTP: contains a web-app combining HTTP and MQTT, like it was before.

  There can be multiple MQTT and HTTP instances using the same database, to allow for horizontal
  scaling on a cloud infrastructure. The instances communicate over a pluggable message bus.

* There are now three docker images:
  * The stand-alone HTTP package: fraunhoferiosb/frost-server-http.
  * The stand-alone MQTT package: fraunhoferiosb/frost-server-mqtt.
  * The all-in-one package: fraunhoferiosb/frost-server.

  An example configuration for docker-compose can be found as docker-compose-separated.yaml,
  that shows how the HTTP and MQTT packages can be started separately, with an MQTT message bus
  for communication between the HTTP and MQTT instances.

* All configuration parameters can now be overridden using environment variables.

**Bugfixes**
* Fixed service prefix in default config file.
* Fixed Tomcat breaking selfLinks for ids that are URLs.
* Fixed $select not working for @iot.id, in MQTT.
* Fixed #48: creation in Observations in MultiDatastreams using DataArray formatting fails.


## Release Version 1.5

**New Features**
* We have a name: FROST-Server
* Implemented the Batch-processing extension.

**Bugfixes**
* Fixed that the Docker image was built every time. Build it using `mvn dockerfile:build -pl SensorThingsServer`


## Release Version 1.4

**New Features**
* Available through dockerhub: `docker pull fraunhoferiosb/sensorthingsserver`

**Bugfixes**
* Fixed deep insert of MultiDatastreams.
* Fixed building with Java 9.
* Fixed EntityType initialisation sometimes failing.


## Release Version 1.3

**New Features**
* Added new backends, using PostgreSQL with String and UUID columns for entity ids.
* Improved support for Wildfly.

**Bugfixes**
* Improved memory use when fetching large Observations with a high $top.


## Release Version 1.2

**New Features**
* Added support for ISO8601 Interval formats in the form of \[instant]/\[duration]
  and \[duration]/\[instant]. For example: 2015-10-14T01:01:01.000+02:00/P1D.
* Added a json properties field to (Multi)Datastream, FoI, Location, ObservedProperty and Sensor.
* Added setting to limit data site of responses. In case of Observations with
  large results, or Things with large properties, this will reduce the $top when
  the max size is reached.

**Bugfixes**
* Fixed only application/vnd.geo+json being recognised as GeoJSON, but not application/geo+json.
* Fixed GIS-filters on FeatureOfInterest/feature not working.


## Release Version 1.1

**New Features**
* Implemented MultiDatastream.
* Added full support for filtering and odering on json fields.
* Added Docker support.
* Explicitly set the SRIDs from 0 to 4326 for PostgreSQL databases.
* Added support for direct filtering on boolean fields.
* Separated options for MQTT address to bind and internal address to connect to.

**Bugfixes**
* Fixed result not always being correctly saved when updating observations.
* Fixed MQTT not working on MultiDatastream subscriptions.
* Fixed single quotes in string literals.
* Fixed filtering on json Observation/result values.
* Fixed crs of inserted geoJson.
* Fixed update on MultiDatastream.
* Fixed #8: Only keep one subscription per topic, with a count of how many clients use it.
* Fixed inserting Observations using MultiDatastreams(x)/Observations
* Fixed #5: MultiDatastream(x)/Observations did not work.
* Fixed MQTT subscriptions on navigation links returning too much.
* Fixed incorrect /$value response for time instance and time interval properties.


## Release Version 1.0

First release!
