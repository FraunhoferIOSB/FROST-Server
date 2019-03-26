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
package de.fraunhofer.iosb.ilt.statests.util.mqtt;

import de.fraunhofer.iosb.ilt.statests.util.EntityType;
import static de.fraunhofer.iosb.ilt.statests.util.EntityType.DATASTREAM;
import static de.fraunhofer.iosb.ilt.statests.util.EntityType.FEATURE_OF_INTEREST;
import static de.fraunhofer.iosb.ilt.statests.util.EntityType.HISTORICAL_LOCATION;
import static de.fraunhofer.iosb.ilt.statests.util.EntityType.LOCATION;
import static de.fraunhofer.iosb.ilt.statests.util.EntityType.OBSERVATION;
import static de.fraunhofer.iosb.ilt.statests.util.EntityType.OBSERVED_PROPERTY;
import static de.fraunhofer.iosb.ilt.statests.util.EntityType.SENSOR;
import static de.fraunhofer.iosb.ilt.statests.util.EntityType.THING;
import de.fraunhofer.iosb.ilt.statests.util.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONObject;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public class MqttHelper {

    /**
     * The number of milliseconds to wait after an insert, to give the server
     * time to process it.
     */
    public static final int WAIT_AFTER_INSERT = 500;
    public static final int QOS = 2;
    public static final String CLIENT_ID = "STA-test_suite";
    public static final String MQTT_TOPIC_PREFIX = "v1.0/";
    private final String mqttServerUri;
    private final long mqttTimeout;

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MqttHelper.class);

    public static void waitMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            // rude wakeup
        }
    }

    public MqttHelper(String mqttServerUri, long mqttTimeout) {
        this.mqttServerUri = mqttServerUri;
        this.mqttTimeout = mqttTimeout;
    }

    public void publish(String topic, String message) {
        publish(topic, message, QOS, false);
    }

    public void publish(String topic, String message, int qos, boolean retained) {
        MqttClient client = null;
        try {
            client = new MqttClient(mqttServerUri, CLIENT_ID);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            client.connect(connOpts);
            client.publish(topic, message.getBytes(), qos, retained);
        } catch (MqttException ex) {
            LOGGER.error("Exception:", ex);
            Assert.fail("error publishing message on MQTT: " + ex.getMessage());
        } finally {
            if (client != null) {
                try {
                    client.disconnect();
                    client.close();
                } catch (MqttException ex) {
                }
            }
        }
    }

    public <T> MqttBatchResult<T> executeRequests(Callable<T> action, String... topics) {
        MqttBatchResult<T> result = new MqttBatchResult<>(topics.length);
        Map<String, Future<JSONObject>> tempResult = new HashMap<>(topics.length);
        ExecutorService executor = Executors.newFixedThreadPool(topics.length);
        try {
            for (String topic : topics) {
                MqttListener listener = new MqttListener(mqttServerUri, topic);
                listener.connect();
                tempResult.put(topic, executor.submit(listener));
            }

            // Give the MQTT server time to process the subscriptions.
            waitMillis(200);

            try {
                LOGGER.debug("Calling action...");
                result.setActionResult(action.call());
            } catch (Exception ex) {
                LOGGER.error("Exception:", ex);
                Assert.fail("Topics: " + Arrays.toString(topics) + " Error executing : " + ex.getMessage());
            }
            executor.shutdown();
            if (!executor.awaitTermination(mqttTimeout, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
            for (Map.Entry<String, Future<JSONObject>> entry : tempResult.entrySet()) {
                result.addMessage(entry.getKey(), entry.getValue().get());
            }
        } catch (InterruptedException | ExecutionException ex) {
            LOGGER.error("Exception:", ex);
            Assert.fail("Topics: " + Arrays.toString(topics) + " Error subscribing to MQTT: " + ex.getMessage());
        } finally {
            executor.shutdownNow();
        }
        return result;
    }

    public static List<String> getRelativeTopicsForEntity(EntityType entityType, Map<EntityType, Object> ids) {
        List<String> result = new ArrayList<>();
        switch (entityType) {
            case THING:
                result.add(getTopic(EntityType.DATASTREAM, ids) + "/Thing");
                result.add(getTopic(EntityType.OBSERVATION, ids) + "/Datastream/Thing");
                result.add(getTopic(EntityType.HISTORICAL_LOCATION, ids) + "/Thing");
                break;
            case LOCATION:
                break;
            case SENSOR:
                result.add(getTopic(EntityType.DATASTREAM, ids) + "/Sensor");
                result.add(getTopic(EntityType.OBSERVATION, ids) + "/Datastream/Sensor");
                break;
            case OBSERVED_PROPERTY:
                result.add(getTopic(EntityType.DATASTREAM, ids) + "/ObservedProperty");
                result.add(getTopic(EntityType.OBSERVATION, ids) + "/Datastream/ObservedProperty");
                break;
            case FEATURE_OF_INTEREST:
                result.add(getTopic(EntityType.OBSERVATION, ids) + "/FeatureOfInterest");
                break;
            case DATASTREAM:
                result.add(getTopic(EntityType.OBSERVATION, ids) + "/Datastream");
                break;
            case OBSERVATION:
                break;
            case HISTORICAL_LOCATION:
                break;
            default:
                throw new IllegalArgumentException("Unknown EntityType '" + entityType.toString() + "'");
        }
        return result;
    }

    public static List<String> getRelativeTopicsForEntitySet(EntityType entityType, Map<EntityType, Object> ids) {
        List<String> result = new ArrayList<>();
        switch (entityType) {
            case THING:
                result.add(getTopic(EntityType.LOCATION, ids) + "/Things");
                break;
            case LOCATION:
                result.add(getTopic(EntityType.THING, ids) + "/Locations");
                result.add(getTopic(EntityType.DATASTREAM, ids) + "/Thing/Locations");
                result.add(getTopic(EntityType.HISTORICAL_LOCATION, ids) + "/Thing/Locations");
                break;
            case SENSOR:
                break;
            case OBSERVED_PROPERTY:
                break;
            case FEATURE_OF_INTEREST:
                break;
            case DATASTREAM:
                result.add(getTopic(EntityType.THING, ids) + "/Datastreams");
                result.add(getTopic(EntityType.HISTORICAL_LOCATION, ids) + "/Thing/Datastreams");
                result.add(getTopic(EntityType.SENSOR, ids) + "/Datastreams");
                result.add(getTopic(EntityType.OBSERVED_PROPERTY, ids) + "/Datastreams");
                break;
            case OBSERVATION:
                result.add(getTopic(EntityType.DATASTREAM, ids) + "/Observations");
                break;
            case HISTORICAL_LOCATION:
                result.add(getTopic(EntityType.THING, ids) + "/HistoricalLocations");
                result.add(getTopic(EntityType.DATASTREAM, ids) + "/Thing/HistoricalLocations");
                result.add(getTopic(EntityType.LOCATION, ids) + "/HistoricalLocations");
                break;
            default:
                throw new IllegalArgumentException("Unknown EntityType '" + entityType.toString() + "'");
        }
        return result;
    }

    public static String getTopic(EntityType entityType, List<String> selectedProperties) {
        return getTopic(entityType) + "?$select=" + selectedProperties.stream().collect(Collectors.joining(","));
    }

    public static String getTopic(EntityType entityType, Object id, String property) {
        return getTopic(entityType) + "(" + Utils.quoteIdForUrl(id) + ")/" + property;
    }

    public static String getTopic(EntityType entityType, Object id) {
        return getTopic(entityType) + "(" + Utils.quoteIdForUrl(id) + ")";
    }

    public static String getTopic(EntityType entityType) {
        switch (entityType) {
            case THING:
                return MQTT_TOPIC_PREFIX + "Things";
            case LOCATION:
                return MQTT_TOPIC_PREFIX + "Locations";
            case SENSOR:
                return MQTT_TOPIC_PREFIX + "Sensors";
            case OBSERVED_PROPERTY:
                return MQTT_TOPIC_PREFIX + "ObservedProperties";
            case FEATURE_OF_INTEREST:
                return MQTT_TOPIC_PREFIX + "FeaturesOfInterest";
            case DATASTREAM:
                return MQTT_TOPIC_PREFIX + "Datastreams";
            case OBSERVATION:
                return MQTT_TOPIC_PREFIX + "Observations";
            case HISTORICAL_LOCATION:
                return MQTT_TOPIC_PREFIX + "HistoricalLocations";
            default:
                throw new IllegalArgumentException("Unknown EntityType '" + entityType.toString() + "'");
        }
    }

    private static String getTopic(EntityType entityType, Map<EntityType, Object> ids) {
        return getTopic(entityType, ids.get(entityType));
    }

}
