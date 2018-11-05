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
import de.fraunhofer.iosb.ilt.sta.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.builder.MultiDatastreamBuilder;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySet;
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
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQMultiDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQMultiDatastreamsObsProperties;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQObservations;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.QCollection;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.util.GeoHelper;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.sta.util.NoSuchEntityException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
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
public class MultiDatastreamFactory<I extends SimpleExpression<J> & Path<J>, J> implements EntityFactory<MultiDatastream, I, J> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiDatastreamFactory.class);

    private final EntityFactories<I, J> entityFactories;
    private final AbstractQMultiDatastreams<?, I, J> qInstance;
    private final QCollection<I, J> qCollection;

    public MultiDatastreamFactory(EntityFactories<I, J> factories, AbstractQMultiDatastreams<?, I, J> qInstance) {
        this.entityFactories = factories;
        this.qInstance = qInstance;
        this.qCollection = factories.qCollection;
    }

    @Override
    public MultiDatastream create(Tuple tuple, Query query, DataSize dataSize) {
        Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();
        MultiDatastream entity = new MultiDatastream();
        entity.setName(tuple.get(qInstance.name));
        entity.setDescription(tuple.get(qInstance.description));
        J id = entityFactories.getIdFromTuple(tuple, qInstance.getId());
        if (id != null) {
            entity.setId(entityFactories.idFromObject(id));
        }
        List<String> observationTypes = Utils.jsonToObject(tuple.get(qInstance.observationTypes), EntityFactories.TYPE_LIST_STRING);
        entity.setMultiObservationDataTypes(observationTypes);
        String observedArea = tuple.get(qInstance.observedArea.asText());
        if (observedArea != null) {
            try {
                Polygon polygon = GeoHelper.parsePolygon(observedArea);
                entity.setObservedArea(polygon);
            } catch (IllegalArgumentException e) {
                // It's not a polygon, probably a point or a line.
            }
        }
        Timestamp pTimeStart = tuple.get(qInstance.phenomenonTimeStart);
        Timestamp pTimeEnd = tuple.get(qInstance.phenomenonTimeEnd);
        if (pTimeStart != null && pTimeEnd != null) {
            entity.setPhenomenonTime(Utils.intervalFromTimes(pTimeStart, pTimeEnd));
        }
        Timestamp rTimeEnd = tuple.get(qInstance.resultTimeEnd);
        Timestamp rTimeStart = tuple.get(qInstance.resultTimeStart);
        if (rTimeStart != null && rTimeEnd != null) {
            entity.setResultTime(Utils.intervalFromTimes(rTimeStart, rTimeEnd));
        }
        if (select.isEmpty() || select.contains(EntityProperty.PROPERTIES)) {
            String props = tuple.get(qInstance.properties);
            entity.setProperties(Utils.jsonToObject(props, Map.class));
        }
        entity.setSensor(entityFactories.sensorFromId(tuple, qInstance.getSensorId()));
        entity.setThing(entityFactories.thingFromId(tuple, qInstance.getThingId()));
        List<UnitOfMeasurement> units = Utils.jsonToObject(tuple.get(qInstance.unitOfMeasurements), EntityFactories.TYPE_LIST_UOM);
        entity.setUnitOfMeasurements(units);
        return entity;
    }

    @Override
    public boolean insert(PostgresPersistenceManager<I, J> pm, MultiDatastream ds) throws NoSuchEntityException, IncompleteEntityException {
        // First check Sensor and Thing
        Sensor s = ds.getSensor();
        entityFactories.entityExistsOrCreate(pm, s);

        Thing t = ds.getThing();
        entityFactories.entityExistsOrCreate(pm, t);

        SQLQueryFactory qFactory = pm.createQueryFactory();

        AbstractQMultiDatastreams<? extends AbstractQMultiDatastreams, I, J> qd = qCollection.qMultiDatastreams;
        SQLInsertClause insert = qFactory.insert(qd);
        insert.set(qd.name, ds.getName());
        insert.set(qd.description, ds.getDescription());
        insert.set(qd.observationTypes, EntityFactories.objectToJson(ds.getMultiObservationDataTypes()));
        insert.set(qd.unitOfMeasurements, EntityFactories.objectToJson(ds.getUnitOfMeasurements()));
        insert.set(qd.properties, EntityFactories.objectToJson(ds.getProperties()));

        insert.set(qd.phenomenonTimeStart, new Timestamp(PostgresPersistenceManager.DATETIME_MAX.getMillis()));
        insert.set(qd.phenomenonTimeEnd, new Timestamp(PostgresPersistenceManager.DATETIME_MIN.getMillis()));
        insert.set(qd.resultTimeStart, new Timestamp(PostgresPersistenceManager.DATETIME_MAX.getMillis()));
        insert.set(qd.resultTimeEnd, new Timestamp(PostgresPersistenceManager.DATETIME_MIN.getMillis()));

        insert.set(qd.getSensorId(), (J) s.getId().getValue());
        insert.set(qd.getThingId(), (J) t.getId().getValue());

        entityFactories.insertUserDefinedId(pm, insert, qd.getId(), ds);

        J multiDatastreamId = insert.executeWithKey(qd.getId());
        LOGGER.debug("Inserted multiDatastream. Created id = {}.", multiDatastreamId);
        ds.setId(entityFactories.idFromObject(multiDatastreamId));

        // Create new Locations, if any.
        EntitySet<ObservedProperty> ops = ds.getObservedProperties();
        int rank = 0;
        for (ObservedProperty op : ops) {
            entityFactories.entityExistsOrCreate(pm, op);
            J opId = (J) op.getId().getValue();

            AbstractQMultiDatastreamsObsProperties<? extends AbstractQMultiDatastreamsObsProperties, I, J> qMdOp = qCollection.qMultiDatastreamsObsProperties;
            insert = qFactory.insert(qMdOp);
            insert.set(qMdOp.getMultiDatastreamId(), multiDatastreamId);
            insert.set(qMdOp.getObsPropertyId(), opId);
            insert.set(qMdOp.rank, rank);
            insert.execute();
            LOGGER.debug("Linked MultiDatastream {} to ObservedProperty {} with rank {}.", multiDatastreamId, opId, rank);
            rank++;
        }

        // Create Observations, if any.
        for (Observation o : ds.getObservations()) {
            o.setMultiDatastream(new MultiDatastreamBuilder().setId(ds.getId()).build());
            o.complete();
            pm.insert(o);
        }

        return true;
    }

    @Override
    public EntityChangedMessage update(PostgresPersistenceManager<I, J> pm, MultiDatastream md, J mdsId) throws NoSuchEntityException, IncompleteEntityException {
        SQLQueryFactory qFactory = pm.createQueryFactory();
        AbstractQMultiDatastreams<? extends AbstractQMultiDatastreams, I, J> qmd = qCollection.qMultiDatastreams;
        SQLUpdateClause update = qFactory.update(qmd);
        EntityChangedMessage message = new EntityChangedMessage();

        if (md.isSetName()) {
            if (md.getName() == null) {
                throw new IncompleteEntityException("name" + CAN_NOT_BE_NULL);
            }
            update.set(qmd.name, md.getName());
            message.addField(EntityProperty.NAME);
        }
        if (md.isSetDescription()) {
            if (md.getDescription() == null) {
                throw new IncompleteEntityException(EntityProperty.DESCRIPTION.jsonName + CAN_NOT_BE_NULL);
            }
            update.set(qmd.description, md.getDescription());
            message.addField(EntityProperty.DESCRIPTION);
        }
        if (md.isSetProperties()) {
            update.set(qmd.properties, EntityFactories.objectToJson(md.getProperties()));
            message.addField(EntityProperty.PROPERTIES);
        }

        if (md.isSetSensor()) {
            if (!entityFactories.entityExists(pm, md.getSensor())) {
                throw new NoSuchEntityException("Sensor with no id or not found.");
            }
            update.set(qmd.getSensorId(), (J) md.getSensor().getId().getValue());
            message.addField(NavigationProperty.SENSOR);
        }
        if (md.isSetThing()) {
            if (!entityFactories.entityExists(pm, md.getThing())) {
                throw new NoSuchEntityException("Thing with no id or not found.");
            }
            update.set(qmd.getThingId(), (J) md.getThing().getId().getValue());
            message.addField(NavigationProperty.THING);
        }

        MultiDatastream original = (MultiDatastream) pm.get(EntityType.MULTIDATASTREAM, entityFactories.idFromObject(mdsId));
        int countOrig = original.getMultiObservationDataTypes().size();

        int countUom = countOrig;
        if (md.isSetUnitOfMeasurements()) {
            if (md.getUnitOfMeasurements() == null) {
                throw new IncompleteEntityException("unitOfMeasurements" + CAN_NOT_BE_NULL);
            }
            List<UnitOfMeasurement> uoms = md.getUnitOfMeasurements();
            countUom = uoms.size();
            update.set(qmd.unitOfMeasurements, EntityFactories.objectToJson(uoms));
            message.addField(EntityProperty.UNITOFMEASUREMENTS);
        }
        int countDataTypes = countOrig;
        if (md.isSetMultiObservationDataTypes()) {
            List<String> dataTypes = md.getMultiObservationDataTypes();
            if (dataTypes == null) {
                throw new IncompleteEntityException("multiObservationDataTypes" + CAN_NOT_BE_NULL);
            }
            countDataTypes = dataTypes.size();
            update.set(qmd.observationTypes, EntityFactories.objectToJson(dataTypes));
            message.addField(EntityProperty.MULTIOBSERVATIONDATATYPES);
        }
        EntitySet<ObservedProperty> ops = md.getObservedProperties();
        int countOps = countOrig + ops.size();
        for (ObservedProperty op : ops) {
            if (op.getId() == null || !entityFactories.entityExists(pm, op)) {
                throw new NoSuchEntityException("ObservedProperty with no id or not found.");
            }
        }

        if (countUom != countDataTypes) {
            throw new IllegalArgumentException("New number of unitOfMeasurements does not match new number of multiObservationDataTypes.");
        }
        if (countUom != countOps) {
            throw new IllegalArgumentException("New number of unitOfMeasurements does not match new number of ObservedProperties.");
        }

        update.where(qmd.getId().eq(mdsId));
        long count = 0;
        if (!update.isEmpty()) {
            count = update.execute();
        }
        if (count > 1) {
            LOGGER.error("Updating Datastream {} caused {} rows to change!", mdsId, count);
            throw new IllegalStateException(CHANGED_MULTIPLE_ROWS);
        }

        // Link existing ObservedProperties to the MultiDatastream.
        int rank = countOrig;
        for (ObservedProperty op : ops) {
            J opId = (J) op.getId().getValue();
            AbstractQMultiDatastreamsObsProperties<? extends AbstractQMultiDatastreamsObsProperties, I, J> qMdOp = qCollection.qMultiDatastreamsObsProperties;
            long oCount = qFactory.insert(qMdOp)
                    .set(qMdOp.getMultiDatastreamId(), mdsId)
                    .set(qMdOp.getObsPropertyId(), opId)
                    .set(qMdOp.rank, rank)
                    .execute();
            if (oCount > 0) {
                LOGGER.debug("Assigned datastream {} to ObservedProperty {} with rank {}.", mdsId, opId, rank);
            }
            rank++;
        }

        // Link existing Observations to the MultiDatastream.
        for (Observation o : md.getObservations()) {
            if (o.getId() == null || !entityFactories.entityExists(pm, o)) {
                throw new NoSuchEntityException(EntityType.OBSERVATION.entityName + NO_ID_OR_NOT_FOUND);
            }
            J obsId = (J) o.getId().getValue();
            AbstractQObservations<? extends AbstractQObservations, I, J> qo = qCollection.qObservations;
            long oCount = qFactory.update(qo)
                    .set(qo.getDatastreamId(), mdsId)
                    .where(qo.getId().eq(obsId))
                    .execute();
            if (oCount > 0) {
                LOGGER.debug("Assigned multiDatastream {} to Observation {}.", mdsId, obsId);
            }
        }

        LOGGER.debug("Updated multiDatastream {}", mdsId);
        return message;
    }

    @Override
    public I getPrimaryKey() {
        return qInstance.getId();
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.MULTIDATASTREAM;
    }

}
