---
layout: default
title: Tailoring Requests
category: STA
order: 3
---

# Tailoring Requests

In many cases, the user may wish to custom tailor the response from the SensorThings API so that exactly the required values are returned,
greatly reducing overhead. SensorThings API provides the request parameters filter and expand for this purpose.

* The **filter** request parameter makes it possible to specify exactly which attributes are to be provided in the response. 
* The **expand** request parameter makes it possible to create a response returning multiple object types nested within each other.

## Returning Specific Attributes of an Object

Most applications do not require all attributes provided by the SensorThings API object types. While such as reduction in the breadth of the data may not seem especially relevant pertaining to one individual object, when thousands of objects are requested such reduction through filtering can vastly reduce the volume of data being transferred.
In the following example, we request all Things from the API, but wish to reduce the individual objects to their identifier and description. The request for this is as follows:

```
http://server.de/SensorThingsService/v1.0/Things?$select=@iot.id,description
```

This request provides a list of all Things reduced to the two attributes description and iot.id in the response:

```
{
  "value" : [
    {
      "description" : "camping lantern",
      "@iot.id" : 1
    },
    {
      "description" : "camping stove",
      "@iot.id" : 2
    }
  ]
}
```

## Extending with further Object Types
In many use cases, one wishes to join together related object types within one request. A common example of this is to provide all Things together with all Datastreams associated with this Thing. Building on the example above, we’d like to add all Datastreams to the reduced Thing description described above. As we are performing this request only for a specific Thing with the identifier 1, the identifier of the Thing is included in the URL. For this purpose, we must add the expand request parameter to the URL from the previous example as follows:

```
http://server.de/SensorThingsService/v1.0/Things(1)?$select=@iot.id,description&$expand=Datastreams
```

This request provides the identifier and description of Thing with identifier 1, together with all Datastreams associated with this Thing:

```
{
  "description" : "camping lantern",
  "@iot.id" : 1,
  "Datastreams" : [
    {
      "description" : "Temperature measurement",
      "observationType" : "http://www.opengis.net/.../OGC-OM/2.0/OM_Measurement",
      "unitOfMeasurement" : {
        "name" : "Celsius",
        "symbol" : "°C",
        "definition" : "http://www.qudt.org/.../Instances.html#Celsius"
      },
      "phenomenonTime" : "2016-06-22T15:21:31+02:00/2016-06-22T15:21:31+02:00",
      "resultTime" : "2016-06-22T15:21:31+02:00/2016-06-22T15:21:31+02:00",
      "@iot.id" : 19,
      "@iot.selfLink" : "http://.../Datastreams(19)"
    }, {…}, {…}
  ]
}
```

## More and Less Content

As a further step, we only want an overview of the associated Datastreams; we’d like to reduce the content of the returned Datastreams to their identifier and description. This is done by adding the select request parameter for the Datastreams at the end of the URL from the example above:

```
http://server.de/SensorThingsService/v1.0/Things(1)?$select=@iot.id,description&$expand=Datastreams($select=@iot.id,description)
```

The resulting response provides us with a simple overview of Thing 1 together with the available Datastreams pertaining to this Thing:

```
{
  "description" : "camping lantern",
  "@iot.id" : 17,
  "Datastreams" : [
    {
      "description" : "Temperature measurement",
      "@iot.id" : 19
    },
    {
      "description" : "Humidity measurement",
      "@iot.id" : 21
    }
  ]
}
```

## Counts
In many use cases, it is important to know how many objects of a specific type are available from the API. While many default deployments do not automatically return this value, setting the request parameter “count” to true enables this functionality. When “$count=true” is appended to the request URL, the response will contain an "@iot.count" attribute providing the total number of objects available from the API data source.
A closely related request parameter is “top”, that makes it possible to specify the maximum number of objects to be returned in the response. Adding “$top=4” to the request URL will provide a maximum of 4 objects in the response. The following URL specifies that at most 4 Observations are to be returned, together with the total number of Observations available from the API:

```
http://server.de/SensorThingsService/v1.0/Observations?$top=4&$count=true
```

This request provides the following response:

```
{
  "@iot.count" : 16,
  "@iot.nextLink" : "/SensorThingsService/v1.0/Observations?$top=4&$skip=4",
  "value" : [
    {
      "phenomenonTime" : "2016-06-22T13:21:31.144Z",
      "resultTime" : null,
      "result" : 1,
      "@iot.id" : 34,
      "@iot.selfLink" : "http://.../Observations(34)"
    }, {
      …
    }, {
      …
    }, {
      …
    }
  ]
}
Based on this response, we can see that four of the sixteen available Observations have been returned.
```

