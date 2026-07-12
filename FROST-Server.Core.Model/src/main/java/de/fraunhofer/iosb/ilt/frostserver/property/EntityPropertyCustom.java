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
package de.fraunhofer.iosb.ilt.frostserver.property;

import static de.fraunhofer.iosb.ilt.frostserver.property.PropertyAbstract.Option.NULLABLE;

import de.fraunhofer.iosb.ilt.frostserver.model.ComplexValue;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive;
import java.util.Objects;

/**
 * A custom (user-defined) entity property.
 */
public class EntityPropertyCustom extends PropertyAbstract<Object> implements EntityProperty<Object> {

    private static final String NOT_SUPPORTED = "Not supported on custom properties.";

    public EntityPropertyCustom(String name) {
        super(name, TypeSimplePrimitive.EDM_UNTYPED, NULLABLE);
        String finalName = name;
        Integer realIndex;
        if (finalName.startsWith("[") && finalName.endsWith("]")) {
            try {
                realIndex = Integer.valueOf(finalName.substring(1, finalName.length() - 1));
                finalName = realIndex.toString();
            } catch (NumberFormatException e) {
                // Not a number...
            }
            setName(finalName);
        }
    }

    @Override
    public Object getFrom(ComplexValue<?> entity) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public void setOn(ComplexValue<?> entity, Object value) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public boolean isSetOn(ComplexValue<?> entity) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
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
        final EntityPropertyCustom other = (EntityPropertyCustom) obj;
        return Objects.equals(this.getName(), other.getName());
    }

}
