package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.persistence.IdManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationManyToManyOrdered;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
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

public class AbstractTableObsProperties<J extends Comparable> extends StaTableAbstract<J, AbstractTableObsProperties<J>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTableObsProperties.class.getName());

    private static final long serialVersionUID = -1873692390;

    private static AbstractTableObsProperties INSTANCE;
    private static DataType INSTANCE_ID_TYPE;

    public static <J extends Comparable> AbstractTableObsProperties<J> getInstance(DataType<J> idType) {
        if (INSTANCE == null) {
            INSTANCE_ID_TYPE = idType;
            INSTANCE = new AbstractTableObsProperties(INSTANCE_ID_TYPE);
            return INSTANCE;
        }
        if (INSTANCE_ID_TYPE.equals(idType)) {
            return INSTANCE;
        }
        return new AbstractTableObsProperties<>(idType);
    }

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
    private AbstractTableObsProperties(DataType<J> idType) {
        super(idType, DSL.name("OBS_PROPERTIES"), null);
    }

    private AbstractTableObsProperties(Name alias, AbstractTableObsProperties<J> aliased) {
        super(aliased.getIdType(), alias, aliased);
    }

    @Override
    public void initRelations() {
        final TableCollection<J> tables = getTables();
        registerRelation(
                new RelationOneToMany<>(this, AbstractTableDatastreams.getInstance(getIdType()), EntityType.DATASTREAM, true)
                        .setSourceFieldAccessor(AbstractTableObsProperties::getId)
                        .setTargetFieldAccessor(AbstractTableDatastreams::getObsPropertyId)
        );

        registerRelation(
                new RelationManyToManyOrdered<>(this, AbstractTableMultiDatastreamsObsProperties.getInstance(getIdType()), AbstractTableMultiDatastreams.getInstance(getIdType()), EntityType.MULTI_DATASTREAM)
                        .setOrderFieldAcc((AbstractTableMultiDatastreamsObsProperties<J> table) -> table.colRank)
                        .setSourceFieldAcc(AbstractTableObsProperties::getId)
                        .setSourceLinkFieldAcc(AbstractTableMultiDatastreamsObsProperties::getObsPropertyId)
                        .setTargetLinkFieldAcc(AbstractTableMultiDatastreamsObsProperties::getMultiDatastreamId)
                        .setTargetFieldAcc(AbstractTableMultiDatastreams::getId)
        );
    }

    @Override
    public void initProperties(final EntityFactories<J> entityFactories) {
        final IdManager idManager = entityFactories.idManager;
        pfReg.addEntryId(idManager, AbstractTableObsProperties::getId);
        pfReg.addEntryString(EntityPropertyMain.DEFINITION, table -> table.colDefinition);
        pfReg.addEntryString(EntityPropertyMain.DESCRIPTION, table -> table.colDescription);
        pfReg.addEntryString(EntityPropertyMain.NAME, table -> table.colName);
        pfReg.addEntryMap(EntityPropertyMain.PROPERTIES, table -> table.colProperties);
        pfReg.addEntry(NavigationPropertyMain.DATASTREAMS, AbstractTableObsProperties::getId, idManager);
        pfReg.addEntry(NavigationPropertyMain.MULTIDATASTREAMS, AbstractTableObsProperties::getId, idManager);
    }

    @Override
    public boolean insertIntoDatabase(PostgresPersistenceManager<J> pm, Entity entity) throws NoSuchEntityException, IncompleteEntityException {
        EntitySet mds = entity.getProperty(NavigationPropertyMain.MULTIDATASTREAMS);
        if (mds != null && !mds.isEmpty()) {
            throw new IllegalArgumentException("Adding a MultiDatastream to an ObservedProperty is not allowed.");
        }
        return super.insertIntoDatabase(pm, entity);
    }

    @Override
    public EntityChangedMessage updateInDatabase(PostgresPersistenceManager<J> pm, Entity entity, J entityId) throws NoSuchEntityException, IncompleteEntityException {
        EntitySet mds = entity.getProperty(NavigationPropertyMain.MULTIDATASTREAMS);
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
        AbstractTableMultiDatastreams<J> tMd = AbstractTableMultiDatastreams.getInstance(getIdType());
        AbstractTableMultiDatastreamsObsProperties<J> tMdOp = AbstractTableMultiDatastreamsObsProperties.getInstance(getIdType());
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
        return EntityType.OBSERVED_PROPERTY;
    }

    @Override
    public TableField<Record, J> getId() {
        return colId;
    }

    @Override
    public AbstractTableObsProperties<J> as(Name alias) {
        return new AbstractTableObsProperties<>(alias, this);
    }

    @Override
    public AbstractTableObsProperties<J> as(String alias) {
        return new AbstractTableObsProperties<>(DSL.name(alias), this);
    }

    @Override
    public AbstractTableObsProperties<J> getThis() {
        return this;
    }

}
