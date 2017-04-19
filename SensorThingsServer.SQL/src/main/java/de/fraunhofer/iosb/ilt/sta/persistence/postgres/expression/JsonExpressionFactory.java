/*
 * Copyright (C) 2017 Fraunhofer IOSB.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.sta.persistence.postgres.expression;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringTemplate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Hylke van der Schaaf
 */
public class JsonExpressionFactory {

    private final Expression<?> jsonField;
    private final List<String> path = new ArrayList<>();

    public JsonExpressionFactory(Expression<?> jsonField) {
        this.jsonField = jsonField;
    }

    public JsonExpressionFactory addToPath(String key) {
        path.add(key);
        return this;
    }

    public Expression<?> build() {
        StringBuilder templateCore = new StringBuilder();
        templateCore.append("{0}::jsonb#>>'{");
        boolean firstDone = false;
        for (String key : path) {
            if (firstDone) {
                templateCore.append(",");
            } else {
                firstDone = true;
            }
            templateCore.append(key);
        }
        templateCore.append("}'");
        String templateNumber = "(" + templateCore + ")::numeric";
        String templateBoolean = "(" + templateCore + ")::boolean";

        Map<String, Expression<?>> expressions = new HashMap<>();
        Map<String, Expression<?>> expressionsForOrder = new HashMap<>();
        expressions.put("n", Expressions.numberTemplate(Double.class, templateNumber, jsonField));
        expressions.put("b", Expressions.booleanTemplate(templateBoolean, jsonField));
        StringTemplate stringTemplate = Expressions.stringTemplate(templateCore.toString(), jsonField);
        expressions.put("s", stringTemplate);
        expressionsForOrder.put("s", stringTemplate);

        ListExpression listExpression = new ListExpression(expressions, expressionsForOrder);

        return listExpression;
    }

}
