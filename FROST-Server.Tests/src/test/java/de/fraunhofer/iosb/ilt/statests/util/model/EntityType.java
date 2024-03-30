/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.statests.util.model;

import static de.fraunhofer.iosb.ilt.statests.util.Extension.ACTUATION;
import static de.fraunhofer.iosb.ilt.statests.util.Extension.CORE;

import de.fraunhofer.iosb.ilt.statests.util.Extension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * List of entity types in SensorThings API.
 *
 * TODO: Add MultiDatastream
 */
public enum EntityType {
    ACTUATOR("Actuator", "Actuators", ACTUATION),
    TASK("Task", "Tasks", ACTUATION),
    TASKING_CAPABILITY("TaskingCapability", "TaskingCapabilities", ACTUATION),
    THING("Thing", "Things"),
    LOCATION("Location", "Locations"),
    SENSOR("Sensor", "Sensors"),
    OBSERVED_PROPERTY("ObservedProperty", "ObservedProperties"),
    OBSERVATION("Observation", "Observations"),
    DATASTREAM("Datastream", "Datastreams"),
    FEATURE_OF_INTEREST("FeatureOfInterest", "FeaturesOfInterest"),
    HISTORICAL_LOCATION("HistoricalLocation", "HistoricalLocations");

    /**
     * The singular name of the entity type.
     */
    public final String singular;
    /**
     * The plural (collection) name of the entity type.
     */
    public final String plural;
    /**
     * The extension that defines this EntityType.
     */
    public final Extension extension;

    private final List<EntityProperty> properties = new ArrayList<>();
    private final Map<String, EntityProperty> propertiesByName = new HashMap<>();
    private final Map<String, EntityProperty> editablePropertiesByName = new HashMap<>();
    private final Map<Extension, List<String>> relations = new EnumMap<>(Extension.class);

    private static final Map<String, EntityType> NAMES_MAP = new HashMap<>();
    private static final Set<String> NAMES_PLURAL = new HashSet<>();

    static {
        ACTUATOR.addProperty("name", false, true);
        ACTUATOR.addProperty("description", false, true);
        ACTUATOR.addProperty("encodingType", false, true);
        ACTUATOR.addProperty("metadata", false, true);
        ACTUATOR.addRelations(ACTUATION, TASKING_CAPABILITY.plural);

        TASK.addProperty("creationTime", false, true);
        TASK.addProperty("taskingParameters", false, false, "object");
        TASK.addRelations(ACTUATION, TASKING_CAPABILITY.singular);

        TASKING_CAPABILITY.addProperty("name", false, true);
        TASKING_CAPABILITY.addProperty("description", false, true);
        TASKING_CAPABILITY.addProperty("properties", true, false, "object");
        TASKING_CAPABILITY.addProperty("taskingParameters", false, false, "object");
        TASKING_CAPABILITY.addRelations(ACTUATION, THING.singular, ACTUATOR.singular, TASK.plural);

        THING.addProperty("name", false, true);
        THING.addProperty("description", false, true);
        THING.addProperty("properties", true, false, "object");
        THING.addRelations(CORE, DATASTREAM.plural, HISTORICAL_LOCATION.plural, LOCATION.plural);
        THING.addRelations(ACTUATION, TASKING_CAPABILITY.plural);

        LOCATION.addProperty("name", false, true);
        LOCATION.addProperty("description", false, true);
        LOCATION.addProperty("encodingType", false, true);
        LOCATION.addProperty("location", false, false, "object");
        LOCATION.addRelations(CORE, HISTORICAL_LOCATION.plural, THING.plural);

        SENSOR.addProperty("name", false, true);
        SENSOR.addProperty("description", false, true);
        SENSOR.addProperty("encodingType", false, true);
        SENSOR.addProperty("metadata", false, true);
        SENSOR.addRelations(CORE, DATASTREAM.plural);

        OBSERVED_PROPERTY.addProperty("name", false, true);
        OBSERVED_PROPERTY.addProperty("definition", false, true);
        OBSERVED_PROPERTY.addProperty("description", false, true);
        OBSERVED_PROPERTY.addRelations(CORE, DATASTREAM.plural);

        OBSERVATION.addProperty("phenomenonTime", false, true);
        OBSERVATION.addProperty("result", false, true, "any");
        OBSERVATION.addProperty("resultTime", false, true);
        OBSERVATION.addProperty("resultQuality", true, true);
        OBSERVATION.addProperty("validTime", true, true);
        OBSERVATION.addProperty("parameters", true, true, "object");
        OBSERVATION.addRelations(CORE, DATASTREAM.singular, FEATURE_OF_INTEREST.singular);

        DATASTREAM.addProperty("name", false, true);
        DATASTREAM.addProperty("description", false, true);
        DATASTREAM.addProperty("unitOfMeasurement", false, false, true, "object");
        DATASTREAM.addProperty("observationType", false, true);
        DATASTREAM.addProperty("observedArea", true, false, false, "object");
        DATASTREAM.addProperty("phenomenonTime", true, true, false, "string");
        DATASTREAM.addProperty("resultTime", true, true, false, "string");
        DATASTREAM.addRelations(CORE, THING.singular, SENSOR.singular, OBSERVED_PROPERTY.singular, OBSERVATION.plural);

        FEATURE_OF_INTEREST.addProperty("name", false, true);
        FEATURE_OF_INTEREST.addProperty("description", false, true);
        FEATURE_OF_INTEREST.addProperty("encodingType", false, true);
        FEATURE_OF_INTEREST.addProperty("feature", false, false, "object");
        FEATURE_OF_INTEREST.addRelations(CORE, OBSERVATION.plural);

        HISTORICAL_LOCATION.addProperty("time", false, true);
        HISTORICAL_LOCATION.addRelations(CORE, THING.singular, LOCATION.plural);

        for (EntityType entityType : EntityType.values()) {
            NAMES_MAP.put(entityType.singular, entityType);
            NAMES_MAP.put(entityType.plural, entityType);
            NAMES_PLURAL.add(entityType.plural);
        }
    }

