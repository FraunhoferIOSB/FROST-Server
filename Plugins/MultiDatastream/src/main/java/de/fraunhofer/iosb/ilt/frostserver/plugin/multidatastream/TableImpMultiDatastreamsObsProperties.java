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
package de.fraunhofer.iosb.ilt.frostserver.plugin.multidatastream;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaLinkTable;
import org.jooq.DataType;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

public class TableImpMultiDatastreamsObsProperties extends StaLinkTable<TableImpMultiDatastreamsObsProperties> {

    private static final long serialVersionUID = 344714892;

    /**
     * The column <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES.RANK</code>.
     */
    public final TableField<Record, Integer> colRank = createField(DSL.name("RANK"), SQLDataType.INTEGER.nullable(false), this);

    /**
     * The column
     * <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES.MULTI_DATASTREAM_ID</code>.
     */
    public final TableField<Record, ?> colMultiDatastreamId;

    /**
     * The column
     * <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES.OBS_PROPERTY_ID</code>.
     */
    public final TableField<Record, ?> colObsPropertyId;

    /**
     * Create a <code>public.MULTI_DATASTREAMS_OBS_PROPERTIES</code> table
     * reference.
     *
     * @param idTypeMds The (SQL)DataType of the MultiDatastreamId column used
     * in the actual database.
     * @param idTypeObsProp The (SQL)DataType of the ObsPropertyId column used
     * in the actual database.
     */
    public TableImpMultiDatastreamsObsProperties(DataType<?> idTypeMds, DataType<?> idTypeObsProp) {
        super(DSL.name("MULTI_DATASTREAMS_OBS_PROPERTIES"), null);
        colMultiDatastreamId = createField(DSL.name("MULTI_DATASTREAM_ID"), idTypeMds);
        colObsPropertyId = createField(DSL.name("OBS_PROPERTY_ID"), idTypeObsProp);
    }

    private TableImpMultiDatastreamsObsProperties(Name alias, TableImpMultiDatastreamsObsProperties aliased) {
        super(alias, aliased);
        colMultiDatastreamId = createField(DSL.name("MULTI_DATASTREAM_ID"), aliased.colMultiDatastreamId.getDataType());
        colObsPropertyId = createField(DSL.name("OBS_PROPERTY_ID"), aliased.colObsPropertyId.getDataType());
    }

    public TableField<Record, ?> getMultiDatastreamId() {
        return colMultiDatastreamId;
    }

    public TableField<Record, ?> getObsPropertyId() {
        return colObsPropertyId;
    }

    @Override
    public TableImpMultiDatastreamsObsProperties as(Name alias) {
        return new TableImpMultiDatastreamsObsProperties(alias, this).initCustomFields();
    }

    @Override
    public TableImpMultiDatastreamsObsProperties getThis() {
        return this;
    }

}
