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

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.annotations.Annotatable;
import de.fraunhofer.iosb.ilt.frostserver.model.core.annotations.Annotation;
import de.fraunhofer.iosb.ilt.frostserver.property.type.PropertyType;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
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
public class EntityPropertyMain<P> implements Annotatable, EntityProperty<P> {

    /**
     * The entityName of this property.
     */
    public final String name;

    /**
     * The type(class) of the type of the value of this property.
     */
    private final PropertyType type;

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
        this(name, type, false, false);
    }

    public EntityPropertyMain(String name, PropertyType type, String... aliases) {
        this(name, type, false, false, aliases);
    }

    public EntityPropertyMain(String name, PropertyType type, boolean hasCustomProperties, boolean serialiseNull, String... aliases) {
        if (type == null) {
            throw new IllegalArgumentException("Type must not be null");
        }
        this.type = type;
        this.aliases = new ArrayList<>();
        this.aliases.add(name);
        this.name = StringHelper.deCapitalize(name);
        this.aliases.addAll(Arrays.asList(aliases));
        this.hasCustomProperties = hasCustomProperties;
        this.serialiseNull = serialiseNull;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getJsonName() {
        return name;
    }

    public Collection<String> getAliases() {
        return aliases;
    }

    @Override
    public PropertyType getType() {
        return type;
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
        return Objects.equals(this.name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.name);
    }

    @Override
    public String toString() {
        return getName();
    }
}
