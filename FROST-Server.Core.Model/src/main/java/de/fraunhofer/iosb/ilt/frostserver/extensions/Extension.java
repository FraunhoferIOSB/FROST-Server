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
package de.fraunhofer.iosb.ilt.frostserver.extensions;

/**
 * The list of hard-coded functionality.
 */
public enum Extension {
    CORE,
    ENTITY_LINKING,
    FILTERED_DELETES,
    MQTT,
    MQTT_EXPAND,
    MQTT_FILTER;

    /**
     * Flag indicating the server feature should be exposed on the index page.
     */
    private final boolean exposedFeature;

    private Extension() {
        this.exposedFeature = true;
    }

    public boolean isExposedFeature() {
        return exposedFeature;
    }

}
