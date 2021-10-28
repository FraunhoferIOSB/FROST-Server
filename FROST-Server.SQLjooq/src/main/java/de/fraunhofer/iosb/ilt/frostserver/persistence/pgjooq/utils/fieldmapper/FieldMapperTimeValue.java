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

import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostserver.model.loader.DefEntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.StaTimeIntervalWrapper.KEY_TIME_INTERVAL_END;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.StaTimeIntervalWrapper.KEY_TIME_INTERVAL_START;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
import org.jooq.Name;
import org.jooq.Table;

/**
 *
 * @author hylke
 */
public class FieldMapperTimeValue extends FieldMapperAbstract {

    private String fieldStart;
    private String fieldEnd;

    private int fieldStartIdx;
    private int fieldEndIdx;

    private DefEntityProperty parent;

    @Override
    public void setParent(DefEntityProperty parent) {
        this.parent = parent;
    }

    @Override
    public void registerField(PostgresPersistenceManager ppm, StaMainTable staTable) {
        // find the actual field
        final Name tableName = staTable.getQualifiedName();
        final Table<?> dbTable = ppm.getDbTable(tableName);
        fieldStartIdx = getOrRegisterField(fieldStart, dbTable, staTable);
        fieldEndIdx = getOrRegisterField(fieldEnd, dbTable, staTable);
    }

    @Override
    public <T extends StaMainTable<T>> void registerMapping(PostgresPersistenceManager ppm, T table) {
        final EntityProperty<TimeValue> property = parent.getEntityProperty();
        final PropertyFieldRegistry<T> pfReg = table.getPropertyFieldRegistry();
        final int idxStart = fieldStartIdx;
        final int idxEnd = fieldEndIdx;
        pfReg.addEntry(property,
                new PropertyFieldRegistry.ConverterTimeValue<>(property, t -> t.field(idxStart), t -> t.field(idxEnd)),
                new PropertyFieldRegistry.NFP<>(KEY_TIME_INTERVAL_START, t -> t.field(idxStart)),
                new PropertyFieldRegistry.NFP<>(KEY_TIME_INTERVAL_END, t -> t.field(idxEnd)));
    }

    /**
     * @return the fieldStart
     */
    public String getFieldStart() {
        return fieldStart;
    }

    /**
     * @param fieldStart the fieldStart to set
     */
    public void setFieldStart(String fieldStart) {
        this.fieldStart = fieldStart;
    }

    /**
     * @return the fieldEnd
     */
    public String getFieldEnd() {
        return fieldEnd;
    }

    /**
     * @param fieldEnd the fieldEnd to set
     */
    public void setFieldEnd(String fieldEnd) {
        this.fieldEnd = fieldEnd;
    }

}
