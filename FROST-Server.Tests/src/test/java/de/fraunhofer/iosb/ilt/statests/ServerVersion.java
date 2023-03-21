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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.statests;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author scf
 */
public enum ServerVersion {
    v_1_0("v1.0"),
    v_1_1("v1.1");

    public final String urlPart;
    public static final Map<String, ServerVersion> urlMap = new HashMap<>();

    private ServerVersion(String urlPart) {
        this.urlPart = urlPart;
    }

    private static void init() {
        if (!urlMap.isEmpty()) {
            return;
        }
        for (ServerVersion version : ServerVersion.values()) {
            urlMap.put(version.urlPart, version);
        }
    }

    public static ServerVersion forString(String versionString) {
        init();
        return urlMap.get(versionString);
    }

}
