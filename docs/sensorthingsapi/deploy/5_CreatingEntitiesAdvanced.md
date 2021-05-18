---
layout: default
title: Advanced Entity Creation
category: deploymentTutorial
topCategory: STA
order: 5
---

# Creating multiple related entities in one POST

It is possible to create an entities, and its relations, in one POST, by giving the full related
entity in the JSON instead of only the entitiy id. The following POST creates a Thing, a Location, two Datastreams
(linked to the same, existing Sensor) each with an ObservedProperty, in one POST.

```
POST https://example.org/FROST-Server/v1.1/Things
```
```javascript
{
  "name" : "Kitchen",
  "description" : "The Kitchen in my house",
  "properties" : {
    "oven" : true,
    "heatingPlates" : 4
  },
  "Locations": [
    {
      "name": "Location of the kitchen",
      "description": "This is where the kitchen is",
      "encodingType": "application/geo+json",
      "location": {
        "type": "Point",
        "coordinates": [8.438889, 44.27253]
      }
    }
  ],
  "Datastreams": [
    {
      "name": "Temperature in the Kitchen",
      "description" : "The temperature in the kitchen, measured by the sensor next to the window",
      "observationType": "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement",
      "unitOfMeasurement": {
        "name": "Degree Celsius",
        "symbol": "°C",
        "definition": "ucum:Cel"
      },
      "Sensor": {"@iot.id": 5},
      "ObservedProperty": {
        "name": "Temperature",
        "description": "Temperature",
        "definition": "http://dd.eionet.europa.eu/vocabularyconcept/aq/meteoparameter/54"
      }
    },{
      "name": "Humidity in the Kitchen",
      "description" : "The relative humidity in the kitchen, measured by the sensor next to the window",
      "observationType": "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement",
      "unitOfMeasurement": {
        "name": "Percent",
        "symbol": "%",
        "definition": "ucum:%"
      },
      "Sensor": {"@iot.id": 5},
      "ObservedProperty": {
        "name": "Relative humidity",
        "description": "Relative humidity",
        "definition": "http://dd.eionet.europa.eu/vocabularyconcept/aq/meteoparameter/58"
      }
    }
  ]
}
```


