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
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.PostGisGeometryBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTableDynamic;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.Utils;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.SQLDataType;

/**
 *
 * @author hylke
 */
public class FieldMapperGeometry extends FieldMapperAbstract {

    private String fieldSource;
    private String fieldGeom;

    private int fieldSourceIdx;
    private int fieldGeomIdx;

    @Override
    public void registerField(PostgresPersistenceManager ppm, StaTableDynamic staTable, Property property) {
        final Name tableName = staTable.getQualifiedName();
        Table<?> dbTable = ppm.getDbTable(tableName);
        fieldSourceIdx = getOrRegisterField(fieldSource, dbTable, staTable);
        fieldGeomIdx = getOrRegisterField(fieldGeom, dbTable, staTable, new PostGisGeometryBinding());
    }

    @Override
    public <J extends Comparable<J>> void registerMapping(PostgresPersistenceManager ppm, StaTableDynamic<J> table, Property property) {
        PropertyFieldRegistry<J, StaTableDynamic<J>> pfReg = table.getPropertyFieldRegistry();
        final int idxLocation = fieldSourceIdx;
        final int idxGeom = fieldGeomIdx;
        pfReg.addEntry(property,
                new PropertyFieldRegistry.ConverterRecordDeflt<>(
                        (StaTableDynamic<J> t, Record tuple, Entity entity, DataSize dataSize) -> {
                            String locationString = tuple.get(t.field(idxLocation, SQLDataType.CLOB));
                            dataSize.increase(locationString == null ? 0 : locationString.length());
                            entity.setProperty(property, Utils.locationUnknownEncoding(locationString));
                        },
                        (t, entity, insertFields) -> {
                            Object feature = entity.getProperty(property);
                            EntityFactories.insertGeometry(insertFields, t.field(idxLocation, SQLDataType.CLOB), t.field(idxGeom), null, feature);
                        },
                        (t, entity, updateFields, message) -> {
                            Object feature = entity.getProperty(property);
                            EntityFactories.insertGeometry(updateFields, t.field(idxLocation, SQLDataType.CLOB), t.field(idxGeom), null, feature);
                            message.addField(property);
                        }),
                new PropertyFieldRegistry.NFP<>("j", t -> t.field(idxLocation)));
        pfReg.addEntryNoSelect(property, "g", t -> t.field(idxGeom));
    }

    /**
     * @return the fieldSource
     */
    public String getFieldSource() {
        return fieldSource;
    }

    /**
     * @param fieldSource the fieldSource to set
     */
    public void setFieldSource(String fieldSource) {
        this.fieldSource = fieldSource;
    }

    /**
     * @return the fieldGeom
     */
    public String getFieldGeom() {
        return fieldGeom;
    }

    /**
     * @param fieldGeom the fieldGeom to set
     */
    public void setFieldGeom(String fieldGeom) {
        this.fieldGeom = fieldGeom;
    }

}
