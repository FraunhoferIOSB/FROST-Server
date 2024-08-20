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
package de.fraunhofer.iosb.ilt.statests.c06dataarrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.JsonNode;
import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerSettings;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods.HttpResponse;
import de.fraunhofer.iosb.ilt.statests.util.ServiceUrlHelper;
import de.fraunhofer.iosb.ilt.statests.util.Utils;
import de.fraunhofer.iosb.ilt.statests.util.model.EntityType;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.geojson.Point;
import org.junit.jupiter.api.AfterAll;
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

    private static final List<Entity> THINGS = new ArrayList<>();
    private static final List<Entity> LOCATIONS = new ArrayList<>();
    private static final List<Entity> SENSORS = new ArrayList<>();
    private static final List<Entity> O_PROPS = new ArrayList<>();
    private static final List<Entity> DATASTREAMS = new ArrayList<>();
    private static final List<Entity> MULTIDATASTREAMS = new ArrayList<>();
    private static final List<Entity> OBSERVATIONS = new ArrayList<>();
    private static final List<Entity> FEATURES = new ArrayList<>();

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
        Entity ds1 = DATASTREAMS.get(0);
        Entity ds2 = DATASTREAMS.get(1);
        Entity foi1 = FEATURES.get(0);
        // Try to create four observations
        // The second one should return "error".
        String jsonString = "[\n"
                + "  {\n"
                + "    \"Datastream\": {\n"
                + "      \"@iot.id\": " + Utils.quoteForJson(ds1.getPrimaryKeyValues()[0]) + "\n"
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
                + "        " + Utils.quoteForJson(foi1.getPrimaryKeyValues()[0]) + "\n"
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
                + "      \"@iot.id\": " + Utils.quoteForJson(ds2.getPrimaryKeyValues()[0]) + "\n"
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
            json = Utils.MAPPER.readTree(response);
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

            Object[] obsPk = Utils.pkFromPostResult(textValue);
            Entity obs;
            try {
                obs = sSrvc.dao(sMdl.etObservation).find(obsPk);
            } catch (ServiceFailureException ex) {
                fail("Failed to retrieve created observation for request: " + urlString);
                return;
            }

            OBSERVATIONS.add(obs);
        }
        Entity obs7 = OBSERVATIONS.get(5);
        Entity obs8 = OBSERVATIONS.get(6);
        Entity foiObs7;
        Entity foiObs8;
        try {
            foiObs7 = obs7.getProperty(sMdl.npObservationFeatureofinterest);
            foiObs8 = obs8.getProperty(sMdl.npObservationFeatureofinterest);
        } catch (ServiceFailureException ex) {
            fail("Failed to retrieve feature of interest for created observation for request: " + urlString);
            return;
        }
        message = "Autogenerated Features of interest should be equal.";
        assertArrayEquals(foiObs8.getPrimaryKeyValues(), foiObs7.getPrimaryKeyValues(), message);
    }

    @Test
    void test04PostDataArrayMultiDatastream() {
        LOGGER.info("  test04PostDataArrayMultiDatastream");
        if (!serverSettings.implementsRequirement(version, ServerSettings.MULTIDATA_REQ)) {
            return;
        }
        Entity mds1 = MULTIDATASTREAMS.get(0);
        // Try to create four observations
        // The second one should return "error".
        String jsonString = "[\n"
                + "  {\n"
                + "    \"MultiDatastream\": {\n"
                + "      \"@iot.id\": " + Utils.quoteForJson(mds1.getPrimaryKeyValues()[0]) + "\n"
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
            json = Utils.MAPPER.readTree(response);
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

            Object[] obsId = Utils.pkFromPostResult(textValue);
            Entity obs;
            try {
                obs = sSrvc.dao(sMdl.etObservation).find(obsId);
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
            json = Utils.MAPPER.readTree(response);
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
        EntityUtils.deleteAll(service);
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
        Entity thing = sMdl.newThing("Thing 1", "The first thing.");
        sSrvc.create(thing);
        THINGS.add(thing);

        // Locations 0
        Entity location = sMdl.newLocation("Location 1.0", "Location of Thing 1.", "application/vnd.geo+json", new Point(8, 51));
        location.addNavigationEntity(sMdl.npLocationThings, THINGS.get(0));
        sSrvc.create(location);
        LOCATIONS.add(location);

        Entity sensor = sMdl.newSensor("Sensor 1", "The first sensor.", "text", "Some metadata.");
        sSrvc.create(sensor);
        SENSORS.add(sensor);

        sensor = sMdl.newSensor("Sensor 2", "The second sensor.", "text", "Some metadata.");
        sSrvc.create(sensor);
        SENSORS.add(sensor);

        Entity obsProp = sMdl.newObservedProperty("Temperature", "http://dbpedia.org/page/Temperature", "The temperature of the thing.");
        sSrvc.create(obsProp);
        O_PROPS.add(obsProp);

        Entity datastream = sMdl.newDatastream("Datastream 1", "The temperature of thing 1, sensor 1.", "someType", new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
        datastream.setProperty(sMdl.npDatastreamThing, THINGS.get(0));
        datastream.setProperty(sMdl.npDatastreamSensor, SENSORS.get(0));
        datastream.setProperty(sMdl.npDatastreamObservedproperty, obsProp);
        sSrvc.create(datastream);
        DATASTREAMS.add(datastream);

        datastream = sMdl.newDatastream("Datastream 2", "The temperature of thing 1, sensor 2.", "someType", new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
        datastream.setProperty(sMdl.npDatastreamThing, THINGS.get(0));
        datastream.setProperty(sMdl.npDatastreamSensor, SENSORS.get(1));
        datastream.setProperty(sMdl.npDatastreamObservedproperty, obsProp);
        sSrvc.create(datastream);
        DATASTREAMS.add(datastream);

        Entity foi = sMdl.newFeatureOfInterest("Feature 1", "Feature 1 for thing 1, sensor 1", "application/vnd.geo+json", new Point(8, 51));
        sSrvc.create(foi);
        FEATURES.add(foi);

        foi = sMdl.newFeatureOfInterest("Feature 2", "Feature 2 for thing 1, sensor 2", "application/vnd.geo+json", new Point(8, 51));
        sSrvc.create(foi);
        FEATURES.add(foi);

        Entity o = sMdl.newObservation(1, ZonedDateTime.parse("2016-01-01T01:01:01.000Z"), DATASTREAMS.get(0));
        o.setProperty(sMdl.npObservationFeatureofinterest, FEATURES.get(0));
        sSrvc.create(o);
        OBSERVATIONS.add(o);

        o = sMdl.newObservation(2, ZonedDateTime.parse("2016-01-02T01:01:01.000Z"), DATASTREAMS.get(1));
        o.setProperty(sMdl.npObservationFeatureofinterest, FEATURES.get(0));
        sSrvc.create(o);
        OBSERVATIONS.add(o);

        o = sMdl.newObservation(3, ZonedDateTime.parse("2016-01-03T01:01:01.000Z"), DATASTREAMS.get(0));
        o.setProperty(sMdl.npObservationFeatureofinterest, FEATURES.get(1));
        sSrvc.create(o);
        OBSERVATIONS.add(o);

        o = sMdl.newObservation(4, ZonedDateTime.parse("2016-01-04T01:01:01.000Z"), DATASTREAMS.get(1));
        o.setProperty(sMdl.npObservationFeatureofinterest, FEATURES.get(1));
        sSrvc.create(o);
        OBSERVATIONS.add(o);

        if (serverSettings.implementsRequirement(version, serverSettings.MULTIDATA_REQ)) {
            Entity obsProp1 = sMdl.newObservedProperty("Wind speed", "http://dbpedia.org/page/Wind_speed", "The wind speed.");
            sSrvc.create(obsProp1);
            O_PROPS.add(obsProp1);

            Entity obsProp2 = sMdl.newObservedProperty("Wind direction", "http://dbpedia.org/page/Wind_direction", "The wind direction.");
            sSrvc.create(obsProp2);
            O_PROPS.add(obsProp2);

            Entity multiDatastream = mMdl.newMultiDatastream("MultiDatastream 1", "The wind at thing 1.",
                    new UnitOfMeasurement("m/s", "m/s", "m/s"),
                    new UnitOfMeasurement("degrees", "deg", "deg"));
            multiDatastream.setProperty(sMdl.npDatastreamThing, THINGS.get(0));
            multiDatastream.setProperty(sMdl.npDatastreamSensor, SENSORS.get(0));
            multiDatastream.addNavigationEntity(mMdl.npMultidatastreamObservedproperties, obsProp1);
            multiDatastream.addNavigationEntity(mMdl.npMultidatastreamObservedproperties, obsProp2);
            sSrvc.create(multiDatastream);
            MULTIDATASTREAMS.add(multiDatastream);

            o = mMdl.newObservation(new Double[]{5.0, 45.0}, ZonedDateTime.parse("2016-01-01T01:01:01.000Z"), MULTIDATASTREAMS.get(0))
                    .setProperty(sMdl.npObservationFeatureofinterest, FEATURES.get(0));
            sSrvc.create(o);
            OBSERVATIONS.add(o);

            o = mMdl.newObservation(new Double[]{5.0, 45.0}, ZonedDateTime.parse("2016-01-02T01:01:01.000Z"), MULTIDATASTREAMS.get(0))
                    .setProperty(sMdl.npObservationFeatureofinterest, FEATURES.get(0));
            sSrvc.create(o);
            OBSERVATIONS.add(o);

            o = mMdl.newObservation(new Double[]{5.0, 45.0}, ZonedDateTime.parse("2016-01-03T01:01:01.000Z"), MULTIDATASTREAMS.get(0))
                    .setProperty(sMdl.npObservationFeatureofinterest, FEATURES.get(0));
            sSrvc.create(o);
            OBSERVATIONS.add(o);

            o = mMdl.newObservation(new Double[]{6.0, 50.0}, ZonedDateTime.parse("2016-01-04T01:01:01.000Z"), MULTIDATASTREAMS.get(0))
                    .setProperty(sMdl.npObservationFeatureofinterest, FEATURES.get(0));
            sSrvc.create(o);
            OBSERVATIONS.add(o);
        }

    }

}
