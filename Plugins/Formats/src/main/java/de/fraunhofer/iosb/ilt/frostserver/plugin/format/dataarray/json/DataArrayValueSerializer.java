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
import static de.fraunhofer.iosb.ilt.frostserver.property.SpecialNames.AT_IOT_NAVIGATION_LINK;

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.plugin.format.dataarray.DataArrayValue;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * Serialiser for data array value objects.
 */
public class DataArrayValueSerializer extends ValueSerializer<DataArrayValue> {

    private static final String DATAARRAY_IOT_COUNT = "dataArray" + AT_IOT_COUNT;
    private static final String MULTI_DATASTREAM_IOT_NAVIGATION_LINK = "MultiDatastream" + AT_IOT_NAVIGATION_LINK;
    private static final String DATASTREAM_IOT_NAVIGATION_LINK = "Datastream" + AT_IOT_NAVIGATION_LINK;

    @Override
    public void serialize(DataArrayValue value, JsonGenerator gen, SerializationContext ctx) throws JacksonException {
        gen.writeStartObject();
        Entity datastream = value.getDatastream();
        if (datastream != null && datastream.getSelfLink() != null) {
            gen.writeStringProperty(DATASTREAM_IOT_NAVIGATION_LINK, datastream.getSelfLink());
        }
        Entity multiDatastream = value.getMultiDatastream();
        if (multiDatastream != null && multiDatastream.getSelfLink() != null) {
            gen.writeStringProperty(MULTI_DATASTREAM_IOT_NAVIGATION_LINK, multiDatastream.getSelfLink());
        }
        gen.writePOJOProperty("components", value.getComponents());
        int count = value.getDataArray().size();
        if (count >= 0) {
            gen.writeNumberProperty(DATAARRAY_IOT_COUNT, count);
        }
        gen.writePOJOProperty("dataArray", value.getDataArray());
        gen.writeEndObject();
    }

    @Override
    public boolean isEmpty(SerializationContext ctx, DataArrayValue value) {
        return (value == null || value.getDataArray().isEmpty());
    }

}
