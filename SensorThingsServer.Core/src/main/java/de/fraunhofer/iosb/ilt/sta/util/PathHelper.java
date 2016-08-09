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

/**
 *
 * @author jab
 */
public class PathHelper {

    private PathHelper() {

    }

    public static NavigationProperty getNavigationProperty(EntityType source, EntityType destination) {
        switch (source) {
            case Datastream:
                switch (destination) {
                    case Sensor:
                        return NavigationProperty.Sensor;
                    case ObservedProperty:
                        return NavigationProperty.ObservedProperty;
                    case Observation:
                        return NavigationProperty.Observations;
                    case Thing:
                        return NavigationProperty.Thing;
                }
            case Thing:
                switch (destination) {
                    case HistoricalLocation:
                        return NavigationProperty.HistoricalLocations;
                    case Location:
                        return NavigationProperty.Location;
                    case Datastream:
                        return NavigationProperty.Datastreams;
                }
            case Location:
                switch (destination) {
                    case Thing:
                        return NavigationProperty.Things;
                    case HistoricalLocation:
                        return NavigationProperty.HistoricalLocations;
                }
            case HistoricalLocation:
                switch (destination) {
                    case Thing:
                        return NavigationProperty.Things;
                    case Location:
                        return NavigationProperty.Location;
                }
            case Sensor:
                switch (destination) {
                    case Datastream:
                        return NavigationProperty.Datastreams;
                }
            case ObservedProperty:
                switch (destination) {
                    case Datastream:
                        return NavigationProperty.Datastreams;
                }
            case Observation:
                switch (destination) {
                    case Datastream:
                        return NavigationProperty.Datastream;
                    case FeatureOfInterest:
                        return NavigationProperty.FeatureOfInterest;
                }
            case FeatureOfInterest:
                switch (destination) {
                    case Observation:
                        return NavigationProperty.Observations;
                }
        }
        return null;
    }
}
