/*
 * Copyright (C) 2016 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.sta.util;

import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.NavigationProperty;
import de.fraunhofer.iosb.ilt.sta.path.Property;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePath;
import de.fraunhofer.iosb.ilt.sta.query.Expand;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class VisibilityHelper {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(VisibilityHelper.class);

    private static class Visibility {

        Set<Property> allProperties = new HashSet<>();
        Set<Property> visibleProperties = new HashSet<>();
        Set<String> visiblePropertyNames;
        Set<NavigationProperty> navLinkProperties = new HashSet<>();
        Map<NavigationProperty, Visibility> expandVisibility = new HashMap<>();

        public Set<String> getVisiblePropertyNames() {
            if (visiblePropertyNames == null) {
                visiblePropertyNames = visibleProperties.stream().map(x -> x.getJsonName()).collect(Collectors.toSet());
            }
            return visiblePropertyNames;
        }

        /**
         * Merge the other visibility into this one.
         *
         * Currently takes the most visible version.
         *
         * @param other The visibility to merge into this one.
         */
        public void merge(Visibility other) {
            visiblePropertyNames = null;
            visibleProperties.addAll(other.visibleProperties);
            navLinkProperties.addAll(other.navLinkProperties);
            for (Map.Entry<NavigationProperty, Visibility> otherSet : other.expandVisibility.entrySet()) {
                NavigationProperty otherSubProp = otherSet.getKey();
                Visibility otherSubVis = otherSet.getValue();
                Visibility subVis = expandVisibility.get(otherSubProp);
                if (subVis == null) {
                    expandVisibility.put(otherSubProp, otherSubVis);
                } else {
                    subVis.merge(otherSubVis);
                }
            }
        }
    }

    private VisibilityHelper() {
    }

    public static void applyVisibility(Entity entity, ResourcePath path, Query query, boolean useAbsoluteNavigationLinks) {
        if (path.isRef()) {
            Set<Property> select = query.getSelect();
            select.clear();
            select.add(EntityProperty.SelfLink);
        }
        Visibility v = createVisibility(entity.getEntityType(), query, true);
        applyVisibility(entity, path, v, useAbsoluteNavigationLinks);
    }

    public static void applyVisibility(EntitySet<? extends Entity> entitySet, ResourcePath path, Query query, boolean useAbsoluteNavigationLinks) {
        if (entitySet.isEmpty()) {
            return;
        }
        if (path.isRef()) {
            Set<Property> select = query.getSelect();
            select.clear();
            select.add(EntityProperty.SelfLink);
        }
        EntityType type = entitySet.asList().get(0).getEntityType();
        Visibility v = createVisibility(type, query, true);
        applyVisibility(entitySet, path, v, useAbsoluteNavigationLinks);
    }

    private static void applyVisibility(Entity e, ResourcePath path, Visibility v, boolean useAbsoluteNavigationLinks) {
        e.setSelfLink(UrlHelper.generateSelfLink(path, e));
        for (Property p : v.navLinkProperties) {
            Object child = e.getProperty(p);
            if (child instanceof Entity) {
                Entity childEntity = (Entity) child;
                childEntity.setNavigationLink(UrlHelper.generateNavLink(path, e, childEntity, useAbsoluteNavigationLinks));
            } else if (child instanceof EntitySet) {
                EntitySet childSet = (EntitySet) child;
                childSet.setNavigationLink(UrlHelper.generateNavLink(path, e, childSet, useAbsoluteNavigationLinks));
            }
        }
        for (Map.Entry<NavigationProperty, Visibility> es : v.expandVisibility.entrySet()) {
            Object property = e.getProperty(es.getKey());
            if (property instanceof Entity) {
                Entity entity = (Entity) property;
                entity.setExportObject(true);
                applyVisibility(entity, path, es.getValue(), useAbsoluteNavigationLinks);
            } else if (property instanceof EntitySet) {
                EntitySet entitySet = (EntitySet) property;
                applyVisibility(entitySet, path, es.getValue(), useAbsoluteNavigationLinks);
                entitySet.setExportObject(true);
            }
        }
        e.setSelectedPropertyNames(v.getVisiblePropertyNames());
    }

    private static void applyVisibility(EntitySet<? extends Entity> es, ResourcePath path, Visibility v, boolean useAbsoluteNavigationLinks) {
        for (Entity e : es) {
            applyVisibility(e, path, v, useAbsoluteNavigationLinks);
        }
    }

    private static Visibility createVisibility(EntityType entityType, Query query, boolean topLevel) {
        Visibility v = new Visibility();
        Set<Property> properties = entityType.getPropertySet();
        v.allProperties.addAll(properties);

        if (query == null || query.getSelect().isEmpty()) {
            if (topLevel) {
                v.visibleProperties.addAll(v.allProperties);
                copyNavigationProperties(v.allProperties, v.navLinkProperties);
            } else {
                copyEntityProperties(v.allProperties, v.visibleProperties);
            }
        }
        if (query != null) {
            if (!query.getSelect().isEmpty()) {
                for (Property select : query.getSelect()) {
                    v.visibleProperties.add(select);
                    if (select instanceof NavigationProperty) {
                        v.navLinkProperties.add((NavigationProperty) select);
                    }
                }
            }

            for (Expand expand : query.getExpand()) {
                List<NavigationProperty> expPath = expand.getPath();
                Visibility level = v;
                for (int i = 0; i < expPath.size(); i++) {
                    NavigationProperty np = expPath.get(i);
                    Visibility subLevel;
                    if (i == expPath.size() - 1) {
                        subLevel = createVisibility(np.type, expand.getSubQuery(), false);
                    } else {
                        subLevel = createVisibility(np.type, null, false);
                    }
                    Visibility existingVis = level.expandVisibility.get(np);
                    if (existingVis != null) {
                        subLevel.merge(existingVis);
                    }
                    level.expandVisibility.put(np, subLevel);
                    level = subLevel;
                }
            }
        }
        return v;
    }

    private static void copyEntityProperties(Set<Property> from, Set<Property> to) {
        for (Property p : from) {
            if (EntityProperty.class.isAssignableFrom(p.getClass())) {
                to.add(p);
            }
        }
    }

    private static void copyNavigationProperties(Set<Property> from, Set<NavigationProperty> to) {
        for (Property p : from) {
            if (NavigationProperty.class.isAssignableFrom(p.getClass())) {
                to.add((NavigationProperty) p);
            }
        }
    }

    private static void copyNavigationProperties2(Set<Property> from, Set<Property> to) {
        for (Property p : from) {
            if (NavigationProperty.class.isAssignableFrom(p.getClass())) {
                to.add((NavigationProperty) p);
            }
        }
    }

}
