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
package de.fraunhofer.iosb.ilt.statests.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link java.lang.Object#clone()} returns a deep copy.
 *
 * @author Hylke van der Schaaf
 */
public class Expand implements Cloneable {

    private final List<PathElement> path = new ArrayList<>();
    private final Query query;
    private EntityType entityType;

    public Expand() {
        query = new Query();
        query.setParent(this);
    }

    public Expand(Query query) {
        this.query = query;
        query.setParent(this);
    }

    public List<PathElement> getPath() {
        return path;
    }

    public Expand addElement(PathElement element) {
        path.add(element);
        return this;
    }

    public boolean isCollection() {
        return path.isEmpty() || path.get(path.size() - 1).isCollection();
    }

    public EntityType getEntityType() {
        if (entityType == null) {
            entityType = path.get(path.size() - 1).getEntityType();
        }
        return entityType;
    }

    public Expand setEntityType(EntityType entityType) {
        this.entityType = entityType;
        return this;
    }

    public Query getQuery() {
        return query;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean firstDone = false;
        for (PathElement element : path) {
            if (firstDone) {
                sb.append("/");
            } else {
                firstDone = true;
            }
            sb.append(element.toString());
        }
        if (!query.isEmpty()) {
            sb.append('(');
            sb.append(query.toString(true));
            sb.append(')');
        }
        return sb.toString();
    }

    public boolean isToplevel() {
        return false;
    }

    /**
     * Turns a multi-level expand (Datastreams/Sensor) into nested expands.
     *
     * @return a proper nested expand.
     */
    public Expand reNest() {
        if (path.size() == 1) {
            return this;
        }
        query.reNestExpands();
        Query currentQuery = query;
        Expand currentExpand = null;
        for (int i = path.size() - 1; i >= 0; i--) {
            currentExpand = new Expand(currentQuery);
            currentExpand.addElement(path.get(i));
            currentQuery = new Query();
            currentQuery.addExpand(currentExpand);
        }
        return currentExpand;
    }

    @Override
    public Expand clone() {
        Expand clone;
        try {
            // Can't use super.clone() since that would make the path of the
            // clone a reference to our path.
            clone = getClass().getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException ex) {
            // should not happen
            throw new IllegalStateException(ex);
        }
        for (PathElement item : path) {
            clone.path.add(item.clone());
        }
        clone.query.duplicate(query);
        return clone;
    }

}
