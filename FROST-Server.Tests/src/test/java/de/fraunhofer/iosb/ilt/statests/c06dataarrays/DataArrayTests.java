package de.fraunhofer.iosb.ilt.statests.c06dataarrays;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
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
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityType;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods.HttpResponse;
import de.fraunhofer.iosb.ilt.statests.util.ServiceUrlHelper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.geojson.Point;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
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
public abstract class DataArrayTests extends AbstractTestClass {

    public static class Implementation10 extends DataArrayTests {

        public Implementation10() {
            super(ServerVersion.v_1_0);
        }

    }

    public static class Implementation11 extends DataArrayTests {

        public Implementation11() {
            super(ServerVersion.v_1_1);
        }

    }

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

    private static final List<Thing> THINGS = new ArrayList<>();
    private static final List<Location> LOCATIONS = new ArrayList<>();
    private static final List<Sensor> SENSORS = new ArrayList<>();
    private static final List<ObservedProperty> O_PROPS = new ArrayList<>();
    private static final List<Datastream> DATASTREAMS = new ArrayList<>();
    private static final List<MultiDatastream> MULTIDATASTREAMS = new ArrayList<>();
    private static final List<Observation> OBSERVATIONS = new ArrayList<>();
    private static final List<FeatureOfInterest> FEATURES = new ArrayList<>();

    public DataArrayTests(ServerVersion version) {
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

    @Test
    void test01GetDataArray() throws ServiceFailureException {
        LOGGER.info("  test01GetDataArray");
        String urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.OBSERVATION, null, null, "?$count=true&$top=3&$resultFormat=dataArray");
        HttpResponse responseMap = HTTPMethods.doGet(urlString);
        String message = "Error getting Observations using Data Array: Code " + responseMap.response;
        assertEquals(200, responseMap.code, message);

        validateGetDataArrayResponse(responseMap.response, urlString, new HashSet<>(Arrays.asList(OBSERVATION_PROPERTIES)));
    }

    @Test
    void test02GetDataArraySelect() throws ServiceFailureException {
        LOGGER.info("  test02GetDataArraySelect");
        String urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.OBSERVATION, null, null, "?$count=true&$top=4&$resultFormat=dataArray&$select=result,phenomenonTime&$orderby=phenomenonTime%20desc");
        HttpResponse responseMap = HTTPMethods.doGet(urlString);
        String message = "Error getting Observations using Data Array: Code " + responseMap.response;
        assertEquals(200, responseMap.code, message);

