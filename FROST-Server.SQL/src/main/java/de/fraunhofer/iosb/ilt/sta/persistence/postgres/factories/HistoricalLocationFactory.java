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
import de.fraunhofer.iosb.ilt.sta.model.HistoricalLocation;
import de.fraunhofer.iosb.ilt.sta.model.Location;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.NavigationProperty;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.DataSize;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories.CAN_NOT_BE_NULL;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories.CHANGED_MULTIPLE_ROWS;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories.LINKED_L_TO_HL;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories.LINKED_L_TO_T;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories.UNLINKED_L_FROM_T;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories.insertTimeInstant;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.Utils;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQLocationsHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQThingsLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.QCollection;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.sta.util.NoSuchEntityException;
import java.sql.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hylke van der Schaaf
 * @param <I> The type of path used for the ID fields.
 * @param <J> The type of the ID fields.
 */
public class HistoricalLocationFactory<I extends SimpleExpression<J> & Path<J>, J> implements EntityFactory<HistoricalLocation, I, J> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(HistoricalLocationFactory.class);
    private final EntityFactories<I, J> entityFactories;
    private final AbstractQHistLocations<?, I, J> qInstance;
    private final QCollection<I, J> qCollection;

    public HistoricalLocationFactory(EntityFactories<I, J> factories, AbstractQHistLocations<?, I, J> qInstance) {
        this.entityFactories = factories;
        this.qInstance = qInstance;
        this.qCollection = factories.qCollection;
    }

    @Override
    public HistoricalLocation create(Tuple tuple, Query query, DataSize dataSize) {
        HistoricalLocation entity = new HistoricalLocation();
        J id = entityFactories.getIdFromTuple(tuple, qInstance.getId());
        if (id != null) {
            entity.setId(entityFactories.idFromObject(id));
        }
        entity.setThing(entityFactories.thingFromId(tuple, qInstance.getThingId()));
        entity.setTime(Utils.instantFromTime(tuple.get(qInstance.time)));
        return entity;
    }

    @Override
    public boolean insert(PostgresPersistenceManager<I, J> pm, HistoricalLocation h) throws NoSuchEntityException, IncompleteEntityException {
        Thing t = h.getThing();
        entityFactories.entityExistsOrCreate(pm, t);
        J thingId = (J) h.getThing().getId().getValue();

        Timestamp newTime = new Timestamp(h.getTime().getDateTime().getMillis());

        SQLQueryFactory qFactory = pm.createQueryFactory();
        AbstractQHistLocations<? extends AbstractQHistLocations, I, J> qhl = qCollection.qHistLocations;
        SQLInsertClause insert = qFactory.insert(qhl);
        insert.set(qhl.time, newTime);
        insert.set(qhl.getThingId(), thingId);

        entityFactories.insertUserDefinedId(pm, insert, qhl.getId(), h);

        J generatedId = insert.executeWithKey(qhl.getId());
        LOGGER.debug("Inserted HistoricalLocation. Created id = {}.", generatedId);
        h.setId(entityFactories.idFromObject(generatedId));

        EntitySet<Location> locations = h.getLocations();
        for (Location l : locations) {
            entityFactories.entityExistsOrCreate(pm, l);
            J lId = (J) l.getId().getValue();
            AbstractQLocationsHistLocations<? extends AbstractQLocationsHistLocations, I, J> qlhl = qCollection.qLocationsHistLocations;
            insert = qFactory.insert(qlhl);
            insert.set(qlhl.getHistLocationId(), generatedId);
            insert.set(qlhl.getLocationId(), lId);
            insert.execute();
            LOGGER.debug(LINKED_L_TO_HL, lId, generatedId);
        }

        // https://github.com/opengeospatial/sensorthings/issues/30
        // Check the time of the latest HistoricalLocation of our thing.
        // If this time is earlier than our time, set the Locations of our Thing to our Locations.
        Tuple lastHistLocation = qFactory.select(qhl.all())
                .from(qhl)
                .where(qhl.getThingId().eq(thingId).and(qhl.time.gt(newTime)))
                .orderBy(qhl.time.desc())
                .limit(1).fetchFirst();
        if (lastHistLocation == null) {
            // We are the newest.
            // Unlink old Locations from Thing.
            AbstractQThingsLocations<? extends AbstractQThingsLocations, I, J> qtl = qCollection.qThingsLocations;
            long count = qFactory.delete(qtl).where(qtl.getThingId().eq(thingId)).execute();
            LOGGER.debug(UNLINKED_L_FROM_T, count, thingId);

            // Link new locations to Thing, track the ids.
            for (Location l : h.getLocations()) {
                if (l.getId() == null || !entityFactories.entityExists(pm, l)) {
                    throw new NoSuchEntityException("Location with no id.");
                }
                J locationId = (J) l.getId().getValue();

                qFactory.insert(qtl)
                        .set(qtl.getThingId(), thingId)
                        .set(qtl.getLocationId(), locationId)
                        .execute();
                LOGGER.debug(LINKED_L_TO_T, locationId, thingId);
            }
        }
        return true;
    }

    @Override
    public EntityChangedMessage update(PostgresPersistenceManager<I, J> pm, HistoricalLocation hl, J id) throws IncompleteEntityException {
        SQLQueryFactory qFactory = pm.createQueryFactory();
        AbstractQHistLocations<? extends AbstractQHistLocations, I, J> qhl = qCollection.qHistLocations;
        SQLUpdateClause update = qFactory.update(qhl);
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
            insertTimeInstant(update, qhl.time, hl.getTime());
            message.addField(EntityProperty.TIME);
        }
        update.where(qhl.getId().eq(id));
        long count = 0;
        if (!update.isEmpty()) {
            count = update.execute();
        }
        if (count > 1) {
            LOGGER.error("Updating HistoricalLocation {} caused {} rows to change!", id, count);
            throw new IllegalStateException(CHANGED_MULTIPLE_ROWS);
        }
        LOGGER.debug("Updated HistoricalLocation {}", id);

        // Link existing locations to the HistoricalLocation.
        for (Location l : hl.getLocations()) {
            if (!entityFactories.entityExists(pm, l)) {
                throw new IllegalArgumentException("Unknown Location or Location with no id.");
            }
            J lId = (J) l.getId().getValue();

            AbstractQLocationsHistLocations<? extends AbstractQLocationsHistLocations, I, J> qlhl = qCollection.qLocationsHistLocations;
            SQLInsertClause insert = qFactory.insert(qlhl);
            insert.set(qlhl.getHistLocationId(), id);
            insert.set(qlhl.getLocationId(), lId);
            insert.execute();
            LOGGER.debug(LINKED_L_TO_HL, lId, id);
        }
        return message;
    }

    @Override
    public void delete(PostgresPersistenceManager<I, J> pm, J entityId) throws NoSuchEntityException {
        long count = pm.createQueryFactory()
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
    public I getPrimaryKey() {
        return qInstance.getId();
    }

}
