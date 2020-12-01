package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationManyToManyOrdered;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
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
        final TableImpMultiDatastreamsObsProperties<J> tableDsOp = tables.getTableForClass(TableImpMultiDatastreamsObsProperties.class);
        final TableImpMultiDatastreams<J> tableMds = tables.getTableForClass(TableImpMultiDatastreams.class);
        registerRelation(new RelationManyToManyOrdered<>(this, tableDsOp, tableMds, modelRegistry.MULTI_DATASTREAM)
                .setOrderFieldAcc((TableImpMultiDatastreamsObsProperties<J> table) -> table.colRank)
                .setSourceFieldAcc(TableImpObsProperties::getId)
                .setSourceLinkFieldAcc(TableImpMultiDatastreamsObsProperties::getObsPropertyId)
                .setTargetLinkFieldAcc(TableImpMultiDatastreamsObsProperties::getMultiDatastreamId)
                .setTargetFieldAcc(TableImpMultiDatastreams::getId)
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
        pfReg.addEntry(modelRegistry.NP_MULTIDATASTREAMS, TableImpObsProperties::getId, idManager);
    }

    @Override
    public boolean insertIntoDatabase(PostgresPersistenceManager<J> pm, Entity entity) throws NoSuchEntityException, IncompleteEntityException {
        ModelRegistry modelRegistry = getModelRegistry();
        EntitySet mds = entity.getProperty(modelRegistry.NP_MULTIDATASTREAMS);
        if (mds != null && !mds.isEmpty()) {
            throw new IllegalArgumentException("Adding a MultiDatastream to an ObservedProperty is not allowed.");
        }
        return super.insertIntoDatabase(pm, entity);
    }

    @Override
    public EntityChangedMessage updateInDatabase(PostgresPersistenceManager<J> pm, Entity entity, J entityId) throws NoSuchEntityException, IncompleteEntityException {
        ModelRegistry modelRegistry = getModelRegistry();
        EntitySet mds = entity.getProperty(modelRegistry.NP_MULTIDATASTREAMS);
        if (mds != null && !mds.isEmpty()) {
            throw new IllegalArgumentException("Adding a MultiDatastream to an ObservedProperty is not allowed.");
        }
        return super.updateInDatabase(pm, entity, entityId);
    }

    @Override
    public void delete(PostgresPersistenceManager<J> pm, J entityId) throws NoSuchEntityException {
        // First delete all MultiDatastreams that link to this ObservedProperty.
        // Must happen first, since the links in the link table would be gone otherwise.
        final TableCollection<J> tables = getTables();
        TableImpMultiDatastreams<J> tMd = tables.getTableForClass(TableImpMultiDatastreams.class);
        TableImpMultiDatastreamsObsProperties<J> tMdOp = tables.getTableForClass(TableImpMultiDatastreamsObsProperties.class);
        long count = pm.getDslContext()
                .delete(tMd)
                .where(
                        tMd.getId().in(
                                DSL.select(tMdOp.getMultiDatastreamId()).from(tMdOp).where(tMdOp.getObsPropertyId().eq(entityId))
                        ))
                .execute();
        LOGGER.debug("Deleted {} MultiDatastreams.", count);
        // Now delete the OP itself.
        super.delete(pm, entityId);
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
