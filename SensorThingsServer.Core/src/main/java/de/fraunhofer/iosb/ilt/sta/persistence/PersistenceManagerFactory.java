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

import de.fraunhofer.iosb.ilt.sta.settings.CoreSettings;
import javax.swing.event.EventListenerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public class PersistenceManagerFactory {

    private static final String ERROR_MSG = "Could not generate PersistenceManager instance: ";
    private static final EventListenerList entityChangeListeners = new EventListenerList();
    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceManagerFactory.class);
    private static PersistenceManagerFactory instance;

    public static synchronized void init(CoreSettings settings) {
        if (instance == null) {
            instance = new PersistenceManagerFactory(settings);
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
    private final CoreSettings settings;

    private PersistenceManagerFactory(CoreSettings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("settings must be non-null");
        }
        this.settings = settings;
        try {
            String pmiClsName = settings.getPersistenceSettings().getPersistenceManagerImplementationClass();
            if ("de.fraunhofer.iosb.ilt.sta.persistence.postgres.PostgresPersistenceManager".equals(pmiClsName)) {
                pmiClsName = "de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.PostgresPersistenceManagerLong";
                LOGGER.warn("The persistenceManager is renamed to {}", pmiClsName);
            }
            persistenceManagerClass = Class.forName(pmiClsName);
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException(ERROR_MSG + "Class '" + settings.getPersistenceSettings().getPersistenceManagerImplementationClass() + "' could not be found", ex);
        }
    }

    public PersistenceManager create() {
        PersistenceManager persistenceManager = null;
        try {
            persistenceManager = (PersistenceManager) persistenceManagerClass.newInstance();
            persistenceManager.init(settings);
            for (EntityChangeListener listener : entityChangeListeners.getListeners(EntityChangeListener.class)) {
                persistenceManager.addEntityChangeListener(listener);
            }
        } catch (InstantiationException | IllegalAccessException ex) {
            LOGGER.error(ERROR_MSG + "Class '" + settings.getPersistenceSettings().getPersistenceManagerImplementationClass() + "' could not be instantiated", ex);
        }
        return persistenceManager;
    }
}
