/*
 * Copyright (C) 2016 Fraunhofer IOSB.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.frostserver.property;

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import java.util.Objects;

/**
 *
 * @author Hylke van der Schaaf
 */
public class CustomProperty implements Property {

    private static final String UNSUPPORTED = "Not supported on custom properties.";
    /**
     * The name of this property as used in URLs.
     */
    public final String name;
    public final Integer index;

    public CustomProperty(String name) {
        String finalName = name;
        Integer realIndex = null;
        if (finalName.startsWith("[") && finalName.endsWith("]")) {
            try {
                realIndex = Integer.parseInt(finalName.substring(1, finalName.length() - 1));
                finalName = realIndex.toString();
            } catch (NumberFormatException e) {
                // Not a number...
            }
        }
        this.name = finalName;
        this.index = realIndex;
    }

    public CustomProperty(Integer index) {
        this.name = null;
        this.index = index;
    }

    public boolean isArrayIndex() {
        return index != null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getJsonName() {
        return name;
    }

    public Integer getIndex() {
        return index;
    }

    @Override
    public Object getFrom(Entity entity) {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public void setOn(Entity entity, Object value) {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public boolean isSetOn(Entity entity) {
        throw new UnsupportedOperationException(UNSUPPORTED);
    }

    @Override
    public String toString() {
        if (isArrayIndex()) {
            return "[" + getIndex() + "]";
        } else {
            return getName();
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, index);
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
        final CustomProperty other = (CustomProperty) obj;
        return Objects.equals(this.name, other.name)
                && Objects.equals(this.index, other.index);
    }

}
