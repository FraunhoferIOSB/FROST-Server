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
package de.fraunhofer.iosb.ilt.frostserver.property;

import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author jab
 */
public enum EntityProperty implements Property {
    CREATIONTIME("CreationTime"),
    DESCRIPTION("Description"),
    DEFINITION("Definition"),
    ENCODINGTYPE("EncodingType"),
    FEATURE("Feature", true),
    ID("Id", "id", "@iot.id"),
    LOCATION("Location", true),
    METADATA("Metadata"),
    MULTIOBSERVATIONDATATYPES("MultiObservationDataTypes"),
    NAME("Name"),
    OBSERVATIONTYPE("ObservationType"),
    OBSERVEDAREA("ObservedArea"),
    PHENOMENONTIME("PhenomenonTime"),
    PARAMETERS("Parameters", true),
    PROPERTIES("Properties", true),
    RESULT("Result", true),
    RESULTTIME("ResultTime"),
    RESULTQUALITY("ResultQuality", true),
    SELFLINK("SelfLink", "@iot.selfLink", "@iot.selfLink"),
    TASKINGPARAMETERS("TaskingParameters", true),
    TIME("Time"),
    UNITOFMEASUREMENT("UnitOfMeasurement", true),
    UNITOFMEASUREMENTS("UnitOfMeasurements", true),
    VALIDTIME("ValidTime");

    /**
     * The entitiyName of this property as used in URLs.
     */
    public final String entitiyName;
    public final String jsonName;
    public final String getterName;
    public final String setterName;
    public final String isSetName;
    public final boolean hasCustomProperties;
    private final Collection<String> aliases;

    private EntityProperty(String codeName) {
        this(codeName, false);
    }

    private EntityProperty(String codeName, boolean hasCustomProperties) {
        this.aliases = new ArrayList<>();
        this.aliases.add(codeName);
        this.entitiyName = StringHelper.deCapitalize(codeName);
        this.jsonName = entitiyName;
        this.getterName = "get" + codeName;
        this.setterName = "set" + codeName;
        this.isSetName = "isSet" + codeName;
        this.hasCustomProperties = hasCustomProperties;
    }

    private EntityProperty(String codeName, String pathName, String jsonName, String... aliases) {
        this.aliases = new ArrayList<>();
        this.entitiyName = pathName;
        this.jsonName = jsonName;
        this.aliases.add(name());
        this.aliases.add(jsonName);
        this.aliases.addAll(Arrays.asList(aliases));
        String capitalized = StringHelper.capitalize(codeName);
        this.getterName = "get" + capitalized;
        this.setterName = "set" + capitalized;
        this.isSetName = "isSet" + capitalized;
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
        return entitiyName;
    }

    @Override
    public String getJsonName() {
        return jsonName;
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
