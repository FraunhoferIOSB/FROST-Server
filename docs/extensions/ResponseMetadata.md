---
layout: default
title: Response Metadata
category: extensions
order: 8
---

# Response Metadata

This extension allows to limit the amount of control information in responses, adapted from [OData JSON Format Version 4.01 - 3.1](http://docs.oasis-open.org/odata/odata-json-format/v4.01/odata-json-format-v4.01.html#sec_ControllingtheAmountofControlInforma).

The metadata can be applied to the $resultMetadata parameter of a request to influence how much control information will be included in the response.

Sample requests:

```
GET /v1.1/Things(1)?$resultMetadata=full
POST /v1.1/Things?$resultMetadata=minimal
POST /v1.1/CreateObservations?$resultMetadata=off
```

## metadata=full

The full list of control information that may appear in a metadata=full response,
which is the same as what is defined in SensorThings standard, is as follows:

- count: the total count of a collection of entities, if requested;
- navigationLink: the link used to retrieve the values of a navigation property;
- nextLink: the next link of a collection with partial results;
- selfLink: URL of an entity.

## metadata=minimal

metadata=minimal removes all control information listed in metadata=full section, except:

- count: if requested;
- nextLink: the next link of a collection with partial results.

The created entity URLs are also removed in responses, either in location header or in content.

## metadata=off

metadata=off removes all control information listed in metadata=full section, except:

- count: if requested

The created entity URLs are also removed in responses, either in location header or in content.

## Conformance Class

The conformance class this extension must register in the SensorThings (v1.1 and up) index document is:

    https://fraunhoferiosb.github.io/FROST-Server/extensions/ResponseMetadata.html


