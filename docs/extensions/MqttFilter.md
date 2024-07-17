---
layout: default
title: Filtering Entities in MQTT
category: extensions
order: 31
---

# Filtering MQTT Responses

The function described here is an MQTT version of the function "Filter", which is based on HTTP "Filter" function.
Therefore the purpose is similar.

As MQTT is event based, whenever a parameter of any Thing changes (relations, e.g. Datastreams, excluded), responses will be sent to the subscribing clients.

**ATTENTION:** This function may create a high number of database reads and cause a performance drop.

## Basic Use

To use the function, the filter parameter is added to the subscription topic:

```
v1.1/Datastreams(5)/Observations
	?$select=phenomenonTime,result
	&$filter=result gt 5
```

This subscription topic will only push Observations for Datastream `5` when the result of the new or changed Observation is greater than 5.

## Conformance Class

The conformance class this extension must register in the SensorThings (v1.1 and up) index document is:

    https://fraunhoferiosb.github.io/FROST-Server/extensions/MqttFilter.html

