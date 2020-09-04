/*
 * Copyright (C) 2018 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * @author scf
 * @param <J> The type of the ID fields.
 */
public class TableCollection<J extends Comparable> {

    private static final String CHANGE_AFTER_INIT = "setters or init can not be called after init.";
    private final String basicPersistenceType;

    private AbstractTableActuators<J> tableActuators;
    private AbstractTableDatastreams<J> tableDatastreams;
    private AbstractTableFeatures<J> tableFeatures;
    private AbstractTableHistLocations<J> tableHistLocations;
    private AbstractTableLocations<J> tableLocations;
    private AbstractTableLocationsHistLocations<J> tableLocationsHistLocations;
    private AbstractTableMultiDatastreams<J> tableMultiDatastreams;
    private AbstractTableMultiDatastreamsObsProperties<J> tableMultiDatastreamsObsProperties;
    private AbstractTableObservations<J> tableObservations;
    private AbstractTableObsProperties<J> tableObsProperties;
    private AbstractTableSensors<J> tableSensors;
    private AbstractTableTasks<J> tableTasks;
    private AbstractTableTaskingCapabilities<J> tableTaskingCapabilities;
    private AbstractTableThings<J> tableThings;
    private AbstractTableThingsLocations<J> tableThingsLocations;
    private Map<EntityType, StaMainTable<J,?, ?>> tablesByType;

    public TableCollection(String basicPersistenceType) {
        this.basicPersistenceType = basicPersistenceType;
    }

    public TableCollection<J> init() {
        if (tablesByType != null) {
            throw new IllegalArgumentException(CHANGE_AFTER_INIT);
        }
        tablesByType = Collections.unmodifiableMap(createMap());
        return this;
    }

    public String getBasicPersistenceType() {
        return basicPersistenceType;
    }

    public StaMainTable<J,?, ?> getTableForType(EntityType type) {
        return tablesByType.get(type);
    }
    
    public Collection<StaMainTable<J, ?, ?>> getAllTables() {
        return tablesByType.values();
    }

    private Map<EntityType, StaMainTable<J,?, ?>> createMap() {
        EnumMap<EntityType, StaMainTable<J,?, ?>> map = new EnumMap<>(EntityType.class);
        addAndInit(map, EntityType.ACTUATOR, tableActuators);
        addAndInit(map, EntityType.DATASTREAM, tableDatastreams);
        addAndInit(map, EntityType.FEATUREOFINTEREST, tableFeatures);
        addAndInit(map, EntityType.HISTORICALLOCATION, tableHistLocations);
        addAndInit(map, EntityType.LOCATION, tableLocations);
        addAndInit(map, EntityType.MULTIDATASTREAM, tableMultiDatastreams);
        addAndInit(map, EntityType.OBSERVATION, tableObservations);
        addAndInit(map, EntityType.OBSERVEDPROPERTY, this.tableObsProperties);
        addAndInit(map, EntityType.SENSOR, tableSensors);
        addAndInit(map, EntityType.TASK, tableTasks);
        addAndInit(map, EntityType.TASKINGCAPABILITY, tableTaskingCapabilities);
        addAndInit(map, EntityType.THING, tableThings);
        return map;
    }

    private void addAndInit(Map<EntityType, StaMainTable<J,?, ?>> map, EntityType type, StaTableAbstract<J,?, ?> table) {
        map.put(type, table);
        table.setTables(this);
    }

    /**
     * @return the tableActuators
     */
    public AbstractTableActuators<J> getTableActuators() {
        return tableActuators;
    }

    /**
     * @param tableActuators the tableActuators to set
     * @return this
     */
    public TableCollection<J> setTableActuators(AbstractTableActuators<J> tableActuators) {
        if (tablesByType != null) {
            throw new IllegalArgumentException(CHANGE_AFTER_INIT);
        }
        this.tableActuators = tableActuators;
        return this;
    }

    /**
     * @return the tableDatastreams
     */
    public AbstractTableDatastreams<J> getTableDatastreams() {
        return tableDatastreams;
    }

