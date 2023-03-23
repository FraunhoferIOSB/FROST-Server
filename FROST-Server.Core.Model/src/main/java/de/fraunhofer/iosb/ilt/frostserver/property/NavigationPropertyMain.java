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

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.NavigableElement;
import de.fraunhofer.iosb.ilt.frostserver.model.core.annotations.Annotatable;
import de.fraunhofer.iosb.ilt.frostserver.model.core.annotations.Annotation;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.UrlHelper;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author jab
 * @author scf
 * @param <P> The entityType of the value of the property.
 */
public abstract class NavigationPropertyMain<P extends NavigableElement> extends PropertyAbstract<P> implements Annotatable, NavigationProperty<P> {

    public static class NavigationPropertyEntity extends NavigationPropertyMain<Entity> {

        public NavigationPropertyEntity(String propertyName, boolean required) {
            super(propertyName, false, required, !required);
        }

        public NavigationPropertyEntity(String propertyName, NavigationPropertyMain inverse, boolean required) {
            super(propertyName, false, required, !required);
            setInverses(inverse);
        }
    }

    public static class NavigationPropertyEntitySet extends NavigationPropertyMain<EntitySet> {

        public NavigationPropertyEntitySet(String propertyName) {
            super(propertyName, true, false, true);
        }

        public NavigationPropertyEntitySet(String propertyName, NavigationPropertyMain inverse) {
            super(propertyName, true, false, true);
            setInverses(inverse);
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

    private NavigationPropertyMain<?> inverse;

    /**
     * The (OData)annotations for this Navigation Property.
     */
    private final List<Annotation> annotations = new ArrayList<>();

    private NavigationPropertyMain(String propertyName, boolean isSet, boolean required, boolean nullable) {
        super(propertyName, TypeComplex.STA_OBJECT, required, nullable, false);
        this.entitySet = isSet;
        this.required = required;
        this.nullable = nullable;
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

    public void setRequired(boolean required) {
        this.required = required;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    @Override
    public P getFrom(Entity entity) {
        return entity.getProperty(this);
    }

    @Override
    public void setOn(Entity entity, P value) {
        entity.setProperty(this, value);
    }

    @Override
    public boolean isSetOn(Entity entity) {
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

}
