---
layout: default
title: Tailoring Responses
category: STA
order: 3
---

# Tailoring Responses

There are many ways a user may wish to custom tailor the response from the SensorThings API for various usage types.
For this purpose, SensorThings API provides the following request parameters, that are described in more detail in the sections below.

* **$top:** specify the maximum number of objects to be returned. The usual default setting for $top is 100.
* **$skip:** used for paging, skip over the first n records and provide records from the n + 1 on.
* **$count:** return the total number of objects in the response. The usual default setting for $count is false.
* **$orderBy:** used to specify that the returned objects should be ordered by a specific attribute, either ascending or descending.
* **$select:** specify exactly which attributes are to be provided in the response. 
* **$filter:** specify filters that control which entities are returned. See [Filtering](STA-Filtering)
* **$expand:** create a response returning multiple object types nested within each other. See [Expanding](STA-Expanding)

Parameters are separated using the `&` character and each request parameter can appear only once, unless used inside the $expand parameter.


## Returning Specific Attributes of an Object

Most applications do not require all attributes provided by the SensorThings API object types.
While such as reduction in the breadth of the data may not seem especially relevant pertaining to one individual object, when thousands of objects are requested such reduction through filtering can vastly reduce the volume of data being transferred.
In the following example, we request all Things from the API, but wish to reduce the individual objects to their identifier and description. The request for this is as follows:

```
http://server.de/FROST-Server/v1.0/Things?$select=@iot.id,description
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


## Counts

In many use cases, it is important to know how many objects of a specific type are available from the API. While many default deployments do not automatically return this value, setting the request parameter “count” to true enables this functionality. When “$count=true” is appended to the request URL, the response will contain an "@iot.count" attribute providing the total number of objects available from the API data source.
A closely related request parameter is “top”, that makes it possible to specify the maximum number of objects to be returned in the response. Adding “$top=4” to the request URL will provide a maximum of 4 objects in the response. The following URL specifies that at most 4 Observations are to be returned, together with the total number of Observations available from the API:

```
http://server.de/FROST-Server/v1.0/Observations?$top=4&$count=true
```

This request provides the following response:

```
{
  "@iot.count" : 16,
  "@iot.nextLink" : "/FROST-Server/v1.0/Observations?$top=4&$skip=4",
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
```

Based on this response, we can see that four of the sixteen available Observations have been returned.
The “$count” parameter can also be used inside an $expand parameter, for instance, to request the number of Datastreams in a Thing.


## Sorting Responses

It is often useful to receive a dataset ordered by a specific attribute. The “orderBy” request parameter is available for this purpose, the response dataset can be ordered in either ascending or descending order by use of “asc” or desc” respectively.
```
http://.../Observations?$orderby=phenomenonTime asc
```
A useful application of this request parameter is to pair it with “top” to get the latest available observation value for a feature:
```
http://.../ FeaturesOfInterest(1)/Observations?$orderby=phenomenonTime desc&$top=1
```

## Mixing Request Parameters

Top-level `$options` are separated with a `&`. 

    v1.1/Things?$expand=Locations&$top=1

Use `,` to separate items in a `$select` or `$expand`.

    v1.1/Things?$expand=Locations,Datastreams&$top=1

Within a `$expand`, you use `;` to separate `$options`. Use `( )` to clearly specify which request parameters apply to which expanded object type

    v1.1/Things?$expand=Locations($select=id,name;$top=1),Datastreams($select=name,id;$top=1)&$top=1
