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
    private static final String ERROR_NO_LINK = "Entity of type {} can not contain {}.";

    private PathHelper() {

    }

    public static NavigationProperty getNavigationProperty(EntityType source, EntityType destination) {
        switch (source) {
            case DATASTREAM:
                switch (destination) {
                    case SENSOR:
                        return NavigationProperty.SENSOR;
                    case OBSERVEDPROPERTY:
                        return NavigationProperty.OBSERVEDPROPERTY;
                    case OBSERVATION:
                        return NavigationProperty.OBSERVATIONS;
                    case THING:
                        return NavigationProperty.THING;
                    default:
                        LOGGER.error(ERROR_NO_LINK, source, destination);
                        break;
                }
                break;

            case MULTIDATASTREAM:
                switch (destination) {
                    case SENSOR:
                        return NavigationProperty.SENSOR;
                    case OBSERVEDPROPERTY:
                        return NavigationProperty.OBSERVEDPROPERTIES;
                    case OBSERVATION:
                        return NavigationProperty.OBSERVATIONS;
                    case THING:
                        return NavigationProperty.THING;
                    default:
                        LOGGER.error(ERROR_NO_LINK, source, destination);
                        break;
                }
                break;

            case THING:
                switch (destination) {
                    case HISTORICALLOCATION:
                        return NavigationProperty.HISTORICALLOCATIONS;
                    case LOCATION:
                        return NavigationProperty.LOCATIONS;
                    case DATASTREAM:
                        return NavigationProperty.DATASTREAMS;
                    case MULTIDATASTREAM:
                        return NavigationProperty.MULTIDATASTREAMS;
                    default:
                        LOGGER.error(ERROR_NO_LINK, source, destination);
                        break;
                }
                break;

            case LOCATION:
                switch (destination) {
                    case THING:
                        return NavigationProperty.THINGS;
                    case HISTORICALLOCATION:
                        return NavigationProperty.HISTORICALLOCATIONS;
                    default:
                        LOGGER.error(ERROR_NO_LINK, source, destination);
                        break;
                }
                break;

            case HISTORICALLOCATION:
                switch (destination) {
                    case THING:
                        return NavigationProperty.THINGS;
                    case LOCATION:
                        return NavigationProperty.LOCATIONS;
                    default:
                        LOGGER.error(ERROR_NO_LINK, source, destination);
                        break;
                }
                break;

            case SENSOR:
                switch (destination) {
                    case DATASTREAM:
                        return NavigationProperty.DATASTREAMS;
                    case MULTIDATASTREAM:
                        return NavigationProperty.MULTIDATASTREAMS;
                    default:
                        LOGGER.error(ERROR_NO_LINK, source, destination);
                        break;
                }
                break;

            case OBSERVEDPROPERTY:
                switch (destination) {
                    case DATASTREAM:
                        return NavigationProperty.DATASTREAMS;
                    case MULTIDATASTREAM:
                        return NavigationProperty.MULTIDATASTREAMS;
                    default:
                        LOGGER.error(ERROR_NO_LINK, source, destination);
                        break;
                }
                break;

            case OBSERVATION:
                switch (destination) {
                    case MULTIDATASTREAM:
                        return NavigationProperty.MULTIDATASTREAM;
                    case DATASTREAM:
                        return NavigationProperty.DATASTREAM;
                    case FEATUREOFINTEREST:
                        return NavigationProperty.FEATUREOFINTEREST;
                    default:
                        LOGGER.error(ERROR_NO_LINK, source, destination);
                        break;
                }
                break;

            case FEATUREOFINTEREST:
                if (destination == EntityType.OBSERVATION) {
                    return NavigationProperty.OBSERVATIONS;
                } else {
                    LOGGER.error(ERROR_NO_LINK, source, destination);
                }
                break;

            default:
                LOGGER.error("Unknown entity type: {}.", source);
        }
        LOGGER.warn("No link known between {} and {}.", source, destination);
        return null;
    }
}
