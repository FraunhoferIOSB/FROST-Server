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
package de.fraunhofer.iosb.ilt.frostserver.plugin.actuation;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.MomentBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTableAbstract;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.ConverterTimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
import net.time4j.Moment;
import org.jooq.DataType;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;

public class TableImpTasks extends StaTableAbstract<TableImpTasks> {

    private static final long serialVersionUID = -1457801967;

    /**
     * The column <code>public.TASKS.CREATION_TIME</code>.
     */
    public final TableField<Record, Moment> colCreationTime = createField(DSL.name("CREATION_TIME"), SQLDataType.TIMESTAMP, this, "", new MomentBinding());

    /**
     * The column <code>public.TASKINGCAPABILITIES.EP_PROPERTIES</code>.
     */
    public final TableField<Record, JsonValue> colTaskingParameters = createField(DSL.name("TASKING_PARAMETERS"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());

    /**
     * The column <code>public.TASKS.EP_ID</code>.
     */
    public final TableField<Record, ?> colId = createField(DSL.name("ID"), getIdType(), this);

    /**
     * The column <code>public.TASKS.THING_ID</code>.
     */
    public final TableField<Record, ?> colTaskingCapabilityId;

    private final transient PluginActuation pluginActuation;
    private final transient PluginCoreModel pluginCoreModel;

    /**
     * Create a <code>public.TASKS</code> table reference.
     *
     * @param idType The (SQL)DataType of the Id columns used in the actual
     * database.
     * @param pluginActuation the actuation plugin this table belongs to.
     * @param pluginCoreModel the coreModel plugin that this data model links
     * to.
     */
    public TableImpTasks(DataType<?> idType, DataType<?> idTypeTaskingCap, PluginActuation pluginActuation, PluginCoreModel pluginCoreModel) {
        super(idType, DSL.name("TASKS"), null);
        this.pluginActuation = pluginActuation;
        this.pluginCoreModel = pluginCoreModel;
        colTaskingCapabilityId = createField(DSL.name("TASKINGCAPABILITY_ID"), idTypeTaskingCap);
    }

    private TableImpTasks(Name alias, TableImpTasks aliased, PluginActuation pluginActuation, PluginCoreModel pluginCoreModel) {
        super(aliased.getIdType(), alias, aliased);
        this.pluginActuation = pluginActuation;
        this.pluginCoreModel = pluginCoreModel;
        colTaskingCapabilityId = createField(DSL.name("TASKINGCAPABILITY_ID"), aliased.colTaskingCapabilityId.getDataType());
    }

    @Override
    public void initRelations() {
        final TableCollection tables = getTables();
        final TableImpTaskingCapabilities tableTaskingCaps = tables.getTableForClass(TableImpTaskingCapabilities.class);
        registerRelation(new RelationOneToMany<>(pluginActuation.npTaskingCapabilityTask, this, tableTaskingCaps)
                .setSourceFieldAccessor(TableImpTasks::getTaskingCapabilityId)
                .setTargetFieldAccessor(TableImpTaskingCapabilities::getId)
        );
    }

    @Override
    public void initProperties(final EntityFactories entityFactories) {
        pfReg.addEntryId(TableImpTasks::getId);
        pfReg.addEntry(pluginCoreModel.epCreationTime, table -> table.colCreationTime,
                new ConverterTimeInstant<>(pluginCoreModel.epCreationTime, table -> table.colCreationTime));
        pfReg.addEntryMap(pluginActuation.epTaskingParameters, table -> table.colTaskingParameters);
        pfReg.addEntry(pluginActuation.npTaskingCapabilityTask, TableImpTasks::getTaskingCapabilityId);
    }

    @Override
    public EntityType getEntityType() {
        return pluginActuation.etTask;
    }

    @Override
    public TableField<Record, ?> getId() {
        return colId;
    }

    public TableField<Record, ?> getTaskingCapabilityId() {
        return colTaskingCapabilityId;
    }

    @Override
    public TableImpTasks as(Name alias) {
        return new TableImpTasks(alias, this, pluginActuation, pluginCoreModel).initCustomFields();
    }

    @Override
    public TableImpTasks getThis() {
        return this;
    }

}
