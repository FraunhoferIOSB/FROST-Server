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
import de.fraunhofer.iosb.ilt.sta.settings.annotation.DefaultValueBoolean;
import de.fraunhofer.iosb.ilt.sta.settings.annotation.DefaultValueInt;
import java.util.Map;
import java.util.Set;

/**
 * Interface defining default methods for working with classes with fields
 * annotated with {@link DefaultValue} or {@link DefaultValueInt}.
 *
 * @author Brian Miles, scf
 */
public interface ConfigDefaults {

    /**
     * Returns the default value of a field annotated with either
     * {@link DefaultValue} or {@link DefaultValueInt}.
     *
     * @param fieldValue The value of the annotated field
     * @return The default value of the annotated field. If there is no such a
     * field, an IllegalArgumentException is thrown.
     */
    default String defaultValue(String fieldValue) {
        return ConfigUtils.getDefaultValue(this.getClass(), fieldValue);
    }

    /**
     * Returns the default value of a field annotated with
     * {@link DefaultValueInt}.
     *
     * @param fieldValue The value of the annotated field
     * @return The default value of the annotated field. If there is no such a
     * field, an IllegalArgumentException is thrown.
     */
    default int defaultValueInt(String fieldValue) {
        return ConfigUtils.getDefaultValueInt(this.getClass(), fieldValue);
    }

    /**
     * Returns the default value of a field annotated with
     * {@link DefaultValueBoolean}.
     *
     * @param fieldValue The value of the annotated field
     * @return The default value of the annotated field. If there is no such a
     * field, an IllegalArgumentException is thrown.
     */
    default boolean defaultValueBoolean(String fieldValue) {
        return ConfigUtils.getDefaultValueBoolean(this.getClass(), fieldValue);
    }

    /**
     * Return a list of field names that were annotated with either
     * {@link DefaultValue} or {@link DefaultValueInt}.
     *
     * @return The list of field names so annotated.
     */
    default Set<String> configTags() {
        return ConfigUtils.getConfigTags(this.getClass());
    }

    /**
     * Return a mapping of config tag value and default value for any field
     * annotated with either {@link DefaultValue} or {@link DefaultValueInt}.
     *
     * @return Mapping of config tag value and default value
     */
    default Map<String, String> configDefaults() {
        return ConfigUtils.getConfigDefaults(this.getClass());
    }

    /**
     * Return a mapping of config tag value and default value for any field
     * annotated with {@link DefaultValueInt}.
     *
     * @return Mapping of config tag value and default value
     */
    default Map<String, Integer> configDefaultsInt() {
        return ConfigUtils.getConfigDefaultsInt(this.getClass());
    }
}
