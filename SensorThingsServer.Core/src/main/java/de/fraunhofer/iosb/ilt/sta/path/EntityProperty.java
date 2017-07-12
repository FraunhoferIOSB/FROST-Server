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
public enum EntityProperty implements Property {

    Description,
    Definition,
    EncodingType,
    Feature,
    Id("id", "@iot.id"),
    Location,
    Metadata,
    MultiObservationDataTypes,
    Name,
    ObservationType,
    ObservedArea,
    PhenomenonTime,
    Parameters(true),
    Properties(true),
    Result(true),
    ResultTime,
    ResultQuality,
    SelfLink("@iot.selfLink", "@iot.selfLink"),
    Time,
    UnitOfMeasurement(true),
    UnitOfMeasurements(true),
    ValidTime;
    /**
     * The name of this property as used in URLs.
     */
    public final String name;
    public final String getterName;
    public final String setterName;
    public final boolean hasCustomProperties;
    private final Collection<String> aliases;

    private EntityProperty() {
        this(false);
    }

    private EntityProperty(boolean hasCustomProperties) {
        this.aliases = new ArrayList<>();
        this.aliases.add(name());
        this.name = name().substring(0, 1).toLowerCase() + name().substring(1);
        this.getterName = "get" + name();
        this.setterName = "set" + name();
        this.hasCustomProperties = hasCustomProperties;
    }

    private EntityProperty(String name, String... aliases) {
        this.aliases = new ArrayList<>();
        this.aliases.add(name());
        this.name = name;
        this.aliases.addAll(Arrays.asList(aliases));
        this.getterName = "get" + name();
        this.setterName = "set" + name();
        this.hasCustomProperties = false;
    }

    public static EntityProperty fromString(String propertyName) {
        for (EntityProperty property : EntityProperty.values()) {
            for (String alias : property.aliases) {
                if (propertyName.equalsIgnoreCase(alias)) {
                    return property;
                }
            }
        }
        throw new IllegalArgumentException("no entity property with name '" + propertyName + "'");
    }

    @Override
    public String getName() {
        return name;
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
