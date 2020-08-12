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
package de.fraunhofer.iosb.ilt.frostserver.path;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import java.util.Objects;

/**
 *
 * @author jab
 */
public class PathElementEntitySet implements PathElement {

    private EntityType entityType;
    private PathElement parent;

    public PathElementEntitySet() {
    }

    public PathElementEntitySet(EntityType entityType, PathElement parent) {
        this.entityType = entityType;
        this.parent = parent;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    @Override
    public PathElement getParent() {
        return parent;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    @Override
    public void setParent(PathElement parent) {
        this.parent = parent;
    }

    @Override
    public void visit(ResourcePathVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return entityType.plural;
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityType, parent);
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
                && Objects.equals(this.parent, other.parent);
    }

}
