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

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;

/**
 *
 * @author jab
 * @author scf
 * @param <P> The type of the value of the property.
 */
public interface NavigationProperty<P> extends Property<P> {

    /**
     * The entity type this property links to.
     *
     * @return The entity type this property links to.
     */
    public EntityType getEntityType();

    /**
     * flag indicating this navigation property is a Set.
     *
     * @return true if this navigation property is a Set.
     */
    public boolean isEntitySet();

    /**
     * Check if the given entityType has this Property.
     *
     * @param entityType The EntityType to check.
     * @return true if the given entityType has this Property.
     */
    public boolean validFor(EntityType entityType);

    /**
     * Get the navigationLink for this property, for the given parent.
     *
     * @param parent The entity to get the link for.
     * @return The navigationLink for this property, for the given parent.
     */
    public String getNavigationLink(Entity parent);
}
