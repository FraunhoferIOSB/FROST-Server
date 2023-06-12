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
package de.fraunhofer.iosb.ilt.frostserver.query;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElement;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementCustomProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyCustom;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author jab
 */
public class Expand {

    private ModelRegistry modelRegistry;
    private List<String> rawPath;
    private NavigationProperty validatedPath;
    private Query parentQuery;
    private Query subQuery;

    public Expand(ModelRegistry modelRegistry) {
        this.modelRegistry = modelRegistry;
    }

    public Expand(ModelRegistry modelRegistry, Query subQuery) {
        this.modelRegistry = modelRegistry;
        if (subQuery != null) {
            setSubQuery(subQuery);
        }
    }

    public Expand(ModelRegistry modelRegistry, NavigationProperty path) {
        if (path == null) {
            throw new IllegalArgumentException("path must be non-empty");
        }
        this.modelRegistry = modelRegistry;
        this.validatedPath = path;
    }

    public Expand(ModelRegistry modelRegistry, Query subQuery, NavigationProperty path) {
        if (path == null) {
            throw new IllegalArgumentException("paths must be non-empty");
        }
        this.modelRegistry = modelRegistry;
        this.validatedPath = path;
        setSubQuery(subQuery);
    }

    public NavigationProperty getPath() {
        return validatedPath;
    }

    public void addToRawPath(String subPath) {
        if (rawPath == null) {
            rawPath = new ArrayList<>();
        }
        this.rawPath.add(subPath);
    }

    public List<String> getRawPath() {
        if (rawPath == null) {
            final String[] items = StringUtils.split(validatedPath.getName(), '/');
            rawPath = Arrays.asList(items);
        }
        return rawPath;
    }

    public boolean hasSubQuery() {
        return subQuery != null;
    }

    public Query getSubQuery() {
        if (subQuery == null) {
            final Query newSubQuery = new Query(parentQuery)
                    .validate(validatedPath.getEntityType());
            setSubQuery(newSubQuery);
        }
        return subQuery;
    }

    public final Expand setSubQuery(Query subQuery) {
        this.subQuery = subQuery;
        this.subQuery.setParentExpand(this);
        return this;
    }

    public Query getParentQuery() {
        return parentQuery;
    }

    public void setParentQuery(Query parentQuery) {
        this.parentQuery = parentQuery;
    }

    public void validate(ResourcePath path) {
        PathElement mainElement = path.getMainElement();
        if (mainElement instanceof PathElementProperty || mainElement instanceof PathElementCustomProperty) {
            throw new IllegalArgumentException("No expand allowed on property paths.");
        }
        EntityType entityType = path.getMainElementType();
        if (entityType == null) {
            throw new IllegalStateException("Unkown ResourcePathElementType found.");
        }
        validate(entityType);
    }

    protected void validate(EntityType entityType) {
        if (validatedPath == null) {
            final String firstRawPath = rawPath.get(0);
            final Property property = entityType.getProperty(firstRawPath);
            final int rawCount = rawPath.size();
            if (property instanceof NavigationPropertyMain npm) {
                if (npm.isAdminOnly() && !parentQuery.getPrincipal().isAdmin()) {
                    throw new IllegalArgumentException("Unknown path '" + firstRawPath + "' in expand on entity type " + entityType.entityName);
                }
                validatedPath = npm;
                if (rawCount > 1) {
                    // Need to re-nest this expand!
                    Expand subExpand = new Expand(modelRegistry, subQuery);
                    for (int i = 1; i < rawCount; i++) {
                        subExpand.addToRawPath(rawPath.get(i));
                    }
                    rawPath.clear();
                    rawPath.add(firstRawPath);
                    subQuery = new Query(parentQuery);
                    subQuery.addExpand(subExpand);
                    subQuery.setParentExpand(this);
                }
            } else if (property instanceof EntityPropertyMain && ((EntityPropertyMain) property).hasCustomProperties) {
                EntityPropertyMain entityPropertyMain = (EntityPropertyMain) property;
                NavigationPropertyCustom tempPath = new NavigationPropertyCustom(modelRegistry, entityPropertyMain);
                for (int i = 1; i < rawCount; i++) {
                    tempPath.addToSubPath(rawPath.get(i));
                }
                validatedPath = tempPath;
            } else {
                throw new IllegalArgumentException("Unknown path '" + firstRawPath + "' in expand on entity type " + entityType.entityName);
            }

        }
        if (subQuery != null && validatedPath instanceof NavigationPropertyMain) {
            if (!subQuery.hasMetadata()) {
                subQuery.setMetadata(parentQuery.getMetadata());
            }
            subQuery.validate(validatedPath.getEntityType());
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(validatedPath, subQuery);
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
        final Expand other = (Expand) obj;
        return Objects.equals(this.getRawPath(), other.getRawPath())
                && Objects.equals(this.subQuery, other.subQuery);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (validatedPath == null) {
            sb.append(StringUtils.join(rawPath, "/"));
        } else {
            sb.append(validatedPath.getName());
        }
        if (subQuery != null && !subQuery.isEmpty()) {
            sb.append('(');
            sb.append(subQuery.toString(true));
            sb.append(')');
        }
        return sb.toString();
    }

}
