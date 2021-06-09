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
import de.fraunhofer.iosb.ilt.frostserver.persistence.orakeljooq.tables.StaTable;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.jooq.Binding;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
public abstract class FieldMapperAbstract implements FieldMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(FieldMapperAbstract.class.getName());

    public static int getOrRegisterField(final OrakelPersistenceManager opm, final String fieldName, StaTable staTable) {
        return getOrRegisterField(opm, fieldName, staTable, null);
    }

    public static int getOrRegisterField(final OrakelPersistenceManager opm, final String fieldName, StaTable staTable, Binding binding) {
        int idx = staTable.indexOf(fieldName);
        if (idx >= 0) {
            return idx;
        }
        ResultSet resultSet = opm.getDslContext()
                .select(DSL.asterisk())
                .from(staTable)
                .where(DSL.field("ROWNUM").le(1))
                .fetchResultSet();

        int fieldColumn = -1;
        Field<?> dbField = null;
        try {
            fieldColumn = resultSet.findColumn(fieldName);
            int columnType = resultSet.getMetaData().getColumnType(fieldColumn);
            String columnTypeName = resultSet.getMetaData().getColumnTypeName(fieldColumn);

            dbField = DSL.field(DSL.name(fieldName));
        } catch (SQLException ex) {
            LOGGER.error("Failed to find column for {}", fieldName, ex);
            throw new IllegalArgumentException("Could not find field " + fieldName + " on table " + staTable.getName());
        }
//        Field<?> dbField = new dbTable.field(fieldName);
        if (dbField == null) {
            LOGGER.error("Could not find field {} on table {}.", fieldName, staTable.getName());
            throw new IllegalArgumentException("Could not find field " + fieldName + " on table " + staTable.getName());
        }
        DataType<?> dataType = dbField.getDataType();
        LOGGER.info("  Registering {} -> {}.{} ({})", staTable.getName(), staTable.getName(), fieldName, dataType);
        return staTable.registerField(fieldName, dataType, binding);
    }

}
