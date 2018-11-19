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

import de.fraunhofer.iosb.ilt.sta.util.StringHelper;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author jab
 */
public enum NavigationProperty implements Property {

    DATASTREAM("Datastream", EntityType.DATASTREAM, false),
    DATASTREAMS("Datastreams", EntityType.DATASTREAM, true),
    MULTIDATASTREAM("MultiDatastream", EntityType.MULTIDATASTREAM, false),
    MULTIDATASTREAMS("MultiDatastreams", EntityType.MULTIDATASTREAM, true),
    FEATUREOFINTEREST("FeatureOfInterest", EntityType.FEATUREOFINTEREST, false),
    HISTORICALLOCATIONS("HistoricalLocations", EntityType.HISTORICALLOCATION, true),
    LOCATION("Location", EntityType.LOCATION, false),
    LOCATIONS("Locations", EntityType.LOCATION, true),
    OBSERVATIONS("Observations", EntityType.OBSERVATION, true),
    OBSERVEDPROPERTY("ObservedProperty", EntityType.OBSERVEDPROPERTY, false),
    OBSERVEDPROPERTIES("ObservedProperties", EntityType.OBSERVEDPROPERTY, true),
    SENSOR("Sensor", EntityType.SENSOR, false),
    THING("Thing", EntityType.THING, false),
    THINGS("Things", EntityType.THING, true);

    private final Collection<String> aliases;
    /**
     * The type of entity that this navigation property points to.
     */
    public final EntityType type;
    public final String propertyName;
    public final String getterName;
    public final String setterName;
    public final String isSetName;
    public final boolean isSet;

    private NavigationProperty(String propertyName, EntityType type, boolean isSet) {
        this.propertyName = propertyName;
        this.aliases = new ArrayList<>();
        this.aliases.add(propertyName);
        this.type = type;
        this.isSet = isSet;
        String capitalized = StringHelper.capitalize(propertyName);
        this.getterName = "get" + capitalized;
        this.setterName = "set" + capitalized;
        this.isSetName = "isSet" + capitalized;
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
        return propertyName;
    }

    @Override
    public String getJsonName() {
        return propertyName;
    }

    @Override
    public String getGetterName() {
        return getterName;
    }

    @Override
    public String getSetterName() {
        return setterName;
    }

    @Override
    public String getIsSetName() {
        return isSetName;
    }

}
