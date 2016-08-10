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

/**
 *
 * @author jab
 */
public class MqttSettings {

    public static final boolean DEFAULT_ENABLE_MQTT = true;
    public static final int DEFAULT_QOS_LEVEL = 2;
    public static final int DEFAULT_PORT = 1883;
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_WEBSOCKET_PORT = 9876;

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
        this.port = port;
    }

    public void setQosLevel(int qosLevel) {
        this.qosLevel = qosLevel;
    }

    public String getHost() {
        return host;
    }

    public String getTopicPrefix() {
        return topicPrefix;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setTopicPrefix(String topicPrefix) {
        this.topicPrefix = topicPrefix;
    }

    public String getTempPath() {
        return tempPath;
    }

    public void setTempPath(String tempPath) {
        this.tempPath = tempPath;
    }

    public int getWebsocketPort() {
        return websocketPort;
    }

    public void setWebsocketPort(int websocketPort) {
        this.websocketPort = websocketPort;
    }

}
