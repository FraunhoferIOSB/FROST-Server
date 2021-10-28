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

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.loader.DefEntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.ResultType;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.Utils;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import java.math.BigDecimal;
import java.util.Map;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Table;

/**
 *
 * @author hylke
 */
public class FieldMapperResult extends FieldMapperAbstract {

    private String fieldType;
    private String fieldString;
    private String fieldNumber;
    private String fieldJson;
    private String fieldBoolean;

    private int fieldTypeIdx;
    private int fieldStringIdx;
    private int fieldNumberIdx;
    private int fieldJsonIdx;
    private int fieldBooleanIdx;

    private DefEntityProperty parent;

    @Override
    public void setParent(DefEntityProperty parent) {
        this.parent = parent;
    }

    @Override
    public void registerField(PostgresPersistenceManager ppm, StaMainTable staTable) {
        // find the actual field
        final Name tableName = staTable.getQualifiedName();
        Table<?> dbTable = ppm.getDbTable(tableName);
        fieldTypeIdx = getOrRegisterField(fieldType, dbTable, staTable);
        fieldStringIdx = getOrRegisterField(fieldString, dbTable, staTable);
        fieldNumberIdx = getOrRegisterField(fieldNumber, dbTable, staTable);
        fieldJsonIdx = getOrRegisterField(fieldJson, dbTable, staTable, new JsonBinding());
        fieldBooleanIdx = getOrRegisterField(fieldBoolean, dbTable, staTable);
    }

    @Override
    public <T extends StaMainTable<T>> void registerMapping(PostgresPersistenceManager ppm, T table) {
        final PropertyFieldRegistry<T> pfReg = table.getPropertyFieldRegistry();
        final int idxType = fieldTypeIdx;
        final int idxString = fieldStringIdx;
        final int idxNumber = fieldNumberIdx;
        final int idxJson = fieldJsonIdx;
        final int idxBoolean = fieldBooleanIdx;
        final EntityPropertyMain property = parent.getEntityProperty();

        pfReg.addEntry(property,
                new PropertyFieldRegistry.ConverterRecordDeflt<>(
                        (T t, Record tuple, Entity entity, DataSize dataSize) -> readResultFromDb(entity, property, t, tuple, dataSize, idxType, idxString, idxNumber, idxBoolean, idxJson),
                        (t, entity, insertFields) -> handleResult(entity, property, t, insertFields, idxType, idxString, idxNumber, idxBoolean, idxJson),
                        (t, entity, updateFields, message) -> {
                            handleResult(entity, property, t, updateFields, idxType, idxString, idxNumber, idxBoolean, idxJson);
                            message.addField(property);
                        }),
                new PropertyFieldRegistry.NFP<>("n", t -> t.field(idxNumber)),
                new PropertyFieldRegistry.NFP<>("b", t -> t.field(idxBoolean)),
                new PropertyFieldRegistry.NFP<>("s", t -> t.field(idxString)),
                new PropertyFieldRegistry.NFP<>("j", t -> t.field(idxJson)),
                new PropertyFieldRegistry.NFP<>("t", t -> t.field(idxType)));
    }

    public <T extends StaMainTable<T>> void handleResult(
            Entity entity, Property property,
            T table, Map<Field, Object> output,
            int idxReTy, int idxReSt, int idxReNu, int idxReBo, int idxReJs) {
        Object result = entity.getProperty(property);
        if (result instanceof Number) {
            output.put(table.field(idxReTy), ResultType.NUMBER.sqlValue());
            output.put(table.field(idxReSt), result.toString());
            output.put(table.field(idxReNu), ((Number) result).doubleValue());
            output.put(table.field(idxReBo), null);
            output.put(table.field(idxReJs), null);
        } else if (result instanceof Boolean) {
            output.put(table.field(idxReTy), ResultType.BOOLEAN.sqlValue());
            output.put(table.field(idxReSt), result.toString());
            output.put(table.field(idxReBo), result);
            output.put(table.field(idxReNu), null);
            output.put(table.field(idxReJs), null);
        } else if (result instanceof String) {
            output.put(table.field(idxReTy), ResultType.STRING.sqlValue());
            output.put(table.field(idxReSt), result.toString());
            output.put(table.field(idxReNu), null);
            output.put(table.field(idxReBo), null);
            output.put(table.field(idxReJs), null);
        } else {
            output.put(table.field(idxReTy), ResultType.OBJECT_ARRAY.sqlValue());
            output.put(table.field(idxReJs), EntityFactories.objectToJson(result));
            output.put(table.field(idxReSt), null);
            output.put(table.field(idxReNu), null);
            output.put(table.field(idxReBo), null);
        }
    }

