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
package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.longid;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.*;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractRecordDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractRecordFeatures;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractRecordHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractRecordLocationsHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractRecordLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractRecordMultiDatastreamsObsProperties;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractRecordMultiDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractRecordObsProperties;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractRecordObservations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractRecordSensors;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractRecordThingsLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractRecordThings;
import org.jooq.Table;

/**
 * @author scf
 * @param <J> The type of the ID fields.
 */
public class QCollection<J> {

    public final Table<AbstractRecordDatastreams> qDatastreams;
    public final Table<AbstractRecordMultiDatastreams> qMultiDatastreams;
    public final Table<AbstractRecordThings> qThings;
    public final Table<AbstractRecordFeatures> qFeatures;
    public final Table<AbstractRecordHistLocations> qHistLocations;
    public final Table<AbstractRecordLocations> qLocations;
    public final Table<AbstractRecordSensors> qSensors;
    public final Table<AbstractRecordObservations> qObservations;
    public final Table<AbstractRecordObsProperties> qObsProperties;
    public final Table<AbstractRecordLocationsHistLocations> qLocationsHistLocations;
    public final Table<AbstractRecordMultiDatastreamsObsProperties> qMultiDatastreamsObsProperties;
    public final Table<AbstractRecordThingsLocations> qThingsLocations;

    public QCollection(Table<AbstractRecordDatastreams> qDatastreams, Table<AbstractRecordMultiDatastreams> qMultiDatastreams, Table<AbstractRecordThings> qThings, Table<AbstractRecordFeatures> qFeatures, Table<AbstractRecordHistLocations> qHistLocations, Table<AbstractRecordLocations> qLocations, Table<AbstractRecordSensors> qSensors, Table<AbstractRecordObservations> qObservations, Table<AbstractRecordObsProperties> qObsProperties, Table<AbstractRecordLocationsHistLocations> qLocationsHistLocations, Table<AbstractRecordMultiDatastreamsObsProperties> qMultiDatastreamsObsProperties, Table<AbstractRecordThingsLocations> qThingsLocations) {
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
    }

}
