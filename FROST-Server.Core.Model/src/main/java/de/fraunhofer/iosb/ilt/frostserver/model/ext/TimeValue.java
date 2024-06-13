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

import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex.NAME_INTERVAL_END;
import static de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex.NAME_INTERVAL_START;

import de.fraunhofer.iosb.ilt.frostserver.model.ComplexValue;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.property.type.TypeComplex;
import java.time.Instant;
import java.util.Objects;
import net.time4j.Moment;

/**
 * Common interface for time values. Needed as STA sometimes does not specify
 * wether an instant or an interval will be passed.
 */
public class TimeValue implements TimeObject, ComplexValue<TimeValue> {

    public static EntityPropertyMain<TimeInstant> EP_START_TIME = TypeComplex.EP_START_TIME;
    public static EntityPropertyMain<TimeInstant> EP_END_TIME = TypeComplex.EP_INTERVAL_END_TIME;

    private TimeInstant instant;
    private TimeInterval interval;

    public TimeValue() {
        this.instant = null;
        this.interval = null;
    }

    public TimeValue(TimeInstant timeInstant) {
        this.instant = timeInstant;
        this.interval = null;
    }

    public TimeValue(TimeInterval timeInterval) {
        this.instant = null;
        this.interval = timeInterval;
    }

    public static TimeValue create(Moment start) {
        return new TimeValue(TimeInstant.create(start));
    }

    public static TimeValue create(Moment start, Moment end) {
        return new TimeValue(TimeInterval.create(start, end));
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
        if (instant != null) {
            return instant.isEmpty();
        }
        if (interval != null) {
            return interval.isEmpty();
        }
        return true;
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

    @Override
    public <P> P getProperty(Property<P> property) {
        if (property != EP_START_TIME && property != EP_END_TIME) {
            throw new IllegalArgumentException("Unknown sub-property: " + property);
        }
        if (isInterval()) {
            return interval.getProperty(property);
        } else {
            return (P) instant;
        }
    }

    @Override
    public TimeValue setProperty(Property property, Object value) {
        Moment moment;
        if (value == null) {
            moment = null;
        } else if (value instanceof Moment m) {
            moment = m;
        } else if (value instanceof Instant i) {
            moment = Moment.from(i);
        } else {
            throw new IllegalArgumentException("TimeInterval only accepts Moment or Instant, not " + value.getClass().getName());
        }
        if (property == EP_START_TIME) {
            if (moment == null) {
                return this;
            }
            if (instant != null) {
                instant = new TimeInstant(moment);
            } else {
                interval.setProperty(property, moment);
            }
            return this;
        }
        if (property == EP_END_TIME) {
            if (instant != null) {
                if (moment == null) {
                    return this;
                }
                // setting end on instant, convert to interval.
                interval = TimeInterval.create(instant.getDateTime(), moment);
                instant = null;
            } else {
                if (moment == null) {
                    // Removing end from interval, convert to instant
                    instant = TimeInstant.create(interval.getStart());
                    interval = null;
                } else {
                    interval.setProperty(property, moment);
                }
            }
        }
        throw new IllegalArgumentException("Unknown sub-property: " + property);

    }

    @Override
    public Object getProperty(String name) {
        switch (name) {
            case NAME_INTERVAL_START:
                return getProperty(EP_START_TIME);

            case NAME_INTERVAL_END:
                return getProperty(EP_END_TIME);

            default:
                throw new IllegalArgumentException("Unknown sub-property: " + name);
        }
    }

    @Override
    public TimeValue setProperty(String name, Object value) {
        switch (name) {
            case NAME_INTERVAL_START:
                return setProperty(EP_START_TIME, value);

            case NAME_INTERVAL_END:
                return setProperty(EP_END_TIME, value);

            default:
                throw new IllegalArgumentException("Unknown sub-property: " + name);
        }
    }

}
