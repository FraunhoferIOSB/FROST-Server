/*
 * Copyright 2016 Open Geospatial Consortium.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.iosb.ilt.statests.c07mqttcreate;

import de.fraunhofer.iosb.ilt.statests.ServerSettings;
import de.fraunhofer.iosb.ilt.statests.TestSuite;
import de.fraunhofer.iosb.ilt.statests.util.ControlInformation;
import de.fraunhofer.iosb.ilt.statests.util.EntityHelper;
import de.fraunhofer.iosb.ilt.statests.util.EntityType;
import de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper;
import static de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper.WAIT_AFTER_INSERT;
import static de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper.waitMillis;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public class Capability7Tests {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Capability7Tests.class);

    private static ServerSettings serverSettings;

    private static MqttHelper mqttHelper;
    private static EntityHelper entityHelper;

    @BeforeClass
    public static void setUp() {
        LOGGER.info("Setting up.");
        TestSuite suite = TestSuite.getInstance();
        serverSettings = suite.getServerSettings();

        long mqttTimeout = serverSettings.mqttTimeOut;

        entityHelper = new EntityHelper(serverSettings.serviceUrl);
        mqttHelper = new MqttHelper(serverSettings.mqttUrl, mqttTimeout);
    }

    @AfterClass
    public static void tearDown() {
        LOGGER.info("Tearing down.");
        entityHelper.deleteEverything();
    }

    @Test
    public void checkCreateObservationDirect() {
        LOGGER.info("  checkCreateObservationDirect");
        entityHelper.deleteEntityType(EntityType.OBSERVATION);
        JSONObject createdObservation = getObservation();
        mqttHelper.publish(MqttHelper.getTopic(EntityType.OBSERVATION), createdObservation.toString());

        // Give MQTT time to work.
        waitMillis(MqttHelper.WAIT_AFTER_INSERT);

        JSONObject latestObservation = entityHelper.getAnyEntity(
                EntityType.OBSERVATION,
                "$expand=Datastream($select=id),FeatureOfInterest($select=id)&$select=result,phenomenonTime,validTime,parameters");
        Assert.assertTrue(jsonEquals(latestObservation, createdObservation));
    }

    @Test
    public void checkCreateObservationViaDatastream() {
        LOGGER.info("  checkCreateObservationViaDatastream");
        entityHelper.deleteEntityType(EntityType.OBSERVATION);
        JSONObject createdObservation = getObservation();
        Object datastreamId = -1;
        try {
            datastreamId = createdObservation.getJSONObject("Datastream").get(ControlInformation.ID);
        } catch (JSONException ex) {
            LOGGER.error("Exception:", ex);
            Assert.fail("Datastream of created observation does not contain @iot.id: " + ex.getMessage());
        }
        mqttHelper.publish(MqttHelper.getTopic(EntityType.DATASTREAM, datastreamId, "Observations"), createdObservation.toString());

        // Give MQTT time to work.
        waitMillis(WAIT_AFTER_INSERT);

        JSONObject latestObservation = entityHelper.getAnyEntity(
                EntityType.OBSERVATION,
                "$expand=Datastream($select=id),FeatureOfInterest($select=id)&$select=result,phenomenonTime,validTime,parameters");
        Assert.assertTrue(jsonEquals(latestObservation, createdObservation));
    }

    @Test
    public void checkCreateObservationViaFeatureOfInterest() {
        LOGGER.info("  checkCreateObservationViaFeatureOfInterest");
        entityHelper.deleteEntityType(EntityType.OBSERVATION);
        JSONObject createdObservation = getObservation();
        Object featureOfInterestId = -1;
        try {
            featureOfInterestId = createdObservation.getJSONObject("FeatureOfInterest").get(ControlInformation.ID);
        } catch (JSONException ex) {
            LOGGER.error("Exception:", ex);
            Assert.fail("created observation does not contain @iot.id: " + ex.getMessage());
        }
        mqttHelper.publish(MqttHelper.getTopic(EntityType.FEATURE_OF_INTEREST, featureOfInterestId, "Observations"), createdObservation.toString());

        // Give MQTT time to work.
        waitMillis(WAIT_AFTER_INSERT);

        JSONObject latestObservation = entityHelper.getAnyEntity(
                EntityType.OBSERVATION,
                "$expand=Datastream($select=id),FeatureOfInterest($select=id)&$select=result,phenomenonTime,validTime,parameters");
        Assert.assertTrue(jsonEquals(latestObservation, createdObservation));
    }

    @Test
    public void checkCreateObservationWithDeepInsert() {
        LOGGER.info("  checkCreateObservationWithDeepInsert");
        entityHelper.deleteEntityType(EntityType.OBSERVATION);
        JSONObject createdObservation = getObservationWithDeepInsert();
        mqttHelper.publish(MqttHelper.getTopic(EntityType.OBSERVATION), createdObservation.toString());

        // Give MQTT time to work.
        waitMillis(WAIT_AFTER_INSERT);

        JSONObject latestObservation = entityHelper.getAnyEntity(
                EntityType.OBSERVATION,
                expandQueryFromJsonObject(createdObservation));
        Assert.assertTrue(jsonEquals(latestObservation, createdObservation));
    }

    private String expandQueryFromJsonObject(JSONObject expectedResult) {
        return expandQueryFromJsonObject(expectedResult, "&");
    }

    private String expandQueryFromJsonObject(JSONObject expectedResult, String seperator) {
        String result = "";
        List<String> selects = new ArrayList<>();
        List<String> expands = new ArrayList<>();
        Iterator iterator = expectedResult.keys();
        while (iterator.hasNext()) {
            String key = iterator.next().toString();
            EntityType relationType = null;
            try {
                relationType = EntityType.getForRelation(key);
            } catch (IllegalArgumentException ex) {

            }
            // check if navigationLink or simple property
            if (relationType != null) {
                try {
                    expands.add(key + "(" + expandQueryFromJsonObject(expectedResult.getJSONObject(key), ";") + ")");
                } catch (JSONException ex) {
                    LOGGER.error("Exception:", ex);
                    Assert.fail("JSON element addressed by navigationLink is no valid JSON object: " + ex.getMessage());
                }
            } else {
                selects.add(key);
            }
        }
        if (!selects.isEmpty()) {
            result += "$select=" + selects.stream().collect(Collectors.joining(","));
        }
        if (!expands.isEmpty()) {
            if (!result.isEmpty()) {
                result += seperator;
            }
            result += "$expand=" + expands.stream().collect(Collectors.joining(","));
        }
        return result;
    }

    private static boolean jsonEquals(JSONObject obj1, JSONObject obj2) {
        if (obj1 == obj2) {
            return true;
        }
        if (obj1 == null) {
            return false;
        }
        if (obj1.getClass() != obj2.getClass()) {
            return false;
        }
        if (obj1.length() != obj2.length()) {
            return false;
        }
        Iterator iterator = obj1.keys();
        while (iterator.hasNext()) {
            String key = iterator.next().toString();
            if (!obj2.has(key)) {
                return false;
            }
            try {
                Object val1 = obj1.get(key);
                if (val1 instanceof JSONObject) {
                    if (!jsonEquals((JSONObject) val1, (JSONObject) obj2.getJSONObject(key))) {
                        return false;
                    }
                } else if (val1 instanceof JSONArray) {
                    JSONArray arr1 = (JSONArray) val1;
                    JSONArray arr2 = obj2.getJSONArray(key);
                    if (!jsonEquals(arr1, arr2)) {
                        return false;
                    }
                } // check here for properties ending on 'time"
                else if (key.toLowerCase().endsWith("time")) {
                    if (!checkTimeEquals(val1.toString(), obj2.get(key).toString())) {
                        return false;
                    }
                } else if (!val1.equals(obj2.get(key))) {
                    return false;
                }
            } catch (JSONException ex) {
                return false;
            }
        }
        return true;
    }

    private static boolean jsonEquals(JSONArray arr1, JSONArray arr2) {
        if (arr1.length() != arr2.length()) {
            return false;
        }
        for (int i = 0; i < arr1.length(); i++) {
            Object val1 = arr1.get(i);
            if (val1 instanceof JSONObject) {
                if (!jsonEquals((JSONObject) val1, arr2.getJSONObject(i))) {
                    return false;
                }
            } else if (val1 instanceof JSONArray) {
                if (!jsonEquals((JSONArray) val1, arr2.getJSONArray(i))) {
                    return false;
                }
            } else if (!val1.equals(arr2.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkTimeEquals(String val1, String val2) {
        try {
            DateTime dateTime1 = DateTime.parse(val1);
            DateTime dateTime2 = DateTime.parse(val2);
            return dateTime1.isEqual(dateTime2);
        } catch (Exception ex) {
            // do nothing
        }
        try {
            Interval interval1 = Interval.parse(val1);
            Interval interval2 = Interval.parse(val2);
            return interval1.isEqual(interval2);
        } catch (Exception ex) {
            Assert.fail("time properies could neither be parsed as time nor as interval");
        }
        return false;
    }

    private JSONObject getObservation() {
        long value = new Random().nextLong();
        Object thingId = entityHelper.createThing();
        Object observedPropertyId = entityHelper.createObservedProperty();
        Object sensorId = entityHelper.createSensor();
        Object datastreamId = entityHelper.createDatastream(thingId, observedPropertyId, sensorId);
        Object featureOfInterestId = entityHelper.createFeatureOfInterest();
        try {
            return new JSONObject("{\n"
                    + "  \"phenomenonTime\": \"2015-03-01T02:40:00+02:00\",\n"
                    + "  \"validTime\": \"2016-01-01T01:01:01.000Z/2016-01-01T23:59:59.000Z\",\n"
                    + "  \"result\": " + value + ",\n"
                    + "  \"parameters\":{\"param1\": \"some value1\", \"param2\": \"some value2\"},\n"
                    + "  \"Datastream\":{\"@iot.id\": " + datastreamId + "},\n"
                    + "  \"FeatureOfInterest\": {\"@iot.id\": " + featureOfInterestId + "}  \n"
                    + "}");
        } catch (JSONException ex) {
            LOGGER.error("Exception:", ex);
            Assert.fail("error converting obsveration to JSON: " + ex.getMessage());
        }
        return null;
    }

    private JSONObject getObservationWithDeepInsert() {
        long value = new Random().nextLong();
        try {
            return new JSONObject("{\n"
                    + "	\"phenomenonTime\": \"2015-03-01T00:00:00.000Z\",\n"
                    + "	\"result\": " + value + ",\n"
                    + "	\"FeatureOfInterest\": {\n"
                    + "		\"name\": \"A weather station.\",\n"
                    + "		\"description\": \"A weather station for " + value + ".\",\n"
                    + "		\"encodingType\": \"application/vnd.geo+json\",\n"
                    + "		\"feature\": {\n"
                    + "			\"type\": \"Point\",\n"
                    + "			\"coordinates\": [\n"
                    + "				-114.05,\n"
                    + "				51.05\n"
                    + "			]\n"
                    + "		}\n"
                    + "	},\n"
                    + "	\"Datastream\": {\n"
                    + "		\"unitOfMeasurement\": {\n"
                    + "			\"name\": \"Celsius\",\n"
                    + "			\"symbol\": \"degC\",\n"
                    + "			\"definition\": \"http://qudt.org/vocab/unit#DegreeCelsius\"\n"
                    + "		},\n"
                    + "		\"name\": \"test datastream.\",\n"
                    + "		\"description\": \"test datastream for " + value + ".\",\n"
                    + "		\"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                    + "		\"Thing\": {\n"
                    + "			\"name\": \"Test Thing\",\n"
                    + "			\"description\": \"This is a Test Thing for " + value + "\"\n"
                    + "		},\n"
                    + "		\"ObservedProperty\": {\n"
                    + "			\"name\": \"Luminous Flux\",\n"
                    + "			\"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html#LuminousFlux\",\n"
                    + "			\"description\": \"Luminous Flux for " + value + ".\"\n"
                    + "		},\n"
                    + "		\"Sensor\": {        \n"
                    + "			\"name\": \"Acme Fluxomatic 1000\",\n"
                    + "			\"description\": \"Acme Fluxomatic for " + value + "\",\n"
                    + "			\"encodingType\": \"http://schema.org/description\",\n"
                    + "			\"metadata\": \"Light flux sensor\"\n"
                    + "		}\n"
                    + "	}\n"
                    + "}\n"
                    + "");
        } catch (JSONException ex) {
            LOGGER.error("Exception:", ex);
            Assert.fail("error converting obsveration to JSON: " + ex.getMessage());
        }
        return null;
    }
}
