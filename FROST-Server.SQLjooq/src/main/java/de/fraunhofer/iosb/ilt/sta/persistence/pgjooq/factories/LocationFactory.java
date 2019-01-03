/*
 * Copyright (C) 2018 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
 * Karlsruhe, Germany.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.factories;

import de.fraunhofer.iosb.ilt.sta.messagebus.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.sta.model.HistoricalLocation;
import de.fraunhofer.iosb.ilt.sta.model.Location;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.Property;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.DataSize;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.EntityFactories;
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.EntityFactories.CAN_NOT_BE_NULL;
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.EntityFactories.CHANGED_MULTIPLE_ROWS;
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.EntityFactories.CREATED_HL;
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.EntityFactories.LINKED_L_TO_HL;
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.EntityFactories.LINKED_L_TO_T;
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.EntityFactories.UNLINKED_L_FROM_T;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.Utils;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractRecordHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractRecordLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractRecordLocationsHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractRecordThingsLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableLocationsHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableThingsLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.QCollection;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import static de.fraunhofer.iosb.ilt.sta.settings.CoreSettings.UTC;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.sta.util.NoSuchEntityException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hylke van der Schaaf
 * @param <I> The type of path used for the ID fields.
 * @param <J> The type of the ID fields.
 */
