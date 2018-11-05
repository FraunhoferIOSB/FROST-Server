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
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.builder.DatastreamBuilder;
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.NavigationProperty;
import de.fraunhofer.iosb.ilt.sta.path.Property;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.DataSize;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories.CAN_NOT_BE_NULL;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories.CHANGED_MULTIPLE_ROWS;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.EntityFactories.NO_ID_OR_NOT_FOUND;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.Utils;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQObservations;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.QCollection;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.util.GeoHelper;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.sta.util.NoSuchEntityException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.geojson.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hylke van der Schaaf
 * @param <I> The type of path used for the ID fields.
 * @param <J> The type of the ID fields.
 */
public class DatastreamFactory<I extends SimpleExpression<J> & Path<J>, J> implements EntityFactory<Datastream, I, J> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DatastreamFactory.class);

    private final EntityFactories<I, J> entityFactories;
    private final AbstractQDatastreams<?, I, J> qInstance;
    private final QCollection<I, J> qCollection;

    public DatastreamFactory(EntityFactories<I, J> factories, AbstractQDatastreams<?, I, J> qInstance) {
        this.entityFactories = factories;
        this.qInstance = qInstance;
        this.qCollection = factories.qCollection;
    }

    @Override
    public Datastream create(Tuple tuple, Query query, DataSize dataSize) {
        Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();
        Datastream entity = new Datastream();
        entity.setName(tuple.get(qInstance.name));
        entity.setDescription(tuple.get(qInstance.description));
        J entityId = entityFactories.getIdFromTuple(tuple, qInstance.getId());
        if (entityId != null) {
            entity.setId(entityFactories.idFromObject(entityId));
        }
        entity.setObservationType(tuple.get(qInstance.observationType));
        String observedArea = tuple.get(qInstance.observedArea.asText());
        if (observedArea != null) {
            try {
                Polygon polygon = GeoHelper.parsePolygon(observedArea);
                entity.setObservedArea(polygon);
            } catch (IllegalArgumentException e) {
                // It's not a polygon, probably a point or a line.
            }
        }
        ObservedProperty op = entityFactories.observedProperyFromId(tuple, qInstance.getObsPropertyId());
        entity.setObservedProperty(op);
        Timestamp pTimeStart = tuple.get(qInstance.phenomenonTimeStart);
        Timestamp pTimeEnd = tuple.get(qInstance.phenomenonTimeEnd);
        if (pTimeStart != null && pTimeEnd != null) {
            entity.setPhenomenonTime(Utils.intervalFromTimes(pTimeStart, pTimeEnd));
        }
        Timestamp rTimeStart = tuple.get(qInstance.resultTimeStart);
        Timestamp rTimeEnd = tuple.get(qInstance.resultTimeEnd);
        if (rTimeStart != null && rTimeEnd != null) {
            entity.setResultTime(Utils.intervalFromTimes(rTimeStart, rTimeEnd));
        }
        if (select.isEmpty() || select.contains(EntityProperty.PROPERTIES)) {
            String props = tuple.get(qInstance.properties);
            entity.setProperties(Utils.jsonToObject(props, Map.class));
        }
        entity.setSensor(entityFactories.sensorFromId(tuple, qInstance.getSensorId()));
        entity.setThing(entityFactories.thingFromId(tuple, qInstance.getThingId()));
        entity.setUnitOfMeasurement(new UnitOfMeasurement(tuple.get(qInstance.unitName), tuple.get(qInstance.unitSymbol), tuple.get(qInstance.unitDefinition)));
        return entity;
    }

    @Override
    public boolean insert(PostgresPersistenceManager<I, J> pm, Datastream ds) throws NoSuchEntityException, IncompleteEntityException {
        // First check ObservedPropery, Sensor and Thing
        ObservedProperty op = ds.getObservedProperty();
        entityFactories.entityExistsOrCreate(pm, op);

        Sensor s = ds.getSensor();
        entityFactories.entityExistsOrCreate(pm, s);

        Thing t = ds.getThing();
        entityFactories.entityExistsOrCreate(pm, t);

        SQLQueryFactory qFactory = pm.createQueryFactory();

        AbstractQDatastreams<? extends AbstractQDatastreams, I, J> qd = qCollection.qDatastreams;
        SQLInsertClause insert = qFactory.insert(qd);
        insert.set(qd.name, ds.getName());
        insert.set(qd.description, ds.getDescription());
        insert.set(qd.observationType, ds.getObservationType());
        insert.set(qd.unitDefinition, ds.getUnitOfMeasurement().getDefinition());
        insert.set(qd.unitName, ds.getUnitOfMeasurement().getName());
        insert.set(qd.unitSymbol, ds.getUnitOfMeasurement().getSymbol());
        insert.set(qd.properties, EntityFactories.objectToJson(ds.getProperties()));

        insert.set(qd.phenomenonTimeStart, new Timestamp(PostgresPersistenceManager.DATETIME_MAX.getMillis()));
        insert.set(qd.phenomenonTimeEnd, new Timestamp(PostgresPersistenceManager.DATETIME_MIN.getMillis()));
        insert.set(qd.resultTimeStart, new Timestamp(PostgresPersistenceManager.DATETIME_MAX.getMillis()));
        insert.set(qd.resultTimeEnd, new Timestamp(PostgresPersistenceManager.DATETIME_MIN.getMillis()));

        insert.set(qd.getObsPropertyId(), (J) op.getId().getValue());
        insert.set(qd.getSensorId(), (J) s.getId().getValue());
        insert.set(qd.getThingId(), (J) t.getId().getValue());

        entityFactories.insertUserDefinedId(pm, insert, qd.getId(), ds);

        J datastreamId = insert.executeWithKey(qd.getId());
        LOGGER.debug("Inserted datastream. Created id = {}.", datastreamId);
        ds.setId(entityFactories.idFromObject(datastreamId));

        // Create Observations, if any.
        for (Observation o : ds.getObservations()) {
            o.setDatastream(new DatastreamBuilder().setId(ds.getId()).build());
            o.complete();
            pm.insert(o);
        }

        return true;
    }

    @Override
    public EntityChangedMessage update(PostgresPersistenceManager<I, J> pm, Datastream d, J dsId) throws NoSuchEntityException, IncompleteEntityException {

        SQLQueryFactory qFactory = pm.createQueryFactory();
        AbstractQDatastreams<? extends AbstractQDatastreams, I, J> qd = qCollection.qDatastreams;

        SQLUpdateClause update = qFactory.update(qd);
        EntityChangedMessage message = new EntityChangedMessage();

        if (d.isSetName()) {
            if (d.getName() == null) {
                throw new IncompleteEntityException("name" + CAN_NOT_BE_NULL);
            }
            update.set(qd.name, d.getName());
            message.addField(EntityProperty.NAME);
        }
        if (d.isSetDescription()) {
            if (d.getDescription() == null) {
                throw new IncompleteEntityException(EntityProperty.DESCRIPTION.jsonName + CAN_NOT_BE_NULL);
            }
            update.set(qd.description, d.getDescription());
            message.addField(EntityProperty.DESCRIPTION);
        }
        if (d.isSetObservationType()) {
            if (d.getObservationType() == null) {
                throw new IncompleteEntityException("observationType" + CAN_NOT_BE_NULL);
            }
            update.set(qd.observationType, d.getObservationType());
            message.addField(EntityProperty.OBSERVATIONTYPE);
        }
        if (d.isSetProperties()) {
            update.set(qd.properties, EntityFactories.objectToJson(d.getProperties()));
            message.addField(EntityProperty.PROPERTIES);
        }
        if (d.isSetObservedProperty()) {
            if (!entityFactories.entityExists(pm, d.getObservedProperty())) {
                throw new NoSuchEntityException("ObservedProperty with no id or not found.");
            }
            update.set(qd.getObsPropertyId(), (J) d.getObservedProperty().getId().getValue());
            message.addField(NavigationProperty.OBSERVEDPROPERTY);
        }
        if (d.isSetSensor()) {
            if (!entityFactories.entityExists(pm, d.getSensor())) {
                throw new NoSuchEntityException("Sensor with no id or not found.");
            }
            update.set(qd.getSensorId(), (J) d.getSensor().getId().getValue());
            message.addField(NavigationProperty.SENSOR);
        }
        if (d.isSetThing()) {
            if (!entityFactories.entityExists(pm, d.getThing())) {
                throw new NoSuchEntityException("Thing with no id or not found.");
            }
            update.set(qd.getThingId(), (J) d.getThing().getId().getValue());
            message.addField(NavigationProperty.THING);
        }
        if (d.isSetUnitOfMeasurement()) {
            if (d.getUnitOfMeasurement() == null) {
                throw new IncompleteEntityException("unitOfMeasurement" + CAN_NOT_BE_NULL);
            }
            UnitOfMeasurement uom = d.getUnitOfMeasurement();
            update.set(qd.unitDefinition, uom.getDefinition());
            update.set(qd.unitName, uom.getName());
            update.set(qd.unitSymbol, uom.getSymbol());
            message.addField(EntityProperty.UNITOFMEASUREMENT);
        }

        update.where(qd.getId().eq(dsId));
        long count = 0;
        if (!update.isEmpty()) {
            count = update.execute();
        }
        if (count > 1) {
            LOGGER.error("Updating Datastream {} caused {} rows to change!", dsId, count);
            throw new IllegalStateException(CHANGED_MULTIPLE_ROWS);
        }

        // Link existing Observations to the Datastream.
        for (Observation o : d.getObservations()) {
            if (o.getId() == null || !entityFactories.entityExists(pm, o)) {
                throw new NoSuchEntityException(EntityType.OBSERVATION.entityName + NO_ID_OR_NOT_FOUND);
            }
            J obsId = (J) o.getId().getValue();
            AbstractQObservations<? extends AbstractQObservations, I, J> qo = qCollection.qObservations;
            long oCount = qFactory.update(qo)
                    .set(qo.getDatastreamId(), dsId)
                    .where(qo.getId().eq(obsId))
                    .execute();
            if (oCount > 0) {
                LOGGER.debug("Assigned datastream {} to Observation {}.", dsId, obsId);
            }
        }

        LOGGER.debug("Updated Datastream {}", dsId);
        return message;
    }

    @Override
    public void delete(PostgresPersistenceManager<I, J> pm, J entityId) throws NoSuchEntityException {
        long count = pm.createQueryFactory()
                .delete(qInstance)
                .where(qInstance.getId().eq(entityId))
                .execute();
        if (count == 0) {
            throw new NoSuchEntityException("Datastream " + entityId + " not found.");
        }
    }

    @Override
    public I getPrimaryKey() {
        return qInstance.getId();
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.DATASTREAM;
    }

}
