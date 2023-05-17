/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.statests.c03filtering;

import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsSensingV11.EP_VALIDTIME;
import static de.fraunhofer.iosb.ilt.statests.util.EntityUtils.testFilterResults;
import static de.fraunhofer.iosb.ilt.statests.util.Utils.getFromList;

import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostclient.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.geojson.Polygon;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the geospatial functions.
 *
 * @author Hylke van der Schaaf
 */
public abstract class GeoTests extends AbstractTestClass {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GeoTests.class);

    private static final List<Entity> DATASTREAMS = new ArrayList<>();
    private static final List<Entity> FEATURESOFINTEREST = new ArrayList<>();
    private static final List<Entity> LOCATIONS = new ArrayList<>();
    private static final List<Entity> OBSERVATIONS = new ArrayList<>();
    private static final List<Entity> O_PROPS = new ArrayList<>();
    private static final List<Entity> SENSORS = new ArrayList<>();
    private static final List<Entity> THINGS = new ArrayList<>();

    public GeoTests(ServerVersion version) {
        super(version);
    }

    @Override
    protected void setUpVersion() throws ServiceFailureException, URISyntaxException {
        LOGGER.info("Setting up for version {}.", version.urlPart);
        createEntities();
    }

    @Override
    protected void tearDownVersion() throws ServiceFailureException {
        cleanup();
    }

    @AfterAll
    public static void tearDown() throws ServiceFailureException {
        LOGGER.info("Tearing down.");
        cleanup();
    }

    private static void cleanup() throws ServiceFailureException {
        EntityUtils.deleteAll(version, serverSettings, service);
        THINGS.clear();
        FEATURESOFINTEREST.clear();
        LOCATIONS.clear();
        SENSORS.clear();
        O_PROPS.clear();
        DATASTREAMS.clear();
        OBSERVATIONS.clear();
    }

    private static void createEntities() throws ServiceFailureException, URISyntaxException {
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

    private static void createObsProp() throws ServiceFailureException, URISyntaxException {
        Entity obsProp = sMdl.newObservedProperty("Temperature", "http://ucom.org/temperature", "The temperature of the thing.");
        sSrvc.create(obsProp);
        O_PROPS.add(obsProp);
    }

    private static void createDatastreams() throws ServiceFailureException {
        Entity datastream = sMdl.newDatastream("Datastream 1", "The temperature of thing 1, sensor 1.", "someType", new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
        datastream.setProperty(sMdl.npDatastreamThing, THINGS.get(0));
        datastream.setProperty(sMdl.npDatastreamSensor, SENSORS.get(0));
        datastream.setProperty(sMdl.npDatastreamObservedproperty, O_PROPS.get(0));
        sSrvc.create(datastream);
        DATASTREAMS.add(datastream);

        datastream = sMdl.newDatastream("Datastream 2", "The temperature of thing 2, sensor 1.", "someType", new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
        datastream.setProperty(sMdl.npDatastreamThing, THINGS.get(1));
        datastream.setProperty(sMdl.npDatastreamSensor, SENSORS.get(0));
        datastream.setProperty(sMdl.npDatastreamObservedproperty, O_PROPS.get(0));
        sSrvc.create(datastream);
        DATASTREAMS.add(datastream);

        datastream = sMdl.newDatastream("Datastream 3", "The temperature of thing 3, sensor 1.", "someType", new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
        datastream.setProperty(sMdl.npDatastreamThing, THINGS.get(2));
        datastream.setProperty(sMdl.npDatastreamSensor, SENSORS.get(0));
        datastream.setProperty(sMdl.npDatastreamObservedproperty, O_PROPS.get(0));
        sSrvc.create(datastream);
        DATASTREAMS.add(datastream);
    }

    private static void createLocation0() throws ServiceFailureException {
        // Locations 0
        Point gjo = new Point(8, 51);
        Entity location = sMdl.newLocation("Location 1.0", "First Location of Thing 1.", "application/vnd.geo+json", gjo);
        location.addNavigationEntity(sMdl.npLocationThings, THINGS.get(0));
        sSrvc.create(location);
        LOCATIONS.add(location);

        Entity featureOfInterest = sMdl.newFeatureOfInterest("FoI 0", "This should be FoI #0.", "application/geo+json", gjo);
        sSrvc.create(featureOfInterest);
        FEATURESOFINTEREST.add(featureOfInterest);

        Entity o = sMdl.newObservation(1, ZonedDateTime.parse("2016-01-01T01:01:01.000Z"), DATASTREAMS.get(0))
                .setProperty(sMdl.npObservationFeatureofinterest, featureOfInterest)
                .setProperty(EP_VALIDTIME, TimeInterval.create(Instant.parse("2016-01-01T01:01:01.000Z"), Instant.parse("2016-01-01T23:59:59.999Z")));
        sSrvc.create(o);
        OBSERVATIONS.add(o);
    }

    private static void createLocation1() throws ServiceFailureException {
        // Locations 1
        Point gjo = new Point(8, 52);
        Entity location = sMdl.newLocation("Location 1.1", "Second Entity of Thing 1.", "application/vnd.geo+json", gjo);
        location.addNavigationEntity(sMdl.npLocationThings, THINGS.get(0));
        sSrvc.create(location);
        LOCATIONS.add(location);

        Entity featureOfInterest = sMdl.newFeatureOfInterest("FoI 1", "This should be FoI #1.", "application/geo+json", gjo);
        sSrvc.create(featureOfInterest);
        FEATURESOFINTEREST.add(featureOfInterest);

        Entity o = sMdl.newObservation(2, ZonedDateTime.parse("2016-01-02T01:01:01.000Z"), DATASTREAMS.get(0))
                .setProperty(sMdl.npObservationFeatureofinterest, featureOfInterest)
                .setProperty(EP_VALIDTIME, TimeInterval.create(Instant.parse("2016-01-02T01:01:01.000Z"), Instant.parse("2016-01-02T23:59:59.999Z")));
        sSrvc.create(o);
        OBSERVATIONS.add(o);
    }

    private static void createLocation2() throws ServiceFailureException {
        // Locations 2
        Point gjo = new Point(8, 53);
        Entity location = sMdl.newLocation("Location 2", "Location of Thing 2.", "application/vnd.geo+json", gjo);
        location.addNavigationEntity(sMdl.npLocationThings, THINGS.get(1));
        sSrvc.create(location);
        LOCATIONS.add(location);

        Entity featureOfInterest = sMdl.newFeatureOfInterest("FoI 2", "This should be FoI #2.", "application/geo+json", gjo);
        sSrvc.create(featureOfInterest);
        FEATURESOFINTEREST.add(featureOfInterest);

        Entity o = sMdl.newObservation(3, ZonedDateTime.parse("2016-01-03T01:01:01.000Z"), DATASTREAMS.get(1))
                .setProperty(sMdl.npObservationFeatureofinterest, featureOfInterest)
                .setProperty(EP_VALIDTIME, TimeInterval.create(Instant.parse("2016-01-03T01:01:01.000Z"), Instant.parse("2016-01-03T23:59:59.999Z")));
        sSrvc.create(o);
        OBSERVATIONS.add(o);
    }

    private static void createLocation3() throws ServiceFailureException {
        // Locations 3
        Point gjo = new Point(8, 54);
        Entity location = sMdl.newLocation("Location 3", "Location of Thing 3.", "application/vnd.geo+json", gjo);
        location.addNavigationEntity(sMdl.npLocationThings, THINGS.get(2));
        sSrvc.create(location);
        LOCATIONS.add(location);

        Entity featureOfInterest = sMdl.newFeatureOfInterest("FoI 3", "This should be FoI #3.", "application/geo+json", gjo);
        sSrvc.create(featureOfInterest);
        FEATURESOFINTEREST.add(featureOfInterest);

        Entity o = sMdl.newObservation(4, ZonedDateTime.parse("2016-01-04T01:01:01.000Z"), DATASTREAMS.get(2))
                .setProperty(sMdl.npObservationFeatureofinterest, featureOfInterest)
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
        location.addNavigationEntity(sMdl.npLocationThings, THINGS.get(3));
        sSrvc.create(location);
        LOCATIONS.add(location);

        Entity featureOfInterest = sMdl.newFeatureOfInterest("FoI 4", "This should be FoI #4.", "application/geo+json", gjo);
        sSrvc.create(featureOfInterest);
        FEATURESOFINTEREST.add(featureOfInterest);
    }

    private static void createLocation5() throws ServiceFailureException {
        // Locations 5
        LineString gjo = new LineString(
                new LngLatAlt(5, 52),
                new LngLatAlt(5, 53));
        Entity location = sMdl.newLocation("Location 5", "A line.", "application/vnd.geo+json", gjo);
        sSrvc.create(location);
        LOCATIONS.add(location);

        Entity featureOfInterest = sMdl.newFeatureOfInterest("FoI 5", "This should be FoI #5.", "application/geo+json", gjo);
        sSrvc.create(featureOfInterest);
        FEATURESOFINTEREST.add(featureOfInterest);
    }

    private static void createLocation6() throws ServiceFailureException {
        // Locations 6
        LineString gjo = new LineString(
                new LngLatAlt(5, 52),
                new LngLatAlt(6, 53));
        Entity location = sMdl.newLocation("Location 6", "A longer line.", "application/vnd.geo+json", gjo);
        sSrvc.create(location);
        LOCATIONS.add(location);

        Entity featureOfInterest = sMdl.newFeatureOfInterest("FoI 6", "This should be FoI #6.", "application/geo+json", gjo);
        sSrvc.create(featureOfInterest);
        FEATURESOFINTEREST.add(featureOfInterest);
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

        Entity featureOfInterest = sMdl.newFeatureOfInterest("FoI 7", "This should be FoI #7.", "application/geo+json", gjo);
        sSrvc.create(featureOfInterest);
        FEATURESOFINTEREST.add(featureOfInterest);
    }

    /**
     * Test the geo.distance filter function.
     *
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    void testGeoDistance() throws ServiceFailureException {
        LOGGER.info("  testGeoDistance");
        testFilterResults(sSrvc.dao(sMdl.etLocation), "geo.distance(location, geography'POINT(8 54.1)') lt 1", getFromList(LOCATIONS, 3));
        testFilterResults(sSrvc.dao(sMdl.etLocation), "geo.distance(location, geography'POINT(8 54.1)') gt 1", getFromList(LOCATIONS, 0, 1, 2, 4, 5, 6, 7));
        testFilterResults(sSrvc.dao(sMdl.etObservation), "geo.distance(FeatureOfInterest/feature, geography'POINT(8 54.1)') lt 1", getFromList(OBSERVATIONS, 3));
        testFilterResults(sSrvc.dao(sMdl.etObservation), "geo.distance(FeatureOfInterest/feature, geography'POINT(8 54.1)') gt 1", getFromList(OBSERVATIONS, 0, 1, 2));
    }

    /**
     * Test the geo.intersects filter function.
     *
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    void testGeoIntersects() throws ServiceFailureException {
        LOGGER.info("  testGeoIntersects");
        testFilterResults(sSrvc.dao(sMdl.etLocation), "geo.intersects(location, geography'LINESTRING(7.5 51, 7.5 54)')", getFromList(LOCATIONS, 4, 7));
        testFilterResults(sSrvc.dao(sMdl.etFeatureOfInterest), "geo.intersects(feature, geography'LINESTRING(7.5 51, 7.5 54)')", getFromList(FEATURESOFINTEREST, 4, 7));
        testFilterResults(sSrvc.dao(sMdl.etDatastream),
                "geo.intersects(observedArea, geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))')",
                getFromList(DATASTREAMS, 0, 1));
    }

    /**
     * Test the geo.length filter function.
     *
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    void testGeoLength() throws ServiceFailureException {
        LOGGER.info("  testGeoLength");
        testFilterResults(sSrvc.dao(sMdl.etLocation), "geo.length(location) gt 1", getFromList(LOCATIONS, 6, 7));
        testFilterResults(sSrvc.dao(sMdl.etLocation), "geo.length(location) ge 1", getFromList(LOCATIONS, 5, 6, 7));
        testFilterResults(sSrvc.dao(sMdl.etLocation), "geo.length(location) eq 1", getFromList(LOCATIONS, 5));
        testFilterResults(sSrvc.dao(sMdl.etLocation), "geo.length(location) ne 1", getFromList(LOCATIONS, 0, 1, 2, 3, 4, 6, 7));
        testFilterResults(sSrvc.dao(sMdl.etLocation), "geo.length(location) le 4", getFromList(LOCATIONS, 0, 1, 2, 3, 4, 5, 6, 7));
        testFilterResults(sSrvc.dao(sMdl.etLocation), "geo.length(location) lt 4", getFromList(LOCATIONS, 0, 1, 2, 3, 4, 5, 6));
        testFilterResults(sSrvc.dao(sMdl.etFeatureOfInterest), "geo.length(feature) gt 1", getFromList(FEATURESOFINTEREST, 6, 7));
        testFilterResults(sSrvc.dao(sMdl.etFeatureOfInterest), "geo.length(feature) ge 1", getFromList(FEATURESOFINTEREST, 5, 6, 7));
        testFilterResults(sSrvc.dao(sMdl.etFeatureOfInterest), "geo.length(feature) eq 1", getFromList(FEATURESOFINTEREST, 5));
        testFilterResults(sSrvc.dao(sMdl.etFeatureOfInterest), "geo.length(feature) ne 1", getFromList(FEATURESOFINTEREST, 0, 1, 2, 3, 4, 6, 7));
        testFilterResults(sSrvc.dao(sMdl.etFeatureOfInterest), "geo.length(feature) le 4", getFromList(FEATURESOFINTEREST, 0, 1, 2, 3, 4, 5, 6, 7));
        testFilterResults(sSrvc.dao(sMdl.etFeatureOfInterest), "geo.length(feature) lt 4", getFromList(FEATURESOFINTEREST, 0, 1, 2, 3, 4, 5, 6));
    }

    /**
     * Test the st_contains filter function.
     *
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    void testStContains() throws ServiceFailureException {
        LOGGER.info("  testStContains");
        testFilterResults(sSrvc.dao(sMdl.etLocation),
                "st_contains(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', location)",
                getFromList(LOCATIONS, 1, 2));
        testFilterResults(sSrvc.dao(sMdl.etObservation),
                "st_contains(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', FeatureOfInterest/feature)",
                getFromList(OBSERVATIONS, 1, 2));
        testFilterResults(sSrvc.dao(sMdl.etDatastream),
                "st_contains(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', observedArea)",
                getFromList(DATASTREAMS, 1));
    }

    /**
     * Test the st_crosses filter function.
     *
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    void testStCrosses() throws ServiceFailureException {
        LOGGER.info("  testStCrosses");
        testFilterResults(sSrvc.dao(sMdl.etLocation), "st_crosses(geography'LINESTRING(7.5 51.5, 7.5 53.5)', location)", getFromList(LOCATIONS, 4, 7));
        testFilterResults(sSrvc.dao(sMdl.etFeatureOfInterest), "st_crosses(geography'LINESTRING(7.5 51.5, 7.5 53.5)', feature)", getFromList(FEATURESOFINTEREST, 4, 7));
    }

    /**
     * Test the st_disjoint filter function.
     *
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    void testStDisjoint() throws ServiceFailureException {
        LOGGER.info("  testStDisjoint");
        testFilterResults(sSrvc.dao(sMdl.etLocation),
                "st_disjoint(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', location)",
                getFromList(LOCATIONS, 0, 3, 5, 6));
        testFilterResults(sSrvc.dao(sMdl.etFeatureOfInterest),
                "st_disjoint(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', feature)",
                getFromList(FEATURESOFINTEREST, 0, 3, 5, 6));
    }

    /**
     * Test the st_equals filter function.
     *
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    void testStEquals() throws ServiceFailureException {
        LOGGER.info("  testStEquals");
        testFilterResults(sSrvc.dao(sMdl.etLocation), "st_equals(location, geography'POINT(8 53)')", getFromList(LOCATIONS, 2));
        testFilterResults(sSrvc.dao(sMdl.etFeatureOfInterest), "st_equals(feature, geography'POINT(8 53)')", getFromList(FEATURESOFINTEREST, 2));
    }

    /**
     * Test the st_intersects filter function.
     *
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    void testStIntersects() throws ServiceFailureException {
        LOGGER.info("  testStIntersects");
        testFilterResults(sSrvc.dao(sMdl.etLocation), "st_intersects(location, geography'LINESTRING(7.5 51, 7.5 54)')", getFromList(LOCATIONS, 4, 7));
        testFilterResults(sSrvc.dao(sMdl.etFeatureOfInterest), "st_intersects(feature, geography'LINESTRING(7.5 51, 7.5 54)')", getFromList(FEATURESOFINTEREST, 4, 7));
        testFilterResults(sSrvc.dao(sMdl.etDatastream),
                "st_intersects(observedArea, geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))')",
                getFromList(DATASTREAMS, 0, 1));
    }

    /**
     * Test the st_overlaps filter function.
     *
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    void testStOverlaps() throws ServiceFailureException {
        LOGGER.info("  testStOverlaps");
        testFilterResults(sSrvc.dao(sMdl.etLocation),
                "st_overlaps(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', location)",
                getFromList(LOCATIONS, 4));
        testFilterResults(sSrvc.dao(sMdl.etFeatureOfInterest),
                "st_overlaps(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', feature)",
                getFromList(FEATURESOFINTEREST, 4));
    }

    /**
     * Test the st_relate filter function.
     *
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    void testStRelate() throws ServiceFailureException {
        LOGGER.info("  testStRelate");
        testFilterResults(sSrvc.dao(sMdl.etLocation),
                "st_relate(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', location, 'T********')",
                getFromList(LOCATIONS, 1, 2, 4, 7));
        testFilterResults(sSrvc.dao(sMdl.etFeatureOfInterest),
                "st_relate(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', feature, 'T********')",
                getFromList(FEATURESOFINTEREST, 1, 2, 4, 7));
    }

    /**
     * Test the st_touches filter function.
     *
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    void testStTouches() throws ServiceFailureException {
        LOGGER.info("  testStTouches");
        testFilterResults(sSrvc.dao(sMdl.etLocation), "st_touches(geography'POLYGON((8 53, 7.5 54.5, 8.5 54.5, 8 53))', location)", getFromList(LOCATIONS, 2, 4));
        testFilterResults(sSrvc.dao(sMdl.etFeatureOfInterest), "st_touches(geography'POLYGON((8 53, 7.5 54.5, 8.5 54.5, 8 53))', feature)", getFromList(FEATURESOFINTEREST, 2, 4));
    }

    /**
     * Test the st_within filter function.
     *
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    void testStWithin() throws ServiceFailureException {
        LOGGER.info("  testStWithin");
        testFilterResults(sSrvc.dao(sMdl.etLocation), "st_within(geography'POINT(7.5 52.75)', location)", getFromList(LOCATIONS, 4));
        testFilterResults(sSrvc.dao(sMdl.etFeatureOfInterest), "st_within(geography'POINT(7.5 52.75)', feature)", getFromList(FEATURESOFINTEREST, 4));
    }

}
