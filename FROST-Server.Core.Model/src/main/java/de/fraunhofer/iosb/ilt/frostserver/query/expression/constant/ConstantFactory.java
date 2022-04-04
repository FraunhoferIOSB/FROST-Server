/*
 * Copyright (C) 2022 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.fraunhofer.iosb.ilt.frostserver.query.expression.constant;

import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeObject;
import java.math.BigDecimal;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
public class ConstantFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConstantFactory.class.getName());

    private ConstantFactory() {
        // Not for public construction.
    }

    public static Constant of(Object object) {
        if (object instanceof BigDecimal) {
            return new StringConstant(Objects.toString(object));
        }
        if (object instanceof Boolean) {
            return new BooleanConstant((Boolean) object);
        }
        if (object instanceof Double) {
            return new DoubleConstant((Double) object);
        }
        if (object instanceof Id) {
            return new IdConstant((Id) object);
        }
        if (object instanceof Integer) {
            return new IntegerConstant((Integer) object);
        }
        if (object instanceof Long) {
            return new IntegerConstant((Long) object);
        }
        if (object instanceof String) {
            return new StringConstant(Objects.toString(object));
        }
        if (object instanceof TimeObject) {
            return new TimeObjectConstant((TimeObject) object);
        }
        LOGGER.warn("Can not make constant of {}", object.getClass());
        return null;
    }

}
