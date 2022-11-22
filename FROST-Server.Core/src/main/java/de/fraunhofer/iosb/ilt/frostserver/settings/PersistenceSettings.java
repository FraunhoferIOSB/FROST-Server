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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public class PersistenceSettings implements ConfigDefaults {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceSettings.class.getName());

    public enum CountMode {
        FULL,
        LIMIT_SAMPLE,
        SAMPLE_LIMIT,
        LIMIT_ESTIMATE,
        ESTIMATE_LIMIT;

        public static CountMode fromValue(String value) {
            try {
                return CountMode.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException exc) {
                LOGGER.error("No CountMode named {}, should be one of {}", value, CountMode.values());
                return FULL;
            }
        }
    }

    /**
     * Tags
     */
    @DefaultValue("de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager")
    public static final String TAG_IMPLEMENTATION_CLASS = "persistenceManagerImplementationClass";
    @DefaultValue("ServerGeneratedOnly")
    public static final String TAG_ID_GENERATION_MODE = "idGenerationMode";
    @DefaultValueBoolean(false)
    public static final String TAG_AUTO_UPDATE_DATABASE = "autoUpdateDatabase";
    @DefaultValueInt(200)
    public static final String TAG_SLOW_QUERY_THRESHOLD = "slowQueryThreshold";
    @DefaultValueInt(0)
    public static final String TAG_QUERY_TIMEOUT = "queryTimeout";
    @DefaultValue("full")
    public static final String TAG_COUNT_MODE = "countMode";
    @DefaultValueInt(10_000)
    public static final String TAG_ESTIMATE_COUNT_THRESHOLD = "countEstimateThreshold";
    @DefaultValueBoolean(false)
    public static final String TAG_TRANSACTION_ROLE = "transactionRole";

    /**
     * Fully-qualified class name of the PersistenceManager implementation class
     */
    private String persistenceManagerImplementationClass;
    private String idGenerationMode;
    private boolean autoUpdateDatabase;
    private CountMode countMode;
    private int estimateCountThreshold;
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
     * Flag indicating role should be set in transaction from HTTP user, typically for Row-Level Security.
     */
    private boolean transactionRole;
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
        idGenerationMode = settings.get(TAG_ID_GENERATION_MODE, getClass());
        autoUpdateDatabase = settings.getBoolean(TAG_AUTO_UPDATE_DATABASE, getClass());
        slowQueryThreshold = settings.getInt(TAG_SLOW_QUERY_THRESHOLD, getClass());
        logSlowQueries = slowQueryThreshold > 0;
        queryTimeout = settings.getInt(TAG_QUERY_TIMEOUT, getClass());
        timeoutQueries = queryTimeout > 0;
        countMode = CountMode.fromValue(settings.get(TAG_COUNT_MODE, getClass()));
        estimateCountThreshold = settings.getInt(TAG_ESTIMATE_COUNT_THRESHOLD, getClass());
        transactionRole = settings.getBoolean(TAG_TRANSACTION_ROLE, getClass());
        customSettings = settings;
    }

    public String getPersistenceManagerImplementationClass() {
        return persistenceManagerImplementationClass;
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

    public boolean isTransactionRole() {
        return transactionRole;
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

    public CountMode getCountMode() {
        return countMode;
    }

    public int getEstimateCountThreshold() {
        return estimateCountThreshold;
    }

}
