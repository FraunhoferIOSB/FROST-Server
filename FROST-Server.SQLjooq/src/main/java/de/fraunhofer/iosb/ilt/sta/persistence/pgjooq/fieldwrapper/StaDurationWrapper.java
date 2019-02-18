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
package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.fieldwrapper;

import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.Utils.INTERVAL_PARAM;
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.Utils.TIMESTAMP_PARAM;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.DurationConstant;
import java.time.OffsetDateTime;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
public class StaDurationWrapper implements TimeFieldWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(StaDurationWrapper.class.getName());

    private final Field<String> duration;

    public StaDurationWrapper(final DurationConstant duration) {
        this.duration = DSL.val(duration.asISO8601());
    }

    public StaDurationWrapper(final Field<String> duration) {
        this.duration = duration;
    }

    /**
     * Create a new StaDuration by taking the difference between the two given
     * timestamps.
     *
     * @param ts1 The first timestamp.
     * @param ts2 The second timestamp, to be subtracted from the first.
     */
    public StaDurationWrapper(final Field<OffsetDateTime> ts1, final Field<OffsetDateTime> ts2) {
        String template = "(" + TIMESTAMP_PARAM + " - " + TIMESTAMP_PARAM + ")";
        this.duration = DSL.field(template, String.class, ts1, ts2);
    }

    @Override
    public Field getDefaultField() {
        return getDuration();
    }

    @Override
    public <T> Field<T> getFieldAsType(Class<T> expectedClazz, boolean canCast) {
        Class<String> fieldType = duration.getType();
        if (expectedClazz.isAssignableFrom(fieldType)) {
            return (Field<T>) duration;
        }
        if (canCast && expectedClazz == String.class) {
            return (Field<T>) duration.cast(String.class);
        }
        LOGGER.debug("Not a {}: {} ({} -- {})", expectedClazz.getName(), duration, duration.getClass().getName(), fieldType.getName());
        return null;
    }

    @Override
    public Field<OffsetDateTime> getDateTime() {
        throw new UnsupportedOperationException("Can not convert duration to DateTime.");
    }

    @Override
    public boolean isUtc() {
        // durations are always utc.
        return true;
    }

    public Field<String> getDuration() {
        return duration;
    }

    @Override
    public FieldWrapper after(FieldWrapper other) {
        throw new UnsupportedOperationException("Can not use after with duration.");
    }

    @Override
    public FieldWrapper before(FieldWrapper other) {
        throw new UnsupportedOperationException("Can not use before with duration.");
    }

    @Override
    public FieldWrapper meets(FieldWrapper other) {
        throw new UnsupportedOperationException("Can not use meets with duration.");
    }

    @Override
    public FieldWrapper contains(FieldWrapper other) {
        throw new UnsupportedOperationException("Can not use contais with duration.");
    }

    @Override
    public FieldWrapper overlaps(FieldWrapper other) {
        throw new UnsupportedOperationException("Can not use overlaps with duration.");
    }

    @Override
    public FieldWrapper starts(FieldWrapper other) {
        throw new UnsupportedOperationException("Can not use starts with duration.");
    }

    @Override
    public FieldWrapper finishes(FieldWrapper other) {
        throw new UnsupportedOperationException("Can not use finishes with duration.");
    }

    private FieldWrapper specificOp(String op, StaDurationWrapper other) {
        String template = "(" + INTERVAL_PARAM + " " + op + " " + INTERVAL_PARAM + ")";
        Field<OffsetDateTime> expression = DSL.field(template, OffsetDateTime.class, this.duration, other.duration);
        return new StaDateTimeWrapper(expression);
    }

    private FieldWrapper specificOp(String op, StaTimeIntervalWrapper other) {
        Field<OffsetDateTime> dtEnd = other.getEnd();
        Field<OffsetDateTime> dtStart = other.getStart();
        String template = "(" + INTERVAL_PARAM + " " + op + " " + TIMESTAMP_PARAM + ")";
        Field<OffsetDateTime> newStart = DSL.field(template, OffsetDateTime.class, duration, dtStart);
        Field<OffsetDateTime> newEnd = DSL.field(template, OffsetDateTime.class, duration, dtEnd);
        return new StaTimeIntervalWrapper(newStart, newEnd);
    }

    private FieldWrapper specificOp(String op, StaDateTimeWrapper other) {
        String template = "(" + INTERVAL_PARAM + " " + op + " " + TIMESTAMP_PARAM + ")";
        Field<OffsetDateTime> expression = DSL.field(template, OffsetDateTime.class, duration, other);
        return new StaDateTimeWrapper(expression);
    }

    private FieldWrapper specificOp(String op, Field<Number> other) {
        switch (op) {
            case "*":
            case "/":
                String template = "(" + INTERVAL_PARAM + " " + op + " (?))";
                Field<String> expression = DSL.field(template, String.class, this.duration, other);
                return new StaDurationWrapper(expression);

            default:
                throw new UnsupportedOperationException("Can not '" + op + "' with Duration and " + other.getClass().getName());
        }
    }

    @Override
    public FieldWrapper simpleOp(String op, FieldWrapper other) {
        if (other instanceof StaDurationWrapper) {
            return specificOp(op, (StaDurationWrapper) other);
        }
        if (other instanceof StaTimeIntervalWrapper) {
            return specificOp(op, (StaTimeIntervalWrapper) other);
        }
        if (other instanceof StaDateTimeWrapper) {
            return specificOp(op, (StaDateTimeWrapper) other);
        }
        if (other instanceof FieldListWrapper) {
            Field<Number> nrOther = ((FieldListWrapper) other).getFieldAsType(Number.class, true);
            return specificOp(op, nrOther);
        }
        Field otherField = other.getFieldAsType(Number.class, true);
        if (otherField != null) {
            return specificOp(op, otherField);
        }

        throw new UnsupportedOperationException("Can not add, sub, mul or div with Duration and " + other.getClass().getName());
    }

    @Override
    public FieldWrapper simpleOpBool(String op, FieldWrapper other) {
        if (other instanceof StaDurationWrapper) {
            StaDurationWrapper cd = (StaDurationWrapper) other;
            String template = "(" + INTERVAL_PARAM + " " + op + " " + INTERVAL_PARAM + ")";
            return new SimpleFieldWrapper(
                    DSL.condition(template, this.duration, cd.duration)
            );
        }
        throw new UnsupportedOperationException("Can not compare between Duration and " + other.getClass().getName());
    }

}
