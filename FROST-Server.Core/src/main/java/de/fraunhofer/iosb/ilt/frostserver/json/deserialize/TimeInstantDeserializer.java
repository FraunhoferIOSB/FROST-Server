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
package de.fraunhofer.iosb.ilt.frostserver.json.deserialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import java.io.IOException;

/**
 * Helper for deserialization of TimeInstant objects from JSON.
 *
 * @author jab
 */
public class TimeInstantDeserializer extends StdDeserializer<TimeInstant> {

    public TimeInstantDeserializer() {
        super(TimeInstant.class);
    }

    @Override
    public TimeInstant deserialize(JsonParser jp, DeserializationContext dc) throws IOException {
        return TimeInstant.parse(jp.getValueAsString());
    }

}
