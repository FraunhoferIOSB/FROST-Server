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
package de.fraunhofer.iosb.ilt.frostserver.query.expression;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.path.ParserContext;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.query.PropertyPlaceholder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A path is a variable that points to an entity property.
 */
public class Path implements Variable<Path> {

    public static final String EXPR_NAME_PATH = "path";

    private ExpressionHandler handler;

    private final PropertyPlaceholder rawElements;
    private final List<Property> elements;

    public Path() {
        this.rawElements = null;
        this.elements = new ArrayList<>();
    }

    public Path(String... elements) {
        this(new PropertyPlaceholder(elements));
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

    @Override
    public String getName() {
        return EXPR_NAME_PATH;
    }

    @Override
    public boolean hasHandler() {
        return handler != null;
    }

    @Override
    public ExpressionHandler getHandler() {
        return handler;
    }

    @Override
    public <R> Path setHandler(ExpressionHandler<Path, ExpressionHelper<R>, R> handler) {
        this.handler = handler;
        return getSelf();
    }

    @Override
    public <R> R handle(ExpressionHelper<R> h) {
        return (R) handler.handle(this, h);
    }

    @Override
    public void validate(ParserContext context, EntityType type) {
        if (!elements.isEmpty()) {
            throw new IllegalStateException("Double Validation of Path!");
        }
        EntityType localType = type;
        String topName = rawElements.getName();
        Property property = context.parseProperty(localType, topName, null);
        if (property == null) {
            throw new IllegalArgumentException("Unknown Property: " + topName);
        }
        elements.add(property);
        if (property instanceof NavigationPropertyMain npm) {
            localType = npm.getEntityType();
        }
        for (String rawElement : rawElements.getSubPath()) {
            property = context.parseProperty(localType, rawElement, property);
            if (property instanceof NavigationProperty navigationProperty) {
                localType = navigationProperty.getEntityType();
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

    public Property lastElement() {
        if (elements.isEmpty() && rawElements != null) {
            throw new IllegalStateException("Path with raw elements must be validated before use.");
        }
        return elements.get(elements.size() - 1);
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
    public Path newInstance() {
        return new Path()
                .setHandler(handler);
    }

    public Path newInstance(PropertyPlaceholder rawElements) {
        return new Path(rawElements).setHandler(handler);
    }

    @Override
    public Path getSelf() {
        return this;
    }

}
