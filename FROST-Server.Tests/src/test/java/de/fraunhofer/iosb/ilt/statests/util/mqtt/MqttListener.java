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
import org.junit.Assert;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
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
            final CountDownLatch connectBarrier = new CountDownLatch(1);
            mqttClient = new MqttAsyncClient(mqttServerUri, MqttHelper.CLIENT_ID + "-" + topic + "-" + UUID.randomUUID(), new MemoryPersistence());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            mqttClient.connect(connOpts, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    mqttClient.setCallback(new MqttCallback() {
                        @Override
                        public void connectionLost(Throwable thrwbl) {
                            LOGGER.error("Exception:", thrwbl);
                            Assert.fail("MQTT connection lost.");
                        }

                        @Override
                        public void messageArrived(String topic, MqttMessage mm) throws Exception {
                            if (barrier.getCount() > 0) {
                                result = new JSONObject(new String(mm.getPayload(), StandardCharsets.UTF_8));
                                barrier.countDown();
                                LOGGER.debug("Received on {}. To go: {}", topic, barrier.getCount());
                            } else {
                                LOGGER.error("Received on {}. Barrier already empty!", topic);
                            }
                        }

                        @Override
                        public void deliveryComplete(IMqttDeliveryToken imdt) {
                        }
                    });
                    try {
                        mqttClient.subscribe(topic, MqttHelper.QOS, null, new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken imt) {
                                LOGGER.debug("Subscribed to {}", topic);
                                connectBarrier.countDown();
                            }

                            @Override
                            public void onFailure(IMqttToken imt, Throwable thrwbl) {
                                LOGGER.error("Exception:", thrwbl);
                                Assert.fail("MQTT subscribe failed: " + thrwbl.getMessage());
                            }
                        });
                    } catch (MqttException ex) {
                        LOGGER.error("Exception:", ex);
                        Assert.fail("Error MQTT subscribe: " + ex.getMessage());
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    LOGGER.error("Exception:", exception);
                    Assert.fail("MQTT connect failed: " + exception.getMessage());
                }
            });
            try {
                connectBarrier.await();
            } catch (InterruptedException ex) {
                LOGGER.error("Exception:", ex);
            }
        } catch (MqttException | IllegalArgumentException ex) {
            LOGGER.error("Exception:", ex);
            Assert.fail("Could not connect to MQTT server: " + ex.getMessage());
        }
    }

    @Override
    public JSONObject call() throws Exception {
        try {
            barrier.await();
        } catch (InterruptedException ex) {
            LOGGER.error("waiting for MQTT events on {} timed out.", topic);
            LOGGER.error("Exception:", ex);
            Assert.fail("waiting for MQTT events on " + topic + " timed out: " + ex.getMessage());
            throw ex;
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
