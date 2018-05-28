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
 * aString with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.sta.persistence.postgres.uuidid;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.mysema.commons.lang.CloseableIterator;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.ComparablePath;
import de.fraunhofer.iosb.ilt.sta.json.deserialize.EntityParser;
import de.fraunhofer.iosb.ilt.sta.json.deserialize.custom.GeoJsonDeserializier;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.FeatureOfInterest;
import de.fraunhofer.iosb.ilt.sta.model.HistoricalLocation;
import de.fraunhofer.iosb.ilt.sta.model.Location;
import de.fraunhofer.iosb.ilt.sta.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.Property;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.DataSize;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.ResultType;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.util.GeoHelper;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.geojson.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author scf
 * @author selimnairb
 */
public class PropertyHelper {

    public static interface entityFromTupleFactory<T extends Entity> {

        /**
         * Creates a T, reading the Tuple with a qObject using no alias.
         *
         * @param tuple The tuple to create the Entity from.
         * @param query The query used to request the data.
         * @param dataSize The counter for the data size. This counts only the
         * variable-sided elements, such as Observation.result and
         * Thing.properties.
         * @return The Entity created from the Tuple.
         */
        public T create(Tuple tuple, Query query, DataSize dataSize);

        /**
         * Get the primary key of the table of the entity this factory
         *
         * @return The primary key of the table of the entity this factory
         * creates, using no alias.
         */
        public ComparablePath<UUID> getPrimaryKey();

