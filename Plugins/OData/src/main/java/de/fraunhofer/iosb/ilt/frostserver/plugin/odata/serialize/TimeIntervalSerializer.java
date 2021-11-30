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
package de.fraunhofer.iosb.ilt.frostserver.plugin.odata.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInterval;
import java.io.IOException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Serializer for TimeValue objects.
 *
 * @author jab
 */
public class TimeIntervalSerializer extends JsonSerializer<TimeInterval> {

    @Override
    public void serialize(TimeInterval value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value.isEmpty()) {
            gen.writeNull();
        } else {
            gen.writeStartObject();
            final Interval interval = value.getInterval();
            final DateTime start = interval.getStart();
            final DateTime end = interval.getEnd();
            final DateTimeFormatter timePrinter = ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC);
            gen.writeObjectField("start", timePrinter.print(start));
            if (!start.equals(end)) {
                gen.writeObjectField("end", timePrinter.print(end));
            }
            gen.writeEndObject();
        }
    }

}
