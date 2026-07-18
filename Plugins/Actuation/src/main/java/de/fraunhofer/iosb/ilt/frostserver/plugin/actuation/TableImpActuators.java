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
package de.fraunhofer.iosb.ilt.frostserver.plugin.actuation;

import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTableAbstract.TYPE_JSONB;
import static de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.TableImpSensors.NAME_COL_METADATA;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTableAbstract;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.Utils;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.validator.SecurityTableWrapper;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
import de.fraunhofer.iosb.ilt.frostserver.property.StandardProperties;
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

public class TableImpActuators extends StaTableAbstract<TableImpActuators> {

    private static final long serialVersionUID = 1850108682;

    /**
     * The column <code>public.ACTUATORS.EP_DESCRIPTION</code>.
     */
    public final TableField<Record, String> colDescription = createField(DSL.name("DESCRIPTION"), SQLDataType.CLOB, this);

    /**
     * The column <code>public.ACTUATORS.ENCODING_TYPE</code>.
     */
    public final TableField<Record, String> colEncodingType = createField(DSL.name("ENCODING_TYPE"), SQLDataType.CLOB, this);

    /**
     * The column <code>public.ACTUATORS.EP_METADATA</code>.
     */
    public final TableField<Record, JsonValue> colMetadata = createField(DSL.name(NAME_COL_METADATA), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", JsonBinding.instance());

    /**
     * The column <code>public.ACTUATORS.EP_NAME</code>.
     */
    public final TableField<Record, String> colName = createField(DSL.name("NAME"), SQLDataType.CLOB.defaultValue(DSL.field("'no name'::text", SQLDataType.CLOB)), this);

    /**
     * The column <code>public.ACTUATORS.EP_PROPERTIES</code>.
     */
    public final TableField<Record, JsonValue> colProperties = createField(DSL.name("PROPERTIES"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", JsonBinding.instance());

    /**
     * The column <code>public.ACTUATORS.EP_ID</code>.
     */
    public final TableField<Record, ?> colId = createField(DSL.name("ID"), getIdType(), this);

    private final transient PluginActuation pluginActuation;
    private final transient PluginCoreModel pluginCoreModel;

    /**
     * Create a <code>public.ACTUATORS</code> table reference.
     *
     * @param <I> The id column types.
     * @param idType The (SQL)DataType of the Id columns used in the actual
     * database.
     * @param pluginActuation the actuation plugin this table belongs to.
     * @param pluginCoreModel the coreModel plugin that this data model links
     * to.
     */
    public <I> TableImpActuators(DataType<I> idType, PluginActuation pluginActuation, PluginCoreModel pluginCoreModel) {
        super(idType, DSL.name("ACTUATORS"), null, null);
        this.pluginActuation = pluginActuation;
        this.pluginCoreModel = pluginCoreModel;
    }

    private TableImpActuators(Name alias, TableImpActuators aliased, PluginActuation pluginActuation, PluginCoreModel pluginCoreModel) {
        this(alias, aliased, aliased, pluginActuation, pluginCoreModel);
    }

    private TableImpActuators(Name alias, TableImpActuators aliased, Table updatedSql, PluginActuation pluginActuation, PluginCoreModel pluginCoreModel) {
        super(aliased.getIdType(), alias, aliased, updatedSql);
        this.pluginActuation = pluginActuation;
        this.pluginCoreModel = pluginCoreModel;
    }

    @Override
    public void initRelations() {
        final TableCollection tables = getTables();
        final TableImpTaskingCapabilities tableTaskCaps = tables.getTableForClass(TableImpTaskingCapabilities.class);
        registerRelation(new RelationOneToMany<>(pluginActuation.npTaskingCapabilitiesActuator, this, tableTaskCaps)
                .setSourceFieldAccessor(TableImpActuators::getId)
                .setTargetFieldAccessor(TableImpTaskingCapabilities::getActuatorId));
    }

    @Override
    public void initProperties(final EntityFactories entityFactories) {
        pfReg.addEntryId(TableImpActuators::getId);
        pfReg.addEntryString(pluginCoreModel.epName, table -> table.colName);
        pfReg.addEntryString(pluginCoreModel.epDescription, table -> table.colDescription);
        pfReg.addEntryString(StandardProperties.EP_ENCODINGTYPE, table -> table.colEncodingType);
        pfReg.addEntry(pluginCoreModel.epMetadata,
                true,
                new PropertyFieldRegistry.ConverterRecordDeflt<>(
                        (table, tuple, entity, dataSize) -> {
                            JsonValue data = Utils.getFieldJsonValue(tuple, colMetadata);
                            if (data == null) {
                                return;
                            }
                            dataSize.increase(data.getStringLength());
                            entity.setProperty(pluginCoreModel.epMetadata, data.getValue());
                        },
                        (table, entity, insertFields) -> {
                            Object metadata = entity.getProperty(pluginCoreModel.epMetadata);
                            insertFields.put(table.colMetadata, new JsonValue(metadata));
                        },
                        (table, entity, updateFields, message) -> {
                            Object metadata = entity.getProperty(pluginCoreModel.epMetadata);
                            updateFields.put(table.colMetadata, new JsonValue(metadata));
                            message.addField(pluginCoreModel.epMetadata);
                        }),
                new PropertyFieldRegistry.NFP<>("j", table -> table.colMetadata));
        pfReg.addEntryMap(StandardProperties.EP_PROPERTIES, table -> table.colProperties);
        pfReg.addEntry(pluginActuation.npTaskingCapabilitiesActuator, TableImpActuators::getId);
    }

    @Override
    public EntityType getEntityType() {
        return pluginActuation.etActuator;
    }

    @Override
    public List<Field> getPkFields() {
        return Arrays.asList(colId);
    }

    public <T> TableField<Record, T> getId() {
        return (TableField<Record, T>) colId;
    }

    @Override
    public TableImpActuators as(Name alias) {
        return new TableImpActuators(alias, this, pluginActuation, pluginCoreModel).initCustomFields();
    }

    @Override
    public TableImpActuators asSecure(String name, JooqPersistenceManager pm) {
        final SecurityTableWrapper securityWrapper = getSecurityWrapper();
        if (securityWrapper == null || PrincipalExtended.getLocalPrincipal().isAdmin()) {
            return as(name);
        }
        final Table wrappedTable = securityWrapper.wrap(this, pm);
        return new TableImpActuators(DSL.name(name), this, wrappedTable, pluginActuation, pluginCoreModel).initCustomFields();
    }

    @Override
    public TableImpActuators getThis() {
        return this;
    }

}
