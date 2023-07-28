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
package de.fraunhofer.iosb.ilt.frostserver.messagebus;

import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class MessageBusFactory {

    private static final String ERROR_MSG = "Could not generate MessageBus instance: ";

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageBusFactory.class);

    private static final Map<String, String> RENAME = new HashMap<>();

    static {
        RENAME.put("de.fraunhofer.iosb.ilt.sta.messagebus.InternalMessageBus", InternalMessageBus.class.getName());
        RENAME.put("de.fraunhofer.iosb.ilt.sta.messagebus.MqttMessageBus", MqttMessageBus.class.getName());
    }

    public static synchronized void createMessageBus(CoreSettings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("settings must be non-null");
        }
        MessageBus instance = settings.getMessageBus();
        if (instance == null) {
            String mbClsName = settings.getBusSettings().getBusImplementationClass();
            try {

                if (RENAME.containsKey(mbClsName)) {
                    String oldName = mbClsName;
                    mbClsName = RENAME.get(mbClsName);
                    LOGGER.warn("Using MessageBus {} instead of old name {}", mbClsName, oldName);
                }

                Class<?> messageBusClass = Class.forName(mbClsName);
                instance = (MessageBus) messageBusClass.getDeclaredConstructor().newInstance();
                instance.init(settings);
                settings.setMessageBus(instance);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException ex) {
                throw new IllegalArgumentException(ERROR_MSG + "Class '" + mbClsName + "' could not be started", ex);
            }
        }
    }

    private MessageBusFactory() {
        // should not be instantiated.
    }

}
