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
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.SelectedProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
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
public class PropertyResolver<J extends Comparable> {

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
    private final String basicPersistenceType;

    public PropertyResolver(TableCollection<J> tableCollection, String basicPersistenceType) {
        this.tableCollection = tableCollection;
        this.basicPersistenceType = basicPersistenceType;
        init();
    }

    private void init() {
        initActuators();
        initDatastreams();
        initMultiDatastreams();
        initFeatures();
        initHistLocations();
        initLocations();
        initObsProperties();
        initObservations();
        initSensors();
        initTasks();
        initTaskingCapabilities();
        initThings();
    }

    private void initActuators() {
        Class<? extends AbstractTableActuators> tableClass = tableCollection.getTableActuators().getClass();
        addEntry(EntityPropertyMain.ID, tableClass, AbstractTableActuators::getId);
        addEntry(EntityPropertyMain.SELFLINK, tableClass, AbstractTableActuators::getId);
        addEntry(EntityPropertyMain.NAME, tableClass, table -> table.colName);
        addEntry(EntityPropertyMain.DESCRIPTION, tableClass, table -> table.colDescription);
        addEntry(EntityPropertyMain.ENCODINGTYPE, tableClass, table -> table.colEncodingType);
        addEntry(EntityPropertyMain.METADATA, tableClass, table -> table.colMetadata);
        addEntry(EntityPropertyMain.PROPERTIES, tableClass, table -> table.colProperties);
        addEntry(NavigationPropertyMain.TASKINGCAPABILITIES, tableClass, AbstractTableActuators::getId);
    }

    private void initDatastreams() {
        Class<? extends AbstractTableDatastreams> tableClass = tableCollection.getTableDatastreams().getClass();
        addEntry(EntityPropertyMain.ID, tableClass, AbstractTableDatastreams::getId);
        addEntry(EntityPropertyMain.SELFLINK, tableClass, AbstractTableDatastreams::getId);
        addEntry(EntityPropertyMain.NAME, tableClass, table -> table.colName);
        addEntry(EntityPropertyMain.DESCRIPTION, tableClass, table -> table.colDescription);
        addEntry(EntityPropertyMain.OBSERVATIONTYPE, tableClass, table -> table.colObservationType);
        addEntry(EntityPropertyMain.OBSERVEDAREA, tableClass, "s", table -> table.colObservedAreaText);
        addEntryNoSelect(EntityPropertyMain.OBSERVEDAREA, tableClass, "g", table -> table.colObservedArea);
        addEntry(EntityPropertyMain.PHENOMENONTIME, tableClass, KEY_TIME_INTERVAL_START, table -> table.colPhenomenonTimeStart);
        addEntry(EntityPropertyMain.PHENOMENONTIME, tableClass, KEY_TIME_INTERVAL_END, table -> table.colPhenomenonTimeEnd);
        addEntry(EntityPropertyMain.PROPERTIES, tableClass, table -> table.colProperties);
        addEntry(EntityPropertyMain.RESULTTIME, tableClass, KEY_TIME_INTERVAL_START, table -> table.colResultTimeStart);
        addEntry(EntityPropertyMain.RESULTTIME, tableClass, KEY_TIME_INTERVAL_END, table -> table.colResultTimeEnd);
        addEntry(EntityPropertyMain.UNITOFMEASUREMENT, tableClass, "definition", table -> table.colUnitDefinition);
        addEntry(EntityPropertyMain.UNITOFMEASUREMENT, tableClass, "name", table -> table.colUnitName);
        addEntry(EntityPropertyMain.UNITOFMEASUREMENT, tableClass, "symbol", table -> table.colUnitSymbol);
        addEntry(NavigationPropertyMain.SENSOR, tableClass, AbstractTableDatastreams::getSensorId);
        addEntry(NavigationPropertyMain.OBSERVEDPROPERTY, tableClass, AbstractTableDatastreams::getObsPropertyId);
        addEntry(NavigationPropertyMain.THING, tableClass, AbstractTableDatastreams::getThingId);
        addEntry(NavigationPropertyMain.OBSERVATIONS, tableClass, AbstractTableDatastreams::getId);
    }

