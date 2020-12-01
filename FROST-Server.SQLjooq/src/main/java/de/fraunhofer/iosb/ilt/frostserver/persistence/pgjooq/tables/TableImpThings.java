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
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationManyToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import de.fraunhofer.iosb.ilt.frostserver.util.Constants;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableImpThings<J extends Comparable> extends StaTableAbstract<J, TableImpThings<J>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TableImpThings.class.getName());
    private static final long serialVersionUID = -729589982;

    /**
     * The column <code>public.THINGS.DESCRIPTION</code>.
     */
    public final TableField<Record, String> colDescription = createField(DSL.name("DESCRIPTION"), SQLDataType.CLOB, this);

    /**
     * The column <code>public.THINGS.PROPERTIES</code>.
     */
    public final TableField<Record, JsonValue> colProperties = createField(DSL.name("PROPERTIES"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());

    /**
     * The column <code>public.THINGS.NAME</code>.
     */
    public final TableField<Record, String> colName = createField(DSL.name("NAME"), SQLDataType.CLOB.defaultValue(DSL.field("'no name'::text", SQLDataType.CLOB)), this);

    /**
     * The column <code>public.THINGS.ID</code>.
     */
    public final TableField<Record, J> colId = createField(DSL.name("ID"), getIdType(), this);

    /**
     * Create a <code>public.THINGS</code> table reference
     */
    public TableImpThings(DataType<J> idType) {
        super(idType, DSL.name("THINGS"), null);
    }

    private TableImpThings(Name alias, TableImpThings<J> aliased) {
        super(aliased.getIdType(), alias, aliased);
    }

    @Override
    public void initRelations() {
        final TableCollection<J> tables = getTables();
        final ModelRegistry modelRegistry = getModelRegistry();
        final TableImpDatastreams<J> tableDs = tables.getTableForClass(TableImpDatastreams.class);
        registerRelation(new RelationOneToMany<>(this, tableDs, modelRegistry.DATASTREAM, true)
                .setSourceFieldAccessor(TableImpThings::getId)
                .setTargetFieldAccessor(TableImpDatastreams::getThingId)
        );
        final TableImpMultiDatastreams<J> tableMds = tables.getTableForClass(TableImpMultiDatastreams.class);
        registerRelation(new RelationOneToMany<>(this, tableMds, modelRegistry.MULTI_DATASTREAM, true)
                .setSourceFieldAccessor(TableImpThings::getId)
                .setTargetFieldAccessor(TableImpMultiDatastreams::getThingId)
        );
        final TableImpHistLocations<J> tableHistLoc = tables.getTableForClass(TableImpHistLocations.class);
        registerRelation(new RelationOneToMany<>(this, tableHistLoc, modelRegistry.HISTORICAL_LOCATION, true)
                .setSourceFieldAccessor(TableImpThings::getId)
                .setTargetFieldAccessor(TableImpHistLocations::getThingId)
        );
        final TableImpThingsLocations<J> tableThingsLocs = tables.getTableForClass(TableImpThingsLocations.class);
        final TableImpLocations<J> tableLocs = tables.getTableForClass(TableImpLocations.class);
        registerRelation(new RelationManyToMany<>(this, tableThingsLocs, tableLocs, modelRegistry.LOCATION)
                .setSourceFieldAcc(TableImpThings::getId)
                .setSourceLinkFieldAcc(TableImpThingsLocations::getThingId)
                .setTargetLinkFieldAcc(TableImpThingsLocations::getLocationId)
                .setTargetFieldAcc(TableImpLocations::getId)
        );
    }

    @Override
    public void initProperties(final EntityFactories<J> entityFactories) {
        final ModelRegistry modelRegistry = getModelRegistry();
        final IdManager idManager = entityFactories.getIdManager();
        pfReg.addEntryId(idManager, TableImpThings::getId);
        pfReg.addEntryString(modelRegistry.EP_NAME, table -> table.colName);
        pfReg.addEntryString(modelRegistry.EP_DESCRIPTION, table -> table.colDescription);
        pfReg.addEntryMap(modelRegistry.EP_PROPERTIES, table -> table.colProperties);
        pfReg.addEntry(modelRegistry.NP_DATASTREAMS, TableImpThings::getId, idManager);
        pfReg.addEntry(modelRegistry.NP_HISTORICALLOCATIONS, TableImpThings::getId, idManager);
        pfReg.addEntry(modelRegistry.NP_LOCATIONS, TableImpThings::getId, idManager);
        pfReg.addEntry(modelRegistry.NP_MULTIDATASTREAMS, TableImpThings::getId, idManager);
    }

    @Override
    protected void updateNavigationPropertySet(Entity thing, EntitySet linkedSet, PostgresPersistenceManager<J> pm, boolean forInsert) throws IncompleteEntityException, NoSuchEntityException {
        final ModelRegistry modelRegistry = getModelRegistry();
        EntityType linkedEntityType = linkedSet.getEntityType();
        if (linkedEntityType.equals(modelRegistry.LOCATION)) {
            final TableCollection<J> tables = getTables();
            J thingId = (J) thing.getId().getValue();
            DSLContext dslContext = pm.getDslContext();
            EntityFactories<J> entityFactories = pm.getEntityFactories();
            TableImpThingsLocations<J> ttl = tables.getTableForClass(TableImpThingsLocations.class);

            if (!forInsert) {
                // Unlink old Locations from Thing.
                long count = dslContext.delete(ttl).where(ttl.getThingId().eq(thingId)).execute();
                LOGGER.debug(EntityFactories.UNLINKED_L_FROM_T, count, thingId);
            }

            // Maybe Create new Locations and link them to this Thing.
            List<J> locationIds = new ArrayList<>();
            for (Entity l : linkedSet) {
                if (forInsert) {
                    entityFactories.entityExistsOrCreate(pm, l);
                } else if (!entityFactories.entityExists(pm, l)) {
                    throw new NoSuchEntityException("Linked Location with no id.");
                }
                J lId = (J) l.getId().getValue();

                dslContext.insertInto(ttl)
                        .set(ttl.getThingId(), thingId)
                        .set(ttl.getLocationId(), lId)
                        .execute();
                LOGGER.debug(EntityFactories.LINKED_L_TO_T, lId, thingId);
                locationIds.add(lId);
            }

            // Now link the new locations also to a historicalLocation.
            if (!locationIds.isEmpty()) {
                // Insert a new HL into the DB
                TableImpHistLocations<J> qhl = tables.getTableForClass(TableImpHistLocations.class);
                Record1<J> newHistLoc = dslContext.insertInto(qhl)
                        .set(qhl.getThingId(), thingId)
                        .set(qhl.time, OffsetDateTime.now(Constants.UTC))
                        .returningResult(qhl.getId())
                        .fetchOne();
                J histLocationId = newHistLoc.component1();
                LOGGER.debug(EntityFactories.CREATED_HL, histLocationId);

                // Link the locations to the new HL
                TableImpLocationsHistLocations<J> qlhl = tables.getTableForClass(TableImpLocationsHistLocations.class);
                for (J locId : locationIds) {
                    dslContext.insertInto(qlhl)
                            .set(qlhl.getHistLocationId(), histLocationId)
                            .set(qlhl.getLocationId(), locId)
                            .execute();
                    LOGGER.debug(EntityFactories.LINKED_L_TO_HL, locId, histLocationId);
                }

                // Send a message about the creation of a new HL
                Entity newHl = pm.get(modelRegistry.HISTORICAL_LOCATION, pm.getIdManager().fromObject(histLocationId));
                newHl.setQuery(modelRegistry.getMessageQueryGenerator().getQueryFor(newHl.getEntityType()));
                pm.getEntityChangedMessages().add(
                        new EntityChangedMessage()
                                .setEventType(EntityChangedMessage.Type.CREATE)
                                .setEntity(newHl)
                );
            }
            return;
        }
        super.updateNavigationPropertySet(thing, linkedSet, pm, forInsert);
    }

    @Override
    public EntityType getEntityType() {
        final ModelRegistry modelRegistry = getModelRegistry();
        return modelRegistry.THING;
    }

    @Override
    public TableField<Record, J> getId() {
        return colId;
    }

    @Override
    public TableImpThings<J> as(Name alias) {
        return new TableImpThings<>(alias, this);
    }

    @Override
    public TableImpThings<J> as(String alias) {
        return new TableImpThings<>(DSL.name(alias), this);
    }

    @Override
    public TableImpThings<J> getThis() {
        return this;
    }

}
