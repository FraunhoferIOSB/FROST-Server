/*
 * Copyright (C) 2018 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
 *
 * @author scf
 */
public enum Extension {
    CORE,
    ACTUATION("actuation"),
    MULTI_DATASTREAM("multiDatastream"),
    MQTT("mqtt");

    /**
     * Flag indicating the server feature should be exposed on the index page.
     */
    private final boolean exposedFeature;
    /**
     * The name of the feature to use on the index page.
     */
    private final String featureListName;

    private Extension() {
        this.exposedFeature = false;
        this.featureListName = null;
    }

    private Extension(String featureListName) {
        this.exposedFeature = true;
        this.featureListName = featureListName;
    }

    public boolean isExposedFeature() {
        return exposedFeature;
    }

    public String getFeatureListName() {
        return featureListName;
    }

}
