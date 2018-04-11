
# Release Version 1.6
Version 1.6 has not been released yet.

**New Features**
* Improved time handling in queries. FROST-Server can now calculate with times:

    ```/Observations?$filter=phenomenonTime gt now() sub duration'P1D' mul (Datastream/properties/days)```

* Separated the MQTT and HTTP parts and created a stand-alone MQTT package.
  There can be multiple MQTT and HTTP instances using the same database. The instances
  communicate over a pluggable message bus. The default message bus uses MQTT.
* Created a docker image for the MQTT package: fraunhoferiosb/frost-server-mqtt.
* All configuration parameters can now be overridden using environment variables.

**Bugfixes**
* Fixed service prefix in default config file.
* Fixed Tomcat breaking selfLinks for ids that are URLs.
* Fixed $select not working for @iot.id, in MQTT.


# Release Version 1.5
Version 1.5 was released on 2018-02-15.

**New Features**
* We have a name: FROST-Server
* Implemented the Batch-processing extension.

**Bugfixes**
* Fixed that the Docker image was built every time. Build it using `mvn dockerfile:build -pl SensorThingsServer`


# Release Version 1.4
Version 1.4 was released on 2018-02-07.

**New Features**
* Available through dockerhub: `docker pull fraunhoferiosb/sensorthingsserver`

**Bugfixes**
* Fixed deep insert of MultiDatastreams.
* Fixed building with Java 9.
* Fixed EntityType initialisation sometimes failing.


# Release Version 1.3
Version 1.3 was released on 2018-01-22.

**New Features**
* Added new backends, using PostgreSQL with String and UUID columns for entity ids.
* Improved support for Wildfly.

**Bugfixes**
* Improved memory use when fetching large Observations with a high $top.


# Release Version 1.2
Version 1.2 was released on 2017-12-04.

**New Features**
* Added support for ISO8601 Interval formats in the form of [instant]/[duration] and [duration]/[instant]. For example: 2015-10-14T01:01:01.000+02:00/P1D.
* Added a json properties field to (Multi)Datastream, FoI, Location, ObservedProperty and Sensor.
* Added setting to limit data site of responses. In case of Observations with large results, or Things with large properties, this will reduce the $top when the max size is reached.

**Bugfixes**
* Fixed only application/vnd.geo+json being recognised as GeoJSON, but not application/geo+json.
* Fixed GIS-filters on FeatureOfInterest/feature not working.


# Release Version 1.1
Version 1.1 was released on 2017-10-05.

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



# Release Version 1.0
Version 1.0 was released on 2016-11-03

