---
layout: default
title: Deep Select
category: extensions
order: 5
---

# Deep Select

When fetching entities with large properties or parameters fields, it is sometimes desirable to only fetch parts of the properties or parameters.
This extension enables fetching only parts of complex properties, like `Thing/properties`, `Observation/parameters` or `Location/location`.

## Syntax and Returned Data

Selected sub-fields are separated by a `/`.

The nesting structure of the returned fields is not changed.

For example, the following request returns the name and the properties/type field of all Things:
```
v1.1/Things?$select=name,properties/type
```
The returned data could be:
```JSON
{
    "value": [
        {
            "name": "Brunnentobelbach",
            "properties": {
                "type": "waterBody"
            }
        },
        {
            "name": "Ramstel",
            "properties": {
                "type": "waterBody"
            }
        },
        {
            "name": "Erlenbach",
            "properties": {
                "type": "waterBody"
            }
        },
        {
            "name": "Ehemaliger Kraftwerksleerschuss",
            "properties": {
                "type": "waterBody"
            }
        }
    ]
}
```


## Conformance Class

The conformance class this extension must register in the SensorThings (v1.1 and up) index document is:

    https://fraunhoferiosb.github.io/FROST-Server/extensions/DeepSelect.html


