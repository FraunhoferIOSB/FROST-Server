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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq;

import de.fraunhofer.iosb.ilt.frostserver.path.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.Property;
import de.fraunhofer.iosb.ilt.frostserver.persistence.BasicPersistenceType;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.FieldWrapper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.SimpleFieldWrapper;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.StaDateTimeWrapper;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.StaTimeIntervalWrapper.KEY_TIME_INTERVAL_END;
import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper.StaTimeIntervalWrapper.KEY_TIME_INTERVAL_START;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableActuators;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableDatastreams;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableFeatures;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableHistLocations;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableLocations;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableMultiDatastreams;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableObsProperties;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableObservations;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableSensors;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableTaskingCapabilities;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableTasks;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableThings;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
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

/**
 * @author scf
 * @param <J> The type of the ID fields.
 */
public class PropertyResolver<J> {

    private static interface ExpressionFactory<T> {

        Field get(T table);
    }

    /**
     * The Fields that are allowed be appear in select statements.
     */
    private final Map<Property, Map<Class, Map<String, ExpressionFactory>>> epMapSelect = new HashMap<>();
    /**
     * The Fields that are allowed in where and orderby statements.
     */
    private final Map<Property, Map<Class, Map<String, ExpressionFactory>>> epMapAll = new HashMap<>();
    /**
     * All fields, by class.
     */
    private final Map<Class, List<ExpressionFactory>> allForClass = new HashMap<>();

    private final TableCollection<J> tableCollection;
    private final BasicPersistenceType basicPersistenceType;

    public PropertyResolver(TableCollection<J> tableCollection, BasicPersistenceType basicPersistenceType) {
        this.tableCollection = tableCollection;
        this.basicPersistenceType = basicPersistenceType;
        init();
    }

