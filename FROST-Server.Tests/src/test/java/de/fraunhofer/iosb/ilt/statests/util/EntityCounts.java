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

import de.fraunhofer.iosb.ilt.statests.util.model.EntityType;
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
