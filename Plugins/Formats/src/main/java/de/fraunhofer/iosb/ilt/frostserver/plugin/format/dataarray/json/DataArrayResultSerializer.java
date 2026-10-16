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

import de.fraunhofer.iosb.ilt.frostserver.plugin.format.dataarray.DataArrayResult;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 *
 */
public class DataArrayResultSerializer extends ValueSerializer<DataArrayResult> {

    @Override
    public void serialize(DataArrayResult value, JsonGenerator gen, SerializationContext ctx) throws JacksonException {
        gen.writeStartObject();
        long count = value.getCount();
        if (count >= 0) {
            gen.writeNumberProperty(AT_IOT_COUNT, count);
        }
        String nextLink = value.getNextLink();
        if (nextLink != null) {
            gen.writeStringProperty(AT_IOT_NEXT_LINK, nextLink);
        }

        gen.writePOJOProperty("value", value.getValue());
        gen.writeEndObject();
    }

    @Override
    public boolean isEmpty(SerializationContext ctx, DataArrayResult value) {
        return (value == null || value.getValue() == null || value.getValue().isEmpty());
    }

}
