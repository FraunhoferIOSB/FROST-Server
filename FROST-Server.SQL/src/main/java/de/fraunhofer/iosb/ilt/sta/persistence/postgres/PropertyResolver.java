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
import com.querydsl.core.types.dsl.SimpleExpression;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.NavigationProperty;
import de.fraunhofer.iosb.ilt.sta.path.Property;
import de.fraunhofer.iosb.ilt.sta.persistence.BasicPersistenceType;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.expression.StaTimeIntervalExpression.KEY_TIME_INTERVAL_END;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.expression.StaTimeIntervalExpression.KEY_TIME_INTERVAL_START;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author scf
 * @param <I> The type of path used for the ID fields.
 * @param <J> The type of the ID fields.
 */
public class PropertyResolver<I extends SimpleExpression<J> & Path<J>, J> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyResolver.class);

    private static interface ExpressionFactory<T> {

        Expression<?> get(T qPath);
    }

    private final Map<Property, Map<Class, ExpressionFactory>> epMapSingle = new HashMap<>();
    private final Map<Property, Map<Class, Map<String, ExpressionFactory>>> epMapMulti = new HashMap<>();
    private final Map<Class, List<ExpressionFactory>> allForClass = new HashMap<>();

    public final QCollection<I, J> qCollection;
    private final BasicPersistenceType basicPersistenceType;

    public PropertyResolver(EntityFactories<I, J> entityFactories, BasicPersistenceType basicPersistenceType) {
        this.qCollection = entityFactories.qCollection;
        this.basicPersistenceType = basicPersistenceType;
        init();
    }

    private void init() {
        Class<? extends AbstractQDatastreams> qDatastreamsClass = qCollection.qDatastreams.getClass();
        addEntry(EntityProperty.ID, qDatastreamsClass, (ExpressionFactory<AbstractQDatastreams>) AbstractQDatastreams::getId);
        addEntry(EntityProperty.SELFLINK, qDatastreamsClass, (ExpressionFactory<AbstractQDatastreams>) AbstractQDatastreams::getId);
        addEntry(EntityProperty.NAME, qDatastreamsClass, (ExpressionFactory<AbstractQDatastreams>) (AbstractQDatastreams qPath) -> qPath.name);
        addEntry(EntityProperty.DESCRIPTION, qDatastreamsClass, (ExpressionFactory<AbstractQDatastreams>) (AbstractQDatastreams qPath) -> qPath.description);
        addEntry(EntityProperty.OBSERVATIONTYPE, qDatastreamsClass, (ExpressionFactory<AbstractQDatastreams>) (AbstractQDatastreams qPath) -> qPath.observationType);
        addEntry(EntityProperty.OBSERVEDAREA, qDatastreamsClass, (ExpressionFactory<AbstractQDatastreams>) (AbstractQDatastreams qPath) -> qPath.observedArea.asText());
        addEntry(EntityProperty.PHENOMENONTIME, qDatastreamsClass, KEY_TIME_INTERVAL_START, (ExpressionFactory<AbstractQDatastreams>) (AbstractQDatastreams qPath) -> qPath.phenomenonTimeStart);
        addEntry(EntityProperty.PHENOMENONTIME, qDatastreamsClass, KEY_TIME_INTERVAL_END, (ExpressionFactory<AbstractQDatastreams>) (AbstractQDatastreams qPath) -> qPath.phenomenonTimeEnd);
        addEntry(EntityProperty.PROPERTIES, qDatastreamsClass, (ExpressionFactory<AbstractQDatastreams>) (AbstractQDatastreams qPath) -> qPath.properties);
        addEntry(EntityProperty.RESULTTIME, qDatastreamsClass, KEY_TIME_INTERVAL_START, (ExpressionFactory<AbstractQDatastreams>) (AbstractQDatastreams qPath) -> qPath.resultTimeStart);
        addEntry(EntityProperty.RESULTTIME, qDatastreamsClass, KEY_TIME_INTERVAL_END, (ExpressionFactory<AbstractQDatastreams>) (AbstractQDatastreams qPath) -> qPath.resultTimeEnd);
        addEntry(EntityProperty.UNITOFMEASUREMENT, qDatastreamsClass, "definition", (ExpressionFactory<AbstractQDatastreams>) (AbstractQDatastreams qPath) -> qPath.unitDefinition);
        addEntry(EntityProperty.UNITOFMEASUREMENT, qDatastreamsClass, "name", (ExpressionFactory<AbstractQDatastreams>) (AbstractQDatastreams qPath) -> qPath.unitName);
        addEntry(EntityProperty.UNITOFMEASUREMENT, qDatastreamsClass, "symbol", (ExpressionFactory<AbstractQDatastreams>) (AbstractQDatastreams qPath) -> qPath.unitSymbol);
        addEntry(NavigationProperty.SENSOR, qDatastreamsClass, (ExpressionFactory<AbstractQDatastreams>) AbstractQDatastreams::getSensorId);
        addEntry(NavigationProperty.OBSERVEDPROPERTY, qDatastreamsClass, (ExpressionFactory<AbstractQDatastreams>) AbstractQDatastreams::getObsPropertyId);
        addEntry(NavigationProperty.THING, qDatastreamsClass, (ExpressionFactory<AbstractQDatastreams>) AbstractQDatastreams::getThingId);

        Class<? extends AbstractQMultiDatastreams> qMultiDatastreamsClass = qCollection.qMultiDatastreams.getClass();
        addEntry(EntityProperty.ID, qMultiDatastreamsClass, (ExpressionFactory<AbstractQMultiDatastreams>) AbstractQMultiDatastreams::getId);
        addEntry(EntityProperty.SELFLINK, qMultiDatastreamsClass, (ExpressionFactory<AbstractQMultiDatastreams>) AbstractQMultiDatastreams::getId);
        addEntry(EntityProperty.NAME, qMultiDatastreamsClass, (ExpressionFactory<AbstractQMultiDatastreams>) (AbstractQMultiDatastreams qPath) -> qPath.name);
        addEntry(EntityProperty.DESCRIPTION, qMultiDatastreamsClass, (ExpressionFactory<AbstractQMultiDatastreams>) (AbstractQMultiDatastreams qPath) -> qPath.description);
        addEntry(EntityProperty.MULTIOBSERVATIONDATATYPES, qMultiDatastreamsClass, (ExpressionFactory<AbstractQMultiDatastreams>) (AbstractQMultiDatastreams qPath) -> qPath.observationTypes);
        addEntry(EntityProperty.OBSERVEDAREA, qMultiDatastreamsClass, (ExpressionFactory<AbstractQMultiDatastreams>) (AbstractQMultiDatastreams qPath) -> qPath.observedArea.asText());
        addEntry(EntityProperty.PHENOMENONTIME, qMultiDatastreamsClass, KEY_TIME_INTERVAL_START, (ExpressionFactory<AbstractQMultiDatastreams>) (AbstractQMultiDatastreams qPath) -> qPath.phenomenonTimeStart);
        addEntry(EntityProperty.PHENOMENONTIME, qMultiDatastreamsClass, KEY_TIME_INTERVAL_END, (ExpressionFactory<AbstractQMultiDatastreams>) (AbstractQMultiDatastreams qPath) -> qPath.phenomenonTimeEnd);
        addEntry(EntityProperty.PROPERTIES, qMultiDatastreamsClass, (ExpressionFactory<AbstractQMultiDatastreams>) (AbstractQMultiDatastreams qPath) -> qPath.properties);
        addEntry(EntityProperty.RESULTTIME, qMultiDatastreamsClass, KEY_TIME_INTERVAL_START, (ExpressionFactory<AbstractQMultiDatastreams>) (AbstractQMultiDatastreams qPath) -> qPath.resultTimeStart);
        addEntry(EntityProperty.RESULTTIME, qMultiDatastreamsClass, KEY_TIME_INTERVAL_END, (ExpressionFactory<AbstractQMultiDatastreams>) (AbstractQMultiDatastreams qPath) -> qPath.resultTimeEnd);
        addEntry(EntityProperty.UNITOFMEASUREMENTS, qMultiDatastreamsClass, (ExpressionFactory<AbstractQMultiDatastreams>) (AbstractQMultiDatastreams qPath) -> qPath.unitOfMeasurements);
        addEntry(NavigationProperty.SENSOR, qMultiDatastreamsClass, (ExpressionFactory<AbstractQMultiDatastreams>) AbstractQMultiDatastreams::getSensorId);
        addEntry(NavigationProperty.THING, qMultiDatastreamsClass, (ExpressionFactory<AbstractQMultiDatastreams>) AbstractQMultiDatastreams::getThingId);

        Class<? extends AbstractQFeatures> qFeaturesClass = qCollection.qFeatures.getClass();
        addEntry(EntityProperty.ID, qFeaturesClass, (ExpressionFactory<AbstractQFeatures>) AbstractQFeatures::getId);
        addEntry(EntityProperty.SELFLINK, qFeaturesClass, (ExpressionFactory<AbstractQFeatures>) AbstractQFeatures::getId);
        addEntry(EntityProperty.NAME, qFeaturesClass, (ExpressionFactory<AbstractQFeatures>) (AbstractQFeatures qPath) -> qPath.name);
        addEntry(EntityProperty.DESCRIPTION, qFeaturesClass, (ExpressionFactory<AbstractQFeatures>) (AbstractQFeatures qPath) -> qPath.description);
        addEntry(EntityProperty.ENCODINGTYPE, qFeaturesClass, (ExpressionFactory<AbstractQFeatures>) (AbstractQFeatures qPath) -> qPath.encodingType);
        addEntry(EntityProperty.FEATURE, qFeaturesClass, "j", (ExpressionFactory<AbstractQFeatures>) (AbstractQFeatures qPath) -> qPath.feature);
        addEntry(EntityProperty.FEATURE, qFeaturesClass, "g", (ExpressionFactory<AbstractQFeatures>) (AbstractQFeatures qPath) -> qPath.geom);
        addEntry(EntityProperty.PROPERTIES, qFeaturesClass, (ExpressionFactory<AbstractQFeatures>) (AbstractQFeatures qPath) -> qPath.properties);

        Class<? extends AbstractQHistLocations> qHistLocationsClass = qCollection.qHistLocations.getClass();
        addEntry(EntityProperty.ID, qHistLocationsClass, (ExpressionFactory<AbstractQHistLocations>) AbstractQHistLocations::getId);
        addEntry(EntityProperty.SELFLINK, qHistLocationsClass, (ExpressionFactory<AbstractQHistLocations>) AbstractQHistLocations::getId);
        addEntry(EntityProperty.TIME, qHistLocationsClass, (ExpressionFactory<AbstractQHistLocations>) (AbstractQHistLocations qPath) -> qPath.time);
        addEntry(NavigationProperty.THING, qHistLocationsClass, (ExpressionFactory<AbstractQHistLocations>) AbstractQHistLocations::getThingId);

        Class<? extends AbstractQLocations> qLocationsClass = qCollection.qLocations.getClass();
        addEntry(EntityProperty.ID, qLocationsClass, (ExpressionFactory<AbstractQLocations>) AbstractQLocations::getId);
        addEntry(EntityProperty.SELFLINK, qLocationsClass, (ExpressionFactory<AbstractQLocations>) AbstractQLocations::getId);
        addEntry(EntityProperty.NAME, qLocationsClass, (ExpressionFactory<AbstractQLocations>) (AbstractQLocations qPath) -> qPath.name);
        addEntry(EntityProperty.DESCRIPTION, qLocationsClass, (ExpressionFactory<AbstractQLocations>) (AbstractQLocations qPath) -> qPath.description);
        addEntry(EntityProperty.ENCODINGTYPE, qLocationsClass, (ExpressionFactory<AbstractQLocations>) (AbstractQLocations qPath) -> qPath.encodingType);
        addEntry(EntityProperty.LOCATION, qLocationsClass, "j", (ExpressionFactory<AbstractQLocations>) (AbstractQLocations qPath) -> qPath.location);
        addEntry(EntityProperty.LOCATION, qLocationsClass, "g", (ExpressionFactory<AbstractQLocations>) (AbstractQLocations qPath) -> qPath.geom);
        addEntry(EntityProperty.PROPERTIES, qLocationsClass, (ExpressionFactory<AbstractQLocations>) (AbstractQLocations qPath) -> qPath.properties);

        Class<? extends AbstractQObsProperties> qObsPropertiesClass = qCollection.qObsProperties.getClass();
        addEntry(EntityProperty.ID, qObsPropertiesClass, (ExpressionFactory<AbstractQObsProperties>) AbstractQObsProperties::getId);
        addEntry(EntityProperty.SELFLINK, qObsPropertiesClass, (ExpressionFactory<AbstractQObsProperties>) AbstractQObsProperties::getId);
        addEntry(EntityProperty.DEFINITION, qObsPropertiesClass, (ExpressionFactory<AbstractQObsProperties>) (AbstractQObsProperties qPath) -> qPath.definition);
        addEntry(EntityProperty.DESCRIPTION, qObsPropertiesClass, (ExpressionFactory<AbstractQObsProperties>) (AbstractQObsProperties qPath) -> qPath.description);
        addEntry(EntityProperty.NAME, qObsPropertiesClass, (ExpressionFactory<AbstractQObsProperties>) (AbstractQObsProperties qPath) -> qPath.name);
        addEntry(EntityProperty.PROPERTIES, qObsPropertiesClass, (ExpressionFactory<AbstractQObsProperties>) (AbstractQObsProperties qPath) -> qPath.properties);

        Class<? extends AbstractQObservations> qObservationsClass = qCollection.qObservations.getClass();
        addEntry(EntityProperty.ID, qObservationsClass, (ExpressionFactory<AbstractQObservations>) AbstractQObservations::getId);
        addEntry(EntityProperty.SELFLINK, qObservationsClass, (ExpressionFactory<AbstractQObservations>) AbstractQObservations::getId);
        addEntry(EntityProperty.PARAMETERS, qObservationsClass, (ExpressionFactory<AbstractQObservations>) (AbstractQObservations qPath) -> qPath.parameters);
        addEntry(EntityProperty.PHENOMENONTIME, qObservationsClass, KEY_TIME_INTERVAL_START, (ExpressionFactory<AbstractQObservations>) (AbstractQObservations qPath) -> qPath.phenomenonTimeStart);
        addEntry(EntityProperty.PHENOMENONTIME, qObservationsClass, KEY_TIME_INTERVAL_END, (ExpressionFactory<AbstractQObservations>) (AbstractQObservations qPath) -> qPath.phenomenonTimeEnd);
        addEntry(EntityProperty.RESULT, qObservationsClass, "n", (ExpressionFactory<AbstractQObservations>) (AbstractQObservations qPath) -> qPath.resultNumber);
        addEntry(EntityProperty.RESULT, qObservationsClass, "b", (ExpressionFactory<AbstractQObservations>) (AbstractQObservations qPath) -> qPath.resultBoolean);
        addEntry(EntityProperty.RESULT, qObservationsClass, "s", (ExpressionFactory<AbstractQObservations>) (AbstractQObservations qPath) -> qPath.resultString);
        addEntry(EntityProperty.RESULT, qObservationsClass, "j", (ExpressionFactory<AbstractQObservations>) (AbstractQObservations qPath) -> qPath.resultJson);
        addEntry(EntityProperty.RESULT, qObservationsClass, "t", (ExpressionFactory<AbstractQObservations>) (AbstractQObservations qPath) -> qPath.resultType);
        addEntry(EntityProperty.RESULTQUALITY, qObservationsClass, (ExpressionFactory<AbstractQObservations>) (AbstractQObservations qPath) -> qPath.resultQuality);
        addEntry(EntityProperty.RESULTTIME, qObservationsClass, (ExpressionFactory<AbstractQObservations>) (AbstractQObservations qPath) -> qPath.resultTime);
        addEntry(EntityProperty.VALIDTIME, qObservationsClass, KEY_TIME_INTERVAL_START, (ExpressionFactory<AbstractQObservations>) (AbstractQObservations qPath) -> qPath.validTimeStart);
        addEntry(EntityProperty.VALIDTIME, qObservationsClass, KEY_TIME_INTERVAL_END, (ExpressionFactory<AbstractQObservations>) (AbstractQObservations qPath) -> qPath.validTimeEnd);
        addEntry(NavigationProperty.FEATUREOFINTEREST, qObservationsClass, (ExpressionFactory<AbstractQObservations>) AbstractQObservations::getFeatureId);
        addEntry(NavigationProperty.DATASTREAM, qObservationsClass, (ExpressionFactory<AbstractQObservations>) AbstractQObservations::getDatastreamId);
        addEntry(NavigationProperty.MULTIDATASTREAM, qObservationsClass, (ExpressionFactory<AbstractQObservations>) AbstractQObservations::getMultiDatastreamId);

        Class<? extends AbstractQSensors> qSensorsClass = qCollection.qSensors.getClass();
        addEntry(EntityProperty.ID, qSensorsClass, (ExpressionFactory<AbstractQSensors>) AbstractQSensors::getId);
        addEntry(EntityProperty.SELFLINK, qSensorsClass, (ExpressionFactory<AbstractQSensors>) AbstractQSensors::getId);
        addEntry(EntityProperty.NAME, qSensorsClass, (ExpressionFactory<AbstractQSensors>) (AbstractQSensors qPath) -> qPath.name);
        addEntry(EntityProperty.DESCRIPTION, qSensorsClass, (ExpressionFactory<AbstractQSensors>) (AbstractQSensors qPath) -> qPath.description);
        addEntry(EntityProperty.ENCODINGTYPE, qSensorsClass, (ExpressionFactory<AbstractQSensors>) (AbstractQSensors qPath) -> qPath.encodingType);
        addEntry(EntityProperty.METADATA, qSensorsClass, (ExpressionFactory<AbstractQSensors>) (AbstractQSensors qPath) -> qPath.metadata);
        addEntry(EntityProperty.PROPERTIES, qSensorsClass, (ExpressionFactory<AbstractQSensors>) (AbstractQSensors qPath) -> qPath.properties);

        Class<? extends AbstractQThings> qThingsClass = qCollection.qThings.getClass();
        addEntry(EntityProperty.ID, qThingsClass, (ExpressionFactory<AbstractQThings>) AbstractQThings::getId);
        addEntry(EntityProperty.SELFLINK, qThingsClass, (ExpressionFactory<AbstractQThings>) AbstractQThings::getId);
        addEntry(EntityProperty.NAME, qThingsClass, (ExpressionFactory<AbstractQThings>) (AbstractQThings qPath) -> qPath.name);
        addEntry(EntityProperty.DESCRIPTION, qThingsClass, (ExpressionFactory<AbstractQThings>) (AbstractQThings qPath) -> qPath.description);
        addEntry(EntityProperty.PROPERTIES, qThingsClass, (ExpressionFactory<AbstractQThings>) (AbstractQThings qPath) -> qPath.properties);
    }

    public BasicPersistenceType getBasicPersistenceType() {
        return basicPersistenceType;
    }

    /**
     *
     * @param qPath The path to get expressions for.
     * @param target The list to add to. If null a new list will be created.
     * @return The target list, or a new list if target was null.
     */
    public Collection<Expression<?>> expressionsForClass(Path<?> qPath, Collection<Expression<?>> target) {
        List<ExpressionFactory> list = allForClass.get(qPath.getClass());
        if (target == null) {
            target = new ArrayList<>();
        }
        for (ExpressionFactory f : list) {
            target.add(f.get(qPath));
        }
        return target;
    }

    public Expression<?> expressionForProperty(EntityProperty property, Path<?> qPath) {
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
    public Collection<Expression<?>> expressionsForProperty(Property property, Path<?> qPath, Collection<Expression<?>> target) {
        Map<Class, Map<String, ExpressionFactory>> innerMap = epMapMulti.get(property);
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
    public Map<String, Expression<?>> expressionsForProperty(EntityProperty property, Path<?> qPath, Map<String, Expression<?>> target) {
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

    /**
     * Get the set of expressions for the given set of selected properties.
     *
     * @param qPath The entity path to get the expressions for.
     * @param selectedProperties The set of properties to get the expressions
     * of.
     * @return The set of expressions.
     */
    public Expression<?>[] getExpressions(Path<?> qPath, Set<Property> selectedProperties) {
        Set<Expression<?>> exprSet = new HashSet<>();
        if (selectedProperties.isEmpty()) {
            expressionsForClass(qPath, exprSet);
        } else {
            for (Property property : selectedProperties) {
                expressionsForProperty(property, qPath, exprSet);
            }
        }
        return exprSet.toArray(new Expression<?>[exprSet.size()]);
    }

    private void addEntry(Property property, Class clazz, ExpressionFactory factory) {
        addEntrySingle(property, clazz, factory);
        addEntryMulti(property, clazz, null, factory);
        addToAll(clazz, factory);
    }

    private void addEntry(Property property, Class clazz, String name, ExpressionFactory factory) {
        addEntrySingle(property, clazz, factory);
        addEntryMulti(property, clazz, name, factory);
        addToAll(clazz, factory);
    }

    private void addToAll(Class clazz, ExpressionFactory factory) {
        List<ExpressionFactory> list = allForClass.computeIfAbsent(
                clazz,
                k -> new ArrayList<>()
        );
        list.add(factory);
    }

    private void addEntrySingle(Property property, Class clazz, ExpressionFactory factory) {
        Map<Class, ExpressionFactory> innerMap = epMapSingle.computeIfAbsent(
                property,
                k -> new HashMap<>()
        );
        if (innerMap.containsKey(clazz)) {
            LOGGER.trace("Class {} already has a registration for {}.", clazz.getName(), property);
            return;
        }
        innerMap.put(clazz, factory);
    }

    private void addEntryMulti(Property property, Class clazz, String name, ExpressionFactory factory) {
        Map<Class, Map<String, ExpressionFactory>> innerMap = epMapMulti.computeIfAbsent(
                property,
                k -> new HashMap<>()
        );
        Map<String, ExpressionFactory> coreMap = innerMap.computeIfAbsent(
                clazz,
                k -> new LinkedHashMap<>()
        );
        if (name == null) {
            name = Integer.toString(coreMap.size());
        }
        coreMap.put(name, factory);
    }
}
