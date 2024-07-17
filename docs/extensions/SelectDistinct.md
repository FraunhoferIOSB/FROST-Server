---
layout: default
title: Select Distinct
category: extensions
order: 16
---

# Select Distinct

It is quite useful to give Entities common properties, like a "type".
But when filtering on such a common property the client needs to know what the used values are.
This extension allows a client to request all distinct values for a field or a set of fields.

Distinct select can be used in Expands, and can be ordered.
When combining `$orderby` with a distinct select, it is only possible to order by the exact
fields that are selected.

Note that selecting distinct values for the `@iot.id` field makes no sense, since the ID field is unique for each entity.

## Syntax and Returned Data

To request the distinct values for a set of selected fields, add the `distinct:` keyword at the start of the `$select` parameter.

The returned data is formatted just like a non-distinct request of the same type would be.

For example, the following request returns all distinct values of the properties/type field of all Things:
```
v1.1/Things?$select=distinct:properties/type
```
The returned data could be:
```JSON
{
    "value": [
        {
            "properties": {
                "type": "waterBody"
            }
        },
        {
            "properties": {
                "type": "station"
            }
        },
        {
            "properties": {
                "type": "aquifer"
            }
        }
    ]
}
```

The following request returns all distinct combinations that exist, for the fields properties/type and properties/subType
```
v1.1/Things?$select=distinct:properties/type,properties/subType
```
The returned data could be:
```JSON
{
    "value": [
        {
            "properties": {
                "subType": "river",
                "type": "waterBody"
            }
        },
        {
            "properties": {
                "subType": "Grundwasserleiter und Grundwassergeringleiter",
                "type": "aquifer"
            }
        },
        {
            "properties": {
                "subType": "Grundwasserleiter",
                "type": "aquifer"
            }
        },
        {
            "properties": {
                "subType": "Grundwassergeringleiter",
                "type": "aquifer"
            }
        },
        {
            "properties": {
                "type": "station"
            }
        }
    ]
}
```

## Conformance Class

The conformance class this extension must register in the SensorThings (v1.1 and up) index document is:

    https://fraunhoferiosb.github.io/FROST-Server/extensions/SelectDistinct.html


