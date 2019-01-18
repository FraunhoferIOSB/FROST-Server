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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysema.commons.lang.CloseableIterator;
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
import de.fraunhofer.iosb.ilt.sta.json.deserialize.EntityParser;
import de.fraunhofer.iosb.ilt.sta.json.deserialize.custom.GeoJsonDeserializier;
import de.fraunhofer.iosb.ilt.sta.json.serialize.GeoJsonSerializer;
import de.fraunhofer.iosb.ilt.sta.model.Actuator;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.FeatureOfInterest;
import de.fraunhofer.iosb.ilt.sta.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Task;
import de.fraunhofer.iosb.ilt.sta.model.TaskingCapability;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.builder.FeatureOfInterestBuilder;
import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.sta.model.core.Id;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePath;
import de.fraunhofer.iosb.ilt.sta.persistence.IdManager;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.factories.ActuatorFactory;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.factories.DatastreamFactory;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.factories.EntityFactory;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.factories.FeatureOfInterestFactory;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.factories.HistoricalLocationFactory;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.factories.LocationFactory;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.factories.MultiDatastreamFactory;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.factories.ObservationFactory;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.factories.ObservedPropertyFactory;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.factories.SensorFactory;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.factories.TaskFactory;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.factories.TaskingCapabilityFactory;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.factories.ThingFactory;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQActuators;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQFeatures;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQMultiDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQObsProperties;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQObservations;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQSensors;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQTaskingCapabilities;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQTasks;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQThings;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQThingsLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.QCollection;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.sta.util.NoSuchEntityException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.geojson.Crs;
import org.geojson.Feature;
import org.geojson.GeoJsonObject;
import org.geojson.jackson.CrsType;
import org.geolatte.common.dataformats.json.jackson.JsonException;
import org.geolatte.geom.Geometry;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author scf
 * @param <I> The type of path used for the ID fields.
 * @param <J> The type of the ID fields.
 */
public class EntityFactories<I extends SimpleExpression<J> & Path<J>, J> {

