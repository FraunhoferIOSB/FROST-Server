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

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.NavigationProperty;
import de.fraunhofer.iosb.ilt.sta.path.Property;
import de.fraunhofer.iosb.ilt.sta.persistence.QDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.QFeatures;
import de.fraunhofer.iosb.ilt.sta.persistence.QHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.QLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.QMultiDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.QObsProperties;
import de.fraunhofer.iosb.ilt.sta.persistence.QObservations;
import de.fraunhofer.iosb.ilt.sta.persistence.QSensors;
import de.fraunhofer.iosb.ilt.sta.persistence.QThings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class PropertyResolver {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyResolver.class);
    public static final String KEY_TIME_INTERVAL_START = "tStart";
    public static final String KEY_TIME_INTERVAL_END = "tEnd";

    private static interface ExpressionFactory<T> {

        Expression<?> get(T qPath);
    }

    private static final Map<Property, Map<Class, ExpressionFactory>> epMapSingle = new HashMap<>();
    private static final Map<Property, Map<Class, Map<String, ExpressionFactory>>> epMapMulti = new HashMap<>();
    private static final Map<Class, List<ExpressionFactory>> allForClass = new HashMap<>();

    static {
        addEntry(EntityProperty.Id, QDatastreams.class, (ExpressionFactory<QDatastreams>) (QDatastreams qPath) -> qPath.id);
        addEntry(EntityProperty.SelfLink, QDatastreams.class, (ExpressionFactory<QDatastreams>) (QDatastreams qPath) -> qPath.id);
        addEntry(EntityProperty.Name, QDatastreams.class, (ExpressionFactory<QDatastreams>) (QDatastreams qPath) -> qPath.name);
        addEntry(EntityProperty.Description, QDatastreams.class, (ExpressionFactory<QDatastreams>) (QDatastreams qPath) -> qPath.description);
        addEntry(EntityProperty.ObservationType, QDatastreams.class, (ExpressionFactory<QDatastreams>) (QDatastreams qPath) -> qPath.observationType);
        addEntry(EntityProperty.ObservedArea, QDatastreams.class, (ExpressionFactory<QDatastreams>) (QDatastreams qPath) -> qPath.observedArea.asText());
        addEntry(EntityProperty.PhenomenonTime, QDatastreams.class, KEY_TIME_INTERVAL_START, (ExpressionFactory<QDatastreams>) (QDatastreams qPath) -> qPath.phenomenonTimeStart);
        addEntry(EntityProperty.PhenomenonTime, QDatastreams.class, KEY_TIME_INTERVAL_END, (ExpressionFactory<QDatastreams>) (QDatastreams qPath) -> qPath.phenomenonTimeEnd);
        addEntry(EntityProperty.ResultTime, QDatastreams.class, KEY_TIME_INTERVAL_START, (ExpressionFactory<QDatastreams>) (QDatastreams qPath) -> qPath.resultTimeStart);
        addEntry(EntityProperty.ResultTime, QDatastreams.class, KEY_TIME_INTERVAL_END, (ExpressionFactory<QDatastreams>) (QDatastreams qPath) -> qPath.resultTimeEnd);
        addEntry(EntityProperty.UnitOfMeasurement, QDatastreams.class, "definition", (ExpressionFactory<QDatastreams>) (QDatastreams qPath) -> qPath.unitDefinition);
        addEntry(EntityProperty.UnitOfMeasurement, QDatastreams.class, "name", (ExpressionFactory<QDatastreams>) (QDatastreams qPath) -> qPath.unitName);
        addEntry(EntityProperty.UnitOfMeasurement, QDatastreams.class, "symbol", (ExpressionFactory<QDatastreams>) (QDatastreams qPath) -> qPath.unitSymbol);
        addEntry(NavigationProperty.Sensor, QDatastreams.class, (ExpressionFactory<QDatastreams>) (QDatastreams qPath) -> qPath.sensorId);
        addEntry(NavigationProperty.ObservedProperty, QDatastreams.class, (ExpressionFactory<QDatastreams>) (QDatastreams qPath) -> qPath.obsPropertyId);
        addEntry(NavigationProperty.Thing, QDatastreams.class, (ExpressionFactory<QDatastreams>) (QDatastreams qPath) -> qPath.thingId);

        addEntry(EntityProperty.Id, QMultiDatastreams.class, (ExpressionFactory<QMultiDatastreams>) (QMultiDatastreams qPath) -> qPath.id);
        addEntry(EntityProperty.SelfLink, QMultiDatastreams.class, (ExpressionFactory<QMultiDatastreams>) (QMultiDatastreams qPath) -> qPath.id);
        addEntry(EntityProperty.Name, QMultiDatastreams.class, (ExpressionFactory<QMultiDatastreams>) (QMultiDatastreams qPath) -> qPath.name);
        addEntry(EntityProperty.Description, QMultiDatastreams.class, (ExpressionFactory<QMultiDatastreams>) (QMultiDatastreams qPath) -> qPath.description);
        addEntry(EntityProperty.MultiObservationDataTypes, QMultiDatastreams.class, (ExpressionFactory<QMultiDatastreams>) (QMultiDatastreams qPath) -> qPath.observationTypes);
        addEntry(EntityProperty.ObservedArea, QMultiDatastreams.class, (ExpressionFactory<QMultiDatastreams>) (QMultiDatastreams qPath) -> qPath.observedArea.asText());
        addEntry(EntityProperty.PhenomenonTime, QMultiDatastreams.class, KEY_TIME_INTERVAL_START, (ExpressionFactory<QMultiDatastreams>) (QMultiDatastreams qPath) -> qPath.phenomenonTimeStart);
        addEntry(EntityProperty.PhenomenonTime, QMultiDatastreams.class, KEY_TIME_INTERVAL_END, (ExpressionFactory<QMultiDatastreams>) (QMultiDatastreams qPath) -> qPath.phenomenonTimeEnd);
        addEntry(EntityProperty.ResultTime, QMultiDatastreams.class, KEY_TIME_INTERVAL_START, (ExpressionFactory<QMultiDatastreams>) (QMultiDatastreams qPath) -> qPath.resultTimeStart);
        addEntry(EntityProperty.ResultTime, QMultiDatastreams.class, KEY_TIME_INTERVAL_END, (ExpressionFactory<QMultiDatastreams>) (QMultiDatastreams qPath) -> qPath.resultTimeEnd);
        addEntry(EntityProperty.UnitOfMeasurements, QMultiDatastreams.class, (ExpressionFactory<QMultiDatastreams>) (QMultiDatastreams qPath) -> qPath.unitOfMeasurements);
        addEntry(NavigationProperty.Sensor, QMultiDatastreams.class, (ExpressionFactory<QMultiDatastreams>) (QMultiDatastreams qPath) -> qPath.sensorId);
        addEntry(NavigationProperty.Thing, QMultiDatastreams.class, (ExpressionFactory<QMultiDatastreams>) (QMultiDatastreams qPath) -> qPath.thingId);

        addEntry(EntityProperty.Id, QFeatures.class, (ExpressionFactory<QFeatures>) (QFeatures qPath) -> qPath.id);
        addEntry(EntityProperty.SelfLink, QFeatures.class, (ExpressionFactory<QFeatures>) (QFeatures qPath) -> qPath.id);
        addEntry(EntityProperty.Name, QFeatures.class, (ExpressionFactory<QFeatures>) (QFeatures qPath) -> qPath.name);
        addEntry(EntityProperty.Description, QFeatures.class, (ExpressionFactory<QFeatures>) (QFeatures qPath) -> qPath.description);
        addEntry(EntityProperty.EncodingType, QFeatures.class, (ExpressionFactory<QFeatures>) (QFeatures qPath) -> qPath.encodingType);
        addEntry(EntityProperty.Feature, QFeatures.class, (ExpressionFactory<QFeatures>) (QFeatures qPath) -> qPath.feature);

        addEntry(EntityProperty.Id, QHistLocations.class, (ExpressionFactory<QHistLocations>) (QHistLocations qPath) -> qPath.id);
        addEntry(EntityProperty.SelfLink, QHistLocations.class, (ExpressionFactory<QHistLocations>) (QHistLocations qPath) -> qPath.id);
        addEntry(EntityProperty.Time, QHistLocations.class, (ExpressionFactory<QHistLocations>) (QHistLocations qPath) -> qPath.time);
        addEntry(NavigationProperty.Thing, QHistLocations.class, (ExpressionFactory<QHistLocations>) (QHistLocations qPath) -> qPath.thingId);

        addEntry(EntityProperty.Id, QLocations.class, (ExpressionFactory<QLocations>) (QLocations qPath) -> qPath.id);
        addEntry(EntityProperty.SelfLink, QLocations.class, (ExpressionFactory<QLocations>) (QLocations qPath) -> qPath.id);
        addEntry(EntityProperty.Name, QLocations.class, (ExpressionFactory<QLocations>) (QLocations qPath) -> qPath.name);
        addEntry(EntityProperty.Description, QLocations.class, (ExpressionFactory<QLocations>) (QLocations qPath) -> qPath.description);
        addEntry(EntityProperty.EncodingType, QLocations.class, (ExpressionFactory<QLocations>) (QLocations qPath) -> qPath.encodingType);
        addEntry(EntityProperty.Location, QLocations.class, (ExpressionFactory<QLocations>) (QLocations qPath) -> qPath.location);
        addEntry(EntityProperty.Location, QLocations.class, (ExpressionFactory<QLocations>) (QLocations qPath) -> qPath.geom);

        addEntry(EntityProperty.Id, QObsProperties.class, (ExpressionFactory<QObsProperties>) (QObsProperties qPath) -> qPath.id);
        addEntry(EntityProperty.SelfLink, QObsProperties.class, (ExpressionFactory<QObsProperties>) (QObsProperties qPath) -> qPath.id);
        addEntry(EntityProperty.Definition, QObsProperties.class, (ExpressionFactory<QObsProperties>) (QObsProperties qPath) -> qPath.definition);
        addEntry(EntityProperty.Description, QObsProperties.class, (ExpressionFactory<QObsProperties>) (QObsProperties qPath) -> qPath.description);
        addEntry(EntityProperty.Name, QObsProperties.class, (ExpressionFactory<QObsProperties>) (QObsProperties qPath) -> qPath.name);

        addEntry(EntityProperty.Id, QObservations.class, (ExpressionFactory<QObservations>) (QObservations qPath) -> qPath.id);
        addEntry(EntityProperty.SelfLink, QObservations.class, (ExpressionFactory<QObservations>) (QObservations qPath) -> qPath.id);
        addEntry(EntityProperty.Parameters, QObservations.class, (ExpressionFactory<QObservations>) (QObservations qPath) -> qPath.parameters);
        addEntry(EntityProperty.PhenomenonTime, QObservations.class, KEY_TIME_INTERVAL_START, (ExpressionFactory<QObservations>) (QObservations qPath) -> qPath.phenomenonTimeStart);
        addEntry(EntityProperty.PhenomenonTime, QObservations.class, KEY_TIME_INTERVAL_END, (ExpressionFactory<QObservations>) (QObservations qPath) -> qPath.phenomenonTimeEnd);
        addEntry(EntityProperty.Result, QObservations.class, "n", (ExpressionFactory<QObservations>) (QObservations qPath) -> qPath.resultNumber);
        addEntry(EntityProperty.Result, QObservations.class, "b", (ExpressionFactory<QObservations>) (QObservations qPath) -> qPath.resultBoolean);
        addEntry(EntityProperty.Result, QObservations.class, "s", (ExpressionFactory<QObservations>) (QObservations qPath) -> qPath.resultString);
        addEntry(EntityProperty.Result, QObservations.class, "j", (ExpressionFactory<QObservations>) (QObservations qPath) -> qPath.resultJson);
        addEntry(EntityProperty.Result, QObservations.class, "t", (ExpressionFactory<QObservations>) (QObservations qPath) -> qPath.resultType);
        addEntry(EntityProperty.ResultQuality, QObservations.class, (ExpressionFactory<QObservations>) (QObservations qPath) -> qPath.resultQuality);
        addEntry(EntityProperty.ResultTime, QObservations.class, (ExpressionFactory<QObservations>) (QObservations qPath) -> qPath.resultTime);
        addEntry(EntityProperty.ValidTime, QObservations.class, KEY_TIME_INTERVAL_START, (ExpressionFactory<QObservations>) (QObservations qPath) -> qPath.validTimeStart);
        addEntry(EntityProperty.ValidTime, QObservations.class, KEY_TIME_INTERVAL_END, (ExpressionFactory<QObservations>) (QObservations qPath) -> qPath.validTimeEnd);
        addEntry(NavigationProperty.FeatureOfInterest, QObservations.class, (ExpressionFactory<QObservations>) (QObservations qPath) -> qPath.featureId);
        addEntry(NavigationProperty.Datastream, QObservations.class, (ExpressionFactory<QObservations>) (QObservations qPath) -> qPath.datastreamId);
        addEntry(NavigationProperty.MultiDatastream, QObservations.class, (ExpressionFactory<QObservations>) (QObservations qPath) -> qPath.multiDatastreamId);

        addEntry(EntityProperty.Id, QSensors.class, (ExpressionFactory<QSensors>) (QSensors qPath) -> qPath.id);
        addEntry(EntityProperty.SelfLink, QSensors.class, (ExpressionFactory<QSensors>) (QSensors qPath) -> qPath.id);
        addEntry(EntityProperty.Name, QSensors.class, (ExpressionFactory<QSensors>) (QSensors qPath) -> qPath.name);
        addEntry(EntityProperty.Description, QSensors.class, (ExpressionFactory<QSensors>) (QSensors qPath) -> qPath.description);
        addEntry(EntityProperty.EncodingType, QSensors.class, (ExpressionFactory<QSensors>) (QSensors qPath) -> qPath.encodingType);
        addEntry(EntityProperty.Metadata, QSensors.class, (ExpressionFactory<QSensors>) (QSensors qPath) -> qPath.metadata);

        addEntry(EntityProperty.Id, QThings.class, (ExpressionFactory<QThings>) (QThings qPath) -> qPath.id);
        addEntry(EntityProperty.SelfLink, QThings.class, (ExpressionFactory<QThings>) (QThings qPath) -> qPath.id);
        addEntry(EntityProperty.Name, QThings.class, (ExpressionFactory<QThings>) (QThings qPath) -> qPath.name);
        addEntry(EntityProperty.Description, QThings.class, (ExpressionFactory<QThings>) (QThings qPath) -> qPath.description);
        addEntry(EntityProperty.Properties, QThings.class, (ExpressionFactory<QThings>) (QThings qPath) -> qPath.properties);
    }

    /**
     *
     * @param qPath The path to get expressions for.
     * @param target The list to add to. If null a new list will be created.
     * @return The target list, or a new list if target was null.
     */
    public static List<Expression<?>> expressionsForClass(Path<?> qPath, List<Expression<?>> target) {
        List<ExpressionFactory> list = allForClass.get(qPath.getClass());
        if (target == null) {
            target = new ArrayList<>();
        }
        for (ExpressionFactory f : list) {
            target.add(f.get(qPath));
        }
        return target;
    }

    public static Expression<?> expressionForProperty(EntityProperty property, Path<?> qPath) {
        Map<Class, ExpressionFactory> innerMap = epMapSingle.get(property);
        if (innerMap == null) {
            throw new IllegalArgumentException("ObservedProperty has no property called " + property.toString());
        }
        return innerMap.get(qPath.getClass()).get(qPath);
    }

    /**
     * Get a list of expressions for the given property and path. Add it to the
     * given list, or a new list.
     *
     * @param property The property to get expressions for.
     * @param qPath The path to get expressions for.
     * @param target The list to add to. If null a new list will be created.
     * @return The target list, or a new list if target was null.
     */
    public static List<Expression<?>> expressionsForProperty(EntityProperty property, Path<?> qPath, List< Expression<?>> target) {
        Map<Class, Map<String, ExpressionFactory>> innerMap = epMapMulti.get(property);
        if (innerMap == null) {
            throw new IllegalArgumentException("ObservedProperty has no property called " + property.toString());
        }
        Map<String, ExpressionFactory> coreMap = innerMap.get(qPath.getClass());
        if (target == null) {
            target = new ArrayList<>();
        }
        for (Map.Entry<String, ExpressionFactory> es : coreMap.entrySet()) {
            target.add(es.getValue().get(qPath));
        }
        return target;
    }

    /**
     * Get a Map of expressions for the given property and path. Add it to the
     * given Map, or a new Map.
     *
     * @param property The property to get expressions for.
     * @param qPath The path to get expressions for.
     * @param target The Map to add to. If null a new Map will be created.
     * @return The target Map, or a new Map if target was null.
     */
    public static Map<String, Expression<?>> expressionsForProperty(EntityProperty property, Path<?> qPath, Map<String, Expression<?>> target) {
        Map<Class, Map<String, ExpressionFactory>> innerMap = epMapMulti.get(property);
        if (innerMap == null) {
            throw new IllegalArgumentException("We do not know any property called " + property.toString());
        }
        Map<String, ExpressionFactory> coreMap = innerMap.get(qPath.getClass());
        if (coreMap == null) {
            throw new IllegalArgumentException("No property called " + property.toString() + " for " + qPath.getClass());
        }
        if (target == null) {
            target = new LinkedHashMap<>();
        }
        for (Map.Entry<String, ExpressionFactory> es : coreMap.entrySet()) {
            target.put(es.getKey(), es.getValue().get(qPath));
        }
        return target;
    }

    private static void addEntry(Property p, Class c, ExpressionFactory f) {
        addEntrySingle(p, c, f);
        addEntryMulti(p, c, null, f);
        addToAll(c, f);
    }

    private static void addEntry(Property p, Class c, String name, ExpressionFactory f) {
        addEntrySingle(p, c, f);
        addEntryMulti(p, c, name, f);
        addToAll(c, f);
    }

    private static void addToAll(Class c, ExpressionFactory f) {
        List<ExpressionFactory> list = allForClass.get(c);
        if (list == null) {
            list = new ArrayList<>();
            allForClass.put(c, list);
        }
        list.add(f);
    }

    private static void addEntrySingle(Property p, Class c, ExpressionFactory f) {
        Map<Class, ExpressionFactory> innerMap = epMapSingle.get(p);
        if (innerMap == null) {
            innerMap = new HashMap<>();
            epMapSingle.put(p, innerMap);
        }
        if (innerMap.containsKey(c)) {
            LOGGER.trace("Class {} already has a registration for {}.", c.getName(), p);
            return;
        }
        innerMap.put(c, f);
    }

    private static void addEntryMulti(Property p, Class c, String name, ExpressionFactory f) {
        Map<Class, Map<String, ExpressionFactory>> innerMap = epMapMulti.get(p);
        if (innerMap == null) {
            innerMap = new HashMap<>();
            epMapMulti.put(p, innerMap);
        }
        Map<String, ExpressionFactory> coreMap = innerMap.get(c);
        if (coreMap == null) {
            coreMap = new LinkedHashMap<>();
            innerMap.put(c, coreMap);
        }
        if (name == null) {
            name = Integer.toString(coreMap.size());
        }
        coreMap.put(name, f);
    }
}
