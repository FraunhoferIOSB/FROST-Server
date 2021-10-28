package de.fraunhofer.iosb.ilt.frostserver.plugin.coremodel;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
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
    public final TableField<Record, JsonValue> colProperties = createField(DSL.name(NAME_COL_PROPERTIES), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());

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
        super(idType, DSL.name(NAME_TABLE), null);
        this.pluginCoreModel = pluginCoreModel;
    }

    private TableImpSensors(Name alias, TableImpSensors aliased, PluginCoreModel pluginCoreModel) {
        super(aliased.getIdType(), alias, aliased);
        this.pluginCoreModel = pluginCoreModel;
    }

    @Override
    public void initRelations() {
        final TableCollection tables = getTables();
        TableImpDatastreams tableDs = tables.getTableForClass(TableImpDatastreams.class);
        registerRelation(new RelationOneToMany<>(pluginCoreModel.npDatastreamsSensor, this, tableDs)
                .setSourceFieldAccessor(TableImpSensors::getId)
                .setTargetFieldAccessor(TableImpDatastreams::getSensorId)
        );
    }

    @Override
    public void initProperties(final EntityFactories entityFactories) {
        pfReg.addEntryId(entityFactories, TableImpSensors::getId);
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
    public TableField<Record, ?> getId() {
        return colId;
    }

    @Override
    public TableImpSensors as(Name alias) {
        return new TableImpSensors(alias, this, pluginCoreModel).initCustomFields();
    }

    @Override
    public TableImpSensors getThis() {
        return this;
    }

}
