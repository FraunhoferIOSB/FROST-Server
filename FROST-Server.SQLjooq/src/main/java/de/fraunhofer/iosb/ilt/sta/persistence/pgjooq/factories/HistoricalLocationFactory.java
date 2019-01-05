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
import de.fraunhofer.iosb.ilt.sta.path.NavigationProperty;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.DataSize;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.EntityFactories;
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.EntityFactories.CAN_NOT_BE_NULL;
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.EntityFactories.CHANGED_MULTIPLE_ROWS;
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.EntityFactories.LINKED_L_TO_HL;
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.EntityFactories.LINKED_L_TO_T;
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.EntityFactories.UNLINKED_L_FROM_T;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.Utils;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractRecordHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractRecordLocationsHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractRecordThingsLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableLocationsHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableThingsLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.QCollection;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import static de.fraunhofer.iosb.ilt.sta.settings.CoreSettings.UTC;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.sta.util.NoSuchEntityException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collections;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hylke van der Schaaf
 * @param <J> The type of the ID fields.
 */
public class HistoricalLocationFactory<J> implements EntityFactory<HistoricalLocation, J> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(HistoricalLocationFactory.class);
    private final EntityFactories<J> entityFactories;
    private final AbstractTableHistLocations<J> qInstance;
    private final QCollection<J> qCollection;

    public HistoricalLocationFactory(EntityFactories<J> factories, AbstractTableHistLocations<J> qInstance) {
        this.entityFactories = factories;
        this.qInstance = qInstance;
        this.qCollection = factories.qCollection;
    }

    @Override
    public HistoricalLocation create(Record tuple, Query query, DataSize dataSize) {
        HistoricalLocation entity = new HistoricalLocation();
        J id = entityFactories.getIdFromRecord(tuple, qInstance.getId());
        if (id != null) {
            entity.setId(entityFactories.idFromObject(id));
        }
        entity.setThing(entityFactories.thingFromId(tuple, qInstance.getThingId()));
        entity.setTime(Utils.instantFromTime(tuple.get(qInstance.time)));
        return entity;
    }

    @Override
    public boolean insert(PostgresPersistenceManager<J> pm, HistoricalLocation h) throws NoSuchEntityException, IncompleteEntityException {
        Thing t = h.getThing();
        entityFactories.entityExistsOrCreate(pm, t);
        J thingId = (J) h.getThing().getId().getValue();

        OffsetDateTime newTime = OffsetDateTime.ofInstant(Instant.ofEpochMilli(h.getTime().getDateTime().getMillis()), UTC);

        DSLContext dslContext = pm.createDdslContext();
        AbstractTableHistLocations<J> qhl = qCollection.qHistLocations;
        AbstractRecordHistLocations<J> newHistLoc = dslContext.newRecord(qhl);
        newHistLoc.set(qhl.time, newTime);
        newHistLoc.set(qhl.getThingId(), thingId);

        entityFactories.insertUserDefinedId(pm, newHistLoc, qhl.getId(), h);

        newHistLoc.store();
        J generatedId = newHistLoc.getId();
        LOGGER.debug("Inserted HistoricalLocation. Created id = {}.", generatedId);
        h.setId(entityFactories.idFromObject(generatedId));

        EntitySet<Location> locations = h.getLocations();
        for (Location l : locations) {
            entityFactories.entityExistsOrCreate(pm, l);
            J lId = (J) l.getId().getValue();
            AbstractTableLocationsHistLocations<J> qlhl = qCollection.qLocationsHistLocations;
            AbstractRecordLocationsHistLocations<J> newLink = dslContext.newRecord(qlhl);
            newLink.set(qlhl.getHistLocationId(), generatedId);
            newLink.set(qlhl.getLocationId(), lId);
            newLink.store();
            LOGGER.debug(LINKED_L_TO_HL, lId, generatedId);
        }

        // https://github.com/opengeospatial/sensorthings/issues/30
        // Check the time of the latest HistoricalLocation of our thing.
        // If this time is earlier than our time, set the Locations of our Thing to our Locations.
        Record lastHistLocation = dslContext.select(Collections.emptyList())
                .from(qhl)
                .where(qhl.getThingId().eq(thingId).and(qhl.time.gt(newTime)))
                .orderBy(qhl.time.desc())
                .limit(1)
                .fetchOne();
        if (lastHistLocation == null) {
            // We are the newest.
            // Unlink old Locations from Thing.
            AbstractTableThingsLocations<J> qtl = qCollection.qThingsLocations;
            long count = dslContext
                    .delete(qtl)
                    .where(qtl.getThingId().eq(thingId))
                    .execute();
            LOGGER.debug(UNLINKED_L_FROM_T, count, thingId);

            // Link new locations to Thing, track the ids.
            for (Location l : h.getLocations()) {
                if (l.getId() == null || !entityFactories.entityExists(pm, l)) {
                    throw new NoSuchEntityException("Location with no id.");
                }
                J locationId = (J) l.getId().getValue();

                AbstractRecordThingsLocations<J> newThingLoc = dslContext.newRecord(qtl);
                newThingLoc.set(qtl.getThingId(), thingId);
                newThingLoc.set(qtl.getLocationId(), locationId);
                newThingLoc.store();
                LOGGER.debug(LINKED_L_TO_T, locationId, thingId);
            }
        }
        return true;
    }

    @Override
    public EntityChangedMessage update(PostgresPersistenceManager<J> pm, HistoricalLocation hl, J id) throws IncompleteEntityException {
        DSLContext dslContext = pm.createDdslContext();
        AbstractTableHistLocations<J> qhl = qCollection.qHistLocations;
        AbstractRecordHistLocations<J> update = dslContext.newRecord(qhl);

        EntityChangedMessage message = new EntityChangedMessage();

        if (hl.isSetThing()) {
            if (!entityFactories.entityExists(pm, hl.getThing())) {
                throw new IncompleteEntityException("Thing" + CAN_NOT_BE_NULL);
            }
            update.set(qhl.getThingId(), (J) hl.getThing().getId().getValue());
            message.addField(NavigationProperty.THING);
        }
        if (hl.isSetTime()) {
            if (hl.getTime() == null) {
                throw new IncompleteEntityException("time" + CAN_NOT_BE_NULL);
            }
            update.set(qhl.time, hl.getTime().getOffsetDateTime());
            message.addField(EntityProperty.TIME);
        }
        update.setId(id);
        long count = 0;
        if (update.changed()) {
            count = update.store();
        }
        if (count > 1) {
            LOGGER.error("Updating Location {} caused {} rows to change!", id, count);
            throw new IllegalStateException(CHANGED_MULTIPLE_ROWS);
        }
        LOGGER.debug("Updated Location {}", id);

        // Link existing locations to the HistoricalLocation.
        for (Location l : hl.getLocations()) {
            if (!entityFactories.entityExists(pm, l)) {
                throw new IllegalArgumentException("Unknown Location or Location with no id.");
            }
            J lId = (J) l.getId().getValue();

            AbstractTableLocationsHistLocations<J> qlhl = qCollection.qLocationsHistLocations;
            AbstractRecordLocationsHistLocations<J> insert = dslContext.newRecord(qlhl);
            insert.set(qlhl.getHistLocationId(), id);
            insert.set(qlhl.getLocationId(), lId);
            insert.store();
            LOGGER.debug(LINKED_L_TO_HL, lId, id);
        }
        return message;
    }

    @Override
    public void delete(PostgresPersistenceManager<J> pm, J entityId) throws NoSuchEntityException {
        long count = pm.createDdslContext()
                .delete(qInstance)
                .where(qInstance.getId().eq(entityId))
                .execute();
        if (count == 0) {
            throw new NoSuchEntityException("HistoricalLocation " + entityId + " not found.");
        }
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.HISTORICALLOCATION;
    }

    @Override
    public Field<J> getPrimaryKey() {
        return qInstance.getId();
    }

}
