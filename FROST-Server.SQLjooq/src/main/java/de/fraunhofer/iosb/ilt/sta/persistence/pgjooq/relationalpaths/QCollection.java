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
package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths;

import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * @author scf
 * @param <J> The type of the ID fields.
 */
public class QCollection<J> {

    public final AbstractTableDatastreams<J> qDatastreams;
    public final AbstractTableFeatures<J> qFeatures;
    public final AbstractTableHistLocations<J> qHistLocations;
    public final AbstractTableLocations<J> qLocations;
    public final AbstractTableLocationsHistLocations<J> qLocationsHistLocations;
    public final AbstractTableMultiDatastreams<J> qMultiDatastreams;
    public final AbstractTableMultiDatastreamsObsProperties<J> qMultiDatastreamsObsProperties;
    public final AbstractTableObservations<J> qObservations;
    public final AbstractTableObsProperties<J> qObsProperties;
    public final AbstractTableSensors<J> qSensors;
    public final AbstractTableThings<J> qThings;
    public final AbstractTableThingsLocations<J> qThingsLocations;
    public final Map<EntityType, StaTable<J>> tablesByType;

    public QCollection(
            AbstractTableDatastreams<J> qDatastreams,
            AbstractTableFeatures<J> qFeatures,
            AbstractTableHistLocations<J> qHistLocations,
            AbstractTableLocations<J> qLocations,
            AbstractTableLocationsHistLocations<J> qLocationsHistLocations,
            AbstractTableMultiDatastreams<J> qMultiDatastreams,
            AbstractTableMultiDatastreamsObsProperties<J> qMultiDatastreamsObsProperties,
            AbstractTableObservations<J> qObservations,
            AbstractTableObsProperties<J> qObsProperties,
            AbstractTableSensors<J> qSensors,
            AbstractTableThings<J> qThings,
            AbstractTableThingsLocations<J> qThingsLocations
    ) {
        this.qDatastreams = qDatastreams;
        this.qFeatures = qFeatures;
        this.qHistLocations = qHistLocations;
        this.qLocations = qLocations;
        this.qLocationsHistLocations = qLocationsHistLocations;
        this.qMultiDatastreams = qMultiDatastreams;
        this.qMultiDatastreamsObsProperties = qMultiDatastreamsObsProperties;
        this.qSensors = qSensors;
        this.qObservations = qObservations;
        this.qObsProperties = qObsProperties;
        this.qThings = qThings;
        this.qThingsLocations = qThingsLocations;

        tablesByType = Collections.unmodifiableMap(createMap());
    }

    private Map<EntityType, StaTable<J>> createMap() {
        EnumMap<EntityType, StaTable<J>> map = new EnumMap(EntityType.class);
        map.put(EntityType.DATASTREAM, qDatastreams);
        map.put(EntityType.FEATUREOFINTEREST, qFeatures);
        map.put(EntityType.HISTORICALLOCATION, qHistLocations);
        map.put(EntityType.LOCATION, qLocations);
        map.put(EntityType.MULTIDATASTREAM, qMultiDatastreams);
        map.put(EntityType.OBSERVATION, qObservations);
        map.put(EntityType.OBSERVEDPROPERTY, this.qObsProperties);
        map.put(EntityType.SENSOR, qSensors);
        map.put(EntityType.THING, qThings);
        return map;
    }

}
