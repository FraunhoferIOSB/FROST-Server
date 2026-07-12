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

import static de.fraunhofer.iosb.ilt.frostserver.property.PropertyAbstract.Option.NULLABLE;
import static de.fraunhofer.iosb.ilt.frostserver.property.PropertyAbstract.Option.READONLY;
import static de.fraunhofer.iosb.ilt.frostserver.property.PropertyAbstract.Option.REQUIRED;

import de.fraunhofer.iosb.ilt.frostserver.property.type.PropertyType;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

/**
 * An abstract base class for Properties.
 *
 * @param <P> The type of the values of this property.
 */
public abstract class PropertyAbstract<P> implements Property<P> {

    public static enum Option {
        READONLY,
        REQUIRED,
        NULLABLE,
        CUSTOM_PROPS,
        SERIALISE_NULLS
    }

    private String name;
    private PropertyType type;
    /**
     * Flag indicating the property must be explicitly set.
     */
    private boolean required;
    /**
     * Flag indicating the property may be set to null.
     */
    private boolean nullable;
    /**
     * Flag indicating the property is system generated and can not be edited by
     * the user.
     */
    private boolean readOnly;

    protected PropertyAbstract(String name, PropertyType type, Option... options) {
        this(name, type, EnumSet.copyOf(Arrays.asList(options)));
    }

    protected PropertyAbstract(String name, PropertyType type, Set<Option> options) {
        if (type == null) {
            throw new IllegalArgumentException("Type must not be null");
        }
        this.name = name;
        this.type = type;
        setRequired(options.contains(REQUIRED));
        setReadOnly(options.contains(READONLY));
        setNullable(options.contains(NULLABLE));
    }

    @Override
    public String getName() {
        return name;
    }

    protected final void setName(String name) {
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

    protected final void setType(PropertyType type) {
        this.type = type;
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    public final void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public boolean isNullable() {
        return nullable;
    }

    public final void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    public final void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Override
    public String toString() {
        return getName();
    }

}
