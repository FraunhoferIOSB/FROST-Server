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

import com.fasterxml.jackson.core.type.TypeReference;
import de.fraunhofer.iosb.ilt.frostserver.extensions.Extension;
import static de.fraunhofer.iosb.ilt.frostserver.extensions.Extension.ACTUATION;
import static de.fraunhofer.iosb.ilt.frostserver.extensions.Extension.CORE;
import static de.fraunhofer.iosb.ilt.frostserver.extensions.Extension.MULTI_DATASTREAM;
import static de.fraunhofer.iosb.ilt.frostserver.model.Actuator.TYPE_REFERENCE_ACTUATOR;
import static de.fraunhofer.iosb.ilt.frostserver.model.Datastream.TYPE_REFERENCE_DATASTREAM;
import static de.fraunhofer.iosb.ilt.frostserver.model.FeatureOfInterest.TYPE_REFERENCE_FOI;
import static de.fraunhofer.iosb.ilt.frostserver.model.HistoricalLocation.TYPE_REFERENCE_HISTORICALLOCATION;
import static de.fraunhofer.iosb.ilt.frostserver.model.Location.TYPE_REFERENCE_LOCATION;
import static de.fraunhofer.iosb.ilt.frostserver.model.MultiDatastream.TYPE_REFERENCE_MULTIDATASTREAM;
import static de.fraunhofer.iosb.ilt.frostserver.model.Observation.TYPE_REFERENCE_OBSERVATION;
import static de.fraunhofer.iosb.ilt.frostserver.model.ObservedProperty.TYPE_REFERENCE_OBSERVEDPROPERTY;
import static de.fraunhofer.iosb.ilt.frostserver.model.Sensor.TYPE_REFERENCE_SENSOR;
import static de.fraunhofer.iosb.ilt.frostserver.model.Task.TYPE_REFERENCE_TASK;
import static de.fraunhofer.iosb.ilt.frostserver.model.TaskingCapability.TYPE_REFERENCE_TASKINGCAP;
import static de.fraunhofer.iosb.ilt.frostserver.model.Thing.TYPE_REFERENCE_THING;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.geojson.GeoJsonObject;

/**
 * The types of entities in STA.
 *
 * @author jab, scf
 */
public enum EntityType {

    ACTUATOR("Actuator", "Actuators", ACTUATION, TYPE_REFERENCE_ACTUATOR, Actuator.class),
    DATASTREAM("Datastream", "Datastreams", CORE, TYPE_REFERENCE_DATASTREAM, Datastream.class),
    MULTIDATASTREAM("MultiDatastream", "MultiDatastreams", MULTI_DATASTREAM, TYPE_REFERENCE_MULTIDATASTREAM, MultiDatastream.class),
    FEATUREOFINTEREST("FeatureOfInterest", "FeaturesOfInterest", CORE, TYPE_REFERENCE_FOI, FeatureOfInterest.class),
    HISTORICALLOCATION("HistoricalLocation", "HistoricalLocations", CORE, TYPE_REFERENCE_HISTORICALLOCATION, HistoricalLocation.class),
    LOCATION("Location", "Locations", CORE, TYPE_REFERENCE_LOCATION, Location.class),
    OBSERVATION("Observation", "Observations", CORE, TYPE_REFERENCE_OBSERVATION, Observation.class),
    OBSERVEDPROPERTY("ObservedProperty", "ObservedProperties", CORE, TYPE_REFERENCE_OBSERVEDPROPERTY, ObservedProperty.class),
    SENSOR("Sensor", "Sensors", CORE, TYPE_REFERENCE_SENSOR, Sensor.class),
    TASK("Task", "Tasks", ACTUATION, TYPE_REFERENCE_TASK, Task.class),
    TASKINGCAPABILITY("TaskingCapability", "TaskingCapabilities", ACTUATION, TYPE_REFERENCE_TASKINGCAP, TaskingCapability.class),
    THING("Thing", "Things", CORE, TYPE_REFERENCE_THING, Thing.class);

