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
package de.fraunhofer.iosb.ilt.frostserver.query.expression;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.path.ParserHelper;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.query.PropertyPlaceholder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author jab
 */
public class Path implements Variable {

    private final PropertyPlaceholder rawElements;
    private final List<Property> elements;

    public Path() {
        this.rawElements = null;
        this.elements = new ArrayList<>();
    }

    public Path(PropertyPlaceholder rawElements) {
        this.rawElements = rawElements;
        this.elements = new ArrayList<>();
    }

    public Path(Property... elements) {
        this.rawElements = null;
        this.elements = Arrays.asList(elements);
    }

    public Path(List<Property> elements) {
        this.rawElements = null;
        this.elements = elements;
    }

    /**
     * Validate the raw elements in this Path against the given EntityType,
     * turning it into a usable Path.
     *
     * @param type The starting point of this Path, or null to validate against
     * the service Root.
     */
    @Override
    public void validate(ParserHelper helper, EntityType type) {
        if (!elements.isEmpty()) {
            throw new IllegalStateException("Double Validation of Path!");
        }
        EntityType localType = type;
        String topName = rawElements.getName();
        Property property = localType.getProperty(topName);
        if (property == null) {
            throw new IllegalArgumentException("Unknown Property: " + topName);
        }
        elements.add(property);
        if (property instanceof NavigationPropertyMain) {
            localType = ((NavigationPropertyMain) property).getEntityType();
        }
        for (String rawElement : rawElements.getSubPath()) {
            property = helper.parseProperty(localType, rawElement, property);
            if (property instanceof NavigationProperty) {
                localType = ((NavigationProperty) property).getEntityType();
            }
            if (property == null) {
                throw new IllegalArgumentException("Unknown Property: " + rawElement);
            }
            elements.add(property);
        }
    }

    public List<Property> getElements() {
        if (elements.isEmpty() && rawElements != null) {
            throw new IllegalStateException("Path with raw elements must be validated before use.");
        }
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
        if (elements.isEmpty() && rawElements != null) {
            throw new IllegalStateException("Path with raw elements must be validated before use.");
        }
        return toString();
    }

    @Override
    public <O> O accept(ExpressionVisitor<O> visitor) {
        return visitor.visit(this);
    }

}
