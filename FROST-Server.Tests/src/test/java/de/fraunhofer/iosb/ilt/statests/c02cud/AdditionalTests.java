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
package de.fraunhofer.iosb.ilt.statests.c02cud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.dao.ObservationDao;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.FeatureOfInterest;
import de.fraunhofer.iosb.ilt.sta.model.HistoricalLocation;
import de.fraunhofer.iosb.ilt.sta.model.Location;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.builder.HistoricalLocationBuilder;
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.geojson.Point;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests additional details not part of the official tests.
 *
 * @author Hylke van der Schaaf
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public abstract class AdditionalTests extends AbstractTestClass {

    public static class Implementation10 extends AdditionalTests {

        public Implementation10() {
            super(ServerVersion.v_1_0);
        }

    }

    public static class Implementation11 extends AdditionalTests {

        public Implementation11() {
            super(ServerVersion.v_1_1);
        }

    }

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AdditionalTests.class);

    private static final List<Thing> THINGS = new ArrayList<>();
    private static final List<Datastream> DATASTREAMS = new ArrayList<>();
    private static final List<Observation> OBSERVATIONS = new ArrayList<>();

    public AdditionalTests(ServerVersion version) {
        super(version);
    }

    @Override
    protected void setUpVersion() {
        LOGGER.info("Setting up for version {}.", version.urlPart);
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
        DATASTREAMS.clear();
        OBSERVATIONS.clear();
    }

    /**
     * Check the creation of a FoI on Observation creation, for Things that have
     * multiple Locations, only one of which is a geoJson location.
     *
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    void test01MultipleLocations() throws ServiceFailureException {
        LOGGER.info("  test01MultipleLocations");
        EntityUtils.deleteAll(version, serverSettings, service);

        Thing thing = new Thing("Thing 1", "The first thing.");

        Location location1 = new Location("Location 1.0, Address", "The address of Thing 1.", "text/plain", "");
        thing.getLocations().add(location1);
        Location location2 = new Location("Location 1.0", "Location of Thing 1.", "application/geo+json", new Point(8, 51));
        thing.getLocations().add(location2);
        Location location3 = new Location("Location 1.0, Directions", "How to find Thing 1 in human language.", "text/plain", "");
        thing.getLocations().add(location3);

        service.create(thing);
        THINGS.add(thing);

        Sensor sensor = new Sensor("Sensor 1", "The first sensor.", "text", "Some metadata.");
        ObservedProperty obsProp = new ObservedProperty("Temperature", "http://ucom.org/temperature", "The temperature of the thing.");
        Datastream datastream = new Datastream("Datastream 1", "The temperature of thing 1, sensor 1.", "someType", new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
        datastream.setSensor(sensor);
        datastream.setObservedProperty(obsProp);
        datastream.setThing(thing);

        service.create(datastream);
        DATASTREAMS.add(datastream);

        ObservationDao doa = service.observations();
        Observation observation = new Observation(1.0, DATASTREAMS.get(0));
        doa.create(observation);
        OBSERVATIONS.add(observation);

        Observation found;
        found = doa.find(observation.getId());
        FeatureOfInterest featureOfInterest = found.getFeatureOfInterest();

        assertNotNull(featureOfInterest, "A FeatureOfInterest should have been generated, but got NULL.");
    }

    @Test
    void test02GeneratePhenomenonTime() throws ServiceFailureException {
        LOGGER.info("  test02GeneratePhenomenonTime");
        ObservationDao doa = service.observations();
        Observation observation = new Observation(1.0, DATASTREAMS.get(0));
        doa.create(observation);
        OBSERVATIONS.add(observation);

        Observation found;
        found = doa.find(observation.getId());
        assertNotNull(found.getPhenomenonTime(), "phenomenonTime should be auto generated.");
    }

    /**
     * Check if adding a new HistoricalLocation to a Thing changes the Location
     * of the Thing, if the new HistoricalLocation has a time that is later than
     * all others of the same Thing.
     *
     * Check if adding a new HistoricalLocation to a Thing does not change the
     * Location of the Thing, if the new HistoricalLocation has a time that is
     * not later than all others of the same Thing.
     *
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    void test03HistoricalLocationThing() throws ServiceFailureException {
        LOGGER.info("  test03HistoricalLocationThing");
        EntityUtils.deleteAll(version, serverSettings, service);

        // Create a thing
        Thing thing = new Thing("Thing 1", "The first thing.");
        service.create(thing);

        // Create three locations.
        Location location1 = new Location("Location 1.0", "Location Number 1.", "application/vnd.geo+json", new Point(8, 50));
        Location location2 = new Location("Location 2.0", "Location Number 2.", "application/vnd.geo+json", new Point(8, 51));
        Location location3 = new Location("Location 3.0", "Location Number 3.", "application/vnd.geo+json", new Point(8, 52));
        service.create(location1);
        service.create(location2);
        service.create(location3);

        // Give the Thing location 1
        thing.getLocations().add(location1.withOnlyId());
        service.update(thing);

        // Get the generated HistoricalLocation and change the time to a known value.
        List<HistoricalLocation> histLocations = thing.historicalLocations().query().list().toList();

        assertEquals(1, histLocations.size(), "Incorrect number of HistoricalLocations for Thing.");

        HistoricalLocation histLocation = histLocations.get(0);
        histLocation.setTime(ZonedDateTime.parse("2016-01-01T06:00:00.000Z"));
        service.update(histLocation);

        // Now create a new HistoricalLocation for the Thing, with a later time.
        HistoricalLocation histLocation2 = HistoricalLocationBuilder.builder()
                .location(location2)
                .time(ZonedDateTime.parse("2016-01-01T07:00:00.000Z"))
                .thing(thing.withOnlyId())
                .build();
        service.create(histLocation2);

        // Check if the Location of the Thing is now Location 2.
        List<Location> thingLocations = thing.locations().query().list().toList();

        assertEquals(1, thingLocations.size(), "Incorrect number of Locations for Thing.");

        assertEquals(location2, thingLocations.get(0));

        // Now create a new HistoricalLocation for the Thing, with an earlier time.
        HistoricalLocation histLocation3 = HistoricalLocationBuilder.builder()
                .location(location3)
                .time(ZonedDateTime.parse("2016-01-01T05:00:00.000Z"))
                .thing(thing.withOnlyId())
                .build();
        service.create(histLocation3);

        // Check if the Location of the Thing is still Location 2.
        thingLocations = thing.locations().query().list().toList();

        assertEquals(1, thingLocations.size(), "Incorrect number of Locations for Thing.");

        assertEquals(location2, thingLocations.get(0));
    }

    /**
     * Tests requests on paths like Things(x)/Datastreams(y)/Observations, where
     * Datastream(y) exists, but is not part of the, also existing, Things(x).
     *
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    void test04PostInvalidPath() throws ServiceFailureException {
        LOGGER.info("  test04PostInvalidPath");
        EntityUtils.deleteAll(version, serverSettings, service);
        // Create two things

        Location location1 = new Location("LocationThing1", "Location of Thing 1", "application/geo+json", new Point(8, 50));
        service.create(location1);

        Thing thing1 = new Thing("Thing 1", "The first thing.");
        thing1.getLocations().add(location1.withOnlyId());
        service.create(thing1);

        Thing thing2 = new Thing("Thing 2", "The second thing.");
        thing2.getLocations().add(location1.withOnlyId());
        service.create(thing2);

        Sensor sensor1 = new Sensor("Test Thermometre", "Test Sensor", "None", "-");
        service.create(sensor1);

        ObservedProperty obsProp1 = new ObservedProperty("Temperature", "http://example.org", "-");
        service.create(obsProp1);

        Datastream datastream1 = new Datastream("Ds 1, Thing 1", "The datastream of Thing 1", "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement", new UnitOfMeasurement("Degrees Celcius", "°C", "http://qudt.org/vocab/unit#DegreeCelsius"));
        datastream1.setThing(thing1);
        datastream1.setSensor(sensor1);
        datastream1.setObservedProperty(obsProp1);
        service.create(datastream1);

        Observation obs1 = new Observation(1.0, datastream1);
        service.create(obs1);

        testGet(thing1, datastream1, thing2);

        // PUT tests
        String urlObsGood = serverSettings.getServiceUrl(version)
                + "/Things(" + thing1.getId().getUrl() + ")"
                + "/Datastreams(" + datastream1.getId().getUrl() + ")"
                + "/Observations(" + obs1.getId().getUrl() + ")";
        String urlObsBad = serverSettings.getServiceUrl(version)
                + "/Things(" + thing2.getId().getUrl() + ")"
                + "/Datastreams(" + datastream1.getId().getUrl() + ")"
                + "/Observations(" + obs1.getId().getUrl() + ")";

        testPut(urlObsGood, urlObsBad);
        testPatch(urlObsGood, urlObsBad);
        testDelete(urlObsBad, urlObsGood);
    }

    private void testGet(Thing thing1, Datastream datastream1, Thing thing2) {
        // GET tests
        HTTPMethods.HttpResponse response;
        String url = serverSettings.getServiceUrl(version) + "/Things(" + thing1.getId().getUrl() + ")/Datastreams(" + datastream1.getId().getUrl() + ")/Observations";
        response = HTTPMethods.doGet(url);
        assertEquals(200, response.code, "Get should return 201 Created for url " + url);

        url = serverSettings.getServiceUrl(version) + "/Things(" + thing2.getId().getUrl() + ")/Datastreams(" + datastream1.getId().getUrl() + ")/Observations";
        response = HTTPMethods.doGet(url);
        assertEquals(404, response.code, "Get should return 404 Not Found for url " + url);

        // POST tests
        url = serverSettings.getServiceUrl(version) + "/Things(" + thing1.getId().getUrl() + ")/Datastreams(" + datastream1.getId().getUrl() + ")/Observations";
        String observationJson = "{\n"
                + "  \"phenomenonTime\": \"2015-03-01T03:00:00.000Z\",\n"
                + "  \"result\": 300\n"
                + "}";
        response = HTTPMethods.doPost(url, observationJson);
        assertEquals(201, response.code, "Post should return 201 Created for url " + url);

        url = serverSettings.getServiceUrl(version) + "/Things(" + thing2.getId().getUrl() + ")/Datastreams(" + datastream1.getId().getUrl() + ")/Observations";
        response = HTTPMethods.doPost(url, observationJson);
        assertNotEquals(201, response.code, "Post should not return 201 Created for url " + url);
    }

    private void testPut(String urlObsGood, String urlObsBad) {
        String observationJson;
        HTTPMethods.HttpResponse response;
        observationJson = "{\n"
                + "  \"phenomenonTime\": \"2015-03-01T03:00:00.000Z\",\n"
                + "  \"result\": 301\n"
                + "}";
        response = HTTPMethods.doPut(urlObsGood, observationJson);
        assertEquals(200, response.code, "Post should return 200 Ok for url " + urlObsGood);
        observationJson = "{\n"
                + "  \"phenomenonTime\": \"2015-03-01T03:00:00.000Z\",\n"
                + "  \"result\": 302\n"
                + "}";
        response = HTTPMethods.doPut(urlObsBad, observationJson);
        assertEquals(404, response.code, "Post should return 404 Not Found for url " + urlObsBad);
    }

    private void testPatch(String urlObsGood, String urlObsBad) {
        String observationJson;
        HTTPMethods.HttpResponse response;
        // PATCH tests
        observationJson = "{\n"
                + "  \"result\": 303\n"
                + "}";
        response = HTTPMethods.doPatch(urlObsGood, observationJson);
        assertEquals(200, response.code, "Post should return 200 Ok for url " + urlObsGood);
        observationJson = "{\n"
                + "  \"result\": 304\n"
                + "}";
        response = HTTPMethods.doPatch(urlObsBad, observationJson);
        assertNotEquals(200, response.code, "Post should not return 200 Ok for url " + urlObsBad);
    }

    private void testDelete(String urlObsBad, String urlObsGood) {
        HTTPMethods.HttpResponse response;
        // DELETE tests
        response = HTTPMethods.doDelete(urlObsBad);
        assertEquals(404, response.code, "Post should return 404 Not Found for url " + urlObsBad);
        response = HTTPMethods.doGet(urlObsGood);
        assertEquals(200, response.code, "Get should return 200 Ok for url " + urlObsGood);
        response = HTTPMethods.doDelete(urlObsGood);
        assertEquals(200, response.code, "Post should return 200 Ok for url " + urlObsGood);
        response = HTTPMethods.doGet(urlObsGood);
        assertEquals(404, response.code, "Get should return 404 Not Found for url " + urlObsGood);
    }

    @Test
    void test05RecreateAutomaticFoi() throws ServiceFailureException {
        LOGGER.info("  test05RecreateAutomaticFoi");
        EntityUtils.deleteAll(version, serverSettings, service);
        // Create two things

        Location location1 = new Location("LocationThing1", "Location of Thing 1", "application/geo+json", new Point(8, 50));
        service.create(location1);

        Thing thing1 = new Thing("Thing 1", "The first thing.");
        thing1.getLocations().add(location1.withOnlyId());
        service.create(thing1);

        Sensor sensor1 = new Sensor("Test Thermometre", "Test Sensor", "None", "-");
        service.create(sensor1);

        ObservedProperty obsProp1 = new ObservedProperty("Temperature", "http://example.org", "-");
        service.create(obsProp1);

        Datastream datastream1 = new Datastream("Ds 1, Thing 1", "The datastream of Thing 1", "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement", new UnitOfMeasurement("Degrees Celcius", "°C", "http://qudt.org/vocab/unit#DegreeCelsius"));
        datastream1.setThing(thing1);
        datastream1.setSensor(sensor1);
        datastream1.setObservedProperty(obsProp1);
        service.create(datastream1);

        Observation obs1 = new Observation(1.0, datastream1);
        service.create(obs1);

        FeatureOfInterest foiGenerated1 = service.observations().find(obs1.getId()).getFeatureOfInterest();
        assertNotNull(foiGenerated1);

        service.delete(foiGenerated1);

        Observation obs2 = new Observation(1.0, datastream1);
        service.create(obs2);

        FeatureOfInterest foiGenerated2 = service.observations().find(obs2.getId()).getFeatureOfInterest();
        assertNotNull(foiGenerated2);

        assertNotEquals(foiGenerated1, foiGenerated2);
    }
}
