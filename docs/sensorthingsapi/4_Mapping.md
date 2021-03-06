---
layout: default
title: Mapping
category: STA
childCategory: mappingTutorial
order: 4
---

# Mapping

In this section we create maps!
Maps are an important way to display Observational data.

<div id="mymap" style="height: 800px; width: 100%;"></div>
<p>Data from <a href="https://ec.europa.eu/eurostat/de/web/gisco/geodata/reference-data/administrative-units-statistical-units/nuts">Eurostat</a>.</p>

<script src="https://cdn.plot.ly/plotly-latest.min.js"></script>
<link rel="stylesheet" href="https://unpkg.com/leaflet@1.6.0/dist/leaflet.css"
    integrity="sha512-xwE/Az9zrjBIphAcBb3F6JVqxf46+CDLwfLMHloNu6KEQCAWi6HcDUbeOfBIptF7tcCzusKFjFw2yuvEpDL9wQ=="
    crossorigin="" />
<script src="https://unpkg.com/leaflet@1.6.0/dist/leaflet.js"
    integrity="sha512-gZwIG9x3wUXg2hdXF6+rVkLF/0Vi9U8D2Ntg4Ga5I5BZpVkVxlJWbSQtXPSiUTtC0TjtGOmxa1AJPuV0CPthew=="
    crossorigin=""></script>
<script src="https://api.mapbox.com/mapbox.js/plugins/leaflet-fullscreen/v1.0.1/Leaflet.fullscreen.min.js"></script>
<link href="https://api.mapbox.com/mapbox.js/plugins/leaflet-fullscreen/v1.0.1/leaflet.fullscreen.css" rel="stylesheet" />
<script src="https://unpkg.com/sta-map"></script>
<script type="text/javascript">
	let initMap = function() {
		var mymap = L.map('mymap').setView([51.505, 8.0], 4);
		mymap.addControl(new L.Control.Fullscreen());
		L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
		    attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors',
		    maxZoom: 18
		}).addTo(mymap);
		L.stam({
		    baseUrl: "https://demography.k8s.ilt-dmz.iosb.fraunhofer.de/v1.1",
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
		    clusterMin: 50,
		    queryObject: [
		        {
		            zoomLevel: {
		                from: 0,
		                to: 5
		            },
		            query: {
		                count: false,
		                skip: 0,
		                entityType: 'Things',
		                filter: "properties/type eq 'NUTS' and properties/level eq 0",
		                select: ["id","name","description","properties"],
		                expand: [
		                    {
		                        count: false,
		                        entityType: 'Locations',
		                        filter: "properties/scale eq 60",
		                        select: ["id","name","description","properties","encodingType","location"],
		                        expand: null,
		                        top: 1
		                    },
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
		                ]
		            }
		        },
		        {
		            zoomLevel: {
		                from: 6,
		                to: 6
		            },
		            query: {
		                count: false,
		                skip: 0,
		                entityType: 'Things',
		                filter: "properties/type eq 'NUTS' and properties/level eq 1",
		                select: ["id","name","description","properties"],
		                expand: [
		                    {
		                        count: false,
		                        skip: 0,
		                        entityType: 'Locations',
		                        filter: "properties/scale eq 20",
		                        select: ["id","name","description","properties","encodingType","location"],
		                        expand: null,
		                        top: 1
		                    },
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
		                ]
		            }
		        },
		        {
		            zoomLevel: {
		                from: 7,
		                to: 8
		            },
		            query: {
		                count: false,
		                skip: 0,
		                entityType: 'Things',
		                filter: "properties/type eq 'NUTS' and properties/level eq 2",
		                select: ["id","name","description","properties"],
		                expand: [
		                    {
		                        count: false,
		                        skip: 0,
		                        entityType: 'Locations',
		                        filter: "properties/scale eq 10",
		                        select: ["id","name","description","properties","encodingType","location"],
		                        expand: null,
		                        top: 1
		                    },
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
		                ]
		            }
		        },
		        {
		            zoomLevel: {
		                from: 9,
		                to: 10
		            },
		            query: {
		                count: false,
		                skip: 0,
		                entityType: 'Things',
		                filter: "properties/type eq 'NUTS' and properties/level eq 3",
		                select: ["id","name","description","properties"],
		                expand: [
		                    {
		                        count: false,
		                        skip: 0,
		                        entityType: 'Locations',
		                        filter: "properties/scale eq 3",
		                        select: ["id","name","description","properties","encodingType","location"],
		                        expand: null,
		                        top: 1
		                    },
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
		                ]
		            }
		        },
		        {
		            zoomLevel: {
		                from: 11,
		                to: 13
		            },
		            query: {
		                count: false,
		                skip: 0,
		                entityType: 'Things',
		                filter: "properties/type eq 'NUTS' and properties/level eq 3",
		                select: ["id","name","description","properties"],
		                expand: [
		                    {
		                        count: false,
		                        skip: 0,
		                        entityType: 'Locations',
		                        filter: "properties/scale eq 1",
		                        select: ["id","name","description","properties","encodingType","location"],
		                        expand: null,
		                        top: 1
		                    },
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
		                ]
		            }
		        }
		    ]
		}).addTo(mymap);
	}
	function ready(fn) {
		if (document.readyState != 'loading') {
			fn();
		} else {
			document.addEventListener('DOMContentLoaded', fn);
		}
	}
	ready(initMap);
</script>
