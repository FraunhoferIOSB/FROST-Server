---
layout: default
title: OGC SensorThings API
category: STA
order: 1
---

# The OGC SensorThings API

To quote the standard itself:

> The OGC SensorThings API is an OGC standard specification for providing an open and unified way to interconnect IoT devices, data, and applications over the Web.
> The SensorThings API is an open standard, builds on Web protocols and the OGC Sensor Web Enablement standards, and applies an easy-to-use REST-like style.

SensorThings API provides access to up-to-date measurement information that is:

* RESTful
* (Geo)JSON encoded
* Available via OASIS Odata URL patterns and query options
* Capable of directly including sensor data via the ISO MQTT protocol

This means that data from the API can be easily viewed using a normal Web Browser. One can simply navigate from one object to the next by clicking the URLs provided within the data.

For more information, please see the following sections:
* [Deployment](2_Deploy.html)
* [Requesting Data](3_GettingData.html)
* [Creating Maps](4_Mapping.html)


If you're more for a hands-on approach, you can also just start playing with some of our services. Here's the link for near-real-time air quality data:
* [https://airquality-frost.k8s.ilt-dmz.iosb.fraunhofer.de/v1.1](https://airquality-frost.k8s.ilt-dmz.iosb.fraunhofer.de/v1.1)

For context, a viewer operating on this endpoint:
* [https://api4inspire.k8s.ilt-dmz.iosb.fraunhofer.de/servlet/is/127/](https://api4inspire.k8s.ilt-dmz.iosb.fraunhofer.de/servlet/is/127/)

## REST Cheat Sheet

The OGC SensorThings API is a RESTful API.
Here is a quick table that lists the major functions of the API:

|                             | GET               | POST          | PATCH  | DELETE |
|-----------------------------|-------------------|---------------|--------|--------|
| __v1.1__                    | Get index         |               |        |        |
| __v1.1/Type__               | Get all of type   | Create        |        |        |
| __v1.1/Type(id)__           | Get one of type   |               | Update | Delete |
| __v1.1/Type(id)/Entity__    | Get linked entity | Create Linked |        |        |
| __v1.1/Type(id)/EntitySet__ | Get all linked    |               |        |        |



