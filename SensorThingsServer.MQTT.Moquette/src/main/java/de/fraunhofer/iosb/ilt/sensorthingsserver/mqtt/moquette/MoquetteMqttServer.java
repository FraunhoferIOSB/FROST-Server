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

import de.fraunhofer.iosb.ilt.sta.MqttSettings;
import de.fraunhofer.iosb.ilt.sta.mqtt.MqttServer;
import de.fraunhofer.iosb.ilt.sta.mqtt.subscription.SubscriptionEvent;
import de.fraunhofer.iosb.ilt.sta.mqtt.subscription.SubscriptionListener;
import io.moquette.BrokerConstants;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.InterceptConnectMessage;
import io.moquette.interception.messages.InterceptDisconnectMessage;
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

    private Server mqttBroker;
    private MqttClient client;
    protected EventListenerList subscriptionListeners = new EventListenerList();
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(MoquetteMqttServer.class);
    private MqttSettings settings;
    private final Map<String, List<String>> clientSubscriptions = new HashMap<>();

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
    public void removeSubscriptionListener(SubscriptionListener handler) {
        subscriptionListeners.remove(SubscriptionListener.class, handler);
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
            public void onConnect(InterceptConnectMessage msg) {
                clientSubscriptions.put(msg.getClientID(), new ArrayList<>());
            }

            @Override
            public void onDisconnect(InterceptDisconnectMessage msg) {
                clientSubscriptions.get(msg.getClientID()).stream().forEach((subscribedTopic) -> {
                    fireUnsubscribe(new SubscriptionEvent(subscribedTopic));
                });
                clientSubscriptions.remove(msg.getClientID());
            }

            @Override
            public void onSubscribe(InterceptSubscribeMessage msg) {
                clientSubscriptions.get(msg.getClientID()).add(msg.getTopicFilter());
                fireSubscribe(new SubscriptionEvent(msg.getTopicFilter()));
            }

            @Override
            public void onUnsubscribe(InterceptUnsubscribeMessage msg) {
                clientSubscriptions.get(msg.getClientID()).remove(msg.getTopicFilter());
                fireUnsubscribe(new SubscriptionEvent(msg.getTopicFilter()));
            }
        });

        IConfig config = new MemoryConfig(new Properties());
        config.setProperty(BrokerConstants.PORT_PROPERTY_NAME, Integer.toString(settings.getPort()));
        config.setProperty(BrokerConstants.HOST_PROPERTY_NAME, settings.getHost());
        config.setProperty(BrokerConstants.ALLOW_ANONYMOUS_PROPERTY_NAME, Boolean.TRUE.toString());
        config.setProperty(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME, Paths.get(settings.getTempPath(), BrokerConstants.DEFAULT_MOQUETTE_STORE_MAP_DB_FILENAME).toString());
        // TODO
        config.setProperty(BrokerConstants.WEB_SOCKET_PORT_PROPERTY_NAME, "9876");
        try {
            mqttBroker.startServer(config, userHandlers);
            String broker = "tcp://" + settings.getHost() + ":" + settings.getPort();
            String clientId = "The server itself (publish channel).";
            try {
                client = new MqttClient(broker, clientId, new MemoryPersistence());
                MqttConnectOptions connOpts = new MqttConnectOptions();
                connOpts.setCleanSession(true);
                LOGGER.info("paho-client connecting to broker: " + broker);
                try {
                    client.connect(connOpts);
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
//        Runtime.getRuntime().addShutdownHook(new Thread() {
//            @Override
//            public void run() {
//                LOGGER.info("stopping moquette mqtt broker..");
//                mqttBroker.stopServer();
//                LOGGER.info("moquette mqtt broker stopped");
//            }
//        });
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
    public void init(MqttSettings settings) {
        this.settings = settings;
    }

}
