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
package de.fraunhofer.iosb.ilt.frostserver.model.ext;

import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.UTC;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Objects;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Represents ISO8601 Instant.
 *
 * @author jab
 */
public class TimeInstant implements TimeObject {

    /**
     * TODO: Convert to java.time.OffsetDateTime
     */
    private final DateTime dateTime;

    public TimeInstant(DateTime dateTime) {
        this.dateTime = dateTime;
    }

    public static TimeInstant now() {
        return new TimeInstant(DateTime.now());
    }

    public static TimeInstant now(DateTimeZone timeZone) {
        return new TimeInstant(DateTime.now(timeZone));
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
        return this.dateTime.isEqual(other.dateTime);
    }

    public static TimeInstant parse(String value) {
        return new TimeInstant(DateTime.parse(value));
    }

    public static TimeInstant create(Long value) {
        return new TimeInstant(new DateTime(value));
    }

    public static TimeInstant create(Long value, DateTimeZone timeZone) {
        return new TimeInstant(new DateTime(value, timeZone));
    }

    public static TimeInstant parse(String value, DateTimeFormatter dtf) {
        return new TimeInstant(DateTime.parse(value, dtf));
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public OffsetDateTime getOffsetDateTime() {
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(dateTime.getMillis()), UTC);
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
        return ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC).print(dateTime);
    }

    @Override
    public String toString() {
        return asISO8601();
    }

}
