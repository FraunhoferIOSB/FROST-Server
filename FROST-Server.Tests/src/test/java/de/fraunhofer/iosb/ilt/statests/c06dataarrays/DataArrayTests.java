package de.fraunhofer.iosb.ilt.statests.c06dataarrays;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.dao.BaseDao;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.Entity;
import de.fraunhofer.iosb.ilt.sta.model.FeatureOfInterest;
import de.fraunhofer.iosb.ilt.sta.model.Id;
import de.fraunhofer.iosb.ilt.sta.model.IdLong;
import de.fraunhofer.iosb.ilt.sta.model.IdString;
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
import de.fraunhofer.iosb.ilt.statests.util.EntityType;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods;
import de.fraunhofer.iosb.ilt.statests.util.ServiceURLBuilder;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.geojson.Point;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some odd tests.
 *
 * @author Hylke van der Schaaf
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DataArrayTests {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DataArrayTests.class);
    private static final String[] OBSERVATION_PROPERTIES = new String[]{
        "id",
        "phenomenonTime",
        "result",
        "resultTime",
        "resultQuality",
        "validTime",
        "parameters"};

    private static ServerSettings serverSettings;
    private static SensorThingsService service;

    private static final List<Thing> THINGS = new ArrayList<>();
    private static final List<Location> LOCATIONS = new ArrayList<>();
    private static final List<Sensor> SENSORS = new ArrayList<>();
    private static final List<ObservedProperty> O_PROPS = new ArrayList<>();
    private static final List<Datastream> DATASTREAMS = new ArrayList<>();
    private static final List<MultiDatastream> MULTIDATASTREAMS = new ArrayList<>();
    private static final List<Observation> OBSERVATIONS = new ArrayList<>();
    private static final List<FeatureOfInterest> FEATURES = new ArrayList<>();

    @BeforeClass()
    public static void setUp() throws MalformedURLException, ServiceFailureException, URISyntaxException {
        LOGGER.info("Setting up class.");
        TestSuite suite = TestSuite.getInstance();
        serverSettings = suite.getServerSettings();
        service = new SensorThingsService(new URL(serverSettings.serviceUrl));
        createEntities();
    }

    @AfterClass
    public static void tearDown() {
        LOGGER.info("tearing down class.");
        try {
            EntityUtils.deleteAll(service);
        } catch (ServiceFailureException ex) {
            LOGGER.error("Failed to clean database.", ex);
        }
    }

    private static void createEntities() throws ServiceFailureException, URISyntaxException {
        Thing thing = new Thing("Thing 1", "The first thing.");
        service.create(thing);
        THINGS.add(thing);

        // Locations 0
        Location location = new Location("Location 1.0", "Location of Thing 1.", "application/vnd.geo+json", new Point(8, 51));
        location.getThings().add(THINGS.get(0));
        service.create(location);
        LOCATIONS.add(location);

        Sensor sensor = new Sensor("Sensor 1", "The first sensor.", "text", "Some metadata.");
        service.create(sensor);
        SENSORS.add(sensor);

        sensor = new Sensor("Sensor 2", "The second sensor.", "text", "Some metadata.");
        service.create(sensor);
        SENSORS.add(sensor);

        ObservedProperty obsProp = new ObservedProperty("Temperature", new URI("http://dbpedia.org/page/Temperature"), "The temperature of the thing.");
        service.create(obsProp);
        O_PROPS.add(obsProp);

        Datastream datastream = new Datastream("Datastream 1", "The temperature of thing 1, sensor 1.", "someType", new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
        datastream.setThing(THINGS.get(0));
        datastream.setSensor(SENSORS.get(0));
        datastream.setObservedProperty(obsProp);
        service.create(datastream);
        DATASTREAMS.add(datastream);

        datastream = new Datastream("Datastream 2", "The temperature of thing 1, sensor 2.", "someType", new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
        datastream.setThing(THINGS.get(0));
        datastream.setSensor(SENSORS.get(1));
        datastream.setObservedProperty(obsProp);
        service.create(datastream);
        DATASTREAMS.add(datastream);

        FeatureOfInterest foi = new FeatureOfInterest("Feature 1", "Feature 1 for thing 1, sensor 1", "application/vnd.geo+json", new Point(8, 51));
        service.create(foi);
        FEATURES.add(foi);

        foi = new FeatureOfInterest("Feature 2", "Feature 2 for thing 1, sensor 2", "application/vnd.geo+json", new Point(8, 51));
        service.create(foi);
        FEATURES.add(foi);

        Observation o = new Observation(1, DATASTREAMS.get(0));
        o.setPhenomenonTimeFrom(ZonedDateTime.parse("2016-01-01T01:01:01.000Z"));
        o.setFeatureOfInterest(FEATURES.get(0));
        service.create(o);
        OBSERVATIONS.add(o);

        o = new Observation(2, DATASTREAMS.get(1));
        o.setPhenomenonTimeFrom(ZonedDateTime.parse("2016-01-02T01:01:01.000Z"));
        o.setFeatureOfInterest(FEATURES.get(0));
        service.create(o);
        OBSERVATIONS.add(o);

        o = new Observation(3, DATASTREAMS.get(0));
        o.setPhenomenonTimeFrom(ZonedDateTime.parse("2016-01-03T01:01:01.000Z"));
        o.setFeatureOfInterest(FEATURES.get(1));
        service.create(o);
        OBSERVATIONS.add(o);

        o = new Observation(4, DATASTREAMS.get(1));
        o.setPhenomenonTimeFrom(ZonedDateTime.parse("2016-01-04T01:01:01.000Z"));
        o.setFeatureOfInterest(FEATURES.get(1));
        service.create(o);
        OBSERVATIONS.add(o);

        if (serverSettings.hasMultiDatastream) {
            ObservedProperty obsProp1 = new ObservedProperty("Wind speed", new URI("http://dbpedia.org/page/Wind_speed"), "The wind speed.");
            service.create(obsProp1);
            O_PROPS.add(obsProp1);

            ObservedProperty obsProp2 = new ObservedProperty("Wind direction", new URI("http://dbpedia.org/page/Wind_direction"), "The wind direction.");
            service.create(obsProp2);
            O_PROPS.add(obsProp2);

            MultiDatastream multiDatastream = new MultiDatastream();
            multiDatastream.setName("MultiDatastream 1");
            multiDatastream.setDescription("The wind at thing 1.");
            multiDatastream.addMultiObservationDataTypes("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
            multiDatastream.addMultiObservationDataTypes("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
            multiDatastream.addUnitOfMeasurement(new UnitOfMeasurement("m/s", "m/s", "m/s"));
            multiDatastream.addUnitOfMeasurement(new UnitOfMeasurement("degrees", "deg", "deg"));
            multiDatastream.setThing(THINGS.get(0));
            multiDatastream.setSensor(SENSORS.get(0));
            multiDatastream.getObservedProperties().add(obsProp1);
            multiDatastream.getObservedProperties().add(obsProp2);
            service.create(multiDatastream);
            MULTIDATASTREAMS.add(multiDatastream);

            o = new Observation(new Double[]{5.0, 45.0}, MULTIDATASTREAMS.get(0));
            o.setPhenomenonTimeFrom(ZonedDateTime.parse("2016-01-01T01:01:01.000Z"));
            o.setFeatureOfInterest(FEATURES.get(0));
            service.create(o);
            OBSERVATIONS.add(o);

            o = new Observation(new Double[]{5.0, 45.0}, MULTIDATASTREAMS.get(0));
            o.setPhenomenonTimeFrom(ZonedDateTime.parse("2016-01-02T01:01:01.000Z"));
            o.setFeatureOfInterest(FEATURES.get(0));
            service.create(o);
            OBSERVATIONS.add(o);

            o = new Observation(new Double[]{5.0, 45.0}, MULTIDATASTREAMS.get(0));
            o.setPhenomenonTimeFrom(ZonedDateTime.parse("2016-01-03T01:01:01.000Z"));
            o.setFeatureOfInterest(FEATURES.get(0));
            service.create(o);
            OBSERVATIONS.add(o);

            o = new Observation(new Double[]{6.0, 50.0}, MULTIDATASTREAMS.get(0));
            o.setPhenomenonTimeFrom(ZonedDateTime.parse("2016-01-04T01:01:01.000Z"));
            o.setFeatureOfInterest(FEATURES.get(0));
            service.create(o);
            OBSERVATIONS.add(o);
        }

    }

    public void filterAndCheck(BaseDao doa, String filter, List<? extends Entity> expected) {
        try {
            EntityList<Observation> result = doa.query().filter(filter).list();
            EntityUtils.resultTestResult check = EntityUtils.resultContains(result, expected);
            String message = "Failed on filter: " + filter + " Cause: " + check.message;
            Assert.assertTrue(message, check.testOk);
        } catch (ServiceFailureException ex) {
            LOGGER.error("Exception:", ex);
            Assert.fail("Failed to call service: " + ex.getMessage());
        }
    }

    @Test
    public void test01GetDataArray() throws ServiceFailureException {
        String urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.OBSERVATION, null, null, "?$count=true&$top=3&$resultFormat=dataArray");
        Map<String, Object> responseMap = HTTPMethods.doGet(urlString);
        String response = responseMap.get("response").toString();
        int responseCode = Integer.parseInt(responseMap.get("response-code").toString());
        String message = "Error getting Observations using Data Array: Code " + responseCode;
        Assert.assertEquals(message, 200, responseCode);

        validateGetDataArrayResponse(response, urlString, new HashSet<>(Arrays.asList(OBSERVATION_PROPERTIES)));
    }

    @Test
    public void test02GetDataArraySelect() throws ServiceFailureException {
        String urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.OBSERVATION, null, null, "?$count=true&$top=4&$resultFormat=dataArray&$select=result,phenomenonTime&$orderby=phenomenonTime%20desc");
        Map<String, Object> responseMap = HTTPMethods.doGet(urlString);
        String response = responseMap.get("response").toString();
        int responseCode = Integer.parseInt(responseMap.get("response-code").toString());
        String message = "Error getting Observations using Data Array: Code " + responseCode;
        Assert.assertEquals(message, 200, responseCode);

        validateGetDataArrayResponse(response, urlString, new HashSet<>(Arrays.asList("result", "phenomenonTime")));
    }

    private void validateGetDataArrayResponse(String response, String urlString, Set<String> requestedProperties) {
        JsonNode json;
        try {
            json = new ObjectMapper().readTree(response);
        } catch (IOException ex) {
            LOGGER.error("Exception:", ex);
            Assert.fail("Server returned malformed JSON for request: " + urlString + " Exception: " + ex.getMessage());
            return;
        }
        if (!json.isObject()) {
            Assert.fail("Server did not return a JSON object for request: " + urlString);
        }
        if (!json.has("@iot.count")) {
            Assert.fail("Object did not contain a @iot.count field for request: " + urlString);
        }
        if (!json.has("@iot.nextLink")) {
            Assert.fail("Object did not contain a @iot.nextLink field for request: " + urlString);
        }
        JsonNode value = json.get("value");
        if (value == null || !value.isArray()) {
            Assert.fail("value field is not an array for request: " + urlString);
            return;
        }
        for (JsonNode valueItem : value) {
            if (!valueItem.isObject()) {
                Assert.fail("item in value array is not an object for request: " + urlString);
                return;
            }
            if (!valueItem.has("Datastream@iot.navigationLink") && !valueItem.has("MultiDatastream@iot.navigationLink")) {
                Assert.fail("item in value array does not contain (Multi)Datastream@navigationLink for request: " + urlString);
            }
            JsonNode components = valueItem.get("components");
            if (components == null || !components.isArray()) {
                Assert.fail("components field is not an array for request: " + urlString);
                return;
            }
            Set<String> foundComponents = new HashSet<>();
            for (JsonNode component : components) {
                if (!component.isTextual()) {
                    Assert.fail("components field contains a non-string for request: " + urlString);
                    return;
                }
                String componentName = component.textValue();
                foundComponents.add(componentName);
                if (!requestedProperties.contains(componentName)) {
                    if (componentName.equals("@iot.id") && requestedProperties.contains("id")) {
                        // It's ok, id with a different name
                    } else {
                        Assert.fail("Found non-requested component '" + componentName + "' for request: " + urlString);
                    }
                }
            }
            if (components.size() != foundComponents.size()) {
                Assert.fail("components field contains duplicates for request: " + urlString);
            }
            for (String component : requestedProperties) {
                if (!foundComponents.contains(component)) {
                    if (component.equals("id") && foundComponents.contains("@iot.id")) {
                        continue;
                    }
                    Assert.fail("components field does not contain entry '" + component + "' for request: " + urlString);
                }
            }
            long claimedCount = valueItem.get("dataArray@iot.count").longValue();
            JsonNode dataArray = valueItem.get("dataArray");
            if (!dataArray.isArray()) {
                Assert.fail("dataArray field is not an array for request: " + urlString);
                return;
            }
            if (claimedCount != dataArray.size()) {
                Assert.fail("dataArray contains " + dataArray.size() + " entities. dataArray@iot.count claims '" + claimedCount + "'. Request: " + urlString);
            }
            for (JsonNode dataField : dataArray) {
                if (!dataField.isArray()) {
                    Assert.fail("dataArray contains a non-array entry for request: " + urlString);
                    return;
                }
                if (dataField.size() != components.size()) {
                    Assert.fail("dataArray contains an array entry with invalid length " + dataField.size() + " for request: " + urlString);
                    return;
                }
            }
        }
    }

    @Test
    public void test03PostDataArray() {
        Datastream ds1 = DATASTREAMS.get(0);
        Datastream ds2 = DATASTREAMS.get(1);
        FeatureOfInterest foi1 = FEATURES.get(0);
        FeatureOfInterest foi2 = FEATURES.get(1);
        // Try to create four observations
        // The second one should return "error".
        String jsonString = "[\n"
                + "  {\n"
                + "    \"Datastream\": {\n"
                + "      \"@iot.id\": " + ds1.getId().getJson() + "\n"
                + "    },\n"
                + "    \"components\": [\n"
                + "      \"phenomenonTime\",\n"
                + "      \"result\",\n"
                + "      \"FeatureOfInterest/id\"\n"
                + "    ],\n"
                + "    \"dataArray@iot.count\":2,\n"
                + "    \"dataArray\": [\n"
                + "      [\n"
                + "        \"2010-12-23T10:20:00-0700\",\n"
                + "        20,\n"
                + "        " + foi1.getId().getJson() + "\n"
                + "      ],\n"
                + "      [\n"
                + "        \"2010-12-23T10:21:00-0700\",\n"
                + "        30,\n"
                + "        \"probablyNotAValidId\"\n"
                + "      ]\n"
                + "    ]\n"
                + "  },\n"
                + "  {\n"
                + "    \"Datastream\": {\n"
                + "      \"@iot.id\": " + ds2.getId().getJson() + "\n"
                + "    },\n"
                + "    \"components\": [\n"
                + "      \"phenomenonTime\",\n"
                + "      \"result\"\n"
                + "    ],\n"
                + "    \"dataArray@iot.count\":2,\n"
                + "    \"dataArray\": [\n"
                + "      [\n"
                + "        \"2010-12-23T10:20:00-0700\",\n"
                + "        65\n"
                + "      ],\n"
                + "      [\n"
                + "        \"2010-12-23T10:21:00-0700\",\n"
                + "        60\n"
                + "      ]\n"
                + "    ]\n"
                + "  }\n"
                + "]";
        String urlString = serverSettings.serviceUrl + "/CreateObservations";
        Map<String, Object> responseMap = HTTPMethods.doPost(urlString, jsonString);
        String response = responseMap.get("response").toString();
        int responseCode = Integer.parseInt(responseMap.get("response-code").toString());
        String message = "Error posting Observations using Data Array: Code " + responseCode;
        Assert.assertEquals(message, 201, responseCode);

        JsonNode json;
        try {
            json = new ObjectMapper().readTree(response);
        } catch (IOException ex) {
            LOGGER.error("Exception:", ex);
            Assert.fail("Server returned malformed JSON for request: " + urlString + " Exception: " + ex);
            return;
        }

        if (!json.isArray()) {
            Assert.fail("Server did not return a JSON array for request: " + urlString);
        }

        int i = 0;
        for (JsonNode resultLine : json) {
            i++;
            if (!resultLine.isTextual()) {
                Assert.fail("Server returned a non-text result line for request: " + urlString);
                return;
            }
            String textValue = resultLine.textValue();
            if (textValue.toLowerCase().startsWith("error") && i != 2) {
                Assert.fail("Server returned an error for request: " + urlString);
            }
            if (!textValue.toLowerCase().startsWith("error") && i == 2) {
                Assert.fail("Server should have returned an error for non-valid id for request: " + urlString);
            }
            if (i == 2) {
                continue;
            }

            Id obsId = idFromPostResult(textValue);
            Observation obs;
            try {
                obs = service.observations().find(obsId);
            } catch (ServiceFailureException ex) {
                Assert.fail("Failed to retrieve created observation for request: " + urlString);
                return;
            }

            OBSERVATIONS.add(obs);
        }
        Observation obs7 = OBSERVATIONS.get(5);
        Observation obs8 = OBSERVATIONS.get(6);
        FeatureOfInterest foiObs7;
        FeatureOfInterest foiObs8;
        try {
            foiObs7 = obs7.getFeatureOfInterest();
            foiObs8 = obs8.getFeatureOfInterest();
        } catch (ServiceFailureException ex) {
            Assert.fail("Failed to retrieve feature of interest for created observation for request: " + urlString);
            return;
        }
        message = "Autogenerated Features of interest should be equal.";
        Assert.assertEquals(message, foiObs8.getId(), foiObs7.getId());
    }

    @Test
    public void test04PostDataArrayMultiDatastream() {
        if (!serverSettings.hasMultiDatastream) {
            return;
        }
        MultiDatastream mds1 = MULTIDATASTREAMS.get(0);
        FeatureOfInterest foi1 = FEATURES.get(0);
        // Try to create four observations
        // The second one should return "error".
        String jsonString = "[\n"
                + "  {\n"
                + "    \"MultiDatastream\": {\n"
                + "      \"@iot.id\": " + mds1.getId().getJson() + "\n"
                + "    },\n"
                + "    \"components\": [\n"
                + "      \"phenomenonTime\",\n"
                + "      \"result\"\n"
                + "    ],\n"
                + "    \"dataArray@iot.count\":2,\n"
                + "    \"dataArray\": [\n"
                + "      [\n"
                + "        \"2010-12-23T10:20:00-0700\",\n"
                + "        [5,20]\n"
                + "      ],\n"
                + "      [\n"
                + "        \"2010-12-23T10:21:00-0700\",\n"
                + "        30\n"
                + "      ]\n"
                + "    ]\n"
                + "  }"
                + "]";
        String urlString = serverSettings.serviceUrl + "/CreateObservations";
        Map<String, Object> responseMap = HTTPMethods.doPost(urlString, jsonString);
        String response = responseMap.get("response").toString();
        int responseCode = Integer.parseInt(responseMap.get("response-code").toString());
        String message = "Error posting Observations using Data Array: Code " + responseCode;
        Assert.assertEquals(message, 201, responseCode);

        JsonNode json;
        try {
            json = new ObjectMapper().readTree(response);
        } catch (IOException ex) {
            LOGGER.error("Exception:", ex);
            Assert.fail("Server returned malformed JSON for request: " + urlString + " Exception: " + ex);
            return;
        }

        if (!json.isArray()) {
            Assert.fail("Server did not return a JSON array for request: " + urlString);
        }

        int i = 0;
        for (JsonNode resultLine : json) {
            i++;
            if (!resultLine.isTextual()) {
                Assert.fail("Server returned a non-text result line for request: " + urlString);
                return;
            }
            String textValue = resultLine.textValue();
            if (textValue.toLowerCase().startsWith("error") && i != 2) {
                Assert.fail("Server returned an error for request: " + urlString);
            }
            if (!textValue.toLowerCase().startsWith("error") && i == 2) {
                Assert.fail("Server should have returned an error for non-valid id for request: " + urlString);
            }
            if (i == 2) {
                continue;
            }

            Id obsId = idFromPostResult(textValue);
            Observation obs;
            try {
                obs = service.observations().find(obsId);
            } catch (ServiceFailureException ex) {
                Assert.fail("Failed to retrieve created observation for request: " + urlString);
                return;
            }

            OBSERVATIONS.add(obs);
        }
    }

    private Id idFromPostResult(String postResultLine) {
        int pos1 = postResultLine.lastIndexOf("(") + 1;
        int pos2 = postResultLine.lastIndexOf(")");
        String part = postResultLine.substring(pos1, pos2);
        try {
            return new IdLong(Long.parseLong(part));
        } catch (NumberFormatException exc) {
            // Id was not a long, thus a String.
            if (!part.startsWith("'") || !part.endsWith("'")) {
                throw new IllegalArgumentException("Strings in urls must be quoted with single quotes.");
            }
            return new IdString(part.substring(1, part.length() - 1));
        }
    }
}
