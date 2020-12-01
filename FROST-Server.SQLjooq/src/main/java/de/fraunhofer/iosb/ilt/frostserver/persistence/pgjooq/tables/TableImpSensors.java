package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import org.jooq.DataType;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;

public class TableImpSensors<J extends Comparable> extends StaTableAbstract<J, TableImpSensors<J>> {

    private static final long serialVersionUID = 1850108682;

    /**
     * The column <code>public.SENSORS.DESCRIPTION</code>.
     */
    public final TableField<Record, String> colDescription = createField(DSL.name("DESCRIPTION"), SQLDataType.CLOB, this);

    /**
     * The column <code>public.SENSORS.ENCODING_TYPE</code>.
     */
    public final TableField<Record, String> colEncodingType = createField(DSL.name("ENCODING_TYPE"), SQLDataType.CLOB, this);

    /**
     * The column <code>public.SENSORS.METADATA</code>.
     */
    public final TableField<Record, String> colMetadata = createField(DSL.name("METADATA"), SQLDataType.CLOB, this);

    /**
     * The column <code>public.SENSORS.NAME</code>.
     */
    public final TableField<Record, String> colName = createField(DSL.name("NAME"), SQLDataType.CLOB.defaultValue(DSL.field("'no name'::text", SQLDataType.CLOB)), this);

    /**
     * The column <code>public.SENSORS.PROPERTIES</code>.
     */
    public final TableField<Record, JsonValue> colProperties = createField(DSL.name("PROPERTIES"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());

    /**
     * The column <code>public.SENSORS.ID</code>.
     */
    public final TableField<Record, J> colId = createField(DSL.name("ID"), getIdType(), this);

    /**
     * Create a <code>public.SENSORS</code> table reference
     */
    public TableImpSensors(DataType<J> idType) {
        super(idType, DSL.name("SENSORS"), null);
    }

    private TableImpSensors(Name alias, TableImpSensors<J> aliased) {
        super(aliased.getIdType(), alias, aliased);
    }

    @Override
    public void initRelations() {
        final TableCollection<J> tables = getTables();
        final ModelRegistry modelRegistry = getModelRegistry();
        TableImpDatastreams<J> tableDs = tables.getTableForClass(TableImpDatastreams.class);
        registerRelation(new RelationOneToMany<>(this, tableDs, modelRegistry.DATASTREAM, true)
                .setSourceFieldAccessor(TableImpSensors::getId)
                .setTargetFieldAccessor(TableImpDatastreams::getSensorId)
        );
        final TableImpMultiDatastreams<J> tableMds = tables.getTableForClass(TableImpMultiDatastreams.class);
        registerRelation(new RelationOneToMany<>(this, tableMds, modelRegistry.MULTI_DATASTREAM, true)
                .setSourceFieldAccessor(TableImpSensors::getId)
                .setTargetFieldAccessor(TableImpMultiDatastreams::getSensorId)
        );
    }

    @Override
    public void initProperties(final EntityFactories<J> entityFactories) {
        final ModelRegistry modelRegistry = getModelRegistry();
        final IdManager idManager = entityFactories.getIdManager();
        pfReg.addEntryId(idManager, TableImpSensors::getId);
        pfReg.addEntryString(modelRegistry.EP_NAME, table -> table.colName);
        pfReg.addEntryString(modelRegistry.EP_DESCRIPTION, table -> table.colDescription);
        pfReg.addEntryString(modelRegistry.EP_ENCODINGTYPE, table -> table.colEncodingType);
        pfReg.addEntryString(modelRegistry.EP_METADATA, table -> table.colMetadata);
        pfReg.addEntryMap(modelRegistry.EP_PROPERTIES, table -> table.colProperties);
        pfReg.addEntry(modelRegistry.NP_DATASTREAMS, TableImpSensors::getId, idManager);
        pfReg.addEntry(modelRegistry.NP_MULTIDATASTREAMS, TableImpSensors::getId, idManager);
    }

    @Override
    public EntityType getEntityType() {
        final ModelRegistry modelRegistry = getModelRegistry();
        return modelRegistry.SENSOR;
    }

    @Override
    public TableField<Record, J> getId() {
        return colId;
    }

    @Override
    public TableImpSensors<J> as(Name alias) {
        return new TableImpSensors<>(alias, this);
    }

    @Override
    public TableImpSensors<J> as(String alias) {
        return new TableImpSensors<>(DSL.name(alias), this);
    }

    @Override
    public TableImpSensors<J> getThis() {
        return this;
    }

}
