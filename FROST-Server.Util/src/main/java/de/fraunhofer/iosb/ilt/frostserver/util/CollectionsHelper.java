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
package de.fraunhofer.iosb.ilt.frostserver.util;

import de.fraunhofer.iosb.ilt.frostserver.property.ComplexValue;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
public class CollectionsHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionsHelper.class);

    private CollectionsHelper() {
        // Utility class
    }

    public static void setOn(final Map<String, Object> map, final String path, final Object value) {
        setOn(map, Arrays.asList(StringUtils.split(path, '/')), value);
    }

    public static void setOn(final Map<String, Object> map, final List<String> path, final Object value) {
        setOn(map, path, 0, value);
    }

    public static void setOn(final Map<String, Object> map, final List<String> path, final int idx, final Object value) {
        final String key = path.get(idx);
        if (idx == path.size() - 1) {
            map.put(key, value);
            return;
        }
        Object subEntry = map.computeIfAbsent(key, t -> new HashMap<>());
        if (subEntry instanceof Map subMap) {
            setOn(subMap, path, idx + 1, value);
            return;
        }
        if (subEntry instanceof List) {
            // If it's alrady there, and not a map, then we did not create it,
            // and the item to add should already exist.
            return;
        }
        throw new IllegalArgumentException("Element at path index " + idx + " is not a map or list.");
    }

    public static Object getFrom(final List<Object> list, final List<String> path) {
        return getFrom((Object) list, path);
    }

    public static Object getFrom(final Map<String, Object> map, final List<String> path) {
        return getFrom((Object) map, path);
    }

    public static Object getFrom(final ComplexValue cv, final List<String> path) {
        return getFrom((Object) cv, path);
    }

    private static Object getFrom(final Object mapOrList, final List<String> path) {
        Object currentEntry = mapOrList;
        int last = path.size();
        for (int idx = 0; idx < last; idx++) {
            String key = path.get(idx);
            if (currentEntry instanceof Map map) {
                currentEntry = map.get(key);
            } else if (currentEntry instanceof ComplexValue cv) {
                currentEntry = cv.get(key);
            } else if (currentEntry instanceof List list) {
                try {
                    currentEntry = list.get(Integer.parseInt(key));
                } catch (NumberFormatException | IndexOutOfBoundsException ex) {
                    LOGGER.warn("Failed to get {} from {}.", key, currentEntry, ex);
                    return null;
                }
            }
        }
        return currentEntry;
    }

    public static PropertyBuilder propertiesBuilder() {
        return new PropertyBuilder();
    }

    public static class PropertyBuilder {

        Map<String, Object> properties = new HashMap<>();

        public PropertyBuilder addProperty(String key, Object value) {
            properties.put(key, value);
            return this;
        }

        public PropertyBuilder addPath(String path, Object value) {
            CollectionsHelper.setOn(properties, path, value);
            return this;
        }

        public Map<String, Object> build() {
            return properties;
        }
    }

}
