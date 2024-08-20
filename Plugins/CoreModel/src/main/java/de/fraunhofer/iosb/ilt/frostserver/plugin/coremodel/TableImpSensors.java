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
package de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTableAbstract;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.validator.SecurityTableWrapper;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import java.util.Arrays;
import java.util.List;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;

public class TableImpSensors extends StaTableAbstract<TableImpSensors> {

    public static final String NAME_TABLE = "SENSORS";
    public static final String NAME_COL_DESCRIPTION = "DESCRIPTION";
    public static final String NAME_COL_ENCODINGTYPE = "ENCODING_TYPE";
    public static final String NAME_COL_ID = "ID";
    public static final String NAME_COL_METADATA = "METADATA";
    public static final String NAME_COL_NAME = "NAME";
    public static final String NAME_COL_PROPERTIES = "PROPERTIES";

    private static final long serialVersionUID = 1850108682;

    /**
     * The column <code>public.SENSORS.DESCRIPTION</code>.
     */
    public final TableField<Record, String> colDescription = createField(DSL.name(NAME_COL_DESCRIPTION), SQLDataType.CLOB, this);

    /**
     * The column <code>public.SENSORS.ENCODING_TYPE</code>.
     */
    public final TableField<Record, String> colEncodingType = createField(DSL.name(NAME_COL_ENCODINGTYPE), SQLDataType.CLOB, this);

    /**
     * The column <code>public.SENSORS.METADATA</code>.
     */
    public final TableField<Record, String> colMetadata = createField(DSL.name(NAME_COL_METADATA), SQLDataType.CLOB, this);

    /**
     * The column <code>public.SENSORS.NAME</code>.
     */
    public final TableField<Record, String> colName = createField(DSL.name(NAME_COL_NAME), SQLDataType.CLOB.defaultValue(DSL.field("'no name'::text", SQLDataType.CLOB)), this);

    /**
     * The column <code>public.SENSORS.PROPERTIES</code>.
     */
    public final TableField<Record, JsonValue> colProperties = createField(DSL.name(NAME_COL_PROPERTIES), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", JsonBinding.instance());

    /**
     * The column <code>public.SENSORS.ID</code>.
     */
    public final TableField<Record, ?> colId = createField(DSL.name(NAME_COL_ID), getIdType(), this);

    private final transient PluginCoreModel pluginCoreModel;

    /**
     * Create a <code>public.SENSORS</code> table reference.
     *
     * @param idType The (SQL)DataType of the Id columns used in the actual
     * database.
     * @param pluginCoreModel the coreModel plugin this table belongs to.
     */
    public TableImpSensors(DataType<?> idType, PluginCoreModel pluginCoreModel) {
        super(idType, DSL.name(NAME_TABLE), null, null);
        this.pluginCoreModel = pluginCoreModel;
    }

    private TableImpSensors(Name alias, TableImpSensors aliased, PluginCoreModel pluginCoreModel) {
        this(alias, aliased, aliased, pluginCoreModel);
    }

    private TableImpSensors(Name alias, TableImpSensors aliased, Table updatedSql, PluginCoreModel pluginCoreModel) {
        super(aliased.getIdType(), alias, aliased, updatedSql);
        this.pluginCoreModel = pluginCoreModel;
    }

    @Override
    public void initRelations() {
        final TableCollection tables = getTables();
        TableImpDatastreams tableDs = tables.getTableForClass(TableImpDatastreams.class);
        registerRelation(new RelationOneToMany<>(pluginCoreModel.npDatastreamsSensor, this, tableDs)
                .setSourceFieldAccessor(TableImpSensors::getId)
                .setTargetFieldAccessor(TableImpDatastreams::getSensorId));
    }

    @Override
    public void initProperties(final EntityFactories entityFactories) {
        pfReg.addEntryId(TableImpSensors::getId);
        pfReg.addEntryString(pluginCoreModel.epName, table -> table.colName);
        pfReg.addEntryString(pluginCoreModel.epDescription, table -> table.colDescription);
        pfReg.addEntryString(ModelRegistry.EP_ENCODINGTYPE, table -> table.colEncodingType);
        pfReg.addEntryString(pluginCoreModel.epMetadata, table -> table.colMetadata);
        pfReg.addEntryMap(ModelRegistry.EP_PROPERTIES, table -> table.colProperties);
        pfReg.addEntry(pluginCoreModel.npDatastreamsSensor, TableImpSensors::getId);
    }

    @Override
    public EntityType getEntityType() {
        return pluginCoreModel.etSensor;
    }

    @Override
    public List<Field> getPkFields() {
        return Arrays.asList(colId);
    }

    public TableField<Record, ?> getId() {
        return colId;
    }

    @Override
    public TableImpSensors as(Name alias) {
        return new TableImpSensors(alias, this, pluginCoreModel).initCustomFields();
    }

    @Override
    public TableImpSensors asSecure(String name, JooqPersistenceManager pm) {
        final SecurityTableWrapper securityWrapper = getSecurityWrapper();
        if (securityWrapper == null || PrincipalExtended.getLocalPrincipal().isAdmin()) {
            return as(name);
        }
        final Table wrappedTable = securityWrapper.wrap(this, pm);
        return new TableImpSensors(DSL.name(name), this, wrappedTable, pluginCoreModel).initCustomFields();
    }

    @Override
    public TableImpSensors getThis() {
        return this;
    }

}
