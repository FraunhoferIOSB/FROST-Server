/*
 * Copyright (C) 2024 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
import org.jooq.impl.DSL;

/**
 *
 * @author Hylke van der Schaaf
 */
public class NullWrapper implements FieldWrapper {

    @Override
    public boolean isCondition() {
        return false;
    }

    @Override
    public Condition getCondition() {
        return DSL.nullCondition();
    }

    @Override
    public Field getDefaultField() {
        return null;
    }

    @Override
    public <T> Field<T> getFieldAsType(Class<T> expectedClazz, boolean canCast) {

        return null;
    }

    @Override
    public String toString() {
        return getClass().getName();
    }

}
