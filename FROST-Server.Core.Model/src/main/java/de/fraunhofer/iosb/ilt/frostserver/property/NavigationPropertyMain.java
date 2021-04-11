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

import com.fasterxml.jackson.core.type.TypeReference;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.NavigableElement;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_ENTITY;
import static de.fraunhofer.iosb.ilt.frostserver.model.ext.TypeReferencesHelper.TYPE_REFERENCE_ENTITYSET;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.UrlHelper;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 *
 * @author jab
 * @author scf
 * @param <P> The entityType of the value of the property.
 */
public class NavigationPropertyMain<P extends NavigableElement> implements NavigationProperty<P> {

    public static class NavigationPropertyEntity extends NavigationPropertyMain<Entity> {

        public NavigationPropertyEntity(String propertyName) {
            super(propertyName, false, TYPE_REFERENCE_ENTITY);
        }
    }

    public static class NavigationPropertyEntitySet extends NavigationPropertyMain<EntitySet> {

        public NavigationPropertyEntitySet(String propertyName) {
            super(propertyName, true, TYPE_REFERENCE_ENTITYSET);
        }
    }

    /**
     * The name of the navigation property in urls.
     */
    private final String name;
    /**
     * The type(class) of the type of the value of this property.
     */
    private final TypeReference<P> type;
    /**
     * The entityType of entity that this navigation property points to.
     */
    private EntityType entityType;
    /**
     * Flag indication the path is to an EntitySet.
     */
    private final boolean entitySet;

    private final Collection<String> aliases;

    private NavigationPropertyMain(String propertyName, boolean isSet, TypeReference<P> type) {
        this.type = type;
        this.name = propertyName;
        this.aliases = new ArrayList<>();
        this.aliases.add(propertyName);
        this.entitySet = isSet;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    @Override
    public EntityType getEntityType() {
        return entityType;
    }

    @Override
    public boolean validFor(EntityType entityType) {
        return (entityType.getProperty(name) instanceof NavigationProperty);
    }

    @Override
    public boolean isEntitySet() {
        return entitySet;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getJsonName() {
        return name;
    }

    @Override
    public TypeReference<P> getType() {
        return type;
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
        String link = parent.getSelfLink() + '/' + name;
        Query query = parent.getQuery();
        if (query != null && !query.getSettings().useAbsoluteNavigationLinks()) {
            ResourcePath path = query.getPath();
            String curPath = path.getServiceRootUrl() + '/' + path.getVersion().urlPart + path.getPath();
            link = UrlHelper.getRelativePath(link, curPath);
        }
        return link;
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
