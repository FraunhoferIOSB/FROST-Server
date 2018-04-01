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
package de.fraunhofer.iosb.ilt.sta.json.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import de.fraunhofer.iosb.ilt.sta.model.ext.EntitySetResult;
import java.io.IOException;

/**
 *
 * @author jab
 */
public class EntitySetResultSerializer extends JsonSerializer<EntitySetResult> {

    @Override
    public void serialize(EntitySetResult value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        gen.writeStartObject();
        long count = value.getValues().getCount();
        if (count >= 0) {
            gen.writeNumberField("@iot.count", count);
        }
        String nextLink = value.getValues().getNextLink();
        if (nextLink != null) {
            gen.writeStringField("@iot.nextLink", nextLink);
        }

        // TODO begin/end array, iterate over content
        gen.writeFieldName("value");
        gen.writeObject(value.getValues());
        gen.writeEndObject();
    }

    @Override
    public boolean isEmpty(SerializerProvider provider, EntitySetResult value) {
        return (value == null || value.getValues() == null);
    }

}
