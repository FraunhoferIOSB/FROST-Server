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
 * Base class for numeric constants.
 *
 * @param <T> The exact type of the implementing class.
 * @param <V> The type of Number this constant encapsulates.
 */
public abstract class NumericConstant<T extends NumericConstant<T, V>, V extends Number> extends Constant<T, V> {

    protected NumericConstant(String name) {
        super(name);
    }

    protected NumericConstant(String name, V value) {
        super(name, value);
    }

    @Override
    public String toUrl() {
        return getValue().toString();
    }

}
