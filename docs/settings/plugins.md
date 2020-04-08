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


## Plugin Settings

These plugins are provided with FROST-Server by default.


### BatchProcessing

The BatchProcessing plugin implements the Batch Requests extension as described
in the SensorThings API standard.

* **plugins.batchProcessing.enable:**  
  Toggle indicating BatchProcessing should be enabled. Defaults: `true`.


### DataArray

The DataArray plugin implements the SensorThings Data Array Extension as described
in the SensorThings API standard.

* **plugins.dataArray.enable:**  
  Toggle indicating the ResultFormat dataArray should be enabled. Defaults: `true`.


### CSV Result Format

The CSV plugin implements a CSV result formatter, enabling CSV output as described
in: [CSV-ResultFormat](https://github.com/INSIDE-information-systems/SensorThingsAPI/blob/master/CSV-ResultFormat/CSV-ResultFormat.md)

* **plugins.csv.enable:**  
  Toggle indicating the ResultFormat CSV should be enabled. Defaults: `true`.


### OpenAPI

The OpenAPI plugin makes an OpenAPI description of the SensorThings service available
at the /v1.x/api path.
This description is still experimental, and probably incomplete.
If you use it and have experience with OpenAPI, we welcome feedback!

* **plugins.openapi.enable:**  
  Toggle indicating the OpenAPI plugin should be enabled. Defaults: `false`.
