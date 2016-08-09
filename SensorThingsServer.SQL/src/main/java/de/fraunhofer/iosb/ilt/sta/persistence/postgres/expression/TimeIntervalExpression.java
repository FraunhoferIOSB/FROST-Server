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

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Visitor;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.DateTimeTemplate;
import com.querydsl.core.types.dsl.Expressions;
import de.fraunhofer.iosb.ilt.sta.model.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.PgExpressionHandler;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.PropertyResolver;
import java.sql.Timestamp;
import java.util.Map;

/**
 * Some paths point to time-intervals that return two column references. If the
 * references include a start and end time, they are treated as a time interval.
 */
public class TimeIntervalExpression implements TimeExpression {

    final Expression<?> start;
    final Expression<?> end;

    public TimeIntervalExpression(Map<String, Expression<?>> expressions) {
        this.start = expressions.get(PropertyResolver.KEY_TIME_INTERVAL_START);
        this.end = expressions.get(PropertyResolver.KEY_TIME_INTERVAL_END);
    }

    public TimeIntervalExpression(Expression<?> start, Expression<?> end) {
        this.start = start;
        this.end = end;
    }

    public Expression<?> getStart() {
        return start;
    }

    public Expression<?> getEnd() {
        return end;
    }

    @Override
    public Object accept(Visitor vstr, Object c) {
        throw new UnsupportedOperationException("visit on TimeIntervalExpression not supported.");
    }

    @Override
    public Class getType() {
        return TimeInterval.class;
    }

    @Override
    public BooleanExpression eq(Expression<?> other) {
        if (other instanceof TimeIntervalExpression) {
            TimeIntervalExpression tiOther = (TimeIntervalExpression) other;
            DateTimeExpression ownStart = PgExpressionHandler.checkType(DateTimeExpression.class, start, false);
            DateTimeExpression othStart = PgExpressionHandler.checkType(DateTimeExpression.class, tiOther.start, false);
            DateTimeExpression ownEnd = PgExpressionHandler.checkType(DateTimeExpression.class, end, false);
            DateTimeExpression othEnd = PgExpressionHandler.checkType(DateTimeExpression.class, tiOther.end, false);
            return ownStart.eq(othStart).and(ownEnd.eq(othEnd));
        }
        if (other instanceof DateTimeExpression) {
            DateTimeExpression dtStart = PgExpressionHandler.checkType(DateTimeExpression.class, start, false);
            DateTimeExpression dtEnd = PgExpressionHandler.checkType(DateTimeExpression.class, end, false);
            return dtEnd.goe(other).and(dtStart.loe(other));
        }
        throw new UnsupportedOperationException("Can not compare TimeInterval to " + other.getClass().getName() + " for equality.");
    }

    @Override
    public BooleanExpression neq(Expression<?> other) {
        return eq(other).not();
    }

    @Override
    public BooleanExpression gt(Expression<?> other) {
        DateTimeExpression dt = PgExpressionHandler.checkType(DateTimeExpression.class, start, false);
        if (other instanceof DateTimeExpression) {
            return dt.gt(other);
        }
        if (other instanceof TimeIntervalExpression) {
            TimeIntervalExpression ti = (TimeIntervalExpression) other;
            return dt.gt(ti.getEnd());
        }
        throw new UnsupportedOperationException("Can not compare TimeInterval to " + other.getClass().getName());
    }

    @Override
    public BooleanExpression ge(Expression<?> other) {
        if (other instanceof DateTimeExpression) {
            DateTimeExpression dt = PgExpressionHandler.checkType(DateTimeExpression.class, end, false);
            return dt.goe(other);
        }
        throw new UnsupportedOperationException("GreaterEqual is ill defined for time intervals. Use before, after, starts, finishes, during, contains, meets or overlaps.");
    }

    @Override
    public BooleanExpression lt(Expression<?> other) {
        DateTimeExpression dt = PgExpressionHandler.checkType(DateTimeExpression.class, end, false);
        if (other instanceof DateTimeExpression) {
            return dt.lt(other);
        }
        if (other instanceof TimeIntervalExpression) {
            TimeIntervalExpression ti = (TimeIntervalExpression) other;
            return dt.lt(ti.getStart());
        }
        throw new UnsupportedOperationException("Can not compare TimeInterval to " + other.getClass().getName());
    }

    @Override
    public BooleanExpression le(Expression<?> other) {
        if (other instanceof DateTimeExpression) {
            DateTimeExpression dt = PgExpressionHandler.checkType(DateTimeExpression.class, start, false);
            return dt.loe(other);
        }
        throw new UnsupportedOperationException("LessEqual is ill defined for time intervals. Use before, after, starts, finishes, during, contains, meets or overlaps.");
    }

    @Override
    public TimeIntervalExpression add(Expression<?> other) {
        return simpleOp("+", other);
    }

    @Override
    public TimeExpression subi(Expression<?> other) {
        if (other instanceof DateTimeExpression) {
            // We calculate with the start time and return a duration.
            DateTimeExpression dtStart = PgExpressionHandler.checkType(DateTimeExpression.class, start, false);
            String template = "({0}::timestamp - {1}::timestamp)";
            return new ConstantDurationExpression(Expressions.stringTemplate(template, other, dtStart));
        }
        throw new UnsupportedOperationException("Can not subtract TimeInterval from anything.");
    }

    @Override
    public TimeExpression sub(Expression<?> other) {
        if (other instanceof DateTimeExpression) {
            // We calculate with the start time and return a duration.
            DateTimeExpression dtStart = PgExpressionHandler.checkType(DateTimeExpression.class, start, false);
            String template = "({0}::timestamp - {1}::timestamp)";
            return new ConstantDurationExpression(Expressions.stringTemplate(template, dtStart, other));
        }
        return simpleOp("-", other);
    }

    @Override
    public TimeIntervalExpression mul(Expression<?> other) {
        return simpleOp("*", other);
    }

    @Override
    public TimeIntervalExpression div(Expression<?> other) {
        return simpleOp("/", other);
    }

    private TimeIntervalExpression simpleOp(String op, Expression<?> other) {
        if (other instanceof ConstantDurationExpression) {
            ConstantDurationExpression cd = (ConstantDurationExpression) other;
            DateTimeExpression dtEnd = PgExpressionHandler.checkType(DateTimeExpression.class, end, false);
            DateTimeExpression dtStart = PgExpressionHandler.checkType(DateTimeExpression.class, start, false);
            String template = "({0}::timestamp " + op + " {1}::interval)";
            DateTimeTemplate<Timestamp> newStart = Expressions.dateTimeTemplate(Timestamp.class, template, dtStart, cd.duration);
            DateTimeTemplate<Timestamp> newEnd = Expressions.dateTimeTemplate(Timestamp.class, template, dtEnd, cd.duration);
            return new TimeIntervalExpression(newStart, newEnd);
        }
        throw new UnsupportedOperationException("Can not add TimeInterval and " + other.getClass().getName());
    }

}
