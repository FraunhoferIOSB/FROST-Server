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
package de.fraunhofer.iosb.ilt.statests.f03metadata;

import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsSensingV11.EP_NAME;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsSensingV11.EP_PROPERTIES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.MapValue;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostclient.utils.CollectionsHelper;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityType;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods.HttpResponse;
import de.fraunhofer.iosb.ilt.statests.util.ServiceUrlHelper;
import de.fraunhofer.iosb.ilt.statests.util.Utils;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.geojson.Point;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public abstract class MetadataTests extends AbstractTestClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataTests.class.getName());

    private static final List<Entity> DATASTREAMS = new ArrayList<>();
    private static final List<Entity> FEATURES = new ArrayList<>();
    private static final List<Entity> LOCATIONS = new ArrayList<>();
    private static final List<Entity> OBSERVATIONS = new ArrayList<>();
    private static final List<Entity> O_PROPS = new ArrayList<>();
    private static final List<Entity> SENSORS = new ArrayList<>();
    private static final List<Entity> THINGS = new ArrayList<>();

    private static final Map<String, String> SERVER_PROPERTIES = new LinkedHashMap<>();

    static {
        SERVER_PROPERTIES.put(CoreSettings.PREFIX_EXTENSION + CoreSettings.TAG_CUSTOM_LINKS_ENABLE, "true");
    }

    public MetadataTests(ServerVersion version) {
        super(version, SERVER_PROPERTIES);
    }

    @Override
    protected void setUpVersion() throws ServiceFailureException, URISyntaxException {
        LOGGER.info("Setting up for version {}.", version.urlPart);
        createEntities();
    }

    @Override
    protected void tearDownVersion() throws ServiceFailureException {
        LOGGER.info("Cleaning up after version {}.", version.urlPart);
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
        FEATURES.clear();
        LOCATIONS.clear();
        SENSORS.clear();
        O_PROPS.clear();
        DATASTREAMS.clear();
        OBSERVATIONS.clear();
    }

    private static void createEntities() throws ServiceFailureException, URISyntaxException {
        Entity thing1 = sMdl.newThing("Thing 1", "The first thing.");
        MapValue properties = new MapValue();
        thing1.setProperty(EP_PROPERTIES, properties);
        sSrvc.create(thing1);
        THINGS.add(thing1);

        Entity thing2 = sMdl.newThing("Thing 2", "The second thing.");
        properties = CollectionsHelper.propertiesBuilder()
                .addItem("parent.Thing@iot.id", thing1.getPrimaryKeyValues()[0])
                .build();
        thing2.setProperty(EP_PROPERTIES, properties);
        sSrvc.create(thing2);
        THINGS.add(thing2);

        Entity thing3 = sMdl.newThing("Thing 3", "The third thing.");
        properties = CollectionsHelper.propertiesBuilder()
                .addItem("parent.Thing@iot.id", thing1.getPrimaryKeyValues()[0])
                .build();
        thing3.setProperty(EP_PROPERTIES, properties);
        sSrvc.create(thing3);
        THINGS.add(thing3);

        Entity thing4 = sMdl.newThing("Thing 4", "The fourth thing.");
        properties = CollectionsHelper.propertiesBuilder()
                .addItem("parent.Thing@iot.id", thing2.getPrimaryKeyValues()[0])
                .build();
        thing4.setProperty(EP_PROPERTIES, properties);
        sSrvc.create(thing4);
        THINGS.add(thing4);

        Entity location = sMdl.newLocation("Location 1.0", "Location of Thing 1.", "application/vnd.geo+json",
                new Point(8, 51));
        location.addNavigationEntity(sMdl.npLocationThings, THINGS.get(0));
        sSrvc.create(location);
        LOCATIONS.add(location);

        Entity sensor = sMdl.newSensor("Sensor 1", "The first sensor.", "text", "Some metadata.");
        sSrvc.create(sensor);
        SENSORS.add(sensor);

        sensor = sMdl.newSensor("Sensor 2", "The second sensor.", "text", "Some metadata.");
        sSrvc.create(sensor);
        SENSORS.add(sensor);

        Entity obsProp = sMdl.newObservedProperty("Temperature", "http://dbpedia.org/page/Temperature",
                "The temperature of the thing.");
        sSrvc.create(obsProp);
        O_PROPS.add(obsProp);

        Entity datastream = sMdl.newDatastream("Datastream 1", "The temperature of thing 1, sensor 1.", "someType",
                new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
        datastream.setProperty(sMdl.npDatastreamThing, THINGS.get(0));
        datastream.setProperty(sMdl.npDatastreamSensor, SENSORS.get(0));
        datastream.setProperty(sMdl.npDatastreamObservedproperty, obsProp);
        sSrvc.create(datastream);
        DATASTREAMS.add(datastream);

        datastream = sMdl.newDatastream("Datastream 2", "The temperature of thing 1, sensor 2.", "someType",
                new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
        datastream.setProperty(sMdl.npDatastreamThing, THINGS.get(0));
        datastream.setProperty(sMdl.npDatastreamSensor, SENSORS.get(1));
        datastream.setProperty(sMdl.npDatastreamObservedproperty, obsProp);
        sSrvc.create(datastream);
        DATASTREAMS.add(datastream);

        Entity foi = sMdl.newFeatureOfInterest("Feature 1", "Feature 1 for thing 1, sensor 1",
                "application/vnd.geo+json", new Point(8, 51));
        sSrvc.create(foi);
        FEATURES.add(foi);

        foi = sMdl.newFeatureOfInterest("Feature 2", "Feature 2 for thing 1, sensor 2", "application/vnd.geo+json",
                new Point(8, 51));
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
    }

    @Test
    void test01InvalidMetadata() {
        LOGGER.info("  test01InvalidMetadata");
        HttpResponse result = getEntity("invalid");
        assertEquals(400, result.code);
    }

    @Test
    void test02EntityMetadata() {
        LOGGER.info("  test02EntityMetadata");
        testEntityMetadata("full", true);
        testEntityMetadata("minimal", false);
        testEntityMetadata("off", false);
    }

    private void testEntityMetadata(String metadata, boolean hasNavigationLink) {
        HttpResponse result = getEntity(metadata);
        assertEquals(200, result.code);
        JsonNode thing = null;
        try {
            thing = Utils.MAPPER.readTree(result.response);
        } catch (JsonProcessingException ex) {
            Assertions.fail("Failed to parse " + metadata);
        }
        assertEquals(
                hasNavigationLink,
                thing.get("properties").has("parent.Thing@iot.navigationLink"),
                metadata + " metadata navigationLink");
        if (hasNavigationLink) {
            assertEquals(
                    generateSelfLink(0),
                    thing.get("properties").get("parent.Thing@iot.navigationLink").asText(),
                    metadata + " metadata navigationLink expected");
        }
    }

    private HttpResponse getEntity(String metadata) {
        String urlString = ServiceUrlHelper.buildURLString(
                serverSettings.getServiceUrl(version),
                EntityType.THING,
                THINGS.get(1).getPrimaryKeyValues()[0],
                null,
                "?$resultMetadata=" + metadata);
        HttpResponse result = HTTPMethods.doGet(urlString);
        return result;
    }

    @Test
    void test03MetadataInExpand() {
        LOGGER.info("  test03MetadataInExpand");
        testMetadataInExpand("full", true, true);
        testMetadataInExpand("minimal", false, false);
        testMetadataInExpand("none", false, false);
        testMetadataInExpand("off", false, false);
    }

    private void testMetadataInExpand(String metadata, boolean hasSelfLink, boolean hasNavigationLink) {
        String queryString = "?$filter=id%20eq%20" + Utils.quoteForUrl(THINGS.get(1).getPrimaryKeyValues()[0])
                + "&$expand=properties/parent.Thing&$resultMetadata=" + metadata;
        String urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.THING, null, null, queryString);
        HttpResponse result = HTTPMethods.doGet(urlString);
        assertEquals(200, result.code);
        JsonNode response = null;
        try {
            response = Utils.MAPPER.readTree(result.response);
        } catch (JsonProcessingException ex) {
            Assertions.fail("Failed to parse " + metadata);
        }
        assertFalse(response.has("@iot.nextLink"), metadata + " metadata nextLink");
        JsonNode thing = response.get("value").get(0);

        assertEquals(hasSelfLink, thing.has("@iot.selfLink"), metadata + " metadata selfLink");
        if (hasSelfLink) {
            assertEquals(generateSelfLink(1), thing.get("@iot.selfLink").asText(), metadata + " metadata selfLink");
        }
        JsonNode properties = thing.get("properties");
        assertEquals(hasNavigationLink,
                properties.has("parent.Thing@iot.navigationLink"),
                metadata + " metadata navigationLink");
        if (hasNavigationLink) {
            assertEquals(generateSelfLink(0),
                    properties.get("parent.Thing@iot.navigationLink").asText(),
                    metadata + " metadata navigationLink");
        }
        JsonNode parent = thing.get("properties").get("parent.Thing");
        final String expected = THINGS.get(0).getProperty(EP_NAME);
        final String actual = parent.get("name").asText();
        assertEquals(expected, actual, "parent.Thing should have expanded");
    }

    private String generateSelfLink(int thingIndex) {
        return new StringBuilder(getServerSettings().getServiceRootUrl())
                .append('/')
                .append(version.urlPart)
                .append("/Things(")
                .append(Utils.quoteForUrl(THINGS.get(thingIndex).getPrimaryKeyValues()[0]))
                .append(')')
                .toString();
    }

    @Test
    void test04PostDataArray() {
        LOGGER.info("  test04PostDataArray");
        testPostDataArray("full", true);
        testPostDataArray("minimal", true);
        testPostDataArray("none", true);
        testPostDataArray("off", false);
    }

    private void testPostDataArray(String metadata, boolean hasLinks) {
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
        String urlString = serverSettings.getServiceUrl(version) + "/CreateObservations?$resultMetadata=" + metadata;
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

            if (hasLinks) {
                Object[] obsId = Utils.pkFromPostResult(textValue);
                Entity obs;
                try {
                    obs = sSrvc.dao(sMdl.etObservation).find(obsId);
                } catch (ServiceFailureException ex) {
                    fail("Failed to retrieve created observation for request: " + urlString);
                    return;
                }
                OBSERVATIONS.add(obs);
            } else {
                assertEquals("", textValue, metadata + " metadata should remove links");
            }
        }
        if (hasLinks) {
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
            Assertions.assertArrayEquals(foiObs8.getPrimaryKeyValues(), foiObs7.getPrimaryKeyValues(), message);
        }
    }

    @Test
    void test05PostData() {
        LOGGER.info("  test05PostData");
        testPostData("full", true);
        testPostData("minimal", true);
        testPostData("none", true);
        testPostData("off", false);
    }

    private void testPostData(String metadata, boolean hasLinks) {
        String urlString = ServiceUrlHelper.buildURLString(
                serverSettings.getServiceUrl(version),
                EntityType.THING,
                null,
                null,
                "?$resultMetadata=" + metadata);
        HttpResponse responseMap = HTTPMethods.doPost(urlString,
                "{\"description\": \"thing description\",\"name\": \"thing name\"}");
        String response = responseMap.response;
        assertEquals(201, responseMap.code, "HTTP code should be 201");
        if (hasLinks) {
            assertNotNull(HTTPMethods.idFromSelfLink(response));
        } else {
            assertEquals("", response);
        }

    }

}
