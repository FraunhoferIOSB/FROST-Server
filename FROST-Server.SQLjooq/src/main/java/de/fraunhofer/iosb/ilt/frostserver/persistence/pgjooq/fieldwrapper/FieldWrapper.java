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

import org.jooq.Condition;
import org.jooq.Field;

/**
 *
 * @author Hylke van der Schaaf
 */
public interface FieldWrapper {

    /**
     * Check if the field is a Condition.
     *
     * @return true if the Field is a Condition.
     */
    public default boolean isCondition() {
        return false;
    }

    /**
     * Get the Condition wrapped, or null if the wrapped item is not a
     * condition.
     *
     * @return the Condition wrapped, or null if the wrapped item is not a
     * condition.
     */
    public default Condition getCondition() {
        return null;
    }

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
     * @param <T> the Type of the Field to return.
     * @param expectedClazz The class implementing the type of the Field to
     * return.
     * @param canCast Flag indicating casting is allowed.
     * @return A field of the requested type, or null.
     */
    public <T> Field<T> getFieldAsType(Class<T> expectedClazz, boolean canCast);

}
