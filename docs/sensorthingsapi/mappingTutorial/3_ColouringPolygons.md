---
title: Colouring Polygons
layout: default
category: mappingTutorial
topCategory: STA
order: 3
---

# Colouring Polygons based on Observations

In our previous excersize we made the map [3_ColouringPolygonsStart.html](3_ColouringPolygonsStart.html), with the different NUTS regions.
Now, lets colour them by Population Density.
To do that, we first need to get an idea about the distribution of the value.
Lets find the minimum and maximum values for the population density.

* Find the OP for density:
  ```
  https://demography.k8s.ilt-dmz.iosb.fraunhofer.de/v1.1/ObservedProperties
  ```
* Find all Observations that have Datastreams that have this OP
  ```
  https://demography.k8s.ilt-dmz.iosb.fraunhofer.de/v1.1/Observations?$count=true&
    $filter=Datastream/ObservedProperty/id eq 1
  ```
* Limit to just the last year
  ```
  https://demography.k8s.ilt-dmz.iosb.fraunhofer.de/v1.1/Observations?$count=true&
    $filter=Datastream/ObservedProperty/id eq 1 and phenomenonTime ge 2018-01-01T00:00:00.000Z
  ```
* Order by result
  ```
  https://demography.k8s.ilt-dmz.iosb.fraunhofer.de/v1.1/Observations?$count=true&
    $filter=Datastream/ObservedProperty/id eq 1 and phenomenonTime ge 2018-01-01T00:00:00.000Z&
    $orderby=result asc
  ```
* Only select the result
  ```
  https://demography.k8s.ilt-dmz.iosb.fraunhofer.de/v1.1/Observations?$count=true&
    $filter=Datastream/ObservedProperty/id eq 1 and phenomenonTime ge 2018-01-01T00:00:00.000Z&
    $orderby=result asc&
    $select=reult&
    $top=1000
  ```
* Minimum: 1.3,  
  Maximum: 21043.6  
  That's a really large range...

We can start by setting the fillOpacity to result/1000, that should give us some interesting visuals.

To get this data into our map, we first extend the query so that we get the data we need directly with the Thing:
* Datastreams
  * ObseredProperty
  * Last Observation

To do this, we have to modify the `epand` section of the STAM map configuration.
At first we only do this for the first zoom level, once we're happy with how it works, we can do it for the rest.

```
    {
        count: false,
        entityType: 'Datastreams',
        select: ["id","name","unitOfMeasurement"],
        expand: [
            {
                count: false,
                entityType: 'Observations',
                select: ["result"],
                orderby: "phenomenonTime desc"
            },
            {
                entityType: 'ObservedProperty',
                select: ["name","id"]
            }
        ]
    }
```

Now we can tell the map to set the opacity of the polygon to the result:
```
    polygonStyle: function (thing) {
        let datastream;
        let dsList = thing.properties.Datastreams;
        for (let i = 0;i < dsList.length;i++) {
            let ds = dsList[i];
            if (ds.ObservedProperty["@iot.id"] === 1) {
                datastream = ds;
                break;
            }
        }
        if (datastream === undefined) {
            return {color: "#FF0000"};
        }
        if (datastream.Observations === undefined) {
            return {color: "#FFFF00"};
        }
        let result = datastream.Observations[0].result;
        let opacity = result / 1000;
        return {color: "#0000FF", fillOpacity: opacity};
    },
```
Once we're certain it works correctly, we can add the `expand` to the other zoom levels.

Getting the latest result in the popup would be useful too, especially since we already load the data!
To do that, we need to make our own popup.

```
markerClick: function (thing) {
    var div = document.createElement('div');
    div.innerHTML = '<h3>' + thing.properties.name + '</h3>';
    var list = document.createElement('ul');
    let dsList = thing.properties.Datastreams;
    for (let i = 0;i < dsList.length;i++) {
        let ds = dsList[i];
        var li = document.createElement('li');
        li.innerHTML = ds.ObservedProperty.name + ":<br><b>" + ds.Observations[0].result + "</b> " + ds.unitOfMeasurement.symbol;
        list.appendChild(li);
    }
    div.appendChild(list);
    return div;
},
```

The final version of our page is now: [3_ColouringPolygonsFinal.html](3_ColouringPolygonsFinal.html)


