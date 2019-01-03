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

import org.jooq.Field;

/**
 *
 * @author Hylke van der Schaaf
 */
public interface FieldWrapper {

    /**
     * Get the default Field contained in this wrapper. A wrapper may wrap
     * multiple fields with different types.
     *
     * @return the default Field of this wrapper.
     */
    public Field getDefaultField();

    public static Field fieldFromObject(Object in) {
        if (in instanceof Field) {
            return (Field) in;
        }
        if (in instanceof FieldWrapper) {
            FieldWrapper fieldWrapper = (FieldWrapper) in;
            return fieldWrapper.getDefaultField();
        }
        throw new IllegalArgumentException("Object is not a Field or FieldWrapper: " + in.getClass());
    }

    /**
     * Return a field of the requested type, optionally casting if possible, or
     * null if not.
     *
     * @param <T>
     * @param expectedClazz
     * @param canCast
     * @return
     */
    public <T> Field<T> checkType(Class<T> expectedClazz, boolean canCast);

}
