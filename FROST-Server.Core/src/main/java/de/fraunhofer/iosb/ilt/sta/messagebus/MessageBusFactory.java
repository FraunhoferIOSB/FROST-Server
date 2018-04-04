/*
 * Copyright (C) 2018 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.sta.messagebus;

import de.fraunhofer.iosb.ilt.sta.settings.CoreSettings;

/**
 *
 * @author scf
 */
public class MessageBusFactory {

    private static final String ERROR_MSG = "Could not generate MessageBus instance: ";
    private static Class messageBusClass;
    private static MessageBus instance;

    public static synchronized void init(CoreSettings settings) {
        if (instance == null) {
            if (settings == null) {
                throw new IllegalArgumentException("settings must be non-null");
            }
            try {
                String mbClsName = settings.getPersistenceSettings().getPersistenceManagerImplementationClass();
                messageBusClass = Class.forName(mbClsName);
                instance = (MessageBus) messageBusClass.newInstance();
                instance.init(settings);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                throw new IllegalArgumentException(ERROR_MSG + "Class '" + settings.getPersistenceSettings().getPersistenceManagerImplementationClass() + "' could not be found", ex);
            }
        }
    }

    public static MessageBus getMessageBus() {
        if (instance == null) {
            throw new IllegalStateException("MessageBusFactory is not initialized! Call init() before accessing the instance.");
        }
        return instance;
    }

    private MessageBusFactory() {
        // should not be instantiated.
    }

}