        /**
         * Get the EntityType of the Entities created by this factory.
         *
         * @return The EntityType of the Entities created by this factory.
         */
        public EntityType getEntityType();

    }

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyHelper.class);
    private static final TypeReference<List<String>> TYPE_LIST_STRING = new TypeReference<List<String>>() {
        // Empty on purpose.
    };
    private static final TypeReference<List<UnitOfMeasurement>> TYPE_LIST_UOM = new TypeReference<List<UnitOfMeasurement>>() {
        // Empty on purpose.
    };
    private static final Map<Class<? extends Entity>, entityFromTupleFactory<? extends Entity>> FACTORY_PER_ENTITY = new HashMap<>();

    public static Expression<?>[] getExpressions(Path<?> qPath, Set<EntityProperty> selectedProperties) {
        List<Expression<?>> exprList = new ArrayList<>();
        if (selectedProperties.isEmpty()) {
            PropertyResolver.expressionsForClass(qPath, exprList);
        } else {
            for (EntityProperty property : selectedProperties) {
                PropertyResolver.expressionsForProperty(property, qPath, exprList);
            }
        }
        return exprList.toArray(new Expression<?>[exprList.size()]);
    }

    public static <T extends Entity> EntitySet<T> createSetFromTuples(entityFromTupleFactory<T> factory, CloseableIterator<Tuple> tuples, Query query, long maxDataSize) {
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

    public static class DatastreamFactory implements PropertyHelper.entityFromTupleFactory<Datastream> {

        public static final DatastreamFactory withDefaultAlias = new DatastreamFactory(new QDatastreams(PathSqlBuilderUuid.ALIAS_PREFIX + "1"));
        private final QDatastreams qInstance;

        public DatastreamFactory(QDatastreams qInstance) {
            this.qInstance = qInstance;
        }

        @Override
        public Datastream create(Tuple tuple, Query query, DataSize dataSize) {
            Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();

            Datastream entity = new Datastream();
            entity.setName(tuple.get(qInstance.name));
            entity.setDescription(tuple.get(qInstance.description));
            UUID id = tuple.get(qInstance.id);
            if (id != null) {
                entity.setId(new UuidId(tuple.get(qInstance.id)));
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
            ObservedProperty op = observedProperyFromId(tuple.get(qInstance.obsPropertyId));
            entity.setObservedProperty(op);

            Timestamp pTimeStart = tuple.get(qInstance.phenomenonTimeStart);
            Timestamp pTimeEnd = tuple.get(qInstance.phenomenonTimeEnd);
            if (pTimeStart != null && pTimeEnd != null) {
                entity.setPhenomenonTime(intervalFromTimes(pTimeStart, pTimeEnd));
            }

            Timestamp rTimeStart = tuple.get(qInstance.resultTimeStart);
            Timestamp rTimeEnd = tuple.get(qInstance.resultTimeEnd);
            if (rTimeStart != null && rTimeEnd != null) {
                entity.setResultTime(intervalFromTimes(rTimeStart, rTimeEnd));
            }

            if (select.isEmpty() || select.contains(EntityProperty.Properties)) {
                String props = tuple.get(qInstance.properties);
                entity.setProperties(jsonToObject(props, Map.class));
            }

            entity.setSensor(sensorFromId(tuple.get(qInstance.sensorId)));
            entity.setThing(thingFromId(tuple.get(qInstance.thingId)));

            entity.setUnitOfMeasurement(new UnitOfMeasurement(tuple.get(qInstance.unitName), tuple.get(qInstance.unitSymbol), tuple.get(qInstance.unitDefinition)));
            return entity;
        }

        @Override
        public ComparablePath<UUID> getPrimaryKey() {
            return qInstance.id;
        }

        @Override
        public EntityType getEntityType() {
            return EntityType.Datastream;
        }

    }

    public static class MultiDatastreamFactory implements PropertyHelper.entityFromTupleFactory<MultiDatastream> {

        public static final MultiDatastreamFactory withDefaultAlias = new MultiDatastreamFactory(new QMultiDatastreams(PathSqlBuilderUuid.ALIAS_PREFIX + "1"));
        private final QMultiDatastreams qInstance;

        public MultiDatastreamFactory(QMultiDatastreams qInstance) {
            this.qInstance = qInstance;
        }

        @Override
        public MultiDatastream create(Tuple tuple, Query query, DataSize dataSize) {
            Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();

            MultiDatastream entity = new MultiDatastream();
            entity.setName(tuple.get(qInstance.name));
            entity.setDescription(tuple.get(qInstance.description));
            UUID id = tuple.get(qInstance.id);
            if (id != null) {
                entity.setId(new UuidId(tuple.get(qInstance.id)));
            }

            List<String> observationTypes = jsonToObject(tuple.get(qInstance.observationTypes), TYPE_LIST_STRING);
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
                entity.setPhenomenonTime(intervalFromTimes(pTimeStart, pTimeEnd));
            }

            Timestamp rTimeStart = tuple.get(qInstance.resultTimeStart);
            Timestamp rTimeEnd = tuple.get(qInstance.resultTimeEnd);
            if (rTimeStart != null && rTimeEnd != null) {
                entity.setResultTime(intervalFromTimes(rTimeStart, rTimeEnd));
            }

            if (select.isEmpty() || select.contains(EntityProperty.Properties)) {
                String props = tuple.get(qInstance.properties);
                entity.setProperties(jsonToObject(props, Map.class));
            }

            entity.setSensor(sensorFromId(tuple.get(qInstance.sensorId)));
            entity.setThing(thingFromId(tuple.get(qInstance.thingId)));

            List<UnitOfMeasurement> units = jsonToObject(tuple.get(qInstance.unitOfMeasurements), TYPE_LIST_UOM);
            entity.setUnitOfMeasurements(units);
            return entity;
        }

        @Override
        public ComparablePath<UUID> getPrimaryKey() {
            return qInstance.id;
        }

        @Override
        public EntityType getEntityType() {
            return EntityType.MultiDatastream;
        }

    }

    public static class ThingFactory implements PropertyHelper.entityFromTupleFactory<Thing> {

        public static final ThingFactory withDefaultAlias = new ThingFactory(new QThings(PathSqlBuilderUuid.ALIAS_PREFIX + "1"));
        private final QThings qInstance;

        public ThingFactory(QThings qInstance) {
            this.qInstance = qInstance;
        }

        @Override
        public Thing create(Tuple tuple, Query query, DataSize dataSize) {
            Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();

            Thing entity = new Thing();
            entity.setName(tuple.get(qInstance.name));
            entity.setDescription(tuple.get(qInstance.description));

            UUID id = tuple.get(qInstance.id);
            if (id != null) {
                entity.setId(new UuidId(tuple.get(qInstance.id)));
            }

            if (select.isEmpty() || select.contains(EntityProperty.Properties)) {
                String props = tuple.get(qInstance.properties);
                dataSize.increase(props == null ? 0 : props.length());
                entity.setProperties(jsonToObject(props, Map.class));
            }

            return entity;
        }

        @Override
        public ComparablePath<UUID> getPrimaryKey() {
            return qInstance.id;
        }

        @Override
        public EntityType getEntityType() {
            return EntityType.Thing;
        }

    }

    public static class FeatureOfInterestFactory implements PropertyHelper.entityFromTupleFactory<FeatureOfInterest> {

        public static final FeatureOfInterestFactory withDefaultAlias = new FeatureOfInterestFactory(new QFeatures(PathSqlBuilderUuid.ALIAS_PREFIX + "1"));
        private final QFeatures qInstance;

        public FeatureOfInterestFactory(QFeatures qInstance) {
            this.qInstance = qInstance;
        }

        @Override
        public FeatureOfInterest create(Tuple tuple, Query query, DataSize dataSize) {
            Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();

            FeatureOfInterest entity = new FeatureOfInterest();
            UUID id = tuple.get(qInstance.id);
            if (id != null) {
                entity.setId(new UuidId(tuple.get(qInstance.id)));
            }

            entity.setName(tuple.get(qInstance.name));
            entity.setDescription(tuple.get(qInstance.description));
            String encodingType = tuple.get(qInstance.encodingType);
            entity.setEncodingType(encodingType);

            if (select.isEmpty() || select.contains(EntityProperty.Feature)) {
                String locationString = tuple.get(qInstance.feature);
                dataSize.increase(locationString == null ? 0 : locationString.length());
                entity.setFeature(locationFromEncoding(encodingType, locationString));
            }

            if (select.isEmpty() || select.contains(EntityProperty.Properties)) {
                String props = tuple.get(qInstance.properties);
                entity.setProperties(jsonToObject(props, Map.class));
            }

            return entity;
        }

        @Override
        public ComparablePath<UUID> getPrimaryKey() {
            return qInstance.id;
        }

        @Override
        public EntityType getEntityType() {
            return EntityType.FeatureOfInterest;
        }

    }

    public static class HistoricalLocationFactory implements PropertyHelper.entityFromTupleFactory<HistoricalLocation> {

        public static final HistoricalLocationFactory withDefaultAlias = new HistoricalLocationFactory(new QHistLocations(PathSqlBuilderUuid.ALIAS_PREFIX + "1"));
        private final QHistLocations qInstance;

        public HistoricalLocationFactory(QHistLocations qInstance) {
            this.qInstance = qInstance;
        }

        @Override
        public HistoricalLocation create(Tuple tuple, Query query, DataSize dataSize) {
            HistoricalLocation entity = new HistoricalLocation();
            UUID id = tuple.get(qInstance.id);
            if (id != null) {
                entity.setId(new UuidId(tuple.get(qInstance.id)));
            }

            entity.setThing(thingFromId(tuple.get(qInstance.thingId)));
            entity.setTime(instantFromTime(tuple.get(qInstance.time)));
            return entity;
        }

        @Override
        public ComparablePath<UUID> getPrimaryKey() {
            return qInstance.id;
        }

        @Override
        public EntityType getEntityType() {
            return EntityType.HistoricalLocation;
        }

    }

    public static class LocationFactory implements PropertyHelper.entityFromTupleFactory<Location> {

        public static final LocationFactory withDefaultAlias = new LocationFactory(new QLocations(PathSqlBuilderUuid.ALIAS_PREFIX + "1"));
        private final QLocations qInstance;

        public LocationFactory(QLocations qInstance) {
            this.qInstance = qInstance;
        }

        @Override
        public Location create(Tuple tuple, Query query, DataSize dataSize) {
            Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();
            Location entity = new Location();
            UUID id = tuple.get(qInstance.id);
            if (id != null) {
                entity.setId(new UuidId(tuple.get(qInstance.id)));
            }

            entity.setName(tuple.get(qInstance.name));
            entity.setDescription(tuple.get(qInstance.description));
            String encodingType = tuple.get(qInstance.encodingType);
            entity.setEncodingType(encodingType);

            if (select.isEmpty() || select.contains(EntityProperty.Location)) {
                String locationString = tuple.get(qInstance.location);
                dataSize.increase(locationString == null ? 0 : locationString.length());
                entity.setLocation(locationFromEncoding(encodingType, locationString));
            }

            if (select.isEmpty() || select.contains(EntityProperty.Properties)) {
                String props = tuple.get(qInstance.properties);
                entity.setProperties(jsonToObject(props, Map.class));
            }

            return entity;
        }

        @Override
        public ComparablePath<UUID> getPrimaryKey() {
            return qInstance.id;
        }

        @Override
        public EntityType getEntityType() {
            return EntityType.Location;
        }

    }

    public static class SensorFactory implements PropertyHelper.entityFromTupleFactory<Sensor> {

        public static final SensorFactory withDefaultAlias = new SensorFactory(new QSensors(PathSqlBuilderUuid.ALIAS_PREFIX + "1"));
        private final QSensors qInstance;

        public SensorFactory(QSensors qInstance) {
            this.qInstance = qInstance;
        }

        @Override
        public Sensor create(Tuple tuple, Query query, DataSize dataSize) {
            Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();

            Sensor entity = new Sensor();
            entity.setName(tuple.get(qInstance.name));
            entity.setDescription(tuple.get(qInstance.description));
            entity.setEncodingType(tuple.get(qInstance.encodingType));

            UUID id = tuple.get(qInstance.id);
            if (id != null) {
                entity.setId(new UuidId(tuple.get(qInstance.id)));
            }

            if (select.isEmpty() || select.contains(EntityProperty.Metadata)) {
                String metaDataString = tuple.get(qInstance.metadata);
                dataSize.increase(metaDataString == null ? 0 : metaDataString.length());
                entity.setMetadata(metaDataString);
            }

            if (select.isEmpty() || select.contains(EntityProperty.Properties)) {
                String props = tuple.get(qInstance.properties);
                entity.setProperties(jsonToObject(props, Map.class));
            }

            return entity;
        }

        @Override
        public ComparablePath<UUID> getPrimaryKey() {
            return qInstance.id;
        }

        @Override
        public EntityType getEntityType() {
            return EntityType.Sensor;
        }

    }

    public static class ObservationFactory implements PropertyHelper.entityFromTupleFactory<Observation> {

        public static final ObservationFactory withDefaultAlias = new ObservationFactory(new QObservations(PathSqlBuilderUuid.ALIAS_PREFIX + "1"));
        private final QObservations qInstance;

        public ObservationFactory(QObservations qInstance) {
            this.qInstance = qInstance;
        }

        @Override
        public Observation create(Tuple tuple, Query query, DataSize dataSize) {
            Observation entity = new Observation();
            Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();

            UUID dsId = tuple.get(qInstance.datastreamId);
            if (dsId != null) {
                entity.setDatastream(datastreamFromId(dsId));
            }
            UUID mDsId = tuple.get(qInstance.multiDatastreamId);
            if (mDsId != null) {
                entity.setMultiDatastream(multiDatastreamFromId(mDsId));
            }

            entity.setFeatureOfInterest(featureOfInterestFromId(tuple.get(qInstance.featureId)));
            UUID id = tuple.get(qInstance.id);
            if (id != null) {
                entity.setId(new UuidId(tuple.get(qInstance.id)));
            }

            if (select.isEmpty() || select.contains(EntityProperty.Parameters)) {
                String props = tuple.get(qInstance.parameters);
                dataSize.increase(props == null ? 0 : props.length());
                entity.setParameters(jsonToObject(props, Map.class));
            }

            Timestamp pTimeStart = tuple.get(qInstance.phenomenonTimeStart);
            Timestamp pTimeEnd = tuple.get(qInstance.phenomenonTimeEnd);
            entity.setPhenomenonTime(valueFromTimes(pTimeStart, pTimeEnd));

            if (select.isEmpty() || select.contains(EntityProperty.Result)) {
                Byte resultTypeOrd = tuple.get(qInstance.resultType);
                if (resultTypeOrd != null) {
                    ResultType resultType = ResultType.fromSqlValue(resultTypeOrd);
                    switch (resultType) {
                        case BOOLEAN:
                            entity.setResult(tuple.get(qInstance.resultBoolean));
                            break;

                        case NUMBER:
                            try {
                                entity.setResult(new BigDecimal(tuple.get(qInstance.resultString)));
                            } catch (NumberFormatException e) {
                                // It was not a Number? Use the double value.
                                entity.setResult(tuple.get(qInstance.resultNumber));
                            }
                            break;

                        case OBJECT_ARRAY:
                            String jsonData = tuple.get(qInstance.resultJson);
                            dataSize.increase(jsonData == null ? 0 : jsonData.length());
                            entity.setResult(jsonToTree(jsonData));
                            break;

                        case STRING:
                            String stringData = tuple.get(qInstance.resultString);
                            dataSize.increase(stringData == null ? 0 : stringData.length());
                            entity.setResult(stringData);
                            break;
                    }
                }
            }

            if (select.isEmpty() || select.contains(EntityProperty.ResultQuality)) {
                String resultQuality = tuple.get(qInstance.resultQuality);
                dataSize.increase(resultQuality == null ? 0 : resultQuality.length());
                entity.setResultQuality(jsonToObject(resultQuality, Object.class));
            }

            entity.setResultTime(instantFromTime(tuple.get(qInstance.resultTime)));

            Timestamp vTimeStart = tuple.get(qInstance.validTimeStart);
            Timestamp vTimeEnd = tuple.get(qInstance.validTimeEnd);
            if (vTimeStart != null && vTimeEnd != null) {
                entity.setValidTime(intervalFromTimes(vTimeStart, vTimeEnd));
            }
            return entity;
        }

        @Override
        public ComparablePath<UUID> getPrimaryKey() {
            return qInstance.id;
        }

        @Override
        public EntityType getEntityType() {
            return EntityType.Observation;
        }

    }

    public static class ObservedPropertyFactory implements PropertyHelper.entityFromTupleFactory<ObservedProperty> {

        public static final ObservedPropertyFactory withDefaultAlias = new ObservedPropertyFactory(new QObsProperties(PathSqlBuilderUuid.ALIAS_PREFIX + "1"));
        private final QObsProperties qInstance;

        public ObservedPropertyFactory(QObsProperties qInstance) {
            this.qInstance = qInstance;
        }

        @Override
        public ObservedProperty create(Tuple tuple, Query query, DataSize dataSize) {
            Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();

            ObservedProperty entity = new ObservedProperty();
            entity.setDefinition(tuple.get(qInstance.definition));
            entity.setDescription(tuple.get(qInstance.description));
            UUID id = tuple.get(qInstance.id);
            if (id != null) {
                entity.setId(new UuidId(tuple.get(qInstance.id)));
            }

            entity.setName(tuple.get(qInstance.name));

            if (select.isEmpty() || select.contains(EntityProperty.Properties)) {
                String props = tuple.get(qInstance.properties);
                entity.setProperties(jsonToObject(props, Map.class));
            }

            return entity;
        }

        @Override
        public ComparablePath<UUID> getPrimaryKey() {
            return qInstance.id;
        }

        @Override
        public EntityType getEntityType() {
            return EntityType.ObservedProperty;
        }

    }

    static {
        FACTORY_PER_ENTITY.put(Datastream.class, DatastreamFactory.withDefaultAlias);
        FACTORY_PER_ENTITY.put(MultiDatastream.class, MultiDatastreamFactory.withDefaultAlias);
        FACTORY_PER_ENTITY.put(Thing.class, ThingFactory.withDefaultAlias);
        FACTORY_PER_ENTITY.put(FeatureOfInterest.class, FeatureOfInterestFactory.withDefaultAlias);
        FACTORY_PER_ENTITY.put(HistoricalLocation.class, HistoricalLocationFactory.withDefaultAlias);
        FACTORY_PER_ENTITY.put(Location.class, LocationFactory.withDefaultAlias);
        FACTORY_PER_ENTITY.put(Sensor.class, SensorFactory.withDefaultAlias);
        FACTORY_PER_ENTITY.put(Observation.class, ObservationFactory.withDefaultAlias);
        FACTORY_PER_ENTITY.put(ObservedProperty.class, ObservedPropertyFactory.withDefaultAlias);
    }

    /**
     * Get the factory for the given entity class, using the default alias
     * PathSqlBuilderUuid.ALIAS_PREFIX + "1".
     *
     * @param <T> The type of entity to get the factory for.
     * @param clazz The class of the entity to get the factory for.
     * @return the factory for the given entity class.
     */
    public static <T extends Entity> entityFromTupleFactory<T> getFactoryFor(Class<T> clazz) {
        entityFromTupleFactory<? extends Entity> factory = FACTORY_PER_ENTITY.get(clazz);
        if (factory == null) {
            throw new AssertionError("No factory found for " + clazz.getName());
        }
        return (entityFromTupleFactory<T>) factory;
    }

    private static TimeInterval intervalFromTimes(Timestamp timeStart, Timestamp timeEnd) {
        if (timeStart == null) {
            timeStart = Timestamp.valueOf(LocalDateTime.MAX);
        }
        if (timeEnd == null) {
            timeEnd = Timestamp.valueOf(LocalDateTime.MIN);
        }
        if (timeEnd.before(timeStart)) {
            return null;
        } else {
            return TimeInterval.create(timeStart.getTime(), timeEnd.getTime());
        }
    }

    private static TimeInstant instantFromTime(Timestamp time) {
        if (time == null) {
            return new TimeInstant(null);
        }
        return TimeInstant.create(time.getTime());
    }

    private static TimeValue valueFromTimes(Timestamp timeStart, Timestamp timeEnd) {
        if (timeEnd == null || timeEnd.equals(timeStart)) {
            return instantFromTime(timeStart);
        }
        return intervalFromTimes(timeStart, timeEnd);
    }

    private static Datastream datastreamFromId(UUID id) {
        if (id == null) {
            return null;
        }
        Datastream ds = new Datastream(true);
        ds.setId(new UuidId(id));
        ds.setExportObject(false);
        return ds;
    }

    private static MultiDatastream multiDatastreamFromId(UUID id) {
        if (id == null) {
            return null;
        }
        MultiDatastream ds = new MultiDatastream();
        ds.setId(new UuidId(id));
        ds.setExportObject(false);
        return ds;
    }

    private static FeatureOfInterest featureOfInterestFromId(UUID id) {
        if (id == null) {
            return null;
        }
        FeatureOfInterest foi = new FeatureOfInterest();
        foi.setId(new UuidId(id));
        foi.setExportObject(false);
        return foi;
    }

    private static ObservedProperty observedProperyFromId(UUID id) {
        if (id == null) {
            return null;
        }
        ObservedProperty op = new ObservedProperty();
        op.setId(new UuidId(id));
        op.setExportObject(false);
        return op;
    }

    private static Sensor sensorFromId(UUID id) {
        if (id == null) {
            return null;
        }
        Sensor sensor = new Sensor();
        sensor.setId(new UuidId(id));
        sensor.setExportObject(false);
        return sensor;
    }

    private static Thing thingFromId(UUID id) {
        if (id == null) {
            return null;
        }
        Thing thing = new Thing();
        thing.setId(new UuidId(id));
        thing.setExportObject(false);
        return thing;
    }

    public static Object locationFromEncoding(String encodingType, String locationString) {
        if (locationString == null || locationString.isEmpty()) {
            return null;
        }
        if (encodingType != null && GeoJsonDeserializier.ENCODINGS.contains(encodingType.toLowerCase())) {
            try {
                Object geoJson = new GeoJsonDeserializier().deserialize(locationString);
                return geoJson;
            } catch (IOException ex) {
                LOGGER.error("Failed to deserialise geoJson.");

            }
        } else {
            try {
                Map map = jsonToObject(locationString, Map.class
                );
                return map;
            } catch (Exception e) {
                LOGGER.trace("Not a map.");
            }
            return locationString;
        }
        return null;
    }

    public static JsonNode jsonToTree(String json) {
        if (json == null) {
            return null;
        }

        try {
            return EntityParser.getSimpleObjectMapper().readTree(json);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to parse stored json.", ex);
        }
    }

    public static <T> T jsonToObject(String json, Class<T> clazz) {
        if (json == null) {
            return null;
        }
        try {
            return EntityParser.getSimpleObjectMapper().readValue(json, clazz);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to parse stored json.", ex);
        }
    }

    public static <T> T jsonToObject(String json, TypeReference<T> typeReference) {
        if (json == null) {
            return null;
        }
        try {
            return EntityParser.getSimpleObjectMapper().readValue(json, typeReference);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to parse stored json.", ex);
        }
    }

}
