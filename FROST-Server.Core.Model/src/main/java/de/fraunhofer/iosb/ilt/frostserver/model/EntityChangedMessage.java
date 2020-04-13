/*
 * Copyright (C) 2018 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author scf
 */
public class EntityChangedMessage {

    /**
     * The types of changes that can happen to entities.
     */
    public enum Type {
        CREATE,
        UPDATE,
        DELETE
    }
    /**
     * The type of event that this message describes.
     */
    private Type eventType;
    /**
     * The fields of the entity that were affected, if the type was UPDATE. For
     * Create and Delete this is always empty, since all fields are affected.
     */
    private Set<EntityProperty> epFields;
    private Set<NavigationPropertyMain> npFields;
    /**
     * The type of the entity that was affected.
     */
    private EntityType entityType;
    /**
     * The new version of the entity (for create/update) or the old entity (for
     * delete).
     */
    private Entity entity;

    public Type getEventType() {
        return eventType;
    }

    public EntityChangedMessage setEventType(Type eventType) {
        this.eventType = eventType;
        return this;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public EntityChangedMessage setEntityType(EntityType entityType) {
        this.entityType = entityType;
        return this;
    }

    public Set<EntityProperty> getEpFields() {
        return epFields;
    }

    public Set<NavigationPropertyMain> getNpFields() {
        return npFields;
    }

    @JsonIgnore
    public Set<Property> getFields() {
        if (epFields == null && npFields == null) {
            return Collections.emptySet();
        }
        Set<Property> fields = new HashSet<>();
        if (epFields != null) {
            fields.addAll(epFields);
        }
        if (npFields != null) {
            fields.addAll(npFields);
        }
        return fields;
    }

    public EntityChangedMessage addField(Property field) {
        if (field instanceof EntityProperty) {
            addEpField((EntityProperty) field);
        } else if (field instanceof NavigationPropertyMain) {
            addNpField((NavigationPropertyMain) field);
        } else {
            throw new IllegalArgumentException("Field is not an entity or navigation property: " + field);
        }
        return this;
    }

    public EntityChangedMessage addEpField(EntityProperty field) {
        if (epFields == null) {
            epFields = new HashSet<>();
        }
        epFields.add(field);
        return this;
    }

    public EntityChangedMessage addNpField(NavigationPropertyMain field) {
        if (npFields == null) {
            npFields = new HashSet<>();
        }
        npFields.add(field);
        return this;
    }

    public Entity getEntity() {
        return entity;
    }

    public EntityChangedMessage setEntity(Entity entity) {
        this.entity = entity;
        this.entityType = entity.getEntityType();
        return this;
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
        final EntityChangedMessage other = (EntityChangedMessage) obj;
        if (this.eventType != other.eventType) {
            return false;
        }
        if (!Objects.equals(this.epFields, other.epFields)) {
            return false;
        }
        if (this.entityType != other.entityType) {
            return false;
        }
        return Objects.equals(this.entity, other.entity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventType, epFields, entityType, entity);
    }

}
