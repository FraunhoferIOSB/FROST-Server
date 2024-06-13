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
package de.fraunhofer.iosb.ilt.frostserver.model;

import de.fraunhofer.iosb.ilt.frostserver.property.Property;

/**
 * Interface that values of complex properties should implement to make it
 * easier to access sub-properties.
 *
 * @param <S> The type of the complex value (for fluent API)
 */
public interface ComplexValue<S extends ComplexValue<S>> {

    /**
     * Get the value of the given property.
     *
     * @param <P> The type of the property and value.
     * @param property The property to get the value of.
     * @return the value of the requested property.
     */
    public <P> P getProperty(Property<P> property);

    /**
     * Set the given property to the given value.
     *
     * @param <P> The type of the property.
     * @param property The property to set.
     * @param value The value to set the property to.
     * @return this.
     */
    public <P> S setProperty(Property<P> property, P value);

    /**
     * Get the custom property with the given name. Only valid for ComplexTypes
     * that are classed as openType, returns null for non-openTypes.
     *
     * @param name The name of the custom property to fetch.
     * @return The value of the custom property.
     */
    public Object getProperty(String name);

    /**
     * Set the custom property with the given name to the given value. Only
     * valid for ComplexTypes that are classed as openType.
     *
     * @param name The name of the custom property to set.
     * @param value The value of the custom property to set.
     * @return this.
     */
    public S setProperty(String name, Object value);

}
