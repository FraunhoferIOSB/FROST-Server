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
package de.fraunhofer.iosb.ilt.frostserver.settings;

import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValue;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValueBoolean;
import de.fraunhofer.iosb.ilt.frostserver.settings.annotation.DefaultValueInt;

/**
 *
 * @author jab
 */
public class PersistenceSettings implements ConfigDefaults {

    /**
     * Tags
     */
    @DefaultValue("de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.imp.PostgresPersistenceManagerLong")
    public static final String TAG_IMPLEMENTATION_CLASS = "persistenceManagerImplementationClass";
    @DefaultValueBoolean(false)
    public static final String TAG_ALWAYS_ORDERBY_ID = "alwaysOrderbyId";
    @DefaultValue("ServerGeneratedOnly")
    public static final String TAG_ID_GENERATION_MODE = "idGenerationMode";
    @DefaultValueBoolean(false)
    public static final String TAG_AUTO_UPDATE_DATABASE = "autoUpdateDatabase";
    @DefaultValueInt(200)
    public static final String TAG_SLOW_QUERY_THRESHOLD = "slowQueryThreshold";
    @DefaultValueInt(0)
    public static final String TAG_QUERY_TIMEOUT = "queryTimeout";

    /**
     * Fully-qualified class name of the PersistenceManager implementation class
     */
    private String persistenceManagerImplementationClass;
    private boolean alwaysOrderbyId;
    private String idGenerationMode;
    private boolean autoUpdateDatabase;
    /**
     * The threshold for queries to be logged as slow, in milliseconds.
     */
    private int slowQueryThreshold;
    /**
     * Flag indicating slow queries should be logged.
     */
    private boolean logSlowQueries;
    /**
     * The timeout for individual queries, in seconds.
     */
    private int queryTimeout;
    /**
     * Flag indicating a queryTimeout is set.
     */
    private boolean timeoutQueries;
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
        queryTimeout = settings.getInt(TAG_QUERY_TIMEOUT, getClass());
        timeoutQueries = queryTimeout > 0;
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

    /**
     * The threshold for queries to be logged as slow, in milliseconds.
     *
     * @return The threshold for queries to be logged as slow, in milliseconds.
     */
    public int getSlowQueryThreshold() {
        return slowQueryThreshold;
    }

    /**
     * Flag indicating slow queries should be logged.
     *
     * @return true if slow queries should be logged.
     */
    public boolean isLogSlowQueries() {
        return logSlowQueries;
    }

    /**
     * Get the timeout for individual queries, in seconds.
     *
     * @return The timeout for individual queries, in seconds.
     */
    public int getQueryTimeout() {
        return queryTimeout;
    }

    /**
     * Flag indicating a queryTimeout is set.
     *
     * @return true if a queryTimeout is set.
     */
    public boolean isTimeoutQueries() {
        return timeoutQueries;
    }

}
