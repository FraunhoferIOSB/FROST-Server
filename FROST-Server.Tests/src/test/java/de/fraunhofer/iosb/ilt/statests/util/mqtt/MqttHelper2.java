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
package de.fraunhofer.iosb.ilt.statests.util.mqtt;

import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.fraunhofer.iosb.ilt.frostclient.SensorThingsService;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.model.EntityType;
import de.fraunhofer.iosb.ilt.frostclient.utils.MqttConfig;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.MqttManager;
import de.fraunhofer.iosb.ilt.statests.util.Utils;
import de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttListener.ReceivedListener;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import net.time4j.range.MomentInterval;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper for using MQTT in tests.
 */
public class MqttHelper2 {

    public static final int WAIT_AFTER_INSERT = 100;
    public static final int WAIT_AFTER_CLEANUP = 1;
    public static final int QOS = 2;
    public String clientId = "TS";

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttHelper2.class);

    private final SensorThingsService sSrvc;
    private final String mqttServerUri;

    /**
     * The MQTT timeout in ms
     */
    private final long mqttTimeoutMs;

    public MqttHelper2(SensorThingsService sSrvc, String mqttServerUri, long mqttTimeoutMs) {
        this(sSrvc, mqttServerUri, mqttTimeoutMs, "TS");
    }

    public MqttHelper2(SensorThingsService sSrvc, String mqttServerUri, long mqttTimeoutMs, String clientId) {
        this.sSrvc = sSrvc;
        this.mqttServerUri = mqttServerUri;
        this.mqttTimeoutMs = mqttTimeoutMs;
        this.clientId = clientId;
    }

    public MqttHelper2 setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public boolean isAuthSet() {
        return sSrvc.getOrCreateMqttConfig().isAuthSet();
    }

    public static void waitMillis(long millis) {
        try {
            LOGGER.trace("      Sleeping {}", millis);
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            // rude wakeup
        }
    }

    public SensorThingsService getService() {
        return sSrvc;
    }

    public void publish(String topic, String message) {
        publish(topic, message, QOS, false);
    }

    public void publish(String topic, String message, int qos, boolean retained) {
        MqttAsyncClient client = null;
        try {
            client = new MqttAsyncClient(mqttServerUri, "Pub-" + clientId);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            if (isAuthSet()) {
                MqttConfig mqttConfig = sSrvc.getMqttConfig();
                connOpts.setUserName(mqttConfig.getUsername());
                connOpts.setPassword(mqttConfig.getPassword().toCharArray());
            }
            connOpts.setCleanSession(true);
            client.connect(connOpts).waitForCompletion(1000);
            client.publish(topic, message.getBytes(), qos, retained).waitForCompletion(1000);
        } catch (MqttException ex) {
            LOGGER.error("Exception on server {} :", mqttServerUri, ex);
            fail("error publishing message on MQTT: " + ex.getMessage());
        } finally {
            if (client != null) {
                try {
                    client.disconnect();
                } catch (MqttException ex) {
                }
                try {
                    client.close();
                } catch (MqttException ex) {
                }
            }
        }
    }

    public void executeRequest(MqttAction ma) {
        LOGGER.debug("  Executing Request...");
        final ExecutorService executor = Executors.newFixedThreadPool(ma.topics.size());
        try {
            for (TestSubscription tl : ma.topics) {
                LOGGER.debug("  Creating Subsctiption for {} messages on {}", tl.getExpectedCount(), tl.topic);
                MqttListener listener = new MqttListener(mqttServerUri, tl.getTopic(), tl.getExpectedCount());
                if (tl.mqttHelper.isAuthSet()) {
                    MqttConfig mqttConfig = tl.mqttHelper.sSrvc.getMqttConfig();
                    listener.setAuth(mqttConfig.getUsername(), mqttConfig.getPassword());
                }
                listener.setListener(tl.mqttReceivedListener);
                listener.connect();
                executor.submit(listener);
            }
            LOGGER.debug("  Calling action...");
            ma.action.call();
            LOGGER.debug("  waiting for listeners to finish...");
            executor.shutdown();
            if (!executor.awaitTermination(mqttTimeoutMs, TimeUnit.MILLISECONDS)) {
                LOGGER.debug("  Listeners did not finish!");
            }
            LOGGER.debug("  done waiting for listeners to finish.");

        } catch (Exception ex) {
            LOGGER.error("Exception on server {} :", mqttServerUri, ex);
            fail("Topics: " + ma.topics + " Error executing : " + ex.getMessage());
        } finally {
            executor.shutdownNow();
        }
        MqttManager.clearTestSubscriptionListeners();
        Assertions.assertFalse(ma.topics.isEmpty(), "No topics to test?");
        for (TestSubscription tl : ma.topics) {
            Assertions.assertTrue(
                    tl.checkAllReceived(mqttTimeoutMs),
                    () -> "failure checking received entities on " + tl.getTopic());
            Assertions.assertTrue(
                    tl.allReceived(),
                    () -> "Did not receive " + tl.getExpectedCount() + " messages on " + tl.getTopic());
            Assertions.assertFalse(
                    tl.hasErrors(),
                    () -> "Errors encountered on " + tl.getTopic() + "; Latest: " + tl.getErrors().get(0));
        }

    }

    private static boolean jsonEqualsWithLinkResolving(JsonNode node1, JsonNode node2, String topic) {
        if (node1 instanceof ObjectNode obj1 && node2 instanceof ObjectNode obj2) {
            return jsonEqualsWithLinkResolving(obj1, obj2, topic);
        }
        if (node1 instanceof ArrayNode arr1 && node2 instanceof ArrayNode arr2) {
            return jsonEqualsWithLinkResolving(arr1, arr2, topic);
        }
        return false;
    }

    private static boolean jsonEqualsWithLinkResolving(ArrayNode arr1, ArrayNode arr2, String topic) {
        if (arr1.size() != arr2.size()) {
            return false;
        }
        for (int i = 0; i < arr1.size(); i++) {
            Object val1 = arr1.get(i);
            if (val1 instanceof ObjectNode) {
                if (!jsonEqualsWithLinkResolving((ObjectNode) val1, (ObjectNode) arr2.get(i), topic)) {
                    return false;
                }
            } else if (val1 instanceof ArrayNode) {
                if (!jsonEqualsWithLinkResolving((ArrayNode) val1, (ArrayNode) arr2.get(i), topic)) {
                    return false;
                }
            } else if (!val1.equals(arr2.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean jsonEqualsWithLinkResolving(ObjectNode obj1, ObjectNode obj2, String topic) {
        if (obj1 == obj2) {
            return true;
        }
        if (obj1 == null) {
            return false;
        }
        if (obj1.getClass() != obj2.getClass()) {
            return false;
        }
        if (obj1.size() != obj2.size()) {
            return false;
        }
        Iterator<String> iterator = obj1.fieldNames();
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (!obj2.has(key)) {
                return false;
            }
            JsonNode val1 = obj1.get(key);
            if (val1 == null) {
                return obj2.get(key) == null;
            } else if (val1 instanceof ObjectNode) {
                if (!jsonEqualsWithLinkResolving((ObjectNode) val1, (ObjectNode) obj2.get(key), topic)) {
                    return false;
                }
            } else if (val1 instanceof ArrayNode) {
                ArrayNode arr1 = (ArrayNode) val1;
                ArrayNode arr2 = (ArrayNode) obj2.get(key);
                if (!jsonEqualsWithLinkResolving(arr1, arr2, topic)) {
                    return false;
                }
            } else if (key.toLowerCase().endsWith("time")) {
                if (!checkTimeEquals(val1.textValue(), obj2.get(key).textValue())) {
                    return false;
                }
            } else if (topic != null && !topic.isEmpty() && key.endsWith("@iot.navigationLink")) {
                String version = topic.substring(0, topic.indexOf("/"));

                String selfLink1 = obj1.get("@iot.selfLink").textValue();
                URI baseUri1 = URI.create(selfLink1.substring(0, selfLink1.indexOf(version))).resolve(topic);
                String navLink1 = obj1.get(key).textValue();
                String absoluteUri1 = baseUri1.resolve(navLink1).toString();

                String selfLink2 = obj2.get("@iot.selfLink").textValue();
                URI baseUri2 = URI.create(selfLink2.substring(0, selfLink2.indexOf(version))).resolve(topic);
                String navLink2 = obj2.get(key).textValue();
                String absoluteUri2 = baseUri2.resolve(navLink2).toString();
                if (!absoluteUri1.equals(absoluteUri2)) {
                    return false;
                }

            } else if (!val1.equals(obj2.get(key))) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkTimeEquals(String val1, String val2) {
        if (val1 == null) {
            return val2 == null;
        }
        if (val1.equals(val2)) {
            return true;
        }

        try {
            ZonedDateTime dateTime1 = ZonedDateTime.parse(val1);
            ZonedDateTime dateTime2 = ZonedDateTime.parse(val2);
            return dateTime1.isEqual(dateTime2);
        } catch (Exception ex) {
            // do nothing
        }
        try {
            MomentInterval interval1 = MomentInterval.parseISO(val1);
            MomentInterval interval2 = MomentInterval.parseISO(val2);
            return interval1.equals(interval2);
        } catch (RuntimeException | ParseException ex) {
            fail("time properies could neither be parsed as time nor as interval");
        }

        return false;
    }

    /**
     * A subscription for Tests. The expected entities are Futures, since some
     * details of those entities (like IDs) are server generated and thus not
     * known until after the entities are actually created.
     */
    public static class TestSubscription {

        private final MqttHelper2 mqttHelper;
        /**
         * The topic for the subscription.
         */
        private String topic;

        /**
         * These are the entities that we expect to receive. When creating the
         * Subscription we don't actually know all details yet, since some
         * properties are server generated.
         */
        private final List<Future<Entity>> expectedEntities = new ArrayList<>();

        /**
         * These are JSON objects that we expect to receive.
         */
        private final List<Future<JsonNode>> expectedJson = new ArrayList<>();

        /**
         * The list of receivedErrors that we expect to receive.
         */
        private final List<String> expectedErrors = new ArrayList<>();

        /**
         * Flag indicating this Subscription expects an Entity for each received
         * message.
         */
        private boolean expectsEntities;

        /**
         * Flag indicating this Subscription expects JSON for each received
         * message.
         */
        private boolean expectsJson;

        private final List<Entity> receivedEntities = new ArrayList<>();
        private final List<JsonNode> receivedJson = new ArrayList<>();
        private final List<String> receivedErrors = new ArrayList<>();

        private ReceivedListener mqttReceivedListener;

        public TestSubscription(MqttHelper2 mqttHelper) {
            this(mqttHelper, null);
        }

        public TestSubscription(MqttHelper2 mqttHelper, String topic) {
            this.mqttHelper = mqttHelper;
            this.topic = topic;
        }

        public String getTopic() {
            return topic;
        }

        public TestSubscription setTopic(String topic) {
            this.topic = topic;
            return this;
        }

        public boolean isEntitiesExpected() {
            return expectsEntities;
        }

        public TestSubscription addExpectedEntity(Future<Entity> expected) {
            expectedEntities.add(expected);
            expectsEntities = true;
            return this;
        }

        public List<Future<Entity>> getExpectedEntities() {
            return expectedEntities;
        }

        public boolean isJsonExpected() {
            return expectsJson;
        }

        public TestSubscription addExpectedJson(Future<JsonNode> expected) {
            expectedJson.add(expected);
            expectsJson = true;
            return this;
        }

        public List<Future<JsonNode>> getExpectedJson() {
            return expectedJson;
        }

        public TestSubscription addExpectedError(String errorPrefix) {
            expectedErrors.add(errorPrefix);
            return this;
        }

        public List<String> getExpectedErrors() {
            return expectedErrors;
        }

        public int getExpectedCount() {
            return expectedEntities.size() + expectedJson.size() + expectedErrors.size();
        }

        public TestSubscription setMqttReceivedListener(ReceivedListener mqttReceivedListener) {
            this.mqttReceivedListener = mqttReceivedListener;
            return this;
        }

        public TestSubscription received(JsonNode json) {
            if (expectsJson) {
                LOGGER.debug("    Logging received JSON {}", json);
                synchronized (receivedJson) {
                    receivedJson.add(json);
                }
                LOGGER.debug("    Done Logging received JSON {}", json);
            }
            return this;
        }

        public TestSubscription received(Entity receivedEntity) {
            if (expectsEntities) {
                LOGGER.debug("    Logging received Entity {}", receivedEntity);
                synchronized (receivedEntities) {
                    receivedEntities.add(receivedEntity);
                }
                LOGGER.debug("    Done Logging received Entity {}", receivedEntity);
            }
            return this;
        }

        /**
         * Compare all received entities against all the expected entities until
         * a match is found. The matching expected entity is removed from the
         * list. This call may block until all expected Futures are available.
         *
         * @param timeoutMs the maximum number of milliseconds to wait.
         * @return true this.
         */
        public boolean checkAllReceived(long timeoutMs) {
            LOGGER.debug("    Checking all received.");
            while (!receivedEntities.isEmpty()) {
                Entity receivedEntity;
                synchronized (receivedEntities) {
                    receivedEntity = receivedEntities.remove(0);
                    LOGGER.debug("    Checking received entity {}.", receivedEntity);
                }
                boolean result = checkReceived(receivedEntity, timeoutMs);
                if (!result) {
                    LOGGER.debug("    Check failed on entity {}.", receivedEntity);
                    return false;
                }
            }
            while (!receivedJson.isEmpty()) {
                JsonNode jsonNode;
                synchronized (receivedJson) {
                    jsonNode = receivedJson.remove(0);
                    LOGGER.debug("    Checking received JSON {}.", jsonNode);
                }
                boolean result = checkReceived(jsonNode, timeoutMs);
                if (!result) {
                    LOGGER.debug("    Check failed on JOSN {}.", jsonNode);
                    return false;
                }
            }
            while (!receivedErrors.isEmpty()) {
                String gotError = receivedErrors.remove(0);
                boolean found = false;
                for (String expErr : expectedErrors) {
                    if (gotError.startsWith(expErr)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    LOGGER.debug("    Received unexpected error: {}", gotError);
                    return false;
                }
            }
            return true;
        }

        /**
         * Compare the given entity against all the expected entities until a
         * match is found. The matching expected entity is removed from the
         * list. This call may block until all expected Futures are available.
         *
         * @param receivedEntity the received Entity.
         * @param timeoutMs the maximum number of milliseconds to wait.
         * @return true this.
         */
        public boolean checkReceived(Entity receivedEntity, long timeoutMs) {
            LOGGER.debug("    Received entity {}", receivedEntity);
            Iterator<Future<Entity>> it = expectedEntities.iterator();
            while (it.hasNext()) {
                Future<Entity> entityFuture = it.next();
                try {
                    LOGGER.debug("    Getting expected for entity {}", receivedEntity);
                    Entity expected = entityFuture.get(timeoutMs, TimeUnit.MILLISECONDS);
                    LOGGER.debug("    Comparing received entity {} against expected {}", receivedEntity, expected);
                    if (expected.equals(receivedEntity)) {
                        it.remove();
                        LOGGER.debug("    Received entity {} matches expected {}", receivedEntity, expected);
                        return true;
                    }
                } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                    LOGGER.warn("Exeption waiting for future.", ex);
                    return false;
                }
            }
            receivedErrors.add("    Received entity " + receivedEntity + " matches nothing");
            return false;
        }

        /**
         * Compare the given JsonNode against all the expected JsonNodes until a
         * match is found. The matching expected JsonNode is removed from the
         * list. This call may block until all expected Futures are available.
         *
         * @param received the received JsonNode.
         * @param timeoutMs the maximum number of milliseconds to wait.
         * @return true this.
         */
        public boolean checkReceived(JsonNode received, long timeoutMs) {
            LOGGER.debug("    Received JSON {}", received);
            Iterator<Future<JsonNode>> it = expectedJson.iterator();
            while (it.hasNext()) {
                Future<JsonNode> entityFuture = it.next();
                try {
                    LOGGER.debug("    Getting expected for JSON {}", received);
                    JsonNode expected = entityFuture.get(timeoutMs, TimeUnit.MILLISECONDS);
                    LOGGER.debug("    Comparing received JSON {} against expected {}", received, expected);
                    if (jsonEqualsWithLinkResolving(expected, received, topic)) {
                        it.remove();
                        LOGGER.debug("    Received JSON {} matches expected {}", received, expected);
                        return true;
                    }
                } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                    LOGGER.warn("Exeption waiting for future.", ex);
                    return false;
                }
            }
            LOGGER.debug("    Received JSON {} matches nothing", received);
            return false;
        }

        public boolean allReceived() {
            return expectedEntities.isEmpty();
        }

        public List<String> getErrors() {
            return receivedErrors;
        }

        public boolean hasErrors() {
            return !receivedErrors.isEmpty();
        }

        public TestSubscription addError(String error) {
            receivedErrors.add(error);
            return this;
        }

        public TestSubscription createReceivedListener(EntityType et) {
            final ReceivedListener listener = (result, isError) -> {
                if (isError) {
                    addError(result);
                    return;
                }
                try {
                    if (expectsEntities) {
                        Entity entity = mqttHelper.getService().getJsonReader().parseEntity(et, result);
                        received(entity);
                    }
                    if (expectsJson) {
                        JsonNode tree = Utils.MAPPER.readTree(result);
                        received(tree);
                    }
                } catch (IOException ex) {
                    addError("Failed to parse " + et + " from " + result);
                }
            };
            setMqttReceivedListener(listener);
            return this;
        }
    }

    public static class MqttAction {

        private final Callable<Object> action;
        private final List<TestSubscription> topics = new ArrayList<>();

        public MqttAction(Callable<Object> action) {
            this.action = action;
        }

        public MqttAction(Callable<Object> action, List<TestSubscription> topics) {
            this(action);
            this.addAll(topics);
        }

        public final MqttAction add(TestSubscription topic) {
            this.topics.add(topic);
            return this;
        }

        public final MqttAction addAll(List<TestSubscription> topics) {
            this.topics.addAll(topics);
            return this;
        }

        public Callable<Object> getAction() {
            return action;
        }

        public List<TestSubscription> getTopics() {
            return topics;
        }

    }

}
