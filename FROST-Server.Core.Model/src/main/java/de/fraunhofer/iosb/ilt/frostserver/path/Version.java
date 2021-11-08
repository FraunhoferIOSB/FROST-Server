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
package de.fraunhofer.iosb.ilt.frostserver.path;

import java.util.HashMap;
import java.util.Map;

/**
 * The versions that FROST supports.
 *
 * @author scf
 */
public class Version {

    public static Version V_1_0 = new Version("v1.0");
    public static Version V_1_1 = new Version("v1.1");

    private static final Map<String, Version> URL_MAP = new HashMap<>();

    public final String urlPart;

    private Version(String urlPart) {
        this.urlPart = urlPart;
    }

    @Override
    public String toString() {
        return urlPart;
    }

}
