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
package de.fraunhofer.iosb.ilt.frostserver.mcp.resource;

import de.fraunhofer.iosb.ilt.frostserver.mcp.util.StaJsonHelper;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PrimaryKey;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.property.type.PropertyType;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeSimplePrimitive;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The single introspection point for the frost://model resources: walks the
 * live ModelRegistry and generates the model overview and per-entity-type
 * detail documents. Never mentions STA types directly; Edm.* property type
 * names surface verbatim, Edm.Untyped becomes the free-form object
 * annotation.
 */
public class ModelResourceGenerator {

    private static final String UNTYPED_ANNOTATION = "{\"type\": \"object\", \"additionalProperties\": true}";

    private final ModelRegistry modelRegistry;

    public ModelResourceGenerator(ModelRegistry modelRegistry) {
        this.modelRegistry = modelRegistry;
    }

    /**
     * Generates the full model overview: one entry per non-admin entity type
     * with its property and navigation summaries.
     *
     * @return the pretty-printed model JSON.
     */
    public String generateModel() {
        final Map<String, Object> model = new LinkedHashMap<>();
        final List<Map<String, Object>> entities = new ArrayList<>();
        for (final EntityType entityType : modelRegistry.getEntityTypes()) {
            entities.add(generateEntityType(entityType));
        }
        model.put("entities", entities);
        return StaJsonHelper.writeValueAsPrettyString(model);
    }

    /**
     * Generates the detail document for a single entity type.
     *
     * @param entitySet The entity set (plural) name.
     * @return the pretty-printed entity-type JSON, or null when the name is
     * unknown (or admin-only).
     */
    public String generateEntityType(String entitySet) {
        final EntityType entityType = modelRegistry.getEntityTypeForName(entitySet, false);
        return entityType == null ? null : StaJsonHelper.writeValueAsPrettyString(generateEntityType(entityType));
    }

    /**
     * Generates the summary map for one entity type: names, primary key,
     * entity properties and navigation properties.
     *
     * @param entityType The entity type to describe.
     * @return the entity-type descriptor map.
     */
    private Map<String, Object> generateEntityType(EntityType entityType) {
        final Map<String, Object> type = new LinkedHashMap<>();
        type.put("name", entityType.entityName);
        type.put("entitySet", entityType.plural);

        final PrimaryKey primaryKey = entityType.getPrimaryKey();
        if (primaryKey != null && primaryKey.size() == 1) {
            final Property<?> keyProperty = primaryKey.getKeyProperties().get(0);
            type.put("key", keyProperty.getName());
            type.put("keyType", edmName(keyProperty.getType()));
        } else if (primaryKey != null) {
            final List<String> keys = new ArrayList<>();
            primaryKey.getKeyProperties().forEach(property -> keys.add(property.getName()));
            type.put("key", keys);
        }

        final List<Map<String, Object>> properties = new ArrayList<>();
        for (final EntityProperty<?> property : entityType.getEntityProperties()) {
            properties.add(generateProperty(property));
        }
        type.put("properties", properties);

        final List<Map<String, Object>> navigation = new ArrayList<>();
        for (final NavigationProperty property : entityType.getNavigationProperties()) {
            navigation.add(generateNavigationProperty(property));
        }
        type.put("navigationProperties", navigation);
        return type;
    }

    private Map<String, Object> generateProperty(Property<?> property) {
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", property.getName());
        map.put("jsonName", property.getJsonName());
        map.put("type", edmName(property.getType()));
        if (property.getType() != null && TypeSimplePrimitive.EDM_UNTYPED_NAME.equals(property.getType().getName())) {
            map.put("jsonSchema", UNTYPED_ANNOTATION);
        }
        map.put("required", property.isRequired());
        map.put("nullable", property.isNullable());
        map.put("readOnly", property.isReadOnly());
        return map;
    }

    private Map<String, Object> generateNavigationProperty(NavigationProperty property) {
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", property.getName());
        map.put("jsonName", property.getJsonName());
        map.put("target", property.getEntityType().entityName);
        map.put("targetSet", property.getEntityType().plural);
        map.put("isEntitySet", property.isEntitySet());
        final NavigationProperty inverse = property.getInverse();
        map.put("inverse", inverse == null ? null : inverse.getName());
        map.put("required", property.isRequired());
        map.put("nullable", property.isNullable());
        return map;
    }

    private String edmName(PropertyType type) {
        return type == null ? null : type.getName();
    }

}
