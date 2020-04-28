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
package de.fraunhofer.iosb.ilt.frostserver.util;

import de.fraunhofer.iosb.ilt.frostserver.extensions.Extension;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.query.Expand;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author scf
 */
public class VisibilityHelper {

    private final CoreSettings settings;
    private final Map<EntityType, Set<Property>> enabledProperties = new EnumMap<>(EntityType.class);

    public VisibilityHelper(CoreSettings settings) {
        this.settings = settings;
    }

    public void applyVisibility(Entity<?> entity, ResourcePath path, Query query, boolean useAbsoluteNavigationLinks) {
        if (path.isRef()) {
            Set<Property> select = query.getSelect();
            select.clear();
            select.add(EntityProperty.SELFLINK);
        }
        Visibility v = createVisibility(entity.getEntityType(), query, true);
        applyVisibility(entity, path, v, useAbsoluteNavigationLinks);
    }

    public void applyVisibility(EntitySet<? extends Entity> entitySet, ResourcePath path, Query query, boolean useAbsoluteNavigationLinks) {
        if (entitySet.isEmpty()) {
            return;
        }
        if (path.isRef()) {
            Set<Property> select = query.getSelect();
            select.clear();
            select.add(EntityProperty.SELFLINK);
        }
        EntityType type = entitySet.asList().get(0).getEntityType();
        Visibility v = createVisibility(type, query, true);
        applyVisibility(entitySet, path, v, useAbsoluteNavigationLinks);
    }

    private void applyVisibility(Entity<?> e, ResourcePath path, Visibility v, boolean useAbsoluteNavigationLinks) {
        if (e.getId() != null) {
            e.setSelfLink(UrlHelper.generateSelfLink(path, e));
        }
        for (Property p : v.navLinkProperties) {
            Object child = e.getProperty(p);
            if (child instanceof Entity) {
                Entity<?> childEntity = (Entity) child;
                childEntity.setNavigationLink(UrlHelper.generateNavLink(path, e, childEntity, useAbsoluteNavigationLinks));
            } else if (child instanceof EntitySet) {
                EntitySet childSet = (EntitySet) child;
                childSet.setNavigationLink(UrlHelper.generateNavLink(path, e, childSet, useAbsoluteNavigationLinks));
            }
        }
        for (Map.Entry<NavigationProperty, Visibility> es : v.expandVisibility.entrySet()) {
            Object property = es.getKey().getFrom(e);
            if (property instanceof Entity) {
                Entity<?> entity = (Entity) property;
                entity.setExportObject(true);
                applyVisibility(entity, path, es.getValue(), useAbsoluteNavigationLinks);
            } else if (property instanceof EntitySet) {
                EntitySet entitySet = (EntitySet) property;
                applyVisibility(entitySet, path, es.getValue(), useAbsoluteNavigationLinks);
                entitySet.setExportObject(true);
            }
        }
        e.setSelectedPropertyNames(v.getVisiblePropertyNames());

        CustomLinksHelper.expandCustomLinks(settings, e, path);
    }

    private void applyVisibility(EntitySet<? extends Entity> es, ResourcePath path, Visibility v, boolean useAbsoluteNavigationLinks) {
        for (Entity<?> e : es) {
            applyVisibility(e, path, v, useAbsoluteNavigationLinks);
        }
    }

    private Visibility createVisibility(EntityType entityType, Query query, boolean topLevel) {
        Visibility v = new Visibility();
        Set<Property> properties = getPropertiesForEntityType(entityType);
        v.allProperties.addAll(properties);

        if (query == null || query.getSelect().isEmpty()) {
            if (topLevel) {
                v.visibleProperties.addAll(v.allProperties);
                copyNavigationProperties(v.allProperties, v.navLinkProperties);
            } else {
                copyEntityProperties(v.allProperties, v.visibleProperties);
            }
        }
        if (query == null) {
            return v;
        }
        if (!query.getSelect().isEmpty()) {
            for (Property select : query.getSelect()) {
                v.visibleProperties.add(select);
                if (select instanceof NavigationProperty) {
                    v.navLinkProperties.add((NavigationProperty) select);
                }
            }
        }

        for (Expand expand : query.getExpand()) {
            expandVisibility(v, expand);
        }
        return v;
    }

    private void expandVisibility(Visibility v, Expand expand) {
        NavigationProperty expPath = expand.getPath();
        Visibility level = v;

        Visibility subLevel = createVisibility(expPath.getType(), expand.getSubQuery(), false);
        Visibility existingVis = level.expandVisibility.get(expPath);
        if (existingVis != null) {
            subLevel.merge(existingVis);
        }
        level.expandVisibility.put(expPath, subLevel);
    }

    private Set<Property> getPropertiesForEntityType(EntityType entityType) {
        return enabledProperties.computeIfAbsent(entityType, t -> {
            Set<Property> set = new HashSet<>();
            Set<Extension> enabledExtensions = settings.getEnabledExtensions();
            for (Property property : entityType.getPropertySet()) {
                if (property instanceof NavigationProperty) {
                    NavigationProperty navigationProperty = (NavigationProperty) property;
                    if (enabledExtensions.contains(navigationProperty.getType().extension)) {
                        set.add(property);
                    }
                } else {
                    set.add(property);
                }
            }
            return set;
        });
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

    private static class Visibility {

        private final Set<Property> allProperties = new HashSet<>();
        private final Set<Property> visibleProperties = new HashSet<>();
        private Set<String> visiblePropertyNames;
        private final Set<NavigationProperty> navLinkProperties = new HashSet<>();
        private final Map<NavigationProperty, Visibility> expandVisibility = new HashMap<>();

        public Set<String> getVisiblePropertyNames() {
            if (visiblePropertyNames == null) {
                visiblePropertyNames = visibleProperties
                        .stream()
                        .map(Property::getJsonName)
                        .collect(Collectors.toSet());
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
}
