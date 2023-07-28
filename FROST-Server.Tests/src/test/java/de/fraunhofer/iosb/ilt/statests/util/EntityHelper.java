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
package de.fraunhofer.iosb.ilt.statests.util;

import static de.fraunhofer.iosb.ilt.statests.util.Utils.quoteIdForJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import de.fraunhofer.iosb.ilt.statests.ServerSettings;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods.HttpResponse;
import de.fraunhofer.iosb.ilt.statests.util.mqtt.DeepInsertInfo;
import de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public class EntityHelper {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityHelper.class);

    private final String rootUri;
    private final ServerVersion version;
    private final ServerSettings serverSettings;
    private final Map<EntityType, Object> latestEntities = new HashMap<>();

    public EntityHelper(ServerVersion version, ServerSettings serverSettings) {
        this.rootUri = serverSettings.getServiceUrl(version);
        this.version = version;
        this.serverSettings = serverSettings;
    }

    private static String concatOverlapping(String s1, String s2) {
        if (!s1.contains(s2.substring(0, 1))) {
            return s1 + s2;
        }
        int idx = s2.length();
        try {
            while (!s1.endsWith(s2.substring(0, idx))) {
                idx--;
            }
        } catch (Exception e) {
        }
        return s1 + s2.substring(idx);
    }

    public void deleteEverything() {
        deleteEntityType(EntityType.FEATURE_OF_INTEREST);
        deleteEntityType(EntityType.SENSOR);
        deleteEntityType(EntityType.OBSERVED_PROPERTY);
        deleteEntityType(EntityType.HISTORICAL_LOCATION);
        deleteEntityType(EntityType.LOCATION);
        deleteEntityType(EntityType.THING);
        deleteEntityType(EntityType.DATASTREAM);
        deleteEntityType(EntityType.OBSERVATION);
        if (serverSettings.implementsRequirement(version, serverSettings.TASKING_REQ)) {
            deleteEntityType(EntityType.ACTUATOR);
            deleteEntityType(EntityType.TASKING_CAPABILITY);
            deleteEntityType(EntityType.TASK);
        }
        latestEntities.clear();
    }

    /**
     * Delete all the entities of a certain entity type
     *
     * @param entityType The entity type from EntityType enum
     */
    public void deleteEntityType(EntityType entityType) {
        JSONArray array = null;
        do {
            try {
                String urlString = ServiceUrlHelper.buildURLString(rootUri, entityType, null, null, null);
                HttpResponse responseMap = HTTPMethods.doGet(urlString);
                JSONObject result = new JSONObject(responseMap.response);
                array = result.getJSONArray("value");
                for (int i = 0; i < array.length(); i++) {
                    Object id = array.getJSONObject(i).get(ControlInformation.ID);
                    deleteEntity(entityType, id);
                }
            } catch (JSONException e) {
                LOGGER.error("Exception: ", e);
                fail("An Exception occurred during testing: " + e.getMessage());
            }
        } while (array.length() > 0);
    }

    public Object createDatastream(Object thingId, Object observedPropertyId, Object sensorId) {
        try {
            String urlParameters = "{\n"
                    + "  \"unitOfMeasurement\": {\n"
                    + "    \"name\": \"Celsius\",\n"
                    + "    \"symbol\": \"degC\",\n"
                    + "    \"definition\": \"http://qudt.org/vocab/unit#DegreeCelsius\"\n"
                    + "  },\n"
                    + "  \"name\": \"test datastream.\",\n"
                    + "  \"description\": \"test datastream.\",\n"
                    + "  \"phenomenonTime\": \"2014-03-01T13:00:00Z/2015-05-11T15:30:00Z\",\n"
                    + "  \"resultTime\": \"2014-03-01T13:00:00Z/2015-05-11T15:30:00Z\",\n"
                    + "  \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                    + "  \"Thing\": { \"@iot.id\": " + quoteIdForJson(thingId) + " },\n"
                    + "  \"ObservedProperty\":{ \"@iot.id\":" + quoteIdForJson(observedPropertyId) + "},\n"
                    + "  \"Sensor\": { \"@iot.id\": " + quoteIdForJson(sensorId) + " }\n"
                    + "}";
            JSONObject entity = postEntity(EntityType.DATASTREAM, urlParameters);
            return entity.get(ControlInformation.ID);
        } catch (JSONException e) {
            fail("An Exception occurred during testing!:\n" + e.getMessage());
        }
        return -1;
    }

    public Object createDatastreamWithDeepInsert(Object thingId) {
        try {
            String urlParameters = "{\n"
                    + "  \"unitOfMeasurement\": {\n"
                    + "    \"name\": \"Celsius\",\n"
                    + "    \"symbol\": \"degC\",\n"
                    + "    \"definition\": \"http://qudt.org/vocab/unit#DegreeCelsius\"\n"
                    + "  },\n"
                    + "  \"name\": \"test datastream.\",\n"
                    + "  \"description\": \"test datastream.\",\n"
                    + "  \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                    + "  \"Thing\": { \"@iot.id\": " + quoteIdForJson(thingId) + " },\n"
                    + "   \"ObservedProperty\": {\n"
                    + "        \"name\": \"Luminous Flux\",\n"
                    + "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html#LuminousFlux\",\n"
                    + "        \"description\": \"Luminous Flux or Luminous Power is the measure of the perceived power of light.\"\n"
                    + "   },\n"
                    + "   \"Sensor\": {        \n"
                    + "        \"name\": \"Acme Fluxomatic 1000\",\n"
                    + "        \"description\": \"Acme Fluxomatic 1000\",\n"
                    + "        \"encodingType\": \"http://schema.org/description\",\n"
                    + "        \"metadata\": \"Light flux sensor\"\n"
                    + "   },\n"
                    + "      \"Observations\": [\n"
                    + "        {\n"
                    + "          \"phenomenonTime\": \"2015-03-01T00:10:00Z\",\n"
                    + "          \"result\": 10\n"
                    + "        }\n"
                    + "      ]"
                    + "}";
            JSONObject entity = postEntity(EntityType.DATASTREAM, urlParameters);
            return entity.get(ControlInformation.ID);
        } catch (JSONException ex) {
            LOGGER.error("Exception:", ex);
            fail("An Exception occurred during testing: " + ex);
        }
        return -1l;
    }

    public Object createFeatureOfInterest() {
        try {
            String urlParameters = "{\n"
                    + "  \"name\": \"A weather station.\",\n"
                    + "  \"description\": \"A weather station.\",\n"
                    + "  \"encodingType\": \"application/vnd.geo+json\",\n"
                    + "  \"feature\": {\n"
                    + "    \"type\": \"Point\",\n"
                    + "    \"coordinates\": [\n"
                    + "      10,\n"
                    + "      10\n"
                    + "    ]\n"
                    + "  }\n"
                    + "}";
            JSONObject entity = postEntity(EntityType.FEATURE_OF_INTEREST, urlParameters);
            return entity.get(ControlInformation.ID);
        } catch (JSONException e) {
            fail("An Exception occurred during testing!:\n" + e.getMessage());
        }
        return -1;
    }

    public Object createHistoricalLocation(Object thingId, Object locationId) {
        try {
            String urlParameters = "{\n"
                    + "  \"time\": \"2015-03-01T00:40:00.000Z\",\n"
                    + "  \"Thing\":{\"@iot.id\": " + quoteIdForJson(thingId) + "},\n"
                    + "  \"Locations\": [{\"@iot.id\": " + quoteIdForJson(locationId) + "}]  \n"
                    + "}";
            JSONObject entity = postEntity(EntityType.HISTORICAL_LOCATION, urlParameters);
            return entity.get(ControlInformation.ID);
        } catch (JSONException e) {
            fail("An Exception occurred during testing!:\n" + e.getMessage());
        }
        return -1;
    }

    public Object createLocation(Object thingId) {
        try {
            String urlParameters = "{\n"
                    + "  \"name\": \"bow river\",\n"
                    + "  \"description\": \"bow river\",\n"
                    + "  \"encodingType\": \"application/vnd.geo+json\",\n"
                    + "  \"Things\":[{\"@iot.id\": " + quoteIdForJson(thingId) + "}],\n"
                    + "  \"location\": { \"type\": \"Point\", \"coordinates\": [-114.05, 51.05] }\n"
                    + "}";
            JSONObject entity = postEntity(EntityType.LOCATION, urlParameters);
            return entity.get(ControlInformation.ID);
        } catch (JSONException e) {
            fail("An Exception occurred during testing!:\n" + e.getMessage());
        }
        return -1;
    }

    public Object createObservation(Object datastreamId, Object featureOfInterstId) {
        try {
            String urlParameters = "{\n"
                    + "  \"phenomenonTime\": \"2015-03-01T00:40:00.000Z\",\n"
                    + "  \"validTime\": \"2016-01-01T02:01:01+01:00/2016-01-02T00:59:59+01:00\",\n"
                    + "  \"result\": 8,\n"
                    + "  \"parameters\":{\"param1\": \"some value1\", \"param2\": \"some value2\"},\n"
                    + "  \"Datastream\":{\"@iot.id\": " + quoteIdForJson(datastreamId) + "},\n"
                    + "  \"FeatureOfInterest\": {\"@iot.id\": " + quoteIdForJson(featureOfInterstId) + "}\n"
                    + "}";
            JSONObject entity = postEntity(EntityType.OBSERVATION, urlParameters);
            return entity.get(ControlInformation.ID);
        } catch (JSONException e) {
            fail("An Exception occurred during testing!:\n" + e.getMessage());
        }
        return -1;
    }

    public Object createObservation(Object datastreamId) {
        try {
            String urlParameters = "{\n"
                    + "  \"phenomenonTime\": \"2015-03-01T00:40:00.000Z\",\n"
                    + "  \"validTime\": \"2016-01-01T02:01:01+01:00/2016-01-02T00:59:59+01:00\",\n"
                    + "  \"result\": 8,\n"
                    + "  \"parameters\":{\"param1\": \"some value1\", \"param2\": \"some value2\"},\n"
                    + "  \"Datastream\":{\"@iot.id\": " + quoteIdForJson(datastreamId) + "}\n"
                    + "}";
            JSONObject entity = postEntity(EntityType.OBSERVATION, urlParameters);
            return entity.get(ControlInformation.ID);
        } catch (JSONException e) {
            fail("An Exception occurred during testing!:\n" + e.getMessage());
        }
        return -1;
    }

    public Object createObservationWithDeepInsert(Object datastreamId) {
        try {
            String urlParameters = "{\n"
                    + "  \"phenomenonTime\": \"2015-03-01T00:00:00Z\",\n"
                    + "  \"result\": 100,\n"
                    + "  \"FeatureOfInterest\": {\n"
                    + "  \t\"name\": \"A weather station.\",\n"
                    + "  \t\"description\": \"A weather station.\",\n"
                    + "  \t\"encodingType\": \"application/vnd.geo+json\",\n"
                    + "    \"feature\": {\n"
                    + "      \"type\": \"Point\",\n"
                    + "      \"coordinates\": [\n"
                    + "        -114.05,\n"
                    + "        51.05\n"
                    + "      ]\n"
                    + "    }\n"
                    + "  },\n"
                    + "  \"Datastream\":{\"@iot.id\": " + quoteIdForJson(datastreamId) + "}\n"
                    + "}";
            JSONObject entity = postEntity(EntityType.OBSERVATION, urlParameters);
            return entity.get(ControlInformation.ID);
        } catch (JSONException ex) {
            fail("An Exception occurred during testing: " + ex);
        }
        return -1l;
    }

    public Object createObservedProperty() {
        try {
            String urlParameters = "{\n"
                    + "  \"name\": \"DewPoint Temperature\",\n"
                    + "  \"definition\": \"http://dbpedia.org/page/Dew_point\",\n"
                    + "  \"description\": \"The dewpoint temperature is the temperature to which the air must be cooled, at constant pressure, for dew to form. As the grass and other objects near the ground cool to the dewpoint, some of the water vapor in the atmosphere condenses into liquid water on the objects.\"\n"
                    + "}";
            JSONObject entity = postEntity(EntityType.OBSERVED_PROPERTY, urlParameters);
            return entity.get(ControlInformation.ID);
        } catch (JSONException e) {
            fail("An Exception occurred during testing!:\n" + e.getMessage());
        }
        return -1;
    }

    public Object createSensor() {
        try {
            String urlParameters = "{\n"
                    + "  \"name\": \"Fuguro Barometer\",\n"
                    + "  \"description\": \"Fuguro Barometer\",\n"
                    + "  \"encodingType\": \"http://schema.org/description\",\n"
                    + "  \"metadata\": \"Barometer\"\n"
                    + "}";
            JSONObject entity = postEntity(EntityType.SENSOR, urlParameters);
            return entity.get(ControlInformation.ID);
        } catch (JSONException e) {
            fail("An Exception occurred during testing!:\n" + e.getMessage());
        }
        return -1;
    }

    public Object createThing() {
        try {
            String urlParameters = "{"
                    + "\"name\":\"Test Thing\","
                    + "\"description\":\"This is a Test Thing From TestNG\""
                    + "}";
            JSONObject entity = postEntity(EntityType.THING, urlParameters);
            return entity.get(ControlInformation.ID);
        } catch (JSONException e) {
            fail("An Exception occurred during testing!:\n" + e.getMessage());
        }
        return -1;
    }

    public Object createThingWithDeepInsert() {
        try {
            String urlParameters = "{\n"
                    + "  \"name\": \"Office Building\",\n"
                    + "  \"description\": \"Office Building\",\n"
                    + "  \"properties\": {\n"
                    + "    \"reference\": \"Third Floor\"\n"
                    + "  },\n"
                    + "  \"Locations\": [\n"
                    + "    {\n"
                    + "      \"name\": \"West Roof\",\n"
                    + "      \"description\": \"West Roof\",\n"
                    + "      \"location\": { \"type\": \"Point\", \"coordinates\": [-117.05, 51.05] },\n"
                    + "      \"encodingType\": \"application/vnd.geo+json\"\n"
                    + "    }\n"
                    + "  ],\n"
                    + "  \"Datastreams\": [\n"
                    + "    {\n"
                    + "      \"unitOfMeasurement\": {\n"
                    + "        \"name\": \"Lumen\",\n"
                    + "        \"symbol\": \"lm\",\n"
                    + "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#Lumen\"\n"
                    + "      },\n"
                    + "      \"name\": \"Light exposure.\",\n"
                    + "      \"description\": \"Light exposure.\",\n"
                    + "      \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                    + "      \"ObservedProperty\": {\n"
                    + "        \"name\": \"Luminous Flux\",\n"
                    + "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html#LuminousFlux\",\n"
                    + "        \"description\": \"Luminous Flux or Luminous Power is the measure of the perceived power of light.\"\n"
                    + "      },\n"
                    + "      \"Sensor\": {        \n"
                    + "        \"name\": \"Acme Fluxomatic 1000\",\n"
                    + "        \"description\": \"Acme Fluxomatic 1000\",\n"
                    + "        \"encodingType\": \"http://schema.org/description\",\n"
                    + "        \"metadata\": \"Light flux sensor\"\n"
                    + "      }\n"
                    + "    }\n"
                    + "  ]\n"
                    + "}";
            JSONObject entity = postEntity(EntityType.THING, urlParameters);
            return entity.get(ControlInformation.ID);
        } catch (JSONException ex) {
            LOGGER.error("Exception:", ex);
            fail("An Exception occurred during testing: " + ex.getMessage());
        }
        return -1l;
    }

    public void deleteEntity(EntityType entityType, Object id) {
        String urlString = ServiceUrlHelper.buildURLString(rootUri, entityType, id, null, null);
        HttpResponse responseMap = HTTPMethods.doDelete(urlString);
        int responseCode = responseMap.code;
        String message = "DELETE does not work properly for " + entityType + " with id " + id + ". Returned with response code " + responseCode + ".";
        assertEquals(200, responseCode, message);

        responseMap = HTTPMethods.doGet(urlString);
        responseCode = responseMap.code;
        message = "Deleted entity was not actually deleted : " + entityType + "(" + id + ").";
        assertEquals(404, responseCode, message);
    }

    public DeepInsertInfo getDeepInsertInfo(EntityType entityType) {
        DeepInsertInfo result = new DeepInsertInfo(entityType);
        switch (entityType) {
            case THING: {
                result.getSubEntityTypes().add(EntityType.LOCATION);
                result.getSubEntityTypes().add(EntityType.DATASTREAM);
                result.getSubEntityTypes().add(EntityType.OBSERVED_PROPERTY);
                result.getSubEntityTypes().add(EntityType.SENSOR);
                break;
            }
            case DATASTREAM: {
                result.getSubEntityTypes().add(EntityType.OBSERVATION);
                result.getSubEntityTypes().add(EntityType.OBSERVED_PROPERTY);
                result.getSubEntityTypes().add(EntityType.SENSOR);
                break;
            }
            case OBSERVATION: {
                result.getSubEntityTypes().add(EntityType.FEATURE_OF_INTEREST);
                break;
            }
            default:
                throw new IllegalStateException();
        }
        return result;
    }

    public JSONObject getEntity(EntityType entityType, Object id) {
        if (id == null) {
            return null;
        }
        String urlString = ServiceUrlHelper.buildURLString(rootUri, entityType, id, null, null);
        try {
            return new JSONObject(HTTPMethods.doGet(urlString).response);
        } catch (JSONException e) {
            LOGGER.error("Exception:", e);
            fail("An Exception occurred during testing!: " + e.getMessage());
            return null;
        }
    }

    public JSONObject getEntity(String relativeUrl) {
        String urlString = concatOverlapping(rootUri, relativeUrl);
        try {
            return new JSONObject(HTTPMethods.doGet(urlString).response);
        } catch (JSONException e) {
            LOGGER.error("Exception:", e);
            fail("An Exception occurred during testing!: " + e.getMessage());
            return null;
        }
    }

    public Object getLastestEntityId(EntityType entityType) {
        return latestEntities.get(entityType);
    }

    /**
     * Tries to fetch the given entity type, with the given query options,
     * retrying a maximum of retries times, waiting MqttHelper.WAIT_AFTER_INSERT
     * milliseconds between retries.
     *
     * @param entityType The entity type to fetch the first entity of.
     * @param queryOptions The query options to use while fetching.
     * @param retries The maximum number of retries.
     * @return The first entity found, or null after all retries.
     */
    public JSONObject getAnyEntity(EntityType entityType, String queryOptions, int retries) {
        String urlString = ServiceUrlHelper.buildURLString(rootUri, entityType, null, null, null) + "?$top=1";
        if (queryOptions != null && !queryOptions.isEmpty()) {
            urlString += "&" + queryOptions;
        }
        try {
            int retry = 0;
            while (retry < retries) {
                String json = HTTPMethods.doGet(urlString).response;
                JSONArray items = new JSONObject(json).getJSONArray("value");
                if (!items.isEmpty()) {
                    return items.getJSONObject(0);
                } else {
                    retry++;
                    LOGGER.debug("No data yet. Retries: {}, URL: {}", retry, urlString);
                    MqttHelper.waitMillis(MqttHelper.WAIT_AFTER_INSERT);
                }
            }
            LOGGER.error("Failed to read an entity from url after {} tries: {}", retries, urlString);
            return null;
        } catch (JSONException e) {
            LOGGER.error("Failed while reading from url {}", urlString);
            LOGGER.error("Exception:", e);
            fail("An Exception occurred during testing!: " + e.getMessage());
            return null;
        }
    }

    public Map<String, Object> getEntityChanges(EntityType entityType, List<String> selectedProperties) {
        return getEntityChanges(entityType).entrySet().stream().filter(x -> selectedProperties.contains(x.getKey())).collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
    }

    public Map<String, Object> getEntityChanges(EntityType entityType) {
        switch (entityType) {
            case THING:
                return getThingChanges();
            case DATASTREAM:
                return getDatastreamChanges();
            case FEATURE_OF_INTEREST:
                return getFeatureOfInterestChanges();
            case HISTORICAL_LOCATION:
                return getHistoricalLocationChanges();
            case LOCATION:
                return getLocationChanges();
            case OBSERVATION:
                return getObservationChanges();
            case OBSERVED_PROPERTY:
                return getObservedPropertyChanges();
            case SENSOR:
                return getSensorChanges();
            default:
                throw new IllegalStateException("Unsupported entityType '" + entityType + "'");
        }
    }

    public JSONObject patchEntity(EntityType entityType, Map<String, Object> changes, Object id) {
        String urlString = ServiceUrlHelper.buildURLString(rootUri, entityType, id, null, null);
        HttpResponse responseMap = null;
        try {
            responseMap = HTTPMethods.doPatch(urlString, new JSONObject(changes).toString());
            int responseCode = responseMap.code;
            String message = "Error during updating(PATCH) of entity " + entityType.name();
            assertEquals(200, responseCode, message);

            responseMap = HTTPMethods.doGet(urlString);
            JSONObject result = new JSONObject(responseMap.response);
            return result;

        } catch (JSONException e) {
            LOGGER.error("Response: {}", responseMap);
            LOGGER.error("Exception:", e);
            fail("An Exception occurred during testing!:\n" + e.getMessage());
            return null;
        }
    }

    public JSONObject putEntity(EntityType entityType, Map<String, Object> changes, Object id) {
        String urlString = ServiceUrlHelper.buildURLString(rootUri, entityType, id, null, null);
        try {
            JSONObject entity = getEntity(entityType, id);
            clearLinks(entity);
            for (Map.Entry<String, Object> entry : changes.entrySet()) {
                entity.put(entry.getKey(), entry.getValue());
            }
            HttpResponse responseMap = HTTPMethods.doPut(urlString, entity.toString());
            int responseCode = responseMap.code;
            String message = "Error during updating(PUT) of entity " + entityType.name() + ": " + responseMap.response;
            assertEquals(200, responseCode, message);
            responseMap = HTTPMethods.doGet(urlString);
            JSONObject result = new JSONObject(responseMap.response);
            return result;

        } catch (JSONException e) {
            LOGGER.error("Exception:", e);
            fail("An Exception occurred during testing!:\n" + e.getMessage());
            return null;
        }
    }

    public JSONObject updateEntitywithPATCH(EntityType entityType, Object id) {
        latestEntities.put(entityType, id);
        return patchEntity(entityType, getEntityChanges(entityType), id);
    }

    public JSONObject updateEntitywithPUT(EntityType entityType, Object id) {
        latestEntities.put(entityType, id);
        return putEntity(entityType, getEntityChanges(entityType), id);
    }

    private void clearLinks(Object obj) {
        if (!(obj instanceof JSONObject)) {
            return;
        }
        JSONObject entity = (JSONObject) obj;
        Iterator iterator = entity.keys();
        while (iterator.hasNext()) {
            String key = iterator.next().toString();
            if (key.contains("@")) {
                iterator.remove();
                //entity.remove(key);
            } else {
                try {
                    Object val = entity.get(key);
                    if (val instanceof JSONObject) {
                        clearLinks((JSONObject) val);
                    } else if (val instanceof JSONArray) {
                        JSONArray arr = (JSONArray) val;
                        for (int i = 0; i < arr.length(); i++) {
                            clearLinks(arr.get(i));
                        }
                    }
                } catch (JSONException ex) {
                    fail();
                }
            }
        }
    }

    private Map<String, Object> getDatastreamChanges() {
        try {
            Map<String, Object> changes = new HashMap<>();
            changes.put("name", "Data coming from sensor on ISS.");
            changes.put("description", "Data coming from sensor on ISS.");
            changes.put("observationType", "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Observation");
            changes.put("unitOfMeasurement", new JSONObject("{\"name\": \"Entropy\",\"symbol\": \"S\",\"definition\": \"http://qudt.org/vocab/unit#Entropy\"}"));
            return changes;
        } catch (JSONException ex) {
            LOGGER.error("Exception:", ex);
            fail("Generating Datastream changes failed: " + ex.getMessage());
        }
        throw new IllegalStateException();
    }

    private Map<String, Object> getFeatureOfInterestChanges() {
        try {
            Map<String, Object> changes = new HashMap<>();
            changes.put("encodingType", "SQUARE");
            changes.put("feature", new JSONObject("{ \"type\": \"Point\", \"coordinates\": [-114.05, 51.05] }"));
            changes.put("name", "POIUYTREW");
            changes.put("description", "POIUYTREW");
            return changes;
        } catch (JSONException ex) {
            LOGGER.error("Exception:", ex);
            fail("Generating FeatureOfInterest changes failed: " + ex.getMessage());
        }
        throw new IllegalStateException();
    }

    private Map<String, Object> getHistoricalLocationChanges() {
        Map<String, Object> changes = new HashMap<>();
        changes.put("time", "2015-08-01T00:00:00.000Z");
        return changes;
    }

    private Map<String, Object> getLocationChanges() {
        try {
            Map<String, Object> changes = new HashMap<>();
            changes.put("encodingType", "UPDATED ENCODING");
            changes.put("name", "UPDATED NAME");
            changes.put("description", "UPDATED DESCRIPTION");
            changes.put("location", new JSONObject("{ \"type\": \"Point\", \"coordinates\": [-114.05, 50] }}"));
            return changes;
        } catch (JSONException ex) {
            LOGGER.error("Exception:", ex);
            fail("Generating Location changes failed: " + ex.getMessage());
        }
        throw new IllegalStateException();
    }

    private Map<String, Object> getObservationChanges() {
        try {
            Map<String, Object> changes = new HashMap<>();
            changes.put("result", "99");
            changes.put("phenomenonTime", "2015-08-01T00:40:00.000Z");
            changes.put("resultTime", "2015-12-12T12:12:12.000Z");
            changes.put("validTime", "2016-12-12T12:12:12+01:00/2016-12-12T23:59:59+01:00");
            changes.put("parameters", new JSONObject("{\"param1\": \"some updated value1\", \"param2\": \"some updated value2\"}"));

            return changes;
        } catch (JSONException ex) {
            LOGGER.error("Exception:", ex);
            fail("Generating Observation changes failed: " + ex.getMessage());
        }
        throw new IllegalStateException();
    }

    private Map<String, Object> getObservedPropertyChanges() {
        Map<String, Object> changes = new HashMap<>();
        changes.put("name", "QWERTY");
        changes.put("definition", "ZXCVB");
        changes.put("description", "POIUYTREW");
        return changes;
    }

    private Map<String, Object> getSensorChanges() {
        Map<String, Object> changes = new HashMap<>();
        changes.put("name", "UPDATED");
        changes.put("description", "UPDATED");
        changes.put("encodingType", "http://schema.org/newDescription");
        changes.put("metadata", "UPDATED");
        return changes;
    }

    private Map<String, Object> getThingChanges() {
        Map<String, Object> changes = new HashMap<>();
        changes.put("name", "This is a Updated Test Thing From TestNG");
        changes.put("description", "This is a Updated Test Thing From TestNG");
        return changes;
    }

    public Map<String, Object> getThingChangesLocation(Object locationId) {
        Map<String, Object> location = new HashMap<>();
        location.put("@iot.id", locationId);
        List<Object> locations = new ArrayList<>();
        locations.add(location);
        Map<String, Object> changes = new HashMap<>();
        changes.put("Locations", locations);
        return changes;
    }

    private JSONObject postEntity(EntityType entityType, String urlParameters) {
        String urlString = ServiceUrlHelper.buildURLString(rootUri, entityType, null, null, null);
        try {
            HttpResponse responseMap = HTTPMethods.doPost(urlString, urlParameters);
            int responseCode = responseMap.code;

            String message = "Error during creation of entity " + entityType.name();
            assertEquals(201, responseCode, message);

            String response = responseMap.response;
            Object id = response.substring(response.indexOf("(") + 1, response.indexOf(")"));
            urlString = urlString + "(" + id + ")";
            responseMap = HTTPMethods.doGet(urlString);
            responseCode = responseMap.code;

            message = "The POSTed entity is not created.";
            assertEquals(200, responseCode, message);

            JSONObject result = new JSONObject(responseMap.response);
            return result;
        } catch (JSONException e) {
            fail("An Exception occurred during testing!:\n" + e.getMessage());
            return null;
        }
    }

}
