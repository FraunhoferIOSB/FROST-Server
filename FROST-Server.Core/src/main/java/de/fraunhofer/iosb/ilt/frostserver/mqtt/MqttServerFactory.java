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
package de.fraunhofer.iosb.ilt.frostserver.mqtt;

import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.MqttSettings;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public class MqttServerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttServerFactory.class);
    private static final String ERROR_MSG = "Could not generate MqttServer instance: ";

    private static final Map<String, String> RENAME = new HashMap<>();

    static {
        RENAME.put("de.fraunhofer.iosb.ilt.sensorthingsserver.mqtt.moquette.MoquetteMqttServer", "de.fraunhofer.iosb.ilt.frostserver.mqtt.moquette.MoquetteMqttServer");
    }

    private static MqttServerFactory instance;

    public static MqttServerFactory getInstance() {
        if (instance == null) {
            instance = new MqttServerFactory();
        }
        return instance;
    }

    private MqttServerFactory() {

    }

    /**
     * Get a new instance of the MQTT server that is defined in the given
     * coreSettings. The server will be initialised, but not started.
     *
     * @param settings The settings to use to find and initialise the MQTT
     * server.
     * @return An initialised MQTT server, or null if none is configured.
     */
    public MqttServer get(CoreSettings settings) {
        MqttServer mqttServer = get(settings.getMqttSettings());
        if (mqttServer != null) {
            mqttServer.init(settings);
        }
        return mqttServer;
    }

    /**
     * Get a new instance of the MQTT server that is defined in the given
     * coreSettings. The server will not be initialised.
     *
     * @param mqttSettings The mqttSettings to use to find and initialise the
     * MQTT server.
     * @return An MQTT server instance, or null if none is configured.
     */
    public MqttServer get(MqttSettings mqttSettings) {
        if (mqttSettings == null || !mqttSettings.isEnableMqtt()) {
            return null;
        }
        MqttServer mqttServer = null;
        try {
            String mqttServerClassName = mqttSettings.getMqttServerImplementationClass();

            if (RENAME.containsKey(mqttServerClassName)) {
                String oldName = mqttServerClassName;
                mqttServerClassName = RENAME.get(mqttServerClassName);
                LOGGER.warn("Using MqttServerClass {} instead of old name {}", mqttServerClassName, oldName);
            }

            Class<?> clazz = Class.forName(mqttServerClassName);
            if (!MqttServer.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException("MqttImplementationClass must implement interface '" + MqttServer.class.getName() + "'");
            }
            mqttServer = (MqttServer) clazz.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException ex) {
            LOGGER.error("{} Class '{}' could not be found", ERROR_MSG, mqttSettings.getMqttServerImplementationClass(), ex);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException ex) {
            LOGGER.error("{} Class '{}' could not be instantiated", ERROR_MSG, mqttSettings.getMqttServerImplementationClass(), ex);
        }
        return mqttServer;
    }

}
