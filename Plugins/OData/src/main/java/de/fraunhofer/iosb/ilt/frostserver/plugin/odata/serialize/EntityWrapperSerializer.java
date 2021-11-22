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
package de.fraunhofer.iosb.ilt.frostserver.plugin.odata.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import de.fraunhofer.iosb.ilt.frostserver.json.serialize.EntitySerializer;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_NAVIGATION_LINK;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_SELF_LINK;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles serialization of Entity objects. If a field is of type Entity and
 * contains a non-empty navigationLink the field will be renamed with the suffix
 * '@iot.navigationLink' and will only contain the navigationLink as String.
 *
 * @author jab
 * @author scf
 */
public class EntityWrapperSerializer extends JsonSerializer<EntityWrapper> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityWrapperSerializer.class.getName());

    private EntitySerializer innerSerialiser;

    public EntityWrapperSerializer() {
        this(AT_IOT_NAVIGATION_LINK, AT_IOT_SELF_LINK);
    }

    public EntityWrapperSerializer(String navLinkPostFix, String selfLinkField) {
        this.innerSerialiser = new EntitySerializer(navLinkPostFix, selfLinkField);
    }

    @Override
    public void serialize(EntityWrapper wrapper, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        try {
            gen.writeStringField(JsonWriterOdata.AT_CONTEXT, wrapper.getContext());
            innerSerialiser.writeContent(wrapper.getEntity(), gen);
        } catch (IOException | RuntimeException exc) {
            LOGGER.error("Failed to serialise entity.", exc);
            throw new IOException("could not serialize Entity");
        } finally {
            gen.writeEndObject();
        }
    }

}
