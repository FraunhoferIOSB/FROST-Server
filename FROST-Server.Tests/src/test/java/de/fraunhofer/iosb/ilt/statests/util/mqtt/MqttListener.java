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

import de.fraunhofer.iosb.ilt.frostserver.mqtt.MqttManager;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.subscription.SubscriptionEvent;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.subscription.SubscriptionListener;
import de.fraunhofer.iosb.ilt.statests.ServerSettings;
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
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 * @author scf
 */
public class MqttListener implements Callable<JSONObject> {

    /**
     * The logger for this class.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MqttListener.class);
    private final CountDownLatch barrier;
    private final String topic;
    private final String mqttServerUri;

    private MqttAsyncClient mqttClient;
    private JSONObject result;

    public MqttListener(String mqttServer, String topic) {
        this.mqttServerUri = mqttServer;
        this.topic = topic;
        barrier = new CountDownLatch(1);
    }

    public void connect() {
        try {
            final CountDownLatch connectBarrier = new CountDownLatch(2);
            mqttClient = new MqttAsyncClient(mqttServerUri, MqttHelper.CLIENT_ID + "-" + topic + "-" + UUID.randomUUID(), new MemoryPersistence());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            MqttManager.addTestSubscriptionListener(new SubscriptionListener() {
                @Override
                public void onSubscribe(SubscriptionEvent subscription) {
                    if (topic.equals(subscription.getTopic())) {
                        connectBarrier.countDown();
                    }
                }

                @Override
                public void onUnsubscribe(SubscriptionEvent subscription) {
                }
            });
            mqttClient.connect(connOpts, new IMqttActionListener() {
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
                                result = new JSONObject(new String(mm.getPayload(), StandardCharsets.UTF_8));
                                barrier.countDown();
                                LOGGER.debug("          Received on {}. To go: {}", topic, barrier.getCount());
                            } else {
                                LOGGER.error("          Received on {}. Barrier already empty!", topic);
                            }
                        }

                        @Override
                        public void deliveryComplete(IMqttDeliveryToken imdt) {
                            // Nothing to do here.
                        }
                    });
                    try {
                        mqttClient.subscribe(topic, MqttHelper.QOS, null, new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken imt) {
                                LOGGER.debug("      Subscribed to {}", topic);
                                connectBarrier.countDown();
                            }

                            @Override
                            public void onFailure(IMqttToken imt, Throwable thrwbl) {
                                LOGGER.error("Exception:", thrwbl);
                                fail("MQTT subscribe failed: " + thrwbl.getMessage());
                            }
                        });
                    } catch (MqttException ex) {
                        LOGGER.error("Exception:", ex);
                        fail("Error MQTT subscribe: " + ex.getMessage());
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    LOGGER.error("Exception:", exception);
                    fail("MQTT connect failed: " + exception.getMessage());
                }
            });
            try {
                connectBarrier.await(ServerSettings.MQTT_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                LOGGER.error("Exception:", ex);
            }
        } catch (MqttException | IllegalArgumentException ex) {
            LOGGER.error("Exception:", ex);
            fail("Could not connect to MQTT server: " + ex.getMessage());
        }
    }

    @Override
    public JSONObject call() throws InterruptedException, MqttException {
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
}