        validateGetDataArrayResponse(responseMap.response, urlString, new HashSet<>(Arrays.asList("result", "phenomenonTime")));
    }

    @Test
    void test03PostDataArray() {
        LOGGER.info("  test03PostDataArray");
        Datastream ds1 = DATASTREAMS.get(0);
        Datastream ds2 = DATASTREAMS.get(1);
        FeatureOfInterest foi1 = FEATURES.get(0);
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
                + "        \"2010-12-23T10:20:00-07:00\",\n"
                + "        20,\n"
                + "        " + foi1.getId().getJson() + "\n"
                + "      ],\n"
                + "      [\n"
                + "        \"2010-12-23T10:21:00-07:00\",\n"
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
                + "        \"2010-12-23T10:20:00-07:00\",\n"
                + "        65\n"
                + "      ],\n"
                + "      [\n"
                + "        \"2010-12-23T10:21:00-07:00\",\n"
                + "        60\n"
                + "      ]\n"
                + "    ]\n"
                + "  }\n"
                + "]";
        String urlString = serverSettings.getServiceUrl(version) + "/CreateObservations";
        HttpResponse responseMap = HTTPMethods.doPost(urlString, jsonString);
        String response = responseMap.response;
        int responseCode = responseMap.code;
        String message = "Error posting Observations using Data Array: Code " + responseCode;
        assertEquals(201, responseCode, message);

        JsonNode json;
        try {
            json = new ObjectMapper().readTree(response);
        } catch (IOException ex) {
            LOGGER.error("Exception:", ex);
            fail("Server returned malformed JSON for request: " + urlString + " Exception: " + ex);
            return;
        }

        if (!json.isArray()) {
            fail("Server did not return a JSON array for request: " + urlString);
        }

        int i = 0;
        for (JsonNode resultLine : json) {
            i++;
            if (!resultLine.isTextual()) {
                fail("Server returned a non-text result line for request: " + urlString);
                return;
            }
            String textValue = resultLine.textValue();
            if (textValue.toLowerCase().startsWith("error") && i != 2) {
                fail("Server returned an error for request: " + urlString);
            }
            if (!textValue.toLowerCase().startsWith("error") && i == 2) {
                fail("Server should have returned an error for non-valid id for request: " + urlString);
            }
            if (i == 2) {
                continue;
            }

            Id obsId = idFromPostResult(textValue);
            Observation obs;
            try {
                obs = service.observations().find(obsId);
            } catch (ServiceFailureException ex) {
                fail("Failed to retrieve created observation for request: " + urlString);
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
            fail("Failed to retrieve feature of interest for created observation for request: " + urlString);
            return;
        }
        message = "Autogenerated Features of interest should be equal.";
        assertEquals(foiObs8.getId(), foiObs7.getId(), message);
    }

    @Test
    void test04PostDataArrayMultiDatastream() {
        LOGGER.info("  test04PostDataArrayMultiDatastream");
        if (!serverSettings.implementsRequirement(version, serverSettings.MULTIDATA_REQ)) {
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
                + "        \"2010-12-23T10:20:00-07:00\",\n"
                + "        [5,20]\n"
                + "      ],\n"
                + "      [\n"
                + "        \"2010-12-23T10:21:00-07:00\",\n"
                + "        30\n"
                + "      ]\n"
                + "    ]\n"
                + "  }"
                + "]";
        String urlString = serverSettings.getServiceUrl(version) + "/CreateObservations";
        HttpResponse responseMap = HTTPMethods.doPost(urlString, jsonString);
        String response = responseMap.response;
        int responseCode = responseMap.code;
        String message = "Error posting Observations using Data Array: Code " + responseCode;
        assertEquals(201, responseCode, message);

        JsonNode json;
        try {
            json = new ObjectMapper().readTree(response);
        } catch (IOException ex) {
            LOGGER.error("Exception:", ex);
            fail("Server returned malformed JSON for request: " + urlString + " Exception: " + ex);
            return;
        }

        if (!json.isArray()) {
            fail("Server did not return a JSON array for request: " + urlString);
        }

        int i = 0;
        for (JsonNode resultLine : json) {
            i++;
            if (!resultLine.isTextual()) {
                fail("Server returned a non-text result line for request: " + urlString);
                return;
            }
            String textValue = resultLine.textValue();
            if (textValue.toLowerCase().startsWith("error") && i != 2) {
                fail("Server returned an error for request: " + urlString);
            }
            if (!textValue.toLowerCase().startsWith("error") && i == 2) {
                fail("Server should have returned an error for non-valid id for request: " + urlString);
            }
            if (i == 2) {
                continue;
            }

            Id obsId = idFromPostResult(textValue);
            Observation obs;
            try {
                obs = service.observations().find(obsId);
            } catch (ServiceFailureException ex) {
                fail("Failed to retrieve created observation for request: " + urlString);
                return;
            }

            OBSERVATIONS.add(obs);
        }
    }

    private void validateGetDataArrayResponse(String response, String urlString, Set<String> requestedProperties) {
        JsonNode json;
        try {
            json = new ObjectMapper().readTree(response);
        } catch (IOException ex) {
            LOGGER.error("Exception:", ex);
            fail("Server returned malformed JSON for request: " + urlString + " Exception: " + ex.getMessage());
            return;
        }
        if (!json.isObject()) {
            fail("Server did not return a JSON object for request: " + urlString);
        }
        if (!json.has("@iot.count")) {
            fail("Object did not contain a @iot.count field for request: " + urlString);
        }
        if (!json.has("@iot.nextLink")) {
            fail("Object did not contain a @iot.nextLink field for request: " + urlString);
        }
        JsonNode value = json.get("value");
        if (value == null || !value.isArray()) {
            fail("value field is not an array for request: " + urlString);
            return;
        }
        for (JsonNode valueItem : value) {
            if (!valueItem.isObject()) {
                fail("item in value array is not an object for request: " + urlString);
                return;
            }
            if (!valueItem.has("Datastream@iot.navigationLink") && !valueItem.has("MultiDatastream@iot.navigationLink")) {
                fail("item in value array does not contain (Multi)Datastream@navigationLink for request: " + urlString);
            }
            JsonNode components = valueItem.get("components");
            if (components == null || !components.isArray()) {
                fail("components field is not an array for request: " + urlString);
                return;
            }
            Set<String> foundComponents = new HashSet<>();
            for (JsonNode component : components) {
                if (!component.isTextual()) {
                    fail("components field contains a non-string for request: " + urlString);
                    return;
                }
                String componentName = component.textValue();
                foundComponents.add(componentName);
                if (!requestedProperties.contains(componentName)) {
                    if (componentName.equals("@iot.id") && requestedProperties.contains("id")) {
                        // It's ok, id with a different name
                    } else {
                        fail("Found non-requested component '" + componentName + "' for request: " + urlString);
                    }
                }
            }
            if (components.size() != foundComponents.size()) {
                fail("components field contains duplicates for request: " + urlString);
            }
            for (String component : requestedProperties) {
                if (!foundComponents.contains(component)) {
                    if (component.equals("id") && foundComponents.contains("@iot.id")) {
                        continue;
                    }
                    fail("components field does not contain entry '" + component + "' for request: " + urlString);
                }
            }
            long claimedCount = valueItem.get("dataArray@iot.count").longValue();
            JsonNode dataArray = valueItem.get("dataArray");
            if (!dataArray.isArray()) {
                fail("dataArray field is not an array for request: " + urlString);
                return;
            }
            if (claimedCount != dataArray.size()) {
                fail("dataArray contains " + dataArray.size() + " entities. dataArray@iot.count claims '" + claimedCount + "'. Request: " + urlString);
            }
            for (JsonNode dataField : dataArray) {
                if (!dataField.isArray()) {
                    fail("dataArray contains a non-array entry for request: " + urlString);
                    return;
                }
                if (dataField.size() != components.size()) {
                    fail("dataArray contains an array entry with invalid length " + dataField.size() + " for request: " + urlString);
                    return;
                }
            }
        }
    }

    private static void cleanup() throws ServiceFailureException {
        EntityUtils.deleteAll(version, serverSettings, service);
        THINGS.clear();
        LOCATIONS.clear();
        SENSORS.clear();
        O_PROPS.clear();
        DATASTREAMS.clear();
        MULTIDATASTREAMS.clear();
        OBSERVATIONS.clear();
        FEATURES.clear();
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

        if (serverSettings.implementsRequirement(version, serverSettings.MULTIDATA_REQ)) {
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