    public <T extends StaMainTable<T>> void readResultFromDb(
            Entity entity, Property property,
            T table, Record tuple, DataSize dataSize,
            int idxReTy, int idxReSt, int idxReNu, int idxReBo, int idxReJs) {
        Short resultTypeOrd = Utils.getFieldOrNull(tuple, (Field<Short>) table.field(idxReTy));
        if (resultTypeOrd != null) {
            ResultType resultType = ResultType.fromSqlValue(resultTypeOrd);
            switch (resultType) {
                case BOOLEAN:
                    entity.setProperty(property, Utils.getFieldOrNull(tuple, table.field(idxReBo)));
                    break;

                case NUMBER:
                    handleNumber(entity, property, table, tuple, idxReSt, idxReNu);
                    break;

                case OBJECT_ARRAY:
                    JsonValue jsonData = Utils.getFieldJsonValue(tuple, (Field<JsonValue>) table.field(idxReJs));
                    dataSize.increase(jsonData.getStringLength());
                    entity.setProperty(property, jsonData.getValue());
                    break;

                case STRING:
                    String stringData = Utils.getFieldOrNull(tuple, (Field<String>) table.field(idxReSt));
                    dataSize.increase(stringData == null ? 0 : stringData.length());
                    entity.setProperty(property, stringData);
                    break;

                default:
                    throw new IllegalStateException("Unhandled resultType: " + resultType);
            }
        }
    }

    private <T extends StaMainTable<T>> void handleNumber(Entity entity, Property property, T table, Record tuple, int idxReSt, int idxReNu) {
        try {
            entity.setProperty(property, new BigDecimal(Utils.getFieldOrNull(tuple, (Field<String>) table.field(idxReSt))));
        } catch (NumberFormatException | NullPointerException e) {
            // It was not a Number? Use the double value.
            entity.setProperty(property, Utils.getFieldOrNull(tuple, table.field(idxReNu)));
        }
    }

    /**
     * @return the fieldType
     */
    public String getFieldType() {
        return fieldType;
    }

    /**
     * @param fieldType the fieldType to set
     */
    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    /**
     * @return the fieldString
     */
    public String getFieldString() {
        return fieldString;
    }

    /**
     * @param fieldString the fieldString to set
     */
    public void setFieldString(String fieldString) {
        this.fieldString = fieldString;
    }

    /**
     * @return the fieldNumber
     */
    public String getFieldNumber() {
        return fieldNumber;
    }

    /**
     * @param fieldNumber the fieldNumber to set
     */
    public void setFieldNumber(String fieldNumber) {
        this.fieldNumber = fieldNumber;
    }

    /**
     * @return the fieldJson
     */
    public String getFieldJson() {
        return fieldJson;
    }

    /**
     * @param fieldJson the fieldJson to set
     */
    public void setFieldJson(String fieldJson) {
        this.fieldJson = fieldJson;
    }

    /**
     * @return the fieldBoolean
     */
    public String getFieldBoolean() {
        return fieldBoolean;
    }

    /**
     * @param fieldBoolean the fieldBoolean to set
     */
    public void setFieldBoolean(String fieldBoolean) {
        this.fieldBoolean = fieldBoolean;
    }

}
