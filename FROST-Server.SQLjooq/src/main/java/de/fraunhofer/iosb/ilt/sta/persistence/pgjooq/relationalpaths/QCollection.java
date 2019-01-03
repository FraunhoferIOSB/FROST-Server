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
import org.jooq.Record;

/**
 * @author scf
 * @param <J> The type of the ID fields.
 */
public class QCollection<J> {

    public final AbstractTableDatastreams<J> qDatastreams;
    public final AbstractTableMultiDatastreams<J> qMultiDatastreams;
    public final AbstractTableThings<J> qThings;
    public final AbstractTableFeatures<J> qFeatures;
    public final AbstractTableHistLocations<J> qHistLocations;
    public final AbstractTableLocations<J> qLocations;
    public final AbstractTableSensors<J> qSensors;
    public final AbstractTableObservations<J> qObservations;
    public final AbstractTableObsProperties<J> qObsProperties;
    public final AbstractTableLocationsHistLocations<J> qLocationsHistLocations;
    public final AbstractTableMultiDatastreamsObsProperties<J> qMultiDatastreamsObsProperties;
    public final AbstractTableThingsLocations<J> qThingsLocations;
    public final Map<EntityType, StaTable<J, ? extends Record>> tablesByType;

    public final AbstractRecordDatastreams<J> recordDatastreams;
    public final AbstractRecordFeatures<J> recordFeatures;
    public final AbstractRecordHistLocations<J> recordHistLocations;
    public final AbstractRecordLocations<J> recordLocations;
    public final AbstractRecordLocationsHistLocations<J> recordLocationsHistLocations;
    public final AbstractRecordMultiDatastreams<J> recordMultiDatastreams;
    public final AbstractRecordMultiDatastreamsObsProperties<J> recordMultiDatastreamsObsProperties;
    public final AbstractRecordObsProperties<J> recordObsProperties;
    public final AbstractRecordObservations<J> recordObservations;
    public final AbstractRecordSensors<J> recordSensors;
    public final AbstractRecordThings<J> recordThings;
    public final AbstractRecordThingsLocations<J> recordThingsLocations;

    public QCollection(
            AbstractTableDatastreams<J> qDatastreams,
            AbstractTableMultiDatastreams<J> qMultiDatastreams,
            AbstractTableThings<J> qThings,
            AbstractTableFeatures<J> qFeatures,
            AbstractTableHistLocations<J> qHistLocations,
            AbstractTableLocations<J> qLocations, AbstractTableSensors<J> qSensors,
            AbstractTableObservations<J> qObservations,
            AbstractTableObsProperties<J> qObsProperties,
            AbstractTableLocationsHistLocations<J> qLocationsHistLocations,
            AbstractTableMultiDatastreamsObsProperties<J> qMultiDatastreamsObsProperties,
            AbstractTableThingsLocations<J> qThingsLocations,
            AbstractRecordDatastreams<J> recordDatastreams,
            AbstractRecordFeatures<J> recordFeatures, AbstractRecordHistLocations<J> recordHistLocations, AbstractRecordLocations<J> recordLocations, AbstractRecordLocationsHistLocations<J> recordLocationsHistLocations, AbstractRecordMultiDatastreams<J> recordMultiDatastreams, AbstractRecordMultiDatastreamsObsProperties<J> recordMultiDatastreamsObsProperties, AbstractRecordObsProperties<J> recordObsProperties, AbstractRecordObservations<J> recordObservations, AbstractRecordSensors<J> recordSensors, AbstractRecordThings<J> recordThings, AbstractRecordThingsLocations<J> recordThingsLocations) {
        this.qDatastreams = qDatastreams;
        this.qMultiDatastreams = qMultiDatastreams;
        this.qThings = qThings;
        this.qFeatures = qFeatures;
        this.qHistLocations = qHistLocations;
        this.qLocations = qLocations;
        this.qSensors = qSensors;
        this.qObservations = qObservations;
        this.qObsProperties = qObsProperties;
        this.qLocationsHistLocations = qLocationsHistLocations;
        this.qMultiDatastreamsObsProperties = qMultiDatastreamsObsProperties;
        this.qThingsLocations = qThingsLocations;

        this.recordDatastreams = recordDatastreams;
        this.recordFeatures = recordFeatures;
        this.recordHistLocations = recordHistLocations;
        this.recordLocations = recordLocations;
        this.recordLocationsHistLocations = recordLocationsHistLocations;
        this.recordMultiDatastreams = recordMultiDatastreams;
        this.recordMultiDatastreamsObsProperties = recordMultiDatastreamsObsProperties;
        this.recordObsProperties = recordObsProperties;
        this.recordObservations = recordObservations;
        this.recordSensors = recordSensors;
        this.recordThings = recordThings;
        this.recordThingsLocations = recordThingsLocations;

        tablesByType = Collections.unmodifiableMap(createMap());
    }

    private Map<EntityType, StaTable<J, ? extends Record>> createMap() {
        EnumMap<EntityType, StaTable<J, ? extends Record>> map = new EnumMap(EntityType.class);
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
