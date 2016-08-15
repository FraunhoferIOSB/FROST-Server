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

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author jab
 */
public class PersistenceSettings {

    /**
     * Tags
     */
    private static final String TAG_IMPLEMENTATION_CLASS = "persistenceManagerImplementationClass";

    private static final List<String> ALL_PROPERTIES = Arrays.asList(
            TAG_IMPLEMENTATION_CLASS
    );

    /**
     * Fully-qualified class name of the PersistenceManager implementation class
     */
    private String persistenceManagerImplementationClass;
    /**
     * Extension point for implementation specific settings
     */
    private Settings customSettings;

    public PersistenceSettings(String prefix, Settings settings) {
        if (prefix == null || prefix.isEmpty()) {
            throw new IllegalArgumentException("settings most be non-empty");
        }
        if (settings == null) {
            throw new IllegalArgumentException("settings most be non-null");
        }
        init(prefix, settings);
    }

    private void init(String prefix, Settings settings) {
        if (!settings.contains(TAG_IMPLEMENTATION_CLASS)) {
            throw new IllegalArgumentException(getClass().getName() + " must contain property '" + TAG_IMPLEMENTATION_CLASS + "'");
        }
        persistenceManagerImplementationClass = settings.getString(TAG_IMPLEMENTATION_CLASS);
        customSettings = settings.filter(x -> !ALL_PROPERTIES.contains(x.replaceFirst(prefix, "")));
    }

    public String getPersistenceManagerImplementationClass() {
        return persistenceManagerImplementationClass;
    }

    public Settings getCustomSettings() {
        return customSettings;
    }
}
