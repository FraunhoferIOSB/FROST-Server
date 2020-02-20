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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.HistoricalLocation;
import de.fraunhofer.iosb.ilt.frostserver.model.Location;
import de.fraunhofer.iosb.ilt.frostserver.model.Thing;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.Utils;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.Utils.getFieldOrNull;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.CAN_NOT_BE_NULL;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.CHANGED_MULTIPLE_ROWS;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.LINKED_L_TO_HL;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.LINKED_L_TO_T;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.UNLINKED_L_FROM_T;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableHistLocations;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableLocationsHistLocations;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableThingsLocations;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.UTC;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
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
    private final AbstractTableHistLocations<J> table;
    private final TableCollection<J> tableCollection;

    public HistoricalLocationFactory(EntityFactories<J> factories, AbstractTableHistLocations<J> table) {
        this.entityFactories = factories;
        this.table = table;
        this.tableCollection = factories.tableCollection;
    }

    @Override
    public HistoricalLocation create(Record tuple, Query query, DataSize dataSize) {
        HistoricalLocation entity = new HistoricalLocation();
        J id = getFieldOrNull(tuple, table.getId());
        if (id != null) {
            entity.setId(entityFactories.idFromObject(id));
        }
        entity.setThing(entityFactories.thingFromId(tuple, table.getThingId()));
        entity.setTime(Utils.instantFromTime(getFieldOrNull(tuple, table.time)));
        return entity;
    }

    @Override
    public boolean insert(PostgresPersistenceManager<J> pm, HistoricalLocation h) throws NoSuchEntityException, IncompleteEntityException {
        Thing t = h.getThing();
        entityFactories.entityExistsOrCreate(pm, t);
        J thingId = (J) h.getThing().getId().getValue();

        OffsetDateTime newTime = OffsetDateTime.ofInstant(Instant.ofEpochMilli(h.getTime().getDateTime().getMillis()), UTC);

        DSLContext dslContext = pm.getDslContext();

        Map<Field, Object> insert = new HashMap<>();
        insert.put(table.time, newTime);
        insert.put(table.getThingId(), thingId);

        entityFactories.insertUserDefinedId(pm, insert, table.getId(), h);

        Record1<J> result = dslContext.insertInto(table)
                .set(insert)
                .returningResult(table.getId())
                .fetchOne();
        J generatedId = result.component1();
        LOGGER.debug("Inserted HistoricalLocation. Created id = {}.", generatedId);
        h.setId(entityFactories.idFromObject(generatedId));

        EntitySet<Location> locations = h.getLocations();
        for (Location l : locations) {
            entityFactories.entityExistsOrCreate(pm, l);
            J lId = (J) l.getId().getValue();
            AbstractTableLocationsHistLocations<J> qlhl = tableCollection.tableLocationsHistLocations;
            dslContext.insertInto(qlhl)
                    .set(qlhl.getHistLocationId(), generatedId)
                    .set(qlhl.getLocationId(), lId)
                    .execute();
            LOGGER.debug(LINKED_L_TO_HL, lId, generatedId);
        }

        // https://github.com/opengeospatial/sensorthings/issues/30
        // Check the time of the latest HistoricalLocation of our thing.
        // If this time is earlier than our time, set the Locations of our Thing to our Locations.
        Record lastHistLocation = dslContext.select(Collections.emptyList())
                .from(table)
                .where(table.getThingId().eq(thingId).and(table.time.gt(newTime)))
                .orderBy(table.time.desc())
                .limit(1)
                .fetchOne();
        if (lastHistLocation == null) {
            // We are the newest.
            // Unlink old Locations from Thing.
            AbstractTableThingsLocations<J> qtl = tableCollection.tableThingsLocations;
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

                dslContext.insertInto(qtl)
                        .set(qtl.getThingId(), thingId)
                        .set(qtl.getLocationId(), locationId)
                        .execute();
                LOGGER.debug(LINKED_L_TO_T, locationId, thingId);
            }
        }
        return true;
    }

    @Override
    public EntityChangedMessage update(PostgresPersistenceManager<J> pm, HistoricalLocation hl, J id) throws IncompleteEntityException {
        Map<Field, Object> update = new HashMap<>();

        EntityChangedMessage message = new EntityChangedMessage();

        if (hl.isSetThing()) {
            if (!entityFactories.entityExists(pm, hl.getThing())) {
                throw new IncompleteEntityException("Thing" + CAN_NOT_BE_NULL);
            }
            update.put(table.getThingId(), (J) hl.getThing().getId().getValue());
            message.addField(NavigationProperty.THING);
        }
        if (hl.isSetTime()) {
            if (hl.getTime() == null) {
                throw new IncompleteEntityException("time" + CAN_NOT_BE_NULL);
            }
            update.put(table.time, hl.getTime().getOffsetDateTime());
            message.addField(EntityProperty.TIME);
        }

        DSLContext dslContext = pm.getDslContext();
        long count = 0;
        if (!update.isEmpty()) {
            count = dslContext.update(table)
                    .set(update)
                    .where(table.getId().equal(id))
                    .execute();
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

            AbstractTableLocationsHistLocations<J> qlhl = tableCollection.tableLocationsHistLocations;
            dslContext.insertInto(qlhl)
                    .set(qlhl.getHistLocationId(), id)
                    .set(qlhl.getLocationId(), lId)
                    .execute();
            LOGGER.debug(LINKED_L_TO_HL, lId, id);
        }
        return message;
    }

    @Override
    public void delete(PostgresPersistenceManager<J> pm, J entityId) throws NoSuchEntityException {
        long count = pm.getDslContext()
                .delete(table)
                .where(table.getId().eq(entityId))
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
        return table.getId();
    }

}
