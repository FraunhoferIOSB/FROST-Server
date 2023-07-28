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
package de.fraunhofer.iosb.ilt.statests.util;

import de.fraunhofer.iosb.ilt.sta.model.Entity;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public class Utils {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    private Utils() {
    }

    public static String urlEncode(String link) {
        try {
            return URLEncoder.encode(link, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException ex) {
        }
        return link;
    }

    /**
     * Quote the ID for use in json, if needed.
     *
     * @param id The id to quote.
     * @return The quoted id.
     */
    public static String quoteIdForJson(Object id) {
        if (id instanceof Number) {
            return id.toString();
        }
        return "\"" + id + "\"";
    }

    /**
     * Quote the ID for use in URLs, if needed.
     *
     * @param id The id to quote.
     * @return The quoted id.
     */
    public static String quoteIdForUrl(Object id) {
        if (id instanceof Number) {
            return id.toString();
        }
        return "'" + id + "'";
    }

    public static Object idObjectFromPostResult(String postResultLine) {
        int pos1 = postResultLine.lastIndexOf("(") + 1;
        int pos2 = postResultLine.lastIndexOf(")");
        String part = postResultLine.substring(pos1, pos2);
        try {
            return Long.parseLong(part);
        } catch (NumberFormatException exc) {
            // Id was not a long, thus a String.
            if (!part.startsWith("'") || !part.endsWith("'")) {
                throw new IllegalArgumentException("Strings in urls must be quoted with single quotes.");
            }
            return part.substring(1, part.length() - 1);
        }
    }

    public static <T extends Entity<T>> List<T> getFromList(List<T> list, int... ids) {
        List<T> result = new ArrayList<>();
        for (int i : ids) {
            result.add(list.get(i));
        }
        return result;
    }

    public static <T extends Entity<T>> List<T> getFromListExcept(List<T> list, int... ids) {
        List<T> result = new ArrayList<>(list);
        for (int i : ids) {
            result.remove(list.get(i));
        }
        return result;
    }

    public static <T extends Entity<T>> List<T> removeFromList(List<T> sourceList, List<T> remaining, int... ids) {
        for (int i : ids) {
            remaining.remove(sourceList.get(i));
        }
        return remaining;
    }

}
