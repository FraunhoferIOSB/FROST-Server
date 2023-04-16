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
package de.fraunhofer.iosb.ilt.frostserver.query.expression;

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.query.PrincipalExtended;

/**
 * A context object for dynamic data to be used when processing (rendering) a
 * query. This allows a query to be parsed once, and subsequently be re-used
 * multiple times with different context data. Each thread has its own versions
 * of the variables.
 */
public class DynamicContext {

    /**
     * The user to render the query for.
     */
    private ThreadLocal<PrincipalExtended> user = new ThreadLocal<>();

    /**
     * The entity to render the query for. When editing, this is the entity
     * being edited.
     */
    private ThreadLocal<Entity> entity = new ThreadLocal<>();

    /**
     * Clear all data from the context.
     */
    public void clear() {
        user.remove();
        entity.remove();
    }

    /**
     * The user to render the query for.
     *
     * @return The PrincipalExtended of the user.
     */
    public PrincipalExtended getUser() {
        return user.get();
    }

    /**
     * The user to render the query for.
     *
     * @param user The PrincipalExtended of the user.
     */
    public void setUser(PrincipalExtended user) {
        this.user.set(user);
    }

    /**
     * The entity to render the query for. When editing, this is the entity
     * being edited.
     *
     * @return the entity
     */
    public Entity getEntity() {
        return entity.get();
    }

    /**
     * The entity to render the query for. When editing, this is the entity
     * being edited.
     *
     * @param entity the entity to set
     */
    public void setEntity(Entity entity) {
        this.entity.set(entity);
    }

}
