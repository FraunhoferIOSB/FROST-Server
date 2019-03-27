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
package de.fraunhofer.iosb.ilt.frostserver.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author jab
 */
public class StringHelper {

    public static final Charset UTF8 = StandardCharsets.UTF_8;

    private StringHelper() {
        // Utility class, not to be instantiated.
    }

    public static String capitalize(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    public static String deCapitalize(String string) {
        return string.substring(0, 1).toLowerCase() + string.substring(1);
    }

    public static boolean isNullOrEmpty(String string) {
        return string == null || string.isEmpty();
    }

    /**
     * Replaces characters that might break logging output. Currently: \n, \r
     * and \t
     *
     * @param string The string to clean.
     * @return The cleaned string.
     */
    public static String cleanForLogging(String string) {
        return string.replaceAll("[\\n|\\r|\\t]", "_nrt_");
    }
}
