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

import de.fraunhofer.iosb.ilt.frostserver.model.Datastream;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.HistoricalLocation;
import de.fraunhofer.iosb.ilt.frostserver.model.Location;
import de.fraunhofer.iosb.ilt.frostserver.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.frostserver.model.TaskingCapability;
import de.fraunhofer.iosb.ilt.frostserver.model.Thing;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.Utils;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.Utils.getFieldOrNull;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.CAN_NOT_BE_NULL;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.CHANGED_MULTIPLE_ROWS;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.CREATED_HL;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.LINKED_L_TO_HL;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.LINKED_L_TO_T;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.NO_ID_OR_NOT_FOUND;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.UNLINKED_L_FROM_T;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableDatastreams;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableHistLocations;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableLocationsHistLocations;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableMultiDatastreams;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableThings;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableThingsLocations;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.UTC;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hylke van der Schaaf
 *
 * @param <J> The type of the ID fields.
 */
public class ThingFactory<J extends Comparable> implements EntityFactory<Thing, J> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ThingFactory.class);

    private final EntityFactories<J> entityFactories;
    private final AbstractTableThings<J> table;
    private final TableCollection<J> tableCollection;

    public ThingFactory(EntityFactories<J> factories, AbstractTableThings<J> table) {
        this.entityFactories = factories;
        this.table = table;
        this.tableCollection = factories.tableCollection;
    }

    @Override
    public Thing create(Record tuple, Query query, DataSize dataSize) {
        Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();
        Thing entity = new Thing();
        entity.setName(getFieldOrNull(tuple, table.name));
        entity.setDescription(getFieldOrNull(tuple, table.description));
        J id = getFieldOrNull(tuple, table.getId());
        if (id != null) {
            entity.setId(entityFactories.idFromObject(id));
        }
        if (select.isEmpty() || select.contains(EntityProperty.PROPERTIES)) {
            String props = getFieldOrNull(tuple, table.properties);
            dataSize.increase(props == null ? 0 : props.length());
            entity.setProperties(Utils.jsonToObject(props, Map.class));
        }
        return entity;
    }

    @Override
    public boolean insert(PostgresPersistenceManager<J> pm, Thing t) throws NoSuchEntityException, IncompleteEntityException {
        Map<Field, Object> insert = new HashMap<>();
        insert.put(table.name, t.getName());
        insert.put(table.description, t.getDescription());
        insert.put(table.properties, EntityFactories.objectToJson(t.getProperties()));

        entityFactories.insertUserDefinedId(pm, insert, table.getId(), t);

        DSLContext dslContext = pm.getDslContext();
        Record1<J> result = dslContext.insertInto(table)
                .set(insert)
                .returningResult(table.getId())
                .fetchOne();
        J thingId = result.component1();
        LOGGER.debug("Inserted Thing. Created id = {}.", thingId);
        t.setId(entityFactories.idFromObject(thingId));

        // Create new Locations, if any.
        List<J> locationIds = new ArrayList<>();
        for (Location l : t.getLocations()) {
            entityFactories.entityExistsOrCreate(pm, l);
            J lId = (J) l.getId().getValue();

            AbstractTableThingsLocations<J> qtl = tableCollection.tableThingsLocations;
            dslContext.insertInto(qtl)
                    .set(qtl.getThingId(), thingId)
                    .set(qtl.getLocationId(), lId)
                    .execute();
            LOGGER.debug(LINKED_L_TO_T, lId, thingId);
            locationIds.add(lId);
        }

        // Now link the new locations also to a historicalLocation.
        if (!locationIds.isEmpty()) {
            AbstractTableHistLocations<J> qhl = tableCollection.tableHistLocations;
            Record1<J> newHistLoc = dslContext.insertInto(qhl)
                    .set(qhl.getThingId(), thingId)
                    .set(qhl.time, OffsetDateTime.now(UTC))
                    .returningResult(qhl.getId())
                    .fetchOne();
            J histLocationId = newHistLoc.component1();
            LOGGER.debug(CREATED_HL, histLocationId);

            AbstractTableLocationsHistLocations<J> qlhl = tableCollection.tableLocationsHistLocations;
            for (J locId : locationIds) {
                dslContext.insertInto(qlhl)
                        .set(qlhl.getHistLocationId(), histLocationId)
                        .set(qlhl.getLocationId(), locId)
                        .execute();
                LOGGER.debug(LINKED_L_TO_HL, locId, histLocationId);
            }
        }

        // Create new datastreams, if any.
        for (Datastream ds : t.getDatastreams()) {
            ds.setThing(new Thing(t.getId()));
            ds.complete();
            pm.insert(ds);
        }

        // Create new multiDatastreams, if any.
        for (MultiDatastream mds : t.getMultiDatastreams()) {
            mds.setThing(new Thing(t.getId()));
            mds.complete();
            pm.insert(mds);
        }

        // Create new HistoricalLocations, if any.
        for (HistoricalLocation hl : t.getHistoricalLocations()) {
            hl.setThing(t);
            hl.complete();
            pm.insert(hl);
        }

        // Create new TaskingCapabilities, if any.
        for (TaskingCapability tc : t.getTaskingCapabilities()) {
            tc.setThing(t);
            tc.complete();
            pm.insert(tc);
        }
        return true;
    }

    @Override
    public EntityChangedMessage update(PostgresPersistenceManager<J> pm, Thing t, J thingId) throws NoSuchEntityException, IncompleteEntityException {
        Map<Field, Object> update = new HashMap<>();
        EntityChangedMessage message = new EntityChangedMessage();

        if (t.isSetName()) {
            if (t.getName() == null) {
                throw new IncompleteEntityException("name" + CAN_NOT_BE_NULL);
            }
            update.put(table.name, t.getName());
            message.addField(EntityProperty.NAME);
        }
        if (t.isSetDescription()) {
            if (t.getDescription() == null) {
                throw new IncompleteEntityException(EntityProperty.DESCRIPTION.jsonName + CAN_NOT_BE_NULL);
            }
            update.put(table.description, t.getDescription());
            message.addField(EntityProperty.DESCRIPTION);
        }
        if (t.isSetProperties()) {
            update.put(table.properties, EntityFactories.objectToJson(t.getProperties()));
            message.addField(EntityProperty.PROPERTIES);
        }

        DSLContext dslContext = pm.getDslContext();
        long count = 0;
        if (!update.isEmpty()) {
            count = dslContext.update(table)
                    .set(update)
                    .where(table.getId().equal(thingId))
                    .execute();
        }
        if (count > 1) {
            LOGGER.error("Updating Thing {} caused {} rows to change!", thingId, count);
            throw new IllegalStateException(CHANGED_MULTIPLE_ROWS);
        }
        LOGGER.debug("Updated Thing {}", thingId);

        linkExistingDatastreams(t, pm, dslContext, thingId);

        linkExistingMultiDatastreams(t, pm, dslContext, thingId);

        linkExistingLocations(t, dslContext, thingId, pm);
        return message;
    }

    private void linkExistingLocations(Thing t, DSLContext dslContext, J thingId, PostgresPersistenceManager<J> pm) throws NoSuchEntityException {
        if (t.getLocations().isEmpty()) {
            return;
        }
        // Unlink old Locations from Thing.
        AbstractTableThingsLocations<J> ttl = tableCollection.tableThingsLocations;
        long count = dslContext.delete(ttl).where(ttl.getThingId().eq(thingId)).execute();
        LOGGER.debug(UNLINKED_L_FROM_T, count, thingId);

        // Link new locations to Thing, track the ids.
        List<J> locationIds = new ArrayList<>();
        for (Location l : t.getLocations()) {
            if (l.getId() == null || !entityFactories.entityExists(pm, l)) {
                throw new NoSuchEntityException("Location with no id.");
            }
            J locationId = (J) l.getId().getValue();

            dslContext.insertInto(ttl)
                    .set(ttl.getThingId(), thingId)
                    .set(ttl.getLocationId(), locationId)
                    .execute();
            LOGGER.debug(LINKED_L_TO_T, locationId, thingId);
            locationIds.add(locationId);
        }

        // Now link the newly linked locations also to a historicalLocation.
        if (!locationIds.isEmpty()) {
            AbstractTableHistLocations<J> thl = tableCollection.tableHistLocations;
            Record1<J> insert = dslContext.insertInto(thl)
                    .set(thl.getThingId(), thingId)
                    .set(thl.time, OffsetDateTime.now(UTC))
                    .returningResult(thl.getId())
                    .fetchOne();
            J histLocationId = insert.component1();
            LOGGER.debug(CREATED_HL, histLocationId);

            AbstractTableLocationsHistLocations<J> qlhl = tableCollection.tableLocationsHistLocations;
            for (J locId : locationIds) {
                dslContext.insertInto(qlhl)
                        .set(qlhl.getHistLocationId(), histLocationId)
                        .set(qlhl.getLocationId(), locId)
                        .execute();
                LOGGER.debug(LINKED_L_TO_HL, locId, histLocationId);
            }

            HistoricalLocation newHl = (HistoricalLocation) pm.get(EntityType.HISTORICALLOCATION, pm.getIdManager().fromObject(histLocationId));
            newHl.getThing().setExportObject(true);
            pm.getEntityChangedMessages().add(
                    new EntityChangedMessage()
                            .setEventType(EntityChangedMessage.Type.CREATE)
                            .setEntity(newHl)
            );
        }
    }

    private void linkExistingMultiDatastreams(Thing t, PostgresPersistenceManager<J> pm, DSLContext dslContext, J thingId) throws NoSuchEntityException {
        // Link existing MultiDatastreams to the thing.
        for (MultiDatastream mds : t.getMultiDatastreams()) {
            if (mds.getId() == null || !entityFactories.entityExists(pm, mds)) {
                throw new NoSuchEntityException("MultiDatastream" + NO_ID_OR_NOT_FOUND);
            }
            J mdsId = (J) mds.getId().getValue();
            AbstractTableMultiDatastreams<J> qmds = tableCollection.tableMultiDatastreams;
            long mdsCount = dslContext.update(qmds)
                    .set(qmds.getThingId(), thingId)
                    .where(qmds.getId().eq(mdsId))
                    .execute();
            if (mdsCount > 0) {
                LOGGER.debug("Assigned multiDatastream {} to thing {}.", mdsId, thingId);
            }
        }
    }

    private void linkExistingDatastreams(Thing t, PostgresPersistenceManager<J> pm, DSLContext dslContext, J thingId) throws NoSuchEntityException {
        // Link existing Datastreams to the thing.
        for (Datastream ds : t.getDatastreams()) {
            if (ds.getId() == null || !entityFactories.entityExists(pm, ds)) {
                throw new NoSuchEntityException("Datastream" + NO_ID_OR_NOT_FOUND);
            }
            J dsId = (J) ds.getId().getValue();
            AbstractTableDatastreams<J> qds = tableCollection.tableDatastreams;
            long dsCount = dslContext.update(qds)
                    .set(qds.getThingId(), thingId)
                    .where(qds.getId().eq(dsId))
                    .execute();
            if (dsCount > 0) {
                LOGGER.debug("Assigned datastream {} to thing {}.", dsId, thingId);
            }
        }
    }

    @Override
    public void delete(PostgresPersistenceManager<J> pm, J entityId) throws NoSuchEntityException {
        long count = pm.getDslContext()
                .delete(table)
                .where(table.getId().eq(entityId))
                .execute();
        if (count == 0) {
            throw new NoSuchEntityException("Thing " + entityId + " not found.");
        }
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.THING;
    }

    @Override
    public Field<J> getPrimaryKey() {
        return table.getId();
    }

}
