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

    /**
     * Create a <code>public.OBS_PROPERTIES</code> table reference
     */
    public TableImpObsProperties(DataType<J> idType) {
        super(idType, DSL.name("OBS_PROPERTIES"), null);
    }

    private TableImpObsProperties(Name alias, TableImpObsProperties<J> aliased) {
        super(aliased.getIdType(), alias, aliased);
    }

    @Override
    public void initRelations() {
        final TableCollection<J> tables = getTables();
        final ModelRegistry modelRegistry = getModelRegistry();
        final TableImpDatastreams<J> tableDs = tables.getTableForClass(TableImpDatastreams.class);
        registerRelation(new RelationOneToMany<>(this, tableDs, modelRegistry.DATASTREAM, true)
                .setSourceFieldAccessor(TableImpObsProperties::getId)
                .setTargetFieldAccessor(TableImpDatastreams::getObsPropertyId)
        );
    }

    @Override
    public void initProperties(final EntityFactories<J> entityFactories) {
        ModelRegistry modelRegistry = getModelRegistry();
        final IdManager idManager = entityFactories.getIdManager();
        pfReg.addEntryId(idManager, TableImpObsProperties::getId);
        pfReg.addEntryString(modelRegistry.EP_DEFINITION, table -> table.colDefinition);
        pfReg.addEntryString(modelRegistry.EP_DESCRIPTION, table -> table.colDescription);
        pfReg.addEntryString(modelRegistry.EP_NAME, table -> table.colName);
        pfReg.addEntryMap(modelRegistry.EP_PROPERTIES, table -> table.colProperties);
        pfReg.addEntry(modelRegistry.NP_DATASTREAMS, TableImpObsProperties::getId, idManager);
    }

    @Override
    public EntityType getEntityType() {
        ModelRegistry modelRegistry = getModelRegistry();
        return modelRegistry.OBSERVED_PROPERTY;
    }

    @Override
    public TableField<Record, J> getId() {
        return colId;
    }

    @Override
    public TableImpObsProperties<J> as(Name alias) {
        return new TableImpObsProperties<>(alias, this);
    }

    @Override
    public TableImpObsProperties<J> as(String alias) {
        return new TableImpObsProperties<>(DSL.name(alias), this);
    }

    @Override
    public TableImpObsProperties<J> getThis() {
        return this;
    }

}
