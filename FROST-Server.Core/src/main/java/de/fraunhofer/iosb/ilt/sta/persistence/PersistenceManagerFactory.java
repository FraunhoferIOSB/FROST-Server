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
package de.fraunhofer.iosb.ilt.sta.persistence;

import de.fraunhofer.iosb.ilt.sta.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.sta.settings.PersistenceSettings;
import de.fraunhofer.iosb.ilt.sta.util.UpgradeFailedException;
import java.io.IOException;
import java.io.StringWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public class PersistenceManagerFactory {

    private static final String ERROR_MSG = "Could not generate PersistenceManager instance: ";

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceManagerFactory.class);
    private static PersistenceManagerFactory instance;
    private static boolean maybeUpdateDatabase = true;

    public static synchronized void init(CoreSettings settings) {
        if (instance == null) {
            instance = new PersistenceManagerFactory(settings);
            maybeUpdateDatabase(settings, instance);
        }
        if (maybeUpdateDatabase) {
            maybeUpdateDatabase(settings, instance);
        }
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
        } catch (InstantiationException | IllegalAccessException ex) {
            LOGGER.error(ERROR_MSG + "Class '" + settings.getPersistenceSettings().getPersistenceManagerImplementationClass() + "' could not be instantiated", ex);
        }
        return persistenceManager;
    }

    private static void maybeUpdateDatabase(CoreSettings coreSettings, PersistenceManagerFactory instance) {
        PersistenceSettings persistenceSettings = coreSettings.getPersistenceSettings();
        if (persistenceSettings.isAutoUpdateDatabase()) {
            StringWriter updateLog = new StringWriter();
            try {
                boolean success = instance.create().doUpgrades(updateLog);
                maybeUpdateDatabase = !success;
                if (success) {
                    LOGGER.info("Database-update successful.");
                } else {
                    LOGGER.info("Database-update not successful, trying again later.");
                }
            } catch (UpgradeFailedException ex) {
                LOGGER.error("Database upgrade failed.", ex);
                maybeUpdateDatabase = false;
            } catch (IOException ex) {
                // Should not happen, StringWriter does not throw IOExceptions.
                LOGGER.error("Database upgrade failed.", ex);
            }
            String logString = updateLog.toString();
            if (!logString.isEmpty()) {
                LOGGER.info("Database-update-log:\n{}", logString);
            }
        }
    }

}
