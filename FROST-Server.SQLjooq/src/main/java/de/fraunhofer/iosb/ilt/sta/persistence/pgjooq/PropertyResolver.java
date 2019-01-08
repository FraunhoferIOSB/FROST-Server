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
package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq;

import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.NavigationProperty;
import de.fraunhofer.iosb.ilt.sta.path.Property;
import de.fraunhofer.iosb.ilt.sta.persistence.BasicPersistenceType;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.expression.FieldWrapper;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.expression.SimpleFieldWrapper;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.expression.StaDateTimeExpression;
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.expression.StaTimeIntervalExpression.KEY_TIME_INTERVAL_END;
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.expression.StaTimeIntervalExpression.KEY_TIME_INTERVAL_START;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableFeatures;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableMultiDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableObsProperties;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableObservations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableSensors;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableThings;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.QCollection;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jooq.Field;
import org.jooq.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author scf
 * @param <J> The type of the ID fields.
 */
public class PropertyResolver<J> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyResolver.class);

    private static interface ExpressionFactory<T> {

        Field get(T qPath);
    }

    private final Map<Property, Map<Class, ExpressionFactory>> epMapSingle = new HashMap<>();
    private final Map<Property, Map<Class, Map<String, ExpressionFactory>>> epMapMulti = new HashMap<>();
    private final Map<Class, List<ExpressionFactory>> allForClass = new HashMap<>();

    public final QCollection<J> qCollection;
    private final BasicPersistenceType basicPersistenceType;

    public PropertyResolver(QCollection<J> qCollection, BasicPersistenceType basicPersistenceType) {
        this.qCollection = qCollection;
        this.basicPersistenceType = basicPersistenceType;
        init();
    }

    private void init() {
        Class<? extends Table> qDatastreamsClass = qCollection.qDatastreams.getClass();
        addEntry(EntityProperty.ID, qDatastreamsClass, (ExpressionFactory<AbstractTableDatastreams>) AbstractTableDatastreams::getId);
        addEntry(EntityProperty.SELFLINK, qDatastreamsClass, (ExpressionFactory<AbstractTableDatastreams>) AbstractTableDatastreams::getId);
        addEntry(EntityProperty.NAME, qDatastreamsClass, (ExpressionFactory<AbstractTableDatastreams>) (AbstractTableDatastreams qPath) -> qPath.name);
        addEntry(EntityProperty.DESCRIPTION, qDatastreamsClass, (ExpressionFactory<AbstractTableDatastreams>) (AbstractTableDatastreams qPath) -> qPath.description);
        addEntry(EntityProperty.OBSERVATIONTYPE, qDatastreamsClass, (ExpressionFactory<AbstractTableDatastreams>) (AbstractTableDatastreams qPath) -> qPath.observationType);
        addEntry(EntityProperty.OBSERVEDAREA, qDatastreamsClass, "s", (ExpressionFactory<AbstractTableDatastreams>) (AbstractTableDatastreams qPath) -> qPath.observedAreaText);
        addEntry(EntityProperty.OBSERVEDAREA, qDatastreamsClass, "g", (ExpressionFactory<AbstractTableDatastreams>) (AbstractTableDatastreams qPath) -> qPath.observedArea);
        addEntry(EntityProperty.PHENOMENONTIME, qDatastreamsClass, KEY_TIME_INTERVAL_START, (ExpressionFactory<AbstractTableDatastreams>) (AbstractTableDatastreams qPath) -> qPath.phenomenonTimeStart);
        addEntry(EntityProperty.PHENOMENONTIME, qDatastreamsClass, KEY_TIME_INTERVAL_END, (ExpressionFactory<AbstractTableDatastreams>) (AbstractTableDatastreams qPath) -> qPath.phenomenonTimeEnd);
        addEntry(EntityProperty.PROPERTIES, qDatastreamsClass, (ExpressionFactory<AbstractTableDatastreams>) (AbstractTableDatastreams qPath) -> qPath.properties);
        addEntry(EntityProperty.RESULTTIME, qDatastreamsClass, KEY_TIME_INTERVAL_START, (ExpressionFactory<AbstractTableDatastreams>) (AbstractTableDatastreams qPath) -> qPath.resultTimeStart);
        addEntry(EntityProperty.RESULTTIME, qDatastreamsClass, KEY_TIME_INTERVAL_END, (ExpressionFactory<AbstractTableDatastreams>) (AbstractTableDatastreams qPath) -> qPath.resultTimeEnd);
        addEntry(EntityProperty.UNITOFMEASUREMENT, qDatastreamsClass, "definition", (ExpressionFactory<AbstractTableDatastreams>) (AbstractTableDatastreams qPath) -> qPath.unitDefinition);
        addEntry(EntityProperty.UNITOFMEASUREMENT, qDatastreamsClass, "name", (ExpressionFactory<AbstractTableDatastreams>) (AbstractTableDatastreams qPath) -> qPath.unitName);
        addEntry(EntityProperty.UNITOFMEASUREMENT, qDatastreamsClass, "symbol", (ExpressionFactory<AbstractTableDatastreams>) (AbstractTableDatastreams qPath) -> qPath.unitSymbol);
        addEntry(NavigationProperty.SENSOR, qDatastreamsClass, (ExpressionFactory<AbstractTableDatastreams>) AbstractTableDatastreams::getSensorId);
        addEntry(NavigationProperty.OBSERVEDPROPERTY, qDatastreamsClass, (ExpressionFactory<AbstractTableDatastreams>) AbstractTableDatastreams::getObsPropertyId);
        addEntry(NavigationProperty.THING, qDatastreamsClass, (ExpressionFactory<AbstractTableDatastreams>) AbstractTableDatastreams::getThingId);

        Class<? extends Table> qMultiDatastreamsClass = qCollection.qMultiDatastreams.getClass();
        addEntry(EntityProperty.ID, qMultiDatastreamsClass, (ExpressionFactory<AbstractTableMultiDatastreams>) AbstractTableMultiDatastreams::getId);
        addEntry(EntityProperty.SELFLINK, qMultiDatastreamsClass, (ExpressionFactory<AbstractTableMultiDatastreams>) AbstractTableMultiDatastreams::getId);
        addEntry(EntityProperty.NAME, qMultiDatastreamsClass, (ExpressionFactory<AbstractTableMultiDatastreams>) (AbstractTableMultiDatastreams qPath) -> qPath.name);
        addEntry(EntityProperty.DESCRIPTION, qMultiDatastreamsClass, (ExpressionFactory<AbstractTableMultiDatastreams>) (AbstractTableMultiDatastreams qPath) -> qPath.description);
        addEntry(EntityProperty.MULTIOBSERVATIONDATATYPES, qMultiDatastreamsClass, (ExpressionFactory<AbstractTableMultiDatastreams>) (AbstractTableMultiDatastreams qPath) -> qPath.observationTypes);
        addEntry(EntityProperty.OBSERVEDAREA, qMultiDatastreamsClass, "s", (ExpressionFactory<AbstractTableMultiDatastreams>) (AbstractTableMultiDatastreams qPath) -> qPath.observedAreaText);
        addEntry(EntityProperty.OBSERVEDAREA, qMultiDatastreamsClass, "g", (ExpressionFactory<AbstractTableMultiDatastreams>) (AbstractTableMultiDatastreams qPath) -> qPath.observedArea);
        addEntry(EntityProperty.PHENOMENONTIME, qMultiDatastreamsClass, KEY_TIME_INTERVAL_START, (ExpressionFactory<AbstractTableMultiDatastreams>) (AbstractTableMultiDatastreams qPath) -> qPath.phenomenonTimeStart);
        addEntry(EntityProperty.PHENOMENONTIME, qMultiDatastreamsClass, KEY_TIME_INTERVAL_END, (ExpressionFactory<AbstractTableMultiDatastreams>) (AbstractTableMultiDatastreams qPath) -> qPath.phenomenonTimeEnd);
        addEntry(EntityProperty.PROPERTIES, qMultiDatastreamsClass, (ExpressionFactory<AbstractTableMultiDatastreams>) (AbstractTableMultiDatastreams qPath) -> qPath.properties);
        addEntry(EntityProperty.RESULTTIME, qMultiDatastreamsClass, KEY_TIME_INTERVAL_START, (ExpressionFactory<AbstractTableMultiDatastreams>) (AbstractTableMultiDatastreams qPath) -> qPath.resultTimeStart);
        addEntry(EntityProperty.RESULTTIME, qMultiDatastreamsClass, KEY_TIME_INTERVAL_END, (ExpressionFactory<AbstractTableMultiDatastreams>) (AbstractTableMultiDatastreams qPath) -> qPath.resultTimeEnd);
        addEntry(EntityProperty.UNITOFMEASUREMENTS, qMultiDatastreamsClass, (ExpressionFactory<AbstractTableMultiDatastreams>) (AbstractTableMultiDatastreams qPath) -> qPath.unitOfMeasurements);
        addEntry(NavigationProperty.SENSOR, qMultiDatastreamsClass, (ExpressionFactory<AbstractTableMultiDatastreams>) AbstractTableMultiDatastreams::getSensorId);
        addEntry(NavigationProperty.THING, qMultiDatastreamsClass, (ExpressionFactory<AbstractTableMultiDatastreams>) AbstractTableMultiDatastreams::getThingId);

        Class<? extends Table> qFeaturesClass = qCollection.qFeatures.getClass();
        addEntry(EntityProperty.ID, qFeaturesClass, (ExpressionFactory<AbstractTableFeatures>) AbstractTableFeatures::getId);
        addEntry(EntityProperty.SELFLINK, qFeaturesClass, (ExpressionFactory<AbstractTableFeatures>) AbstractTableFeatures::getId);
        addEntry(EntityProperty.NAME, qFeaturesClass, (ExpressionFactory<AbstractTableFeatures>) (AbstractTableFeatures qPath) -> qPath.name);
        addEntry(EntityProperty.DESCRIPTION, qFeaturesClass, (ExpressionFactory<AbstractTableFeatures>) (AbstractTableFeatures qPath) -> qPath.description);
        addEntry(EntityProperty.ENCODINGTYPE, qFeaturesClass, (ExpressionFactory<AbstractTableFeatures>) (AbstractTableFeatures qPath) -> qPath.encodingType);
        addEntry(EntityProperty.FEATURE, qFeaturesClass, "j", (ExpressionFactory<AbstractTableFeatures>) (AbstractTableFeatures qPath) -> qPath.feature);
        addEntry(EntityProperty.FEATURE, qFeaturesClass, "g", (ExpressionFactory<AbstractTableFeatures>) (AbstractTableFeatures qPath) -> qPath.geom);
        addEntry(EntityProperty.PROPERTIES, qFeaturesClass, (ExpressionFactory<AbstractTableFeatures>) (AbstractTableFeatures qPath) -> qPath.properties);

        Class<? extends Table> qHistLocationsClass = qCollection.qHistLocations.getClass();
        addEntry(EntityProperty.ID, qHistLocationsClass, (ExpressionFactory<AbstractTableHistLocations>) AbstractTableHistLocations::getId);
        addEntry(EntityProperty.SELFLINK, qHistLocationsClass, (ExpressionFactory<AbstractTableHistLocations>) AbstractTableHistLocations::getId);
        addEntry(EntityProperty.TIME, qHistLocationsClass, (ExpressionFactory<AbstractTableHistLocations>) (AbstractTableHistLocations qPath) -> qPath.time);
        addEntry(NavigationProperty.THING, qHistLocationsClass, (ExpressionFactory<AbstractTableHistLocations>) AbstractTableHistLocations::getThingId);

        Class<? extends Table> qLocationsClass = qCollection.qLocations.getClass();
        addEntry(EntityProperty.ID, qLocationsClass, (ExpressionFactory<AbstractTableLocations>) AbstractTableLocations::getId);
        addEntry(EntityProperty.SELFLINK, qLocationsClass, (ExpressionFactory<AbstractTableLocations>) AbstractTableLocations::getId);
        addEntry(EntityProperty.NAME, qLocationsClass, (ExpressionFactory<AbstractTableLocations>) (AbstractTableLocations qPath) -> qPath.name);
        addEntry(EntityProperty.DESCRIPTION, qLocationsClass, (ExpressionFactory<AbstractTableLocations>) (AbstractTableLocations qPath) -> qPath.description);
        addEntry(EntityProperty.ENCODINGTYPE, qLocationsClass, (ExpressionFactory<AbstractTableLocations>) (AbstractTableLocations qPath) -> qPath.encodingType);
        addEntry(EntityProperty.LOCATION, qLocationsClass, "j", (ExpressionFactory<AbstractTableLocations>) (AbstractTableLocations qPath) -> qPath.location);
        addEntry(EntityProperty.LOCATION, qLocationsClass, "g", (ExpressionFactory<AbstractTableLocations>) (AbstractTableLocations qPath) -> qPath.geom);
        addEntry(EntityProperty.PROPERTIES, qLocationsClass, (ExpressionFactory<AbstractTableLocations>) (AbstractTableLocations qPath) -> qPath.properties);

        Class<? extends Table> qObsPropertiesClass = qCollection.qObsProperties.getClass();
        addEntry(EntityProperty.ID, qObsPropertiesClass, (ExpressionFactory<AbstractTableObsProperties>) AbstractTableObsProperties::getId);
        addEntry(EntityProperty.SELFLINK, qObsPropertiesClass, (ExpressionFactory<AbstractTableObsProperties>) AbstractTableObsProperties::getId);
        addEntry(EntityProperty.DEFINITION, qObsPropertiesClass, (ExpressionFactory<AbstractTableObsProperties>) (AbstractTableObsProperties qPath) -> qPath.definition);
        addEntry(EntityProperty.DESCRIPTION, qObsPropertiesClass, (ExpressionFactory<AbstractTableObsProperties>) (AbstractTableObsProperties qPath) -> qPath.description);
        addEntry(EntityProperty.NAME, qObsPropertiesClass, (ExpressionFactory<AbstractTableObsProperties>) (AbstractTableObsProperties qPath) -> qPath.name);
        addEntry(EntityProperty.PROPERTIES, qObsPropertiesClass, (ExpressionFactory<AbstractTableObsProperties>) (AbstractTableObsProperties qPath) -> qPath.properties);

        Class<? extends Table> qObservationsClass = qCollection.qObservations.getClass();
        addEntry(EntityProperty.ID, qObservationsClass, (ExpressionFactory<AbstractTableObservations>) AbstractTableObservations::getId);
        addEntry(EntityProperty.SELFLINK, qObservationsClass, (ExpressionFactory<AbstractTableObservations>) AbstractTableObservations::getId);
        addEntry(EntityProperty.PARAMETERS, qObservationsClass, (ExpressionFactory<AbstractTableObservations>) (AbstractTableObservations qPath) -> qPath.parameters);
        addEntry(EntityProperty.PHENOMENONTIME, qObservationsClass, KEY_TIME_INTERVAL_START, (ExpressionFactory<AbstractTableObservations>) (AbstractTableObservations qPath) -> qPath.phenomenonTimeStart);
        addEntry(EntityProperty.PHENOMENONTIME, qObservationsClass, KEY_TIME_INTERVAL_END, (ExpressionFactory<AbstractTableObservations>) (AbstractTableObservations qPath) -> qPath.phenomenonTimeEnd);
        addEntry(EntityProperty.RESULT, qObservationsClass, "n", (ExpressionFactory<AbstractTableObservations>) (AbstractTableObservations qPath) -> qPath.resultNumber);
        addEntry(EntityProperty.RESULT, qObservationsClass, "b", (ExpressionFactory<AbstractTableObservations>) (AbstractTableObservations qPath) -> qPath.resultBoolean);
        addEntry(EntityProperty.RESULT, qObservationsClass, "s", (ExpressionFactory<AbstractTableObservations>) (AbstractTableObservations qPath) -> qPath.resultString);
        addEntry(EntityProperty.RESULT, qObservationsClass, "j", (ExpressionFactory<AbstractTableObservations>) (AbstractTableObservations qPath) -> qPath.resultJson);
        addEntry(EntityProperty.RESULT, qObservationsClass, "t", (ExpressionFactory<AbstractTableObservations>) (AbstractTableObservations qPath) -> qPath.resultType);
        addEntry(EntityProperty.RESULTQUALITY, qObservationsClass, (ExpressionFactory<AbstractTableObservations>) (AbstractTableObservations qPath) -> qPath.resultQuality);
        addEntry(EntityProperty.RESULTTIME, qObservationsClass, (ExpressionFactory<AbstractTableObservations>) (AbstractTableObservations qPath) -> qPath.resultTime);
        addEntry(EntityProperty.VALIDTIME, qObservationsClass, KEY_TIME_INTERVAL_START, (ExpressionFactory<AbstractTableObservations>) (AbstractTableObservations qPath) -> qPath.validTimeStart);
        addEntry(EntityProperty.VALIDTIME, qObservationsClass, KEY_TIME_INTERVAL_END, (ExpressionFactory<AbstractTableObservations>) (AbstractTableObservations qPath) -> qPath.validTimeEnd);
        addEntry(NavigationProperty.FEATUREOFINTEREST, qObservationsClass, (ExpressionFactory<AbstractTableObservations>) AbstractTableObservations::getFeatureId);
        addEntry(NavigationProperty.DATASTREAM, qObservationsClass, (ExpressionFactory<AbstractTableObservations>) AbstractTableObservations::getDatastreamId);
        addEntry(NavigationProperty.MULTIDATASTREAM, qObservationsClass, (ExpressionFactory<AbstractTableObservations>) AbstractTableObservations::getMultiDatastreamId);

        Class<? extends Table> qSensorsClass = qCollection.qSensors.getClass();
        addEntry(EntityProperty.ID, qSensorsClass, (ExpressionFactory<AbstractTableSensors>) AbstractTableSensors::getId);
        addEntry(EntityProperty.SELFLINK, qSensorsClass, (ExpressionFactory<AbstractTableSensors>) AbstractTableSensors::getId);
        addEntry(EntityProperty.NAME, qSensorsClass, (ExpressionFactory<AbstractTableSensors>) (AbstractTableSensors qPath) -> qPath.name);
        addEntry(EntityProperty.DESCRIPTION, qSensorsClass, (ExpressionFactory<AbstractTableSensors>) (AbstractTableSensors qPath) -> qPath.description);
        addEntry(EntityProperty.ENCODINGTYPE, qSensorsClass, (ExpressionFactory<AbstractTableSensors>) (AbstractTableSensors qPath) -> qPath.encodingType);
        addEntry(EntityProperty.METADATA, qSensorsClass, (ExpressionFactory<AbstractTableSensors>) (AbstractTableSensors qPath) -> qPath.metadata);
        addEntry(EntityProperty.PROPERTIES, qSensorsClass, (ExpressionFactory<AbstractTableSensors>) (AbstractTableSensors qPath) -> qPath.properties);

        Class<? extends Table> qThingsClass = qCollection.qThings.getClass();
        addEntry(EntityProperty.ID, qThingsClass, (ExpressionFactory<AbstractTableThings>) AbstractTableThings::getId);
        addEntry(EntityProperty.SELFLINK, qThingsClass, (ExpressionFactory<AbstractTableThings>) AbstractTableThings::getId);
        addEntry(EntityProperty.NAME, qThingsClass, (ExpressionFactory<AbstractTableThings>) (AbstractTableThings qPath) -> qPath.name);
        addEntry(EntityProperty.DESCRIPTION, qThingsClass, (ExpressionFactory<AbstractTableThings>) (AbstractTableThings qPath) -> qPath.description);
        addEntry(EntityProperty.PROPERTIES, qThingsClass, (ExpressionFactory<AbstractTableThings>) (AbstractTableThings qPath) -> qPath.properties);
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
    public Collection<Field> expressionsForClass(Table qPath, Collection<Field> target) {
        List<ExpressionFactory> list = allForClass.get(qPath.getClass());
        if (target == null) {
            target = new ArrayList<>();
        }
        for (ExpressionFactory f : list) {
            target.add(f.get(qPath));
        }
        return target;
    }

    public Field expressionForProperty(EntityProperty property, Table qPath) {
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
    public Collection<Field> expressionsForProperty(Property property, Table qPath, Collection<Field> target) {
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
    public Map<String, Field> expressionsForProperty(EntityProperty property, Table qPath, Map<String, Field> target) {
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
    public Set<Field> getExpressions(Table qPath, Set<Property> selectedProperties) {
        Set<Field> exprSet = new HashSet<>();
        if (selectedProperties.isEmpty()) {
            expressionsForClass(qPath, exprSet);
        } else {
            for (Property property : selectedProperties) {
                expressionsForProperty(property, qPath, exprSet);
            }
        }
        return exprSet;
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

    public static FieldWrapper wrapField(Field field) {
        Class fieldType = field.getType();
        if (OffsetDateTime.class.isAssignableFrom(fieldType)) {
            return new StaDateTimeExpression(field);
        }
        return new SimpleFieldWrapper(field);
    }
}
