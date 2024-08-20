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
package de.fraunhofer.iosb.ilt.statests.util;

/**
 * SensorThings common control information
 */
public class ControlInformation {

    /**
     * The system-generated identifier of an entity. It is unique among the
     * entities of the same entity type in a SensorThings service.
     */
    public static final String ID = "@iot.id";
    /**
     * The absolute URL of an entity that is unique among all other entities.
     */
    public static final String SELF_LINK = "@iot.selfLink";
    /**
     * The relative URL that retrieves content of related entities.
     */
    public static final String NAVIGATION_LINK = "@iot.navigationLink";
}
