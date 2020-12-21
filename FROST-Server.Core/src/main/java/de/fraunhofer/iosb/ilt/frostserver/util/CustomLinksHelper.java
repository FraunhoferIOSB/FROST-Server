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
package de.fraunhofer.iosb.ilt.frostserver.util;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.UrlHelper;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_NAVIGATION_LINK;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author hylke
 */
public class CustomLinksHelper {

    private final Pattern entityLinkNamePattern;

    private final ModelRegistry modelRegistry;

    public CustomLinksHelper(ModelRegistry modelRegistry) {
        this.modelRegistry = modelRegistry;
        String entityTypeRegex = StringUtils.join(
                modelRegistry.getEntityTypes()
                        .stream()
                        .map(type -> type.entityName)
                        .collect(Collectors.toList()),
                '|');
        entityLinkNamePattern = Pattern.compile("([a-zA-Z0-9._-]+)\\.(" + entityTypeRegex + ")@iot\\.id");
    }

    /**
     * Takes a string in the for of "[linkName].[EntityTypeName]" and returns
     * the EntityType, or null if the string is not in the required form.
     *
     * @param name The name to check.
     * @return The EntityType of the link.
     */
    public EntityType getTypeForCustomLinkName(String name) {
        String[] split = StringUtils.split(name, '.');
        if (split.length == 1) {
            return null;
        }
        String last = split[split.length - 1];
        return modelRegistry.getEntityTypeForName(last);
    }

    public void expandCustomLinks(CoreSettings settings, Entity entity, ResourcePath path) {
        final Settings experimentalSettings = settings.getExtensionSettings();
        if (experimentalSettings.getBoolean(CoreSettings.TAG_CUSTOM_LINKS_ENABLE, CoreSettings.class)) {
            int recurseDepth = experimentalSettings.getInt(CoreSettings.TAG_CUSTOM_LINKS_RECURSE_DEPTH, CoreSettings.class);
            for (EntityPropertyMain property : modelRegistry.getEntityPropertiesFreeMap()) {
                final Object properties = entity.getProperty(property);
                if (properties instanceof Map) {
                    expandCustomLinks((Map<String, Object>) properties, path, recurseDepth);
                }
            }
        }
    }

    public void expandCustomLinks(Map<String, Object> properties, ResourcePath path, int recurseDepth) {
        if (properties == null) {
            return;
        }
        Map<String, Object> toAdd = new LinkedHashMap<>();
        for (Map.Entry<String, Object> propertyEntry : properties.entrySet()) {
            Object value = propertyEntry.getValue();
            if (value instanceof Map) {
                Map<String, Object> subMap = (Map<String, Object>) value;
                if (recurseDepth > 0) {
                    expandCustomLinks(subMap, path, recurseDepth - 1);
                }
            } else if (value instanceof Number || value instanceof String) {
                String key = propertyEntry.getKey();
                Matcher matcher = entityLinkNamePattern.matcher(key);
                if (matcher.matches()) {
                    String name = matcher.group(1);
                    EntityType type = modelRegistry.getEntityTypeForName(matcher.group(2));
                    Object id = propertyEntry.getValue();
                    String navLinkName = name + "." + type.entityName + AT_IOT_NAVIGATION_LINK;
                    toAdd.put(navLinkName, UrlHelper.generateSelfLink(path.getServiceRootUrl(), path.getVersion(), type, id));
                }
            }
        }
        properties.putAll(toAdd);
    }

    public void cleanPropertiesMap(CoreSettings settings, Entity entity) {
        final Settings experimentalSettings = settings.getExtensionSettings();
        if (!experimentalSettings.getBoolean(CoreSettings.TAG_CUSTOM_LINKS_ENABLE, CoreSettings.class)) {
            return;
        }
        int recurseDepth = experimentalSettings.getInt(CoreSettings.TAG_CUSTOM_LINKS_RECURSE_DEPTH, CoreSettings.class);
        for (EntityPropertyMain property : modelRegistry.getEntityPropertiesFreeMap()) {
            final Object properties = entity.getProperty(property);
            if (properties instanceof Map) {
                cleanPropertiesMap((Map<String, Object>) properties, recurseDepth);
            }
        }
    }

    public void cleanPropertiesMap(Map<String, Object> properties, int recurseDepth) {
        if (properties == null) {
            return;
        }
        List<String> toRemove = new ArrayList<>();
        for (Map.Entry<String, Object> propertyEntry : properties.entrySet()) {
            Object value = propertyEntry.getValue();
            if (value instanceof Map) {
                Map<String, Object> subMap = (Map<String, Object>) value;
                if (recurseDepth > 0) {
                    cleanPropertiesMap(subMap, recurseDepth - 1);
                }
            } else if (value instanceof Number || value instanceof String) {
                String key = propertyEntry.getKey();
                Matcher matcher = entityLinkNamePattern.matcher(key);
                if (matcher.matches()) {
                    String name = matcher.group(1);
                    EntityType type = modelRegistry.getEntityTypeForName(matcher.group(2));
                    String itemName = name + "." + type.entityName;
                    String navLinkName = itemName + AT_IOT_NAVIGATION_LINK;
                    toRemove.add(itemName);
                    toRemove.add(navLinkName);
                }
            }
        }
        for (String key : toRemove) {
            properties.remove(key);
        }
    }
}
