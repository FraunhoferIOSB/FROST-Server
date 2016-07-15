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

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import de.fraunhofer.iosb.ilt.sta.deserialize.EntityParser;
import de.fraunhofer.iosb.ilt.sta.deserialize.custom.geojson.GeoJsonDeserializier;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.FeatureOfInterest;
import de.fraunhofer.iosb.ilt.sta.model.HistoricalLocation;
import de.fraunhofer.iosb.ilt.sta.model.Location;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.sta.model.id.LongId;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.persistence.QDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.QFeatures;
import de.fraunhofer.iosb.ilt.sta.persistence.QHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.QLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.QObsProperties;
import de.fraunhofer.iosb.ilt.sta.persistence.QObservations;
import de.fraunhofer.iosb.ilt.sta.persistence.QSensors;
import de.fraunhofer.iosb.ilt.sta.persistence.QThings;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author scf
 */
public class PropertyHelper {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyHelper.class);

    public static Expression<?>[] getExpressions(Path<?> qPath, List<EntityProperty> selectedProperties) {
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

    public static Datastream createDatastreamFromTuple(Tuple tuple, QDatastreams qDatastreams) {
        Datastream entity = new Datastream();
        entity.setName(tuple.get(qDatastreams.name));
        entity.setDescription(tuple.get(qDatastreams.description));
        Long id = tuple.get(qDatastreams.id);
        if (id != null) {
            entity.setId(new LongId(tuple.get(qDatastreams.id)));
        }
        entity.setObservationType(tuple.get(qDatastreams.observationType));

        // TODO: Figure out database storage for polygons.
        // entity.setObservedArea();
        ObservedProperty op = observedProperyFromId(tuple.get(qDatastreams.obsPropertyId));
        entity.setObservedProperty(op);

        Timestamp pTimeStart = tuple.get(qDatastreams.phenomenonTimeStart);
        Timestamp pTimeEnd = tuple.get(qDatastreams.phenomenonTimeEnd);
        entity.setPhenomenonTime(intervalFromTimes(pTimeStart, pTimeEnd));

        Timestamp rTimeStart = tuple.get(qDatastreams.resultTimeStart);
        Timestamp rTimeEnd = tuple.get(qDatastreams.resultTimeEnd);
        entity.setResultTime(intervalFromTimes(rTimeStart, rTimeEnd));

        entity.setSensor(sensorFromId(tuple.get(qDatastreams.sensorId)));
        entity.setThing(thingFromId(tuple.get(qDatastreams.thingId)));

        entity.setUnitOfMeasurement(new UnitOfMeasurement(tuple.get(qDatastreams.unitName), tuple.get(qDatastreams.unitSymbol), tuple.get(qDatastreams.unitDefinition)));
        return entity;
    }

    public static EntitySet<Datastream> createDatastreamsFromTuples(List<Tuple> tuples, QDatastreams qDatastreams) {
        EntitySet<Datastream> entitySet = new EntitySetImpl<>(EntityType.Datastream);
        for (Tuple tuple : tuples) {
            entitySet.add(createDatastreamFromTuple(tuple, qDatastreams));
        }
        return entitySet;
    }

    public static Thing createThingFromTuple(Tuple tuple, QThings qThings) {
        Thing entity = new Thing();
        entity.setName(tuple.get(qThings.name));
        entity.setDescription(tuple.get(qThings.description));
        Long id = tuple.get(qThings.id);
        if (id != null) {
            entity.setId(new LongId(tuple.get(qThings.id)));
        }

        String props = tuple.get(qThings.properties);
        entity.setProperties(jsonToObject(props, Map.class));
        return entity;
    }

    public static EntitySet<Thing> createThingsFromTuples(List<Tuple> tuples, QThings qThings) {
        EntitySet<Thing> entitySet = new EntitySetImpl<>(EntityType.Thing);
        for (Tuple tuple : tuples) {
            entitySet.add(createThingFromTuple(tuple, qThings));
        }
        return entitySet;
    }

    public static FeatureOfInterest createFeatureOfInterestFromTuple(Tuple tuple, QFeatures qFeatures) {
        FeatureOfInterest entity = new FeatureOfInterest();
        Long id = tuple.get(qFeatures.id);
        if (id != null) {
            entity.setId(new LongId(tuple.get(qFeatures.id)));
        }

        entity.setName(tuple.get(qFeatures.name));
        entity.setDescription(tuple.get(qFeatures.description));
        String encodingType = tuple.get(qFeatures.encodingType);
        String locationString = tuple.get(qFeatures.feature);
        entity.setEncodingType(encodingType);
        entity.setFeature(locationFromEncoding(encodingType, locationString));
        return entity;
    }

    public static EntitySet<FeatureOfInterest> createFeaturesOfInterestFromTuples(List<Tuple> tuples, QFeatures qFeatures) {
        EntitySet<FeatureOfInterest> entitySet = new EntitySetImpl<>(EntityType.FeatureOfInterest);
        for (Tuple tuple : tuples) {
            entitySet.add(createFeatureOfInterestFromTuple(tuple, qFeatures));
        }
        return entitySet;
    }

    public static HistoricalLocation createHistoricalLocationFromTuple(Tuple tuple, QHistLocations qHistoricalLocation) {
        HistoricalLocation entity = new HistoricalLocation();
        Long id = tuple.get(qHistoricalLocation.id);
        if (id != null) {
            entity.setId(new LongId(tuple.get(qHistoricalLocation.id)));
        }

        entity.setThing(thingFromId(tuple.get(qHistoricalLocation.thingId)));
        entity.setTime(instantFromTime(tuple.get(qHistoricalLocation.time)));
        return entity;
    }

    public static EntitySet<HistoricalLocation> createHistoricalLocationsFromTuples(List<Tuple> tuples, QHistLocations qHistLocations) {
        EntitySet<HistoricalLocation> entitySet = new EntitySetImpl<>(EntityType.HistoricalLocation);
        for (Tuple tuple : tuples) {
            entitySet.add(createHistoricalLocationFromTuple(tuple, qHistLocations));
        }
        return entitySet;
    }

    public static Location createLocationFromTuple(Tuple tuple, QLocations qLocation) {
        Location entity = new Location();
        Long id = tuple.get(qLocation.id);
        if (id != null) {
            entity.setId(new LongId(tuple.get(qLocation.id)));
        }

        entity.setName(tuple.get(qLocation.name));
        entity.setDescription(tuple.get(qLocation.description));
        String encodingType = tuple.get(qLocation.encodingType);
        String locationString = tuple.get(qLocation.location);
        entity.setEncodingType(encodingType);
        entity.setLocation(locationFromEncoding(encodingType, locationString));
        return entity;
    }

    public static EntitySet<Location> createLocationsFromTuples(List<Tuple> tuples, QLocations qLocations) {
        EntitySet<Location> entitySet = new EntitySetImpl<>(EntityType.Location);
        for (Tuple tuple : tuples) {
            entitySet.add(createLocationFromTuple(tuple, qLocations));
        }
        return entitySet;
    }

    public static Sensor createSensorFromTuple(Tuple tuple, QSensors qSensors) {
        Sensor entity = new Sensor();
        entity.setName(tuple.get(qSensors.name));
        entity.setDescription(tuple.get(qSensors.description));
        entity.setEncodingType(tuple.get(qSensors.encodingType));
        Long id = tuple.get(qSensors.id);
        if (id != null) {
            entity.setId(new LongId(tuple.get(qSensors.id)));
        }

        entity.setMetadata(tuple.get(qSensors.metadata));
        return entity;
    }

    public static EntitySet<Sensor> createSensorsFromTuples(List<Tuple> tuples, QSensors qSensors) {
        EntitySet<Sensor> entitySet = new EntitySetImpl<>(EntityType.Sensor);
        for (Tuple tuple : tuples) {
            entitySet.add(createSensorFromTuple(tuple, qSensors));
        }
        return entitySet;
    }

    public static Observation createObservationFromTuple(Tuple tuple, QObservations qObservations) {
        Observation entity = new Observation();
        entity.setDatastream(datastreamFromId(tuple.get(qObservations.datastreamId)));
        entity.setFeatureOfInterest(featureOfInterestFromId(tuple.get(qObservations.featureId)));
        Long id = tuple.get(qObservations.id);
        if (id != null) {
            entity.setId(new LongId(tuple.get(qObservations.id)));
        }

        String props = tuple.get(qObservations.parameters);
        entity.setParameters(jsonToObject(props, Map.class));

        Timestamp pTimeStart = tuple.get(qObservations.phenomenonTimeStart);
        Timestamp pTimeEnd = tuple.get(qObservations.phenomenonTimeEnd);
        entity.setPhenomenonTime(valueFromTimes(pTimeStart, pTimeEnd));

        Double numberRes = tuple.get(qObservations.resultNumber);
        String stringRes = tuple.get(qObservations.resultString);
        if (numberRes != null) {
            try {
                entity.setResult(new BigDecimal(stringRes));
            } catch (NumberFormatException e) {
                // It was not a Number? Use the double value.
                entity.setResult(numberRes);
            }
        } else {
            entity.setResult(stringRes);
        }

        String resultQuality = tuple.get(qObservations.resultQuality);
        entity.setResultQuality(jsonToObject(resultQuality, Object.class));
        entity.setResultTime(instantFromTime(tuple.get(qObservations.resultTime)));

        Timestamp vTimeStart = tuple.get(qObservations.validTimeStart);
        Timestamp vTimeEnd = tuple.get(qObservations.validTimeEnd);
        if (vTimeStart != null && vTimeEnd != null) {
            entity.setValidTime(intervalFromTimes(vTimeStart, vTimeEnd));
        }
        return entity;
    }

    public static EntitySet<Observation> createObservationsFromTuples(List<Tuple> tuples, QObservations qObservations) {
        EntitySet<Observation> entitySet = new EntitySetImpl<>(EntityType.Observation);
        for (Tuple tuple : tuples) {
            entitySet.add(createObservationFromTuple(tuple, qObservations));
        }
        return entitySet;
    }

    public static ObservedProperty createObservedPropertyFromTuple(Tuple tuple, QObsProperties qObsProperties) {
        ObservedProperty entity = new ObservedProperty();
        entity.setDefinition(tuple.get(qObsProperties.definition));
        entity.setDescription(tuple.get(qObsProperties.description));
        Long id = tuple.get(qObsProperties.id);
        if (id != null) {
            entity.setId(new LongId(tuple.get(qObsProperties.id)));
        }

        entity.setName(tuple.get(qObsProperties.name));
        return entity;
    }

    public static EntitySet<ObservedProperty> createObservedPropertiesFromTuples(List<Tuple> tuples, QObsProperties qObsProperties) {
        EntitySet<ObservedProperty> entitySet = new EntitySetImpl<>(EntityType.ObservedProperty);
        for (Tuple tuple : tuples) {
            entitySet.add(createObservedPropertyFromTuple(tuple, qObsProperties));
        }
        return entitySet;
    }

    private static TimeInterval intervalFromTimes(Timestamp timeStart, Timestamp timeEnd) {
        if (timeStart == null) {
            timeStart = Timestamp.valueOf(LocalDateTime.MAX);
        }
        if (timeEnd == null) {
            timeEnd = Timestamp.valueOf(LocalDateTime.MIN);
        }
        if (timeStart.before(timeEnd)) {
            return TimeInterval.create(timeStart.getTime(), timeEnd.getTime());
        } else {
            return null;
        }
    }

    private static TimeInstant instantFromTime(Timestamp time) {
        if (time == null) {
            return new TimeInstant(null);
        }
        return TimeInstant.create(time.getTime());
    }

    private static TimeValue valueFromTimes(Timestamp timeStart, Timestamp timeEnd) {
        if (timeEnd == null) {
            return instantFromTime(timeStart);
        }
        return intervalFromTimes(timeStart, timeEnd);
    }

    private static Datastream datastreamFromId(Long id) {
        if (id == null) {
            return null;
        }
        Datastream ds = new Datastream();
        ds.setId(new LongId(id));
        ds.setExportObject(false);
        return ds;
    }

    private static FeatureOfInterest featureOfInterestFromId(Long id) {
        if (id == null) {
            return null;
        }
        FeatureOfInterest foi = new FeatureOfInterest();
        foi.setId(new LongId(id));
        foi.setExportObject(false);
        return foi;
    }

    private static ObservedProperty observedProperyFromId(Long id) {
        if (id == null) {
            return null;
        }
        ObservedProperty op = new ObservedProperty();
        op.setId(new LongId(id));
        op.setExportObject(false);
        return op;
    }

    private static Sensor sensorFromId(Long id) {
        if (id == null) {
            return null;
        }
        Sensor sensor = new Sensor();
        sensor.setId(new LongId(id));
        sensor.setExportObject(false);
        return sensor;
    }

    private static Thing thingFromId(Long id) {
        if (id == null) {
            return null;
        }
        Thing thing = new Thing();
        thing.setId(new LongId(id));
        thing.setExportObject(false);
        return thing;
    }

    public static Object locationFromEncoding(String encodingType, String locationString) {
        if (locationString == null || locationString.isEmpty()) {
            return null;
        }
        if ("application/vnd.geo+json".equalsIgnoreCase(encodingType)) {
            try {
                Object geoJson = new GeoJsonDeserializier().deserialize(locationString);
                return geoJson;
            } catch (IOException ex) {
                LOGGER.error("Failed to deserialise geoJson.");
            }
        } else {
            try {
                Map map = jsonToObject(locationString, Map.class);
                return map;
            } catch (Exception e) {
                LOGGER.trace("Not a map.");
            }
            return locationString;
        }
        return null;
    }

    public static <T> T jsonToObject(String json, Class<T> clazz) {
        if (json == null) {
            return null;
        }
        try {
            return new EntityParser(LongId.class).parseObject(clazz, json);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to parse stored json.", ex);
        }
    }

}
