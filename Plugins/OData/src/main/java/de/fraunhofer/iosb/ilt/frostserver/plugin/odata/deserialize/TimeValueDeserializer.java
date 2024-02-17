/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.plugin.odata.deserialize;

import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex.KEY_INTERVAL_END;
import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex.KEY_INTERVAL_START;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.io.IOException;
import java.text.ParseException;
import net.time4j.Moment;
import net.time4j.format.expert.Iso8601Format;

/**
 * Helper for deserialization of TimeInterval objects from JSON.
 *
 * @author jab
 */
public class TimeValueDeserializer extends StdDeserializer<TimeValue> {

    public TimeValueDeserializer() {
        super(TimeInterval.class);
    }

    @Override
    public TimeValue deserialize(JsonParser parser, DeserializationContext dc) throws IOException {
        Moment start = null;
        Moment end = null;
        JsonToken currentToken = parser.nextToken();

        while (currentToken == JsonToken.FIELD_NAME) {
            String fieldName = parser.getCurrentName();
            switch (fieldName) {
                case KEY_INTERVAL_START:
                    start = TimeValueDeserializer.parseTime(parser);
                    break;

                case KEY_INTERVAL_END:
                    end = TimeValueDeserializer.parseTime(parser);
                    break;

                default:
                    throw new IllegalArgumentException("Found field " + fieldName + " expected one of: start, end");
            }
            currentToken = parser.nextToken();
        }

        if (start == null && end == null) {
            return null;
        }
        if (start != null) {
            return TimeValue.create(start);
        }
        if (end != null) {
            return TimeValue.create(end);
        }
        return TimeValue.create(start, end);
    }

    public static Moment parseTime(JsonParser parser) throws IllegalArgumentException, IOException {
        parser.nextToken();
        final String valueAsString = parser.getValueAsString();
        try {
            return Iso8601Format.EXTENDED_DATE_TIME_OFFSET.parse(valueAsString);
        } catch (ParseException ex) {
            throw new IllegalArgumentException("Failed to parse TimeInstant " + StringHelper.cleanForLogging(valueAsString), ex);
        }
    }
}
