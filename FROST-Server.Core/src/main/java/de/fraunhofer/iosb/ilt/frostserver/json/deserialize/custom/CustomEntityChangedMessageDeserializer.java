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
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.Observation;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationProperty;
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
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        JsonNode obj = mapper.readTree(parser);
        Iterator<Map.Entry<String, JsonNode>> i = obj.fields();
        EntityType type = null;
        JsonNode entityJson = null;
        while (i.hasNext()) {
            Map.Entry<String, JsonNode> next = i.next();
            String name = next.getKey();
            JsonNode value = next.getValue();
            switch (name.toLowerCase()) {
                case "eventtype":
                    message.setEventType(EntityChangedMessage.Type.valueOf(value.asText()));
                    break;

                case "entitytype":
                    type = EntityType.valueOf(value.asText());
                    break;

                case "epfields":
                    for (JsonNode field : value) {
                        String fieldName = field.asText();
                        message.addEpField(EntityProperty.valueOf(fieldName));
                    }
                    break;

                case "npfields":
                    for (JsonNode field : value) {
                        String fieldName = field.asText();
                        message.addNpField(NavigationProperty.valueOf(fieldName));
                    }
                    break;

                case "entity":
                    entityJson = value;
                    break;

                default:
                    LOGGER.warn("Unknown field in message: {}", name);
                    break;
            }
        }
        if (type == null || entityJson == null) {
            throw new IllegalArgumentException("Message json with no type or no entity.");
        }
        message.setEntity(parseEntity(mapper, entityJson, type));
        return message;
    }

    private static Entity parseEntity(ObjectMapper mapper, JsonNode entityJson, EntityType entityType) {
        Entity entity = mapper.convertValue(entityJson, entityType.getImplementingClass());
        if (entity instanceof Observation) {
            Observation observation = (Observation) entity;
            if (observation.getResultTime() == null) {
                observation.setResultTime(new TimeInstant(null));
            }
        }
        for (NavigationProperty property : entityType.getNavigationEntities()) {
            Object parentObject = entity.getProperty(property);
            if (parentObject instanceof Entity) {
                Entity parentEntity = (Entity) parentObject;
                parentEntity.setExportObject(false);
            }
        }
        return entity;
    }
}
