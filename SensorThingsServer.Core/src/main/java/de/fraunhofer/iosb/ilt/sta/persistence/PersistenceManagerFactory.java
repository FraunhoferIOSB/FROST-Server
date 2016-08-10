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
package de.fraunhofer.iosb.ilt.sta.persistence;

import java.util.Properties;
import javax.swing.event.EventListenerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public class PersistenceManagerFactory {

    private static final String PERSISTENCE_CONFIG_KEY = "PersistenceImplementationClass";
    private static final String ERROR_MSG = "Could not generate PersistenceManager instance: ";
    private static final EventListenerList entityChangeListeners = new EventListenerList();
    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceManagerFactory.class);
    private static PersistenceManagerFactory instance;

    public static synchronized void init(Properties properties) {
        if (instance == null) {
            instance = new PersistenceManagerFactory(properties);
        }
    }

    public static void addEntityChangeListener(EntityChangeListener listener) {
        entityChangeListeners.add(EntityChangeListener.class, listener);
    }

    public static void removeEntityChangeListener(EntityChangeListener listener) {
        entityChangeListeners.remove(EntityChangeListener.class, listener);
    }

    public static PersistenceManagerFactory getInstance() {
        if (instance == null) {
            throw new IllegalStateException("PersistanceManagerFactory is not initialized! Call init() before accessing the instance.");
        }
        return instance;
    }
    private final Class persistenceManagerClass;
    private final Properties properties;

    private PersistenceManagerFactory(Properties properties) {
        if (properties == null || !properties.containsKey(PERSISTENCE_CONFIG_KEY)) {
            throw new IllegalArgumentException(ERROR_MSG + "properties are null or paramter '" + PERSISTENCE_CONFIG_KEY + "' is not set.");
        }
        this.properties = properties;
        String persistenceManagerClassName = properties.getProperty(PERSISTENCE_CONFIG_KEY);
        try {
            persistenceManagerClass = Class.forName(persistenceManagerClassName);
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException(ERROR_MSG + "Class '" + persistenceManagerClassName + "' could not be found", ex);
        }
    }

    public PersistenceManager create() {
        PersistenceManager persistenceManager = null;
        try {
            persistenceManager = (PersistenceManager) persistenceManagerClass.newInstance();
            persistenceManager.init(properties);
            for (EntityChangeListener listener : entityChangeListeners.getListeners(EntityChangeListener.class)) {
                persistenceManager.addEntityChangeListener(listener);
            }
        } catch (InstantiationException | IllegalAccessException ex) {
            LOGGER.error(ERROR_MSG + "Class '" + properties.getProperty(PERSISTENCE_CONFIG_KEY) + "' could not be instantiated", ex);
        }
        return persistenceManager;
    }
}
