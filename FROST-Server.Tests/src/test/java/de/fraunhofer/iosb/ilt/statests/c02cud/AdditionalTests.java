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

import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsSensingV11.EP_PHENOMENONTIME;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsSensingV11.EP_TIME;
import static de.fraunhofer.iosb.ilt.frostclient.utils.ParserUtils.formatKeyValuesForUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.fraunhofer.iosb.ilt.frostclient.dao.Dao;
import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostclient.model.ext.UnitOfMeasurement;
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

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AdditionalTests.class);

    private static final List<Entity> THINGS = new ArrayList<>();
    private static final List<Entity> DATASTREAMS = new ArrayList<>();
    private static final List<Entity> OBSERVATIONS = new ArrayList<>();

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
        EntityUtils.deleteAll(service);
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
        EntityUtils.deleteAll(service);

        Entity thing = sMdl.newThing("Thing 1", "The first thing.");

        Entity location1 = sMdl.newLocation("Location 1.0, Address", "The address of Thing 1.", "text/plain", "");
        thing.getProperty(sMdl.npThingLocations).add(location1);
        Entity location2 = sMdl.newLocation("Location 1.0", "Location of Thing 1.", "application/geo+json", new Point(8, 51));
        thing.getProperty(sMdl.npThingLocations).add(location2);
        Entity location3 = sMdl.newLocation("Location 1.0, Directions", "How to find Thing 1 in human language.", "text/plain", "");
        thing.getProperty(sMdl.npThingLocations).add(location3);

        sSrvc.create(thing);
        THINGS.add(thing);

        Entity sensor = sMdl.newSensor("Sensor 1", "The first sensor.", "text", "Some metadata.");
        Entity obsProp = sMdl.newObservedProperty("Temperature", "http://ucom.org/temperature", "The temperature of the thing.");
        Entity datastream = sMdl.newDatastream("Datastream 1", "The temperature of thing 1, sensor 1.", new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
        datastream.setProperty(sMdl.npDatastreamSensor, sensor);
        datastream.setProperty(sMdl.npDatastreamObservedproperty, obsProp);
        datastream.setProperty(sMdl.npDatastreamThing, thing);

        sSrvc.create(datastream);
        DATASTREAMS.add(datastream);

        Dao doa = sSrvc.dao(sMdl.etObservation);
        Entity observation = sMdl.newObservation(1.0, DATASTREAMS.get(0));
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
        Entity observation = sMdl.newObservation(1.0, DATASTREAMS.get(0));
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
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    void test03HistoricalLocationThing() throws ServiceFailureException {
        LOGGER.info("  test03HistoricalLocationThing");
        EntityUtils.deleteAll(service);

        // Create a thing
        Entity thing = sMdl.newThing("Thing 1", "The first thing.");
        sSrvc.create(thing);

        // Create three locations.
        Entity location1 = sMdl.newLocation("Location 1.0", "Location Number 1.", "application/vnd.geo+json", new Point(8, 50));
        Entity location2 = sMdl.newLocation("Location 2.0", "Location Number 2.", "application/vnd.geo+json", new Point(8, 51));
        Entity location3 = sMdl.newLocation("Location 3.0", "Location Number 3.", "application/vnd.geo+json", new Point(8, 52));
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
        Entity histLocation2 = sMdl.newHistoricalLocation(ZonedDateTime.parse("2016-01-01T07:00:00.000Z"), thing.withOnlyPk(), location2);
        sSrvc.create(histLocation2);

        // Check if the Location of the Thing is now Location 2.
        List<Entity> thingLocations = thing.query(sMdl.npThingLocations).list().toList();

        assertEquals(1, thingLocations.size(), "Incorrect number of Locations for Thing.");

        assertEquals(location2, thingLocations.get(0));

        // Now create a new HistoricalLocation for the Thing, with an earlier time.
        Entity histLocation3 = sMdl.newHistoricalLocation(ZonedDateTime.parse("2016-01-01T05:00:00.000Z"), thing.withOnlyPk(), location3.withOnlyPk());
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
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    void test04PostInvalidPath() throws ServiceFailureException {
        LOGGER.info("  test04PostInvalidPath");
        EntityUtils.deleteAll(service);
        // Create two things

        Entity location1 = sMdl.newLocation("LocationThing1", "Location of Thing 1", "application/geo+json", new Point(8, 50));
        sSrvc.create(location1);

        Entity thing1 = sMdl.newThing("Thing 1", "The first thing.");
        thing1.getProperty(sMdl.npThingLocations).add(location1.withOnlyPk());
        sSrvc.create(thing1);

        Entity thing2 = sMdl.newThing("Thing 2", "The second thing.");
        thing2.getProperty(sMdl.npThingLocations).add(location1.withOnlyPk());
        sSrvc.create(thing2);

        Entity sensor1 = sMdl.newSensor("Test Thermometre", "Test Sensor", "None", "-");
        sSrvc.create(sensor1);

        Entity obsProp1 = sMdl.newObservedProperty("Temperature", "http://example.org", "-");
        sSrvc.create(obsProp1);

        Entity datastream1 = sMdl.newDatastream("Ds 1, Thing 1", "The datastream of Thing 1", "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement", new UnitOfMeasurement("Degrees Celcius", "°C", "http://qudt.org/vocab/unit#DegreeCelsius"));
        datastream1.setProperty(sMdl.npDatastreamThing, thing1);
        datastream1.setProperty(sMdl.npDatastreamSensor, sensor1);
        datastream1.setProperty(sMdl.npDatastreamObservedproperty, obsProp1);
        sSrvc.create(datastream1);

        Entity obs1 = sMdl.newObservation(1.0, datastream1);
        sSrvc.create(obs1);

        testGet(thing1, datastream1, thing2);

        // PUT tests
        String urlObsGood = serverSettings.getServiceUrl(version)
                + "/Things(" + formatKeyValuesForUrl(thing1.getPrimaryKeyValues()) + ")"
                + "/Datastreams(" + formatKeyValuesForUrl(datastream1.getPrimaryKeyValues()) + ")"
                + "/Observations(" + formatKeyValuesForUrl(obs1.getPrimaryKeyValues()) + ")";
        String urlObsBad = serverSettings.getServiceUrl(version)
                + "/Things(" + formatKeyValuesForUrl(thing2.getPrimaryKeyValues()) + ")"
                + "/Datastreams(" + formatKeyValuesForUrl(datastream1.getPrimaryKeyValues()) + ")"
                + "/Observations(" + formatKeyValuesForUrl(obs1.getPrimaryKeyValues()) + ")";

        testPut(urlObsGood, urlObsBad);
        testPatch(urlObsGood, urlObsBad);
        testDelete(urlObsBad, urlObsGood);
    }

    private void testGet(Entity thing1, Entity datastream1, Entity thing2) {
        // GET tests
        HTTPMethods.HttpResponse response;
        String url = serverSettings.getServiceUrl(version) + "/Things(" + formatKeyValuesForUrl(thing1.getPrimaryKeyValues()) + ")/Datastreams(" + formatKeyValuesForUrl(datastream1.getPrimaryKeyValues()) + ")/Observations";
        response = HTTPMethods.doGet(url);
        assertEquals(200, response.code, "Get should return 201 Created for url " + url);

        url = serverSettings.getServiceUrl(version) + "/Things(" + formatKeyValuesForUrl(thing2.getPrimaryKeyValues()) + ")/Datastreams(" + formatKeyValuesForUrl(datastream1.getPrimaryKeyValues()) + ")/Observations";
        response = HTTPMethods.doGet(url);
        assertEquals(404, response.code, "Get should return 404 Not Found for url " + url);

        // POST tests
        url = serverSettings.getServiceUrl(version) + "/Things(" + formatKeyValuesForUrl(thing1.getPrimaryKeyValues()) + ")/Datastreams(" + formatKeyValuesForUrl(datastream1.getPrimaryKeyValues()) + ")/Observations";
        String observationJson = "{\n"
                + "  \"phenomenonTime\": \"2015-03-01T03:00:00.000Z\",\n"
                + "  \"result\": 300\n"
                + "}";
        response = HTTPMethods.doPost(url, observationJson);
        assertEquals(201, response.code, "Post should return 201 Created for url " + url);

        url = serverSettings.getServiceUrl(version) + "/Things(" + formatKeyValuesForUrl(thing2.getPrimaryKeyValues()) + ")/Datastreams(" + formatKeyValuesForUrl(datastream1.getPrimaryKeyValues()) + ")/Observations";
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
        EntityUtils.deleteAll(service);
        // Create two things

        Entity location1 = sMdl.newLocation("LocationThing1", "Location of Thing 1", "application/geo+json", new Point(8, 50));
        sSrvc.create(location1);

        Entity thing1 = sMdl.newThing("Thing 1", "The first thing.");
        thing1.getProperty(sMdl.npThingLocations).add(location1.withOnlyPk());
        sSrvc.create(thing1);

        Entity sensor1 = sMdl.newSensor("Test Thermometre", "Test Sensor", "None", "-");
        sSrvc.create(sensor1);

        Entity obsProp1 = sMdl.newObservedProperty("Temperature", "http://example.org", "-");
        sSrvc.create(obsProp1);

        Entity datastream1 = sMdl.newDatastream("Ds 1, Thing 1", "The datastream of Thing 1", new UnitOfMeasurement("Degrees Celcius", "°C", "http://qudt.org/vocab/unit#DegreeCelsius"));
        datastream1.setProperty(sMdl.npDatastreamThing, thing1);
        datastream1.setProperty(sMdl.npDatastreamSensor, sensor1);
        datastream1.setProperty(sMdl.npDatastreamObservedproperty, obsProp1);
        sSrvc.create(datastream1);

        Entity obs1 = sMdl.newObservation(1.0, datastream1);
        sSrvc.create(obs1);

        Entity foiGenerated1 = sSrvc.dao(sMdl.etObservation).find(obs1.getPrimaryKeyValues()).getProperty(sMdl.npObservationFeatureofinterest);
        assertNotNull(foiGenerated1);

        sSrvc.delete(foiGenerated1);

        Entity obs2 = sMdl.newObservation(1.0, datastream1);
        sSrvc.create(obs2);

        Entity foiGenerated2 = sSrvc.dao(sMdl.etObservation).find(obs2.getPrimaryKeyValues()).getProperty(sMdl.npObservationFeatureofinterest);
        assertNotNull(foiGenerated2);

        assertNotEquals(foiGenerated1, foiGenerated2);
    }
}
