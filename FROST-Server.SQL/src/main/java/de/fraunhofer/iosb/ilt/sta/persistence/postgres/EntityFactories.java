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

import com.fasterxml.jackson.core.type.TypeReference;
import com.mysema.commons.lang.CloseableIterator;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.SimpleExpression;
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
import de.fraunhofer.iosb.ilt.sta.model.core.Id;
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.Property;
import de.fraunhofer.iosb.ilt.sta.persistence.IdManager;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQFeatures;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQMultiDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQObsProperties;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQObservations;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQSensors;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQThings;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.QCollection;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.util.GeoHelper;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.geojson.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author scf
 * @param <I> The type of path used for the ID fields.
 * @param <J> The type of the ID fields.
 */
public class EntityFactories<I extends SimpleExpression<J> & Path<J>, J> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityFactories.class);
    private static final TypeReference<List<String>> TYPE_LIST_STRING = new TypeReference<List<String>>() {
        // Empty on purpose.
    };
    private static final TypeReference<List<UnitOfMeasurement>> TYPE_LIST_UOM = new TypeReference<List<UnitOfMeasurement>>() {
        // Empty on purpose.
    };

    public final IdManager<J> idManager;
    public final QCollection<I, J> qCollection;

    public final DatastreamFactory<I, J> datastreamFactory;
    public final MultiDatastreamFactory<I, J> multiDatastreamFactory;
    public final ThingFactory<I, J> thingFactory;
    public final FeatureOfInterestFactory<I, J> featureOfInterestFactory;
    public final HistoricalLocationFactory<I, J> historicalLocationFactory;
    public final LocationFactory<I, J> locationFactory;
    public final SensorFactory<I, J> sensorFactory;
    public final ObservationFactory<I, J> observationFactory;
    public final ObservedPropertyFactory<I, J> observedPropertyFactory;

    private final Map<Class<? extends Entity>, EntityFromTupleFactory<? extends Entity, I, J>> factoryPerEntity = new HashMap<>();

    public static class DatastreamFactory<I extends SimpleExpression<J> & Path<J>, J> implements EntityFromTupleFactory<Datastream, I, J> {

        private final EntityFactories<I, J> factories;
        private final AbstractQDatastreams<?, I, J> qInstance;

        public DatastreamFactory(EntityFactories<I, J> factories, AbstractQDatastreams<?, I, J> qInstance) {
            this.factories = factories;
            this.qInstance = qInstance;
        }

        @Override
        public Datastream create(Tuple tuple, Query query, DataSize dataSize) {
            Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();

            Datastream entity = new Datastream();
            entity.setName(tuple.get(qInstance.name));
            entity.setDescription(tuple.get(qInstance.description));
            J entityId = factories.getIdFromTuple(tuple, qInstance.getId());
            if (entityId != null) {
                entity.setId(factories.idFromObject(entityId));
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
            ObservedProperty op = factories.observedProperyFromId(tuple, qInstance.getObsPropertyId());
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
            entity.setSensor(factories.sensorFromId(tuple, qInstance.getSensorId()));

            entity.setThing(factories.thingFromId(tuple, qInstance.getThingId()));

            entity.setUnitOfMeasurement(new UnitOfMeasurement(tuple.get(qInstance.unitName), tuple.get(qInstance.unitSymbol), tuple.get(qInstance.unitDefinition)));
            return entity;
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

    public static class MultiDatastreamFactory<I extends SimpleExpression<J> & Path<J>, J> implements EntityFromTupleFactory<MultiDatastream, I, J> {

        private final EntityFactories<I, J> factories;
        private final AbstractQMultiDatastreams<?, I, J> qInstance;

        public MultiDatastreamFactory(EntityFactories<I, J> factories, AbstractQMultiDatastreams<?, I, J> qInstance) {
            this.factories = factories;
            this.qInstance = qInstance;
        }

        @Override
        public MultiDatastream create(Tuple tuple, Query query, DataSize dataSize) {
            Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();

            MultiDatastream entity = new MultiDatastream();
            entity.setName(tuple.get(qInstance.name));
            entity.setDescription(tuple.get(qInstance.description));
            J id = factories.getIdFromTuple(tuple, qInstance.getId());
            if (id != null) {
                entity.setId(factories.idFromObject(id));
            }

            List<String> observationTypes = Utils.jsonToObject(tuple.get(qInstance.observationTypes), TYPE_LIST_STRING);
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

            entity.setSensor(factories.sensorFromId(tuple, qInstance.getSensorId()));
            entity.setThing(factories.thingFromId(tuple, qInstance.getThingId()));

            List<UnitOfMeasurement> units = Utils.jsonToObject(tuple.get(qInstance.unitOfMeasurements), TYPE_LIST_UOM);
            entity.setUnitOfMeasurements(units);
            return entity;
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

    public static class ThingFactory<I extends SimpleExpression<J> & Path<J>, J> implements EntityFromTupleFactory<Thing, I, J> {

        private final EntityFactories<I, J> factories;
        private final AbstractQThings<?, I, J> qInstance;

        public ThingFactory(EntityFactories<I, J> factories, AbstractQThings<?, I, J> qInstance) {
            this.factories = factories;
            this.qInstance = qInstance;
        }

        @Override
        public Thing create(Tuple tuple, Query query, DataSize dataSize) {
            Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();

            Thing entity = new Thing();
            entity.setName(tuple.get(qInstance.name));
            entity.setDescription(tuple.get(qInstance.description));

            J id = factories.getIdFromTuple(tuple, qInstance.getId());
            if (id != null) {
                entity.setId(factories.idFromObject(id));
            }

            if (select.isEmpty() || select.contains(EntityProperty.PROPERTIES)) {
                String props = tuple.get(qInstance.properties);
                dataSize.increase(props == null ? 0 : props.length());
                entity.setProperties(Utils.jsonToObject(props, Map.class));
            }

            return entity;
        }

        @Override
        public I getPrimaryKey() {
            return qInstance.getId();
        }

        @Override
        public EntityType getEntityType() {
            return EntityType.THING;
        }

    }

    public static class FeatureOfInterestFactory<I extends SimpleExpression<J> & Path<J>, J> implements EntityFromTupleFactory<FeatureOfInterest, I, J> {

        private final EntityFactories<I, J> factories;
        private final AbstractQFeatures<?, I, J> qInstance;

        public FeatureOfInterestFactory(EntityFactories<I, J> factories, AbstractQFeatures<?, I, J> qInstance) {
            this.factories = factories;
            this.qInstance = qInstance;
        }

        @Override
        public FeatureOfInterest create(Tuple tuple, Query query, DataSize dataSize) {
            Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();

            FeatureOfInterest entity = new FeatureOfInterest();
            J id = factories.getIdFromTuple(tuple, qInstance.getId());
            if (id != null) {
                entity.setId(factories.idFromObject(id));
            }

            entity.setName(tuple.get(qInstance.name));
            entity.setDescription(tuple.get(qInstance.description));
            String encodingType = tuple.get(qInstance.encodingType);
            entity.setEncodingType(encodingType);

            if (select.isEmpty() || select.contains(EntityProperty.FEATURE)) {
                String locationString = tuple.get(qInstance.feature);
                dataSize.increase(locationString == null ? 0 : locationString.length());
                entity.setFeature(Utils.locationFromEncoding(encodingType, locationString));
            }

            if (select.isEmpty() || select.contains(EntityProperty.PROPERTIES)) {
                String props = tuple.get(qInstance.properties);
                entity.setProperties(Utils.jsonToObject(props, Map.class));
            }

            return entity;
        }

        @Override
        public I getPrimaryKey() {
            return qInstance.getId();
        }

        @Override
        public EntityType getEntityType() {
            return EntityType.FEATUREOFINTEREST;
        }

    }

    public static class HistoricalLocationFactory<I extends SimpleExpression<J> & Path<J>, J> implements EntityFromTupleFactory<HistoricalLocation, I, J> {

        private final EntityFactories<I, J> factories;
        private final AbstractQHistLocations<?, I, J> qInstance;

        public HistoricalLocationFactory(EntityFactories<I, J> factories, AbstractQHistLocations<?, I, J> qInstance) {
            this.factories = factories;
            this.qInstance = qInstance;
        }

        @Override
        public HistoricalLocation create(Tuple tuple, Query query, DataSize dataSize) {
            HistoricalLocation entity = new HistoricalLocation();
            J id = factories.getIdFromTuple(tuple, qInstance.getId());
            if (id != null) {
                entity.setId(factories.idFromObject(id));
            }

            entity.setThing(factories.thingFromId(tuple, qInstance.getThingId()));
            entity.setTime(Utils.instantFromTime(tuple.get(qInstance.time)));
            return entity;
        }

        @Override
        public I getPrimaryKey() {
            return qInstance.getId();
        }

        @Override
        public EntityType getEntityType() {
            return EntityType.HISTORICALLOCATION;
        }

    }

    public static class LocationFactory<I extends SimpleExpression<J> & Path<J>, J> implements EntityFromTupleFactory<Location, I, J> {

        private final EntityFactories<I, J> factories;
        private final AbstractQLocations<?, I, J> qInstance;

        public LocationFactory(EntityFactories<I, J> factories, AbstractQLocations<?, I, J> qInstance) {
            this.factories = factories;
            this.qInstance = qInstance;
        }

        @Override
        public Location create(Tuple tuple, Query query, DataSize dataSize) {
            Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();
            Location entity = new Location();
            J id = factories.getIdFromTuple(tuple, qInstance.getId());
            if (id != null) {
                entity.setId(factories.idFromObject(id));
            }

            entity.setName(tuple.get(qInstance.name));
            entity.setDescription(tuple.get(qInstance.description));
            String encodingType = tuple.get(qInstance.encodingType);
            entity.setEncodingType(encodingType);

            if (select.isEmpty() || select.contains(EntityProperty.LOCATION)) {
                String locationString = tuple.get(qInstance.location);
                dataSize.increase(locationString == null ? 0 : locationString.length());
                entity.setLocation(Utils.locationFromEncoding(encodingType, locationString));
            }

            if (select.isEmpty() || select.contains(EntityProperty.PROPERTIES)) {
                String props = tuple.get(qInstance.properties);
                entity.setProperties(Utils.jsonToObject(props, Map.class));
            }

            return entity;
        }

        @Override
        public I getPrimaryKey() {
            return qInstance.getId();
        }

        @Override
        public EntityType getEntityType() {
            return EntityType.LOCATION;
        }

    }

    public static class SensorFactory<I extends SimpleExpression<J> & Path<J>, J> implements EntityFromTupleFactory<Sensor, I, J> {

        private final EntityFactories<I, J> factories;
        private final AbstractQSensors<?, I, J> qInstance;

        public SensorFactory(EntityFactories<I, J> factories, AbstractQSensors<?, I, J> qInstance) {
            this.factories = factories;
            this.qInstance = qInstance;
        }

        @Override
        public Sensor create(Tuple tuple, Query query, DataSize dataSize) {
            Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();

            Sensor entity = new Sensor();
            entity.setName(tuple.get(qInstance.name));
            entity.setDescription(tuple.get(qInstance.description));
            entity.setEncodingType(tuple.get(qInstance.encodingType));

            J id = factories.getIdFromTuple(tuple, qInstance.getId());
            if (id != null) {
                entity.setId(factories.idFromObject(id));
            }

            if (select.isEmpty() || select.contains(EntityProperty.METADATA)) {
                String metaDataString = tuple.get(qInstance.metadata);
                dataSize.increase(metaDataString == null ? 0 : metaDataString.length());
                entity.setMetadata(metaDataString);
            }

            if (select.isEmpty() || select.contains(EntityProperty.PROPERTIES)) {
                String props = tuple.get(qInstance.properties);
                entity.setProperties(Utils.jsonToObject(props, Map.class));
            }

            return entity;
        }

        @Override
        public I getPrimaryKey() {
            return qInstance.getId();
        }

        @Override
        public EntityType getEntityType() {
            return EntityType.SENSOR;
        }

    }

    public static class ObservationFactory<I extends SimpleExpression<J> & Path<J>, J> implements EntityFromTupleFactory<Observation, I, J> {

        private final EntityFactories<I, J> factories;
        private final AbstractQObservations<?, I, J> qInstance;

        public ObservationFactory(EntityFactories<I, J> factories, AbstractQObservations<?, I, J> qInstance) {
            this.factories = factories;
            this.qInstance = qInstance;
        }

        @Override
        public Observation create(Tuple tuple, Query query, DataSize dataSize) {
            Observation entity = new Observation();
            Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();

            J dsId = factories.getIdFromTuple(tuple, qInstance.getDatastreamId());
            if (dsId != null) {
                entity.setDatastream(factories.datastreamFromId(dsId));
            }
            J mDsId = factories.getIdFromTuple(tuple, qInstance.getMultiDatastreamId());
            if (mDsId != null) {
                entity.setMultiDatastream(factories.multiDatastreamFromId(mDsId));
            }

            entity.setFeatureOfInterest(factories.featureOfInterestFromId(tuple, qInstance.getFeatureId()));
            J id = factories.getIdFromTuple(tuple, qInstance.getId());
            if (id != null) {
                entity.setId(factories.idFromObject(id));
            }

            if (select.isEmpty() || select.contains(EntityProperty.PARAMETERS)) {
                String props = tuple.get(qInstance.parameters);
                dataSize.increase(props == null ? 0 : props.length());
                entity.setParameters(Utils.jsonToObject(props, Map.class));
            }

            Timestamp pTimeStart = tuple.get(qInstance.phenomenonTimeStart);
            Timestamp pTimeEnd = tuple.get(qInstance.phenomenonTimeEnd);
            entity.setPhenomenonTime(Utils.valueFromTimes(pTimeStart, pTimeEnd));

            if (select.isEmpty() || select.contains(EntityProperty.RESULT)) {
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
                            entity.setResult(Utils.jsonToTree(jsonData));
                            break;

                        case STRING:
                            String stringData = tuple.get(qInstance.resultString);
                            dataSize.increase(stringData == null ? 0 : stringData.length());
                            entity.setResult(stringData);
                            break;
                    }
                }
            }

            if (select.isEmpty() || select.contains(EntityProperty.RESULTQUALITY)) {
                String resultQuality = tuple.get(qInstance.resultQuality);
                dataSize.increase(resultQuality == null ? 0 : resultQuality.length());
                entity.setResultQuality(Utils.jsonToObject(resultQuality, Object.class));
            }

            entity.setResultTime(Utils.instantFromTime(tuple.get(qInstance.resultTime)));

            Timestamp vTimeStart = tuple.get(qInstance.validTimeStart);
            Timestamp vTimeEnd = tuple.get(qInstance.validTimeEnd);
            if (vTimeStart != null && vTimeEnd != null) {
                entity.setValidTime(Utils.intervalFromTimes(vTimeStart, vTimeEnd));
            }
            return entity;
        }

        @Override
        public I getPrimaryKey() {
            return qInstance.getId();
        }

        @Override
        public EntityType getEntityType() {
            return EntityType.OBSERVATION;
        }

    }

    public static class ObservedPropertyFactory<I extends SimpleExpression<J> & Path<J>, J> implements EntityFromTupleFactory<ObservedProperty, I, J> {

        private final EntityFactories<I, J> factories;
        private final AbstractQObsProperties<?, I, J> qInstance;

        public ObservedPropertyFactory(EntityFactories<I, J> factories, AbstractQObsProperties<?, I, J> qInstance) {
            this.factories = factories;
            this.qInstance = qInstance;
        }

        @Override
        public ObservedProperty create(Tuple tuple, Query query, DataSize dataSize) {
            Set<Property> select = query == null ? Collections.emptySet() : query.getSelect();

            ObservedProperty entity = new ObservedProperty();
            entity.setDefinition(tuple.get(qInstance.definition));
            entity.setDescription(tuple.get(qInstance.description));
            J id = factories.getIdFromTuple(tuple, qInstance.getId());
            if (id != null) {
                entity.setId(factories.idFromObject(id));
            }

            entity.setName(tuple.get(qInstance.name));

            if (select.isEmpty() || select.contains(EntityProperty.PROPERTIES)) {
                String props = tuple.get(qInstance.properties);
                entity.setProperties(Utils.jsonToObject(props, Map.class));
            }

            return entity;
        }

        @Override
        public I getPrimaryKey() {
            return qInstance.getId();
        }

        @Override
        public EntityType getEntityType() {
            return EntityType.OBSERVEDPROPERTY;
        }

    }

    public EntityFactories(IdManager<J> idManager, QCollection<I, J> qCollection) {
        this.idManager = idManager;
        this.qCollection = qCollection;

        String defaultPrefix = PathSqlBuilderImp.ALIAS_PREFIX + "1";

        datastreamFactory = new DatastreamFactory<>(this, qCollection.qDatastreams.newWithAlias(defaultPrefix));
        multiDatastreamFactory = new MultiDatastreamFactory<>(this, qCollection.qMultiDatastreams.newWithAlias(defaultPrefix));
        thingFactory = new ThingFactory<>(this, qCollection.qThings.newWithAlias(defaultPrefix));
        featureOfInterestFactory = new FeatureOfInterestFactory<>(this, qCollection.qFeatures.newWithAlias(defaultPrefix));
        historicalLocationFactory = new HistoricalLocationFactory<>(this, qCollection.qHistLocations.newWithAlias(defaultPrefix));
        locationFactory = new LocationFactory<>(this, qCollection.qLocations.newWithAlias(defaultPrefix));
        sensorFactory = new SensorFactory<>(this, qCollection.qSensors.newWithAlias(defaultPrefix));
        observationFactory = new ObservationFactory<>(this, qCollection.qObservations.newWithAlias(defaultPrefix));
        observedPropertyFactory = new ObservedPropertyFactory<>(this, qCollection.qObsProperties.newWithAlias(defaultPrefix));

        factoryPerEntity.put(Datastream.class, datastreamFactory);
        factoryPerEntity.put(MultiDatastream.class, multiDatastreamFactory);
        factoryPerEntity.put(Thing.class, thingFactory);
        factoryPerEntity.put(FeatureOfInterest.class, featureOfInterestFactory);
        factoryPerEntity.put(HistoricalLocation.class, historicalLocationFactory);
        factoryPerEntity.put(Location.class, locationFactory);
        factoryPerEntity.put(Sensor.class, sensorFactory);
        factoryPerEntity.put(Observation.class, observationFactory);
        factoryPerEntity.put(ObservedProperty.class, observedPropertyFactory);

    }

    public <T extends Entity> EntitySet<T> createSetFromTuples(EntityFromTupleFactory<T, I, J> factory, CloseableIterator<Tuple> tuples, Query query, long maxDataSize) {
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
     * @param clazz The class of the entity to get the factory for.
     * @return the factory for the given entity class.
     */
    public <T extends Entity> EntityFromTupleFactory<T, I, J> getFactoryFor(Class<T> clazz) {
        EntityFromTupleFactory<? extends Entity, I, J> factory = factoryPerEntity.get(clazz);
        if (factory == null) {
            throw new AssertionError("No factory found for " + clazz.getName());
        }
        return (EntityFromTupleFactory<T, I, J>) factory;
    }

    public J getIdFromTuple(Tuple t, I path) {
        return t.get(path);
    }

    public Id idFromObject(J id) {
        return idManager.fromObject(id);
    }

    public Datastream datastreamFromId(Tuple tuple, I path) {
        return datastreamFromId(getIdFromTuple(tuple, path));
    }

    public Datastream datastreamFromId(J id) {
        if (id == null) {
            return null;
        }
        Datastream ds = new Datastream(true);
        ds.setId(idManager.fromObject(id));
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

}
