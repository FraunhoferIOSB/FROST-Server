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
package de.fraunhofer.iosb.ilt.sta.path;

import java.util.Objects;

/**
 *
 * @author Hylke van der Schaaf
 */
public class CustomProperty implements Property {

    /**
     * The name of this property as used in URLs.
     */
    public final String name;
    public final Integer index;

    public CustomProperty(String name) {
        Integer realIndex = null;
        if (name.startsWith("[") && name.endsWith("]")) {
            try {
                realIndex = Integer.parseInt(name.substring(1, name.length() - 1));
                name = realIndex.toString();
            } catch (NumberFormatException e) {
                // Not a number...
            }
        }
        this.name = name;
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

    public Integer getIndex() {
        return index;
    }

    @Override
    public String getGetterName() {
        throw new UnsupportedOperationException("Not supported on custom properties.");
    }

    @Override
    public String getSetterName() {
        throw new UnsupportedOperationException("Not supported on custom properties.");
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
        int hash = 5;
        hash = 17 * hash + Objects.hashCode(this.name);
        hash = 17 * hash + Objects.hashCode(this.index);
        return hash;
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
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return Objects.equals(this.index, other.index);
    }

}
