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

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.property.type.PropertyType;
import java.util.Comparator;

/**
 *
 * @author jab
 * @author scf
 * @param <P> The type of the value of the property.
 */
public interface Property<P> extends Comparable<Property<?>> {

    /**
     * The name of this property as used in URLs.
     *
     * @return The name of this property as used in URLs.
     */
    public String getName();

    /**
     * The name of this property as used in JSON.
     *
     * @return The name of this property as used in JSON.
     */
    public String getJsonName();

    /**
     * The class of the type of the value of this property.
     *
     * @return The class of the type of the value of this property.
     */
    public PropertyType getType();

    /**
     * Flag indicating the property must be explicitly set.
     *
     * @return the 'required' flag
     */
    public boolean isRequired();

    /**
     * Flag indicating the property may be set to null.
     *
     * @return the nullable flag
     */
    public boolean isNullable();

    /**
     * Flag indicating the property is system generated and can not be edited by
     * the user.
     *
     * @return the readOnly flag.
     */
    public boolean isReadOnly();

    /**
     * Get the value of this property from the given entity.
     *
     * @param entity The entity to get this property from.
     * @return This property, fetched from the given entity.
     */
    public P getFrom(Entity entity);

    /**
     * Set this property to the given value, on the given entity.
     *
     * @param entity The entity to set this property on.
     * @param value The value to set the property to.
     */
    public void setOn(Entity entity, P value);

    /**
     * Check if this property is set on the given entity.
     *
     * @param entity The entity for which to check if this entity is set.
     * @return True if this property is set on the given entity.
     */
    public boolean isSetOn(Entity entity);

    /**
     * The priority used for ordering. Important when a property needs to be
     * handled before another property.
     *
     * @return the priority of this Property.
     */
    public default int getPriority() {
        return 0;
    }

    /**
     * A comparator for comparing Property Objects.
     */
    public static final Comparator<Property> COMPARATOR = Comparator
            .comparingInt((Property p) -> p.getPriority())
            .thenComparing(Property::getName);

    @Override
    public default int compareTo(Property o) {
        return COMPARATOR.compare(this, o);
    }

}
