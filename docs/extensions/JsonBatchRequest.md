---
layout: default
title: JSON Batch Requests
category: extensions
order: 7
---

# JSON Batch Requests

Standard SensorThings API v1.0 and v1.1 only supports multipart batch requests adapted from [OData Version 4.0 Part 1: Protocol - 11.7](http://docs.oasis-open.org/odata/odata/v4.0/os/part1-protocol/odata-v4.0-os-part1-protocol.html#_Toc372793748).

This extension allows JSON batch requests adapted from [OData Version 4.01. Part 1: Protocol - 11.7](https://docs.oasis-open.org/odata/odata/v4.01/odata-v4.01-part1-protocol.html#_Toc31359017)
and [OData JSON Format Version 4.01 - 19](http://docs.oasis-open.org/odata/odata-json-format/v4.01/odata-json-format-v4.01.html#_Toc38457781).

A Batch Request header example:

```
    POST /v1.1/$batch HTTP/1.1
    Host: example.org
    Content-Type: application/json
    Content-Length: ###

    <JSON Batch request body>
```

## Batch request body example

The following example shows a Batch Request that contains the following operations in the order listed:

- A query request
- Change Set that contains the following 2 requests:
  - Insert entity (with id 1)
  - Update request (with id 2)
- A second query request

```json
{
  "requests": [
    {
      "id": "0",
      "method": "get",
      "url": "Things(1)"
    },
    {
      "id": "1",
      "atomicityGroup": "group1",
      "method": "post",
      "url": "Things",
      "body": <JSON representation of a new Thing>
    },
    {
      "id": "2",
      "atomicityGroup": "group1",
      "method": "patch",
      "url": "Things(1)",
      "body": <JSON representation of Thing(1)>
    },
    {
      "id": "3",
      "method": "get",
      "url": "Things(3)"
    }
  ]
}
```

## Referencing new entities in a change set example


Actions can reference entities previously created. To make a created entity referenceable, the POST that creates the entity must have
a id property, the content of which can be any string. Subsequent requests in the same
change set can now use the value of this property, prefixed with a $, in places where the ID of the
created entity is required.

Example: A Batch Request that containing a single change set that contains the following requests:

1. Insert a new Sensor entity (with id sensor1)
2. Insert a new Datastream entity, referencing the previously created Sensor entity as "$sensor1".
 

```json
{
  "requests": [
    {
      "id": "sensor1",
      "atomicityGroup": "group1",
      "method": "post",
      "url": "Sensors",
      "body": {
		  "name": "DS18B20",
		  "description": "DS18B20 is an air temperature sensor",
		  "encodingType": "application/pdf",
		  "metadata": "http://datasheets.maxim-ic.com/en/ds/DS18B20.pdf"
		}
    },
    {
      "id": "2",
      "atomicityGroup": "group1",
      "method": "post",
      "url": "Things(5)/Datastreams",
      "body": {
		  "name": "Temperature Thing 5",
		  "description": "The temperature of thing 5",
		  "ObservedProperty: {"@iot.id": 12},
		  "Sensor": {"@iot.id": "$sensor1"}
		}
    }
  ]
}
```

## Batch-processing response

Example referencing the first batch request example above, assume all the requests except the final query request succeed. In this case the response would be:

```json
{
  "responses": [
    {
      "id": "0",
      "status": 200,
      "body": <JSON representation of the Thing entity with id = 1>
    },
    {
      "id": "1",
      "status": 201,
      "body": <JSON representation of a new Thing entity>,
      "location": "http://example.org/v1.1/Things(99)"
    },
    {
      "id": "2",
      "status": 204
    },
    {
      "id": "3",
      "status": 404,
      "body": <Error message>
    }
  ]
}
```

## Conformance Class

The conformance class this extension must register in the SensorThings (v1.1 and up) index document is:

    https://fraunhoferiosb.github.io/FROST-Server/extensions/JsonBatchRequest.html


