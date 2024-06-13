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

import static de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended.INTERNAL_ADMIN_PRINCIPAL;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.Version;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.query.Expand;
import de.fraunhofer.iosb.ilt.frostserver.query.Metadata;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
    private Set<EntityPropertyMain> epFields;
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

    public Set<EntityPropertyMain> getEpFields() {
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
        if (field instanceof EntityPropertyMain entityPropertyMain) {
            addEpField(entityPropertyMain);
        } else if (field instanceof NavigationPropertyMain navigationPropertyMain) {
            addNpField(navigationPropertyMain);
        } else {
            throw new IllegalArgumentException("Field is not an entity or navigation property: " + field);
        }
        return this;
    }

    public EntityChangedMessage addEpField(EntityPropertyMain field) {
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

    public static class QueryGenerator {

        private final QueryDefaults queryDefaults = new QueryDefaults(true, false, 1, 1, false);
        /**
         * The queries used when serialising entities in messages.
         */
        public final Map<EntityType, Query> messageQueries = new ConcurrentHashMap<>();

        public Query getQueryFor(EntityType entityType) {
            return messageQueries.computeIfAbsent(entityType, t -> {
                // ServiceRootUrl and version are irrelevant for these internally used messages.
                Query query = new Query(t.getModelRegistry(), queryDefaults, new ResourcePath("", Version.V_1_0, "/" + entityType.entityName), INTERNAL_ADMIN_PRINCIPAL)
                        .setMetadata(Metadata.INTERNAL_COMPARE);
                for (EntityPropertyMain ep : entityType.getEntityProperties()) {
                    query.addSelect(ep);
                }
                for (NavigationPropertyMain np : entityType.getNavigationEntities()) {
                    Query subQuery = new Query(t.getModelRegistry(), queryDefaults, new ResourcePath("", Version.V_1_0, "/" + np.getName()), INTERNAL_ADMIN_PRINCIPAL)
                            .addSelect(np.getEntityType().getPrimaryKey().getKeyProperties())
                            .setMetadata(Metadata.INTERNAL_COMPARE);
                    query.addExpand(new Expand(t.getModelRegistry(), np).setSubQuery(subQuery));
                }
                return query;
            });
        }
    }
}