    private void init() {
        Class<? extends Table> tableClass = tableCollection.tableActuators.getClass();
        ExpressionFactory<? extends Table> selfId = (ExpressionFactory<AbstractTableActuators>) AbstractTableActuators::getId;
        addEntry(EntityProperty.ID, tableClass, selfId);
        addEntry(EntityProperty.SELFLINK, tableClass, selfId);
        addEntry(EntityProperty.NAME, tableClass, (ExpressionFactory<AbstractTableActuators>) (AbstractTableActuators table) -> table.name);
        addEntry(EntityProperty.DESCRIPTION, tableClass, (ExpressionFactory<AbstractTableActuators>) (AbstractTableActuators table) -> table.description);
        addEntry(EntityProperty.ENCODINGTYPE, tableClass, (ExpressionFactory<AbstractTableActuators>) (AbstractTableActuators table) -> table.encodingType);
        addEntry(EntityProperty.METADATA, tableClass, (ExpressionFactory<AbstractTableActuators>) (AbstractTableActuators table) -> table.metadata);
        addEntry(EntityProperty.PROPERTIES, tableClass, (ExpressionFactory<AbstractTableActuators>) (AbstractTableActuators table) -> table.properties);
        addEntry(NavigationProperty.TASKINGCAPABILITIES, tableClass, selfId);

        tableClass = tableCollection.tableDatastreams.getClass();
        selfId = (ExpressionFactory<AbstractTableDatastreams>) AbstractTableDatastreams::getId;
        addEntry(EntityProperty.ID, tableClass, selfId);
        addEntry(EntityProperty.SELFLINK, tableClass, selfId);
        addEntry(EntityProperty.NAME, tableClass, (ExpressionFactory<AbstractTableDatastreams>) (AbstractTableDatastreams table) -> table.name);
        addEntry(EntityProperty.DESCRIPTION, tableClass, (ExpressionFactory<AbstractTableDatastreams>) (AbstractTableDatastreams table) -> table.description);
        addEntry(EntityProperty.OBSERVATIONTYPE, tableClass, (ExpressionFactory<AbstractTableDatastreams>) (AbstractTableDatastreams table) -> table.observationType);
        addEntry(EntityProperty.OBSERVEDAREA, tableClass, "s", (ExpressionFactory<AbstractTableDatastreams>) (AbstractTableDatastreams table) -> table.observedAreaText);
        addEntryNoSelect(EntityProperty.OBSERVEDAREA, tableClass, "g", (ExpressionFactory<AbstractTableDatastreams>) (AbstractTableDatastreams table) -> table.observedArea);
        addEntry(EntityProperty.PHENOMENONTIME, tableClass, KEY_TIME_INTERVAL_START, (ExpressionFactory<AbstractTableDatastreams>) (AbstractTableDatastreams table) -> table.phenomenonTimeStart);
        addEntry(EntityProperty.PHENOMENONTIME, tableClass, KEY_TIME_INTERVAL_END, (ExpressionFactory<AbstractTableDatastreams>) (AbstractTableDatastreams table) -> table.phenomenonTimeEnd);
        addEntry(EntityProperty.PROPERTIES, tableClass, (ExpressionFactory<AbstractTableDatastreams>) (AbstractTableDatastreams table) -> table.properties);
        addEntry(EntityProperty.RESULTTIME, tableClass, KEY_TIME_INTERVAL_START, (ExpressionFactory<AbstractTableDatastreams>) (AbstractTableDatastreams table) -> table.resultTimeStart);
        addEntry(EntityProperty.RESULTTIME, tableClass, KEY_TIME_INTERVAL_END, (ExpressionFactory<AbstractTableDatastreams>) (AbstractTableDatastreams table) -> table.resultTimeEnd);
        addEntry(EntityProperty.UNITOFMEASUREMENT, tableClass, "definition", (ExpressionFactory<AbstractTableDatastreams>) (AbstractTableDatastreams table) -> table.unitDefinition);
        addEntry(EntityProperty.UNITOFMEASUREMENT, tableClass, "name", (ExpressionFactory<AbstractTableDatastreams>) (AbstractTableDatastreams table) -> table.unitName);
        addEntry(EntityProperty.UNITOFMEASUREMENT, tableClass, "symbol", (ExpressionFactory<AbstractTableDatastreams>) (AbstractTableDatastreams table) -> table.unitSymbol);
        addEntry(NavigationProperty.SENSOR, tableClass, (ExpressionFactory<AbstractTableDatastreams>) AbstractTableDatastreams::getSensorId);
        addEntry(NavigationProperty.OBSERVEDPROPERTY, tableClass, (ExpressionFactory<AbstractTableDatastreams>) AbstractTableDatastreams::getObsPropertyId);
        addEntry(NavigationProperty.THING, tableClass, (ExpressionFactory<AbstractTableDatastreams>) AbstractTableDatastreams::getThingId);
        addEntry(NavigationProperty.OBSERVATIONS, tableClass, selfId);

        tableClass = tableCollection.tableMultiDatastreams.getClass();
        selfId = (ExpressionFactory<AbstractTableMultiDatastreams>) AbstractTableMultiDatastreams::getId;
        addEntry(EntityProperty.ID, tableClass, selfId);
        addEntry(EntityProperty.SELFLINK, tableClass, selfId);
        addEntry(EntityProperty.NAME, tableClass, (ExpressionFactory<AbstractTableMultiDatastreams>) (AbstractTableMultiDatastreams table) -> table.name);
        addEntry(EntityProperty.DESCRIPTION, tableClass, (ExpressionFactory<AbstractTableMultiDatastreams>) (AbstractTableMultiDatastreams table) -> table.description);
        addEntry(EntityProperty.MULTIOBSERVATIONDATATYPES, tableClass, (ExpressionFactory<AbstractTableMultiDatastreams>) (AbstractTableMultiDatastreams table) -> table.observationTypes);
        addEntry(EntityProperty.OBSERVEDAREA, tableClass, "s", (ExpressionFactory<AbstractTableMultiDatastreams>) (AbstractTableMultiDatastreams table) -> table.observedAreaText);
        addEntryNoSelect(EntityProperty.OBSERVEDAREA, tableClass, "g", (ExpressionFactory<AbstractTableMultiDatastreams>) (AbstractTableMultiDatastreams table) -> table.observedArea);
        addEntry(EntityProperty.PHENOMENONTIME, tableClass, KEY_TIME_INTERVAL_START, (ExpressionFactory<AbstractTableMultiDatastreams>) (AbstractTableMultiDatastreams table) -> table.phenomenonTimeStart);
        addEntry(EntityProperty.PHENOMENONTIME, tableClass, KEY_TIME_INTERVAL_END, (ExpressionFactory<AbstractTableMultiDatastreams>) (AbstractTableMultiDatastreams table) -> table.phenomenonTimeEnd);
        addEntry(EntityProperty.PROPERTIES, tableClass, (ExpressionFactory<AbstractTableMultiDatastreams>) (AbstractTableMultiDatastreams table) -> table.properties);
        addEntry(EntityProperty.RESULTTIME, tableClass, KEY_TIME_INTERVAL_START, (ExpressionFactory<AbstractTableMultiDatastreams>) (AbstractTableMultiDatastreams table) -> table.resultTimeStart);
        addEntry(EntityProperty.RESULTTIME, tableClass, KEY_TIME_INTERVAL_END, (ExpressionFactory<AbstractTableMultiDatastreams>) (AbstractTableMultiDatastreams table) -> table.resultTimeEnd);
        addEntry(EntityProperty.UNITOFMEASUREMENTS, tableClass, (ExpressionFactory<AbstractTableMultiDatastreams>) (AbstractTableMultiDatastreams table) -> table.unitOfMeasurements);
        addEntry(NavigationProperty.SENSOR, tableClass, (ExpressionFactory<AbstractTableMultiDatastreams>) AbstractTableMultiDatastreams::getSensorId);
        addEntry(NavigationProperty.THING, tableClass, (ExpressionFactory<AbstractTableMultiDatastreams>) AbstractTableMultiDatastreams::getThingId);
        addEntry(NavigationProperty.OBSERVEDPROPERTIES, tableClass, selfId);
        addEntry(NavigationProperty.OBSERVATIONS, tableClass, selfId);

        tableClass = tableCollection.tableFeatures.getClass();
        selfId = (ExpressionFactory<AbstractTableFeatures>) AbstractTableFeatures::getId;
        addEntry(EntityProperty.ID, tableClass, selfId);
        addEntry(EntityProperty.SELFLINK, tableClass, selfId);
        addEntry(EntityProperty.NAME, tableClass, (ExpressionFactory<AbstractTableFeatures>) (AbstractTableFeatures table) -> table.name);
        addEntry(EntityProperty.DESCRIPTION, tableClass, (ExpressionFactory<AbstractTableFeatures>) (AbstractTableFeatures table) -> table.description);
        addEntry(EntityProperty.ENCODINGTYPE, tableClass, (ExpressionFactory<AbstractTableFeatures>) (AbstractTableFeatures table) -> table.encodingType);
        addEntry(EntityProperty.FEATURE, tableClass, "j", (ExpressionFactory<AbstractTableFeatures>) (AbstractTableFeatures table) -> table.feature);
        addEntryNoSelect(EntityProperty.FEATURE, tableClass, "g", (ExpressionFactory<AbstractTableFeatures>) (AbstractTableFeatures table) -> table.geom);
        addEntry(EntityProperty.PROPERTIES, tableClass, (ExpressionFactory<AbstractTableFeatures>) (AbstractTableFeatures table) -> table.properties);
        addEntry(NavigationProperty.OBSERVATIONS, tableClass, selfId);

        tableClass = tableCollection.tableHistLocations.getClass();
        selfId = (ExpressionFactory<AbstractTableHistLocations>) AbstractTableHistLocations::getId;
        addEntry(EntityProperty.ID, tableClass, selfId);
        addEntry(EntityProperty.SELFLINK, tableClass, selfId);
        addEntry(EntityProperty.TIME, tableClass, (ExpressionFactory<AbstractTableHistLocations>) (AbstractTableHistLocations table) -> table.time);
        addEntry(NavigationProperty.THING, tableClass, (ExpressionFactory<AbstractTableHistLocations>) AbstractTableHistLocations::getThingId);
        addEntry(NavigationProperty.LOCATIONS, tableClass, selfId);

        tableClass = tableCollection.tableLocations.getClass();
        selfId = (ExpressionFactory<AbstractTableLocations>) AbstractTableLocations::getId;
        addEntry(EntityProperty.ID, tableClass, selfId);
        addEntry(EntityProperty.SELFLINK, tableClass, selfId);
        addEntry(EntityProperty.NAME, tableClass, (ExpressionFactory<AbstractTableLocations>) (AbstractTableLocations table) -> table.name);
        addEntry(EntityProperty.DESCRIPTION, tableClass, (ExpressionFactory<AbstractTableLocations>) (AbstractTableLocations table) -> table.description);
        addEntry(EntityProperty.ENCODINGTYPE, tableClass, (ExpressionFactory<AbstractTableLocations>) (AbstractTableLocations table) -> table.encodingType);
        addEntry(EntityProperty.LOCATION, tableClass, "j", (ExpressionFactory<AbstractTableLocations>) (AbstractTableLocations table) -> table.location);
        addEntryNoSelect(EntityProperty.LOCATION, tableClass, "g", (ExpressionFactory<AbstractTableLocations>) (AbstractTableLocations table) -> table.geom);
        addEntry(EntityProperty.PROPERTIES, tableClass, (ExpressionFactory<AbstractTableLocations>) (AbstractTableLocations table) -> table.properties);
        addEntry(NavigationProperty.THINGS, tableClass, selfId);
        addEntry(NavigationProperty.HISTORICALLOCATIONS, tableClass, selfId);

        tableClass = tableCollection.tableObsProperties.getClass();
        selfId = (ExpressionFactory<AbstractTableObsProperties>) AbstractTableObsProperties::getId;
        addEntry(EntityProperty.ID, tableClass, selfId);
        addEntry(EntityProperty.SELFLINK, tableClass, selfId);
        addEntry(EntityProperty.DEFINITION, tableClass, (ExpressionFactory<AbstractTableObsProperties>) (AbstractTableObsProperties table) -> table.definition);
        addEntry(EntityProperty.DESCRIPTION, tableClass, (ExpressionFactory<AbstractTableObsProperties>) (AbstractTableObsProperties table) -> table.description);
        addEntry(EntityProperty.NAME, tableClass, (ExpressionFactory<AbstractTableObsProperties>) (AbstractTableObsProperties table) -> table.name);
        addEntry(EntityProperty.PROPERTIES, tableClass, (ExpressionFactory<AbstractTableObsProperties>) (AbstractTableObsProperties table) -> table.properties);
        addEntry(NavigationProperty.DATASTREAMS, tableClass, selfId);
        addEntry(NavigationProperty.MULTIDATASTREAMS, tableClass, selfId);

        tableClass = tableCollection.tableObservations.getClass();
        selfId = (ExpressionFactory<AbstractTableObservations>) AbstractTableObservations::getId;
        addEntry(EntityProperty.ID, tableClass, selfId);
        addEntry(EntityProperty.SELFLINK, tableClass, selfId);
        addEntry(EntityProperty.PARAMETERS, tableClass, (ExpressionFactory<AbstractTableObservations>) (AbstractTableObservations table) -> table.parameters);
        addEntry(EntityProperty.PHENOMENONTIME, tableClass, KEY_TIME_INTERVAL_START, (ExpressionFactory<AbstractTableObservations>) (AbstractTableObservations table) -> table.phenomenonTimeStart);
        addEntry(EntityProperty.PHENOMENONTIME, tableClass, KEY_TIME_INTERVAL_END, (ExpressionFactory<AbstractTableObservations>) (AbstractTableObservations table) -> table.phenomenonTimeEnd);
        addEntry(EntityProperty.RESULT, tableClass, "n", (ExpressionFactory<AbstractTableObservations>) (AbstractTableObservations table) -> table.resultNumber);
        addEntry(EntityProperty.RESULT, tableClass, "b", (ExpressionFactory<AbstractTableObservations>) (AbstractTableObservations table) -> table.resultBoolean);
        addEntry(EntityProperty.RESULT, tableClass, "s", (ExpressionFactory<AbstractTableObservations>) (AbstractTableObservations table) -> table.resultString);
        addEntry(EntityProperty.RESULT, tableClass, "j", (ExpressionFactory<AbstractTableObservations>) (AbstractTableObservations table) -> table.resultJson);
        addEntry(EntityProperty.RESULT, tableClass, "t", (ExpressionFactory<AbstractTableObservations>) (AbstractTableObservations table) -> table.resultType);
        addEntry(EntityProperty.RESULTQUALITY, tableClass, (ExpressionFactory<AbstractTableObservations>) (AbstractTableObservations table) -> table.resultQuality);
        addEntry(EntityProperty.RESULTTIME, tableClass, (ExpressionFactory<AbstractTableObservations>) (AbstractTableObservations table) -> table.resultTime);
        addEntry(EntityProperty.VALIDTIME, tableClass, KEY_TIME_INTERVAL_START, (ExpressionFactory<AbstractTableObservations>) (AbstractTableObservations table) -> table.validTimeStart);
        addEntry(EntityProperty.VALIDTIME, tableClass, KEY_TIME_INTERVAL_END, (ExpressionFactory<AbstractTableObservations>) (AbstractTableObservations table) -> table.validTimeEnd);
        addEntry(NavigationProperty.FEATUREOFINTEREST, tableClass, (ExpressionFactory<AbstractTableObservations>) AbstractTableObservations::getFeatureId);
        addEntry(NavigationProperty.DATASTREAM, tableClass, (ExpressionFactory<AbstractTableObservations>) AbstractTableObservations::getDatastreamId);
        addEntry(NavigationProperty.MULTIDATASTREAM, tableClass, (ExpressionFactory<AbstractTableObservations>) AbstractTableObservations::getMultiDatastreamId);

        tableClass = tableCollection.tableSensors.getClass();
        selfId = (ExpressionFactory<AbstractTableSensors>) AbstractTableSensors::getId;
        addEntry(EntityProperty.ID, tableClass, selfId);
        addEntry(EntityProperty.SELFLINK, tableClass, selfId);
        addEntry(EntityProperty.NAME, tableClass, (ExpressionFactory<AbstractTableSensors>) (AbstractTableSensors table) -> table.name);
        addEntry(EntityProperty.DESCRIPTION, tableClass, (ExpressionFactory<AbstractTableSensors>) (AbstractTableSensors table) -> table.description);
        addEntry(EntityProperty.ENCODINGTYPE, tableClass, (ExpressionFactory<AbstractTableSensors>) (AbstractTableSensors table) -> table.encodingType);
        addEntry(EntityProperty.METADATA, tableClass, (ExpressionFactory<AbstractTableSensors>) (AbstractTableSensors table) -> table.metadata);
        addEntry(EntityProperty.PROPERTIES, tableClass, (ExpressionFactory<AbstractTableSensors>) (AbstractTableSensors table) -> table.properties);
        addEntry(NavigationProperty.DATASTREAMS, tableClass, selfId);
        addEntry(NavigationProperty.MULTIDATASTREAMS, tableClass, selfId);

        tableClass = tableCollection.tableTasks.getClass();
        selfId = (ExpressionFactory<AbstractTableTasks>) AbstractTableTasks::getId;
        addEntry(EntityProperty.ID, tableClass, selfId);
        addEntry(EntityProperty.SELFLINK, tableClass, selfId);
        addEntry(EntityProperty.CREATIONTIME, tableClass, (ExpressionFactory<AbstractTableTasks>) (AbstractTableTasks table) -> table.creationTime);
        addEntry(EntityProperty.TASKINGPARAMETERS, tableClass, (ExpressionFactory<AbstractTableTasks>) (AbstractTableTasks table) -> table.taskingParameters);
        addEntry(NavigationProperty.TASKINGCAPABILITY, tableClass, (ExpressionFactory<AbstractTableTasks>) AbstractTableTasks::getTaskingCapabilityId);

        tableClass = tableCollection.tableTaskingCapabilities.getClass();
        selfId = (ExpressionFactory<AbstractTableTaskingCapabilities>) AbstractTableTaskingCapabilities::getId;
        addEntry(EntityProperty.ID, tableClass, selfId);
        addEntry(EntityProperty.SELFLINK, tableClass, selfId);
        addEntry(EntityProperty.NAME, tableClass, (ExpressionFactory<AbstractTableTaskingCapabilities>) (AbstractTableTaskingCapabilities table) -> table.name);
        addEntry(EntityProperty.DESCRIPTION, tableClass, (ExpressionFactory<AbstractTableTaskingCapabilities>) (AbstractTableTaskingCapabilities table) -> table.description);
        addEntry(EntityProperty.PROPERTIES, tableClass, (ExpressionFactory<AbstractTableTaskingCapabilities>) (AbstractTableTaskingCapabilities table) -> table.properties);
        addEntry(EntityProperty.TASKINGPARAMETERS, tableClass, (ExpressionFactory<AbstractTableTaskingCapabilities>) (AbstractTableTaskingCapabilities table) -> table.taskingParameters);
        addEntry(NavigationProperty.ACTUATOR, tableClass, (ExpressionFactory<AbstractTableTaskingCapabilities>) AbstractTableTaskingCapabilities::getActuatorId);
        addEntry(NavigationProperty.THING, tableClass, (ExpressionFactory<AbstractTableTaskingCapabilities>) AbstractTableTaskingCapabilities::getThingId);
        addEntry(NavigationProperty.TASKS, tableClass, selfId);

        tableClass = tableCollection.tableThings.getClass();
        selfId = (ExpressionFactory<AbstractTableThings>) AbstractTableThings::getId;
        addEntry(EntityProperty.ID, tableClass, selfId);
        addEntry(EntityProperty.SELFLINK, tableClass, selfId);
        addEntry(EntityProperty.NAME, tableClass, (ExpressionFactory<AbstractTableThings>) (AbstractTableThings table) -> table.name);
        addEntry(EntityProperty.DESCRIPTION, tableClass, (ExpressionFactory<AbstractTableThings>) (AbstractTableThings table) -> table.description);
        addEntry(EntityProperty.PROPERTIES, tableClass, (ExpressionFactory<AbstractTableThings>) (AbstractTableThings table) -> table.properties);
        addEntry(NavigationProperty.DATASTREAMS, tableClass, selfId);
        addEntry(NavigationProperty.HISTORICALLOCATIONS, tableClass, selfId);
        addEntry(NavigationProperty.LOCATIONS, tableClass, selfId);
        addEntry(NavigationProperty.MULTIDATASTREAMS, tableClass, selfId);
        addEntry(NavigationProperty.TASKINGCAPABILITIES, tableClass, selfId);
    }

