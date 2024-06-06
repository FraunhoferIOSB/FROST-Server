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

import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.StaTimeIntervalWrapper.KEY_TIME_INTERVAL_END;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.StaTimeIntervalWrapper.KEY_TIME_INTERVAL_START;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.fraunhofer.iosb.ilt.configurable.annotations.ConfigurableField;
import de.fraunhofer.iosb.ilt.configurable.editor.EditorString;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.MomentBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
import java.util.HashMap;
import java.util.Map;
import org.jooq.Name;
import org.jooq.Table;

/**
 *
 * @author hylke
 */
public class FieldMapperTimeInterval extends FieldMapperAbstractEp {

    @ConfigurableField(editor = EditorString.class,
            label = "Start Field", description = "The database field to use for the start DateTime.")
    @EditorString.EdOptsString()
    private String fieldStart;

    @ConfigurableField(editor = EditorString.class,
            label = "End Field", description = "The database field to use for the end DateTime.")
    @EditorString.EdOptsString()
    private String fieldEnd;

    @JsonIgnore
    private int fieldStartIdx;
    @JsonIgnore
    private int fieldEndIdx;

    @Override
    public void registerField(JooqPersistenceManager ppm, StaMainTable staTable) {
        // find the actual field
        final Name tableName = staTable.getQualifiedName();
        Table<?> dbTable = ppm.getDbTable(tableName);
        fieldStartIdx = getOrRegisterField(fieldStart, dbTable, staTable, MomentBinding.instance());
        fieldEndIdx = getOrRegisterField(fieldEnd, dbTable, staTable, MomentBinding.instance());
    }

    @Override
    public <T extends StaMainTable<T>> void registerMapping(JooqPersistenceManager ppm, T table) {
        final EntityProperty<TimeInterval> property = getParent().getEntityProperty();
        final PropertyFieldRegistry<T> pfReg = table.getPropertyFieldRegistry();
        final int idxStart = fieldStartIdx;
        final int idxEnd = fieldEndIdx;
        pfReg.addEntry(property,
                new PropertyFieldRegistry.ConverterTimeInterval<>(property, t -> t.field(idxStart), t -> t.field(idxEnd)),
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
     * @return this.
     */
    public FieldMapperTimeInterval setFieldStart(String fieldStart) {
        this.fieldStart = fieldStart;
        return this;
    }

    /**
     * @return the fieldEnd
     */
    public String getFieldEnd() {
        return fieldEnd;
    }

    /**
     * @param fieldEnd the fieldEnd to set
     * @return this.
     */
    public FieldMapperTimeInterval setFieldEnd(String fieldEnd) {
        this.fieldEnd = fieldEnd;
        return this;
    }

    @Override
    public Map<String, String> getFieldTypes() {
        Map<String, String> value = new HashMap<>();
        value.put(fieldStart, "TIMESTAMP WITH TIME ZONE");
        value.put(fieldEnd, "TIMESTAMP WITH TIME ZONE");
        return value;
    }

}
