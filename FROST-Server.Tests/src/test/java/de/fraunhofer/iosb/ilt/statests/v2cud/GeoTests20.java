/*
 * Copyright (C) 2024 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
 * Karlsruhe, Germany.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.statests.v2cud;

import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing.EP_VALIDTIME;
import static de.fraunhofer.iosb.ilt.statests.util.EntityUtils.testFilterResults;
import static de.fraunhofer.iosb.ilt.statests.util.Utils.getFromList;

import de.fraunhofer.iosb.ilt.frostclient.SensorThingsService;
import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.json.SimpleJsonMapper;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV20Core;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostclient.models.swecommon.util.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods.HttpResponse;
import de.fraunhofer.iosb.ilt.statests.util.Utils;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.geojson.Feature;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.geojson.Polygon;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;

/**
 * Tests for the geospatial functions.
 */
public class GeoTests20 extends AbstractTestClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoTests20.class);

    private static final List<Entity> DATASTREAMS = new ArrayList<>();
    private static final List<Entity> FEATURES = new ArrayList<>();
    private static final List<Entity> LOCATIONS = new ArrayList<>();
    private static final List<Entity> OBSERVATIONS = new ArrayList<>();
    private static final List<Entity> O_PROPS = new ArrayList<>();
    private static final List<Entity> SENSORS = new ArrayList<>();
    private static final List<Entity> THINGS = new ArrayList<>();
    private static SensorThingsV20Core sMdl;

    private static final Map<String, String> SERVER_PROPERTIES = new LinkedHashMap<>();

    static {
        SERVER_PROPERTIES.put("plugins.modelLoader.enable", "true");
        SERVER_PROPERTIES.put("plugins.coreModel.enable", "false");
        SERVER_PROPERTIES.put("plugins.coreModel.idType", "LONG");
        SERVER_PROPERTIES.put("plugins.coreService.enable", "true");
        SERVER_PROPERTIES.put("plugins.coreModelV2.enable", "true");
    }

    public GeoTests20() {
        super(ServerVersion.V_2_0, SERVER_PROPERTIES);
    }

    @Override
    protected void setUpVersion() throws ServiceFailureException {
        LOGGER.info("Setting up for version {}.", version.urlPart);
        sMdl = sSrvc.getModel(SensorThingsV20Core.class);
        createEntities();
    }

    @Override
    protected SensorThingsService createService() throws MalformedURLException, URISyntaxException {
        return new SensorThingsService(new SensorThingsV20Core())
                .setBaseUrl(new URI(serverSettings.getServiceUrl(version)).toURL())
                .init();
    }

    @AfterAll
    static void tearDown() throws ServiceFailureException {
        LOGGER.info("Tearing down.");
        cleanup();
    }

    private static void cleanup() throws ServiceFailureException {
        EntityUtils.deleteAll(sSrvc);
        THINGS.clear();
        FEATURES.clear();
        LOCATIONS.clear();
        SENSORS.clear();
        O_PROPS.clear();
        DATASTREAMS.clear();
        OBSERVATIONS.clear();
    }

    private static void createEntities() throws ServiceFailureException {
        createThings();
        createSensor();
        createObsProp();
        createDatastreams();
        createLocation0();
        createLocation1();
        createLocation2();
        createLocation3();
        createLocation4();
        createLocation5();
        createLocation6();
        createLocation7();
    }

    private static void createThings() throws ServiceFailureException {
        Entity thing = sMdl.newThing("Thing 1", "The first thing.");
        sSrvc.create(thing);
        THINGS.add(thing);

        thing = sMdl.newThing("Thing 2", "The second thing.");
        sSrvc.create(thing);
        THINGS.add(thing);

        thing = sMdl.newThing("Thing 3", "The third thing.");
        sSrvc.create(thing);
        THINGS.add(thing);

        thing = sMdl.newThing("Thing 4", "The fourt thing.");
        sSrvc.create(thing);
        THINGS.add(thing);
    }

    private static void createSensor() throws ServiceFailureException {
        Entity sensor = sMdl.newSensor("Sensor 1", "The first sensor.", "text", "Some metadata.");
        sSrvc.create(sensor);
        SENSORS.add(sensor);
    }

    private static void createObsProp() throws ServiceFailureException {
        Entity obsProp = sMdl.newObservedProperty("Temperature", "http://ucom.org/temperature", "The temperature of the thing.");
        sSrvc.create(obsProp);
        O_PROPS.add(obsProp);
    }

    private static void createDatastreams() throws ServiceFailureException {
        Entity datastream = sMdl.newDatastream("Datastream 1", "The temperature of thing 1, sensor 1.",
                O_PROPS.get(0).getSelfLink(false),
                new UnitOfMeasurement()
                        .setLabel("degree celcius")
                        .setSymbol("°C")
                        .setCode("ucum:T"));
        datastream.setProperty(sMdl.npDatastreamThing, THINGS.get(0).asReference());
        datastream.setProperty(sMdl.npDatastreamSensor, SENSORS.get(0).asReference());
        sSrvc.create(datastream);
        DATASTREAMS.add(datastream);

        datastream = sMdl.newDatastream("Datastream 2", "The temperature of thing 2, sensor 1.",
                O_PROPS.get(0).getSelfLink(false),
                new UnitOfMeasurement()
                        .setLabel("degree celcius")
                        .setSymbol("°C")
                        .setCode("ucum:T"));
        datastream.setProperty(sMdl.npDatastreamThing, THINGS.get(1).asReference());
        datastream.setProperty(sMdl.npDatastreamSensor, SENSORS.get(0).asReference());
        sSrvc.create(datastream);
        DATASTREAMS.add(datastream);

        datastream = sMdl.newDatastream("Datastream 3", "The temperature of thing 3, sensor 1.",
                O_PROPS.get(0).getSelfLink(false),
                new UnitOfMeasurement()
                        .setLabel("degree celcius")
                        .setSymbol("°C")
                        .setCode("ucum:T"));
        datastream.setProperty(sMdl.npDatastreamThing, THINGS.get(2).asReference());
        datastream.setProperty(sMdl.npDatastreamSensor, SENSORS.get(0).asReference());
        sSrvc.create(datastream);
        DATASTREAMS.add(datastream);

        datastream = sMdl.newDatastream("Datastream 4", "The temperature of thing 4, sensor 1.",
                O_PROPS.get(0).getSelfLink(false),
                new UnitOfMeasurement()
                        .setLabel("degree celcius")
                        .setSymbol("°C")
                        .setCode("ucum:T"));
        datastream.setProperty(sMdl.npDatastreamThing, THINGS.get(3).asReference());
        datastream.setProperty(sMdl.npDatastreamSensor, SENSORS.get(0).asReference());
        datastream.addNavigationEntity(sMdl.npDatastreamObservedproperties, O_PROPS.get(0).asReference());
        sSrvc.create(datastream);
        DATASTREAMS.add(datastream);
    }

    private static void createLocation0() throws ServiceFailureException {
        // Locations 0
        Point gjo = new Point(8, 51);
        Entity location = sMdl.newLocation("Location 1.0", "First Location of Thing 1.", "application/vnd.geo+json", gjo);
        location.addNavigationEntity(sMdl.npLocationThings, THINGS.get(0).asReference());
        sSrvc.create(location);
        LOCATIONS.add(location);

        Entity featureOfInterest = sMdl.newFeature("FoI 0", "This should be FoI #0.", "application/geo+json", gjo);
        sSrvc.create(featureOfInterest);
        FEATURES.add(featureOfInterest);

        Entity o = sMdl.newObservation(1, ZonedDateTime.parse("2016-01-01T01:01:01.000Z"), DATASTREAMS.get(0).asReference())
                .setProperty(sMdl.npObservationProximateFoi, featureOfInterest)
                .setProperty(EP_VALIDTIME, TimeInterval.create(Instant.parse("2016-01-01T01:01:01.000Z"), Instant.parse("2016-01-01T23:59:59.999Z")));
        sSrvc.create(o);
        OBSERVATIONS.add(o);
    }

    private static void createLocation1() throws ServiceFailureException {
        // Locations 1
        Point gjo = new Point(8, 52);
        Entity location = sMdl.newLocation("Location 1.1", "Second Entity of Thing 1.", "application/vnd.geo+json", gjo);
        location.addNavigationEntity(sMdl.npLocationThings, THINGS.get(0).asReference());
        sSrvc.create(location);
        LOCATIONS.add(location);

        Entity featureOfInterest = sMdl.newFeature("FoI 1", "This should be FoI #1.", "application/geo+json", gjo);
        sSrvc.create(featureOfInterest);
        FEATURES.add(featureOfInterest);

        Entity o = sMdl.newObservation(2, ZonedDateTime.parse("2016-01-02T01:01:01.000Z"), DATASTREAMS.get(0).asReference())
                .setProperty(sMdl.npObservationProximateFoi, featureOfInterest)
                .setProperty(EP_VALIDTIME, TimeInterval.create(Instant.parse("2016-01-02T01:01:01.000Z"), Instant.parse("2016-01-02T23:59:59.999Z")));
        sSrvc.create(o);
        OBSERVATIONS.add(o);
    }

    private static void createLocation2() throws ServiceFailureException {
        // Locations 2
        Point gjo = new Point(8, 53);
        Entity location = sMdl.newLocation("Location 2", "Location of Thing 2.", "application/vnd.geo+json", gjo);
        location.addNavigationEntity(sMdl.npLocationThings, THINGS.get(1).asReference());
        sSrvc.create(location);
        LOCATIONS.add(location);

        Entity featureOfInterest = sMdl.newFeature("FoI 2", "This should be FoI #2.", "application/geo+json", gjo);
        sSrvc.create(featureOfInterest);
        FEATURES.add(featureOfInterest);

        Entity o = sMdl.newObservation(3, ZonedDateTime.parse("2016-01-03T01:01:01.000Z"), DATASTREAMS.get(1).asReference())
                .setProperty(sMdl.npObservationProximateFoi, featureOfInterest)
                .setProperty(EP_VALIDTIME, TimeInterval.create(Instant.parse("2016-01-03T01:01:01.000Z"), Instant.parse("2016-01-03T23:59:59.999Z")));
        sSrvc.create(o);
        OBSERVATIONS.add(o);
    }

    private static void createLocation3() throws ServiceFailureException {
        // Locations 3
        Point point = new Point(8, 54);
        Feature gjo = new Feature();
        gjo.setGeometry(point);
        Entity location = sMdl.newLocation("Location 3", "Location of Thing 3.", "application/geo+json", gjo);
        location.addNavigationEntity(sMdl.npLocationThings, THINGS.get(2).asReference());
        sSrvc.create(location);
        LOCATIONS.add(location);

        Entity featureOfInterest = sMdl.newFeature("FoI 3", "This should be FoI #3.", "application/geo+json", gjo);
        sSrvc.create(featureOfInterest);
        FEATURES.add(featureOfInterest);

        Entity o = sMdl.newObservation(4, ZonedDateTime.parse("2016-01-04T01:01:01.000Z"), DATASTREAMS.get(2).asReference())
                .setProperty(sMdl.npObservationProximateFoi, featureOfInterest)
                .setProperty(EP_VALIDTIME, TimeInterval.create(Instant.parse("2016-01-04T01:01:01.000Z"), Instant.parse("2016-01-04T23:59:59.999Z")));
        sSrvc.create(o);
        OBSERVATIONS.add(o);
    }

    private static void createLocation4() throws ServiceFailureException {
        // Locations 4
        Polygon gjo = new Polygon(
                new LngLatAlt(8, 53),
                new LngLatAlt(7, 52),
                new LngLatAlt(7, 53),
                new LngLatAlt(8, 53));
        Entity location = sMdl.newLocation("Location 4", "Location of Thing 4.", "application/vnd.geo+json", gjo);
        location.addNavigationEntity(sMdl.npLocationThings, THINGS.get(3).asReference());
        sSrvc.create(location);
        LOCATIONS.add(location);

        Entity featureOfInterest = sMdl.newFeature("FoI 4", "This should be FoI #4.", "application/geo+json", gjo);
        sSrvc.create(featureOfInterest);
        FEATURES.add(featureOfInterest);

        Entity o = sMdl.newObservation(4, ZonedDateTime.parse("2016-01-04T01:01:01.000Z"), DATASTREAMS.get(3).asReference())
                .setProperty(sMdl.npObservationProximateFoi, featureOfInterest)
                .setProperty(EP_VALIDTIME, TimeInterval.create(Instant.parse("2016-01-04T01:01:01.000Z"), Instant.parse("2016-01-04T23:59:59.999Z")));
        sSrvc.create(o);
        OBSERVATIONS.add(o);
    }

    private static void createLocation5() throws ServiceFailureException {
        // Locations 5
        LineString gjo = new LineString(
                new LngLatAlt(5, 52),
                new LngLatAlt(5, 53));
        Entity location = sMdl.newLocation("Location 5", "A line.", "application/vnd.geo+json", gjo);
        sSrvc.create(location);
        LOCATIONS.add(location);

        Entity featureOfInterest = sMdl.newFeature("FoI 5", "This should be FoI #5.", "application/geo+json", gjo);
        sSrvc.create(featureOfInterest);
        FEATURES.add(featureOfInterest);
    }

    private static void createLocation6() throws ServiceFailureException {
        // Locations 6
        LineString gjo = new LineString(
                new LngLatAlt(5, 52),
                new LngLatAlt(6, 53));
        Entity location = sMdl.newLocation("Location 6", "A longer line.", "application/vnd.geo+json", gjo);
        sSrvc.create(location);
        LOCATIONS.add(location);

        Entity featureOfInterest = sMdl.newFeature("FoI 6", "This should be FoI #6.", "application/geo+json", gjo);
        sSrvc.create(featureOfInterest);
        FEATURES.add(featureOfInterest);
    }

    private static void createLocation7() throws ServiceFailureException {
        // Locations 7
        LineString gjo = new LineString(
                new LngLatAlt(4, 52),
                new LngLatAlt(8, 52));
        Entity location = sMdl.newLocation("Location 7", "The longest line.", "application/vnd.geo+json",
                gjo);
        sSrvc.create(location);
        LOCATIONS.add(location);

        Entity featureOfInterest = sMdl.newFeature("FoI 7", "This should be FoI #7.", "application/geo+json", gjo);
        sSrvc.create(featureOfInterest);
        FEATURES.add(featureOfInterest);
    }

    /**
     * Test the geo.distance filter function.
     *
     * @throws ServiceFailureException If the sSrvc doesn't respond.
     */
    @Test
    void testGeoDistance() {
        LOGGER.info("  testGeoDistance");
        testFilterResults(sSrvc.dao(sMdl.etLocation), "geo.distance(location, geography'POINT(8 54.1)') lt 1", getFromList(LOCATIONS, 3));
        testFilterResults(sSrvc.dao(sMdl.etLocation), "geo.distance(location, geography'POINT(8 54.1)') gt 1", getFromList(LOCATIONS, 0, 1, 2, 4, 5, 6, 7));
        testFilterResults(sSrvc.dao(sMdl.etObservation), "geo.distance(ProximateFeatureOfInterest/feature, geography'POINT(8 54.1)') lt 1", getFromList(OBSERVATIONS, 3));
        testFilterResults(sSrvc.dao(sMdl.etObservation), "geo.distance(ProximateFeatureOfInterest/feature, geography'POINT(8 54.1)') gt 1", getFromList(OBSERVATIONS, 0, 1, 2, 4));
        testFilterResults(sSrvc.dao(sMdl.etLocation), "geo.distance(location, geography'SRID=4326;POINT(8 54.1)') lt 1", getFromList(LOCATIONS, 3));
    }

    /**
     * Test the geo.intersects filter function.
     *
     * @throws ServiceFailureException If the sSrvc doesn't respond.
     */
    @Test
    void testGeoIntersects() {
        LOGGER.info("  testGeoIntersects");
        testFilterResults(sSrvc.dao(sMdl.etLocation), "geo.intersects(location, geography'LINESTRING(7.5 51, 7.5 54)')", getFromList(LOCATIONS, 4, 7));
        testFilterResults(sSrvc.dao(sMdl.etFeature), "geo.intersects(feature, geography'LINESTRING(7.5 51, 7.5 54)')", getFromList(FEATURES, 4, 7));
        testFilterResults(sSrvc.dao(sMdl.etDatastream),
                "geo.intersects(observedArea, geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))')",
                getFromList(DATASTREAMS, 0, 1, 3));
        testFilterResults(sSrvc.dao(sMdl.etLocation), "geo.intersects(location, geography'SRID=4326;LINESTRING(7.5 51, 7.5 54)')", getFromList(LOCATIONS, 4, 7));
        testFilterResults(sSrvc.dao(sMdl.etDatastream),
                "geo.intersects(observedArea, geography'SRID=4326;POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))')",
                getFromList(DATASTREAMS, 0, 1, 3));
    }

    /**
     * Test the geo.length filter function.
     *
     * @throws ServiceFailureException If the sSrvc doesn't respond.
     */
    @Test
    void testGeoLength() {
        LOGGER.info("  testGeoLength");
        testFilterResults(sSrvc.dao(sMdl.etLocation), "geo.length(location) gt 1", getFromList(LOCATIONS, 6, 7));
        testFilterResults(sSrvc.dao(sMdl.etLocation), "geo.length(location) ge 1", getFromList(LOCATIONS, 5, 6, 7));
        testFilterResults(sSrvc.dao(sMdl.etLocation), "geo.length(location) eq 1", getFromList(LOCATIONS, 5));
        testFilterResults(sSrvc.dao(sMdl.etLocation), "geo.length(location) ne 1", getFromList(LOCATIONS, 0, 1, 2, 3, 4, 6, 7));
        testFilterResults(sSrvc.dao(sMdl.etLocation), "geo.length(location) le 4", getFromList(LOCATIONS, 0, 1, 2, 3, 4, 5, 6, 7));
        testFilterResults(sSrvc.dao(sMdl.etLocation), "geo.length(location) lt 4", getFromList(LOCATIONS, 0, 1, 2, 3, 4, 5, 6));
        testFilterResults(sSrvc.dao(sMdl.etFeature), "geo.length(feature) gt 1", getFromList(FEATURES, 6, 7));
        testFilterResults(sSrvc.dao(sMdl.etFeature), "geo.length(feature) ge 1", getFromList(FEATURES, 5, 6, 7));
        testFilterResults(sSrvc.dao(sMdl.etFeature), "geo.length(feature) eq 1", getFromList(FEATURES, 5));
        testFilterResults(sSrvc.dao(sMdl.etFeature), "geo.length(feature) ne 1", getFromList(FEATURES, 0, 1, 2, 3, 4, 6, 7));
        testFilterResults(sSrvc.dao(sMdl.etFeature), "geo.length(feature) le 4", getFromList(FEATURES, 0, 1, 2, 3, 4, 5, 6, 7));
        testFilterResults(sSrvc.dao(sMdl.etFeature), "geo.length(feature) lt 4", getFromList(FEATURES, 0, 1, 2, 3, 4, 5, 6));
    }

    /**
     * Test the st_contains filter function.
     *
     * @throws ServiceFailureException If the sSrvc doesn't respond.
     */
    @Test
    void testStContains() {
        LOGGER.info("  testStContains");
        testFilterResults(sSrvc.dao(sMdl.etLocation),
                "st_contains(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', location)",
                getFromList(LOCATIONS, 1, 2));
        testFilterResults(sSrvc.dao(sMdl.etObservation),
                "st_contains(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', ProximateFeatureOfInterest/feature)",
                getFromList(OBSERVATIONS, 1, 2));
        testFilterResults(sSrvc.dao(sMdl.etDatastream),
                "st_contains(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', observedArea)",
                getFromList(DATASTREAMS, 1));
    }

    /**
     * Test the st_crosses filter function.
     *
     * @throws ServiceFailureException If the sSrvc doesn't respond.
     */
    @Test
    void testStCrosses() {
        LOGGER.info("  testStCrosses");
        testFilterResults(sSrvc.dao(sMdl.etLocation), "st_crosses(geography'LINESTRING(7.5 51.5, 7.5 53.5)', location)", getFromList(LOCATIONS, 4, 7));
        testFilterResults(sSrvc.dao(sMdl.etFeature), "st_crosses(geography'LINESTRING(7.5 51.5, 7.5 53.5)', feature)", getFromList(FEATURES, 4, 7));
    }

    /**
     * Test the st_disjoint filter function.
     *
     * @throws ServiceFailureException If the sSrvc doesn't respond.
     */
    @Test
    void testStDisjoint() {
        LOGGER.info("  testStDisjoint");
        testFilterResults(sSrvc.dao(sMdl.etLocation),
                "st_disjoint(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', location)",
                getFromList(LOCATIONS, 0, 3, 5, 6));
        testFilterResults(sSrvc.dao(sMdl.etFeature),
                "st_disjoint(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', feature)",
                getFromList(FEATURES, 0, 3, 5, 6));
    }

    /**
     * Test the st_equals filter function.
     *
     * @throws ServiceFailureException If the sSrvc doesn't respond.
     */
    @Test
    void testStEquals() {
        LOGGER.info("  testStEquals");
        testFilterResults(sSrvc.dao(sMdl.etLocation), "st_equals(location, geography'POINT(8 53)')", getFromList(LOCATIONS, 2));
        testFilterResults(sSrvc.dao(sMdl.etFeature), "st_equals(feature, geography'POINT(8 53)')", getFromList(FEATURES, 2));
    }

    /**
     * Test the st_intersects filter function.
     *
     * @throws ServiceFailureException If the sSrvc doesn't respond.
     */
    @Test
    void testStIntersects() {
        LOGGER.info("  testStIntersects");
        testFilterResults(sSrvc.dao(sMdl.etLocation), "st_intersects(location, geography'LINESTRING(7.5 51, 7.5 54)')", getFromList(LOCATIONS, 4, 7));
        testFilterResults(sSrvc.dao(sMdl.etFeature), "st_intersects(feature, geography'LINESTRING(7.5 51, 7.5 54)')", getFromList(FEATURES, 4, 7));
        testFilterResults(sSrvc.dao(sMdl.etDatastream),
                "st_intersects(observedArea, geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))')",
                getFromList(DATASTREAMS, 0, 1, 3));
    }

    /**
     * Test the st_overlaps filter function.
     *
     * @throws ServiceFailureException If the sSrvc doesn't respond.
     */
    @Test
    void testStOverlaps() {
        LOGGER.info("  testStOverlaps");
        testFilterResults(sSrvc.dao(sMdl.etLocation),
                "st_overlaps(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', location)",
                getFromList(LOCATIONS, 4));
        testFilterResults(sSrvc.dao(sMdl.etFeature),
                "st_overlaps(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', feature)",
                getFromList(FEATURES, 4));
    }

    /**
     * Test the st_relate filter function.
     *
     * @throws ServiceFailureException If the sSrvc doesn't respond.
     */
    @Test
    void testStRelate() {
        LOGGER.info("  testStRelate");
        testFilterResults(sSrvc.dao(sMdl.etLocation),
                "st_relate(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', location, 'T********')",
                getFromList(LOCATIONS, 1, 2, 4, 7));
        testFilterResults(sSrvc.dao(sMdl.etFeature),
                "st_relate(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', feature, 'T********')",
                getFromList(FEATURES, 1, 2, 4, 7));
    }

    /**
     * Test the st_touches filter function.
     *
     * @throws ServiceFailureException If the sSrvc doesn't respond.
     */
    @Test
    void testStTouches() {
        LOGGER.info("  testStTouches");
        testFilterResults(sSrvc.dao(sMdl.etLocation), "st_touches(geography'POLYGON((8 53, 7.5 54.5, 8.5 54.5, 8 53))', location)", getFromList(LOCATIONS, 2, 4));
        testFilterResults(sSrvc.dao(sMdl.etFeature), "st_touches(geography'POLYGON((8 53, 7.5 54.5, 8.5 54.5, 8 53))', feature)", getFromList(FEATURES, 2, 4));
    }

    /**
     * Test the st_within filter function.
     *
     * @throws ServiceFailureException If the sSrvc doesn't respond.
     */
    @Test
    void testStWithin() {
        LOGGER.info("  testStWithin");
        testFilterResults(sSrvc.dao(sMdl.etLocation), "st_within(geography'POINT(7.5 52.75)', location)", getFromList(LOCATIONS, 4));
        testFilterResults(sSrvc.dao(sMdl.etFeature), "st_within(geography'POINT(7.5 52.75)', feature)", getFromList(FEATURES, 4));
    }

    /**
     * Test JSON-filters on geoJson fields.
     */
    @Test
    void testGeoJsonFilters() {
        LOGGER.info("  testGeoJsonFilters");
        testFilterResults(sSrvc.dao(sMdl.etDatastream),
                "observedArea/type eq 'Point'",
                getFromList(DATASTREAMS, 1, 2));
        testFilterResults(sSrvc.dao(sMdl.etDatastream),
                "observedArea/type eq 'LineString'",
                getFromList(DATASTREAMS, 0));
        testFilterResults(sSrvc.dao(sMdl.etLocation),
                "location/type eq 'Point'",
                getFromList(LOCATIONS, 0, 1, 2));
        testFilterResults(sSrvc.dao(sMdl.etLocation),
                "location/geometry/type eq 'Point'",
                getFromList(LOCATIONS, 3));
        testFilterResults(sSrvc.dao(sMdl.etLocation),
                "location/type eq 'LineString'",
                getFromList(LOCATIONS, 5, 6, 7));
    }

    /**
     * Test GeoJSON result format.
     */
    @Test
    void testGeoJsonFormat() throws IOException {
        LOGGER.info("  testGeoJsonFormat");
        String geoJsonExpected = IOUtils.resourceToString("geoJsonResult.json", StandardCharsets.UTF_8, getClass().getClassLoader());
        JsonNode expected = SimpleJsonMapper.getSimpleObjectMapper().readTree(geoJsonExpected);

        String url = sSrvc.getBaseUrl() + sMdl.etLocation.mainSet + "?$select=name,description,encodingType,location&$orderby=id&$format=GeoJSON";
        HttpResponse response = HTTPMethods.doGet(url);
        JsonNode received = SimpleJsonMapper.getSimpleObjectMapper().readTree(response.response);

        Assertions.assertTrue(Utils.jsonEquals(expected, received));
    }
}
