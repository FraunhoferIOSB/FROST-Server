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
import de.fraunhofer.iosb.ilt.frostserver.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.service.UpdateMode;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jab
 * @author scf
 */
public abstract class AbstractPersistenceManager implements PersistenceManager {

    /**
     * The changed entity messages that need to be sent to the bus.
     */
    private final List<EntityChangedMessage> changedEntities;

    protected AbstractPersistenceManager() {
        this.changedEntities = new ArrayList<>();
    }

    @Override
    public List<EntityChangedMessage> getEntityChangedMessages() {
        return changedEntities;
    }

    private Entity fetchEntity(EntityType entityType, Id id) {
        return get(entityType, id);
    }

    @Override
    public boolean insert(Entity entity, UpdateMode updateMode) throws NoSuchEntityException, IncompleteEntityException {
        boolean result = doInsert(entity, updateMode);
        if (result) {
            Entity newEntity = fetchEntity(
                    entity.getEntityType(),
                    entity.getId());
            newEntity.setQuery(getCoreSettings().getModelRegistry().getMessageQueryGenerator().getQueryFor(entity.getEntityType()));
            changedEntities.add(
                    new EntityChangedMessage()
                            .setEventType(EntityChangedMessage.Type.CREATE)
                            .setEntity(newEntity));
        }
        return result;
    }

    public abstract boolean doInsert(Entity entity, UpdateMode updateMode) throws NoSuchEntityException, IncompleteEntityException;

    @Override
    public boolean delete(PathElementEntity pathElement) throws NoSuchEntityException {
        Entity entity = getEntityByEntityPath(pathElement);
        boolean result = doDelete(pathElement);
        if (result) {
            entity.setQuery(getCoreSettings().getModelRegistry().getMessageQueryGenerator().getQueryFor(entity.getEntityType()));
            changedEntities.add(
                    new EntityChangedMessage()
                            .setEventType(EntityChangedMessage.Type.DELETE)
                            .setEntity(entity));
        }
        return result;
    }

    @Override
    public void delete(ResourcePath path, Query query) throws NoSuchEntityException {
        doDelete(path, query);
    }

    private Entity getEntityByEntityPath(PathElementEntity pathElement) {
        ResourcePath path = new ResourcePath();
        path.addPathElement(new PathElementEntitySet(pathElement.getEntityType()), false, false);
        pathElement.setParent(path.getLastElement());
        path.addPathElement(pathElement, true, true);
        return (Entity) get(path, null);
    }

    public abstract boolean doDelete(PathElementEntity pathElement) throws NoSuchEntityException;

    public abstract void doDelete(ResourcePath path, Query query);

    @Override
    public boolean update(PathElementEntity pathElement, Entity entity, UpdateMode updateMode) throws NoSuchEntityException, IncompleteEntityException {
        EntityChangedMessage result = doUpdate(pathElement, entity, updateMode);
        if (result != null) {
            result.setEventType(EntityChangedMessage.Type.UPDATE);
            final EntityType entityType = entity.getEntityType();
            Entity newEntity = fetchEntity(entityType, entity.getId());
            newEntity.setQuery(getCoreSettings().getModelRegistry().getMessageQueryGenerator().getQueryFor(entityType));
            result.setEntity(newEntity);
            changedEntities.add(result);
        }
        return result != null;
    }

    /**
     * Update the given entity and return a message with the fields that were
     * changed.The entity is added to the message by the
     * AbstractPersistenceManager.
     *
     * @param pathElement The path to the entity to update.
     * @param entity The updated entity.
     * @param updateMode the rules to follow regarding navigation links.
     * @return A message with the fields that were changed. The entity is added
     * by the AbstractPersistenceManager.
     * @throws NoSuchEntityException If the entity does not exist.
     * @throws IncompleteEntityException If the entity does not have all the
     * required fields.
     */
    public abstract EntityChangedMessage doUpdate(PathElementEntity pathElement, Entity entity, UpdateMode updateMode) throws NoSuchEntityException, IncompleteEntityException;

    @Override
    public boolean update(PathElementEntity pathElement, JsonPatch patch) throws NoSuchEntityException, IncompleteEntityException {
        EntityChangedMessage result = doUpdate(pathElement, patch);
        if (result != null) {
            if (result.getFields().isEmpty()) {
                // Successful update, but no changes.
                return true;
            }
            result.setEventType(EntityChangedMessage.Type.UPDATE);

            Entity entity = result.getEntity();
            final EntityType entityType = entity.getEntityType();
            Entity newEntity = fetchEntity(entityType, entity.getId());
            newEntity.setQuery(getCoreSettings().getModelRegistry().getMessageQueryGenerator().getQueryFor(entityType));
            result.setEntity(newEntity);

            changedEntities.add(result);
        }
        return result != null;
    }

    /**
     * Update the given entity and return a message with the entity and fields
     * that were changed.
     *
     * @param pathElement The path to the entity to update.
     * @param patch The patch to apply to the entity.
     * @return A message with the entity and the fields that were changed.
     * @throws NoSuchEntityException If the entity does not exist.
     * @throws IncompleteEntityException If the entity does not have all the
     * required fields.
     */
    public abstract EntityChangedMessage doUpdate(PathElementEntity pathElement, JsonPatch patch) throws NoSuchEntityException, IncompleteEntityException;

    /**
     * If there are changes to send, connect to bus and send them.
     */
    private void fireEntityChangeEvents() {
        MessageBus messageBus = getCoreSettings().getMessageBus();
        changedEntities.forEach(messageBus::sendMessage);
        clearEntityChangedEvents();
    }

    private void clearEntityChangedEvents() {
        changedEntities.clear();
    }

    @Override
    public void commit() {
        if (doCommit()) {
            fireEntityChangeEvents();
        }
    }

    protected abstract boolean doCommit();

    @Override
    public void rollback() {
        if (doRollback()) {
            clearEntityChangedEvents();
        }
    }

    protected abstract boolean doRollback();

    @Override
    public void close() {
        if (doClose()) {
            clearEntityChangedEvents();
        }
    }

    protected abstract boolean doClose();
}
