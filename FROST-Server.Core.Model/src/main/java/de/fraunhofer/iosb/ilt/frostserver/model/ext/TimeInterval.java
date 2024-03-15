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
package de.fraunhofer.iosb.ilt.frostserver.model.ext;

import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex.NAME_INTERVAL_END;
import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex.NAME_INTERVAL_START;

import de.fraunhofer.iosb.ilt.frostserver.model.ComplexValue;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.text.ParseException;
import java.time.Instant;
import java.util.Objects;
import net.time4j.Moment;
import net.time4j.range.MomentInterval;

/**
 * Represent an ISO8601 time interval.
 */
public class TimeInterval implements TimeObject, ComplexValue<TimeInterval> {

    public static EntityPropertyMain<TimeInstant> EP_START_TIME = TypeComplex.EP_START_TIME;
    public static EntityPropertyMain<TimeInstant> EP_END_TIME = TypeComplex.EP_INTERVAL_END_TIME;

    private MomentInterval interval;

    public TimeInterval() {
        this.interval = MomentInterval.between(Moment.nowInSystemTime(), Moment.nowInSystemTime());
    }

    public TimeInterval(MomentInterval interval) {
        if (interval == null) {
            throw new IllegalArgumentException("Interval must be non-null");
        }
        this.interval = interval;
    }

    @Override
    public int hashCode() {
        return Objects.hash(interval);
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
        return Objects.equals(this.interval, other.interval);
    }

    public static TimeInterval create(Moment start, Moment end) {
        return new TimeInterval(MomentInterval.between(start, end));
    }

    public static TimeInterval parse(String value) {
        try {
            return new TimeInterval(MomentInterval.parseISO(value));
        } catch (ParseException ex) {
            throw new IllegalArgumentException("Failed to parse TimeInterval " + StringHelper.cleanForLogging(value), ex);
        }
    }

    public MomentInterval getInterval() {
        return interval;
    }

    @Override
    public boolean isEmpty() {
        return interval == null;
    }

    @Override
    public String asISO8601() {
        return StringHelper.FORMAT_INTERVAL.print(interval);
    }

    @Override
    public String toString() {
        return asISO8601();
    }

    public Moment getStart() {
        return interval.getStartAsMoment();
    }

    public Moment getEnd() {
        return interval.getEndAsMoment();
    }

    @Override
    public Object getProperty(String name) {
        switch (name) {
            case NAME_INTERVAL_START:
                return interval.getStartAsMoment();

            case NAME_INTERVAL_END:
                return interval.getEndAsMoment();

            default:
                throw new IllegalArgumentException("Unknown sub-property: " + name);
        }
    }

    @Override
    public TimeInterval setProperty(String name, Object value) {
        switch (name) {
            case NAME_INTERVAL_START:
                return setProperty(EP_START_TIME, value);

            case NAME_INTERVAL_END:
                return setProperty(EP_END_TIME, value);

            default:
                throw new IllegalArgumentException("Unknown sub-property: " + name);
        }
    }

    @Override
    public <P> P getProperty(Property<P> property) {
        if (property == EP_START_TIME) {
            return (P) interval.getStartAsMoment();
        }
        if (property == EP_END_TIME) {
            return (P) interval.getStartAsMoment();
        }
        throw new IllegalArgumentException("Unknown sub-property: " + property);
    }

    @Override
    public TimeInterval setProperty(Property property, Object value) {
        if (value == null) {
            return this;
        }
        Moment moment;
        if (value instanceof Moment m) {
            moment = m;
        } else if (value instanceof Instant i) {
            moment = Moment.from(i);
        } else {
            throw new IllegalArgumentException("TimeInterval only accepts Moment or Instant, not " + value.getClass().getName());
        }
        if (property == EP_START_TIME) {
            interval = interval.withStart(moment);
            return this;
        }
        if (property == EP_END_TIME) {
            interval = interval.withEnd(moment).withOpenEnd();
            return this;
        }
        throw new IllegalArgumentException("Unknown sub-property: " + property);
    }

}
