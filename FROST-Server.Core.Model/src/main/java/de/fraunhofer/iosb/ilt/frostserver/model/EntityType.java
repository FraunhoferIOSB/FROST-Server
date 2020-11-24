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

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntityValidator;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The types of entities in STA.
 *
 * @author jab, scf
 */
public class EntityType implements Comparable<EntityType> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityType.class.getName());

    public static final EntityType THING = new EntityType("Thing", "Things");
    public static final EntityType SENSOR = new EntityType("Sensor", "Sensors");
    public static final EntityType OBSERVED_PROPERTY = new EntityType("ObservedProperty", "ObservedProperties");
    public static final EntityType OBSERVATION = new EntityType("Observation", "Observations");
    public static final EntityType LOCATION = new EntityType("Location", "Locations");
    public static final EntityType HISTORICAL_LOCATION = new EntityType("HistoricalLocation", "HistoricalLocations");
    public static final EntityType FEATURE_OF_INTEREST = new EntityType("FeatureOfInterest", "FeaturesOfInterest");
    public static final EntityType MULTI_DATASTREAM = new EntityType("MultiDatastream", "MultiDatastreams");
    public static final EntityType DATASTREAM = new EntityType("Datastream", "Datastreams");

    private static final Map<String, EntityType> TYPES_BY_NAME = new HashMap<>();
    private static final Set<EntityType> TYPES = new LinkedHashSet<>();

    public static final void resetEntityTypes() {
        for (EntityType entityType : TYPES) {
            entityType.clear();
        }
        TYPES.clear();
        TYPES_BY_NAME.clear();
    }

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
            initDefaultTypes();
        }
        return TYPES_BY_NAME.get(typeName);
    }

    public static final Set<EntityType> getEntityTypes() {
        if (TYPES.isEmpty()) {
            initDefaultTypes();
        }
        return TYPES;
    }

    // ToDo: move to extensions
    public static synchronized void initDefaultTypes() {
        if (TYPES.contains(DATASTREAM)) {
            return;
        }
        registerEntityType(DATASTREAM)
                .registerProperty(EntityPropertyMain.ID, false)
                .registerProperty(EntityPropertyMain.SELFLINK, false)
                .registerProperty(EntityPropertyMain.NAME, true)
                .registerProperty(EntityPropertyMain.DESCRIPTION, true)
                .registerProperty(EntityPropertyMain.OBSERVATIONTYPE, true)
                .registerProperty(EntityPropertyMain.UNITOFMEASUREMENT, true)
                .registerProperty(EntityPropertyMain.OBSERVEDAREA, false)
                .registerProperty(EntityPropertyMain.PHENOMENONTIME_DS, false)
                .registerProperty(EntityPropertyMain.PROPERTIES, false)
                .registerProperty(EntityPropertyMain.RESULTTIME_DS, false)
                .registerProperty(NavigationPropertyMain.OBSERVEDPROPERTY, true)
                .registerProperty(NavigationPropertyMain.SENSOR, true)
                .registerProperty(NavigationPropertyMain.THING, true)
                .registerProperty(NavigationPropertyMain.OBSERVATIONS, false);
        registerEntityType(MULTI_DATASTREAM)
                .registerProperty(EntityPropertyMain.ID, false)
                .registerProperty(EntityPropertyMain.SELFLINK, false)
                .registerProperty(EntityPropertyMain.NAME, true)
                .registerProperty(EntityPropertyMain.DESCRIPTION, true)
                .registerProperty(EntityPropertyMain.OBSERVATIONTYPE, false)
                .registerProperty(EntityPropertyMain.MULTIOBSERVATIONDATATYPES, true)
                .registerProperty(EntityPropertyMain.UNITOFMEASUREMENTS, true)
                .registerProperty(EntityPropertyMain.OBSERVEDAREA, false)
                .registerProperty(EntityPropertyMain.PHENOMENONTIME_DS, false)
                .registerProperty(EntityPropertyMain.PROPERTIES, false)
                .registerProperty(EntityPropertyMain.RESULTTIME_DS, false)
                .registerProperty(NavigationPropertyMain.OBSERVEDPROPERTIES, false)
                .registerProperty(NavigationPropertyMain.SENSOR, true)
                .registerProperty(NavigationPropertyMain.THING, true)
                .registerProperty(NavigationPropertyMain.OBSERVATIONS, false)
                .addValidator((entity, entityPropertiesOnly) -> {
                    List<UnitOfMeasurement> unitOfMeasurements = entity.getProperty(EntityPropertyMain.UNITOFMEASUREMENTS);
                    List<String> multiObservationDataTypes = entity.getProperty(EntityPropertyMain.MULTIOBSERVATIONDATATYPES);
                    EntitySet observedProperties = entity.getProperty(NavigationPropertyMain.OBSERVEDPROPERTIES);
                    if (unitOfMeasurements == null || unitOfMeasurements.size() != multiObservationDataTypes.size()) {
                        throw new IllegalArgumentException("Size of list of unitOfMeasurements (" + unitOfMeasurements.size() + ") is not equal to size of multiObservationDataTypes (" + multiObservationDataTypes.size() + ").");
                    }
                    if (!entityPropertiesOnly && observedProperties == null || observedProperties.size() != multiObservationDataTypes.size()) {
                        final int opSize = observedProperties == null ? 0 : observedProperties.size();
                        throw new IllegalArgumentException("Size of list of observedProperties (" + opSize + ") is not equal to size of multiObservationDataTypes (" + multiObservationDataTypes.size() + ").");
                    }
                    String observationType = entity.getProperty(EntityPropertyMain.OBSERVATIONTYPE);
                    if (observationType == null || !observationType.equalsIgnoreCase("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation")) {
                        throw new IllegalArgumentException("ObservationType must be http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_ComplexObservation.");
                    }
                });
        registerEntityType(FEATURE_OF_INTEREST)
                .registerProperty(EntityPropertyMain.ID, false)
                .registerProperty(EntityPropertyMain.SELFLINK, false)
                .registerProperty(EntityPropertyMain.NAME, true)
                .registerProperty(EntityPropertyMain.DESCRIPTION, true)
                .registerProperty(EntityPropertyMain.ENCODINGTYPE, true)
                .registerProperty(EntityPropertyMain.FEATURE, true)
                .registerProperty(EntityPropertyMain.PROPERTIES, false)
                .registerProperty(NavigationPropertyMain.OBSERVATIONS, false);
        registerEntityType(HISTORICAL_LOCATION)
                .registerProperty(EntityPropertyMain.ID, false)
                .registerProperty(EntityPropertyMain.SELFLINK, false)
                .registerProperty(EntityPropertyMain.TIME, true)
                .registerProperty(NavigationPropertyMain.THING, true)
                .registerProperty(NavigationPropertyMain.LOCATIONS, false);
        registerEntityType(LOCATION)
                .registerProperty(EntityPropertyMain.ID, false)
                .registerProperty(EntityPropertyMain.SELFLINK, false)
                .registerProperty(EntityPropertyMain.NAME, true)
                .registerProperty(EntityPropertyMain.DESCRIPTION, true)
                .registerProperty(EntityPropertyMain.ENCODINGTYPE, true)
                .registerProperty(EntityPropertyMain.LOCATION, true)
                .registerProperty(EntityPropertyMain.PROPERTIES, false)
                .registerProperty(NavigationPropertyMain.HISTORICALLOCATIONS, false)
                .registerProperty(NavigationPropertyMain.THINGS, false);
        registerEntityType(OBSERVATION)
                .registerProperty(EntityPropertyMain.ID, false)
                .registerProperty(EntityPropertyMain.SELFLINK, false)
                .registerProperty(EntityPropertyMain.PHENOMENONTIME, false)
                .registerProperty(EntityPropertyMain.RESULTTIME, false)
                .registerProperty(EntityPropertyMain.RESULT, true)
                .registerProperty(EntityPropertyMain.RESULTQUALITY, false)
                .registerProperty(EntityPropertyMain.VALIDTIME, false)
                .registerProperty(EntityPropertyMain.PARAMETERS, false)
                .registerProperty(NavigationPropertyMain.DATASTREAM, false)
                .registerProperty(NavigationPropertyMain.MULTIDATASTREAM, false)
                .registerProperty(NavigationPropertyMain.FEATUREOFINTEREST, false)
                .addValidator((entity, entityPropertiesOnly) -> {
                    if (!entityPropertiesOnly) {
                        Entity datastream = entity.getProperty(NavigationPropertyMain.DATASTREAM);
                        Entity multiDatastream = entity.getProperty(NavigationPropertyMain.MULTIDATASTREAM);
                        if (datastream != null && multiDatastream != null) {
                            throw new IllegalArgumentException("Observation can not have both a Datasteam and a MultiDatastream.");
                        }
                        if (datastream == null && multiDatastream == null) {
                            throw new IncompleteEntityException("Observation must have either a Datasteam or a MultiDatastream.");
                        }
                        if (multiDatastream != null) {
                            Object result = entity.getProperty(EntityPropertyMain.RESULT);
                            if (!(result instanceof List)) {
                                throw new IllegalArgumentException("Observation in a MultiDatastream must have an Array result.");
                            }
                        }
                    }
                });
        registerEntityType(OBSERVED_PROPERTY)
                .registerProperty(EntityPropertyMain.ID, false)
                .registerProperty(EntityPropertyMain.SELFLINK, false)
                .registerProperty(EntityPropertyMain.NAME, true)
                .registerProperty(EntityPropertyMain.DEFINITION, true)
                .registerProperty(EntityPropertyMain.DESCRIPTION, true)
                .registerProperty(EntityPropertyMain.PROPERTIES, false)
                .registerProperty(NavigationPropertyMain.DATASTREAMS, false)
                .registerProperty(NavigationPropertyMain.MULTIDATASTREAMS, false);
        registerEntityType(SENSOR)
                .registerProperty(EntityPropertyMain.ID, false)
                .registerProperty(EntityPropertyMain.SELFLINK, false)
                .registerProperty(EntityPropertyMain.NAME, true)
                .registerProperty(EntityPropertyMain.DESCRIPTION, true)
                .registerProperty(EntityPropertyMain.ENCODINGTYPE, true)
                .registerProperty(EntityPropertyMain.METADATA, true)
                .registerProperty(EntityPropertyMain.PROPERTIES, false)
                .registerProperty(NavigationPropertyMain.DATASTREAMS, false)
                .registerProperty(NavigationPropertyMain.MULTIDATASTREAMS, false);
        registerEntityType(THING)
                .registerProperty(EntityPropertyMain.ID, false)
                .registerProperty(EntityPropertyMain.SELFLINK, false)
                .registerProperty(EntityPropertyMain.NAME, true)
                .registerProperty(EntityPropertyMain.DESCRIPTION, true)
                .registerProperty(EntityPropertyMain.PROPERTIES, false)
                .registerProperty(NavigationPropertyMain.LOCATIONS, false)
                .registerProperty(NavigationPropertyMain.HISTORICALLOCATIONS, false)
                .registerProperty(NavigationPropertyMain.DATASTREAMS, false)
                .registerProperty(NavigationPropertyMain.MULTIDATASTREAMS, false);
    }

    public static void initFinalise() {
        initDefaultTypes();
        LOGGER.info("Finalising {} EntityTypes.", TYPES.size());
        for (EntityType type : TYPES) {
            type.init();
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

    private boolean initialised = false;
    /**
     * The Set of PROPERTIES that Entities of this type have, mapped to the flag
     * indicating if they are required.
     */
    private final Map<Property, Boolean> propertyMap = new LinkedHashMap<>();
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
    private final Set<NavigationPropertyMain<Entity>> navigationEntities = new LinkedHashSet<>();
    /**
     * The set of Navigation properties pointing to entity sets.
     */
    private final Set<NavigationPropertyMain<EntitySet>> navigationSets = new LinkedHashSet<>();
    /**
     * The map of NavigationProperties by their target EntityTypes.
     */
    private final Map<EntityType, NavigationPropertyMain> navigationPropertiesByTarget = new HashMap<>();

    private final List<EntityValidator> validatorsNewEntity = new ArrayList<>();
    private final List<EntityValidator> validatorsUpdateEntity = new ArrayList<>();

    public EntityType(String singular, String plural) {
        this.entityName = singular;
        this.plural = plural;
    }

    public void clear() {
        initialised = false;
        propertyMap.clear();
        entityProperties.clear();
        navigationEntities.clear();
        navigationProperties.clear();
        navigationPropertiesByTarget.clear();
        navigationSets.clear();
        validatorsNewEntity.clear();
        validatorsUpdateEntity.clear();
    }

    public EntityType registerProperty(Property property, boolean required) {
        propertyMap.put(property, required);
        return this;
    }

    private void init() {
        initialised = true;
        for (Property property : propertyMap.keySet()) {
            if (property instanceof EntityPropertyMain) {
                entityProperties.add((EntityPropertyMain) property);
            }
            if (property instanceof NavigationPropertyMain) {
                NavigationPropertyMain np = (NavigationPropertyMain) property;
                navigationProperties.add(np);
                if (np.isEntitySet()) {
                    navigationSets.add(np);
                } else {
                    navigationEntities.add(np);
                }
                navigationPropertiesByTarget.put(np.getEntityType(), np);
            }
        }
    }

    public EntityType addValidator(EntityValidator validator) {
        validatorsNewEntity.add(validator);
        return this;
    }

    /**
     * The Map of PROPERTIES that Entities of this type have, with their
     * required status.
     *
     * @return The Set of PROPERTIES that Entities of this type have.
     */
    public Map<Property, Boolean> getPropertyMap() {
        if (!initialised) {
            initFinalise();
        }
        return propertyMap;
    }

    /**
     * The Set of PROPERTIES that Entities of this type have.
     *
     * @return The Set of PROPERTIES that Entities of this type have.
     */
    public Set<Property> getPropertySet() {
        if (!initialised) {
            initFinalise();
        }
        return propertyMap.keySet();
    }

    /**
     * Get the set of Entity properties.
     *
     * @return The set of Entity properties.
     */
    public Set<EntityPropertyMain> getEntityProperties() {
        if (!initialised) {
            initFinalise();
        }
        return entityProperties;
    }

    /**
     * Get the set of Navigation properties.
     *
     * @return The set of Navigation properties.
     */
    public Set<NavigationPropertyMain> getNavigationProperties() {
        if (!initialised) {
            initFinalise();
        }
        return navigationProperties;
    }

    /**
     * Get the set of Navigation properties pointing to single entities.
     *
     * @return The set of Navigation properties pointing to single entities.
     */
    public Set<NavigationPropertyMain<Entity>> getNavigationEntities() {
        if (!initialised) {
            initFinalise();
        }
        return navigationEntities;
    }

    /**
     * Get the set of Navigation properties pointing to entity sets.
     *
     * @return The set of Navigation properties pointing to entity sets.
     */
    public Set<NavigationPropertyMain<EntitySet>> getNavigationSets() {
        if (!initialised) {
            initFinalise();
        }
        return navigationSets;
    }

    public NavigationPropertyMain getNavigationProperty(EntityType to) {
        if (!initialised) {
            initFinalise();
        }
        return navigationPropertiesByTarget.get(to);
    }

    /**
     * @param property The property to check the required state for.
     * @return True when the property is required, false otherwise.
     */
    public boolean isRequired(Property property) {
        if (!initialised) {
            initFinalise();
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
        for (EntityValidator validator : validatorsNewEntity) {
            validator.validate(entity, entityPropertiesOnly);
        }
    }

    public void complete(Entity entity, PathElementEntitySet containingSet) throws IncompleteEntityException {
        EntityType type = containingSet.getEntityType();
        if (type != entity.getEntityType()) {
            throw new IllegalArgumentException("Set of type " + type + " can not contain a " + entity.getEntityType());
        }

        checkParent(containingSet, entity);

        complete(entity, false);
    }

    public void validateUpdate(Entity entity) throws IncompleteEntityException {
        for (EntityValidator validator : validatorsUpdateEntity) {
            validator.validate(entity, false);
        }
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

    @Override
    public int compareTo(EntityType o) {
        return entityName.compareTo(o.entityName);
    }

}
