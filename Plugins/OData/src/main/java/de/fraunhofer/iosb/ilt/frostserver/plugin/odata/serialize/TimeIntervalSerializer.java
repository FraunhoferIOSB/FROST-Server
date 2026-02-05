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
import static de.fraunhofer.iosb.ilt.frostserver.util.StringHelper.FORMAT_MOMENT;

import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeInterval;
import net.time4j.Moment;
import net.time4j.range.MomentInterval;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * Serializer for TimeValue objects.
 */
public class TimeIntervalSerializer extends ValueSerializer<TimeInterval> {

    @Override
    public void serialize(TimeInterval value, JsonGenerator gen, SerializationContext serializers) throws JacksonException {
        if (value.isEmpty()) {
            gen.writeNull();
        } else {
            gen.writeStartObject();
            final MomentInterval interval = value.getInterval();
            final Moment start = interval.getStartAsMoment();
            final Moment end = interval.getEndAsMoment();
            gen.writePOJOProperty(NAME_INTERVAL_START, FORMAT_MOMENT.print(start));
            if (!start.equals(end)) {
                gen.writePOJOProperty(NAME_INTERVAL_END, FORMAT_MOMENT.print(end));
            }
            gen.writeEndObject();
        }
    }

}
