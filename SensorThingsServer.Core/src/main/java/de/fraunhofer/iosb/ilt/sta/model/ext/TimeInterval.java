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
import de.fraunhofer.iosb.ilt.sta.deserialize.TimeIntervalDeserializer;
import java.util.Objects;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Represent an ISO8601 time instant.
 *
 * @author jab
 */
@JsonDeserialize(using = TimeIntervalDeserializer.class)
public class TimeInterval implements TimeValue {

    private Interval interval;

    private TimeInterval() {
    }

    private TimeInterval(Interval interval) {
        assert (interval != null);
        this.interval = interval;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + Objects.hashCode(this.interval);
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
        final TimeInterval other = (TimeInterval) obj;
        if (!Objects.equals(this.interval, other.interval)) {
            return false;
        }
        return true;
    }

    public static TimeInterval create(long start, long end) {
        return new TimeInterval(new Interval(start, end));
    }

    public static TimeInterval create(long start, long end, DateTimeZone timeZone) {
        return new TimeInterval(new Interval(start, end, timeZone));
    }

    public static TimeInterval parse(String value) {
        return new TimeInterval(Interval.parse(value));
    }

    public Interval getInterval() {
        return interval;
    }

    @Override
    public String asISO8601() {
        DateTimeFormatter printer = ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC);
        printer = printer.withChronology(interval.getChronology());
        StringBuffer buf = new StringBuffer(48);
        printer.printTo(buf, interval.getStartMillis());
        buf.append('/');
        printer.printTo(buf, interval.getEndMillis());
        return buf.toString();
    }

    @Override
    public String toString() {
        return asISO8601();
    }

}
