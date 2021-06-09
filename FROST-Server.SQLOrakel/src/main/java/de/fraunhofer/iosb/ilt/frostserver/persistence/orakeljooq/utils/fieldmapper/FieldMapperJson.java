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

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.loader.DefEntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.persistence.orakeljooq.OrakelPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.orakeljooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.orakeljooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.orakeljooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.orakeljooq.utils.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.orakeljooq.utils.PropertyFieldRegistry;
import de.fraunhofer.iosb.ilt.frostserver.persistence.orakeljooq.utils.Utils;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
import org.jooq.Field;
import org.jooq.Record;

/**
 *
 * @author hylke
 */
public class FieldMapperJson extends FieldMapperAbstract {

    private String field;
    /**
     * Flag indicating the data type is a Map, not a raw json type.
     */
    private boolean isMap = true;

    private int fieldIdx;

    private DefEntityProperty parent;

    @Override
    public void setParent(DefEntityProperty parent) {
        this.parent = parent;
    }

    @Override
    public void registerField(OrakelPersistenceManager ppm, StaMainTable staTable) {
        fieldIdx = getOrRegisterField(ppm, field, staTable, new JsonBinding());
    }

    @Override
    public <J extends Comparable<J>, T extends StaMainTable<J, T>> void registerMapping(OrakelPersistenceManager ppm, T table) {
        final EntityProperty entityProperty = parent.getEntityProperty();
        final PropertyFieldRegistry<J, T> pfReg = table.getPropertyFieldRegistry();
        final int idx = fieldIdx;
        if (isMap) {
            pfReg.addEntryMap(entityProperty, t -> t.field(idx));
        } else {
            pfReg.addEntry(entityProperty, t -> t.field(idx),
                    new PropertyFieldRegistry.ConverterRecordDeflt<>(
                            (T t, Record tuple, Entity entity, DataSize dataSize) -> {
                                final JsonValue fieldJsonValue = Utils.getFieldJsonValue(tuple, (Field) t.field(idx));
                                dataSize.increase(fieldJsonValue.getStringLength());
                                entity.setProperty(entityProperty, fieldJsonValue.getValue(entityProperty.getType()));
                            },
                            (t, entity, insertFields) -> {
                                insertFields.put(t.field(idx), new JsonValue(entity.getProperty(entityProperty)));
                            },
                            (t, entity, updateFields, message) -> {
                                updateFields.put(t.field(idx), new JsonValue(entity.getProperty(entityProperty)));
                                message.addField(entityProperty);
                            }));
        }
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

    /**
     * @return the isMap
     */
    public boolean getIsMap() {
        return isMap;
    }

    /**
     * @param isMap the isMap to set
     */
    public void setIsMap(boolean isMap) {
        this.isMap = isMap;
    }

}
