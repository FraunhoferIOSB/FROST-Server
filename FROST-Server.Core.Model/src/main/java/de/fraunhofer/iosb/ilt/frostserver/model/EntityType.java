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
package de.fraunhofer.iosb.ilt.frostserver.model;

import de.fraunhofer.iosb.ilt.frostserver.extensions.Extension;
import static de.fraunhofer.iosb.ilt.frostserver.extensions.Extension.ACTUATION;
import static de.fraunhofer.iosb.ilt.frostserver.extensions.Extension.CORE;
import static de.fraunhofer.iosb.ilt.frostserver.extensions.Extension.MULTI_DATASTREAM;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * The types of entities in STA.
 *
 * @author jab, scf
 */
public enum EntityType {

    ACTUATOR("Actuator", "Actuators", ACTUATION, Actuator.class),
    DATASTREAM("Datastream", "Datastreams", CORE, Datastream.class),
    MULTIDATASTREAM("MultiDatastream", "MultiDatastreams", MULTI_DATASTREAM, MultiDatastream.class),
    FEATUREOFINTEREST("FeatureOfInterest", "FeaturesOfInterest", CORE, FeatureOfInterest.class),
    HISTORICALLOCATION("HistoricalLocation", "HistoricalLocations", CORE, HistoricalLocation.class),
    LOCATION("Location", "Locations", CORE, Location.class),
    OBSERVATION("Observation", "Observations", CORE, Observation.class),
    OBSERVEDPROPERTY("ObservedProperty", "ObservedProperties", CORE, ObservedProperty.class),
    SENSOR("Sensor", "Sensors", CORE, Sensor.class),
    TASK("Task", "Tasks", ACTUATION, Task.class),
    TASKINGCAPABILITY("TaskingCapability", "TaskingCapabilities", ACTUATION, TaskingCapability.class),
    THING("Thing", "Things", CORE, Thing.class);

    /**
     * The map of entity names to entities.
     */
    private static final Map<String, EntityType> TYPES_BY_NAME = new HashMap<>();

    /**
     * The entitiyName of this entity type as used in URLs.
     */
    public final String entityName;
    /**
     * The entitiyName of collections of this entity type as used in URLs.
     */
    public final String plural;
    /**
     * The extension that defines this Entity Type.
     */
    public final Extension extension;

    /**
     * The writable version of the properties map, for internal use only.
     */
    private final Map<Property, Boolean> propertyMapRw = new LinkedHashMap<>();
    /**
     * The Set of PROPERTIES that Entities of this type have, mapped to the flag
     * indicating if they are required.
     */
    private final Map<Property, Boolean> propertyMap = Collections.unmodifiableMap(propertyMapRw);
    /**
     * The set of Navigation properties pointing to single entities.
     */
    private final Set<NavigationPropertyMain> navigationEntities = new LinkedHashSet<>();
    /**
     * The set of Navigation properties pointing to entity sets.
     */
    private final Set<NavigationPropertyMain> navigationSets = new LinkedHashSet<>();

    private final Class<? extends Entity> implementingClass;

    private static synchronized void init() {
        if (!ACTUATOR.propertyMap.isEmpty()) {
            return;
        }
        initActuator();
        initDatastream();
        initMultiDatastream();
        initFeatureOfInterest();
        initHistLocation();
        initLocation();
        initObservation();
        initObsProp();
        initSensor();
        initTask();
        initTaskingCapability();
        initThing();
        initFinalise();
    }

    private static void initActuator() {
        Map<Property, Boolean> propertyMap;
        propertyMap = ACTUATOR.propertyMapRw;
        propertyMap.put(EntityProperty.ID, false);
        propertyMap.put(EntityProperty.SELFLINK, false);
        propertyMap.put(EntityProperty.NAME, true);
        propertyMap.put(EntityProperty.DESCRIPTION, true);
        propertyMap.put(EntityProperty.ENCODINGTYPE, true);
        propertyMap.put(EntityProperty.METADATA, true);
        propertyMap.put(EntityProperty.PROPERTIES, false);
        propertyMap.put(NavigationPropertyMain.TASKINGCAPABILITIES, false);
    }

