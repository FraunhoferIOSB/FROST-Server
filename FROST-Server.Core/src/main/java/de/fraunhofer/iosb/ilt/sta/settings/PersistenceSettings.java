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
package de.fraunhofer.iosb.ilt.sta.settings;

/**
 *
 * @author jab
 */
public class PersistenceSettings {

    /**
     * Tags
     */
    private static final String TAG_IMPLEMENTATION_CLASS = "persistenceManagerImplementationClass";
    private static final String DEFAULT_IMPLEMENTATION_CLASS = "de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.PostgresPersistenceManagerLong";
    private static final String TAG_ALWAYS_ORDERBY_ID = "alwaysOrderbyId";
    private static final String TAG_ID_GENERATION_MODE = "idGenerationMode";
    private static final String TAG_AUTO_UPDATE_DATABASE = "autoUpdateDatabase";
    private static final boolean DEFAULT_AUTO_UPDATE_DATABASE = false;

    /**
     * Fully-qualified class name of the PersistenceManager implementation class
     */
    private String persistenceManagerImplementationClass;
    private boolean alwaysOrderbyId = true;
    private String idGenerationMode = "ServerGeneratedOnly";
    private boolean autoUpdateDatabase;
    /**
     * Extension point for implementation specific settings
     */
    private Settings customSettings;

    public PersistenceSettings(Settings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("settings most be non-null");
        }
        init(settings);
    }

    private void init(Settings settings) {
        persistenceManagerImplementationClass = settings.get(TAG_IMPLEMENTATION_CLASS, DEFAULT_IMPLEMENTATION_CLASS);
        alwaysOrderbyId = settings.getBoolean(TAG_ALWAYS_ORDERBY_ID, alwaysOrderbyId);
        idGenerationMode = settings.get(TAG_ID_GENERATION_MODE, idGenerationMode);
        autoUpdateDatabase = settings.getBoolean(TAG_AUTO_UPDATE_DATABASE, DEFAULT_AUTO_UPDATE_DATABASE);
        customSettings = settings;
    }

    public String getPersistenceManagerImplementationClass() {
        return persistenceManagerImplementationClass;
    }

    public boolean getAlwaysOrderbyId() {
        return alwaysOrderbyId;
    }

    public boolean isAutoUpdateDatabase() {
        return autoUpdateDatabase;
    }

    public Settings getCustomSettings() {
        return customSettings;
    }

    public String getIdGenerationMode() {
        return idGenerationMode;
    }
}
