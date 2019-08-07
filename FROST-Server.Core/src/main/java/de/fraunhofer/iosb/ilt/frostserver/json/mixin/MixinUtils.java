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
package de.fraunhofer.iosb.ilt.frostserver.json.mixin;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.frostserver.model.Actuator;
import de.fraunhofer.iosb.ilt.frostserver.model.Datastream;
import de.fraunhofer.iosb.ilt.frostserver.model.FeatureOfInterest;
import de.fraunhofer.iosb.ilt.frostserver.model.HistoricalLocation;
import de.fraunhofer.iosb.ilt.frostserver.model.Location;
import de.fraunhofer.iosb.ilt.frostserver.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.frostserver.model.Observation;
import de.fraunhofer.iosb.ilt.frostserver.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.frostserver.model.Sensor;
import de.fraunhofer.iosb.ilt.frostserver.model.Task;
import de.fraunhofer.iosb.ilt.frostserver.model.TaskingCapability;
import de.fraunhofer.iosb.ilt.frostserver.model.Thing;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.EntitySetResult;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;

/**
 *
 * @author scf
 */
public class MixinUtils {

    private MixinUtils() {
        // Utility class, not to be instantiated.
    }

    public static void addMixins(ObjectMapper mapper) {
        mapper.addMixIn(Actuator.class, ActuatorMixIn.class);
        mapper.addMixIn(Datastream.class, DatastreamMixIn.class);
        mapper.addMixIn(MultiDatastream.class, MultiDatastreamMixIn.class);
        mapper.addMixIn(FeatureOfInterest.class, FeatureOfInterestMixIn.class);
        mapper.addMixIn(HistoricalLocation.class, HistoricalLocationMixIn.class);
        mapper.addMixIn(Location.class, LocationMixIn.class);
        mapper.addMixIn(Observation.class, ObservationMixIn.class);
        mapper.addMixIn(ObservedProperty.class, ObservedPropertyMixIn.class);
        mapper.addMixIn(Sensor.class, SensorMixIn.class);
        mapper.addMixIn(Task.class, TaskMixIn.class);
        mapper.addMixIn(TaskingCapability.class, TaskingCapabilityMixIn.class);
        mapper.addMixIn(Thing.class, ThingMixIn.class);
        mapper.addMixIn(UnitOfMeasurement.class, UnitOfMeasurementMixIn.class);
        mapper.addMixIn(EntitySetResult.class, EntitySetResultMixIn.class);
    }

}
