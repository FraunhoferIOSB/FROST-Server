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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.frostserver.json.deserialize.JsonReader;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
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

    @Override
    public EntityChangedMessage deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        EntityChangedMessage message = new EntityChangedMessage();
        JsonToken currentToken = parser.nextToken();
        EntityType type = null;
        Entity entity = null;
        while (currentToken == JsonToken.FIELD_NAME) {
            String fieldName = parser.getCurrentName();
            currentToken = parser.nextToken();
            switch (fieldName) {
                case "eventType":
                    message.setEventType(EntityChangedMessage.Type.valueOf(parser.getValueAsString()));
                    break;

                case "entityType":
                    type = EntityType.getEntityTypeForName(parser.getValueAsString());
                    if (entity != null) {
                        entity.setEntityType(type);
                        message.setEntity(entity);
                    }
                    break;

                case "epFields":
                    currentToken = parser.nextToken();
                    while (currentToken == JsonToken.VALUE_STRING) {
                        fieldName = parser.getValueAsString();
                        message.addEpField(EntityPropertyMain.valueOf(fieldName));
                        currentToken = parser.nextToken();
                    }
                    break;

                case "npFields":
                    currentToken = parser.nextToken();
                    while (currentToken == JsonToken.VALUE_STRING) {
                        fieldName = parser.getValueAsString();
                        message.addNpField(NavigationPropertyMain.valueOf(fieldName));
                        currentToken = parser.nextToken();
                    }
                    break;

                case "entity":
                    entity = CustomEntityDeserializer.getInstance(type).deserialize(parser, ctxt);
                    if (type != null) {
                        message.setEntity(entity);
                    }
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

}
