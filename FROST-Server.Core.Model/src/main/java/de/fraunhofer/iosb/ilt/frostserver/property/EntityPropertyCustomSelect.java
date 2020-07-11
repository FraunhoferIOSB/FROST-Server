/*
 * Copyright (C) 2016 Fraunhofer IOSB.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.frostserver.property;

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.util.CollectionsHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * A custom property as used in $select. In this case the entire custom property
 * path is captured in one single property.
 *
 * @author Hylke van der Schaaf
 */
public class EntityPropertyCustomSelect implements EntityProperty {

    private static final String NOT_SUPPORTED = "Not supported on custom properties.";

    private final EntityPropertyMain entityProperty;
    private final List<String> subPath = new ArrayList<>();

    public EntityPropertyCustomSelect(EntityPropertyMain entityProperty) {
        this.entityProperty = entityProperty;
    }

    public EntityPropertyMain getMainEntityProperty() {
        return entityProperty;
    }

    public List<String> getSubPath() {
        return subPath;
    }

    public EntityPropertyCustomSelect addToSubPath(String subPathElement) {
        subPath.add(subPathElement);
        return this;
    }

    @Override
    public String getName() {
        return entityProperty.entitiyName + "/" + StringUtils.join(subPath, '/');
    }

    @Override
    public String getJsonName() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public Object getFrom(Entity entity) {
        Object baseProperty = entity.getProperty(entityProperty);
        if (baseProperty instanceof Map) {
            return CollectionsHelper.getFrom((Map<String, Object>) baseProperty, subPath);
        }
        return null;
    }

    @Override
    public void setOn(Entity entity, Object value) {
        Object baseProperty = entity.getProperty(entityProperty);
        if (baseProperty == null) {
            Map<String, Object> basePropertyMap = new HashMap<>();
            baseProperty = basePropertyMap;
            entity.setProperty(entityProperty, baseProperty);
        }
        if (baseProperty instanceof Map) {
            CollectionsHelper.setOn((Map<String, Object>) baseProperty, subPath, value);
        } else {
            throw new UnsupportedOperationException("Can not set: " + entityProperty + " value is not a map.");
        }
    }

    @Override
    public boolean isSetOn(Entity entity) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityProperty, subPath);
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
        final EntityPropertyCustomSelect other = (EntityPropertyCustomSelect) obj;
        return Objects.equals(this.entityProperty, other.entityProperty)
                && Objects.equals(this.subPath, other.subPath);
    }

}
