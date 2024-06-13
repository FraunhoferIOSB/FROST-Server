/*
 * Copyright (C) 2024 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
public class PathElementArrayIndex implements PathElement {

    private int index;
    private PathElement parent;

    public PathElementArrayIndex() {
    }

    public PathElementArrayIndex(int index, PathElement parent) {
        this.index = index;
        this.parent = parent;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public PathElement getParent() {
        return parent;
    }

    public void setIndex(int index) {
        this.index = index;
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
        return "[" + index + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, parent);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PathElementArrayIndex other = (PathElementArrayIndex) obj;
        return Objects.equals(this.index, other.index)
                && Objects.equals(this.parent, other.parent);
    }

}
