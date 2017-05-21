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
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
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

    private static enum CompareType {
        NUMBER,
        BOOLEAN,
        STRING
    }
    public static final String KEY_JSONB = "j";
    public static final String KEY_NUMBER = "n";
    public static final String KEY_STRING = "s";
    public static final String KEY_BOOLEAN = "b";
    private final Expression<?> jsonField;
    private final List<String> path = new ArrayList<>();

    public static class ListExpressionJson extends ListExpression {

        private final ComparableTemplate jsonExpression;

        public ListExpressionJson(Map<String, Expression<?>> expressions, Map<String, Expression<?>> expressionsForOrder, ComparableTemplate jsonExpression) {
            super(expressions, expressionsForOrder);
            this.jsonExpression = jsonExpression;
        }

        public ComparableTemplate getJsonExpression() {
            return jsonExpression;
        }

        public ComparableTemplate otherToJson(Expression<?> other) {
            return Expressions.comparableTemplate(String.class, "to_jsonb({0})", other);
        }

        public BooleanExpression eq(Expression<?> other) {
            CompareType type = getOtherType(other);
            switch (type) {
                case BOOLEAN:
                case NUMBER:
                    return jsonExpression.eq(otherToJson(other));
                case STRING:
                default:
                    return ((StringTemplate) getExpression(KEY_STRING)).eq(StringCastExpressionFactory.build(other));
            }
        }

        public BooleanExpression ne(Expression<?> other) {
            return jsonExpression.ne(otherToJson(other));
        }

        public BooleanExpression lt(Expression<?> other) {
            CompareType type = getOtherType(other);
            switch (type) {
                case BOOLEAN:
                case NUMBER:
                    return jsonExpression.lt(otherToJson(other)).and(createTypePredicate(type));
                case STRING:
                default:
                    return ((StringTemplate) getExpression(KEY_STRING)).lt(StringCastExpressionFactory.build(other));
            }
        }

        public BooleanExpression loe(Expression<?> other) {
            CompareType type = getOtherType(other);
            switch (type) {
                case BOOLEAN:
                case NUMBER:
                    return jsonExpression.loe(otherToJson(other)).and(createTypePredicate(type));
                case STRING:
                default:
                    return ((StringTemplate) getExpression(KEY_STRING)).loe(StringCastExpressionFactory.build(other));
            }
        }

        public BooleanExpression gt(Expression<?> other) {
            CompareType type = getOtherType(other);
            switch (type) {
                case BOOLEAN:
                case NUMBER:
                    return jsonExpression.gt(otherToJson(other)).and(createTypePredicate(type));
                case STRING:
                default:
                    return ((StringTemplate) getExpression(KEY_STRING)).gt(StringCastExpressionFactory.build(other));
            }
        }

        public BooleanExpression goe(Expression<?> other) {
            CompareType type = getOtherType(other);
            switch (type) {
                case BOOLEAN:
                case NUMBER:
                    return jsonExpression.goe(otherToJson(other)).and(createTypePredicate(type));
                case STRING:
                default:
                    return ((StringTemplate) getExpression(KEY_STRING)).goe(StringCastExpressionFactory.build(other));
            }
        }

        /**
         * The (json) type of the other expression.
         *
         * @param e
         * @return
         */
        private CompareType getOtherType(Expression<?> e) {
            if (e instanceof NumberExpression) {
                return CompareType.NUMBER;
            }
            if (e instanceof BooleanExpression) {
                return CompareType.BOOLEAN;
            }
            return CompareType.STRING;
        }

        /**
         * Find the common type that should be used to compare the given two
         * expressions.
         *
         * @param other The type of the other that we should enforce on the json
         * type.
         * @return the extra predicate to enforce the type with.
         */
        private Predicate createTypePredicate(CompareType other) {
            switch (other) {
                case NUMBER:
                    return Expressions.stringTemplate("jsonb_typeof({0})", jsonExpression).eq("number");
                case BOOLEAN:
                    return Expressions.stringTemplate("jsonb_typeof({0})", jsonExpression).eq("boolean");
                default:
                    return null;
            }
        }
    };

    public JsonExpressionFactory(Expression<?> jsonField) {
        this.jsonField = jsonField;
    }

    public JsonExpressionFactory addToPath(String key) {
        path.add(key);
        return this;
    }

    public Expression<?> build() {
        StringBuilder templateCore = new StringBuilder();
        boolean firstDone = false;
        for (String key : path) {
            if (firstDone) {
                templateCore.append(",");
            } else {
                firstDone = true;
            }
            templateCore.append(key);
        }
        String templateCoreString = templateCore.toString();
        String templateJsonb = "{0}::jsonb#>'{" + templateCoreString + "}'";
        String templateString = "{0}::jsonb#>>'{" + templateCoreString + "}'";
        String templateNumber = "safe_cast_to_numeric({0}::jsonb#>'{" + templateCoreString + "}')";
        String templateBoolean = "safe_cast_to_boolean({0}::jsonb#>'{" + templateCoreString + "}')";

        Map<String, Expression<?>> expressions = new HashMap<>();
        Map<String, Expression<?>> expressionsForOrder = new HashMap<>();
        StringTemplate stringTemplate = Expressions.stringTemplate(templateString, jsonField);
        // TODO: Review if this should change to the jsonb field.
        expressionsForOrder.put("s", stringTemplate);
        expressions.put(KEY_STRING, stringTemplate);
        expressions.put(KEY_NUMBER, Expressions.numberTemplate(Double.class, templateNumber, jsonField));
        expressions.put(KEY_BOOLEAN, Expressions.booleanTemplate(templateBoolean, jsonField));
        ComparableTemplate<String> jsonExpression = Expressions.comparableTemplate(String.class, templateJsonb, jsonField);
        expressions.put(KEY_JSONB, jsonExpression);

        ListExpression listExpression = new ListExpressionJson(expressions, expressionsForOrder, jsonExpression);

        return listExpression;
    }

}
