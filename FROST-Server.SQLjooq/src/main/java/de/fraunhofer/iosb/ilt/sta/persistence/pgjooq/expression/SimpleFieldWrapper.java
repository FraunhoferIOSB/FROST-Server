/*
 * Copyright (C) 2019 Fraunhofer IOSB.
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

import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Hylke van der Schaaf
 */
public class SimpleFieldWrapper implements FieldWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleFieldWrapper.class.getName());

    private final Field field;
    private final Condition condition;

    public SimpleFieldWrapper(Field field) {
        this.field = field;
        this.condition = null;
    }

    public SimpleFieldWrapper(Condition condition) {
        this.field = DSL.field(condition);
        this.condition = condition;
    }

    @Override
    public boolean isCondition() {
        return condition != null;
    }

    @Override
    public Condition getCondition() {
        return condition;
    }

    @Override
    public Field getDefaultField() {
        return field;
    }

    @Override
    public <T> Field<T> getFieldAsType(Class<T> expectedClazz, boolean canCast) {
        Field field = getDefaultField();
        Class fieldType = field.getType();
        if (expectedClazz.isAssignableFrom(fieldType)) {
            return field;
        }
        if (canCast && expectedClazz == String.class && Number.class.isAssignableFrom(fieldType)) {
            return field.cast(String.class);
        }
        LOGGER.trace("Not a {}: {} ({} -- {})", expectedClazz.getName(), field, field.getClass().getName(), fieldType.getName());
        return null;
    }

    @Override
    public String toString() {
        return getClass().getName() + " Field: " + field.toString() + " Condition: " + condition.toString();
    }

}
