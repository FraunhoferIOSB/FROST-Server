---
layout: default
title: GeoJSON ResultFormat
category: extensions
order: 2
---

# GeoJSON ResultFormat

Many viewers exist that can accept GeoJSON, but not SensorThings API responses.
To make it possible for these viewers to display data from a SensorThings API service, this extension describes a GeoJSON ResultFormat.

[https://github.com/opengeospatial/sensorthings/issues/70](https://github.com/opengeospatial/sensorthings/issues/70)

The GeoJSON result format can be requested by setting the request parameter `ResultFormat=GeoJSON`.


## GeoJSON response

The GeoJSON result format returns a FeatureCollection containing a feature for each Entity in the returned entity set.
When requesting a single entity, the result is a FeatureCollection, containing a single item.

The GeoJSON resultFormat takes each Entity of the query result and flattens it into the properties field of the GeoJSON Feature,
using the JSON path as the name of each property. This includes expanded Entities.

The geometry of each Feature is set to the first geometry that is found in the Feature's Entity or its expands.
This means that when querying Locations, the geometry is taken from the location field of each Location.
When querying Things and expanding Locations, the geometry is set to the location field of the first Location in the Locations of each Thing.


## Examples

A simple example: `/v1.1/Locations?$resultFormat=GeoJSON`

```json
{
	"type": "FeatureCollection",
	"features": [
	    {
	        "type": "Feature",
	        "properties": {
	            "id": 11,
	            "name": "Lustenau Wiesenrain",
	            "description": "Location of air quality station Lustenau Wiesenrain",
	            "encodingType": "application/geo+json",
	            "properties/namespace": "AT.0008.20.AQ",
	            "properties/localId": "STA.08.0706"
	        },
	        "geometry": {
	            "type": "Point",
	            "coordinates": [
	                9.653713,
	                47.410164
	            ]
	        }
	    },
	    {
	        "type": "Feature",
	        "properties": {
	            "id": 12,
	            "name": "Dornbirn Stadtstraße",
	            "description": "Location of air quality station Dornbirn Stadtstraße",
	            "encodingType": "application/geo+json",
	            "properties/namespace": "AT.0008.20.AQ",
	            "properties/localId": "STA.08.0807"
	        },
	        "geometry": {
	            "type": "Point",
	            "coordinates": [
	                9.743440833,
	                47.410032222
	            ]
	        }
	    }
	]
}
```

A more complex example:
```
v1.1/Datastreams?
	$select=name,unitOfMeasurement
	&$expand=
		Thing($select=name,properties;$expand=Locations($select=id,location))
		,ObservedProperty($select=name)
		,Observations($select=result,phenomenonTime;$orderby=phenomenonTime desc;$top=2)
	&$resultFormat=GeoJSON
	&$top=2
```

```json
{

    "type": "FeatureCollection",
    "features": [
        {
            "type": "Feature",
            "properties": {
                "name": "SPO-FR20019_8",
                "unitOfMeasurement/name": "ug/m3",
                "unitOfMeasurement/symbol": "ug/m3",
                "unitOfMeasurement/definition": "ug/m3",
                "Thing/name": "STA-FR20019",
                "Thing/properties/owner": "http://dd.eionet.europa.eu",
                "Thing/properties/AirQualityStationArea": "urban",
                "Thing/properties/metadata": "http://discomap.eea.europa.eu/map/fme/metadata/PanEuropean_metadata.csv",
                "Thing/properties/namespace": "FR.LCSQA-INERIS.AQ",
                "Thing/properties/mediaMonitored": "http://inspire.ec.europa.eu/codelist/MediaValue/air",
                "Thing/properties/beginTime": "1993-01-01T00:00:00",
                "Thing/properties/localId": "STA-FR20019",
                "Thing/Locations/0/id": 207,
                "ObservedProperty/name": "NO2",
                "Observations/0/result": 6,
                "Observations/0/phenomenonTime": "2020-03-30T13:00:00.000Z/2020-03-30T14:00:00.000Z",
                "Observations/1/result": 5.8,
                "Observations/1/phenomenonTime": "2020-03-30T12:00:00.000Z/2020-03-30T13:00:00.000Z"
            },
            "geometry": {
                "type": "Point",
                "coordinates": [
                    4.886214,
                    45.75836
                ]
            }
        },
        {
            "type": "Feature",
            "properties": {
                "name": "SPO.03.0302.1683.1.1",
                "unitOfMeasurement/name": "ug.m-3",
                "unitOfMeasurement/symbol": "ug.m-3",
                "unitOfMeasurement/definition": "http://dd.eionet.europa.eu/vocabulary/uom/concentration/ug.m-3",
                "Thing/name": "Stixneusiedl",
                "Thing/properties/owner": "http://luft.umweltbundesamt.at",
                "Thing/properties/metadata": "http://luft.umweltbundesamt.at/inspire/wfs?service=WFS&version=2.0.0&request=GetFeature&typeName=aqd:AQD_Station",
                "Thing/properties/namespace": "AT.0008.20.AQ",
                "Thing/properties/mobile": false,
                "Thing/properties/mediaMonitored": "http://inspire.ec.europa.eu/codelist/MediaValue/air",
                "Thing/properties/measurementRegime": "http://inspire.ec.europa.eu/codelist/MeasurementRegimeValue/continuousDataCollection",
                "Thing/properties/beginTime": "1989-04-10",
                "Thing/properties/localId": "STA.03.0302",
                "Thing/Locations/0/id": 154,
                "ObservedProperty/name": "SO2",
                "Observations/0/result": 3.0,
                "Observations/0/phenomenonTime": "2020-03-20T12:00:00.000Z/2020-03-20T13:00:00.000Z",
                "Observations/1/result": 3.0,
                "Observations/1/phenomenonTime": "2020-03-20T11:00:00.000Z/2020-03-20T12:00:00.000Z"
            },
            "geometry": {
                "type": "Point",
                "coordinates": [
                    16.675278,
                    48.050278
                ]
            }
        }
    ]

}
```


## Conformance Class

The conformance class this extension must register in the SensorThings (v1.1 and up) index document is:

    https://fraunhoferiosb.github.io/FROST-Server/extensions/GeoJSON-ResultFormat.md


