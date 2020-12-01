package de.fraunhofer.iosb.ilt.frostserver.plugin.actuation;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTableAbstract;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import org.jooq.DataType;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;

public class TableImpActuators<J extends Comparable> extends StaTableAbstract<J, TableImpActuators<J>> {

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
    public final TableField<Record, String> colMetadata = createField(DSL.name("METADATA"), SQLDataType.CLOB, this);

    /**
     * The column <code>public.ACTUATORS.EP_NAME</code>.
     */
    public final TableField<Record, String> colName = createField(DSL.name("NAME"), SQLDataType.CLOB.defaultValue(DSL.field("'no name'::text", SQLDataType.CLOB)), this);

    /**
     * The column <code>public.ACTUATORS.EP_PROPERTIES</code>.
     */
    public final TableField<Record, JsonValue> colProperties = createField(DSL.name("PROPERTIES"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());

    /**
     * The column <code>public.ACTUATORS.EP_ID</code>.
     */
    public final TableField<Record, J> colId = createField(DSL.name("ID"), getIdType(), this);

    private final PluginActuation pluginActuation;

    /**
     * Create a <code>public.ACTUATORS</code> table reference
     */
    public TableImpActuators(DataType<J> idType, PluginActuation pluginActuation) {
        super(idType, DSL.name("ACTUATORS"), null);
        this.pluginActuation = pluginActuation;
    }

    private TableImpActuators(Name alias, TableImpActuators<J> aliased, PluginActuation pluginActuation) {
        super(aliased.getIdType(), alias, aliased);
        this.pluginActuation = pluginActuation;
    }

    @Override
    public void initRelations() {
        final TableCollection<J> tables = getTables();
        final TableImpTaskingCapabilities<J> tableTaskCaps = tables.getTableForClass(TableImpTaskingCapabilities.class);
        registerRelation(new RelationOneToMany<>(this, tableTaskCaps, pluginActuation.TASKING_CAPABILITY, true)
                .setSourceFieldAccessor(TableImpActuators::getId)
                .setTargetFieldAccessor(TableImpTaskingCapabilities::getActuatorId)
        );
    }

    @Override
    public void initProperties(final EntityFactories<J> entityFactories) {
        final ModelRegistry modelRegistry = getModelRegistry();
        final IdManager idManager = entityFactories.getIdManager();
        pfReg.addEntryId(idManager, TableImpActuators::getId);
        pfReg.addEntryString(modelRegistry.EP_NAME, table -> table.colName);
        pfReg.addEntryString(modelRegistry.EP_DESCRIPTION, table -> table.colDescription);
        pfReg.addEntryString(ModelRegistry.EP_ENCODINGTYPE, table -> table.colEncodingType);
        pfReg.addEntryString(modelRegistry.EP_METADATA, table -> table.colMetadata);
        pfReg.addEntryMap(modelRegistry.EP_PROPERTIES, table -> table.colProperties);
        pfReg.addEntry(pluginActuation.NP_TASKINGCAPABILITIES, TableImpActuators::getId, idManager);
    }

    @Override
    public EntityType getEntityType() {
        return pluginActuation.ACTUATOR;
    }

    @Override
    public TableField<Record, J> getId() {
        return colId;
    }

    @Override
    public TableImpActuators<J> as(Name alias) {
        return new TableImpActuators<>(alias, this, pluginActuation);
    }

    @Override
    public TableImpActuators<J> as(String alias) {
        return new TableImpActuators<>(DSL.name(alias), this, pluginActuation);
    }

    @Override
    public TableImpActuators<J> getThis() {
        return this;
    }

}
