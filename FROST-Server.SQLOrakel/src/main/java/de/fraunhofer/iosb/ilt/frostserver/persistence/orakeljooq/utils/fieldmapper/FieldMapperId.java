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
package de.fraunhofer.iosb.ilt.frostserver.persistence.orakeljooq.utils.fieldmapper;

import de.fraunhofer.iosb.ilt.frostserver.persistence.orakeljooq.OrakelPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.orakeljooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.orakeljooq.tables.StaTableDynamic;
import de.fraunhofer.iosb.ilt.frostserver.persistence.orakeljooq.utils.PropertyFieldRegistry;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.impl.DSL;
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
    public void registerField(OrakelPersistenceManager ppm, StaMainTable staTable) {
        if (!(staTable instanceof StaTableDynamic)) {
            throw new IllegalArgumentException("Id fields can only be registered on StaTableDynamic, not on " + staTable.getClass().getName());
        }
        int idx = staTable.indexOf(field);
        if (idx >= 0) {
            return;
        }
        StaTableDynamic staTableDynamic = (StaTableDynamic) staTable;
        ResultSet resultSet = ppm.getDslContext()
                .select(DSL.asterisk())
                .from(staTable)
                .where(DSL.field("ROWNUM").le(1))
                .fetchResultSet();

        int fieldColumn = -1;
        Field<?> dbField = null;
        try {
            fieldColumn = resultSet.findColumn(field);
            int columnType = resultSet.getMetaData().getColumnType(fieldColumn);
            String columnTypeName = resultSet.getMetaData().getColumnTypeName(fieldColumn);

            dbField = DSL.field(DSL.name(field));
        } catch (SQLException ex) {
            LOGGER.error("Failed to find column for {}", field, ex);
            throw new IllegalArgumentException("Could not find field " + field + " on table " + staTable.getName());
        }
//        Field<?> dbField = new dbTable.field(fieldName);
        if (dbField == null) {
            LOGGER.error("Could not find field {} on table {}.", field, staTable.getName());
            throw new IllegalArgumentException("Could not find field " + field + " on table " + staTable.getName());
        }
        DataType<?> dataType = dbField.getDataType();
        LOGGER.info("  Registering {} -> {}.{} ({})", staTable.getName(), staTable.getName(), field, dataType);
        staTableDynamic.registerIdField(field, dataType);
    }

    @Override
    public <J extends Comparable<J>, T extends StaMainTable<J, T>> void registerMapping(OrakelPersistenceManager ppm, T table) {
        PropertyFieldRegistry<J, T> pfReg = table.getPropertyFieldRegistry();
        pfReg.addEntryId(ppm.getIdManager(), t -> t.getId());
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
