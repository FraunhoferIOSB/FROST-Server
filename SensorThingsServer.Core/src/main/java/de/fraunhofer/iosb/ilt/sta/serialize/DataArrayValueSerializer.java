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
package de.fraunhofer.iosb.ilt.sta.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import de.fraunhofer.iosb.ilt.sta.formatter.DataArrayResult;
import de.fraunhofer.iosb.ilt.sta.formatter.DataArrayValue;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.ext.EntitySetResult;
import java.io.IOException;

/**
 *
 * @author jab
 */
public class DataArrayValueSerializer extends JsonSerializer<DataArrayValue> {

    @Override
    public void serialize(DataArrayValue value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        gen.writeStartObject();
        Datastream datastream = value.getDatastream();
        if (datastream != null) {
            gen.writeStringField("Datastream@iot.navigationLink", datastream.getNavigationLink());
        }
        gen.writeObjectField("components", value.getComponents());
        int count = value.getDataArray().size();
        if (count >= 0) {
            gen.writeNumberField("dataArray@iot.count", count);
        }
        gen.writeFieldName("dataArray");
        gen.writeObject(value.getDataArray());
        gen.writeEndObject();
    }

    @Override
    public boolean isEmpty(SerializerProvider provider, DataArrayValue value) {
        return (value == null || value.getDataArray().isEmpty());
    }

}
