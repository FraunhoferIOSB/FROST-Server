/*
 * Copyright (C) 2018 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
import com.fasterxml.jackson.databind.JsonDeserializer;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class CustomEntityChangedMessageDeserializer extends JsonDeserializer<EntityChangedMessage> {

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
    public EntityChangedMessage deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        EntityChangedMessage message = new EntityChangedMessage();
        JsonToken currentToken = parser.nextToken();
        EntityType type = null;
        Entity entity = null;
        while (currentToken == JsonToken.FIELD_NAME) {
            String fieldName = parser.getCurrentName();
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

    private EntityType handleEntityType(JsonParser parser, Entity entity, EntityChangedMessage message) throws IOException {
        final String typeString = parser.getValueAsString();
        EntityType type = modelRegistry.getEntityTypeForName(typeString);
        if (type == null) {
            throw new IllegalArgumentException("Unknown EntityType: " + typeString);
        }
        if (entity != null) {
            entity.setEntityType(type);
            entity.setQuery(queryGenerator.getQueryFor(type));
            message.setEntity(entity);
        }
        return type;
    }

    private void handleEpFields(JsonParser parser, EntityType type, EntityChangedMessage message) throws IllegalArgumentException, IOException {
        String fieldName;
        if (type == null) {
            throw new IllegalArgumentException(TYPE_NOT_KNOW_YET);
        }
        JsonToken currentToken = parser.nextToken();
        while (currentToken == JsonToken.VALUE_STRING) {
            fieldName = parser.getValueAsString();
            message.addEpField((EntityPropertyMain) type.getProperty(fieldName));
            currentToken = parser.nextToken();
        }
    }

    private void handleNpFields(JsonParser parser, EntityType type, EntityChangedMessage message) throws IllegalArgumentException, IOException {
        String fieldName;
        if (type == null) {
            throw new IllegalArgumentException(TYPE_NOT_KNOW_YET);
        }
        JsonToken currentToken = parser.nextToken();
        while (currentToken == JsonToken.VALUE_STRING) {
            fieldName = parser.getValueAsString();
            message.addNpField((NavigationPropertyMain) type.getProperty(fieldName));
            currentToken = parser.nextToken();
        }
    }

    private Entity handleEntity(JsonParser parser, DeserializationContext ctxt, EntityType type, EntityChangedMessage message) throws IOException, IllegalArgumentException {
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
