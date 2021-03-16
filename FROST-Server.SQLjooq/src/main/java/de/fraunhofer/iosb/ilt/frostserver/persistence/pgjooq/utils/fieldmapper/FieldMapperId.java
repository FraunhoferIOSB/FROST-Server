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

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTableDynamic;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The field mapper for Id fields.
 *
 * @author hylke
 */
public class FieldMapperId extends FieldMapperAbstract {

    private static final Logger LOGGER = LoggerFactory.getLogger(FieldMapperId.class.getName());

    private String field;

    @Override
    public void registerField(PostgresPersistenceManager ppm, StaTableDynamic staTable, Property property) {
        final Name tableName = staTable.getQualifiedName();
        Table<?> dbTable = ppm.getDbTable(tableName);
        Field<?> dbField = dbTable.field(field);
        if (dbField == null) {
            LOGGER.error("Could not find field {} on table {}.", field, tableName);
            return;
        }
        DataType<?> dataType = dbField.getDataType();
        LOGGER.info("  Registering {} -> {}.{}", staTable.getName(), dbTable.getName(), field);
        staTable.registerIdField(field, dataType);
    }

    @Override
    public <J extends Comparable<J>> void registerMapping(PostgresPersistenceManager ppm, StaTableDynamic<J> table, Property property) {
        PropertyFieldRegistry<J, StaTableDynamic<J>> pfReg = table.getPropertyFieldRegistry();
        pfReg.addEntryId(ppm.getIdManager(), StaTableDynamic::getId);
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