public class LocationFactory<J> implements EntityFactory<Location, J> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LocationFactory.class);

    private final EntityFactories<J> entityFactories;
    private final AbstractTableLocations<J> qInstance;
    private final QCollection<J> qCollection;

    public LocationFactory(EntityFactories<J> factories, AbstractTableLocations<J> qInstance) {
        this.entityFactories = factories;
        this.qInstance = qInstance;
        this.qCollection = factories.qCollection;
    }

    @Override
    public Location create(Record tuple, Query query, DataSize dataSize) {
        Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();
        Location entity = new Location();
        J id = entityFactories.getIdFromRecord(tuple, qInstance.getId());
        if (id != null) {
            entity.setId(entityFactories.idFromObject(id));
        }
        entity.setName(tuple.get(qInstance.name));
        entity.setDescription(tuple.get(qInstance.description));
        String encodingType = tuple.get(qInstance.encodingType);
        entity.setEncodingType(encodingType);
        if (select.isEmpty() || select.contains(EntityProperty.LOCATION)) {
            String locationString = tuple.get(qInstance.location);
            dataSize.increase(locationString == null ? 0 : locationString.length());
            entity.setLocation(Utils.locationFromEncoding(encodingType, locationString));
        }
        if (select.isEmpty() || select.contains(EntityProperty.PROPERTIES)) {
            String props = tuple.get(qInstance.properties);
            entity.setProperties(Utils.jsonToObject(props, Map.class));
        }
        return entity;
    }

    @Override
    public boolean insert(PostgresPersistenceManager<J> pm, Location l) throws NoSuchEntityException, IncompleteEntityException {
        DSLContext dslContext = pm.createDdslContext();
        AbstractTableLocations<J> ql = qCollection.qLocations;
        AbstractRecordLocations<J> insert = dslContext.newRecord(ql);

        insert.set(ql.name, l.getName());
        insert.set(ql.description, l.getDescription());
        insert.set(ql.properties, EntityFactories.objectToJson(l.getProperties()));

        String encodingType = l.getEncodingType();
        insert.set(ql.encodingType, encodingType);

        // TODO: This will probably need a Binding
        // EntityFactories.insertGeometry(insert, ql.location, ql.geom, encodingType, l.getLocation());
        entityFactories.insertUserDefinedId(pm, insert, ql.getId(), l);

        insert.store();
        J locationId = insert.getId();
        LOGGER.debug("Inserted Location. Created id = {}.", locationId);
        l.setId(entityFactories.idFromObject(locationId));

        // Link Things
        EntitySet<Thing> things = l.getThings();
        for (Thing t : things) {
            entityFactories.entityExistsOrCreate(pm, t);
            linkThingToLocation(dslContext, t, locationId);
        }

        return true;
    }

    @Override
    public EntityChangedMessage update(PostgresPersistenceManager<J> pm, Location location, J locationId) throws NoSuchEntityException, IncompleteEntityException {
        DSLContext dslContext = pm.createDdslContext();
        AbstractTableLocations<J> ql = qCollection.qLocations;
        AbstractRecordLocations<J> update = dslContext.newRecord(ql);
        EntityChangedMessage message = new EntityChangedMessage();

        updateName(location, update, ql, message);
        updateDescription(location, update, ql, message);
        updateProperties(location, update, ql, message);
        updateLocationAndEncoding(location, locationId, update, ql, message, dslContext);

        update.setId(locationId);
        long count = 0;
        if (update.changed()) {
            count = update.store();
        }
        if (count > 1) {
            LOGGER.error("Updating Location {} caused {} rows to change!", locationId, count);
            throw new IllegalStateException(CHANGED_MULTIPLE_ROWS);
        }
        LOGGER.debug("Updated Location {}", locationId);

        linkHistoricalLocations(location, dslContext, locationId);
        linkThings(location, pm, dslContext, locationId);

        return message;
    }

    private void updateName(Location location, AbstractRecordLocations<J> update, AbstractTableLocations<J> ql, EntityChangedMessage message) throws IncompleteEntityException {
        if (location.isSetName()) {
            if (location.getName() == null) {
                throw new IncompleteEntityException("name" + CAN_NOT_BE_NULL);
            }
            update.set(ql.name, location.getName());
            message.addField(EntityProperty.NAME);
        }
    }

    private void updateDescription(Location location, AbstractRecordLocations<J> update, AbstractTableLocations<J> ql, EntityChangedMessage message) throws IncompleteEntityException {
        if (location.isSetDescription()) {
            if (location.getDescription() == null) {
                throw new IncompleteEntityException(EntityProperty.DESCRIPTION.jsonName + CAN_NOT_BE_NULL);
            }
            update.set(ql.description, location.getDescription());
            message.addField(EntityProperty.DESCRIPTION);
        }
    }

    private void updateProperties(Location location, AbstractRecordLocations<J> update, AbstractTableLocations<J> ql, EntityChangedMessage message) {
        if (location.isSetProperties()) {
            update.set(ql.properties, EntityFactories.objectToJson(location.getProperties()));
            message.addField(EntityProperty.PROPERTIES);
        }
    }

    private void updateLocationAndEncoding(Location location, J locationId, AbstractRecordLocations<J> update, AbstractTableLocations<J> ql, EntityChangedMessage message, DSLContext dslContext) throws IncompleteEntityException {
        if (location.isSetEncodingType() && location.getEncodingType() == null) {
            throw new IncompleteEntityException("encodingType" + CAN_NOT_BE_NULL);
        }
        if (location.isSetLocation() && location.getLocation() == null) {
            throw new IncompleteEntityException("locations" + CAN_NOT_BE_NULL);
        }
        if (location.isSetEncodingType() && location.getEncodingType() != null && location.isSetLocation() && location.getLocation() != null) {
            String encodingType = location.getEncodingType();
            update.set(ql.encodingType, encodingType);

            // TODO: This will probably need a Binding
            // EntityFactories.insertGeometry(update, ql.location, ql.geom, encodingType, location.getLocation());
            message.addField(EntityProperty.ENCODINGTYPE);
            message.addField(EntityProperty.LOCATION);
        } else if (location.isSetEncodingType() && location.getEncodingType() != null) {
            String encodingType = location.getEncodingType();
            update.set(ql.encodingType, encodingType);
            message.addField(EntityProperty.ENCODINGTYPE);
        } else if (location.isSetLocation() && location.getLocation() != null) {
            String encodingType = dslContext.select(ql.encodingType)
                    .from(ql)
                    .where(ql.getId().eq(locationId))
                    .fetchOne(ql.encodingType);
            Object parsedObject = EntityFactories.reParseGeometry(encodingType, location.getLocation());
            // TODO: This will probably need a Binding
            // EntityFactories.insertGeometry(update, ql.location, ql.geom, encodingType, parsedObject);
            message.addField(EntityProperty.LOCATION);
        }
    }

    private void linkThings(Location l, PostgresPersistenceManager<J> pm, DSLContext dslContext, J locationId) throws NoSuchEntityException {
        EntitySet<Thing> things = l.getThings();
        for (Thing t : things) {
            if (!entityFactories.entityExists(pm, t)) {
                throw new NoSuchEntityException("Thing not found.");
            }
            linkThingToLocation(dslContext, t, locationId);
        }
    }

    private void linkHistoricalLocations(Location l, DSLContext dslContext, J locationId) {
        for (HistoricalLocation hl : l.getHistoricalLocations()) {
            if (hl.getId() == null) {
                throw new IllegalArgumentException("HistoricalLocation with no id.");
            }
            J hlId = (J) hl.getId().getValue();

            AbstractTableLocationsHistLocations<J> qlhl = qCollection.qLocationsHistLocations;
            AbstractRecordLocationsHistLocations<J> insert = dslContext.newRecord(qlhl);
            insert.set(qlhl.getHistLocationId(), hlId);
            insert.set(qlhl.getLocationId(), locationId);
            insert.store();
            LOGGER.debug(LINKED_L_TO_HL, locationId, hlId);
        }
    }

    @Override
    public void delete(PostgresPersistenceManager<J> pm, J entityId) throws NoSuchEntityException {
        long count = pm.createDdslContext()
                .delete(qInstance)
                .where(qInstance.getId().eq(entityId))
                .execute();
        if (count == 0) {
            throw new NoSuchEntityException("Location " + entityId + " not found.");
        }
        LOGGER.debug("Deleted {} Locations", count);
        // Also delete all historicalLocations that no longer reference any location
        AbstractTableHistLocations<J> qhl = qCollection.qHistLocations;
        AbstractTableLocationsHistLocations<J> qlhl = qCollection.qLocationsHistLocations;
        count = pm.createDdslContext()
                .delete(qhl)
                .where(qhl.getId().in(
                        DSL.select(qhl.getId())
                                .from(qhl)
                                .leftJoin(qlhl).on(qhl.getId().eq(qlhl.getHistLocationId()))
                                .where(qlhl.getLocationId().isNull())
                ))
                .execute();
        LOGGER.debug("Deleted {} HistoricalLocations", count);
    }

    private void linkThingToLocation(DSLContext dslContext, Thing t, J locationId) {
        J thingId = (J) t.getId().getValue();

        // Unlink old Locations from Thing.
        AbstractTableThingsLocations<J> qtl = qCollection.qThingsLocations;
        long delCount = dslContext.delete(qtl).where(qtl.getThingId().eq(thingId)).execute();
        LOGGER.debug(UNLINKED_L_FROM_T, delCount, thingId);

        // Link new Location to thing.
        AbstractRecordThingsLocations<J> linkThing = dslContext.newRecord(qtl);
        linkThing.set(qtl.getThingId(), thingId);
        linkThing.set(qtl.getLocationId(), locationId);
        linkThing.store();
        LOGGER.debug(LINKED_L_TO_T, locationId, thingId);

        // Create HistoricalLocation for Thing
        AbstractTableHistLocations<J> qhl = qCollection.qHistLocations;
        AbstractRecordHistLocations<J> linkHistLoc = dslContext.newRecord(qhl);
        linkHistLoc.set(qhl.getThingId(), thingId);
        linkHistLoc.set(qhl.time, OffsetDateTime.now(UTC));
        linkHistLoc.store();
        J histLocationId = linkHistLoc.getId();
        LOGGER.debug(CREATED_HL, histLocationId);

        // Link Location to HistoricalLocation.
        AbstractTableLocationsHistLocations<J> qlhl = qCollection.qLocationsHistLocations;
        AbstractRecordLocationsHistLocations<J> linkLoc = dslContext.newRecord(qlhl);
        linkLoc.set(qlhl.getHistLocationId(), histLocationId);
        linkLoc.set(qlhl.getLocationId(), locationId);
        linkLoc.store();
        LOGGER.debug(LINKED_L_TO_HL, locationId, histLocationId);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.LOCATION;
    }

    @Override
    public Field<J> getPrimaryKey() {
        return qInstance.getId();
    }

}
