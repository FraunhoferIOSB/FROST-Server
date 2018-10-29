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
package de.fraunhofer.iosb.ilt.sta.persistence.postgres.expression;

import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Visitor;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.DateTimeTemplate;
import com.querydsl.core.types.dsl.Expressions;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.PgExpressionHandler;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.Utils.INTERVAL_1;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.Utils.TIMESTAMP_0;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.Utils.TIMESTAMP_1;
import java.sql.Timestamp;
import java.util.Map;

/**
 * Some paths point to time-intervals that return two column references. If the
 * references include a start and end time, they are treated as a time interval.
 */
public class StaTimeIntervalExpression implements TimeExpression {

    public static final String KEY_TIME_INTERVAL_START = "tStart";
    public static final String KEY_TIME_INTERVAL_END = "tEnd";
    private static final String INCOMPATIBLE_OP = "Incompatible operator: Interval '";
    /**
     * Flag indicating that the original time given was in utc.
     */
    private boolean utc = true;
    final DateTimeExpression<Timestamp> start;
    final DateTimeExpression<Timestamp> end;

    public StaTimeIntervalExpression(Map<String, Expression<?>> expressions) {
        this.start = (DateTimeExpression) expressions.get(KEY_TIME_INTERVAL_START);
        this.end = (DateTimeExpression) expressions.get(KEY_TIME_INTERVAL_END);
    }

    public StaTimeIntervalExpression(DateTimeExpression<Timestamp> start, DateTimeExpression<Timestamp> end) {
        this.start = start;
        this.end = end;
    }

    public StaTimeIntervalExpression(Timestamp start, Timestamp end) {
        this.start = StaDateTimeExpression.createDateTimeExpression(ConstantImpl.create(start));
        this.end = StaDateTimeExpression.createDateTimeExpression(ConstantImpl.create(end));
    }

    public DateTimeExpression<Timestamp> getStart() {
        return start;
    }

    public DateTimeExpression<Timestamp> getEnd() {
        return end;
    }

    @Override
    public DateTimeExpression<Timestamp> getDateTime() {
        return start;
    }

    @Override
    public boolean isUtc() {
        return utc;
    }

    @Override
    public Object accept(Visitor vstr, Object c) {
        throw new UnsupportedOperationException("visit on TimeIntervalExpression not supported.");
    }

    @Override
    public Class getType() {
        return TimeInterval.class;
    }

    private Expression<?> specificOp(String op, StaDurationExpression other) {
        switch (op) {
            case "+":
            case "-":
                DateTimeExpression dtEnd = PgExpressionHandler.checkType(DateTimeExpression.class, end, false);
                DateTimeExpression dtStart = PgExpressionHandler.checkType(DateTimeExpression.class, start, false);
                String template = "(" + TIMESTAMP_0 + " " + op + " " + INTERVAL_1 + ")";
                DateTimeTemplate<Timestamp> newStart = Expressions.dateTimeTemplate(Timestamp.class, template, dtStart, other.getDuration());
                DateTimeTemplate<Timestamp> newEnd = Expressions.dateTimeTemplate(Timestamp.class, template, dtEnd, other.getDuration());
                return new StaTimeIntervalExpression(newStart, newEnd);

            default:
                throw new UnsupportedOperationException(INCOMPATIBLE_OP + op + "' " + other.getClass().getName());

        }
    }

    private Expression<?> specificOp(String op, StaDateTimeExpression other) {
        if ("-".equals(op)) {
            // We calculate with the start time and return a duration.
            DateTimeExpression dtStart = PgExpressionHandler.checkType(DateTimeExpression.class, start, false);
            String template = "(" + TIMESTAMP_0 + " - " + TIMESTAMP_1 + ")";
            return new StaDurationExpression(Expressions.stringTemplate(template, dtStart, other.getDateTime()));

        } else {
            throw new UnsupportedOperationException(INCOMPATIBLE_OP + op + "' " + other.getClass().getName());
        }
    }

