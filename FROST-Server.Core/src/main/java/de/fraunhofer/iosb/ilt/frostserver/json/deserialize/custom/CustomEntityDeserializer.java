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
package de.fraunhofer.iosb.ilt.frostserver.json.deserialize.custom;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
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
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class CustomEntityDeserializer extends JsonDeserializer<Entity> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomEntityDeserializer.class.getName());
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

    public CustomEntityDeserializer(ModelRegistry modelRegistry, EntityType entityType) {
        this.modelRegistry = modelRegistry;
        this.entityType = entityType;
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
        Entity target = new DefaultEntity(entityType);

        boolean failOnUnknown = ctxt.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        JsonToken currentToken = parser.nextToken();
        while (currentToken == JsonToken.FIELD_NAME) {
            String fieldName = parser.getCurrentName();
            Property property = entityType.getProperty(fieldName);
            if (property == null) {
                if (failOnUnknown) {
                    final String message = "Unknown field: " + fieldName + " on " + entityType.entityName + " expected one of: " + entityType.getPropertySet();
                    throw new UnrecognizedPropertyException(parser, message, parser.getCurrentLocation(), DefaultEntity.class, fieldName, null);
                } else {
                    parser.nextValue();
                    parser.readValueAsTree();
                }
            } else {
                deserializeProperty(parser, ctxt, property, target);
            }
            currentToken = parser.nextToken();
        }

        return target;
    }

    private void deserializeProperty(JsonParser parser, DeserializationContext ctxt, Property property, Entity target) throws IOException {
        if (property instanceof EntityPropertyMain epm) {
            deserializeEntityProperty(parser, ctxt, epm, target);
        } else if (property instanceof NavigationPropertyEntity npe) {
            deserializeNavigationProperty(parser, ctxt, npe, target);
        } else if (property instanceof NavigationPropertyEntitySet npes) {
            deserializeNavigationProperty(parser, ctxt, npes, target);
        }
    }

    private void deserializeNavigationProperty(JsonParser parser, DeserializationContext ctxt, NavigationPropertyEntitySet npes, Entity result) throws IOException {
        final EntityType setType = npes.getEntityType();
        EntitySet entitySet = new EntitySetImpl(npes);
        CustomEntityDeserializer setEntityDeser = getInstance(modelRegistry, setType);
        result.setProperty(npes, entitySet);
        JsonToken curToken = parser.nextToken();
        if (curToken != JsonToken.START_ARRAY) {
            final String message = "Expected start of array for: " + npes.getName() + " on " + entityType.entityName + BUT_FOUND + curToken;
            throw MismatchedInputException.from(parser, DefaultEntity.class, message);
        }
        curToken = parser.nextToken();
        if (curToken != JsonToken.START_OBJECT && curToken != JsonToken.END_ARRAY) {
            final String message = "Expected object in array for: " + npes.getName() + " on " + entityType.entityName + BUT_FOUND + curToken;
            throw MismatchedInputException.from(parser, DefaultEntity.class, message);
        }
        while (curToken != null && curToken != JsonToken.END_ARRAY) {
            entitySet.add(setEntityDeser.deserialize(parser, ctxt));
            curToken = parser.nextToken();
        }
    }

    private void deserializeNavigationProperty(JsonParser parser, DeserializationContext ctxt, NavigationPropertyEntity npe, Entity target) throws IOException {
        final EntityType targetEntityType = npe.getEntityType();
        JsonToken nextToken = parser.nextToken();
        if (nextToken != JsonToken.START_OBJECT) {
            final String message = "Expected start of object for: " + npe.getName() + " on " + entityType.entityName + BUT_FOUND + nextToken;
            throw MismatchedInputException.from(parser, DefaultEntity.class, message);
        }
        Entity value = getInstance(modelRegistry, targetEntityType)
                .deserialize(parser, ctxt);
        npe.setOn(target, value);
    }

    private void deserializeEntityProperty(JsonParser parser, DeserializationContext ctxt, EntityPropertyMain epm, Entity target) throws IOException {
        parser.nextValue();
        final JsonDeserializer deserializer = epm.getType().getDeserializer();
        if (deserializer == null) {
            LOGGER.error("Missing deserialiser for {}/{}", entityType, epm);
            return;
        }
        Object value = deserializer.deserialize(parser, ctxt);
        epm.setOn(target, value);
    }

}
