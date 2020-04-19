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
import de.fraunhofer.iosb.ilt.frostserver.model.Observation;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.NamedEntity;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.Settings;
import java.util.ArrayList;
import java.util.Arrays;
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

    private static final String ENTITY_TYPE_REGEX = StringUtils.join(
            Arrays.asList(EntityType.values())
                    .stream()
                    .map(type -> type.entityName)
                    .collect(Collectors.toList()),
            '|');

    public static final Pattern ENTITY_LINK_NAME_PATTERN = Pattern.compile("([a-zA-Z0-9._-]+)\\.(" + ENTITY_TYPE_REGEX + ")@iot\\.id");

    private CustomLinksHelper() {
        // Utility class
    }

    public static void expandCustomLinks(CoreSettings settings, Entity<?> e, ResourcePath path) {
        final Settings experimentalSettings = settings.getExperimentalSettings();
        if (experimentalSettings.getBoolean(CoreSettings.TAG_ENABLE_CUSTOM_LINKS, CoreSettings.class)) {
            int recurseDepth = experimentalSettings.getInt(CoreSettings.TAG_CUSTOM_LINKS_RECURSE_DEPTH, CoreSettings.class);
            if (e instanceof NamedEntity) {
                CustomLinksHelper.expandCustomLinks(((NamedEntity) e).getProperties(), path, recurseDepth);
            } else if (e instanceof Observation) {
                CustomLinksHelper.expandCustomLinks(((Observation) e).getParameters(), path, recurseDepth);
            }
        }
    }

    public static void expandCustomLinks(Map<String, Object> properties, ResourcePath path, int recurseDepth) {
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
                Matcher matcher = ENTITY_LINK_NAME_PATTERN.matcher(key);
                if (matcher.matches()) {
                    String name = matcher.group(1);
                    EntityType type = EntityType.getEntityTypeForName(matcher.group(2));
                    Object id = propertyEntry.getValue();
                    String navLinkName = name + "." + type.entityName + "@Iot.Navigationlink";
                    toAdd.put(navLinkName, UrlHelper.generateSelfLink(path.getServiceRootUrl(), type, id));
                }
            }
        }
        properties.putAll(toAdd);
    }

    public static void cleanPropertiesMap(CoreSettings settings, Entity<?> entity) {
        final Settings experimentalSettings = settings.getExperimentalSettings();
        if (!experimentalSettings.getBoolean(CoreSettings.TAG_ENABLE_CUSTOM_LINKS, CoreSettings.class)) {
            return;
        }
        int recurseDepth = experimentalSettings.getInt(CoreSettings.TAG_CUSTOM_LINKS_RECURSE_DEPTH, CoreSettings.class);
        if (entity instanceof NamedEntity) {
            cleanPropertiesMap(((NamedEntity) entity).getProperties(), recurseDepth);
        } else if (entity instanceof Observation) {
            cleanPropertiesMap(((Observation) entity).getParameters(), recurseDepth);
        }
    }

    public static void cleanPropertiesMap(Map<String, Object> properties, int recurseDepth) {
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
                Matcher matcher = ENTITY_LINK_NAME_PATTERN.matcher(key);
                if (matcher.matches()) {
                    String name = matcher.group(1);
                    EntityType type = EntityType.getEntityTypeForName(matcher.group(2));
                    String itemName = name + "." + type.entityName;
                    String navLinkName = itemName + "@Iot.Navigationlink";
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
