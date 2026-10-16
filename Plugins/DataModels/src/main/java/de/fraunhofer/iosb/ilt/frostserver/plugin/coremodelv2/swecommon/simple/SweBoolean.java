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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;

/**
 * Swe-Common Boolean.
 */
public class SweBoolean extends AbstractSimpleComponent<SweBoolean, Boolean> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SweBoolean.class.getName());

    public static final String SWE_NAME = "Boolean";

    @Override
    public boolean valueIsValid() {
        return true;
    }

    @Override
    public boolean validate(Object input) {
        if (input == null) {
            return isOptional() || isSecret();
        }
        if (input instanceof JsonNode j) {
            return validate(j);
        }
        if (input instanceof Boolean b) {
            return validate(b);
        }
        LOGGER.debug("Non-boolean value {} for Count.", input);
        return false;
    }

    @Override
    public boolean validate(JsonNode input) {
        if (input == null) {
            return isOptional() || isSecret();
        }
        if (!input.isBoolean()) {
            LOGGER.debug("Non-boolean value {} for Count.", input);
            return false;
        }
        return true;
    }

    @Override
    protected SweBoolean self() {
        return this;
    }

}
