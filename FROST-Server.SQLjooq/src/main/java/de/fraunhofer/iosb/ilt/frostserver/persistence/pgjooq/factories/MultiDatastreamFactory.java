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
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.frostserver.model.Observation;
import de.fraunhofer.iosb.ilt.frostserver.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.frostserver.model.Sensor;
import de.fraunhofer.iosb.ilt.frostserver.model.Thing;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.Utils;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.JsonValue;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.CAN_NOT_BE_NULL;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.CHANGED_MULTIPLE_ROWS;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.NO_ID_OR_NOT_FOUND;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableMultiDatastreams;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableMultiDatastreamsObsProperties;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableObservations;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.util.GeoHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.geojson.GeoJsonObject;
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
public class MultiDatastreamFactory<J extends Comparable> implements EntityFactory<MultiDatastream, J> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiDatastreamFactory.class);

    private final EntityFactories<J> entityFactories;
    private final AbstractTableMultiDatastreams<J> table;
    private final TableCollection<J> tableCollection;

    public MultiDatastreamFactory(EntityFactories<J> factories, AbstractTableMultiDatastreams<J> table) {
        this.entityFactories = factories;
        this.table = table;
        this.tableCollection = factories.tableCollection;
    }

    @Override
    public MultiDatastream create(Record tuple, Query query, DataSize dataSize) {
        Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();
        MultiDatastream entity = new MultiDatastream();
        entity.setName(Utils.getFieldOrNull(tuple, table.colName));
        entity.setDescription(Utils.getFieldOrNull(tuple, table.colDescription));
        J id = Utils.getFieldOrNull(tuple, table.getId());
        if (id != null) {
            entity.setId(entityFactories.idFromObject(id));
        }
        List<String> observationTypes = Utils.getFieldJsonValue(tuple, table.colObservationTypes).getValue(Utils.TYPE_LIST_STRING);
        entity.setMultiObservationDataTypes(observationTypes);
        String observedArea = Utils.getFieldOrNull(tuple, table.colObservedAreaText);
        if (observedArea != null) {
            try {
                GeoJsonObject area = GeoHelper.parseGeoJson(observedArea);
                entity.setObservedArea(area);
            } catch (IOException e) {
                // It's not a polygon, probably a point or a line.
            }
        }
        OffsetDateTime pTimeStart = Utils.getFieldOrNull(tuple, table.colPhenomenonTimeStart);
        OffsetDateTime pTimeEnd = Utils.getFieldOrNull(tuple, table.colPhenomenonTimeEnd);
        if (pTimeStart != null && pTimeEnd != null) {
            entity.setPhenomenonTime(Utils.intervalFromTimes(pTimeStart, pTimeEnd));
        }
        OffsetDateTime rTimeEnd = Utils.getFieldOrNull(tuple, table.colResultTimeEnd);
        OffsetDateTime rTimeStart = Utils.getFieldOrNull(tuple, table.colResultTimeStart);
        if (rTimeStart != null && rTimeEnd != null) {
            entity.setResultTime(Utils.intervalFromTimes(rTimeStart, rTimeEnd));
        }
        if (select.isEmpty() || select.contains(EntityProperty.PROPERTIES)) {
            JsonValue props = Utils.getFieldJsonValue(tuple, table.colProperties);
            dataSize.increase(props.getStringLength());
            entity.setProperties(props.getMapValue());
        }
        entity.setSensor(entityFactories.sensorFromId(tuple, table.getSensorId()));
        entity.setThing(entityFactories.thingFromId(tuple, table.getThingId()));
        List<UnitOfMeasurement> units = Utils.getFieldJsonValue(tuple, table.colUnitOfMeasurements).getValue(Utils.TYPE_LIST_UOM);
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

        Map<Field, Object> insert = new HashMap<>();
        insert.put(table.colName, ds.getName());
        insert.put(table.colDescription, ds.getDescription());
        insert.put(table.colObservationTypes, new JsonValue(ds.getMultiObservationDataTypes()));
        insert.put(table.colUnitOfMeasurements, new JsonValue(ds.getUnitOfMeasurements()));
        insert.put(table.colProperties, new JsonValue(ds.getProperties()));

        insert.put(table.getSensorId(), s.getId().getValue());
        insert.put(table.getThingId(), t.getId().getValue());

        entityFactories.insertUserDefinedId(pm, insert, table.getId(), ds);

        DSLContext dslContext = pm.getDslContext();
        Record1<J> result = dslContext.insertInto(table)
                .set(insert)
                .returningResult(table.getId())
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

            AbstractTableMultiDatastreamsObsProperties<J> tMdOp = tableCollection.getTableMultiDatastreamsObsProperties();
            dslContext.insertInto(tMdOp)
                    .set(tMdOp.getMultiDatastreamId(), multiDatastreamId)
                    .set(tMdOp.getObsPropertyId(), opId)
                    .set(tMdOp.colRank, rank)
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

        Map<Field, Object> update = new HashMap<>();
        EntityChangedMessage message = new EntityChangedMessage();

        updateName(md, update, message);
        updateDescription(md, update, message);
        updateProperties(md, update, message);
        updateSensor(md, pm, update, message);
        updateThing(md, pm, update, message);

        MultiDatastream original = (MultiDatastream) pm.get(EntityType.MULTIDATASTREAM, entityFactories.idFromObject(mdsId));
        int countOrig = original.getMultiObservationDataTypes().size();

        int countUom = updateUnitsOfMeasure(countOrig, md, update, message);
        int countDataTypes = updateDataTypes(countOrig, md, update, message);

        EntitySet<ObservedProperty> ops = md.getObservedProperties();
        int countOps = countOrig + ops.size();

        if (countUom != countDataTypes) {
            throw new IllegalArgumentException("New number of unitOfMeasurements does not match new number of multiObservationDataTypes.");
        }
        if (countUom != countOps) {
            throw new IllegalArgumentException("New number of unitOfMeasurements does not match new number of ObservedProperties.");
        }

        DSLContext dslContext = pm.getDslContext();
        long count = 0;
        if (!update.isEmpty()) {
            count = dslContext.update(table)
                    .set(update)
                    .where(table.getId().equal(mdsId))
                    .execute();
        }
        if (count > 1) {
            LOGGER.error("Updating Datastream {} caused {} rows to change!", mdsId, count);
            throw new IllegalStateException(CHANGED_MULTIPLE_ROWS);
        }

        linkExistingObservedProperties(mdsId, countOrig, ops, dslContext, pm, entityFactories);

        linkExistingObservations(md, pm, entityFactories, dslContext, mdsId);

        LOGGER.debug("Updated multiDatastream {}", mdsId);
        return message;
    }

    private void updateName(MultiDatastream md, Map<Field, Object> update, EntityChangedMessage message) throws IncompleteEntityException {
        if (md.isSetName()) {
            if (md.getName() == null) {
                throw new IncompleteEntityException("name" + CAN_NOT_BE_NULL);
            }
            update.put(table.colName, md.getName());
            message.addField(EntityProperty.NAME);
        }
    }

    private void updateDescription(MultiDatastream md, Map<Field, Object> update, EntityChangedMessage message) throws IncompleteEntityException {
        if (md.isSetDescription()) {
            if (md.getDescription() == null) {
                throw new IncompleteEntityException(EntityProperty.DESCRIPTION.jsonName + CAN_NOT_BE_NULL);
            }
            update.put(table.colDescription, md.getDescription());
            message.addField(EntityProperty.DESCRIPTION);
        }
    }

    private void updateProperties(MultiDatastream md, Map<Field, Object> update, EntityChangedMessage message) {
        if (md.isSetProperties()) {
            update.put(table.colProperties, new JsonValue(md.getProperties()));
            message.addField(EntityProperty.PROPERTIES);
        }
    }

    private void updateSensor(MultiDatastream md, PostgresPersistenceManager<J> pm, Map<Field, Object> update, EntityChangedMessage message) throws NoSuchEntityException {
        if (md.isSetSensor()) {
            if (!entityFactories.entityExists(pm, md.getSensor())) {
                throw new NoSuchEntityException("Sensor with no id or not found.");
            }
            update.put(table.getSensorId(), md.getSensor().getId().getValue());
            message.addField(NavigationPropertyMain.SENSOR);
        }
    }

    private void updateThing(MultiDatastream md, PostgresPersistenceManager<J> pm, Map<Field, Object> update, EntityChangedMessage message) throws NoSuchEntityException {
        if (md.isSetThing()) {
            if (!entityFactories.entityExists(pm, md.getThing())) {
                throw new NoSuchEntityException("Thing with no id or not found.");
            }
            update.put(table.getThingId(), md.getThing().getId().getValue());
            message.addField(NavigationPropertyMain.THING);
        }
    }

    private int updateUnitsOfMeasure(int countOrig, MultiDatastream md, Map<Field, Object> update, EntityChangedMessage message) throws IncompleteEntityException {
        int countUom = countOrig;
        if (md.isSetUnitOfMeasurements()) {
            if (md.getUnitOfMeasurements() == null) {
                throw new IncompleteEntityException("unitOfMeasurements" + CAN_NOT_BE_NULL);
            }
            List<UnitOfMeasurement> uoms = md.getUnitOfMeasurements();
            countUom = uoms.size();
            update.put(table.colUnitOfMeasurements, new JsonValue(uoms));
            message.addField(EntityProperty.UNITOFMEASUREMENTS);
        }
        return countUom;
    }

    private int updateDataTypes(int countOrig, MultiDatastream md, Map<Field, Object> update, EntityChangedMessage message) throws IncompleteEntityException {
        int countDataTypes = countOrig;
        if (md.isSetMultiObservationDataTypes()) {
            List<String> dataTypes = md.getMultiObservationDataTypes();
            if (dataTypes == null) {
                throw new IncompleteEntityException("multiObservationDataTypes" + CAN_NOT_BE_NULL);
            }
            countDataTypes = dataTypes.size();
            update.put(table.colObservationTypes, new JsonValue(dataTypes));
            message.addField(EntityProperty.MULTIOBSERVATIONDATATYPES);
        }
        return countDataTypes;
    }

    private static <J extends Comparable> void linkExistingObservedProperties(J mdsId, int countOrig, EntitySet<ObservedProperty> ops, DSLContext dslContext, PostgresPersistenceManager<J> pm, EntityFactories<J> entityFactories) throws NoSuchEntityException {
        // Link existing ObservedProperties to the MultiDatastream.
        int rank = countOrig;
        for (ObservedProperty op : ops) {
            if (op.getId() == null || !entityFactories.entityExists(pm, op)) {
                throw new NoSuchEntityException("ObservedProperty with no id or not found.");
            }
            J opId = (J) op.getId().getValue();
            AbstractTableMultiDatastreamsObsProperties<J> tMdOp = entityFactories.tableCollection.getTableMultiDatastreamsObsProperties();
            int count = dslContext.insertInto(tMdOp)
                    .set(tMdOp.getMultiDatastreamId(), mdsId)
                    .set(tMdOp.getObsPropertyId(), opId)
                    .set(tMdOp.colRank, rank)
                    .execute();
            if (count > 0) {
                LOGGER.debug("Assigned datastream {} to ObservedProperty {} with rank {}.", mdsId, opId, rank);
            }
            rank++;
        }
    }

    private static <J extends Comparable> void linkExistingObservations(MultiDatastream md, PostgresPersistenceManager<J> pm, EntityFactories<J> entityFactories, DSLContext dslContext, J mdsId) throws NoSuchEntityException {
        // Link existing Observations to the MultiDatastream.
        for (Observation o : md.getObservations()) {
            if (o.getId() == null || !entityFactories.entityExists(pm, o)) {
                throw new NoSuchEntityException(EntityType.OBSERVATION.entityName + NO_ID_OR_NOT_FOUND);
            }
            J obsId = (J) o.getId().getValue();
            AbstractTableObservations<J> tObs = entityFactories.tableCollection.getTableObservations();
            long oCount = dslContext.update(tObs)
                    .set(tObs.getDatastreamId(), mdsId)
                    .where(tObs.getId().eq(obsId))
                    .execute();
            if (oCount > 0) {
                LOGGER.debug("Assigned multiDatastream {} to Observation {}.", mdsId, obsId);
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
            throw new NoSuchEntityException("MultiDatastream " + entityId + " not found.");
        }
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.MULTIDATASTREAM;
    }

    @Override
    public Field<J> getPrimaryKey() {
        return table.getId();
    }

}
