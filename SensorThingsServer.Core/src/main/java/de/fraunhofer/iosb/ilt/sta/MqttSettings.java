/*
 * Copyright (C) 2016 Fraunhofer IOSB
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.sta;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;

/**
 *
 * @author jab
 */
public class MqttSettings {

    private static final int MIN_PORT = 1025;
    private static final int MAX_PORT = 65535;
    private static final int MIN_QOS_LEVEL = 0;
    private static final int MAX_QOS_LEVEL = 2;
    private static final boolean DEFAULT_ENABLE_MQTT = true;
    private static final int DEFAULT_QOS_LEVEL = 2;
    private static final int DEFAULT_PORT = 1883;
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_WEBSOCKET_PORT = 9876;
    private static final int DEFAULT_MESSAGE_QUEUE_SIZE = 10;
    private static final int DEFAULT_THREAD_POOL_SIZE = 10;

    /**
     * Defines if MQTT should be enabled or not
     */
    private boolean enableMqtt = DEFAULT_ENABLE_MQTT;

    /**
     * The URL the server will be started
     */
    private String host = "";

    /**
     * The port used to run the MQTT server.
     */
    private int port = DEFAULT_PORT;

    /**
     * A prefix used for all topics. By default, this we be the versio number of
     * the service.
     */
    private String topicPrefix;

    /**
     * Quality of Service Level used to deliver MQTT messages
     */
    private int qosLevel = DEFAULT_QOS_LEVEL;

    /**
     * Fully qualified name of the class to instantiate the MQTT server from.
     * Must implement MqttServer interface.
     */
    private final String implementationClass;
    /**
     * Temp path for MQTT temp files.
     */
    private String tempPath;
    /**
     * Port to offer MQTT services as WebSocket.
     */
    private int websocketPort = DEFAULT_WEBSOCKET_PORT;
    /**
     * Queue size for messages passed between PersistenceManager and MqttManager
     */
    private int messageQueueSize = DEFAULT_MESSAGE_QUEUE_SIZE;
    /**
     * Number of threads used to process EntityChangeEvents
     */
    private int threadPoolSize = DEFAULT_THREAD_POOL_SIZE;

    public MqttSettings(String implementationClass) {
        this.implementationClass = implementationClass;
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

    public String getImplementationClass() {
        return implementationClass;
    }

    public void setEnableMqtt(boolean enableMqtt) {
        this.enableMqtt = enableMqtt;
    }

    public void setPort(int port) {
        if (websocketPort < MIN_PORT || websocketPort > MAX_PORT) {
            throw new IllegalArgumentException("Port must be between " + MIN_PORT + " and " + MAX_PORT);
        }
        this.port = port;
    }

    public void setQosLevel(int qosLevel) {
        if (qosLevel < MIN_QOS_LEVEL || qosLevel > MAX_QOS_LEVEL) {
            throw new IllegalArgumentException("QoS Level must be between " + MIN_QOS_LEVEL + " and " + MAX_QOS_LEVEL);
        }
        this.qosLevel = qosLevel;
    }

    public String getHost() {
        return host;
    }

    public String getTopicPrefix() {
        return topicPrefix;
    }

    public void setHost(String host) {
        if (host == null || host.isEmpty()) {
            throw new IllegalArgumentException("Host must be non-empty");
        }
        this.host = host;
    }

    public void setTopicPrefix(String topicPrefix) {
        this.topicPrefix = topicPrefix;
    }

    public String getTempPath() {
        return tempPath;
    }

    public void setTempPath(String tempPath) {
        if (tempPath == null || tempPath.isEmpty()) {
            throw new IllegalArgumentException("tempPath must be non-empty");
        }
        if (Files.notExists(Paths.get(tempPath), LinkOption.NOFOLLOW_LINKS)) {
            throw new IllegalArgumentException("tempPath '" + tempPath + "' does not exist");
        }
        this.tempPath = tempPath;
    }

    public int getWebsocketPort() {
        return websocketPort;
    }

    public void setWebsocketPort(int websocketPort) {
        if (websocketPort < MIN_PORT || websocketPort > MAX_PORT) {
            throw new IllegalArgumentException("Websocket port must be between " + MIN_PORT + " and " + MAX_PORT);
        }
        this.websocketPort = websocketPort;
    }

    public int getMessageQueueSize() {
        return messageQueueSize;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public void setMessageQueueSize(int messageQueueSize) {
        if (messageQueueSize < 1) {
            throw new IllegalArgumentException("MessageQueueSize must be > 0");
        }
        this.messageQueueSize = messageQueueSize;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        if (threadPoolSize < 1) {
            throw new IllegalArgumentException("ThreadPoolSize must be > 0");
        }
        this.threadPoolSize = threadPoolSize;
    }

}
