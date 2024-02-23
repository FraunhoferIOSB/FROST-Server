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
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.query.OrderBy;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Path;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
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

    /**
     * Flag indicating only admin users are allowed to see this entity type.
     */
    private boolean adminOnly;

    /**
     * How IDs are generated for this entity type.
     */
    private Object idGenerationMode;

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
     * The Set of PROPERTIES that Entities of this type have, mapped by their
     * name.
     */
    private final Map<String, Property> propertiesByName = new LinkedHashMap<>();
    /**
     * The set of Entity properties.
     */
    private final Set<EntityPropertyMain> entityProperties = new LinkedHashSet<>();
    /**
     * The set of Navigation properties.
     */
    private final Set<NavigationPropertyMain> navigationProperties = new TreeSet<>();
    /**
     * The set of Navigation properties pointing to single entities.
     */
    private final Set<NavigationPropertyMain<Entity>> navigationEntities = new TreeSet<>();
    /**
     * The set of Navigation properties pointing to entity sets.
     */
    private final Set<NavigationPropertyMain<EntitySet>> navigationSets = new TreeSet<>();

    private final Map<String, EntityValidator> validatorsCreateEntity = new LinkedHashMap<>();
    private final Map<String, EntityValidator> validatorsUpdateEntity = new LinkedHashMap<>();

    /**
     * The (OData)annotations for this Entity Type.
     */
    private final List<Annotation> annotations = new ArrayList<>();

    /**
     * The list of oderby's to use by default.
     */
    private final List<OrderBy> orderbyDefaults = new ArrayList<>();

    /**
     * The ModelRegistry this EntityType is registered on.
     */
    private ModelRegistry modelRegistry;

    public EntityType(String singular, String plural) {
        this(singular, plural, false);
    }

    public EntityType(String singular, String plural, boolean adminOnly) {
        this.entityName = singular;
        this.plural = plural;
        this.adminOnly = adminOnly;
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

    /**
     * Flag indicating only admin users are allowed to see this entity type.
     *
     * @return true if only admin users are allowed to see this entity type.
     */
    public boolean isAdminOnly() {
        return adminOnly;
    }

    /**
     * Set the flag indicating only admin users are allowed to see this entity
     * type.
     *
     * @param adminOnly if true, only admin users are allowed to see this entity
     * type.
     */
    public void setAdminOnly(boolean adminOnly) {
        this.adminOnly = adminOnly;
    }

    public void init() {
        if (initialised) {
            LOGGER.error("Re-Init of EntityType!");
        }
        initialised = true;
        for (Property property : properties) {
            if (property instanceof EntityPropertyMain entityPropertyMain) {
                entityProperties.add(entityPropertyMain);
            }
            if (property instanceof NavigationPropertyMain np) {
                if (np.getEntityType() == null) {
                    np.setEntityType(modelRegistry.getEntityTypeForName(np.getName(), true));
                }

                navigationProperties.add(np);
                if (np.isEntitySet()) {
                    navigationSets.add(np);
                } else {
                    navigationEntities.add(np);
                }
            }
        }
        // Make sure we have a default orderby and that it contains the primary key last.
        boolean pkOrder = false;
        final String pkName = primaryKey.getName();
        for (OrderBy order : orderbyDefaults) {
            if (pkName.equals(order.getExpression().toUrl())) {
                pkOrder = true;
            }
        }
        if (!pkOrder) {
            orderbyDefaults.add(new OrderBy(new Path(primaryKey), OrderBy.OrderType.ASCENDING));
        }
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
     * @return the unmodifiable map of Update-validators.
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
        if (property instanceof EntityPropertyMain entityPropertyMain) {
            return entityPropertyMain;
        }
        return null;
    }

    public NavigationPropertyMain getNavigationProperty(String name) {
        Property property = propertiesByName.get(name);
        if (property instanceof NavigationPropertyMain navigationPropertyMain) {
            return navigationPropertyMain;
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

    /**
     * Run Create-validators on the entity. This checks if all required
     * properties are non-null on the given Entity. This may change the entity
     * to remove computed or read-only values.
     *
     * @param entity the Entity to check.
     * @throws IncompleteEntityException If any of the required properties are
     * null.
     * @throws IllegalStateException If any of the required properties are
     * incorrect (i.e. Observation with both a Datastream and a MultiDatastream.
     */
    public void validateCreate(Entity entity) throws IncompleteEntityException {
        for (Property property : getPropertySet()) {
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
            validator.validate(entity);
        }
    }

    public void validateUpdate(Entity entity) throws IncompleteEntityException {
        for (Property property : getPropertySet()) {
            if (!(property instanceof EntityPropertyMain)) {
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
            }
        }
        for (EntityValidator validator : validatorsUpdateEntity.values()) {
            validator.validate(entity);
        }
    }

    public void setParent(PathElementEntitySet containingSet, Entity entity) throws IncompleteEntityException {
        EntityType setType = containingSet.getEntityType();
        if (setType != entity.getEntityType()) {
            throw new IllegalArgumentException("Set of type " + setType + " can not contain a " + entity.getEntityType());
        }
        PathElement parent = containingSet.getParent();
        if (parent == null) {
            throw new IllegalArgumentException("Set does not have a parent entity!");
        }
        if (parent instanceof PathElementEntity parentEntity) {
            Id parentId = parentEntity.getId();
            if (parentId == null) {
                return;
            }
            setParent(entity, containingSet.getNavigationProperty().getInverse(), parentId);
        }
    }

    private void setParent(Entity entity, NavigationPropertyMain navPropToParent, Id parentId) throws IncompleteEntityException {
        if (navPropToParent == null) {
            LOGGER.error("Incorrect 'parent' entity type for {}: {}", entityName, navPropToParent);
            throw new IncompleteEntityException("Incorrect 'parent' entity type for " + entityName + ": " + navPropToParent);
        }
        EntityType parentType = navPropToParent.getEntityType();
        if (navPropToParent instanceof NavigationPropertyEntitySet navPropToParentSet) {
            entity.addNavigationEntity(navPropToParentSet, new DefaultEntity(parentType).setId(parentId));
        } else if (navPropToParent instanceof NavigationPropertyEntity navPropToParentEntity) {
            Entity parent = entity.getProperty(navPropToParentEntity);
            if (parent != null && !parentId.equals(parent.getId())) {
                throw new IllegalArgumentException("Navigation property " + navPropToParent.getName() + " set in both JSON and URL.");
            }
            entity.setProperty(navPropToParent, new DefaultEntity(parentType).setId(parentId));
        } else {
            throw new IllegalStateException("Unknown navigation property type: " + navPropToParent);
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

    /**
     * How IDs are generated for this entity type. Exact values depend on the
     * PersistenceManager that is used.
     *
     * @return the idGenerationMode
     */
    public Object getIdGenerationMode() {
        return idGenerationMode;
    }

    /**
     * How IDs are generated for this entity type.
     *
     * @param idGenerationMode the idGenerationMode to set
     */
    public void setIdGenerationMode(Object idGenerationMode) {
        this.idGenerationMode = idGenerationMode;
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
        if (rawId instanceof UUID uuid) {
            return new IdUuid(uuid);
        }
        if (rawId instanceof Number number) {
            return new IdLong(number.longValue());
        }
        if (rawId instanceof CharSequence) {
            return new IdString(rawId.toString());
        }
        throw new IllegalArgumentException("Can not use " + ((rawId == null) ? "null" : rawId.getClass().getName()) + " (" + input + ") as an Id");
    }

    public List<OrderBy> getOrderbyDefaults() {
        return orderbyDefaults;
    }

    public EntityType addOrderByDefault(OrderBy order) {
        orderbyDefaults.add(order);
        return this;
    }

    public EntityType clearOrderByDefaults() {
        orderbyDefaults.clear();
        return this;
    }
}
