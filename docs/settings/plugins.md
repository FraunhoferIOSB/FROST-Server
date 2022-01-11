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

