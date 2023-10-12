---
layout: default
title: Expanding Entities in MQTT
category: gettingData
topCategory: STA
order: 26
---

# Extending MQTT Responses with nested Entities

The function described here is an MQTT version of the function "Expanding", which is based on HTTP "Expanding" function. Therefore the purpose is similar.

In many use cases, one wishes to join together related object types within one MQTT subscription topic. A common example is to provide a Thing, together with all associated Datastreams.

As MQTT is event based, whenever a parameter of any Thing changes (relations, e.g. Datastreams, excluded), responses will be sent to the subscribing clients.

**ATTENTION:** This function may create a high number of database reads and cause a performance drop.

To activate the function, the expand parameter is added to the subscription topic:

```
v1.1/Things
	?$select=@iot.id,description
	&$expand=Datastreams
```

This subscription topic will provide the identifier and description of all Things which have changed, together with all associated Datastreams:

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

In order to reduce the volume of the transferred data, it is possible to reduce the content of the Datastreams to selected parameters. This is done by adding the select parameter for the Datastreams:

The example below reduces the content of the returned Datastreams to their identifier and description.

```
v1.1/Things
	?$select=@iot.id,description
	&$expand=Datastreams($select=@iot.id,description)
```

The resulting response provides us with a simple overview of the changed Thing together with the available Datastreams:

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

Inside each expand, all the MQTT topic parameters can be used, including `$expand` itself.
We could expand the Datastreams in the example above with the result and phenomenonTime of the latest Observation of each Datastream, and the total number of Observations.

```
v1.1/Things
	?$select=@iot.id,description
	&$expand=Datastreams(
		$select=@iot.id,description;
		$expand=Observations(
			$select=result,phenomenonTime;
      "$orderby=phenomenonTime desc;
			$top=1;
			$count=true"
		)
	)
```

For clarity, the MQTT topic example above is split over multiple lines. A real topic would be a single line.
Inside each expand, the parameters are separated with a `;` character, not with a `&`.
The resulting response provides us with a simple overview of a Thing, together with the associated Datastreams and the last Observation for each Datastream:

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
