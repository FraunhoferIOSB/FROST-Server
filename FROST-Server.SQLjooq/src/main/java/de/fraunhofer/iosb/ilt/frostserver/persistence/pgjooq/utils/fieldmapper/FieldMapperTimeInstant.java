/*
 * Copyright (C) 2021 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.fieldmapper;

import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTableDynamic;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import org.jooq.Name;
import org.jooq.Table;

/**
 *
 * @author hylke
 */
public class FieldMapperTimeInstant extends FieldMapperAbstract {

    private String field;

    private int fieldIdx;

    @Override
    public void registerField(PostgresPersistenceManager ppm, StaTableDynamic staTable, Property property) {
        final Name tableName = staTable.getQualifiedName();
        Table<?> dbTable = ppm.getDbTable(tableName);
        fieldIdx = getOrRegisterField(field, dbTable, staTable);
    }

    @Override
    public <J extends Comparable<J>> void registerMapping(PostgresPersistenceManager ppm, StaTableDynamic<J> table, Property property) {
        if (!(property instanceof EntityProperty)) {
            throw new IllegalArgumentException("Property must be an EntityProperty, got: " + property);
        }
        EntityProperty<TimeInstant> tvp = (EntityProperty<TimeInstant>) property;
        PropertyFieldRegistry<J, StaTableDynamic<J>> pfReg = table.getPropertyFieldRegistry();
        final int idx = fieldIdx;
        pfReg.addEntry(
                tvp,
                t -> t.field(idx),
                new PropertyFieldRegistry.ConverterTimeInstant<>(tvp, t -> t.field(idx)));
    }

    /**
     * @return the field
     */
    public String getField() {
        return field;
    }

    /**
     * @param field the field to set
     */
    public void setField(String field) {
        this.field = field;
    }

}
