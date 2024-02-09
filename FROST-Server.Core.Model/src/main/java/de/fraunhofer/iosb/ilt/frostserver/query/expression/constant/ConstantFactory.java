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
        if (object == null) {
            return null;
        }
        if (object instanceof BigDecimal) {
            return new StringConstant(Objects.toString(object));
        }
        if (object instanceof Boolean aBoolean) {
            return new BooleanConstant(aBoolean);
        }
        if (object instanceof Double aDouble) {
            return new DoubleConstant(aDouble);
        }
        if (object instanceof Id id) {
            return new IdConstant(id);
        }
        if (object instanceof Integer integer) {
            return new IntegerConstant(integer);
        }
        if (object instanceof Long aLong) {
            return new IntegerConstant(aLong);
        }
        if (object instanceof String) {
            return new StringConstant(Objects.toString(object));
        }
        if (object instanceof TimeObject timeObject) {
            return new TimeObjectConstant(timeObject);
        }
        LOGGER.warn("Can not make constant of {}", object.getClass());
        return null;
    }

}
