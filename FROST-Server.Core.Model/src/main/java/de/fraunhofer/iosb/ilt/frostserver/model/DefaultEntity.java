/*
 * Copyright (C) 2024 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PkValue;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PrimaryKey;
import de.fraunhofer.iosb.ilt.frostserver.path.UrlHelper;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author hylke
 */
public class DefaultEntity implements Entity {

    private EntityType entityType;
    private final Map<EntityPropertyMain, Object> entityProperties = new HashMap<>();
    private final Map<NavigationPropertyMain, Object> navProperties = new HashMap<>();
    private final Set<Property> setProperties = new HashSet<>();
    /**
     * The query used to load this entity.
     */
    private Query query;
    /**
     * The selfLink or @id of this entity.
     */
    private String selfLink;

    public DefaultEntity(EntityType entityType) {
        this.entityType = entityType;
    }

    public DefaultEntity(EntityType entityType, PkValue pkValue) {
        this.entityType = entityType;
        setPrimaryKeyValues(pkValue);
    }

    /**
     * Get the primary key definition of the (EntityType of the) Entity. This is
     * a shorthand for getEntityType().getPrimaryKey();
     *
     * @return The primary key definition of the Entity.
     */
    @Override
    public final PrimaryKey getPrimaryKey() {
        return entityType.getPrimaryKey();
    }

    /**
     * Key the values of the primary key fields for this Entity.
     *
     * @return the primary key values.
     */
    @Override
    public final PkValue getPrimaryKeyValues() {
        List<EntityPropertyMain> keyProperties = entityType.getPrimaryKey().getKeyProperties();
        PkValue pkValue = new PkValue(keyProperties.size());
        int idx = 0;
        for (EntityPropertyMain keyProperty : keyProperties) {
            pkValue.set(idx, getProperty(keyProperty));
            idx++;
        }
        return pkValue;
    }

