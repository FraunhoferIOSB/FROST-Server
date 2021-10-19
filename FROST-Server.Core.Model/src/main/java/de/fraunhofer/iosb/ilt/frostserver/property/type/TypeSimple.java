/*
 * Copyright (C) 2021 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.fraunhofer.iosb.ilt.frostserver.property.type;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 *
 * @author hylke
 */
public abstract class TypeSimple extends PropertyType {

    private final TypeSimplePrimitive underlyingType;

    protected TypeSimple(String name, String description, TypeReference typeReference) {
        super(name, description, typeReference);
        if (this instanceof TypeSimplePrimitive) {
            this.underlyingType = (TypeSimplePrimitive) this;
        } else {
            throw new IllegalArgumentException("This constuctor can only be used by subclass TypeSimplePrimitive");
        }
    }

    protected TypeSimple(String name, String description, TypeSimplePrimitive underlyingType) {
        super(name, description, underlyingType.getTypeReference());
        this.underlyingType = underlyingType;
    }

    public TypeSimplePrimitive getUnderlyingType() {
        return underlyingType;
    }

}
