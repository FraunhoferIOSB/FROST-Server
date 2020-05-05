---
layout: default
title: Example Queries
category: STA
order: 6
---

All these examples are not urlencoded, for readability. If you use these examples, don't forget to urlencode.

## Greater than and smaller than
All things with an Id greater equal 5 and lessequal 20:

`v1.0/Things?$filter=id ge 15 and id le 20`

## Overlapping time frames
The phenomenonTime and result of the first 1000 observations, ordered by phenomenonTime that overlap with the time frame from 2017-10-10 07:00:00 UTC to 2017-10-11 07:00:00 UTC (1 day):
```
v1.0/Observations
  ?$orderby=phenomenonTime asc
  &$top=1000
  &$select=phenomenonTime, result
  &$filter=overlaps(phenomenonTime, 2017-10-10T07:00:00Z/2017-10-11T07:00:00Z)
```

## The previous day
The observations of a Datastream, for the last day:

```
Datastreams(1)/Observations?$filter=phenomenonTime gt now() sub duration'P1D'
```

## The last x days

The observations of a Datastream, for the last days, where the number of days is configured in properties/days of the Datastream:

```
Datastreams(1)/Observations?$filter=phenomenonTime gt now() sub duration'P1D' mul Datastream/properties/days
```

## Odd or even

All observations with an even result
```
Observations?$filter=result mod 2 eq 0
```

## Filters on related Entities

Datastreams that have data for the ObservedProperty with id 1
```
Datastreams?$filter=ObservedProperty/@iot.id eq 1
```

ObservedProperties that are measured at the same station as the ObservedProperty with name Temperature
```
ObservedProperties?$filter=Datastream/Thing/Datastreams/ObservedProperty/name eq 'Temperature'
```


## Threshold detection

Filtering Observations where result is greater than a threshold stored in the properties of their Datastream. The "add 0" is to indicate we want to use a numeric comparison. Since both Observations/result and Datastreams/properties/max can be anything, the server would otherwise default to a (safe) string-comparison.
```
Observations?$filter=result gt Datastream/properties/max add 0
```

## Ordering by function

Functions work for Ordering
```
Datastreams?$orderby=length(name) desc
```

## Give me EVERYTHING!
All things, with their current Locations and Datastreams, and for those Datastreams the ObservedProperty and the last Observation:
```
v1.0/Things
  ?$select=name,description,@iot.id
  &$expand=
    Locations
      ($select=name,description,location,@iot.id)
    ,Datastreams
      ($select=name,description,@iot.id
      ;$expand=
        Sensor
          ($select=name,description,@iot.id)
        ,ObservedProperty
          ($select=name,description,@iot.id)
        ,Observations
          ($select=result,phenomenonTime,@iot.id
          ;$orderby=phenomenonTime%20desc
          ;$top=2
          )
      )
```


