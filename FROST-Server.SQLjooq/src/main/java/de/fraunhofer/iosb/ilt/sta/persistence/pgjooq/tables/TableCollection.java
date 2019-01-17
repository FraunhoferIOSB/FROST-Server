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
package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * @author scf
 * @param <J> The type of the ID fields.
 */
public class TableCollection<J> {

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
    public final AbstractTableThings<J> tableThings;
    public final AbstractTableThingsLocations<J> tableThingsLocations;
    public final Map<EntityType, StaTable<J>> tablesByType;

    public TableCollection(
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
            AbstractTableThings<J> tableThings,
            AbstractTableThingsLocations<J> tableThingsLocations
    ) {
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
        this.tableThings = tableThings;
        this.tableThingsLocations = tableThingsLocations;

        tablesByType = Collections.unmodifiableMap(createMap());
    }

    private Map<EntityType, StaTable<J>> createMap() {
        EnumMap<EntityType, StaTable<J>> map = new EnumMap(EntityType.class);
        map.put(EntityType.DATASTREAM, tableDatastreams);
        map.put(EntityType.FEATUREOFINTEREST, tableFeatures);
        map.put(EntityType.HISTORICALLOCATION, tableHistLocations);
        map.put(EntityType.LOCATION, tableLocations);
        map.put(EntityType.MULTIDATASTREAM, tableMultiDatastreams);
        map.put(EntityType.OBSERVATION, tableObservations);
        map.put(EntityType.OBSERVEDPROPERTY, this.tableObsProperties);
        map.put(EntityType.SENSOR, tableSensors);
        map.put(EntityType.THING, tableThings);
        return map;
    }

}
