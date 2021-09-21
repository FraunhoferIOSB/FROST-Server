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

public class TableImpObsProperties<J extends Comparable> extends StaTableAbstract<J, TableImpObsProperties<J>> {

    public static final String NAME_TABLE = "OBS_PROPERTIES";
    public static final String NAME_COL_DEFINITION = "DEFINITION";
    public static final String NAME_COL_DESCRIPTION = "DESCRIPTION";
    public static final String NAME_COL_ID = "ID";
    public static final String NAME_COL_NAME = "NAME";
    public static final String NAME_COL_PROPERTIES = "PROPERTIES";

    private static final long serialVersionUID = -1873692390;

    /**
     * The column <code>public.OBS_PROPERTIES.NAME</code>.
     */
    public final TableField<Record, String> colName = createField(DSL.name(NAME_COL_NAME), SQLDataType.CLOB, this);

    /**
     * The column <code>public.OBS_PROPERTIES.DEFINITION</code>.
     */
    public final TableField<Record, String> colDefinition = createField(DSL.name(NAME_COL_DEFINITION), SQLDataType.CLOB, this);

    /**
     * The column <code>public.OBS_PROPERTIES.DESCRIPTION</code>.
     */
    public final TableField<Record, String> colDescription = createField(DSL.name(NAME_COL_DESCRIPTION), SQLDataType.CLOB, this);

    /**
     * The column <code>public.OBS_PROPERTIES.PROPERTIES</code>.
     */
    public final TableField<Record, JsonValue> colProperties = createField(DSL.name(NAME_COL_PROPERTIES), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());

    /**
     * The column <code>public.OBS_PROPERTIES.ID</code>.
     */
    public final TableField<Record, J> colId = createField(DSL.name(NAME_COL_ID), getIdType(), this);

    private final transient PluginCoreModel pluginCoreModel;

    /**
     * Create a <code>public.OBS_PROPERTIES</code> table reference.
     *
     * @param idType The (SQL)DataType of the Id columns used in the actual
     * database.
     * @param pluginCoreModel the coreModel plugin this table belongs to.
     */
    public TableImpObsProperties(DataType<J> idType, PluginCoreModel pluginCoreModel) {
        super(idType, DSL.name(NAME_TABLE), null);
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
        registerRelation(new RelationOneToMany<>(pluginCoreModel.npDatastreamsObsProp, this, tableDs)
                .setSourceFieldAccessor(TableImpObsProperties::getId)
                .setTargetFieldAccessor(TableImpDatastreams::getObsPropertyId)
        );
    }

    @Override
    public void initProperties(final EntityFactories<J> entityFactories) {
        final IdManager idManager = entityFactories.getIdManager();
        pfReg.addEntryId(idManager, TableImpObsProperties::getId);
        pfReg.addEntryString(pluginCoreModel.epDefinition, table -> table.colDefinition);
        pfReg.addEntryString(pluginCoreModel.epDescription, table -> table.colDescription);
        pfReg.addEntryString(pluginCoreModel.epName, table -> table.colName);
        pfReg.addEntryMap(ModelRegistry.EP_PROPERTIES, table -> table.colProperties);
        pfReg.addEntry(pluginCoreModel.npDatastreamsObsProp, TableImpObsProperties::getId);
    }

    @Override
    public EntityType getEntityType() {
        return pluginCoreModel.etObservedProperty;
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
    public TableImpObsProperties<J> getThis() {
        return this;
    }

}
