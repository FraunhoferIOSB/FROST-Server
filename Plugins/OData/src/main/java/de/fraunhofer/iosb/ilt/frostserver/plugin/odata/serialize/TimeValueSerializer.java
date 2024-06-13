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
package de.fraunhofer.iosb.ilt.frostserver.plugin.odata.serialize;

import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex.NAME_INTERVAL_END;
import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex.NAME_INTERVAL_START;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.io.IOException;
import net.time4j.Moment;
import net.time4j.range.MomentInterval;

/**
 * Serializer for TimeValue objects.
 *
 * @author jab
 */
public class TimeValueSerializer extends JsonSerializer<TimeValue> {

    @Override
    public void serialize(TimeValue value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value.isEmpty()) {
            gen.writeNull();
        } else {
            gen.writeStartObject();
            if (value.isInstant()) {
                gen.writeObjectField(NAME_INTERVAL_START, value.asISO8601());
            } else {
                final MomentInterval interval = value.getInterval().getInterval();
                final Moment start = interval.getStartAsMoment();
                final Moment end = interval.getEndAsMoment();
                gen.writeObjectField(NAME_INTERVAL_START, StringHelper.FORMAT_MOMENT.print(start));
                if (!start.equals(end)) {
                    gen.writeObjectField(NAME_INTERVAL_END, StringHelper.FORMAT_MOMENT.print(end));
                }
            }
            gen.writeEndObject();
        }
    }

}
