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
import java.util.function.Predicate;

/**
 *
 * @author jab
 */
public class Settings {

    private final Properties properties;
    private String prefix;

    public Settings(Properties properties, String prefix) {
        this(properties);
        this.prefix = (prefix == null ? "" : prefix);
    }

    public boolean contains(String name) {
        return properties.containsKey(getPropertyKey(name));
    }

    public Settings(Properties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("properties must be non-null");
        }
        this.prefix = "";
        this.properties = properties;
    }

    public Settings() {
        properties = new Properties();
        this.prefix = "";
    }

    public Settings(String prefix) {
        properties = new Properties();
        this.prefix = prefix;
    }

    public Settings filter(String prefix) {
        Settings result = filter(x -> x.startsWith(prefix));
        result.prefix = prefix;
        return result;
    }

    public Settings filter(Predicate<String> filter) {
        Settings result = new Settings(prefix);
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            if (filter.test(entry.getKey().toString())) {
                result.properties.setProperty(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        return result;
    }

    private String getPropertyKey(String propertyName) {
        return prefix + propertyName;
    }

    private void checkExists(String key) {
        if (!properties.containsKey(key)) {
            throw new PropertyMissingException(key);
        }
    }

    public void set(String name, String value) {
        properties.put(getPropertyKey(name), value);
    }

    public <T> T get(String name, Class<T> returnType) {
        if (returnType.equals(Integer.class)) {
            return returnType.cast(getInt(getPropertyKey(name)));
        } else if (returnType.equals(Double.class)) {
            return returnType.cast(getDouble(getPropertyKey(name)));
        } else if (returnType.equals(Boolean.class)) {
            return returnType.cast(getBoolean(getPropertyKey(name)));
        }
        return returnType.cast(getString(getPropertyKey(name)));
    }

    public <T> T getWithDefault(String name, T defaultValue, Class<T> returnType) {
        try {
            return get(name, returnType);
        } catch (Exception ex) {
            // nothing to do here
        }
        return defaultValue;
    }

    public String getString(String name) {
        String key = getPropertyKey(name);
        checkExists(key);
        return properties.get(key).toString();
    }

    public Object get(String name) {
        String key = getPropertyKey(name);
        checkExists(key);
        return properties.get(key);
    }

    public int getInt(String name) {
        String key = getPropertyKey(name);
        checkExists(key);
        try {
            return Integer.parseInt(properties.get(key).toString());
        } catch (NumberFormatException ex) {
            throw new PropertyTypeException(key, Integer.class, ex);
        }
    }

    public long getLong(String name) {
        String key = getPropertyKey(name);
        checkExists(key);
        try {
            return Long.parseLong(properties.get(key).toString());
        } catch (NumberFormatException ex) {
            throw new PropertyTypeException(key, Long.class, ex);
        }
    }

    public double getDouble(String name) {
        String key = getPropertyKey(name);
        checkExists(key);
        try {
            return Double.parseDouble(properties.get(key).toString());
        } catch (NumberFormatException ex) {
            throw new PropertyTypeException(key, Double.class, ex);
        }
    }

    public boolean getBoolean(String name) {
        String key = getPropertyKey(name);
        checkExists(key);
        try {
            return Boolean.valueOf(properties.get(key).toString());
        } catch (Exception ex) {
            throw new PropertyTypeException(key, Boolean.class, ex);
        }
    }

}
