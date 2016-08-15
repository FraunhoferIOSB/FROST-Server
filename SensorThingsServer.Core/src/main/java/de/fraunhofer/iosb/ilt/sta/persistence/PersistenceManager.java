/*
 * Copyright (C) 2016 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.sta.persistence;

import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.model.id.Id;
import de.fraunhofer.iosb.ilt.sta.path.EntityPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntitySetPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePath;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.sta.util.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.sta.util.NoSuchEntityException;

/**
 *
 * @author jab
 */
public interface PersistenceManager {

    public boolean validatePath(ResourcePath path);

    /**
     * Entity pre-filled with context of URL
     *
     * @param entity
     * @return true if the entity was successfully inserted.
     *
     * @throws NoSuchEntityException If any of the required navigation links
     * point to a non-existing entity.
     * @throws IncompleteEntityException If an in-line entity is incomplete.
     */
    public boolean insert(Entity entity) throws NoSuchEntityException, IncompleteEntityException;

    public Object get(ResourcePath path, Query query);

    public default Entity getEntityById(String serviceRootUrl, EntityType entityType, Id id) {
        ResourcePath path = new ResourcePath();
        path.addPathElement(new EntitySetPathElement(entityType, null), false, false);
        path.addPathElement(new EntityPathElement(id, entityType, path.getLastElement()), true, true);
        path.setServiceRootUrl(serviceRootUrl);
        return (Entity) get(path, null);
    }

    public default <T> T get(ResourcePath path, Query query, Class<T> clazz) {
        Object result = get(path, query);
        if (!clazz.isAssignableFrom(result.getClass())) {
            throw new IllegalArgumentException("The resourcepath does not result in an instance of class " + clazz.getName());
        }
        return clazz.cast(result);
    }

    public boolean delete(EntityPathElement pathElement) throws NoSuchEntityException;

    public boolean update(EntityPathElement pathElement, Entity entity) throws NoSuchEntityException;

    public void addEntityChangeListener(EntityChangeListener listener);

    public void removeEntityChangeListener(EntityChangeListener listener);

    public void init(CoreSettings settings);

    public void commit();

    public void rollback();

    public void close();

    public default void rollbackAndClose() {
        rollback();
        close();
    }

    public default void commitAndClose() {
        commit();
        close();
    }
}
