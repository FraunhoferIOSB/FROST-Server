---
layout: default
title: The Data Model
category: STA
order: 1
---

# The SensorThings API Data Model

In order to understand the SensorThings API, it is essential to understand the underlying data model. This model consists of the following classes:

* **Things**: Some real-world thing, in this context usually the thing where the sensor is located. This could be a monitoring station or just the room where the sensor has been mounted.
* **Locations**: Location of the Thing. The Location is provided separately from the Thing as a Thing can move to a different Location.
* **Datastreams**: a Datastream links together a Thing with a Sensor that is measuring an ObservedProperty to provide an entry point for a time series. All Observations created by the Sensor pertaining to the ObservedProperty are linked to this Datastream.
* **Sensors**: A description of a sensor that provides values, including information on the measurement method. While formally designed for sensors in the IoT context, the Sensor could also be a human observer.
* **ObservedProperties**: The ObservedProperties describe what is being measured by a Sensor at a specific Location.
* **Observations**: A single measurement value. Information on what ObservedProperty was measured with which type of Sensor is provided by the Datastream to which this Observation is linked; the object on which the measurement was performed is provided by the FeatureOfInterest of this Observation.
* **FeaturesOfInterest**: the FeatureOfInterest is the object on which the measurement was performed.

![SensorThings API Data Model](images/SensorThingsAPI_DatenModel_v1.1-900.png)

