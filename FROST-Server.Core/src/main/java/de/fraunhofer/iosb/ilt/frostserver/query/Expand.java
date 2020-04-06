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
package de.fraunhofer.iosb.ilt.frostserver.query;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElement;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementCustomProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import java.util.Objects;

/**
 *
 * @author jab
 */
public class Expand {

    private NavigationProperty path;
    private Query subQuery;

    public Expand() {
    }

    public Expand(NavigationProperty path) {
        if (path == null) {
            throw new IllegalArgumentException("path must be non-empty");
        }
        this.path = path;
    }

    public Expand(Query subQuery, NavigationProperty path) {
        if (path == null) {
            throw new IllegalArgumentException("paths must be non-empty");
        }
        this.subQuery = subQuery;
        this.path = path;
    }

    public NavigationProperty getPath() {
        return path;
    }

    public void setPath(NavigationProperty path) {
        this.path = path;
    }

    public Query getSubQuery() {
        return subQuery;
    }

    public void setSubQuery(Query subQuery) {
        this.subQuery = subQuery;
    }

    public void validate(ResourcePath path) {
        PathElement mainElement = path.getMainElement();
        if (mainElement instanceof PathElementProperty || mainElement instanceof PathElementCustomProperty) {
            throw new IllegalArgumentException("No expand allowed on property paths.");
        }
        EntityType entityType = path.getMainElementType();
        if (entityType == null) {
            throw new IllegalStateException("Unkown ResourcePathElementType found.");
        }
        validate(entityType);
    }

    protected void validate(EntityType entityType) {
        if (!path.validFor(entityType)) {
            throw new IllegalArgumentException("Invalid expand path '" + path.getName() + "' on entity type " + entityType.entityName);
        }
        // TODO refactor.
        if (subQuery != null && path instanceof NavigationPropertyMain) {
            subQuery.validate(path.getType());
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, subQuery);
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
        return Objects.equals(this.path, other.path)
                && Objects.equals(this.subQuery, other.subQuery);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(path.getName());
        if (subQuery != null) {
            sb.append('(');
            sb.append(subQuery.toString(true));
            sb.append(')');
        }
        return sb.toString();
    }

}
