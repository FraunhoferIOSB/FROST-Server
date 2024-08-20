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
import java.util.List;

/**
 *
 * @author hylke
 */
public interface PrimaryKey {

    /**
     * Get the size of the key, which is the number of properties in the key.
     *
     * @return the size of the key.
     */
    public int size();

    /**
     * Get the list of properties composing the primary key.
     *
     * @return the list of properties composing the primary key.
     */
    public List<EntityPropertyMain> getKeyProperties();

    /**
     * Get the key property with the given index.
     *
     * @param idx The index of the property, must be less than size().
     * @return The requested key property.
     */
    public EntityPropertyMain getKeyProperty(int idx);
}
