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
package de.fraunhofer.iosb.ilt.frostserver.model.core;

import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import java.util.Map;
import java.util.Set;

/**
 * Interface for Complex Types and Entity Types that contain sub-properties.
 *
 * @param <T> The type of the implementing class, for method chaining.
 */
public interface ContainerType<T extends ContainerType<T>> {

    /**
     * Register a new Property on this container type.
     *
     * @param property The property to register.
     * @return this.
     */
    public T registerProperty(Property property);

    /**
     * Check if this type allows user-defined properties.
     *
     * @return true if this is an open type, false otherwise.
     */
    public boolean isOpenType();

    /**
     * Get all the entity properties registered on this container type.
     *
     * @return all the properties.
     */
    public Set<EntityPropertyMain> getEntityProperties();

    /**
     * Get the entity property with the given name.
     *
     * @param name The name of the property.
     * @return The property with the given name, or null.
     */
    public EntityPropertyMain getEntityProperty(String name);

    /**
     * Get the map of all properties by their name.
     *
     * @return The name-property map.
     */
    public Map<String, Property> getPropertiesByName();

}
