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

import de.fraunhofer.iosb.ilt.frostserver.query.expression.Expression;
import java.util.ArrayList;
import java.util.List;

/**
 * A constant that is a list of constants.
 *
 * @param <T> The exact type of the implementing class.
 * @param <V> The type of the constants in the list.
 */
public class ConstantList<T extends Constant<T, V>, V> extends Constant<ConstantList<T, V>, List<Constant<T, V>>> {

    public static final String EXPR_NAME_CONSTANTLIST = "constantlist";

    public ConstantList() {
        super(EXPR_NAME_CONSTANTLIST, new ArrayList<>());
    }

    public ConstantList<T, V> addItem(Constant item) {
        getValue().add(item);
        return this;
    }

    public List<Object> getValueList() {
        return getValue().stream().map(c -> (Object) c.value).toList();
    }

    @Override
    public ConstantList<T, V> addParameter(Expression parameter) {
        if (parameter instanceof Constant c) {
            getValue().add(c);
        } else {
            throw new IllegalArgumentException("ConstantList should not contain " + parameter + "");
        }
        return this;
    }

    public boolean contains(Constant<T, V> search) {
        return getValue().contains(search);
    }

    @Override
    public String toUrl() {
        StringBuilder result = new StringBuilder();
        for (Constant<T, V> item : getValue()) {
            result.append(item.toUrl());
            result.append(',');
        }
        return '(' + result.substring(0, result.length() - 1) + ')';
    }
}
