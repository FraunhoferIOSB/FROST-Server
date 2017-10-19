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

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class CoreSettings {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoreSettings.class);

    /**
     * Tags
     */
    public static final String TAG_API_VERSION = "ApiVersion";
    private static final String TAG_DEFAULT_COUNT = "defaultCount";
    private static final String TAG_DEFAULT_TOP = "defaultTop";
    private static final String TAG_MAX_TOP = "maxTop";
    private static final String TAG_MAX_DATASIZE = "maxDataSize";
    public static final String TAG_SERVICE_ROOT_URL = "serviceRootUrl";
    private static final String TAG_USE_ABSOLUTE_NAVIGATION_LINKS = "useAbsoluteNavigationLinks";
    private static final String TAG_TEMP_PATH = "tempPath";

    /**
     * Defaults
     */
    private static final String DEFAULT_API_VERSION = "v1.0";
    private static final int DEFAULT_MAX_TOP = 100;
    private static final long DEFAULT_MAX_DATASIZE = 25000000;
    private static final boolean DEFAULT_COUNT = true;
    private static final boolean DEFAULT_USE_ABSOLUTE_NAVIGATION_LINKS = true;

    /**
     * Prefixes
     */
    private static final String PREFIX_MQTT = "mqtt.";
    private static final String PREFIX_PERSISTENCE = "persistence.";

    /**
     * Root URL of the service to run
     */
    private String serviceRootUrl;

    /**
     * API Version
     */
    private String apiVersion = DEFAULT_API_VERSION;

    /**
     * Root URL of the service to run
     */
    private boolean useAbsoluteNavigationLinks = DEFAULT_USE_ABSOLUTE_NAVIGATION_LINKS;

    /**
     * The default top to use when no specific top is set.
     */
    private int topDefault = DEFAULT_MAX_TOP;
    /**
     * The maximum allowed top.
     */
    private int topMax = DEFAULT_MAX_TOP;
    /**
     * The maximum data size.
     */
    private long dataSizeMax = DEFAULT_MAX_DATASIZE;
    /**
     * The default count to use when no specific count is set.
     */
    private boolean countDefault = DEFAULT_COUNT;
    /**
     * Path to temp folder
     */
    private String tempPath;
    /**
     * The MQTT settings to use
     */
    private MqttSettings mqttSettings;
    /**
     * The Persistence settings to use
     */
    private PersistenceSettings persistenceSettings;

    /**
     * The default top to use when no specific top is set.
     *
     * @return the topDefault
     */
    public int getTopDefault() {
        return topDefault;
    }

    /**
     * The default top to use when no specific top is set.
     *
     * @param topDefault the topDefault to set
     */
    public void setTopDefault(int topDefault) {
        this.topDefault = topDefault;
    }

    /**
     * The maximum allowed top.
     *
     * @return the topMax
     */
    public int getTopMax() {
        return topMax;
    }

    /**
     * The maximum allowed top.
     *
     * @param topMax the topMax to set
     */
    public void setTopMax(int topMax) {
        this.topMax = topMax;
    }

    /**
     * The maximum allowed data size to return in a single query. This uses a
     * very coarse estimation of the size, using only the fields with an
     * unbounded size, such as Observation.result and Thing.properties.
     *
     * @return The maximum result data size.
     */
    public long getDataSizeMax() {
        return dataSizeMax;
    }

    /**
     * The maximum allowed data size to return in a single query. This uses a
     * very coarse estimation of the size, using only the fields with an
     * unbounded size, such as Observation.result and Thing.properties.
     *
     * @param dataSizeMax The maximum result data size.
     */
    public void setDataSizeMax(long dataSizeMax) {
        this.dataSizeMax = dataSizeMax;
    }

    /**
     * The default count to use when no specific count is set.
     *
     * @return the countDefault
     */
    public boolean isCountDefault() {
        return countDefault;
    }

    /**
     * The default count to use when no specific count is set.
     *
     * @param countDefault the countDefault to set
     */
    public void setCountDefault(boolean countDefault) {
        this.countDefault = countDefault;
    }

    public CoreSettings() {

    }

    public CoreSettings(Properties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must be non-null");
        }
        init(properties);
    }

    public CoreSettings(Properties properties, String serviceRootUrl, String tempPath) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must be non-null");
        }
        properties.setProperty(TAG_SERVICE_ROOT_URL, serviceRootUrl);
        properties.setProperty(TAG_TEMP_PATH, tempPath);
        init(properties);
    }

    private void init(Properties properties) {
        Settings settings = new Settings(properties);
        if (!settings.contains(TAG_SERVICE_ROOT_URL)) {
            throw new IllegalArgumentException(getClass().getName() + " must contain property '" + TAG_SERVICE_ROOT_URL + "'");
        }
        if (!settings.contains(TAG_TEMP_PATH)) {
            throw new IllegalArgumentException(getClass().getName() + " must contain property '" + TAG_TEMP_PATH + "'");
        }
        tempPath = settings.getString(TAG_TEMP_PATH);
        if (tempPath == null || tempPath.isEmpty()) {
            throw new IllegalArgumentException("tempPath must be non-empty");
        }
        if (Files.notExists(Paths.get(tempPath), LinkOption.NOFOLLOW_LINKS)) {
            throw new IllegalArgumentException("tempPath '" + tempPath + "' does not exist");
        }
        serviceRootUrl = settings.getString(TAG_SERVICE_ROOT_URL);
        apiVersion = settings.getWithDefault(TAG_API_VERSION, DEFAULT_API_VERSION, String.class);
        useAbsoluteNavigationLinks = settings.getWithDefault(TAG_USE_ABSOLUTE_NAVIGATION_LINKS, DEFAULT_USE_ABSOLUTE_NAVIGATION_LINKS, Boolean.class);
        countDefault = settings.getWithDefault(TAG_DEFAULT_COUNT, DEFAULT_COUNT, Boolean.class);
        topDefault = settings.getWithDefault(TAG_DEFAULT_TOP, DEFAULT_MAX_TOP, Integer.class);
        topMax = settings.getWithDefault(TAG_MAX_TOP, DEFAULT_MAX_TOP, Integer.class);
        dataSizeMax = settings.getWithDefault(TAG_MAX_DATASIZE, DEFAULT_MAX_DATASIZE, Long.class);
        mqttSettings = new MqttSettings(PREFIX_MQTT, settings.filter(PREFIX_MQTT));
        persistenceSettings = new PersistenceSettings(PREFIX_PERSISTENCE, settings.filter(PREFIX_PERSISTENCE));
        if (mqttSettings.getTopicPrefix() == null || mqttSettings.getTopicPrefix().isEmpty()) {
            mqttSettings.setTopicPrefix(apiVersion + "/");
        }
    }

    public static CoreSettings load(String file) {
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(Paths.get(file, (String) null))) {
            properties.load(reader);
        } catch (IOException ex) {
            LOGGER.error("error loading properties file, using defaults", ex);
        }
        return load(properties);
    }

    public static CoreSettings load(Properties properties) {
        return new CoreSettings(properties);
    }

    public MqttSettings getMqttSettings() {
        return mqttSettings;
    }

    public PersistenceSettings getPersistenceSettings() {
        return persistenceSettings;
    }

    public String getServiceRootUrl() {
        return serviceRootUrl;
    }

    public boolean isUseAbsoluteNavigationLinks() {
        return useAbsoluteNavigationLinks;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public String getTempPath() {
        return tempPath;
    }

}
