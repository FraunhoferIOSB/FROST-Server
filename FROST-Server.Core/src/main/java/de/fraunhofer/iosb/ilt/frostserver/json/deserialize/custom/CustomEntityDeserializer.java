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
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.JsonReader;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 * @param <T> The type of the entity to deserialize.
 */
public class CustomEntityDeserializer<T extends Entity<T>> extends JsonDeserializer<T> {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(JsonReader.class);
    private final Class<T> clazz;
    private final EntityType entityType;
    private final Map<String, PropertyData> propertyByName = new HashMap<>();

    public CustomEntityDeserializer(Class<T> clazz) {
        this.clazz = clazz;
        entityType = EntityType.getEntityTypeForClass(clazz);
        for (Property property : entityType.getPropertySet()) {
            if (property instanceof EntityPropertyMain) {
                propertyByName.put(
                        property.getJsonName(),
                        new PropertyData(
                                property,
                                entityType.getPropertyType((EntityPropertyMain) property),
                                false,
                                null));
            } else if (property instanceof NavigationPropertyMain) {
                NavigationPropertyMain np = (NavigationPropertyMain) property;
                propertyByName.put(
                        property.getJsonName(),
                        new PropertyData(
                                property,
                                np.getType().getImplementingTypeRef(),
                                np.isEntitySet(),
                                np.getType()));
            }
        }
    }

    @Override
    public T deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        T result;
        try {
            result = clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException ex) {
            throw new IOException("Error deserializing JSON!", ex);
        }
        boolean failOnUnknown = ctxt.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        DelayedField delayedField = null;
        JsonToken currentToken = parser.nextToken();
        while (currentToken != JsonToken.END_OBJECT) {
            String fieldName = parser.getCurrentName();
            parser.nextValue();
            PropertyData propertyData = propertyByName.get(fieldName);
            if (propertyData == null) {
                if (failOnUnknown) {
                    throw new UnrecognizedPropertyException(parser, "Unknown field: " + fieldName, parser.getCurrentLocation(), clazz, fieldName, null);
                } else {
                    parser.readValueAsTree();
                }
            } else {
                if (propertyData.property instanceof EntityPropertyMain) {
                    EntityPropertyMain entityPropertyMain = (EntityPropertyMain) propertyData.property;
                    if (propertyData.valueTypeRef == null) {
                        Object encodingType = entityPropertyMain.ENCODINGTYPE.getFrom(result);
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
                } else if (propertyData.property instanceof NavigationPropertyMain) {
                    NavigationPropertyMain navPropertyMain = (NavigationPropertyMain) propertyData.property;
                    if (propertyData.isEntitySet) {
                        deserialiseEntitySet(navPropertyMain, result, parser, propertyData);
                    } else {
                        Object value = parser.readValueAs(propertyData.valueTypeRef);
                        navPropertyMain.setOn(result, value);
                    }
                }
            }
            currentToken = parser.nextToken();
        }

        if (delayedField != null) {
            EntityPropertyMain entityPropertyMain = delayedField.entityPropertyMain;
            Object encodingType = entityPropertyMain.ENCODINGTYPE.getFrom(result);
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

    private void deserialiseEntitySet(NavigationPropertyMain navPropertyMain, T result, JsonParser parser, PropertyData propertyData) throws IOException {
        EntitySet entitySet = (EntitySet) navPropertyMain.getFrom(result);
        parser.nextToken();
        Iterator valueIter = parser.readValuesAs(propertyData.valueTypeRef);
        while (valueIter.hasNext()) {
            Object entity = valueIter.next();
            entitySet.add(entity);
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
