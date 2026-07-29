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
package de.fraunhofer.iosb.ilt.frostserver.mqtt.moquette;

import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.TAG_AUTH_ALLOW_ANON_READ;

import de.fraunhofer.iosb.ilt.frostserver.mqtt.MqttManager;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.MqttServer;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.create.RequestEvent;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.create.RequestEventListener;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.subscription.SubscriptionEvent;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.subscription.SubscriptionListener;
import de.fraunhofer.iosb.ilt.frostserver.parser.path.PathParser;
import de.fraunhofer.iosb.ilt.frostserver.parser.query.QueryParser;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.request.ServiceContext;
import de.fraunhofer.iosb.ilt.frostserver.request.Version;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.MqttSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.UnknownVersionException;
import de.fraunhofer.iosb.ilt.frostserver.util.MetricsSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.UserCaches;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import de.fraunhofer.iosb.ilt.settings.ConfigDefaults;
import de.fraunhofer.iosb.ilt.settings.Settings;
import de.fraunhofer.iosb.ilt.settings.annotation.DefaultValue;
import de.fraunhofer.iosb.ilt.settings.annotation.DefaultValueInt;
import de.fraunhofer.iosb.ilt.settings.annotation.SensitiveValue;
import io.moquette.BrokerConstants;
import io.moquette.broker.Server;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.subscriptions.Subscription;
import io.moquette.broker.subscriptions.Topic;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.InterceptHandler;
import io.moquette.interception.TopicRewriter;
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
import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Moquette implementation of the FROST MqttServer interface.
 */
public class MoquetteMqttServer implements MqttServer, ConfigDefaults, TopicRewriter {

    /**
     * Custom Settings | Tags
     */
    @DefaultValueInt(9876)
    public static final String TAG_WEBSOCKET_PORT = "WebsocketPort";
    @DefaultValueInt(50)
    public static final String TAG_MAX_IN_FLIGHT = "maxInFlight";
    @DefaultValue("")
    public static final String TAG_KEYSTORE_PATH = "javaKeystorePath";
    @SensitiveValue
    @DefaultValue("")
    public static final String TAG_KEYSTORE_PASS = "keyStorePassword";
    @SensitiveValue
    @DefaultValue("")
    public static final String TAG_KEYMANAGER_PASS = "keyManagerPassword";
    @DefaultValueInt(8883)
    public static final String TAG_SSL_PORT = "sslPort";
    @DefaultValueInt(443)
    public static final String TAG_SSL_WEBSOCKET_PORT = "secureWebsocketPort";
    @DefaultValue("memory")
    public static final String TAG_PERSISTENT_STORE_TYPE = "persistentStoreType";
    @DefaultValue("")
    public static final String TAG_MQTT_TOPIC_ALLOWLIST = "mqtt.topicAllowList";

