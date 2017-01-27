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
package de.fraunhofer.iosb.ilt.sta.model.ext;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.fraunhofer.iosb.ilt.sta.deserialize.TimeInstantDeserializer;
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
@JsonDeserialize(using = TimeInstantDeserializer.class)
public class TimeInstant implements TimeValue {

    private DateTime dateTime;

    private TimeInstant() {
    }

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
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.dateTime);
        return hash;
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
        if (this.dateTime == null | other.dateTime == null) {
            return false;
        }
        if (!this.dateTime.isEqual(other.dateTime)) {
            return false;
        }
        return true;
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

    @Override
    public String asISO8601() {
        if (dateTime == null) {
            return null;
        }
        return ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC).print(dateTime);
    }

    @Override
    public String toString() {
        return asISO8601();
    }

}