    private void initMultiDatastreams() {
        Class<? extends AbstractTableMultiDatastreams> tableClass = tableCollection.getTableMultiDatastreams().getClass();
        addEntry(EntityPropertyMain.ID, tableClass, AbstractTableMultiDatastreams::getId);
        addEntry(EntityPropertyMain.SELFLINK, tableClass, AbstractTableMultiDatastreams::getId);
        addEntry(EntityPropertyMain.NAME, tableClass, table -> table.colName);
        addEntry(EntityPropertyMain.DESCRIPTION, tableClass, table -> table.colDescription);
        addEntry(EntityPropertyMain.MULTIOBSERVATIONDATATYPES, tableClass, table -> table.colObservationTypes);
        addEntry(EntityPropertyMain.OBSERVEDAREA, tableClass, "s", table -> table.colObservedAreaText);
        addEntryNoSelect(EntityPropertyMain.OBSERVEDAREA, tableClass, "g", table -> table.colObservedArea);
        addEntry(EntityPropertyMain.PHENOMENONTIME, tableClass, KEY_TIME_INTERVAL_START, table -> table.colPhenomenonTimeStart);
        addEntry(EntityPropertyMain.PHENOMENONTIME, tableClass, KEY_TIME_INTERVAL_END, table -> table.colPhenomenonTimeEnd);
        addEntry(EntityPropertyMain.PROPERTIES, tableClass, table -> table.colProperties);
        addEntry(EntityPropertyMain.RESULTTIME, tableClass, KEY_TIME_INTERVAL_START, table -> table.colResultTimeStart);
        addEntry(EntityPropertyMain.RESULTTIME, tableClass, KEY_TIME_INTERVAL_END, table -> table.colResultTimeEnd);
        addEntry(EntityPropertyMain.UNITOFMEASUREMENTS, tableClass, table -> table.colUnitOfMeasurements);
        addEntry(NavigationPropertyMain.SENSOR, tableClass, AbstractTableMultiDatastreams::getSensorId);
        addEntry(NavigationPropertyMain.THING, tableClass, AbstractTableMultiDatastreams::getThingId);
        addEntry(NavigationPropertyMain.OBSERVEDPROPERTIES, tableClass, AbstractTableMultiDatastreams::getId);
        addEntry(NavigationPropertyMain.OBSERVATIONS, tableClass, AbstractTableMultiDatastreams::getId);
    }

    private void initFeatures() {
        Class<? extends AbstractTableFeatures> tableClass = tableCollection.getTableFeatures().getClass();
        addEntry(EntityPropertyMain.ID, tableClass, AbstractTableFeatures::getId);
        addEntry(EntityPropertyMain.SELFLINK, tableClass, AbstractTableFeatures::getId);
        addEntry(EntityPropertyMain.NAME, tableClass, table -> table.colName);
        addEntry(EntityPropertyMain.DESCRIPTION, tableClass, table -> table.colDescription);
        addEntry(EntityPropertyMain.ENCODINGTYPE, tableClass, table -> table.colEncodingType);
        addEntry(EntityPropertyMain.FEATURE, tableClass, "j", table -> table.colFeature);
        addEntryNoSelect(EntityPropertyMain.FEATURE, tableClass, "g", table -> table.colGeom);
        addEntry(EntityPropertyMain.PROPERTIES, tableClass, table -> table.colProperties);
        addEntry(NavigationPropertyMain.OBSERVATIONS, tableClass, AbstractTableFeatures::getId);
    }

    private void initHistLocations() {
        Class<? extends AbstractTableHistLocations> tableClass = tableCollection.getTableHistLocations().getClass();
        addEntry(EntityPropertyMain.ID, tableClass, AbstractTableHistLocations::getId);
        addEntry(EntityPropertyMain.SELFLINK, tableClass, AbstractTableHistLocations::getId);
        addEntry(EntityPropertyMain.TIME, tableClass, table -> table.time);
        addEntry(NavigationPropertyMain.THING, tableClass, AbstractTableHistLocations::getThingId);
        addEntry(NavigationPropertyMain.LOCATIONS, tableClass, AbstractTableHistLocations::getId);
    }

    private void initLocations() {
        Class<? extends AbstractTableLocations> tableClass = tableCollection.getTableLocations().getClass();
        addEntry(EntityPropertyMain.ID, tableClass, AbstractTableLocations::getId);
        addEntry(EntityPropertyMain.SELFLINK, tableClass, AbstractTableLocations::getId);
        addEntry(EntityPropertyMain.NAME, tableClass, table -> table.colName);
        addEntry(EntityPropertyMain.DESCRIPTION, tableClass, table -> table.colDescription);
        addEntry(EntityPropertyMain.ENCODINGTYPE, tableClass, table -> table.colEncodingType);
        addEntry(EntityPropertyMain.LOCATION, tableClass, "j", table -> table.colLocation);
        addEntryNoSelect(EntityPropertyMain.LOCATION, tableClass, "g", table -> table.colGeom);
        addEntry(EntityPropertyMain.PROPERTIES, tableClass, table -> table.colProperties);
        addEntry(NavigationPropertyMain.THINGS, tableClass, AbstractTableLocations::getId);
        addEntry(NavigationPropertyMain.HISTORICALLOCATIONS, tableClass, AbstractTableLocations::getId);
    }

