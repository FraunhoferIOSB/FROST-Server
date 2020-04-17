/*
 * Copyright (C) 2020 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package de.fraunhofer.iosb.ilt.frostserver.property;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.NavigableElement;
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
public class NavigationPropertyCustom implements NavigationProperty {

    private static final Logger LOGGER = LoggerFactory.getLogger(NavigationPropertyCustom.class.getName());
    private static final String NOT_SUPPORTED = "Not supported on NavigationPropertyCustom.";

    private final EntityProperty entityProperty;
    private final List<String> subPath = new ArrayList<>();
    private String name;
    private final LinkTargetData targetData = new LinkTargetData();

    public NavigationPropertyCustom(EntityProperty entityProperty) {
        this.entityProperty = entityProperty;
    }

    @Override
    public EntityType getType() {
        return targetData.type;
    }

    public List<String> getSubPath() {
        return subPath;
    }

    public void addToSubPath(String subPathElement) {
        subPath.add(subPathElement);
        name = subPathElement;
    }

    @Override
    public boolean validFor(EntityType entityType) {
        return entityType.getPropertySet().contains(entityProperty);
    }

    @Override
    public boolean isEntitySet() {
        return false;
    }

    @Override
    public String getName() {
        return entityProperty.entitiyName + "/" + StringUtils.join(subPath, '/');
    }

    @Override
    public String getJsonName() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    public void setElementOn(Entity entity, NavigableElement expandedElement) {
        if (!Objects.equals(entity, targetData.entity)) {
            targetData.findLinkTargetData(entity, entityProperty, subPath);
        }
        targetData.containingMap.put(name + "." + targetData.type.entityName, expandedElement);
    }

    public Object getTargetIdFrom(Entity entity) {
        if (!Objects.equals(entity, targetData.entity)) {
            targetData.findLinkTargetData(entity, entityProperty, subPath);
        }
        return targetData.targetId;
    }

    @Override
    public Object getFrom(Entity entity) {
        if (!Objects.equals(entity, targetData.entity)) {
            targetData.findLinkTargetData(entity, entityProperty, subPath);
        }
        return targetData.containingMap.get(targetData.fullKeyEntity);
    }

    @Override
    public void setOn(Entity entity, Object value) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public boolean isSetOn(Entity entity) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    private static class LinkTargetData {

        Entity entity;
        EntityType type;
        Map<String, Object> containingMap;
        String fullKeyId;
        String fullKeyEntity;
        Object targetId;

        public void clear() {
            entity = null;
            type = null;
            containingMap = null;
            fullKeyId = null;
        }

        public void findLinkTargetData(Entity entity, EntityProperty entityProperty, List<String> subPath) {
            clear();
            Object curTarget = entityProperty.getFrom(entity);
            int count = subPath.size();
            for (int idx = 0; idx < count; idx++) {
                String curPathItem = subPath.get(idx);
                if (curTarget instanceof Map) {
                    Map map = (Map) curTarget;
                    curTarget = findInMap(map, curPathItem, idx == count - 1);
                } else {
                    return;
                }
            }
            this.entity = entity;
        }

        private Object findInMap(Map<String, Object> map, String name, boolean last) {
            Object curTarget = map.get(name);
            if (curTarget != null) {
                return curTarget;
            }
            if (last) {
                findLinkEntryInMap(map, name);
            }
            return null;
        }

        private void findLinkEntryInMap(Map<String, Object> map, String name) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith(name)) {
                    String[] split = StringUtils.split(key, ".@");
                    if (split.length != 4) {
                        LOGGER.info("Could not split key: {}", key);
                        return;
                    }
                    if (!"iot".equals(split[2]) && !"id".equals(split[3])) {
                        LOGGER.info("last part of key not @iot.id: {}", key);
                        return;
                    }
                    type = EntityType.getEntityTypeForName(split[1]);
                    containingMap = map;
                    fullKeyId = key;
                    fullKeyEntity = name + "." + type.entityName;
                    targetId = entry.getValue();
                    return;
                }
            }
            LOGGER.trace("Not found in map: {}", name);
        }
    }

}
