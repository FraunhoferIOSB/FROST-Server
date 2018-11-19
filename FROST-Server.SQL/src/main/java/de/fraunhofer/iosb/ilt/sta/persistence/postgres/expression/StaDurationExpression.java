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
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberOperation;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringTemplate;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.PgExpressionHandler;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.Utils.INTERVAL_0;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.Utils.INTERVAL_1;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.Utils.TIMESTAMP_1;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.DurationConstant;
import java.sql.Timestamp;

/**
 *
 * @author scf
 */
public class StaDurationExpression implements TimeExpression {

    private final StringExpression duration;

    public StaDurationExpression(final DurationConstant duration) {
        this.duration = new ConstantStringExpression(duration.asISO8601());
    }

    public StaDurationExpression(final StringExpression duration) {
        this.duration = duration;
    }

    @Override
    public DateTimeExpression<Timestamp> getDateTime() {
        throw new UnsupportedOperationException("Can not convert duration to DateTime.");
    }

    @Override
    public boolean isUtc() {
        // durations are always utc.
        return true;
    }

    public StringExpression getDuration() {
        return duration;
    }

    @Override
    public Class getType() {
        return DurationConstant.class;
    }

    @Override
    public Object accept(Visitor vstr, Object c) {
        return duration.accept(vstr, c);
    }

    @Override
    public BooleanExpression after(Expression<?> other) {
        throw new UnsupportedOperationException("Can not use after with duration.");
    }

    @Override
    public BooleanExpression before(Expression<?> other) {
        throw new UnsupportedOperationException("Can not use before with duration.");
    }

    @Override
    public BooleanExpression meets(Expression<?> other) {
        throw new UnsupportedOperationException("Can not use meets with duration.");
    }

    @Override
    public BooleanExpression contains(Expression<?> other) {
        throw new UnsupportedOperationException("Can not use contais with duration.");
    }

    @Override
    public BooleanExpression overlaps(Expression<?> other) {
        throw new UnsupportedOperationException("Can not use overlaps with duration.");
    }

    @Override
    public BooleanExpression starts(Expression<?> other) {
        throw new UnsupportedOperationException("Can not use starts with duration.");
    }

    @Override
    public BooleanExpression finishes(Expression<?> other) {
        throw new UnsupportedOperationException("Can not use finishes with duration.");
    }

    private Expression<?> specificOp(String op, StaDurationExpression other) {
        String template = "(" + INTERVAL_0 + " " + op + " " + INTERVAL_1 + ")";
        DateTimeTemplate<Timestamp> expression = Expressions.dateTimeTemplate(Timestamp.class, template, this.duration, other.duration);
        return new StaDateTimeExpression(expression);
    }

    private Expression<?> specificOp(String op, StaTimeIntervalExpression other) {
        DateTimeExpression dtEnd = PgExpressionHandler.checkType(DateTimeExpression.class, other.end, false);
        DateTimeExpression dtStart = PgExpressionHandler.checkType(DateTimeExpression.class, other.start, false);
        String template = "(" + INTERVAL_0 + " " + op + " " + TIMESTAMP_1 + ")";
        DateTimeTemplate<Timestamp> newStart = Expressions.dateTimeTemplate(Timestamp.class, template, duration, dtStart);
        DateTimeTemplate<Timestamp> newEnd = Expressions.dateTimeTemplate(Timestamp.class, template, duration, dtEnd);
        return new StaTimeIntervalExpression(newStart, newEnd);
    }

    private Expression<?> specificOp(String op, StaDateTimeExpression other) {
        String template = "(" + INTERVAL_0 + " " + op + " " + TIMESTAMP_1 + ")";
        DateTimeTemplate<Timestamp> expression = Expressions.dateTimeTemplate(Timestamp.class, template, duration, other);
        return new StaDateTimeExpression(expression);
    }

    private Expression<?> specificOp(String op, NumberExpression other) {
        switch (op) {
            case "*":
            case "/":
                String template = "(" + INTERVAL_0 + " " + op + " ({1}))";
                StringTemplate expression = Expressions.stringTemplate(template, this.duration, other);
                return new StaDurationExpression(expression);

            default:
                throw new UnsupportedOperationException("Can not '" + op + "' with Duration and " + other.getClass().getName());
        }
    }

    @Override
    public Expression<?> simpleOp(String op, Expression<?> other) {
        if (other instanceof StaDurationExpression) {
            return specificOp(op, (StaDurationExpression) other);
        }
        if (other instanceof StaTimeIntervalExpression) {
            return specificOp(op, (StaTimeIntervalExpression) other);
        }
        if (other instanceof StaDateTimeExpression) {
            return specificOp(op, (StaDateTimeExpression) other);
        }
        if (other instanceof NumberOperation) {
            return specificOp(op, (NumberOperation) other);
        }
        if (other instanceof ListExpression) {
            NumberExpression nrOther = PgExpressionHandler.getSingleOfType(NumberExpression.class, other);
            return specificOp(op, nrOther);
        }
        throw new UnsupportedOperationException("Can not add, sub, mul or div with Duration and " + other.getClass().getName());
    }

    @Override
    public BooleanExpression simpleOpBool(String op, Expression<?> other) {
        if (other instanceof StaDurationExpression) {
            StaDurationExpression cd = (StaDurationExpression) other;
            String template = "(" + INTERVAL_0 + " " + op + " " + INTERVAL_1 + ")";
            return Expressions.booleanTemplate(template, this.duration, cd.duration);
        }
        throw new UnsupportedOperationException("Can not compare between Duration and " + other.getClass().getName());
    }

}
