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
package de.fraunhofer.iosb.ilt.frostserver.model.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Implementation of EntitySet interface. Should be used whenever a collection
 * of entities is needed.
 *
 * @author jab
 */
public class EntitySetImpl implements EntitySet {

    protected final List<Entity> data;
    protected long count = -1;
    protected String nextLink;

    @JsonIgnore
    private final EntityType type;
    @JsonIgnore
    private NavigationPropertyEntitySet navigationProperty;

    public EntitySetImpl(EntityType type) {
        this.data = new ArrayList<>();
        this.type = type;
    }

    public EntitySetImpl(NavigationPropertyEntitySet navigationProperty) {
        this.data = new ArrayList<>();
        this.type = navigationProperty.getEntityType();
        this.navigationProperty = navigationProperty;
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public Iterator<Entity> iterator() {
        return data.iterator();
    }

    @Override
    public void add(Entity e) {
        data.add(e);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EntitySetImpl other = (EntitySetImpl) obj;
        return Objects.equals(this.data, other.data);
    }

    @Override
    public long getCount() {
        return count;
    }

    @Override
    public void setCount(long count) {
        this.count = count;
    }

    @Override
    public String getNextLink() {
        return nextLink;
    }

    @Override
    public void setNextLink(String nextLink) {
        this.nextLink = nextLink;
    }

    @Override
    public EntityType getEntityType() {
        return type;
    }

    @Override
    public NavigationPropertyEntitySet getNavigationProperty() {
        return navigationProperty;
    }

    public EntitySetImpl setNavigationProperty(NavigationPropertyEntitySet navigationProperty) {
        this.navigationProperty = navigationProperty;
        return this;
    }

}