    private static void initDatastream() {
        Map<Property, Boolean> propertyMap;
        propertyMap = DATASTREAM.propertyMapRw;
        propertyMap.put(EntityProperty.ID, false);
        propertyMap.put(EntityProperty.SELFLINK, false);
        propertyMap.put(EntityProperty.NAME, true);
        propertyMap.put(EntityProperty.DESCRIPTION, true);
        propertyMap.put(EntityProperty.OBSERVATIONTYPE, true);
        propertyMap.put(EntityProperty.UNITOFMEASUREMENT, true);
        propertyMap.put(EntityProperty.OBSERVEDAREA, false);
        propertyMap.put(EntityProperty.PHENOMENONTIME, false);
        propertyMap.put(EntityProperty.PROPERTIES, false);
        propertyMap.put(EntityProperty.RESULTTIME, false);
        propertyMap.put(NavigationPropertyMain.OBSERVEDPROPERTY, true);
        propertyMap.put(NavigationPropertyMain.SENSOR, true);
        propertyMap.put(NavigationPropertyMain.THING, true);
        propertyMap.put(NavigationPropertyMain.OBSERVATIONS, false);
    }

    private static void initMultiDatastream() {
        Map<Property, Boolean> propertyMap;
        propertyMap = MULTIDATASTREAM.propertyMapRw;
        propertyMap.put(EntityProperty.ID, false);
        propertyMap.put(EntityProperty.SELFLINK, false);
        propertyMap.put(EntityProperty.NAME, true);
        propertyMap.put(EntityProperty.DESCRIPTION, true);
        // OBSERVATIONTYPE is required, but must always be the same, thus we set it ourselves.
        propertyMap.put(EntityProperty.OBSERVATIONTYPE, false);
        propertyMap.put(EntityProperty.MULTIOBSERVATIONDATATYPES, true);
        propertyMap.put(EntityProperty.UNITOFMEASUREMENTS, true);
        propertyMap.put(EntityProperty.OBSERVEDAREA, false);
        propertyMap.put(EntityProperty.PHENOMENONTIME, false);
        propertyMap.put(EntityProperty.PROPERTIES, false);
        propertyMap.put(EntityProperty.RESULTTIME, false);
        propertyMap.put(NavigationPropertyMain.OBSERVEDPROPERTIES, true);
        propertyMap.put(NavigationPropertyMain.SENSOR, true);
        propertyMap.put(NavigationPropertyMain.THING, true);
        propertyMap.put(NavigationPropertyMain.OBSERVATIONS, false);
    }

    private static void initFeatureOfInterest() {
        Map<Property, Boolean> propertyMap;
        propertyMap = FEATUREOFINTEREST.propertyMapRw;
        propertyMap.put(EntityProperty.ID, false);
        propertyMap.put(EntityProperty.SELFLINK, false);
        propertyMap.put(EntityProperty.NAME, true);
        propertyMap.put(EntityProperty.DESCRIPTION, true);
        propertyMap.put(EntityProperty.ENCODINGTYPE, true);
        propertyMap.put(EntityProperty.FEATURE, true);
        propertyMap.put(EntityProperty.PROPERTIES, false);
        propertyMap.put(NavigationPropertyMain.OBSERVATIONS, false);
    }

    private static void initHistLocation() {
        Map<Property, Boolean> propertyMap;
        propertyMap = HISTORICALLOCATION.propertyMapRw;
        propertyMap.put(EntityProperty.ID, false);
        propertyMap.put(EntityProperty.SELFLINK, false);
        propertyMap.put(EntityProperty.TIME, true);
        propertyMap.put(NavigationPropertyMain.THING, true);
        propertyMap.put(NavigationPropertyMain.LOCATIONS, false);
    }

