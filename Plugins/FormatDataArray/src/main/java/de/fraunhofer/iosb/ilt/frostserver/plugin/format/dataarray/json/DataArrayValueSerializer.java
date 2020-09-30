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
package de.fraunhofer.iosb.ilt.frostserver.plugin.format.dataarray.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.plugin.format.dataarray.DataArrayValue;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_COUNT;
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_NAVIGATION_LINK;
import java.io.IOException;

/**
 *
 * @author jab
 */
public class DataArrayValueSerializer extends JsonSerializer<DataArrayValue> {

    private static final String DATAARRAY_IOT_COUNT = "dataArray" + AT_IOT_COUNT;
    private static final String MULTI_DATASTREAM_IOT_NAVIGATION_LINK = "MultiDatastream" + AT_IOT_NAVIGATION_LINK;
    private static final String DATASTREAM_IOT_NAVIGATION_LINK = "Datastream" + AT_IOT_NAVIGATION_LINK;

    @Override
    public void serialize(DataArrayValue value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        Entity datastream = value.getDatastream();
        if (datastream != null) {
            gen.writeStringField(DATASTREAM_IOT_NAVIGATION_LINK, datastream.getSelfLink());
        }
        Entity multiDatastream = value.getMultiDatastream();
        if (multiDatastream != null) {
            gen.writeStringField(MULTI_DATASTREAM_IOT_NAVIGATION_LINK, multiDatastream.getSelfLink());
        }
        gen.writeObjectField("components", value.getComponents());
        int count = value.getDataArray().size();
        if (count >= 0) {
            gen.writeNumberField(DATAARRAY_IOT_COUNT, count);
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
