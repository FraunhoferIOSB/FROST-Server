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
package de.fraunhofer.iosb.ilt.statests.util.model;

import de.fraunhofer.iosb.ilt.statests.util.Utils;

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
            value.append('(').append(Utils.quoteForUrl(id)).append(')');
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
