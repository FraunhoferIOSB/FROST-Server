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
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.Utils;
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.Utils.getFieldOrNull;
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.factories.EntityFactories.CAN_NOT_BE_NULL;
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.factories.EntityFactories.CHANGED_MULTIPLE_ROWS;
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.factories.EntityFactories.CREATED_HL;
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.factories.EntityFactories.LINKED_L_TO_HL;
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.factories.EntityFactories.LINKED_L_TO_T;
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.factories.EntityFactories.UNLINKED_L_FROM_T;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableLocationsHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableThingsLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import static de.fraunhofer.iosb.ilt.sta.settings.CoreSettings.UTC;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.sta.util.NoSuchEntityException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hylke van der Schaaf
 * @param <J> The type of the ID fields.
 */
public class LocationFactory<J> implements EntityFactory<Location, J> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LocationFactory.class);

    private final EntityFactories<J> entityFactories;
    private final AbstractTableLocations<J> table;
    private final TableCollection<J> tableCollection;

    public LocationFactory(EntityFactories<J> factories, AbstractTableLocations<J> table) {
        this.entityFactories = factories;
        this.table = table;
        this.tableCollection = factories.tableCollection;
    }

    @Override
    public Location create(Record tuple, Query query, DataSize dataSize) {
        Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();
        Location entity = new Location();
        J id = getFieldOrNull(tuple, table.getId());
        if (id != null) {
            entity.setId(entityFactories.idFromObject(id));
        }
        entity.setName(getFieldOrNull(tuple, table.name));
        entity.setDescription(getFieldOrNull(tuple, table.description));
        String encodingType = getFieldOrNull(tuple, table.encodingType);
        entity.setEncodingType(encodingType);
        if (select.isEmpty() || select.contains(EntityProperty.LOCATION)) {
            String locationString = getFieldOrNull(tuple, table.location);
            dataSize.increase(locationString == null ? 0 : locationString.length());
            entity.setLocation(Utils.locationFromEncoding(encodingType, locationString));
        }
        if (select.isEmpty() || select.contains(EntityProperty.PROPERTIES)) {
            String props = getFieldOrNull(tuple, table.properties);
            entity.setProperties(Utils.jsonToObject(props, Map.class));
        }
        return entity;
    }

    @Override
    public boolean insert(PostgresPersistenceManager<J> pm, Location l) throws NoSuchEntityException, IncompleteEntityException {

        Map<Field, Object> insert = new HashMap<>();

        insert.put(table.name, l.getName());
        insert.put(table.description, l.getDescription());
        insert.put(table.properties, EntityFactories.objectToJson(l.getProperties()));

        String encodingType = l.getEncodingType();
        insert.put(table.encodingType, encodingType);

        EntityFactories.insertGeometry(insert, table.location, table.geom, encodingType, l.getLocation());
        entityFactories.insertUserDefinedId(pm, insert, table.getId(), l);

        DSLContext dslContext = pm.createDdslContext();
        Record1<J> result = dslContext.insertInto(table)
                .set(insert)
                .returningResult(table.getId())
                .fetchOne();
        J locationId = result.component1();
        LOGGER.debug("Inserted Location. Created id = {}.", locationId);
        l.setId(entityFactories.idFromObject(locationId));

        // Link Things
        EntitySet<Thing> things = l.getThings();
        for (Thing t : things) {
            entityFactories.entityExistsOrCreate(pm, t);
            linkThingToLocation(entityFactories, dslContext, t, locationId);
        }

        return true;
    }

    @Override
    public EntityChangedMessage update(PostgresPersistenceManager<J> pm, Location location, J locationId) throws NoSuchEntityException, IncompleteEntityException {
        Map<Field, Object> update = new HashMap<>();
        EntityChangedMessage message = new EntityChangedMessage();
        DSLContext dslContext = pm.createDdslContext();

        updateName(location, update, message);
        updateDescription(location, update, message);
        updateProperties(location, update, message);
        updateLocationAndEncoding(location, locationId, update, message, dslContext);
        update.put(table.getId(), locationId);

        long count = 0;
        if (!update.isEmpty()) {
            count = dslContext.update(table)
                    .set(update)
                    .where(table.getId().equal(locationId))
                    .execute();
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

    private void updateName(Location location, Map<Field, Object> update, EntityChangedMessage message) throws IncompleteEntityException {
        if (location.isSetName()) {
            if (location.getName() == null) {
                throw new IncompleteEntityException("name" + CAN_NOT_BE_NULL);
            }
            update.put(table.name, location.getName());
            message.addField(EntityProperty.NAME);
        }
    }

    private void updateDescription(Location location, Map<Field, Object> update, EntityChangedMessage message) throws IncompleteEntityException {
        if (location.isSetDescription()) {
            if (location.getDescription() == null) {
                throw new IncompleteEntityException(EntityProperty.DESCRIPTION.jsonName + CAN_NOT_BE_NULL);
            }
            update.put(table.description, location.getDescription());
            message.addField(EntityProperty.DESCRIPTION);
        }
    }

    private void updateProperties(Location location, Map<Field, Object> update, EntityChangedMessage message) {
        if (location.isSetProperties()) {
            update.put(table.properties, EntityFactories.objectToJson(location.getProperties()));
            message.addField(EntityProperty.PROPERTIES);
        }
    }

    private void updateLocationAndEncoding(Location location, J locationId, Map<Field, Object> update, EntityChangedMessage message, DSLContext dslContext) throws IncompleteEntityException {
        if (location.isSetEncodingType() && location.getEncodingType() == null) {
            throw new IncompleteEntityException("encodingType" + CAN_NOT_BE_NULL);
        }
        if (location.isSetLocation() && location.getLocation() == null) {
            throw new IncompleteEntityException("locations" + CAN_NOT_BE_NULL);
        }
        if (location.isSetEncodingType() && location.getEncodingType() != null && location.isSetLocation() && location.getLocation() != null) {
            String encodingType = location.getEncodingType();
            update.put(table.encodingType, encodingType);

            EntityFactories.insertGeometry(update, table.location, table.geom, encodingType, location.getLocation());
            message.addField(EntityProperty.ENCODINGTYPE);
            message.addField(EntityProperty.LOCATION);
        } else if (location.isSetEncodingType() && location.getEncodingType() != null) {
            String encodingType = location.getEncodingType();
            update.put(table.encodingType, encodingType);
            message.addField(EntityProperty.ENCODINGTYPE);
        } else if (location.isSetLocation() && location.getLocation() != null) {
            String encodingType = dslContext.select(table.encodingType)
                    .from(table)
                    .where(table.getId().eq(locationId))
                    .fetchOne(table.encodingType);
            Object parsedObject = EntityFactories.reParseGeometry(encodingType, location.getLocation());
            EntityFactories.insertGeometry(update, table.location, table.geom, encodingType, parsedObject);
            message.addField(EntityProperty.LOCATION);
        }
    }

    private void linkThings(Location l, PostgresPersistenceManager<J> pm, DSLContext dslContext, J locationId) throws NoSuchEntityException {
        EntitySet<Thing> things = l.getThings();
        for (Thing t : things) {
            if (!entityFactories.entityExists(pm, t)) {
                throw new NoSuchEntityException("Thing not found.");
            }
            linkThingToLocation(entityFactories, dslContext, t, locationId);
        }
    }

    private void linkHistoricalLocations(Location l, DSLContext dslContext, J locationId) {
        for (HistoricalLocation hl : l.getHistoricalLocations()) {
            if (hl.getId() == null) {
                throw new IllegalArgumentException("HistoricalLocation with no id.");
            }
            J hlId = (J) hl.getId().getValue();

            AbstractTableLocationsHistLocations<J> qlhl = entityFactories.tableCollection.tableLocationsHistLocations;
            dslContext.insertInto(qlhl)
                    .set(qlhl.getHistLocationId(), hlId)
                    .set(qlhl.getLocationId(), locationId)
                    .execute();
            LOGGER.debug(LINKED_L_TO_HL, locationId, hlId);
        }
    }

    @Override
    public void delete(PostgresPersistenceManager<J> pm, J entityId) throws NoSuchEntityException {
        long count = pm.createDdslContext()
                .delete(table)
                .where(table.getId().eq(entityId))
                .execute();
        if (count == 0) {
            throw new NoSuchEntityException("Location " + entityId + " not found.");
        }
        LOGGER.debug("Deleted {} Locations", count);
        // Also delete all historicalLocations that no longer reference any location
        AbstractTableHistLocations<J> qhl = tableCollection.tableHistLocations;
        AbstractTableLocationsHistLocations<J> qlhl = tableCollection.tableLocationsHistLocations;
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

    private static <J> void linkThingToLocation(EntityFactories<J> entityFactories, DSLContext dslContext, Thing t, J locationId) {
        J thingId = (J) t.getId().getValue();

        // Unlink old Locations from Thing.
        AbstractTableThingsLocations<J> qtl = entityFactories.tableCollection.tableThingsLocations;
        long delCount = dslContext.delete(qtl).where(qtl.getThingId().eq(thingId)).execute();
        LOGGER.debug(UNLINKED_L_FROM_T, delCount, thingId);

        // Link new Location to thing.
        dslContext.insertInto(qtl)
                .set(qtl.getThingId(), thingId)
                .set(qtl.getLocationId(), locationId)
                .execute();
        LOGGER.debug(LINKED_L_TO_T, locationId, thingId);

        // Create HistoricalLocation for Thing
        AbstractTableHistLocations<J> qhl = entityFactories.tableCollection.tableHistLocations;
        Record1<J> linkHistLoc = dslContext.insertInto(qhl)
                .set(qhl.getThingId(), thingId)
                .set(qhl.time, OffsetDateTime.now(UTC))
                .returningResult(qhl.getId())
                .fetchOne();
        J histLocationId = linkHistLoc.component1();
        LOGGER.debug(CREATED_HL, histLocationId);

        // Link Location to HistoricalLocation.
        AbstractTableLocationsHistLocations<J> qlhl = entityFactories.tableCollection.tableLocationsHistLocations;
        dslContext.insertInto(qlhl)
                .set(qlhl.getHistLocationId(), histLocationId)
                .set(qlhl.getLocationId(), locationId)
                .execute();
        LOGGER.debug(LINKED_L_TO_HL, locationId, histLocationId);
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.LOCATION;
    }

    @Override
    public Field<J> getPrimaryKey() {
        return table.getId();
    }

}
