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
package de.fraunhofer.iosb.ilt.frostserver.json.deserialize.custom;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import de.fraunhofer.iosb.ilt.frostserver.model.DefaultEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.util.ParserUtils;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author scf
 */
public class CustomEntityDeserializer extends JsonDeserializer<Entity> {

    private static final String BUT_FOUND = " but found: ";

    private static final Map<ModelRegistry, Map<EntityType, CustomEntityDeserializer>> instancePerModelAndType = new HashMap<>();

    public static CustomEntityDeserializer getInstance(final ModelRegistry modelRegistry, final EntityType entityType) {
        return instancePerModelAndType
                .computeIfAbsent(
                        modelRegistry,
                        t -> new HashMap<>())
                .computeIfAbsent(
                        entityType,
                        t -> new CustomEntityDeserializer(modelRegistry, t));
    }

    private final EntityType entityType;
    private final ModelRegistry modelRegistry;
    private final Map<String, PropertyData> propertyByName = new HashMap<>();

    public CustomEntityDeserializer(ModelRegistry modelRegistry, EntityType entityType) {
        this.modelRegistry = modelRegistry;
        this.entityType = entityType;
        final Set<Property> propertySet;
        propertySet = entityType.getPropertySet();

        for (Property property : propertySet) {
            if (property instanceof EntityPropertyMain<?> epm) {
                final PropertyData propertyData = new PropertyData(
                        property,
                        property.getType().getTypeReference(),
                        false,
                        null);
                for (String alias : epm.getAliases()) {
                    propertyByName.put(alias, propertyData);
                }
            } else if (property instanceof NavigationPropertyMain npm) {
                propertyByName.put(
                        property.getJsonName(),
                        new PropertyData(
                                property,
                                null,
                                npm.isEntitySet(),
                                npm.getEntityType()));
            }
        }
    }

    /**
     * Deserialises an Entity, consuming the Object start and end tokens.
     *
     * @param parser The parser to fetch tokens from.
     * @param ctxt The context to fetch settings from.
     * @return The deserialised Entity.
     * @throws IOException If deserialisation fails.
     */
    public Entity deserializeFull(JsonParser parser, DeserializationContext ctxt) throws IOException {
        parser.nextToken();
        Entity result = deserialize(parser, ctxt);
        parser.nextToken();
        return result;
    }

    @Override
    public Entity deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        Entity result = new DefaultEntity(entityType);

        boolean failOnUnknown = ctxt.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        JsonToken currentToken = parser.nextToken();
        while (currentToken == JsonToken.FIELD_NAME) {
            String fieldName = parser.getCurrentName();
            PropertyData propertyData = propertyByName.get(fieldName);
            if (propertyData == null) {
                if (failOnUnknown) {
                    final String message = "Unknown field: " + fieldName + " on " + entityType.entityName + " expected one of: " + propertyByName.keySet();
                    throw new UnrecognizedPropertyException(parser, message, parser.getCurrentLocation(), DefaultEntity.class, fieldName, null);
                } else {
                    parser.nextValue();
                    parser.readValueAsTree();
                }
            } else {
                deserializeProperty(parser, ctxt, result, propertyData);
            }
            currentToken = parser.nextToken();
        }

        return result;
    }

    private void deserializeProperty(JsonParser parser, DeserializationContext ctxt, Entity result, PropertyData propertyData) throws IOException {
        if (propertyData.property instanceof EntityPropertyMain) {
            deserializeEntityProperty(parser, propertyData, result);
        } else if (propertyData.property instanceof NavigationPropertyMain) {
            deserializeNavigationProperty(propertyData, result, parser, ctxt);
        }
    }

    private void deserializeNavigationProperty(PropertyData propertyData, Entity result, JsonParser parser, DeserializationContext ctxt) throws IOException {
        NavigationPropertyMain navPropertyMain = (NavigationPropertyMain) propertyData.property;
        if (propertyData.isEntitySet) {
            deserialiseEntitySet(parser, ctxt, (NavigationPropertyEntitySet) navPropertyMain, result);
        } else {
            final EntityType targetEntityType = navPropertyMain.getEntityType();
            JsonToken nextToken = parser.nextToken();
            if (nextToken != JsonToken.START_OBJECT) {
                final String message = "Expected start of object for: " + propertyData.property.getName() + " on " + entityType.entityName + BUT_FOUND + nextToken;
                throw MismatchedInputException.from(parser, DefaultEntity.class, message);
            }
            Object value = getInstance(modelRegistry, targetEntityType)
                    .deserialize(parser, ctxt);
            navPropertyMain.setOn(result, value);
        }
    }

    private void deserializeEntityProperty(JsonParser parser, PropertyData propertyData, Entity result) throws IOException {
        parser.nextValue();
        EntityPropertyMain entityPropertyMain = (EntityPropertyMain) propertyData.property;
        if (propertyData.valueTypeRef == null) {
            TreeNode value = parser.readValueAsTree();
            entityPropertyMain.setOn(result, value);
        } else if (propertyData.property == entityType.getPrimaryKey()) {
            Object value = parser.readValueAs(propertyData.valueTypeRef);
            entityPropertyMain.setOn(result, ParserUtils.idFromObject(value));
        } else {
            Object value = parser.readValueAs(propertyData.valueTypeRef);
            entityPropertyMain.setOn(result, value);
        }
    }

    private void deserialiseEntitySet(JsonParser parser, DeserializationContext ctxt, NavigationPropertyEntitySet navPropertyMain, Entity result) throws IOException {
        final EntityType setType = navPropertyMain.getEntityType();
        EntitySet entitySet = new EntitySetImpl(navPropertyMain);
        CustomEntityDeserializer setEntityDeser = getInstance(modelRegistry, setType);
        result.setProperty(navPropertyMain, entitySet);
        JsonToken curToken = parser.nextToken();
        if (curToken != JsonToken.START_ARRAY) {
            final String message = "Expected start of array for: " + navPropertyMain.getName() + " on " + entityType.entityName + BUT_FOUND + curToken;
            throw MismatchedInputException.from(parser, DefaultEntity.class, message);
        }
        curToken = parser.nextToken();
        if (curToken != JsonToken.START_OBJECT && curToken != JsonToken.END_ARRAY) {
            final String message = "Expected object in array for: " + navPropertyMain.getName() + " on " + entityType.entityName + BUT_FOUND + curToken;
            throw MismatchedInputException.from(parser, DefaultEntity.class, message);
        }
        while (curToken != null && curToken != JsonToken.END_ARRAY) {
            entitySet.add(setEntityDeser.deserialize(parser, ctxt));
            curToken = parser.nextToken();
        }
    }

    private static class PropertyData {

        final Property property;
        final TypeReference valueTypeRef;
        final boolean isEntitySet;
        final EntityType setType;

        public PropertyData(Property property, TypeReference valueTypeRef, boolean isEntitySet, EntityType setType) {
            this.property = property;
            this.valueTypeRef = valueTypeRef;
            this.isEntitySet = isEntitySet;
            this.setType = setType;
        }

    }
}
