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
package de.fraunhofer.iosb.ilt.sta.deserialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeValue;
import java.io.IOException;

/**
 * Helper for deserialization of TimeValue objects from JSON. May not work properly in every case as deciding wether
 * input is a TimeInstant or a TimeInterval is based on exceptions while parsing
 *
 * @author jab
 */
public class TimeValueDeserializer extends StdDeserializer<TimeValue> {

    public TimeValueDeserializer() {
        super(TimeValue.class);
    }

    @Override
    public TimeValue deserialize(JsonParser jp, DeserializationContext dc) throws IOException, JsonProcessingException {
        TimeValue result;
        JsonNode node = jp.getCodec().readTree(jp);
        try {
            result = TimeInstant.parse(node.asText());
        } catch (Exception e) {
            result = TimeInterval.parse(node.asText());
        }
        return result;
    }

}
