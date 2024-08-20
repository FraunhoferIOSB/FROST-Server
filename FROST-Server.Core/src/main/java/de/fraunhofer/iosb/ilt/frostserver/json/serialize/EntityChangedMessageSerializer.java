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
package de.fraunhofer.iosb.ilt.frostserver.json.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import java.io.IOException;

/**
 * Handles serialization of EntityChangedMessages. Ensures the entityType is
 * serialised before the entity, to make de-serialisation easier.
 *
 * @author hylke
 */
public class EntityChangedMessageSerializer extends JsonSerializer<EntityChangedMessage> {

    @Override
    public void serialize(EntityChangedMessage message, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        final EntityChangedMessage.Type eventType = message.getEventType();
        if (eventType != null) {
            gen.writeStringField("eventType", eventType.name());
        }
        final EntityType entityType = message.getEntityType();
        if (entityType != null) {
            gen.writeStringField("entityType", entityType.entityName);
        }
        gen.writeObjectField("entity", message.getEntity());
        gen.writeObjectField("epFields", message.getEpFields());
        gen.writeObjectField("npFields", message.getNpFields());
        gen.writeEndObject();
    }
}
