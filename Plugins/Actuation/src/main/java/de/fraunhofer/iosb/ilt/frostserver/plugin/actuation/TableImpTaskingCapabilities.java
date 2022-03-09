package de.fraunhofer.iosb.ilt.frostserver.plugin.actuation;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTableAbstract;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.TableImpThings;
import org.jooq.DataType;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;

public class TableImpTaskingCapabilities extends StaTableAbstract<TableImpTaskingCapabilities> {

    private static final long serialVersionUID = -1460005950;

    /**
     * The column <code>public.TASKINGCAPABILITIES.EP_DESCRIPTION</code>.
     */
    public final TableField<Record, String> colDescription = createField(DSL.name("DESCRIPTION"), SQLDataType.CLOB, this);

    /**
     * The column <code>public.TASKINGCAPABILITIES.EP_NAME</code>.
     */
    public final TableField<Record, String> colName = createField(DSL.name("NAME"), SQLDataType.CLOB.defaultValue(DSL.field("'no name'::text", SQLDataType.CLOB)), this);

    /**
     * The column <code>public.TASKINGCAPABILITIES.EP_PROPERTIES</code>.
     */
    public final TableField<Record, JsonValue> colProperties = createField(DSL.name("PROPERTIES"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());

    /**
     * The column <code>public.TASKINGCAPABILITIES.TASKING_PARAMETERS</code>.
     */
    public final TableField<Record, JsonValue> colTaskingParameters = createField(DSL.name("TASKING_PARAMETERS"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());

    /**
     * The column <code>public.TASKINGCAPABILITIES.EP_ID</code>.
     */
    public final TableField<Record, ?> colId = createField(DSL.name("ID"), getIdType(), this);

    /**
     * The column <code>public.TASKINGCAPABILITIES.ACTUATOR_ID</code>.
     */
    public final TableField<Record, ?> colActuatorId;

    /**
     * The column <code>public.TASKINGCAPABILITIES.THING_ID</code>.
     */
    public final TableField<Record, ?> colThingId;

    private final transient PluginActuation pluginActuation;
    private final transient PluginCoreModel pluginCoreModel;

    /**
     * Create a <code>public.TASKINGCAPABILITIES</code> table reference.
     *
     * @param idType The (SQL)DataType of the Id columns used in the actual
     * database.
     * @param pluginActuation the actuation plugin this table belongs to.
     * @param pluginCoreModel the coreModel plugin that this data model links
     * to.
     */
    public TableImpTaskingCapabilities(DataType<?> idType, DataType<?> idTypeActuator, DataType<?> idTypeThing, PluginActuation pluginActuation, PluginCoreModel pluginCoreModel) {
        super(idType, DSL.name("TASKINGCAPABILITIES"), null);
        this.pluginActuation = pluginActuation;
        this.pluginCoreModel = pluginCoreModel;
        colActuatorId = createField(DSL.name("ACTUATOR_ID"), idTypeActuator);
        colThingId = createField(DSL.name("THING_ID"), idTypeThing);
    }

    private TableImpTaskingCapabilities(Name alias, TableImpTaskingCapabilities aliased, PluginActuation pluginActuation, PluginCoreModel pluginCoreModel) {
        super(aliased.getIdType(), alias, aliased);
        this.pluginActuation = pluginActuation;
        this.pluginCoreModel = pluginCoreModel;
        colActuatorId = createField(DSL.name("ACTUATOR_ID"), aliased.colActuatorId.getDataType());
        colThingId = createField(DSL.name("THING_ID"), aliased.colThingId.getDataType());
    }

    @Override
    public void initRelations() {
        final TableCollection tables = getTables();
        TableImpThings tableThings = tables.getTableForClass(TableImpThings.class);
        registerRelation(new RelationOneToMany<>(pluginActuation.npThingTaskCap, this, tableThings)
                .setSourceFieldAccessor(TableImpTaskingCapabilities::getThingId)
                .setTargetFieldAccessor(TableImpThings::getId)
        );
        TableImpActuators tableActuators = tables.getTableForClass(TableImpActuators.class);
        registerRelation(new RelationOneToMany<>(pluginActuation.npActuatorTaskCap, this, tableActuators)
                .setSourceFieldAccessor(TableImpTaskingCapabilities::getActuatorId)
                .setTargetFieldAccessor(TableImpActuators::getId)
        );
        final TableImpTasks tableTasks = tables.getTableForClass(TableImpTasks.class);
        registerRelation(new RelationOneToMany<>(pluginActuation.npTasksTaskCap, this, tableTasks)
                .setSourceFieldAccessor(TableImpTaskingCapabilities::getId)
                .setTargetFieldAccessor(TableImpTasks::getTaskingCapabilityId)
        );

        // We add the relation to us to the Things table.
        final TableImpThings thingsTable = tables.getTableForClass(TableImpThings.class);
        final TableImpTaskingCapabilities tableTaskingCaps = tables.getTableForClass(TableImpTaskingCapabilities.class);
        thingsTable.registerRelation(new RelationOneToMany<>(pluginActuation.npTaskingCapabilitiesThing, thingsTable, tableTaskingCaps)
                .setSourceFieldAccessor(TableImpThings::getId)
                .setTargetFieldAccessor(TableImpTaskingCapabilities::getThingId)
        );

    }

    @Override
    public void initProperties(final EntityFactories entityFactories) {
        final TableCollection tables = getTables();
        pfReg.addEntryId(TableImpTaskingCapabilities::getId);
        pfReg.addEntryString(pluginCoreModel.epName, table -> table.colName);
        pfReg.addEntryString(pluginCoreModel.epDescription, table -> table.colDescription);
        pfReg.addEntryMap(ModelRegistry.EP_PROPERTIES, table -> table.colProperties);
        pfReg.addEntryMap(pluginActuation.epTaskingParameters, table -> table.colTaskingParameters);
        pfReg.addEntry(pluginActuation.npActuatorTaskCap, TableImpTaskingCapabilities::getActuatorId);
        pfReg.addEntry(pluginActuation.npThingTaskCap, TableImpTaskingCapabilities::getThingId);
        pfReg.addEntry(pluginActuation.npTasksTaskCap, TableImpTaskingCapabilities::getId);

        // We register a navigationProperty on the Things table.
        TableImpThings thingsTable = tables.getTableForClass(TableImpThings.class);
        thingsTable.getPropertyFieldRegistry()
                .addEntry(pluginActuation.npTaskingCapabilitiesThing, TableImpThings::getId);
    }

    @Override
    public EntityType getEntityType() {
        return pluginActuation.etTaskingCapability;
    }

    @Override
    public TableField<Record, ?> getId() {
        return colId;
    }

    public TableField<Record, ?> getActuatorId() {
        return colActuatorId;
    }

    public TableField<Record, ?> getThingId() {
        return colThingId;
    }

    @Override
    public TableImpTaskingCapabilities as(Name alias) {
        return new TableImpTaskingCapabilities(alias, this, pluginActuation, pluginCoreModel).initCustomFields();
    }

    @Override
    public TableImpTaskingCapabilities getThis() {
        return this;
    }

}
