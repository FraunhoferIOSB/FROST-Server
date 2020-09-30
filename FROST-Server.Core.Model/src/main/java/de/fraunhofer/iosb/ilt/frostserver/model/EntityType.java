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
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntityValidator;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElement;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.geojson.GeoJsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The types of entities in STA.
 *
 * @author jab, scf
 */
public class EntityType {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityType.class.getName());

    public static final EntityType THING = new EntityType("Thing", "Things");
    public static final EntityType TASKING_CAPABILITY = new EntityType("TaskingCapability", "TaskingCapabilities");
    public static final EntityType TASK = new EntityType("Task", "Tasks");
    public static final EntityType SENSOR = new EntityType("Sensor", "Sensors");
    public static final EntityType OBSERVED_PROPERTY = new EntityType("ObservedProperty", "ObservedProperties");
    public static final EntityType OBSERVATION = new EntityType("Observation", "Observations");
    public static final EntityType LOCATION = new EntityType("Location", "Locations");
    public static final EntityType HISTORICAL_LOCATION = new EntityType("HistoricalLocation", "HistoricalLocations");
    public static final EntityType FEATURE_OF_INTEREST = new EntityType("FeatureOfInterest", "FeaturesOfInterest");
    public static final EntityType MULTI_DATASTREAM = new EntityType("MultiDatastream", "MultiDatastreams");
    public static final EntityType DATASTREAM = new EntityType("Datastream", "Datastreams");
    public static final EntityType ACTUATOR = new EntityType("Actuator", "Actuators");

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

    private static final Map<String, EntityType> TYPES_BY_NAME = new HashMap<>();
    private static final Set<EntityType> TYPES = new LinkedHashSet<>();

    public static final EntityType registerEntityType(EntityType type) {
        if (TYPES_BY_NAME.containsKey(type.entityName)) {
            throw new IllegalArgumentException("An entity type named " + type.entityName + " is already registered");
        }
        TYPES_BY_NAME.put(type.entityName, type);
        TYPES_BY_NAME.put(type.plural, type);
        TYPES.add(type);
        return type;
    }

    public static final EntityType getEntityTypeForName(String typeName) {
        if (TYPES_BY_NAME.isEmpty()) {
            init();
        }
        return TYPES_BY_NAME.get(typeName);
    }

    public static final Set<EntityType> getEntityTypes() {
        if (TYPES.isEmpty()) {
            init();
        }
        return TYPES;
    }

    // ToDo: move to extensions
    private static synchronized void init() {
        if (!TYPES_BY_NAME.isEmpty()) {
            return;
        }
        registerEntityType(ACTUATOR)
                .registerProperty(EntityPropertyMain.ID, false, TYPE_REFERENCE_ID)
                .registerProperty(EntityPropertyMain.SELFLINK, false, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.NAME, true, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.DESCRIPTION, true, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.ENCODINGTYPE, true, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.METADATA, true, TYPE_REFERENCE_OBJECT)
                .registerProperty(EntityPropertyMain.PROPERTIES, false, TYPE_REFERENCE_MAP)
                .registerProperty(NavigationPropertyMain.TASKINGCAPABILITIES, false, null);
        registerEntityType(DATASTREAM)
                .registerProperty(EntityPropertyMain.ID, false, TYPE_REFERENCE_ID)
                .registerProperty(EntityPropertyMain.SELFLINK, false, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.NAME, true, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.DESCRIPTION, true, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.OBSERVATIONTYPE, true, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.UNITOFMEASUREMENT, true, TYPE_REFERENCE_UOM)
                .registerProperty(EntityPropertyMain.OBSERVEDAREA, false, TYPE_REFERENCE_GEOJSONOBJECT)
                .registerProperty(EntityPropertyMain.PHENOMENONTIME, false, TYPE_REFERENCE_TIME_INTERVAL)
                .registerProperty(EntityPropertyMain.PROPERTIES, false, TYPE_REFERENCE_MAP)
                .registerProperty(EntityPropertyMain.RESULTTIME, false, TYPE_REFERENCE_TIME_INTERVAL)
                .registerProperty(NavigationPropertyMain.OBSERVEDPROPERTY, true, null)
                .registerProperty(NavigationPropertyMain.SENSOR, true, null)
                .registerProperty(NavigationPropertyMain.THING, true, null)
                .registerProperty(NavigationPropertyMain.OBSERVATIONS, false, null);
        registerEntityType(MULTI_DATASTREAM)
                .registerProperty(EntityPropertyMain.ID, false, TYPE_REFERENCE_ID)
                .registerProperty(EntityPropertyMain.SELFLINK, false, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.NAME, true, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.DESCRIPTION, true, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.OBSERVATIONTYPE, false, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.MULTIOBSERVATIONDATATYPES, true, TYPE_REFERENCE_LIST_STRING)
                .registerProperty(EntityPropertyMain.UNITOFMEASUREMENTS, true, TYPE_REFERENCE_LIST_UOM)
                .registerProperty(EntityPropertyMain.OBSERVEDAREA, false, TYPE_REFERENCE_GEOJSONOBJECT)
                .registerProperty(EntityPropertyMain.PHENOMENONTIME, false, TYPE_REFERENCE_TIME_INTERVAL)
                .registerProperty(EntityPropertyMain.PROPERTIES, false, TYPE_REFERENCE_MAP)
                .registerProperty(EntityPropertyMain.RESULTTIME, false, TYPE_REFERENCE_TIME_INTERVAL)
                .registerProperty(NavigationPropertyMain.OBSERVEDPROPERTIES, false, null)
                .registerProperty(NavigationPropertyMain.SENSOR, true, null)
                .registerProperty(NavigationPropertyMain.THING, true, null)
                .registerProperty(NavigationPropertyMain.OBSERVATIONS, false, null)
                .addValidator((entity, entityPropertiesOnly) -> {
                    // TODO: When Properties are updated with result value generic, update this.
                    List<UnitOfMeasurement> unitOfMeasurements = (List<UnitOfMeasurement>) entity.getProperty(EntityPropertyMain.UNITOFMEASUREMENTS);
                    List<String> multiObservationDataTypes = (List<String>) entity.getProperty(EntityPropertyMain.MULTIOBSERVATIONDATATYPES);
                    EntitySet observedProperties = (EntitySet) entity.getProperty(NavigationPropertyMain.OBSERVEDPROPERTIES);
                    if (unitOfMeasurements == null || unitOfMeasurements.size() != multiObservationDataTypes.size()) {
                        throw new IllegalStateException("Size of list of unitOfMeasurements (" + unitOfMeasurements.size() + ") is not equal to size of multiObservationDataTypes (" + multiObservationDataTypes.size() + ").");
                    }
                    if (!entityPropertiesOnly && observedProperties == null || observedProperties.size() != multiObservationDataTypes.size()) {
                        final int opSize = observedProperties == null ? 0 : observedProperties.size();
                        throw new IllegalStateException("Size of list of observedProperties (" + opSize + ") is not equal to size of multiObservationDataTypes (" + multiObservationDataTypes.size() + ").");
                    }
                    String observationType = (String) entity.getProperty(EntityPropertyMain.OBSERVATIONTYPE);
                    if (observationType == null || !observationType.equalsIgnoreCase("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation")) {
                        throw new IllegalStateException("ObservationType must be http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation.");
                    }
                });
        registerEntityType(FEATURE_OF_INTEREST)
                .registerProperty(EntityPropertyMain.ID, false, TYPE_REFERENCE_ID)
                .registerProperty(EntityPropertyMain.SELFLINK, false, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.NAME, true, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.DESCRIPTION, true, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.ENCODINGTYPE, true, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.FEATURE, true, null)
                .registerProperty(EntityPropertyMain.PROPERTIES, false, TYPE_REFERENCE_MAP)
                .registerProperty(NavigationPropertyMain.OBSERVATIONS, false, null);
        registerEntityType(HISTORICAL_LOCATION)
                .registerProperty(EntityPropertyMain.ID, false, TYPE_REFERENCE_ID)
                .registerProperty(EntityPropertyMain.SELFLINK, false, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.TIME, true, TYPE_REFERENCE_TIMEINSTANT)
                .registerProperty(NavigationPropertyMain.THING, true, null)
                .registerProperty(NavigationPropertyMain.LOCATIONS, false, null);
        registerEntityType(LOCATION)
                .registerProperty(EntityPropertyMain.ID, false, TYPE_REFERENCE_ID)
                .registerProperty(EntityPropertyMain.SELFLINK, false, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.NAME, true, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.DESCRIPTION, true, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.ENCODINGTYPE, true, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.LOCATION, true, null)
                .registerProperty(EntityPropertyMain.PROPERTIES, false, TYPE_REFERENCE_MAP)
                .registerProperty(NavigationPropertyMain.HISTORICALLOCATIONS, false, null)
                .registerProperty(NavigationPropertyMain.THINGS, false, null);
        registerEntityType(OBSERVATION)
                .registerProperty(EntityPropertyMain.ID, false, TYPE_REFERENCE_ID)
                .registerProperty(EntityPropertyMain.SELFLINK, false, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.PHENOMENONTIME, false, TYPE_REFERENCE_TIMEVALUE)
                .registerProperty(EntityPropertyMain.RESULTTIME, false, TYPE_REFERENCE_TIMEINSTANT)
                .registerProperty(EntityPropertyMain.RESULT, true, TYPE_REFERENCE_OBJECT)
                .registerProperty(EntityPropertyMain.RESULTQUALITY, false, TYPE_REFERENCE_OBJECT)
                .registerProperty(EntityPropertyMain.VALIDTIME, false, TYPE_REFERENCE_TIME_INTERVAL)
                .registerProperty(EntityPropertyMain.PARAMETERS, false, TYPE_REFERENCE_MAP)
                .registerProperty(NavigationPropertyMain.DATASTREAM, false, null)
                .registerProperty(NavigationPropertyMain.MULTIDATASTREAM, false, null)
                .registerProperty(NavigationPropertyMain.FEATUREOFINTEREST, false, null)
                .addValidator((entity, entityPropertiesOnly) -> {
                    if (!entityPropertiesOnly) {
                        Entity datastream = (Entity) entity.getProperty(NavigationPropertyMain.DATASTREAM);
                        Entity multiDatastream = (Entity) entity.getProperty(NavigationPropertyMain.MULTIDATASTREAM);
                        if (datastream != null && multiDatastream != null) {
                            throw new IllegalStateException("Observation can not have both a Datasteam and a MultiDatastream.");
                        }
                        if (datastream == null && multiDatastream == null) {
                            throw new IllegalStateException("Observation must have either a Datasteam or a MultiDatastream.");
                        }
                    }
                });
        registerEntityType(OBSERVED_PROPERTY)
                .registerProperty(EntityPropertyMain.ID, false, TYPE_REFERENCE_ID)
                .registerProperty(EntityPropertyMain.SELFLINK, false, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.NAME, true, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.DEFINITION, true, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.DESCRIPTION, true, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.PROPERTIES, false, TYPE_REFERENCE_MAP)
                .registerProperty(NavigationPropertyMain.DATASTREAMS, false, null)
                .registerProperty(NavigationPropertyMain.MULTIDATASTREAMS, false, null);
        registerEntityType(SENSOR)
                .registerProperty(EntityPropertyMain.ID, false, TYPE_REFERENCE_ID)
                .registerProperty(EntityPropertyMain.SELFLINK, false, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.NAME, true, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.DESCRIPTION, true, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.ENCODINGTYPE, true, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.METADATA, true, TYPE_REFERENCE_OBJECT)
                .registerProperty(EntityPropertyMain.PROPERTIES, false, TYPE_REFERENCE_MAP)
                .registerProperty(NavigationPropertyMain.DATASTREAMS, false, null)
                .registerProperty(NavigationPropertyMain.MULTIDATASTREAMS, false, null);
        registerEntityType(TASK)
                .registerProperty(EntityPropertyMain.ID, false, TYPE_REFERENCE_ID)
                .registerProperty(EntityPropertyMain.SELFLINK, false, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.CREATIONTIME, false, TYPE_REFERENCE_TIMEINSTANT)
                .registerProperty(EntityPropertyMain.TASKINGPARAMETERS, true, TYPE_REFERENCE_MAP)
                .registerProperty(NavigationPropertyMain.TASKINGCAPABILITY, true, null);
        registerEntityType(TASKING_CAPABILITY)
                .registerProperty(EntityPropertyMain.ID, false, TYPE_REFERENCE_ID)
                .registerProperty(EntityPropertyMain.SELFLINK, false, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.NAME, true, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.DESCRIPTION, true, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.PROPERTIES, false, TYPE_REFERENCE_MAP)
                .registerProperty(EntityPropertyMain.TASKINGPARAMETERS, true, TYPE_REFERENCE_MAP)
                .registerProperty(NavigationPropertyMain.ACTUATOR, true, null)
                .registerProperty(NavigationPropertyMain.TASKS, false, null)
                .registerProperty(NavigationPropertyMain.THING, true, null);
        registerEntityType(THING)
                .registerProperty(EntityPropertyMain.ID, false, TYPE_REFERENCE_ID)
                .registerProperty(EntityPropertyMain.SELFLINK, false, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.NAME, true, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.DESCRIPTION, true, TYPE_REFERENCE_STRING)
                .registerProperty(EntityPropertyMain.PROPERTIES, false, TYPE_REFERENCE_MAP)
                .registerProperty(NavigationPropertyMain.LOCATIONS, false, null)
                .registerProperty(NavigationPropertyMain.HISTORICALLOCATIONS, false, null)
                .registerProperty(NavigationPropertyMain.DATASTREAMS, false, null)
                .registerProperty(NavigationPropertyMain.MULTIDATASTREAMS, false, null)
                .registerProperty(NavigationPropertyMain.TASKINGCAPABILITIES, false, null);

        // ToDo: This needs to be called after extensions have had a chance to register their types and modify 
        initFinalise();
    }

    private static void initFinalise() {
        for (EntityType type : TYPES) {
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

    /**
     * The entitiyName of this entity type as used in URLs.
     */
    public final String entityName;
    /**
     * The entitiyName of collections of this entity type as used in URLs.
     */
    public final String plural;

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

    private final List<EntityValidator> entityValidators = new ArrayList<>();

    private EntityType(String singular, String plural) {
        this.entityName = singular;
        this.plural = plural;
    }

    public EntityType registerProperty(Property property, boolean required, TypeReference type) {
        propertyMap.put(property, required);
        if (property instanceof EntityPropertyMain && type != null) {
            propertyTypes.put((EntityPropertyMain) property, type);
        }
        return this;
    }

    public EntityType addValidator(EntityValidator validator) {
        entityValidators.add(validator);
        return this;
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
        return propertyMap.getOrDefault(property, false);
    }

    /**
     * Check if all required properties are non-null on the given Entity.
     *
     * @param entity the Entity to check.
     * @param entityPropertiesOnly flag indicating only the EntityProperties
     * should be checked.
     * @throws IncompleteEntityException If any of the required properties are
     * null.
     * @throws IllegalStateException If any of the required properties are
     * incorrect (i.e. Observation with both a Datastream and a MultiDatastream.
     */
    public void complete(Entity entity, boolean entityPropertiesOnly) throws IncompleteEntityException {
        for (Property property : getPropertySet()) {
            if (entityPropertiesOnly && !(property instanceof EntityPropertyMain)) {
                continue;
            }
            if (isRequired(property) && !entity.isSetProperty(property)) {
                throw new IncompleteEntityException("Missing required property '" + property.getJsonName() + "'");
            }
        }
        for (EntityValidator validator : entityValidators) {
            validator.complete(entity, entityPropertiesOnly);
        }
    }

    public void complete(Entity entity, PathElementEntitySet containingSet) throws IncompleteEntityException {
        EntityType type = containingSet.getEntityType();
        if (type != entity.getEntityType()) {
            throw new IllegalStateException("Set of type " + type + " can not contain a " + entity.getEntityType());
        }

        checkParent(containingSet, entity);

        complete(entity, false);
    }

    private void checkParent(PathElementEntitySet containingSet, Entity entity) throws IncompleteEntityException {
        PathElement parent = containingSet.getParent();
        if (parent instanceof PathElementEntity) {
            PathElementEntity parentEntity = (PathElementEntity) parent;
            Id parentId = parentEntity.getId();
            if (parentId == null) {
                return;
            }
            checkParent(entity, parentEntity, parentId);
        }
    }

    private void checkParent(Entity entity, PathElementEntity parentEntity, Id parentId) throws IncompleteEntityException {
        EntityType parentType = parentEntity.getEntityType();
        final NavigationPropertyMain parentNavProperty = NavigationPropertyMain.fromString(parentType.entityName);
        if (!navigationEntities.contains(parentNavProperty)) {
            LOGGER.error("Incorrect 'parent' entity type for {}: {}", entityName, parentType);
            throw new IncompleteEntityException("Incorrect 'parent' entity type for " + entityName + ": " + parentType);
        }
        entity.setProperty(parentNavProperty, new DefaultEntity(parentType).setId(parentId));
    }

    @Override
    public String toString() {
        return entityName;
    }

}
