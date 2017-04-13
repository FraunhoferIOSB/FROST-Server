/*
 * Copyright (C) 2016 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.sensorthingsserver.mqtt.moquette;

import de.fraunhofer.iosb.ilt.sta.mqtt.MqttServer;
import de.fraunhofer.iosb.ilt.sta.mqtt.create.EntityCreateListener;
import de.fraunhofer.iosb.ilt.sta.mqtt.create.ObservationCreateEvent;
import de.fraunhofer.iosb.ilt.sta.mqtt.subscription.SubscriptionEvent;
import de.fraunhofer.iosb.ilt.sta.mqtt.subscription.SubscriptionListener;
import de.fraunhofer.iosb.ilt.sta.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.sta.settings.MqttSettings;
import io.moquette.BrokerConstants;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.InterceptConnectMessage;
import io.moquette.interception.messages.InterceptDisconnectMessage;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.moquette.interception.messages.InterceptSubscribeMessage;
import io.moquette.interception.messages.InterceptUnsubscribeMessage;
import io.moquette.server.Server;
import io.moquette.server.config.IConfig;
import io.moquette.server.config.MemoryConfig;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import javax.swing.event.EventListenerList;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public class MoquetteMqttServer implements MqttServer {

    /**
     * Custom Settings | Tags
     */
    private static final String TAG_WEBSOCKET_PORT = "WebsocketPort";
    /**
     * Custom Settings | Default values
     */
    private static final int DEFAULT_WEBSOCKET_PORT = 9876;

    private Server mqttBroker;
    private MqttClient client;
    protected EventListenerList subscriptionListeners = new EventListenerList();
    protected EventListenerList entityCreateListeners = new EventListenerList();
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MoquetteMqttServer.class);
    private CoreSettings settings;
    private final Map<String, List<String>> clientSubscriptions = new HashMap<>();
    private final String clientId;

    public MoquetteMqttServer() {
        clientId = "SensorThings API Server (" + UUID.randomUUID() + ")";
    }

    @Override
    public void addEntityCreateListener(EntityCreateListener listener) {
        entityCreateListeners.add(EntityCreateListener.class, listener);
    }

    @Override
    public void publish(String topic, byte[] payload, int qos) {
        if (mqttBroker != null && client != null) {
            if (!client.isConnected()) {
                LOGGER.warn("MQTT client is not connected while trying to publish.");
            } else {
                try {
                    client.publish(topic, payload, qos, false);
                } catch (MqttException ex) {
                    LOGGER.error("publish on topic '" + topic + "' failed.", ex);
                }
            }
        }
    }

    @Override
    public void addSubscriptionListener(SubscriptionListener listener) {
        subscriptionListeners.add(SubscriptionListener.class, listener);
    }

    @Override
    public void removeEntityCreateListener(EntityCreateListener listener) {
        entityCreateListeners.remove(EntityCreateListener.class, listener);
    }

    @Override
    public void removeSubscriptionListener(SubscriptionListener listener) {
        subscriptionListeners.remove(SubscriptionListener.class, listener);
    }

    protected void fireObservationCreate(ObservationCreateEvent e) {
        Object[] listeners = entityCreateListeners.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == EntityCreateListener.class) {
                ((EntityCreateListener) listeners[i + 1]).onObservationCreate(e);
            }
        }
    }

    protected void fireSubscribe(SubscriptionEvent e) {
        Object[] listeners = subscriptionListeners.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == SubscriptionListener.class) {
                ((SubscriptionListener) listeners[i + 1]).onSubscribe(e);
            }
        }
    }

    protected void fireUnsubscribe(SubscriptionEvent e) {
        Object[] listeners = subscriptionListeners.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == SubscriptionListener.class) {
                ((SubscriptionListener) listeners[i + 1]).onUnsubscribe(e);
            }
        }
    }

    @Override
    public void start() {
        mqttBroker = new Server();
        final List<? extends InterceptHandler> userHandlers = Arrays.asList(new AbstractInterceptHandler() {

            @Override
            public void onPublish(InterceptPublishMessage msg) {
                if (msg.getClientID().equalsIgnoreCase(clientId)) {
                    return;
                }
                fireObservationCreate(new ObservationCreateEvent(this, msg.getTopicName(), new String(msg.getPayload().array())));
            }

            @Override
            public void onConnect(InterceptConnectMessage msg) {
                if (msg.getClientID().equalsIgnoreCase(clientId)) {
                    return;
                }
                clientSubscriptions.put(msg.getClientID(), new ArrayList<>());
            }

            @Override
            public void onDisconnect(InterceptDisconnectMessage msg) {
                if (msg.getClientID().equalsIgnoreCase(clientId)) {
                    return;
                }
                clientSubscriptions.get(msg.getClientID()).stream().forEach((subscribedTopic) -> {
                    fireUnsubscribe(new SubscriptionEvent(subscribedTopic));
                });
                clientSubscriptions.remove(msg.getClientID());
            }

            @Override
            public void onSubscribe(InterceptSubscribeMessage msg) {
                if (msg.getClientID().equalsIgnoreCase(clientId)) {
                    return;
                }
                clientSubscriptions.get(msg.getClientID()).add(msg.getTopicFilter());
                fireSubscribe(new SubscriptionEvent(msg.getTopicFilter()));
            }

            @Override
            public void onUnsubscribe(InterceptUnsubscribeMessage msg) {
                if (msg.getClientID().equalsIgnoreCase(clientId)) {
                    return;
                }
                clientSubscriptions.get(msg.getClientID()).remove(msg.getTopicFilter());
                fireUnsubscribe(new SubscriptionEvent(msg.getTopicFilter()));
            }
        });

        IConfig config = new MemoryConfig(new Properties());
        MqttSettings mqttSettings = settings.getMqttSettings();
        config.setProperty(BrokerConstants.PORT_PROPERTY_NAME, Integer.toString(mqttSettings.getPort()));
        config.setProperty(BrokerConstants.HOST_PROPERTY_NAME, mqttSettings.getHost());
        config.setProperty(BrokerConstants.ALLOW_ANONYMOUS_PROPERTY_NAME, Boolean.TRUE.toString());
        config.setProperty(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME,
                Paths.get(settings.getTempPath(),
                        BrokerConstants.DEFAULT_MOQUETTE_STORE_MAP_DB_FILENAME).toString());
        config.setProperty(BrokerConstants.WEB_SOCKET_PORT_PROPERTY_NAME,
                mqttSettings.getCustomSettings().getWithDefault(TAG_WEBSOCKET_PORT, DEFAULT_WEBSOCKET_PORT, Integer.class).toString());
        try {
            mqttBroker.startServer(config, userHandlers);
            String broker = "tcp://" + mqttSettings.getInternalHost() + ":" + mqttSettings.getPort();
            try {
                client = new MqttClient(broker, clientId, new MemoryPersistence());
                MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setCleanSession(true);
                LOGGER.info("paho-client connecting to broker: " + broker);
                try {
                    client.connect(connOpts);
                    client.subscribe("#");
                    LOGGER.info("paho-client connected to broker");
                } catch (MqttException ex) {
                    LOGGER.error("Could not connect to MQTT server.", ex);
                }
            } catch (MqttException ex) {
                LOGGER.error("Could not create MQTT Client.", ex);
            }
        } catch (IOException ex) {
            LOGGER.error("Could not start MQTT server.", ex);
        }
    }

    @Override
    public void stop() {
        if (client != null && client.isConnected()) {
            try {
                client.disconnectForcibly();
            } catch (MqttException ex) {
                LOGGER.debug("exception when forcefully disconnecting MQTT client", ex);
            }
        }
        if (mqttBroker != null) {
            mqttBroker.stopServer();
        }
    }

    @Override
    public void init(CoreSettings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("MqttSettings must be non-null");
        }
        this.settings = settings;
    }

}
