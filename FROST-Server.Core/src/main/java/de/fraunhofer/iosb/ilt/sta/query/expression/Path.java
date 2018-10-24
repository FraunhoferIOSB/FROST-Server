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
package de.fraunhofer.iosb.ilt.sta.query.expression;

import de.fraunhofer.iosb.ilt.sta.path.Property;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author jab
 */
public class Path implements Variable {

    private final List<Property> elements;

    public Path() {
        this.elements = new ArrayList<>();
    }

    public Path(Property... elements) {
        this.elements = Arrays.asList(elements);
    }

    public Path(List<Property> elements) {
        this.elements = elements;
    }

    public List<Property> getElements() {
        return elements;
    }

    @Override
    public int hashCode() {
        return Objects.hash(elements);
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
        final Path other = (Path) obj;
        return Objects.equals(this.elements, other.elements);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean firstDone = false;
        for (Property p : elements) {
            if (firstDone) {
                sb.append("/");
            } else {
                firstDone = true;
            }
            sb.append(p.getName());
        }
        return sb.toString();
    }

    @Override
    public String toUrl() {
        return toString();
    }

    @Override
    public <O> O accept(ExpressionVisitor<O> visitor) {
        return visitor.visit(this);
    }

}
