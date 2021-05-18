---
title: Polygon Things
layout: default
category: mappingTutorial
topCategory: STA
order: 1
---

# Polygon things

Locations and FeaturesOfInterest in a SensorThings service are not always points.
In our Demograph use case we have the statistical NUTS regions as things, with the polygon of the region as Location, in various differnent resolutions.

__Lets put those on a map!__

First, lets see what the data looks like in the service.

- NUTS Regions exist in four levels, level 0 to 3:
  - Level 0: Countries
  - Level 3: Counties

Each Thing in the service has various properties useful for filtering, like countryCode that has the two-letter country code for the NUTS region.
Lets have a look at the various regions for Germany.
The top-level NUTS region for Germany:

```
https://demography.k8s.ilt-dmz.iosb.fraunhofer.de/v1.1/Things?
  $filter=properties/countryCode eq 'DE' and properties/level eq 0&
  $count=true
```
Only one result. That's good, since there is only one Germany!

Going down one level, to level 1:
```
https://demography.k8s.ilt-dmz.iosb.fraunhofer.de/v1.1/Things?
  $filter=properties/countryCode eq 'DE' and properties/level eq 1&
  $count=true
```
gives 16 results, and the lower levels (2 and 3) give 38 and 401 results.

Lets go back to the top level, and expand the Locations:

```
https://demography.k8s.ilt-dmz.iosb.fraunhofer.de/v1.1/Things?
  $filter=properties/countryCode eq 'DE' and properties/level eq 0&
  $count=true&
  $expand=Locations
```

It has 5 Locations! Each with a different scale of the geometry: 1 to 60M, 20M, 10M, 3M and 1M.
When creating a map, we have to choose which one to use.
The 1 to 1M scale version is very big, so when displaying the entire world, it's not a good idea to load the biggest dataset.
Lets start with a map that displays the 60M version of NUTS level 0.
Once again, we start with the final map of our previous excercise and copy it to a new file ([2_PolygonThingsStart.html](2_PolygonThingsStart.html)).

We can remove the `plot` bit from the STAM configuration, update the baseUrl and turn of clustering:
```javascript
    L.stam({
        baseUrl: "https://demography.k8s.ilt-dmz.iosb.fraunhofer.de/v1.1",
        cluster: false,
        clusterMin: 10,
        queryObject: {
            entityType: 'Things'
        }
    }).addTo(mymap);
```
If we now open the map, it loads all 4 NUTS levels at the same time, and for each of them all 5 scales! That's not good, since that results in 98MB of data!

To make the map load only NUTS level 0, we can add a filter to the Things query object:
```javascript
        queryObject: {
            entityType: 'Things',
            filter: "properties/level eq 0"
        }
```

But STAM still loads all 5 Locations of each NUTS region, which is still way to much (19MB of data).
To stop that, we have to take control of how STAM loads the Locations.
By default, STAM will just load everything, and that is not what we want.

To to this, we manually `expand` the Things with their Location, filtered for `properties/scale eq 60`.
A QueryObject in STAM can have an `expand` property that is a list of QueryObjects.
In this case, we just need to add a QueryObject for the Locations:
```javascript
        queryObject: {
            entityType: 'Things',
            filter: "properties/level eq 0",
            expand: [
                {
                    entityType: 'Locations',
                    filter: "properties/scale eq 60"
                }
            ]
        }
```
Now STAM only loads the Location with scale=60 for each Thing.
The resulting data is a lot less, only 1.7MB now.
Stam also directly loads the Datastreams, and clicking on a region brings up the available statistics.
Clicking a Datastream directly shows the graph.
Since these Datastreams only contain 1 point per year, and only for  20 years, there is no need to further limit the graphs.

But, there are more NUTS levels.
It's now time to add the next level, when zooming in.
To do this, we change the queryObject from just an Object, into an Array of Objects:
```javascript
        queryObject: [
            {
                zoomLevel: {
                    from: 0,
                    to: 5
                },
                query: {
                    // Original query object
                }
            }
        ]
```
This lets us set the zoom-level for which the queryObject is valid.
The full queryObject then looks like:

```javascript
        queryObject: [
            {
                zoomLevel: {
                    from: 0,
                    to: 5
                },
                query: {
                    entityType: 'Things',
                    filter: "properties/level eq 0",
                    expand: [
                        {
                            entityType: 'Locations',
                            filter: "properties/scale eq 60"
                        }
                    ]
                }
            }
        ]
```

We can now add a second query for zoom level 6 + 7, which load the NUTS levels 1, with the Locations in scale 20:
```javascript
            {
                zoomLevel: {
                    from: 6,
                    to: 6
                },
                query: {
                    entityType: 'Things',
                    filter: "properties/level eq 1",
                    expand: [
                        {
                            entityType: 'Locations',
                            filter: "properties/scale eq 20"
                        }
                    ]
                }
            }
```
Don't forget the comma (`,`) at the end of the previous queryObject, or the JSON is not valid anymore.
Since the starting zoom level for our map is 5, zooming in once will show the next NUTS level, with the finer scale Geometries.

Clicking on these regions will show the data for these finer regions.

Now, to finalise it, we can add three more levels:
- Zoom Level 7 & 8, with Nuts level 2 and scale 10
- Zoom Level 9, with Nuts level 3 and scale 3
- Zoom Level 10+, with Nuts level 3 and scale 1

```javascript
            {
                zoomLevel: {
                    from: 7,
                    to: 8
                },
                query: {
                    entityType: 'Things',
                    filter: "properties/level eq 2",
                    expand: [
                        {
                            entityType: 'Locations',
                            filter: "properties/scale eq 10"
                        }
                    ]
                }
            },
            {
                zoomLevel: {
                    from: 9,
                    to: 9
                },
                query: {
                    entityType: 'Things',
                    filter: "properties/level eq 3",
                    expand: [
                        {
                            entityType: 'Locations',
                            filter: "properties/scale eq 3"
                        }
                    ]
                }
            },
            {
                zoomLevel: {
                    from: 10
                },
                query: {
                    entityType: 'Things',
                    filter: "properties/level eq 3",
                    expand: [
                        {
                            entityType: 'Locations',
                            filter: "properties/scale eq 1"
                        }
                    ]
                }
            }
```
The final map is now: [2_PolygonThingsFinal.html](2_PolygonThingsFinal.html)

In the next chapter, we make the opacity of the regions depend on the population density: [3_ColouringPolygons.html](3_ColouringPolygons.md).






