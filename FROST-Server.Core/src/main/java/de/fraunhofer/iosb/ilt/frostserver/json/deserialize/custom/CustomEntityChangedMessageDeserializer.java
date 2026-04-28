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

import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.UnknownEntityTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/**
 * Deserialiser for entity changed messages.
 */
public class CustomEntityChangedMessageDeserializer extends ValueDeserializer<EntityChangedMessage> {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomEntityChangedMessageDeserializer.class);
    private static final String TYPE_NOT_KNOW_YET = "Type not know yet.";

    private final ModelRegistry modelRegistry;
    private final EntityChangedMessage.QueryGenerator queryGenerator = new EntityChangedMessage.QueryGenerator();

    public CustomEntityChangedMessageDeserializer(ModelRegistry modelRegistry) {
        this.modelRegistry = modelRegistry;
    }

    @Override
    public EntityChangedMessage deserialize(JsonParser parser, DeserializationContext ctxt) throws JacksonException {
        EntityChangedMessage message = new EntityChangedMessage();
        JsonToken currentToken = parser.nextToken();
        EntityType type = null;
        Entity entity = null;
        while (currentToken == JsonToken.PROPERTY_NAME) {
            String fieldName = parser.currentName();
            parser.nextToken();
            switch (fieldName) {
                case "eventType":
                    message.setEventType(EntityChangedMessage.Type.valueOf(parser.getValueAsString()));
                    break;

                case "entityType":
                    type = handleEntityType(parser, entity, message);
                    break;

                case "epFields":
                    handleEpFields(parser, type, message);
                    break;

                case "npFields":
                    handleNpFields(parser, type, message);
                    break;

                case "entity":
                    entity = handleEntity(parser, ctxt, type, message);
                    break;

                default:
                    LOGGER.warn("Unknown field in message: {}", fieldName);
                    break;
            }
            currentToken = parser.nextToken();
        }

        if (type == null || entity == null) {
            throw new IllegalArgumentException("Message json with no type or no entity.");
        }
        return message;
    }

    private EntityType handleEntityType(JsonParser parser, Entity entity, EntityChangedMessage message) throws JacksonException {
        final String typeString = parser.getValueAsString();
        EntityType type = modelRegistry.getEntityTypeForName(typeString, true);
        if (type == null) {
            LOGGER.info("Unknown EntityType: {}", typeString);
            throw new UnknownEntityTypeException("Unknown EntityType: " + typeString);
        }
        if (entity != null) {
            entity.setType(type);
            entity.setQuery(queryGenerator.getQueryFor(type));
            message.setEntity(entity);
        }
        return type;
    }

    private void handleEpFields(JsonParser parser, EntityType type, EntityChangedMessage message) throws IllegalArgumentException, JacksonException {
        String fieldName;
        if (type == null) {
            throw new IllegalArgumentException(TYPE_NOT_KNOW_YET);
        }
        if (parser.currentToken() == JsonToken.VALUE_NULL) {
            // No Fields.
            return;
        }
        JsonToken currentToken = parser.nextToken();
        while (currentToken == JsonToken.VALUE_STRING) {
            fieldName = parser.getValueAsString();
            message.addEpField((EntityPropertyMain) type.getProperty(fieldName));
            currentToken = parser.nextToken();
        }
    }

    private void handleNpFields(JsonParser parser, EntityType type, EntityChangedMessage message) throws IllegalArgumentException, JacksonException {
        String fieldName;
        if (type == null) {
            throw new IllegalArgumentException(TYPE_NOT_KNOW_YET);
        }
        if (parser.currentToken() == JsonToken.VALUE_NULL) {
            // No Fields.
            return;
        }
        JsonToken currentToken = parser.nextToken();
        while (currentToken == JsonToken.VALUE_STRING) {
            fieldName = parser.getValueAsString();
            message.addNpField((NavigationPropertyMain) type.getProperty(fieldName));
            currentToken = parser.nextToken();
        }
    }

    private Entity handleEntity(JsonParser parser, DeserializationContext ctxt, EntityType type, EntityChangedMessage message) throws JacksonException, IllegalArgumentException {
        if (type == null) {
            throw new IllegalArgumentException(TYPE_NOT_KNOW_YET);
        }
        Entity entity = CustomEntityDeserializer.getInstance(modelRegistry, type)
                .deserialize(parser, ctxt);
        entity.setQuery(queryGenerator.getQueryFor(type));
        message.setEntity(entity);
        return entity;
    }

}
