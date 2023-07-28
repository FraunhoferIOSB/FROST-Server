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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;

/**
 *
 * @author scf
 */
public interface EntityFactory {

    /**
     * Insert the given entity into the database as a new entity.
     *
     * @param pm The persistenceManager to use to access the database.
     * @param entity The entity to insert into the database.
     * @return True if the entity was inserted successfully.
     * @throws NoSuchEntityException If the entity depends on another entity
     * that does not exist.
     * @throws IncompleteEntityException If the entity is not complete and can
     * thus not be inserted.
     */
    public boolean insert(PostgresPersistenceManager pm, Entity entity) throws NoSuchEntityException, IncompleteEntityException;

    /**
     * Update the given entity in the database.
     *
     * @param pm The persistenceManager to use to access the database.
     * @param entity The updated entity.
     * @param entityId The id of the entity to update.
     * @return The message with the details about what was updated.
     * @throws NoSuchEntityException If the update can not happen because a
     * related entity is missing.
     * @throws IncompleteEntityException If the update can not happen because
     * required properties are missing.
     */
    public EntityChangedMessage update(PostgresPersistenceManager pm, Entity entity, Object entityId) throws NoSuchEntityException, IncompleteEntityException;

    /**
     * Delete the entity with the given id.
     *
     * @param pm The persistenceManager to use to access the database.
     * @param entityId The id of the entity to delete.
     * @throws NoSuchEntityException If there was no entity with the given id.
     */
    public void delete(PostgresPersistenceManager pm, Object entityId) throws NoSuchEntityException;

}
