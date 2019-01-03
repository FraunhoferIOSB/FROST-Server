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
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.Location;
import de.fraunhofer.iosb.ilt.sta.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
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
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.EntityFactories.NO_ID_OR_NOT_FOUND;
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.EntityFactories.UNLINKED_L_FROM_T;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.Utils;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractRecordHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractRecordLocationsHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractRecordThings;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractRecordThingsLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableLocationsHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableMultiDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableThings;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableThingsLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.QCollection;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import static de.fraunhofer.iosb.ilt.sta.settings.CoreSettings.UTC;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.sta.util.NoSuchEntityException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hylke van der Schaaf
 *
 * @param <J> The type of the ID fields.
 */
public class ThingFactory<J> implements EntityFactory<Thing, J> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ThingFactory.class);

    private final EntityFactories<J> entityFactories;
    private final AbstractTableThings<J> table;
    private final QCollection<J> tables;

    public ThingFactory(EntityFactories<J> factories, AbstractTableThings<J> ance) {
        this.entityFactories = factories;
        this.table = ance;
        this.tables = factories.qCollection;
    }

    @Override
    public Thing create(Record tuple, Query query, DataSize dataSize) {
        Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();
        Thing entity = new Thing();
        entity.setName(tuple.get(table.name));
        entity.setDescription(tuple.get(table.description));
        J id = entityFactories.getIdFromRecord(tuple, table.getId());
        if (id != null) {
            entity.setId(entityFactories.idFromObject(id));
        }
        if (select.isEmpty() || select.contains(EntityProperty.PROPERTIES)) {
            Object props = tuple.get(table.properties);
            dataSize.increase(props == null ? 0 : props.toString().length());
            entity.setProperties(Utils.jsonToObject(props.toString(), Map.class));
        }
        return entity;
    }

    @Override
    public boolean insert(PostgresPersistenceManager<J> pm, Thing t) throws NoSuchEntityException, IncompleteEntityException {
        DSLContext dslContext = pm.createDdslContext();
        AbstractTableThings<J> qt = tables.qThings;
        AbstractRecordThings<J> insert = dslContext.newRecord(qt);
        insert.set(qt.name, t.getName());
        insert.set(qt.description, t.getDescription());
        insert.set(qt.properties, EntityFactories.objectToJson(t.getProperties()));

        entityFactories.insertUserDefinedId(pm, insert, qt.getId(), t);

        insert.store();
        J thingId = insert.getId();
        LOGGER.debug("Inserted Thing. Created id = {}.", thingId);
        t.setId(entityFactories.idFromObject(thingId));

        // Create new Locations, if any.
        List<J> locationIds = new ArrayList<>();
        for (Location l : t.getLocations()) {
            entityFactories.entityExistsOrCreate(pm, l);
            J lId = (J) l.getId().getValue();

            AbstractTableThingsLocations<J> qtl = tables.qThingsLocations;
            AbstractRecordThingsLocations<J> linkLoc = dslContext.newRecord(qtl);
            linkLoc.set(qtl.getThingId(), thingId);
            linkLoc.set(qtl.getLocationId(), lId);
            linkLoc.store();
            LOGGER.debug(LINKED_L_TO_T, lId, thingId);
            locationIds.add(lId);
        }

        // Now link the new locations also to a historicalLocation.
        if (!locationIds.isEmpty()) {
            AbstractTableHistLocations<J> qhl = tables.qHistLocations;
            AbstractRecordHistLocations<J> newHistLoc = dslContext.newRecord(qhl);
            newHistLoc.set(qhl.getThingId(), thingId);
            newHistLoc.set(qhl.time, OffsetDateTime.now(UTC));
            newHistLoc.store();
            J histLocationId = newHistLoc.getId();
            LOGGER.debug(CREATED_HL, histLocationId);

            AbstractTableLocationsHistLocations<J> qlhl = tables.qLocationsHistLocations;
            for (J locId : locationIds) {
                AbstractRecordLocationsHistLocations<J> linkedLoc = dslContext.newRecord(qlhl);
                linkedLoc.set(qlhl.getHistLocationId(), histLocationId);
                linkedLoc.set(qlhl.getLocationId(), locId);
                linkedLoc.store();
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

        // TODO: allow the creation of historicalLocations through Things
        return true;
    }

    @Override
    public EntityChangedMessage update(PostgresPersistenceManager<J> pm, Thing t, J thingId) throws NoSuchEntityException, IncompleteEntityException {
        DSLContext dslContext = pm.createDdslContext();
        AbstractTableThings<J> qt = tables.qThings;
        AbstractRecordThings<J> update = dslContext.newRecord(qt);
        EntityChangedMessage message = new EntityChangedMessage();

        if (t.isSetName()) {
            if (t.getName() == null) {
                throw new IncompleteEntityException("name" + CAN_NOT_BE_NULL);
            }
            update.set(qt.name, t.getName());
            message.addField(EntityProperty.NAME);
        }
        if (t.isSetDescription()) {
            if (t.getDescription() == null) {
                throw new IncompleteEntityException(EntityProperty.DESCRIPTION.jsonName + CAN_NOT_BE_NULL);
            }
            update.set(qt.description, t.getDescription());
            message.addField(EntityProperty.DESCRIPTION);
        }
        if (t.isSetProperties()) {
            update.set(qt.properties, EntityFactories.objectToJson(t.getProperties()));
            message.addField(EntityProperty.PROPERTIES);
        }
        update.setId(thingId);
        long count = 0;
        if (update.changed()) {
            count = update.store();
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
        AbstractTableThingsLocations<J> qtl = tables.qThingsLocations;
        long count = dslContext.delete(qtl).where(qtl.getThingId().eq(thingId)).execute();
        LOGGER.debug(UNLINKED_L_FROM_T, count, thingId);

        // Link new locations to Thing, track the ids.
        List<J> locationIds = new ArrayList<>();
        for (Location l : t.getLocations()) {
            if (l.getId() == null || !entityFactories.entityExists(pm, l)) {
                throw new NoSuchEntityException("Location with no id.");
            }
            J locationId = (J) l.getId().getValue();

            AbstractRecordThingsLocations<J> insert = dslContext.newRecord(qtl);
            insert.set(qtl.getThingId(), thingId);
            insert.set(qtl.getLocationId(), locationId);
            insert.store();
            LOGGER.debug(LINKED_L_TO_T, locationId, thingId);
            locationIds.add(locationId);
        }

        // Now link the newly linked locations also to a historicalLocation.
        if (!locationIds.isEmpty()) {
            AbstractTableHistLocations<J> qhl = tables.qHistLocations;
            AbstractRecordHistLocations<J> insert = dslContext.newRecord(qhl);
            insert.set(qhl.getThingId(), thingId);
            insert.set(qhl.time, OffsetDateTime.now(UTC));
            insert.store();
            J histLocationId = insert.getId();
            LOGGER.debug(CREATED_HL, histLocationId);

            AbstractTableLocationsHistLocations<J> qlhl = tables.qLocationsHistLocations;
            for (J locId : locationIds) {
                AbstractRecordLocationsHistLocations<J> linkedLoc = dslContext.newRecord(qlhl);
                linkedLoc.set(qlhl.getHistLocationId(), histLocationId);
                linkedLoc.set(qlhl.getLocationId(), locId);
                linkedLoc.store();
                LOGGER.debug(LINKED_L_TO_HL, locId, histLocationId);
            }
        }
    }

    private void linkExistingMultiDatastreams(Thing t, PostgresPersistenceManager<J> pm, DSLContext dslContext, J thingId) throws NoSuchEntityException {
        // Link existing MultiDatastreams to the thing.
        for (MultiDatastream mds : t.getMultiDatastreams()) {
            if (mds.getId() == null || !entityFactories.entityExists(pm, mds)) {
                throw new NoSuchEntityException("MultiDatastream" + NO_ID_OR_NOT_FOUND);
            }
            J mdsId = (J) mds.getId().getValue();
            AbstractTableMultiDatastreams<J> qmds = tables.qMultiDatastreams;
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
            AbstractTableDatastreams<J> qds = tables.qDatastreams;
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
        long count = pm.createDdslContext()
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
