/*
 * Copyright (C) 2024 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.plugin.modelemissionml;

import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.fieldmapper.FieldMapperAbstract.getOrRegisterField;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableField;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorString;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.BigDecimalBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.ConverterRecordDeflt;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.fieldmapper.FieldMapperAbstractEp;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jooq.Name;
import org.jooq.Table;

/**
 * A FieldMapper for Geometry columns.
 */
public class FieldMapperEmissionQuantity extends FieldMapperAbstractEp {

    @ConfigurableField(editor = EditorString.class,
            label = "ValueField", description = "The database field for the value attribute.")
    @EditorString.EdOptsString()
    private String fieldValue;

    @ConfigurableField(editor = EditorString.class,
            label = "QualityField", description = "The database field for the quality attribute.")
    @EditorString.EdOptsString()
    private String fieldQuality;

    @ConfigurableField(editor = EditorString.class,
            label = "UnitLabelField", description = "The database field for the label attribute of the unit.")
    @EditorString.EdOptsString()
    private String fieldUnitLabel;

    @ConfigurableField(editor = EditorString.class,
            label = "UnitSymbolField", description = "The database field for the symbol attribute of the unit.")
    @EditorString.EdOptsString()
    private String fieldUnitSymbol;

    @ConfigurableField(editor = EditorString.class,
            label = "UnitLabelDefinition", description = "The database field for the definition attribute of the unit.")
    @EditorString.EdOptsString()
    private String fieldUnitDefinition;

    @JsonIgnore
    private int fieldIdxValue;
    @JsonIgnore
    private int fieldIdxQuality;
    @JsonIgnore
    private int fieldIdxUnitLabel;
    @JsonIgnore
    private int fieldIdxUnitSymbol;
    @JsonIgnore
    private int fieldIdxUnitDefinition;

    @Override
    public void registerField(JooqPersistenceManager ppm, StaMainTable staTable) {
        final Name tableName = staTable.getQualifiedName();
        final Table<?> dbTable = ppm.getDbTable(tableName);
        fieldIdxValue = getOrRegisterField(fieldValue, dbTable, staTable, BigDecimalBinding.dataType(), BigDecimalBinding.instance());
        fieldIdxQuality = getOrRegisterField(fieldQuality, dbTable, staTable, JsonBinding.instance());
        fieldIdxUnitDefinition = getOrRegisterField(fieldUnitDefinition, dbTable, staTable);
        fieldIdxUnitLabel = getOrRegisterField(fieldUnitLabel, dbTable, staTable);
        fieldIdxUnitSymbol = getOrRegisterField(fieldUnitSymbol, dbTable, staTable);
    }

    @Override
    public <T extends StaMainTable<T>> void registerMapping(JooqPersistenceManager ppm, T table) {
        final EntityPropertyMain property = getParent().getEntityProperty();
        final PropertyFieldRegistry<T> pfReg = table.getPropertyFieldRegistry();
        final int idxValue = fieldIdxValue;
        final int idxQuality = fieldIdxQuality;
        final int idxUnitDef = fieldIdxUnitDefinition;
        final int idxUnitLab = fieldIdxUnitLabel;
        final int idxUnitSym = fieldIdxUnitSymbol;

        pfReg.addEntry(property, new ConverterRecordDeflt<>())
                .addSubProperty(pfReg.createEntryNumeric(PluginModelEmissionML.EM_EP_VALUE, t -> t.field(idxValue)))
                .addSubProperty(pfReg.createEntryJson(PluginModelEmissionML.EM_EP_QUALITY, t -> t.field(idxQuality)))
                .addSubProperty(pfReg.createEntryComplex(PluginModelEmissionML.EM_EP_UNIT, false, new ConverterRecordDeflt<>())
                        .addSubProperty(pfReg.createEntryString(PluginModelEmissionML.EM_EP_UOM_DEFINITION, t -> t.field(idxUnitDef)))
                        .addSubProperty(pfReg.createEntryString(PluginModelEmissionML.EM_EP_UOM_LABEL, t -> t.field(idxUnitLab)))
                        .addSubProperty(pfReg.createEntryString(PluginModelEmissionML.EM_EP_UOM_SYMBOL, t -> t.field(idxUnitSym))));

    }

    @Override
    public Map<String, String> getFieldTypes() {
        Map<String, String> value = new LinkedHashMap<>();
        value.put(fieldValue, "NUMERIC");
        value.put(fieldQuality, "TEXT");
        value.put(fieldUnitDefinition, "TEXT");
        value.put(fieldUnitLabel, "TEXT");
        value.put(fieldUnitSymbol, "TEXT");
        return value;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }

    public String getFieldQuality() {
        return fieldQuality;
    }

    public void setFieldQuality(String fieldQuality) {
        this.fieldQuality = fieldQuality;
    }

    public String getFieldUnitLabel() {
        return fieldUnitLabel;
    }

    public void setFieldUnitLabel(String fieldUnitLabel) {
        this.fieldUnitLabel = fieldUnitLabel;
    }

    public String getFieldUnitSymbol() {
        return fieldUnitSymbol;
    }

    public void setFieldUnitSymbol(String fieldUnitSymbol) {
        this.fieldUnitSymbol = fieldUnitSymbol;
    }

    public String getFieldUnitDefinition() {
        return fieldUnitDefinition;
    }

    public void setFieldUnitDefinition(String fieldUnitDefinition) {
        this.fieldUnitDefinition = fieldUnitDefinition;
    }

}
