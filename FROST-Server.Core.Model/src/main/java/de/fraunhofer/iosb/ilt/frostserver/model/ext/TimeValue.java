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

import java.util.Objects;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;

/**
 * Common interface for time values. Needed as STA sometimes does not specify
 * wether an instant or an interval will be passed.
 *
 * @author jab
 */
public class TimeValue implements TimeObject {

    private final TimeInstant instant;
    private final TimeInterval interval;

    public TimeValue(TimeInstant timeInstant) {
        this.instant = timeInstant;
        this.interval = null;
    }

    public TimeValue(TimeInterval timeInterval) {
        this.instant = null;
        this.interval = timeInterval;
    }

    public static TimeValue create(Long value) {
        return new TimeValue(TimeInstant.create(value));
    }

    public static TimeValue create(Long value, DateTimeZone timeZone) {
        return new TimeValue(TimeInstant.create(value, timeZone));
    }

    public static TimeValue parse(String value, DateTimeFormatter dtf) {
        return new TimeValue(TimeInstant.parse(value, dtf));
    }

    public static TimeValue create(long start, long end) {
        return new TimeValue(TimeInterval.create(start, end));
    }

    public static TimeValue create(long start, long end, DateTimeZone timeZone) {
        return new TimeValue(TimeInterval.create(start, end, timeZone));
    }

    public boolean isInstant() {
        return instant != null;
    }

    public TimeInstant getInstant() {
        return instant;
    }

    public boolean isInterval() {
        return interval != null;
    }

    public TimeInterval getInterval() {
        return interval;
    }

    @Override
    public String asISO8601() {
        return instant == null ? interval.asISO8601() : instant.asISO8601();
    }

    @Override
    public boolean isEmpty() {
        return instant == null ? interval.isEmpty() : instant.isEmpty();
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
        final TimeValue other = (TimeValue) obj;
        if (!Objects.equals(this.instant, other.instant)) {
            return false;
        }
        return Objects.equals(this.interval, other.interval);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.instant);
        hash = 67 * hash + Objects.hashCode(this.interval);
        return hash;
    }

}
