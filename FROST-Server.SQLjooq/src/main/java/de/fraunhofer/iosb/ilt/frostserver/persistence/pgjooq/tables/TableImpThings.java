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
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationManyToMany;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationOneToMany;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
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

    private static TableImpThings INSTANCE;
    private static DataType INSTANCE_ID_TYPE;

    public static <J extends Comparable> TableImpThings<J> getInstance(DataType<J> idType) {
        if (INSTANCE == null) {
            INSTANCE_ID_TYPE = idType;
            INSTANCE = new TableImpThings(INSTANCE_ID_TYPE);
            return INSTANCE;
        }
        if (INSTANCE_ID_TYPE.equals(idType)) {
            return INSTANCE;
        }
        return new TableImpThings<>(idType);
    }

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
    private TableImpThings(DataType<J> idType) {
        super(idType, DSL.name("THINGS"), null);
    }

    private TableImpThings(Name alias, TableImpThings<J> aliased) {
        super(aliased.getIdType(), alias, aliased);
    }

    @Override
    public void initRelations() {
        registerRelation(new RelationOneToMany<>(this, TableImpDatastreams.getInstance(getIdType()), EntityType.DATASTREAM, true)
                        .setSourceFieldAccessor(TableImpThings::getId)
                        .setTargetFieldAccessor(TableImpDatastreams::getThingId)
        );

        registerRelation(new RelationOneToMany<>(this, TableImpMultiDatastreams.getInstance(getIdType()), EntityType.MULTI_DATASTREAM, true)
                        .setSourceFieldAccessor(TableImpThings::getId)
                        .setTargetFieldAccessor(TableImpMultiDatastreams::getThingId)
        );


        registerRelation(new RelationOneToMany<>(this, TableImpHistLocations.getInstance(getIdType()), EntityType.HISTORICAL_LOCATION, true)
                        .setSourceFieldAccessor(TableImpThings::getId)
                        .setTargetFieldAccessor(TableImpHistLocations::getThingId)
        );

        registerRelation(new RelationManyToMany<>(this, TableImpThingsLocations.getInstance(getIdType()), TableImpLocations.getInstance(getIdType()), EntityType.LOCATION)
                .setSourceFieldAcc(TableImpThings::getId)
                .setSourceLinkFieldAcc(TableImpThingsLocations::getThingId)
                .setTargetLinkFieldAcc(TableImpThingsLocations::getLocationId)
                .setTargetFieldAcc(TableImpLocations::getId)
        );
    }

    @Override
    public void initProperties(final EntityFactories<J> entityFactories) {
        final IdManager idManager = entityFactories.idManager;
        pfReg.addEntryId(idManager, TableImpThings::getId);
        pfReg.addEntryString(EntityPropertyMain.NAME, table -> table.colName);
        pfReg.addEntryString(EntityPropertyMain.DESCRIPTION, table -> table.colDescription);
        pfReg.addEntryMap(EntityPropertyMain.PROPERTIES, table -> table.colProperties);
        pfReg.addEntry(NavigationPropertyMain.DATASTREAMS, TableImpThings::getId, idManager);
        pfReg.addEntry(NavigationPropertyMain.HISTORICALLOCATIONS, TableImpThings::getId, idManager);
        pfReg.addEntry(NavigationPropertyMain.LOCATIONS, TableImpThings::getId, idManager);
        pfReg.addEntry(NavigationPropertyMain.MULTIDATASTREAMS, TableImpThings::getId, idManager);
    }

    @Override
    protected void updateNavigationPropertySet(Entity thing, EntitySet linkedSet, PostgresPersistenceManager<J> pm, boolean forInsert) throws IncompleteEntityException, NoSuchEntityException {
        EntityType linkedEntityType = linkedSet.getEntityType();
        if (linkedEntityType.equals(EntityType.LOCATION)) {
            J thingId = (J) thing.getId().getValue();
            DSLContext dslContext = pm.getDslContext();
            EntityFactories<J> entityFactories = pm.getEntityFactories();
            TableImpThingsLocations<J> ttl = TableImpThingsLocations.getInstance(getIdType());

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
                TableImpHistLocations<J> qhl = TableImpHistLocations.getInstance(getIdType());
                Record1<J> newHistLoc = dslContext.insertInto(qhl)
                        .set(qhl.getThingId(), thingId)
                        .set(qhl.time, OffsetDateTime.now(Constants.UTC))
                        .returningResult(qhl.getId())
                        .fetchOne();
                J histLocationId = newHistLoc.component1();
                LOGGER.debug(EntityFactories.CREATED_HL, histLocationId);

                // Link the locations to the new HL
                TableImpLocationsHistLocations<J> qlhl = TableImpLocationsHistLocations.getInstance(getIdType());
                for (J locId : locationIds) {
                    dslContext.insertInto(qlhl)
                            .set(qlhl.getHistLocationId(), histLocationId)
                            .set(qlhl.getLocationId(), locId)
                            .execute();
                    LOGGER.debug(EntityFactories.LINKED_L_TO_HL, locId, histLocationId);
                }

                // Send a message about the creation of a new HL
                Entity newHl = pm.get(EntityType.HISTORICAL_LOCATION, pm.getIdManager().fromObject(histLocationId));
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
        return EntityType.THING;
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
