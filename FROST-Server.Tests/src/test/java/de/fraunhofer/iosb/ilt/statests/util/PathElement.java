/*
 * Copyright 2016 Open Geospatial Consortium.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.iosb.ilt.statests.util;

/**
 *
 * @author Hylke van der Schaaf
 */
public class PathElement implements Cloneable {

    private final EntityType entityType;
    private final boolean plural;
    private final Object id;

    public PathElement(EntityType entityType, boolean plural, Object id) {
        this.entityType = entityType;
        this.plural = plural;
        this.id = id;
    }

    public PathElement(String pathPart) {
        this(pathPart, null);
    }

    public PathElement(String pathPart, Object id) {
        entityType = EntityType.getForRelation(pathPart);
        plural = EntityType.isPlural(pathPart);
        this.id = id;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public String getPropertyName() {
        return plural ? entityType.plural : entityType.singular;
    }

    @Override
    public String toString() {
        StringBuilder value = new StringBuilder();
        value.append(plural ? entityType.plural : entityType.singular);
        if (id != null) {
            value.append('(').append(Utils.quoteIdForUrl(id)).append(')');
        }
        return value.toString();
    }

    public boolean isCollection() {
        return plural && id == null;
    }

    public Object getId() {
        return id;
    }

    @Override
    protected PathElement clone() {
        PathElement clone;
        try {
            clone = (PathElement) super.clone();
        } catch (CloneNotSupportedException ex) {
            // should not happen
            throw new IllegalStateException(ex);
        }
        return clone;
    }

}
