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

import de.fraunhofer.iosb.ilt.sta.persistence.postgres.factories.ThingFactory;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.factories.ObservationFactory;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.factories.FeatureOfInterestFactory;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.factories.ObservedPropertyFactory;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.factories.SensorFactory;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.factories.MultiDatastreamFactory;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.factories.HistoricalLocationFactory;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.factories.LocationFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.mysema.commons.lang.CloseableIterator;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.SimpleExpression;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.FeatureOfInterest;
import de.fraunhofer.iosb.ilt.sta.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.sta.model.core.Id;
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.persistence.IdManager;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.factories.DatastreamFactory;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.QCollection;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
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
    public static final TypeReference<List<String>> TYPE_LIST_STRING = new TypeReference<List<String>>() {
        // Empty on purpose.
    };
    public static final TypeReference<List<UnitOfMeasurement>> TYPE_LIST_UOM = new TypeReference<List<UnitOfMeasurement>>() {
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

    private final Map<EntityType, EntityFromTupleFactory<? extends Entity, I, J>> factoryPerEntity = new EnumMap<>(EntityType.class);

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

        factoryPerEntity.put(EntityType.DATASTREAM, datastreamFactory);
        factoryPerEntity.put(EntityType.MULTIDATASTREAM, multiDatastreamFactory);
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
     * @param type The type of the entity to get the factory for.
     * @return the factory for the given entity class.
     */
    public <T extends Entity> EntityFromTupleFactory<T, I, J> getFactoryFor(EntityType type) {
        EntityFromTupleFactory<? extends Entity, I, J> factory = factoryPerEntity.get(type);
        if (factory == null) {
            throw new AssertionError("No factory found for " + type);
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
