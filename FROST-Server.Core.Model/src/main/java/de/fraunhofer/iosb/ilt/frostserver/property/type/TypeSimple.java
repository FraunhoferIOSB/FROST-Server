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
package de.fraunhofer.iosb.ilt.frostserver.property.type;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ValueDeserializer;

/**
 * A Simple type extends a single primitive type.
 */
public abstract class TypeSimple extends PropertyType {

    private Parser parser;
    private final TypeSimplePrimitive underlyingType;

    protected TypeSimple(String name, String description, TypeReference typeReference) {
        this(name, description, typeReference, null);
    }

    protected TypeSimple(String name, String description, TypeReference tr, Parser parser) {
        this(name, description, ParserUtils.getDefaultDeserializer(tr), parser);
    }

    protected TypeSimple(String name, String description, ValueDeserializer deserializer, Parser parser) {
        super(name, description, deserializer, ParserUtils.getDefaultSerializer());
        if (this instanceof TypeSimplePrimitive typeSimplePrimitive) {
            this.underlyingType = typeSimplePrimitive;
        } else {
            throw new IllegalArgumentException("This constuctor can only be used by subclass TypeSimplePrimitive or TypeSimpleSet");
        }
        this.parser = parser;
    }

    protected TypeSimple(String name, String description, TypeSimplePrimitive ut) {
        super(name, description, ut.getDeserializer(), ut.getSerializer());
        this.underlyingType = ut;
        this.parser = ut.getParser();
    }

    protected TypeSimple(String name, String description, TypeSimplePrimitive underlyingType, TypeReference tr, Parser parser) {
        super(name, description, ParserUtils.getDefaultDeserializer(tr), ParserUtils.getDefaultSerializer());
        this.underlyingType = underlyingType;
        this.parser = parser;
    }

    /**
     * Get the direct underlying type, that may itself have an underlying type.
     *
     * @return the direct underlying type.
     */
    public TypeSimplePrimitive getUnderlyingType() {
        return underlyingType;
    }

    /**
     * Get the ultimate, deepest underlying type.
     *
     * @return the ultimate, deepest underlying type.
     */
    public TypeSimplePrimitive getUltimateUnderlyingType() {
        TypeSimplePrimitive deepest = null;
        TypeSimplePrimitive next = underlyingType;
        while (next != null && next != deepest) {
            deepest = next;
            next = deepest.getUnderlyingType();
        }
        return deepest;
    }

    protected Parser getParser() {
        return parser;
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
