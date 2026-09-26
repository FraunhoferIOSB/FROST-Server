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
package de.fraunhofer.iosb.ilt.statests.c02cud;

import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing.EP_PHENOMENONTIME;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing.EP_TIME;
import static de.fraunhofer.iosb.ilt.frostclient.utils.StringHelper.formatKeyValuesForUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.fraunhofer.iosb.ilt.frostclient.dao.Dao;
import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.exception.StatusCodeException;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods;
import de.fraunhofer.iosb.ilt.statests.util.ServiceUrlHelper;
import de.fraunhofer.iosb.ilt.statests.util.model.EntityType;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.geojson.Point;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests additional details not part of the official tests.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public abstract class AdditionalTests extends AbstractTestClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdditionalTests.class);

    private static final List<Entity> THINGS = new ArrayList<>();
    private static final List<Entity> DATASTREAMS = new ArrayList<>();
    private static final List<Entity> OBSERVATIONS = new ArrayList<>();
    private static SensorThingsV11Sensing sMdl;

    public AdditionalTests(ServerVersion version) {
        super(version);
    }

    @Override
    protected void setUpVersion() {
        LOGGER.info("Setting up for version {}.", version.urlPart);
        sMdl = sSrvc.getModel(SensorThingsV11Sensing.class);
    }

    public static void cleanup() {
        EntityUtils.deleteAll(sSrvc);
        THINGS.clear();
        DATASTREAMS.clear();
        OBSERVATIONS.clear();
        AbstractTestClass.cleanup();
    }

    /**
     * Check the creation of a FoI on Observation creation, for Things that have
     * multiple Locations, only one of which is a geoJson location.
     *
     * @throws ServiceFailureException If the sSrvc doesn't respond.
     */
    @Test
    void test01MultipleLocations() throws ServiceFailureException {
        LOGGER.info("  test01MultipleLocations");
        EntityUtils.deleteAll(sSrvc);

        Entity location1 = sMdl.buildLocation()
                .setName("Location 1.0, Address")
                .setDescription("The address of Thing 1.")
                .setEncodingType("text/plain")
                .setLocation("Street Lane 1, City of Townsville")
                .build();
        Entity location2 = sMdl.buildLocation()
                .setName("Location 1.0")
                .setDescription("Location of Thing 1.")
                .setEncodingType("application/geo+json")
                .setLocation(new Point(8, 51))
                .build();
        Entity location3 = sMdl.buildLocation()
                .setName("Location 1.0, Directions")
                .setDescription("How to find Thing 1 in human language.")
                .setEncodingType("text/plain")
                .setLocation("Third rock from the Sun")
                .build();
        Entity thing = sMdl.buildThing()
                .setName("Thing 1")
                .setDescription("The first thing.")
                .addLocation(location1)
                .addLocation(location2)
                .addLocation(location3)
                .build();

        sSrvc.create(thing);
        THINGS.add(thing);

        Entity sensor = sMdl.buildSensor()
                .setName("Sensor 1")
                .setDescription("The first sensor.")
                .setEncodingType("text")
                .setMetadata("Some metadata.")
                .build();
        Entity obsProp = sMdl.buildObservedProperty()
                .setName("Temperature")
                .setDefinition("http://ucom.org/temperature")
                .setDescription("The temperature of the thing.")
                .build();
        Entity datastream = sMdl.buildDatastream()
                .setName("Datastream 1")
                .setDescription("The temperature of thing 1, sensor 1.")
                .setUnitOfMeasurement(new UnitOfMeasurement("degree celcius", "°C", "ucum:T"))
                .setSensor(sensor)
                .setObservedProperty(obsProp)
                .setThing(thing.withOnlyPk())
                .build();

        sSrvc.create(datastream);
        DATASTREAMS.add(datastream);

        Dao doa = sSrvc.dao(sMdl.etObservation);
        Entity observation = sMdl.buildObservation()
                .setResult(1.0)
                .setDatastream(DATASTREAMS.get(0))
                .build();
        doa.create(observation);
        OBSERVATIONS.add(observation);

        Entity found;
        found = doa.find(observation.getPrimaryKeyValues());
        Entity featureOfInterest = found.getProperty(sMdl.npObservationFeatureofinterest);

        assertNotNull(featureOfInterest, "A FeatureOfInterest should have been generated, but got NULL.");
    }

    @Test
    void test02GeneratePhenomenonTime() throws ServiceFailureException {
        LOGGER.info("  test02GeneratePhenomenonTime");
        Dao doa = sSrvc.dao(sMdl.etObservation);
        Entity observation = sMdl.buildObservation()
                .setResult(1.0)
                .setDatastream(DATASTREAMS.get(0))
                .build();
        doa.create(observation);
        OBSERVATIONS.add(observation);

        Entity found;
        found = doa.find(observation.getPrimaryKeyValues());
        assertNotNull(found.getProperty(EP_PHENOMENONTIME), "phenomenonTime should be auto generated.");
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
     * @throws ServiceFailureException If the sSrvc doesn't respond.
     */
    @Test
    void test03HistoricalLocationThing() throws ServiceFailureException {
        LOGGER.info("  test03HistoricalLocationThing");
        EntityUtils.deleteAll(sSrvc);

        // Create a thing
        Entity thing = sMdl.buildThing().setName("Thing 1").setDescription("The first thing.").build();
        sSrvc.create(thing);

        // Create three locations.
        Entity location1 = sMdl.buildLocation()
                .setName("Location 1.0")
                .setDescription("Location Number 1.")
                .setEncodingType("application/vnd.geo+json")
                .setLocation(new Point(8, 50))
                .build();
        Entity location2 = sMdl.buildLocation()
                .setName("Location 2.0")
                .setDescription("Location Number 2.")
                .setEncodingType("application/vnd.geo+json")
                .setLocation(new Point(8, 51))
                .build();
        Entity location3 = sMdl.buildLocation()
                .setName("Location 3.0")
                .setDescription("Location Number 3.")
                .setEncodingType("application/vnd.geo+json")
                .setLocation(new Point(8, 52))
                .build();
        sSrvc.create(location1);
        sSrvc.create(location2);
        sSrvc.create(location3);

        // Give the Thing location 1
        thing.getProperty(sMdl.npThingLocations).add(location1.withOnlyPk());
        sSrvc.update(thing);

        // Get the generated HistoricalLocation and change the time to a known value.
        List<Entity> histLocations = thing.query(sMdl.npThingHistoricallocations).list().toList();

        assertEquals(1, histLocations.size(), "Incorrect number of HistoricalLocations for Thing.");

        Entity histLocation = histLocations.get(0);
        histLocation.setProperty(EP_TIME, TimeInstant.create(ZonedDateTime.parse("2016-01-01T06:00:00.000Z")));
        sSrvc.update(histLocation);

        // Now create a new HistoricalLocation for the Thing, with a later time.
        Entity histLocation2 = sMdl.buildHistoricalLocation()
                .setTime(TimeInstant.parse("2016-01-01T07:00:00.000Z"))
                .setThing(thing.withOnlyPk())
                .addLocation(location2)
                .build();
        sSrvc.create(histLocation2);

        // Check if the Location of the Thing is now Location 2.
        List<Entity> thingLocations = thing.query(sMdl.npThingLocations).list().toList();

        assertEquals(1, thingLocations.size(), "Incorrect number of Locations for Thing.");

        assertEquals(location2, thingLocations.get(0));

        // Now create a new HistoricalLocation for the Thing, with an earlier time.
        Entity histLocation3 = sMdl.buildHistoricalLocation()
                .setTime(TimeInstant.parse("2016-01-01T05:00:00.000Z"))
                .setThing(thing.withOnlyPk())
                .addLocation(location3.withOnlyPk())
                .build();
        sSrvc.create(histLocation3);

        // Check if the Location of the Thing is still Location 2.
        thingLocations = thing.query(sMdl.npThingLocations).list().toList();

        assertEquals(1, thingLocations.size(), "Incorrect number of Locations for Thing.");

        assertEquals(location2, thingLocations.get(0));
    }

    /**
     * Tests requests on paths like Things(x)/Datastreams(y)/Observations, where
     * Datastream(y) exists, but is not part of the, also existing, Things(x).
     *
     * @throws ServiceFailureException If the sSrvc doesn't respond.
     */
    @Test
    void test04PostInvalidPath() throws ServiceFailureException {
        LOGGER.info("  test04PostInvalidPath");
        EntityUtils.deleteAll(sSrvc);
        // Create two things

        Entity location1 = sMdl.buildLocation()
                .setName("LocationThing1")
                .setDescription("Location of Thing 1")
                .setEncodingType("application/geo+json")
                .setLocation(new Point(8, 50))
                .build();
        sSrvc.create(location1);

        Entity thing1 = sMdl.buildThing()
                .setName("Thing 1")
                .setDescription("The first thing.")
                .build();
        thing1.getProperty(sMdl.npThingLocations).add(location1.withOnlyPk());
        sSrvc.create(thing1);

        Entity thing2 = sMdl.buildThing()
                .setName("Thing 2")
                .setDescription("The second thing.")
                .build();
        thing2.getProperty(sMdl.npThingLocations).add(location1.withOnlyPk());
        sSrvc.create(thing2);

        Entity sensor1 = sMdl.buildSensor()
                .setName("Test Thermometre")
                .setDescription("Test Sensor")
                .setEncodingType("None")
                .setMetadata("-")
                .build();
        sSrvc.create(sensor1);

        Entity obsProp1 = sMdl.buildObservedProperty()
                .setName("Temperature")
                .setDefinition("http://example.org")
                .setDescription("-")
                .build();
        sSrvc.create(obsProp1);

        Entity datastream1 = sMdl.buildDatastream()
                .setName("Ds 1, Thing 1")
                .setDescription("The datastream of Thing 1")
                .setObservationType("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement")
                .setUnitOfMeasurement(new UnitOfMeasurement("Degrees Celcius", "°C", "http://qudt.org/vocab/unit#DegreeCelsius"))
                .setThing(thing1)
                .setSensor(sensor1)
                .setObservedProperty(obsProp1)
                .build();
        sSrvc.create(datastream1);

        Entity obs1 = sMdl.buildObservation()
                .setResult(1.0)
                .setDatastream(datastream1)
                .build();
        sSrvc.create(obs1);

        testGet(thing1, datastream1, thing2);

        // PUT tests
        String urlObsGood = serverSettings.getServiceUrl(version)
                + "/Things(" + formatKeyValuesForUrl(thing1) + ")"
                + "/Datastreams(" + formatKeyValuesForUrl(datastream1) + ")"
                + "/Observations(" + formatKeyValuesForUrl(obs1) + ")";
        String urlObsBad = serverSettings.getServiceUrl(version)
                + "/Things(" + formatKeyValuesForUrl(thing2) + ")"
                + "/Datastreams(" + formatKeyValuesForUrl(datastream1) + ")"
                + "/Observations(" + formatKeyValuesForUrl(obs1) + ")";

        testPut(urlObsGood, urlObsBad);
        testPatch(urlObsGood, urlObsBad);
        testDelete(urlObsBad, urlObsGood);
    }

    private void testGet(Entity thing1, Entity datastream1, Entity thing2) {
        // GET tests
        HTTPMethods.HttpResponse response;
        String url = serverSettings.getServiceUrl(version) + "/Things(" + formatKeyValuesForUrl(thing1) + ")/Datastreams(" + formatKeyValuesForUrl(datastream1) + ")/Observations";
        response = HTTPMethods.doGet(url);
        assertEquals(200, response.code, "Get should return 201 Created for url " + url);

        url = serverSettings.getServiceUrl(version) + "/Things(" + formatKeyValuesForUrl(thing2) + ")/Datastreams(" + formatKeyValuesForUrl(datastream1) + ")/Observations";
        response = HTTPMethods.doGet(url);
        assertEquals(404, response.code, "Get should return 404 Not Found for url " + url);

        // POST tests
        url = serverSettings.getServiceUrl(version) + "/Things(" + formatKeyValuesForUrl(thing1) + ")/Datastreams(" + formatKeyValuesForUrl(datastream1) + ")/Observations";
        String observationJson = """
                {
                  "phenomenonTime": "2015-03-01T03:00:00.000Z",
                  "result": 300
                }""";
        response = HTTPMethods.doPost(url, observationJson);
        assertEquals(201, response.code, "Post should return 201 Created for url " + url);

        url = serverSettings.getServiceUrl(version) + "/Things(" + formatKeyValuesForUrl(thing2) + ")/Datastreams(" + formatKeyValuesForUrl(datastream1) + ")/Observations";
        response = HTTPMethods.doPost(url, observationJson);
        assertNotEquals(201, response.code, "Post should not return 201 Created for url " + url);
    }

    private void testPut(String urlObsGood, String urlObsBad) {
        String observationJson;
        HTTPMethods.HttpResponse response;
        observationJson = """
                {
                  "phenomenonTime": "2015-03-01T03:00:00.000Z",
                  "result": 301
                }""";
        response = HTTPMethods.doPut(urlObsGood, observationJson);
        assertEquals(200, response.code, "Post should return 200 Ok for url " + urlObsGood + "\n" + response.toString());
        observationJson = """
                {
                  "phenomenonTime": "2015-03-01T03:00:00.000Z",
                  "result": 302
                }""";
        response = HTTPMethods.doPut(urlObsBad, observationJson);
        assertEquals(404, response.code, "Post should return 404 Not Found for url " + urlObsBad);
    }

    private void testPatch(String urlObsGood, String urlObsBad) {
        String observationJson;
        HTTPMethods.HttpResponse response;
        // PATCH tests
        observationJson = """
                {
                  "result": 303
                }""";
        response = HTTPMethods.doPatch(urlObsGood, observationJson);
        assertEquals(200, response.code, "Post should return 200 Ok for url " + urlObsGood);
        observationJson = """
                {
                  "result": 304
                }""";
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
        EntityUtils.deleteAll(sSrvc);
        DATASTREAMS.clear();
        // Create two things

        Entity location1 = sMdl.buildLocation()
                .setName("LocationThing1")
                .setDescription("Location of Thing 1")
                .setEncodingType("application/geo+json")
                .setLocation(new Point(8, 50))
                .build();
        sSrvc.create(location1);

        Entity thing1 = sMdl.buildThing()
                .setName("Thing 1")
                .setDescription("The first thing.")
                .build();
        thing1.getProperty(sMdl.npThingLocations).add(location1.withOnlyPk());
        sSrvc.create(thing1);

        Entity sensor1 = sMdl.buildSensor()
                .setName("Test Thermometre")
                .setDescription("Test Sensor")
                .setEncodingType("None")
                .setMetadata("-")
                .build();
        sSrvc.create(sensor1);

        Entity obsProp1 = sMdl.buildObservedProperty()
                .setName("Temperature")
                .setDefinition("http://example.org")
                .setDescription("-")
                .build();
        sSrvc.create(obsProp1);

        Entity datastream1 = sMdl.buildDatastream()
                .setName("Ds 1, Thing 1")
                .setDescription("The datastream of Thing 1")
                .setUnitOfMeasurement(new UnitOfMeasurement("Degrees Celcius", "°C", "http://qudt.org/vocab/unit#DegreeCelsius"))
                .setThing(thing1.withOnlyPk())
                .setSensor(sensor1.withOnlyPk())
                .setObservedProperty(obsProp1.withOnlyPk())
                .build();
        sSrvc.create(datastream1);
        DATASTREAMS.add(datastream1);

        Entity obs1 = sMdl.buildObservation()
                .setResult(1.0)
                .setDatastream(datastream1)
                .build();
        sSrvc.create(obs1);

        Entity foiGenerated1 = sSrvc.dao(sMdl.etObservation).find(obs1.getPrimaryKeyValues()).getProperty(sMdl.npObservationFeatureofinterest);
        assertNotNull(foiGenerated1);

        sSrvc.delete(foiGenerated1);

        Entity obs2 = sMdl.buildObservation()
                .setResult(1.0)
                .setDatastream(datastream1)
                .build();
        sSrvc.create(obs2);

        Entity foiGenerated2 = sSrvc.dao(sMdl.etObservation).find(obs2.getPrimaryKeyValues()).getProperty(sMdl.npObservationFeatureofinterest);
        assertNotNull(foiGenerated2);

        assertNotEquals(foiGenerated1, foiGenerated2);

        Entity datastream2 = sMdl.buildDatastream()
                .setName("Ds 2, Thing 1")
                .setDescription("The second datastream of Thing 1")
                .setUnitOfMeasurement(new UnitOfMeasurement("Degrees Celcius", "°C", "http://qudt.org/vocab/unit#DegreeCelsius"))
                .setThing(thing1.withOnlyPk())
                .setSensor(sensor1.withOnlyPk())
                .setObservedProperty(obsProp1.withOnlyPk())
                .build();
        sSrvc.create(datastream2);
        DATASTREAMS.add(datastream2);
    }

    @Test
    void test06DoubleConflictingNavProp() throws ServiceFailureException {
        LOGGER.info("  test06DoubleConflictingNavProp");
        Dao doa = DATASTREAMS.get(0).dao(sMdl.npDatastreamObservations);
        Entity observation = sMdl.buildObservation()
                .setResult(1.0)
                .setDatastream(DATASTREAMS.get(1))
                .build();
        StatusCodeException exc = Assertions.assertThrows(StatusCodeException.class,
                () -> doa.create(observation),
                "Creating an Observation with conflicting Datastreams should have failed.");
        assertEquals(400, exc.getStatusCode(), "Unexpected status code.");
    }

    @Test
    void test07DoubleNonConflictingNavProp() throws ServiceFailureException {
        LOGGER.info("  test07DoubleNonConflictingNavProp");
        Dao doa = DATASTREAMS.get(0).dao(sMdl.npDatastreamObservations);
        Entity observation = sMdl.buildObservation()
                .setResult(1.0)
                .setDatastream(DATASTREAMS.get(0))
                .build();
        doa.create(observation);
        OBSERVATIONS.add(observation);

        Entity found;
        found = doa.find(observation.getPrimaryKeyValues());
        assertNotNull(found.getProperty(EP_PHENOMENONTIME), "phenomenonTime should be auto generated.");
    }

    @Test
    void test08IncompletePut() throws ServiceFailureException {
        Entity thing = sMdl.buildThing()
                .setName("Thing Put")
                .setDescription("A thing for testing PUT.")
                .build();
        sSrvc.create(thing);

        String urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.THING, thing.getPrimaryKeyValues().get(0), null, null);
        HTTPMethods.HttpResponse responseMap = HTTPMethods.doPut(urlString, "{}");
        int responseCode = responseMap.code;
        String message = "Invalid put should have failed.";
        assertEquals(400, responseCode, message);
    }
}