    private static final String VALUE_STORE_TYPE_H2 = "h2";

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MoquetteMqttServer.class);

    /**
     * TODO: Make this configurable in Moquette, and fix it there!
     */
    public String responseTopicBase = BrokerConstants.RESPONSE_TOPIC_BASE;

    private Server mqttBroker;
    protected List<SubscriptionListener> subscriptionListeners = new CopyOnWriteArrayList<>();
    protected List<RequestEventListener> entityCreateListeners = new CopyOnWriteArrayList<>();
    private CoreSettings settings;
    private ServiceContext contextNormalise;
    private boolean fineGrainedAuth = false;
    private final AtomicLong keyGenerator = new AtomicLong();
    private final Map<String, Set<String>> clientSubscriptions = new HashMap<>();
    private AuthWrapper authWrapper;

    /**
     * The MQTT Id used by the FROST server to connect to the MQTT broker.
     */
    private final String frostClientId;

    public MoquetteMqttServer() {
        frostClientId = "SensorThings API Server (" + UUID.randomUUID() + ")";
    }

    @Override
    public UserCaches getUserCaches() {
        if (authWrapper == null) {
            return null;
        }
        return authWrapper.getUserCaches();
    }

    @Override
    public void addEntityCreateListener(RequestEventListener listener) {
        entityCreateListeners.add(listener);
    }

    @Override
    public void publish(String topic, String message, int qos) {
        publish(topic, message, qos, null);
    }

    @Override
    public void publish(String topic, String message, int qos, String contentType, Map<String, String> userProps, byte[] corrData) {
        MqttProperties props = new MqttProperties();
        if (corrData != null) {
            props.add(new MqttProperties.BinaryProperty(MqttProperties.MqttPropertyType.CORRELATION_DATA.value(), corrData));
        }
        if (!StringHelper.isNullOrEmpty(contentType)) {
            props.add(new MqttProperties.StringProperty(MqttProperties.MqttPropertyType.CONTENT_TYPE.value(), contentType));
        }
        for (var entry : userProps.entrySet()) {
            props.add(new MqttProperties.UserProperty(entry.getKey(), entry.getValue()));
        }
        publish(topic, message, qos, props);
    }

    public void publish(String topic, String message, int qos, MqttProperties properties) {
        LOGGER.debug("Publishing to {}", topic);
        if (mqttBroker != null) {
            final ByteBuf payload = ByteBufUtil.writeUtf8(UnpooledByteBufAllocator.DEFAULT, message);
            MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH, false, MqttQoS.valueOf(qos), false, 0);
            MqttPublishVariableHeader varHeader = new MqttPublishVariableHeader(topic, 0, properties);
            MqttPublishMessage mqttPublishMessage = new MqttPublishMessage(fixedHeader, varHeader, payload);
            mqttBroker.internalPublish(mqttPublishMessage, frostClientId);
        }
    }

    @Override
    public void addSubscriptionListener(SubscriptionListener listener) {
        subscriptionListeners.add(listener);
    }

    @Override
    public void removeEntityCreateListener(RequestEventListener listener) {
        entityCreateListeners.remove(listener);
    }

    @Override
    public void removeSubscriptionListener(SubscriptionListener listener) {
        subscriptionListeners.remove(listener);
    }

    protected void fireEntityCreate(RequestEvent e) {
        for (var l : entityCreateListeners) {
            try {
                l.onRequestReceived(e);
            } catch (RuntimeException ex) {
                LOGGER.debug("Exception handling entity create.", ex);
            }
        }
    }

    protected void fireSubscribe(SubscriptionEvent e) {
        for (var l : subscriptionListeners) {
            try {
                l.onSubscribe(e);
            } catch (RuntimeException ex) {
                LOGGER.debug("Exception handling subscribe.", ex);
            }
        }
    }

    protected void fireUnsubscribe(SubscriptionEvent e) {
        for (var l : subscriptionListeners) {
            try {
                l.onUnsubscribe(e);
            } catch (RuntimeException ex) {
                LOGGER.debug("Exception handling unSubscribe.", ex);
            }
        }
    }

    @Override
    public void start() {
        mqttBroker = new Server();
        final List<? extends InterceptHandler> userHandlers = Arrays.asList(new AbstractInterceptHandlerImpl());

        final MqttSettings mqttSettings = settings.getMqttSettings();
        final Settings customSettings = mqttSettings.getCustomSettings();
        final Settings authSettings = settings.getAuthSettings();
        final MetricsSettings metricsSettings = settings.getMetricsSettings();
        final boolean allowAnonRead = authSettings.getBoolean(TAG_AUTH_ALLOW_ANON_READ, CoreSettings.class);

        final ConfigWrapper config = new ConfigWrapper(customSettings);
        if (authWrapper != null) {
            config.setProperty(IConfig.ALLOW_ANONYMOUS_PROPERTY_NAME, allowAnonRead);
        }
        mqttBroker.setTopicRewriter(this);

        // Ensure the immediate_flush property has a default of true.
        config.intProp(IConfig.BUFFER_FLUSH_MS_PROPERTY_NAME, 0);
        config.boolProp(IConfig.ENABLE_TELEMETRY_NAME, false);
        config.setProperty(IConfig.PORT_PROPERTY_NAME, Integer.toString(mqttSettings.getPort()));
        config.setProperty(IConfig.HOST_PROPERTY_NAME, mqttSettings.getHost());
        config.setProperty(IConfig.ALLOW_ANONYMOUS_PROPERTY_NAME, Boolean.TRUE.toString());

        if (metricsSettings.isEnabled()) {
            if (!customSettings.containsName("metrics.provider.class")) {
                customSettings.set("metrics.provider.class", "MetricsProviderPrometheus");
            }
            customSettings.set("metrics.endpoint.port", 0);
        }

        String persistentStoreType = customSettings.get(TAG_PERSISTENT_STORE_TYPE, getClass());
        if (VALUE_STORE_TYPE_H2.equalsIgnoreCase(persistentStoreType)) {
            String tempPath = Paths.get(settings.getTempPath()).toString();
            String persistentStore = customSettings.get(IConfig.DATA_PATH_PROPERTY_NAME, tempPath);
            config.setProperty(IConfig.DATA_PATH_PROPERTY_NAME, persistentStore);
        }
        config.setProperty(IConfig.WEB_SOCKET_PORT_PROPERTY_NAME, customSettings.get(TAG_WEBSOCKET_PORT, getClass()));

        String keystorePath = customSettings.get(TAG_KEYSTORE_PATH, getClass());
        if (!keystorePath.isEmpty()) {
            LOGGER.info("Configuring keystore for ssl");
            config.setProperty(IConfig.JKS_PATH_PROPERTY_NAME, keystorePath);
            config.setProperty(IConfig.KEY_STORE_PASSWORD_PROPERTY_NAME, customSettings.get(TAG_KEYSTORE_PASS, getClass()));
            config.setProperty(IConfig.KEY_MANAGER_PASSWORD_PROPERTY_NAME, customSettings.get(TAG_KEYMANAGER_PASS, getClass()));
            config.setProperty(IConfig.SSL_PORT_PROPERTY_NAME, customSettings.get(TAG_SSL_PORT, getClass()));
            config.setProperty(IConfig.WSS_PORT_PROPERTY_NAME, customSettings.get(TAG_SSL_WEBSOCKET_PORT, getClass()));
        }

        try {
            mqttBroker.startServer(config, userHandlers, null, authWrapper, authWrapper);
        } catch (IOException ex) {
            LOGGER.error("Failed to start MQTT Broker!", ex);
        }
    }

    private AuthWrapper createAuthWrapper() {
        Settings authSettings = settings.getAuthSettings();
        String authProviderClassName = authSettings.get(CoreSettings.TAG_AUTH_PROVIDER, "");
        if (!StringHelper.isNullOrEmpty(authProviderClassName)) {
            fineGrainedAuth = authSettings.getBoolean(CoreSettings.TAG_AUTH_MQTT_FINE_GRAINED_AUTH, CoreSettings.class);
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
        contextNormalise = new ServiceContext()
                .setFunctionRegistry(settings.getFunctionRegistry())
                .setModelRegistry(settings.getModelRegistry())
                .setMqttContext(true)
                .setQueryDefaults(settings.getQueryDefaults()
                        .copy()
                        .setAlwaysOrder(false));
        authWrapper = createAuthWrapper();
    }

    private String normaliseTopic(String topicClient) {
        final int idx = topicClient.indexOf('?');
        String pathString = idx >= 0
                ? topicClient.substring(0, idx)
                : topicClient;
        String queryString = idx >= 0
                ? topicClient.substring(idx + 1)
                : "";
        String topicInternal = StringUtils.removeStart(topicClient, '/');
        Version version;
        try {
            version = MqttManager.getVersionFromTopic(settings, topicInternal);
            final String pathNonVersion = pathString.substring(version.urlPart.length());
            final ResourcePath path = PathParser.parsePath(contextNormalise, version, pathNonVersion);
            final Query query = QueryParser.parseQuery(queryString, contextNormalise, path)
                    .validate(null, path.getMainElementType());
            query.normalise();
            final String nQueryString = query.toUnencodedString(false);
            if (StringHelper.isNullOrEmpty(nQueryString)) {
                topicInternal = version.urlPart + '/' + path.getUrl();
            } else {
                topicInternal = version.urlPart + '/' + path.getUrl() + "?" + nQueryString;
            }
        } catch (UnknownVersionException ex) {
            // Not a STA topic.
            LOGGER.debug("\nNormalised {}\n        to {}", topicClient, topicInternal);
            return topicInternal;
        } catch (RuntimeException ex) {
            LOGGER.debug("Failed to normalise {}", topicClient);
            return topicInternal;
        }
        LOGGER.debug("Normalised {}\n        to {}", topicClient, topicInternal);
        return topicInternal;
    }

    @Override
    public Topic rewriteTopic(Subscription subscription) {
        String clientId = subscription.getClientId();
        final Topic topicFilterClient = subscription.getTopicFilterClient();
        final String topicClient = topicFilterClient.toString();
        if (topicClient.startsWith(responseTopicBase)) {
            // Response topics must not be rewritten or normalised.
            return topicFilterClient;
        }

        String topicInternal = normaliseTopic(topicClient);

        if (fineGrainedAuth) {
            PrincipalExtended userPrincipal = authWrapper.getUserPrincipal(clientId);
            long userKey = userPrincipal.getUserKey();
            if (userKey < 0) {
                userKey = keyGenerator.incrementAndGet();
                userPrincipal.updateUserKey(userKey);
                authWrapper.getUserCaches().registerPrincipal(userKey, userPrincipal);
            }
            topicInternal = Long.toString(userKey) + '/' + topicInternal;
            LOGGER.debug("Topic rewritten from {} to {}", topicClient, topicInternal);
        }
        return Topic.asTopic(topicInternal);
    }

    @Override
    public Topic rewriteTopicInverse(Topic clientTopic, Topic publishedTopic) {
        LOGGER.debug("Inverse Topic rewrite request from {} for {}", clientTopic, publishedTopic);
        return publishedTopic;
    }

    private class AbstractInterceptHandlerImpl extends AbstractInterceptHandler {

        @Override
        public void onPublish(InterceptPublishMessage msg) {
            try {
                if (frostClientId.equals(msg.getClientID())) {
                    return;
                }
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("      Moquette -> FROST on {}", msg.getTopicName());
                }
                String payload = msg.getPayload().toString(StringHelper.UTF8);
                PrincipalExtended userPrincipal;
                if (authWrapper == null) {
                    userPrincipal = PrincipalExtended.ANONYMOUS_PRINCIPAL;
                } else {
                    userPrincipal = authWrapper.getUserPrincipal(msg.getClientID());
                }
                final MqttPublishVariableHeader variableHeader = msg.getMessage().variableHeader();
                final MqttProperties mqttProps = variableHeader.properties();
                final RequestEvent event = new RequestEvent(msg.getTopicName(), payload, userPrincipal);

                final List<MqttProperties.UserProperty> userProps = (List<MqttProperties.UserProperty>) mqttProps.getProperties(MqttProperties.MqttPropertyType.USER_PROPERTY.value());
                for (MqttProperties.UserProperty p : userProps) {
                    MqttProperties.StringPair v = p.value();
                    event.addUserProperty(v.key, v.value);
                }
                final MqttProperties.StringProperty responseTopicProp = (MqttProperties.StringProperty) mqttProps.getProperty(MqttProperties.MqttPropertyType.RESPONSE_TOPIC.value());
                if (responseTopicProp != null) {
                    event.setResponseTopic(responseTopicProp.value());
                }
                final MqttProperties.BinaryProperty correlationDataProp = (MqttProperties.BinaryProperty) mqttProps.getProperty(MqttProperties.MqttPropertyType.CORRELATION_DATA.value());
                if (correlationDataProp != null) {
                    event.setCorrelationData(correlationDataProp.value());
                }
                final MqttProperties.StringProperty contentType = (MqttProperties.StringProperty) mqttProps.getProperty(MqttProperties.MqttPropertyType.CONTENT_TYPE.value());
                if (contentType != null) {
                    event.setContentType(contentType.value());
                }

                fireEntityCreate(event);
            } finally {
                super.onPublish(msg);
            }
        }

        @Override
        public void onConnect(InterceptConnectMessage msg) {
            final String clientId = msg.getClientID();
            if (clientId.equalsIgnoreCase(frostClientId)) {
                return;
            }
            LOGGER.trace("      Client connected: {}", clientId);
            clientSubscriptions.put(clientId, ConcurrentHashMap.newKeySet());
        }

        @Override
        public void onDisconnect(InterceptDisconnectMessage msg) {
            final String clientId = msg.getClientID();
            if (clientId.equalsIgnoreCase(frostClientId)) {
                return;
            }
            LOGGER.trace("      Client disconnected: {}", clientId);
            clientSubscriptions
                    .getOrDefault(clientId, Collections.emptySet())
                    .forEach(subscribedTopic -> fireUnsubscribe(new SubscriptionEvent(clientId, subscribedTopic, null)));
            clientSubscriptions.remove(clientId);
        }

        @Override
        public void onSubscribe(InterceptSubscribeMessage msg) {
            final String clientId = msg.getClientID();
            if (clientId.equalsIgnoreCase(frostClientId)) {
                return;
            }
            final String topicFilterClient = msg.getTopicFilterClient();
            final String topicFilterIntrnl = msg.getTopicFilterInternal();
            if (topicFilterIntrnl.startsWith(responseTopicBase)) {
                LOGGER.debug("      Ignoring subscription of {} to response topic {}", clientId, topicFilterIntrnl);
                return;
            }
            LOGGER.debug("      Client {} subscribed to {}", clientId, topicFilterIntrnl);
            if (clientSubscriptions
                    .computeIfAbsent(clientId, t -> ConcurrentHashMap.newKeySet())
                    .add(topicFilterIntrnl)) {
                fireSubscribe(new SubscriptionEvent(clientId, topicFilterIntrnl, topicFilterClient));
            } else {
                LOGGER.warn("Client {} subscribed to {} twice!", clientId, topicFilterIntrnl);
            }
        }

        @Override
        public void onUnsubscribe(InterceptUnsubscribeMessage msg) {
            final String clientId = msg.getClientID();
            if (frostClientId.equals(clientId)) {
                return;
            }
            final String topicFilterIntrnl = msg.getTopicFilterInternal();
            final String topicFilterClient = msg.getTopicFilterClient();
            LOGGER.debug("      Client {} unsubscribed from {}", clientId, topicFilterIntrnl);
            boolean removed = clientSubscriptions.getOrDefault(clientId, Collections.emptySet())
                    .remove(topicFilterIntrnl);
            if (removed) {
                fireUnsubscribe(new SubscriptionEvent(clientId, topicFilterIntrnl, topicFilterClient));
            }
        }

        @Override
        public String getID() {
            return frostClientId;
        }

        @Override
        public void onSessionLoopError(Throwable thrwbl) {
            LOGGER.error("MQTT Session Loop caused an exception!", thrwbl);
        }
    }

}
