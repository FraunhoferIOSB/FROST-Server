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
import de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel.PluginCoreModel;
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
    private final PluginCoreModel pluginCoreModel;

    /**
     * Create a <code>public.ACTUATORS</code> table reference.
     *
     * @param idType The (SQL)DataType of the Id columns used in the actual
     * database.
     * @param pluginActuation the actuation plugin this table belongs to.
     * @param pluginCoreModel the coreModel plugin that this data model links
     * to.
     */
    public TableImpActuators(DataType<J> idType, PluginActuation pluginActuation, PluginCoreModel pluginCoreModel) {
        super(idType, DSL.name("ACTUATORS"), null);
        this.pluginActuation = pluginActuation;
        this.pluginCoreModel = pluginCoreModel;
    }

    private TableImpActuators(Name alias, TableImpActuators<J> aliased, PluginActuation pluginActuation, PluginCoreModel pluginCoreModel) {
        super(aliased.getIdType(), alias, aliased);
        this.pluginActuation = pluginActuation;
        this.pluginCoreModel = pluginCoreModel;
    }

    @Override
    public void initRelations() {
        final TableCollection<J> tables = getTables();
        final TableImpTaskingCapabilities<J> tableTaskCaps = tables.getTableForClass(TableImpTaskingCapabilities.class);
        registerRelation(new RelationOneToMany<>(this, tableTaskCaps, pluginActuation.etTaskingCapability, true)
                .setSourceFieldAccessor(TableImpActuators::getId)
                .setTargetFieldAccessor(TableImpTaskingCapabilities::getActuatorId)
        );
    }

    @Override
    public void initProperties(final EntityFactories<J> entityFactories) {
        final IdManager idManager = entityFactories.getIdManager();
        pfReg.addEntryId(idManager, TableImpActuators::getId);
        pfReg.addEntryString(pluginCoreModel.epName, table -> table.colName);
        pfReg.addEntryString(pluginCoreModel.epDescription, table -> table.colDescription);
        pfReg.addEntryString(ModelRegistry.EP_ENCODINGTYPE, table -> table.colEncodingType);
        pfReg.addEntryString(pluginCoreModel.epMetadata, table -> table.colMetadata);
        pfReg.addEntryMap(ModelRegistry.EP_PROPERTIES, table -> table.colProperties);
        pfReg.addEntry(pluginActuation.npTaskingCapabilitiesActuator, TableImpActuators::getId, idManager);
    }

    @Override
    public EntityType getEntityType() {
        return pluginActuation.etActuator;
    }

    @Override
    public TableField<Record, J> getId() {
        return colId;
    }

    @Override
    public TableImpActuators<J> as(Name alias) {
        return new TableImpActuators<>(alias, this, pluginActuation, pluginCoreModel).initCustomFields();
    }

    @Override
    public TableImpActuators<J> getThis() {
        return this;
    }

}
