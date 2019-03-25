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

/**
 *
 * @author jab
 */
public class BusSettings implements ConfigDefaults {

    /**
     * Tags
     */
    @DefaultValue("de.fraunhofer.iosb.ilt.frostserver.messagebus.InternalMessageBus")
    public static final String TAG_IMPLEMENTATION_CLASS = "busImplementationClass";

    /**
     * Fully-qualified class name of the MqttServer implementation class
     */
    private String busImplementationClass;

    /**
     * Extension point for implementation specific settings
     */
    private Settings customSettings;

    public BusSettings(Settings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("settings most be non-null");
        }
        init(settings);
    }

    private void init(Settings settings) {
        busImplementationClass = settings.get(TAG_IMPLEMENTATION_CLASS, getClass());
        customSettings = settings;
    }

    public String getBusImplementationClass() {
        return busImplementationClass;
    }

    public void setBusImplementationClass(String busImplementationClass) {
        if (busImplementationClass == null || busImplementationClass.isEmpty()) {
            throw new IllegalArgumentException(TAG_IMPLEMENTATION_CLASS + " must be non-empty");
        }
        try {
            Class.forName(busImplementationClass, false, this.getClass().getClassLoader());
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException(TAG_IMPLEMENTATION_CLASS + " '" + busImplementationClass + "' could not be found", ex);
        }
        this.busImplementationClass = busImplementationClass;
    }

    public Settings getCustomSettings() {
        return customSettings;
    }

}
