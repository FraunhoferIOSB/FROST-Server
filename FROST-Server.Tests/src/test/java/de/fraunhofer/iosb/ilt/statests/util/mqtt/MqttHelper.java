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
package de.fraunhofer.iosb.ilt.statests.util.mqtt;

import static de.fraunhofer.iosb.ilt.statests.util.EntityType.DATASTREAM;
import static de.fraunhofer.iosb.ilt.statests.util.EntityType.FEATURE_OF_INTEREST;
import static de.fraunhofer.iosb.ilt.statests.util.EntityType.HISTORICAL_LOCATION;
import static de.fraunhofer.iosb.ilt.statests.util.EntityType.LOCATION;
import static de.fraunhofer.iosb.ilt.statests.util.EntityType.OBSERVATION;
import static de.fraunhofer.iosb.ilt.statests.util.EntityType.OBSERVED_PROPERTY;
import static de.fraunhofer.iosb.ilt.statests.util.EntityType.SENSOR;
import static de.fraunhofer.iosb.ilt.statests.util.EntityType.THING;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.JsonNode;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.MqttManager;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityType;
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
    public static final int WAIT_AFTER_INSERT = 100;
    public static final int WAIT_AFTER_CLEANUP = 500;
    public static final int QOS = 2;
    public static final String CLIENT_ID = "STA-test_suite";
    private final String mqttServerUri;
    private final long mqttTimeout;
    private final ServerVersion version;

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MqttHelper.class);

    public static void waitMillis(long millis) {
        try {
            LOGGER.trace("      Sleeping {}", millis);
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            // rude wakeup
        }
    }

    public MqttHelper(ServerVersion version, String mqttServerUri, long mqttTimeout) {
        this.version = version;
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
            LOGGER.error("Exception on server {} :", mqttServerUri, ex);
            fail("error publishing message on MQTT: " + ex.getMessage());
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
        Map<String, Future<JsonNode>> tempResult = new HashMap<>(topics.length);
        ExecutorService executor = Executors.newFixedThreadPool(topics.length);
        try {
            for (String topic : topics) {
                MqttListener listener = new MqttListener(mqttServerUri, topic);
                listener.connect();
                tempResult.put(topic, executor.submit(listener));
            }

            try {
                LOGGER.debug("  Calling action...");
                result.setActionResult(action.call());
            } catch (Exception ex) {
                LOGGER.error("Exception on server {} :", mqttServerUri, ex);
                fail("Topics: " + Arrays.toString(topics) + " Error executing : " + ex.getMessage());
            }
            executor.shutdown();
            if (!executor.awaitTermination(mqttTimeout, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
            }
            for (Map.Entry<String, Future<JsonNode>> entry : tempResult.entrySet()) {
                result.addMessage(entry.getKey(), entry.getValue().get());
            }
        } catch (InterruptedException | ExecutionException ex) {
            LOGGER.error("Exception on server {} :", mqttServerUri, ex);
            fail("Topics: " + Arrays.toString(topics) + " Error subscribing to MQTT: " + ex.getMessage());
        } finally {
            executor.shutdownNow();
        }
        MqttManager.clearTestSubscriptionListeners();
        return result;
    }

    public List<String> getRelativeTopicsForEntity(EntityType entityType, Map<EntityType, Object> ids) {
        List<String> result = new ArrayList<>();
        switch (entityType) {
            case THING:
                result.add(getTopic(DATASTREAM, ids) + "/Thing");
                result.add(getTopic(OBSERVATION, ids) + "/Datastream/Thing");
                result.add(getTopic(HISTORICAL_LOCATION, ids) + "/Thing");
                break;
            case LOCATION:
                break;
            case SENSOR:
                result.add(getTopic(DATASTREAM, ids) + "/Sensor");
                result.add(getTopic(OBSERVATION, ids) + "/Datastream/Sensor");
                break;
            case OBSERVED_PROPERTY:
                result.add(getTopic(DATASTREAM, ids) + "/ObservedProperty");
                result.add(getTopic(OBSERVATION, ids) + "/Datastream/ObservedProperty");
                break;
            case FEATURE_OF_INTEREST:
                result.add(getTopic(OBSERVATION, ids) + "/FeatureOfInterest");
                break;
            case DATASTREAM:
                result.add(getTopic(OBSERVATION, ids) + "/Datastream");
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

    public List<String> getRelativeTopicsForEntitySet(EntityType entityType, Map<EntityType, Object> ids) {
        List<String> result = new ArrayList<>();
        switch (entityType) {
            case THING:
                result.add(getTopic(LOCATION, ids) + "/Things");
                break;
            case LOCATION:
                result.add(getTopic(THING, ids) + "/Locations");
                result.add(getTopic(DATASTREAM, ids) + "/Thing/Locations");
                result.add(getTopic(HISTORICAL_LOCATION, ids) + "/Thing/Locations");
                break;
            case SENSOR:
                break;
            case OBSERVED_PROPERTY:
                break;
            case FEATURE_OF_INTEREST:
                break;
            case DATASTREAM:
                result.add(getTopic(THING, ids) + "/Datastreams");
                result.add(getTopic(HISTORICAL_LOCATION, ids) + "/Thing/Datastreams");
                result.add(getTopic(SENSOR, ids) + "/Datastreams");
                result.add(getTopic(OBSERVED_PROPERTY, ids) + "/Datastreams");
                break;
            case OBSERVATION:
                result.add(getTopic(DATASTREAM, ids) + "/Observations");
                break;
            case HISTORICAL_LOCATION:
                result.add(getTopic(THING, ids) + "/HistoricalLocations");
                result.add(getTopic(DATASTREAM, ids) + "/Thing/HistoricalLocations");
                result.add(getTopic(LOCATION, ids) + "/HistoricalLocations");
                break;
            default:
                throw new IllegalArgumentException("Unknown EntityType '" + entityType.toString() + "'");
        }
        return result;
    }

    public String getTopic(EntityType entityType, List<String> selectedProperties) {
        return getTopic(entityType) + "?$select=" + selectedProperties.stream().collect(Collectors.joining(","));
    }

    public String getTopic(EntityType entityType, Object id, String property) {
        return getTopic(entityType) + "(" + Utils.quoteForUrl(id) + ")/" + property;
    }

    public String getTopic(EntityType entityType, Object id) {
        return getTopic(entityType) + "(" + Utils.quoteForUrl(id) + ")";
    }

    public String getTopic(EntityType entityType) {
        return version.urlPart + "/" + entityType.plural;
    }

    private String getTopic(EntityType entityType, Map<EntityType, Object> ids) {
        return getTopic(entityType, ids.get(entityType));
    }

}
