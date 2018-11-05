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
package de.fraunhofer.iosb.ilt.sta.persistence.postgres.factories;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLUpdateClause;
import de.fraunhofer.iosb.ilt.sta.messagebus.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.Location;
import de.fraunhofer.iosb.ilt.sta.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.builder.ThingBuilder;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.Property;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.DataSize;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories.CAN_NOT_BE_NULL;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories.CHANGED_MULTIPLE_ROWS;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories.CREATED_HL;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories.LINKED_L_TO_HL;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories.LINKED_L_TO_T;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories.NO_ID_OR_NOT_FOUND;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories.UNLINKED_L_FROM_T;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.Utils;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQLocationsHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQMultiDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQThings;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQThingsLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.QCollection;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.sta.util.NoSuchEntityException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hylke van der Schaaf
 * @param <I> The type of path used for the ID fields.
 * @param <J> The type of the ID fields.
 */
public class ThingFactory<I extends SimpleExpression<J> & Path<J>, J> implements EntityFactory<Thing, I, J> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ThingFactory.class);

    private final EntityFactories<I, J> entityFactories;
    private final AbstractQThings<?, I, J> qInstance;
    private final QCollection<I, J> qCollection;

    public ThingFactory(EntityFactories<I, J> factories, AbstractQThings<?, I, J> qInstance) {
        this.entityFactories = factories;
        this.qInstance = qInstance;
        this.qCollection = factories.qCollection;
    }

    @Override
    public Thing create(Tuple tuple, Query query, DataSize dataSize) {
        Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();
        Thing entity = new Thing();
        entity.setName(tuple.get(qInstance.name));
        entity.setDescription(tuple.get(qInstance.description));
        J id = entityFactories.getIdFromTuple(tuple, qInstance.getId());
        if (id != null) {
            entity.setId(entityFactories.idFromObject(id));
        }
        if (select.isEmpty() || select.contains(EntityProperty.PROPERTIES)) {
            String props = tuple.get(qInstance.properties);
            dataSize.increase(props == null ? 0 : props.length());
            entity.setProperties(Utils.jsonToObject(props, Map.class));
        }
        return entity;
    }

    @Override
    public boolean insert(PostgresPersistenceManager<I, J> pm, Thing t) throws NoSuchEntityException, IncompleteEntityException {
        SQLQueryFactory qFactory = pm.createQueryFactory();
        AbstractQThings<? extends AbstractQThings, I, J> qt = qCollection.qThings;
        SQLInsertClause insert = qFactory.insert(qt);
        insert.set(qt.name, t.getName());
        insert.set(qt.description, t.getDescription());
        insert.set(qt.properties, EntityFactories.objectToJson(t.getProperties()));

        entityFactories.insertUserDefinedId(pm, insert, qt.getId(), t);

        J thingId = insert.executeWithKey(qt.getId());
        LOGGER.debug("Inserted Thing. Created id = {}.", thingId);
        t.setId(entityFactories.idFromObject(thingId));

        // Create new Locations, if any.
        List<J> locationIds = new ArrayList<>();
        for (Location l : t.getLocations()) {
            entityFactories.entityExistsOrCreate(pm, l);
            J lId = (J) l.getId().getValue();

            AbstractQThingsLocations<? extends AbstractQThingsLocations, I, J> qtl = qCollection.qThingsLocations;
            insert = qFactory.insert(qtl);
            insert.set(qtl.getThingId(), thingId);
            insert.set(qtl.getLocationId(), lId);
            insert.execute();
            LOGGER.debug(LINKED_L_TO_T, lId, thingId);
            locationIds.add(lId);
        }

        // Now link the new locations also to a historicalLocation.
        if (!locationIds.isEmpty()) {
            AbstractQHistLocations<? extends AbstractQHistLocations, I, J> qhl = qCollection.qHistLocations;
            insert = qFactory.insert(qhl);
            insert.set(qhl.getThingId(), thingId);
            insert.set(qhl.time, new Timestamp(Calendar.getInstance().getTimeInMillis()));
            // TODO: maybe use histLocationId based on locationIds
            J histLocationId = insert.executeWithKey(qhl.getId());
            LOGGER.debug(CREATED_HL, histLocationId);

            AbstractQLocationsHistLocations<? extends AbstractQLocationsHistLocations, I, J> qlhl = qCollection.qLocationsHistLocations;
            for (J locId : locationIds) {
                qFactory.insert(qlhl)
                        .set(qlhl.getHistLocationId(), histLocationId)
                        .set(qlhl.getLocationId(), locId)
                        .execute();
                LOGGER.debug(LINKED_L_TO_HL, locId, histLocationId);
            }
        }

        // Create new datastreams, if any.
        for (Datastream ds : t.getDatastreams()) {
            ds.setThing(new ThingBuilder().setId(t.getId()).build());
            ds.complete();
            pm.insert(ds);
        }

        // Create new multiDatastreams, if any.
        for (MultiDatastream mds : t.getMultiDatastreams()) {
            mds.setThing(new ThingBuilder().setId(t.getId()).build());
            mds.complete();
            pm.insert(mds);
        }

        // TODO: if we allow the creation of historicalLocations through Things
        // then we have to be able to link those to Locations we might have just created.
        // However, id juggling will be needed!
        return true;
    }

    @Override
    public EntityChangedMessage update(PostgresPersistenceManager<I, J> pm, Thing t, J thingId) throws NoSuchEntityException, IncompleteEntityException {
        SQLQueryFactory qFactory = pm.createQueryFactory();
        AbstractQThings<? extends AbstractQThings, I, J> qt = qCollection.qThings;
        SQLUpdateClause update = qFactory.update(qt);
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
        update.where(qt.getId().eq(thingId));
        long count = 0;
        if (!update.isEmpty()) {
            count = update.execute();
        }
        if (count > 1) {
            LOGGER.error("Updating Thing {} caused {} rows to change!", thingId, count);
            throw new IllegalStateException(CHANGED_MULTIPLE_ROWS);
        }
        LOGGER.debug("Updated Thing {}", thingId);

        // Link existing Datastreams to the thing.
        for (Datastream ds : t.getDatastreams()) {
            if (ds.getId() == null || !entityFactories.entityExists(pm, ds)) {
                throw new NoSuchEntityException("Datastream" + NO_ID_OR_NOT_FOUND);
            }
            J dsId = (J) ds.getId().getValue();
            AbstractQDatastreams<? extends AbstractQDatastreams, I, J> qds = qCollection.qDatastreams;
            long dsCount = qFactory.update(qds)
                    .set(qds.getThingId(), thingId)
                    .where(qds.getId().eq(dsId))
                    .execute();
            if (dsCount > 0) {
                LOGGER.debug("Assigned datastream {} to thing {}.", dsId, thingId);
            }
        }

        // Link existing MultiDatastreams to the thing.
        for (MultiDatastream mds : t.getMultiDatastreams()) {
            if (mds.getId() == null || !entityFactories.entityExists(pm, mds)) {
                throw new NoSuchEntityException("MultiDatastream" + NO_ID_OR_NOT_FOUND);
            }
            J mdsId = (J) mds.getId().getValue();
            AbstractQMultiDatastreams<? extends AbstractQMultiDatastreams, I, J> qmds = qCollection.qMultiDatastreams;
            long mdsCount = qFactory.update(qmds)
                    .set(qmds.getThingId(), thingId)
                    .where(qmds.getId().eq(mdsId))
                    .execute();
            if (mdsCount > 0) {
                LOGGER.debug("Assigned multiDatastream {} to thing {}.", mdsId, thingId);
            }
        }

        // Link existing locations to the thing.
        if (!t.getLocations().isEmpty()) {
            // Unlink old Locations from Thing.
            AbstractQThingsLocations<? extends AbstractQThingsLocations, I, J> qtl = qCollection.qThingsLocations;
            count = qFactory.delete(qtl).where(qtl.getThingId().eq(thingId)).execute();
            LOGGER.debug(UNLINKED_L_FROM_T, count, thingId);

            // Link new locations to Thing, track the ids.
            List<J> locationIds = new ArrayList<>();
            for (Location l : t.getLocations()) {
                if (l.getId() == null || !entityFactories.entityExists(pm, l)) {
                    throw new NoSuchEntityException("Location with no id.");
                }
                J locationId = (J) l.getId().getValue();

                SQLInsertClause insert = qFactory.insert(qtl);
                insert.set(qtl.getThingId(), thingId);
                insert.set(qtl.getLocationId(), locationId);
                insert.execute();
                LOGGER.debug(LINKED_L_TO_T, locationId, thingId);
                locationIds.add(locationId);
            }

            // Now link the newly linked locations also to a historicalLocation.
            if (!locationIds.isEmpty()) {
                AbstractQHistLocations<? extends AbstractQHistLocations, I, J> qhl = qCollection.qHistLocations;
                SQLInsertClause insert = qFactory.insert(qhl);
                insert.set(qhl.getThingId(), thingId);
                insert.set(qhl.time, new Timestamp(Calendar.getInstance().getTimeInMillis()));
                // TODO: maybe use histLocationId based on locationIds
                J histLocationId = insert.executeWithKey(qhl.getId());
                LOGGER.debug(CREATED_HL, histLocationId);

                AbstractQLocationsHistLocations<? extends AbstractQLocationsHistLocations, I, J> qlhl = qCollection.qLocationsHistLocations;
                for (J locId : locationIds) {
                    qFactory.insert(qlhl)
                            .set(qlhl.getHistLocationId(), histLocationId)
                            .set(qlhl.getLocationId(), locId)
                            .execute();
                    LOGGER.debug(LINKED_L_TO_HL, locId, histLocationId);
                }
            }
        }
        return message;
    }

    @Override
    public I getPrimaryKey() {
        return qInstance.getId();
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.THING;
    }

}
