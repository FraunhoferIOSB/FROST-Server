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
package de.fraunhofer.iosb.ilt.frostserver.persistence;

import com.github.fge.jsonpatch.JsonPatch;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PkValue;
import de.fraunhofer.iosb.ilt.frostserver.model.loader.DefModel;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.service.InitResult;
import de.fraunhofer.iosb.ilt.frostserver.service.UpdateMode;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.SecurityModel;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import java.security.Principal;
import java.util.List;

/**
 * The interface for persistence managers.
 */
public interface PersistenceManager extends AutoCloseable {

    public boolean validatePath(ResourcePath path);

    /**
     * Entity pre-filled with context of URL
     *
     * @param entity The entity to insert.
     * @param updateMode The rules to follow wen inserting.
     * @return true if the entity was successfully inserted.
     *
     * @throws NoSuchEntityException If any of the required navigation links
     * point to a non-existing entity.
     * @throws IncompleteEntityException If an in-line entity is incomplete.
     */
    public boolean insert(Entity entity, UpdateMode updateMode) throws NoSuchEntityException, IncompleteEntityException;

    /**
     * Get the Entity of the given EntityType with the given Primary Key.
     *
     * @param entityType The EntityType to fetch.
     * @param pkValue The primary key of the Entity to fetch.
     * @return An entity with the given type and given primary key, or null.
     */
    public Entity get(EntityType entityType, PkValue pkValue);

    public Object get(ResourcePath path, Query query);

    public default <T> T get(ResourcePath path, Query query, Class<T> clazz) {
        Object result = get(path, query);
        if (!clazz.isAssignableFrom(result.getClass())) {
            throw new IllegalArgumentException("The resourcepath does not result in an instance of class " + clazz.getName());
        }
        return clazz.cast(result);
    }

    public boolean delete(PathElementEntity pathElement) throws NoSuchEntityException;

    /**
     * Delete all entities in the given path, matching the filter in the given
     * query.
     *
     * @param path The path to an entity set.
     * @param query The query containing only a filter.
     * @throws NoSuchEntityException If the path does not lead to an entity set.
     */
    public void delete(ResourcePath path, Query query) throws NoSuchEntityException;

    /**
     * Delete the relation specified by the given NavigationProperty, between
     * source and target.
     *
     * @param source the source entity of the relation.
     * @param np the NavigationProperty that specifies the relation.
     * @param target the target entity of the relation.
     * @throws IncompleteEntityException If the relation is required to exist
     * for either the source or target.
     * @throws NoSuchEntityException If the source or target entity does not
     * exist.
     */
    public void deleteRelation(PathElementEntity source, NavigationPropertyMain np, PathElementEntity target) throws IncompleteEntityException, NoSuchEntityException;

    /**
     * Update the given entity.
     *
     * @param pathElement The path to the entity.
     * @param entity The entity.
     * @param updateMode The rules to follow when updating.
     * @return True if the update was successful.
     * @throws NoSuchEntityException If the entity does not exist.
     * @throws IncompleteEntityException If the given entity is missing required
     * fields.
     */
    public boolean update(PathElementEntity pathElement, Entity entity, UpdateMode updateMode) throws NoSuchEntityException, IncompleteEntityException;

    /**
     * Update the given entity using the given (rfc6902) JSON Patch.
     *
     * @param pathElement The path to the entity.
     * @param patch The patch to apply to the entity.
     * @return True if the update was successful.
     * @throws NoSuchEntityException If the entity does not exist.
     * @throws IncompleteEntityException If the patch would cause the given
     * entity to lack required fields.
     */
    public boolean update(PathElementEntity pathElement, JsonPatch patch) throws NoSuchEntityException, IncompleteEntityException;

    /**
     * Get the list of messages that are waiting to be sent to the bus. When an
     * update has side effects that change entities besides the main target
     * entity, additional messages may need to be added.
     *
     * @return The list of messages.
     */
    public List<EntityChangedMessage> getEntityChangedMessages();

    /**
     * Initialise using the given settings.
     *
     * @param settings The settigns to use.
     * @return the result of the initialisation.
     */
    public InitResult init(CoreSettings settings);

    /**
     * Get the settings that were used to initialise this PM.
     *
     * @return The settings that were used to initialise this PM.
     */
    public CoreSettings getCoreSettings();

    public void setRole(Principal user);

    public void commit();

    public void rollback();

    @Override
    public void close();

    public default void rollbackAndClose() {
        rollback();
        close();
    }

    public default void commitAndClose() {
        commit();
        close();
    }

    public default void addModelMapping(DefModel modelDefinition) {
        // Optional method.
    }

    public default void addSecurityDefinition(SecurityModel.SecurityEntry entry) {
        // Optional method.
    }
}
