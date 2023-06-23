---
layout: default
title: Plugin Settings
category: settings
order: 22
---

# Plugin Configuration Options

The functionality of FROST-Server can be expanded using plugins.
Which plugins are loaded is controlled by two parameters:

* **plugins.providedPlugins:**  
  A comma-separated list of class names, listing the plugins provided
  with FROST by default. Normally there should be no need to change this.
* **plugins.plugins:**  
  A comma-separated list of class names, listing additional plugins to load.

Besides these configuration options telling FROST which plugins exist, each plugin
usually has a separate option that controls whether the plugin is active or not.


## Data Model Plugins

These plugins implement the data models  are provided with FROST-Server by default.


### Actuation

The actuation plugin implements the standard OGC SensorThings API - Part 2: Actuation.
It adds the entity types described in this specification.
This plugin requires the CoreModel plugin.

* **plugins.actuation.enable:**  
  Toggle indicating the Actuation plugin should be enabled. Default: `false`.
* **plugins.actuation.idType.actuator:**  
  The type of the primary key column of the Actuator table. Defaults to the value of **plugins.coreModel.idType**.
* **plugins.actuation.idType.task:**  
  The type of the primary key column of the Task table. Defaults to the value of **plugins.coreModel.idType**.
* **plugins.actuation.idType.taskingCapability:**  
  The type of the primary key column of the TaskingCapability table. Defaults to the value of **plugins.coreModel.idType**.


### CoreModel

The Core Model plugin implements the data model of the standard OGC SensorThings
API - Part 1: Sensing.
It adds the entity types described in this specification and their behaviour.

* **plugins.coreModel.enable:**  
  Toggle indicating the CoreModel plugin should be enabled. Default: `true`.
* **plugins.coreModel.editableDsProperties:**  
  Toggle indicating the server-generated properties of (Multi)Datastreams can be edited. Default: `false`.
* **plugins.coreModel.idType:**  
  The default type of the primary key columns, can be overruled on a per-table basis:
  * **`LONG`:**  
    Default value, using Long values for entity ids, generated in sequence.
  * **`STRING`:**  
    Using String values for entity ids, with new values generated using `uuid_generate_v1mc()`.  
    When using this implementation, make sure you execute the command `CREATE EXTENSION "uuid-ossp";` on the database.
  * **`UUID`:**  
    Using uuid values for entity ids, with new values generated using `uuid_generate_v1mc()`.  
    When using this implementation, make sure you execute the command `CREATE EXTENSION "uuid-ossp";` on the database.
* **plugins.coreModel.idType.datastream:**  
  The type of the primary key column of the Datastream table. Defaults to the value of **plugins.coreModel.idType**.
* **plugins.coreModel.idType.feature:**  
  The type of the primary key column of the Features table. Defaults to the value of **plugins.coreModel.idType**.
* **plugins.coreModel.idType.historicalLocation:**  
  The type of the primary key column of the HistoricalLocation table. Defaults to the value of **plugins.coreModel.idType**.
* **plugins.coreModel.idType.location:**  
  The type of the primary key column of the Location table. Defaults to the value of **plugins.coreModel.idType**.
* **plugins.coreModel.idType.observedProperty:**  
  The type of the primary key column of the ObservedProperty table. Defaults to the value of **plugins.coreModel.idType**.
* **plugins.coreModel.idType.observation:**  
  The type of the primary key column of the Observation table. Defaults to the value of **plugins.coreModel.idType**.
* **plugins.coreModel.idType.sensor:**  
  The type of the primary key column of the Sensor table. Defaults to the value of **plugins.coreModel.idType**.
* **plugins.coreModel.idType.thing:**  
  The type of the primary key column of the Thing table. Defaults to the value of **plugins.coreModel.idType**.


### MultiDatastream

The MultiDatastream plugin implements the MultiDatastream extendion of the OGC
SensorThings API standard.
This plugin requires the CoreModel plugin.

* **plugins.multiDatastream.enable:**  
  Toggle indicating the MultiDatastream plugin should be enabled. Default: `false`.
* **plugins.multiDatastream.idType.multiDatastream:**  
  The type of the primary key column of the Datastream table. Defaults to the value of **plugins.coreModel.idType**.


