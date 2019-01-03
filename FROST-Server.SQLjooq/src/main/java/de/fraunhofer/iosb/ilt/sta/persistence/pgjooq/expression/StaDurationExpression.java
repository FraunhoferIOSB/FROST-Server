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
package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.expression;

import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.PgExpressionHandler;
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.Utils.INTERVAL_PARAM;
import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.Utils.TIMESTAMP_PARAM;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.DurationConstant;
import java.time.OffsetDateTime;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.jooq.types.Interval;

/**
 *
 * @author scf
 */
public class StaDurationExpression implements TimeExpression {

    private final Field<Interval> duration;

    public StaDurationExpression(final DurationConstant duration) {
        this.duration = DSL.field(duration.asISO8601(), Interval.class);
    }

    public StaDurationExpression(final Field<Interval> duration) {
        this.duration = duration;
    }

    /**
     * Create a new StaDuration by taking the difference between the two given
     * timestamps.
     *
     * @param ts1 The first timestamp.
     * @param ts2 The second timestamp, to be subtracted from the first.
     */
    public StaDurationExpression(final Field<OffsetDateTime> ts1, final Field<OffsetDateTime> ts2) {
        String template = "(" + TIMESTAMP_PARAM + " - " + TIMESTAMP_PARAM + ")";
        this.duration = DSL.field(template, Interval.class, ts1, ts2);
    }

    @Override
    public Field getDefaultField() {
        return duration;
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

    public Field<Interval> getDuration() {
        return duration;
    }

    @Override
    public Condition after(Object other) {
        throw new UnsupportedOperationException("Can not use after with duration.");
    }

    @Override
    public Condition before(Object other) {
        throw new UnsupportedOperationException("Can not use before with duration.");
    }

    @Override
    public Condition meets(Object other) {
        throw new UnsupportedOperationException("Can not use meets with duration.");
    }

    @Override
    public Condition contains(Object other) {
        throw new UnsupportedOperationException("Can not use contais with duration.");
    }

    @Override
    public Condition overlaps(Object other) {
        throw new UnsupportedOperationException("Can not use overlaps with duration.");
    }

    @Override
    public Condition starts(Object other) {
        throw new UnsupportedOperationException("Can not use starts with duration.");
    }

    @Override
    public Condition finishes(Object other) {
        throw new UnsupportedOperationException("Can not use finishes with duration.");
    }

    private Object specificOp(String op, StaDurationExpression other) {
        String template = "(" + INTERVAL_PARAM + " " + op + " " + INTERVAL_PARAM + ")";
        Field<OffsetDateTime> expression = DSL.field(template, OffsetDateTime.class, this.duration, other.duration);
        return new StaDateTimeExpression(expression);
    }

    private Object specificOp(String op, StaTimeIntervalExpression other) {
        Field<OffsetDateTime> dtEnd = PgExpressionHandler.checkType(OffsetDateTime.class, other.end, false);
        Field<OffsetDateTime> dtStart = PgExpressionHandler.checkType(OffsetDateTime.class, other.start, false);
        String template = "(" + INTERVAL_PARAM + " " + op + " " + TIMESTAMP_PARAM + ")";
        Field<OffsetDateTime> newStart = DSL.field(template, OffsetDateTime.class, duration, dtStart);
        Field<OffsetDateTime> newEnd = DSL.field(template, OffsetDateTime.class, duration, dtEnd);
        return new StaTimeIntervalExpression(newStart, newEnd);
    }

    private Object specificOp(String op, StaDateTimeExpression other) {
        String template = "(" + INTERVAL_PARAM + " " + op + " " + TIMESTAMP_PARAM + ")";
        Field<OffsetDateTime> expression = DSL.field(template, OffsetDateTime.class, duration, other);
        return new StaDateTimeExpression(expression);
    }

    private Object specificOp(String op, Field<Number> other) {
        switch (op) {
            case "*":
            case "/":
                String template = "(" + INTERVAL_PARAM + " " + op + " (?))";
                Field<Interval> expression = DSL.field(template, Interval.class, this.duration, other);
                return new StaDurationExpression(expression);

            default:
                throw new UnsupportedOperationException("Can not '" + op + "' with Duration and " + other.getClass().getName());
        }
    }

    @Override
    public Object simpleOp(String op, Object other) {
        if (other instanceof StaDurationExpression) {
            return specificOp(op, (StaDurationExpression) other);
        }
        if (other instanceof StaTimeIntervalExpression) {
            return specificOp(op, (StaTimeIntervalExpression) other);
        }
        if (other instanceof StaDateTimeExpression) {
            return specificOp(op, (StaDateTimeExpression) other);
        }
        if (other instanceof Field) {
            Field otherField = (Field) other;
            if (Number.class.isAssignableFrom(otherField.getType())) {
                return specificOp(op, otherField);
            }
        }
        if (other instanceof ListExpression) {
            Field<Number> nrOther = PgExpressionHandler.getSingleOfType(Number.class, other);
            return specificOp(op, nrOther);
        }
        throw new UnsupportedOperationException("Can not add, sub, mul or div with Duration and " + other.getClass().getName());
    }

    @Override
    public Condition simpleOpBool(String op, Object other) {
        if (other instanceof StaDurationExpression) {
            StaDurationExpression cd = (StaDurationExpression) other;
            String template = "(" + INTERVAL_PARAM + " " + op + " " + INTERVAL_PARAM + ")";
            return DSL.condition(template, this.duration, cd.duration);
        }
        throw new UnsupportedOperationException("Can not compare between Duration and " + other.getClass().getName());
    }

}
