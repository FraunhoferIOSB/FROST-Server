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

import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public class Settings {

    private static final Logger LOGGER = LoggerFactory.getLogger(Settings.class);
    private static final String NOT_SET_USING_DEFAULT_VALUE = "Not set {}{}, using default value {}.";
    private static final String ERROR_GETTING_SETTINGS_VALUE = "error getting settings value";

    private final Properties properties;
    private String prefix;

    private static Properties addEnvironment(Properties wrapped) {
        Map<String, String> environment = System.getenv();
        Properties wrapper = new Properties(wrapped);

        for (Map.Entry<String, String> entry : environment.entrySet()) {
            String key = entry.getKey().replaceAll("_", ".");
            LOGGER.debug("Added environment variable: {}", key);
            wrapper.setProperty(key, entry.getValue());
        }
        return wrapper;
    }

    /**
     * Creates a new settings, containing only environment variables.
     */
    public Settings() {
        this(new Properties(), "", true);
    }

    /**
     * Creates a new settings, containing only environment variables with the
     * given prefix.
     *
     * @param prefix The prefix to use. Only parameters with the given prefix
     * are accessed.
     */
    public Settings(String prefix) {
        this(new Properties(), prefix, true);
    }

    /**
     * Creates a new settings, containing the given properties, and environment
     * variables, with no prefix.
     *
     * @param properties The properties to use. These can be overridden by
     * environment variables.
     */
    public Settings(Properties properties) {
        this(properties, "", true);
    }

    /**
     * Creates a new settings, containing the given properties, and environment
     * variables, with the given prefix.
     *
     * @param properties The properties to use.
     * @param prefix The prefix to use.
     * @param wrapInEnvironment Flag indicating if environment variables can
     * override the given properties.
     */
    public Settings(Properties properties, String prefix, boolean wrapInEnvironment) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must be non-null");
        }
        if (wrapInEnvironment) {
            this.properties = addEnvironment(properties);
        } else {
            this.properties = properties;
        }
        this.prefix = (prefix == null ? "" : prefix);
    }

    /**
     * Get the prefix used in this Settings.
     *
     * @return The prefix used in this Settings.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Get the properties used in this Settings. This is the properties
     * configured when creating this Settings, optionally wrapped in a
     * properties containing all environment variables.
     *
     * @return The properties used in this Settings.
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Get the key as it is used in the config file or environment variables,
     * for the property with the given name. The key is the name, with the
     * prefix prepended to it.
     *
     * @param propertyName The name to get the key for.
     * @return prefix + propertyName
     */
    private String getPropertyKey(String propertyName) {
        return prefix + propertyName.replaceAll("_", ".");
    }

    /**
     * Check if there is a property with the given name. The prefix is prepended
     * to the name before lookup.
     *
     * @param name The name to look up.
     * @return True if there is a property with the given name.
     */
    public boolean containsName(String name) {
        // properties.containsKey ignores properties defaults
        String val = properties.getProperty(getPropertyKey(name));
        return val != null;
    }

    /**
     * Check if the given key is present. Throws a PropertyMissingException if
     * not.
     *
     * @param key The key to look up.
     */
    private void checkExists(String key) {
        if (properties.getProperty(key) != null) {
            return;
        }
        throw new PropertyMissingException(key);
    }

    public void set(String name, String value) {
        properties.put(getPropertyKey(name), value);
    }

    /**
     * Get the property with the given name, prefixed with the prefix of this
     * properties.
     *
     * @param name The name of the property to get. The prefix will be prepended
     * to this name.
     * @return The value of the requested property. Throws a
     * PropertyMissingException if the property is not found.
     */
    public String get(String name) {
        String key = getPropertyKey(name);
        checkExists(key);
        String value = properties.getProperty(key);
        LOGGER.info("Setting {}{} has value {}.", prefix, name, value);
        return value;
    }

    public String get(String name, String defaultValue) {
        String key = getPropertyKey(name);
        String value = properties.getProperty(key);
        if (value == null) {
            LOGGER.info(NOT_SET_USING_DEFAULT_VALUE, prefix, name, defaultValue);
            return defaultValue;
        }
        LOGGER.info("Setting {}{} has value {}.", prefix, name, value);
        return value;
    }

    public String get(String name, Class<? extends ConfigDefaults> defaultsProvider) {
        String key = getPropertyKey(name);
        String value = properties.getProperty(key);
        if (value == null) {
            String defaultValue = ConfigUtils.getDefaultValue(defaultsProvider, name);
            LOGGER.info(NOT_SET_USING_DEFAULT_VALUE, prefix, name, defaultValue);
            return defaultValue;
        }
        LOGGER.info("Setting {}{} has value {}.", prefix, name, value);
        return value;
    }

    public int getInt(String name) {
        try {
            return Integer.parseInt(get(name));
        } catch (NumberFormatException ex) {
            throw new PropertyTypeException(name, Integer.class, ex);
        }
    }

    public int getInt(String name, int defaultValue) {
        try {
            return getInt(name);
        } catch (Exception ex) {
            LOGGER.info(NOT_SET_USING_DEFAULT_VALUE, prefix, name, defaultValue);
            LOGGER.trace(ERROR_GETTING_SETTINGS_VALUE, ex);
            return defaultValue;
        }
    }

    public int getInt(String name, Class<? extends ConfigDefaults> defaultsProvider) {
        try {
            return getInt(name);
        } catch (Exception ex) {
            int defaultValue = ConfigUtils.getDefaultValueInt(defaultsProvider, name);
            LOGGER.info(NOT_SET_USING_DEFAULT_VALUE, prefix, name, defaultValue);
            LOGGER.trace(ERROR_GETTING_SETTINGS_VALUE, ex);
            return defaultValue;
        }
    }

    public long getLong(String name) {
        try {
            return Long.parseLong(get(name));
        } catch (NumberFormatException ex) {
            throw new PropertyTypeException(name, Long.class, ex);
        }
    }

    public long getLong(String name, long defaultValue) {
        try {
            return getLong(name);
        } catch (Exception ex) {
            LOGGER.info(NOT_SET_USING_DEFAULT_VALUE, prefix, name, defaultValue);
            LOGGER.trace(ERROR_GETTING_SETTINGS_VALUE, ex);
            return defaultValue;
        }
    }

    public long getLong(String name, Class<? extends ConfigDefaults> defaultsProvider) {
        try {
            return getLong(name);
        } catch (Exception ex) {
            int defaultValue = ConfigUtils.getDefaultValueInt(defaultsProvider, name);
            LOGGER.info(NOT_SET_USING_DEFAULT_VALUE, prefix, name, defaultValue);
            LOGGER.trace(ERROR_GETTING_SETTINGS_VALUE, ex);
            return defaultValue;
        }
    }

    public double getDouble(String name) {
        try {
            return Double.parseDouble(get(name));
        } catch (NumberFormatException ex) {
            throw new PropertyTypeException(name, Double.class, ex);
        }
    }

    public double getDouble(String name, double defaultValue) {
        try {
            return getDouble(name);
        } catch (Exception ex) {
            LOGGER.info(NOT_SET_USING_DEFAULT_VALUE, prefix, name, defaultValue);
            LOGGER.trace(ERROR_GETTING_SETTINGS_VALUE, ex);
            return defaultValue;
        }
    }

    public boolean getBoolean(String name) {
        try {
            return Boolean.valueOf(get(name));
        } catch (PropertyMissingException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new PropertyTypeException(name, Boolean.class, ex);
        }
    }

    public boolean getBoolean(String name, boolean defaultValue) {
        try {
            return getBoolean(name);
        } catch (Exception ex) {
            LOGGER.info(NOT_SET_USING_DEFAULT_VALUE, prefix, name, defaultValue);
            LOGGER.trace(ERROR_GETTING_SETTINGS_VALUE, ex);
            return defaultValue;
        }
    }

    public boolean getBoolean(String name, Class<? extends ConfigDefaults> defaultsProvider) {
        try {
            return getBoolean(name);
        } catch (Exception ex) {
            boolean defaultValue = ConfigUtils.getDefaultValueBoolean(defaultsProvider, name);
            LOGGER.info(NOT_SET_USING_DEFAULT_VALUE, prefix, name, defaultValue);
            LOGGER.trace(ERROR_GETTING_SETTINGS_VALUE, ex);
            return defaultValue;
        }
    }

}
