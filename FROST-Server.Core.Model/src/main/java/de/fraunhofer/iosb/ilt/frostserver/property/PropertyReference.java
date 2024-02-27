/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
import java.util.Objects;

/**
 * A reference to a property, used in lambdas like any().
 *
 * @author scf
 * @param <P> The type of the value of the referenced property.
 */
public class PropertyReference<P> implements Property<P> {

    private final String name;
    private final Property<P> referencedProperty;

    public PropertyReference(String name, Property referencedProperty) {
        this.name = name;
        this.referencedProperty = referencedProperty;
    }

    @Override
    public String getName() {
        return name;
    }

    public Property getReferencedProperty() {
        return referencedProperty;
    }

    @Override
    public String getJsonName() {
        return getName();
    }

    @Override
    public PropertyType getType() {
        return referencedProperty.getType();
    }

    @Override
    public boolean isRequired() {
        return referencedProperty.isRequired();
    }

    @Override
    public boolean isNullable() {
        return referencedProperty.isNullable();
    }

    @Override
    public boolean isReadOnly() {
        return referencedProperty.isReadOnly();
    }

    @Override
    public P getFrom(Entity entity) {
        return referencedProperty.getFrom(entity);
    }

    @Override
    public void setOn(Entity entity, P value) {
        referencedProperty.setOn(entity, value);
    }

    @Override
    public boolean isSetOn(Entity entity) {
        return referencedProperty.isSetOn(entity);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(name);
        return 17 * hash + Objects.hashCode(referencedProperty);
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
        final PropertyReference<?> other = (PropertyReference<?>) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return Objects.equals(this.referencedProperty, other.referencedProperty);
    }

}