    public static final TypeReference<List<String>> TYPE_LIST_STRING = new TypeReference<List<String>>() {
        // Empty on purpose.
    };
    public static final TypeReference<List<UnitOfMeasurement>> TYPE_LIST_UOM = new TypeReference<List<UnitOfMeasurement>>() {
        // Empty on purpose.
    };
    public static final String CAN_NOT_BE_NULL = " can not be null.";
    public static final String CHANGED_MULTIPLE_ROWS = "Update changed multiple rows.";
    public static final String NO_ID_OR_NOT_FOUND = " with no id or non existing.";
    public static final String CREATED_HL = "Created historicalLocation {}";
    public static final String LINKED_L_TO_HL = "Linked location {} to historicalLocation {}.";
    public static final String UNLINKED_L_FROM_T = "Unlinked {} locations from Thing {}.";
    public static final String LINKED_L_TO_T = "Linked Location {} to Thing {}.";

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityFactories.class);
    private static ObjectMapper formatter;

    public final IdManager<J> idManager;
    public final QCollection<I, J> qCollection;

    public final ActuatorFactory<I, J> actuatorFactory;
    public final DatastreamFactory<I, J> datastreamFactory;
    public final MultiDatastreamFactory<I, J> multiDatastreamFactory;
    public final TaskFactory<I, J> taskFactory;
    public final TaskingCapabilityFactory<I, J> taskingCapabilityFactory;
    public final ThingFactory<I, J> thingFactory;
    public final FeatureOfInterestFactory<I, J> featureOfInterestFactory;
    public final HistoricalLocationFactory<I, J> historicalLocationFactory;
    public final LocationFactory<I, J> locationFactory;
    public final SensorFactory<I, J> sensorFactory;
    public final ObservationFactory<I, J> observationFactory;
    public final ObservedPropertyFactory<I, J> observedPropertyFactory;

    private final Map<EntityType, EntityFactory<? extends Entity, I, J>> factoryPerEntity = new EnumMap<>(EntityType.class);

    public EntityFactories(IdManager<J> idManager, QCollection<I, J> qCollection) {
        this.idManager = idManager;
        this.qCollection = qCollection;

        String defaultPrefix = PathSqlBuilderImp.ALIAS_PREFIX + "1";

        actuatorFactory = new ActuatorFactory<>(this, qCollection.qActuators.newWithAlias(defaultPrefix));
        datastreamFactory = new DatastreamFactory<>(this, qCollection.qDatastreams.newWithAlias(defaultPrefix));
        multiDatastreamFactory = new MultiDatastreamFactory<>(this, qCollection.qMultiDatastreams.newWithAlias(defaultPrefix));
        taskFactory = new TaskFactory<>(this, qCollection.qTasks.newWithAlias(defaultPrefix));
        taskingCapabilityFactory = new TaskingCapabilityFactory<>(this, qCollection.qTaskingCapabilities.newWithAlias(defaultPrefix));
        thingFactory = new ThingFactory<>(this, qCollection.qThings.newWithAlias(defaultPrefix));
        featureOfInterestFactory = new FeatureOfInterestFactory<>(this, qCollection.qFeatures.newWithAlias(defaultPrefix));
        historicalLocationFactory = new HistoricalLocationFactory<>(this, qCollection.qHistLocations.newWithAlias(defaultPrefix));
        locationFactory = new LocationFactory<>(this, qCollection.qLocations.newWithAlias(defaultPrefix));
        sensorFactory = new SensorFactory<>(this, qCollection.qSensors.newWithAlias(defaultPrefix));
        observationFactory = new ObservationFactory<>(this, qCollection.qObservations.newWithAlias(defaultPrefix));
        observedPropertyFactory = new ObservedPropertyFactory<>(this, qCollection.qObsProperties.newWithAlias(defaultPrefix));

        factoryPerEntity.put(EntityType.ACTUATOR, actuatorFactory);
        factoryPerEntity.put(EntityType.DATASTREAM, datastreamFactory);
        factoryPerEntity.put(EntityType.MULTIDATASTREAM, multiDatastreamFactory);
        factoryPerEntity.put(EntityType.TASK, taskFactory);
        factoryPerEntity.put(EntityType.TASKINGCAPABILITY, taskingCapabilityFactory);
        factoryPerEntity.put(EntityType.THING, thingFactory);
        factoryPerEntity.put(EntityType.FEATUREOFINTEREST, featureOfInterestFactory);
        factoryPerEntity.put(EntityType.HISTORICALLOCATION, historicalLocationFactory);
        factoryPerEntity.put(EntityType.LOCATION, locationFactory);
        factoryPerEntity.put(EntityType.SENSOR, sensorFactory);
        factoryPerEntity.put(EntityType.OBSERVATION, observationFactory);
        factoryPerEntity.put(EntityType.OBSERVEDPROPERTY, observedPropertyFactory);
    }

    public QCollection<I, J> getQCollection() {
        return qCollection;
    }

    public <T extends Entity<T>> EntitySet<T> createSetFromTuples(EntityFactory<T, I, J> factory, CloseableIterator<Tuple> tuples, Query query, long maxDataSize) {
        EntitySet<T> entitySet = new EntitySetImpl<>(factory.getEntityType());
        int count = 0;
        DataSize size = new DataSize();
        int top = query.getTopOrDefault();
        while (tuples.hasNext()) {
            Tuple tuple = tuples.next();
            entitySet.add(factory.create(tuple, query, size));
            count++;
            if (count >= top) {
                return entitySet;
            }
            if (size.getDataSize() > maxDataSize) {
                LOGGER.debug("Size limit reached: {} > {}.", size.getDataSize(), maxDataSize);
                return entitySet;
            }
        }
        return entitySet;
    }

    /**
     * Get the factory for the given entity class, using the default alias
     * PathSqlBuilderLong.ALIAS_PREFIX + "1".
     *
     * @param <T> The type of entity to get the factory for.
     * @param type The type of the entity to get the factory for.
     * @return the factory for the given entity class.
     */
    public <T extends Entity<T>> EntityFactory<T, I, J> getFactoryFor(EntityType type) {
        EntityFactory<? extends Entity, I, J> factory = factoryPerEntity.get(type);
        if (factory == null) {
            throw new AssertionError("No factory found for " + type);
        }
        return (EntityFactory<T, I, J>) factory;
    }

    public J getIdFromTuple(Tuple t, I path) {
        return t.get(path);
    }

    public Id idFromObject(J id) {
        return idManager.fromObject(id);
    }

    public Actuator actuatorFromId(Tuple tuple, I path) {
        return actuatorFromId(getIdFromTuple(tuple, path));
    }

    public Actuator actuatorFromId(J id) {
        if (id == null) {
            return null;
        }
        Actuator a = new Actuator();
        a.setId(idManager.fromObject(id));
        a.setExportObject(false);
        return a;
    }

    public Datastream datastreamFromId(Tuple tuple, I path) {
        return datastreamFromId(getIdFromTuple(tuple, path));
    }

    public Datastream datastreamFromId(J id) {
        if (id == null) {
            return null;
        }
        Datastream ds = new Datastream(true, idManager.fromObject(id));
        ds.setExportObject(false);
        return ds;
    }

    public MultiDatastream multiDatastreamFromId(Tuple tuple, I path) {
        return multiDatastreamFromId(getIdFromTuple(tuple, path));
    }

    public MultiDatastream multiDatastreamFromId(J id) {
        if (id == null) {
            return null;
        }
        MultiDatastream ds = new MultiDatastream();
        ds.setId(idManager.fromObject(id));
        ds.setExportObject(false);
        return ds;
    }

    public FeatureOfInterest featureOfInterestFromId(Tuple tuple, I path) {
        return featureOfInterestFromId(getIdFromTuple(tuple, path));
    }

    public FeatureOfInterest featureOfInterestFromId(J id) {
        if (id == null) {
            return null;
        }
        FeatureOfInterest foi = new FeatureOfInterest();
        foi.setId(idManager.fromObject(id));
        foi.setExportObject(false);
        return foi;
    }

    public ObservedProperty observedProperyFromId(Tuple tuple, I path) {
        return observedProperyFromId(getIdFromTuple(tuple, path));
    }

    public ObservedProperty observedProperyFromId(J id) {
        if (id == null) {
            return null;
        }
        ObservedProperty op = new ObservedProperty();
        op.setId(idManager.fromObject(id));
        op.setExportObject(false);
        return op;
    }

    public Sensor sensorFromId(Tuple tuple, I path) {
        return sensorFromId(getIdFromTuple(tuple, path));
    }

    public Sensor sensorFromId(J id) {
        if (id == null) {
            return null;
        }
        Sensor sensor = new Sensor();
        sensor.setId(idManager.fromObject(id));
        sensor.setExportObject(false);
        return sensor;
    }

    public Task taskFromId(Tuple tuple, I path) {
        return taskFromId(getIdFromTuple(tuple, path));
    }

    public Task taskFromId(J id) {
        if (id == null) {
            return null;
        }
        Task task = new Task();
        task.setId(idManager.fromObject(id));
        task.setExportObject(false);
        return task;
    }

    public TaskingCapability taskingCapabilityFromId(Tuple tuple, I path) {
        return taskingCapabilityFromId(getIdFromTuple(tuple, path));
    }

    public TaskingCapability taskingCapabilityFromId(J id) {
        if (id == null) {
            return null;
        }
        TaskingCapability taskingCapability = new TaskingCapability();
        taskingCapability.setId(idManager.fromObject(id));
        taskingCapability.setExportObject(false);
        return taskingCapability;
    }

    public Thing thingFromId(Tuple tuple, I path) {
        return thingFromId(getIdFromTuple(tuple, path));
    }

    public Thing thingFromId(J id) {
        if (id == null) {
            return null;
        }
        Thing thing = new Thing();
        thing.setId(idManager.fromObject(id));
        thing.setExportObject(false);
        return thing;
    }

    public FeatureOfInterest generateFeatureOfInterest(PostgresPersistenceManager<I, J> pm, Id datastreamId, boolean isMultiDatastream) throws NoSuchEntityException, IncompleteEntityException {
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
            foi.setId(idFromObject(genFoiId));
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
            featureOfInterestFactory.insert(pm, foi);
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

    public <T extends StoreClause> void insertUserDefinedId(PostgresPersistenceManager<I, J> pm, T clause, Path idPath, Entity entity) throws IncompleteEntityException {
        IdGenerationHandler idhandler = pm.createIdGenerationHanlder(entity);
        if (idhandler.useClientSuppliedId()) {
            idhandler.modifyClientSuppliedId();
            clause.set(idPath, (J) idhandler.getIdValue());
        }
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
    public void entityExistsOrCreate(PostgresPersistenceManager<I, J> pm, Entity e) throws NoSuchEntityException, IncompleteEntityException {
        if (e == null) {
            throw new NoSuchEntityException("No entity!");
        }

        if (e.getId() == null) {
            e.complete();
            // no id but complete -> create
            pm.insert(e);
            return;
        }

        if (entityExists(pm, e)) {
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

    public boolean entityExists(PostgresPersistenceManager<I, J> pm, Entity e) {
        if (e == null || e.getId() == null) {
            return false;
        }
        J id = (J) e.getId().getValue();
        SQLQueryFactory qFactory = pm.createQueryFactory();
        long count = 0;
        switch (e.getEntityType()) {
            case ACTUATOR:
                AbstractQActuators<? extends AbstractQActuators, I, J> a = qCollection.qActuators;
                count = qFactory.select()
                        .from(a)
                        .where(a.getId().eq(id))
                        .fetchCount();
                break;

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

            case TASK:
                AbstractQTasks<? extends AbstractQTasks, I, J> tsk = qCollection.qTasks;
                count = qFactory.select()
                        .from(tsk)
                        .where(tsk.getId().eq(id))
                        .fetchCount();
                break;

            case TASKINGCAPABILITY:
                AbstractQTaskingCapabilities<? extends AbstractQTaskingCapabilities, I, J> tcp = qCollection.qTaskingCapabilities;
                count = qFactory.select()
                        .from(tcp)
                        .where(tcp.getId().eq(id))
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

    public boolean entityExists(PostgresPersistenceManager<I, J> pm, ResourcePath path) {
        long count = pm.count(path, null);
        if (count > 1) {
            LOGGER.error("More than one instance of {}", path);
        }
        return count > 0;
    }

    public static <T extends StoreClause> T insertTimeValue(T clause, DateTimePath<Timestamp> startPath, DateTimePath<Timestamp> endPath, TimeValue time) {
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

    public static <T extends StoreClause> T insertTimeInstant(T clause, DateTimePath<Timestamp> path, TimeInstant time) {
        if (time == null) {
            return clause;
        }
        clause.set(path, new Timestamp(time.getDateTime().getMillis()));
        return clause;
    }

    public static <T extends StoreClause> T insertTimeInterval(T clause, DateTimePath<Timestamp> startPath, DateTimePath<Timestamp> endPath, TimeInterval time) {
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
    public static <T extends StoreClause> T insertGeometry(T clause, StringPath locationPath, GeometryPath<Geometry> geomPath, String encodingType, final Object location) {
        if (encodingType != null && GeoJsonDeserializier.ENCODINGS.contains(encodingType.toLowerCase())) {
            insertGeometryKnownEncoding(location, clause, geomPath, locationPath);
        } else {
            String json;
            json = objectToJson(location);
            clause.setNull(geomPath);
            clause.set(locationPath, json);
        }
        return clause;
    }

    private static <T extends StoreClause> void insertGeometryKnownEncoding(final Object location, T clause, GeometryPath<Geometry> geomPath, StringPath locationPath) {
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
            Utils.getGeoJsonMapper().fromJson(geoJson, Geometry.class);
        } catch (JsonException ex) {
            throw new IllegalArgumentException("Invalid geoJson: " + ex.getMessage());
        }
        clause.set(geomPath, Expressions.template(Geometry.class, "ST_Force2D(ST_Transform(ST_GeomFromGeoJSON({0}), 4326))", geoJson));
        clause.set(locationPath, locJson);
    }

    public static Object reParseGeometry(String encodingType, Object object) {
        String json = objectToJson(object);
        return Utils.locationFromEncoding(encodingType, json);
    }

    public static String objectToJson(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return getFormatter().writeValueAsString(object);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not serialise object.", ex);
        }
    }

    public static ObjectMapper getFormatter() {
        if (formatter == null) {
            formatter = EntityParser.getSimpleObjectMapper();
        }
        return formatter;
    }

}
