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

import de.fraunhofer.iosb.ilt.frostserver.model.ComplexValue;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.NavigableElement;
import de.fraunhofer.iosb.ilt.frostserver.model.core.annotations.Annotatable;
import de.fraunhofer.iosb.ilt.frostserver.model.core.annotations.Annotation;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.UrlHelper;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * The property that represents a link to another entity or set of entities.
 *
 * @param <P> The entityType of the value of the property.
 */
public abstract class NavigationPropertyMain<P extends NavigableElement> extends PropertyAbstract<P> implements Annotatable, NavigationProperty<P> {

    public static class NavigationPropertyEntity extends NavigationPropertyMain<Entity> {

        public NavigationPropertyEntity(String propertyName, String... options) {
            this(propertyName, null, 0, options);
        }

        public NavigationPropertyEntity(String propertyName, Set<String> options) {
            this(propertyName, null, 0, options);
        }

        public NavigationPropertyEntity(String propertyName, int priority, String... options) {
            this(propertyName, null, priority, options);
        }

        public NavigationPropertyEntity(String propertyName, int priority, Set<String> options) {
            this(propertyName, null, priority, options);
        }

        public NavigationPropertyEntity(String propertyName, NavigationPropertyMain inverse, String... options) {
            this(propertyName, inverse, 0, options);
        }

        public NavigationPropertyEntity(String propertyName, NavigationPropertyMain inverse, Set<String> options) {
            this(propertyName, inverse, 0, options);
        }

        public NavigationPropertyEntity(String propertyName, NavigationPropertyMain inverse, int priority, String... options) {
            this(propertyName, inverse, priority, new HashSet<>(Arrays.asList(options)));
        }

        public NavigationPropertyEntity(String propertyName, NavigationPropertyMain inverse, int priority, Set<String> options) {
            super(propertyName, false, priority, options);
            if (inverse != null) {
                setInverses(inverse);
            }
        }
    }

    public static class NavigationPropertyEntitySet extends NavigationPropertyMain<EntitySet> {

        public NavigationPropertyEntitySet(String propertyName) {
            this(propertyName, null, 0);
        }

        public NavigationPropertyEntitySet(String propertyName, int priority) {
            this(propertyName, null, priority);
        }

        public NavigationPropertyEntitySet(String propertyName, NavigationPropertyMain inverse) {
            this(propertyName, inverse, 0);
        }

        public NavigationPropertyEntitySet(String propertyName, NavigationPropertyMain inverse, int priority) {
            super(propertyName, true, priority, NULLABLE);
            if (inverse != null) {
                setInverses(inverse);
            }
        }
    }

    /**
     * The entityType of entity that this navigation property points to.
     */
    private EntityType entityType;

    /**
     * Flag indication the path is to an EntitySet.
     */
    private final boolean entitySet;

    /**
     * The inverse of this navigation link.
     */
    private NavigationPropertyMain<?> inverse;

    /**
     * The (OData)annotations for this Navigation Property.
     */
    private final List<Annotation> annotations = new ArrayList<>();

    /**
     * The priority used for ordering.
     */
    private final int priority;

    private NavigationPropertyMain(String propertyName, boolean isSet, int priority, String... options) {
        this(propertyName, isSet, priority, new HashSet<>(Arrays.asList(options)));
    }

    private NavigationPropertyMain(String propertyName, boolean isSet, int priority, Set<String> options) {
        super(propertyName, TypeSimplePrimitive.EDM_UNTYPED, options);
        this.entitySet = isSet;
        this.priority = priority;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
        if (entitySet) {
            setType(new TypeEntitySet(entityType));
        } else {
            setType(new TypeEntity(entityType));
        }
    }

    @Override
    public NavigationPropertyMain getInverse() {
        return inverse;
    }

    public final void setInverse(NavigationPropertyMain inverse) {
        this.inverse = inverse;
    }

    public final void setInverses(NavigationPropertyMain inverse) {
        this.inverse = inverse;
        inverse.setInverse(this);
    }

    @Override
    public EntityType getEntityType() {
        return entityType;
    }

    @Override
    public boolean validFor(EntityType entityType) {
        return (entityType.getProperty(getName()) instanceof NavigationProperty);
    }

    @Override
    public boolean isEntitySet() {
        return entitySet;
    }

    /**
     * Flag indicating only admin users are allowed to see the target entity
     * type.
     *
     * @return true if only admin users are allowed to see the target entity
     * type.
     */
    @Override
    public boolean isAdminOnly() {
        return entityType.isAdminOnly();
    }

    @Override
    public P getFrom(ComplexValue<?> entity) {
        return entity.getProperty(this);
    }

    @Override
    public void setOn(ComplexValue<?> entity, P value) {
        entity.setProperty(this, value);
    }

    @Override
    public boolean isSetOn(ComplexValue<?> entity) {
        return entity.isSetProperty(this);
    }

    @Override
    public String getNavigationLink(Entity parent) {
        String selfLink = parent.getSelfLink();
        if (selfLink == null) {
            return null;
        }
        String link = selfLink + '/' + getName();
        Query query = parent.getQuery();
        if (query != null && !query.getSettings().useAbsoluteNavigationLinks()) {
            ResourcePath path = query.getPath();
            String curPath = path.getServiceRootUrl() + '/' + path.getVersion().urlPart + path.getPath();
            link = UrlHelper.getRelativePath(link, curPath);
        }
        return link;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public NavigationPropertyMain<P> addAnnotation(Annotation annotation) {
        annotations.add(annotation);
        return this;
    }

    public NavigationPropertyMain<P> addAnnotations(List<Annotation> annotationsToAdd) {
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
        final NavigationPropertyMain<?> other = (NavigationPropertyMain<?>) obj;
        return Objects.equals(getName(), other.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getName());
    }

    @Override
    public String toString() {
        return getName();
    }

}
