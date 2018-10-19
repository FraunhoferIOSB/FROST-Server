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
package de.fraunhofer.iosb.ilt.sta.json.deserialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeInterval;
import java.io.IOException;

/**
 * Helper for deserialization of TimeInterval objects from JSON.
 *
 * @author jab
 */
public class TimeIntervalDeserializer extends StdDeserializer<TimeInterval> {

    public TimeIntervalDeserializer() {
        super(TimeInterval.class);
    }

    @Override
    public TimeInterval deserialize(JsonParser jp, DeserializationContext dc) throws IOException {
        return TimeInterval.parse(((JsonNode) jp.getCodec().readTree(jp)).asText());
    }

}
