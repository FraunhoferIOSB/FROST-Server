---
layout: default
title: Expanding Entities
category: gettingData
topCategory: STA
order: 5
---

# Extending Responses with nested Entities

In many use cases, one wishes to join together related object types within one request.
A common example of this is to provide all Things together with all Datastreams associated with this Thing.
Building on the example above, we’d like to add all Datastreams to the reduced Thing description described above.
As we are performing this request only for a specific Thing with the identifier 1, the identifier of the Thing is included in the URL.
For this purpose, we must add the expand request parameter to the URL from the previous example as follows:

```
http://server.de/FROST-Server/v1.1/Things(1)
	?$select=@iot.id,description
	&$expand=Datastreams
```

This request provides the identifier and description of Thing with identifier 1, together with all Datastreams associated with this Thing:

```javascript
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
http://server.de/FROST-Server/v1.1/Things(1)
	?$select=@iot.id,description
	&$expand=Datastreams($select=@iot.id,description)
```

The resulting response provides us with a simple overview of Thing 1 together with the available Datastreams pertaining to this Thing:

```javascript
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
Inside each expand, all the request parameters can be used, including `$expand` itself.
We could expand the Datastreams in the example above with the result and phenomenonTime of the latest Observation of each Datastream, and the total number of Observations.


```
http://server.de/FROST-Server/v1.1/Things(1)
	?$select=@iot.id,description
	&$expand=Datastreams(
		$select=@iot.id,description;
		$expand=Observations(
			$select=result,phenomenonTime;
			$orderby=phenomenonTime desc;
			$top=1;
			$count=true
		)
	)
```

For clarity, the request example above is split over multiple lines. A real request would be a single line.
Inside each expand, the request parameters are separated with a `;` character, not with a `&`.
The resulting response provides us with a simple overview of Thing 1 together with the available Datastreams pertaining to this Thing, and the last Observation for each Datastream:

```javascript
{
  "description" : "camping lantern",
  "@iot.id" : 17,
  "Datastreams" : [
    {
      "description" : "Temperature measurement",
      "@iot.id" : 19,
      "Observations": [
        {
          "phenomenonTime": "2020-04-10T16:00:00.000Z",
          "result": 4.6
        }
      ],
      "Observations@iot.count": 11179,
      "Observations@iot.nextLink": "http://server.de/FROST-Server/v1.1/Datastreams(19)/Observations?$top=1&$skip=1&$select=result,phenomenonTime&$orderby=phenomenonTime+desc&$count=true"
    },
    {
      "description" : "Humidity measurement",
      "@iot.id" : 21,
      "Observations": [
        {
          "phenomenonTime": "2020-04-10T16:00:00.000Z",
          "result": 40.5
        }
      ],
      "Observations@iot.count": 19938,
      "Observations@iot.nextLink": "http://server.de/FROST-Server/v1.1/Datastreams(21)/Observations?$top=1&$skip=1&$select=result,phenomenonTime&$orderby=phenomenonTime+desc&$count=true"
    }
  ]
}
```

We now see serveral new things in the response:
* Inside each Datastream there is a field `Observations@iot.count` the holds the total number of Observations for this Datastream.
* Each Datastream has an `Observations@iot.nextLink` that holds a URL that points to the next Observation in the Datastream.

