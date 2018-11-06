/*
 * Copyright (C) 2016 Fraunhofer IOSB
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.sta.util;

import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.NavigationProperty;
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
    private static final Map<EntityType, Map<EntityType, NavigationProperty>> navigationMap = new EnumMap<>(EntityType.class);

    static {
        Map<EntityType, NavigationProperty> navPropsForType = getNavPropsForType(EntityType.DATASTREAM);
        navPropsForType.put(EntityType.SENSOR, NavigationProperty.SENSOR);
        navPropsForType.put(EntityType.OBSERVEDPROPERTY, NavigationProperty.OBSERVEDPROPERTY);
        navPropsForType.put(EntityType.OBSERVATION, NavigationProperty.OBSERVATIONS);
        navPropsForType.put(EntityType.THING, NavigationProperty.THING);

        navPropsForType = getNavPropsForType(EntityType.MULTIDATASTREAM);
        navPropsForType.put(EntityType.SENSOR, NavigationProperty.SENSOR);
        navPropsForType.put(EntityType.OBSERVEDPROPERTY, NavigationProperty.OBSERVEDPROPERTIES);
        navPropsForType.put(EntityType.OBSERVATION, NavigationProperty.OBSERVATIONS);
        navPropsForType.put(EntityType.THING, NavigationProperty.THING);

        navPropsForType = getNavPropsForType(EntityType.THING);
        navPropsForType.put(EntityType.HISTORICALLOCATION, NavigationProperty.HISTORICALLOCATIONS);
        navPropsForType.put(EntityType.LOCATION, NavigationProperty.LOCATIONS);
        navPropsForType.put(EntityType.DATASTREAM, NavigationProperty.DATASTREAMS);
        navPropsForType.put(EntityType.MULTIDATASTREAM, NavigationProperty.MULTIDATASTREAMS);

        navPropsForType = getNavPropsForType(EntityType.LOCATION);
        navPropsForType.put(EntityType.THING, NavigationProperty.THINGS);
        navPropsForType.put(EntityType.HISTORICALLOCATION, NavigationProperty.HISTORICALLOCATIONS);

        navPropsForType = getNavPropsForType(EntityType.HISTORICALLOCATION);
        navPropsForType.put(EntityType.THING, NavigationProperty.THINGS);
        navPropsForType.put(EntityType.LOCATION, NavigationProperty.LOCATIONS);

        navPropsForType = getNavPropsForType(EntityType.SENSOR);
        navPropsForType.put(EntityType.DATASTREAM, NavigationProperty.DATASTREAMS);
        navPropsForType.put(EntityType.MULTIDATASTREAM, NavigationProperty.MULTIDATASTREAMS);

        navPropsForType = getNavPropsForType(EntityType.OBSERVEDPROPERTY);
        navPropsForType.put(EntityType.DATASTREAM, NavigationProperty.DATASTREAMS);
        navPropsForType.put(EntityType.MULTIDATASTREAM, NavigationProperty.MULTIDATASTREAMS);

        navPropsForType = getNavPropsForType(EntityType.OBSERVATION);
        navPropsForType.put(EntityType.DATASTREAM, NavigationProperty.DATASTREAM);
        navPropsForType.put(EntityType.MULTIDATASTREAM, NavigationProperty.MULTIDATASTREAM);
        navPropsForType.put(EntityType.FEATUREOFINTEREST, NavigationProperty.FEATUREOFINTEREST);

        navPropsForType = getNavPropsForType(EntityType.FEATUREOFINTEREST);
        navPropsForType.put(EntityType.OBSERVATION, NavigationProperty.OBSERVATIONS);
    }

    private PathHelper() {
        // Utility class, not to be instantiated.
    }

    private static Map<EntityType, NavigationProperty> getNavPropsForType(EntityType source) {
        return navigationMap.computeIfAbsent(
                source,
                t -> new EnumMap<>(EntityType.class)
        );
    }

    public static NavigationProperty getNavigationProperty(EntityType source, EntityType destination) {
        Map<EntityType, NavigationProperty> destMap = navigationMap.get(source);
        if (destMap == null) {
            LOGGER.error("Unknown entity type: {}.", source);
            return null;
        }
        NavigationProperty navProp = destMap.get(destination);
        if (navProp == null) {
            LOGGER.error("No link known between {} and {}.", source, destination);
        }
        return navProp;
    }
}
