package de.fraunhofer.iosb.ilt.statests.c05multidatastream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.Entity;
import de.fraunhofer.iosb.ilt.sta.model.Location;
import de.fraunhofer.iosb.ilt.sta.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.ext.EntityList;
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.sta.service.SensorThingsService;
import de.fraunhofer.iosb.ilt.statests.ServerSettings;
import de.fraunhofer.iosb.ilt.statests.TestSuite;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods.HttpResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.geojson.Point;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.internal.ArrayComparisonFailure;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some odd tests.
 *
 * @author Hylke van der Schaaf
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MultiDatastreamTests {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiDatastreamTests.class);

    private static ServerSettings serverSettings;
    private static SensorThingsService service;

    private static final List<Thing> THINGS = new ArrayList<>();
    private static final List<Location> LOCATIONS = new ArrayList<>();
    private static final List<Sensor> SENSORS = new ArrayList<>();
    private static final List<ObservedProperty> OBSERVED_PROPS = new ArrayList<>();
    private static final List<Datastream> DATASTREAMS = new ArrayList<>();
    private static final List<MultiDatastream> MULTIDATASTREAMS = new ArrayList<>();
    private static final List<Observation> OBSERVATIONS = new ArrayList<>();

    public MultiDatastreamTests() {
    }

    @BeforeClass
    public static void setUp() throws MalformedURLException, ServiceFailureException, URISyntaxException {
        LOGGER.info("Setting up.");
        TestSuite suite = TestSuite.getInstance();
        serverSettings = suite.getServerSettings();

        Assert.assertTrue("Conformance level 5 not checked since MultiDatastreams not listed in Service Root.", serverSettings.hasMultiDatastream);

        service = new SensorThingsService(new URL(serverSettings.serviceUrl));
        createEntities();
    }

    @AfterClass
    public static void tearDown() {
        LOGGER.info("Tearing down.");
        try {
            EntityUtils.deleteAll(service);
        } catch (ServiceFailureException ex) {
            LOGGER.error("Failed to clean database.", ex);
        }
    }

    /**
     * Creates some basic non-MultiDatastream entities.
     *
     * @throws ServiceFailureException
     * @throws URISyntaxException
     */
    private static void createEntities() throws ServiceFailureException, URISyntaxException {
        Location location = new Location("Location 1.0", "Location of Thing 1.", "application/vnd.geo+json", new Point(8, 51));
        service.create(location);
        LOCATIONS.add(location);

        Thing thing = new Thing("Thing 1", "The first thing.");
        thing.getLocations().add(location.withOnlyId());
        service.create(thing);
        THINGS.add(thing);

        thing = new Thing("Thing 2", "The second thing.");
        thing.getLocations().add(location.withOnlyId());
        service.create(thing);
        THINGS.add(thing);

        Sensor sensor = new Sensor("Sensor 1", "The first sensor.", "text", "Some metadata.");
        service.create(sensor);
        SENSORS.add(sensor);

        sensor = new Sensor("Sensor 2", "The second sensor.", "text", "Some metadata.");
        service.create(sensor);
        SENSORS.add(sensor);

        ObservedProperty obsProp = new ObservedProperty("ObservedProperty 1", new URI("http://ucom.org/temperature"), "The temperature of the thing.");
        service.create(obsProp);
        OBSERVED_PROPS.add(obsProp);

        obsProp = new ObservedProperty("ObservedProperty 2", new URI("http://ucom.org/humidity"), "The humidity of the thing.");
        service.create(obsProp);
        OBSERVED_PROPS.add(obsProp);

        Datastream datastream = new Datastream("Datastream 1", "The temperature of thing 1, sensor 1.", "someType", new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
        DATASTREAMS.add(datastream);
        datastream.setThing(THINGS.get(0).withOnlyId());
        datastream.setSensor(SENSORS.get(0).withOnlyId());
        datastream.setObservedProperty(OBSERVED_PROPS.get(0).withOnlyId());
        service.create(datastream);

        datastream = new Datastream("Datastream 2", "The temperature of thing 2, sensor 2.", "someType", new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
        DATASTREAMS.add(datastream);
        datastream.setThing(THINGS.get(1).withOnlyId());
        datastream.setSensor(SENSORS.get(1).withOnlyId());
        datastream.setObservedProperty(OBSERVED_PROPS.get(0).withOnlyId());
        service.create(datastream);

        createObservation(DATASTREAMS.get(0).withOnlyId(), -1);
        createObservation(DATASTREAMS.get(1).withOnlyId(), 0);
    }

    private static void createObservation(Datastream ds, double result) throws ServiceFailureException {
        Observation o = new Observation(result, ds);
        service.create(o);
        OBSERVATIONS.add(o);
    }

    private static void createObservation(MultiDatastream ds, double... result) throws ServiceFailureException {
        Observation o = new Observation(result, ds);
        service.create(o);
        OBSERVATIONS.add(o);
    }

    private void updateForException(String test, Entity entity) {
        try {
            service.update(entity);
        } catch (ServiceFailureException ex) {
            return;
        }
        Assert.fail(test + " Update did not respond with 400 Bad Request.");
    }

    private void checkResult(String test, EntityUtils.resultTestResult result) {
        Assert.assertTrue(test + " " + result.message, result.testOk);
    }

    private void checkObservedPropertiesFor(MultiDatastream md, ObservedProperty... expectedObservedProps) throws ArrayComparisonFailure, ServiceFailureException {
        ObservedProperty[] fetchedObservedProps2 = md.observedProperties().query().list().toArray(new ObservedProperty[0]);
        String message = "Incorrect Observed Properties returned.";
        Assert.assertArrayEquals(message, expectedObservedProps, fetchedObservedProps2);
    }

    @Test
    public void test01MultiDatastream() throws ServiceFailureException {
        LOGGER.info("  test01MultiDatastream");
        // Create a MultiDatastream with one ObservedProperty.
        MultiDatastream md1 = new MultiDatastream();
        md1.setName("MultiDatastream 1");
        md1.setDescription("The first test MultiDatastream.");
        md1.addUnitOfMeasurement(new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));

        List<String> dataTypes1 = new ArrayList<>();
        dataTypes1.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        md1.setMultiObservationDataTypes(dataTypes1);

        md1.setThing(THINGS.get(0).withOnlyId());
        md1.setSensor(SENSORS.get(0).withOnlyId());

        List<ObservedProperty> observedProperties = new ArrayList<>();
        observedProperties.add(OBSERVED_PROPS.get(0).withOnlyId());
        md1.setObservedProperties(observedProperties);

        service.create(md1);
        MULTIDATASTREAMS.add(md1);

        // Create a MultiDatastream with two different ObservedProperties.
        MultiDatastream md2 = new MultiDatastream();
        md2.setName("MultiDatastream 2");
        md2.setDescription("The second test MultiDatastream.");
        md2.addUnitOfMeasurement(new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
        md2.addUnitOfMeasurement(new UnitOfMeasurement("percent", "%", "ucum:%"));

        List<String> dataTypes2 = new ArrayList<>();
        dataTypes2.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        dataTypes2.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        md2.setMultiObservationDataTypes(dataTypes2);

        md2.setThing(THINGS.get(0).withOnlyId());
        md2.setSensor(SENSORS.get(0).withOnlyId());

        List<ObservedProperty> observedProperties2 = new ArrayList<>();
        observedProperties2.add(OBSERVED_PROPS.get(0).withOnlyId());
        observedProperties2.add(OBSERVED_PROPS.get(1).withOnlyId());
        md2.setObservedProperties(observedProperties2);

        service.create(md2);
        MULTIDATASTREAMS.add(md2);

        // Create a MultiDatastream with two different ObservedProperties, in the opposite order.
        MultiDatastream md3 = new MultiDatastream();
        md3.setName("MultiDatastream 3");
        md3.setDescription("The third test MultiDatastream.");
        md3.addUnitOfMeasurement(new UnitOfMeasurement("percent", "%", "ucum:%"));
        md3.addUnitOfMeasurement(new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));

        List<String> dataTypes3 = new ArrayList<>();
        dataTypes3.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        dataTypes3.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        md3.setMultiObservationDataTypes(dataTypes3);

        md3.setThing(THINGS.get(0).withOnlyId());
        md3.setSensor(SENSORS.get(0).withOnlyId());

        List<ObservedProperty> observedProperties3 = new ArrayList<>();
        observedProperties3.add(OBSERVED_PROPS.get(1).withOnlyId());
        observedProperties3.add(OBSERVED_PROPS.get(0).withOnlyId());
        md3.setObservedProperties(observedProperties3);

        service.create(md3);
        MULTIDATASTREAMS.add(md3);

        // Create a MultiDatastream with two of the same ObservedProperties.
        MultiDatastream md4 = new MultiDatastream();
        md4.setName("MultiDatastream 4");
        md4.setDescription("The fourth test MultiDatastream.");
        md4.addUnitOfMeasurement(new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
        md4.addUnitOfMeasurement(new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));

        List<String> dataTypes4 = new ArrayList<>();
        dataTypes4.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        dataTypes4.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        md4.setMultiObservationDataTypes(dataTypes4);

        md4.setThing(THINGS.get(0).withOnlyId());
        md4.setSensor(SENSORS.get(1).withOnlyId());

        List<ObservedProperty> observedProperties4 = new ArrayList<>();
        observedProperties4.add(OBSERVED_PROPS.get(0).withOnlyId());
        observedProperties4.add(OBSERVED_PROPS.get(0).withOnlyId());
        md4.setObservedProperties(observedProperties4);

        service.create(md4);
        MULTIDATASTREAMS.add(md4);
        Assert.assertEquals(4, MULTIDATASTREAMS.size());
    }

    @Test
    public void test02ObservationInMultiDatastream() throws ServiceFailureException {
        LOGGER.info("  test02ObservationInMultiDatastream");
        createObservation(MULTIDATASTREAMS.get(0).withOnlyId(), 1);
        createObservation(MULTIDATASTREAMS.get(0).withOnlyId(), 2);
        createObservation(MULTIDATASTREAMS.get(0).withOnlyId(), 3);

        createObservation(MULTIDATASTREAMS.get(1).withOnlyId(), 4, 1);
        createObservation(MULTIDATASTREAMS.get(1).withOnlyId(), 5, 2);
        createObservation(MULTIDATASTREAMS.get(1).withOnlyId(), 6, 3);

        createObservation(MULTIDATASTREAMS.get(2).withOnlyId(), 7, 4);
        createObservation(MULTIDATASTREAMS.get(2).withOnlyId(), 8, 5);
        createObservation(MULTIDATASTREAMS.get(2).withOnlyId(), 9, 6);

        createObservation(MULTIDATASTREAMS.get(3).withOnlyId(), 10, 7);
        createObservation(MULTIDATASTREAMS.get(3).withOnlyId(), 11, 8);
        createObservation(MULTIDATASTREAMS.get(3).withOnlyId(), 12, 9);
        Assert.assertEquals(14, OBSERVATIONS.size());
    }

    @Test
    public void test03ObservationInMultiDatastreamIncorrect() throws ServiceFailureException {
        LOGGER.info("  test03ObservationInMultiDatastreamIncorrect");
        boolean failed = false;
        try {
            Observation o = new Observation(1, MULTIDATASTREAMS.get(1).withOnlyId());
            service.create(o);
        } catch (ServiceFailureException e) {
            failed = true;
        }
        if (!failed) {
            Assert.fail("Service should have rejected posting non-array result to a multidatastream.");
        }

        failed = false;
        try {
            createObservation(MULTIDATASTREAMS.get(0).withOnlyId(), 1, 2);
        } catch (ServiceFailureException e) {
            failed = true;
        }
        if (!failed) {
            Assert.fail("Service should have rejected posting 2 results to a multidatastream with only 1 observed property.");
        }
        failed = false;
        try {
            createObservation(MULTIDATASTREAMS.get(1).withOnlyId(), 1);
        } catch (ServiceFailureException e) {
            failed = true;
        }
        if (!failed) {
            Assert.fail("Service should have rejected posting 1 result to a multidatastream with 2 observed properties.");
        }
    }

    private JsonNode getJsonObject(String urlString) {
        urlString = urlString.replaceAll(Pattern.quote("["), "%5B").replaceAll(Pattern.quote("]"), "%5D");
        HttpResponse responseMap = HTTPMethods.doGet(urlString);
        String message = "Error getting Observations using Data Array: Code " + responseMap.code;
        Assert.assertEquals(message, 200, responseMap.code);

        JsonNode json;
        try {
            json = new ObjectMapper().readTree(responseMap.response);
        } catch (IOException ex) {
            Assert.fail("Server returned malformed JSON for request: " + urlString + " Exception: " + ex);
            return null;
        }
        if (!json.isObject()) {
            Assert.fail("Server did not return a JSON object for request: " + urlString);
        }
        return json;
    }

    private JsonNode getJsonValue(String urlString) {
        JsonNode json = getJsonObject(urlString);
        JsonNode value = json.get("value");
        if (value == null || !value.isArray()) {
            Assert.fail("value field is not an array for request: " + urlString);
        }
        return value;
    }

    private void entitiesHaveOneOf(JsonNode value, String EntityName, String... properties) {
        for (JsonNode valueItem : value) {
            if (!valueItem.isObject()) {
                Assert.fail("item in " + EntityName + " array is not an object.");
                return;
            }
            for (String property : properties) {
                if (valueItem.has(property)) {
                    return;
                }
            }
            Assert.fail("item in " + EntityName + " array does not contain any of " + Arrays.toString(properties));
        }
    }

    @Test
    public void test04Json() throws ServiceFailureException {
        LOGGER.info("  test04Json");
        JsonNode json = getJsonValue(serverSettings.serviceUrl + "/Things");
        entitiesHaveOneOf(json, "Things", "MultiDatastreams@iot.navigationLink");
        json = getJsonValue(serverSettings.serviceUrl + "/Sensors");
        entitiesHaveOneOf(json, "Sensors", "MultiDatastreams@iot.navigationLink");
        json = getJsonValue(serverSettings.serviceUrl + "/ObservedProperties");
        entitiesHaveOneOf(json, "ObservedProperties", "MultiDatastreams@iot.navigationLink");
        json = getJsonValue(serverSettings.serviceUrl + "/Observations");
        entitiesHaveOneOf(json, "Observations", "MultiDatastream@iot.navigationLink", "Datastream@iot.navigationLink");
        json = getJsonValue(serverSettings.serviceUrl + "/MultiDatastreams");
        for (String property : EntityTypeMds.MULTI_DATASTREAM.getProperties()) {
            entitiesHaveOneOf(json, "MultiDatastreams", property);
        }
        for (String relation : EntityTypeMds.MULTI_DATASTREAM.getRelations()) {
            entitiesHaveOneOf(json, "MultiDatastreams", relation + "@iot.navigationLink");
        }

        String urlString = serverSettings.serviceUrl + "/Observations(" + OBSERVATIONS.get(5).getId().getUrl() + ")/result[0]";
        json = getJsonObject(urlString);
        JsonNode value = json.get("result[0]");
        if (value == null || !value.isNumber()) {
            Assert.fail("Did not get a numeric value for result[0] for url: " + urlString);
        } else {
            String message = "Did not get correct value for url: " + urlString;
            Assert.assertEquals(message, 4, value.asInt());
        }
    }

    @Test
    public void test05MultiDatastreamThings() throws ServiceFailureException {
        LOGGER.info("  test05MultiDatastreamThings");
        // Check if all Datastreams and MultiDatastreams are linked to Thing 1.
        Thing fetchedThing = service.things().find(THINGS.get(0).getId());
        EntityList<Datastream> fetchedDatastreams = fetchedThing.datastreams().query().list();
        checkResult("Check Datastreams linked to Thing 1.", EntityUtils.resultContains(fetchedDatastreams, DATASTREAMS.get(0)));
        EntityList<MultiDatastream> fetchedMultiDatastreams = fetchedThing.multiDatastreams().query().list();
        checkResult("Check MultiDatastreams linked to Thing 1.", EntityUtils.resultContains(fetchedMultiDatastreams, new ArrayList<>(MULTIDATASTREAMS)));
    }

    @Test
    public void test06MultiDatastreamSensors() throws ServiceFailureException {
        LOGGER.info("  test06MultiDatastreamSensors");
        // Check if all Datastreams and MultiDatastreams are linked to Sensor 1.
        Sensor fetchedSensor = service.sensors().find(SENSORS.get(0).getId());
        EntityList<Datastream> fetchedDatastreams = fetchedSensor.datastreams().query().list();
        checkResult("Check Datastreams linked to Sensor 1.", EntityUtils.resultContains(fetchedDatastreams, DATASTREAMS.get(0)));
        EntityList<MultiDatastream> fetchedMultiDatastreams = fetchedSensor.multiDatastreams().query().list();
        checkResult(
                "Check MultiDatastreams linked to Sensor 1.",
                EntityUtils.resultContains(fetchedMultiDatastreams, getFromList(MULTIDATASTREAMS, 0, 1, 2)));
    }

    @Test
    public void test07MultiDatastreamObservedProperties1() throws ServiceFailureException {
        LOGGER.info("  test07MultiDatastreamObservedProperties1");
        // Check if all Datastreams and MultiDatastreams are linked to ObservedProperty 1.
        ObservedProperty fetchedObservedProp = service.observedProperties().find(OBSERVED_PROPS.get(0).getId());
        EntityList<Datastream> fetchedDatastreams = fetchedObservedProp.datastreams().query().list();
        checkResult(
                "Check Datastreams linked to ObservedProperty 1.",
                EntityUtils.resultContains(fetchedDatastreams, getFromList(DATASTREAMS, 0, 1)));
        EntityList<MultiDatastream> fetchedMultiDatastreams = fetchedObservedProp.multiDatastreams().query().list();
        checkResult(
                "Check MultiDatastreams linked to ObservedProperty 1.",
                EntityUtils.resultContains(fetchedMultiDatastreams, new ArrayList<>(MULTIDATASTREAMS)));
    }

    @Test
    public void test08MultiDatastreamObservedProperties2() throws ServiceFailureException {
        LOGGER.info("  test08MultiDatastreamObservedProperties2");
        // Check if MultiDatastreams 2 and 3 are linked to ObservedProperty 2.
        ObservedProperty fetchedObservedProp = service.observedProperties().find(OBSERVED_PROPS.get(1).getId());
        EntityList<Datastream> fetchedDatastreams = fetchedObservedProp.datastreams().query().list();
        checkResult(
                "Check Datastreams linked to ObservedProperty 2.",
                EntityUtils.resultContains(fetchedDatastreams, new ArrayList<>()));
        EntityList<MultiDatastream> fetchedMultiDatastreams = fetchedObservedProp.multiDatastreams().query().list();
        checkResult(
                "Check MultiDatastreams linked to ObservedProperty 2.",
                EntityUtils.resultContains(fetchedMultiDatastreams, getFromList(MULTIDATASTREAMS, 1, 2)));
    }

    @Test
    public void test09ObservationLinks1() throws ServiceFailureException {
        LOGGER.info("  test09ObservationLinks1");
        // First Observation should have a Datastream but not a MultiDatasteam.
        Observation fetchedObservation = service.observations().find(OBSERVATIONS.get(0).getId());
        Datastream fetchedDatastream = fetchedObservation.getDatastream();

        String message = "Observation has wrong or no Datastream";
        Assert.assertEquals(message, DATASTREAMS.get(0), fetchedDatastream);

        MultiDatastream fetchedMultiDatastream = fetchedObservation.getMultiDatastream();

        message = "Observation should not have a MultiDatastream";
        Assert.assertEquals(message, null, fetchedMultiDatastream);
    }

    @Test
    public void test10ObservationLinks2() throws ServiceFailureException {
        LOGGER.info("  test10ObservationLinks2");
        // Second Observation should not have a Datastream but a MultiDatasteam.
        Observation fetchedObservation = service.observations().find(OBSERVATIONS.get(2).getId());
        Datastream fetchedDatastream = fetchedObservation.getDatastream();

        String message = "Observation should not have a Datastream";
        Assert.assertEquals(message, null, fetchedDatastream);

        MultiDatastream fetchedMultiDatastream = fetchedObservation.getMultiDatastream();

        message = "Observation has wrong or no MultiDatastream";
        Assert.assertEquals(message, MULTIDATASTREAMS.get(0), fetchedMultiDatastream);
    }

    @Test
    public void test11ObservedPropertyOrder() throws ServiceFailureException {
        LOGGER.info("  test11ObservedPropertyOrder");
        // Check if the MultiDatastreams have the correct ObservedProperties in the correct order.
        checkObservedPropertiesFor(MULTIDATASTREAMS.get(0), OBSERVED_PROPS.get(0));
        checkObservedPropertiesFor(MULTIDATASTREAMS.get(1), OBSERVED_PROPS.get(0), OBSERVED_PROPS.get(1));
        checkObservedPropertiesFor(MULTIDATASTREAMS.get(2), OBSERVED_PROPS.get(1), OBSERVED_PROPS.get(0));
        checkObservedPropertiesFor(MULTIDATASTREAMS.get(3), OBSERVED_PROPS.get(0), OBSERVED_PROPS.get(0));
    }

    @Test
    public void test12IncorrectObservation() throws ServiceFailureException {
        LOGGER.info("  test12IncorrectObservation");
        // Try to give Observation 1 a MultiDatastream without removing the Datastream. Should give an error.
        Observation modifiedObservation = OBSERVATIONS.get(0).withOnlyId();
        modifiedObservation.setMultiDatastream(MULTIDATASTREAMS.get(0).withOnlyId());
        updateForException("Linking Observation to Datastream AND MultiDatastream.", modifiedObservation);
    }

    @Test
    public void test13IncorrectObservedProperty() throws ServiceFailureException {
        LOGGER.info("  test13IncorrectObservedProperty");
        // Try to add a MultiDatastream to an ObservedProperty. Should give an error.
        ObservedProperty modifiedObservedProp = OBSERVED_PROPS.get(1).withOnlyId();
        modifiedObservedProp.getMultiDatastreams().add(MULTIDATASTREAMS.get(0).withOnlyId());
        updateForException("Linking MultiDatastream to Observed property.", modifiedObservedProp);
    }

    @Test
    public void test14FetchObservationsByMultiDatastream() throws ServiceFailureException {
        LOGGER.info("  test14FetchObservationsByMultiDatastream");
        EntityList<Observation> observations = MULTIDATASTREAMS.get(0).observations().query().list();
        checkResult(
                "Looking for all observations",
                EntityUtils.resultContains(observations, getFromList(OBSERVATIONS, 2, 3, 4)));

        observations = MULTIDATASTREAMS.get(1).observations().query().list();
        checkResult(
                "Looking for all observations",
                EntityUtils.resultContains(observations, getFromList(OBSERVATIONS, 5, 6, 7)));

        observations = MULTIDATASTREAMS.get(2).observations().query().list();
        checkResult(
                "Looking for all observations",
                EntityUtils.resultContains(observations, getFromList(OBSERVATIONS, 8, 9, 10)));

        observations = MULTIDATASTREAMS.get(3).observations().query().list();
        checkResult(
                "Looking for all observations",
                EntityUtils.resultContains(observations, getFromList(OBSERVATIONS, 11, 12, 13)));
    }

    @Test
    public void test15Observations() throws ServiceFailureException {
        LOGGER.info("  test15Observations");
        // Check if all observations are there.
        EntityList<Observation> fetchedObservations = service.observations().query().list();
        checkResult(
                "Looking for all observations",
                EntityUtils.resultContains(fetchedObservations, new ArrayList<>(OBSERVATIONS)));
    }

    @Test
    public void test16DeleteObservedProperty() throws ServiceFailureException {
        LOGGER.info("  test16DeleteObservedProperty");
        // Deleting ObservedProperty 2 should delete MultiDatastream 2 and 3 and their Observations.
        service.delete(OBSERVED_PROPS.get(1));
        EntityList<MultiDatastream> fetchedMultiDatastreams = service.multiDatastreams().query().list();
        checkResult(
                "Checking if MultiDatastreams are automatically deleted.",
                EntityUtils.resultContains(fetchedMultiDatastreams, getFromList(MULTIDATASTREAMS, 0, 3)));
        EntityList<Observation> fetchedObservations = service.observations().query().list();
        checkResult(
                "Checking if Observations are automatically deleted.",
                EntityUtils.resultContains(fetchedObservations, getFromList(OBSERVATIONS, 0, 1, 2, 3, 4, 11, 12, 13)));
    }

    @Test
    public void test17DeleteSensor() throws ServiceFailureException {
        LOGGER.info("  test17DeleteSensor");
        // Deleting Sensor 2 should delete MultiDatastream 4
        service.delete(SENSORS.get(1));
        EntityList<MultiDatastream> fetchedMultiDatastreams = service.multiDatastreams().query().list();
        checkResult(
                "Checking if MultiDatastreams are automatically deleted.",
                EntityUtils.resultContains(fetchedMultiDatastreams, getFromList(MULTIDATASTREAMS, 0)));
    }

    @Test
    public void test18DeleteThing() throws ServiceFailureException {
        LOGGER.info("  test18DeleteThing");
        // Deleting Thing 1 should delete the last MultiDatastream.
        service.delete(THINGS.get(0));
        EntityList<MultiDatastream> fetchedMultiDatastreams = service.multiDatastreams().query().list();
        checkResult(
                "Checking if MultiDatastreams are automatically deleted.",
                EntityUtils.resultContains(fetchedMultiDatastreams, new ArrayList<>()));
    }

    public static <T extends Entity<T>> List<T> getFromList(List<T> list, int... ids) {
        List<T> result = new ArrayList<>();
        for (int i : ids) {
            result.add(list.get(i));
        }
        return result;
    }

}
