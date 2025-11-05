---
layout: default
title: Data Model DCAT
category: extensions
order: 1
---

# DCAT Data Model Plugin

Extension to define mappings of sensor data to a [DCAT-AP Specification 3.0](https://www.dcat-ap.de/def/dcatde/3.0/spec/) compliant description. 

## Description

A SensorThings API server, like the FROST-Server, is designed to provide datastreams for various application domains and aims to support the FAIR data principles for making research data Findable, Accessible, Interoperable, and Reusable. In order to make the content of a FROST-Server findable, this data model plugin provides the means to export some of the STA data, as a [DCAT-AP Specification 3.0](https://www.dcat-ap.de/def/dcatde/3.0/spec/)  compliant catalogue description. The catalogue will be provided as an harvestable file, containing the descriptions of the published data sets. 

## Data Model

The image below shows the data elements added by the DCAT Data Model. The __Dataset__ class is linked to the __Datastream__ class from the core STA data model.

```mermaid
classDiagram
    direction LR

    class DataService {
      id: Id
      title: string
      endpointURL: string
    }

    class Dataset {
      id: Id
      title: string
      description: string
      keywords: string
      temporalResolution: string
      spatialResolution: double
    }

    class Distribution {
      id: Id
      accessURL: string
      title: string
      description: string
      format: string
      availability: string
    }

    class License {
      id: Id
      definition: string
    }

    class Standard {
      id: Id
      definition: string
      title: string
    }

    class Agent {
      id: Id
      name: string
      email: string
      telephone: string
      countryName: string
      postalCode: string
      locality: string
      streetAddress: string
    }

    class Datastream {
      id: Id
    }

    %% Beziehungen
    DataService "*" --> "1" Agent : Publisher
    DataService "*" -- "*" Standard : ConformsTo
    DataService "*" --> "1" License : License
    DataService "*" -- "*" Dataset : Datasets

    Dataset "*" --> "1" Agent : Publisher
    Dataset "*" -- "*" Agent : Creators
    Dataset "*" -- "*" Standard : ConformsTo
    Dataset "*" -- "*" Agent : ContactPoint
    Dataset "*" -- "*" Datastream : Datastreams

    Distribution "*" -- "*" DataService : AccessServices
    Distribution "*" --> "1" License : License
    Distribution "*" -- "*" Standard : ConformsTo
    Distribution "*" --> "1" Dataset : Dataset
```


## Example Data

TODO: Illustrative example, like the DAKIMO datastream.


## Conformance Class

The DCAT-AP data model is compliant to the Specification 3.0:

    https://www.dcat-ap.de/def/dcatde/3.0/spec/

The conformance class this extension registers in the SensorThings (v1.1 and up) index document is:

    https://fraunhoferiosb.github.io/FROST-Server/extensions/DataModel-Projects.html

