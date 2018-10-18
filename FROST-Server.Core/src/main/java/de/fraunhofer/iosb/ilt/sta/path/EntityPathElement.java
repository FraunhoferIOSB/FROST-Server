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
package de.fraunhofer.iosb.ilt.sta.path;

import de.fraunhofer.iosb.ilt.sta.model.core.Id;
import java.util.Objects;

/**
 *
 * @author jab
 */
public class EntityPathElement implements ResourcePathElement {

    private Id id;
    private EntityType entityType;
    private ResourcePathElement parent;

    public EntityPathElement() {
    }

    public EntityPathElement(Id id, EntityType entityType, ResourcePathElement parent) {
        this.id = id;
        this.entityType = entityType;
        this.parent = parent;
    }

    public Id getId() {
        return id;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    @Override
    public ResourcePathElement getParent() {
        return parent;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public void setParent(ResourcePathElement parent) {
        this.parent = parent;
    }

    @Override
    public void visit(ResourcePathVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return entityType.entityName;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + Objects.hashCode(this.id);
        hash = 11 * hash + Objects.hashCode(this.entityType);
        hash = 11 * hash + Objects.hashCode(this.parent);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EntityPathElement other = (EntityPathElement) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (this.entityType != other.entityType) {
            return false;
        }
        if (!Objects.equals(this.parent, other.parent)) {
            return false;
        }
        return true;
    }

}
