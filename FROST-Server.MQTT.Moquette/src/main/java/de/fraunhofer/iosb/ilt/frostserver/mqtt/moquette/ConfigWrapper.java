/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.fraunhofer.iosb.ilt.frostserver.mqtt.moquette;

import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import io.moquette.broker.config.FileResourceLoader;
import io.moquette.broker.config.IConfig;
import io.moquette.broker.config.IResourceLoader;

/**
 *
 * @author hylke
 */
public class ConfigWrapper extends IConfig {

    private final Settings wrappedSettings;

    public ConfigWrapper(Settings wrappedSettings) {
        this.wrappedSettings = wrappedSettings;
    }

    @Override
    public void setProperty(String name, String value) {
        wrappedSettings.set(name, value);
    }

    @Override
    public String getProperty(String name) {
        return wrappedSettings.get(name, (String) null);
    }

    @Override
    public String getProperty(String name, String defaultValue) {
        return wrappedSettings.get(name, defaultValue);
    }

    @Override
    public int intProp(String propertyName, int defaultValue) {
        return wrappedSettings.getInt(propertyName, defaultValue);
    }

    @Override
    public boolean boolProp(String propertyName, boolean defaultValue) {
        return wrappedSettings.getBoolean(propertyName, defaultValue);
    }

    @Override
    public IResourceLoader getResourceLoader() {
        return new FileResourceLoader();
    }

}
