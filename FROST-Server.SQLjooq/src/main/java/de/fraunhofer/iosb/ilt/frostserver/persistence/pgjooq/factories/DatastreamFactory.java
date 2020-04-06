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
import de.fraunhofer.iosb.ilt.frostserver.model.Observation;
import de.fraunhofer.iosb.ilt.frostserver.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.frostserver.model.Sensor;
import de.fraunhofer.iosb.ilt.frostserver.model.Thing;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.Utils;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.Utils.getFieldOrNull;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.CAN_NOT_BE_NULL;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.CHANGED_MULTIPLE_ROWS;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories.NO_ID_OR_NOT_FOUND;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableDatastreams;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableObservations;
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
 * @param <J> The type of the ID fields.
 */
public class DatastreamFactory<J extends Comparable> implements EntityFactory<Datastream, J> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DatastreamFactory.class);

    private final EntityFactories<J> entityFactories;
    private final AbstractTableDatastreams<J> table;

    public DatastreamFactory(EntityFactories<J> factories, AbstractTableDatastreams<J> table) {
        this.entityFactories = factories;
        this.table = table;
    }

    @Override
    public Datastream create(Record tuple, Query query, DataSize dataSize) {
        Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();
        Datastream entity = new Datastream();
        J entityId = getFieldOrNull(tuple, table.getId());
        if (entityId != null) {
            entity.setId(entityFactories.idFromObject(entityId));
        }
        entity.setName(getFieldOrNull(tuple, table.name));
        entity.setDescription(getFieldOrNull(tuple, table.description));
        entity.setObservationType(getFieldOrNull(tuple, table.observationType));
        String observedArea = getFieldOrNull(tuple, table.observedAreaText);
        if (observedArea != null) {
            try {
                GeoJsonObject area = GeoHelper.parseGeoJson(observedArea);
                entity.setObservedArea(area);
            } catch (IOException e) {
                // It's not a polygon, probably a point or a line.
            }
        }
        ObservedProperty op = entityFactories.observedProperyFromId(tuple, table.getObsPropertyId());
        entity.setObservedProperty(op);
        OffsetDateTime pTimeStart = getFieldOrNull(tuple, table.phenomenonTimeStart);
        OffsetDateTime pTimeEnd = getFieldOrNull(tuple, table.phenomenonTimeEnd);
        if (pTimeStart != null && pTimeEnd != null) {
            entity.setPhenomenonTime(Utils.intervalFromTimes(pTimeStart, pTimeEnd));
        }
        OffsetDateTime rTimeStart = getFieldOrNull(tuple, table.resultTimeStart);
        OffsetDateTime rTimeEnd = getFieldOrNull(tuple, table.resultTimeEnd);
        if (rTimeStart != null && rTimeEnd != null) {
            entity.setResultTime(Utils.intervalFromTimes(rTimeStart, rTimeEnd));
        }
        if (select.isEmpty() || select.contains(EntityProperty.PROPERTIES)) {
            String props = getFieldOrNull(tuple, table.properties);
            entity.setProperties(Utils.jsonToObject(props, Map.class));
        }
        entity.setSensor(entityFactories.sensorFromId(tuple, table.getSensorId()));
        entity.setThing(entityFactories.thingFromId(tuple, table.getThingId()));
        entity.setUnitOfMeasurement(new UnitOfMeasurement(getFieldOrNull(tuple, table.unitName), getFieldOrNull(tuple, table.unitSymbol), getFieldOrNull(tuple, table.unitDefinition)));
        return entity;
    }

    @Override
    public boolean insert(PostgresPersistenceManager<J> pm, Datastream ds) throws NoSuchEntityException, IncompleteEntityException {
        // First check ObservedPropery, Sensor and Thing
        ObservedProperty op = ds.getObservedProperty();
        entityFactories.entityExistsOrCreate(pm, op);

        Sensor s = ds.getSensor();
        entityFactories.entityExistsOrCreate(pm, s);

        Thing t = ds.getThing();
        entityFactories.entityExistsOrCreate(pm, t);

        Map<Field, Object> insert = new HashMap<>();

        insert.put(table.name, ds.getName());
        insert.put(table.description, ds.getDescription());
        insert.put(table.observationType, ds.getObservationType());
        insert.put(table.unitDefinition, ds.getUnitOfMeasurement().getDefinition());
        insert.put(table.unitName, ds.getUnitOfMeasurement().getName());
        insert.put(table.unitSymbol, ds.getUnitOfMeasurement().getSymbol());
        insert.put(table.properties, EntityFactories.objectToJson(ds.getProperties()));

        insert.put(table.getObsPropertyId(), (J) op.getId().getValue());
        insert.put(table.getSensorId(), (J) s.getId().getValue());
        insert.put(table.getThingId(), (J) t.getId().getValue());

        entityFactories.insertUserDefinedId(pm, insert, table.getId(), ds);

        DSLContext dslContext = pm.getDslContext();
        Record1<J> result = dslContext.insertInto(table)
                .set(insert)
                .returningResult(table.getId())
                .fetchOne();
        J datastreamId = result.component1();
        LOGGER.debug("Inserted datastream. Created id = {}.", datastreamId);
        ds.setId(entityFactories.idFromObject(datastreamId));

        // Create Observations, if any.
        for (Observation o : ds.getObservations()) {
            o.setDatastream(new Datastream(ds.getId()));
            o.complete();
            pm.insert(o);
        }

        return true;
    }

    @Override
    public EntityChangedMessage update(PostgresPersistenceManager<J> pm, Datastream datastream, J dsId) throws NoSuchEntityException, IncompleteEntityException {
        Map<Field, Object> update = new HashMap<>();
        EntityChangedMessage message = new EntityChangedMessage();

        updateName(datastream, update, message);
        updateDescription(datastream, update, message);
        updateObservationType(datastream, update, message);
        updateProperties(datastream, update, message);
        updateObservedProperty(datastream, pm, update, message);
        updateSensor(datastream, pm, update, message);
        updateThing(datastream, pm, update, message);
        updateUnitOfMeasurement(datastream, update, message);

        DSLContext dslContext = pm.getDslContext();
        long count = 0;
        if (!update.isEmpty()) {
            count = dslContext.update(table)
                    .set(update)
                    .where(table.getId().equal(dsId))
                    .execute();
        }
        if (count > 1) {
            LOGGER.error("Updating Datastream {} caused {} rows to change!", dsId, count);
            throw new IllegalStateException(CHANGED_MULTIPLE_ROWS);
        }

        linkExistingObservations(datastream, pm, dslContext, dsId);

        LOGGER.debug("Updated Datastream {}", dsId);
        return message;
    }

    private void updateUnitOfMeasurement(Datastream datastream, Map<Field, Object> update, EntityChangedMessage message) throws IncompleteEntityException {
        if (datastream.isSetUnitOfMeasurement()) {
            if (datastream.getUnitOfMeasurement() == null) {
                throw new IncompleteEntityException("unitOfMeasurement" + CAN_NOT_BE_NULL);
            }
            UnitOfMeasurement uom = datastream.getUnitOfMeasurement();
            update.put(table.unitDefinition, uom.getDefinition());
            update.put(table.unitName, uom.getName());
            update.put(table.unitSymbol, uom.getSymbol());
            message.addField(EntityProperty.UNITOFMEASUREMENT);
        }
    }

    private void updateThing(Datastream datastream, PostgresPersistenceManager<J> pm, Map<Field, Object> update, EntityChangedMessage message) throws NoSuchEntityException {
        if (datastream.isSetThing()) {
            if (!entityFactories.entityExists(pm, datastream.getThing())) {
                throw new NoSuchEntityException("Thing with no id or not found.");
            }
            update.put(table.getThingId(), (J) datastream.getThing().getId().getValue());
            message.addField(NavigationPropertyMain.THING);
        }
    }

    private void updateSensor(Datastream datastream, PostgresPersistenceManager<J> pm, Map<Field, Object> update, EntityChangedMessage message) throws NoSuchEntityException {
        if (datastream.isSetSensor()) {
            if (!entityFactories.entityExists(pm, datastream.getSensor())) {
                throw new NoSuchEntityException("Sensor with no id or not found.");
            }
            update.put(table.getSensorId(), (J) datastream.getSensor().getId().getValue());
            message.addField(NavigationPropertyMain.SENSOR);
        }
    }

    private void updateObservedProperty(Datastream datastream, PostgresPersistenceManager<J> pm, Map<Field, Object> update, EntityChangedMessage message) throws NoSuchEntityException {
        if (datastream.isSetObservedProperty()) {
            if (!entityFactories.entityExists(pm, datastream.getObservedProperty())) {
                throw new NoSuchEntityException("ObservedProperty with no id or not found.");
            }
            update.put(table.getObsPropertyId(), (J) datastream.getObservedProperty().getId().getValue());
            message.addField(NavigationPropertyMain.OBSERVEDPROPERTY);
        }
    }

    private void updateProperties(Datastream datastream, Map<Field, Object> update, EntityChangedMessage message) {
        if (datastream.isSetProperties()) {
            update.put(table.properties, EntityFactories.objectToJson(datastream.getProperties()));
            message.addField(EntityProperty.PROPERTIES);
        }
    }

    private void updateObservationType(Datastream datastream, Map<Field, Object> update, EntityChangedMessage message) throws IncompleteEntityException {
        if (datastream.isSetObservationType()) {
            if (datastream.getObservationType() == null) {
                throw new IncompleteEntityException("observationType" + CAN_NOT_BE_NULL);
            }
            update.put(table.observationType, datastream.getObservationType());
            message.addField(EntityProperty.OBSERVATIONTYPE);
        }
    }

    private void updateDescription(Datastream datastream, Map<Field, Object> update, EntityChangedMessage message) throws IncompleteEntityException {
        if (datastream.isSetDescription()) {
            if (datastream.getDescription() == null) {
                throw new IncompleteEntityException(EntityProperty.DESCRIPTION.jsonName + CAN_NOT_BE_NULL);
            }
            update.put(table.description, datastream.getDescription());
            message.addField(EntityProperty.DESCRIPTION);
        }
    }

    private void updateName(Datastream d, Map<Field, Object> update, EntityChangedMessage message) throws IncompleteEntityException {
        if (d.isSetName()) {
            if (d.getName() == null) {
                throw new IncompleteEntityException("name" + CAN_NOT_BE_NULL);
            }
            update.put(table.name, d.getName());
            message.addField(EntityProperty.NAME);
        }
    }

    private void linkExistingObservations(Datastream d, PostgresPersistenceManager<J> pm, DSLContext dslContext, J dsId) throws NoSuchEntityException {
        for (Observation o : d.getObservations()) {
            if (o.getId() == null || !entityFactories.entityExists(pm, o)) {
                throw new NoSuchEntityException(EntityType.OBSERVATION.entityName + NO_ID_OR_NOT_FOUND);
            }
            J obsId = (J) o.getId().getValue();
            AbstractTableObservations<J> tableObs = entityFactories.tableCollection.tableObservations;
            long oCount = dslContext.update(tableObs)
                    .set(tableObs.getDatastreamId(), dsId)
                    .where(tableObs.getId().eq(obsId))
                    .execute();
            if (oCount > 0) {
                LOGGER.debug("Assigned datastream {} to Observation {}.", dsId, obsId);
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
            throw new NoSuchEntityException("Datastream " + entityId + " not found.");
        }
    }

    @Override
    public Field<J> getPrimaryKey() {
        return table.getId();
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.DATASTREAM;
    }

}
