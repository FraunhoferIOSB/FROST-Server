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
package de.fraunhofer.iosb.ilt.statests.c05multidatastream;

import static de.fraunhofer.iosb.ilt.frostclient.utils.ParserUtils.formatKeyValuesForUrl;
import static de.fraunhofer.iosb.ilt.statests.util.Utils.getFromList;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.model.EntitySet;
import de.fraunhofer.iosb.ilt.frostclient.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerSettings;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods.HttpResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.geojson.Point;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some odd tests.
 *
 * @author Hylke van der Schaaf
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public abstract class MultiDatastreamTests extends AbstractTestClass {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiDatastreamTests.class);

    private static final List<Entity> THINGS = new ArrayList<>();
    private static final List<Entity> LOCATIONS = new ArrayList<>();
    private static final List<Entity> SENSORS = new ArrayList<>();
    private static final List<Entity> OBSERVED_PROPS = new ArrayList<>();
    private static final List<Entity> DATASTREAMS = new ArrayList<>();
    private static final List<Entity> MULTIDATASTREAMS = new ArrayList<>();
    private static final List<Entity> OBSERVATIONS = new ArrayList<>();

    public MultiDatastreamTests(ServerVersion version) {
        super(version);
    }

    @BeforeEach
    public void before() {
        assumeTrue(
                serverSettings.implementsRequirement(version, ServerSettings.MULTIDATA_REQ),
                "Conformance level 5 not checked since MultiDatastreams not listed in Service Root.");
    }

    @Override
    protected void setUpVersion() throws ServiceFailureException, URISyntaxException {
        LOGGER.info("Setting up for version {}.", version.urlPart);
        assumeTrue(
                serverSettings.implementsRequirement(version, ServerSettings.MULTIDATA_REQ),
                "Conformance level 5 not checked since MultiDatastreams not listed in Service Root.");
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
        OBSERVED_PROPS.clear();
        DATASTREAMS.clear();
        MULTIDATASTREAMS.clear();
        OBSERVATIONS.clear();
    }

    /**
     * Creates some basic non-MultiDatastream entities.
     *
     * @throws ServiceFailureException
     * @throws URISyntaxException
     */
    private static void createEntities() throws ServiceFailureException, URISyntaxException {
        Entity location = sMdl.newLocation("Location 1.0", "Location of Thing 1.", "application/vnd.geo+json", new Point(8, 51));
        sSrvc.create(location);
        LOCATIONS.add(location);

        Entity thing = sMdl.newThing("Thing 1", "The first thing.");
        thing.getProperty(sMdl.npThingLocations).add(location.withOnlyPk());
        sSrvc.create(thing);
        THINGS.add(thing);

        thing = sMdl.newThing("Thing 2", "The second thing.");
        thing.getProperty(sMdl.npThingLocations).add(location.withOnlyPk());
        sSrvc.create(thing);
        THINGS.add(thing);

        Entity sensor = sMdl.newSensor("Sensor 1", "The first sensor.", "text", "Some metadata.");
        sSrvc.create(sensor);
        SENSORS.add(sensor);

        sensor = sMdl.newSensor("Sensor 2", "The second sensor.", "text", "Some metadata.");
        sSrvc.create(sensor);
        SENSORS.add(sensor);

        Entity obsProp = sMdl.newObservedProperty("ObservedProperty 1", "http://ucom.org/temperature", "The temperature of the thing.");
        sSrvc.create(obsProp);
        OBSERVED_PROPS.add(obsProp);

        obsProp = sMdl.newObservedProperty("ObservedProperty 2", "http://ucom.org/humidity", "The humidity of the thing.");
        sSrvc.create(obsProp);
        OBSERVED_PROPS.add(obsProp);

        Entity datastream = sMdl.newDatastream("Datastream 1", "The temperature of thing 1, sensor 1.", "someType", new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
        DATASTREAMS.add(datastream);
        datastream.setProperty(sMdl.npDatastreamThing, THINGS.get(0).withOnlyPk());
        datastream.setProperty(sMdl.npDatastreamSensor, SENSORS.get(0).withOnlyPk());
        datastream.setProperty(sMdl.npDatastreamObservedproperty, OBSERVED_PROPS.get(0).withOnlyPk());
        sSrvc.create(datastream);

        datastream = sMdl.newDatastream("Datastream 2", "The temperature of thing 2, sensor 2.", "someType", new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
        DATASTREAMS.add(datastream);
        datastream.setProperty(sMdl.npDatastreamThing, THINGS.get(1).withOnlyPk());
        datastream.setProperty(sMdl.npDatastreamSensor, SENSORS.get(1).withOnlyPk());
        datastream.setProperty(sMdl.npDatastreamObservedproperty, OBSERVED_PROPS.get(0).withOnlyPk());
        sSrvc.create(datastream);

        createObservation(DATASTREAMS.get(0).withOnlyPk(), -1);
        createObservation(DATASTREAMS.get(1).withOnlyPk(), 0);
    }

    private static void createObservation(Entity ds, double result) throws ServiceFailureException {
        Entity o;
        if (sMdl.etDatastream.equals(ds.getEntityType())) {
            o = sMdl.newObservation(result, ds);
        } else {
            createObservationMds(ds, result);
            return;
        }
        sSrvc.create(o);
        OBSERVATIONS.add(o);
    }

    private static void createObservationMds(Entity ds, double... result) throws ServiceFailureException {
        Entity o;
        if (sMdl.etDatastream.equals(ds.getEntityType())) {
            o = sMdl.newObservation(result, ds);
        } else {
            o = mMdl.newObservation(result, ds);
        }
        sSrvc.create(o);
        OBSERVATIONS.add(o);
    }

    private void updateForException(String test, Entity entity) {
        try {
            sSrvc.update(entity);
        } catch (ServiceFailureException ex) {
            return;
        }
        fail(test + " Update did not respond with 400 Bad Request.");
    }

    private void checkResult(String test, EntityUtils.ResultTestResult result) {
        assertTrue(result.testOk, test + " " + result.message);
    }

    private void checkObservedPropertiesFor(Entity md, Entity... expectedObservedProps) throws ServiceFailureException {
        Entity[] fetchedObservedProps2 = md.query(mMdl.npMultidatastreamObservedproperties).list().toList().toArray(Entity[]::new);
        String message = "Incorrect Observed Properties returned.";
        assertArrayEquals(expectedObservedProps, fetchedObservedProps2, message);
    }

    @Test
    void test01MultiDatastream() throws ServiceFailureException {
        LOGGER.info("  test01MultiDatastream");
        // Create a MultiDatastream with one ObservedProperty.
        Entity md1 = mMdl.newMultiDatastream("MultiDatastream 1", "The first test MultiDatastream.", new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));

        md1.setProperty(sMdl.npDatastreamThing, THINGS.get(0).withOnlyPk());
        md1.setProperty(sMdl.npDatastreamSensor, SENSORS.get(0).withOnlyPk());
        md1.addNavigationEntity(mMdl.npMultidatastreamObservedproperties, OBSERVED_PROPS.get(0).withOnlyPk());

        sSrvc.create(md1);
        MULTIDATASTREAMS.add(md1);

        // Create a MultiDatastream with two different ObservedProperties.
        Entity md2 = mMdl.newMultiDatastream("MultiDatastream 2", "The second test MultiDatastream.",
                new UnitOfMeasurement("degree celcius", "°C", "ucum:T"),
                new UnitOfMeasurement("percent", "%", "ucum:%"));

        md2.setProperty(sMdl.npDatastreamThing, THINGS.get(0).withOnlyPk());
        md2.setProperty(sMdl.npDatastreamSensor, SENSORS.get(0).withOnlyPk());

        md2.addNavigationEntity(mMdl.npMultidatastreamObservedproperties, OBSERVED_PROPS.get(0).withOnlyPk());
        md2.addNavigationEntity(mMdl.npMultidatastreamObservedproperties, OBSERVED_PROPS.get(1).withOnlyPk());

        sSrvc.create(md2);
        MULTIDATASTREAMS.add(md2);

        // Create a MultiDatastream with two different ObservedProperties, in the opposite order.
        Entity md3 = mMdl.newMultiDatastream("MultiDatastream 3", "The third test MultiDatastream.",
                new UnitOfMeasurement("percent", "%", "ucum:%"),
                new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));

        md3.setProperty(sMdl.npDatastreamThing, THINGS.get(0).withOnlyPk());
        md3.setProperty(sMdl.npDatastreamSensor, SENSORS.get(0).withOnlyPk());

        md3.addNavigationEntity(mMdl.npMultidatastreamObservedproperties, OBSERVED_PROPS.get(1).withOnlyPk());
        md3.addNavigationEntity(mMdl.npMultidatastreamObservedproperties, OBSERVED_PROPS.get(0).withOnlyPk());

        sSrvc.create(md3);
        MULTIDATASTREAMS.add(md3);

        // Create a MultiDatastream with two of the same ObservedProperties.
        Entity md4 = mMdl.newMultiDatastream("MultiDatastream 4", "The fourth test MultiDatastream.",
                new UnitOfMeasurement("degree celcius", "°C", "ucum:T"),
                new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));

        md4.setProperty(sMdl.npDatastreamThing, THINGS.get(0).withOnlyPk());
        md4.setProperty(sMdl.npDatastreamSensor, SENSORS.get(1).withOnlyPk());

        md4.addNavigationEntity(mMdl.npMultidatastreamObservedproperties, OBSERVED_PROPS.get(0).withOnlyPk());
        md4.addNavigationEntity(mMdl.npMultidatastreamObservedproperties, OBSERVED_PROPS.get(0).withOnlyPk());

        sSrvc.create(md4);
        MULTIDATASTREAMS.add(md4);
        assertEquals(4, MULTIDATASTREAMS.size());
    }

    @Test
    void test02ObservationInMultiDatastream() throws ServiceFailureException {
        LOGGER.info("  test02ObservationInMultiDatastream");
        createObservation(MULTIDATASTREAMS.get(0).withOnlyPk(), 1);
        createObservation(MULTIDATASTREAMS.get(0).withOnlyPk(), 2);
        createObservation(MULTIDATASTREAMS.get(0).withOnlyPk(), 3);

        createObservationMds(MULTIDATASTREAMS.get(1).withOnlyPk(), 4, 1);
        createObservationMds(MULTIDATASTREAMS.get(1).withOnlyPk(), 5, 2);
        createObservationMds(MULTIDATASTREAMS.get(1).withOnlyPk(), 6, 3);

        createObservationMds(MULTIDATASTREAMS.get(2).withOnlyPk(), 7, 4);
        createObservationMds(MULTIDATASTREAMS.get(2).withOnlyPk(), 8, 5);
        createObservationMds(MULTIDATASTREAMS.get(2).withOnlyPk(), 9, 6);

        createObservationMds(MULTIDATASTREAMS.get(3).withOnlyPk(), 10, 7);
        createObservationMds(MULTIDATASTREAMS.get(3).withOnlyPk(), 11, 8);
        createObservationMds(MULTIDATASTREAMS.get(3).withOnlyPk(), 12, 9);
        assertEquals(14, OBSERVATIONS.size());
    }

    @Test
    void test03ObservationInMultiDatastreamIncorrect() throws ServiceFailureException {
        LOGGER.info("  test03ObservationInMultiDatastreamIncorrect");
        try {
            Entity o = mMdl.newObservation(1, MULTIDATASTREAMS.get(1).withOnlyPk());
            sSrvc.create(o);
            fail("Service should have rejected posting non-array result to a multidatastream.");
        } catch (ServiceFailureException e) {
        }

        try {
            createObservationMds(MULTIDATASTREAMS.get(0).withOnlyPk(), 1, 2);
            fail("Service should have rejected posting 2 results to a multidatastream with only 1 observed property.");
        } catch (ServiceFailureException e) {
        }
        try {
            createObservation(MULTIDATASTREAMS.get(1).withOnlyPk(), 1);
            fail("Service should have rejected posting 1 result to a multidatastream with 2 observed properties.");
        } catch (ServiceFailureException e) {
        }
    }

    private JsonNode getJsonObject(String urlString) {
        urlString = urlString.replaceAll(Pattern.quote("["), "%5B").replaceAll(Pattern.quote("]"), "%5D");
        HttpResponse responseMap = HTTPMethods.doGet(urlString);
        String message = "Error getting Observations using Data Array: Code " + responseMap.code;
        assertEquals(200, responseMap.code, message);

        JsonNode json;
        try {
            json = new ObjectMapper().readTree(responseMap.response);
        } catch (IOException ex) {
            fail("Server returned malformed JSON for request: " + urlString + " Exception: " + ex);
            return null;
        }
        if (!json.isObject()) {
            fail("Server did not return a JSON object for request: " + urlString);
        }
        return json;
    }

    private JsonNode getJsonValue(String urlString) {
        JsonNode json = getJsonObject(urlString);
        JsonNode value = json.get("value");
        if (value == null || !value.isArray()) {
            fail("value field is not an array for request: " + urlString);
        }
        return value;
    }

    private void entitiesHaveOneOf(JsonNode value, String EntityName, String... properties) {
        for (JsonNode valueItem : value) {
            if (!valueItem.isObject()) {
                fail("item in " + EntityName + " array is not an object.");
                return;
            }
            for (String property : properties) {
                if (valueItem.has(property)) {
                    return;
                }
            }
            fail("item in " + EntityName + " array does not contain any of " + Arrays.toString(properties));
        }
    }

    @Test
    void test04Json() throws ServiceFailureException {
        LOGGER.info("  test04Json");
        JsonNode json = getJsonValue(serverSettings.getServiceUrl(version) + "/Things");
        entitiesHaveOneOf(json, "Things", "MultiDatastreams@iot.navigationLink");
        json = getJsonValue(serverSettings.getServiceUrl(version) + "/Sensors");
        entitiesHaveOneOf(json, "Sensors", "MultiDatastreams@iot.navigationLink");
        json = getJsonValue(serverSettings.getServiceUrl(version) + "/ObservedProperties");
        entitiesHaveOneOf(json, "ObservedProperties", "MultiDatastreams@iot.navigationLink");
        json = getJsonValue(serverSettings.getServiceUrl(version) + "/Observations");
        entitiesHaveOneOf(json, "Observations", "MultiDatastream@iot.navigationLink", "Datastream@iot.navigationLink");
        json = getJsonValue(serverSettings.getServiceUrl(version) + "/MultiDatastreams");
        for (String property : EntityTypeMds.MULTI_DATASTREAM.getProperties()) {
            entitiesHaveOneOf(json, "MultiDatastreams", property);
        }
        for (String relation : EntityTypeMds.MULTI_DATASTREAM.getRelations()) {
            entitiesHaveOneOf(json, "MultiDatastreams", relation + "@iot.navigationLink");
        }

        String urlString = serverSettings.getServiceUrl(version) + "/Observations(" + formatKeyValuesForUrl(OBSERVATIONS.get(5)) + ")/result[0]";
        json = getJsonObject(urlString);
        JsonNode value = json.get("result[0]");
        if (value == null || !value.isNumber()) {
            fail("Did not get a numeric value for result[0] for url: " + urlString);
        } else {
            String message = "Did not get correct value for url: " + urlString;
            assertEquals(4, value.asInt(), message);
        }
    }

    @Test
    void test05MultiDatastreamThings() throws ServiceFailureException {
        LOGGER.info("  test05MultiDatastreamThings");
        // Check if all Datastreams and MultiDatastreams are linked to Thing 1.
        Entity fetchedThing = sSrvc.dao(sMdl.etThing).find(THINGS.get(0).getPrimaryKeyValues());
        EntitySet fetchedDatastreams = fetchedThing.query(sMdl.npThingDatastreams).list();
        checkResult("Check Datastreams linked to Thing 1.", EntityUtils.resultContains(fetchedDatastreams, DATASTREAMS.get(0)));
        EntitySet fetchedMultiDatastreams = fetchedThing.query(mMdl.npThingMultidatastreams).list();
        checkResult("Check MultiDatastreams linked to Thing 1.", EntityUtils.resultContains(fetchedMultiDatastreams, new ArrayList<>(MULTIDATASTREAMS)));
    }

    @Test
    void test06MultiDatastreamSensors() throws ServiceFailureException {
        LOGGER.info("  test06MultiDatastreamSensors");
        // Check if all Datastreams and MultiDatastreams are linked to Sensor 1.
        Entity fetchedSensor = sSrvc.dao(sMdl.etSensor).find(SENSORS.get(0).getPrimaryKeyValues());
        EntitySet fetchedDatastreams = fetchedSensor.query(sMdl.npSensorDatastreams).list();
        checkResult("Check Datastreams linked to Sensor 1.", EntityUtils.resultContains(fetchedDatastreams, DATASTREAMS.get(0)));
        EntitySet fetchedMultiDatastreams = fetchedSensor.query(mMdl.npSensorMultidatastreams).list();
        checkResult(
                "Check MultiDatastreams linked to Sensor 1.",
                EntityUtils.resultContains(fetchedMultiDatastreams, getFromList(MULTIDATASTREAMS, 0, 1, 2)));
    }

    @Test
    void test07MultiDatastreamObservedProperties1() throws ServiceFailureException {
        LOGGER.info("  test07MultiDatastreamObservedProperties1");
        // Check if all Datastreams and MultiDatastreams are linked to ObservedProperty 1.
        Entity fetchedObservedProp = sSrvc.dao(sMdl.etObservedProperty).find(OBSERVED_PROPS.get(0).getPrimaryKeyValues());
        EntitySet fetchedDatastreams = fetchedObservedProp.query(sMdl.npObspropDatastreams).list();
        checkResult(
                "Check Datastreams linked to ObservedProperty 1.",
                EntityUtils.resultContains(fetchedDatastreams, getFromList(DATASTREAMS, 0, 1)));
        EntitySet fetchedMultiDatastreams = fetchedObservedProp.query(mMdl.npObspropMultidatastreams).list();
        checkResult(
                "Check MultiDatastreams linked to ObservedProperty 1.",
                EntityUtils.resultContains(fetchedMultiDatastreams, new ArrayList<>(MULTIDATASTREAMS)));
    }

    @Test
    void test08MultiDatastreamObservedProperties2() throws ServiceFailureException {
        LOGGER.info("  test08MultiDatastreamObservedProperties2");
        // Check if MultiDatastreams 2 and 3 are linked to ObservedProperty 2.
        Entity fetchedObservedProp = sSrvc.dao(sMdl.etObservedProperty).find(OBSERVED_PROPS.get(1).getPrimaryKeyValues());
        EntitySet fetchedDatastreams = fetchedObservedProp.query(sMdl.npObspropDatastreams).list();
        checkResult(
                "Check Datastreams linked to ObservedProperty 2.",
                EntityUtils.resultContains(fetchedDatastreams, new ArrayList<>()));
        EntitySet fetchedMultiDatastreams = fetchedObservedProp.query(mMdl.npObspropMultidatastreams).list();
        checkResult(
                "Check MultiDatastreams linked to ObservedProperty 2.",
                EntityUtils.resultContains(fetchedMultiDatastreams, getFromList(MULTIDATASTREAMS, 1, 2)));
    }

    @Test
    void test09ObservationLinks1() throws ServiceFailureException {
        LOGGER.info("  test09ObservationLinks1");
        // First Observation should have a Datastream but not a MultiDatasteam.
        Entity fetchedObservation = sSrvc.dao(sMdl.etObservation).find(OBSERVATIONS.get(0).getPrimaryKeyValues());
        Entity fetchedDatastream = fetchedObservation.getProperty(sMdl.npObservationDatastream);

        String message = "Observation has wrong or no Datastream";
        assertEquals(DATASTREAMS.get(0).withOnlyPk(), fetchedDatastream.withOnlyPk(), message);

        Entity fetchedMultiDatastream = fetchedObservation.getProperty(mMdl.npObservationMultidatastream);

        message = "Observation should not have a MultiDatastream";
        assertEquals(null, fetchedMultiDatastream, message);
    }

    @Test
    void test10ObservationLinks2() throws ServiceFailureException {
        LOGGER.info("  test10ObservationLinks2");
        // Second Observation should not have a Datastream but a MultiDatasteam.
        Entity fetchedObservation = sSrvc.dao(sMdl.etObservation).find(OBSERVATIONS.get(2).getPrimaryKeyValues());
        Entity fetchedDatastream = fetchedObservation.getProperty(sMdl.npObservationDatastream);

        String message = "Observation should not have a Datastream";
        assertEquals(null, fetchedDatastream, message);

        Entity fetchedMultiDatastream = fetchedObservation.getProperty(mMdl.npObservationMultidatastream);

        message = "Observation has wrong or no MultiDatastream";
        assertEquals(MULTIDATASTREAMS.get(0).withOnlyPk(), fetchedMultiDatastream.withOnlyPk(), message);
    }

    @Test
    void test11ObservedPropertyOrder() throws ServiceFailureException {
        LOGGER.info("  test11ObservedPropertyOrder");
        // Check if the MultiDatastreams have the correct ObservedProperties in the correct order.
        checkObservedPropertiesFor(MULTIDATASTREAMS.get(0), OBSERVED_PROPS.get(0));
        checkObservedPropertiesFor(MULTIDATASTREAMS.get(1), OBSERVED_PROPS.get(0), OBSERVED_PROPS.get(1));
        checkObservedPropertiesFor(MULTIDATASTREAMS.get(2), OBSERVED_PROPS.get(1), OBSERVED_PROPS.get(0));
        checkObservedPropertiesFor(MULTIDATASTREAMS.get(3), OBSERVED_PROPS.get(0), OBSERVED_PROPS.get(0));
    }

    @Test
    void test12IncorrectObservation() throws ServiceFailureException {
        LOGGER.info("  test12IncorrectObservation");
        // Try to give Observation 1 a MultiDatastream without removing the Datastream. Should give an error.
        Entity modifiedObservation = OBSERVATIONS.get(0).withOnlyPk();
        modifiedObservation.setProperty(mMdl.npObservationMultidatastream, MULTIDATASTREAMS.get(0).withOnlyPk());
        updateForException("Linking Observation to Datastream AND MultiDatastream.", modifiedObservation);
    }

    @Test
    void test13IncorrectObservedProperty() throws ServiceFailureException {
        LOGGER.info("  test13IncorrectObservedProperty");
        // Try to add a MultiDatastream to an ObservedProperty. Should give an error.
        Entity modifiedObservedProp = OBSERVED_PROPS.get(1).withOnlyPk();
        modifiedObservedProp.getProperty(mMdl.npObspropMultidatastreams).add(MULTIDATASTREAMS.get(0).withOnlyPk());
        updateForException("Linking MultiDatastream to Observed property.", modifiedObservedProp);
    }

    @Test
    void test14FetchObservationsByMultiDatastream() throws ServiceFailureException {
        LOGGER.info("  test14FetchObservationsByMultiDatastream");
        EntitySet observations = MULTIDATASTREAMS.get(0).query(mMdl.npMultidatastreamObservations).list();
        checkResult(
                "Looking for all observations",
                EntityUtils.resultContains(observations, getFromList(OBSERVATIONS, 2, 3, 4)));

        observations = MULTIDATASTREAMS.get(1).query(mMdl.npMultidatastreamObservations).list();
        checkResult(
                "Looking for all observations",
                EntityUtils.resultContains(observations, getFromList(OBSERVATIONS, 5, 6, 7)));

        observations = MULTIDATASTREAMS.get(2).query(mMdl.npMultidatastreamObservations).list();
        checkResult(
                "Looking for all observations",
                EntityUtils.resultContains(observations, getFromList(OBSERVATIONS, 8, 9, 10)));

        observations = MULTIDATASTREAMS.get(3).query(mMdl.npMultidatastreamObservations).list();
        checkResult(
                "Looking for all observations",
                EntityUtils.resultContains(observations, getFromList(OBSERVATIONS, 11, 12, 13)));
    }

    @Test
    void test15Observations() throws ServiceFailureException {
        LOGGER.info("  test15Observations");
        // Check if all observations are there.
        EntitySet fetchedObservations = sSrvc.query(sMdl.etObservation).list();
        checkResult(
                "Looking for all observations",
                EntityUtils.resultContains(fetchedObservations, new ArrayList<>(OBSERVATIONS)));
    }

    @Test
    void test16DeleteObservedProperty() throws ServiceFailureException {
        LOGGER.info("  test16DeleteObservedProperty");
        // Deleting ObservedProperty 2 should delete MultiDatastream 2 and 3 and their Observations.
        sSrvc.delete(OBSERVED_PROPS.get(1));
        EntitySet fetchedMultiDatastreams = sSrvc.query(mMdl.etMultiDatastream).list();
        checkResult(
                "Checking if MultiDatastreams are automatically deleted.",
                EntityUtils.resultContains(fetchedMultiDatastreams, getFromList(MULTIDATASTREAMS, 0, 3)));
        EntitySet fetchedObservations = sSrvc.query(sMdl.etObservation).list();
        checkResult(
                "Checking if Observations are automatically deleted.",
                EntityUtils.resultContains(fetchedObservations, getFromList(OBSERVATIONS, 0, 1, 2, 3, 4, 11, 12, 13)));
    }

    @Test
    void test17DeleteSensor() throws ServiceFailureException {
        LOGGER.info("  test17DeleteSensor");
        // Deleting Sensor 2 should delete MultiDatastream 4
        sSrvc.delete(SENSORS.get(1));
        EntitySet fetchedMultiDatastreams = sSrvc.query(mMdl.etMultiDatastream).list();
        checkResult(
                "Checking if MultiDatastreams are automatically deleted.",
                EntityUtils.resultContains(fetchedMultiDatastreams, getFromList(MULTIDATASTREAMS, 0)));
    }

    @Test
    void test18DeleteThing() throws ServiceFailureException {
        LOGGER.info("  test18DeleteThing");
        // Deleting Thing 1 should delete the last MultiDatastream.
        sSrvc.delete(THINGS.get(0));
        EntitySet fetchedMultiDatastreams = sSrvc.query(mMdl.etMultiDatastream).list();
        checkResult(
                "Checking if MultiDatastreams are automatically deleted.",
                EntityUtils.resultContains(fetchedMultiDatastreams, new ArrayList<>()));
    }

}
