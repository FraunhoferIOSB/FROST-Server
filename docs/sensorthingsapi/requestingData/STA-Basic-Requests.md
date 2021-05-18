---
layout: default
title: Basic Requests
category: gettingData
topCategory: STA
order: 2
---

# Basic Requests

|                   | GET             |
|-------------------|-----------------|
| __v1.1__          | Get index       |
| __v1.1/Type__     | Get all of type |
| __v1.1/Type(id)__ | Get one of type |


## List All Object Types

The base requests allowing access to the seven core classes described above are available from the SensorThings API landing page under the “value” attribute.
Each value consists of a “name” that provides the name of the SensorThings class together with a “url” that provides access to all objects of this class.
The following provides an overview of the SensorThings response:
```javascript
{
  "value" : [ {
      "name" : "Datastreams",
      "url" : "http://server.de/FROST-Server/v1.0/Datastreams"
    }, {
      "name" : "FeaturesOfInterest",
      "url" : "http://server.de/FROST-Server/v1.0/FeaturesOfInterest"
    }, {
    …
    …
    }, {
      "name" : "Things",
      "url" : "http://server.de/FROST-Server/v1.0/Things"
    }
  ]
}
```


## A Type of Object

All objects of a specific type can be accessed via the URLs provided in the SensorThings API landing page and described above. In order to request the Things provided by this service, use the following URL:
```
http://server.de/FROST-Server/v1.0/Things
```
The SensorThings API will respond with a list of available Things as follows:
```javascript
{
  "value" : [
    {
      "description" : "camping lantern",
      "properties" : {
        "property1" : "it’s waterproof",
        "property2" : "it glows in the dark"
      },
      "Locations@iot.navigationLink" : "http://…/Things(1)/Locations",
      "HistoricalLocations@iot.navigationLink" : "http://…/Things(1)/HistoricalLocations",
      "Datastreams@iot.navigationLink" : "http://…/Things(1)/Datastreams",
      "@iot.id" : 1,
      "@iot.selfLink" : "http://…/Things(1)"
    },
    {
      … A further Thing …
    }
  ]
}
```
> Note: http://…/ is shorthand for http://server.de/FROST-Server/v1.0/

All other object types can be accessed in the same manner using the URLs provided in the SensorThings API landing page.


## A Specific Object

A specific object (in this case Thing) can be requested from the API based on its identifier.
In the example above one can see that the property `@iot.id` has the value 1, adding this identifier at
 the end of the URL in round brackets () creates the URL for this specific object as follows
  (Alternatively, this URL is provided in the `@iot.selfLink` attribute of this object): 

```
http://server.de/FROST-Server/v1.0/Things(1)
```

The response to the request for a specific object (Thing) consists of exactly this object:
```javascript
{
  "description" : "camping lantern",
  "properties" : {
    "property1" : "it’s waterproof",
    "property2" : "it glows in the dark"
  },
  "Locations@iot.navigationLink" : "http://…/Things(1)/Locations",
  "HistoricalLocations@iot.navigationLink" : "http://…/Things(1)/HistoricalLocations",
  "Datastreams@iot.navigationLink" : "http://…/Things(1)/Datastreams",
  "@iot.id" : 1,
  "@iot.selfLink" : "http://…/Things(1)"
}
```

All other object types can be directly addressed in the same manner.


## A Related Object

One of the strengths of the SensorThings API is the ability to easily navigate through data along the associations between object types.
For example, if one wants to know what Datastreams are available for a specific Thing, all one needs to do is add the name of the
Datastreams class to the end of the URL for the specific Thing as follows (Alternatively, this URL is provided in the "Datastreams@iot.navigationLink"
attribute of this object):

```
http://server.de/FROST-Server/v1.0/Things(1)/Datastreams
```

This request provides a list of all Datastreams associated with Thing 1 in the following response format:

```javascript
{
  "value" : [ {
    "description" : "Temperature measurements for Thing 1",
    "observationType" : "http://www.opengis.net/…/OGC-OM/2.0/OM_Measurement",
    "unitOfMeasurement" : {
      "name" : "Celsius",
      "symbol" : "°C",
      "definition" : "http://www.qudt.org/…/Instances.html#Celsius"
    },
    "phenomenonTime" : "2016-06-22T13:44:52+02:00/2016-06-22T13:44:53+02:00",
    "resultTime" : "2016-06-22T13:44:52+02:00/2016-06-22T13:44:52+02:00",
    "Sensor@iot.navigationLink" : "http://…/Datastreams(1)/Sensor",
    "ObservedProperty@iot.navigationLink" : "http://…/Datastreams(1)/ObservedProperty",
    "Thing@iot.navigationLink" : "http://…/Datastreams(1)/Thing",
    "Observations@iot.navigationLink" : "http://…/Datastreams(1)/Observations",
    "@iot.id" : 1,
    "@iot.selfLink" : "http://…/Datastreams(1)"
  }, {…}, {…} ]
}
```

All associations between objects can be traversed in the same manner.