    private static void initLocation() {
        Map<Property, Boolean> propertyMap;
        propertyMap = LOCATION.propertyMapRw;
        propertyMap.put(EntityProperty.ID, false);
        propertyMap.put(EntityProperty.SELFLINK, false);
        propertyMap.put(EntityProperty.NAME, true);
        propertyMap.put(EntityProperty.DESCRIPTION, true);
        propertyMap.put(EntityProperty.ENCODINGTYPE, true);
        propertyMap.put(EntityProperty.LOCATION, true);
        propertyMap.put(EntityProperty.PROPERTIES, false);
        propertyMap.put(NavigationPropertyMain.HISTORICALLOCATIONS, false);
        propertyMap.put(NavigationPropertyMain.THINGS, false);
    }

    private static void initObservation() {
        Map<Property, Boolean> propertyMap;
        propertyMap = OBSERVATION.propertyMapRw;
        propertyMap.put(EntityProperty.ID, false);
        propertyMap.put(EntityProperty.SELFLINK, false);
        propertyMap.put(EntityProperty.PHENOMENONTIME, false);
        propertyMap.put(EntityProperty.RESULTTIME, false);
        propertyMap.put(EntityProperty.RESULT, true);
        propertyMap.put(EntityProperty.RESULTQUALITY, false);
        propertyMap.put(EntityProperty.VALIDTIME, false);
        propertyMap.put(EntityProperty.PARAMETERS, false);
        // One of the following two is mandatory.
        propertyMap.put(NavigationPropertyMain.DATASTREAM, false);
        propertyMap.put(NavigationPropertyMain.MULTIDATASTREAM, false);
        // FEATUREOFINTEREST must be generated on the fly if not present.
        propertyMap.put(NavigationPropertyMain.FEATUREOFINTEREST, false);
    }

    private static void initObsProp() {
        Map<Property, Boolean> propertyMap;
        propertyMap = OBSERVEDPROPERTY.propertyMapRw;
        propertyMap.put(EntityProperty.ID, false);
        propertyMap.put(EntityProperty.SELFLINK, false);
        propertyMap.put(EntityProperty.NAME, true);
        propertyMap.put(EntityProperty.DEFINITION, true);
        propertyMap.put(EntityProperty.DESCRIPTION, true);
        propertyMap.put(EntityProperty.PROPERTIES, false);
        propertyMap.put(NavigationPropertyMain.DATASTREAMS, false);
        propertyMap.put(NavigationPropertyMain.MULTIDATASTREAMS, false);
    }

    private static void initSensor() {
        Map<Property, Boolean> propertyMap;
        propertyMap = SENSOR.propertyMapRw;
        propertyMap.put(EntityProperty.ID, false);
        propertyMap.put(EntityProperty.SELFLINK, false);
        propertyMap.put(EntityProperty.NAME, true);
        propertyMap.put(EntityProperty.DESCRIPTION, true);
        propertyMap.put(EntityProperty.ENCODINGTYPE, true);
        propertyMap.put(EntityProperty.METADATA, true);
        propertyMap.put(EntityProperty.PROPERTIES, false);
        propertyMap.put(NavigationPropertyMain.DATASTREAMS, false);
        propertyMap.put(NavigationPropertyMain.MULTIDATASTREAMS, false);
    }

    private static void initTask() {
        Map<Property, Boolean> propertyMap;
        propertyMap = TASK.propertyMapRw;
        propertyMap.put(EntityProperty.ID, false);
        propertyMap.put(EntityProperty.SELFLINK, false);
        propertyMap.put(EntityProperty.CREATIONTIME, false);
        propertyMap.put(EntityProperty.TASKINGPARAMETERS, true);
        propertyMap.put(NavigationPropertyMain.TASKINGCAPABILITY, true);
    }

