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
package de.fraunhofer.iosb.ilt.frostserver.property;

/**
 *
 * @author jab
 */
public interface Property {

    /**
     * @return The name of this property as used in URLs.
     */
    public String getName();

    /**
     * @return The name of this property as used in JSON.
     */
    public String getJsonName();

    /**
     * @return The name of the getter method for this property.
     */
    public String getGetterName();

    /**
     * @return The name of the setter method for this property.
     */
    public String getSetterName();

    /**
     * @return The name of the method to check if this property is set.
     */
    public String getIsSetName();
}
