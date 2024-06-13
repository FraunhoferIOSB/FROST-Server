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
package de.fraunhofer.iosb.ilt.frostserver.plugin.odata.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.EntitySerializer;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles serialization of wrapped Entity objects that add a context property.
 *
 * @author jab
 * @author scf
 */
public class EntityWrapperSerializer extends JsonSerializer<EntityWrapper> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityWrapperSerializer.class.getName());

    private final EntitySerializer innerSerialiser;
    private final String contextField;

    public EntityWrapperSerializer(String contextField, String countField, String navLinkField, String nextLinkField, String selfLinkField) {
        this.contextField = contextField;
        this.innerSerialiser = new EntitySerializer(true, countField, navLinkField, nextLinkField, selfLinkField);
    }

    @Override
    public void serialize(EntityWrapper wrapper, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        try {
            gen.writeStringField(contextField, wrapper.getContext());
            innerSerialiser.writeContent(wrapper.getEntity(), gen);
        } catch (IOException | RuntimeException exc) {
            LOGGER.error("Failed to serialise entity.", exc);
            throw new IOException("could not serialize Entity");
        } finally {
            gen.writeEndObject();
        }
    }

}