    public BasicPersistenceType getBasicPersistenceType() {
        return basicPersistenceType;
    }

    /**
     * @return the tableCollection
     */
    public TableCollection<J> getTableCollection() {
        return tableCollection;
    }

    /**
     * Get the Fields for the given class, that are allowed to be used in the
     * select clause of a query.
     *
     * @param table The table to get expressions for.
     * @param target The list to add to. If null a new list will be created.
     * @return The target list, or a new list if target was null.
     */
    public Collection<Field> getSelectFieldsForClass(Table table, Collection<Field> target) {
        List<ExpressionFactory> list = allForClass.get(table.getClass());
        if (target == null) {
            target = new ArrayList<>();
        }
        for (ExpressionFactory f : list) {
            target.add(f.get(table));
        }
        return target;
    }

    /**
     * Get a list of Fields for the given property and table. Add it to the
     * given list, or a new list.
     *
     * @param property The property to get expressions for.
     * @param table The table to get expressions for.
     * @param target The list to add to. If null a new list will be created.
     * @return The target list, or a new list if target was null.
     */
    public Collection<Field> getSelectFieldsForProperty(Property property, Table table, Collection<Field> target) {
        Map<Class, Map<String, ExpressionFactory>> innerMap = epMapSelect.get(property);
        if (innerMap == null) {
            return target;
        }
        Map<String, ExpressionFactory> coreMap = innerMap.get(table.getClass());
        if (target == null) {
            target = new ArrayList<>();
        }
        for (Map.Entry<String, ExpressionFactory> es : coreMap.entrySet()) {
            target.add(es.getValue().get(table));
        }
        return target;
    }

