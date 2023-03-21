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
package de.fraunhofer.iosb.ilt.frostserver.path;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import java.util.Objects;

/**
 *
 * @author jab
 */
public class PathElementEntitySet implements PathElementEntityType {

    private final NavigationPropertyEntitySet np;
    private final EntityType entityType;
    private PathElementEntity parent;

    public PathElementEntitySet(NavigationPropertyEntitySet np, PathElementEntity parent) {
        this.np = np;
        this.entityType = null;
        this.parent = parent;
    }

    public PathElementEntitySet(EntityType entityType) {
        this.np = null;
        this.entityType = entityType;
    }

    @Override
    public String getName() {
        if (np != null) {
            return np.getName();
        }
        return entityType.plural;
    }

    @Override
    public NavigationPropertyEntitySet getNavigationProperty() {
        return np;
    }

    @Override
    public EntityType getEntityType() {
        if (np != null) {
            return np.getEntityType();
        }
        return entityType;
    }

    @Override
    public PathElementEntity getParent() {
        return parent;
    }

    @Override
    public void setParent(PathElement parent) {
        if (parent instanceof PathElementEntity pathElementEntity) {
            this.parent = pathElementEntity;
        } else if (parent == null) {
            this.parent = null;
        } else {
            throw new IllegalArgumentException("The parent of a Set must be an Entity.");
        }
    }

    @Override
    public void visit(ResourcePathVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityType, np, parent);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PathElementEntitySet other = (PathElementEntitySet) obj;
        return this.entityType == other.entityType
                && this.np == other.np
                && Objects.equals(this.parent, other.parent);
    }

}
