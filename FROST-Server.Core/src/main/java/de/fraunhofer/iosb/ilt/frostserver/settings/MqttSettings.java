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
package de.fraunhofer.iosb.ilt.frostserver.settings;

import de.fraunhofer.iosb.ilt.frostserver.extensions.Extension;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.MqttServer;
import de.fraunhofer.iosb.ilt.frostserver.mqtt.MqttServerFactory;
import static de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings.PREFIX_MQTT;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValue;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValueBoolean;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValueInt;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public class MqttSettings implements ConfigDefaults {

    /**
     * Tags
     */
    @DefaultValue("de.fraunhofer.iosb.ilt.sensorthingsserver.mqtt.moquette.MoquetteMqttServer")
    public static final String TAG_IMPLEMENTATION_CLASS = "mqttServerImplementationClass";
    @DefaultValueBoolean(true)
    public static final String TAG_ENABLED = "Enabled";
    @DefaultValueInt(2)
    public static final String TAG_QOS = "QoS";
    @DefaultValueInt(1883)
    public static final String TAG_PORT = "Port";
    @DefaultValue("0.0.0.0")
    public static final String TAG_HOST = "Host";
    @DefaultValue("localhost")
    public static final String TAG_HOST_INTERNAL = "internalHost";
    @DefaultValueInt(10)
    public static final String TAG_SUBSCRIBE_MESSAGE_QUEUE_SIZE = "SubscribeMessageQueueSize";
    @DefaultValueInt(10)
    public static final String TAG_SUBSCRIBE_THREAD_POOL_SIZE = "SubscribeThreadPoolSize";
    @DefaultValueInt(10)
    public static final String TAG_CREATE_MESSAGE_QUEUE_SIZE = "CreateMessageQueueSize";
    @DefaultValueInt(5)
    public static final String TAG_CREATE_THREAD_POOL_SIZE = "CreateThreadPoolSize";
    @DefaultValue("")
    public static final String TAG_EXPOSED_MQTT_ENDPOINTS = "exposedEndpoints";

    /**
     * Constraints
     */
    public static final int MIN_PORT = 1025;
    public static final int MAX_PORT = 65535;
    public static final int MIN_QOS_LEVEL = 0;
    public static final int MAX_QOS_LEVEL = 2;

    private static final String MUST_BE_POSITIVE = " must be > 0";

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MqttSettings.class);

    /**
     * The core settings that is the parent of this mqttSettings.
     */
    private CoreSettings coreSettings;
    /**
     * Fully-qualified class name of the MqttServer implementation class.
     */
    private String mqttServerImplementationClass;

    /**
     * Defines if MQTT should be enabled or not.
     */
    private boolean enableMqtt;

    /**
     * The external IP address or host name the MQTT server should listen on.
     * Set to 0.0.0.0 to listen on all interfaces.
     */
    private String host;

    /**
     * The internal host name of the MQTT server.
     */
    private String internalHost;

    /**
     * The port used to run the MQTT server.
     */
    private int port;

    /**
     * Quality of Service Level used to deliver MQTT messages
     */
    private int qosLevel;

    /**
     * The endpoint of the mqtt service to expose to clients.
     */
    private List<String> endpoints;

    /**
     * Queue size for subscribe messages passed between PersistenceManager and
     * MqttManager.
     */
    private int subscribeMessageQueueSize;
    /**
     * Number of threads used to process EntityChangeEvents.
     */
    private int subscribeThreadPoolSize;
    /**
     * Queue size for create messages passed between PersistenceManager and
     * MqttManager.
     */
    private int createMessageQueueSize;
    /**
     * Number of threads used to process ObservationCreateEvents.
     */
    private int createThreadPoolSize;
    /**
     * Extension point for implementation specific settings.
     */
    private Settings customSettings;

    public MqttSettings(CoreSettings coreSettings, Settings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("settings most be non-null");
        }
        this.coreSettings = coreSettings;
        init(coreSettings, settings);
    }

    private void init(CoreSettings coreSettings, Settings customSettings) {
        this.customSettings = customSettings;
        mqttServerImplementationClass = customSettings.get(TAG_IMPLEMENTATION_CLASS, getClass());
        enableMqtt = customSettings.getBoolean(TAG_ENABLED, getClass());
        port = customSettings.getInt(TAG_PORT, getClass());
        setHost(customSettings.get(TAG_HOST, getClass()));
        setInternalHost(customSettings.get(TAG_HOST_INTERNAL, getClass()));
        setSubscribeMessageQueueSize(customSettings.getInt(TAG_SUBSCRIBE_MESSAGE_QUEUE_SIZE, getClass()));
        setSubscribeThreadPoolSize(customSettings.getInt(TAG_SUBSCRIBE_THREAD_POOL_SIZE, getClass()));
        setCreateMessageQueueSize(customSettings.getInt(TAG_CREATE_MESSAGE_QUEUE_SIZE, getClass()));
        setCreateThreadPoolSize(customSettings.getInt(TAG_CREATE_THREAD_POOL_SIZE, getClass()));
        setQosLevel(customSettings.getInt(TAG_QOS, getClass()));

        if (enableMqtt) {
            coreSettings.getEnabledExtensions().add(Extension.MQTT);
        }
    }

    private void searchExposedEndpoints(CoreSettings coreSettings) {
        if (!enableMqtt) {
            endpoints = Collections.emptyList();
            return;
        }
        String endpointsString = customSettings.get(TAG_EXPOSED_MQTT_ENDPOINTS, getClass());
        if (!endpointsString.isEmpty()) {
            String[] splitEndpoints = endpointsString.split(",");
            endpoints = Collections.unmodifiableList(Arrays.asList(splitEndpoints));
        } else {
            String serviceRootUrl = coreSettings.getServiceRootUrl(Version.v_1_1);
            try {
                URL serviceRoot = new URL(serviceRootUrl);
                endpoints.add("mqtt://" + serviceRoot.getHost() + ":" + getPort());
                LOGGER.info("Generated MQTT endpoint list: {}", endpoints);
                LOGGER.info("Please set " + PREFIX_MQTT + "." + TAG_EXPOSED_MQTT_ENDPOINTS + " to set the correct MQTT end points.");
            } catch (MalformedURLException ex) {
                LOGGER.error("Failed to create MQTT urls.", ex);
            }
        }
    }

    public void fillServerSettings(Map<String, Object> target) {
        if (enableMqtt) {
            if (endpoints == null) {
                searchExposedEndpoints(coreSettings);
            }
            for (String requirement : Extension.MQTT.getRequirements()) {
                Map<String, Object> mqttSettings = new HashMap<>();
                mqttSettings.put("endpoints", endpoints);
                target.put(requirement, mqttSettings);
            }
        }
    }

    public boolean isEnableMqtt() {
        return enableMqtt;
    }

    public int getPort() {
        return port;
    }

    public int getQosLevel() {
        return qosLevel;
    }

    public void setEnableMqtt(boolean enableMqtt) {
        this.enableMqtt = enableMqtt;
    }

    public void setQosLevel(int qosLevel) {
        if (qosLevel < MIN_QOS_LEVEL || qosLevel > MAX_QOS_LEVEL) {
            throw new IllegalArgumentException(TAG_QOS + " must be between " + MIN_QOS_LEVEL + " and " + MAX_QOS_LEVEL);
        }
        this.qosLevel = qosLevel;
    }

    /**
     * The external IP address or host name the MQTT server should listen on.
     * Set to 0.0.0.0 to listen on all interfaces.
     *
     * @return The external IP address or host name the MQTT server should
     * listen on.
     */
    public String getHost() {
        return host;
    }

    /**
     * The internal host name of the MQTT server.
     *
     * @return The internal host name of the MQTT server.
     */
    public String getInternalHost() {
        return internalHost;
    }

    public String getTopicPrefix(Version version) {
        return version.urlPart + "/";
    }

    /**
     * The external IP address or host name the MQTT server should listen on.
     * Set to 0.0.0.0 to listen on all interfaces.
     *
     * @param host The external IP address or host name the MQTT server should
     * listen on.
     */
    public void setHost(String host) {
        if (host == null || host.isEmpty()) {
            throw new IllegalArgumentException(TAG_HOST + " must be non-empty");
        }
        this.host = host;
    }

    /**
     * The internal host name of the MQTT server.
     *
     * @param internalHost The internal host name of the MQTT server.
     */
    public void setInternalHost(String internalHost) {
        this.internalHost = internalHost;
    }

    public int getSubscribeMessageQueueSize() {
        return subscribeMessageQueueSize;
    }

    public int getSubscribeThreadPoolSize() {
        return subscribeThreadPoolSize;
    }

    public void setSubscribeMessageQueueSize(int subscribeMessageQueueSize) {
        if (subscribeMessageQueueSize < 1) {
            throw new IllegalArgumentException(TAG_SUBSCRIBE_MESSAGE_QUEUE_SIZE + MUST_BE_POSITIVE);
        }
        this.subscribeMessageQueueSize = subscribeMessageQueueSize;
    }

    public void setSubscribeThreadPoolSize(int subscribeThreadPoolSize) {
        if (subscribeThreadPoolSize < 1) {
            throw new IllegalArgumentException(TAG_SUBSCRIBE_THREAD_POOL_SIZE + MUST_BE_POSITIVE);
        }
        this.subscribeThreadPoolSize = subscribeThreadPoolSize;
    }

    public String getMqttServerImplementationClass() {
        return mqttServerImplementationClass;
    }

    public void setMqttServerImplementationClass(String mqttServerImplementationClass) {
        if (mqttServerImplementationClass == null || mqttServerImplementationClass.isEmpty()) {
            throw new IllegalArgumentException(TAG_IMPLEMENTATION_CLASS + " must be non-empty");
        }
        try {
            Class.forName(mqttServerImplementationClass, false, this.getClass().getClassLoader());
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException(TAG_IMPLEMENTATION_CLASS + " '" + mqttServerImplementationClass + "' could not be found", ex);
        }
        this.mqttServerImplementationClass = mqttServerImplementationClass;
    }

    public Settings getCustomSettings() {
        return customSettings;
    }

    public int getCreateMessageQueueSize() {
        return createMessageQueueSize;
    }

    public int getCreateThreadPoolSize() {
        return createThreadPoolSize;
    }

    public void setCreateMessageQueueSize(int createMessageQueueSize) {
        if (createMessageQueueSize < 1) {
            throw new IllegalArgumentException(TAG_CREATE_MESSAGE_QUEUE_SIZE + MUST_BE_POSITIVE);
        }
        this.createMessageQueueSize = createMessageQueueSize;
    }

    public void setCreateThreadPoolSize(int createThreadPoolSize) {
        if (createThreadPoolSize < 1) {
            throw new IllegalArgumentException(TAG_CREATE_THREAD_POOL_SIZE + MUST_BE_POSITIVE);
        }
        this.createThreadPoolSize = createThreadPoolSize;
    }

}
