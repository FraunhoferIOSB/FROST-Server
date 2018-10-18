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
package de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.NavigationProperty;
import de.fraunhofer.iosb.ilt.sta.path.Property;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.expression.StaTimeIntervalExpression.KEY_TIME_INTERVAL_END;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.expression.StaTimeIntervalExpression.KEY_TIME_INTERVAL_START;
import java.util.ArrayList;
import java.util.Collection;
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

    private PropertyResolver() {
        // Utility class, not to be instantiated.
    }

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyResolver.class);

    private static interface ExpressionFactory<T> {

        Expression<?> get(T qPath);
    }

    private static final Map<Property, Map<Class, ExpressionFactory>> EP_MAP_SINGLE = new HashMap<>();
    private static final Map<Property, Map<Class, Map<String, ExpressionFactory>>> EP_MAP_MULTI = new HashMap<>();
    private static final Map<Class, List<ExpressionFactory>> ALL_FOR_CLASS = new HashMap<>();

    static {
        addEntry(EntityProperty.ID, QDatastreams.class, (ExpressionFactory<QDatastreams>) (QDatastreams qPath) -> qPath.id);
        addEntry(EntityProperty.SELFLINK, QDatastreams.class, (ExpressionFactory<QDatastreams>) (QDatastreams qPath) -> qPath.id);
        addEntry(EntityProperty.NAME, QDatastreams.class, (ExpressionFactory<QDatastreams>) (QDatastreams qPath) -> qPath.name);
        addEntry(EntityProperty.DESCRIPTION, QDatastreams.class, (ExpressionFactory<QDatastreams>) (QDatastreams qPath) -> qPath.description);
        addEntry(EntityProperty.OBSERVATIONTYPE, QDatastreams.class, (ExpressionFactory<QDatastreams>) (QDatastreams qPath) -> qPath.observationType);
        addEntry(EntityProperty.OBSERVEDAREA, QDatastreams.class, (ExpressionFactory<QDatastreams>) (QDatastreams qPath) -> qPath.observedArea.asText());
        addEntry(EntityProperty.PHENOMENONTIME, QDatastreams.class, KEY_TIME_INTERVAL_START, (ExpressionFactory<QDatastreams>) (QDatastreams qPath) -> qPath.phenomenonTimeStart);
        addEntry(EntityProperty.PHENOMENONTIME, QDatastreams.class, KEY_TIME_INTERVAL_END, (ExpressionFactory<QDatastreams>) (QDatastreams qPath) -> qPath.phenomenonTimeEnd);
        addEntry(EntityProperty.PROPERTIES, QDatastreams.class, (ExpressionFactory<QDatastreams>) (QDatastreams qPath) -> qPath.properties);
        addEntry(EntityProperty.RESULTTIME, QDatastreams.class, KEY_TIME_INTERVAL_START, (ExpressionFactory<QDatastreams>) (QDatastreams qPath) -> qPath.resultTimeStart);
        addEntry(EntityProperty.RESULTTIME, QDatastreams.class, KEY_TIME_INTERVAL_END, (ExpressionFactory<QDatastreams>) (QDatastreams qPath) -> qPath.resultTimeEnd);
        addEntry(EntityProperty.UNITOFMEASUREMENT, QDatastreams.class, "definition", (ExpressionFactory<QDatastreams>) (QDatastreams qPath) -> qPath.unitDefinition);
        addEntry(EntityProperty.UNITOFMEASUREMENT, QDatastreams.class, "name", (ExpressionFactory<QDatastreams>) (QDatastreams qPath) -> qPath.unitName);
        addEntry(EntityProperty.UNITOFMEASUREMENT, QDatastreams.class, "symbol", (ExpressionFactory<QDatastreams>) (QDatastreams qPath) -> qPath.unitSymbol);
        addEntry(NavigationProperty.SENSOR, QDatastreams.class, (ExpressionFactory<QDatastreams>) (QDatastreams qPath) -> qPath.sensorId);
        addEntry(NavigationProperty.OBSERVEDPROPERTY, QDatastreams.class, (ExpressionFactory<QDatastreams>) (QDatastreams qPath) -> qPath.obsPropertyId);
        addEntry(NavigationProperty.THING, QDatastreams.class, (ExpressionFactory<QDatastreams>) (QDatastreams qPath) -> qPath.thingId);

        addEntry(EntityProperty.ID, QMultiDatastreams.class, (ExpressionFactory<QMultiDatastreams>) (QMultiDatastreams qPath) -> qPath.id);
        addEntry(EntityProperty.SELFLINK, QMultiDatastreams.class, (ExpressionFactory<QMultiDatastreams>) (QMultiDatastreams qPath) -> qPath.id);
        addEntry(EntityProperty.NAME, QMultiDatastreams.class, (ExpressionFactory<QMultiDatastreams>) (QMultiDatastreams qPath) -> qPath.name);
        addEntry(EntityProperty.DESCRIPTION, QMultiDatastreams.class, (ExpressionFactory<QMultiDatastreams>) (QMultiDatastreams qPath) -> qPath.description);
        addEntry(EntityProperty.MULTIOBSERVATIONDATATYPES, QMultiDatastreams.class, (ExpressionFactory<QMultiDatastreams>) (QMultiDatastreams qPath) -> qPath.observationTypes);
        addEntry(EntityProperty.OBSERVEDAREA, QMultiDatastreams.class, (ExpressionFactory<QMultiDatastreams>) (QMultiDatastreams qPath) -> qPath.observedArea.asText());
        addEntry(EntityProperty.PHENOMENONTIME, QMultiDatastreams.class, KEY_TIME_INTERVAL_START, (ExpressionFactory<QMultiDatastreams>) (QMultiDatastreams qPath) -> qPath.phenomenonTimeStart);
        addEntry(EntityProperty.PHENOMENONTIME, QMultiDatastreams.class, KEY_TIME_INTERVAL_END, (ExpressionFactory<QMultiDatastreams>) (QMultiDatastreams qPath) -> qPath.phenomenonTimeEnd);
        addEntry(EntityProperty.PROPERTIES, QMultiDatastreams.class, (ExpressionFactory<QMultiDatastreams>) (QMultiDatastreams qPath) -> qPath.properties);
        addEntry(EntityProperty.RESULTTIME, QMultiDatastreams.class, KEY_TIME_INTERVAL_START, (ExpressionFactory<QMultiDatastreams>) (QMultiDatastreams qPath) -> qPath.resultTimeStart);
        addEntry(EntityProperty.RESULTTIME, QMultiDatastreams.class, KEY_TIME_INTERVAL_END, (ExpressionFactory<QMultiDatastreams>) (QMultiDatastreams qPath) -> qPath.resultTimeEnd);
        addEntry(EntityProperty.UNITOFMEASUREMENTS, QMultiDatastreams.class, (ExpressionFactory<QMultiDatastreams>) (QMultiDatastreams qPath) -> qPath.unitOfMeasurements);
        addEntry(NavigationProperty.SENSOR, QMultiDatastreams.class, (ExpressionFactory<QMultiDatastreams>) (QMultiDatastreams qPath) -> qPath.sensorId);
        addEntry(NavigationProperty.THING, QMultiDatastreams.class, (ExpressionFactory<QMultiDatastreams>) (QMultiDatastreams qPath) -> qPath.thingId);

        addEntry(EntityProperty.ID, QFeatures.class, (ExpressionFactory<QFeatures>) (QFeatures qPath) -> qPath.id);
        addEntry(EntityProperty.SELFLINK, QFeatures.class, (ExpressionFactory<QFeatures>) (QFeatures qPath) -> qPath.id);
        addEntry(EntityProperty.NAME, QFeatures.class, (ExpressionFactory<QFeatures>) (QFeatures qPath) -> qPath.name);
        addEntry(EntityProperty.DESCRIPTION, QFeatures.class, (ExpressionFactory<QFeatures>) (QFeatures qPath) -> qPath.description);
        addEntry(EntityProperty.ENCODINGTYPE, QFeatures.class, (ExpressionFactory<QFeatures>) (QFeatures qPath) -> qPath.encodingType);
        addEntry(EntityProperty.FEATURE, QFeatures.class, "j", (ExpressionFactory<QFeatures>) (QFeatures qPath) -> qPath.feature);
        addEntry(EntityProperty.FEATURE, QFeatures.class, "g", (ExpressionFactory<QFeatures>) (QFeatures qPath) -> qPath.geom);
        addEntry(EntityProperty.PROPERTIES, QFeatures.class, (ExpressionFactory<QFeatures>) (QFeatures qPath) -> qPath.properties);

        addEntry(EntityProperty.ID, QHistLocations.class, (ExpressionFactory<QHistLocations>) (QHistLocations qPath) -> qPath.id);
        addEntry(EntityProperty.SELFLINK, QHistLocations.class, (ExpressionFactory<QHistLocations>) (QHistLocations qPath) -> qPath.id);
        addEntry(EntityProperty.TIME, QHistLocations.class, (ExpressionFactory<QHistLocations>) (QHistLocations qPath) -> qPath.time);
        addEntry(NavigationProperty.THING, QHistLocations.class, (ExpressionFactory<QHistLocations>) (QHistLocations qPath) -> qPath.thingId);

        addEntry(EntityProperty.ID, QLocations.class, (ExpressionFactory<QLocations>) (QLocations qPath) -> qPath.id);
        addEntry(EntityProperty.SELFLINK, QLocations.class, (ExpressionFactory<QLocations>) (QLocations qPath) -> qPath.id);
        addEntry(EntityProperty.NAME, QLocations.class, (ExpressionFactory<QLocations>) (QLocations qPath) -> qPath.name);
        addEntry(EntityProperty.DESCRIPTION, QLocations.class, (ExpressionFactory<QLocations>) (QLocations qPath) -> qPath.description);
        addEntry(EntityProperty.ENCODINGTYPE, QLocations.class, (ExpressionFactory<QLocations>) (QLocations qPath) -> qPath.encodingType);
        addEntry(EntityProperty.LOCATION, QLocations.class, "j", (ExpressionFactory<QLocations>) (QLocations qPath) -> qPath.location);
        addEntry(EntityProperty.LOCATION, QLocations.class, "g", (ExpressionFactory<QLocations>) (QLocations qPath) -> qPath.geom);
        addEntry(EntityProperty.PROPERTIES, QLocations.class, (ExpressionFactory<QLocations>) (QLocations qPath) -> qPath.properties);

        addEntry(EntityProperty.ID, QObsProperties.class, (ExpressionFactory<QObsProperties>) (QObsProperties qPath) -> qPath.id);
        addEntry(EntityProperty.SELFLINK, QObsProperties.class, (ExpressionFactory<QObsProperties>) (QObsProperties qPath) -> qPath.id);
        addEntry(EntityProperty.DEFINITION, QObsProperties.class, (ExpressionFactory<QObsProperties>) (QObsProperties qPath) -> qPath.definition);
        addEntry(EntityProperty.DESCRIPTION, QObsProperties.class, (ExpressionFactory<QObsProperties>) (QObsProperties qPath) -> qPath.description);
        addEntry(EntityProperty.NAME, QObsProperties.class, (ExpressionFactory<QObsProperties>) (QObsProperties qPath) -> qPath.name);
        addEntry(EntityProperty.PROPERTIES, QObsProperties.class, (ExpressionFactory<QObsProperties>) (QObsProperties qPath) -> qPath.properties);

        addEntry(EntityProperty.ID, QObservations.class, (ExpressionFactory<QObservations>) (QObservations qPath) -> qPath.id);
        addEntry(EntityProperty.SELFLINK, QObservations.class, (ExpressionFactory<QObservations>) (QObservations qPath) -> qPath.id);
        addEntry(EntityProperty.PARAMETERS, QObservations.class, (ExpressionFactory<QObservations>) (QObservations qPath) -> qPath.parameters);
        addEntry(EntityProperty.PHENOMENONTIME, QObservations.class, KEY_TIME_INTERVAL_START, (ExpressionFactory<QObservations>) (QObservations qPath) -> qPath.phenomenonTimeStart);
        addEntry(EntityProperty.PHENOMENONTIME, QObservations.class, KEY_TIME_INTERVAL_END, (ExpressionFactory<QObservations>) (QObservations qPath) -> qPath.phenomenonTimeEnd);
        addEntry(EntityProperty.RESULT, QObservations.class, "n", (ExpressionFactory<QObservations>) (QObservations qPath) -> qPath.resultNumber);
        addEntry(EntityProperty.RESULT, QObservations.class, "b", (ExpressionFactory<QObservations>) (QObservations qPath) -> qPath.resultBoolean);
        addEntry(EntityProperty.RESULT, QObservations.class, "s", (ExpressionFactory<QObservations>) (QObservations qPath) -> qPath.resultString);
        addEntry(EntityProperty.RESULT, QObservations.class, "j", (ExpressionFactory<QObservations>) (QObservations qPath) -> qPath.resultJson);
        addEntry(EntityProperty.RESULT, QObservations.class, "t", (ExpressionFactory<QObservations>) (QObservations qPath) -> qPath.resultType);
        addEntry(EntityProperty.RESULTQUALITY, QObservations.class, (ExpressionFactory<QObservations>) (QObservations qPath) -> qPath.resultQuality);
        addEntry(EntityProperty.RESULTTIME, QObservations.class, (ExpressionFactory<QObservations>) (QObservations qPath) -> qPath.resultTime);
        addEntry(EntityProperty.VALIDTIME, QObservations.class, KEY_TIME_INTERVAL_START, (ExpressionFactory<QObservations>) (QObservations qPath) -> qPath.validTimeStart);
        addEntry(EntityProperty.VALIDTIME, QObservations.class, KEY_TIME_INTERVAL_END, (ExpressionFactory<QObservations>) (QObservations qPath) -> qPath.validTimeEnd);
        addEntry(NavigationProperty.FEATUREOFINTEREST, QObservations.class, (ExpressionFactory<QObservations>) (QObservations qPath) -> qPath.featureId);
        addEntry(NavigationProperty.DATASTREAM, QObservations.class, (ExpressionFactory<QObservations>) (QObservations qPath) -> qPath.datastreamId);
        addEntry(NavigationProperty.MULTIDATASTREAM, QObservations.class, (ExpressionFactory<QObservations>) (QObservations qPath) -> qPath.multiDatastreamId);

        addEntry(EntityProperty.ID, QSensors.class, (ExpressionFactory<QSensors>) (QSensors qPath) -> qPath.id);
        addEntry(EntityProperty.SELFLINK, QSensors.class, (ExpressionFactory<QSensors>) (QSensors qPath) -> qPath.id);
        addEntry(EntityProperty.NAME, QSensors.class, (ExpressionFactory<QSensors>) (QSensors qPath) -> qPath.name);
        addEntry(EntityProperty.DESCRIPTION, QSensors.class, (ExpressionFactory<QSensors>) (QSensors qPath) -> qPath.description);
        addEntry(EntityProperty.ENCODINGTYPE, QSensors.class, (ExpressionFactory<QSensors>) (QSensors qPath) -> qPath.encodingType);
        addEntry(EntityProperty.METADATA, QSensors.class, (ExpressionFactory<QSensors>) (QSensors qPath) -> qPath.metadata);
        addEntry(EntityProperty.PROPERTIES, QSensors.class, (ExpressionFactory<QSensors>) (QSensors qPath) -> qPath.properties);

        addEntry(EntityProperty.ID, QThings.class, (ExpressionFactory<QThings>) (QThings qPath) -> qPath.id);
        addEntry(EntityProperty.SELFLINK, QThings.class, (ExpressionFactory<QThings>) (QThings qPath) -> qPath.id);
        addEntry(EntityProperty.NAME, QThings.class, (ExpressionFactory<QThings>) (QThings qPath) -> qPath.name);
        addEntry(EntityProperty.DESCRIPTION, QThings.class, (ExpressionFactory<QThings>) (QThings qPath) -> qPath.description);
        addEntry(EntityProperty.PROPERTIES, QThings.class, (ExpressionFactory<QThings>) (QThings qPath) -> qPath.properties);
    }

    /**
     *
     * @param qPath The path to get expressions for.
     * @param target The list to add to. If null a new list will be created.
     * @return The target list, or a new list if target was null.
     */
    public static Collection<Expression<?>> expressionsForClass(Path<?> qPath, Collection<Expression<?>> target) {
        List<ExpressionFactory> list = ALL_FOR_CLASS.get(qPath.getClass());
        if (target == null) {
            target = new ArrayList<>();
        }
        for (ExpressionFactory f : list) {
            target.add(f.get(qPath));
        }
        return target;
    }

    public static Expression<?> expressionForProperty(EntityProperty property, Path<?> qPath) {
        Map<Class, ExpressionFactory> innerMap = EP_MAP_SINGLE.get(property);
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
    public static Collection<Expression<?>> expressionsForProperty(Property property, Path<?> qPath, Collection<Expression<?>> target) {
        Map<Class, Map<String, ExpressionFactory>> innerMap = EP_MAP_MULTI.get(property);
        if (innerMap == null) {
            return target;
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
        Map<Class, Map<String, ExpressionFactory>> innerMap = EP_MAP_MULTI.get(property);
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
        List<ExpressionFactory> list = ALL_FOR_CLASS.get(c);
        if (list == null) {
            list = new ArrayList<>();
            ALL_FOR_CLASS.put(c, list);
        }
        list.add(f);
    }

    private static void addEntrySingle(Property p, Class c, ExpressionFactory f) {
        Map<Class, ExpressionFactory> innerMap = EP_MAP_SINGLE.get(p);
        if (innerMap == null) {
            innerMap = new HashMap<>();
            EP_MAP_SINGLE.put(p, innerMap);
        }
        if (innerMap.containsKey(c)) {
            LOGGER.trace("Class {} already has a registration for {}.", c.getName(), p);
            return;
        }
        innerMap.put(c, f);
    }

    private static void addEntryMulti(Property p, Class c, String name, ExpressionFactory f) {
        Map<Class, Map<String, ExpressionFactory>> innerMap = EP_MAP_MULTI.get(p);
        if (innerMap == null) {
            innerMap = new HashMap<>();
            EP_MAP_MULTI.put(p, innerMap);
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
