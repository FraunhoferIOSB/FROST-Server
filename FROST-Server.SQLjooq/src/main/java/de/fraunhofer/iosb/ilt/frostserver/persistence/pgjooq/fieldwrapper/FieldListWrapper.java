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

import java.util.Collection;
import java.util.Map;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;

/**
 * Some paths, like Observation.result and the time-interval paths, return two
 * column references. This class is just to encapsulate these cases.
 */
public class FieldListWrapper implements FieldWrapper {

    private final Map<String, Field> expressions;
    private final Map<String, Field> expressionsForOrder;
    /**
     * Flag to avoid double checking the fields for conditions.
     */
    private boolean conditionChecked = false;
    /**
     * The condition is lazily initialised by isCondition().
     */
    private Condition condition;

    public FieldListWrapper(Map<String, Field> expressions) {
        this.expressions = expressions;
        this.expressionsForOrder = expressions;
    }

    public FieldListWrapper(Map<String, Field> expressions, Map<String, Field> expressionsForOrder) {
        this.expressions = expressions;
        this.expressionsForOrder = expressionsForOrder;
    }

    @Override
    public boolean isCondition() {
        if (condition != null) {
            return true;
        }
        if (conditionChecked) {
            return false;
        }
        conditionChecked = true;
        for (Field expression : getExpressions().values()) {
            if (Boolean.class.isAssignableFrom(expression.getType())) {
                condition = DSL.condition(expression);
                return true;
            }
        }
        return false;
    }

    @Override
    public Condition getCondition() {
        if (!conditionChecked && condition == null) {
            isCondition();
        }
        return condition;
    }

    @Override
    public Field getDefaultField() {
        return expressions.values().iterator().next();
    }

    @Override
    public <T> Field<T> getFieldAsType(Class<T> expectedClazz, boolean canCast) {
        Collection<Field> values = expressions.values();
        // Two passes, first do an exact check (no casting allowed)
        for (Field subResult : values) {
            Class fieldType = subResult.getType();
            if (expectedClazz.isAssignableFrom(fieldType)) {
                return subResult;
            }
        }
        // No exact check. Now check again, but allow casting.
        for (Field subResult : values) {
            Class fieldType = subResult.getType();
            if (expectedClazz == String.class && Number.class.isAssignableFrom(fieldType)) {
                return subResult.cast(String.class);
            }
        }
        return null;
    }

    public final Map<String, Field> getExpressions() {
        return expressions;
    }

    public final Map<String, Field> getExpressionsForOrder() {
        return expressionsForOrder;
    }

    public Field getExpression(String name) {
        return expressions.get(name);
    }

    @Override
    public String toString() {
        return getClass().getName() + " Expressions: " + expressions.toString();
    }

}
