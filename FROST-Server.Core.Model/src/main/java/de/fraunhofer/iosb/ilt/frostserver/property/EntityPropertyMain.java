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

import static de.fraunhofer.iosb.ilt.frostserver.property.PropertyAbstract.Option.CUSTOM_PROPS;
import static de.fraunhofer.iosb.ilt.frostserver.property.PropertyAbstract.Option.NULLABLE;
import static de.fraunhofer.iosb.ilt.frostserver.property.PropertyAbstract.Option.SERIALISE_NULLS;

import de.fraunhofer.iosb.ilt.frostserver.model.ComplexValue;
import de.fraunhofer.iosb.ilt.frostserver.model.core.annotations.Annotatable;
import de.fraunhofer.iosb.ilt.frostserver.model.core.annotations.Annotation;
import de.fraunhofer.iosb.ilt.frostserver.property.type.PropertyType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A main data Property of an Entity or ComplexValue.
 *
 * @param <P> The type of the value of the property.
 */
public class EntityPropertyMain<P> extends PropertyAbstract<P> implements Annotatable, EntityProperty<P> {

    /**
     * Flag indicating the property has sub-properties and can be queried, even
     * though it is not a complex type.
     */
    public final boolean hasCustomProperties;

    /**
     * Flag indicating a null value should not be ignored, but serialised as
     * Json NULL.
     */
    public final boolean serialiseNull;

    private final Collection<String> aliases = new ArrayList<>();

    /**
     * The (OData)annotations for this Entity Property.
     */
    private final List<Annotation> annotations = new ArrayList<>();

    public EntityPropertyMain(String name, PropertyType type) {
        this(name, type, NULLABLE);
    }

    public EntityPropertyMain(String name, PropertyType type, Option... options) {
        this(name, type, new HashSet<>(Arrays.asList(options)));
    }

    public EntityPropertyMain(String name, PropertyType type, Set<Option> options) {
        super(name, type, options);
        aliases.add(name);
        this.hasCustomProperties = options.contains(CUSTOM_PROPS);
        this.serialiseNull = options.contains(SERIALISE_NULLS);
    }

    public Collection<String> getAliases() {
        return aliases;
    }

    public EntityPropertyMain<P> setAliases(String... aliases) {
        if (this.aliases.size() != 1) {
            throw new IllegalStateException("Aliases already set for " + getName());
        }
        this.aliases.addAll(Arrays.asList(aliases));
        return this;
    }

    public boolean hasCustomProperties() {
        return hasCustomProperties;
    }

    public boolean isSerialiseNull() {
        return serialiseNull;
    }

    @Override
    public <C extends ComplexValue<C>> P getFrom(ComplexValue<C> entity) {
        return entity.getProperty(this);
    }

    @Override
    public <C extends ComplexValue<C>> boolean isSetOn(ComplexValue<C> entity) {
        return entity.isSetProperty(this);
    }

    @Override
    public <C extends ComplexValue<C>> void setOn(ComplexValue<C> entity, P value) {
        entity.setProperty(this, value);
    }

    @Override
    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public EntityPropertyMain<P> addAnnotation(Annotation annotation) {
        annotations.add(annotation);
        return this;
    }

    public EntityPropertyMain<P> addAnnotations(List<Annotation> annotationsToAdd) {
        annotations.addAll(annotationsToAdd);
        return this;
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
        final EntityPropertyMain<?> other = (EntityPropertyMain<?>) obj;
        return Objects.equals(getName(), other.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getName());
    }

}
