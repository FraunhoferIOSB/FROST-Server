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
package de.fraunhofer.iosb.ilt.sta.settings;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author jab
 */
public class MqttSettings {

    /**
     * Tags
     */
    private static final String TAG_IMPLEMENTATION_CLASS = "mqttServerImplementationClass";
    private static final String TAG_ENABLED = "Enabled";
    private static final String TAG_QOS = "QoS";
    private static final String TAG_PORT = "Port";
    private static final String TAG_HOST = "Host";
    private static final String TAG_HOST_INTERNAL = "internalHost";
    private static final String TAG_SUBSCRIBE_MESSAGE_QUEUE_SIZE = "SubscribeMessageQueueSize";
    private static final String TAG_SUBSCRIBE_THREAD_POOL_SIZE = "SubscribeThreadPoolSize";
    private static final String TAG_CREATE_MESSAGE_QUEUE_SIZE = "CreateMessageQueueSize";
    private static final String TAG_CREATE_THREAD_POOL_SIZE = "CreateThreadPoolSize";

    /**
     * Default values
     */
    private static final boolean DEFAULT_ENABLE_MQTT = true;
    private static final int DEFAULT_QOS_LEVEL = 2;
    private static final int DEFAULT_PORT = 1883;
    private static final String DEFAULT_HOST = "0.0.0.0";
    private static final String DEFAULT_HOST_INTERNAL = "localhost";
    private static final int DEFAULT_SUBSCRIBE_MESSAGE_QUEUE_SIZE = 10;
    private static final int DEFAULT_SUBSCRIBE_THREAD_POOL_SIZE = 10;
    private static final int DEFAULT_CREATE_MESSAGE_QUEUE_SIZE = 10;
    private static final int DEFAULT_CREATE_THREAD_POOL_SIZE = 5;

    /**
     * Constraints
     */
    public static final int MIN_PORT = 1025;
    public static final int MAX_PORT = 65535;
    public static final int MIN_QOS_LEVEL = 0;
    public static final int MAX_QOS_LEVEL = 2;

    private static final List<String> ALL_PROPERTIES = Arrays.asList(
            TAG_ENABLED,
            TAG_HOST,
            TAG_IMPLEMENTATION_CLASS,
            TAG_SUBSCRIBE_MESSAGE_QUEUE_SIZE,
            TAG_CREATE_MESSAGE_QUEUE_SIZE,
            TAG_PORT,
            TAG_QOS,
            TAG_SUBSCRIBE_THREAD_POOL_SIZE,
            TAG_CREATE_THREAD_POOL_SIZE);

    /**
     * Fully-qualified class name of the MqttServer implementation class
     */
    private String mqttServerImplementationClass;

    /**
     * Defines if MQTT should be enabled or not
     */
    private boolean enableMqtt = DEFAULT_ENABLE_MQTT;

    /**
     * The external IP address or host name the MQTT server should listen on.
     * Set to 0.0.0.0 to listen on all interfaces.
     */
    private String host = DEFAULT_HOST;

    /**
     * The internal host name of the MQTT server.
     */
    private String internalHost = DEFAULT_HOST_INTERNAL;

    /**
     * The port used to run the MQTT server.
     */
    private int port = DEFAULT_PORT;

    /**
     * A prefix used for all topics. By default, this we be the version number
     * of the service.
     */
    private String topicPrefix;

    /**
     * Quality of Service Level used to deliver MQTT messages
     */
    private int qosLevel = DEFAULT_QOS_LEVEL;

    /**
     * Queue size for subscribe messages passed between PersistenceManager and
     * MqttManager
     */
    private int subscribeMessageQueueSize = DEFAULT_SUBSCRIBE_MESSAGE_QUEUE_SIZE;
    /**
     * Number of threads used to process EntityChangeEvents
     */
    private int subscribeThreadPoolSize = DEFAULT_SUBSCRIBE_THREAD_POOL_SIZE;
    /**
     * Queue size for create messages passed between PersistenceManager and
     * MqttManager
     */
    private int createMessageQueueSize = DEFAULT_CREATE_MESSAGE_QUEUE_SIZE;
    /**
     * Number of threads used to process ObservationCreateEvents
     */
    private int createThreadPoolSize = DEFAULT_CREATE_THREAD_POOL_SIZE;
    /**
     * Extension point for implementation specific settings
     */
    private Settings customSettings;

