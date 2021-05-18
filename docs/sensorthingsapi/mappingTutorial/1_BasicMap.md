---
title: Basic Mapping
layout: default
category: mappingTutorial
topCategory: STA
order: 1
---

# Creating a basic map

To set up a HTML page with a map in it, we first have to start with an empty HTML page:

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">

    <script src="https://cdn.plot.ly/plotly-latest.min.js"></script>

    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.6.0/dist/leaflet.css"
        integrity="sha512-xwE/Az9zrjBIphAcBb3F6JVqxf46+CDLwfLMHloNu6KEQCAWi6HcDUbeOfBIptF7tcCzusKFjFw2yuvEpDL9wQ=="
        crossorigin="" rel="stylesheet" />
    <script src="https://unpkg.com/leaflet@1.6.0/dist/leaflet.js"
        integrity="sha512-gZwIG9x3wUXg2hdXF6+rVkLF/0Vi9U8D2Ntg4Ga5I5BZpVkVxlJWbSQtXPSiUTtC0TjtGOmxa1AJPuV0CPthew=="
        crossorigin=""></script>

    <script src="https://api.mapbox.com/mapbox.js/plugins/leaflet-fullscreen/v1.0.1/Leaflet.fullscreen.min.js"></script>
    <link href="https://api.mapbox.com/mapbox.js/plugins/leaflet-fullscreen/v1.0.1/leaflet.fullscreen.css" rel="stylesheet" />

    <script src="https://unpkg.com/sta-map"></script>

    <style>
        html, body {height:100%;}
        #mymap {
            height: 85%;
            width: 100%;
            background-color: #CCC;
        }
    </style>

    <title>Demo Map</title>
</head>
<body>

<h1>Demo Map</h1>

<div id="mymap"></div>

<script type="text/javascript">
</script>

</body>
</html>
```
[0_BasePage.html](0_BasePage.html)

This page has all the elements needed to show a map, but it does not actually initialise the map yet.
Instead it only shows a gray placeholder where the map will be.

To activate the map, we have to add a bit of JavaScript in the `<script>` tag near the end of the file:

```javascript
    var mymap = L.map('mymap').setView([49.0, 8.4], 5);
    mymap.addControl(new L.Control.Fullscreen());
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors',
        maxZoom: 18
    }).addTo(mymap);
```

This creates the map, and adds a OpenStreetMap background layer to it.

Next, we add a STAM layer that shows data from the AirQuality use case:

```javascript
    L.stam({
        baseUrl: "https://airquality-frost.k8s.ilt-dmz.iosb.fraunhofer.de/v1.1",
        cluster: true,
        clusterMin: 10,
        queryObject: {
            entityType: 'Things'
        }
    }).addTo(mymap);
```

Clicking on a sensor, and opening a Datastream takes a long time, because STAM is loading almost three years of hourly data.
Lets change that so it only loads the last month by using the plot section of the STAM configuration and adding a startDate.
Don't forget to add the `,` after the previous object in the JSON!
```javascript
        plot: {
            startDate: new Date(Date.now() - 1000*60*60*24*30),
            endDate: new Date()
        }
```

Your map should now look like [1_BasicMapFinal.html](1_BasicMapFinal.html) and clicking a Datastream in a Station should give the last month of data.






