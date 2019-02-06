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
package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.factories;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import de.fraunhofer.iosb.ilt.sta.persistence.IdManager;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.DataSize;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.IdGenerationHandler;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.QueryBuilder;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.Utils;
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.Utils.getFieldOrNull;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableMultiDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableThings;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableThingsLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.StaTable;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import static de.fraunhofer.iosb.ilt.sta.settings.CoreSettings.UTC;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.sta.util.NoSuchEntityException;
import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
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
import org.jooq.Cursor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Record3;
import org.jooq.Result;
import org.jooq.SelectConditionStep;
import org.jooq.SelectOnConditionStep;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author scf
 * @param <J> The type of the ID fields.
 */
public class EntityFactories<J> {

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
    private static final Field nullField = DSL.field("null", Object.class);

    public final IdManager<J> idManager;
    public final TableCollection<J> tableCollection;

    public final ActuatorFactory<J> actuatorFactory;
    public final DatastreamFactory<J> datastreamFactory;
    public final MultiDatastreamFactory<J> multiDatastreamFactory;
    public final TaskFactory<J> taskFactory;
    public final TaskingCapabilityFactory<J> taskingCapabilityFactory;
    public final ThingFactory<J> thingFactory;
    public final FeatureOfInterestFactory<J> featureOfInterestFactory;
    public final HistoricalLocationFactory<J> historicalLocationFactory;
    public final LocationFactory<J> locationFactory;
    public final SensorFactory<J> sensorFactory;
    public final ObservationFactory<J> observationFactory;
    public final ObservedPropertyFactory<J> observedPropertyFactory;

    private final Map<EntityType, EntityFactory<? extends Entity, J>> factoryPerEntity = new EnumMap<>(EntityType.class);

    public EntityFactories(IdManager<J> idManager, TableCollection<J> tableCollection) {
        this.idManager = idManager;
        this.tableCollection = tableCollection;

        String defaultPrefix = QueryBuilder.ALIAS_PREFIX + "1";

        actuatorFactory = new ActuatorFactory<>(this, tableCollection.tableActuators.as(defaultPrefix));
        datastreamFactory = new DatastreamFactory<>(this, tableCollection.tableDatastreams.as(defaultPrefix));
        featureOfInterestFactory = new FeatureOfInterestFactory<>(this, tableCollection.tableFeatures.as(defaultPrefix));
        historicalLocationFactory = new HistoricalLocationFactory<>(this, tableCollection.tableHistLocations.as(defaultPrefix));
        locationFactory = new LocationFactory<>(this, tableCollection.tableLocations.as(defaultPrefix));
        multiDatastreamFactory = new MultiDatastreamFactory<>(this, tableCollection.tableMultiDatastreams.as(defaultPrefix));
        observationFactory = new ObservationFactory<>(this, tableCollection.tableObservations.as(defaultPrefix));
        observedPropertyFactory = new ObservedPropertyFactory<>(this, tableCollection.tableObsProperties.as(defaultPrefix));
        sensorFactory = new SensorFactory<>(this, tableCollection.tableSensors.as(defaultPrefix));
        taskFactory = new TaskFactory<>(this, tableCollection.tableTasks.as(defaultPrefix));
        taskingCapabilityFactory = new TaskingCapabilityFactory<>(this, tableCollection.tableTaskingCapabilities.as(defaultPrefix));
        thingFactory = new ThingFactory<>(this, tableCollection.tableThings.as(defaultPrefix));

        factoryPerEntity.put(EntityType.ACTUATOR, actuatorFactory);
        factoryPerEntity.put(EntityType.DATASTREAM, datastreamFactory);
        factoryPerEntity.put(EntityType.FEATUREOFINTEREST, featureOfInterestFactory);
        factoryPerEntity.put(EntityType.HISTORICALLOCATION, historicalLocationFactory);
        factoryPerEntity.put(EntityType.LOCATION, locationFactory);
        factoryPerEntity.put(EntityType.MULTIDATASTREAM, multiDatastreamFactory);
        factoryPerEntity.put(EntityType.OBSERVATION, observationFactory);
        factoryPerEntity.put(EntityType.OBSERVEDPROPERTY, observedPropertyFactory);
        factoryPerEntity.put(EntityType.SENSOR, sensorFactory);
        factoryPerEntity.put(EntityType.TASK, taskFactory);
        factoryPerEntity.put(EntityType.TASKINGCAPABILITY, taskingCapabilityFactory);
        factoryPerEntity.put(EntityType.THING, thingFactory);
    }

    public TableCollection<J> getTableCollection() {
        return tableCollection;
    }

