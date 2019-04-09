package de.fraunhofer.iosb.ilt.statests.c03filtering;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.dao.BaseDao;
import de.fraunhofer.iosb.ilt.sta.jackson.ObjectMapperFactory;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.Entity;
import de.fraunhofer.iosb.ilt.sta.model.Location;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
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
 * Tests for the getting and filtering JSON properties.
 *
 * @author Hylke van der Schaaf
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JsonPropertiesTests {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonPropertiesTests.class);

    private static ServerSettings serverSettings;
    private static SensorThingsService service;

    private static final List<Thing> THINGS = new ArrayList<>();
    private static final List<Location> LOCATIONS = new ArrayList<>();
    private static final List<Sensor> SENSORS = new ArrayList<>();
    private static final List<ObservedProperty> O_PROPS = new ArrayList<>();
    private static final List<Datastream> DATASTREAMS = new ArrayList<>();
    private static final List<Observation> OBSERVATIONS = new ArrayList<>();

    @BeforeClass
    public static void setUp() throws MalformedURLException, ServiceFailureException, URISyntaxException, IOException {
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

    private static void createEntities() throws ServiceFailureException, URISyntaxException, IOException {
        for (int i = 0; i < 4; i++) {
            Map<String, Object> properties = new HashMap<>();
            properties.put("string", generateString(i, 10));
            properties.put("boolean", i % 2 == 0);
            properties.put("int", i + 8);
            properties.put("intArray", generateIntArray(i + 8, 5));
            properties.put("intIntArray", generateIntIntArray(i + 8, 3));
            properties.put("objArray", generateObjectList(i + 8, 3));
            Thing thing = new Thing("Thing " + i, "It's a thing.");
            thing.setProperties(properties);
            service.create(thing);
            THINGS.add(thing);
        }

        Location location = new Location("Location 1", "Location of Thing 1.", "application/vnd.geo+json", new Point(8, 52));
        location.getThings().add(THINGS.get(0).withOnlyId());
        service.create(location);
        LOCATIONS.add(location);

        Sensor sensor = new Sensor("Sensor 1", "The first sensor.", "text", "Some metadata.");
        service.create(sensor);
        SENSORS.add(sensor);

        ObservedProperty obsProp = new ObservedProperty("Temperature", new URI("http://ucom.org/temperature"), "The temperature of the thing.");
        service.create(obsProp);
        O_PROPS.add(obsProp);

        Datastream datastream = new Datastream("Datastream 1", "The temperature of thing 1, sensor 1.", "someType", new UnitOfMeasurement("degree celcius", "°C", "Cel"));
        datastream.setThing(THINGS.get(0));
        datastream.setSensor(sensor);
        datastream.setObservedProperty(obsProp);
        service.create(datastream);
        DATASTREAMS.add(datastream);

        ObjectMapper mapper = ObjectMapperFactory.get();
        String resultQualityObjectString = ""
                + "{\"DQ_Status\":{"
                + "  \"code\": 2,"
                + "  \"label\": \"Niveau 1\",\n"
                + "  \"comment\": \"Donnée contrôlée niveau 1 (données contrôlées)\""
                + "}}";
        String resultQualityArrayString = "[\n"
                + "    {\n"
                + "        \"nameOfMeasure\": \"DQ_Status\",\n"
                + "        \"DQ_Result\": {\n"
                + "            \"code\": 2,\n"
                + "            \"label\": \"Niveau 1\",\n"
                + "            \"comment\": \"Donnée contrôlée niveau 1 (données contrôlées)\"\n"
                + "        }\n"
                + "    },\n"
                + "    {\n"
                + "        \"nameOfMeasure\": \"DQ_Qualification\",\n"
                + "        \"DQ_Result\": {\n"
                + "            \"code\": 1,\n"
                + "            \"label\": \"Correcte\",\n"
                + "            \"comment\": \"Correcte\"\n"
                + "        }\n"
                + "    }\n"
                + "\n"
                + "]";
        TreeNode rqObject = mapper.readTree(resultQualityObjectString);
        JsonNode rqArray = mapper.readTree(resultQualityArrayString);

        for (int i = 0; i <= 12; i++) {
            Map<String, Object> parameters = new HashMap<>();
            Observation o = new Observation(i, datastream);
            parameters.put("string", generateString(i, 10));
            parameters.put("boolean", i % 2 == 0);
            parameters.put("int", i);
            parameters.put("intArray", generateIntArray(i, 5));
            parameters.put("intIntArray", generateIntIntArray(i, 3));
            parameters.put("objArray", generateObjectList(i, 3));
            o.setParameters(parameters);
            o.setResultQuality(i % 2 == 0 ? rqObject : rqArray);
            service.create(o);
            OBSERVATIONS.add(o);
        }
        {
            // 13
            Map<String, Object> parameters = new HashMap<>();
            Observation o = new Observation("badVales1", datastream);
            parameters.put("int", generateString(13, 10));
            parameters.put("string", 13 % 2 == 0);
            parameters.put("boolean", 13);
            parameters.put("objArray", generateIntArray(13, 5));
            parameters.put("intArray", generateIntIntArray(13, 3));
            parameters.put("intIntArray", generateObjectList(13, 3));
            o.setParameters(parameters);
            service.create(o);
            OBSERVATIONS.add(o);
        }
        {
            // 14
            Map<String, Object> parameters = new HashMap<>();
            Observation o = new Observation("badVales2", datastream);
            parameters.put("boolean", generateString(14, 10));
            parameters.put("int", 14 % 2 == 0);
            parameters.put("string", 14);
            parameters.put("intIntArray", generateIntArray(14, 5));
            parameters.put("objArray", generateIntIntArray(14, 3));
            parameters.put("intArray", generateObjectList(14, 3));
            o.setParameters(parameters);
            service.create(o);
            OBSERVATIONS.add(o);
        }
        {
            // 15
            Map<String, Object> parameters = new HashMap<>();
            Observation o = new Observation("badVales3", datastream);
            parameters.put("boolean", "true");
            parameters.put("int", "5");
            o.setParameters(parameters);
            service.create(o);
            OBSERVATIONS.add(o);
        }

        datastream = new Datastream("Datastream 2", "The temperature of thing 1, sensor 1.", "someType", new UnitOfMeasurement("degree Fahrenheit", "°F", "[degF]"));
        datastream.setThing(THINGS.get(0));
        datastream.setSensor(sensor);
        datastream.setObservedProperty(obsProp);
        service.create(datastream);
        DATASTREAMS.add(datastream);
    }

    /**
     * Generates a string of letters, with the given length, starting at the
     * given letter, where a=0.
     *
     * @param startLetter the starting letter (a=0).
     * @param length The length of the string to generate.
     * @return The string.
     */
    public static String generateString(int startLetter, int length) {
        StringBuilder sb = new StringBuilder();
        char curLetter = (char) ('a' + startLetter % 26);
        for (int i = 0; i < length; i++) {
            sb.append(curLetter);
            curLetter++;
            if (curLetter > 'z') {
                curLetter = 'a';
            }
        }
        return sb.toString();
    }

    /**
     * Generates an array of numbers, with the given length, starting at the
     * given number.
     *
     * @param startValue the starting number.
     * @param length The length of the array to generate.
     * @return The string.
     */
    public static int[] generateIntArray(int startValue, int length) {
        int[] value = new int[length];
        int curVal = startValue;
        for (int i = 0; i < length; i++) {
            value[i] = curVal;
            curVal++;
        }
        return value;
    }

    public static int[][] generateIntIntArray(int startValue, int length) {
        int[][] value = new int[length][];
        int curVal = startValue;
        for (int i = 0; i < length; i++) {
            value[i] = generateIntArray(curVal, length);
            curVal++;
        }
        return value;
    }

    public static List<Object> generateObjectList(int startValue, int length) {
        List<Object> value = new ArrayList<>();
        int curVal = startValue;
        for (int i = 0; i < length; i++) {
            Map<String, Object> newObject = new HashMap<>();
            newObject.put("string", generateString(curVal, 10));
            newObject.put("boolean", curVal % 2 == 0);
            newObject.put("int", curVal);
            newObject.put("intArray", generateIntArray(curVal, 3));
            value.add(newObject);
            curVal++;
        }
        return value;
    }

    public void filterAndCheck(BaseDao doa, String filter, List<? extends Entity> expected) {
        try {
            EntityList<Observation> result = doa.query().filter(filter).list();
            EntityUtils.resultTestResult check = EntityUtils.resultContains(result, expected);
            String message = "Failed on filter: " + filter + " Cause: " + check.message;
            Assert.assertTrue(message, check.testOk);
        } catch (ServiceFailureException ex) {
            Assert.fail("Failed to call service: " + ex.getMessage());
        }
    }

    /**
     * Test if deep-requests on Things/properties work.
     */
    @Test
    public void test01FetchLowLevelThingProperties() {
        String urlString = serverSettings.serviceUrl + "/Things(" + THINGS.get(0).getId().getUrl() + ")/properties/string";
        JsonNode json = getJsonObjectForResponse(urlString);
        testResponseProperty(json, "string", (String) THINGS.get(0).getProperties().get("string"), urlString);

        urlString = serverSettings.serviceUrl + "/Things(" + THINGS.get(0).getId().getUrl() + ")/properties/boolean";
        json = getJsonObjectForResponse(urlString);
        testResponseProperty(json, "boolean", (Boolean) THINGS.get(0).getProperties().get("boolean"), urlString);

        urlString = serverSettings.serviceUrl + "/Things(" + THINGS.get(0).getId().getUrl() + ")/properties/int";
        json = getJsonObjectForResponse(urlString);
        testResponseProperty(json, "int", (Integer) THINGS.get(0).getProperties().get("int"), urlString);

        urlString = serverSettings.serviceUrl + "/Things(" + THINGS.get(0).getId().getUrl() + ")/properties/intArray";
        json = getJsonObjectForResponse(urlString);
        testResponseProperty(json, "intArray", (int[]) THINGS.get(0).getProperties().get("intArray"), urlString);

        urlString = serverSettings.serviceUrl + "/Things(" + THINGS.get(0).getId().getUrl() + ")/properties/intArray[1]";
        json = getJsonObjectForResponse(urlString);
        testResponseProperty(json, "intArray[1]", ((int[]) THINGS.get(0).getProperties().get("intArray"))[1], urlString);

        urlString = serverSettings.serviceUrl + "/Things(" + THINGS.get(0).getId().getUrl() + ")/properties/intIntArray[1]";
        json = getJsonObjectForResponse(urlString);
        testResponseProperty(json, "intIntArray[1]", ((int[][]) THINGS.get(0).getProperties().get("intIntArray"))[1], urlString);

        urlString = serverSettings.serviceUrl + "/Things(" + THINGS.get(0).getId().getUrl() + ")/properties/intIntArray[0][1]";
        json = getJsonObjectForResponse(urlString);
        testResponseProperty(json, "intIntArray[0][1]", ((int[][]) THINGS.get(0).getProperties().get("intIntArray"))[0][1], urlString);

        urlString = serverSettings.serviceUrl + "/Things(" + THINGS.get(0).getId().getUrl() + ")/properties/objArray[0]/string";
        json = getJsonObjectForResponse(urlString);
        testResponseProperty(json, "string", ((List<Map<String, String>>) THINGS.get(0).getProperties().get("objArray")).get(0).get("string"), urlString);
    }

    /**
     * Test if deep-requests on Observations/parameters work.
     */
    @Test
    public void test02FetchLowLevelObservationParameters() {
        String urlString = serverSettings.serviceUrl + "/Observations(" + OBSERVATIONS.get(0).getId().getUrl() + ")/parameters/string";
        JsonNode json = getJsonObjectForResponse(urlString);
        testResponseProperty(json, "string", (String) OBSERVATIONS.get(0).getParameters().get("string"), urlString);

        urlString = serverSettings.serviceUrl + "/Observations(" + OBSERVATIONS.get(0).getId().getUrl() + ")/parameters/boolean";
        json = getJsonObjectForResponse(urlString);
        testResponseProperty(json, "boolean", (Boolean) OBSERVATIONS.get(0).getParameters().get("boolean"), urlString);

        urlString = serverSettings.serviceUrl + "/Observations(" + OBSERVATIONS.get(0).getId().getUrl() + ")/parameters/int";
        json = getJsonObjectForResponse(urlString);
        testResponseProperty(json, "int", (Integer) OBSERVATIONS.get(0).getParameters().get("int"), urlString);

        urlString = serverSettings.serviceUrl + "/Observations(" + OBSERVATIONS.get(0).getId().getUrl() + ")/parameters/intArray";
        json = getJsonObjectForResponse(urlString);
        testResponseProperty(json, "intArray", (int[]) OBSERVATIONS.get(0).getParameters().get("intArray"), urlString);

        urlString = serverSettings.serviceUrl + "/Observations(" + OBSERVATIONS.get(0).getId().getUrl() + ")/parameters/intArray[1]";
        json = getJsonObjectForResponse(urlString);
        testResponseProperty(json, "intArray[1]", ((int[]) OBSERVATIONS.get(0).getParameters().get("intArray"))[1], urlString);

        urlString = serverSettings.serviceUrl + "/Observations(" + OBSERVATIONS.get(0).getId().getUrl() + ")/parameters/intIntArray[1]";
        json = getJsonObjectForResponse(urlString);
        testResponseProperty(json, "intIntArray[1]", ((int[][]) OBSERVATIONS.get(0).getParameters().get("intIntArray"))[1], urlString);

        urlString = serverSettings.serviceUrl + "/Observations(" + OBSERVATIONS.get(0).getId().getUrl() + ")/parameters/intIntArray[0][1]";
        json = getJsonObjectForResponse(urlString);
        testResponseProperty(json, "intIntArray[0][1]", ((int[][]) OBSERVATIONS.get(0).getParameters().get("intIntArray"))[0][1], urlString);

        urlString = serverSettings.serviceUrl + "/Observations(" + OBSERVATIONS.get(0).getId().getUrl() + ")/parameters/objArray[0]/string";
        json = getJsonObjectForResponse(urlString);
        testResponseProperty(json, "string", ((List<Map<String, String>>) OBSERVATIONS.get(0).getParameters().get("objArray")).get(0).get("string"), urlString);

    }

    /**
     * Test if filtering Things/properties and Observations/parameters against a
     * string constant works.
     */
    @Test
    public void test03StringFilter() {
        filterAndCheck(service.things(), "properties/string eq '" + THINGS.get(2).getProperties().get("string") + "'", getFromList(THINGS, 2));
        filterAndCheck(service.observations(), "parameters/string eq '" + OBSERVATIONS.get(2).getParameters().get("string") + "'", getFromList(OBSERVATIONS, 2));

        filterAndCheck(service.things(), "substringof('cdefgh', properties/string)", getFromList(THINGS, 0, 1, 2));
        filterAndCheck(service.observations(), "substringof('cdefgh', parameters/string)", getFromList(OBSERVATIONS, 0, 1, 2));

        filterAndCheck(service.things(), "properties/objArray[0]/string eq 'jklmnopqrs'", getFromList(THINGS, 1));
        filterAndCheck(service.observations(), "parameters/objArray[0]/string eq 'jklmnopqrs'", getFromList(OBSERVATIONS, 9));

        filterAndCheck(service.observations(), "parameters/int eq '5'", getFromList(OBSERVATIONS, 5, 15));
    }

    /**
     * Test if filtering Things/properties and Observations/parameters against a
     * numeric constant works.
     */
    @Test
    public void test04NumberFilter() {
        filterAndCheck(service.things(), "properties/int eq " + THINGS.get(2).getProperties().get("int"), getFromList(THINGS, 2));
        filterAndCheck(service.observations(), "parameters/int eq " + OBSERVATIONS.get(2).getParameters().get("int"), getFromList(OBSERVATIONS, 2));

        filterAndCheck(service.things(), "properties/int gt 9", getFromList(THINGS, 2, 3));
        filterAndCheck(service.observations(), "parameters/int gt 8", getFromList(OBSERVATIONS, 9, 10, 11, 12));

        filterAndCheck(service.things(), "properties/int lt 9", getFromList(THINGS, 0));
        filterAndCheck(service.observations(), "parameters/int lt 8", getFromList(OBSERVATIONS, 0, 1, 2, 3, 4, 5, 6, 7));

        filterAndCheck(service.things(), "properties/intArray[1] gt 10", getFromList(THINGS, 2, 3));
        filterAndCheck(service.observations(), "parameters/intArray[1] gt 9", getFromList(OBSERVATIONS, 9, 10, 11, 12));

        filterAndCheck(service.things(), "properties/intArray[1] lt 10", getFromList(THINGS, 0));
        filterAndCheck(service.observations(), "parameters/intArray[1] lt 9", getFromList(OBSERVATIONS, 0, 1, 2, 3, 4, 5, 6, 7));

        filterAndCheck(service.things(), "properties/intIntArray[1][0] gt 10", getFromList(THINGS, 2, 3));
        filterAndCheck(service.observations(), "parameters/intIntArray[1][0] gt 9", getFromList(OBSERVATIONS, 9, 10, 11, 12));

        filterAndCheck(service.things(), "properties/objArray[1]/intArray[0] gt 10", getFromList(THINGS, 2, 3));
        filterAndCheck(service.observations(), "parameters/objArray[1]/intArray[0] gt 9", getFromList(OBSERVATIONS, 9, 10, 11, 12));
    }

    /**
     * Test if filtering Things/properties and Observations/parameters against a
     * boolean constant works.
     */
    @Test
    public void test05BooleanFilter() {
        filterAndCheck(service.things(), "properties/boolean eq " + THINGS.get(1).getProperties().get("boolean"), getFromList(THINGS, 1, 3));
        filterAndCheck(service.observations(), "parameters/boolean eq " + OBSERVATIONS.get(1).getParameters().get("boolean"), getFromList(OBSERVATIONS, 1, 3, 5, 7, 9, 11));

        filterAndCheck(service.things(), "properties/boolean", getFromList(THINGS, 0, 2));
        filterAndCheck(service.observations(), "parameters/boolean", getFromList(OBSERVATIONS, 0, 2, 4, 6, 8, 10, 12));

        filterAndCheck(service.things(), "not properties/boolean", getFromList(THINGS, 1, 3));
        filterAndCheck(service.observations(), "not parameters/boolean", getFromList(OBSERVATIONS, 1, 3, 5, 7, 9, 11));

        filterAndCheck(service.things(), "properties/objArray[1]/boolean", getFromList(THINGS, 1, 3));
        filterAndCheck(service.observations(), "parameters/objArray[1]/boolean", getFromList(OBSERVATIONS, 1, 3, 5, 7, 9, 11));
    }

    /**
     * Test if filtering on the Datastreams/unitOfMeasurement works.
     */
    @Test
    public void test06UnitOfMeasurementFilter() {
        filterAndCheck(service.datastreams(), "unitOfMeasurement/symbol eq '" + DATASTREAMS.get(0).getUnitOfMeasurement().getSymbol() + "'", getFromList(DATASTREAMS, 0));
        filterAndCheck(service.datastreams(), "unitOfMeasurement/symbol eq '" + DATASTREAMS.get(1).getUnitOfMeasurement().getSymbol() + "'", getFromList(DATASTREAMS, 1));
        filterAndCheck(service.datastreams(), "unitOfMeasurement/name eq '" + DATASTREAMS.get(0).getUnitOfMeasurement().getName() + "'", getFromList(DATASTREAMS, 0));
        filterAndCheck(service.datastreams(), "unitOfMeasurement/name eq '" + DATASTREAMS.get(1).getUnitOfMeasurement().getName() + "'", getFromList(DATASTREAMS, 1));
        filterAndCheck(service.datastreams(), "unitOfMeasurement/definition eq '" + DATASTREAMS.get(0).getUnitOfMeasurement().getDefinition() + "'", getFromList(DATASTREAMS, 0));
        filterAndCheck(service.datastreams(), "unitOfMeasurement/definition eq '" + DATASTREAMS.get(1).getUnitOfMeasurement().getDefinition() + "'", getFromList(DATASTREAMS, 1));
    }

    /**
     * Test if filtering on the Observations/resultQuality works.
     */
    @Test
    public void test20ResultQualityFilter() {
        filterAndCheck(service.observations(), "resultQuality/DQ_Status/code eq 2", getFromList(OBSERVATIONS, 0, 2, 4, 6, 8, 10, 12));
        filterAndCheck(service.observations(), "resultQuality[0]/DQ_Result/code eq 2", getFromList(OBSERVATIONS, 1, 3, 5, 7, 9, 11));
    }

    private JsonNode getJsonObjectForResponse(String urlString) {
        // Ensure [ and ] are urlEncoded.
        urlString = urlString.replaceAll(Pattern.quote("["), "%5B").replaceAll(Pattern.quote("]"), "%5D");
        HttpResponse responseMap = HTTPMethods.doGet(urlString);
        String message = "Incorrect response code (" + responseMap.code + ") for url: " + urlString;
        Assert.assertEquals(message, 200, responseMap.code);
        JsonNode json;
        try {
            json = new ObjectMapper().readTree(responseMap.response);
        } catch (IOException ex) {
            Assert.fail("Server returned malformed JSON for request: " + urlString + " Exception: " + ex.getMessage());
            return null;
        }

        if (!json.isObject()) {
            Assert.fail("Server did not return a JSON object for request: " + urlString);
        }
        return json;
    }

    private void testResponseProperty(JsonNode response, String propertyName, String expectedValue, String urlForError) {
        JsonNode value = response.get(propertyName);
        if (value == null || !value.isTextual()) {
            Assert.fail("field '" + propertyName + "' is not an string for request: " + urlForError);
            return;
        }
        String message = "field '" + propertyName + "' does not have the correct value for request: " + urlForError;
        Assert.assertEquals(message, expectedValue, value.textValue());
    }

    private void testResponseProperty(JsonNode response, String propertyName, Boolean expectedValue, String urlForError) {
        JsonNode value = response.get(propertyName);
        if (value == null || !value.isBoolean()) {
            Assert.fail("field '" + propertyName + "' is not an boolean for request: " + urlForError);
            return;
        }
        String message = "field '" + propertyName + "' does not have the correct value for request: " + urlForError;
        Assert.assertEquals(message, expectedValue, value.booleanValue());
    }

    private void testResponseProperty(JsonNode response, String propertyName, Integer expectedValue, String urlForError) {
        JsonNode value = response.get(propertyName);
        if (value == null || !value.isInt()) {
            Assert.fail("field '" + propertyName + "' is not an integer for request: " + urlForError);
            return;
        }
        String message = "field '" + propertyName + "' does not have the correct value for request: " + urlForError;
        Assert.assertEquals(message, expectedValue.intValue(), value.intValue());
    }

    private void testResponseProperty(JsonNode response, String propertyName, int[] expectedValue, String urlForError) {
        JsonNode value = response.get(propertyName);
        if (value == null || !value.isArray()) {
            Assert.fail("field '" + propertyName + "' is not an array for request: " + urlForError);
            return;
        }
        String message = "array '" + propertyName + "' does not have the correct size for request: " + urlForError;
        Assert.assertEquals(message, expectedValue.length, value.size());

        int i = 0;
        for (Iterator<JsonNode> it = value.elements(); it.hasNext();) {
            JsonNode element = it.next();
            if (!element.isInt()) {
                Assert.fail("array '" + propertyName + "' contains non-integer element '" + element.toString() + "' for request: " + urlForError);
            }
            message = "array '" + propertyName + "' contains incorrect value at position " + i + " for request: " + urlForError;
            Assert.assertEquals(message, expectedValue[i], element.intValue());
            i++;
        }
    }

    public static <T extends Entity<T>> List<T> getFromList(List<T> list, int... ids) {
        List<T> result = new ArrayList<>();
        for (int i : ids) {
            result.add(list.get(i));
        }
        return result;
    }
}
