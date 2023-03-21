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
package de.fraunhofer.iosb.ilt.frostserver.mqtt.moquette;

import de.fraunhofer.iosb.ilt.frostserver.mqtt.MqttServer;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.create.EntityCreateEvent;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.create.EntityCreateListener;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.subscription.SubscriptionEvent;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.subscription.SubscriptionListener;
import de.fraunhofer.iosb.ilt.frostserver.settings.ConfigDefaults;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.MqttSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValue;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValueInt;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import io.moquette.BrokerConstants;
import io.moquette.broker.Server;
import io.moquette.broker.config.IConfig;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.InterceptConnectMessage;
import io.moquette.interception.messages.InterceptDisconnectMessage;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.moquette.interception.messages.InterceptSubscribeMessage;
import io.moquette.interception.messages.InterceptUnsubscribeMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.swing.event.EventListenerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public class MoquetteMqttServer implements MqttServer, ConfigDefaults {

    /**
     * Custom Settings | Tags
     */
    @DefaultValueInt(9876)
    public static final String TAG_WEBSOCKET_PORT = "WebsocketPort";
    @DefaultValueInt(50)
    public static final String TAG_MAX_IN_FLIGHT = "maxInFlight";
    @DefaultValue("")
    public static final String TAG_KEYSTORE_PATH = "javaKeystorePath";
    @DefaultValue("")
    public static final String TAG_KEYSTORE_PASS = "keyStorePassword";
    @DefaultValue("")
    public static final String TAG_KEYMANAGER_PASS = "keyManagerPassword";
    @DefaultValueInt(8883)
    public static final String TAG_SSL_PORT = "sslPort";
    @DefaultValueInt(443)
    public static final String TAG_SSL_WEBSOCKET_PORT = "secureWebsocketPort";
    @DefaultValue("memory")
    public static final String TAG_PERSISTENT_STORE_TYPE = "persistentStoreType";

    private static final String VALUE_STORE_TYPE_H2 = "h2";

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MoquetteMqttServer.class);

    private Server mqttBroker;
    protected EventListenerList subscriptionListeners = new EventListenerList();
    protected EventListenerList entityCreateListeners = new EventListenerList();
    private CoreSettings settings;
    private final Map<String, List<String>> clientSubscriptions = new HashMap<>();

    /**
     * The MQTT Id used by the FROST server to connect to the MQTT broker.
     */
    private final String frostClientId;

    public MoquetteMqttServer() {
        frostClientId = "SensorThings API Server (" + UUID.randomUUID() + ")";
    }

    @Override
    public void addEntityCreateListener(EntityCreateListener listener) {
        entityCreateListeners.add(EntityCreateListener.class, listener);
    }

    @Override
    public void publish(String topic, String message, int qos) {
        if (mqttBroker != null) {
            final ByteBuf payload = ByteBufUtil.writeUtf8(UnpooledByteBufAllocator.DEFAULT, message);
            MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH, false, MqttQoS.valueOf(qos), false, 0);
            MqttPublishVariableHeader varHeader = new MqttPublishVariableHeader(topic, 0);
            MqttPublishMessage mqttPublishMessage = new MqttPublishMessage(fixedHeader, varHeader, payload);
            mqttBroker.internalPublish(mqttPublishMessage, frostClientId);
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

    protected void fireEntityCreate(EntityCreateEvent e) {
        Object[] listeners = entityCreateListeners.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == EntityCreateListener.class) {
                ((EntityCreateListener) listeners[i + 1]).onEntityCreate(e);
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
        final List<? extends InterceptHandler> userHandlers = Arrays.asList(new AbstractInterceptHandlerImpl());

        MqttSettings mqttSettings = settings.getMqttSettings();
        Settings customSettings = mqttSettings.getCustomSettings();
        IConfig config = new ConfigWrapper(customSettings);

        // Ensure the immediate_flush property has a default of true.
        customSettings.getBoolean(BrokerConstants.IMMEDIATE_BUFFER_FLUSH_PROPERTY_NAME, true);
        config.setProperty(BrokerConstants.PORT_PROPERTY_NAME, Integer.toString(mqttSettings.getPort()));
        config.setProperty(BrokerConstants.HOST_PROPERTY_NAME, mqttSettings.getHost());
        config.setProperty(BrokerConstants.ALLOW_ANONYMOUS_PROPERTY_NAME, Boolean.TRUE.toString());

        String persistentStoreType = customSettings.get(TAG_PERSISTENT_STORE_TYPE, getClass());
        if (VALUE_STORE_TYPE_H2.equalsIgnoreCase(persistentStoreType)) {
            String defaultPersistentStore = Paths.get(settings.getTempPath(), BrokerConstants.DEFAULT_MOQUETTE_STORE_H2_DB_FILENAME).toString();
            String persistentStore = customSettings.get(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME, defaultPersistentStore);
            config.setProperty(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME, persistentStore);
        }
        config.setProperty(BrokerConstants.WEB_SOCKET_PORT_PROPERTY_NAME, customSettings.get(TAG_WEBSOCKET_PORT, getClass()));

        String keystorePath = customSettings.get(TAG_KEYSTORE_PATH, getClass());
        if (!keystorePath.isEmpty()) {
            LOGGER.info("Configuring keystore for ssl");
            config.setProperty(BrokerConstants.JKS_PATH_PROPERTY_NAME, keystorePath);
            config.setProperty(BrokerConstants.KEY_STORE_PASSWORD_PROPERTY_NAME, customSettings.get(TAG_KEYSTORE_PASS, getClass(), false));
            config.setProperty(BrokerConstants.KEY_MANAGER_PASSWORD_PROPERTY_NAME, customSettings.get(TAG_KEYMANAGER_PASS, getClass(), false));
            config.setProperty(BrokerConstants.SSL_PORT_PROPERTY_NAME, customSettings.get(TAG_SSL_PORT, getClass()));
            config.setProperty(BrokerConstants.WSS_PORT_PROPERTY_NAME, customSettings.get(TAG_SSL_WEBSOCKET_PORT, getClass()));
        }

        AuthWrapper authWrapper = createAuthWrapper();

        mqttBroker.startServer(config, userHandlers, null, authWrapper, authWrapper);
    }

    private AuthWrapper createAuthWrapper() {
        Settings authSettings = settings.getAuthSettings();
        String authProviderClassName = authSettings.get(CoreSettings.TAG_AUTH_PROVIDER, "");
        if (!StringHelper.isNullOrEmpty(authProviderClassName)) {
            return new AuthWrapper(settings, authProviderClassName, frostClientId);
        }
        return null;
    }

    @Override
    public void stop() {
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

    private class AbstractInterceptHandlerImpl extends AbstractInterceptHandler {

        @Override
        public void onPublish(InterceptPublishMessage msg) {
            if (msg.getClientID().equalsIgnoreCase(frostClientId)) {
                return;
            }
            LOGGER.trace("      Moquette -> FROST on {}", msg.getTopicName());
            String payload = msg.getPayload().toString(StringHelper.UTF8);
            fireEntityCreate(new EntityCreateEvent(this, msg.getTopicName(), payload));
        }

        @Override
        public void onConnect(InterceptConnectMessage msg) {
            final String clientId = msg.getClientID();
            if (clientId.equalsIgnoreCase(frostClientId)) {
                return;
            }
            LOGGER.trace("      Client connected: {}", clientId);
            clientSubscriptions.put(clientId, new ArrayList<>());
        }

        @Override
        public void onDisconnect(InterceptDisconnectMessage msg) {
            final String clientId = msg.getClientID();
            if (clientId.equalsIgnoreCase(frostClientId)) {
                return;
            }
            LOGGER.trace("      Client disconnected: {}", clientId);
            clientSubscriptions.getOrDefault(clientId, new ArrayList<>())
                    .stream().forEach(
                            subscribedTopic -> fireUnsubscribe(new SubscriptionEvent(subscribedTopic))
                    );
            clientSubscriptions.remove(clientId);
        }

        @Override
        public void onSubscribe(InterceptSubscribeMessage msg) {
            final String clientId = msg.getClientID();
            if (clientId.equalsIgnoreCase(frostClientId)) {
                return;
            }
            final String topicFilter = msg.getTopicFilter();
            LOGGER.trace("      Client {} subscribed to {}", clientId, topicFilter);
            clientSubscriptions.getOrDefault(
                    clientId, new ArrayList<>()
            ).add(topicFilter);
            fireSubscribe(new SubscriptionEvent(topicFilter));
        }

        @Override
        public void onUnsubscribe(InterceptUnsubscribeMessage msg) {
            final String clientId = msg.getClientID();
            if (clientId.equalsIgnoreCase(frostClientId)) {
                return;
            }
            final String topicFilter = msg.getTopicFilter();
            LOGGER.trace("      Client {} unsubscribed from {}", clientId, topicFilter);
            clientSubscriptions.getOrDefault(clientId, new ArrayList<>())
                    .remove(topicFilter);
            fireUnsubscribe(new SubscriptionEvent(topicFilter));
        }

        @Override
        public String getID() {
            return frostClientId;
        }
    }

}
