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
package de.fraunhofer.iosb.ilt.frostserver.model.ext;

import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.text.ParseException;
import java.util.Objects;
import net.time4j.Moment;
import net.time4j.SystemClock;
import net.time4j.format.expert.Iso8601Format;
import net.time4j.format.expert.MultiFormatParser;

/**
 * Represents ISO8601 Instant.
 *
 * @author jab
 */
public class TimeInstant implements TimeObject {

    public static final MultiFormatParser<Moment> ISO_FORMAT = MultiFormatParser.of(Iso8601Format.EXTENDED_DATE_TIME_OFFSET, Iso8601Format.BASIC_DATE_TIME_OFFSET);
    private final Moment dateTime;

    public TimeInstant(Moment dateTime) {
        this.dateTime = dateTime;
    }

    public static TimeInstant now() {
        return new TimeInstant(SystemClock.currentMoment());
    }

    @Override
    public int hashCode() {
        return Objects.hash(dateTime);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TimeInstant other = (TimeInstant) obj;
        if (this.dateTime == null && other.dateTime == null) {
            return true;
        }
        if (this.dateTime == null || other.dateTime == null) {
            return false;
        }
        return this.dateTime.equals(other.dateTime);
    }

    public static TimeInstant parse(String value) {

        try {
            return new TimeInstant(ISO_FORMAT.parse(value));
        } catch (ParseException ex) {
            throw new IllegalArgumentException("Failed to parse TimeInstant " + StringHelper.cleanForLogging(value), ex);
        }
    }

    public static TimeInstant create(Moment time) {
        return new TimeInstant(time);
    }

    public Moment getDateTime() {
        return dateTime;
    }

    @Override
    public boolean isEmpty() {
        return dateTime == null;
    }

    @Override
    public String asISO8601() {
        if (dateTime == null) {
            return "";
        }
        return StringHelper.FORMAT_MOMENT.print(dateTime);
    }

    @Override
    public String toString() {
        return asISO8601();
    }

}
