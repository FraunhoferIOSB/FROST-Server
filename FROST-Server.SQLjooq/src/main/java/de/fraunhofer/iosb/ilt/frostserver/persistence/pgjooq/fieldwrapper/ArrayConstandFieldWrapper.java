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

import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.ConstantList;
import java.util.List;
import org.jooq.Field;
import org.jooq.impl.DSL;

/**
 * A Wrapper for an array of constants.
 */
public class ArrayConstandFieldWrapper implements FieldWrapper {

    private final ConstantList list;

    public ArrayConstandFieldWrapper(ConstantList list) {
        this.list = list;
    }

    public List getValueList() {
        return list.getValueList();
    }

    @Override
    public Field getDefaultField() {
        return DSL.array(list.getValueList().toArray());
    }

    @Override
    public <T> Field<T> getFieldAsType(Class<T> expectedClazz, boolean canCast) {
        return null;
    }

}
