/*
 * Copyright (C) 2016 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.sta.query;

import java.util.Objects;

import de.fraunhofer.iosb.ilt.sta.query.expression.Expression;

/**
 *
 * @author jab
 */
public class OrderBy {

    public static enum OrderType {

        Ascending("asc"),
        Descending("desc");
        public final String name;

        private OrderType(String name) {
            this.name = name;
        }

    }
    private final Expression expression;
    private final OrderType type;

    public OrderBy(Expression expression, OrderType type) {
        this.expression = expression;
        this.type = type;
    }

    public OrderBy(Expression expression) {
        this.expression = expression;
        this.type = OrderType.Ascending;
    }

    public Expression getExpression() {
        return expression;
    }

    public OrderType getType() {
        return type;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.expression);
        hash = 97 * hash + Objects.hashCode(this.type);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OrderBy other = (OrderBy) obj;
        if (!Objects.equals(this.expression, other.expression)) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return expression.toUrl() + " " + type.name;
    }

}