    /**
     * Get a Map of expressions for the given property and table. Add it to the
     * given Map, or a new Map.
     *
     * @param property The property to get expressions for.
     * @param table The table to get expressions for.
     * @param target The Map to add to. If null a new Map will be created.
     * @return The target Map, or a new Map if target was null.
     */
    public Map<String, Field> getAllFieldsForProperty(EntityProperty property, Table table, Map<String, Field> target) {
        Map<Class, Map<String, ExpressionFactory>> innerMap = epMapAll.get(property);
        if (innerMap == null) {
            throw new IllegalArgumentException("We do not know any property called " + property.toString());
        }
        Map<String, ExpressionFactory> coreMap = innerMap.get(table.getClass());
        if (coreMap == null) {
            throw new IllegalArgumentException("No property called " + property.toString() + " for " + table.getClass());
        }
        if (target == null) {
            target = new LinkedHashMap<>();
        }
        for (Map.Entry<String, ExpressionFactory> es : coreMap.entrySet()) {
            target.put(es.getKey(), es.getValue().get(table));
        }
        return target;
    }

    /**
     * Get the set of expressions for the given set of selected properties.
     *
     * @param table The entity table to get the expressions for.
     * @param selectedProperties The set of properties to get the expressions
     * of.
     * @return The set of expressions.
     */
    public Set<Field> getFieldsForProperties(Table table, Set<Property> selectedProperties) {
        Set<Field> exprSet = new HashSet<>();
        if (selectedProperties.isEmpty()) {
            getSelectFieldsForClass(table, exprSet);
        } else {
            for (Property property : selectedProperties) {
                getSelectFieldsForProperty(property, table, exprSet);
            }
        }
        return exprSet;
    }

