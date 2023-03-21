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
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntity;
import java.util.Objects;

/**
 *
 * @author jab
 */
public class PathElementEntity implements PathElementEntityType {

    private final NavigationPropertyEntity np;
    private final EntityType entityType;
    private PathElement parent;
    private Id id;

    public PathElementEntity(NavigationPropertyEntity np, PathElement parent) {
        this.np = np;
        this.entityType = null;
        this.parent = parent;
    }

    public PathElementEntity(EntityType entityType, PathElement parent) {
        this.np = null;
        this.entityType = entityType;
        this.parent = parent;
    }

    public PathElementEntity(Id id, EntityType entityType, PathElement parent) {
        this.np = null;
        this.entityType = entityType;
        this.parent = parent;
        this.id = id;
    }

    public Id getId() {
        return id;
    }

    @Override
    public String getName() {
        if (np != null) {
            return np.getName();
        }
        return entityType.entityName;
    }

    @Override
    public NavigationPropertyEntity getNavigationProperty() {
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
    public PathElement getParent() {
        return parent;
    }

    public PathElementEntity setId(Id id) {
        this.id = id;
        return this;
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
        return getName();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, entityType, np, parent);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PathElementEntity other = (PathElementEntity) obj;
        return Objects.equals(this.id, other.id)
                && this.np == other.np
                && this.entityType == other.entityType
                && Objects.equals(this.parent, other.parent);
    }

}
