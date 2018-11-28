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
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility classes for the ConfigDefaults interface.
 *
 * @author Brian Miles, scf
 */
public class ConfigUtils {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigUtils.class);
    private static final String UNABLE_TO_ACCESS_FIELD_ON_OBJECT = "Unable to access field '{}' on object: {}.";

    private ConfigUtils() {
        // Utility class.
    }

    /**
     * Return a list of field names, of the given Object, that were annotated
     * with either {@link DefaultValue} or {@link DefaultValueInt}.
     *
     * @param target The class to get the config field names for.
     * @return The list of field names so annotated.
     */
    public static Set<String> getConfigTags(ConfigDefaults target) {
        Set<String> configTags = new HashSet<>();
        for (Field f : target.getClass().getFields()) {
            try {
                if (f.isAnnotationPresent(DefaultValue.class) || f.isAnnotationPresent(DefaultValueInt.class)) {
                    configTags.add(f.get(target).toString());
                }
            } catch (IllegalAccessException e) {
                LOGGER.warn(UNABLE_TO_ACCESS_FIELD_ON_OBJECT, f.getName(), target);
            }
        }
        return configTags;
    }

    /**
     * Return a mapping of config tag value and default value for any field, of
     * the given Object, annotated with either {@link DefaultValue} or
     * {@link DefaultValueInt}.
     *
     * @param target The class to get the config fields for.
     * @return Mapping of config tag value and default value
     */
    public static Map<String, String> getConfigDefaults(ConfigDefaults target) {
        Map<String, String> configDefaults = new HashMap<>();
        for (Field f : target.getClass().getFields()) {
            String defaultValue = null;
            if (f.isAnnotationPresent(DefaultValue.class)) {
                defaultValue = f.getAnnotation(DefaultValue.class).value();
            } else if (f.isAnnotationPresent(DefaultValueInt.class)) {
                defaultValue = Integer.toString(f.getAnnotation(DefaultValueInt.class).value());
            }
            try {
                if (defaultValue != null) {
                    String key = f.get(target).toString();
                    configDefaults.put(key, defaultValue);
                }
            } catch (IllegalAccessException exc) {
                LOGGER.warn(UNABLE_TO_ACCESS_FIELD_ON_OBJECT, f.getName(), target);
            }
        }
        return configDefaults;
    }

    /**
     * Return a mapping of config tag value and default value for any field, of
     * the given Object, annotated with {@link DefaultValueInt}.
     *
     * @param target The class to get the config fields for.
     * @return Mapping of config tag value and default value
     */
    public static Map<String, Integer> getConfigDefaultsInt(ConfigDefaults target) {
        Map<String, Integer> configDefaults = new HashMap<>();
        List<Field> fields = FieldUtils.getFieldsListWithAnnotation(target.getClass(), DefaultValueInt.class);
        for (Field f : fields) {
            try {
                configDefaults.put(
                        f.get(target).toString(),
                        f.getAnnotation(DefaultValueInt.class).value());
            } catch (IllegalAccessException exc) {
                LOGGER.warn(UNABLE_TO_ACCESS_FIELD_ON_OBJECT, f.getName(), target);
            }
        }
        return configDefaults;
    }
}