    private Expression<?> specificOp(String op, StaTimeIntervalExpression other) {
        if ("-".equals(op)) {
            // We calculate with the start time and return a duration.
            DateTimeExpression s1 = PgExpressionHandler.checkType(DateTimeExpression.class, start, false);
            DateTimeExpression s2 = PgExpressionHandler.checkType(DateTimeExpression.class, other.start, false);
            String template = "(" + TIMESTAMP_0 + " - " + TIMESTAMP_1 + ")";
            return new StaDurationExpression(Expressions.stringTemplate(template, s1, s2));

        } else {
            throw new UnsupportedOperationException(INCOMPATIBLE_OP + op + "' " + other.getClass().getName());
        }
    }

    @Override
    public Expression<?> simpleOp(String op, Expression<?> other) {
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

    private BooleanExpression specificOpBool(String op, StaDateTimeExpression other) {
        DateTimeExpression s1 = PgExpressionHandler.checkType(DateTimeExpression.class, start, false);
        DateTimeExpression e1 = PgExpressionHandler.checkType(DateTimeExpression.class, end, false);
        DateTimeExpression t2 = other.getDateTime();
        switch (op) {
            case "=":
                return s1.eq(t2).and(e1.eq(t2));

            case ">":
                return s1.gt(t2);

            case ">=":
                return s1.goe(t2);

            case "<":
                return e1.loe(t2).and(s1.lt(t2));

            case "<=":
                return e1.loe(t2);

            case "a":
                return s1.gt(t2);

            case "b":
                return e1.loe(t2).and(s1.lt(t2));

            case "c":
                return s1.loe(t2).and(e1.gt(t2));

            case "m":
                return s1.eq(t2).or(e1.eq(t2));

            case "o":
                return s1.eq(t2).or(s1.loe(t2).and(e1.gt(t2)));

            case "s":
                return s1.eq(t2);

            case "f":
                return e1.eq(t2);

            default:
                throw new UnsupportedOperationException("Unknown boolean operation: " + op);
        }
    }

    private BooleanExpression specificOpBool(String op, StaTimeIntervalExpression other) {
        DateTimeExpression s1 = PgExpressionHandler.checkType(DateTimeExpression.class, start, false);
        DateTimeExpression e1 = PgExpressionHandler.checkType(DateTimeExpression.class, end, false);
        DateTimeExpression s2 = PgExpressionHandler.checkType(DateTimeExpression.class, other.getStart(), false);
        DateTimeExpression e2 = PgExpressionHandler.checkType(DateTimeExpression.class, other.getEnd(), false);
        switch (op) {
            case "=":
                return s1.eq(s2).and(e1.eq(e2));

            case ">":
                return s1.goe(e2).and(s1.gt(s2));

            case ">=":
                return s1.goe(s2).and(e1.goe(e2));

            case "<":
                return e1.loe(s2).and(s1.lt(s2));

            case "<=":
                return s1.loe(s2).and(e1.loe(e2));

            case "a":
                return s1.goe(e2).and(s1.gt(s2));

            case "b":
                return e1.loe(s2).and(s1.lt(s2));

            case "c":
                return s1.loe(s2).and(e1.gt(s2)).and(e1.goe(e2));

            case "m":
                return s1.eq(e2).or(e1.eq(s2));

            case "o":
                return s1.goe(e2).or(s2.goe(e1)).not().or(s1.eq(s2));

            case "s":
                return s1.eq(s2);

            case "f":
                return e1.eq(e2);

            default:
                throw new UnsupportedOperationException("Unknown boolean operation: " + op);
        }
    }

    @Override
    public BooleanExpression simpleOpBool(String op, Expression<?> other) {
        if (other instanceof StaDateTimeExpression) {
            return specificOpBool(op, (StaDateTimeExpression) other);
        }
        if (other instanceof StaTimeIntervalExpression) {
            return specificOpBool(op, (StaTimeIntervalExpression) other);
        }
        throw new UnsupportedOperationException("Can not compare between Duration and " + other.getClass().getName());
    }

}
