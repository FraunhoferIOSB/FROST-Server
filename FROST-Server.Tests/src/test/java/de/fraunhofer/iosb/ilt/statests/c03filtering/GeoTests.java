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
package de.fraunhofer.iosb.ilt.statests.c03filtering;

import static de.fraunhofer.iosb.ilt.statests.util.EntityUtils.testFilterResults;
import static de.fraunhofer.iosb.ilt.statests.util.Utils.getFromList;

import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.json.SimpleJsonMapper;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods.HttpResponse;
import de.fraunhofer.iosb.ilt.statests.util.Utils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.geojson.Feature;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.geojson.Polygon;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;

/**
 * Tests for the geospatial functions.
 */
public abstract class GeoTests extends AbstractTestClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoTests.class);

    private static final List<Entity> DATASTREAMS = new ArrayList<>();
    private static final List<Entity> FEATURESOFINTEREST = new ArrayList<>();
    private static final List<Entity> LOCATIONS = new ArrayList<>();
    private static final List<Entity> OBSERVATIONS = new ArrayList<>();
    private static final List<Entity> O_PROPS = new ArrayList<>();
    private static final List<Entity> SENSORS = new ArrayList<>();
    private static final List<Entity> THINGS = new ArrayList<>();
    private static SensorThingsV11Sensing sMdl;

    public GeoTests(ServerVersion version) {
        super(version);
    }

    @Override
    protected void setUpVersion() throws ServiceFailureException {
        LOGGER.info("Setting up for version {}.", version.urlPart);
        sMdl = sSrvc.getModel(SensorThingsV11Sensing.class);
        createEntities();
    }

    public static void cleanup() {
        EntityUtils.deleteAll(sSrvc);
        THINGS.clear();
        FEATURESOFINTEREST.clear();
        LOCATIONS.clear();
        SENSORS.clear();
        O_PROPS.clear();
        DATASTREAMS.clear();
        OBSERVATIONS.clear();
        AbstractTestClass.cleanup();
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
        Entity thing = sMdl.buildThing()
                .setName("Thing 1")
                .setDescription("The first thing.")
                .build();
        sSrvc.create(thing);
        THINGS.add(thing);

        thing = sMdl.buildThing()
                .setName("Thing 2")
                .setDescription("The second thing.")
                .build();
        sSrvc.create(thing);
        THINGS.add(thing);

        thing = sMdl.buildThing()
                .setName("Thing 3")
                .setDescription("The third thing.")
                .build();
        sSrvc.create(thing);
        THINGS.add(thing);

        thing = sMdl.buildThing()
                .setName("Thing 4")
                .setDescription("The fourt thing.")
                .build();
        sSrvc.create(thing);
        THINGS.add(thing);
    }

    private static void createSensor() throws ServiceFailureException {
        Entity sensor = sMdl.buildSensor()
                .setName("Sensor 1")
                .setDescription("The first sensor.")
                .setEncodingType("text")
                .setMetadata("Some metadata.")
                .build();
        sSrvc.create(sensor);
        SENSORS.add(sensor);
    }

    private static void createObsProp() throws ServiceFailureException {
        Entity obsProp = sMdl.buildObservedProperty()
                .setName("Temperature")
                .setDefinition("http://ucom.org/temperature")
                .setDescription("The temperature of the thing.")
                .build();
        sSrvc.create(obsProp);
        O_PROPS.add(obsProp);
    }

    private static void createDatastreams() throws ServiceFailureException {
        Entity datastream = sMdl.buildDatastream()
                .setName("Datastream 1")
                .setDescription("The temperature of thing 1, sensor 1.")
                .setObservationType("someType")
                .setUnitOfMeasurement(new UnitOfMeasurement("degree celcius", "°C", "ucum:T"))
                .setThing(THINGS.get(0))
                .setSensor(SENSORS.get(0))
                .setObservedProperty(O_PROPS.get(0))
                .build();
        sSrvc.create(datastream);
        DATASTREAMS.add(datastream);

        datastream = sMdl.buildDatastream()
                .setName("Datastream 2")
                .setDescription("The temperature of thing 2, sensor 1.")
                .setObservationType("someType")
                .setUnitOfMeasurement(new UnitOfMeasurement("degree celcius", "°C", "ucum:T"))
                .setThing(THINGS.get(1))
                .setSensor(SENSORS.get(0))
                .setObservedProperty(O_PROPS.get(0))
                .build();
        sSrvc.create(datastream);
        DATASTREAMS.add(datastream);

        datastream = sMdl.buildDatastream()
                .setName("Datastream 3")
                .setDescription("The temperature of thing 3, sensor 1.")
                .setObservationType("someType")
                .setUnitOfMeasurement(new UnitOfMeasurement("degree celcius", "°C", "ucum:T"))
                .setThing(THINGS.get(2))
                .setSensor(SENSORS.get(0))
                .setObservedProperty(O_PROPS.get(0))
                .build();
        sSrvc.create(datastream);
        DATASTREAMS.add(datastream);

        datastream = sMdl.buildDatastream()
                .setName("Datastream 4")
                .setDescription("The temperature of thing 4, sensor 1.")
                .setObservationType("someType")
                .setUnitOfMeasurement(new UnitOfMeasurement("degree celcius", "°C", "ucum:T"))
                .setThing(THINGS.get(3))
                .setSensor(SENSORS.get(0))
                .setObservedProperty(O_PROPS.get(0))
                .build();
        sSrvc.create(datastream);
        DATASTREAMS.add(datastream);
    }

    private static void createLocation0() throws ServiceFailureException {
        // Locations 0
        Point gjo = new Point(8, 51);
        Entity location = sMdl.buildLocation()
                .setName("Location 1.0")
                .setDescription("First Location of Thing 1.")
                .setEncodingType("application/vnd.geo+json")
                .setLocation(gjo)
                .addThing(THINGS.get(0))
                .build();
        sSrvc.create(location);
        LOCATIONS.add(location);

        Entity featureOfInterest = sMdl.buildFeature()
                .setName("FoI 0")
                .setDescription("This should be FoI #0.")
                .setEncodingType("application/geo+json")
                .setFeature(gjo)
                .build();
        sSrvc.create(featureOfInterest);
        FEATURESOFINTEREST.add(featureOfInterest);

        Entity o = sMdl.buildObservation()
                .setResult(1)
                .setPhenomenonTime(TimeValue.create(ZonedDateTime.parse("2016-01-01T01:01:01.000Z")))
                .setDatastream(DATASTREAMS.get(0))
                .setFeatureOfInterest(featureOfInterest)
                .setValidTime(TimeInterval.create(Instant.parse("2016-01-01T01:01:01.000Z"), Instant.parse("2016-01-01T23:59:59.999Z")))
                .build();
        sSrvc.create(o);
        OBSERVATIONS.add(o);
    }

    private static void createLocation1() throws ServiceFailureException {
        // Locations 1
        Point gjo = new Point(8, 52);
        Entity location = sMdl.buildLocation()
                .setName("Location 1.1")
                .setDescription("Second Entity of Thing 1.")
                .setEncodingType("application/vnd.geo+json")
                .setLocation(gjo)
                .addThing(THINGS.get(0))
                .build();
        sSrvc.create(location);
        LOCATIONS.add(location);

        Entity featureOfInterest = sMdl.buildFeature()
                .setName("FoI 1")
                .setDescription("This should be FoI #1.")
                .setEncodingType("application/geo+json")
                .setFeature(gjo)
                .build();
        sSrvc.create(featureOfInterest);
        FEATURESOFINTEREST.add(featureOfInterest);

        Entity o = sMdl.buildObservation()
                .setResult(2)
                .setPhenomenonTimeStart(TimeInstant.parseMoment("2016-01-02T01:01:01.000Z"))
                .setDatastream(DATASTREAMS.get(0))
                .setFeatureOfInterest(featureOfInterest)
                .setValidTime(TimeInterval.create(Instant.parse("2016-01-02T01:01:01.000Z"), Instant.parse("2016-01-02T23:59:59.999Z")))
                .build();
        sSrvc.create(o);
        OBSERVATIONS.add(o);
    }

    private static void createLocation2() throws ServiceFailureException {
        // Locations 2
        Point gjo = new Point(8, 53);
        Entity location = sMdl.buildLocation()
                .setName("Location 2")
                .setDescription("Location of Thing 2.")
                .setEncodingType("application/vnd.geo+json")
                .setLocation(gjo)
                .addThing(THINGS.get(1))
                .build();
        sSrvc.create(location);
        LOCATIONS.add(location);

        Entity featureOfInterest = sMdl.buildFeature()
                .setName("FoI 2")
                .setDescription("This should be FoI #2.")
                .setEncodingType("application/geo+json")
                .setFeature(gjo)
                .build();
        sSrvc.create(featureOfInterest);
        FEATURESOFINTEREST.add(featureOfInterest);

        Entity o = sMdl.buildObservation()
                .setResult(3)
                .setPhenomenonTimeStart(TimeInstant.parseMoment("2016-01-03T01:01:01.000Z"))
                .setDatastream(DATASTREAMS.get(1))
                .setFeatureOfInterest(featureOfInterest)
                .setValidTime(TimeInterval.create(Instant.parse("2016-01-03T01:01:01.000Z"), Instant.parse("2016-01-03T23:59:59.999Z")))
                .build();
        sSrvc.create(o);
        OBSERVATIONS.add(o);
    }

    private static void createLocation3() throws ServiceFailureException {
        // Locations 3
        Point point = new Point(8, 54);
        Feature gjo = new Feature();
        gjo.setGeometry(point);
        Entity location = sMdl.buildLocation()
                .setName("Location 3")
                .setDescription("Location of Thing 3.")
                .setEncodingType("application/geo+json")
                .setLocation(gjo)
                .addThing(THINGS.get(2))
                .build();
        sSrvc.create(location);
        LOCATIONS.add(location);

        Entity featureOfInterest = sMdl.buildFeature()
                .setName("FoI 3")
                .setDescription("This should be FoI #3.")
                .setEncodingType("application/geo+json")
                .setFeature(gjo)
                .build();
        sSrvc.create(featureOfInterest);
        FEATURESOFINTEREST.add(featureOfInterest);

        Entity o = sMdl.buildObservation()
                .setResult(4)
                .setPhenomenonTimeStart(TimeInstant.parseMoment("2016-01-04T01:01:01.000Z"))
                .setDatastream(DATASTREAMS.get(2))
                .setFeatureOfInterest(featureOfInterest)
                .setValidTime(TimeInterval.create(Instant.parse("2016-01-04T01:01:01.000Z"), Instant.parse("2016-01-04T23:59:59.999Z")))
                .build();
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
        Entity location = sMdl.buildLocation()
                .setName("Location 4")
                .setDescription("Location of Thing 4.")
                .setEncodingType("application/vnd.geo+json")
                .setLocation(gjo)
                .addThing(THINGS.get(3))
                .build();
        sSrvc.create(location);
        LOCATIONS.add(location);

        Entity featureOfInterest = sMdl.buildFeature()
                .setName("FoI 4")
                .setDescription("This should be FoI #4.")
                .setEncodingType("application/geo+json")
                .setFeature(gjo)
                .build();
        sSrvc.create(featureOfInterest);
        FEATURESOFINTEREST.add(featureOfInterest);

        Entity o = sMdl.buildObservation()
                .setResult(4)
                .setPhenomenonTimeStart(TimeInstant.parseMoment("2016-01-04T01:01:01.000Z"))
                .setDatastream(DATASTREAMS.get(3))
                .setFeatureOfInterest(featureOfInterest)
                .setValidTime(TimeInterval.create(Instant.parse("2016-01-04T01:01:01.000Z"), Instant.parse("2016-01-04T23:59:59.999Z")))
                .build();
        sSrvc.create(o);
        OBSERVATIONS.add(o);
    }

    private static void createLocation5() throws ServiceFailureException {
        // Locations 5
        LineString gjo = new LineString(
                new LngLatAlt(5, 52),
                new LngLatAlt(5, 53));
        Entity location = sMdl.buildLocation()
                .setName("Location 5")
                .setDescription("A line.")
                .setEncodingType("application/vnd.geo+json")
                .setLocation(gjo)
                .build();
        sSrvc.create(location);
        LOCATIONS.add(location);

        Entity featureOfInterest = sMdl.buildFeature()
                .setName("FoI 5")
                .setDescription("This should be FoI #5.")
                .setEncodingType("application/geo+json")
                .setFeature(gjo)
                .build();
        sSrvc.create(featureOfInterest);
        FEATURESOFINTEREST.add(featureOfInterest);
    }

    private static void createLocation6() throws ServiceFailureException {
        // Locations 6
        LineString gjo = new LineString(
                new LngLatAlt(5, 52),
                new LngLatAlt(6, 53));
        Entity location = sMdl.buildLocation()
                .setName("Location 6")
                .setDescription("A longer line.")
                .setEncodingType("application/vnd.geo+json")
                .setLocation(gjo)
                .build();
        sSrvc.create(location);
        LOCATIONS.add(location);

        Entity featureOfInterest = sMdl.buildFeature()
                .setName("FoI 6")
                .setDescription("This should be FoI #6.")
                .setEncodingType("application/geo+json")
                .setFeature(gjo)
                .build();
        sSrvc.create(featureOfInterest);
        FEATURESOFINTEREST.add(featureOfInterest);
    }

    private static void createLocation7() throws ServiceFailureException {
        // Locations 7
        LineString gjo = new LineString(
                new LngLatAlt(4, 52),
                new LngLatAlt(8, 52));
        Entity location = sMdl.buildLocation()
                .setName("Location 7")
                .setDescription("The longest line.")
                .setEncodingType("application/vnd.geo+json")
                .setLocation(gjo)
                .build();
        sSrvc.create(location);
        LOCATIONS.add(location);

        Entity featureOfInterest = sMdl.buildFeature()
                .setName("FoI 7")
                .setDescription("This should be FoI #7.")
                .setEncodingType("application/geo+json")
                .setFeature(gjo)
                .build();
        sSrvc.create(featureOfInterest);
        FEATURESOFINTEREST.add(featureOfInterest);
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
        testFilterResults(sSrvc.dao(sMdl.etObservation), "geo.distance(FeatureOfInterest/feature, geography'POINT(8 54.1)') lt 1", getFromList(OBSERVATIONS, 3));
        testFilterResults(sSrvc.dao(sMdl.etObservation), "geo.distance(FeatureOfInterest/feature, geography'POINT(8 54.1)') gt 1", getFromList(OBSERVATIONS, 0, 1, 2, 4));
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
        testFilterResults(sSrvc.dao(sMdl.etFeatureOfInterest), "geo.intersects(feature, geography'LINESTRING(7.5 51, 7.5 54)')", getFromList(FEATURESOFINTEREST, 4, 7));
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
     * @throws ServiceFailureException If the sSrvc doesn't respond.
     */
    @Test
    void testStContains() {
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
     * @throws ServiceFailureException If the sSrvc doesn't respond.
     */
    @Test
    void testStCrosses() {
        LOGGER.info("  testStCrosses");
        testFilterResults(sSrvc.dao(sMdl.etLocation), "st_crosses(geography'LINESTRING(7.5 51.5, 7.5 53.5)', location)", getFromList(LOCATIONS, 4, 7));
        testFilterResults(sSrvc.dao(sMdl.etFeatureOfInterest), "st_crosses(geography'LINESTRING(7.5 51.5, 7.5 53.5)', feature)", getFromList(FEATURESOFINTEREST, 4, 7));
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
        testFilterResults(sSrvc.dao(sMdl.etFeatureOfInterest),
                "st_disjoint(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', feature)",
                getFromList(FEATURESOFINTEREST, 0, 3, 5, 6));
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
        testFilterResults(sSrvc.dao(sMdl.etFeatureOfInterest), "st_equals(feature, geography'POINT(8 53)')", getFromList(FEATURESOFINTEREST, 2));
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
        testFilterResults(sSrvc.dao(sMdl.etFeatureOfInterest), "st_intersects(feature, geography'LINESTRING(7.5 51, 7.5 54)')", getFromList(FEATURESOFINTEREST, 4, 7));
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
        testFilterResults(sSrvc.dao(sMdl.etFeatureOfInterest),
                "st_overlaps(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', feature)",
                getFromList(FEATURESOFINTEREST, 4));
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
        testFilterResults(sSrvc.dao(sMdl.etFeatureOfInterest),
                "st_relate(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', feature, 'T********')",
                getFromList(FEATURESOFINTEREST, 1, 2, 4, 7));
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
        testFilterResults(sSrvc.dao(sMdl.etFeatureOfInterest), "st_touches(geography'POLYGON((8 53, 7.5 54.5, 8.5 54.5, 8 53))', feature)", getFromList(FEATURESOFINTEREST, 2, 4));
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
        testFilterResults(sSrvc.dao(sMdl.etFeatureOfInterest), "st_within(geography'POINT(7.5 52.75)', feature)", getFromList(FEATURESOFINTEREST, 4));
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
