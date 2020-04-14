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

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.LoggerFactory;

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

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(EntityProperty.class.getName());
    private static final Map<String, EntityProperty> PROPERTY_BY_NAME = new HashMap<>();

    static {
        for (EntityProperty property : EntityProperty.values()) {
            for (String alias : property.aliases) {
                PROPERTY_BY_NAME.put(alias.toLowerCase(), property);
            }
        }
    }

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
        EntityProperty property = PROPERTY_BY_NAME.get(propertyName.toLowerCase());
        if (property == null) {
            throw new IllegalArgumentException("no entity property with name '" + propertyName + "'");
        }
        return property;
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
    public Object getFrom(Entity entity) {
        try {
            return MethodUtils.invokeMethod(entity, getterName);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            LOGGER.error("Failed to execute getter {} on {}", getterName, entity);
            LOGGER.trace("Exception", ex);
            return null;

        }
    }

    @Override
    public void setOn(Entity entity, Object value) {
        try {
            MethodUtils.invokeMethod(entity, setterName, value);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            LOGGER.error("Failed to execute getter {} on {}", getterName, entity);
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public boolean isSetOn(Entity entity) {
        try {
            return (boolean) MethodUtils.invokeMethod(entity, isSetName);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            LOGGER.error("Failed to execute getter {} on {}", getterName, entity);
            LOGGER.trace("Exception", ex);
            return false;

        }
    }

}
