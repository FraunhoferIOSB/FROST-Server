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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.frostserver.settings;

import java.util.HashMap;
import java.util.Map;

/**
 * The versions that FROST supports.
 *
 * @author scf
 */
public enum Version {
    V_1_0("v1.0"),
    V_1_1("v1.1");

    private static final Map<String, Version> URL_MAP = new HashMap<>();

    public final String urlPart;

    private Version(String urlPart) {
        this.urlPart = urlPart;
    }

    private static void init() {
        if (!URL_MAP.isEmpty()) {
            return;
        }
        for (Version version : values()) {
            URL_MAP.put(version.urlPart, version);
        }
    }

    /**
     * Finds the Version instance that matches the given version String, or null
     * if the string does not match any version.
     *
     * @param versionString The String that appears in a url.
     * @return The Version that matches the given String.
     */
    public static Version forString(String versionString) {
        init();
        return URL_MAP.get(versionString);
    }
}
