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

import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex.KEY_INTERVAL_END;
import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex.KEY_INTERVAL_START;

import de.fraunhofer.iosb.ilt.frostserver.property.ComplexValue;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.text.ParseException;
import java.util.Objects;
import net.time4j.Moment;
import net.time4j.range.MomentInterval;

/**
 * Represent an ISO8601 time interval.
 *
 * @author jab
 */
public class TimeInterval implements TimeObject, ComplexValue {

    private final MomentInterval interval;

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

    @Override
    public Object get(String name) {
        return switch (name) {
            case KEY_INTERVAL_START -> interval.getStartAsMoment();

            case KEY_INTERVAL_END -> interval.getEndAsMoment();

            default -> throw new IllegalArgumentException("Unknown sub-property: " + name);
        };
    }

}
