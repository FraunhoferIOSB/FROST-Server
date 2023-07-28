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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.fieldmapper;

import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableField;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorString;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTableDynamic;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry;
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

    @ConfigurableField(editor = EditorString.class,
            label = "Field", description = "The database field to use.")
    @EditorString.EdOptsString()
    private String field;

    @Override
    public void registerField(PostgresPersistenceManager ppm, StaMainTable staTable) {
        if (!(staTable instanceof StaTableDynamic)) {
            throw new IllegalArgumentException("Id fields can only be registered on StaTableDynamic, not on " + staTable.getClass().getName());
        }
        StaTableDynamic staTableDynamic = (StaTableDynamic) staTable;
        final Name tableName = staTableDynamic.getQualifiedName();
        final Table<?> dbTable = ppm.getDbTable(tableName);
        final Field<?> dbField = dbTable.field(field);
        if (dbField == null) {
            LOGGER.error("Could not find field {} on table {}.", field, tableName);
            return;
        }
        DataType<?> dataType = dbField.getDataType();
        LOGGER.info("  Registering {} -> {}.{}", staTableDynamic.getName(), dbTable.getName(), field);
        staTableDynamic.registerIdField(field, dataType);
    }

    @Override
    public <T extends StaMainTable<T>> void registerMapping(PostgresPersistenceManager ppm, T table) {
        PropertyFieldRegistry<T> pfReg = table.getPropertyFieldRegistry();
        pfReg.addEntryId(StaMainTable::getId);
    }

    /**
     * @return the field
     */
    public String getField() {
        return field;
    }

    /**
     * @param field the field to set
     * @return this.
     */
    public FieldMapperId setField(String field) {
        this.field = field;
        return this;
    }
}
