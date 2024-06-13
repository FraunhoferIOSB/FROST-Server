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

import com.fasterxml.jackson.core.type.TypeReference;
import de.fraunhofer.iosb.ilt.frostserver.util.ParserUtils;

/**
 *
 * @author hylke
 */
public class TypeSimpleSet extends PropertyType {

    private final PropertyType containtedType;

    public TypeSimpleSet(TypeSimple containedType, TypeReference typeReference) {
        super(containedType.getName(), "Collection of " + containedType.getName(), ParserUtils.getDefaultDeserializer(typeReference));
        this.containtedType = containedType;
    }

    public TypeSimpleSet(TypeComplex containedType, TypeReference typeReference) {
        super(containedType.getName(), "Collection of " + containedType.getName(), ParserUtils.getDefaultDeserializer(typeReference));
        this.containtedType = containedType;
    }

    @Override
    public boolean isCollection() {
        return true;
    }

    public PropertyType getContaintedType() {
        return containtedType;
    }

}
