---
layout: default
title: Filtering Entities
category: gettingData
topCategory: STA
order: 4
---

# Filtering

In many use cases, the response should only contain objects that pertain to some specific criteria.
The `$filter` request parameter makes it possible to filter the data to be returned based on the values of specific attributes of the requested data.
Thus, one could request all values above a certain threshold or measured between two specific points in time.
The following request returns all Observations where the result value is greater than 5:

```
http://…/Observations?$filter=result gt 5
```

This request provides the following response:

```javascript
{
  "@iot.count" : 8,
  "@iot.nextLink" : "/v1.1/Observations?$filter=result gt 5&$top=4&$skip=4",
  "value" : [
    {
      "phenomenonTime" : "2016-06-22T13:21:31.144Z",
      "resultTime" : null,
      "result" : 10,
      "@iot.id" : 34,
      "@iot.selfLink" : "/FROST-Server/v1.1/Observations(34)"
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

To make it easier to get the correct order of parameters in functions, remember that in general, functions can be be read as:

    parameter1 functionname parameter2

For example:

    name startswith 'room'.


### String Functions

String matches are case sensitive.

Function | Description &amp; Example
--- | --- 
`substringof(s1, s2)` <br/> bool | Returns true if s1 is a substring of s2 <br/> `Things?$filter=substringof('room', name)` matches `livingroom` and `room S01`
`endswith(s1, s2)` <br/> bool | Returns true if s1 ends with s2 <br/> `Things?$filter=endswith(name, 'room')` matches `livingroom` but not `room S01`
`startswith(s1, s2)` <br/> bool | Returns true if s1 starts with s2 <br/> `Things?$filter=endswith(name, 'room')` matches `room S01` but not `livingroom`
`substring(s1, i1)` <br/> string | Returns the substring of s1, starting at position i1 <br/> `substring(description,1) eq 'ensor Things'`
`substring(s1, i1, i2)` <br/> string | Returns the substring of s1, starting at position i1, with length i2 <br/> `substring(description,2,4) eq 'nsor'`
`length(s1)` <br/> int | Returns the length of string s1 <br/> `length(description) eq 13`
`indexof(s1, s2)` <br/> int | Returns the index of s2 in s1 <br/> `indexof(description,'Sensor') eq 1`
`tolower(s1)` <br/> string | Returns the lower case version of s1 <br/> `tolower(description) eq 'sensor things'`
`toupper(s1)` <br/> string | Returns the upper case version of s1 <br/> `toupper(description) eq 'SENSOR THINGS'`
`trim(s1)` <br/> string | Returns the string s1, with whitespace trimmed from start and end <br/> `trim(description) eq 'Sensor Things'`
`concat(s1, s2)` <br/> string | Returns a string composed of s2 added to the end of s1 <br/> `concat(concat(unitOfMeasurement/symbol,', '), unitOfMeasurement/name) eq 'degree, Celsius'`


### Mathematical Functions

Mathematical functions work on all fields that are numeric, and on numerical constants.

Function | Description &amp; Example
--- | --- 
`round(n1)` <br/> int | Returns n1 rounded to the nearest integer <br/> `round(result) eq 42` matches 41.50 to 42.49
`floor(n1)` <br/> int | Returns n1, rounded down to the nearest integer less than n1 <br/> `floor(result) eq 42` matches 42.00 to 42.99
`ceiling(n1)` <br/> int | Returns n1, rounded up to the nearest integer larger than n1 <br/> `ceiling(result) eq 42` matches 41.01 to 42.00


### Geospatial Functions

Geospatial functions work on all geospatial fields (Location/location and FeatureOfInterest/feature) and on geospatial constants.
Geospatial constants can be specified by using WKT enclosed in `geography'…'`, for example:

```
geography'POINT (30 10)'
geography'LINESTRING (30 10, 10 30, 40 40)'
geography'POLYGON ((30 10, 10 20, 20 40, 40 40, 30 10))'
```

Function | Description &amp; Example
--- | ---
`geo.intersects(g1, g2)` <br/> bool | Returns true if g1 intersects g2 <br/> `geo.intersects(location, geography'POLYGON ((30 10, 10 20, 20 40, 40 40, 30 10))')`
`geo.length(g1)` <br/> number | Returns the length of geometry g1  <br/> `geo.length(location) lt 2` matches all locations that are linestrings with a length less than 2 degrees
`geo.distance(g1, g2)` <br/> number | Returns the distance between g1 and g2 in the units of the server (generally degrees) <br/> `geo.distance(location, geography'POINT (30 10)') lt 1`
`st_equals(g1, g2)` <br/> bool | Returns true if g1 is the same as g2 <br/> `st_equals(location, geography'POINT (30 10)')`
`st_disjoint(g1, g2)` <br/> bool | Returns true if g1 is separated from g2 <br/> `st_disjoint(location, geography'POLYGON ((30 10, 10 20, 20 40, 40 40, 30 10))')`
`st_touches(g1, g2)` <br/> bool | Returns true if g1 touches g2 <br/> `st_touches(location, geography'LINESTRING (30 10, 10 30, 40 40)')`
`st_within(g1, g2)` <br/> bool | Returns true if g1 is within g2 <br/> `st_within(location, geography'POLYGON ((30 10, 10 20, 20 40, 40 40, 30 10))')`
`st_overlaps(g1, g2)` <br/> bool | Returns true if g1 overlaps g2 <br/> `st_overlaps(location, geography'POLYGON ((30 10, 10 20, 20 40, 40 40, 30 10))')`
`st_crosses(g1, g2)` <br/> bool | Returns true if g1 crosses g2 <br/> `st_crosses(location, geography'LINESTRING (30 10, 10 30, 40 40)')`
`st_intersects(g1, g2)` <br/> bool | Returns true if g1 intersects g2 <br/> `st_intersects(location, geography'LINESTRING (30 10, 10 30, 40 40)')`
`st_contains(g1, g2)` <br/> bool | Returns true if g1 contains g2 <br/> `st_contains(location, geography'POINT (30 10)')`
`st_relate(g1, g2, s1)` <br/> bool | Returns true if g1 has a relation with g2 given the [intersection matrix pattern](https://en.wikipedia.org/wiki/DE-9IM) s1 <br/> `st_relate(location, geography'POLYGON ((30 10, 10 20, 20 40, 40 40, 30 10))', 'T********')`


### Temporal Functions

Temporal functions other than `now()` operate on the time as stored in the server.
For FROST-Server this is always in the timezone UTC, but for other servers this may also be the timezone of the original value as it was stored.

Function | Description &amp; Example
--- | --- 
`now()` <br/> datetime | Returns the current time, in the timezone of the server <br/> `phenomenonTime lt now()`
`mindatetime()` <br/> datetime | Returns the minimum time that can be stored in the server <br/> 
`maxdatetime()` <br/> datetime | Returns the maximum time that can be stored in the server <br/> 
`date(t1)` <br/> date | Returns the date part of time t1 <br/> `date(resultTime) ne date(validTime)`
`time(t1)` <br/> time | Returns the time part of time t1 <br/> `time(phenomenonTime) le time(1990-01-01T12:00:00Z)` returns all observations taken between midnight and noon
`year(t1)` <br/> int | Returns the year part of time t1 <br/> `year(phenomenonTime) eq 2015`
`month(t1)` <br/> int | Returns the month part of time t1 <br/> `month(phenomenonTime) eq 12`
`day(t1)` <br/> int | Returns the day part of time t1 <br/> `day(phenomenonTime) eq 31`
`hour(t1)` <br/> int | Returns the hour part of time t1 <br/> `hour(phenomenonTime) eq 23`
`minute(t1)` <br/> int | Returns the minute part of time t1 <br/> `minute(phenomenonTime) eq 59`
`second(t1)` <br/> int | Returns the second part of time t1 <br/> `second(phenomenonTime) eq 59`
`fractionalseconds(t1)` <br/> double | Returns the millisecond part of time t1 <br/> `fractionalseconds(phenomenonTime) eq 0`
`totaloffsetminutes(t1)` <br/> int | Returns the offset part of time t1 <br/> `totaloffsetminutes(phenomenonTime) eq 60`