    public static EntityType getForRelation(String relation) {
        EntityType entityType = NAMES_MAP.get(relation);
        if (entityType == null) {
            throw new IllegalArgumentException("Unknown relation: " + relation);
        }
        return entityType;
    }

    public static boolean isPlural(String relation) {
        return NAMES_PLURAL.contains(relation);
    }

    private EntityType(String singular, String plural) {
        this.extension = CORE;
        this.singular = singular;
        this.plural = plural;
    }

    private EntityType(String singular, String plural, Extension extension) {
        this.extension = extension;
        this.singular = singular;
        this.plural = plural;
    }

    public Extension getExtension() {
        return extension;
    }

    public String getRootEntitySet() {
        return plural;
    }

    public List<String> getRelations(Extension extension) {
        return relations.getOrDefault(extension, Collections.emptyList());
    }

    public List<String> getRelations(Set<Extension> extensions) {
        List<String> value = new ArrayList<>();
        for (Extension extension : extensions) {
            value.addAll(relations.getOrDefault(extension, Collections.emptyList()));
        }
        return value;
    }

    public List<EntityProperty> getProperties() {
        return Collections.unmodifiableList(properties);
    }

    public Set<String> getPropertyNames() {
        return propertiesByName.keySet();
    }

    public Set<String> getEditablePropertyNames() {
        return editablePropertiesByName.keySet();
    }

    public EntityProperty getPropertyForName(String property) {
        return propertiesByName.get(property);
    }

    /**
     * Clears and then fills the target list with either the odd or even
     * properties and relations.Always returns "id".
     *
     * @param extensions The extensions that should be enabled.
     * @param target the list to fill.
     * @param even if true, the even properties are taken, otherwise the odd.
     */
    public void getHalfPropertiesRelations(Set<Extension> extensions, List<String> target, final boolean even) {
        target.clear();
        target.add("id");
        boolean isEven = true;
        for (EntityProperty property : properties) {
            if (even == isEven) {
                target.add(property.name);
            }
            isEven = !isEven;
        }
        for (String relation : getRelations(extensions)) {
            if (even == isEven) {
                target.add(relation);
            }
            isEven = !isEven;
        }
    }

    private void addProperty(String name, boolean optional, boolean canSort) {
        addProperty(name, optional, canSort, true, "string");
    }

    private void addProperty(String name, boolean optional, boolean canSort, String jsonType) {
        addProperty(name, optional, canSort, true, jsonType);
    }

    private void addProperty(String name, boolean optional, boolean canSort, boolean canEdit, String jsonType) {
        EntityProperty property = new EntityProperty(name, optional, canSort, jsonType);
        properties.add(property);
        propertiesByName.put(name, property);
        if (canEdit) {
            editablePropertiesByName.put(name, property);
        }
    }

    private void addRelations(Extension extension, String... relations) {
        this.relations.computeIfAbsent(extension, t -> new ArrayList<>()).addAll(Arrays.asList(relations));
    }

    /**
     * The class representing an EntityProperty.
     */
    public static class EntityProperty {

        public final String name;
        public final boolean optional;
        public final boolean canSort;
        public final String jsonType;

        public EntityProperty(String name, boolean optional, boolean canSort, String jsonType) {
            this.name = name;
            this.optional = optional;
            this.canSort = canSort;
            this.jsonType = jsonType;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
