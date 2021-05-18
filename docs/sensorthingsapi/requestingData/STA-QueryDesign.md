---
layout: default
title: Designing Queries
category: gettingData
topCategory: STA
order: 6
---

# Designing Queries

The hardest part of creating queries for the SensorThings API is knowing where to start.
There are 8 entity types (12 with all extensions) and all data can be reached from each entity type.
To get Observations one could start:

- directly from /Observations
- from /Datastreams and expand Observations
- from /FeaturesOfInterest and expand Observations
- from /Things and expand Datastreams & Observations
- from /ObservedProperties and expand Datastreams & Observations
- from /Sensors and expand Datastreams & Observations
- from /Locations and expand Things, Datastreams & Observations

All these options can give the same data, but it will be structured differently.

![SensorThings API Data Model](../../images/SensorThingsAPI_DatenModel_v1.1-900.png)


## Starting from /Observations

The data is least structured when starting directly from /Observations.
In this case each item in the resulting list is one Observation, but Observations for all Datastreams
(Things/ObservedProperties) or FeaturesOfInterest are mixed together.

Individual timeseries for Datastreams are lost and it is up to the client to sort the data again.
When expanding Datastreams and from there ObservedProperties or Things, the data for the Datastreams is repeated for each Observation.
This result is very much like a CSV file, and quite inefficient.

The resulting data structure looks like this:

- Observation + Datastream + ObservedProperty + Thing
- Observation + Datastream + ObservedProperty + Thing
- Observation + Datastream + ObservedProperty + Thing

It is not possible to get "the latest Observation for each ..." since there is only one list, thus only one "latest Observation".


## Starting from /Things

On the other side of the spectrum is starting from /Things.
Each item in the resulting list will be a Thing, and for each Thing the Datastreams can be expanded, and for each of those the Observations.

- Thing 1
  - Datastream 1 + ObservedProperty 1
    - Observation 1
    - Observation 2
  - Datastream 2 + ObservedProperty 2
    - Observation 3
    - Observation 4
- Thing 2
  - Datastream 3 + ObservedProperty 1
    - Observation 5
    - Observation 6
  - Datastream 4 + ObservedProperty 2
    - Observation 7
    - Observation 8

There is some duplication in the ObservedProperties when fetching data in this way, but the structure is easy to put on a map.



