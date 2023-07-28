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
import de.fraunhofer.iosb.ilt.frostserver.model.core.annotations.Annotatable;
import de.fraunhofer.iosb.ilt.frostserver.model.core.annotations.Annotation;
import de.fraunhofer.iosb.ilt.frostserver.property.type.PropertyType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author jab
 * @author scf
 * @param <P> The type of the value of the property.
 */
public class EntityPropertyMain<P> extends PropertyAbstract<P> implements Annotatable, EntityProperty<P> {

    /**
     * Flag indicating the property has sub-properties.
     */
    public final boolean hasCustomProperties;

    /**
     * Flag indicating a null value should not be ignored, but serialised as
     * Json NULL.
     */
    public final boolean serialiseNull;

    private final Collection<String> aliases;

    /**
     * The (OData)annotations for this Entity Property.
     */
    private final List<Annotation> annotations = new ArrayList<>();

    public EntityPropertyMain(String name, PropertyType type) {
        this(name, type, false, true, false, false);
    }

    public EntityPropertyMain(String name, PropertyType type, boolean required, boolean nullable) {
        this(name, type, required, nullable, false, false);
    }

    public EntityPropertyMain(String name, PropertyType type, boolean required, boolean nullable, boolean hasCustomProperties, boolean serialiseNull) {
        super(name, type, required, nullable, false);

        this.nullable = nullable;
        this.aliases = new ArrayList<>();
        this.aliases.add(name);
        this.hasCustomProperties = hasCustomProperties;
        this.serialiseNull = serialiseNull;
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

    public EntityPropertyMain<P> setRequired(boolean required) {
        this.required = required;
        return this;
    }

    public EntityPropertyMain<P> setNullable(boolean nullable) {
        this.nullable = nullable;
        return this;
    }

    public EntityPropertyMain<P> setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    @Override
    public P getFrom(Entity entity) {
        return entity.getProperty(this);
    }

    @Override
    public boolean isSetOn(Entity entity) {
        return entity.isSetProperty(this);
    }

    @Override
    public void setOn(Entity entity, P value) {
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
