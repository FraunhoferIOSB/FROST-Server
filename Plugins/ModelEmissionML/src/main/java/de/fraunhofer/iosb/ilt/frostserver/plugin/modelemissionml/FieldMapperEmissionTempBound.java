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
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.MomentBinding;
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
public class FieldMapperEmissionTempBound extends FieldMapperAbstractEp {

    @ConfigurableField(editor = EditorString.class,
            label = "TimeField", description = "The database field for the time attribute.")
    @EditorString.EdOptsString()
    private String fieldTime;

    @ConfigurableField(editor = EditorString.class,
            label = "QualityField", description = "The database field for the quality attribute.")
    @EditorString.EdOptsString()
    private String fieldQuality;

    @JsonIgnore
    private int fieldIdxTime;
    @JsonIgnore
    private int fieldIdxQuality;

    @Override
    public void registerField(JooqPersistenceManager ppm, StaMainTable staTable) {
        final Name tableName = staTable.getQualifiedName();
        final Table<?> dbTable = ppm.getDbTable(tableName);
        fieldIdxTime = getOrRegisterField(fieldTime, dbTable, staTable, MomentBinding.instance());
        fieldIdxQuality = getOrRegisterField(fieldQuality, dbTable, staTable, JsonBinding.instance());
    }

    @Override
    public <T extends StaMainTable<T>> void registerMapping(JooqPersistenceManager ppm, T table) {
        final EntityPropertyMain property = getParent().getEntityProperty();
        final PropertyFieldRegistry<T> pfReg = table.getPropertyFieldRegistry();
        final int idxTime = fieldIdxTime;
        final int idxQuality = fieldIdxQuality;

        pfReg.addEntry(property, new ConverterRecordDeflt<>())
                .addSubProperty(pfReg.createEntryTimeInstant(PluginModelEmissionML.EM_EP_TIME, t -> t.field(idxTime)))
                .addSubProperty(pfReg.createEntryJson(PluginModelEmissionML.EM_EP_QUALITY, t -> t.field(idxQuality)));

    }

    @Override
    public Map<String, String> getFieldTypes() {
        Map<String, String> value = new LinkedHashMap<>();
        value.put(fieldTime, "TIMESTAMP WITH TIME ZONE");
        value.put(fieldQuality, "TEXT");
        return value;
    }

    public String getFieldTime() {
        return fieldTime;
    }

    public void setFieldValue(String fieldTime) {
        this.fieldTime = fieldTime;
    }

    public String getFieldQuality() {
        return fieldQuality;
    }

    public void setFieldQuality(String fieldQuality) {
        this.fieldQuality = fieldQuality;
    }

}
