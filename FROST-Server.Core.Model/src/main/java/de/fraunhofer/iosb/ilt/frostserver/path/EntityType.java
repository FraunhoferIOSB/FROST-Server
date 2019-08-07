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
package de.fraunhofer.iosb.ilt.frostserver.path;

import de.fraunhofer.iosb.ilt.frostserver.extensions.Extension;
import static de.fraunhofer.iosb.ilt.frostserver.extensions.Extension.ACTUATION;
import static de.fraunhofer.iosb.ilt.frostserver.extensions.Extension.CORE;
import static de.fraunhofer.iosb.ilt.frostserver.extensions.Extension.MULTI_DATASTREAM;
import de.fraunhofer.iosb.ilt.frostserver.model.Actuator;
import de.fraunhofer.iosb.ilt.frostserver.model.Datastream;
import de.fraunhofer.iosb.ilt.frostserver.model.FeatureOfInterest;
import de.fraunhofer.iosb.ilt.frostserver.model.HistoricalLocation;
import de.fraunhofer.iosb.ilt.frostserver.model.Location;
import de.fraunhofer.iosb.ilt.frostserver.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.frostserver.model.Observation;
import de.fraunhofer.iosb.ilt.frostserver.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.frostserver.model.Sensor;
import de.fraunhofer.iosb.ilt.frostserver.model.Task;
import de.fraunhofer.iosb.ilt.frostserver.model.TaskingCapability;
import de.fraunhofer.iosb.ilt.frostserver.model.Thing;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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

    public static class PropertyEntry {

        public final Property property;
        /**
         * Flag indicating the property must be set when creating an Entity.
         */
        public final boolean required;

        public PropertyEntry(Property property, boolean required) {
            this.property = property;
            this.required = required;
        }

    }
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
    private final Map<Property, Boolean> propertyMapRw = new HashMap<>();
    /**
     * The Set of PROPERTIES that Entities of this type have, mapped to the flag
     * indicating if they are required.
     */
    private final Map<Property, Boolean> propertyMap = Collections.unmodifiableMap(propertyMapRw);
    /**
     * The set of Navigation properties pointing to single entities.
     */
    private final Set<NavigationProperty> navigationEntities = new HashSet<>();
    /**
     * The set of Navigation properties pointing to entity sets.
     */
    private final Set<NavigationProperty> navigationSets = new HashSet<>();

    private final Class<? extends Entity> implementingClass;

    private static void init() {
        Map<Property, Boolean> propertyMap;

        propertyMap = ACTUATOR.propertyMapRw;
        propertyMap.put(EntityProperty.ID, false);
        propertyMap.put(EntityProperty.SELFLINK, false);
        propertyMap.put(EntityProperty.NAME, true);
        propertyMap.put(EntityProperty.DESCRIPTION, true);
        propertyMap.put(EntityProperty.ENCODINGTYPE, true);
        propertyMap.put(EntityProperty.METADATA, true);
        propertyMap.put(EntityProperty.PROPERTIES, false);
        propertyMap.put(NavigationProperty.TASKINGCAPABILITIES, false);

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
        propertyMap.put(NavigationProperty.OBSERVEDPROPERTY, true);
        propertyMap.put(NavigationProperty.SENSOR, true);
        propertyMap.put(NavigationProperty.THING, true);
        propertyMap.put(NavigationProperty.OBSERVATIONS, false);

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
        propertyMap.put(NavigationProperty.OBSERVEDPROPERTIES, true);
        propertyMap.put(NavigationProperty.SENSOR, true);
        propertyMap.put(NavigationProperty.THING, true);
        propertyMap.put(NavigationProperty.OBSERVATIONS, false);

        propertyMap = FEATUREOFINTEREST.propertyMapRw;
        propertyMap.put(EntityProperty.ID, false);
        propertyMap.put(EntityProperty.SELFLINK, false);
        propertyMap.put(EntityProperty.NAME, true);
        propertyMap.put(EntityProperty.DESCRIPTION, true);
        propertyMap.put(EntityProperty.ENCODINGTYPE, true);
        propertyMap.put(EntityProperty.FEATURE, true);
        propertyMap.put(EntityProperty.PROPERTIES, false);
        propertyMap.put(NavigationProperty.OBSERVATIONS, false);

        propertyMap = HISTORICALLOCATION.propertyMapRw;
        propertyMap.put(EntityProperty.ID, false);
        propertyMap.put(EntityProperty.SELFLINK, false);
        propertyMap.put(EntityProperty.TIME, true);
        propertyMap.put(NavigationProperty.THING, true);
        propertyMap.put(NavigationProperty.LOCATIONS, false);

        propertyMap = LOCATION.propertyMapRw;
        propertyMap.put(EntityProperty.ID, false);
        propertyMap.put(EntityProperty.SELFLINK, false);
        propertyMap.put(EntityProperty.NAME, true);
        propertyMap.put(EntityProperty.DESCRIPTION, true);
        propertyMap.put(EntityProperty.ENCODINGTYPE, true);
        propertyMap.put(EntityProperty.LOCATION, true);
        propertyMap.put(EntityProperty.PROPERTIES, false);
        propertyMap.put(NavigationProperty.HISTORICALLOCATIONS, false);
        propertyMap.put(NavigationProperty.THINGS, false);

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
        propertyMap.put(NavigationProperty.DATASTREAM, false);
        propertyMap.put(NavigationProperty.MULTIDATASTREAM, false);
        // FEATUREOFINTEREST must be generated on the fly if not present.
        propertyMap.put(NavigationProperty.FEATUREOFINTEREST, false);

        propertyMap = OBSERVEDPROPERTY.propertyMapRw;
        propertyMap.put(EntityProperty.ID, false);
        propertyMap.put(EntityProperty.SELFLINK, false);
        propertyMap.put(EntityProperty.NAME, true);
        propertyMap.put(EntityProperty.DEFINITION, true);
        propertyMap.put(EntityProperty.DESCRIPTION, true);
        propertyMap.put(EntityProperty.PROPERTIES, false);
        propertyMap.put(NavigationProperty.DATASTREAMS, false);
        propertyMap.put(NavigationProperty.MULTIDATASTREAMS, false);

        propertyMap = SENSOR.propertyMapRw;
        propertyMap.put(EntityProperty.ID, false);
        propertyMap.put(EntityProperty.SELFLINK, false);
        propertyMap.put(EntityProperty.NAME, true);
        propertyMap.put(EntityProperty.DESCRIPTION, true);
        propertyMap.put(EntityProperty.ENCODINGTYPE, true);
        propertyMap.put(EntityProperty.METADATA, true);
        propertyMap.put(EntityProperty.PROPERTIES, false);
        propertyMap.put(NavigationProperty.DATASTREAMS, false);
        propertyMap.put(NavigationProperty.MULTIDATASTREAMS, false);

        propertyMap = TASK.propertyMapRw;
        propertyMap.put(EntityProperty.ID, false);
        propertyMap.put(EntityProperty.SELFLINK, false);
        propertyMap.put(EntityProperty.CREATIONTIME, false);
        propertyMap.put(EntityProperty.TASKINGPARAMETERS, true);
        propertyMap.put(NavigationProperty.TASKINGCAPABILITY, true);

        propertyMap = TASKINGCAPABILITY.propertyMapRw;
        propertyMap.put(EntityProperty.ID, false);
        propertyMap.put(EntityProperty.SELFLINK, false);
        propertyMap.put(EntityProperty.NAME, true);
        propertyMap.put(EntityProperty.DESCRIPTION, true);
        propertyMap.put(EntityProperty.PROPERTIES, false);
        propertyMap.put(EntityProperty.TASKINGPARAMETERS, true);
        propertyMap.put(NavigationProperty.ACTUATOR, true);
        propertyMap.put(NavigationProperty.TASKS, false);
        propertyMap.put(NavigationProperty.THING, true);

        propertyMap = THING.propertyMapRw;
        propertyMap.put(EntityProperty.ID, false);
        propertyMap.put(EntityProperty.SELFLINK, false);
        propertyMap.put(EntityProperty.NAME, true);
        propertyMap.put(EntityProperty.DESCRIPTION, true);
        propertyMap.put(EntityProperty.PROPERTIES, false);
        propertyMap.put(NavigationProperty.LOCATIONS, false);
        propertyMap.put(NavigationProperty.HISTORICALLOCATIONS, false);
        propertyMap.put(NavigationProperty.DATASTREAMS, false);
        propertyMap.put(NavigationProperty.MULTIDATASTREAMS, false);
        propertyMap.put(NavigationProperty.TASKINGCAPABILITIES, false);

        for (EntityType type : EntityType.values()) {
            for (Property property : type.getPropertySet()) {
                if (property instanceof NavigationProperty) {
                    NavigationProperty navigationProperty = (NavigationProperty) property;
                    if (navigationProperty.isSet) {
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

    public Set<NavigationProperty> getNavigationEntities() {
        if (propertyMap.isEmpty()) {
            init();
        }
        return navigationEntities;
    }

    public Set<NavigationProperty> getNavigationSets() {
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

}
