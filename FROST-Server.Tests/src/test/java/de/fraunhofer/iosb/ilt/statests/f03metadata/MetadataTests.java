/*
 * Copyright (C) 2021 Meo Limited.
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

import de.fraunhofer.iosb.ilt.frostserver.path.UrlHelper;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.FeatureOfInterest;
import de.fraunhofer.iosb.ilt.sta.model.Id;
import de.fraunhofer.iosb.ilt.sta.model.IdLong;
import de.fraunhofer.iosb.ilt.sta.model.IdString;
import de.fraunhofer.iosb.ilt.sta.model.Location;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.geojson.Point;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**

 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MetadataTests extends AbstractTestClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataTests.class.getName());

    private static final List<Datastream> DATASTREAMS = new ArrayList<>();
    private static final List<FeatureOfInterest> FEATURES = new ArrayList<>();
    private static final List<Location> LOCATIONS = new ArrayList<>();
    private static final List<Observation> OBSERVATIONS = new ArrayList<>();
    private static final List<ObservedProperty> O_PROPS = new ArrayList<>();
    private static final List<Sensor> SENSORS = new ArrayList<>();
    private static final List<Thing> THINGS = new ArrayList<>();
    private static final Properties SERVER_PROPERTIES = new Properties();

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

    @AfterClass
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
        Thing thing1 = new Thing("Thing 1", "The first thing.");
        Map<String, Object> properties = new HashMap<>();
        thing1.setProperties(properties);
        service.create(thing1);
        THINGS.add(thing1);

        Thing thing2 = new Thing("Thing 2", "The second thing.");
        properties = new HashMap<>();
        properties.put("parent.Thing@iot.id", thing1.getId().getValue());
        thing2.setProperties(properties);
        service.create(thing2);
        THINGS.add(thing2);

        Thing thing3 = new Thing("Thing 3", "The third thing.");
        properties = new HashMap<>();
        properties.put("parent.Thing@iot.id", thing1.getId().getValue());
        thing3.setProperties(properties);
        service.create(thing3);
        THINGS.add(thing3);

        Thing thing4 = new Thing("Thing 4", "The fourth thing.");
        properties = new HashMap<>();
        properties.put("parent.Thing@iot.id", thing2.getId().getValue());
        thing4.setProperties(properties);
        service.create(thing4);
        THINGS.add(thing4);

        Location location = new Location("Location 1.0", "Location of Thing 1.", "application/vnd.geo+json",
                new Point(8, 51));
        location.getThings().add(THINGS.get(0));
        service.create(location);
        LOCATIONS.add(location);

        Sensor sensor = new Sensor("Sensor 1", "The first sensor.", "text", "Some metadata.");
        service.create(sensor);
        SENSORS.add(sensor);

        sensor = new Sensor("Sensor 2", "The second sensor.", "text", "Some metadata.");
        service.create(sensor);
        SENSORS.add(sensor);

        ObservedProperty obsProp = new ObservedProperty("Temperature", new URI("http://dbpedia.org/page/Temperature"),
                "The temperature of the thing.");
        service.create(obsProp);
        O_PROPS.add(obsProp);

        Datastream datastream = new Datastream("Datastream 1", "The temperature of thing 1, sensor 1.", "someType",
                new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
        datastream.setThing(THINGS.get(0));
        datastream.setSensor(SENSORS.get(0));
        datastream.setObservedProperty(obsProp);
        service.create(datastream);
        DATASTREAMS.add(datastream);

        datastream = new Datastream("Datastream 2", "The temperature of thing 1, sensor 2.", "someType",
                new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
        datastream.setThing(THINGS.get(0));
        datastream.setSensor(SENSORS.get(1));
        datastream.setObservedProperty(obsProp);
        service.create(datastream);
        DATASTREAMS.add(datastream);

        FeatureOfInterest foi = new FeatureOfInterest("Feature 1", "Feature 1 for thing 1, sensor 1",
                "application/vnd.geo+json", new Point(8, 51));
        service.create(foi);
        FEATURES.add(foi);

        foi = new FeatureOfInterest("Feature 2", "Feature 2 for thing 1, sensor 2", "application/vnd.geo+json",
                new Point(8, 51));
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
    }

    @Test
    public void test01InvalidMetadata() {
        LOGGER.info("  test01InvalidMetadata");
        HttpResponse result = getEntity("invalid");
        assertEquals(400, result.code);
    }

    @Test
    public void test02EntityMetadata() {
        LOGGER.info("  test02EntityMetadata");
        testEntityMetadata("full", true);
        testEntityMetadata("minimal", false);
        testEntityMetadata("off", false);
    }

    private void testEntityMetadata(String metadata, boolean hasNavigationLink) {
        HttpResponse result = getEntity(metadata);
        assertEquals(200, result.code);
        JSONObject thing = new JSONObject(result.response);
        assertEquals(metadata + " metadata navigationLink", hasNavigationLink,
                thing.getJSONObject("properties").has("parent.Thing@iot.navigationLink"));
        if (hasNavigationLink) {
            assertEquals(metadata + " metadata navigationLink expected", generateSelfLink(0),
                    thing.getJSONObject("properties").get("parent.Thing@iot.navigationLink"));
        }
    }

    private HttpResponse getEntity(String metadata) {
        String urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.THING,
                THINGS.get(1).getId().getValue(), null, "?$resultMetadata=" + metadata);
        HttpResponse result = HTTPMethods.doGet(urlString);
        return result;
    }

    @Test
    public void test03MetadataInExpand() {
        LOGGER.info("  test03MetadataInExpand");
        testMetadataInExpand("full", true, true);
        testMetadataInExpand("minimal", false, false);
        testMetadataInExpand("none", false, false);
        testMetadataInExpand("off", false, false);
    }

    private void testMetadataInExpand(String metadata, boolean hasSelfLink, boolean hasNavigationLink) {
        String urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.THING,
                null, null, "?$filter=id%20eq%20" + THINGS.get(1).getId().getUrl()
                        + "&$expand=properties/parent.Thing&$resultMetadata=" + metadata);
        HttpResponse result = HTTPMethods.doGet(urlString);
        assertEquals(200, result.code);
        JSONObject response = new JSONObject(result.response);
        assertFalse(metadata + " metadata nextLink", response.has("@iot.nextLink"));
        JSONObject thing = response.getJSONArray("value").getJSONObject(0);

        assertEquals(metadata + " metadata selfLink", hasSelfLink, thing.has("@iot.selfLink"));
        if (hasSelfLink) {
            assertEquals(metadata + " metadata selfLink", generateSelfLink(1), thing.get("@iot.selfLink"));
        }
        JSONObject properties = thing.getJSONObject("properties");
        assertEquals(metadata + " metadata navigationLink", hasNavigationLink,
                properties.has("parent.Thing@iot.navigationLink"));
        if (hasNavigationLink) {
            assertEquals(metadata + " metadata navigationLink", generateSelfLink(0),
                    properties.get("parent.Thing@iot.navigationLink"));
        }
        JSONObject parent = thing.getJSONObject("properties").getJSONObject("parent.Thing");
        assertEquals("parent.Thing should have expanded", THINGS.get(0).getName(), parent.get("name"));
    }

    private String generateSelfLink(int thingIndex) {
        return UrlHelper.generateSelfLink(null,
                getServerSettings().getServiceRootUrl(),
                Version.forString(version.urlPart),
                de.fraunhofer.iosb.ilt.frostserver.model.EntityType.THING,
                THINGS.get(thingIndex).getId().getValue());
    }

    @Test
    public void test04PostDataArray() {
        LOGGER.info("  test04PostDataArray");
        testPostDataArray("full", true);
        testPostDataArray("minimal", true);
        testPostDataArray("none", true);
        testPostDataArray("off", false);
    }

    private void testPostDataArray(String metadata, boolean hasLinks) {
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
        String urlString = serverSettings.getServiceUrl(version) + "/CreateObservations?$resultMetadata=" + metadata;
        HttpResponse responseMap = HTTPMethods.doPost(urlString, jsonString);
        String response = responseMap.response;
        int responseCode = responseMap.code;
        String message = "Error posting Observations using Data Array: Code " + responseCode;
        assertEquals(message, 201, responseCode);

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

            if (hasLinks) {
                Id<?> obsId = idFromPostResult(textValue);
                Observation obs;
                try {
                    obs = service.observations().find(obsId);
                } catch (ServiceFailureException ex) {
                    fail("Failed to retrieve created observation for request: " + urlString);
                    return;
                }
                OBSERVATIONS.add(obs);
            } else {
                assertEquals(metadata + " metadata should remove links", "", textValue);
            }
        }
        if (hasLinks) {
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
            assertEquals(message, foiObs8.getId(), foiObs7.getId());
        }
    }

    private Id<?> idFromPostResult(String postResultLine) {
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

    @Test
    public void test05PostData() {
        LOGGER.info("  test05PostData");
        testPostData("full", true);
        testPostData("minimal", true);
        testPostData("none", true);
        testPostData("off", false);
    }

    private void testPostData(String metadata, boolean hasLinks) {
        String urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.THING,
                null, null, "?$resultMetadata=" + metadata);
        HttpResponse responseMap = HTTPMethods.doPost(urlString,
                "{\"description\": \"thing description\",\"name\": \"thing name\"}");
        String response = responseMap.response;
        Assert.assertEquals("HTTP code should be 201", 201, responseMap.code);
        if (hasLinks) {
            Assert.assertNotNull(HTTPMethods.idFromSelfLink(response));
        } else {
            Assert.assertEquals("", response);
        }

    }

}
