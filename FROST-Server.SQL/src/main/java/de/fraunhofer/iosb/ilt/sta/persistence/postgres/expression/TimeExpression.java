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
package de.fraunhofer.iosb.ilt.sta.persistence.postgres.expression;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;

/**
 *
 * @author scf
 */
public interface TimeExpression extends Expression {

    public default BooleanExpression eq(Expression<?> other) {
        return simpleOpBool("=", other);
    }

    public default BooleanExpression neq(Expression<?> other) {
        return eq(other).not();
    }

    public default BooleanExpression gt(Expression<?> other) {
        return simpleOpBool(">", other);
    }

    public default BooleanExpression goe(Expression<?> other) {
        return simpleOpBool(">=", other);
    }

    public default BooleanExpression lt(Expression<?> other) {
        return simpleOpBool("<", other);
    }

    public default BooleanExpression loe(Expression<?> other) {
        return simpleOpBool("<=", other);
    }

    public default BooleanExpression after(Expression<?> other) {
        return simpleOpBool("a", other);
    }

    public default BooleanExpression before(Expression<?> other) {
        return simpleOpBool("b", other);
    }

    public default BooleanExpression meets(Expression<?> other) {
        return simpleOpBool("m", other);
    }

    public default BooleanExpression contains(Expression<?> other) {
        return simpleOpBool("c", other);
    }

    public default BooleanExpression overlaps(Expression<?> other) {
        return simpleOpBool("o", other);
    }

    public default BooleanExpression starts(Expression<?> other) {
        return simpleOpBool("s", other);
    }

    public default BooleanExpression finishes(Expression<?> other) {
        return simpleOpBool("f", other);
    }

    public default Expression<?> add(Expression<?> other) {
        return simpleOp("+", other);
    }

    public default Expression<?> sub(Expression<?> other) {
        return simpleOp("-", other);
    }

    public default Expression<?> mul(Expression<?> other) {
        return simpleOp("*", other);
    }

    public default Expression<?> div(Expression<?> other) {
        return simpleOp("/", other);
    }

    public Expression<?> simpleOp(String op, Expression<?> other);

    public BooleanExpression simpleOpBool(String op, Expression<?> other);
}
