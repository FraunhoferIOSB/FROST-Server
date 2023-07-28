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

import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_ID;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.NavigableElement;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.UrlHelper;
import de.fraunhofer.iosb.ilt.frostserver.property.type.PropertyType;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeEntity;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hylke
 */
public class NavigationPropertyCustom implements NavigationProperty<Entity> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NavigationPropertyCustom.class.getName());
    private static final String NOT_SUPPORTED = "Not supported on NavigationPropertyCustom.";

    private final ModelRegistry modelRegistry;
    private final EntityPropertyMain entityProperty;
    private final List<String> subPath = new ArrayList<>();
    private String name;
    private EntityType type;
    private final LinkTargetData targetData = new LinkTargetData();

    public NavigationPropertyCustom(ModelRegistry modelRegistry, EntityPropertyMain entityProperty) {
        this.modelRegistry = modelRegistry;
        this.entityProperty = entityProperty;
    }

    @Override
    public EntityType getEntityType() {
        return type;
    }

    public List<String> getSubPath() {
        return subPath;
    }

    public NavigationPropertyCustom addToSubPath(String subPathElement) {
        subPath.add(subPathElement);
        String[] split = StringUtils.split(subPathElement, '.');
        if (split.length == 1) {
            return this;
        }
        String typeName = split[split.length - 1];
        name = subPathElement.substring(0, subPathElement.length() - typeName.length() - 1);
        type = modelRegistry.getEntityTypeForName(typeName);
        return this;
    }

    private void init(Entity entity) {
        if (type == null) {
            throw new IllegalArgumentException("Path not to a custom link: " + entityProperty + "/" + StringUtils.join(subPath, '/'));
        }
        if (!Objects.equals(entity, targetData.entity)) {
            targetData.findLinkTargetData(entity, entityProperty, subPath, name, type);
        }
    }

    @Override
    public boolean validFor(EntityType entityType) {
        return entityType.getProperty(entityProperty.getName()) != null;
    }

    @Override
    public boolean isEntitySet() {
        return false;
    }

    @Override
    public String getName() {
        return entityProperty.getName() + "/" + StringUtils.join(subPath, '/');
    }

    @Override
    public String getJsonName() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public PropertyType getType() {
        return new TypeEntity(type);
    }

    @Override
    public boolean isRequired() {
        return false;
    }

    @Override
    public boolean isNullable() {
        return true;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    public void setElementOn(Entity entity, NavigableElement expandedElement) {
        init(entity);
        targetData.containingMap.put(name + "." + type.entityName, expandedElement);
    }

    public Object getTargetIdFrom(Entity entity) {
        init(entity);
        return targetData.targetId;
    }

    @Override
    public Entity getFrom(Entity entity) {
        init(entity);
        if (targetData.containingMap == null) {
            return null;
        }
        return (Entity) targetData.containingMap.get(targetData.fullKeyEntity);
    }

    @Override
    public void setOn(Entity entity, Entity value) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public boolean isSetOn(Entity entity) {
        init(entity);
        if (targetData.containingMap == null) {
            return false;
        }
        return targetData.containingMap.containsKey(targetData.fullKeyEntity);
    }

    @Override
    public String getNavigationLink(Entity parent) {
        String selfLink = parent.getSelfLink();
        if (selfLink == null) {
            return null;
        }
        String link = selfLink + '/' + entityProperty.getName() + '/' + String.join("/", subPath);
        if (!parent.getQuery().getSettings().useAbsoluteNavigationLinks()) {
            Query query = parent.getQuery();
            ResourcePath path = query.getPath();
            String curPath = path.getServiceRootUrl() + path.getPath();
            link = UrlHelper.getRelativePath(link, curPath);
        }
        return link;
    }

    @Override
    public NavigationProperty getInverse() {
        return null;
    }

    private static class LinkTargetData {

        private Entity entity;
        private Map<String, Object> containingMap;
        private String fullKeyEntity;
        private Object targetId;

        public void clear() {
            entity = null;
            containingMap = null;
            fullKeyEntity = null;
            targetId = null;
        }

        public void findLinkTargetData(Entity entity, EntityPropertyMain entityProperty, List<String> subPath, String name, EntityType type) {
            clear();
            Object curTarget = entityProperty.getFrom(entity);
            int count = subPath.size() - 1;
            for (int idx = 0; idx < count; idx++) {
                String curPathItem = subPath.get(idx);
                if (curTarget instanceof Map) {
                    Map<String, Object> map = (Map<String, Object>) curTarget;
                    curTarget = map.get(curPathItem);
                } else {
                    return;
                }
            }
            if (curTarget instanceof Map) {
                findLinkEntryInMap((Map<String, Object>) curTarget, name, type);
            }
            this.entity = entity;
        }

        private void findLinkEntryInMap(Map<String, Object> map, String name, EntityType type) {
            fullKeyEntity = name + "." + type.entityName;
            String keyId = fullKeyEntity + AT_IOT_ID;
            Object keyValue = map.get(keyId);
            if (keyValue == null) {
                LOGGER.trace("Not found in map: {}", name);
            } else {
                containingMap = map;
                targetId = keyValue;
            }
        }
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
        final NavigationPropertyCustom other = (NavigationPropertyCustom) obj;
        if (this.entityProperty != other.entityProperty) {
            return false;
        }
        return Objects.equals(this.subPath, other.subPath);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.entityProperty);
        hash = 37 * hash + Objects.hashCode(this.subPath);
        return hash;
    }

}