    private static void initTaskingCapability() {
        Map<Property, Boolean> propertyMap;
        propertyMap = TASKINGCAPABILITY.propertyMapRw;
        propertyMap.put(EntityProperty.ID, false);
        propertyMap.put(EntityProperty.SELFLINK, false);
        propertyMap.put(EntityProperty.NAME, true);
        propertyMap.put(EntityProperty.DESCRIPTION, true);
        propertyMap.put(EntityProperty.PROPERTIES, false);
        propertyMap.put(EntityProperty.TASKINGPARAMETERS, true);
        propertyMap.put(NavigationPropertyMain.ACTUATOR, true);
        propertyMap.put(NavigationPropertyMain.TASKS, false);
        propertyMap.put(NavigationPropertyMain.THING, true);
    }

    private static void initThing() {
        Map<Property, Boolean> propertyMap;
        propertyMap = THING.propertyMapRw;
        propertyMap.put(EntityProperty.ID, false);
        propertyMap.put(EntityProperty.SELFLINK, false);
        propertyMap.put(EntityProperty.NAME, true);
        propertyMap.put(EntityProperty.DESCRIPTION, true);
        propertyMap.put(EntityProperty.PROPERTIES, false);
        propertyMap.put(NavigationPropertyMain.LOCATIONS, false);
        propertyMap.put(NavigationPropertyMain.HISTORICALLOCATIONS, false);
        propertyMap.put(NavigationPropertyMain.DATASTREAMS, false);
        propertyMap.put(NavigationPropertyMain.MULTIDATASTREAMS, false);
        propertyMap.put(NavigationPropertyMain.TASKINGCAPABILITIES, false);
    }

    private static void initFinalise() {
        for (EntityType type : EntityType.values()) {
            TYPES_BY_NAME.put(type.entityName, type);
            TYPES_BY_NAME.put(type.plural, type);
            for (Property property : type.getPropertySet()) {
                if (property instanceof NavigationPropertyMain) {
                    NavigationPropertyMain navigationProperty = (NavigationPropertyMain) property;
                    if (navigationProperty.isEntitySet()) {
                        type.getNavigationSets().add(navigationProperty);
                    } else {
                        type.getNavigationEntities().add(navigationProperty);
                    }
                }
            }
        }
    }

    private EntityType(String singular, String plural, Extension extension, Class<? extends Entity> implementingClass) {
        this.entityName = singular;
        this.plural = plural;
        this.extension = extension;
        this.implementingClass = implementingClass;
    }

    /**
     * The Map of PROPERTIES that Entities of this type have, with their
     * required status.
     *
     * @return The Set of PROPERTIES that Entities of this type have.
     */
    public Map<Property, Boolean> getPropertyMap() {
        if (propertyMap.isEmpty()) {
            init();
        }
        return propertyMap;
    }

    /**
     * The Set of PROPERTIES that Entities of this type have.
     *
     * @return The Set of PROPERTIES that Entities of this type have.
     */
    public Set<Property> getPropertySet() {
        if (propertyMap.isEmpty()) {
            init();
        }
        return propertyMap.keySet();
    }

    public Set<NavigationPropertyMain> getNavigationEntities() {
        if (propertyMap.isEmpty()) {
            init();
        }
        return navigationEntities;
    }

    public Set<NavigationPropertyMain> getNavigationSets() {
        if (propertyMap.isEmpty()) {
            init();
        }
        return navigationSets;
    }

    /**
     * @param property The property to check the required state for.
     * @return True when the property is required, false otherwise.
     */
    public boolean isRequired(Property property) {
        if (propertyMap.isEmpty()) {
            init();
        }
        if (!propertyMap.containsKey(property)) {
            return false;
        }
        return propertyMap.get(property);
    }

    public Class<? extends Entity> getImplementingClass() {
        return implementingClass;
    }

    public static EntityType getEntityTypeForName(String name) {
        if (TYPES_BY_NAME.isEmpty()) {
            init();
        }
        return TYPES_BY_NAME.get(name);
    }
}
