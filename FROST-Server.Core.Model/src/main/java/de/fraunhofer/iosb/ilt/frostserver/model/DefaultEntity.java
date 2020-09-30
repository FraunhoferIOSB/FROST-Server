/*
 * Copyright (C) 2020 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.path.UrlHelper;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
public class DefaultEntity implements Entity<DefaultEntity> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEntity.class.getName());

    private EntityType entityType;
    private final Map<EntityPropertyMain, Object> entityProperties = new HashMap<>();
    private final Map<NavigationPropertyMain, Object> navProperties = new HashMap<>();
    private final Set<Property> setProperties = new HashSet<>();
    private Query query;

    public DefaultEntity(EntityType entityType) {
        this.entityType = entityType;
    }

    public DefaultEntity(EntityType entityType, Id id) {
        this.entityType = entityType;
        setId(id);
    }

    @Override
    public final Id getId() {
        return (Id) entityProperties.get(EntityPropertyMain.ID);
    }

    @Override
    public final DefaultEntity setId(Id id) {
        entityProperties.put(EntityPropertyMain.ID, id);
        setProperties.add(EntityPropertyMain.ID);
        return this;
    }

    @Override
    public String getSelfLink() {
        String selfLink = (String) entityProperties.get(EntityPropertyMain.SELFLINK);
        if (selfLink == null) {
            selfLink = UrlHelper.generateSelfLink(query.getPath(), this);
            entityProperties.put(EntityPropertyMain.SELFLINK, selfLink);
        }
        return selfLink;
    }

    @Override
    public DefaultEntity setSelfLink(String selfLink) {
        entityProperties.put(EntityPropertyMain.SELFLINK, selfLink);
        setProperties.add(EntityPropertyMain.SELFLINK);
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
        return setProperties.contains(property);
    }

    @Override
    public Object getProperty(Property property) {
        if (property instanceof EntityPropertyMain) {
            return entityProperties.get((EntityPropertyMain) property);
        } else if (property instanceof NavigationPropertyMain) {
            return navProperties.get((NavigationPropertyMain) property);
        }
        return property.getFrom(this);
    }

    @Override
    public DefaultEntity setProperty(Property property, Object value) {
        if (property instanceof EntityPropertyMain) {
            entityProperties.put((EntityPropertyMain) property, value);
            setProperties.add(property);
        } else if (property instanceof NavigationPropertyMain) {
            navProperties.put((NavigationPropertyMain) property, value);
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
        if (property instanceof EntityPropertyMain) {
            entityProperties.remove((EntityPropertyMain) property);
        } else if (property instanceof NavigationPropertyMain) {
            navProperties.remove((NavigationPropertyMain) property);
        }
        setProperties.add(property);
        return this;
    }

    @Override
    public DefaultEntity addNavigationEntity(Entity linkedEntity) {
        final EntityType linkedType = linkedEntity.getEntityType();
        final NavigationPropertyMain navProperty = NavigationPropertyMain.fromString(linkedType.plural);
        EntitySet entitySet = (EntitySet) getProperty(navProperty);
        if (entitySet == null) {
            entitySet = new EntitySetImpl(linkedType);
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
            setProperties.addAll(entityType.getEntityProperties());
            if (!entityPropertiesOnly) {
                setProperties.addAll(entityType.getNavigationEntities());
            }
        }
    }

    @Override
    public void setEntityPropertiesSet(DefaultEntity comparedTo, EntityChangedMessage message) {
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
    public void complete(PathElementEntitySet containingSet) throws IncompleteEntityException {
        entityType.complete(this, containingSet);
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
        return entityProperties.get(EntityPropertyMain.ID) != null;
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

}
