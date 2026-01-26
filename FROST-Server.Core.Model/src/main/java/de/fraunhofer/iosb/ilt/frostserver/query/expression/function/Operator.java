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
package de.fraunhofer.iosb.ilt.frostserver.query.expression.function;

import de.fraunhofer.iosb.ilt.frostserver.query.expression.Expression;
import java.util.List;

/**
 * Operators are Functions that use infix notation.
 *
 * @param <T> The exact type of the implementing class.
 */
public abstract class Operator<T extends Operator<T>> extends Function<T> {

    public Operator(String functionName) {
        super(functionName);
    }

    @Override
    public String toUrl() {
        List<Expression<?>> parameters = getParameters();
        final int size = parameters.size();
        switch (size) {
            case 1:
                return "( " + getName() + " (" + parameters.get(0).toUrl() + "))";
            case 2:
                return "(" + parameters.get(0).toUrl() + " " + getName() + " " + parameters.get(1).toUrl() + ")";
            default:
                throw new IllegalArgumentException("Operator " + getName() + " should not have " + size + " parameters.");
        }
    }

}
