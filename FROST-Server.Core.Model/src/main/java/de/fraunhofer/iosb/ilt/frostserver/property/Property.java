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
package de.fraunhofer.iosb.ilt.frostserver.property;

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;

/**
 *
 * @author jab, scf
 */
public interface Property {

    /**
     * @return The name of this property as used in URLs.
     */
    public String getName();

    /**
     * @return The name of this property as used in JSON.
     */
    public String getJsonName();

    /**
     * @param entity The entity to get this property from.
     * @return This property, fetched from the given entity.
     */
    public Object getFrom(Entity<?> entity);

    /**
     * Set this property to the given value, on the given entity.
     *
     * @param entity The entity to set this property on.
     * @param value The value to set the property to.
     */
    public void setOn(Entity<?> entity, Object value);

    /**
     * Check if this property is set on the given entity.
     *
     * @param entity The entity for which to check if this entity is set.
     * @return True if this property is set on the given entity.
     */
    public boolean isSetOn(Entity<?> entity);

}
