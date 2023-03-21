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
package de.fraunhofer.iosb.ilt.frostserver.property.type;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 *
 * @author hylke
 */
public abstract class TypeSimple extends PropertyType {

    private Parser parser;
    private final TypeSimplePrimitive underlyingType;

    protected TypeSimple(String name, String description, TypeReference typeReference) {
        this(name, description, typeReference, null);
    }

    protected TypeSimple(String name, String description, TypeReference typeReference, Parser parser) {
        super(name, description, typeReference);
        if (this instanceof TypeSimplePrimitive) {
            this.underlyingType = (TypeSimplePrimitive) this;
        } else {
            throw new IllegalArgumentException("This constuctor can only be used by subclass TypeSimplePrimitive or TypeSimpleSet");
        }
        this.parser = parser;
    }

    protected TypeSimple(String name, String description, TypeSimplePrimitive underlyingType, TypeReference typeReference) {
        this(name, description, underlyingType, typeReference, null);
    }

    protected TypeSimple(String name, String description, TypeSimplePrimitive underlyingType, TypeReference typeReference, Parser parser) {
        super(name, description, typeReference);
        this.underlyingType = underlyingType;
        this.parser = parser;
    }

    public TypeSimplePrimitive getUnderlyingType() {
        return underlyingType;
    }

    @Override
    public Object parseFromUrl(String input) {
        if (parser != null) {
            return parser.parseFromUrl(input);
        }
        if (underlyingType != this) {
            return underlyingType.parseFromUrl(input);
        }
        return super.parseFromUrl(input);
    }

    public static interface Parser {

        public Object parseFromUrl(String input);
    }
}
