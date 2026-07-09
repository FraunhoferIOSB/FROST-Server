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

import static de.fraunhofer.iosb.ilt.frostserver.util.StringHelper.isNullOrEmpty;
import static org.junit.jupiter.api.Assertions.fail;

import de.fraunhofer.iosb.ilt.frostserver.mqtt.MqttManager;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.subscription.SubscriptionEvent;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.subscription.SubscriptionListener;
import de.fraunhofer.iosb.ilt.statests.ServerSettings;
import de.fraunhofer.iosb.ilt.statests.util.Utils;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;

/**
 * Helper for connecting to MQTT and listening on topics.
 */
public class MqttListener implements Callable<JsonNode> {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MqttListener.class);

    private final CountDownLatch barrier;
    private final String name;
    private final String topic;
    private final String mqttServerUri;

    private String username;
    private String password;

    private MqttAsyncClient mqttClient;
    private String clientId = "";
    private JsonNode result;
    private ReceivedListener listener;

    public MqttListener(String name, String mqttServer, String topic) {
        this(name, mqttServer, topic, 1);
    }

    public MqttListener(String name, String mqttServer, String topic, int expectedMessages) {
        this.name = name;
        this.mqttServerUri = mqttServer;
        this.topic = topic;
        barrier = new CountDownLatch(expectedMessages);
        LOGGER.debug("{} Created MqttListener for {} expecting {} on {}", name, mqttServer, expectedMessages, topic);
    }

    public String getName() {
        return name;
    }

    public String getTopic() {
        return topic;
    }

    public boolean isDone() {
        return barrier.getCount() == 0;
    }

    public void setListener(ReceivedListener listener) {
        this.listener = listener;
    }

    public MqttListener setAuth(String username, String password) {
        this.username = username;
        this.password = password;
        return this;
    }

    private void notifyMessage(String message) {
        if (barrier.getCount() == 0) {
            LOGGER.error("{} has a negative barrier, received more than expected!", name);
        }
        barrier.countDown();
        LOGGER.debug("{} Received message, barrier now at {}", clientId, barrier.getCount());
        if (listener != null) {
            listener.received(message, false);
        }
    }

    private void notifyError(String message) {
        if (barrier.getCount() == 0) {
            LOGGER.error("{} has a negative barrier, received more than expected!", name);
        }
        barrier.countDown();
        LOGGER.debug("{} Received error, barrier now at {}", clientId, barrier.getCount());
        if (listener != null) {
            listener.received(message, true);
        }
    }

    public MqttListener connect() {
        try {
            final CountDownLatch connectBarrier = new CountDownLatch(2);
            clientId = "TS-" + name + "-" + UUID.randomUUID();
            mqttClient = new MqttAsyncClient(mqttServerUri, clientId, new MemoryPersistence());
            LOGGER.debug("  c: {} connecting to {}", clientId, mqttServerUri);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
            if (!isNullOrEmpty(username)) {
                connOpts.setUserName(username);
                connOpts.setPassword(password.toCharArray());
            }
            connOpts.setCleanSession(true);
            // Listen on the side channel to get confirmation of subscriptions.
            MqttManager.addTestSubscriptionListener(new SubscriptionListener() {
                @Override
                public void onSubscribe(SubscriptionEvent subscription) {
                    final String subbedTopic = subscription.getTopic();
                    if (clientId.equals(subscription.getClientId()) && subbedTopic.endsWith(topic)) {
                        LOGGER.debug("  s: {} Subscribed to {}", clientId, subscription);
                        connectBarrier.countDown();
                    }
                }

                @Override
                public void onUnsubscribe(SubscriptionEvent subscription) {
                    if (clientId.equals(subscription.getClientId()) && topic.equals(subscription.getTopic())) {
                        LOGGER.debug("  s: {} Unsubscribe from {}", clientId, subscription);
                    }
                }
            });
            mqttClient.connect(connOpts, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    LOGGER.debug("  c: {} Connected.", name);
                    mqttClient.setCallback(new MqttCallback() {
                        @Override
                        public void connectionLost(Throwable thrwbl) {
                            LOGGER.info("Connection lost for {}:", name, thrwbl.getMessage());
                            notifyError("MQTT connection lost: " + clientId);
                        }

                        @Override
                        public void messageArrived(String topic, MqttMessage mm) {
                            if (barrier.getCount() > 0) {
                                final String payload = new String(mm.getPayload(), StandardCharsets.UTF_8);
                                try {
                                    result = Utils.MAPPER.readTree(payload);
                                } catch (JacksonException ex) {
                                    LOGGER.error("Failed to parse result", ex);
                                }
                                notifyMessage(payload);
                                LOGGER.debug("  c: {} Received on {}. To go: {}", clientId, topic, barrier.getCount());
                            } else {
                                LOGGER.error("  c: {} Received on {}. Barrier already empty!", clientId, topic);
                            }
                        }

                        @Override
                        public void deliveryComplete(IMqttDeliveryToken imdt) {
                            // Nothing to do here.
                        }
                    });
                    try {
                        mqttClient.subscribe(topic, MqttHelper11.QOS, null, new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken imt) {
                                if (imt.getGrantedQos()[0] == 128) {
                                    LOGGER.debug("  c: {} Failed to subscribe on {}", clientId, topic);
                                    notifyError("Failed to subscribe to " + topic);
                                    // Extra countdown
                                    connectBarrier.countDown();
                                } else {
                                    LOGGER.debug("  c: {} Subscribed to {}", clientId, topic);
                                }
                                connectBarrier.countDown();
                            }

                            @Override
                            public void onFailure(IMqttToken imt, Throwable thrwbl) {
                                LOGGER.debug("Exception during subscribe for {}:", name, thrwbl.getMessage());
                                notifyError("Failed to subscribe to " + topic);
                            }
                        });
                    } catch (MqttException ex) {
                        LOGGER.debug("Exception for {} during subscribe:", name, ex.getMessage());
                        notifyError("Failed to subscribe to " + topic + ": " + ex.getMessage());
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    String message;
                    if (exception instanceof MqttException mexc) {
                        message = Integer.toString(mexc.getReasonCode());
                    } else {
                        message = exception.getMessage();
                    }
                    notifyError("MQTT connect failed: " + message);
                    connectBarrier.countDown();
                    connectBarrier.countDown();
                    LOGGER.debug("  c: {} Exception during connect: {}", name, message);
                }
            });
            try {
                if (connectBarrier.await(ServerSettings.MQTT_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                    LOGGER.debug("  c: {} connect done.", name);
                } else {
                    LOGGER.debug("  c: {} connect failed.", name);
                }
            } catch (InterruptedException ex) {
                LOGGER.error("Exception:", ex);
            }
        } catch (MqttException | IllegalArgumentException ex) {
            LOGGER.info("Exception for {} during connect:", name, ex.getMessage());
            notifyError("Could not connect to MQTT server: " + ex.getMessage());
        }
        return this;
    }

    @Override
    public JsonNode call() throws InterruptedException, MqttException {
        try {
            barrier.await();
        } catch (InterruptedException ex) {
            LOGGER.error("{} waiting for MQTT events on {} timed out: Barrier={}.", name, topic, barrier.getCount());
            LOGGER.debug("Exception:", ex);
            fail(name + " waiting for MQTT events on " + topic + " timed out: " + ex.getMessage());
        } finally {
            if (mqttClient != null) {
                LOGGER.trace("        {} Closing client: unsubscribing...", name);
                final CountDownLatch unsubBarrier = new CountDownLatch(1);
                final CountDownLatch disconnectBarrier = new CountDownLatch(1);
                if (mqttClient.isConnected()) {
                    mqttClient.unsubscribe(topic, null, new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken imt) {
                            unsubBarrier.countDown();
                        }

                        @Override
                        public void onFailure(IMqttToken imt, Throwable exception) {
                            LOGGER.error("Exception:", exception);
                            unsubBarrier.countDown();
                        }
                    });
                    unsubBarrier.await(10, TimeUnit.SECONDS);
                    LOGGER.trace("        Closing client: disconnecting...");
                    mqttClient.disconnect(null, new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            disconnectBarrier.countDown();
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            LOGGER.error("Exception:", exception);
                            disconnectBarrier.countDown();
                        }
                    });
                    disconnectBarrier.await(10, TimeUnit.SECONDS);
                }
                LOGGER.trace("        {} Closing client: closing...", name);
                // Closing the client can take a long time. Do it in the background.
                new Thread(() -> {
                    try {
                        mqttClient.close();
                    } catch (MqttException ex) {
                        LOGGER.error("Exception closing MQTT connection.", ex);
                    }
                }, "MQTT-Close").start();
                LOGGER.trace("        {} Closing client: done.", name);
            }
        }
        return result;
    }

    public interface ReceivedListener {

        public void received(String result, boolean isError);
    }

}
