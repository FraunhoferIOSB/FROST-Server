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
package de.fraunhofer.iosb.ilt.frostserver.settings;

/**
 * An abstract class that provides default configuration values, and access to
 * them.
 *
 * @param <T> The implementing class.
 */
public abstract class ConfigProvider<T> implements ConfigDefaults {

    private Settings settings;

    public ConfigProvider() {
    }

    public ConfigProvider(Settings settings) {
        this.settings = settings;
    }

    public T setSettings(Settings settings) {
        this.settings = settings;
        return getThis();
    }

    public Settings getSettings() {
        return settings;
    }

    public Settings getSubSettings(String prefix) {
        return settings.getSubSettings(prefix);
    }

    public String get(String name) {
        return settings.get(name, getClass());
    }

    public boolean getBoolean(String name) {
        return settings.getBoolean(name, getClass());
    }

    public int getInt(String name) {
        return settings.getInt(name, getClass());
    }

    public long getLong(String name) {
        return settings.getLong(name, getClass());
    }

    public double getDouble(String name) {
        return settings.getDouble(name, getClass());
    }

    public abstract T getThis();
}
