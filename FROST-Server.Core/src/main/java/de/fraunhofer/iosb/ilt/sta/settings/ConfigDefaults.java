/*
 * Copyright (C) 2018 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.sta.settings;

import de.fraunhofer.iosb.ilt.sta.settings.annotation.DefaultValue;
import de.fraunhofer.iosb.ilt.sta.settings.annotation.DefaultValueInt;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface defining default methods for working with classes with fields
 * annotated with {@link DefaultValue} or {@link DefaultValueInt}.
 */
public interface ConfigDefaults {

    public static final Logger LOGGER = LoggerFactory.getLogger(ConfigDefaults.class);

    /**
     * Returns the default value of a field annotated with either {@link DefaultValue}
     * or {@link DefaultValueInt}.
     * @param fieldValue The value of the annotated field
     * @return The default value of the annotated field, or empty string (e.g. "") if the field
     * was not so annotated.
     */
    default public String defaultValue(String fieldValue) {
        for (Field f : this.getClass().getFields()) {
            if (f.isAnnotationPresent(DefaultValue.class)) {
                try {
                    if (f.get(this).toString().equals(fieldValue)) {
                        return f.getAnnotation(DefaultValue.class).value();
                    }
                } catch (IllegalAccessException e) {
                    LOGGER.warn("Unable to access field '"
                            + f.getName() + "' on object: " + this);
                }
            } else if (f.isAnnotationPresent(DefaultValueInt.class)) {
                try {
                    if (f.get(this).toString().equals(fieldValue)) {
                        return Integer.toString(f.getAnnotation(DefaultValueInt.class).value());
                    }
                } catch (IllegalAccessException e) {
                    LOGGER.warn("Unable to access field '"
                            + f.getName() + "' on object: " + this);
                }
            }
        }
        return "";
    }

    /**
     * Returns the default value of a field annotated with {@link DefaultValueInt}.
     * @param fieldValue The value of the annotated field
     * @return The default value of the annotated field, or 0 if the field
     * was not so annotated.
     */
    default public int defaultValueInt(String fieldValue) {
        for (Field f : this.getClass().getFields()) {
            if (f.isAnnotationPresent(DefaultValueInt.class)) {
                try {
                    if (f.get(this).toString().equals(fieldValue)) {
                        return Integer.valueOf(f.getAnnotation(DefaultValueInt.class).value());
                    }
                } catch (IllegalAccessException e) {
                    LOGGER.warn("Unable to access field '"
                            + f.getName() + "' on object: " + this);
                }
            }
        }
        return 0;
    }

    /**
     * Return a list of field names that were annotated with either
     * {@link DefaultValue} or {@link DefaultValueInt}.
     *
     * @return The list of field names so annotated.
     */
    public default Set<String> configTags() {
        Set<String> configTags = new HashSet<>();
        for (Field f : this.getClass().getFields()) {
            try {
                if (f.isAnnotationPresent(DefaultValue.class) || f.isAnnotationPresent(DefaultValueInt.class)) {
                    configTags.add(f.get(this).toString());
                }
            } catch (IllegalAccessException e) {
                LOGGER.warn("Unable to access field '" +
                        f.getName() + "' on object: " + this);
            }
        }
        return configTags;
    }

    /**
     * Return a mapping of config tag value and default value for any field
     * annotated with either {@link DefaultValue} or {@link DefaultValueInt}.
     *
     * @return Mapping of config tag value and default value
     */
    public default Map<String, String> configDefaults() {
        Map<String, String> configDefaults = new HashMap<>();

        for (Field f : this.getClass().getFields()) {
            String defaultValue = null;
            if (f.isAnnotationPresent(DefaultValue.class)) {
                defaultValue = f.getAnnotation(DefaultValue.class).value();
            } else if (f.isAnnotationPresent(DefaultValueInt.class)) {
                defaultValue = Integer.toString(f.getAnnotation(DefaultValueInt.class).value());
            }
            try {
                if (defaultValue != null) {
                    String key = f.get(this).toString();
                    configDefaults.put(key, defaultValue);
                }
            } catch (IllegalAccessException e) {
                LOGGER.warn("Unable to access field '"
                        + f.getName() + "' on object: " + this);
            }
        }
        return configDefaults;
    }

    /**
     * Return a mapping of config tag value and default value for any
     * field annotated with {@link DefaultValueInt}.
     * @return Mapping of config tag value and default value
     */
    default public Map<String, Integer> configDefaultsInt() {
        Map<String, Integer> configDefaults = new HashMap<>();
        for (Field f : this.getClass().getFields()) {
            try {
                if (f.isAnnotationPresent(DefaultValueInt.class)) {
                    configDefaults.put(f.get(this).toString(),
                            Integer.valueOf(f.getAnnotation(DefaultValueInt.class).value()));
                }
            } catch (IllegalAccessException e) {
                LOGGER.warn("Unable to access field '" +
                        f.getName() + "' on object: " + this);
            }
        }
        return configDefaults;
    }
}
