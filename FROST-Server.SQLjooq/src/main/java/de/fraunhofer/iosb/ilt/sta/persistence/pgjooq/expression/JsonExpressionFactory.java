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
package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.expression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;

/**
 *
 * @author Hylke van der Schaaf
 */
public class JsonExpressionFactory {

    private enum CompareType {
        NUMBER,
        BOOLEAN,
        STRING
    }

    public static final String KEY_JSONB = "j";
    public static final String KEY_NUMBER = "n";
    public static final String KEY_STRING = "s";
    public static final String KEY_BOOLEAN = "b";
    private final Field<String> jsonField;
    private final List<String> path = new ArrayList<>();

    public static class ListExpressionJson extends ListExpression {

        private final Field<Object> jsonExpression;

        public ListExpressionJson(Map<String, Field> expressions, Map<String, Field> expressionsForOrder, Field<Object> jsonExpression) {
            super(expressions, expressionsForOrder);
            this.jsonExpression = jsonExpression;
        }

        public Field<Object> getJsonExpression() {
            return jsonExpression;
        }

        public Field<Object> otherToJson(FieldWrapper other) {
            return otherToJson(other.getDefaultField());
        }

        public Field<Object> otherToJson(Field<?> other) {
            return DSL.field("to_jsonb(?)", Object.class, other);
        }

        public FieldWrapper eq(FieldWrapper other) {
            CompareType type = getOtherType(other);
            switch (type) {
                case BOOLEAN:
                case NUMBER:
                    return new SimpleFieldWrapper(jsonExpression.eq(otherToJson(other)));
                case STRING:
                default:
                    return new SimpleFieldWrapper(getExpression(KEY_STRING).eq(StringCastExpressionFactory.build(other)));
            }
        }

        public FieldWrapper ne(FieldWrapper other) {
            return new SimpleFieldWrapper(jsonExpression.ne(otherToJson(other)));
        }

        public FieldWrapper lt(FieldWrapper other) {
            CompareType type = getOtherType(other);
            switch (type) {
                case BOOLEAN:
                case NUMBER:
                    return new SimpleFieldWrapper(
                            jsonExpression
                                    .lessThan(otherToJson(other))
                                    .and(createTypePredicate(type)));
                case STRING:
                default:
                    return new SimpleFieldWrapper(
                            getExpression(KEY_STRING)
                                    .lessThan(StringCastExpressionFactory.build(other)));
            }
        }

        public FieldWrapper loe(FieldWrapper other) {
            CompareType type = getOtherType(other);
            switch (type) {
                case BOOLEAN:
                case NUMBER:
                    return new SimpleFieldWrapper(
                            jsonExpression
                                    .lessOrEqual(otherToJson(other))
                                    .and(createTypePredicate(type)));
                case STRING:
                default:
                    return new SimpleFieldWrapper(
                            getExpression(KEY_STRING)
                                    .lessOrEqual(StringCastExpressionFactory.build(other)));
            }
        }

        public FieldWrapper gt(FieldWrapper other) {
            CompareType type = getOtherType(other);
            switch (type) {
                case BOOLEAN:
                case NUMBER:
                    return new SimpleFieldWrapper(
                            jsonExpression
                                    .gt(otherToJson(other))
                                    .and(createTypePredicate(type)));
                case STRING:
                default:
                    return new SimpleFieldWrapper(
                            getExpression(KEY_STRING)
                                    .gt(StringCastExpressionFactory.build(other)));
            }
        }

        public FieldWrapper goe(FieldWrapper other) {
            CompareType type = getOtherType(other);
            switch (type) {
                case BOOLEAN:
                case NUMBER:
                    return new SimpleFieldWrapper(
                            jsonExpression
                                    .greaterOrEqual(otherToJson(other))
                                    .and(createTypePredicate(type)));
                case STRING:
                default:
                    return new SimpleFieldWrapper(
                            getExpression(KEY_STRING)
                                    .greaterOrEqual(StringCastExpressionFactory.build(other)));
            }
        }

        private CompareType getOtherType(FieldWrapper other) {
            return getOtherType(other.getDefaultField());
        }

        /**
         * The (json) type of the other expression.
         *
         * @param other
         * @return
         */
        private CompareType getOtherType(Field<?> other) {
            if (Number.class.isAssignableFrom(other.getType())) {
                return CompareType.NUMBER;
            }
            if (Boolean.class.isAssignableFrom(other.getType())) {
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
        private Condition createTypePredicate(CompareType other) {
            switch (other) {
                case NUMBER:
                    return DSL.field("jsonb_typeof(?)", jsonExpression).eq("number");
                case BOOLEAN:
                    return DSL.field("jsonb_typeof(?)", jsonExpression).eq("boolean");
                default:
                    return null;
            }
        }
    }

    public JsonExpressionFactory(Field<String> jsonField) {
        this.jsonField = jsonField;
    }

    public JsonExpressionFactory(FieldWrapper fieldWrapper) {
        this.jsonField = fieldWrapper.getDefaultField();
    }

    public JsonExpressionFactory addToPath(String key) {
        path.add(key);
        return this;
    }

    public ListExpressionJson build() {
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
        String templateJsonb = "?::jsonb#>'{ " + templateCoreString + " }'";
        String templateString = "?::jsonb#>>'{ " + templateCoreString + " }'";
        String templateNumber = "safe_cast_to_numeric(?::jsonb#>'{ " + templateCoreString + " }')";
        String templateBoolean = "safe_cast_to_boolean(?::jsonb#>'{ " + templateCoreString + " }')";

        Map<String, Field> expressions = new HashMap<>();
        Map<String, Field> expressionsForOrder = new HashMap<>();
        Field<String> stringTemplate = DSL.field(templateString, String.class, jsonField);

        expressionsForOrder.put("s", stringTemplate);
        expressions.put(KEY_STRING, stringTemplate);
        expressions.put(KEY_NUMBER, DSL.field(templateNumber, Double.class, jsonField));
        expressions.put(KEY_BOOLEAN, DSL.field(templateBoolean, Boolean.class, jsonField));
        Field<Object> jsonExpression = DSL.field(templateJsonb, Object.class, jsonField);
        expressions.put(KEY_JSONB, jsonExpression);

        return new ListExpressionJson(expressions, expressionsForOrder, jsonExpression);
    }

}
