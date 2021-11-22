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
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_COUNT;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_NEXT_LINK;
import java.io.IOException;

/**
 *
 * @author jab
 */
public class EntitySetResultOdataSerializer extends JsonSerializer<EntitySetResultOdata> {

    @Override
    public void serialize(EntitySetResultOdata value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField(JsonWriterOdata.AT_CONTEXT, value.getContext());

        long count = value.getValues().getCount();
        if (count >= 0) {
            gen.writeNumberField(AT_IOT_COUNT, count);
        }

        gen.writeArrayFieldStart("value");
        for (Entity child : value.getValues()) {
            gen.writeObject(child);
        }
        gen.writeEndArray();

        String nextLink = value.getValues().getNextLink();
        if (nextLink != null) {
            gen.writeStringField(AT_IOT_NEXT_LINK, nextLink);
        }
    }

    @Override
    public boolean isEmpty(SerializerProvider provider, EntitySetResultOdata value) {
        return (value == null || value.getValues() == null);
    }

}
