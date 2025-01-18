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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
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

/**
 * Helper for connecting to MQTT and listening on topics.
 */
public class MqttListener implements Callable<JsonNode> {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MqttListener.class);

    private final CountDownLatch barrier;
    private final String topic;
    private final String mqttServerUri;

    private String username;
    private String password;

    private MqttAsyncClient mqttClient;
    private JsonNode result;
    private ReceivedListener listener;

    public MqttListener(String mqttServer, String topic) {
        this(mqttServer, topic, 1);
    }

    public MqttListener(String mqttServer, String topic, int expectedMessages) {
        this.mqttServerUri = mqttServer;
        this.topic = topic;
        barrier = new CountDownLatch(expectedMessages);
        LOGGER.debug("Created MqttListener for {} expecting {} on {}", mqttServer, expectedMessages, topic);
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
        barrier.countDown();
        LOGGER.debug("Received message, barrier now at {}", barrier.getCount());
        if (listener != null) {
            listener.received(message, false);
        }
    }

    private void notifyError(String message) {
        barrier.countDown();
        LOGGER.debug("Received error, barrier now at {}", barrier.getCount());
        if (listener != null) {
            listener.received(message, true);
        }
    }

    public MqttListener connect() {
        try {
            final CountDownLatch connectBarrier = new CountDownLatch(2);
            mqttClient = new MqttAsyncClient(mqttServerUri, "TS-" + topic + "-" + UUID.randomUUID(), new MemoryPersistence());
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
                    LOGGER.debug("sc: Subscribe to {}", subscription);
                    if (topic.equals(subscription.getTopic())) {
                        connectBarrier.countDown();
                    }
                }

                @Override
                public void onUnsubscribe(SubscriptionEvent subscription) {
                    LOGGER.debug("sc: Unsubscribe from {}", subscription);
                }
            });
            mqttClient.connect(connOpts, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    mqttClient.setCallback(new MqttCallback() {
                        @Override
                        public void connectionLost(Throwable thrwbl) {
                            LOGGER.error("Exception:", thrwbl);
                            fail("MQTT connection lost.");
                        }

                        @Override
                        public void messageArrived(String topic, MqttMessage mm) {
                            if (barrier.getCount() > 0) {
                                final String payload = new String(mm.getPayload(), StandardCharsets.UTF_8);
                                try {
                                    result = Utils.MAPPER.readTree(payload);
                                } catch (JsonProcessingException ex) {
                                    LOGGER.error("Failed to parse result", ex);
                                }
                                notifyMessage(payload);
                                LOGGER.debug("Received on {}. To go: {}", topic, barrier.getCount());
                            } else {
                                LOGGER.error("Received on {}. Barrier already empty!", topic);
                            }
                        }

                        @Override
                        public void deliveryComplete(IMqttDeliveryToken imdt) {
                            // Nothing to do here.
                        }
                    });
                    try {
                        mqttClient.subscribe(topic, MqttHelper2.QOS, null, new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken imt) {
                                if (imt.getGrantedQos()[0] == 128) {
                                    LOGGER.debug("Subscribed Failed on {}", topic);
                                    notifyError("Failed to subscribe to " + topic);
                                    // Extra countdown
                                    connectBarrier.countDown();
                                } else {
                                    LOGGER.debug("Subscribed to {}", topic);
                                }
                                connectBarrier.countDown();
                            }

                            @Override
                            public void onFailure(IMqttToken imt, Throwable thrwbl) {
                                LOGGER.error("Exception:", thrwbl);
                                notifyError("Failed to subscribe to " + topic);
                            }
                        });
                    } catch (MqttException ex) {
                        LOGGER.error("Exception:", ex);
                        notifyError("Failed to subscribe to " + topic + ": " + ex.getMessage());
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    LOGGER.error("Exception:", exception);
                    notifyError("MQTT connect failed: " + exception.getMessage());
                }
            });
            try {
                connectBarrier.await(ServerSettings.MQTT_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                LOGGER.error("Exception:", ex);
            }
        } catch (MqttException | IllegalArgumentException ex) {
            LOGGER.error("Exception:", ex);
            fail("Could not connect to MQTT server: " + ex.getMessage());
        }
        return this;
    }

    @Override
    public JsonNode call() throws InterruptedException, MqttException {
        try {
            barrier.await();
        } catch (InterruptedException ex) {
            LOGGER.error("waiting for MQTT events on {} timed out.", topic);
            LOGGER.error("Exception:", ex);
            fail("waiting for MQTT events on " + topic + " timed out: " + ex.getMessage());
        } finally {
            if (mqttClient != null) {
                LOGGER.trace("        Closing client: unsubscribing...");
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
                LOGGER.trace("        Closing client: closing...");
                // Closing the client can take a long time. Do it in the background.
                new Thread(() -> {
                    try {
                        mqttClient.close();
                    } catch (MqttException ex) {
                        LOGGER.error("Exception closing MQTT connection.", ex);
                    }
                }, "MQTT-Close").start();
                LOGGER.trace("        Closing client: done.");
            }
        }
        return result;
    }

    public interface ReceivedListener {

        public void received(String result, boolean isError);
    }

}