    public MqttSettings(String prefix, Settings settings) {
        if (prefix == null || prefix.isEmpty()) {
            throw new IllegalArgumentException("prfeix most be non-empty");
        }
        if (settings == null) {
            throw new IllegalArgumentException("settings most be non-null");
        }
        init(prefix, settings);
    }

    private void init(String prefix, Settings settings) {
        if (!settings.contains(TAG_IMPLEMENTATION_CLASS)) {
            throw new IllegalArgumentException(getClass().getName() + " must contain property '" + TAG_IMPLEMENTATION_CLASS + "'");
        }
        mqttServerImplementationClass = settings.getString(TAG_IMPLEMENTATION_CLASS);
        enableMqtt = settings.getWithDefault(TAG_ENABLED, DEFAULT_ENABLE_MQTT, Boolean.class);
        port = settings.getWithDefault(TAG_PORT, DEFAULT_PORT, Integer.class);
        setHost(settings.getWithDefault(TAG_HOST, DEFAULT_HOST, String.class));
        setInternalHost(settings.getWithDefault(TAG_HOST_INTERNAL, DEFAULT_HOST_INTERNAL, String.class));
        setSubscribeMessageQueueSize(settings.getWithDefault(TAG_SUBSCRIBE_MESSAGE_QUEUE_SIZE, DEFAULT_SUBSCRIBE_MESSAGE_QUEUE_SIZE, Integer.class));
        setSubscribeThreadPoolSize(settings.getWithDefault(TAG_SUBSCRIBE_THREAD_POOL_SIZE, DEFAULT_SUBSCRIBE_THREAD_POOL_SIZE, Integer.class));
        setCreateMessageQueueSize(settings.getWithDefault(TAG_CREATE_MESSAGE_QUEUE_SIZE, DEFAULT_CREATE_MESSAGE_QUEUE_SIZE, Integer.class));
        setCreateThreadPoolSize(settings.getWithDefault(TAG_CREATE_THREAD_POOL_SIZE, DEFAULT_CREATE_THREAD_POOL_SIZE, Integer.class));
        setQosLevel(settings.getWithDefault(TAG_QOS, DEFAULT_QOS_LEVEL, Integer.class));
        customSettings = settings.filter(x -> !ALL_PROPERTIES.contains(x.replaceFirst(prefix, "")));
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

    public String getTopicPrefix() {
        return topicPrefix;
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

    public void setTopicPrefix(String topicPrefix) {
        this.topicPrefix = topicPrefix;
    }

    public int getSubscribeMessageQueueSize() {
        return subscribeMessageQueueSize;
    }

    public int getSubscribeThreadPoolSize() {
        return subscribeThreadPoolSize;
    }

    public void setSubscribeMessageQueueSize(int subscribeMessageQueueSize) {
        if (subscribeMessageQueueSize < 1) {
            throw new IllegalArgumentException(TAG_SUBSCRIBE_MESSAGE_QUEUE_SIZE + " must be > 0");
        }
        this.subscribeMessageQueueSize = subscribeMessageQueueSize;
    }

    public void setSubscribeThreadPoolSize(int subscribeThreadPoolSize) {
        if (subscribeThreadPoolSize < 1) {
            throw new IllegalArgumentException(TAG_SUBSCRIBE_THREAD_POOL_SIZE + " must be > 0");
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
            throw new IllegalArgumentException(TAG_CREATE_MESSAGE_QUEUE_SIZE + " must be > 0");
        }
        this.createMessageQueueSize = createMessageQueueSize;
    }

    public void setCreateThreadPoolSize(int createThreadPoolSize) {
        if (createThreadPoolSize < 1) {
            throw new IllegalArgumentException(TAG_CREATE_THREAD_POOL_SIZE + " must be > 0");
        }
        this.createThreadPoolSize = createThreadPoolSize;
    }

}
