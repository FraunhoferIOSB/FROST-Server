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
package de.fraunhofer.iosb.ilt.frostserver.plugin.coremodelv2.swecommon.simple;

import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;

/**
 * Abstract superclass for ranges.
 *
 * @param <T> The type of the extending class.
 * @param <V> The type of the Value field.
 */
public abstract class AbstractRange<T extends AbstractSimpleComponent<T, List<V>>, V> extends AbstractSimpleComponent<T, List<V>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRange.class.getName());

    @Override
    public final boolean validate(Object input) {
        if (input == null) {
            return isOptional() || isSecret();
        }
        if (input instanceof JsonNode j) {
            return validate(j);
        }
        if (input instanceof List list) {
            return validate(list);
        }
        if (input instanceof Object[] arr) {
            return validate(Arrays.asList(arr));
        }
        LOGGER.debug("Input not a list/array: {}", input);
        return false;
    }

    @Override
    public final boolean validate(JsonNode input) {
        if (input == null) {
            return isOptional() || isSecret();
        }
        if (input.isArray()) {
            return validateArray(input);
        }
        LOGGER.debug("Input not a list/array: {}", input);
        return false;
    }

    protected abstract boolean validateArray(JsonNode input);

    public abstract boolean validate(List<V> input);
}
