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
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.PostGisGeometryBinding;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.JsonFieldFactory;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.RelationManyToMany;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTableAbstract.jsonFieldFromPath;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.ConverterRecordDeflt;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.NFP;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry.PropertyFields;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.Utils;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.Utils.getFieldOrNull;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyCustomSelect;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.util.Constants;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import java.time.OffsetDateTime;
import org.geolatte.geom.Geometry;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultDataType;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableImpLocations<J extends Comparable> extends StaTableAbstract<J, TableImpLocations<J>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TableImpLocations.class.getName());
    private static final long serialVersionUID = -806078255;
    public static final String TABLE_NAME = "LOCATIONS";

    /**
     * The column <code>public.LOCATIONS.DESCRIPTION</code>.
     */
    public final TableField<Record, String> colDescription = createField(DSL.name("DESCRIPTION"), SQLDataType.CLOB, this);

    /**
     * The column <code>public.LOCATIONS.ENCODING_TYPE</code>.
     */
    public final TableField<Record, String> colEncodingType = createField(DSL.name("ENCODING_TYPE"), SQLDataType.CLOB, this);

    /**
     * The column <code>public.LOCATIONS.LOCATION</code>.
     */
    public final TableField<Record, String> colLocation = createField(DSL.name("LOCATION"), SQLDataType.CLOB, this);

    /**
     * The column <code>public.LOCATIONS.GEOM</code>.
     */
    public final TableField<Record, Geometry> colGeom = createField(DSL.name("GEOM"), DefaultDataType.getDefaultDataType(TYPE_GEOMETRY), this, "", new PostGisGeometryBinding());

    /**
     * The column <code>public.LOCATIONS.NAME</code>.
     */
    public final TableField<Record, String> colName = createField(DSL.name("NAME"), SQLDataType.CLOB.defaultValue(DSL.field("'no name'::text", SQLDataType.CLOB)), this);

    /**
     * The column <code>public.LOCATIONS.PROPERTIES</code>.
     */
    public final TableField<Record, JsonValue> colProperties = createField(DSL.name("PROPERTIES"), DefaultDataType.getDefaultDataType(TYPE_JSONB), this, "", new JsonBinding());

    /**
     * The column <code>public.LOCATIONS.ID</code>.
     */
    public final TableField<Record, J> colId = createField(DSL.name("ID"), getIdType(), this);

    /**
     * The column <code>public.LOCATIONS.GEN_FOI_ID</code>.
     */
    public final TableField<Record, J> colGenFoiId = createField(DSL.name("GEN_FOI_ID"), getIdType(), this);

    /**
     * Create a <code>public.LOCATIONS</code> table reference
     */
    public TableImpLocations(DataType<J> idType) {
        super(idType, DSL.name(TABLE_NAME), null);
    }

    private TableImpLocations(Name alias, TableImpLocations<J> aliased) {
        super(aliased.getIdType(), alias, aliased);
    }

    @Override
    public void initRelations() {
        final TableCollection<J> tables = getTables();
        final ModelRegistry modelRegistry = getModelRegistry();
        final TableImpThingsLocations<J> tableThingsLoc = tables.getTableForClass(TableImpThingsLocations.class);
        final TableImpThings<J> tableThings = tables.getTableForClass(TableImpThings.class);
        registerRelation(new RelationManyToMany<>(this, tableThingsLoc, tableThings, modelRegistry.THING)
                .setSourceFieldAcc(TableImpLocations::getId)
                .setSourceLinkFieldAcc(TableImpThingsLocations::getLocationId)
                .setTargetLinkFieldAcc(TableImpThingsLocations::getThingId)
                .setTargetFieldAcc(TableImpThings::getId)
        );
        final TableImpLocationsHistLocations<J> tableLocHistLoc = tables.getTableForClass(TableImpLocationsHistLocations.class);
        final TableImpHistLocations<J> tableHistLoc = tables.getTableForClass(TableImpHistLocations.class);
        registerRelation(new RelationManyToMany<>(this, tableLocHistLoc, tableHistLoc, modelRegistry.HISTORICAL_LOCATION)
                .setSourceFieldAcc(TableImpLocations::getId)
                .setSourceLinkFieldAcc(TableImpLocationsHistLocations::getLocationId)
                .setTargetLinkFieldAcc(TableImpLocationsHistLocations::getHistLocationId)
                .setTargetFieldAcc(TableImpHistLocations::getId)
        );
    }

    @Override
    public void initProperties(final EntityFactories<J> entityFactories) {
        final IdManager idManager = entityFactories.getIdManager();
        ModelRegistry modelRegistry = getModelRegistry();
        pfReg.addEntryId(idManager, TableImpLocations::getId);
        pfReg.addEntryString(modelRegistry.EP_NAME, table -> table.colName);
        pfReg.addEntryString(modelRegistry.EP_DESCRIPTION, table -> table.colDescription);
        pfReg.addEntryString(modelRegistry.EP_ENCODINGTYPE, table -> table.colEncodingType);
        pfReg.addEntry(modelRegistry.EP_LOCATION,
                new ConverterRecordDeflt<>(
                        (TableImpLocations<J> table, Record tuple, Entity entity, DataSize dataSize) -> {
                            String encodingType = getFieldOrNull(tuple, table.colEncodingType);
                            String locationString = tuple.get(table.colLocation);
                            dataSize.increase(locationString == null ? 0 : locationString.length());
                            entity.setProperty(modelRegistry.EP_LOCATION, Utils.locationFromEncoding(encodingType, locationString));
                        },
                        (table, entity, insertFields) -> {
                            Object feature = entity.getProperty(modelRegistry.EP_LOCATION);
                            String encodingType = entity.getProperty(modelRegistry.EP_ENCODINGTYPE);
                            EntityFactories.insertGeometry(insertFields, table.colLocation, table.colGeom, encodingType, feature);
                        },
                        (table, entity, updateFields, message) -> {
                            Object feature = entity.getProperty(modelRegistry.EP_LOCATION);
                            String encodingType = entity.getProperty(modelRegistry.EP_ENCODINGTYPE);
                            EntityFactories.insertGeometry(updateFields, table.colLocation, table.colGeom, encodingType, feature);
                            message.addField(modelRegistry.EP_LOCATION);
                        }),
                new NFP<>("j", table -> table.colLocation));
        pfReg.addEntryNoSelect(modelRegistry.EP_LOCATION, "g", table -> table.colGeom);
        pfReg.addEntryMap(modelRegistry.EP_PROPERTIES, table -> table.colProperties);
        pfReg.addEntry(modelRegistry.NP_THINGS, TableImpLocations::getId, idManager);
        pfReg.addEntry(modelRegistry.NP_HISTORICALLOCATIONS, TableImpLocations::getId, idManager);
    }

    @Override
    protected void updateNavigationPropertySet(Entity location, EntitySet linkedSet, PostgresPersistenceManager<J> pm, boolean forInsert) throws IncompleteEntityException, NoSuchEntityException {
        EntityType linkedEntityType = linkedSet.getEntityType();
        ModelRegistry modelRegistry = getModelRegistry();
        if (linkedEntityType.equals(modelRegistry.THING)) {
            J locationId = (J) location.getId().getValue();
            DSLContext dslContext = pm.getDslContext();
            EntityFactories<J> entityFactories = pm.getEntityFactories();
            final TableCollection<J> tables = getTables();
            TableImpThingsLocations<J> ttl = tables.getTableForClass(TableImpThingsLocations.class);

            // Maybe Create new Things and link them to this Location.
            for (Entity t : linkedSet) {
                if (forInsert) {
                    entityFactories.entityExistsOrCreate(pm, t);
                } else if (!entityFactories.entityExists(pm, t)) {
                    throw new NoSuchEntityException("Thing not found.");
                }

                J thingId = (J) t.getId().getValue();

                // Unlink old Locations from Thing.
                long delCount = dslContext.delete(ttl).where(ttl.getThingId().eq(thingId)).execute();
                LOGGER.debug(EntityFactories.UNLINKED_L_FROM_T, delCount, thingId);

                // Link new Location to thing.
                dslContext.insertInto(ttl)
                        .set(ttl.getThingId(), thingId)
                        .set(ttl.getLocationId(), locationId)
                        .execute();
                LOGGER.debug(EntityFactories.LINKED_L_TO_T, locationId, thingId);

                // Create HistoricalLocation for Thing
                TableImpHistLocations<J> qhl = tables.getTableForClass(TableImpHistLocations.class);
                Record1<J> linkHistLoc = dslContext.insertInto(qhl)
                        .set(qhl.getThingId(), thingId)
                        .set(qhl.time, OffsetDateTime.now(Constants.UTC))
                        .returningResult(qhl.getId())
                        .fetchOne();
                J histLocationId = linkHistLoc.component1();
                LOGGER.debug(EntityFactories.CREATED_HL, histLocationId);

                // Link Location to HistoricalLocation.
                TableImpLocationsHistLocations<J> qlhl = tables.getTableForClass(TableImpLocationsHistLocations.class);
                dslContext.insertInto(qlhl)
                        .set(qlhl.getHistLocationId(), histLocationId)
                        .set(qlhl.getLocationId(), locationId)
                        .execute();
                LOGGER.debug(EntityFactories.LINKED_L_TO_HL, locationId, histLocationId);

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
        super.updateNavigationPropertySet(location, linkedSet, pm, forInsert);
    }

    @Override
    public void delete(PostgresPersistenceManager<J> pm, J entityId) throws NoSuchEntityException {
        super.delete(pm, entityId);
        final TableCollection<J> tables = getTables();
        // Also delete all historicalLocations that no longer reference any location
        TableImpHistLocations<J> thl = tables.getTableForClass(TableImpHistLocations.class);
        TableImpLocationsHistLocations<J> tlhl = tables.getTableForClass(TableImpLocationsHistLocations.class);
        int count = pm.getDslContext()
                .delete(thl)
                .where(thl.getId().in(
                        DSL.select(thl.getId())
                                .from(thl)
                                .leftJoin(tlhl).on(thl.getId().eq(tlhl.getHistLocationId()))
                                .where(tlhl.getLocationId().isNull())
                ))
                .execute();
        LOGGER.debug("Deleted {} HistoricalLocations", count);

    }

    @Override
    public EntityType getEntityType() {
        return getModelRegistry().LOCATION;
    }

    @Override
    public TableField<Record, J> getId() {
        return colId;
    }

    public TableField<Record, J> getGenFoiId() {
        return colGenFoiId;
    }

    @Override
    public TableImpLocations<J> as(Name alias) {
        return new TableImpLocations<>(alias, this);
    }

    @Override
    public TableImpLocations<J> as(String alias) {
        return new TableImpLocations<>(DSL.name(alias), this);
    }

    @Override
    public PropertyFields<TableImpLocations<J>> handleEntityPropertyCustomSelect(final EntityPropertyCustomSelect epCustomSelect) {
        final EntityPropertyMain mainEntityProperty = epCustomSelect.getMainEntityProperty();
        if (mainEntityProperty == getModelRegistry().EP_LOCATION) {
            PropertyFields<TableImpLocations<J>> mainPropertyFields = pfReg.getSelectFieldsForProperty(mainEntityProperty);
            final Field mainField = mainPropertyFields.fields.values().iterator().next().get(getThis());

            JsonFieldFactory jsonFactory = jsonFieldFromPath(mainField, epCustomSelect);
            return propertyFieldForJsonField(jsonFactory, epCustomSelect);
        }
        return super.handleEntityPropertyCustomSelect(epCustomSelect);
    }

    @Override
    public TableImpLocations<J> getThis() {
        return this;
    }

}