### ModelLoader

The ModelLoader plugin can load data models and security definitions from files.
The models loaded by this plugin may require other data model plugins.
Data model files and security definition files can be edited with the ModelEditor.
Data model files can be initially generated from an existing database using the ModelExtractor.

Using `Security Wrappers` and `Security Validators` it is possible define in minute detail what a user is allowed to read, create, update and delete.
The rules for this can take into account what the relations are that each Entity has with other Entities, as deep as required.
Setting this up correctly is not trivial and a mistake in the authorisation may inadvertently open your data for reading or even editing.
Therefore, we strongly recommend you contact us for support if your use case requires fine-grained authorisation.

* **plugins.modelLoader.enable:** Since 2.0.0  
  Toggle indicating the MultiDatastream plugin should be enabled. Default: `false`.
* **plugins.modelLoader.idType.<EntityTypeName>:**  
  The type of the primary key column of the table for the given Entity Type. Defaults to the value of **plugins.coreModel.idType**.
* **plugins.modelLoader.modelPath:** 2.0.0  
  The file path where model definition files are located. This path is prepended to each entry in **plugins.modelLoader.modelFiles**.
* **plugins.modelLoader.modelFiles:** 2.0.0  
  A comma-separated list of model files to load. Each entry is prefixed with **plugins.modelLoader.modelPath**.
* **plugins.modelLoader.liquibasePath:** 2.0.0  
  The file path where Liquibase (database definition) files are located. This path is prepended to each entry in **plugins.modelLoader.liquibaseFiles**.
* **plugins.modelLoader.liquibaseFiles:** 2.0.0  
  A comma-separated list of Liquibase (database definition) files to load. Each entry is prefixed with **plugins.modelLoader.liquibasePath**.
* **plugins.modelLoader.securityPath:** Since 2.2.0  
  The file path where security definition files are located. This path is prepended to each entry in **plugins.modelLoader.securityFiles**.
* **plugins.modelLoader.securityFiles:** Since 2.2.0  
  A comma-separated list of security files to load. Each entry is prefixed with **plugins.modelLoader.securityPath**.


## Response Format Plugins

These plugins enable various response formats.


### DataArray

The DataArray plugin implements the SensorThings Data Array Extension as described
in the SensorThings API standard.

* **plugins.dataArray.enable:**  
  Toggle indicating the ResultFormat dataArray should be enabled. Default: `true`.


### CSV Result Format

The CSV plugin implements a CSV result formatter, enabling CSV output as described
in: [CSV-ResultFormat](https://github.com/INSIDE-information-systems/SensorThingsAPI/blob/master/CSV-ResultFormat/CSV-ResultFormat.md)

* **plugins.csv.enable:**  
  Toggle indicating the ResultFormat CSV should be enabled. Default: `true`.


### GeoJSON Result Format

The GeoJSON plugin implements a GeoJSON result formatter, enabling GeoJSON output as described
in: [GeoJSON-ResultFormat](https://fraunhoferiosb.github.io/FROST-Server/extensions/GeoJSON-ResultFormat.html)

* **plugins.geojson.enable:**  
  Toggle indicating the ResultFormat GeoJSON should be enabled. Default: `true`.


## Other Plugins

These plugins enable various other behaviours.

### BatchProcessing

The BatchProcessing plugin implements the Batch Requests extension as described
in the SensorThings API standard.

* **plugins.batchProcessing.enable:**  
  Toggle indicating the BatchProcessing plugin should be enabled. Default: `true`.


### OData

The OData plugin makes the data in your FROST-Server available as OData 4.0 and 4.01
API. The endpoint for these are on `[service root URL]/ODATA_4.0/` and `[service root URL]/ODATA_4.01/`.

* **plugins.odata.enable:**  
  Toggle indicating the OData plugin should be enabled. Default: `false`.


### OpenAPI

The OpenAPI plugin makes an OpenAPI description of the SensorThings service available
at the /v1.x/api path.
This description is still experimental, and probably incomplete.
If you use it and have experience with OpenAPI, we welcome feedback!

* **plugins.openApi.enable:**  
  Toggle indicating the OpenAPI plugin should be enabled. Default: `false`.

