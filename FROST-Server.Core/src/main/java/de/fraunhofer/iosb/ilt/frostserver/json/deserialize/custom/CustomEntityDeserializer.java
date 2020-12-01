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
package de.fraunhofer.iosb.ilt.frostserver.json.deserialize.custom;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import de.fraunhofer.iosb.ilt.frostserver.model.DefaultEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author scf
 */
public class CustomEntityDeserializer extends JsonDeserializer<Entity> {

    private static final Map<ModelRegistry, Map<EntityType, CustomEntityDeserializer>> instancePerModelAndType = new HashMap<>();

    public static CustomEntityDeserializer getInstance(final ModelRegistry modelRegistry, final EntityType entityType) {
        return instancePerModelAndType.computeIfAbsent(
                modelRegistry,
                t -> new HashMap<>()
        ).computeIfAbsent(
                entityType,
                t -> new CustomEntityDeserializer(modelRegistry, t)
        );
    }

    private final EntityType entityType;
    private final ModelRegistry modelRegistry;
    private final Map<String, PropertyData> propertyByName = new HashMap<>();

    public CustomEntityDeserializer(ModelRegistry modelRegistry, EntityType entityType) {
        this.modelRegistry = modelRegistry;
        this.entityType = entityType;
        final Set<Property> propertySet;
        if (entityType == null) {
            propertySet = new HashSet<>();
            propertySet.addAll(modelRegistry.getEntityProperties());
            propertySet.addAll(modelRegistry.getNavProperties());
        } else {
            propertySet = entityType.getPropertySet();
        }

        for (Property property : propertySet) {
            if (property instanceof EntityPropertyMain) {
                propertyByName.put(
                        property.getJsonName(),
                        new PropertyData(
                                property,
                                property.getType(),
                                false,
                                null));
            } else if (property instanceof NavigationPropertyMain) {
                NavigationPropertyMain np = (NavigationPropertyMain) property;
                propertyByName.put(
                        property.getJsonName(),
                        new PropertyData(
                                property,
                                null,
                                np.isEntitySet(),
                                np.getEntityType()));
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

        DelayedField delayedField = null;
        JsonToken currentToken = parser.nextToken();
        while (currentToken == JsonToken.FIELD_NAME) {
            String fieldName = parser.getCurrentName();
            parser.nextValue();
            PropertyData propertyData = propertyByName.get(fieldName);
            if (propertyData == null) {
                if (failOnUnknown) {
                    throw new UnrecognizedPropertyException(parser, "Unknown field: " + fieldName, parser.getCurrentLocation(), DefaultEntity.class, fieldName, null);
                } else {
                    parser.readValueAsTree();
                }
            } else {
                delayedField = deserializeProperty(parser, ctxt, result, propertyData, delayedField);
            }
            currentToken = parser.nextToken();
        }

        if (delayedField != null) {
            EntityPropertyMain entityPropertyMain = delayedField.entityPropertyMain;
            Object encodingType = result.getProperty(ModelRegistry.EP_ENCODINGTYPE);
            if (encodingType == null) {
                entityPropertyMain.setOn(result, delayedField.tempValue);
            } else {
                CustomDeserializer deserializer = CustomDeserializationManager.getInstance().getDeserializer(encodingType.toString());
                Object value = deserializer.deserialize(delayedField.tempValue);
                entityPropertyMain.setOn(result, value);
            }
        }
        return result;
    }

    private DelayedField deserializeProperty(JsonParser parser, DeserializationContext ctxt, Entity result, PropertyData propertyData, DelayedField delayedField) throws IOException {
        if (propertyData.property instanceof EntityPropertyMain) {
            delayedField = deserializeEntityProperty(parser, ctxt, propertyData, result, delayedField);
        } else if (propertyData.property instanceof NavigationPropertyMain) {
            deserializeNavigationProperty(propertyData, result, parser, ctxt);
        }
        return delayedField;
    }

    private void deserializeNavigationProperty(PropertyData propertyData, Entity result, JsonParser parser, DeserializationContext ctxt) throws IOException {
        NavigationPropertyMain navPropertyMain = (NavigationPropertyMain) propertyData.property;
        if (propertyData.isEntitySet) {
            deserialiseEntitySet(parser, ctxt, navPropertyMain, result, propertyData);
        } else {
            final EntityType targetEntityType = navPropertyMain.getEntityType();
            Object value = getInstance(modelRegistry, targetEntityType)
                    .deserialize(parser, ctxt);
            navPropertyMain.setOn(result, value);
        }
    }

    private DelayedField deserializeEntityProperty(JsonParser parser, DeserializationContext ctxt, PropertyData propertyData, Entity result, DelayedField delayedField) throws IOException {
        EntityPropertyMain entityPropertyMain = (EntityPropertyMain) propertyData.property;
        if (propertyData.valueTypeRef == null) {
            Object encodingType = ModelRegistry.EP_ENCODINGTYPE.getFrom(result);
            if (encodingType == null) {
                delayedField = new DelayedField(entityPropertyMain, parser.readValueAsTree());
            } else {
                CustomDeserializer deserializer = CustomDeserializationManager.getInstance().getDeserializer(encodingType.toString());
                Object value = deserializer.deserialize(parser, ctxt);
                entityPropertyMain.setOn(result, value);
            }
        } else {
            Object value = parser.readValueAs(propertyData.valueTypeRef);
            entityPropertyMain.setOn(result, value);
        }
        return delayedField;
    }

    private void deserialiseEntitySet(JsonParser parser, DeserializationContext ctxt, NavigationPropertyMain navPropertyMain, Entity result, PropertyData propertyData) throws IOException {
        final EntityType setType = navPropertyMain.getEntityType();
        EntitySet entitySet = new EntitySetImpl(setType);
        CustomEntityDeserializer setEntityDeser = getInstance(modelRegistry, setType);
        result.setProperty(navPropertyMain, entitySet);
        JsonToken curToken = parser.nextToken();
        while (curToken != null && curToken != JsonToken.END_ARRAY) {
            entitySet.add(setEntityDeser.deserialize(parser, ctxt));
            curToken = parser.nextToken();
        }
    }

    private static class DelayedField {

        public final EntityPropertyMain entityPropertyMain;
        public final TreeNode tempValue;

        public DelayedField(EntityPropertyMain entityPropertyMain, TreeNode tempValue) {
            this.entityPropertyMain = entityPropertyMain;
            this.tempValue = tempValue;
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