    @Override
    public boolean primaryKeyFullySet() {
        List<EntityPropertyMain> keyProperties = entityType.getPrimaryKey().getKeyProperties();
        for (EntityPropertyMain keyProperty : keyProperties) {
            Object value = getProperty(keyProperty);
            if (value == null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public final DefaultEntity setPrimaryKeyValues(PkValue values) {
        int idx = 0;
        for (EntityPropertyMain keyProperty : entityType.getPrimaryKey().getKeyProperties()) {
            if (idx >= values.size()) {
                throw new IllegalArgumentException("No value given for keyProperty " + idx);
            }
            setProperty(keyProperty, values.get(idx));
            idx++;
        }
        return this;
    }

    @Override
    public String getSelfLink() {
        if (selfLink == null && query != null) {
            selfLink = UrlHelper.generateSelfLink(query.getPath(), this);
        }
        return selfLink;
    }

    @Override
    public DefaultEntity setSelfLink(String selfLink) {
        this.selfLink = selfLink;
        return this;
    }

    @Override
    public EntityType getEntityType() {
        return entityType;
    }

    @Override
    public DefaultEntity setEntityType(EntityType entityType) {
        if (this.entityType != null) {
            throw new IllegalArgumentException("the type of this entity is alread yet to " + this.entityType.entityName);
        }
        this.entityType = entityType;
        return this;
    }

    @Override
    public boolean isSetProperty(Property property) {
        if (property == ModelRegistry.EP_SELFLINK) {
            return true;
        }
        return setProperties.contains(property);
    }

    @Override
    public <P> P getProperty(Property<P> property) {
        if (property == ModelRegistry.EP_SELFLINK) {
            return (P) getSelfLink();
        } else if (property instanceof EntityPropertyMain entityPropertyMain) {
            return (P) entityProperties.get(entityPropertyMain);
        } else if (property instanceof NavigationPropertyMain navigationPropertyMain) {
            return (P) navProperties.get(navigationPropertyMain);
        }
        return property.getFrom(this);
    }

    @Override
    public Object getProperty(Path path) {
        Object result = this;
        for (Property element : path.getElements()) {
            if (result instanceof Entity entity) {
                result = element.getFrom(entity);
            } else if (result instanceof Map) {
                result = ((Map<String, Object>) result).get(element.getName());
            } else if (result instanceof List list) {
                try {
                    int idx = Integer.parseInt(element.getName());
                    result = list.get(idx);
                } catch (NumberFormatException exc) {
                    // it was not an index...
                    return null;
                }
            } else {
                return null;
            }
        }
        return result;
    }

    @Override
    public <P> DefaultEntity setProperty(Property<P> property, P value) {
        if (property == ModelRegistry.EP_SELFLINK) {
            setSelfLink(String.valueOf(value));
        } else if (property instanceof EntityPropertyMain entityPropertyMain) {
            entityProperties.put(entityPropertyMain, value);
            setProperties.add(property);
        } else if (property instanceof NavigationPropertyMain navigationPropertyMain) {
            navProperties.put(navigationPropertyMain, value);
            if (value == null) {
                setProperties.remove(property);
            } else {
                setProperties.add(property);
            }
        }
        return this;
    }

    @Override
    public DefaultEntity unsetProperty(Property property) {
        if (property instanceof EntityPropertyMain entityPropertyMain) {
            entityProperties.remove(entityPropertyMain);
        } else if (property instanceof NavigationPropertyMain navigationPropertyMain) {
            navProperties.remove(navigationPropertyMain);
        }
        setProperties.remove(property);
        return this;
    }

    @Override
    public DefaultEntity addNavigationEntity(NavigationPropertyEntitySet navProperty, Entity linkedEntity) {
        EntitySet entitySet = getProperty(navProperty);
        if (entitySet == null) {
            entitySet = new EntitySetImpl(navProperty);
            setProperty(navProperty, entitySet);
        }
        entitySet.add(linkedEntity);
        return this;
    }

    @Override
    public void setEntityPropertiesSet(boolean set, boolean entityPropertiesOnly) {
        if (!set) {
            setProperties.clear();
        } else {
            for (EntityPropertyMain property : entityType.getEntityProperties()) {
                if (!property.isReadOnly()) {
                    setProperties.add(property);
                }
            }
            if (!entityPropertiesOnly) {
                setProperties.addAll(entityType.getNavigationEntities());
            }
        }
    }

    @Override
    public void setEntityPropertiesSet(Entity comparedTo, EntityChangedMessage message) {
        setProperties.clear();
        for (EntityPropertyMain property : entityType.getEntityProperties()) {
            if (!Objects.equals(getProperty(property), comparedTo.getProperty(property))) {
                setProperties.add(property);
                message.addEpField(property);
            }
        }
        for (NavigationPropertyMain property : entityType.getNavigationEntities()) {
            if (!Objects.equals(getProperty(property), comparedTo.getProperty(property))) {
                setProperties.add(property);
                message.addNpField(property);
            }
        }
    }

    @Override
    public Query getQuery() {
        return query;
    }

    @Override
    public DefaultEntity setQuery(Query query) {
        this.query = query;
        return this;
    }

    @Override
    public boolean isEmpty() {
        // An entity is empty (not fully loaded) when the only property it has
        // is its ID, and the ID is not the only thing selected.
        // It is not empty when it has more than its ID, or when the ID is the
        // only thing selected.
        if (entityProperties.size() != 1) {
            return false;
        }
        if (query == null) {
            return true;
        }
        if (query.getSelect().size() == 1) {
            return !isSetProperty(query.getSelect().iterator().next());
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DefaultEntity other = (DefaultEntity) obj;
        if (!Objects.equals(this.entityType, other.entityType)) {
            return false;
        }
        if (!Objects.equals(this.entityProperties, other.entityProperties)) {
            return false;
        }
        return Objects.equals(this.navProperties, other.navProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityType, entityProperties, navProperties);
    }

    @Override
    public String toString() {
        return "Entity: " + entityType;
    }

}
