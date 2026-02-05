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
package de.fraunhofer.iosb.ilt.frostserver.json.deserialize;

import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * Helper for deserialization of TimeInstant objects from JSON.
 */
public class TimeInstantDeserializer extends StdDeserializer<TimeInstant> {

    public TimeInstantDeserializer() {
        super(TimeInstant.class);
    }

    @Override
    public TimeInstant deserialize(JsonParser jp, DeserializationContext dc) throws JacksonException {
        final String valueAsString = jp.getValueAsString();
        if (valueAsString == null) {
            return null;
        }
        return TimeInstant.parse(valueAsString);
    }

}
