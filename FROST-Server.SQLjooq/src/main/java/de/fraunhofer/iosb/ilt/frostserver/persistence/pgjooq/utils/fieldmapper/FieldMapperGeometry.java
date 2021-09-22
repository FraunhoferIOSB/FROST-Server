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
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.PostGisGeometryBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.Utils;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

/**
 *
 * @author hylke
 */
public class FieldMapperGeometry extends FieldMapperAbstract {

    private String fieldSource;
    private String fieldGeom;

    private int fieldSourceIdx = -1;
    private int fieldGeomIdx;

    private DefEntityProperty parent;

    @Override
    public void setParent(DefEntityProperty parent) {
        this.parent = parent;
    }

    @Override
    public void registerField(PostgresPersistenceManager ppm, StaMainTable staTable) {
        final Name tableName = staTable.getQualifiedName();
        final Table<?> dbTable = ppm.getDbTable(tableName);
        fieldGeomIdx = getOrRegisterField(fieldGeom, dbTable, staTable, new PostGisGeometryBinding());
        if (!StringHelper.isNullOrEmpty(fieldSource)) {
            fieldSourceIdx = getOrRegisterField(fieldSource, dbTable, staTable);
        }
    }

    @Override
    public <J extends Comparable<J>, T extends StaMainTable<J, T>> void registerMapping(PostgresPersistenceManager ppm, T table) {
        final EntityPropertyMain property = parent.getEntityProperty();
        final PropertyFieldRegistry<J, T> pfReg = table.getPropertyFieldRegistry();
        final int idxLocation = fieldSourceIdx;
        final int idxGeom = fieldGeomIdx;
        final PropertyFieldRegistry.NFP<T> sourcePfr;
        if (idxLocation >= 0) {
            sourcePfr = new PropertyFieldRegistry.NFP<>("j", t -> t.field(idxLocation));
        } else {
            sourcePfr = new PropertyFieldRegistry.NFP<>("j", t -> DSL.field("ST_AsGeoJSON(?)", String.class, t.field(idxGeom, SQLDataType.CLOB)));
        }
        pfReg.addEntry(
                property,
                new PropertyFieldRegistry.ConverterRecordDeflt<>(
                        (T t, Record tuple, Entity entity, DataSize dataSize) -> {
                            String locationString;
                            if (idxLocation >= 0) {
                                locationString = tuple.get(t.field(idxLocation, SQLDataType.CLOB));
                            } else {
                                locationString = tuple.get(
                                        DSL.field("ST_AsGeoJSON(?)", String.class, t.field(idxGeom, SQLDataType.CLOB))
                                );
                            }
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
                sourcePfr);
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
