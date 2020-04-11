---
layout: default
title: Filtering Entities
category: STA
order: 4
---

# Filtering

In many use cases, the response should only contain objects that pertain to some specific criteria.
The “filter” request parameter makes it possible to filter the data to be returned based on the values of specific attributes of the requested data.
Thus, one could request all values above a certain threshold or measured between two specific points in time.
The following request returns all Observations where the result value is greater than 5:

```
http://.../Observations?$filter=result gt 5
```

This request provides the following response:

```
{
  "@iot.count" : 8,
  "@iot.nextLink" : "/v1.0/Observations?$filter=result gt 5&$top=4&$skip=4",
  "value" : [
    {
      "phenomenonTime" : "2016-06-22T13:21:31.144Z",
      "resultTime" : null,
      "result" : 10,
      "@iot.id" : 34,
      "@iot.selfLink" : "/FROST-Server/v1.0/Observations(34)"
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


## Operators

The following shows operators that can be used when composing complex filter requests.


### Comparison Operators

Operator | Description | Example
--- | --- | ---
eq | Equal | /ObservedProperties?$filter=name eq 'CO2'
ne | Not equal | /ObservedProperties?$filter=name ne 'CO2'
gt | Greater than | /Observations?$filter=result gt 5
ge | Greater than or equal | /Observations?$filter=result ge 5
lt | Less than | /Observations?$filter=result lt 5
le | Less than or equal | /Observations?$filter=result le 5


### Logical Operators

Operator | Description | Example
--- | --- | ---
and | Logical and | /Observations?$filter=result le 5 and FeatureOfInterest/id eq '1'
or | Logical or | /Observations?$filter=result gt 20 or result le 3.5
not | Logical negation | /Things?$filter=not startswith(description,'test')


### Grouping Operators

Operator | Description | Example
--- | --- | ---
( ) | Precedence grouping | /Observations?$filter=(result sub 5) gt 10


## Functions
### String Functions

`substringof(p0, p1)`

`endswith(p0, p1)`

`startswith(p0, p1)`

`substring(p0, p1)`

`indexof(p0, p1)`

`length(p0)`

`tolower(p0)`

`toupper(p0)`

`trim(p0)`

`concat(p0, p1)`


### Mathematical Functions

`round(n1)`

`floor(n1)`

`ceiling(n1)`


### Geospatial Functions

`geo.intersects(g1, g2)`

`geo.length(l1)`

`geo.distance(g1, g2)`

`st_equals(g1, g2)`

`st_disjoint(g1, g2)`

`st_touches(g1, g2)`

`st_within(g1, g2)`

`st_overlaps(g1, g2)`

`st_crosses(g1, g2)`

`st_intersects(g1, g2)`

`st_contains(g1, g2)`

`st_relate(g1, g2)`


### Temporal Functions

`now()`

`mindatetime()`

`maxdatetime()`

`date(t1)`

`time(t1)`

`year(t1)`

`month(t1)`

`day(t1)`

`hour(t1)`

`minute(t1)`

`second(t1)`

`fractionalseconds(t1)`

`totaloffsetminutes(t1)`

