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

import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.path.EntityPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntitySetPathElement;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePath;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.sta.util.NoSuchEntityException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.EventListenerList;

/**
 *
 * @author jab
 */
public abstract class AbstractPersistenceManager implements PersistenceManager {

    protected EventListenerList entityChangeListeners = new EventListenerList();
    private final List<Entity> insertedEntities;
    private final List<Entity> deletedEntities;
    private final List<EntityUpdateInfo> updatedEntities;

    protected AbstractPersistenceManager() {
        this.insertedEntities = new ArrayList<>();
        this.deletedEntities = new ArrayList<>();
        this.updatedEntities = new ArrayList<>();
    }

    @Override
    public void addEntityChangeListener(EntityChangeListener listener) {
        entityChangeListeners.add(EntityChangeListener.class, listener);
    }

    @Override
    public void removeEntityChangeListener(EntityChangeListener listener) {
        entityChangeListeners.remove(EntityChangeListener.class, listener);
    }

    protected void fireEntityInserted(Entity e) {
        Object[] listeners = entityChangeListeners.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == EntityChangeListener.class) {
                ((EntityChangeListener) listeners[i + 1]).entityInserted(this, e);
            }
        }
    }

    protected void fireEntityDeleted(Entity e) {
        Object[] listeners = entityChangeListeners.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == EntityChangeListener.class) {
                ((EntityChangeListener) listeners[i + 1]).entityDeleted(this, e);
            }
        }
    }

    protected void fireEntityUpdated(Entity oldEntity, Entity newEntity) {
        Object[] listeners = entityChangeListeners.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == EntityChangeListener.class) {
                ((EntityChangeListener) listeners[i + 1]).entityUpdated(this, oldEntity, newEntity);
            }
        }
    }

    @Override
    public boolean insert(Entity entity) throws NoSuchEntityException, IncompleteEntityException {
        boolean result = doInsert(entity);
        if (result) {
            insertedEntities.add(entity);
        }
        return result;
    }

    public abstract boolean doInsert(Entity entity) throws NoSuchEntityException, IncompleteEntityException;

    @Override
    public boolean delete(EntityPathElement pathElement) throws NoSuchEntityException {
        Entity entity = getEntityByEntityPath(pathElement);
        boolean result = doDelete(pathElement);
        if (result) {
            deletedEntities.add(entity);
        }
        return result;
    }

    private Entity getEntityByEntityPath(EntityPathElement pathElement) {
        ResourcePath path = new ResourcePath();
        path.addPathElement(new EntitySetPathElement(pathElement.getEntityType(), null), false, false);
        pathElement.setParent(path.getLastElement());
        path.addPathElement(pathElement, true, true);
        return (Entity) get(path, null);
    }

    public abstract boolean doDelete(EntityPathElement pathElement) throws NoSuchEntityException;

    @Override
    public boolean update(EntityPathElement pathElement, Entity entity) throws NoSuchEntityException {
        Entity oldEntity = getEntityByEntityPath(pathElement);
        boolean result = doUpdate(pathElement, entity);
        if (result) {
            updatedEntities.add(new EntityUpdateInfo(oldEntity, getEntityByEntityPath(pathElement)));
        }
        return result;
    }

    public abstract boolean doUpdate(EntityPathElement pathElement, Entity entity) throws NoSuchEntityException;

    private void fireEntityChangeEvents() {
        insertedEntities.forEach(e -> fireEntityInserted(e));
        deletedEntities.forEach(e -> fireEntityDeleted(e));
        updatedEntities.forEach(e -> fireEntityUpdated(e.oldEntity, e.newEntity));
        clearEntityChangedEvents();
    }

    private void clearEntityChangedEvents() {
        insertedEntities.clear();
        deletedEntities.clear();
        updatedEntities.clear();
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

    private class EntityUpdateInfo {

        Entity oldEntity;
        Entity newEntity;

        public EntityUpdateInfo(Entity oldEntity, Entity newEntity) {
            this.oldEntity = oldEntity;
            this.newEntity = newEntity;
        }
    }
}
