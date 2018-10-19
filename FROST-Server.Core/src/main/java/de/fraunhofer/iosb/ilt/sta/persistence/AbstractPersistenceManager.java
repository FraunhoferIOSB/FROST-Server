/*
 * Copyright (C) 2016 Fraunhofer IOSB
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.sta.persistence;

import de.fraunhofer.iosb.ilt.sta.messagebus.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.sta.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.sta.messagebus.MessageBusFactory;
import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.model.core.Id;
import de.fraunhofer.iosb.ilt.sta.path.EntityPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntitySetPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.NavigationProperty;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePath;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.sta.util.NoSuchEntityException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jab
 */
public abstract class AbstractPersistenceManager implements PersistenceManager {

    /**
     * The changed entity messages that need to be sent to the bus.
     */
    private final List<EntityChangedMessage> changedEntities;

    protected AbstractPersistenceManager() {
        this.changedEntities = new ArrayList<>();
    }

    private Entity fetchEntity(EntityType entityType, Id id) {
        Entity entity = get(entityType, id);
        for (NavigationProperty property : entityType.getNavigationEntities()) {
            Object parentObject = entity.getProperty(property);
            if (parentObject instanceof Entity) {
                Entity parentEntity = (Entity) parentObject;
                parentEntity.setExportObject(true);
            }
        }
        return entity;
    }

    @Override
    public boolean insert(Entity entity) throws NoSuchEntityException, IncompleteEntityException {
        boolean result = doInsert(entity);
        if (result) {
            Entity newEntity = fetchEntity(
                    entity.getEntityType(),
                    entity.getId());
            changedEntities.add(
                    new EntityChangedMessage()
                            .setEventType(EntityChangedMessage.Type.CREATE)
                            .setEntity(newEntity)
            );
        }
        return result;
    }

    public abstract boolean doInsert(Entity entity) throws NoSuchEntityException, IncompleteEntityException;

    @Override
    public boolean delete(EntityPathElement pathElement) throws NoSuchEntityException {
        Entity entity = getEntityByEntityPath(pathElement);
        boolean result = doDelete(pathElement);
        if (result) {
            changedEntities.add(
                    new EntityChangedMessage()
                            .setEventType(EntityChangedMessage.Type.DELETE)
                            .setEntity(entity)
            );
        }
        return result;
    }

    @Override
    public void delete(ResourcePath path, Query query) throws NoSuchEntityException {
        doDelete(path, query);
    }

    private Entity getEntityByEntityPath(EntityPathElement pathElement) {
        ResourcePath path = new ResourcePath();
        path.addPathElement(new EntitySetPathElement(pathElement.getEntityType(), null), false, false);
        pathElement.setParent(path.getLastElement());
        path.addPathElement(pathElement, true, true);
        return (Entity) get(path, null);
    }

    public abstract boolean doDelete(EntityPathElement pathElement) throws NoSuchEntityException;

    public abstract void doDelete(ResourcePath path, Query query);

    @Override
    public boolean update(EntityPathElement pathElement, Entity entity) throws NoSuchEntityException, IncompleteEntityException {
        EntityChangedMessage result = doUpdate(pathElement, entity);
        if (result != null) {
            result.setEventType(EntityChangedMessage.Type.UPDATE);
            Entity newEntity = fetchEntity(
                    entity.getEntityType(),
                    entity.getId());
            result.setEntity(newEntity);
            changedEntities.add(result);
        }
        return result != null;
    }

    /**
     * Update the given entity and return a message with the fields that were
     * changed. The entity is added to the message by the
     * AbstractPersistenceManager.
     *
     * @param pathElement The path to the entity to update.
     * @param entity The updated entity.
     * @return A message with the fields that were changed. The entity is added
     * by the AbstractPersistenceManager.
     * @throws NoSuchEntityException If the entity does not exist.
     * @throws IncompleteEntityException If the entity does not have all the
     * required fields.
     */
    public abstract EntityChangedMessage doUpdate(EntityPathElement pathElement, Entity entity) throws NoSuchEntityException, IncompleteEntityException;

    /**
     * If there are changes to send, connect to bus and send them.
     */
    private void fireEntityChangeEvents() {
        MessageBus messageBus = MessageBusFactory.getMessageBus();
        changedEntities.forEach(message -> messageBus.sendMessage(message));
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
