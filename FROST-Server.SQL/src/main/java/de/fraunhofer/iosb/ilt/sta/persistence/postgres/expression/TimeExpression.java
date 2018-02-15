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

    public BooleanExpression eq(Expression<?> other);

    public BooleanExpression neq(Expression<?> other);

    public BooleanExpression gt(Expression<?> other);

    public BooleanExpression goe(Expression<?> other);

    public BooleanExpression lt(Expression<?> other);

    public BooleanExpression loe(Expression<?> other);

    public Expression<?> add(Expression<?> other);

    public Expression<?> sub(Expression<?> other);

    /**
     * Inverse subtract (other - this)
     *
     * @param other the thing to subtract this from.
     * @return other - this.
     */
    public Expression<?> subi(Expression<?> other);

    public Expression<?> mul(Expression<?> other);

    public Expression<?> div(Expression<?> other);

}
