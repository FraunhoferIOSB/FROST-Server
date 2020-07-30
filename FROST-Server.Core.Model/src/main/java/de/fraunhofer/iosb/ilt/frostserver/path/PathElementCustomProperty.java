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

import java.util.Objects;

/**
 *
 * @author jab
 */
public class PathElementCustomProperty implements PathElement {

    private String name;
    private PathElement parent;

    public PathElementCustomProperty() {
    }

    public PathElementCustomProperty(String name, PathElement parent) {
        this.name = name;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    @Override
    public PathElement getParent() {
        return parent;
    }

    public void setName(String name) {
        this.name = name;
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
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, parent);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PathElementCustomProperty other = (PathElementCustomProperty) obj;
        return Objects.equals(this.name, other.name)
                && Objects.equals(this.parent, other.parent);
    }

}
