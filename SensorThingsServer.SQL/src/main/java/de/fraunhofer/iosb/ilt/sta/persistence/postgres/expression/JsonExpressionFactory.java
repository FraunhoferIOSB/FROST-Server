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
import java.util.List;

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

    public StringTemplate build() {
        StringBuilder template = new StringBuilder();
        template.append("{0}::jsonb#>>'{");
        boolean firstDone = false;
        for (String key : path) {
            if (firstDone) {
                template.append(",");
            } else {
                firstDone = true;
            }
            template.append(key);
        }
        template.append("}'");
        return Expressions.stringTemplate(template.toString(), jsonField);
    }

}
