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

import static org.junit.jupiter.api.Assertions.fail;

import de.fraunhofer.iosb.ilt.frostclient.SensorThingsService;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.MqttManager;
import de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttListener.ReceivedListener;
import java.io.IOException;
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
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper for using MQTT in tests.
 */
public class MqttHelper2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttHelper2.class);

    private final SensorThingsService sSrvc;
    private final String mqttServerUri;
    /**
     * The MQTT timeout in ms
     */
    private final long mqttTimeoutMs;

    public MqttHelper2(SensorThingsService sSrvc, String mqttServerUri, long mqttTimeoutMs) {
        this.sSrvc = sSrvc;
        this.mqttServerUri = mqttServerUri;
        this.mqttTimeoutMs = mqttTimeoutMs;
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

    public void executeRequest(MqttAction ma) {
        LOGGER.debug("  Executing Request...");
        final ExecutorService executor = Executors.newFixedThreadPool(ma.topics.size());
        try {
            for (TestSubscription tl : ma.topics) {
                LOGGER.debug("  Creating Subsctiption for {} messages on {}", tl.getExpectedCount(), tl.topic);
                MqttListener listener = new MqttListener(mqttServerUri, tl.getTopic(), tl.getExpectedCount());
                listener.connect();
                listener.setListener(tl.mqttReceivedListener);
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
        for (TestSubscription tl : ma.topics) {
            Assertions.assertTrue(tl.checkAllReceived(mqttTimeoutMs), "failure checking received entities.");
            Assertions.assertTrue(tl.allReceived(), () -> "Did not receive " + tl.getExpectedCount() + " messages on " + tl.getTopic());
            Assertions.assertFalse(tl.hasErrors(), () -> "Errors encountered on " + tl.getTopic() + "; Latest: " + tl.getErrors().get(0));
        }

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
        private final String topic;

        /**
         * These are the entities that we expect to receive. When creating the
         * Subscription we don't actually know all details yet, since some
         * properties are server generated.
         */
        private final List<Future<Entity>> expectedEntities = new ArrayList<>();
        private final List<String> errors = new ArrayList<>();
        private final List<Entity> received = new ArrayList<>();

        private ReceivedListener mqttReceivedListener;

        public TestSubscription(MqttHelper2 mqttHelper, String topic) {
            this.mqttHelper = mqttHelper;
            this.topic = topic;
        }

        public String getTopic() {
            return topic;
        }

        public TestSubscription addExpected(Future<Entity> expected) {
            expectedEntities.add(expected);
            return this;
        }

        public List<Future<Entity>> getExpectedEntities() {
            return expectedEntities;
        }

        public int getExpectedCount() {
            return expectedEntities.size();
        }

        public TestSubscription setMqttReceivedListener(ReceivedListener mqttReceivedListener) {
            this.mqttReceivedListener = mqttReceivedListener;
            return this;
        }

        public TestSubscription received(Entity receivedEntity) {
            LOGGER.debug("    Logging received Entity {}", receivedEntity);
            synchronized (received) {
                received.add(receivedEntity);
            }
            LOGGER.debug("    Done Logging received Entity {}", receivedEntity);
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
            while (!received.isEmpty()) {
                Entity receivedEntity;
                synchronized (received) {
                    receivedEntity = received.remove(0);
                    LOGGER.debug("    Checking received entity {}.", receivedEntity);
                }
                boolean result = checkReceived(receivedEntity, timeoutMs);
                if (!result) {
                    LOGGER.debug("    Check failed on entity {}.", receivedEntity);
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
            LOGGER.debug("    Received entity {} matches nothing", receivedEntity);
            return false;
        }

        public boolean allReceived() {
            return expectedEntities.isEmpty();
        }

        public List<String> getErrors() {
            return errors;
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public TestSubscription addError(String error) {
            errors.add(error);
            return this;
        }

        public TestSubscription createReceivedListener(EntityType et) {
            final ReceivedListener listener = (result) -> {
                try {
                    Entity entity = mqttHelper.getService().getJsonReader().parseEntity(et, result);
                    received(entity);
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
