/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper;

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
public class JsonFieldFactory {

    private enum CompareType {
        NUMBER,
        BOOLEAN,
        STRING,
        JSON
    }

    public static final String KEY_JSONB = "j";
    public static final String KEY_NUMBER = "n";
    public static final String KEY_STRING = "s";
    public static final String KEY_BOOLEAN = "b";

    public static class JsonFieldWrapper extends FieldListWrapper {

        private final Field<String> wrappedField;
        private final List<String> path = new ArrayList<>();
        private final Map<String, Field> expressions;
        private final Map<String, Field> expressionsForOrder;
        Field<Object> jsonExpression;

        public JsonFieldWrapper(Field<String> wrappedField) {
            super(new HashMap<>(), new HashMap<>());
            expressions = getExpressions();
            expressionsForOrder = getExpressionsForOrder();
            this.wrappedField = wrappedField;
        }

        public JsonFieldWrapper(FieldWrapper fieldWrapper) {
            this(fieldWrapper.getDefaultField());
        }

        public JsonFieldWrapper addToPath(String key) {
            path.add(key);
            return this;
        }

        public JsonFieldWrapper materialise() {
            if (jsonExpression != null) {
                return this;
            }
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

            expressions.put(KEY_STRING, DSL.field(templateString, String.class, wrappedField));
            expressions.put(KEY_NUMBER, DSL.field(templateNumber, Double.class, wrappedField));
            expressions.put(KEY_BOOLEAN, DSL.field(templateBoolean, Boolean.class, wrappedField));
            jsonExpression = DSL.field(templateJsonb, Object.class, wrappedField);
            expressions.put(KEY_JSONB, jsonExpression);
            expressionsForOrder.put(KEY_JSONB, jsonExpression);
            return this;
        }

        public Field<Object> getJsonExpression() {
            materialise();
            return jsonExpression;
        }

        @Override
        public Field getDefaultField() {
            materialise();
            return getExpression(KEY_STRING);
        }

        @Override
        public <T> Field<T> getFieldAsType(Class<T> expectedClazz, boolean canCast) {
            materialise();
            return super.getFieldAsType(expectedClazz, canCast);
        }

        public Field<Object> otherToJson(FieldWrapper other) {
            materialise();
            return otherToJson(other.getDefaultField());
        }

        public Field<Object> otherToJson(Field<?> other) {
            materialise();
            return DSL.field("to_jsonb(?)", Object.class, other);
        }

        public FieldWrapper eq(FieldWrapper other) {
            materialise();
            CompareType type = getOtherType(other);
            switch (type) {
                case BOOLEAN:
                case NUMBER:
                    return new SimpleFieldWrapper(jsonExpression.eq(otherToJson(other)));
                case STRING:
                default:
                    return new SimpleFieldWrapper(getExpression(KEY_STRING).eq(StringCastHelper.build(other)));
            }
        }

        public FieldWrapper ne(FieldWrapper other) {
            materialise();
            return new SimpleFieldWrapper(jsonExpression.ne(otherToJson(other)));
        }

        public FieldWrapper lt(FieldWrapper other) {
            materialise();
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
                                    .lessThan(StringCastHelper.build(other)));
            }
        }

        public FieldWrapper loe(FieldWrapper other) {
            materialise();
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
                                    .lessOrEqual(StringCastHelper.build(other)));
            }
        }

        public FieldWrapper gt(FieldWrapper other) {
            materialise();
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
                                    .gt(StringCastHelper.build(other)));
            }
        }

        public FieldWrapper goe(FieldWrapper other) {
            materialise();
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
                                    .greaterOrEqual(StringCastHelper.build(other)));
            }
        }

        public FieldWrapper contains(Field item) {
            String template = "? @> to_jsonb(?)";
            Condition result = DSL.condition(template, jsonExpression, item);
            return new SimpleFieldWrapper(result);
        }

        private CompareType getOtherType(FieldWrapper other) {
            materialise();
            if (other instanceof JsonFieldWrapper) {
                return CompareType.JSON;
            }
            return getOtherType(other.getDefaultField());
        }

        /**
         * The (json) type of the other expression.
         *
         * @param other
         * @return
         */
        private CompareType getOtherType(Field<?> other) {
            materialise();
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
            materialise();
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

}
