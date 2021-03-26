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

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.UrlHelper;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public enum NavigationPropertyMain implements NavigationProperty {
    ACTUATOR("Actuator", EntityType.ACTUATOR, false),
    ACTUATORS("Actuators", EntityType.ACTUATOR, true),
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
    TASK("Task", EntityType.TASK, false),
    TASKS("Tasks", EntityType.TASK, true),
    TASKINGCAPABILITY("TaskingCapability", EntityType.TASKINGCAPABILITY, false),
    TASKINGCAPABILITIES("TaskingCapabilities", EntityType.TASKINGCAPABILITY, true),
    THING("Thing", EntityType.THING, false),
    THINGS("Things", EntityType.THING, true);

    private static final Logger LOGGER = LoggerFactory.getLogger(NavigationPropertyMain.class.getName());
    private static final Map<String, NavigationPropertyMain> PROPERTY_BY_NAME = new HashMap<>();

    static {
        for (NavigationPropertyMain property : NavigationPropertyMain.values()) {
            for (String alias : property.aliases) {
                PROPERTY_BY_NAME.put(alias.toLowerCase(), property);
            }
        }
    }

    private final Collection<String> aliases;

    /**
     * The type of entity that this navigation property points to.
     */
    private final EntityType type;
    /**
     * The name of the navigation property in urls.
     */
    private final String propertyName;

    /**
     * The name of the getter to be used on entities to get this navigation
     * property.
     */
    private final String getterName;
    private final Map<Class, Method> methodsGet = new ConcurrentHashMap<>();

    /**
     * The name of the setter to be used on entities to set this navigation
     * property.
     */
    private final String setterName;
    private final Map<Class, Method> methodsSet = new ConcurrentHashMap<>();

    /**
     * The name of the "isSet" method, to check if this navigation property has
     * been set on an entity.
     */
    private final String isSetName;
    private final Map<Class, Method> methodsIsSet = new ConcurrentHashMap<>();
    /**
     * Flag indication the path is to an EntitySet.
     */
    private final boolean entitySet;

    private NavigationPropertyMain(String propertyName, EntityType type, boolean isSet) {
        this.propertyName = propertyName;
        this.aliases = new ArrayList<>();
        this.aliases.add(propertyName);
        this.type = type;
        this.entitySet = isSet;
        String capitalized = StringHelper.capitalize(propertyName);
        this.getterName = "get" + capitalized;
        this.setterName = "set" + capitalized;
        this.isSetName = "isSet" + capitalized;
    }

    public static NavigationPropertyMain fromString(String propertyName) {
        NavigationPropertyMain property = PROPERTY_BY_NAME.get(propertyName.toLowerCase());
        if (property == null) {
            throw new IllegalArgumentException("no navigation property with name '" + propertyName + "'");
        }
        return property;
    }

    @Override
    public EntityType getType() {
        return type;
    }

    @Override
    public boolean validFor(EntityType entityType) {
        return entityType.getPropertySet().contains(this);
    }

    @Override
    public boolean isEntitySet() {
        return entitySet;
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
    public Object getFrom(Entity entity) {
        try {
            return methodsGet.computeIfAbsent(
                    entity.getClass(),
                    t -> MethodUtils.getMatchingAccessibleMethod(t, getterName)
            ).invoke(entity);
        } catch (ConcurrentModificationException ex) {
            LOGGER.warn("Map modified by other thread: NP: {}, E: {}.", this, entity);
            // Try again
            return getFrom(entity);
        } catch (InvocationTargetException | IllegalAccessException ex) {
            LOGGER.error("Failed to execute getter {} on {}", getterName, entity);
            LOGGER.trace("", ex);
            return null;
        }
    }

    @Override
    public void setOn(Entity entity, Object value) {
        try {
            methodsSet.computeIfAbsent(
                    entity.getClass(),
                    t -> MethodUtils.getMatchingAccessibleMethod(t, setterName, value == null ? null : value.getClass())
            ).invoke(entity, value);
        } catch (InvocationTargetException | IllegalAccessException ex) {
            LOGGER.error("Failed to execute setter {} on {}", setterName, entity);
            LOGGER.trace("", ex);
        }
    }

    @Override
    public boolean isSetOn(Entity entity) {
        try {
            return (boolean) methodsIsSet.computeIfAbsent(
                    entity.getClass(),
                    t -> MethodUtils.getMatchingAccessibleMethod(t, isSetName)
            ).invoke(entity);
        } catch (InvocationTargetException | IllegalAccessException ex) {
            LOGGER.error("Failed to execute isSet {} on {}", isSetName, entity);
            LOGGER.trace("", ex);
            return false;
        }
    }

    @Override
    public String getNavigationLink(Entity parent) {
        String link = parent.getSelfLink() + '/' + propertyName;
        Query query = parent.getQuery();
        if (query != null && !query.getSettings().useAbsoluteNavigationLinks()) {
            ResourcePath path = query.getPath();
            String curPath = path.getServiceRootUrl() + '/' + path.getVersion().urlPart + path.getPath();
            link = UrlHelper.getRelativePath(link, curPath);
        }
        return link;
    }

}
