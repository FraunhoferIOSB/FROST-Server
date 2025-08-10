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
package de.fraunhofer.iosb.ilt.frostserver.query.expression.constant;

/**
 * A constant integer.
 */
public class IntegerConstant extends NumericConstant<IntegerConstant, Long> {

    public static final String EXPR_NAME_INTCONSTANT = "intconstant";

    public IntegerConstant() {
        super(EXPR_NAME_INTCONSTANT);
    }

    public IntegerConstant(Integer value) {
        super(EXPR_NAME_INTCONSTANT, value.longValue());
    }

    public IntegerConstant(Long value) {
        super(EXPR_NAME_INTCONSTANT, value);
    }

    public IntegerConstant(String value) {
        super(EXPR_NAME_INTCONSTANT, Long.valueOf(value));
    }

    @Override
    public IntegerConstant getSelf() {
        return this;
    }
}
