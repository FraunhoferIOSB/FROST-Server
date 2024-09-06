---
layout: default
title: Response Metadata
category: extensions
order: 18
---

# Response Metadata

This extension allows to limit the amount of control information in responses, adapted from [OData JSON Format Version 4.01 - 3.1](http://docs.oasis-open.org/odata/odata-json-format/v4.01/odata-json-format-v4.01.html#sec_ControllingtheAmountofControlInforma).

The metadata can be applied to the $resultMetadata parameter of a request to influence how much control information will be included in the response.

Sample requests:

```
GET /v1.1/Things(1)?$resultMetadata=none
POST /v1.1/Things?$resultMetadata=off
POST /v1.1/CreateObservations?$resultMetadata=off
```

## resultMetadata=full

The full list of control information that may appear in a resultMetadata=full response,
which is the same as what is defined in SensorThings standard, is as follows:

- count: the total count of a collection of entities, if requested;
- navigationLink: the link used to retrieve the values of a navigation property;
- nextLink: the next link of a collection with partial results;
- selfLink: URL of an entity.

## resultMetadata=minimal and resultMetadata=none

resultMetadata=minimal and resultMetadata=none remove all control information listed in resultMetadata=full section, except:

- count: if requested;
- nextLink: the next link of a collection with partial results.

## resultMetadata=off

resultMetadata=off does not comply with OData specification and removes all control information listed in resultMetadata=full section, except:

- count: if requested

The created entity URLs are also removed in POST responses, either in location header or in content.

Sample Thing entity creation request and response:

```
POST /v1.1/Things?$resultMetadata=off HTTP/1.1
201 Created
Host: example.org
Content-Length: 0
```

Sample CreateObservations request and response:

```
POST /v1.1/CreateObservations?$resultMetadata=off HTTP/1.1
201 Created
Host: example.org
Content-Length: 15
Content-Type: application/json

["","error",""]
```

## Conformance Class

The conformance class this extension must register in the SensorThings (v1.1 and up) index document is:

    https://fraunhoferiosb.github.io/FROST-Server/extensions/ResponseMetadata.html


