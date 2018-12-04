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
package de.fraunhofer.iosb.ilt.sta.model.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import java.util.Collection;
import java.util.List;

/**
 * Should be implemented by all collections of entities.
 *
 * @author jab
 * @param <T> Type of the collection items. Must implement Entity
 */
public interface EntitySet<T extends Entity<T>> extends Collection<T>, NavigableElement {

    public List<T> asList();

    public long getCount();

    public void setCount(long count);

    public String getNextLink();

    public void setNextLink(String nextLink);

    @JsonIgnore
    public EntityType getEntityType();
}
