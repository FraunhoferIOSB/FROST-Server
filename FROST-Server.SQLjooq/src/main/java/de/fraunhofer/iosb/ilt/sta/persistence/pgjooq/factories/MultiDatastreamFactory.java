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
import de.fraunhofer.iosb.ilt.sta.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.NavigationProperty;
import de.fraunhofer.iosb.ilt.sta.path.Property;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.DataSize;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.Utils;
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.Utils.getFieldOrNull;
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.factories.EntityFactories.CAN_NOT_BE_NULL;
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.factories.EntityFactories.CHANGED_MULTIPLE_ROWS;
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.factories.EntityFactories.NO_ID_OR_NOT_FOUND;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableMultiDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableMultiDatastreamsObsProperties;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableObservations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.QCollection;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.util.GeoHelper;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.sta.util.NoSuchEntityException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.geojson.Polygon;
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
public class MultiDatastreamFactory<J> implements EntityFactory<MultiDatastream, J> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiDatastreamFactory.class);

    private final EntityFactories<J> entityFactories;
    private final AbstractTableMultiDatastreams<J> qInstance;
    private final QCollection<J> qCollection;

    public MultiDatastreamFactory(EntityFactories<J> factories, AbstractTableMultiDatastreams<J> qInstance) {
        this.entityFactories = factories;
        this.qInstance = qInstance;
        this.qCollection = factories.qCollection;
    }

    @Override
    public MultiDatastream create(Record tuple, Query query, DataSize dataSize) {
        Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();
        MultiDatastream entity = new MultiDatastream();
        entity.setName(getFieldOrNull(tuple, qInstance.name));
        entity.setDescription(getFieldOrNull(tuple, qInstance.description));
        J id = getFieldOrNull(tuple, qInstance.getId());
        if (id != null) {
            entity.setId(entityFactories.idFromObject(id));
        }
        List<String> observationTypes = Utils.jsonToObject(getFieldOrNull(tuple, qInstance.observationTypes), EntityFactories.TYPE_LIST_STRING);
        entity.setMultiObservationDataTypes(observationTypes);
        String observedArea = getFieldOrNull(tuple, qInstance.observedAreaText);
        if (observedArea != null) {
            try {
                Polygon polygon = GeoHelper.parsePolygon(observedArea);
                entity.setObservedArea(polygon);
            } catch (IllegalArgumentException e) {
                // It's not a polygon, probably a point or a line.
            }
        }
        OffsetDateTime pTimeStart = getFieldOrNull(tuple, qInstance.phenomenonTimeStart);
        OffsetDateTime pTimeEnd = getFieldOrNull(tuple, qInstance.phenomenonTimeEnd);
        if (pTimeStart != null && pTimeEnd != null) {
            entity.setPhenomenonTime(Utils.intervalFromTimes(pTimeStart, pTimeEnd));
        }
        OffsetDateTime rTimeEnd = getFieldOrNull(tuple, qInstance.resultTimeEnd);
        OffsetDateTime rTimeStart = getFieldOrNull(tuple, qInstance.resultTimeStart);
        if (rTimeStart != null && rTimeEnd != null) {
            entity.setResultTime(Utils.intervalFromTimes(rTimeStart, rTimeEnd));
        }
        if (select.isEmpty() || select.contains(EntityProperty.PROPERTIES)) {
            String props = getFieldOrNull(tuple, qInstance.properties);
            entity.setProperties(Utils.jsonToObject(props, Map.class));
        }
        entity.setSensor(entityFactories.sensorFromId(tuple, qInstance.getSensorId()));
        entity.setThing(entityFactories.thingFromId(tuple, qInstance.getThingId()));
        List<UnitOfMeasurement> units = Utils.jsonToObject(getFieldOrNull(tuple, qInstance.unitOfMeasurements), EntityFactories.TYPE_LIST_UOM);
        entity.setUnitOfMeasurements(units);
        return entity;
    }

    @Override
    public boolean insert(PostgresPersistenceManager<J> pm, MultiDatastream ds) throws NoSuchEntityException, IncompleteEntityException {
        // First check Sensor and Thing
        Sensor s = ds.getSensor();
        entityFactories.entityExistsOrCreate(pm, s);

        Thing t = ds.getThing();
        entityFactories.entityExistsOrCreate(pm, t);

        DSLContext dslContext = pm.createDdslContext();

        AbstractTableMultiDatastreams<J> qd = qCollection.qMultiDatastreams;
        Map<Field, Object> insert = new HashMap<>();
        insert.put(qd.name, ds.getName());
        insert.put(qd.description, ds.getDescription());
        insert.put(qd.observationTypes, EntityFactories.objectToJson(ds.getMultiObservationDataTypes()));
        insert.put(qd.unitOfMeasurements, EntityFactories.objectToJson(ds.getUnitOfMeasurements()));
        insert.put(qd.properties, EntityFactories.objectToJson(ds.getProperties()));

        insert.put(qd.phenomenonTimeStart, PostgresPersistenceManager.DATETIME_MAX);
        insert.put(qd.phenomenonTimeEnd, PostgresPersistenceManager.DATETIME_MIN);
        insert.put(qd.resultTimeStart, PostgresPersistenceManager.DATETIME_MAX);
        insert.put(qd.resultTimeEnd, PostgresPersistenceManager.DATETIME_MIN);

        insert.put(qd.getSensorId(), (J) s.getId().getValue());
        insert.put(qd.getThingId(), (J) t.getId().getValue());

        entityFactories.insertUserDefinedId(pm, insert, qd.getId(), ds);

        Record1<J> result = dslContext.insertInto(qd)
                .set(insert)
                .returningResult(qd.getId())
                .fetchOne();
        J multiDatastreamId = result.component1();
        LOGGER.debug("Inserted multiDatastream. Created id = {}.", multiDatastreamId);
        ds.setId(entityFactories.idFromObject(multiDatastreamId));

        // Link ObservedProperties.
        EntitySet<ObservedProperty> ops = ds.getObservedProperties();
        int rank = 0;
        for (ObservedProperty op : ops) {
            entityFactories.entityExistsOrCreate(pm, op);
            J opId = (J) op.getId().getValue();

            AbstractTableMultiDatastreamsObsProperties<J> qMdOp = qCollection.qMultiDatastreamsObsProperties;
            dslContext.insertInto(qMdOp)
                    .set(qMdOp.getMultiDatastreamId(), multiDatastreamId)
                    .set(qMdOp.getObsPropertyId(), opId)
                    .set(qMdOp.rank, rank)
                    .execute();
            LOGGER.debug("Linked MultiDatastream {} to ObservedProperty {} with rank {}.", multiDatastreamId, opId, rank);
            rank++;
        }

        // Create Observations, if any.
        for (Observation o : ds.getObservations()) {
            o.setMultiDatastream(new MultiDatastream(ds.getId()));
            o.complete();
            pm.insert(o);
        }

        return true;
    }

    @Override
    public EntityChangedMessage update(PostgresPersistenceManager<J> pm, MultiDatastream md, J mdsId) throws NoSuchEntityException, IncompleteEntityException {
        DSLContext dslContext = pm.createDdslContext();
        AbstractTableMultiDatastreams<J> qmd = qCollection.qMultiDatastreams;
        Map<Field, Object> update = new HashMap<>();
        EntityChangedMessage message = new EntityChangedMessage();

        updateName(md, update, qmd, message);
        updateDescription(md, update, qmd, message);
        updateProperties(md, update, qmd, message);

        updateSensor(md, pm, update, qmd, message);
        updateThing(md, pm, update, qmd, message);

        MultiDatastream original = (MultiDatastream) pm.get(EntityType.MULTIDATASTREAM, entityFactories.idFromObject(mdsId));
        int countOrig = original.getMultiObservationDataTypes().size();

        int countUom = updateUnitsOfMeasure(countOrig, md, update, qmd, message);
        int countDataTypes = updateDataTypes(countOrig, md, update, qmd, message);

        EntitySet<ObservedProperty> ops = md.getObservedProperties();
        int countOps = countOrig + ops.size();

        if (countUom != countDataTypes) {
            throw new IllegalArgumentException("New number of unitOfMeasurements does not match new number of multiObservationDataTypes.");
        }
        if (countUom != countOps) {
            throw new IllegalArgumentException("New number of unitOfMeasurements does not match new number of ObservedProperties.");
        }

        long count = 0;
        if (!update.isEmpty()) {
            count = dslContext.update(qmd)
                    .set(update)
                    .where(qmd.getId().equal(mdsId))
                    .execute();
        }
        if (count > 1) {
            LOGGER.error("Updating Datastream {} caused {} rows to change!", mdsId, count);
            throw new IllegalStateException(CHANGED_MULTIPLE_ROWS);
        }

        linkExistingObservedProperties(mdsId, countOrig, ops, dslContext, pm);

        linkExistingObservations(md, pm, dslContext, mdsId);

        LOGGER.debug("Updated multiDatastream {}", mdsId);
        return message;
    }

    private void updateName(MultiDatastream md, Map<Field, Object> update, AbstractTableMultiDatastreams<J> qmd, EntityChangedMessage message) throws IncompleteEntityException {
        if (md.isSetName()) {
            if (md.getName() == null) {
                throw new IncompleteEntityException("name" + CAN_NOT_BE_NULL);
            }
            update.put(qmd.name, md.getName());
            message.addField(EntityProperty.NAME);
        }
    }

    private void updateDescription(MultiDatastream md, Map<Field, Object> update, AbstractTableMultiDatastreams<J> qmd, EntityChangedMessage message) throws IncompleteEntityException {
        if (md.isSetDescription()) {
            if (md.getDescription() == null) {
                throw new IncompleteEntityException(EntityProperty.DESCRIPTION.jsonName + CAN_NOT_BE_NULL);
            }
            update.put(qmd.description, md.getDescription());
            message.addField(EntityProperty.DESCRIPTION);
        }
    }

    private void updateProperties(MultiDatastream md, Map<Field, Object> update, AbstractTableMultiDatastreams<J> qmd, EntityChangedMessage message) {
        if (md.isSetProperties()) {
            update.put(qmd.properties, EntityFactories.objectToJson(md.getProperties()));
            message.addField(EntityProperty.PROPERTIES);
        }
    }

    private void updateSensor(MultiDatastream md, PostgresPersistenceManager<J> pm, Map<Field, Object> update, AbstractTableMultiDatastreams<J> qmd, EntityChangedMessage message) throws NoSuchEntityException {
        if (md.isSetSensor()) {
            if (!entityFactories.entityExists(pm, md.getSensor())) {
                throw new NoSuchEntityException("Sensor with no id or not found.");
            }
            update.put(qmd.getSensorId(), (J) md.getSensor().getId().getValue());
            message.addField(NavigationProperty.SENSOR);
        }
    }

    private void updateThing(MultiDatastream md, PostgresPersistenceManager<J> pm, Map<Field, Object> update, AbstractTableMultiDatastreams<J> qmd, EntityChangedMessage message) throws NoSuchEntityException {
        if (md.isSetThing()) {
            if (!entityFactories.entityExists(pm, md.getThing())) {
                throw new NoSuchEntityException("Thing with no id or not found.");
            }
            update.put(qmd.getThingId(), (J) md.getThing().getId().getValue());
            message.addField(NavigationProperty.THING);
        }
    }

    private int updateUnitsOfMeasure(int countOrig, MultiDatastream md, Map<Field, Object> update, AbstractTableMultiDatastreams<J> qmd, EntityChangedMessage message) throws IncompleteEntityException {
        int countUom = countOrig;
        if (md.isSetUnitOfMeasurements()) {
            if (md.getUnitOfMeasurements() == null) {
                throw new IncompleteEntityException("unitOfMeasurements" + CAN_NOT_BE_NULL);
            }
            List<UnitOfMeasurement> uoms = md.getUnitOfMeasurements();
            countUom = uoms.size();
            update.put(qmd.unitOfMeasurements, EntityFactories.objectToJson(uoms));
            message.addField(EntityProperty.UNITOFMEASUREMENTS);
        }
        return countUom;
    }

    private int updateDataTypes(int countOrig, MultiDatastream md, Map<Field, Object> update, AbstractTableMultiDatastreams<J> qmd, EntityChangedMessage message) throws IncompleteEntityException {
        int countDataTypes = countOrig;
        if (md.isSetMultiObservationDataTypes()) {
            List<String> dataTypes = md.getMultiObservationDataTypes();
            if (dataTypes == null) {
                throw new IncompleteEntityException("multiObservationDataTypes" + CAN_NOT_BE_NULL);
            }
            countDataTypes = dataTypes.size();
            update.put(qmd.observationTypes, EntityFactories.objectToJson(dataTypes));
            message.addField(EntityProperty.MULTIOBSERVATIONDATATYPES);
        }
        return countDataTypes;
    }

    private void linkExistingObservedProperties(J mdsId, int countOrig, EntitySet<ObservedProperty> ops, DSLContext dslContext, PostgresPersistenceManager<J> pm) throws NoSuchEntityException {
        // Link existing ObservedProperties to the MultiDatastream.
        int rank = countOrig;
        for (ObservedProperty op : ops) {
            if (op.getId() == null || !entityFactories.entityExists(pm, op)) {
                throw new NoSuchEntityException("ObservedProperty with no id or not found.");
            }
            J opId = (J) op.getId().getValue();
            AbstractTableMultiDatastreamsObsProperties<J> qMdOp = qCollection.qMultiDatastreamsObsProperties;
            int count = dslContext.insertInto(qMdOp)
                    .set(qMdOp.getMultiDatastreamId(), mdsId)
                    .set(qMdOp.getObsPropertyId(), opId)
                    .set(qMdOp.rank, rank)
                    .execute();
            if (count > 0) {
                LOGGER.debug("Assigned datastream {} to ObservedProperty {} with rank {}.", mdsId, opId, rank);
            }
            rank++;
        }
    }

    private void linkExistingObservations(MultiDatastream md, PostgresPersistenceManager<J> pm, DSLContext dslContext, J mdsId) throws NoSuchEntityException {
        // Link existing Observations to the MultiDatastream.
        for (Observation o : md.getObservations()) {
            if (o.getId() == null || !entityFactories.entityExists(pm, o)) {
                throw new NoSuchEntityException(EntityType.OBSERVATION.entityName + NO_ID_OR_NOT_FOUND);
            }
            J obsId = (J) o.getId().getValue();
            AbstractTableObservations<J> qo = qCollection.qObservations;
            long oCount = dslContext.update(qo)
                    .set(qo.getDatastreamId(), mdsId)
                    .where(qo.getId().eq(obsId))
                    .execute();
            if (oCount > 0) {
                LOGGER.debug("Assigned multiDatastream {} to Observation {}.", mdsId, obsId);
            }
        }
    }

    @Override
    public void delete(PostgresPersistenceManager<J> pm, J entityId) throws NoSuchEntityException {
        long count = pm.createDdslContext()
                .delete(qInstance)
                .where(qInstance.getId().eq(entityId))
                .execute();
        if (count == 0) {
            throw new NoSuchEntityException("MultiDatastream " + entityId + " not found.");
        }
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.MULTIDATASTREAM;
    }

    @Override
    public Field<J> getPrimaryKey() {
        return qInstance.getId();
    }

}
