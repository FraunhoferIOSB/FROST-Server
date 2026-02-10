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

import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex.NAME_INTERVAL_END;
import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex.NAME_INTERVAL_START;

import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import net.time4j.Moment;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * Helper for deserialization of TimeValue objects from JSON. May not work
 * properly in every case as deciding wether input is a TimeInstant or a
 * TimeInterval is based on exceptions while parsing
 */
public class TimeValueDeserializer extends StdDeserializer<TimeValue> {

    public TimeValueDeserializer() {
        super(TimeValue.class);
    }

    @Override
    public TimeValue deserialize(JsonParser jp, DeserializationContext dc) throws JacksonException {
        JsonToken curToken = jp.currentToken();
        if (curToken == JsonToken.VALUE_STRING) {
            return parseStringValue(jp);
        } else if (curToken == JsonToken.START_OBJECT) {
            return parseObjectValue(jp);
        } else {
            throw new IllegalArgumentException("Could not parse TimeValue, found a " + curToken.name());
        }
    }

    private TimeValue parseStringValue(JsonParser jp) throws JacksonException {
        String node = jp.getValueAsString();
        if (node == null) {
            return null;
        }
        try {
            return new TimeValue(TimeInstant.parse(node));
        } catch (IllegalArgumentException e) {
            return new TimeValue(TimeInterval.parse(node));
        }
    }

    private TimeValue parseObjectValue(JsonParser jp) throws JacksonException {
        Moment start = null;
        Moment end = null;
        JsonToken currentToken = jp.nextToken();
        while (currentToken == JsonToken.PROPERTY_NAME) {
            final String fieldName = jp.currentName();
            currentToken = jp.nextToken();
            if (currentToken != JsonToken.VALUE_STRING) {
                throw new IllegalArgumentException("Found " + currentToken + " for " + fieldName + " expected a string");
            }
            final String valueAsString = jp.getValueAsString();
            switch (fieldName) {
                case NAME_INTERVAL_START:
                    start = TimeInstant.parseMoment(valueAsString);
                    break;

                case NAME_INTERVAL_END:
                    end = TimeInstant.parseMoment(valueAsString);
                    break;

                default:
                    throw new IllegalArgumentException("Found field " + fieldName + " expected one of: start, end");
            }
            currentToken = jp.nextToken();
        }

        if (start == null && end == null) {
            return null;
        }
        if (start != null && end == null) {
            return TimeValue.create(start);
        }
        if (end != null && start == null) {
            return TimeValue.create(end);
        }
        return TimeValue.create(start, end);
    }

}