    private void initObservations() {
        Class<? extends AbstractTableObservations> tableClass = tableCollection.getTableObservations().getClass();
        addEntry(EntityPropertyMain.ID, tableClass, AbstractTableObservations::getId);
        addEntry(EntityPropertyMain.SELFLINK, tableClass, AbstractTableObservations::getId);
        addEntry(EntityPropertyMain.PARAMETERS, tableClass, table -> table.colParameters);
        addEntry(EntityPropertyMain.PHENOMENONTIME, tableClass, KEY_TIME_INTERVAL_START, table -> table.colPhenomenonTimeStart);
        addEntry(EntityPropertyMain.PHENOMENONTIME, tableClass, KEY_TIME_INTERVAL_END, table -> table.colPhenomenonTimeEnd);
        addEntry(EntityPropertyMain.RESULT, tableClass, "n", table -> table.colResultNumber);
        addEntry(EntityPropertyMain.RESULT, tableClass, "b", table -> table.colResultBoolean);
        addEntry(EntityPropertyMain.RESULT, tableClass, "s", table -> table.colResultString);
        addEntry(EntityPropertyMain.RESULT, tableClass, "j", table -> table.colResultJson);
        addEntry(EntityPropertyMain.RESULT, tableClass, "t", table -> table.colResultType);
        addEntry(EntityPropertyMain.RESULTQUALITY, tableClass, table -> table.colResultQuality);
        addEntry(EntityPropertyMain.RESULTTIME, tableClass, table -> table.colResultTime);
        addEntry(EntityPropertyMain.VALIDTIME, tableClass, KEY_TIME_INTERVAL_START, table -> table.colValidTimeStart);
        addEntry(EntityPropertyMain.VALIDTIME, tableClass, KEY_TIME_INTERVAL_END, table -> table.colValidTimeEnd);
        addEntry(NavigationPropertyMain.FEATUREOFINTEREST, tableClass, AbstractTableObservations::getFeatureId);
        addEntry(NavigationPropertyMain.DATASTREAM, tableClass, AbstractTableObservations::getDatastreamId);
        addEntry(NavigationPropertyMain.MULTIDATASTREAM, tableClass, AbstractTableObservations::getMultiDatastreamId);
    }

    private void initObsProperties() {
        Class<? extends AbstractTableObsProperties> tableClass = tableCollection.getTableObsProperties().getClass();
        addEntry(EntityPropertyMain.ID, tableClass, AbstractTableObsProperties::getId);
        addEntry(EntityPropertyMain.SELFLINK, tableClass, AbstractTableObsProperties::getId);
        addEntry(EntityPropertyMain.DEFINITION, tableClass, table -> table.colDefinition);
        addEntry(EntityPropertyMain.DESCRIPTION, tableClass, table -> table.colDescription);
        addEntry(EntityPropertyMain.NAME, tableClass, table -> table.colName);
        addEntry(EntityPropertyMain.PROPERTIES, tableClass, table -> table.colProperties);
        addEntry(NavigationPropertyMain.DATASTREAMS, tableClass, AbstractTableObsProperties::getId);
        addEntry(NavigationPropertyMain.MULTIDATASTREAMS, tableClass, AbstractTableObsProperties::getId);
    }

    private void initSensors() {
        Class<? extends AbstractTableSensors> tableClass = tableCollection.getTableSensors().getClass();
        addEntry(EntityPropertyMain.ID, tableClass, AbstractTableSensors::getId);
        addEntry(EntityPropertyMain.SELFLINK, tableClass, AbstractTableSensors::getId);
        addEntry(EntityPropertyMain.NAME, tableClass, table -> table.colName);
        addEntry(EntityPropertyMain.DESCRIPTION, tableClass, table -> table.colDescription);
        addEntry(EntityPropertyMain.ENCODINGTYPE, tableClass, table -> table.colEncodingType);
        addEntry(EntityPropertyMain.METADATA, tableClass, table -> table.colMetadata);
        addEntry(EntityPropertyMain.PROPERTIES, tableClass, table -> table.colProperties);
        addEntry(NavigationPropertyMain.DATASTREAMS, tableClass, AbstractTableSensors::getId);
        addEntry(NavigationPropertyMain.MULTIDATASTREAMS, tableClass, AbstractTableSensors::getId);
    }

    private void initTaskingCapabilities() {
        Class<? extends AbstractTableTaskingCapabilities> tableClass = tableCollection.getTableTaskingCapabilities().getClass();
        addEntry(EntityPropertyMain.ID, tableClass, AbstractTableTaskingCapabilities::getId);
        addEntry(EntityPropertyMain.SELFLINK, tableClass, AbstractTableTaskingCapabilities::getId);
        addEntry(EntityPropertyMain.NAME, tableClass, table -> table.colName);
        addEntry(EntityPropertyMain.DESCRIPTION, tableClass, table -> table.colDescription);
        addEntry(EntityPropertyMain.PROPERTIES, tableClass, table -> table.colProperties);
        addEntry(EntityPropertyMain.TASKINGPARAMETERS, tableClass, table -> table.colTaskingParameters);
        addEntry(NavigationPropertyMain.ACTUATOR, tableClass, AbstractTableTaskingCapabilities::getActuatorId);
        addEntry(NavigationPropertyMain.THING, tableClass, AbstractTableTaskingCapabilities::getThingId);
        addEntry(NavigationPropertyMain.TASKS, tableClass, AbstractTableTaskingCapabilities::getId);
    }

