package de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableImpObsProperties<J extends Comparable> extends StaTableAbstract<J, TableImpObsProperties<J>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TableImpObsProperties.class.getName());

    private static final long serialVersionUID = -1873692390;

    /**
     * The column <code>public.OBS_PROPERTIES.NAME</code>.
     */
    public final TableField<Record, String> colName = createField(DSL.name("NAME"), SQLDataType.CLOB, this);

    /**
     * The column <code>public.OBS_PROPERTIES.DEFINITION</code>.
     */
    public final TableField<Record, String> colDefinition = createField(DSL.name("DEFINITION"), SQLDataType.CLOB, this);

    /**
     * The column <code>public.OBS_PROPERTIES.DESCRIPTION</code>.
     */
    public final TableField<Record, String> colDescription = createField(DSL.name("DESCRIPTION"), SQLDataType.CLOB, this);

    /**
     * The column <code>public.OBS_PROPERTIES.PROPERTIES</code>.
     */
    public final TableField<Record, JsonValue> colProperties = createField(DSL.name("PROPERTIES"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());

    /**
     * The column <code>public.OBS_PROPERTIES.ID</code>.
     */
    public final TableField<Record, J> colId = createField(DSL.name("ID"), getIdType(), this);

    private final PluginCoreModel pluginCoreModel;

    /**
     * Create a <code>public.OBS_PROPERTIES</code> table reference.
     *
     * @param idType The (SQL)DataType of the Id columns used in the actual
     * database.
     * @param pluginCoreModel the coreModel plugin this table belongs to.
     */
    public TableImpObsProperties(DataType<J> idType, PluginCoreModel pluginCoreModel) {
        super(idType, DSL.name("OBS_PROPERTIES"), null);
        this.pluginCoreModel = pluginCoreModel;
    }

    private TableImpObsProperties(Name alias, TableImpObsProperties<J> aliased, PluginCoreModel pluginCoreModel) {
        super(aliased.getIdType(), alias, aliased);
        this.pluginCoreModel = pluginCoreModel;
    }

    @Override
    public void initRelations() {
        final TableCollection<J> tables = getTables();
        final TableImpDatastreams<J> tableDs = tables.getTableForClass(TableImpDatastreams.class);
        registerRelation(new RelationOneToMany<>(this, tableDs, pluginCoreModel.DATASTREAM, true)
                .setSourceFieldAccessor(TableImpObsProperties::getId)
                .setTargetFieldAccessor(TableImpDatastreams::getObsPropertyId)
        );
    }

    @Override
    public void initProperties(final EntityFactories<J> entityFactories) {
        final IdManager idManager = entityFactories.getIdManager();
        pfReg.addEntryId(idManager, TableImpObsProperties::getId);
        pfReg.addEntryString(pluginCoreModel.EP_DEFINITION, table -> table.colDefinition);
        pfReg.addEntryString(pluginCoreModel.EP_DESCRIPTION, table -> table.colDescription);
        pfReg.addEntryString(pluginCoreModel.EP_NAME, table -> table.colName);
        pfReg.addEntryMap(ModelRegistry.EP_PROPERTIES, table -> table.colProperties);
        pfReg.addEntry(pluginCoreModel.NP_DATASTREAMS, TableImpObsProperties::getId, idManager);
    }

    @Override
    public EntityType getEntityType() {
        return pluginCoreModel.OBSERVED_PROPERTY;
    }

    @Override
    public TableField<Record, J> getId() {
        return colId;
    }

    @Override
    public TableImpObsProperties<J> as(Name alias) {
        return new TableImpObsProperties<>(alias, this, pluginCoreModel).initCustomFields();
    }

    @Override
    public TableImpObsProperties<J> as(String alias) {
        return new TableImpObsProperties<>(DSL.name(alias), this, pluginCoreModel).initCustomFields();
    }

    @Override
    public TableImpObsProperties<J> getThis() {
        return this;
    }

}
