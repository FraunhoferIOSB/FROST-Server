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
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * @author scf
 * @param <J> The type of the ID fields.
 */
public class TableCollection<J extends Comparable> {

    public final AbstractTableActuators<J> tableActuators;
    public final AbstractTableDatastreams<J> tableDatastreams;
    public final AbstractTableFeatures<J> tableFeatures;
    public final AbstractTableHistLocations<J> tableHistLocations;
    public final AbstractTableLocations<J> tableLocations;
    public final AbstractTableLocationsHistLocations<J> tableLocationsHistLocations;
    public final AbstractTableMultiDatastreams<J> tableMultiDatastreams;
    public final AbstractTableMultiDatastreamsObsProperties<J> tableMultiDatastreamsObsProperties;
    public final AbstractTableObservations<J> tableObservations;
    public final AbstractTableObsProperties<J> tableObsProperties;
    public final AbstractTableSensors<J> tableSensors;
    public final AbstractTableTasks<J> tableTasks;
    public final AbstractTableTaskingCapabilities<J> tableTaskingCapabilities;
    public final AbstractTableThings<J> tableThings;
    public final AbstractTableThingsLocations<J> tableThingsLocations;
    public final Map<EntityType, StaMainTable<J>> tablesByType;

    public TableCollection(
            AbstractTableActuators<J> tableActuators,
            AbstractTableDatastreams<J> tableDatastreams,
            AbstractTableFeatures<J> tableFeatures,
            AbstractTableHistLocations<J> tableHistLocations,
            AbstractTableLocations<J> tableLocations,
            AbstractTableLocationsHistLocations<J> tableLocationsHistLocations,
            AbstractTableMultiDatastreams<J> tableMultiDatastreams,
            AbstractTableMultiDatastreamsObsProperties<J> tableMultiDatastreamsObsProperties,
            AbstractTableObservations<J> tableObservations,
            AbstractTableObsProperties<J> tableObsProperties,
            AbstractTableSensors<J> tableSensors,
            AbstractTableTasks<J> tableTasks,
            AbstractTableTaskingCapabilities<J> tableTaskingCapabilities,
            AbstractTableThings<J> tableThings,
            AbstractTableThingsLocations<J> tableThingsLocations
    ) {
        this.tableActuators = tableActuators;
        this.tableDatastreams = tableDatastreams;
        this.tableFeatures = tableFeatures;
        this.tableHistLocations = tableHistLocations;
        this.tableLocations = tableLocations;
        this.tableLocationsHistLocations = tableLocationsHistLocations;
        this.tableMultiDatastreams = tableMultiDatastreams;
        this.tableMultiDatastreamsObsProperties = tableMultiDatastreamsObsProperties;
        this.tableSensors = tableSensors;
        this.tableObservations = tableObservations;
        this.tableObsProperties = tableObsProperties;
        this.tableTasks = tableTasks;
        this.tableTaskingCapabilities = tableTaskingCapabilities;
        this.tableThings = tableThings;
        this.tableThingsLocations = tableThingsLocations;

        tablesByType = Collections.unmodifiableMap(createMap());
    }

    public StaMainTable<J> getTableForType(EntityType type) {
        return tablesByType.get(type);
    }

    private Map<EntityType, StaMainTable<J>> createMap() {
        EnumMap<EntityType, StaMainTable<J>> map = new EnumMap<>(EntityType.class);
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

    private void addAndInit(Map<EntityType, StaMainTable<J>> map, EntityType type, StaTableAbstract<J> table) {
        map.put(type, table);
        table.setTables(this);
    }
}
