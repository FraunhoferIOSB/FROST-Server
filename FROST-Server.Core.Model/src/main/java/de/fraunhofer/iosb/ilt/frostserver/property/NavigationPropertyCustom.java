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

import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_ID;

import de.fraunhofer.iosb.ilt.frostserver.model.ComplexValue;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.NavigableElement;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PkValue;
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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * A user-defined navigation property.
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

    private void init(ComplexValue<?> entity) {
        if (type == null) {
            throw new IllegalArgumentException("Path not to a custom link: " + entityProperty + "/" + StringUtils.join(subPath, '/'));
        }
        if (!Objects.equals(entity, targetData.entity)) {
            targetData.findLinkTargetData(entity, entityProperty, subPath, name, type);
        }
    }

    @Override
    public boolean isAdminOnly() {
        return type.isAdminOnly();
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
        targetData.setKey(name + "." + type.entityName, expandedElement);
    }

    public PkValue getTargetIdFrom(Entity entity) {
        init(entity);
        return PkValue.of(targetData.targetId);
    }

    @Override
    public Entity getFrom(ComplexValue<?> entity) {
        init(entity);
        return (Entity) targetData.getKey(targetData.fullKeyEntity);
    }

    @Override
    public void setOn(ComplexValue<?> entity, Entity value) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public boolean isSetOn(ComplexValue<?> entity) {
        init(entity);
        return targetData.containsKey(targetData.fullKeyEntity);
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
            String curPath = query.getContext().getPrefixGen().getUrlPrefix() + path.getPath();
            link = UrlHelper.getRelativePath(link, curPath);
        }
        return link;
    }

    @Override
    public NavigationProperty getInverse() {
        return null;
    }

    private static class LinkTargetData {

        private ComplexValue<?> entity;
        private Map<String, Object> containingMap;
        private ObjectNode containingNode;
        private String fullKeyEntity;
        private Object targetId;

        public void clear() {
            entity = null;
            containingMap = null;
            containingNode = null;
            fullKeyEntity = null;
            targetId = null;
        }

        public boolean containsKey(String key) {
            if (containingNode != null) {
                return containingNode.has(key);
            }
            if (containingMap != null) {
                return containingMap.containsKey(key);
            }
            return false;
        }

        public Object getKey(String key) {
            if (containingNode != null) {
                return containingNode.get(key);
            }
            if (containingMap != null) {
                return containingMap.get(key);
            }
            return null;
        }

        public void setKey(String key, NavigableElement value) {
            if (containingNode != null) {
                containingNode.putPOJO(key, value);
            }
            if (containingMap != null) {
                containingMap.put(key, value);
            }
        }

        public void findLinkTargetData(ComplexValue entity, EntityPropertyMain entityProperty, List<String> subPath, String name, EntityType type) {
            clear();
            Object curTarget = entityProperty.getFrom(entity);
            int count = subPath.size() - 1;
            for (int idx = 0; idx < count; idx++) {
                String curPathItem = subPath.get(idx);
                switch (curTarget) {
                    case ObjectNode on ->
                        curTarget = on.get(curPathItem);
                    case ArrayNode an -> {
                        try {
                            int nr = Integer.parseInt(curPathItem);
                            curTarget = an.get(nr);
                        } catch (NumberFormatException ex) {
                            LOGGER.trace("Not a number, can't access array: {}", curPathItem, ex);
                            return;
                        }
                    }
                    case Map m ->
                        curTarget = m.get(curPathItem);
                    default -> {
                        return;
                    }
                }
            }
            if (curTarget instanceof ObjectNode on) {
                findLinkEntryIn(on, name, type);
            } else if (curTarget instanceof Map m) {
                findLinkEntryIn(m, name, type);
            }
            this.entity = entity;
        }

        private void findLinkEntryIn(ObjectNode on, String name, EntityType type) {
            fullKeyEntity = name + "." + type.entityName;
            String keyId = fullKeyEntity + AT_IOT_ID;
            JsonNode keyValue = on.get(keyId);
            if (keyValue == null) {
                LOGGER.trace("Not found in map: {}", name);
            } else {
                containingMap = null;
                containingNode = on;
                targetId = valueNodeToObject(keyValue);
            }
        }

        private void findLinkEntryIn(Map<String, Object> map, String name, EntityType type) {
            fullKeyEntity = name + "." + type.entityName;
            String keyId = fullKeyEntity + AT_IOT_ID;
            Object keyValue = map.get(keyId);
            if (keyValue == null) {
                LOGGER.trace("Not found in map: {}", name);
            } else {
                containingMap = map;
                containingNode = null;
                targetId = keyValue;
            }
        }

        private Object valueNodeToObject(JsonNode value) {
            if (value.isIntegralNumber()) {
                return value.asLong();
            }
            if (value.isString()) {
                return value.asString();
            }
            return null;
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
