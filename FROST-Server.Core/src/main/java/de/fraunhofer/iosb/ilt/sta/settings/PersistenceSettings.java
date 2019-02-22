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

import de.fraunhofer.iosb.ilt.sta.settings.annotation.DefaultValue;
import de.fraunhofer.iosb.ilt.sta.settings.annotation.DefaultValueBoolean;
import de.fraunhofer.iosb.ilt.sta.settings.annotation.DefaultValueInt;

/**
 *
 * @author jab
 */
public class PersistenceSettings implements ConfigDefaults {

    /**
     * Tags
     */
    @DefaultValue("de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.PostgresPersistenceManagerLong")
    public static final String TAG_IMPLEMENTATION_CLASS = "persistenceManagerImplementationClass";
    @DefaultValueBoolean(true)
    public static final String TAG_ALWAYS_ORDERBY_ID = "alwaysOrderbyId";
    @DefaultValue("ServerGeneratedOnly")
    public static final String TAG_ID_GENERATION_MODE = "idGenerationMode";
    @DefaultValueBoolean(false)
    public static final String TAG_AUTO_UPDATE_DATABASE = "autoUpdateDatabase";
    @DefaultValueInt(200)
    public static final String TAG_SLOW_QUERY_THRESHOLD = "slowQueryThreshold";

    /**
     * Fully-qualified class name of the PersistenceManager implementation class
     */
    private String persistenceManagerImplementationClass;
    private boolean alwaysOrderbyId;
    private String idGenerationMode;
    private boolean autoUpdateDatabase;
    private int slowQueryThreshold;
    private boolean logSlowQueries;
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
        persistenceManagerImplementationClass = settings.get(TAG_IMPLEMENTATION_CLASS, getClass());
        alwaysOrderbyId = settings.getBoolean(TAG_ALWAYS_ORDERBY_ID, getClass());
        idGenerationMode = settings.get(TAG_ID_GENERATION_MODE, getClass());
        autoUpdateDatabase = settings.getBoolean(TAG_AUTO_UPDATE_DATABASE, getClass());
        slowQueryThreshold = settings.getInt(TAG_SLOW_QUERY_THRESHOLD, getClass());
        logSlowQueries = slowQueryThreshold > 0;
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

    public int getSlowQueryThreshold() {
        return slowQueryThreshold;
    }

    public boolean isLogSlowQueries() {
        return logSlowQueries;
    }

}
