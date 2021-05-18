---
layout: default
title: CSV &amp; GeoJSON
category: gettingData
topCategory: STA
order: 7
---

# ResultFormats

The OGC SensorThings API specifies one additional parameter: `$resultFormat`.
This parameter can be used to request the server to return a different format than the standard JSON format.

## DataArray

The DataArray result format can format Observations in a more compact/efficient way.
It only works on collections of Observations.

The following example URL returns a the population density Observations for Denmark, in DataArray format:
```
https://demography.k8s.ilt-dmz.iosb.fraunhofer.de/v1.1/Datastreams(673)/Observations
	?$select=result,phenomenonTime
	&$resultFormat=DataArray
```


## GeoJSON

The GeoJSON result format changes the response of the server to be proper GeoJSON.
The details can be found at the [FROST-Server Documentation Site](https://fraunhoferiosb.github.io/FROST-Server/extensions/GeoJSON-ResultFormat.html).
It is a non-standard result format implemented by FROST-Server.

The following example URL returns a GeoJSON for Denmark, with all population density observations:
```
https://demography.k8s.ilt-dmz.iosb.fraunhofer.de/v1.1/Things(1952)
	?$expand=
		Locations(
			$filter=properties/scale%20eq%2060;
			$select=location),
		Datastreams(
			$filter=ObservedProperty/id eq 1;
			$select=name;
			$expand=Observations($select=phenomenonTime,result))
	&$resultFormat=GeoJSON
```


## CSV

The CSV result format changes the response of the server into [rfc4180](https://tools.ietf.org/html/rfc4180) conform CSV.
The details can be found at the [FROST-Server Documentation Site](https://fraunhoferiosb.github.io/FROST-Server/extensions/CSV-ResultFormat.html).
It is a non-standard result format implemented by FROST-Server.

The following example returns the Population Density for Denmark, as CSV:
```
https://demography.k8s.ilt-dmz.iosb.fraunhofer.de/v1.1/Datastreams(673)/Observations
	?$select=result,phenomenonTime
	&$expand=Datastream($select=id,name)
	&$resultFormat=CSV
```

```CSV
result,phenomenonTime,Datastream/id,Datastream/name
119.3,1990-01-01T00:00:00.000Z/1991-01-01T00:00:00.000Z,673,Population Density for Danmark
119.6,1991-01-01T00:00:00.000Z/1992-01-01T00:00:00.000Z,673,Population Density for Danmark
120.0,1992-01-01T00:00:00.000Z/1993-01-01T00:00:00.000Z,673,Population Density for Danmark
120.4,1993-01-01T00:00:00.000Z/1994-01-01T00:00:00.000Z,673,Population Density for Danmark
120.8,1994-01-01T00:00:00.000Z/1995-01-01T00:00:00.000Z,673,Population Density for Danmark
121.3,1995-01-01T00:00:00.000Z/1996-01-01T00:00:00.000Z,673,Population Density for Danmark
122.1,1996-01-01T00:00:00.000Z/1997-01-01T00:00:00.000Z,673,Population Density for Danmark
122.6,1997-01-01T00:00:00.000Z/1998-01-01T00:00:00.000Z,673,Population Density for Danmark
123.1,1998-01-01T00:00:00.000Z/1999-01-01T00:00:00.000Z,673,Population Density for Danmark
123.4,1999-01-01T00:00:00.000Z/2000-01-01T00:00:00.000Z,673,Population Density for Danmark
123.9,2000-01-01T00:00:00.000Z/2001-01-01T00:00:00.000Z,673,Population Density for Danmark
124.3,2001-01-01T00:00:00.000Z/2002-01-01T00:00:00.000Z,673,Population Density for Danmark
124.7,2002-01-01T00:00:00.000Z/2003-01-01T00:00:00.000Z,673,Population Density for Danmark
125.1,2003-01-01T00:00:00.000Z/2004-01-01T00:00:00.000Z,673,Population Density for Danmark
125.4,2004-01-01T00:00:00.000Z/2005-01-01T00:00:00.000Z,673,Population Density for Danmark
125.7,2005-01-01T00:00:00.000Z/2006-01-01T00:00:00.000Z,673,Population Density for Danmark
126.2,2006-01-01T00:00:00.000Z/2007-01-01T00:00:00.000Z,673,Population Density for Danmark
126.7,2007-01-01T00:00:00.000Z/2008-01-01T00:00:00.000Z,673,Population Density for Danmark
127.5,2008-01-01T00:00:00.000Z/2009-01-01T00:00:00.000Z,673,Population Density for Danmark
128.2,2009-01-01T00:00:00.000Z/2010-01-01T00:00:00.000Z,673,Population Density for Danmark
128.7,2010-01-01T00:00:00.000Z/2011-01-01T00:00:00.000Z,673,Population Density for Danmark
129.7,2011-01-01T00:00:00.000Z/2012-01-01T00:00:00.000Z,673,Population Density for Danmark
130.4,2012-01-01T00:00:00.000Z/2013-01-01T00:00:00.000Z,673,Population Density for Danmark
130.8,2013-01-01T00:00:00.000Z/2014-01-01T00:00:00.000Z,673,Population Density for Danmark
131.5,2014-01-01T00:00:00.000Z/2015-01-01T00:00:00.000Z,673,Population Density for Danmark
132.4,2015-01-01T00:00:00.000Z/2016-01-01T00:00:00.000Z,673,Population Density for Danmark
137.3,2017-01-01T00:00:00.000Z/2018-01-01T00:00:00.000Z,673,Population Density for Danmark
138.0,2018-01-01T00:00:00.000Z/2019-01-01T00:00:00.000Z,673,Population Density for Danmark
```

