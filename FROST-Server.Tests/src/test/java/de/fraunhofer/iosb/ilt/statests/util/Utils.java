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

import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.model.PkValue;
import de.fraunhofer.iosb.ilt.frostclient.utils.StringHelper;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import net.time4j.range.MomentInterval;
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

    public static final ObjectMapper MAPPER = new ObjectMapper();

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
     * @param value The id to quote.
     * @return The quoted id.
     */
    public static String quoteForJson(Object value) {
        if (value instanceof JsonNode jn) {
            if (jn.isNumber()) {
                return jn.asText();
            }
            return "\"" + jn.asText() + "\"";
        }
        if (value instanceof Number) {
            return value.toString();
        }
        return "\"" + value + "\"";
    }

    /**
     * Quote the ID for use in URLs, if needed.
     *
     * @param value The id to quote.
     * @return The quoted id.
     */
    public static String quoteForUrl(Object value) {
        if (value instanceof JsonNode jn) {
            if (jn.isNumber()) {
                return jn.asText();
            }
            return "'" + StringHelper.escapeForStringConstant(jn.asText()) + "'";
        }
        if (value instanceof Number) {
            return value.toString();
        }
        return "'" + StringHelper.escapeForStringConstant(Objects.toString(value)) + "'";
    }

    public static PkValue pkFromPostResult(String postResultLine) {
        int pos1 = postResultLine.lastIndexOf('(') + 1;
        int pos2 = postResultLine.lastIndexOf(')');
        String part = postResultLine.substring(pos1, pos2);
        try {
            return PkValue.of(Long.valueOf(part));
        } catch (NumberFormatException exc) {
            // Id was not a long, thus a String.
            if (!part.startsWith("'") || !part.endsWith("'")) {
                throw new IllegalArgumentException("Strings in urls must be quoted with single quotes.");
            }
            return PkValue.of(part.substring(1, part.length() - 1));
        }
    }

    public static List<Entity> getFromList(List<Entity> list, int... ids) {
        List<Entity> result = new ArrayList<>();
        for (int i : ids) {
            result.add(list.get(i));
        }
        return result;
    }

    public static List<Entity> getFromListExcept(List<Entity> list, int... ids) {
        List<Entity> result = new ArrayList<>(list);
        for (int i : ids) {
            result.remove(list.get(i));
        }
        return result;
    }

    public static List<Entity> removeFromList(List<Entity> sourceList, List<Entity> remaining, int... ids) {
        for (int i : ids) {
            remaining.remove(sourceList.get(i));
        }
        return remaining;
    }

    public static boolean jsonEquals(JsonNode expected, JsonNode given) {
        return jsonEquals(expected, given, false, null);
    }

    public static boolean jsonEquals(JsonNode expected, JsonNode given, String keyName) {
        return jsonEquals(expected, given, false, keyName);
    }

    public static boolean jsonEquals(JsonNode expected, JsonNode given, boolean ignoreExtra, String keyName) {
        if (given == null || expected == null) {
            return given == null && expected == null;
        }
        if (given instanceof NullNode || expected instanceof NullNode) {
            return expected.equals(given);
        }
        if (expected instanceof ObjectNode obj1 && given instanceof ObjectNode obj2) {
            return jsonEquals(obj1, obj2, ignoreExtra);
        }
        if (expected instanceof ArrayNode arr1 && given instanceof ArrayNode arr2) {
            return jsonEquals(arr1, arr2, ignoreExtra);
        }
        if (!StringHelper.isNullOrEmpty(keyName) && keyName.toLowerCase().endsWith("time")) {
            return checkTimeEquals(expected.textValue(), given.textValue());
        }
        return expected.equals(given);
    }

    public static boolean jsonEquals(ObjectNode expected, ObjectNode given, boolean ignoreExtra) {
        if (expected == null) {
            return given == null;
        }
        if (expected.equals(given)) {
            return true;
        }
        if (expected.getClass() != given.getClass()) {
            return false;
        }
        if (expected.size() != given.size() && !ignoreExtra) {
            return false;
        }
        Iterator iterator = expected.fieldNames();
        while (iterator.hasNext()) {
            String key = iterator.next().toString();
            if (!given.has(key)) {
                return false;
            }
            JsonNode val1 = expected.get(key);
            if (val1 instanceof ObjectNode) {
                if (!jsonEquals((ObjectNode) val1, (ObjectNode) given.get(key), ignoreExtra)) {
                    return false;
                }
            } else if (val1 instanceof ArrayNode) {
                ArrayNode arr1 = (ArrayNode) val1;
                ArrayNode arr2 = (ArrayNode) given.get(key);
                if (!jsonEquals(arr1, arr2, ignoreExtra)) {
                    return false;
                }
            }
            if (!jsonEquals(val1, given.get(key), ignoreExtra, key)) {
                return false;
            }
        }
        return true;
    }

    public static boolean jsonEquals(ArrayNode arr1, ArrayNode arr2, boolean ignoreExtra) {
        if (arr1.size() != arr2.size()) {
            return false;
        }
        for (int i = 0; i < arr1.size(); i++) {
            Object val1 = arr1.get(i);
            if (val1 instanceof ObjectNode) {
                if (!jsonEquals((ObjectNode) val1, (ObjectNode) arr2.get(i), ignoreExtra)) {
                    return false;
                }
            } else if (val1 instanceof ArrayNode) {
                if (!jsonEquals((ArrayNode) val1, (ArrayNode) arr2.get(i), ignoreExtra)) {
                    return false;
                }
            } else if (!val1.equals(arr2.get(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkTimeEquals(String expected, String given) {
        try {
            ZonedDateTime dateTime1 = ZonedDateTime.parse(expected);
            ZonedDateTime dateTime2 = ZonedDateTime.parse(given);
            return dateTime1.isEqual(dateTime2);
        } catch (Exception ex) {
            // do nothing
        }
        try {
            MomentInterval interval1 = MomentInterval.parseISO(expected);
            MomentInterval interval2 = MomentInterval.parseISO(given);
            return interval1.equals(interval2);
        } catch (Exception ex) {
            fail("time properies could neither be parsed as time nor as interval: " + expected + " and " + given);
        }
        return false;
    }

}
