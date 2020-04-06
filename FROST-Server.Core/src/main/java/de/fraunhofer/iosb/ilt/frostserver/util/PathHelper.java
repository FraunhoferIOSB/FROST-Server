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
package de.fraunhofer.iosb.ilt.frostserver.util;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import java.util.EnumMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab, scf
 */
public class PathHelper {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PathHelper.class);
    private static final Map<EntityType, Map<EntityType, NavigationPropertyMain>> navigationMap = new EnumMap<>(EntityType.class);

    static {
        Map<EntityType, NavigationPropertyMain> navPropsForType = getNavPropsForType(EntityType.ACTUATOR);
        navPropsForType.put(EntityType.TASKINGCAPABILITY, NavigationPropertyMain.TASKINGCAPABILITIES);

        navPropsForType = getNavPropsForType(EntityType.DATASTREAM);
        navPropsForType.put(EntityType.SENSOR, NavigationPropertyMain.SENSOR);
        navPropsForType.put(EntityType.OBSERVEDPROPERTY, NavigationPropertyMain.OBSERVEDPROPERTY);
        navPropsForType.put(EntityType.OBSERVATION, NavigationPropertyMain.OBSERVATIONS);
        navPropsForType.put(EntityType.THING, NavigationPropertyMain.THING);

        navPropsForType = getNavPropsForType(EntityType.MULTIDATASTREAM);
        navPropsForType.put(EntityType.SENSOR, NavigationPropertyMain.SENSOR);
        navPropsForType.put(EntityType.OBSERVEDPROPERTY, NavigationPropertyMain.OBSERVEDPROPERTIES);
        navPropsForType.put(EntityType.OBSERVATION, NavigationPropertyMain.OBSERVATIONS);
        navPropsForType.put(EntityType.THING, NavigationPropertyMain.THING);

        navPropsForType = getNavPropsForType(EntityType.TASK);
        navPropsForType.put(EntityType.TASKINGCAPABILITY, NavigationPropertyMain.TASKINGCAPABILITY);

        navPropsForType = getNavPropsForType(EntityType.TASKINGCAPABILITY);
        navPropsForType.put(EntityType.ACTUATOR, NavigationPropertyMain.ACTUATOR);
        navPropsForType.put(EntityType.TASK, NavigationPropertyMain.TASKS);
        navPropsForType.put(EntityType.THING, NavigationPropertyMain.THING);

        navPropsForType = getNavPropsForType(EntityType.THING);
        navPropsForType.put(EntityType.HISTORICALLOCATION, NavigationPropertyMain.HISTORICALLOCATIONS);
        navPropsForType.put(EntityType.LOCATION, NavigationPropertyMain.LOCATIONS);
        navPropsForType.put(EntityType.DATASTREAM, NavigationPropertyMain.DATASTREAMS);
        navPropsForType.put(EntityType.MULTIDATASTREAM, NavigationPropertyMain.MULTIDATASTREAMS);
        navPropsForType.put(EntityType.TASKINGCAPABILITY, NavigationPropertyMain.TASKINGCAPABILITIES);

        navPropsForType = getNavPropsForType(EntityType.LOCATION);
        navPropsForType.put(EntityType.THING, NavigationPropertyMain.THINGS);
        navPropsForType.put(EntityType.HISTORICALLOCATION, NavigationPropertyMain.HISTORICALLOCATIONS);

        navPropsForType = getNavPropsForType(EntityType.HISTORICALLOCATION);
        navPropsForType.put(EntityType.THING, NavigationPropertyMain.THINGS);
        navPropsForType.put(EntityType.LOCATION, NavigationPropertyMain.LOCATIONS);

        navPropsForType = getNavPropsForType(EntityType.SENSOR);
        navPropsForType.put(EntityType.DATASTREAM, NavigationPropertyMain.DATASTREAMS);
        navPropsForType.put(EntityType.MULTIDATASTREAM, NavigationPropertyMain.MULTIDATASTREAMS);

        navPropsForType = getNavPropsForType(EntityType.OBSERVEDPROPERTY);
        navPropsForType.put(EntityType.DATASTREAM, NavigationPropertyMain.DATASTREAMS);
        navPropsForType.put(EntityType.MULTIDATASTREAM, NavigationPropertyMain.MULTIDATASTREAMS);

        navPropsForType = getNavPropsForType(EntityType.OBSERVATION);
        navPropsForType.put(EntityType.DATASTREAM, NavigationPropertyMain.DATASTREAM);
        navPropsForType.put(EntityType.MULTIDATASTREAM, NavigationPropertyMain.MULTIDATASTREAM);
        navPropsForType.put(EntityType.FEATUREOFINTEREST, NavigationPropertyMain.FEATUREOFINTEREST);

        navPropsForType = getNavPropsForType(EntityType.FEATUREOFINTEREST);
        navPropsForType.put(EntityType.OBSERVATION, NavigationPropertyMain.OBSERVATIONS);
    }

    private PathHelper() {
        // Utility class, not to be instantiated.
    }

    private static Map<EntityType, NavigationPropertyMain> getNavPropsForType(EntityType source) {
        return navigationMap.computeIfAbsent(
                source,
                t -> new EnumMap<>(EntityType.class)
        );
    }

    public static NavigationPropertyMain getNavigationProperty(EntityType source, EntityType destination) {
        Map<EntityType, NavigationPropertyMain> destMap = navigationMap.get(source);
        if (destMap == null) {
            LOGGER.error("Unknown entity type: {}.", source);
            return null;
        }
        NavigationPropertyMain navProp = destMap.get(destination);
        if (navProp == null) {
            LOGGER.error("No link known between {} and {}.", source, destination);
        }
        return navProp;
    }
}
