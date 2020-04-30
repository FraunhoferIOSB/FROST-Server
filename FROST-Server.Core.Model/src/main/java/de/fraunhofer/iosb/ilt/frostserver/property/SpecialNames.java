/*
 * Copyright (C) 2020 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.property;

/**
 *
 * @author hylke
 */
public final class SpecialNames {

    public static final String IOT_COUNT = "iot.count";
    public static final String AT_IOT_COUNT = '@' + IOT_COUNT;

    public static final String IOT_ID = "iot.id";
    public static final String AT_IOT_ID = '@' + IOT_ID;

    public static final String IOT_NAVIGATION_LINK = "iot.navigationLink";
    public static final String AT_IOT_NAVIGATION_LINK = '@' + IOT_NAVIGATION_LINK;

    public static final String IOT_NEXT_LINK = "iot.nextLink";
    public static final String AT_IOT_NEXT_LINK = '@' + IOT_NEXT_LINK;

    public static final String IOT_SELF_LINK = "iot.selfLink";
    public static final String AT_IOT_SELF_LINK = '@' + IOT_SELF_LINK;

    private SpecialNames() {
        // Utility class.
    }

}