    private static final TypeReference<Id> TYPE_REFERENCE_ID = new TypeReference<Id>() {
        // Empty on purpose.
    };
    private static final TypeReference<Object> TYPE_REFERENCE_OBJECT = new TypeReference<Object>() {
        // Empty on purpose.
    };
    private static final TypeReference<String> TYPE_REFERENCE_STRING = new TypeReference<String>() {
        // Empty on purpose.
    };
    private static final TypeReference<Map<String, Object>> TYPE_REFERENCE_MAP = new TypeReference<Map<String, Object>>() {
        // Empty on purpose.
    };
    private static final TypeReference<TimeInterval> TYPE_REFERENCE_TIME_INTERVAL = new TypeReference<TimeInterval>() {
        // Empty on purpose.
    };
    private static final TypeReference<GeoJsonObject> TYPE_REFERENCE_GEOJSONOBJECT = new TypeReference<GeoJsonObject>() {
        // Empty on purpose.
    };
    private static final TypeReference<UnitOfMeasurement> TYPE_REFERENCE_UOM = new TypeReference<UnitOfMeasurement>() {
        // Empty on purpose.
    };
    private static final TypeReference<List<UnitOfMeasurement>> TYPE_REFERENCE_LIST_UOM = new TypeReference<List<UnitOfMeasurement>>() {
        // Empty on purpose.
    };
    private static final TypeReference<List<String>> TYPE_REFERENCE_LIST_STRING = new TypeReference<List<String>>() {
        // Empty on purpose.
    };
    private static final TypeReference<TimeInstant> TYPE_REFERENCE_TIMEINSTANT = new TypeReference<TimeInstant>() {
        // Empty on purpose.
    };
    private static final TypeReference<TimeValue> TYPE_REFERENCE_TIMEVALUE = new TypeReference<TimeValue>() {
        // Empty on purpose.
    };

    /**
     * The map of entity names to entities.
     */
    private static final Map<String, EntityType> TYPES_BY_NAME = new HashMap<>();
    /**
     * The map of implementing classes to entities.
     */
    private static final Map<Class<? extends Entity>, EntityType> TYPES_BY_CLASS = new HashMap<>();

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
     * The Set of PROPERTIES that Entities of this type have, mapped to the flag
     * indicating if they are required.
     */
    private final Map<Property, Boolean> propertyMap = new LinkedHashMap<>();
    /**
     * The java types of the properties if this entity type.
     */
    private final Map<EntityPropertyMain, TypeReference> propertyTypes = new LinkedHashMap<>();
    /**
     * The set of Entity properties.
     */
    private final Set<EntityPropertyMain> entityProperties = new LinkedHashSet<>();
    /**
     * The set of Navigation properties.
     */
    private final Set<NavigationPropertyMain> navigationProperties = new LinkedHashSet<>();
    /**
     * The set of Navigation properties pointing to single entities.
     */
    private final Set<NavigationPropertyMain> navigationEntities = new LinkedHashSet<>();
    /**
     * The set of Navigation properties pointing to entity sets.
     */
    private final Set<NavigationPropertyMain> navigationSets = new LinkedHashSet<>();