    private void initTasks() {
        Class<? extends AbstractTableTasks> tableClass = tableCollection.getTableTasks().getClass();
        addEntry(EntityPropertyMain.ID, tableClass, AbstractTableTasks::getId);
        addEntry(EntityPropertyMain.SELFLINK, tableClass, AbstractTableTasks::getId);
        addEntry(EntityPropertyMain.CREATIONTIME, tableClass, table -> table.colCreationTime);
        addEntry(EntityPropertyMain.TASKINGPARAMETERS, tableClass, table -> table.colTaskingParameters);
        addEntry(NavigationPropertyMain.TASKINGCAPABILITY, tableClass, AbstractTableTasks::getTaskingCapabilityId);
    }

    private void initThings() {
        Class<? extends AbstractTableThings> tableClass = tableCollection.getTableThings().getClass();
        addEntry(EntityPropertyMain.ID, tableClass, AbstractTableThings::getId);
        addEntry(EntityPropertyMain.SELFLINK, tableClass, AbstractTableThings::getId);
        addEntry(EntityPropertyMain.NAME, tableClass, table -> table.colName);
        addEntry(EntityPropertyMain.DESCRIPTION, tableClass, table -> table.colDescription);
        addEntry(EntityPropertyMain.PROPERTIES, tableClass, table -> table.colProperties);
        addEntry(NavigationPropertyMain.DATASTREAMS, tableClass, AbstractTableThings::getId);
        addEntry(NavigationPropertyMain.HISTORICALLOCATIONS, tableClass, AbstractTableThings::getId);
        addEntry(NavigationPropertyMain.LOCATIONS, tableClass, AbstractTableThings::getId);
        addEntry(NavigationPropertyMain.MULTIDATASTREAMS, tableClass, AbstractTableThings::getId);
        addEntry(NavigationPropertyMain.TASKINGCAPABILITIES, tableClass, AbstractTableThings::getId);
    }

    public String getBasicPersistenceType() {
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
        Collection<Field> result = target;
        List<ExpressionFactory> list = allForClass.get(table.getClass());
        if (result == null) {
            result = new ArrayList<>();
        }
        for (ExpressionFactory f : list) {
            result.add(f.get(table));
        }
        return result;
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
        Collection<Field> result = target;
        Map<Class, Map<String, ExpressionFactory>> innerMap = epMapSelect.get(property);
        if (innerMap == null) {
            return result;
        }
        Map<String, ExpressionFactory> coreMap = innerMap.get(table.getClass());
        if (result == null) {
            result = new ArrayList<>();
        }
        for (Map.Entry<String, ExpressionFactory> es : coreMap.entrySet()) {
            result.add(es.getValue().get(table));
        }
        return result;
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
    public Map<String, Field> getAllFieldsForProperty(EntityPropertyMain property, Table table, Map<String, Field> target) {
        Map<String, Field> result = target;
        Map<Class, Map<String, ExpressionFactory>> innerMap = epMapAll.get(property);
        if (innerMap == null) {
            throw new IllegalArgumentException("We do not know any property called " + property.toString());
        }
        Map<String, ExpressionFactory> coreMap = innerMap.get(table.getClass());
        if (coreMap == null) {
            throw new IllegalArgumentException("No property called " + property.toString() + " for " + table.getClass());
        }
        if (result == null) {
            result = new LinkedHashMap<>();
        }
        for (Map.Entry<String, ExpressionFactory> es : coreMap.entrySet()) {
            result.put(es.getKey(), es.getValue().get(table));
        }
        return result;
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
    private <T> void addEntry(Property property, Class<? extends T> clazz, ExpressionFactory<T> factory) {
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
    private <T> void addEntry(Property property, Class<? extends T> clazz, String name, ExpressionFactory<T> factory) {
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
    private <T> void addEntryNoSelect(Property property, Class<? extends T> clazz, String name, ExpressionFactory<T> factory) {
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
        String key = name;
        if (key == null) {
            key = Integer.toString(coreMap.size());
        }
        coreMap.put(key, factory);
    }

    public static FieldWrapper wrapField(Field field) {
        Class fieldType = field.getType();
        if (OffsetDateTime.class.isAssignableFrom(fieldType)) {
            return new StaDateTimeWrapper(field);
        }
        return new SimpleFieldWrapper(field);
    }

    private static interface ExpressionFactory<T> {

        Field get(T table);
    }

}
