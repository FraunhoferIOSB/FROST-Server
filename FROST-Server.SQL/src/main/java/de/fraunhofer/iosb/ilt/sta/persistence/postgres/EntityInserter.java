/*
     * Copyright (C) 2016 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.sta.persistence.postgres;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.Tuple;
import com.querydsl.core.dml.StoreClause;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimpleExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.spatial.GeometryPath;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.dml.SQLInsertClause;
import com.querydsl.sql.dml.SQLUpdateClause;
import de.fraunhofer.iosb.ilt.sta.json.deserialize.EntityParser;
import de.fraunhofer.iosb.ilt.sta.json.deserialize.custom.GeoJsonDeserializier;
import de.fraunhofer.iosb.ilt.sta.json.serialize.GeoJsonSerializer;
import de.fraunhofer.iosb.ilt.sta.messagebus.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.FeatureOfInterest;
import de.fraunhofer.iosb.ilt.sta.model.HistoricalLocation;
import de.fraunhofer.iosb.ilt.sta.model.Location;
import de.fraunhofer.iosb.ilt.sta.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.builder.DatastreamBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.FeatureOfInterestBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.MultiDatastreamBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.SensorBuilder;
import de.fraunhofer.iosb.ilt.sta.model.builder.ThingBuilder;
import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.sta.model.core.Id;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntitySetPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.NavigationProperty;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePath;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQFeatures;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQLocationsHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQMultiDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQMultiDatastreamsObsProperties;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQObsProperties;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQObservations;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQSensors;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQThings;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQThingsLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.QCollection;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.sta.util.NoSuchEntityException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.geojson.Crs;
import org.geojson.Feature;
import org.geojson.GeoJsonObject;
import org.geojson.jackson.CrsType;
import org.geolatte.common.dataformats.json.jackson.JsonException;
import org.geolatte.common.dataformats.json.jackson.JsonMapper;
import org.geolatte.geom.Geometry;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Hylke van der Schaaf
 * @param <I> The type of path used for the ID fields.
 * @param <J> The type of the ID fields.
 */
