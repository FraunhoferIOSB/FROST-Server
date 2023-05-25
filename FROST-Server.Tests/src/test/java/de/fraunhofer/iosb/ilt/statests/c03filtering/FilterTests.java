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

import static de.fraunhofer.iosb.ilt.statests.util.EntityUtils.testFilterResults;
import static de.fraunhofer.iosb.ilt.statests.util.Utils.getFromList;
import static org.junit.jupiter.api.Assertions.fail;

import de.fraunhofer.iosb.ilt.frostserver.util.CollectionsHelper;
import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.dao.ObservationDao;
import de.fraunhofer.iosb.ilt.sta.dao.ObservedPropertyDao;
import de.fraunhofer.iosb.ilt.sta.dao.ThingDao;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.Location;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.geojson.Polygon;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.extra.Interval;

/**
 * Some odd tests.
 *
 * @author Hylke van der Schaaf
 */
public abstract class FilterTests extends AbstractTestClass {

    public static class Implementation10 extends FilterTests {

        public Implementation10() {
            super(ServerVersion.v_1_0);
        }

    }

    public static class Implementation11 extends FilterTests {

        public Implementation11() {
            super(ServerVersion.v_1_1);
        }

    }

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FilterTests.class);

    private static final List<Thing> THINGS = new ArrayList<>();
    private static final List<Location> LOCATIONS = new ArrayList<>();
    private static final List<Sensor> SENSORS = new ArrayList<>();
    private static final List<ObservedProperty> O_PROPS = new ArrayList<>();
    private static final List<Datastream> DATASTREAMS = new ArrayList<>();
    private static final List<Observation> OBSERVATIONS = new ArrayList<>();

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
        EntityUtils.deleteAll(version, serverSettings, service);
        THINGS.clear();
        LOCATIONS.clear();
        SENSORS.clear();
        O_PROPS.clear();
        DATASTREAMS.clear();
        OBSERVATIONS.clear();
    }

    private static void createEntities() throws ServiceFailureException, URISyntaxException {
        Thing thing = new Thing("Thing 1", "The first thing.");
        service.create(thing);
        THINGS.add(thing);

        thing = new Thing("Thing 2", "The second thing.");
        thing.setProperties(CollectionsHelper.propertiesBuilder().addProperty("field", 2).build());
        service.create(thing);
        THINGS.add(thing);

        thing = new Thing("Thing 3", "The third thing.");
        thing.setProperties(CollectionsHelper.propertiesBuilder().addProperty("field", 3).build());
        service.create(thing);
        THINGS.add(thing);

        thing = new Thing("Thing 4", "The fourth thing.");
        service.create(thing);
        THINGS.add(thing);

        // Locations 0
        Location location = new Location("Location 1.0", "First Location of Thing 1.", "application/vnd.geo+json", new Point(8, 51));
        location.getThings().add(THINGS.get(0));
        service.create(location);
        LOCATIONS.add(location);

        // Locations 1
        location = new Location("Location 1.1", "Second Location of Thing 1.", "application/vnd.geo+json", new Point(8, 52));
        location.getThings().add(THINGS.get(0));
        service.create(location);
        LOCATIONS.add(location);

        // Locations 2
        location = new Location("Location 2", "Location of Thing 2.", "application/vnd.geo+json", new Point(8, 53));
        location.getThings().add(THINGS.get(1));
        service.create(location);
        LOCATIONS.add(location);

        // Locations 3
        location = new Location("Location 3", "Location of Thing 3.", "application/vnd.geo+json", new Point(8, 54));
        location.getThings().add(THINGS.get(2));
        service.create(location);
        LOCATIONS.add(location);

        // Locations 4
        location = new Location("Location 4", "Location of Thing 4.", "application/vnd.geo+json",
                new Polygon(
                        new LngLatAlt(8, 53),
                        new LngLatAlt(7, 52),
                        new LngLatAlt(7, 53),
                        new LngLatAlt(8, 53)));
        location.getThings().add(THINGS.get(3));
        service.create(location);
        LOCATIONS.add(location);

        // Locations 5
        location = new Location("Location 5", "A line.", "application/vnd.geo+json",
                new LineString(
                        new LngLatAlt(5, 52),
                        new LngLatAlt(5, 53)));
        service.create(location);
        LOCATIONS.add(location);

        // Locations 6
        location = new Location("Location 6", "A longer line.", "application/vnd.geo+json",
                new LineString(
                        new LngLatAlt(5, 52),
                        new LngLatAlt(6, 53)));
        service.create(location);
        LOCATIONS.add(location);

        // Locations 7
        location = new Location("Location 7", "The longest line.", "application/vnd.geo+json",
                new LineString(
                        new LngLatAlt(4, 52),
                        new LngLatAlt(8, 52)));
        service.create(location);
        LOCATIONS.add(location);

        createSensor("Sensor 0", "The sensor with idx 0.", "text", "Some metadata.");
        createSensor("Sensor 1", "The sensor with idx 0.", "text", "Some metadata.");
        createSensor("Sensor 2", "The sensor with idx 0.", "text", "Some metadata.");
        createSensor("Sensor 3", "The sensor with idx 0.", "text", "Some metadata.");

        createObservedProperty("ObservedProperty 0", new URI("http://ucom.org/temperature"), "ObservedProperty with index 0.");
        createObservedProperty("ObservedProperty 1", new URI("http://ucom.org/humidity"), "ObservedProperty with index 1.");
        createObservedProperty("ObservedProperty 2", new URI("http://ucom.org/pressure"), "ObservedProperty with index 2.");
        createObservedProperty("ObservedProperty 3", new URI("http://ucom.org/turbidity"), "ObservedProperty with index 3.");

        UnitOfMeasurement uomTemp = new UnitOfMeasurement("degree celcius", "°C", "ucum:T");

        createDatastream("Datastream 0", "Datastream 1 of thing 0, sensor 0.", "someType", uomTemp, THINGS.get(0), SENSORS.get(0), O_PROPS.get(0));
        createDatastream("Datastream 1", "Datastream 2 of thing 0, sensor 1.", "someType", uomTemp, THINGS.get(0), SENSORS.get(1), O_PROPS.get(1));
        createDatastream("Datastream 2", "Datastream 3 of thing 0, sensor 2.", "someType", uomTemp, THINGS.get(0), SENSORS.get(2), O_PROPS.get(2));
        createDatastream("Datastream 3", "Datastream 1 of thing 1, sensor 0.", "someType", uomTemp, THINGS.get(1), SENSORS.get(0), O_PROPS.get(0));
        createDatastream("Datastream 4", "Datastream 2 of thing 1, sensor 1.", "someType", uomTemp, THINGS.get(1), SENSORS.get(1), O_PROPS.get(1));
        createDatastream("Datastream 5", "Datastream 3 of thing 1, sensor 3.", "someType", uomTemp, THINGS.get(1), SENSORS.get(3), O_PROPS.get(3));

        ZonedDateTime startTime = ZonedDateTime.parse("2016-01-01T01:00:00.000Z");
        Interval startInterval = Interval.of(Instant.parse("2016-01-01T01:00:00.000Z"), Instant.parse("2016-01-01T02:00:00.000Z"));

        createObservationSet(DATASTREAMS.get(0), 0, startTime, startInterval, 6);
        createObservationSet(DATASTREAMS.get(1), 3, startTime, startInterval, 6);
        createObservationSet(DATASTREAMS.get(2), 6, startTime, startInterval, 6);
        createObservationSet(DATASTREAMS.get(3), 9, startTime, startInterval, 6);
        createObservationSet(DATASTREAMS.get(4), 12, startTime, startInterval, 6);
        createObservationSet(DATASTREAMS.get(5), 15, startTime, startInterval, 6);

    }

    private static Sensor createSensor(String name, String desc, String type, String metadata) throws ServiceFailureException {
        int idx = SENSORS.size();
        Map<String, Object> properties = new HashMap<>();
        properties.put("idx", idx);

        Sensor sensor = new Sensor(name, desc, type, metadata);
        sensor.setProperties(properties);
        service.create(sensor);
        SENSORS.add(sensor);
        return sensor;
    }

    private static Datastream createDatastream(String name, String desc, String type, UnitOfMeasurement uom, Thing thing, Sensor sensor, ObservedProperty op) throws ServiceFailureException {
        int idx = DATASTREAMS.size();
        Map<String, Object> properties = new HashMap<>();
        properties.put("idx", idx);

        Datastream ds = new Datastream(name, desc, type, uom);
        ds.setProperties(properties);
        ds.setThing(thing);
        ds.setSensor(sensor);
        ds.setObservedProperty(op);
        service.create(ds);
        DATASTREAMS.add(ds);
        return ds;
    }

    private static ObservedProperty createObservedProperty(String name, URI definition, String description) throws ServiceFailureException {
        int idx = O_PROPS.size();
        Map<String, Object> properties = new HashMap<>();
        properties.put("idx", idx);
        ObservedProperty obsProp = new ObservedProperty(name, definition, description);
        obsProp.setProperties(properties);
        service.create(obsProp);
        O_PROPS.add(obsProp);
        return obsProp;
    }

    private static void createObservationSet(Datastream datastream, long resultStart, ZonedDateTime phenomenonTimeStart, Interval validTimeStart, int count) throws ServiceFailureException {
        for (int i = 0; i < count; i++) {
            ZonedDateTime phenTime = phenomenonTimeStart.plus(i, ChronoUnit.HOURS);
            Interval validTime = Interval.of(
                    validTimeStart.getStart().plus(count, ChronoUnit.HOURS),
                    validTimeStart.getEnd().plus(count, ChronoUnit.HOURS));
            createObservation(datastream, resultStart + i, phenTime, validTime);
        }
    }

    private static Observation createObservation(Datastream datastream, long result, ZonedDateTime phenomenonTime, Interval validTime) throws ServiceFailureException {
        int idx = OBSERVATIONS.size();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("idx", idx);
        Observation obs = new Observation(result, datastream);
        obs.setPhenomenonTimeFrom(phenomenonTime);
        obs.setValidTime(validTime);
        obs.setParameters(parameters);
        if (idx % 2 == 0) {
            obs.setResultQuality(idx);
        } else {
            obs.setResultQuality("number-" + idx);
        }
        service.create(obs);
        OBSERVATIONS.add(obs);
        return obs;
    }

    /**
     * Test indirect/deep filter, across entity relations.
     *
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    void testIndirectFilter() throws ServiceFailureException {
        LOGGER.info("  testIndirectFilter");
        ThingDao doa = service.things();
        testFilterResults(doa, "Locations/name eq 'Location 2'", getFromList(THINGS, 1));
        testFilterResults(doa, "startswith(HistoricalLocations/Locations/name, 'Location 1')", getFromList(THINGS, 0));
    }

    /**
     * Test a back-and-forth indirect filter.
     *
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    void testDeepIndirection() throws ServiceFailureException {
        LOGGER.info("  testDeepIndirection");
        ObservedPropertyDao doa = service.observedProperties();

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
        ThingDao doa = service.things();

        testFilterResults(doa, "properties/field eq null", getFromList(THINGS, 0, 3));
        testFilterResults(doa, "Datastreams/id eq null", getFromList(THINGS, 2, 3));
    }

    /**
     * Test not equals null.
     *
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    void testNotEqualsNull() throws ServiceFailureException {
        LOGGER.info("  testNotEqualsNull");
        ThingDao doa = service.things();

        testFilterResults(doa, "properties/field ne null", getFromList(THINGS, 1, 2));
        testFilterResults(doa, "Datastreams/id ne null", getFromList(THINGS, 0, 1));
    }

    /**
     * Test if fetching a property that is NULL returns a 204.
     */
    @Test
    void testNullEntityProperty() {
        LOGGER.info("  testNullEntityProperty");
        String requestUrl = serverSettings.getServiceUrl(version) + "/Things(" + THINGS.get(0).getId().getUrl() + ")/properties";
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
        String requestUrl = serverSettings.getServiceUrl(version) + "/Things(" + THINGS.get(0).getId().getUrl() + ")/properties/$value";
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
        ObservationDao doa = service.observations();
        testFilterResults(doa, "resultQuality eq 'number-1'", getFromList(OBSERVATIONS, 1));
    }

    /**
     * Test if filtering works on requltQuality values that are Numbers.
     */
    @Test
    void testNumericResultQualityValue() {
        LOGGER.info("  testNumericResultQualityValue");
        ObservationDao doa = service.observations();
        testFilterResults(doa, "resultQuality eq 2", getFromList(OBSERVATIONS, 2));
    }

}
