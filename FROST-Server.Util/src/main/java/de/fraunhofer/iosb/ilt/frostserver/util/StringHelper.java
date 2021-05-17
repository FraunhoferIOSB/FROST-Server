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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public class StringHelper {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(StringHelper.class);
    private static final String UTF8_NOT_SUPPORTED = "UTF-8 not supported?";

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
        if (string == null) {
            return "null";
        }
        return string.replaceAll("[\\n\\r\\t]", "_nrt_");
    }

    /**
     * Null-Save replaces characters that might break logging output. Currently:
     * \n, \r and \t
     *
     * @param string The string to clean.
     * @return The cleaned string.
     */
    public static String cleanForLogging(Object object) {
        return cleanForLogging(Objects.toString(object));
    }

    /**
     * Replaces all ' in the string with ''.
     *
     * @param in The string to escape.
     * @return The escaped string.
     */
    public static String escapeForStringConstant(String in) {
        return in.replace("'", "''");
    }

    public static String urlEncode(String input) {
        try {
            return URLEncoder.encode(input, UTF8.name());
        } catch (UnsupportedEncodingException exc) {
            // Should never happen, UTF-8 is build in.
            LOGGER.error(UTF8_NOT_SUPPORTED);
            throw new IllegalStateException(UTF8_NOT_SUPPORTED, exc);
        }
    }

    /**
     * Decode the given input using UTF-8 as character set.
     *
     * @param input The input to urlDecode.
     * @return The decoded input.
     */
    public static String urlDecode(String input) {
        try {
            return URLDecoder.decode(input, UTF8.name());
        } catch (UnsupportedEncodingException exc) {
            // Should never happen, UTF-8 is build in.
            LOGGER.error(UTF8_NOT_SUPPORTED);
            throw new IllegalStateException(UTF8_NOT_SUPPORTED, exc);
        }
    }

    /**
     * Urlencodes the given string, optionally not encoding forward slashes.
     *
     * In urls, forward slashes before the "?" must never be urlEncoded.
     * Urlencoding of slashes could otherwise be used to obfuscate phising URLs.
     *
     * @param string The string to urlEncode.
     * @param notSlashes If true, forward slashes are not encoded.
     * @return The urlEncoded string.
     */
    public static String urlEncode(String string, boolean notSlashes) {
        if (notSlashes) {
            return urlEncodeNotSlashes(string);
        }
        return urlEncode(string);
    }

    /**
     * Urlencodes the given string, except for the forward slashes.
     *
     * @param string The string to urlEncode.
     * @return The urlEncoded string.
     */
    public static String urlEncodeNotSlashes(String string) {
        String[] split = string.split("/");
        for (int i = 0; i < split.length; i++) {
            split[i] = urlEncode(split[i]);
        }
        return String.join("/", split);
    }

}
