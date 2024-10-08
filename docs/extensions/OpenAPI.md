---
layout: default
title: OpenAPI
category: extensions
order: 14
---

# OpenAPI Plugin

This plugin adds OpenAPI descriptions of the services at the following paths:

```
/v1.0/api
/v1.1/api
```

## Request parameters

* **depth**: How deeply to recurse into relations. Integer, default: 1.
* **editing**: Should Create, Update, Delete actions be added. Boolean, default false.
* **properties**: Should direct property paths be added. Boolean, default false.
* **ref**: Should `$ref` paths be added. Boolean, default false.
* **value**: Should `$value` paths be added. Boolean, default false.


## Settings

The plugin has the following setting:

* **plugins.openApi.enable:**  
  Toggle indicating the OpenAPI plugin should be enabled. Default: `false`.

## Conformance Class

The conformance class this extension must register in the SensorThings (v1.1 and up) index document is:

    https://fraunhoferiosb.github.io/FROST-Server/extensions/OpenAPI.html

