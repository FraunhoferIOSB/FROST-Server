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

import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsSensingV11.EP_PROPERTIES;
import static de.fraunhofer.iosb.ilt.frostclient.utils.CollectionsHelper.propertiesBuilder;
import static de.fraunhofer.iosb.ilt.frostclient.utils.ParserUtils.formatKeyValuesForUrl;
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
import de.fraunhofer.iosb.ilt.frostclient.models.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods;
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
 * Some odd tests.
 *
 * @author Hylke van der Schaaf
 */
public abstract class FilterTests extends AbstractTestClass {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FilterTests.class);

    private static final List<Entity> THINGS = new ArrayList<>();
    private static final List<Entity> LOCATIONS = new ArrayList<>();
    private static final List<Entity> SENSORS = new ArrayList<>();
    private static final List<Entity> O_PROPS = new ArrayList<>();
    private static final List<Entity> DATASTREAMS = new ArrayList<>();
    private static final List<Entity> OBSERVATIONS = new ArrayList<>();

    public FilterTests(ServerVersion version) {
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
        EntityUtils.deleteAll(service);
        THINGS.clear();
        LOCATIONS.clear();
        SENSORS.clear();
        O_PROPS.clear();
        DATASTREAMS.clear();
        OBSERVATIONS.clear();
    }

    private static void createEntities() throws ServiceFailureException, URISyntaxException {
        Entity thing = sMdl.newThing("Thing 1", "The first thing.");
        sSrvc.create(thing);
        THINGS.add(thing);

        thing = sMdl.newThing("Thing 2", "The second thing.")
                .setProperty(EP_PROPERTIES, propertiesBuilder().addItem("field", 2).build());
        sSrvc.create(thing);
        THINGS.add(thing);

        thing = sMdl.newThing("Thing 3", "The third thing.")
                .setProperty(EP_PROPERTIES, propertiesBuilder().addItem("field", 3).build());
        sSrvc.create(thing);
        THINGS.add(thing);

        thing = sMdl.newThing("Thing 4", "The fourth thing.");
        sSrvc.create(thing);
        THINGS.add(thing);

        // Locations 0
        Entity location = sMdl.newLocation("Location 1.0", "First Location of Thing 1.", "application/vnd.geo+json", new Point(8, 51))
                .setProperty(EP_PROPERTIES, propertiesBuilder().addItem("field", 1).build())
                .addNavigationEntity(sMdl.npLocationThings, THINGS.get(0));
        sSrvc.create(location);
        LOCATIONS.add(location);

        // Locations 1
        location = sMdl.newLocation("Location 1.1", "Second Location of Thing 1.", "application/vnd.geo+json", new Point(8, 52))
                .setProperty(EP_PROPERTIES, propertiesBuilder().addItem("field", 1.1).build())
                .addNavigationEntity(sMdl.npLocationThings, THINGS.get(0));
        sSrvc.create(location);
        LOCATIONS.add(location);

        // Locations 2
        location = sMdl.newLocation("Location 2", "Location of Thing 2.", "application/vnd.geo+json", new Point(8, 53))
                .setProperty(EP_PROPERTIES, propertiesBuilder().addItem("field", 2).build())
                .addNavigationEntity(sMdl.npLocationThings, THINGS.get(1));
        sSrvc.create(location);
        LOCATIONS.add(location);

        // Locations 3
        location = sMdl.newLocation("Location 3", "Location of Thing 3.", "application/vnd.geo+json", new Point(8, 54))
                .setProperty(EP_PROPERTIES, propertiesBuilder().addItem("field", 3).build())
                .addNavigationEntity(sMdl.npLocationThings, THINGS.get(2));
        sSrvc.create(location);
        LOCATIONS.add(location);

        // Locations 4
        location = sMdl.newLocation("Location 4", "Location of Thing 4.", "application/vnd.geo+json",
                new Polygon(
                        new LngLatAlt(8, 53),
                        new LngLatAlt(7, 52),
                        new LngLatAlt(7, 53),
                        new LngLatAlt(8, 53)))
                .setProperty(EP_PROPERTIES, propertiesBuilder().addItem("field", 4).build())
                .addNavigationEntity(sMdl.npLocationThings, THINGS.get(3));
        sSrvc.create(location);
        LOCATIONS.add(location);

        // Locations 5
        location = sMdl.newLocation("Location 5", "A line.", "application/vnd.geo+json",
                new LineString(
                        new LngLatAlt(5, 52),
                        new LngLatAlt(5, 53)))
                .setProperty(EP_PROPERTIES, propertiesBuilder().addItem("field", 5).build());
        sSrvc.create(location);
        LOCATIONS.add(location);

        // Locations 6
        location = sMdl.newLocation("Location 6", "A longer line.", "application/vnd.geo+json",
                new LineString(
                        new LngLatAlt(5, 52),
                        new LngLatAlt(6, 53)))
                .setProperty(EP_PROPERTIES, propertiesBuilder().addItem("field", 6).build());
        sSrvc.create(location);
        LOCATIONS.add(location);

        // Locations 7
        location = sMdl.newLocation("Location 7", "The longest line.", "application/vnd.geo+json",
                new LineString(
                        new LngLatAlt(4, 52),
                        new LngLatAlt(8, 52)))
                .setProperty(EP_PROPERTIES, propertiesBuilder().addItem("field", 7).build());
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
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    void testIndirectFilter() throws ServiceFailureException {
        LOGGER.info("  testIndirectFilter");
        Dao doa = sSrvc.dao(sMdl.etThing);
        testFilterResults(doa, "Locations/name eq 'Location 2'", getFromList(THINGS, 1));
        testFilterResults(doa, "startswith(HistoricalLocations/Locations/name, 'Location 1')", getFromList(THINGS, 0));
        testFilterResults(doa, "HistoricalLocations/Locations/properties/field eq properties/field", getFromList(THINGS, 1, 2));
    }

    /**
     * Test a back-and-forth indirect filter.
     *
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    void testDeepIndirection() throws ServiceFailureException {
        LOGGER.info("  testDeepIndirection");
        Dao doa = sSrvc.dao(sMdl.etObservedProperty);

        testFilterResults(doa, "Datastreams/Thing/Datastreams/ObservedProperty/name eq 'ObservedProperty 0'", getFromList(O_PROPS, 0, 1, 2, 3));
        testFilterResults(doa, "Datastreams/Thing/Datastreams/ObservedProperty/name eq 'ObservedProperty 3'", getFromList(O_PROPS, 0, 1, 3));
    }

    /**
     * Test equals null.
     *
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    void testEqualsNull() throws ServiceFailureException {
        LOGGER.info("  testEqualsNull");
        Dao doa = sSrvc.dao(sMdl.etThing);

        testFilterResults(doa, "properties/field eq null", getFromList(THINGS, 0, 3));
        testFilterResults(doa, "Datastreams/id eq null", getFromList(THINGS, 3));
    }

    /**
     * Test not equals null.
     *
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    void testNotEqualsNull() throws ServiceFailureException {
        LOGGER.info("  testNotEqualsNull");
        Dao doa = sSrvc.dao(sMdl.etThing);

        testFilterResults(doa, "properties/field ne null", getFromList(THINGS, 1, 2));
        testFilterResults(doa, "Datastreams/id ne null", getFromList(THINGS, 0, 1, 2));
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
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    void testAnyFilter() throws ServiceFailureException {
        LOGGER.info("  testAnyFilter");
        Dao doa = sSrvc.dao(sMdl.etThing);
        testFilterResults(doa, "Locations/any(l:l/name eq 'Location 2')", getFromList(THINGS, 1));
        testFilterResults(doa, "HistoricalLocations/any(hl : hl/Locations/any(l : startswith(l/name, 'Location 1')))", getFromList(THINGS, 0));
        testFilterResults(doa, "HistoricalLocations/any(hl : hl/Locations/any(l : l/properties/field eq properties/field))", getFromList(THINGS, 1, 2));
        testFilterResults(doa, "Datastreams/any(d:d/ObservedProperty/name eq 'ObservedProperty 0') and Datastreams/any(d:d/ObservedProperty/name eq 'ObservedProperty 1')", getFromList(THINGS, 0, 1));
        testFilterResults(doa, "Datastreams/any(d:d/ObservedProperty/name eq 'ObservedProperty 3') and Datastreams/any(d:d/ObservedProperty/name eq 'ObservedProperty 1')", getFromList(THINGS, 1));
        testFilterResults(doa, "Datastreams/any(d:d/ObservedProperty/name eq 'ObservedProperty 0' and d/Sensor/name eq 'Sensor 1')", getFromList(THINGS, 2));
    }

}