    public <T extends Entity<T>> EntitySet<T> createSetFromRecords(EntityFactory<T, J> factory, Cursor<Record> tuples, Query query, long maxDataSize) {
        EntitySet<T> entitySet = new EntitySetImpl<>(factory.getEntityType());
        int count = 0;
        DataSize size = new DataSize();
        int top = query.getTopOrDefault();
        while (tuples.hasNext()) {
            Record tuple = tuples.fetchNext();
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
    public <T extends Entity<T>> EntityFactory<T, J> getFactoryFor(EntityType type) {
        EntityFactory<? extends Entity, J> factory = factoryPerEntity.get(type);
        if (factory == null) {
            throw new AssertionError("No factory found for " + type);
        }
        return (EntityFactory<T, J>) factory;
    }

    public Id idFromObject(J id) {
        return idManager.fromObject(id);
    }

    public Actuator actuatorFromId(Record tuple, Field<J> path) {
        return actuatorFromId(getFieldOrNull(tuple, path));
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

    public Datastream datastreamFromId(Record tuple, Field<J> path) {
        return datastreamFromId(getFieldOrNull(tuple, path));
    }

    public Datastream datastreamFromId(J id) {
        if (id == null) {
            return null;
        }
        Datastream ds = new Datastream(true, idManager.fromObject(id));
        ds.setExportObject(false);
        return ds;
    }

    public MultiDatastream multiDatastreamFromId(Record tuple, Field<J> path) {
        return multiDatastreamFromId(getFieldOrNull(tuple, path));
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

    public FeatureOfInterest featureOfInterestFromId(Record tuple, Field<J> path) {
        return featureOfInterestFromId(getFieldOrNull(tuple, path));
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

    public ObservedProperty observedProperyFromId(Record tuple, Field<J> path) {
        return observedProperyFromId(getFieldOrNull(tuple, path));
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

    public Sensor sensorFromId(Record tuple, Field<J> path) {
        return sensorFromId(getFieldOrNull(tuple, path));
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

    public Task taskFromId(Record tuple, Field<J> path) {
        return taskFromId(getFieldOrNull(tuple, path));
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

    public TaskingCapability taskingCapabilityFromId(Record tuple, Field<J> path) {
        return taskingCapabilityFromId(getFieldOrNull(tuple, path));
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

    public Thing thingFromId(Record tuple, Field<J> path) {
        return thingFromId(getFieldOrNull(tuple, path));
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

    public FeatureOfInterest generateFeatureOfInterest(PostgresPersistenceManager<J> pm, Id datastreamId, boolean isMultiDatastream) throws NoSuchEntityException, IncompleteEntityException {
        J dsId = (J) datastreamId.getValue();
        DSLContext dslContext = pm.createDdslContext();
        AbstractTableLocations<J> ql = tableCollection.tableLocations;
        AbstractTableThingsLocations<J> qtl = tableCollection.tableThingsLocations;
        AbstractTableThings<J> qt = tableCollection.tableThings;
        AbstractTableDatastreams<J> qd = tableCollection.tableDatastreams;
        AbstractTableMultiDatastreams<J> qmd = tableCollection.tableMultiDatastreams;

        SelectOnConditionStep<Record3<J, J, String>> query = dslContext.select(ql.getId(), ql.getGenFoiId(), ql.encodingType)
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
        Result<Record3<J, J, String>> tuples = query.fetch();
        if (tuples.isEmpty()) {
            // Can not generate foi from Thing with no locations.
            throw new NoSuchEntityException("Can not generate foi for Thing with no locations.");
        }
        // See if any of the locations have a generated foi.
        // Also track if any of the location has a supported encoding type.
        J genFoiId = null;
        J locationId = null;
        for (Record tuple : tuples) {
            genFoiId = getFieldOrNull(tuple, ql.getGenFoiId());
            if (genFoiId != null) {
                break;
            }
            String encodingType = getFieldOrNull(tuple, ql.encodingType);
            if (encodingType != null && GeoJsonDeserializier.ENCODINGS.contains(encodingType.toLowerCase())) {
                locationId = getFieldOrNull(tuple, ql.getId());
            }
        }
        // Either genFoiId will have a value, if a generated foi was found,
        // Or locationId will have a value if a supported encoding type was found.

        FeatureOfInterest foi;
        if (genFoiId != null) {
            foi = new FeatureOfInterest();
            foi.setId(idFromObject(genFoiId));
        } else if (locationId != null) {
            SelectConditionStep<Record3<J, String, String>> query2 = dslContext.select(ql.getId(), ql.encodingType, ql.location)
                    .from(ql)
                    .where(ql.getId().eq(locationId));
            Record tuple = query2.fetchOne();
            if (tuple == null) {
                // Can not generate foi from Thing with no locations.
                // Should not happen, since the query succeeded just before.
                throw new NoSuchEntityException("Can not generate foi for Thing with no locations.");
            }
            String encoding = getFieldOrNull(tuple, ql.encodingType);
            String locString = getFieldOrNull(tuple, ql.location);
            Object locObject = Utils.locationFromEncoding(encoding, locString);
            foi = new FeatureOfInterestBuilder()
                    .setName("FoI for location " + locationId)
                    .setDescription("Generated from location " + locationId)
                    .setEncodingType(encoding)
                    .setFeature(locObject)
                    .build();
            featureOfInterestFactory.insert(pm, foi);
            J foiId = (J) foi.getId().getValue();
            dslContext.update(ql)
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

    public void insertUserDefinedId(PostgresPersistenceManager<J> pm, Map<Field, Object> clause, Field<J> idField, Entity entity) throws IncompleteEntityException {
        IdGenerationHandler idhandler = pm.createIdGenerationHanlder(entity);
        if (idhandler.useClientSuppliedId()) {
            idhandler.modifyClientSuppliedId();
            clause.put(idField, (J) idhandler.getIdValue());
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
    public void entityExistsOrCreate(PostgresPersistenceManager<J> pm, Entity e) throws NoSuchEntityException, IncompleteEntityException {
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

    public boolean entityExists(PostgresPersistenceManager<J> pm, EntityType type, Id entityId) {
        J id = (J) entityId.getValue();
        StaTable<J> table = tableCollection.tablesByType.get(type);

        DSLContext dslContext = pm.createDdslContext();

        Integer count = dslContext.selectCount()
                .from(table)
                .where(table.getId().equal(id))
                .fetchOne()
                .component1();

        if (count > 1) {
            LOGGER.error("More than one instance of {} with id {}.", type, id);
        }
        return count > 0;

    }

    public boolean entityExists(PostgresPersistenceManager<J> pm, Entity e) {
        if (e == null || e.getId() == null) {
            return false;
        }
        return entityExists(pm, e.getEntityType(), e.getId());
    }

    public static void insertTimeValue(Map<Field, Object> clause, Field<OffsetDateTime> startPath, Field<OffsetDateTime> endPath, TimeValue time) {
        if (time instanceof TimeInstant) {
            TimeInstant timeInstant = (TimeInstant) time;
            insertTimeInstant(clause, endPath, timeInstant);
            insertTimeInstant(clause, startPath, timeInstant);
        } else if (time instanceof TimeInterval) {
            TimeInterval timeInterval = (TimeInterval) time;
            insertTimeInterval(clause, startPath, endPath, timeInterval);
        }
    }

    public static void insertTimeInstant(Map<Field, Object> clause, Field<OffsetDateTime> path, TimeInstant time) {
        if (time == null) {
            return;
        }
        clause.put(path, time.getOffsetDateTime());
    }

    public static void insertTimeInterval(Map<Field, Object> clause, Field<OffsetDateTime> startPath, Field<OffsetDateTime> endPath, TimeInterval time) {
        if (time == null) {
            return;
        }
        Interval interval = time.getInterval();
        clause.put(startPath, OffsetDateTime.ofInstant(Instant.ofEpochMilli(interval.getStartMillis()), UTC));
        clause.put(endPath, OffsetDateTime.ofInstant(Instant.ofEpochMilli(interval.getEndMillis()), UTC));
    }

    /**
     * Sets both the geometry and location in the clause.
     *
     * @param clause The insert or update clause to add to.
     * @param locationPath The path to the location column.
     * @param geomPath The path to the geometry column.
     * @param encodingType The encoding type.
     * @param location The location.
     */
    public static void insertGeometry(Map<Field, Object> clause, Field<String> locationPath, Field<? extends Object> geomPath, String encodingType, final Object location) {
        if (encodingType != null && GeoJsonDeserializier.ENCODINGS.contains(encodingType.toLowerCase())) {
            insertGeometryKnownEncoding(location, clause, geomPath, locationPath);
        } else {
            String json;
            json = objectToJson(location);
            clause.put(geomPath, nullField);
            clause.put(locationPath, json);
        }
    }

    private static void insertGeometryKnownEncoding(final Object location, Map<Field, Object> clause, Field<? extends Object> geomPath, Field<String> locationPath) {
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
        final String template = "ST_Force2D(ST_Transform(ST_GeomFromGeoJSON({0}), 4326))";
        clause.put(geomPath, DSL.field(template, Object.class, geoJson));
        clause.put(locationPath, locJson);
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
