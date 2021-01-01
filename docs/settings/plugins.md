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


### CoreModel

The Core Model plugin implements the data model of the standard OGC SensorThings
API - Part 1: Sensing.
It adds the entity types described in this specification and their behaviour.

* **plugins.coreModel.enable:**  
  Toggle indicating the CoreModel plugin should be enabled. Default: `true`.


### MultiDatastream

The MultiDatastream plugin implements the MultiDatastream extendion of the OGC
SensorThings API standard.
This plugin requires the CoreModel plugin.

* **plugins.multiDatastream.enable:**  
  Toggle indicating the MultiDatastream plugin should be enabled. Default: `false`.


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


### OpenAPI

The OpenAPI plugin makes an OpenAPI description of the SensorThings service available
at the /v1.x/api path.
This description is still experimental, and probably incomplete.
If you use it and have experience with OpenAPI, we welcome feedback!

* **plugins.openApi.enable:**  
  Toggle indicating the OpenAPI plugin should be enabled. Default: `false`.

