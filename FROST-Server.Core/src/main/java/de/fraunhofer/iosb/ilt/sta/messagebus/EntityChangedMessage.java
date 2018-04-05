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
package de.fraunhofer.iosb.ilt.sta.messagebus;

import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.Property;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author scf
 */
public class EntityChangedMessage {

    public static enum Type {
        CREATE,
        UPDATE,
        DELETE
    }
    /**
     * The type of event that this message describes.
     */
    public Type eventType;
    /**
     * The fields of the entity that were affected, if the type was UPDATE. For
     * Create and Delete this is always empty, since all fields are affected.
     */
    public Set<Property> fields;
    /**
     * The type of the entity that was affected.
     */
    public EntityType entityType;
    /**
     * The new version of the entity (for create/update) or the old entity (for
     * delete).
     */
    public Entity entity;

    public EntityChangedMessage() {
    }

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

    public Set<Property> getFields() {
        return fields;
    }

    public EntityChangedMessage addField(Property field) {
        if (fields == null) {
            fields = new HashSet<>();
        }
        fields.add(field);
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

}
