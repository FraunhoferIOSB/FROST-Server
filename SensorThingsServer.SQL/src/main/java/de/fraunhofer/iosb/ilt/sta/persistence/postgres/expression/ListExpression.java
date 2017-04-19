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
import com.querydsl.core.types.Visitor;
import java.util.Map;

/**
 * Some paths, like Observation.result and the time-interval paths, return two
 * column references. This class is just to encapsulate these cases. If this
 * Expression is used as a normal Expression, the first of the two will be used.
 */
public class ListExpression implements Expression {

    private final Map<String, Expression<?>> expressions;
    private final Map<String, Expression<?>> expressionsForOrder;

    public ListExpression(Map<String, Expression<?>> expressions) {
        this.expressions = expressions;
        this.expressionsForOrder = expressions;
    }

    public ListExpression(Map<String, Expression<?>> expressions, Map<String, Expression<?>> expressionsForOrder) {
        this.expressions = expressions;
        this.expressionsForOrder = expressionsForOrder;
    }

    public Map<String, Expression<?>> getExpressions() {
        return expressions;
    }

    public Map<String, Expression<?>> getExpressionsForOrder() {
        return expressionsForOrder;
    }

    @Override
    public Object accept(Visitor v, Object context) {
        return expressions.values().iterator().next().accept(v, context);
    }

    @Override
    public Class getType() {
        return expressions.values().iterator().next().getType();
    }

}