    /**
     * @param tableDatastreams the tableDatastreams to set
     * @return this
     */
    public TableCollection<J> setTableDatastreams(AbstractTableDatastreams<J> tableDatastreams) {
        if (tablesByType != null) {
            throw new IllegalArgumentException(CHANGE_AFTER_INIT);
        }
        this.tableDatastreams = tableDatastreams;
        return this;
    }

    /**
     * @return the tableFeatures
     */
    public AbstractTableFeatures<J> getTableFeatures() {
        return tableFeatures;
    }

    /**
     * @param tableFeatures the tableFeatures to set
     * @return this
     */
    public TableCollection<J> setTableFeatures(AbstractTableFeatures<J> tableFeatures) {
        if (tablesByType != null) {
            throw new IllegalArgumentException(CHANGE_AFTER_INIT);
        }
        this.tableFeatures = tableFeatures;
        return this;
    }

    /**
     * @return the tableHistLocations
     */
    public AbstractTableHistLocations<J> getTableHistLocations() {
        return tableHistLocations;
    }

    /**
     * @param tableHistLocations the tableHistLocations to set
     * @return this
     */
    public TableCollection<J> setTableHistLocations(AbstractTableHistLocations<J> tableHistLocations) {
        if (tablesByType != null) {
            throw new IllegalArgumentException(CHANGE_AFTER_INIT);
        }
        this.tableHistLocations = tableHistLocations;
        return this;
    }

    /**
     * @return the tableLocations
     */
    public AbstractTableLocations<J> getTableLocations() {
        return tableLocations;
    }

    /**
     * @param tableLocations the tableLocations to set
     * @return this
     */
    public TableCollection<J> setTableLocations(AbstractTableLocations<J> tableLocations) {
        if (tablesByType != null) {
            throw new IllegalArgumentException(CHANGE_AFTER_INIT);
        }
        this.tableLocations = tableLocations;
        return this;
    }

    /**
     * @return the tableLocationsHistLocations
     */
    public AbstractTableLocationsHistLocations<J> getTableLocationsHistLocations() {
        return tableLocationsHistLocations;
    }

    /**
     * @param tableLocationsHistLocations the tableLocationsHistLocations to set
     * @return this
     */
    public TableCollection<J> setTableLocationsHistLocations(AbstractTableLocationsHistLocations<J> tableLocationsHistLocations) {
        if (tablesByType != null) {
            throw new IllegalArgumentException(CHANGE_AFTER_INIT);
        }
        this.tableLocationsHistLocations = tableLocationsHistLocations;
        return this;
    }

    /**
     * @return the tableMultiDatastreams
     */
    public AbstractTableMultiDatastreams<J> getTableMultiDatastreams() {
        return tableMultiDatastreams;
    }

    /**
     * @param tableMultiDatastreams the tableMultiDatastreams to set
     * @return this
     */
    public TableCollection<J> setTableMultiDatastreams(AbstractTableMultiDatastreams<J> tableMultiDatastreams) {
        if (tablesByType != null) {
            throw new IllegalArgumentException(CHANGE_AFTER_INIT);
        }
        this.tableMultiDatastreams = tableMultiDatastreams;
        return this;
    }

    /**
     * @return the tableMultiDatastreamsObsProperties
     */
    public AbstractTableMultiDatastreamsObsProperties<J> getTableMultiDatastreamsObsProperties() {
        return tableMultiDatastreamsObsProperties;
    }

    /**
     * @param tableMultiDatastreamsObsProperties the
     * tableMultiDatastreamsObsProperties to set
     * @return this
     */
    public TableCollection<J> setTableMultiDatastreamsObsProperties(AbstractTableMultiDatastreamsObsProperties<J> tableMultiDatastreamsObsProperties) {
        if (tablesByType != null) {
            throw new IllegalArgumentException(CHANGE_AFTER_INIT);
        }
        this.tableMultiDatastreamsObsProperties = tableMultiDatastreamsObsProperties;
        return this;
    }

    /**
     * @return the tableObservations
     */
    public AbstractTableObservations<J> getTableObservations() {
        return tableObservations;
    }

