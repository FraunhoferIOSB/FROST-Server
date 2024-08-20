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
package de.fraunhofer.iosb.ilt.frostserver.plugin.format.dataarray.json;

import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_COUNT;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_NEXT_LINK;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import de.fraunhofer.iosb.ilt.frostserver.plugin.format.dataarray.DataArrayResult;
import java.io.IOException;

/**
 *
 * @author jab
 */
public class DataArrayResultSerializer extends JsonSerializer<DataArrayResult> {

    @Override
    public void serialize(DataArrayResult value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        long count = value.getCount();
        if (count >= 0) {
            gen.writeNumberField(AT_IOT_COUNT, count);
        }
        String nextLink = value.getNextLink();
        if (nextLink != null) {
            gen.writeStringField(AT_IOT_NEXT_LINK, nextLink);
        }

        gen.writeFieldName("value");
        gen.writeObject(value.getValue());
        gen.writeEndObject();
    }

    @Override
    public boolean isEmpty(SerializerProvider provider, DataArrayResult value) {
        return (value == null || value.getValue() == null || value.getValue().isEmpty());
    }

}