    /**
     * Add an unnamed entry to the Field registry.
     *
     * @param property The property that this field supplies data for.
     * @param clazz The entity class (table) for this field.
     * @param factory The factory to use to generate the Field instance.
     */
    private void addEntry(Property property, Class clazz, ExpressionFactory factory) {
        addEntry(epMapSelect, property, clazz, null, factory);
        addEntry(epMapAll, property, clazz, null, factory);
        addToAll(clazz, factory);
    }

    /**
     * Add an entry to the Field registry.
     *
     * @param property The property that this field supplies data for.
     * @param clazz The entity class (table) for this field.
     * @param name The name to use for this field. (j for json, s for string, g
     * for geometry)
     * @param factory The factory to use to generate the Field instance.
     */
    private void addEntry(Property property, Class clazz, String name, ExpressionFactory factory) {
        addEntry(epMapSelect, property, clazz, name, factory);
        addEntry(epMapAll, property, clazz, name, factory);
        addToAll(clazz, factory);
    }

    /**
     * Add an entry to the Field registry, but do not register it to the entity.
     * This means the field is never used in "select" clauses.
     *
     * @param property The property that this field supplies data for.
     * @param clazz The entity class (table) for this field.
     * @param name The name to use for this field. (j for json, s for string, g
     * for geometry)
     * @param factory The factory to use to generate the Field instance.
     */
    private void addEntryNoSelect(Property property, Class clazz, String name, ExpressionFactory factory) {
        addEntry(epMapAll, property, clazz, name, factory);
    }

    private void addToAll(Class clazz, ExpressionFactory factory) {
        List<ExpressionFactory> list = allForClass.computeIfAbsent(
                clazz,
                k -> new ArrayList<>()
        );
        list.add(factory);
    }

    private void addEntry(Map<Property, Map<Class, Map<String, ExpressionFactory>>> map, Property property, Class clazz, String name, ExpressionFactory factory) {
        Map<Class, Map<String, ExpressionFactory>> innerMap = map.computeIfAbsent(
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
            return new StaDateTimeWrapper(field);
        }
        return new SimpleFieldWrapper(field);
    }

}
