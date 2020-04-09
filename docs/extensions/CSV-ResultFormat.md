---
layout: default
title: CSV ResultFormat
category: extensions
order: 1
---

# CSV ResultFormat

In order to make data obtained from an OGC SensorThings API service more accessible, this extension provides data in a simple CSV result format.
Since many users still like to work with data in spreadsheet applications, the availability of a CSV result format will help convincing people to use the SensorThings API.

https://github.com/INSIDE-information-systems/SensorThingsAPI/issues/3

The CSV result format can be requested by setting the request parameter `ResultFormat=CSV`.
The resulting CSV document must conform to the [RFC 4180](https://tools.ietf.org/html/rfc4180) specification.


## Single Table

For single-table queries, `ResultFormat=CSV` is mostly self-explanatory. 
The header line contains the direct field names, with each further line having the data of one entity.
Navigation links are omitted.

Tricky parts are how to handle:

- the `properties` and `parameters` field: This is a complex property with an unknown set of fields.
  The only way to deal with this is to include the JSON as is.
  Proper encoding to CSV needs to happen, since JSON contains both double quotes and commas, same as CSV, but this is covered by [RFC 4180](https://tools.ietf.org/html/rfc4180) which defines that double quotes are quoted with a double quote.
- `Datastream/unitOfMeasurement`: This is a complex property with three defined fields. 
  The best way seems to be to split this field into three CSV columns: `unitOfMeasurement/name,unitOfMeasurement/symbol,unitOfMeasurement/definition`

For example:

    id,name,unitOfMeasurement/name,unitOfMeasurement/symbol,unitOfMeasurement/definition
    1,My Datastream,degree celcius,°C,ucum:Cel
    2,My other Datastream,degree celcius,°C,ucum:Cel

The format returned when fetching a list of entities, or a single entity can be the same.


## $select

The `$select` request parameter allows control over the fields that are returned.

Future work: selecting sub-properties of complex properties such as `Datastream/unitOfMeasurement`, `properties` and `parameters` fields.


## $expand

Single table results are a nice start of course, but the power of the SensorThings API lies in its advanced features.
One of the great features is $expand, that allows multi-table results.


### expand with 'single' cardinality

Expanding relations with a cardinality of 1, such as Observation->Datastream or Datastream->Thing is easy.
The fields of the expanded EntityType are added to the header, prefixed with the expanded with the navigation link name.

    id,result,phenomenonTime,Datastream/id,Datastream/name
    1,2.9,2005-08-03T23:00:00.000Z,1,My Datastream
    2,2.8,2005-08-03T23:01:00.000Z,1,My Datastream

This will duplicate some data, but the data would also be duplicated in the normal result format.


### expand with 'many' cardinality

Expands of relations with a cardinality of many, such as Thing -> Datastreams, is not allowed, unless the cardinality is reduced to 1 using the `$top=1` parameter.
When requesting more than one item in such an expand, the server will respond with the error: 400 Bad Request.

The reason for this is:
1. Expanding over a relation with a cardinality of many results in multiple nextLinks.
   One for the main table, and another one for each expand.
   In the CSV result format, these nextLinks can not be provided to the user.
   It is thus impossible to download all data this way.
2. The data resulting from an expand with cardinality many can be received by requesting the other side of the relation, and expanding the relation from the other side.


## MultiDatastreams

MultiDatastreams are not supported (yet) by the CSV result format.

MultiDatastreams bring their own set of complications:

- `MultiDatastreams/unitOfMeasurements` is an array of objects, each with 3 fields
- `MultiDatastreams/multiObservationDataTypes` is an array of strings
- `MultiDatastreams/ObservedProperties` is a navigationList with cardinality many
- `MultiDatastreams/Observations/result` is an array

These four arrays/lists are of the same length for any given MultiDatastream, but may be of different length for the next MultiDatastream.
Furthermore, the order of the items in these lists is important.

Options for dealing with MultiDatastreams are:

- Not allow MultiDatastreams in the CSV resultFormat (avoids the problem)
- Encoding the unitOfMeasurements, multiObservationDataTypes, ObservedProperties and Observations/result sets as json arrays in a single CSV line
- Returning a line for each entry in unitsOfMeasurement, multiObservationDataTypes, ObservedProperties and Observations/result

The last option essentially breaks Observations of a MultiDatastream back into normal Observations.


## Conformance Class

The conformance class this extension must register in the SensorThings (v1.1 and up) index document is:

    https://github.com/INSIDE-information-systems/SensorThingsAPI/blob/master/CSV-ResultFormat/CSV-ResultFormat.md"


