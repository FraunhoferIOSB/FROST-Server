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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * A helper class for keeping track of entity counts. Both global counts, and
 * linked counts per individual entity.
 *
 * @author Hylke van der Schaaf
 */
public class EntityCounts {

    private final Map<EntityType, Long> globalCounts = new EnumMap<>(EntityType.class);
    private final Map<EntityType, Map<Object, Map<EntityType, Long>>> linkedCounts = new EnumMap<>(EntityType.class);

    public long getCount(EntityType type) {
        Long count = globalCounts.get(type);
        return count == null ? -1 : count;
    }

    public long getCount(EntityType parentType, Object parentId, EntityType linkedType) {
        Map<Object, Map<EntityType, Long>> parents = linkedCounts.get(parentType);
        if (parents == null) {
            return -1;
        }
        Map<EntityType, Long> parent = parents.get(parentId);
        if (parent == null) {
            return -1;
        }
        Long count = parent.get(linkedType);
        return count == null ? -1 : count;
    }

    private Map<Object, Map<EntityType, Long>> getParents(EntityType type) {
        Map<Object, Map<EntityType, Long>> parents = linkedCounts.get(type);
        if (parents == null) {
            parents = new HashMap<>();
            linkedCounts.put(type, parents);
        }
        return parents;
    }

    private Map<EntityType, Long> getCounts(EntityType parentType, Object parentId) {
        Map<Object, Map<EntityType, Long>> parents = getParents(parentType);
        Map<EntityType, Long> parent = parents.get(parentId);
        if (parent == null) {
            parent = new EnumMap<>(EntityType.class);
            parents.put(parentId, parent);
        }
        return parent;
    }

    public EntityCounts setGlobalCount(EntityType type, long count) {
        globalCounts.put(type, count);
        return this;
    }

    public EntityCounts setCount(EntityType parentType, Object parentId, EntityType linkedType, long count) {
        getCounts(parentType, parentId).put(linkedType, count);
        return this;
    }

    public void clear() {
        globalCounts.clear();
        linkedCounts.clear();
    }
}
