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

import static de.fraunhofer.iosb.ilt.frostclient.utils.CollectionsHelper.propertiesBuilder;
import static de.fraunhofer.iosb.ilt.frostclient.utils.StringHelper.formatKeyValuesForUrl;
import static de.fraunhofer.iosb.ilt.statests.util.EntityUtils.createDatastream;
import static de.fraunhofer.iosb.ilt.statests.util.EntityUtils.createObservationSet;
import static de.fraunhofer.iosb.ilt.statests.util.EntityUtils.createObservedProperty;
import static de.fraunhofer.iosb.ilt.statests.util.EntityUtils.createSensor;
import static de.fraunhofer.iosb.ilt.statests.util.EntityUtils.testFilterResults;
import static de.fraunhofer.iosb.ilt.statests.util.Utils.getFromList;
import static org.junit.jupiter.api.Assertions.fail;

import de.fraunhofer.iosb.ilt.frostclient.dao.Dao;
import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.geojson.Polygon;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some odd tests.
 */
public abstract class FilterTests extends AbstractTestClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilterTests.class);

    private static final List<Entity> THINGS = new ArrayList<>();
    private static final List<Entity> LOCATIONS = new ArrayList<>();
    private static final List<Entity> SENSORS = new ArrayList<>();
    private static final List<Entity> O_PROPS = new ArrayList<>();
    private static final List<Entity> DATASTREAMS = new ArrayList<>();
    private static final List<Entity> OBSERVATIONS = new ArrayList<>();
    private static SensorThingsV11Sensing sMdl;

    public FilterTests(ServerVersion version) {
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
        LOCATIONS.clear();
        SENSORS.clear();
        O_PROPS.clear();
        DATASTREAMS.clear();
        OBSERVATIONS.clear();
        AbstractTestClass.cleanup();
    }

    private static void createEntities() throws ServiceFailureException {
        Entity thing = sMdl.buildThing()
                .setName("Thing 1")
                .setDescription("The first thing.")
                .build();
        sSrvc.create(thing);
        THINGS.add(thing);

        thing = sMdl.buildThing()
                .setName("Thing 2")
                .setDescription("The second thing.")
                .setProperties(
                        propertiesBuilder()
                                .addItem("field", 2)
                                .addItem("string", "one")
                                .build())
                .build();
        sSrvc.create(thing);
        THINGS.add(thing);

        thing = sMdl.buildThing()
                .setName("Thing 3")
                .setDescription("The third thing.")
                .setProperties(
                        propertiesBuilder()
                                .addItem("field", 3)
                                .addItem("string", "two")
                                .build())
                .build();
        sSrvc.create(thing);
        THINGS.add(thing);

        thing = sMdl.buildThing()
                .setName("Thing 4")
                .setDescription("The fourth thing.")
                .build();
        sSrvc.create(thing);
        THINGS.add(thing);

        // Locations 0
        Entity location = sMdl.buildLocation()
                .setName("Location 1.0")
                .setDescription("First Location of Thing 1.")
                .setEncodingType("application/vnd.geo+json")
                .setLocation(new Point(8, 51))
                .setProperties(propertiesBuilder().addItem("field", 1).build())
                .addThing(THINGS.get(0))
                .build();
        sSrvc.create(location);
        LOCATIONS.add(location);

        // Locations 1
        location = sMdl.buildLocation()
                .setName("Location 1.1")
                .setDescription("Second Location of Thing 1.")
                .setEncodingType("application/vnd.geo+json")
                .setLocation(new Point(8, 52))
                .setProperties(propertiesBuilder().addItem("field", 1.1).build())
                .addThing(THINGS.get(0))
                .build();
        sSrvc.create(location);
        LOCATIONS.add(location);

        // Locations 2
        location = sMdl.buildLocation()
                .setName("Location 2")
                .setDescription("Location of Thing 2.")
                .setEncodingType("application/vnd.geo+json")
                .setLocation(new Point(8, 53))
                .setProperties(propertiesBuilder().addItem("field", 2).build())
                .addThing(THINGS.get(1))
                .build();
        sSrvc.create(location);
        LOCATIONS.add(location);

        // Locations 3
        location = sMdl.buildLocation()
                .setName("Location 3")
                .setDescription("Location of Thing 3.")
                .setEncodingType("application/vnd.geo+json")
                .setLocation(new Point(8, 54))
                .setProperties(propertiesBuilder().addItem("field", 3).build())
                .addThing(THINGS.get(2))
                .build();
        sSrvc.create(location);
        LOCATIONS.add(location);

        // Locations 4
        location = sMdl.buildLocation()
                .setName("Location 4")
                .setDescription("Location of Thing 4.")
                .setEncodingType("application/vnd.geo+json")
                .setLocation(new Polygon(
                        new LngLatAlt(8, 53),
                        new LngLatAlt(7, 52),
                        new LngLatAlt(7, 53),
                        new LngLatAlt(8, 53)))
                .setProperties(propertiesBuilder().addItem("field", 4).build())
                .addThing(THINGS.get(3))
                .build();
        sSrvc.create(location);
        LOCATIONS.add(location);

        // Locations 5
        location = sMdl.buildLocation()
                .setName("Location 5")
                .setDescription("A line.")
                .setEncodingType("application/vnd.geo+json")
                .setLocation(new LineString(
                        new LngLatAlt(5, 52),
                        new LngLatAlt(5, 53)))
                .setProperties(propertiesBuilder().addItem("field", 5).build())
                .build();
        sSrvc.create(location);
        LOCATIONS.add(location);

        // Locations 6
        location = sMdl.buildLocation()
                .setName("Location 6")
                .setDescription("A longer line.")
                .setEncodingType("application/vnd.geo+json")
                .setLocation(new LineString(
                        new LngLatAlt(5, 52),
                        new LngLatAlt(6, 53)))
                .setProperties(propertiesBuilder().addItem("field", 6).build())
                .build();
        sSrvc.create(location);
        LOCATIONS.add(location);

        // Locations 7
        location = sMdl.buildLocation()
                .setName("Location 7")
                .setDescription("The longest line.")
                .setEncodingType("application/vnd.geo+json")
                .setLocation(new LineString(
                        new LngLatAlt(4, 52),
                        new LngLatAlt(8, 52)))
                .setProperties(propertiesBuilder().addItem("field", 7).build())
                .build();
        sSrvc.create(location);
        LOCATIONS.add(location);

        createSensor(sSrvc, "Sensor 0", "The sensor with idx 0.", "text", "Some metadata.", SENSORS);
        createSensor(sSrvc, "Sensor 1", "The sensor with idx 1.", "text", "Some metadata.", SENSORS);
        createSensor(sSrvc, "Sensor 2", "The sensor with idx 2.", "text", "Some metadata.", SENSORS);
        createSensor(sSrvc, "Sensor 3", "The sensor with idx 3.", "text", "Some metadata.", SENSORS);

        createObservedProperty(sSrvc, "ObservedProperty 0", "http://ucom.org/temperature", "ObservedProperty with index 0.", O_PROPS);
        createObservedProperty(sSrvc, "ObservedProperty 1", "http://ucom.org/humidity", "ObservedProperty with index 1.", O_PROPS);
        createObservedProperty(sSrvc, "ObservedProperty 2", "http://ucom.org/pressure", "ObservedProperty with index 2.", O_PROPS);
        createObservedProperty(sSrvc, "ObservedProperty 3", "http://ucom.org/turbidity", "ObservedProperty with index 3.", O_PROPS);

        UnitOfMeasurement uomTemp = new UnitOfMeasurement("degree celcius", "°C", "ucum:T");

        createDatastream(sSrvc, "Datastream 0", "Datastream 1 of thing 0, sensor 0.", "someType", uomTemp, THINGS.get(0), SENSORS.get(0), O_PROPS.get(0), DATASTREAMS);
        createDatastream(sSrvc, "Datastream 1", "Datastream 2 of thing 0, sensor 1.", "someType", uomTemp, THINGS.get(0), SENSORS.get(1), O_PROPS.get(1), DATASTREAMS);
        createDatastream(sSrvc, "Datastream 2", "Datastream 3 of thing 0, sensor 2.", "someType", uomTemp, THINGS.get(0), SENSORS.get(2), O_PROPS.get(2), DATASTREAMS);
        createDatastream(sSrvc, "Datastream 3", "Datastream 1 of thing 1, sensor 0.", "someType", uomTemp, THINGS.get(1), SENSORS.get(0), O_PROPS.get(0), DATASTREAMS);
        createDatastream(sSrvc, "Datastream 4", "Datastream 2 of thing 1, sensor 1.", "someType", uomTemp, THINGS.get(1), SENSORS.get(1), O_PROPS.get(1), DATASTREAMS);
        createDatastream(sSrvc, "Datastream 5", "Datastream 3 of thing 1, sensor 3.", "someType", uomTemp, THINGS.get(1), SENSORS.get(3), O_PROPS.get(3), DATASTREAMS);
        createDatastream(sSrvc, "Datastream 6", "Datastream 1 of thing 2, sensor 3.", "someType", uomTemp, THINGS.get(2), SENSORS.get(1), O_PROPS.get(0), DATASTREAMS);

        ZonedDateTime startTime = ZonedDateTime.parse("2016-01-01T01:00:00.000Z");
        TimeInterval startInterval = TimeInterval.create(Instant.parse("2016-01-01T01:00:00.000Z"), Instant.parse("2016-01-01T02:00:00.000Z"));

        createObservationSet(sSrvc, DATASTREAMS.get(0), 0, startTime, startInterval, 6, OBSERVATIONS);
        createObservationSet(sSrvc, DATASTREAMS.get(1), 3, startTime, startInterval, 6, OBSERVATIONS);
        createObservationSet(sSrvc, DATASTREAMS.get(2), 6, startTime, startInterval, 6, OBSERVATIONS);
        createObservationSet(sSrvc, DATASTREAMS.get(3), 9, startTime, startInterval, 6, OBSERVATIONS);
        createObservationSet(sSrvc, DATASTREAMS.get(4), 12, startTime, startInterval, 6, OBSERVATIONS);
        createObservationSet(sSrvc, DATASTREAMS.get(5), 15, startTime, startInterval, 6, OBSERVATIONS);
    }

    /**
     * Test indirect/deep filter, across entity relations.
     *
     * @throws ServiceFailureException If the sSrvc doesn't respond.
     */
    @Test
    void testIndirectFilter() {
        LOGGER.info("  testIndirectFilter");
        Dao doa = sSrvc.dao(sMdl.etThing);
        testFilterResults(doa, "Locations/name eq 'Location 2'", getFromList(THINGS, 1));
        testFilterResults(doa, "startswith(HistoricalLocations/Locations/name, 'Location 1')", getFromList(THINGS, 0));
        testFilterResults(doa, "HistoricalLocations/Locations/properties/field eq properties/field", getFromList(THINGS, 1, 2));
    }

    /**
     * Test a back-and-forth indirect filter.
     *
     * @throws ServiceFailureException If the sSrvc doesn't respond.
     */
    @Test
    void testDeepIndirection() {
        LOGGER.info("  testDeepIndirection");
        Dao doa = sSrvc.dao(sMdl.etObservedProperty);

        testFilterResults(doa, "Datastreams/Thing/Datastreams/ObservedProperty/name eq 'ObservedProperty 0'", getFromList(O_PROPS, 0, 1, 2, 3));
        testFilterResults(doa, "Datastreams/Thing/Datastreams/ObservedProperty/name eq 'ObservedProperty 3'", getFromList(O_PROPS, 0, 1, 3));
    }

    /**
     * Test substring function.
     *
     * @throws ServiceFailureException If the sSrvc doesn't respond.
     */
    @Test
    void testSubString() {
        LOGGER.info("  testSubString");
        Dao doa = sSrvc.dao(sMdl.etThing);

        testFilterResults(doa, "substring(properties/string, 1) eq 'wo'", getFromList(THINGS, 2));
        testFilterResults(doa, "substring(properties/string, 1, 1) eq 'w'", getFromList(THINGS, 2));
        testFilterResults(doa, "substring(name, 4) eq 'g 4'", getFromList(THINGS, 3));
        testFilterResults(doa, "substring(name, 4, 3) eq 'g 4'", getFromList(THINGS, 3));
    }

    /**
     * Test equals null.
     *
     * @throws ServiceFailureException If the sSrvc doesn't respond.
     */
    @Test
    void testEqualsNull() {
        LOGGER.info("  testEqualsNull");
        Dao doa = sSrvc.dao(sMdl.etThing);

        testFilterResults(doa, "properties/field eq null", getFromList(THINGS, 0, 3));
        testFilterResults(doa, "Datastreams/id eq null", getFromList(THINGS, 3));
        testFilterResults(doa, "not properties/field ne null", getFromList(THINGS, 0, 3));
        testFilterResults(doa, "not Datastreams/id ne null", getFromList(THINGS, 3));
    }

    /**
     * Test not equals null.
     *
     * @throws ServiceFailureException If the sSrvc doesn't respond.
     */
    @Test
    void testNotEqualsNull() {
        LOGGER.info("  testNotEqualsNull");
        Dao doa = sSrvc.dao(sMdl.etThing);

        testFilterResults(doa, "properties/field ne null", getFromList(THINGS, 1, 2));
        testFilterResults(doa, "Datastreams/id ne null", getFromList(THINGS, 0, 1, 2));
        testFilterResults(doa, "not properties/field eq null", getFromList(THINGS, 1, 2));
        testFilterResults(doa, "not Datastreams/id eq null", getFromList(THINGS, 0, 1, 2));
    }

    /**
     * Test if fetching a property that is NULL returns a 204.
     */
    @Test
    void testNullEntityProperty() {
        LOGGER.info("  testNullEntityProperty");
        String requestUrl = serverSettings.getServiceUrl(version) + "/Things(" + formatKeyValuesForUrl(THINGS.get(0)) + ")/properties";
        HTTPMethods.HttpResponse result = HTTPMethods.doGet(requestUrl);
        if (result.code != 204) {
            fail("Expected response code 204 on request " + requestUrl);
        }
    }

    /**
     * Test if fetching the $value of a property that is NULL returns a 204.
     */
    @Test
    void testNullEntityPropertyValue() {
        LOGGER.info("  testNullEntityPropertyValue");
        String requestUrl = serverSettings.getServiceUrl(version) + "/Things(" + formatKeyValuesForUrl(THINGS.get(0)) + ")/properties/$value";
        HTTPMethods.HttpResponse result = HTTPMethods.doGet(requestUrl);
        if (result.code != 204) {
            fail("Expected response code 204 on request " + requestUrl);
        }
    }

    /**
     * Test if filtering works on requltQuality values that are Strings.
     */
    @Test
    void testStringResultQualityValue() {
        LOGGER.info("  testStringResultQualityValue");
        Dao doa = sSrvc.dao(sMdl.etObservation);
        testFilterResults(doa, "resultQuality eq 'number-1'", getFromList(OBSERVATIONS, 1));
    }

    /**
     * Test if filtering works on requltQuality values that are Numbers.
     */
    @Test
    void testNumericResultQualityValue() {
        LOGGER.info("  testNumericResultQualityValue");
        Dao doa = sSrvc.dao(sMdl.etObservation);
        testFilterResults(doa, "resultQuality eq 2", getFromList(OBSERVATIONS, 2));
    }

    /**
     * Test indirect/deep filter, across entity relations.
     *
     * @throws ServiceFailureException If the sSrvc doesn't respond.
     */
    @Test
    void testAnyFilter() {
        LOGGER.info("  testAnyFilter");
        Dao doa = sSrvc.dao(sMdl.etThing);
        testFilterResults(doa, "Locations/any(l:l/name eq 'Location 2')", getFromList(THINGS, 1));
        testFilterResults(doa, "not Locations/any(l:l/name ne 'Location 2')", getFromList(THINGS, 1));
        testFilterResults(doa, "HistoricalLocations/any(hl : hl/Locations/any(l : startswith(l/name, 'Location 1')))", getFromList(THINGS, 0));
        testFilterResults(doa, "HistoricalLocations/any(hl : hl/Locations/any(l : l/properties/field eq properties/field))", getFromList(THINGS, 1, 2));
        testFilterResults(doa, "Datastreams/any(d:d/ObservedProperty/name eq 'ObservedProperty 0') and Datastreams/any(d:d/ObservedProperty/name eq 'ObservedProperty 1')", getFromList(THINGS, 0, 1));
        testFilterResults(doa, "Datastreams/any(d:d/ObservedProperty/name eq 'ObservedProperty 3') and Datastreams/any(d:d/ObservedProperty/name eq 'ObservedProperty 1')", getFromList(THINGS, 1));
        testFilterResults(doa, "Datastreams/any(d:d/ObservedProperty/name eq 'ObservedProperty 0' and d/Sensor/name eq 'Sensor 1')", getFromList(THINGS, 2));
    }

}
