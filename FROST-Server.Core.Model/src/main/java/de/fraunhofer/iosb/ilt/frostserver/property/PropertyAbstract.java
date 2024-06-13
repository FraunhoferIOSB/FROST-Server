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

import de.fraunhofer.iosb.ilt.frostserver.property.type.PropertyType;

public abstract class PropertyAbstract<P> implements Property<P> {

    private String name;
    private PropertyType type;
    /**
     * Flag indicating the property must be explicitly set.
     */
    protected boolean required;
    /**
     * Flag indicating the property may be set to null.
     */
    protected boolean nullable;
    /**
     * Flag indicating the property is system generated and can not be edited by
     * the user.
     */
    protected boolean readOnly;

    protected PropertyAbstract(String name, PropertyType type, boolean required, boolean nullable, boolean readOnly) {
        if (type == null) {
            throw new IllegalArgumentException("Type must not be null");
        }
        this.name = name;
        this.type = type;
        this.required = required;
        this.nullable = nullable;
        this.readOnly = readOnly;
    }

    @Override
    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    @Override
    public String getJsonName() {
        return getName();
    }

    @Override
    public PropertyType getType() {
        return type;
    }

    protected void setType(PropertyType type) {
        this.type = type;
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    @Override
    public boolean isNullable() {
        return nullable;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public String toString() {
        return getName();
    }

}
