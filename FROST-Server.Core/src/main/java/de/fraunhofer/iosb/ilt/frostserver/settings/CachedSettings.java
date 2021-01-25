/*
 * Copyright (C) 2019 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * A caching wrapper around a Settings instance.
 *
 * @author scf
 */
public class CachedSettings extends Settings {

    private final Map<String, String> valuesString = new HashMap<>();
    private final Map<String, Integer> valuesInt = new HashMap<>();
    private final Map<String, Long> valuesLong = new HashMap<>();
    private final Map<String, Boolean> valuesBoolean = new HashMap<>();
    private final Map<String, Double> valuesDouble = new HashMap<>();

    /**
     * Creates a new settings, containing only environment variables with the
     * given prefix.
     *
     * @param prefix The prefix to use. Only parameters with the given prefix
     * are accessed.
     */
    public CachedSettings(String prefix) {
        super(prefix);
    }

    /**
     * Creates a new settings, containing the given properties, and environment
     * variables, with no prefix.
     *
     * @param properties The properties to use. These can be overridden by
     * environment variables.
     */
    public CachedSettings(Properties properties) {
        super(properties);
    }

    /**
     * Creates a new settings, containing the given properties, and environment
     * variables, with the given prefix.
     *
     * @param properties The properties to use.
     * @param prefix The prefix to use.
     * @param wrapInEnvironment Flag indicating if environment variables can
     * override the given properties.
     * @param logSensitiveData Flag indicating things like passwords should be
     * logged completely, not hidden.
     */
    public CachedSettings(Properties properties, String prefix, boolean wrapInEnvironment, boolean logSensitiveData) {
        super(properties, prefix, wrapInEnvironment, logSensitiveData);
    }

    @Override
    public String get(String name) {
        if (valuesString.containsKey(name)) {
            return valuesString.get(name);
        }
        String value = super.get(name);
        valuesString.put(name, value);
        return value;
    }

    @Override
    public String get(String name, String defaultValue) {
        if (valuesString.containsKey(name)) {
            return valuesString.get(name);
        }
        String value = super.get(name, defaultValue);
        valuesString.put(name, value);
        return value;
    }

    @Override
    public String get(String name, Class<? extends ConfigDefaults> defaultsProvider) {
        if (valuesString.containsKey(name)) {
            return valuesString.get(name);
        }
        String value = super.get(name, defaultsProvider);
        valuesString.put(name, value);
        return value;
    }

    @Override
    public void set(String name, String value) {
        valuesString.put(name, value);
    }

    @Override
    public void set(String name, boolean value) {
        valuesBoolean.put(name, value);
    }

    @Override
    public boolean getBoolean(String name) {
        if (valuesBoolean.containsKey(name)) {
            return valuesBoolean.get(name);
        }
        boolean value = super.getBoolean(name);
        valuesBoolean.put(name, value);
        return value;
    }

    @Override
    public boolean getBoolean(String name, boolean defaultValue) {
        if (valuesBoolean.containsKey(name)) {
            return valuesBoolean.get(name);
        }
        boolean value = super.getBoolean(name, defaultValue);
        valuesBoolean.put(name, value);
        return value;
    }

    @Override
    public boolean getBoolean(String name, Class<? extends ConfigDefaults> defaultsProvider) {
        if (valuesBoolean.containsKey(name)) {
            return valuesBoolean.get(name);
        }
        boolean value = super.getBoolean(name, defaultsProvider);
        valuesBoolean.put(name, value);
        return value;
    }

    @Override
    public int getInt(String name) {
        if (valuesInt.containsKey(name)) {
            return valuesInt.get(name);
        }
        int value = super.getInt(name);
        valuesInt.put(name, value);
        return value;
    }

    @Override
    public int getInt(String name, int defaultValue) {
        if (valuesInt.containsKey(name)) {
            return valuesInt.get(name);
        }
        int value = super.getInt(name, defaultValue);
        valuesInt.put(name, value);
        return value;
    }

    @Override
    public int getInt(String name, Class<? extends ConfigDefaults> defaultsProvider) {
        if (valuesInt.containsKey(name)) {
            return valuesInt.get(name);
        }
        int value = super.getInt(name, defaultsProvider);
        valuesInt.put(name, value);
        return value;
    }

    @Override
    public long getLong(String name) {
        if (valuesLong.containsKey(name)) {
            return valuesLong.get(name);
        }
        long value = super.getLong(name);
        valuesLong.put(name, value);
        return value;
    }

    @Override
    public long getLong(String name, long defaultValue) {
        if (valuesLong.containsKey(name)) {
            return valuesLong.get(name);
        }
        long value = super.getLong(name, defaultValue);
        valuesLong.put(name, value);
        return value;
    }

    @Override
    public long getLong(String name, Class<? extends ConfigDefaults> defaultsProvider) {
        if (valuesLong.containsKey(name)) {
            return valuesLong.get(name);
        }
        long value = super.getLong(name, defaultsProvider);
        valuesLong.put(name, value);
        return value;
    }

    @Override
    public double getDouble(String name) {
        if (valuesDouble.containsKey(name)) {
            return valuesDouble.get(name);
        }
        double value = super.getDouble(name);
        valuesDouble.put(name, value);
        return value;
    }

    @Override
    public double getDouble(String name, double defaultValue) {
        if (valuesDouble.containsKey(name)) {
            return valuesDouble.get(name);
        }
        double value = super.getDouble(name, defaultValue);
        valuesDouble.put(name, value);
        return value;
    }

}