    private final TypeReference<? extends Entity<?>> implementingTypeRef;
    private final Class<? extends Entity<?>> implementingClass;

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
        ACTUATOR.init(EntityPropertyMain.ID, false, TYPE_REFERENCE_ID);
        ACTUATOR.init(EntityPropertyMain.SELFLINK, false, TYPE_REFERENCE_STRING);
        ACTUATOR.init(EntityPropertyMain.NAME, true, TYPE_REFERENCE_STRING);
        ACTUATOR.init(EntityPropertyMain.DESCRIPTION, true, TYPE_REFERENCE_STRING);
        ACTUATOR.init(EntityPropertyMain.ENCODINGTYPE, true, TYPE_REFERENCE_STRING);
        ACTUATOR.init(EntityPropertyMain.METADATA, true, TYPE_REFERENCE_OBJECT);
        ACTUATOR.init(EntityPropertyMain.PROPERTIES, false, TYPE_REFERENCE_MAP);
        ACTUATOR.init(NavigationPropertyMain.TASKINGCAPABILITIES, false, null);
    }

    private static void initDatastream() {
        DATASTREAM.init(EntityPropertyMain.ID, false, TYPE_REFERENCE_ID);
        DATASTREAM.init(EntityPropertyMain.SELFLINK, false, TYPE_REFERENCE_STRING);
        DATASTREAM.init(EntityPropertyMain.NAME, true, TYPE_REFERENCE_STRING);
        DATASTREAM.init(EntityPropertyMain.DESCRIPTION, true, TYPE_REFERENCE_STRING);
        DATASTREAM.init(EntityPropertyMain.OBSERVATIONTYPE, true, TYPE_REFERENCE_STRING);
        DATASTREAM.init(EntityPropertyMain.UNITOFMEASUREMENT, true, TYPE_REFERENCE_UOM);
        DATASTREAM.init(EntityPropertyMain.OBSERVEDAREA, false, TYPE_REFERENCE_GEOJSONOBJECT);
        DATASTREAM.init(EntityPropertyMain.PHENOMENONTIME, false, TYPE_REFERENCE_TIME_INTERVAL);
        DATASTREAM.init(EntityPropertyMain.PROPERTIES, false, TYPE_REFERENCE_MAP);
        DATASTREAM.init(EntityPropertyMain.RESULTTIME, false, TYPE_REFERENCE_TIME_INTERVAL);
        DATASTREAM.init(NavigationPropertyMain.OBSERVEDPROPERTY, true, null);
        DATASTREAM.init(NavigationPropertyMain.SENSOR, true, null);
        DATASTREAM.init(NavigationPropertyMain.THING, true, null);
        DATASTREAM.init(NavigationPropertyMain.OBSERVATIONS, false, null);
    }

    private static void initMultiDatastream() {
        MULTIDATASTREAM.init(EntityPropertyMain.ID, false, TYPE_REFERENCE_ID);
        MULTIDATASTREAM.init(EntityPropertyMain.SELFLINK, false, TYPE_REFERENCE_STRING);
        MULTIDATASTREAM.init(EntityPropertyMain.NAME, true, TYPE_REFERENCE_STRING);
        MULTIDATASTREAM.init(EntityPropertyMain.DESCRIPTION, true, TYPE_REFERENCE_STRING);
        // OBSERVATIONTYPE is required, but must always be the same, thus we set it ourselves.
        MULTIDATASTREAM.init(EntityPropertyMain.OBSERVATIONTYPE, false, TYPE_REFERENCE_STRING);
        MULTIDATASTREAM.init(EntityPropertyMain.MULTIOBSERVATIONDATATYPES, true, TYPE_REFERENCE_LIST_STRING);
        MULTIDATASTREAM.init(EntityPropertyMain.UNITOFMEASUREMENTS, true, TYPE_REFERENCE_LIST_UOM);
        MULTIDATASTREAM.init(EntityPropertyMain.OBSERVEDAREA, false, TYPE_REFERENCE_GEOJSONOBJECT);
        MULTIDATASTREAM.init(EntityPropertyMain.PHENOMENONTIME, false, TYPE_REFERENCE_TIME_INTERVAL);
        MULTIDATASTREAM.init(EntityPropertyMain.PROPERTIES, false, TYPE_REFERENCE_MAP);
        MULTIDATASTREAM.init(EntityPropertyMain.RESULTTIME, false, TYPE_REFERENCE_TIME_INTERVAL);
        MULTIDATASTREAM.init(NavigationPropertyMain.OBSERVEDPROPERTIES, false, null);
        MULTIDATASTREAM.init(NavigationPropertyMain.SENSOR, true, null);
        MULTIDATASTREAM.init(NavigationPropertyMain.THING, true, null);
        MULTIDATASTREAM.init(NavigationPropertyMain.OBSERVATIONS, false, null);
    }

    private static void initFeatureOfInterest() {
        FEATUREOFINTEREST.init(EntityPropertyMain.ID, false, TYPE_REFERENCE_ID);
        FEATUREOFINTEREST.init(EntityPropertyMain.SELFLINK, false, TYPE_REFERENCE_STRING);
        FEATUREOFINTEREST.init(EntityPropertyMain.NAME, true, TYPE_REFERENCE_STRING);
        FEATUREOFINTEREST.init(EntityPropertyMain.DESCRIPTION, true, TYPE_REFERENCE_STRING);
        FEATUREOFINTEREST.init(EntityPropertyMain.ENCODINGTYPE, true, TYPE_REFERENCE_STRING);
        FEATUREOFINTEREST.init(EntityPropertyMain.FEATURE, true, null);
        FEATUREOFINTEREST.init(EntityPropertyMain.PROPERTIES, false, TYPE_REFERENCE_MAP);
        FEATUREOFINTEREST.init(NavigationPropertyMain.OBSERVATIONS, false, null);
    }

    private static void initHistLocation() {
        HISTORICALLOCATION.init(EntityPropertyMain.ID, false, TYPE_REFERENCE_ID);
        HISTORICALLOCATION.init(EntityPropertyMain.SELFLINK, false, TYPE_REFERENCE_STRING);
        HISTORICALLOCATION.init(EntityPropertyMain.TIME, true, TYPE_REFERENCE_TIMEINSTANT);
        HISTORICALLOCATION.init(NavigationPropertyMain.THING, true, null);
        HISTORICALLOCATION.init(NavigationPropertyMain.LOCATIONS, false, null);
    }

    private static void initLocation() {
        LOCATION.init(EntityPropertyMain.ID, false, TYPE_REFERENCE_ID);
        LOCATION.init(EntityPropertyMain.SELFLINK, false, TYPE_REFERENCE_STRING);
        LOCATION.init(EntityPropertyMain.NAME, true, TYPE_REFERENCE_STRING);
        LOCATION.init(EntityPropertyMain.DESCRIPTION, true, TYPE_REFERENCE_STRING);
        LOCATION.init(EntityPropertyMain.ENCODINGTYPE, true, TYPE_REFERENCE_STRING);
        LOCATION.init(EntityPropertyMain.LOCATION, true, null);
        LOCATION.init(EntityPropertyMain.PROPERTIES, false, TYPE_REFERENCE_MAP);
        LOCATION.init(NavigationPropertyMain.HISTORICALLOCATIONS, false, null);
        LOCATION.init(NavigationPropertyMain.THINGS, false, null);
    }

    private static void initObservation() {
        OBSERVATION.init(EntityPropertyMain.ID, false, TYPE_REFERENCE_ID);
        OBSERVATION.init(EntityPropertyMain.SELFLINK, false, TYPE_REFERENCE_STRING);
        OBSERVATION.init(EntityPropertyMain.PHENOMENONTIME, false, TYPE_REFERENCE_TIMEVALUE);
        OBSERVATION.init(EntityPropertyMain.RESULTTIME, false, TYPE_REFERENCE_TIMEINSTANT);
        OBSERVATION.init(EntityPropertyMain.RESULT, true, TYPE_REFERENCE_OBJECT);
        OBSERVATION.init(EntityPropertyMain.RESULTQUALITY, false, TYPE_REFERENCE_OBJECT);
        OBSERVATION.init(EntityPropertyMain.VALIDTIME, false, TYPE_REFERENCE_TIME_INTERVAL);
        OBSERVATION.init(EntityPropertyMain.PARAMETERS, false, TYPE_REFERENCE_MAP);
        // One of the following two is mandatory.
        OBSERVATION.init(NavigationPropertyMain.DATASTREAM, false, null);
        OBSERVATION.init(NavigationPropertyMain.MULTIDATASTREAM, false, null);
        // FEATUREOFINTEREST must be generated on the fly if not present.
        OBSERVATION.init(NavigationPropertyMain.FEATUREOFINTEREST, false, null);
    }

    private static void initObsProp() {
        OBSERVEDPROPERTY.init(EntityPropertyMain.ID, false, TYPE_REFERENCE_ID);
        OBSERVEDPROPERTY.init(EntityPropertyMain.SELFLINK, false, TYPE_REFERENCE_STRING);
        OBSERVEDPROPERTY.init(EntityPropertyMain.NAME, true, TYPE_REFERENCE_STRING);
        OBSERVEDPROPERTY.init(EntityPropertyMain.DEFINITION, true, TYPE_REFERENCE_STRING);
        OBSERVEDPROPERTY.init(EntityPropertyMain.DESCRIPTION, true, TYPE_REFERENCE_STRING);
        OBSERVEDPROPERTY.init(EntityPropertyMain.PROPERTIES, false, TYPE_REFERENCE_MAP);
        OBSERVEDPROPERTY.init(NavigationPropertyMain.DATASTREAMS, false, null);
        OBSERVEDPROPERTY.init(NavigationPropertyMain.MULTIDATASTREAMS, false, null);
    }

    private static void initSensor() {
        SENSOR.init(EntityPropertyMain.ID, false, TYPE_REFERENCE_ID);
        SENSOR.init(EntityPropertyMain.SELFLINK, false, TYPE_REFERENCE_STRING);
        SENSOR.init(EntityPropertyMain.NAME, true, TYPE_REFERENCE_STRING);
        SENSOR.init(EntityPropertyMain.DESCRIPTION, true, TYPE_REFERENCE_STRING);
        SENSOR.init(EntityPropertyMain.ENCODINGTYPE, true, TYPE_REFERENCE_STRING);
        SENSOR.init(EntityPropertyMain.METADATA, true, TYPE_REFERENCE_OBJECT);
        SENSOR.init(EntityPropertyMain.PROPERTIES, false, TYPE_REFERENCE_MAP);
        SENSOR.init(NavigationPropertyMain.DATASTREAMS, false, null);
        SENSOR.init(NavigationPropertyMain.MULTIDATASTREAMS, false, null);
    }

    private static void initTask() {
        TASK.init(EntityPropertyMain.ID, false, TYPE_REFERENCE_ID);
        TASK.init(EntityPropertyMain.SELFLINK, false, TYPE_REFERENCE_STRING);
        TASK.init(EntityPropertyMain.CREATIONTIME, false, TYPE_REFERENCE_TIMEINSTANT);
        TASK.init(EntityPropertyMain.TASKINGPARAMETERS, true, TYPE_REFERENCE_MAP);
        TASK.init(NavigationPropertyMain.TASKINGCAPABILITY, true, null);
    }

    private static void initTaskingCapability() {
        TASKINGCAPABILITY.init(EntityPropertyMain.ID, false, TYPE_REFERENCE_ID);
        TASKINGCAPABILITY.init(EntityPropertyMain.SELFLINK, false, TYPE_REFERENCE_STRING);
        TASKINGCAPABILITY.init(EntityPropertyMain.NAME, true, TYPE_REFERENCE_STRING);
        TASKINGCAPABILITY.init(EntityPropertyMain.DESCRIPTION, true, TYPE_REFERENCE_STRING);
        TASKINGCAPABILITY.init(EntityPropertyMain.PROPERTIES, false, TYPE_REFERENCE_MAP);
        TASKINGCAPABILITY.init(EntityPropertyMain.TASKINGPARAMETERS, true, TYPE_REFERENCE_MAP);
        TASKINGCAPABILITY.init(NavigationPropertyMain.ACTUATOR, true, null);
        TASKINGCAPABILITY.init(NavigationPropertyMain.TASKS, false, null);
        TASKINGCAPABILITY.init(NavigationPropertyMain.THING, true, null);
    }

    private static void initThing() {
        THING.init(EntityPropertyMain.ID, false, TYPE_REFERENCE_ID);
        THING.init(EntityPropertyMain.SELFLINK, false, TYPE_REFERENCE_STRING);
        THING.init(EntityPropertyMain.NAME, true, TYPE_REFERENCE_STRING);
        THING.init(EntityPropertyMain.DESCRIPTION, true, TYPE_REFERENCE_STRING);
        THING.init(EntityPropertyMain.PROPERTIES, false, TYPE_REFERENCE_MAP);
        THING.init(NavigationPropertyMain.LOCATIONS, false, null);
        THING.init(NavigationPropertyMain.HISTORICALLOCATIONS, false, null);
        THING.init(NavigationPropertyMain.DATASTREAMS, false, null);
        THING.init(NavigationPropertyMain.MULTIDATASTREAMS, false, null);
        THING.init(NavigationPropertyMain.TASKINGCAPABILITIES, false, null);
    }

    private static void initFinalise() {
        for (EntityType type : EntityType.values()) {
            TYPES_BY_NAME.put(type.entityName, type);
            TYPES_BY_NAME.put(type.plural, type);
            TYPES_BY_CLASS.put(type.implementingClass, type);
            for (Property property : type.getPropertySet()) {
                if (property instanceof EntityPropertyMain) {
                    type.getEntityProperties().add((EntityPropertyMain) property);
                } else if (property instanceof NavigationPropertyMain) {
                    NavigationPropertyMain navigationProperty = (NavigationPropertyMain) property;
                    type.getNavigationProperties().add(navigationProperty);
                    if (navigationProperty.isEntitySet()) {
                        type.getNavigationSets().add(navigationProperty);
                    } else {
                        type.getNavigationEntities().add(navigationProperty);
                    }
                }
            }
        }
    }

    private <T extends Entity<T>> EntityType(String singular, String plural, Extension extension, TypeReference<T> implementingTypeRef, Class<T> implementingClass) {
        this.entityName = singular;
        this.plural = plural;
        this.extension = extension;
        this.implementingTypeRef = implementingTypeRef;
        this.implementingClass = implementingClass;
    }

    private void init(Property property, boolean required, TypeReference type) {
        propertyMap.put(property, required);
        if (property instanceof EntityPropertyMain && type != null) {
            propertyTypes.put((EntityPropertyMain) property, type);
        }
    }

    public TypeReference getPropertyType(EntityPropertyMain entityProperty) {
        return propertyTypes.get(entityProperty);
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

    public Set<EntityPropertyMain> getEntityProperties() {
        if (propertyMap.isEmpty()) {
            init();
        }
        return entityProperties;
    }

    public Set<NavigationPropertyMain> getNavigationProperties() {
        if (propertyMap.isEmpty()) {
            init();
        }
        return navigationProperties;
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

    public TypeReference<? extends Entity> getImplementingTypeRef() {
        return implementingTypeRef;
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

    public static EntityType getEntityTypeForClass(Class<? extends Entity> clazz) {
        if (TYPES_BY_CLASS.isEmpty()) {
            init();
        }
        return TYPES_BY_CLASS.get(clazz);
    }
}
