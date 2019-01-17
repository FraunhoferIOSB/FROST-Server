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

import static de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.Utils.INTERVAL_PARAM;
import java.time.OffsetDateTime;
import java.util.Map;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some paths point to time-intervals that return two column references. If the
 * references include a start and end time, they are treated as a time interval.
 */
public class StaTimeIntervalExpression implements TimeExpression {

    private static final Logger LOGGER = LoggerFactory.getLogger(StaTimeIntervalExpression.class.getName());

    public static final String KEY_TIME_INTERVAL_START = "tStart";
    public static final String KEY_TIME_INTERVAL_END = "tEnd";
    private static final String INCOMPATIBLE_OP = "Incompatible operator: Interval '";
    /**
     * Flag indicating that the original time given was in utc.
     */
    private final boolean utc = true;
    private final Field<OffsetDateTime> start;
    private final Field<OffsetDateTime> end;

    public StaTimeIntervalExpression(Map<String, Field> expressions) {
        this.start = expressions.get(KEY_TIME_INTERVAL_START);
        this.end = expressions.get(KEY_TIME_INTERVAL_END);
    }

    public StaTimeIntervalExpression(Field<OffsetDateTime> start, Field<OffsetDateTime> end) {
        this.start = start;
        this.end = end;
    }

    public StaTimeIntervalExpression(OffsetDateTime start, OffsetDateTime end) {
        this.start = DSL.inline(start);
        this.end = DSL.inline(end);
    }

    public Field<OffsetDateTime> getStart() {
        return start;
    }

    public Field<OffsetDateTime> getEnd() {
        return end;
    }

    @Override
    public Field getDefaultField() {
        return start;
    }

    @Override
    public <T> Field<T> getFieldAsType(Class<T> expectedClazz, boolean canCast) {
        Class<OffsetDateTime> fieldType = start.getType();
        if (expectedClazz.isAssignableFrom(fieldType)) {
            return (Field<T>) start;
        }
        if (canCast && expectedClazz == String.class) {
            return (Field<T>) start.cast(String.class);
        }
        LOGGER.debug("Not a {}: {} ({} -- {})", expectedClazz.getName(), start, start.getClass().getName(), fieldType.getName());
        return null;
    }

    @Override
    public Field<OffsetDateTime> getDateTime() {
        return start;
    }

    @Override
    public boolean isUtc() {
        return utc;
    }

    private FieldWrapper specificOp(String op, StaDurationExpression other) {
        switch (op) {
            case "+":
            case "-":
                String template = "(? " + op + " " + INTERVAL_PARAM + ")";
                return new StaTimeIntervalExpression(
                        DSL.field(template, OffsetDateTime.class, start, other.getDuration()),
                        DSL.field(template, OffsetDateTime.class, end, other.getDuration())
                );

            default:
                throw new UnsupportedOperationException(INCOMPATIBLE_OP + op + "' " + other.getClass().getName());

        }
    }

    private FieldWrapper specificOp(String op, StaDateTimeExpression other) {
        if ("-".equals(op)) {
            // We calculate with the start time and return a duration.
            return new StaDurationExpression(start, other.getDateTime());
        }
        throw new UnsupportedOperationException(INCOMPATIBLE_OP + op + "' " + other.getClass().getName());
    }

    private FieldWrapper specificOp(String op, StaTimeIntervalExpression other) {
        if ("-".equals(op)) {
            // We calculate with the start time and return a duration.
            return new StaDurationExpression(start, other.start);

        } else {
            throw new UnsupportedOperationException(INCOMPATIBLE_OP + op + "' " + other.getClass().getName());
        }
    }

    @Override
    public FieldWrapper simpleOp(String op, FieldWrapper other) {
        if (other instanceof StaDurationExpression) {
            return specificOp(op, (StaDurationExpression) other);
        }
        if (other instanceof StaDateTimeExpression) {
            return specificOp(op, (StaDateTimeExpression) other);
        }
        if (other instanceof StaTimeIntervalExpression) {
            return specificOp(op, (StaTimeIntervalExpression) other);
        }
        throw new UnsupportedOperationException("Can not add, sub, mul or div with Duration and " + other.getClass().getName());
    }

    private Condition specificOpBool(String op, StaDateTimeExpression other) {
        Field<OffsetDateTime> s1 = start;
        Field<OffsetDateTime> e1 = end;
        Field<OffsetDateTime> t2 = other.getDateTime();
        switch (op) {
            case "=":
                return s1.equal(t2).and(e1.equal(t2));

            case "!=":
                return s1.notEqual(t2).and(e1.notEqual(t2));

            case ">":
                return s1.greaterThan(t2);

            case ">=":
                return s1.greaterOrEqual(t2);

            case "<":
                return e1.lessOrEqual(t2).and(s1.lessThan(t2));

            case "<=":
                return e1.lessOrEqual(t2);

            case "a":
                return s1.greaterThan(t2);

            case "b":
                return e1.lessOrEqual(t2).and(s1.lessThan(t2));

            case "c":
                return s1.lessOrEqual(t2).and(e1.greaterThan(t2));

            case "m":
                return s1.equal(t2).or(e1.equal(t2));

            case "o":
                return s1.equal(t2).or(s1.lessOrEqual(t2).and(e1.greaterThan(t2)));

            case "s":
                return s1.equal(t2);

            case "f":
                return e1.equal(t2);

            default:
                throw new UnsupportedOperationException("Unknown boolean operation: " + op);
        }
    }

    private Condition specificOpBool(String op, StaTimeIntervalExpression other) {
        Field<OffsetDateTime> s1 = start;
        Field<OffsetDateTime> e1 = end;
        Field<OffsetDateTime> s2 = other.getStart();
        Field<OffsetDateTime> e2 = other.getEnd();
        switch (op) {
            case "=":
                return s1.equal(s2).and(e1.equal(e2));

            case "!=":
                return s1.notEqual(s2).and(e1.notEqual(e2));

            case ">":
                return s1.greaterOrEqual(e2).and(s1.greaterThan(s2));

            case ">=":
                return s1.greaterOrEqual(s2).and(e1.greaterOrEqual(e2));

            case "<":
                return e1.lessOrEqual(s2).and(s1.lessThan(s2));

            case "<=":
                return s1.lessOrEqual(s2).and(e1.lessOrEqual(e2));

            case "a":
                return s1.greaterOrEqual(e2).and(s1.greaterThan(s2));

            case "b":
                return e1.lessOrEqual(s2).and(s1.lessThan(s2));

            case "c":
                return s1.lessOrEqual(s2).and(e1.greaterThan(s2)).and(e1.greaterOrEqual(e2));

            case "m":
                return s1.equal(e2).or(e1.equal(s2));

            case "o":
                return s1.greaterOrEqual(e2).or(s2.greaterOrEqual(e1)).not().or(s1.equal(s2));

            case "s":
                return s1.equal(s2);

            case "f":
                return e1.equal(e2);

            default:
                throw new UnsupportedOperationException("Unknown boolean operation: " + op);
        }
    }

    @Override
    public FieldWrapper simpleOpBool(String op, FieldWrapper other) {
        if (other instanceof StaDateTimeExpression) {
            return new SimpleFieldWrapper(
                    specificOpBool(op, (StaDateTimeExpression) other));
        }
        if (other instanceof StaTimeIntervalExpression) {
            return new SimpleFieldWrapper(
                    specificOpBool(op, (StaTimeIntervalExpression) other));
        }
        throw new UnsupportedOperationException("Can not compare between Duration and " + other.getClass().getName());
    }

}