    /**
     * @param tableObservations the tableObservations to set
     * @return this
     */
    public TableCollection<J> setTableObservations(AbstractTableObservations<J> tableObservations) {
        if (tablesByType != null) {
            throw new IllegalArgumentException(CHANGE_AFTER_INIT);
        }
        this.tableObservations = tableObservations;
        return this;
    }

    /**
     * @return the tableObsProperties
     */
    public AbstractTableObsProperties<J> getTableObsProperties() {
        return tableObsProperties;
    }

    /**
     * @param tableObsProperties the tableObsProperties to set
     * @return this
     */
    public TableCollection<J> setTableObsProperties(AbstractTableObsProperties<J> tableObsProperties) {
        if (tablesByType != null) {
            throw new IllegalArgumentException(CHANGE_AFTER_INIT);
        }
        this.tableObsProperties = tableObsProperties;
        return this;
    }

    /**
     * @return the tableSensors
     */
    public AbstractTableSensors<J> getTableSensors() {
        return tableSensors;
    }

    /**
     * @param tableSensors the tableSensors to set
     * @return this
     */
    public TableCollection<J> setTableSensors(AbstractTableSensors<J> tableSensors) {
        if (tablesByType != null) {
            throw new IllegalArgumentException(CHANGE_AFTER_INIT);
        }
        this.tableSensors = tableSensors;
        return this;
    }

    /**
     * @return the tableTasks
     */
    public AbstractTableTasks<J> getTableTasks() {
        return tableTasks;
    }

    /**
     * @param tableTasks the tableTasks to set
     * @return this
     */
    public TableCollection<J> setTableTasks(AbstractTableTasks<J> tableTasks) {
        if (tablesByType != null) {
            throw new IllegalArgumentException(CHANGE_AFTER_INIT);
        }
        this.tableTasks = tableTasks;
        return this;
    }

    /**
     * @return the tableTaskingCapabilities
     */
    public AbstractTableTaskingCapabilities<J> getTableTaskingCapabilities() {
        return tableTaskingCapabilities;
    }

    /**
     * @param tableTaskingCapabilities the tableTaskingCapabilities to set
     * @return this
     */
    public TableCollection<J> setTableTaskingCapabilities(AbstractTableTaskingCapabilities<J> tableTaskingCapabilities) {
        if (tablesByType != null) {
            throw new IllegalArgumentException(CHANGE_AFTER_INIT);
        }
        this.tableTaskingCapabilities = tableTaskingCapabilities;
        return this;
    }

    /**
     * @return the tableThings
     */
    public AbstractTableThings<J> getTableThings() {
        return tableThings;
    }

    /**
     * @param tableThings the tableThings to set
     * @return this
     */
    public TableCollection<J> setTableThings(AbstractTableThings<J> tableThings) {
        if (tablesByType != null) {
            throw new IllegalArgumentException(CHANGE_AFTER_INIT);
        }
        this.tableThings = tableThings;
        return this;
    }

    /**
     * @return the tableThingsLocations
     */
    public AbstractTableThingsLocations<J> getTableThingsLocations() {
        return tableThingsLocations;
    }

    /**
     * @param tableThingsLocations the tableThingsLocations to set
     * @return this
     */
    public TableCollection<J> setTableThingsLocations(AbstractTableThingsLocations<J> tableThingsLocations) {
        if (tablesByType != null) {
            throw new IllegalArgumentException(CHANGE_AFTER_INIT);
        }
        this.tableThingsLocations = tableThingsLocations;
        return this;
    }

    /**
     * @return the tablesByType
     */
    public Map<EntityType, StaMainTable<J,?, ?>> getTablesByType() {
        return tablesByType;
    }

    /**
     * @param tablesByType the tablesByType to set
     * @return this
     */
    public TableCollection<J> setTablesByType(Map<EntityType, StaMainTable<J,?, ?>> tablesByType) {
        if (tablesByType != null) {
            throw new IllegalArgumentException(CHANGE_AFTER_INIT);
        }
        this.tablesByType = tablesByType;
        return this;
    }
}
