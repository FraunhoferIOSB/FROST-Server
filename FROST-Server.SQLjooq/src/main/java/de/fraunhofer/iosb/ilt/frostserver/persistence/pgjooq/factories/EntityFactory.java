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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories;

import de.fraunhofer.iosb.ilt.frostserver.messagebus.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.path.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.util.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.NoSuchEntityException;
import org.jooq.Field;
import org.jooq.Record;

/**
 *
 * @author scf
 * @param <T> The entity type this factory handles.
 * @param <J> The type of the ID fields.
 */
public interface EntityFactory<T extends Entity, J> {

    /**
     * Creates a Entity of type T, reading the Record with a Table using no
     * alias.
     *
     * @param record The tuple to create the Entity from.
     * @param query The query used to request the data.
     * @param dataSize The counter for the data size. This counts only the
     * variable-sided elements, such as Observation.result and Thing.properties.
     * @return The Entity created from the Tuple.
     */
    public T create(Record record, Query query, DataSize dataSize);

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
    public boolean insert(PostgresPersistenceManager<J> pm, T entity) throws NoSuchEntityException, IncompleteEntityException;

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
    public EntityChangedMessage update(PostgresPersistenceManager<J> pm, T entity, J entityId) throws NoSuchEntityException, IncompleteEntityException;

    /**
     * Delete the entity with the given id.
     *
     * @param pm The persistenceManager to use to access the database.
     * @param entityId The id of the entity to delete.
     * @throws NoSuchEntityException If there was no entity with the given id.
     */
    public void delete(PostgresPersistenceManager<J> pm, J entityId) throws NoSuchEntityException;

    /**
     * Get the primary key of the table of the entity this factory
     *
     * TODO: Rename.
     *
     * @return The primary key of the table of the entity this factory creates,
     * using no alias.
     */
    public Field<J> getPrimaryKey();

    /**
     * Get the EntityType of the Entities created by this factory.
     *
     * @return The EntityType of the Entities created by this factory.
     */
    public EntityType getEntityType();

}
