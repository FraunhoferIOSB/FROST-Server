/*
 * Copyright (C) 2024 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.persistence;

import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.PersistenceSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.LiquibaseUser;
import de.fraunhofer.iosb.ilt.frostserver.util.LiquibaseUtils;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab, scf
 */
public class PersistenceManagerFactory {

    private static final String ERROR_MSG = "Could not generate PersistenceManager instance: ";

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceManagerFactory.class);
    private static final Map<CoreSettings, PersistenceManagerFactory> INSTANCES = new HashMap<>();
    private boolean maybeUpdateDatabase = true;

    public static synchronized PersistenceManagerFactory init(CoreSettings coreSettings) {
        PersistenceManagerFactory instance = INSTANCES.computeIfAbsent(coreSettings, t -> {
            PersistenceManagerFactory newInstance = new PersistenceManagerFactory(coreSettings);
            PersistenceSettings persistenceSettings = coreSettings.getPersistenceSettings();
            newInstance.maybeUpdateDatabase = persistenceSettings.isAutoUpdateDatabase();
            return newInstance;
        });
        instance.maybeUpdateDatabase();
        return instance;
    }

    public static PersistenceManagerFactory getInstance(CoreSettings coreSettings) {
        PersistenceManagerFactory instance = INSTANCES.get(coreSettings);
        if (instance == null) {
            instance = init(coreSettings);
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
            persistenceManagerClass = Class.forName(pmiClsName);
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException(ERROR_MSG + "Class '" + settings.getPersistenceSettings().getPersistenceManagerImplementationClass() + "' could not be found", ex);
        }

    }

    public PersistenceManager create() {
        PersistenceManager persistenceManager = null;
        try {
            persistenceManager = (PersistenceManager) persistenceManagerClass.getDeclaredConstructor().newInstance();
            persistenceManager.init(settings);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException ex) {
            LOGGER.error("{} Class '{}' could not be instantiated", ERROR_MSG, settings.getPersistenceSettings().getPersistenceManagerImplementationClass(), ex);
        }
        return persistenceManager;
    }

    public void maybeUpdateDatabase() {
        if (maybeUpdateDatabase) {
            PersistenceManager pm = create();
            if (pm instanceof LiquibaseUser liquibaseUser) {
                LiquibaseUtils.maybeUpdateDatabase(LOGGER, liquibaseUser);
            }
            final Set<LiquibaseUser> liquibaseUsers = settings.getLiquibaseUsers();
            if (liquibaseUsers.isEmpty()) {
                return;
            }
            for (LiquibaseUser lbu : liquibaseUsers) {
                if (LiquibaseUtils.maybeUpdateDatabase(LOGGER, lbu)) {
                    // upgrade failed, but should be tried again later.
                    return;
                }
            }
            // all upgrades succeeded, or permanently failed. Don't try again.
            maybeUpdateDatabase = false;
        }
    }
}
