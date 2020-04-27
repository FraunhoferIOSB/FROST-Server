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
package de.fraunhofer.iosb.ilt.frostserver.persistence;

import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.PersistenceSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.LiquibaseUtils;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab, scf
 */
public class PersistenceManagerFactory {

    private static final String ERROR_MSG = "Could not generate PersistenceManager instance: ";

    private static final Map<String, String> RENAME = new HashMap<>();

    static {
        String longIdPm = "de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.imp.PostgresPersistenceManagerLong";
        String stringIdPm = "de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.imp.PostgresPersistenceManagerString";
        String uuidIdPm = "de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.imp.PostgresPersistenceManagerUuid";

        RENAME.put("de.fraunhofer.iosb.ilt.sta.persistence.postgres.PostgresPersistenceManager", longIdPm);
        RENAME.put("de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.PostgresPersistenceManagerLong", longIdPm);
        RENAME.put("de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid.PostgresPersistenceManagerString", stringIdPm);
        RENAME.put("de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid.PostgresPersistenceManagerUuid", uuidIdPm);
        RENAME.put("de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.imp.PostgresPersistenceManagerLong", longIdPm);
        RENAME.put("de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.imp.PostgresPersistenceManagerString", stringIdPm);
        RENAME.put("de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.imp.PostgresPersistenceManagerUuid", uuidIdPm);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceManagerFactory.class);
    private static PersistenceManagerFactory instance;
    private static boolean maybeUpdateDatabase = true;

    public static synchronized void init(CoreSettings coreSettings) {
        if (instance == null) {
            instance = new PersistenceManagerFactory(coreSettings);
            PersistenceSettings persistenceSettings = coreSettings.getPersistenceSettings();
            maybeUpdateDatabase = persistenceSettings.isAutoUpdateDatabase();
        }
        if (maybeUpdateDatabase) {
            try (PersistenceManager pm = instance.create()) {
                maybeUpdateDatabase = LiquibaseUtils.maybeUpdateDatabase(LOGGER, pm);
            }
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
    private IdManager idManager;

    private PersistenceManagerFactory(CoreSettings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("settings must be non-null");
        }
        this.settings = settings;
        try {
            String pmiClsName = settings.getPersistenceSettings().getPersistenceManagerImplementationClass();
            if (RENAME.containsKey(pmiClsName)) {
                String oldName = pmiClsName;
                pmiClsName = RENAME.get(pmiClsName);
                LOGGER.warn("Using persistenceManager {} instead of old name {}", pmiClsName, oldName);
            }
            persistenceManagerClass = Class.forName(pmiClsName);
            settings.addLiquibaseUser(persistenceManagerClass);
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

    /**
     * Get a shared IdManager instance. This convenience function avoids having
     * to create a persistenceManager when all that is needed is an IdManager.
     *
     * @return a shared instance of the IdManager of the persistanceManager
     * class.
     */
    public IdManager getIdManager() {
        if (idManager == null) {
            try (PersistenceManager pm = create()) {
                idManager = pm.getIdManager();
            }
        }
        return idManager;
    }

}
