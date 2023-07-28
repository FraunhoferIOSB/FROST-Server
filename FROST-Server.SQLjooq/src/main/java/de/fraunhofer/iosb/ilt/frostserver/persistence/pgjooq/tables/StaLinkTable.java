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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import java.util.ArrayList;
import java.util.List;
import org.jooq.Binding;
import org.jooq.DataType;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;

/**
 *
 * @author hylke
 * @param <T> The exact type of the implementing class.
 */
public abstract class StaLinkTable<T extends StaLinkTable<T>> extends TableImpl<Record> implements StaTable<T> {

    private List<CustomField> customFields;

    protected StaLinkTable(Name alias, StaLinkTable<T> aliased) {
        super(alias, null, aliased);
        if (aliased == null) {
            customFields = new ArrayList<>();
        } else {
            customFields = aliased.customFields;
        }
    }

    @Override
    public final int registerField(Name name, DataType type, Binding binding) {
        customFields.add(new CustomField(name, type, binding));
        TableField newField = createField(name, type, "", binding);
        return fieldsRow().indexOf(newField);
    }

    @Override
    public abstract StaLinkTable<T> as(Name as);

    @Override
    public final StaLinkTable<T> as(String alias) {
        return as(DSL.name(alias));
    }

    /**
     * Must be called directly after creating an alias of this table.
     *
     * @return this.
     */
    protected T initCustomFields() {
        for (CustomField customField : customFields) {
            createField(customField.name, customField.type, "", customField.binding);
        }
        return getThis();
    }

}
