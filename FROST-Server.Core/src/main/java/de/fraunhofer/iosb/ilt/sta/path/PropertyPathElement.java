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

import java.util.Objects;

/**
 *
 * @author jab
 */
public class PropertyPathElement implements ResourcePathElement {

    private EntityProperty property;
    private ResourcePathElement parent;

    public PropertyPathElement() {
    }

    public PropertyPathElement(EntityProperty property, ResourcePathElement parent) {
        this.property = property;
        this.parent = parent;
    }

    public EntityProperty getProperty() {
        return property;
    }

    @Override
    public ResourcePathElement getParent() {
        return parent;
    }

    public void setProperty(EntityProperty property) {
        this.property = property;
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
        return property.entitiyName;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 61 * hash + Objects.hashCode(this.property);
        hash = 61 * hash + Objects.hashCode(this.parent);
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
        final PropertyPathElement other = (PropertyPathElement) obj;
        if (this.property != other.property) {
            return false;
        }
        return Objects.equals(this.parent, other.parent);
    }

}
