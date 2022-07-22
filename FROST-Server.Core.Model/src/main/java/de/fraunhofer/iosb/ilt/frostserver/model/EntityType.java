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
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdLong;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdString;
import de.fraunhofer.iosb.ilt.frostserver.model.core.IdUuid;
import de.fraunhofer.iosb.ilt.frostserver.model.core.annotations.Annotatable;
import de.fraunhofer.iosb.ilt.frostserver.model.core.annotations.Annotation;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElement;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The types of entities in STA.
 *
 * @author jab, scf
 */
public class EntityType implements Annotatable, Comparable<EntityType> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityType.class.getName());

    /**
     * The entityName of this entity type as used in URLs.
     */
    public final String entityName;
    /**
     * The entityName of collections of this entity type as used in URLs.
     */
    public final String plural;

    private boolean initialised = false;

    /**
     * The Property that is the primary key of the entity.
     */
    private EntityPropertyMain primaryKey;
    /**
     * The Set of PROPERTIES that Entities of this type have.
     */
    private final Set<Property> properties = new LinkedHashSet<>();
    /**
     * The Set of PROPERTIES that Entities of this type have, mapped by their name.
     */
    private final Map<String, Property> propertiesByName = new LinkedHashMap<>();
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

    private final Map<String, EntityValidator> validatorsCreateEntity = new LinkedHashMap<>();
    private final Map<String, EntityValidator> validatorsUpdateEntity = new LinkedHashMap<>();

    /**
     * The (OData)annotations for this Entity Type.
     */
    private final List<Annotation> annotations = new ArrayList<>();

    /**
     * The ModelRegistry this EntityType is registered on.
     */
    private ModelRegistry modelRegistry;

    public EntityType(String singular, String plural) {
        this.entityName = singular;
        this.plural = plural;
    }

    public EntityType registerProperty(Property property) {
        properties.add(property);
        propertiesByName.put(property.getName(), property);
        if (property instanceof EntityPropertyMain) {
            EntityPropertyMain<?> propertyMain = (EntityPropertyMain<?>) property;
            for (String alias : propertyMain.getAliases()) {
                propertiesByName.put(alias, property);
            }
            if (primaryKey == null) {
                primaryKey = propertyMain;
            }
        }
        return this;
    }

    public void init() {
        if (initialised) {
            LOGGER.error("Re-Init of EntityType!");
        }
        initialised = true;
        for (Property property : properties) {
            if (property instanceof EntityPropertyMain) {
                entityProperties.add((EntityPropertyMain) property);
            }
            if (property instanceof NavigationPropertyMain) {
                NavigationPropertyMain np = (NavigationPropertyMain) property;
                if (np.getEntityType() == null) {
                    np.setEntityType(modelRegistry.getEntityTypeForName(np.getName()));
                }

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

    /**
     * Adds a creation-validator to the entity type.
     *
     * @param validator The validator to add.
     * @return this.
     * @deprecated use {@link #addCreateValidator(String, EntityValidator)} instead
     */
    @Deprecated(forRemoval = true)
    public EntityType addValidator(EntityValidator validator) {
        return addValidatorForCreate(validator);
    }

    /**
     * Adds a creation-validator to the entity type with a generated name.
     *
     * @param validator The validator to add
     * @return this
     * @deprecated use {@link #addCreateValidator(String, EntityValidator)} instead
     */
    @Deprecated(forRemoval = true)
    public EntityType addValidatorForCreate(EntityValidator validator) {
        int count = validatorsCreateEntity.size() + 1;
        while (validatorsCreateEntity.containsKey(Integer.toString(count))) {
            count++;
        }
        return addCreateValidator(Integer.toString(count), validator);
    }

    /**
     * Adds a create-validator to the entity type with the given name.
     *
     * @param name The name of the validator.
     * @param validator The validator to add.
     * @return this
     */
    public EntityType addCreateValidator(String name, EntityValidator validator) {
        EntityValidator value = validatorsCreateEntity.putIfAbsent(name, validator);
        if (value != null) {
            throw new IllegalArgumentException("A CreateValidator for " + entityName + " already exists with name " + name);
        }
        return this;
    }

    /**
     * Get the unmodifiable map of Create-validators.
     *
     * @return the unmodifiable map of Create-validators.
     */
    public Map<String, EntityValidator> getCreateValidators() {
        return Collections.unmodifiableMap(validatorsCreateEntity);
    }

    /**
     * Remove the UpdateValidator with the given name.
     *
     * @param name The name of the validator to remove.
     * @return the removed Validator.
     */
    public EntityValidator removeCreateValidator(String name) {
        return validatorsCreateEntity.remove(name);
    }

    /**
     * Adds an update-validator to the entity type with a generated name.
     *
     * @param validator The validator to add
     * @return this
     * @deprecated use {@link #addUpdateValidator(String, EntityValidator)} instead
     */
    @Deprecated(forRemoval = true)
    public EntityType addValidatorForUpdate(EntityValidator validator) {
        int count = validatorsUpdateEntity.size() + 1;
        while (validatorsUpdateEntity.containsKey(Integer.toString(count))) {
            count++;
        }
        return addUpdateValidator(Integer.toString(count), validator);
    }

    /**
     * Adds an update-validator to the entity type with the given name.
     *
     * @param name The name of the validator.
     * @param validator The validator to add.
     * @return this
     */
    public EntityType addUpdateValidator(String name, EntityValidator validator) {
        EntityValidator value = validatorsUpdateEntity.putIfAbsent(name, validator);
        if (value != null) {
            throw new IllegalArgumentException("An UpdateValidator for " + entityName + " already exists with name " + name);
        }
        return this;
    }

    /**
     * Get the unmodifiable map of Update-validators.
     *
     * @return the unmodifiable map of Create-validators.
     */
    public Map<String, EntityValidator> getUpdateValidators() {
        return Collections.unmodifiableMap(validatorsUpdateEntity);
    }

    /**
     * Remove the UpdateValidator with the given name.
     *
     * @param name The name of the validator to remove.
     * @return the removed Validator.
     */
    public EntityValidator removeUpdateValidator(String name) {
        return validatorsUpdateEntity.remove(name);
    }

    public EntityPropertyMain<Id> getPrimaryKey() {
        return primaryKey;
    }

    public Property getProperty(String name) {
        return propertiesByName.get(name);
    }

    public EntityPropertyMain getEntityProperty(String name) {
        Property property = propertiesByName.get(name);
        if (property instanceof EntityPropertyMain) {
            return (EntityPropertyMain) property;
        }
        return null;
    }

    public NavigationPropertyMain getNavigationProperty(String name) {
        Property property = propertiesByName.get(name);
        if (property instanceof NavigationPropertyMain) {
            return (NavigationPropertyMain) property;
        }
        return null;
    }

    /**
     * The Set of PROPERTIES that Entities of this type have.
     *
     * @return The Set of PROPERTIES that Entities of this type have.
     */
    public Set<Property> getPropertySet() {
        return properties;
    }

    /**
     * Get the set of Entity properties.
     *
     * @return The set of Entity properties.
     */
    public Set<EntityPropertyMain> getEntityProperties() {
        return entityProperties;
    }

    /**
     * Get the set of Navigation properties.
     *
     * @return The set of Navigation properties.
     */
    public Set<NavigationPropertyMain> getNavigationProperties() {
        return navigationProperties;
    }

    /**
     * Get the set of Navigation properties pointing to single entities.
     *
     * @return The set of Navigation properties pointing to single entities.
     */
    public Set<NavigationPropertyMain<Entity>> getNavigationEntities() {
        return navigationEntities;
    }

    /**
     * Get the set of Navigation properties pointing to entity sets.
     *
     * @return The set of Navigation properties pointing to entity sets.
     */
    public Set<NavigationPropertyMain<EntitySet>> getNavigationSets() {
        return navigationSets;
    }

    public NavigationPropertyMain getNavigationProperty(EntityType to) {
        return navigationPropertiesByTarget.get(to);
    }

    /**
     * Check if all required properties are non-null on the given Entity.
     *
     * @param entity the Entity to check.
     * @param entityPropertiesOnly flag indicating only the EntityProperties should be checked.
     * @throws IncompleteEntityException If any of the required properties are null.
     * @throws IllegalStateException If any of the required properties are incorrect (i.e. Observation with both a
     * Datastream and a MultiDatastream.
     */
    public void complete(Entity entity, boolean entityPropertiesOnly) throws IncompleteEntityException {
        for (Property property : getPropertySet()) {
            if (entityPropertiesOnly && !(property instanceof EntityPropertyMain)) {
                continue;
            }
            if (entity.isSetProperty(property)) {
                if (property.isReadOnly()) {
                    entity.unsetProperty(property);
                    continue;
                }
                if (!property.isNullable() && entity.getProperty(property) == null) {
                    throw new IncompleteEntityException("Property '" + property.getJsonName() + "' must be non-NULL.");
                }
            } else {
                if (property.isRequired()) {
                    throw new IncompleteEntityException("Missing required property '" + property.getJsonName() + "'");
                }
            }
        }
        for (EntityValidator validator : validatorsCreateEntity.values()) {
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
        for (EntityValidator validator : validatorsUpdateEntity.values()) {
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
            checkParent(entity, containingSet.getNavigationProperty().getInverse(), parentId);
        }
    }

    private void checkParent(Entity entity, NavigationPropertyMain navPropToParent, Id parentId) throws IncompleteEntityException {
        if (navPropToParent == null) {
            LOGGER.error("Incorrect 'parent' entity type for {}: {}", entityName, navPropToParent);
            throw new IncompleteEntityException("Incorrect 'parent' entity type for " + entityName + ": " + navPropToParent);
        }
        EntityType parentType = navPropToParent.getEntityType();
        if (navPropToParent.isEntitySet()) {
            entity.addNavigationEntity(new DefaultEntity(parentType).setId(parentId));
        } else {
            entity.setProperty(navPropToParent, new DefaultEntity(parentType).setId(parentId));
        }
    }

    /**
     * The ModelRegistry this EntityType is registered on.
     *
     * @return the modelRegistry
     */
    public ModelRegistry getModelRegistry() {
        return modelRegistry;
    }

    /**
     * The ModelRegistry this EntityType is registered on.
     *
     * @param modelRegistry the modelRegistry to set
     */
    public void setModelRegistry(ModelRegistry modelRegistry) {
        if (this.modelRegistry != null && this.modelRegistry != modelRegistry) {
            throw new IllegalArgumentException("Changing the ModelRegistry on an EntityType is not allowed.");
        }
        this.modelRegistry = modelRegistry;
    }

    @Override
    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public EntityType addAnnotation(Annotation annotation) {
        annotations.add(annotation);
        return this;
    }

    public EntityType addAnnotations(List<Annotation> annotationsToAdd) {
        annotations.addAll(annotationsToAdd);
        return this;
    }

    @Override
    public String toString() {
        return entityName;
    }

    @Override
    public int compareTo(EntityType o) {
        return entityName.compareTo(o.entityName);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EntityType)) {
            return false;
        }
        EntityType other = (EntityType) obj;
        if (entityName.equals(other.entityName)) {
            LOGGER.error("Found other instance of {}", entityName);
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.entityName);
        return hash;
    }

    public Id parsePrimaryKey(String input) {
        Object rawId = primaryKey.getType().parseFromUrl(input);
        if (rawId instanceof UUID) {
            return new IdUuid((UUID) rawId);
        }
        if (rawId instanceof Number) {
            return new IdLong(((Number) rawId).longValue());
        }
        if (rawId instanceof CharSequence) {
            return new IdString(rawId.toString());
        }
        throw new IllegalArgumentException("Can not use " + ((rawId == null) ? "null" : rawId.getClass().getName()) + " (" + input + ") as an Id");
    }
}
