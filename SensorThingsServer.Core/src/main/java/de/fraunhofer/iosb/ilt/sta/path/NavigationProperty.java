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
package de.fraunhofer.iosb.ilt.sta.path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author jab
 */
public enum NavigationProperty implements Property {

    Datastream(EntityType.Datastream, false),
    Datastreams(EntityType.Datastream, true),
    FeatureOfInterest(EntityType.FeatureOfInterest, false),
    HistoricalLocations(EntityType.HistoricalLocation, true),
    Location(EntityType.Location, false),
    Locations(EntityType.Location, true),
    Observations(EntityType.Observation, true),
    ObservedProperty(EntityType.ObservedProperty, false),
    Sensor(EntityType.Sensor, false),
    Thing(EntityType.Thing, false),
    Things(EntityType.Thing, true);

    private final Collection<String> aliases;
    /**
     * The type of entity that this navigation property points to.
     */
    public final EntityType type;
    public final String getterName;
    public final String setterName;
    public final boolean isSet;

    private NavigationProperty(EntityType type, boolean isSet) {
        this.aliases = new ArrayList<>();
        this.aliases.add(toString());
        this.type = type;
        this.isSet = isSet;
        this.getterName = "get" + name();
        this.setterName = "set" + name();
    }

    private NavigationProperty(EntityType type, boolean isSet, String... aliases) {
        this(type, isSet);
        this.aliases.addAll(Arrays.asList(aliases));
    }

    public static NavigationProperty fromString(String propertyName) {
        for (NavigationProperty property : NavigationProperty.values()) {
            for (String alias : property.aliases) {
                if (propertyName.equalsIgnoreCase(alias)) {
                    return property;
                }
            }
        }
        throw new IllegalArgumentException("no navigation property with name '" + propertyName + "'");
    }

    public EntityType getType() {
        return type;
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public String getGetterName() {
        return getterName;
    }

    @Override
    public String getSetterName() {
        return setterName;
    }

}