public class EntityInserter<I extends SimpleExpression<J> & Path<J>, J> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityInserter.class);
    private static final String CAN_NOT_BE_NULL = " can not be null.";
    private static final String CHANGED_MULTIPLE_ROWS = "Update changed multiple rows.";
    private static final String NO_ID_OR_NOT_FOUND = " with no id or non existing.";
    private static final String CREATED_HL = "Created historicalLocation {}";
    private static final String LINKED_L_TO_HL = "Linked location {} to historicalLocation {}.";
    private static final String UNLINKED_L_FROM_T = "Unlinked {} locations from Thing {}.";
    private static final String LINKED_L_TO_T = "Linked Location {} to Thing {}.";

    private final PostgresPersistenceManager<I, J> pm;
    private final EntityFactories<I, J> entityFactories;
    private final QCollection<I, J> qCollection;

    private ObjectMapper formatter;

    public EntityInserter(PostgresPersistenceManager<I, J> pm) {
        this.pm = pm;
        entityFactories = pm.getEntityFactories();
        qCollection = entityFactories.qCollection;
    }

    public boolean insertDatastream(Datastream ds) throws NoSuchEntityException, IncompleteEntityException {
        // First check ObservedPropery, Sensor and Thing
        ObservedProperty op = ds.getObservedProperty();
        entityExistsOrCreate(op);

        Sensor s = ds.getSensor();
        entityExistsOrCreate(s);

        Thing t = ds.getThing();
        entityExistsOrCreate(t);

        SQLQueryFactory qFactory = pm.createQueryFactory();

        AbstractQDatastreams<? extends AbstractQDatastreams, I, J> qd = qCollection.qDatastreams;
        SQLInsertClause insert = qFactory.insert(qd);
        insert.set(qd.name, ds.getName());
        insert.set(qd.description, ds.getDescription());
        insert.set(qd.observationType, ds.getObservationType());
        insert.set(qd.unitDefinition, ds.getUnitOfMeasurement().getDefinition());
        insert.set(qd.unitName, ds.getUnitOfMeasurement().getName());
        insert.set(qd.unitSymbol, ds.getUnitOfMeasurement().getSymbol());
        insert.set(qd.properties, objectToJson(ds.getProperties()));

        insert.set(qd.phenomenonTimeStart, new Timestamp(PostgresPersistenceManager.DATETIME_MAX.getMillis()));
        insert.set(qd.phenomenonTimeEnd, new Timestamp(PostgresPersistenceManager.DATETIME_MIN.getMillis()));
        insert.set(qd.resultTimeStart, new Timestamp(PostgresPersistenceManager.DATETIME_MAX.getMillis()));
        insert.set(qd.resultTimeEnd, new Timestamp(PostgresPersistenceManager.DATETIME_MIN.getMillis()));

        insert.set(qd.getObsPropertyId(), (J) op.getId().getValue());
        insert.set(qd.getSensorId(), (J) s.getId().getValue());
        insert.set(qd.getThingId(), (J) t.getId().getValue());

        insertUserDefinedId(insert, qd.getId(), ds);

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

    public EntityChangedMessage updateDatastream(Datastream d, J dsId) throws NoSuchEntityException, IncompleteEntityException {

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
            update.set(qd.properties, objectToJson(d.getProperties()));
            message.addField(EntityProperty.PROPERTIES);
        }
        if (d.isSetObservedProperty()) {
            if (!entityExists(d.getObservedProperty())) {
                throw new NoSuchEntityException("ObservedProperty with no id or not found.");
            }
            update.set(qd.getObsPropertyId(), (J) d.getObservedProperty().getId().getValue());
            message.addField(NavigationProperty.OBSERVEDPROPERTY);
        }
        if (d.isSetSensor()) {
            if (!entityExists(d.getSensor())) {
                throw new NoSuchEntityException("Sensor with no id or not found.");
            }
            update.set(qd.getSensorId(), (J) d.getSensor().getId().getValue());
            message.addField(NavigationProperty.SENSOR);
        }
        if (d.isSetThing()) {
            if (!entityExists(d.getThing())) {
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
            if (o.getId() == null || !entityExists(o)) {
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

    public boolean insertMultiDatastream(MultiDatastream ds) throws NoSuchEntityException, IncompleteEntityException {
        // First check Sensor and Thing
        Sensor s = ds.getSensor();
        entityExistsOrCreate(s);

        Thing t = ds.getThing();
        entityExistsOrCreate(t);

        SQLQueryFactory qFactory = pm.createQueryFactory();

        AbstractQMultiDatastreams<? extends AbstractQMultiDatastreams, I, J> qd = qCollection.qMultiDatastreams;
        SQLInsertClause insert = qFactory.insert(qd);
        insert.set(qd.name, ds.getName());
        insert.set(qd.description, ds.getDescription());
        insert.set(qd.observationTypes, objectToJson(ds.getMultiObservationDataTypes()));
        insert.set(qd.unitOfMeasurements, objectToJson(ds.getUnitOfMeasurements()));
        insert.set(qd.properties, objectToJson(ds.getProperties()));

        insert.set(qd.phenomenonTimeStart, new Timestamp(PostgresPersistenceManager.DATETIME_MAX.getMillis()));
        insert.set(qd.phenomenonTimeEnd, new Timestamp(PostgresPersistenceManager.DATETIME_MIN.getMillis()));
        insert.set(qd.resultTimeStart, new Timestamp(PostgresPersistenceManager.DATETIME_MAX.getMillis()));
        insert.set(qd.resultTimeEnd, new Timestamp(PostgresPersistenceManager.DATETIME_MIN.getMillis()));

        insert.set(qd.getSensorId(), (J) s.getId().getValue());
        insert.set(qd.getThingId(), (J) t.getId().getValue());

        insertUserDefinedId(insert, qd.getId(), ds);

        J multiDatastreamId = insert.executeWithKey(qd.getId());
        LOGGER.debug("Inserted multiDatastream. Created id = {}.", multiDatastreamId);
        ds.setId(entityFactories.idFromObject(multiDatastreamId));

        // Create new Locations, if any.
        EntitySet<ObservedProperty> ops = ds.getObservedProperties();
        int rank = 0;
        for (ObservedProperty op : ops) {
            entityExistsOrCreate(op);
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

    public EntityChangedMessage updateMultiDatastream(MultiDatastream d, J dsId) throws NoSuchEntityException, IncompleteEntityException {
        SQLQueryFactory qFactory = pm.createQueryFactory();
        AbstractQMultiDatastreams<? extends AbstractQMultiDatastreams, I, J> qd = qCollection.qMultiDatastreams;
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
        if (d.isSetProperties()) {
            update.set(qd.properties, objectToJson(d.getProperties()));
            message.addField(EntityProperty.PROPERTIES);
        }

        if (d.isSetSensor()) {
            if (!entityExists(d.getSensor())) {
                throw new NoSuchEntityException("Sensor with no id or not found.");
            }
            update.set(qd.getSensorId(), (J) d.getSensor().getId().getValue());
            message.addField(NavigationProperty.SENSOR);
        }
        if (d.isSetThing()) {
            if (!entityExists(d.getThing())) {
                throw new NoSuchEntityException("Thing with no id or not found.");
            }
            update.set(qd.getThingId(), (J) d.getThing().getId().getValue());
            message.addField(NavigationProperty.THING);
        }

        MultiDatastream original = (MultiDatastream) pm.get(EntityType.MULTIDATASTREAM, entityFactories.idFromObject(dsId));
        int countOrig = original.getMultiObservationDataTypes().size();

        int countUom = countOrig;
        if (d.isSetUnitOfMeasurements()) {
            if (d.getUnitOfMeasurements() == null) {
                throw new IncompleteEntityException("unitOfMeasurements" + CAN_NOT_BE_NULL);
            }
            List<UnitOfMeasurement> uoms = d.getUnitOfMeasurements();
            countUom = uoms.size();
            update.set(qd.unitOfMeasurements, objectToJson(uoms));
            message.addField(EntityProperty.UNITOFMEASUREMENTS);
        }
        int countDataTypes = countOrig;
        if (d.isSetMultiObservationDataTypes()) {
            List<String> dataTypes = d.getMultiObservationDataTypes();
            if (dataTypes == null) {
                throw new IncompleteEntityException("multiObservationDataTypes" + CAN_NOT_BE_NULL);
            }
            countDataTypes = dataTypes.size();
            update.set(qd.observationTypes, objectToJson(dataTypes));
            message.addField(EntityProperty.MULTIOBSERVATIONDATATYPES);
        }
        EntitySet<ObservedProperty> ops = d.getObservedProperties();
        int countOps = countOrig + ops.size();
        for (ObservedProperty op : ops) {
            if (op.getId() == null || !entityExists(op)) {
                throw new NoSuchEntityException("ObservedProperty with no id or not found.");
            }
        }

        if (countUom != countDataTypes) {
            throw new IllegalArgumentException("New number of unitOfMeasurements does not match new number of multiObservationDataTypes.");
        }
        if (countUom != countOps) {
            throw new IllegalArgumentException("New number of unitOfMeasurements does not match new number of ObservedProperties.");
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

        // Link existing ObservedProperties to the MultiDatastream.
        int rank = countOrig;
        for (ObservedProperty op : ops) {
            J opId = (J) op.getId().getValue();
            AbstractQMultiDatastreamsObsProperties<? extends AbstractQMultiDatastreamsObsProperties, I, J> qMdOp = qCollection.qMultiDatastreamsObsProperties;
            long oCount = qFactory.insert(qMdOp)
                    .set(qMdOp.getMultiDatastreamId(), dsId)
                    .set(qMdOp.getObsPropertyId(), opId)
                    .set(qMdOp.rank, rank)
                    .execute();
            if (oCount > 0) {
                LOGGER.debug("Assigned datastream {} to ObservedProperty {} with rank {}.", dsId, opId, rank);
            }
            rank++;
        }

        // Link existing Observations to the MultiDatastream.
        for (Observation o : d.getObservations()) {
            if (o.getId() == null || !entityExists(o)) {
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

    public boolean insertFeatureOfInterest(FeatureOfInterest foi) throws IncompleteEntityException {
        // No linked entities to check first.
        SQLQueryFactory qFactory = pm.createQueryFactory();
        AbstractQFeatures<? extends AbstractQFeatures, I, J> qfoi = qCollection.qFeatures;
        SQLInsertClause insert = qFactory.insert(qfoi);
        insert.set(qfoi.name, foi.getName());
        insert.set(qfoi.description, foi.getDescription());
        insert.set(qfoi.properties, objectToJson(foi.getProperties()));

        String encodingType = foi.getEncodingType();
        insert.set(qfoi.encodingType, encodingType);
        insertGeometry(insert, qfoi.feature, qfoi.geom, encodingType, foi.getFeature());

        insertUserDefinedId(insert, qfoi.getId(), foi);

        J generatedId = insert.executeWithKey(qfoi.getId());
        LOGGER.debug("Inserted FeatureOfInterest. Created id = {}.", generatedId);
        foi.setId(entityFactories.idFromObject(generatedId));
        return true;
    }

    public EntityChangedMessage updateFeatureOfInterest(FeatureOfInterest foi, J foiId) throws NoSuchEntityException, IncompleteEntityException {
        SQLQueryFactory qFactory = pm.createQueryFactory();
        AbstractQFeatures<? extends AbstractQFeatures, I, J> qfoi = qCollection.qFeatures;
        SQLUpdateClause update = qFactory.update(qfoi);
        EntityChangedMessage message = new EntityChangedMessage();

        if (foi.isSetName()) {
            if (foi.getName() == null) {
                throw new IncompleteEntityException("name" + CAN_NOT_BE_NULL);
            }
            update.set(qfoi.name, foi.getName());
            message.addField(EntityProperty.NAME);
        }
        if (foi.isSetDescription()) {
            if (foi.getDescription() == null) {
                throw new IncompleteEntityException(EntityProperty.DESCRIPTION.jsonName + CAN_NOT_BE_NULL);
            }
            update.set(qfoi.description, foi.getDescription());
            message.addField(EntityProperty.DESCRIPTION);
        }
        if (foi.isSetProperties()) {
            update.set(qfoi.properties, objectToJson(foi.getProperties()));
            message.addField(EntityProperty.PROPERTIES);
        }

        if (foi.isSetEncodingType() && foi.getEncodingType() == null) {
            throw new IncompleteEntityException("encodingType" + CAN_NOT_BE_NULL);
        }
        if (foi.isSetFeature() && foi.getFeature() == null) {
            throw new IncompleteEntityException("feature" + CAN_NOT_BE_NULL);
        }
        if (foi.isSetEncodingType() && foi.getEncodingType() != null && foi.isSetFeature() && foi.getFeature() != null) {
            String encodingType = foi.getEncodingType();
            update.set(qfoi.encodingType, encodingType);
            insertGeometry(update, qfoi.feature, qfoi.geom, encodingType, foi.getFeature());
            message.addField(EntityProperty.ENCODINGTYPE);
            message.addField(EntityProperty.FEATURE);
        } else if (foi.isSetEncodingType() && foi.getEncodingType() != null) {
            String encodingType = foi.getEncodingType();
            update.set(qfoi.encodingType, encodingType);
            message.addField(EntityProperty.ENCODINGTYPE);
        } else if (foi.isSetFeature() && foi.getFeature() != null) {
            String encodingType = qFactory.select(qfoi.encodingType)
                    .from(qfoi)
                    .where(qfoi.getId().eq(foiId))
                    .fetchFirst();
            Object parsedObject = reParseGeometry(encodingType, foi.getFeature());
            insertGeometry(update, qfoi.feature, qfoi.geom, encodingType, parsedObject);
            message.addField(EntityProperty.FEATURE);
        }

        update.where(qfoi.getId().eq(foiId));
        long count = 0;
        if (!update.isEmpty()) {
            count = update.execute();
        }
        if (count > 1) {
            LOGGER.error("Updating FeatureOfInterest {} caused {} rows to change!", foiId, count);
            throw new IllegalStateException(CHANGED_MULTIPLE_ROWS);
        }

        // Link existing Observations to the FeatureOfInterest.
        for (Observation o : foi.getObservations()) {
            if (o.getId() == null || !entityExists(o)) {
                throw new NoSuchEntityException(EntityType.OBSERVATION.entityName + NO_ID_OR_NOT_FOUND);
            }
            J obsId = (J) o.getId().getValue();
            AbstractQObservations<? extends AbstractQObservations, I, J> qo = qCollection.qObservations;
            long oCount = qFactory.update(qo)
                    .set(qo.getFeatureId(), foiId)
                    .where(qo.getId().eq(obsId))
                    .execute();
            if (oCount > 0) {
                LOGGER.debug("Assigned FeatureOfInterest {} to Observation {}.", foiId, obsId);
            }
        }

        LOGGER.debug("Updated FeatureOfInterest {}", foiId);
        return message;
    }

    public FeatureOfInterest generateFeatureOfInterest(Id datastreamId, boolean isMultiDatastream) throws NoSuchEntityException, IncompleteEntityException {
        J dsId = (J) datastreamId.getValue();
        SQLQueryFactory qf = pm.createQueryFactory();
        AbstractQLocations<? extends AbstractQLocations, I, J> ql = qCollection.qLocations;
        AbstractQThingsLocations<? extends AbstractQThingsLocations, I, J> qtl = qCollection.qThingsLocations;
        AbstractQThings<? extends AbstractQThings, I, J> qt = qCollection.qThings;
        AbstractQDatastreams<? extends AbstractQDatastreams, I, J> qd = qCollection.qDatastreams;
        AbstractQMultiDatastreams<? extends AbstractQMultiDatastreams, I, J> qmd = qCollection.qMultiDatastreams;

        SQLQuery<Tuple> query = qf.select(ql.getId(), ql.getGenFoiId(), ql.encodingType)
                .from(ql)
                .innerJoin(qtl).on(ql.getId().eq(qtl.getLocationId()))
                .innerJoin(qt).on(qt.getId().eq(qtl.getThingId()));
        if (isMultiDatastream) {
            query.innerJoin(qmd).on(qmd.getThingId().eq(qt.getId()))
                    .where(qmd.getId().eq(dsId));
        } else {
            query.innerJoin(qd).on(qd.getThingId().eq(qt.getId()))
                    .where(qd.getId().eq(dsId));
        }
        List<Tuple> tuples = query.fetch();
        if (tuples.isEmpty()) {
            // Can not generate foi from Thing with no locations.
            throw new NoSuchEntityException("Can not generate foi for Thing with no locations.");
        }
        // See if any of the locations have a generated foi.
        // Also track if any of the location has a supported encoding type.
        J genFoiId = null;
        J locationId = null;
        for (Tuple tuple : tuples) {
            genFoiId = tuple.get(ql.getGenFoiId());
            if (genFoiId != null) {
                break;
            }
            String encodingType = tuple.get(ql.encodingType);
            if (encodingType != null && GeoJsonDeserializier.ENCODINGS.contains(encodingType.toLowerCase())) {
                locationId = tuple.get(ql.getId());
            }
        }
        // Either genFoiId will have a value, if a generated foi was found,
        // Or locationId will have a value if a supported encoding type was found.

        FeatureOfInterest foi;
        if (genFoiId != null) {
            foi = new FeatureOfInterest();
            foi.setId(entityFactories.idFromObject(genFoiId));
        } else if (locationId != null) {
            query = qf.select(ql.getId(), ql.encodingType, ql.location)
                    .from(ql)
                    .where(ql.getId().eq(locationId));
            Tuple tuple = query.fetchOne();
            if (tuple == null) {
                // Can not generate foi from Thing with no locations.
                // Should not happen, since the query succeeded just before.
                throw new NoSuchEntityException("Can not generate foi for Thing with no locations.");
            }
            String encoding = tuple.get(ql.encodingType);
            String locString = tuple.get(ql.location);
            Object locObject = Utils.locationFromEncoding(encoding, locString);
            foi = new FeatureOfInterestBuilder()
                    .setName("FoI for location " + locationId)
                    .setDescription("Generated from location " + locationId)
                    .setEncodingType(encoding)
                    .setFeature(locObject)
                    .build();
            insertFeatureOfInterest(foi);
            J foiId = (J) foi.getId().getValue();
            qf.update(ql)
                    .set(ql.getGenFoiId(), (J) foi.getId().getValue())
                    .where(ql.getId().eq(locationId))
                    .execute();
            LOGGER.debug("Generated foi {} from Location {}.", foiId, locationId);
        } else {
            // Can not generate foi from Thing with no locations.
            throw new NoSuchEntityException("Can not generate foi for Thing, all locations have an un supported encoding type.");
        }
        return foi;
    }

    public boolean insertHistoricalLocation(HistoricalLocation h) throws NoSuchEntityException, IncompleteEntityException {
        Thing t = h.getThing();
        entityExistsOrCreate(t);
        J thingId = (J) h.getThing().getId().getValue();

        Timestamp newTime = new Timestamp(h.getTime().getDateTime().getMillis());

        SQLQueryFactory qFactory = pm.createQueryFactory();
        AbstractQHistLocations<? extends AbstractQHistLocations, I, J> qhl = qCollection.qHistLocations;
        SQLInsertClause insert = qFactory.insert(qhl);
        insert.set(qhl.time, newTime);
        insert.set(qhl.getThingId(), thingId);

        insertUserDefinedId(insert, qhl.getId(), h);

        J generatedId = insert.executeWithKey(qhl.getId());
        LOGGER.debug("Inserted HistoricalLocation. Created id = {}.", generatedId);
        h.setId(entityFactories.idFromObject(generatedId));

        EntitySet<Location> locations = h.getLocations();
        for (Location l : locations) {
            entityExistsOrCreate(l);
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
                if (l.getId() == null || !entityExists(l)) {
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

    public EntityChangedMessage updateHistoricalLocation(HistoricalLocation hl, J id) throws IncompleteEntityException {
        SQLQueryFactory qFactory = pm.createQueryFactory();
        AbstractQHistLocations<? extends AbstractQHistLocations, I, J> qhl = qCollection.qHistLocations;
        SQLUpdateClause update = qFactory.update(qhl);
        EntityChangedMessage message = new EntityChangedMessage();

        if (hl.isSetThing()) {
            if (!entityExists(hl.getThing())) {
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
            LOGGER.error("Updating Location {} caused {} rows to change!", id, count);
            throw new IllegalStateException(CHANGED_MULTIPLE_ROWS);
        }
        LOGGER.debug("Updated Location {}", id);

        // Link existing locations to the HistoricalLocation.
        for (Location l : hl.getLocations()) {
            if (!entityExists(l)) {
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

    public boolean insertLocation(Location l) throws NoSuchEntityException, IncompleteEntityException {
        SQLQueryFactory qFactory = pm.createQueryFactory();
        AbstractQLocations<? extends AbstractQLocations, I, J> ql = qCollection.qLocations;
        SQLInsertClause insert = qFactory.insert(ql);
        insert.set(ql.name, l.getName());
        insert.set(ql.description, l.getDescription());
        insert.set(ql.properties, objectToJson(l.getProperties()));

        String encodingType = l.getEncodingType();
        insert.set(ql.encodingType, encodingType);
        insertGeometry(insert, ql.location, ql.geom, encodingType, l.getLocation());

        insertUserDefinedId(insert, ql.getId(), l);

        J locationId = insert.executeWithKey(ql.getId());
        LOGGER.debug("Inserted Location. Created id = {}.", locationId);
        l.setId(entityFactories.idFromObject(locationId));

        // Link Things
        EntitySet<Thing> things = l.getThings();
        for (Thing t : things) {
            entityExistsOrCreate(t);
            J thingId = (J) t.getId().getValue();

            // Unlink old Locations from Thing.
            AbstractQThingsLocations<? extends AbstractQThingsLocations, I, J> qtl = qCollection.qThingsLocations;
            long count = qFactory.delete(qtl).where(qtl.getThingId().eq(thingId)).execute();
            LOGGER.debug(UNLINKED_L_FROM_T, count, thingId);

            // Link new Location to thing.
            insert = qFactory.insert(qtl);
            insert.set(qtl.getThingId(), thingId);
            insert.set(qtl.getLocationId(), locationId);
            insert.execute();
            LOGGER.debug(LINKED_L_TO_T, locationId, thingId);

            // Create HistoricalLocation for Thing
            AbstractQHistLocations<? extends AbstractQHistLocations, I, J> qhl = qCollection.qHistLocations;
            insert = qFactory.insert(qhl);
            insert.set(qhl.getThingId(), thingId);
            insert.set(qhl.time, new Timestamp(Calendar.getInstance().getTimeInMillis()));
            // TODO: maybe use histLocationId based on locationId
            J histLocationId = insert.executeWithKey(qhl.getId());
            LOGGER.debug(CREATED_HL, histLocationId);

            // Link Location to HistoricalLocation.
            AbstractQLocationsHistLocations<? extends AbstractQLocationsHistLocations, I, J> qlhl = qCollection.qLocationsHistLocations;
            qFactory.insert(qlhl)
                    .set(qlhl.getHistLocationId(), histLocationId)
                    .set(qlhl.getLocationId(), locationId)
                    .execute();
            LOGGER.debug(LINKED_L_TO_HL, locationId, histLocationId);
        }

        return true;
    }

    public EntityChangedMessage updateLocation(Location l, J locationId) throws NoSuchEntityException, IncompleteEntityException {
        SQLQueryFactory qFactory = pm.createQueryFactory();
        AbstractQLocations<? extends AbstractQLocations, I, J> ql = qCollection.qLocations;
        SQLUpdateClause update = qFactory.update(ql);
        EntityChangedMessage message = new EntityChangedMessage();

        if (l.isSetName()) {
            if (l.getName() == null) {
                throw new IncompleteEntityException("name" + CAN_NOT_BE_NULL);
            }
            update.set(ql.name, l.getName());
            message.addField(EntityProperty.NAME);
        }
        if (l.isSetDescription()) {
            if (l.getDescription() == null) {
                throw new IncompleteEntityException(EntityProperty.DESCRIPTION.jsonName + CAN_NOT_BE_NULL);
            }
            update.set(ql.description, l.getDescription());
            message.addField(EntityProperty.DESCRIPTION);
        }
        if (l.isSetProperties()) {
            update.set(ql.properties, objectToJson(l.getProperties()));
            message.addField(EntityProperty.PROPERTIES);
        }

        if (l.isSetEncodingType() && l.getEncodingType() == null) {
            throw new IncompleteEntityException("encodingType" + CAN_NOT_BE_NULL);
        }
        if (l.isSetLocation() && l.getLocation() == null) {
            throw new IncompleteEntityException("locations" + CAN_NOT_BE_NULL);
        }
        if (l.isSetEncodingType() && l.getEncodingType() != null && l.isSetLocation() && l.getLocation() != null) {
            String encodingType = l.getEncodingType();
            update.set(ql.encodingType, encodingType);
            insertGeometry(update, ql.location, ql.geom, encodingType, l.getLocation());
            message.addField(EntityProperty.ENCODINGTYPE);
            message.addField(EntityProperty.LOCATION);
        } else if (l.isSetEncodingType() && l.getEncodingType() != null) {
            String encodingType = l.getEncodingType();
            update.set(ql.encodingType, encodingType);
            message.addField(EntityProperty.ENCODINGTYPE);
        } else if (l.isSetLocation() && l.getLocation() != null) {
            String encodingType = qFactory.select(ql.encodingType)
                    .from(ql)
                    .where(ql.getId().eq(locationId))
                    .fetchFirst();
            Object parsedObject = reParseGeometry(encodingType, l.getLocation());
            insertGeometry(update, ql.location, ql.geom, encodingType, parsedObject);
            message.addField(EntityProperty.LOCATION);
        }

        update.where(ql.getId().eq(locationId));
        long count = 0;
        if (!update.isEmpty()) {
            count = update.execute();
        }
        if (count > 1) {
            LOGGER.error("Updating Location {} caused {} rows to change!", locationId, count);
            throw new IllegalStateException(CHANGED_MULTIPLE_ROWS);
        }
        LOGGER.debug("Updated Location {}", locationId);

        // Link HistoricalLocation.
        for (HistoricalLocation hl : l.getHistoricalLocations()) {
            if (hl.getId() == null) {
                throw new IllegalArgumentException("HistoricalLocation with no id.");
            }
            J hlId = (J) hl.getId().getValue();

            AbstractQLocationsHistLocations<? extends AbstractQLocationsHistLocations, I, J> qlhl = qCollection.qLocationsHistLocations;
            SQLInsertClause insert = qFactory.insert(qlhl);
            insert.set(qlhl.getHistLocationId(), hlId);
            insert.set(qlhl.getLocationId(), locationId);
            insert.execute();
            LOGGER.debug(LINKED_L_TO_HL, locationId, hlId);
        }

        // Link Things
        EntitySet<Thing> things = l.getThings();
        for (Thing t : things) {
            if (!entityExists(t)) {
                throw new NoSuchEntityException("Thing not found.");
            }
            J thingId = (J) t.getId().getValue();

            // Unlink old Locations from Thing.
            AbstractQThingsLocations<? extends AbstractQThingsLocations, I, J> qtl = qCollection.qThingsLocations;
            count = qFactory.delete(qtl).where(qtl.getThingId().eq(thingId)).execute();
            LOGGER.debug(UNLINKED_L_FROM_T, count, thingId);

            // Link new Location to thing.
            SQLInsertClause insert = qFactory.insert(qtl);
            insert.set(qtl.getThingId(), thingId);
            insert.set(qtl.getLocationId(), locationId);
            insert.execute();
            LOGGER.debug(LINKED_L_TO_T, locationId, thingId);

            // Create HistoricalLocation for Thing
            AbstractQHistLocations<? extends AbstractQHistLocations, I, J> qhl = qCollection.qHistLocations;
            insert = qFactory.insert(qhl);
            insert.set(qhl.getThingId(), thingId);
            insert.set(qhl.time, new Timestamp(Calendar.getInstance().getTimeInMillis()));
            // TODO: maybe use histLocationId based on locationId
            J histLocationId = insert.executeWithKey(qhl.getId());
            LOGGER.debug(CREATED_HL, histLocationId);

            // Link Location to HistoricalLocation.
            AbstractQLocationsHistLocations<? extends AbstractQLocationsHistLocations, I, J> qlhl = qCollection.qLocationsHistLocations;
            qFactory.insert(qlhl)
                    .set(qlhl.getHistLocationId(), histLocationId)
                    .set(qlhl.getLocationId(), locationId)
                    .execute();
            LOGGER.debug(LINKED_L_TO_HL, locationId, histLocationId);
        }
        return message;
    }

    public boolean insertObservation(Observation o) throws NoSuchEntityException, IncompleteEntityException {
        Datastream ds = o.getDatastream();
        MultiDatastream mds = o.getMultiDatastream();
        Id streamId;
        boolean isMultiDatastream = false;
        if (ds != null) {
            entityExistsOrCreate(ds);
            streamId = ds.getId();
        } else if (mds != null) {
            entityExistsOrCreate(mds);
            streamId = mds.getId();
            isMultiDatastream = true;
        } else {
            throw new IncompleteEntityException("Missing Datastream or MultiDatastream.");
        }

        FeatureOfInterest f = o.getFeatureOfInterest();
        if (f == null) {
            f = generateFeatureOfInterest(streamId, isMultiDatastream);
        } else {
            entityExistsOrCreate(f);
        }

        SQLQueryFactory qFactory = pm.createQueryFactory();
        AbstractQObservations<? extends AbstractQObservations, I, J> qo = qCollection.qObservations;
        SQLInsertClause insert = qFactory.insert(qo);

        insert.set(qo.parameters, objectToJson(o.getParameters()));
        TimeValue phenomenonTime = o.getPhenomenonTime();
        if (phenomenonTime == null) {
            phenomenonTime = TimeInstant.now();
        }
        insertTimeValue(insert, qo.phenomenonTimeStart, qo.phenomenonTimeEnd, phenomenonTime);
        insertTimeInstant(insert, qo.resultTime, o.getResultTime());
        insertTimeInterval(insert, qo.validTimeStart, qo.validTimeEnd, o.getValidTime());

        Object result = o.getResult();
        if (isMultiDatastream) {
            if (!(result instanceof List)) {
                throw new IllegalArgumentException("Multidatastream only accepts array results.");
            }
            List list = (List) result;
            ResourcePath path = mds.getPath();
            path.addPathElement(new EntitySetPathElement(EntityType.OBSERVEDPROPERTY, null), false, false);
            long count = pm.count(path, null);
            if (count != list.size()) {
                throw new IllegalArgumentException("Size of result array (" + list.size() + ") must match number of observed properties (" + count + ") in the MultiDatastream.");
            }
        }

        if (result instanceof Number) {
            insert.set(qo.resultType, ResultType.NUMBER.sqlValue());
            insert.set(qo.resultString, result.toString());
            insert.set(qo.resultNumber, ((Number) result).doubleValue());
        } else if (result instanceof Boolean) {
            insert.set(qo.resultType, ResultType.BOOLEAN.sqlValue());
            insert.set(qo.resultString, result.toString());
            insert.set(qo.resultBoolean, (Boolean) result);
        } else if (result instanceof String) {
            insert.set(qo.resultType, ResultType.STRING.sqlValue());
            insert.set(qo.resultString, result.toString());
        } else {
            insert.set(qo.resultType, ResultType.OBJECT_ARRAY.sqlValue());
            insert.set(qo.resultJson, objectToJson(result));
        }

        if (o.getResultQuality() != null) {
            insert.set(qo.resultQuality, o.getResultQuality().toString());
        }
        if (ds != null) {
            insert.set(qo.getDatastreamId(), (J) ds.getId().getValue());
        }
        if (mds != null) {
            insert.set(qo.getMultiDatastreamId(), (J) mds.getId().getValue());
        }
        insert.set(qo.getFeatureId(), (J) f.getId().getValue());

        insertUserDefinedId(insert, qo.getId(), o);

        J generatedId = insert.executeWithKey(qo.getId());
        LOGGER.debug("Inserted Observation. Created id = {}.", generatedId);
        o.setId(entityFactories.idFromObject(generatedId));
        return true;
    }

    public EntityChangedMessage updateObservation(Observation o, J id) throws IncompleteEntityException {
        Observation oldObservation = (Observation) pm.get(EntityType.OBSERVATION, entityFactories.idFromObject(id));
        Datastream ds = oldObservation.getDatastream();
        MultiDatastream mds = oldObservation.getMultiDatastream();
        boolean newHasDatastream = ds != null;
        boolean newHasMultiDatastream = mds != null;

        SQLQueryFactory qFactory = pm.createQueryFactory();
        AbstractQObservations<? extends AbstractQObservations, I, J> qo = qCollection.qObservations;
        SQLUpdateClause update = qFactory.update(qo);
        EntityChangedMessage message = new EntityChangedMessage();

        if (o.isSetDatastream()) {
            if (o.getDatastream() == null) {
                newHasDatastream = false;
                update.setNull(qo.getDatastreamId());
                message.addField(NavigationProperty.DATASTREAM);
            } else {
                if (!entityExists(o.getDatastream())) {
                    throw new IncompleteEntityException("Datastream not found.");
                }
                newHasDatastream = true;
                ds = o.getDatastream();
                update.set(qo.getDatastreamId(), (J) ds.getId().getValue());
                message.addField(NavigationProperty.DATASTREAM);
            }
        }
        if (o.isSetMultiDatastream()) {
            mds = o.getMultiDatastream();
            if (mds == null) {
                newHasMultiDatastream = false;
                update.setNull(qo.getMultiDatastreamId());
                message.addField(NavigationProperty.MULTIDATASTREAM);
            } else {
                if (!entityExists(mds)) {
                    throw new IncompleteEntityException("MultiDatastream not found.");
                }
                newHasMultiDatastream = true;
                update.set(qo.getMultiDatastreamId(), (J) mds.getId().getValue());
                message.addField(NavigationProperty.MULTIDATASTREAM);
            }
        }
        if (newHasDatastream == newHasMultiDatastream) {
            throw new IllegalArgumentException("Observation must have either a Datastream or a MultiDatastream.");
        }
        if (o.isSetFeatureOfInterest()) {
            if (!entityExists(o.getFeatureOfInterest())) {
                throw new IncompleteEntityException("FeatureOfInterest not found.");
            }
            update.set(qo.getFeatureId(), (J) o.getFeatureOfInterest().getId().getValue());
            message.addField(NavigationProperty.FEATUREOFINTEREST);
        }
        if (o.isSetParameters()) {
            update.set(qo.parameters, objectToJson(o.getParameters()));
            message.addField(EntityProperty.PARAMETERS);
        }
        if (o.isSetPhenomenonTime()) {
            if (o.getPhenomenonTime() == null) {
                throw new IncompleteEntityException("phenomenonTime" + CAN_NOT_BE_NULL);
            }
            insertTimeValue(update, qo.phenomenonTimeStart, qo.phenomenonTimeEnd, o.getPhenomenonTime());
            message.addField(EntityProperty.PHENOMENONTIME);
        }

        if (o.isSetResult() && o.getResult() != null) {
            Object result = o.getResult();
            if (newHasMultiDatastream) {
                if (!(result instanceof List)) {
                    throw new IllegalArgumentException("Multidatastream only accepts array results.");
                }
                List list = (List) result;
                ResourcePath path = mds.getPath();
                path.addPathElement(new EntitySetPathElement(EntityType.OBSERVEDPROPERTY, null), false, false);
                long count = pm.count(path, null);
                if (count != list.size()) {
                    throw new IllegalArgumentException("Size of result array (" + list.size() + ") must match number of observed properties (" + count + ") in the MultiDatastream.");
                }
            }
            if (result instanceof Number) {
                update.set(qo.resultType, ResultType.NUMBER.sqlValue());
                update.set(qo.resultString, result.toString());
                update.set(qo.resultNumber, ((Number) result).doubleValue());
                update.setNull(qo.resultBoolean);
                update.setNull(qo.resultJson);
            } else if (result instanceof Boolean) {
                update.set(qo.resultType, ResultType.BOOLEAN.sqlValue());
                update.set(qo.resultString, result.toString());
                update.set(qo.resultBoolean, (Boolean) result);
                update.setNull(qo.resultNumber);
                update.setNull(qo.resultJson);
            } else if (result instanceof String) {
                update.set(qo.resultType, ResultType.STRING.sqlValue());
                update.set(qo.resultString, result.toString());
                update.setNull(qo.resultNumber);
                update.setNull(qo.resultBoolean);
                update.setNull(qo.resultJson);
            } else {
                update.set(qo.resultType, ResultType.OBJECT_ARRAY.sqlValue());
                update.set(qo.resultJson, objectToJson(result));
                update.setNull(qo.resultString);
                update.setNull(qo.resultNumber);
                update.setNull(qo.resultBoolean);
            }
            message.addField(EntityProperty.RESULT);
        }

        if (o.isSetResultQuality()) {
            update.set(qo.resultQuality, objectToJson(o.getResultQuality()));
            message.addField(EntityProperty.RESULTQUALITY);
        }
        if (o.isSetResultTime()) {
            insertTimeInstant(update, qo.resultTime, o.getResultTime());
            message.addField(EntityProperty.RESULTTIME);
        }
        if (o.isSetValidTime()) {
            insertTimeInterval(update, qo.validTimeStart, qo.validTimeEnd, o.getValidTime());
            message.addField(EntityProperty.VALIDTIME);
        }
        update.where(qo.getId().eq(id));
        long count = 0;
        if (!update.isEmpty()) {
            count = update.execute();
        }
        if (count > 1) {
            LOGGER.error("Updating Observation {} caused {} rows to change!", id, count);
            throw new IllegalStateException(CHANGED_MULTIPLE_ROWS);
        }
        LOGGER.debug("Updated Observation {}", id);
        return message;
    }

    public boolean insertObservedProperty(ObservedProperty op) throws NoSuchEntityException, IncompleteEntityException {
        SQLQueryFactory qFactory = pm.createQueryFactory();
        AbstractQObsProperties<? extends AbstractQObsProperties, I, J> qop = qCollection.qObsProperties;
        SQLInsertClause insert = qFactory.insert(qop);
        insert.set(qop.definition, op.getDefinition());
        insert.set(qop.name, op.getName());
        insert.set(qop.description, op.getDescription());
        insert.set(qop.properties, objectToJson(op.getProperties()));

        insertUserDefinedId(insert, qop.getId(), op);

        J generatedId = insert.executeWithKey(qop.getId());
        LOGGER.debug("Inserted ObservedProperty. Created id = {}.", generatedId);
        op.setId(entityFactories.idFromObject(generatedId));

        // Create new datastreams, if any.
        for (Datastream ds : op.getDatastreams()) {
            ds.setSensor(new SensorBuilder().setId(op.getId()).build());
            ds.complete();
            pm.insert(ds);
        }

        // Create new multiDatastreams, if any.
        for (MultiDatastream mds : op.getMultiDatastreams()) {
            mds.setSensor(new SensorBuilder().setId(op.getId()).build());
            mds.complete();
            pm.insert(mds);
        }

        return true;
    }

    public EntityChangedMessage updateObservedProperty(ObservedProperty op, J opId) throws NoSuchEntityException, IncompleteEntityException {
        SQLQueryFactory qFactory = pm.createQueryFactory();
        AbstractQObsProperties<? extends AbstractQObsProperties, I, J> qop = qCollection.qObsProperties;
        SQLUpdateClause update = qFactory.update(qop);
        EntityChangedMessage message = new EntityChangedMessage();

        if (op.isSetDefinition()) {
            if (op.getDefinition() == null) {
                throw new IncompleteEntityException("definition" + CAN_NOT_BE_NULL);
            }
            update.set(qop.definition, op.getDefinition());
            message.addField(EntityProperty.DEFINITION);
        }
        if (op.isSetDescription()) {
            if (op.getDescription() == null) {
                throw new IncompleteEntityException(EntityProperty.DESCRIPTION.jsonName + CAN_NOT_BE_NULL);
            }
            update.set(qop.description, op.getDescription());
            message.addField(EntityProperty.DESCRIPTION);
        }
        if (op.isSetName()) {
            if (op.getName() == null) {
                throw new IncompleteEntityException("name" + CAN_NOT_BE_NULL);
            }
            update.set(qop.name, op.getName());
            message.addField(EntityProperty.NAME);
        }
        if (op.isSetProperties()) {
            update.set(qop.properties, objectToJson(op.getProperties()));
            message.addField(EntityProperty.PROPERTIES);
        }

        update.where(qop.getId().eq(opId));
        long count = 0;
        if (!update.isEmpty()) {
            count = update.execute();
        }
        if (count > 1) {
            LOGGER.error("Updating ObservedProperty {} caused {} rows to change!", opId, count);
            throw new IllegalStateException(CHANGED_MULTIPLE_ROWS);
        }

        // Link existing Datastreams to the observedProperty.
        for (Datastream ds : op.getDatastreams()) {
            if (ds.getId() == null || !entityExists(ds)) {
                throw new NoSuchEntityException("ObservedProperty" + NO_ID_OR_NOT_FOUND);
            }
            J dsId = (J) ds.getId().getValue();
            AbstractQDatastreams<? extends AbstractQDatastreams, I, J> qds = qCollection.qDatastreams;
            long dsCount = qFactory.update(qds)
                    .set(qds.getObsPropertyId(), opId)
                    .where(qds.getId().eq(dsId))
                    .execute();
            if (dsCount > 0) {
                LOGGER.debug("Assigned datastream {} to ObservedProperty {}.", dsId, opId);
            }
        }

        if (!op.getMultiDatastreams().isEmpty()) {
            throw new IllegalArgumentException("Can not add MultiDatastreams to an ObservedProperty.");
        }

        LOGGER.debug("Updated ObservedProperty {}", opId);
        return message;
    }

    public boolean insertSensor(Sensor s) throws NoSuchEntityException, IncompleteEntityException {
        SQLQueryFactory qFactory = pm.createQueryFactory();
        AbstractQSensors<? extends AbstractQSensors, I, J> qs = qCollection.qSensors;
        SQLInsertClause insert = qFactory.insert(qs);
        insert.set(qs.name, s.getName());
        insert.set(qs.description, s.getDescription());
        insert.set(qs.encodingType, s.getEncodingType());
        // TODO: Check metadata serialisation.
        insert.set(qs.metadata, s.getMetadata().toString());
        insert.set(qs.properties, objectToJson(s.getProperties()));

        insertUserDefinedId(insert, qs.getId(), s);

        J generatedId = insert.executeWithKey(qs.getId());
        LOGGER.debug("Inserted Sensor. Created id = {}.", generatedId);
        s.setId(entityFactories.idFromObject(generatedId));

        // Create new datastreams, if any.
        for (Datastream ds : s.getDatastreams()) {
            ds.setSensor(new SensorBuilder().setId(s.getId()).build());
            ds.complete();
            pm.insert(ds);
        }

        // Create new multiDatastreams, if any.
        for (MultiDatastream mds : s.getMultiDatastreams()) {
            mds.setSensor(new SensorBuilder().setId(s.getId()).build());
            mds.complete();
            pm.insert(mds);
        }

        return true;
    }

    public EntityChangedMessage updateSensor(Sensor s, J sensorId) throws NoSuchEntityException, IncompleteEntityException {
        SQLQueryFactory qFactory = pm.createQueryFactory();
        AbstractQSensors<? extends AbstractQSensors, I, J> qs = qCollection.qSensors;
        SQLUpdateClause update = qFactory.update(qs);
        EntityChangedMessage message = new EntityChangedMessage();

        if (s.isSetName()) {
            if (s.getName() == null) {
                throw new IncompleteEntityException("name" + CAN_NOT_BE_NULL);
            }
            update.set(qs.name, s.getName());
            message.addField(EntityProperty.NAME);
        }
        if (s.isSetDescription()) {
            if (s.getDescription() == null) {
                throw new IncompleteEntityException(EntityProperty.DESCRIPTION.jsonName + CAN_NOT_BE_NULL);
            }
            update.set(qs.description, s.getDescription());
            message.addField(EntityProperty.DESCRIPTION);
        }
        if (s.isSetEncodingType()) {
            if (s.getEncodingType() == null) {
                throw new IncompleteEntityException("encodingType" + CAN_NOT_BE_NULL);
            }
            update.set(qs.encodingType, s.getEncodingType());
            message.addField(EntityProperty.ENCODINGTYPE);
        }
        if (s.isSetMetadata()) {
            if (s.getMetadata() == null) {
                throw new IncompleteEntityException("metadata" + CAN_NOT_BE_NULL);
            }
            // TODO: Check metadata serialisation.
            update.set(qs.metadata, s.getMetadata().toString());
            message.addField(EntityProperty.METADATA);
        }
        if (s.isSetProperties()) {
            update.set(qs.properties, objectToJson(s.getProperties()));
            message.addField(EntityProperty.PROPERTIES);
        }

        update.where(qs.getId().eq(sensorId));
        long count = 0;
        if (!update.isEmpty()) {
            count = update.execute();
        }
        if (count > 1) {
            LOGGER.error("Updating Sensor {} caused {} rows to change!", sensorId, count);
            throw new IllegalStateException(CHANGED_MULTIPLE_ROWS);
        }

        // Link existing Datastreams to the sensor.
        for (Datastream ds : s.getDatastreams()) {
            if (ds.getId() == null || !entityExists(ds)) {
                throw new NoSuchEntityException("Datastream" + NO_ID_OR_NOT_FOUND);
            }
            J dsId = (J) ds.getId().getValue();
            AbstractQDatastreams<? extends AbstractQDatastreams, I, J> qds = qCollection.qDatastreams;
            long dsCount = qFactory.update(qds)
                    .set(qds.getSensorId(), sensorId)
                    .where(qds.getId().eq(dsId))
                    .execute();
            if (dsCount > 0) {
                LOGGER.debug("Assigned datastream {} to sensor {}.", dsId, sensorId);
            }
        }

        // Link existing MultiDatastreams to the sensor.
        for (MultiDatastream mds : s.getMultiDatastreams()) {
            if (mds.getId() == null || !entityExists(mds)) {
                throw new NoSuchEntityException("MultiDatastream" + NO_ID_OR_NOT_FOUND);
            }
            J mdsId = (J) mds.getId().getValue();
            AbstractQMultiDatastreams<? extends AbstractQMultiDatastreams, I, J> qmds = qCollection.qMultiDatastreams;
            long mdsCount = qFactory.update(qmds)
                    .set(qmds.getSensorId(), sensorId)
                    .where(qmds.getId().eq(mdsId))
                    .execute();
            if (mdsCount > 0) {
                LOGGER.debug("Assigned multiDatastream {} to sensor {}.", mdsId, sensorId);
            }
        }

        LOGGER.debug("Updated Sensor {}", sensorId);
        return message;
    }

    public boolean insertThing(Thing t) throws NoSuchEntityException, IncompleteEntityException {
        SQLQueryFactory qFactory = pm.createQueryFactory();
        AbstractQThings<? extends AbstractQThings, I, J> qt = qCollection.qThings;
        SQLInsertClause insert = qFactory.insert(qt);
        insert.set(qt.name, t.getName());
        insert.set(qt.description, t.getDescription());
        insert.set(qt.properties, objectToJson(t.getProperties()));

        insertUserDefinedId(insert, qt.getId(), t);

        J thingId = insert.executeWithKey(qt.getId());
        LOGGER.debug("Inserted Thing. Created id = {}.", thingId);
        t.setId(entityFactories.idFromObject(thingId));

        // Create new Locations, if any.
        List<J> locationIds = new ArrayList<>();
        for (Location l : t.getLocations()) {
            entityExistsOrCreate(l);
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

    public EntityChangedMessage updateThing(Thing t, J thingId) throws NoSuchEntityException, IncompleteEntityException {
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
            update.set(qt.properties, objectToJson(t.getProperties()));
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
            if (ds.getId() == null || !entityExists(ds)) {
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
            if (mds.getId() == null || !entityExists(mds)) {
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
                if (l.getId() == null || !entityExists(l)) {
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

    private <T extends StoreClause> void insertUserDefinedId(T clause, Path idPath, Entity entity) throws IncompleteEntityException {
        IdGenerationHandler idhandler = pm.createIdGenerationHanlder(entity);
        if (idhandler.useClientSuppliedId()) {
            idhandler.modifyClientSuppliedId();
            clause.set(idPath, (J) idhandler.getIdValue());
        }
    }

    private static <T extends StoreClause> T insertTimeValue(T clause, DateTimePath<Timestamp> startPath, DateTimePath<Timestamp> endPath, TimeValue time) {
        if (time instanceof TimeInstant) {
            TimeInstant timeInstant = (TimeInstant) time;
            insertTimeInstant(clause, endPath, timeInstant);
            return insertTimeInstant(clause, startPath, timeInstant);
        } else if (time instanceof TimeInterval) {
            TimeInterval timeInterval = (TimeInterval) time;
            return insertTimeInterval(clause, startPath, endPath, timeInterval);
        }
        return clause;
    }

    private static <T extends StoreClause> T insertTimeInstant(T clause, DateTimePath<Timestamp> path, TimeInstant time) {
        if (time == null) {
            return clause;
        }
        clause.set(path, new Timestamp(time.getDateTime().getMillis()));
        return clause;
    }

    private static <T extends StoreClause> T insertTimeInterval(T clause, DateTimePath<Timestamp> startPath, DateTimePath<Timestamp> endPath, TimeInterval time) {
        if (time == null) {
            return clause;
        }
        Interval interval = time.getInterval();
        clause.set(startPath, new Timestamp(interval.getStartMillis()));
        clause.set(endPath, new Timestamp(interval.getEndMillis()));
        return clause;
    }

    /**
     * Sets both the geometry and location in the clause.
     *
     * @param <T> The type of the clause.
     * @param clause The insert or update clause to add to.
     * @param locationPath The path to the location column.
     * @param geomPath The path to the geometry column.
     * @param encodingType The encoding type.
     * @param location The location.
     * @return The insert or update clause.
     */
    private <T extends StoreClause> T insertGeometry(T clause, StringPath locationPath, GeometryPath<Geometry> geomPath, String encodingType, final Object location) {
        if (encodingType != null && GeoJsonDeserializier.ENCODINGS.contains(encodingType.toLowerCase())) {
            String locJson;
            try {
                locJson = new GeoJsonSerializer().serialize(location);
            } catch (JsonProcessingException ex) {
                LOGGER.error("Failed to store.", ex);
                throw new IllegalArgumentException("encoding specifies geoJson, but location not parsable as such.");
            }

            // Postgres does not support Feature.
            Object geoLocation = location;
            if (location instanceof Feature) {
                geoLocation = ((Feature) location).getGeometry();
            }
            // Ensure the geoJson has a crs, otherwise Postgres complains.
            if (geoLocation instanceof GeoJsonObject) {
                GeoJsonObject geoJsonObject = (GeoJsonObject) geoLocation;
                Crs crs = geoJsonObject.getCrs();
                if (crs == null) {
                    crs = new Crs();
                    crs.setType(CrsType.name);
                    crs.getProperties().put("name", "EPSG:4326");
                    geoJsonObject.setCrs(crs);
                }
            }
            String geoJson;
            try {
                geoJson = new GeoJsonSerializer().serialize(geoLocation);
            } catch (JsonProcessingException ex) {
                LOGGER.error("Failed to store.", ex);
                throw new IllegalArgumentException("encoding specifies geoJson, but location not parsable as such.");
            }

            try {
                // geojson.jackson allows invalid polygons, geolatte catches those.
                new JsonMapper().fromJson(geoJson, Geometry.class);
            } catch (JsonException ex) {
                throw new IllegalArgumentException("Invalid geoJson: " + ex.getMessage());
            }
            clause.set(geomPath, Expressions.template(Geometry.class, "ST_Force2D(ST_Transform(ST_GeomFromGeoJSON({0}), 4326))", geoJson));
            clause.set(locationPath, locJson);
        } else {
            String json;
            json = objectToJson(location);
            clause.setNull(geomPath);
            clause.set(locationPath, json);
        }
        return clause;
    }

    private Object reParseGeometry(String encodingType, Object object) {
        String json = objectToJson(object);
        return Utils.locationFromEncoding(encodingType, json);
    }

    /**
     * Throws an exception if the entity has an id, but does not exist or if the
     * entity can not be created.
     *
     * @param pm the persistenceManager
     * @param e The Entity to check.
     * @throws NoSuchEntityException If the entity has an id, but does not
     * exist.
     * @throws IncompleteEntityException If the entity has no id, but is not
     * complete and can thus not be created.
     */
    private void entityExistsOrCreate(Entity e) throws NoSuchEntityException, IncompleteEntityException {
        if (e == null) {
            throw new NoSuchEntityException("No entity!");
        }

        if (e.getId() == null) {
            e.complete();
            // no id but complete -> create
            pm.insert(e);
            return;
        }

        if (entityExists(e)) {
            return;
        }

        // check if this is an incomplete entity
        try {
            e.complete();
        } catch (IncompleteEntityException exc) {
            // not complete and link entity does not exist
            throw new NoSuchEntityException("No such entity '" + e.getEntityType() + "' with id " + e.getId().getValue());
        }

        // complete with id -> create
        pm.insert(e);
    }

    public boolean entityExists(Entity e) {
        if (e == null || e.getId() == null) {
            return false;
        }
        J id = (J) e.getId().getValue();
        SQLQueryFactory qFactory = pm.createQueryFactory();
        long count = 0;
        switch (e.getEntityType()) {
            case DATASTREAM:
                AbstractQDatastreams<? extends AbstractQDatastreams, I, J> d = qCollection.qDatastreams;
                count = qFactory.select()
                        .from(d)
                        .where(d.getId().eq(id))
                        .fetchCount();
                break;

            case MULTIDATASTREAM:
                AbstractQMultiDatastreams<? extends AbstractQMultiDatastreams, I, J> md = qCollection.qMultiDatastreams;
                count = qFactory.select()
                        .from(md)
                        .where(md.getId().eq(id))
                        .fetchCount();
                break;

            case FEATUREOFINTEREST:
                AbstractQFeatures<? extends AbstractQFeatures, I, J> foi = qCollection.qFeatures;
                count = qFactory.select()
                        .from(foi)
                        .where(foi.getId().eq(id))
                        .fetchCount();
                break;

            case HISTORICALLOCATION:
                AbstractQHistLocations<? extends AbstractQHistLocations, I, J> h = qCollection.qHistLocations;
                count = qFactory.select()
                        .from(h)
                        .where(h.getId().eq(id))
                        .fetchCount();
                break;

            case LOCATION:
                AbstractQLocations<? extends AbstractQLocations, I, J> l = qCollection.qLocations;
                count = qFactory.select()
                        .from(l)
                        .where(l.getId().eq(id))
                        .fetchCount();
                break;

            case OBSERVATION:
                AbstractQObservations<? extends AbstractQObservations, I, J> o = qCollection.qObservations;
                count = qFactory.select()
                        .from(o)
                        .where(o.getId().eq(id))
                        .fetchCount();
                break;

            case OBSERVEDPROPERTY:
                AbstractQObsProperties<? extends AbstractQObsProperties, I, J> op = qCollection.qObsProperties;
                count = qFactory.select()
                        .from(op)
                        .where(op.getId().eq(id))
                        .fetchCount();
                break;

            case SENSOR:
                AbstractQSensors<? extends AbstractQSensors, I, J> s = qCollection.qSensors;
                count = qFactory.select()
                        .from(s)
                        .where(s.getId().eq(id))
                        .fetchCount();
                break;

            case THING:
                AbstractQThings<? extends AbstractQThings, I, J> t = qCollection.qThings;
                count = qFactory.select()
                        .from(t)
                        .where(t.getId().eq(id))
                        .fetchCount();
                break;

            default:
                throw new AssertionError(e.getEntityType().name());
        }
        if (count > 1) {
            LOGGER.error("More than one instance of {} with id {}.", e.getEntityType(), id);
        }
        return count > 0;
    }

    public boolean entityExists(ResourcePath path) {
        long count = pm.count(path, null);
        if (count > 1) {
            LOGGER.error("More than one instance of {}", path);
        }
        return count > 0;
    }

    public String objectToJson(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return getFormatter().writeValueAsString(object);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not serialise object.", ex);
        }
    }

    public ObjectMapper getFormatter() {
        if (formatter == null) {
            formatter = EntityParser.getSimpleObjectMapper();
        }
        return formatter;
    }

}
