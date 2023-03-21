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
package de.fraunhofer.iosb.ilt.frostserver.model.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;

/**
 * Should be implemented by all collections of entities.
 *
 * @author jab
 */
public interface EntitySet extends NavigableElement, Iterable<Entity> {

    public void add(Entity entity);

    public long getCount();

    public void setCount(long count);

    public int size();

    public String getNextLink();

    public void setNextLink(String nextLink);

    @JsonIgnore
    public EntityType getEntityType();

    /**
     * Get the navigationProperty that manages this EntitySet. Can be null, for
     * top-level entity sets.
     *
     * @return the navigationProperty that manages this EntitySet.
     */
    @JsonIgnore
    public NavigationPropertyEntitySet getNavigationProperty();
}
