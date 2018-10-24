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
package de.fraunhofer.iosb.ilt.sta.query;

import de.fraunhofer.iosb.ilt.sta.path.CustomPropertyPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.NavigationProperty;
import de.fraunhofer.iosb.ilt.sta.path.PropertyPathElement;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePath;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePathElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author jab
 */
public class Expand {

    private final List<NavigationProperty> path;
    private Query subQuery;

    public Expand() {
        path = new ArrayList<>();
    }

    public Expand(NavigationProperty... paths) {
        if (paths == null || paths.length == 0) {
            throw new IllegalArgumentException("paths must be non-empty");
        }
        this.path = Arrays.asList(paths);
    }

    public Expand(Query subQuery, NavigationProperty... paths) {
        if (paths == null || paths.length == 0) {
            throw new IllegalArgumentException("paths must be non-empty");
        }
        this.subQuery = subQuery;
        this.path = Arrays.asList(paths);
    }

    public List<NavigationProperty> getPath() {
        return path;
    }

    public Query getSubQuery() {
        return subQuery;
    }

    public void setSubQuery(Query subQuery) {
        this.subQuery = subQuery;
    }

    public void validate(ResourcePath path) {
        ResourcePathElement mainElement = path.getMainElement();
        if (mainElement instanceof PropertyPathElement || mainElement instanceof CustomPropertyPathElement) {
            throw new IllegalArgumentException("No expand allowed on property paths.");
        }
        EntityType entityType = path.getMainElementType();
        if (entityType == null) {
            throw new IllegalStateException("Unkown ResourcePathElementType found.");
        }
        validate(entityType);
    }

    protected void validate(EntityType entityType) {
        EntityType currentEntityType = entityType;
        for (NavigationProperty navigationProperty : this.path) {
            if (!currentEntityType.getPropertySet().contains(navigationProperty)) {
                throw new IllegalArgumentException("Invalid expand path '" + navigationProperty.getName() + "' on entity type " + currentEntityType.entityName);
            }
            currentEntityType = navigationProperty.getType();
        }
        if (subQuery != null) {
            subQuery.validate(currentEntityType);
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(this.path);
        hash = 83 * hash + Objects.hashCode(this.subQuery);
        return hash;
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
        final Expand other = (Expand) obj;
        if (!Objects.equals(this.path, other.path)) {
            return false;
        }
        return Objects.equals(this.subQuery, other.subQuery);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean firstDone = false;
        for (NavigationProperty rpe : path) {
            if (firstDone) {
                sb.append("/");
            } else {
                firstDone = true;
            }
            sb.append(rpe.getName());
        }
        if (subQuery != null) {
            sb.append('(');
            sb.append(subQuery.toString(true));
            sb.append(')');
        }
        return sb.toString();
    }

}
