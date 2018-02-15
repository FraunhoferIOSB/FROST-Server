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
import com.querydsl.core.types.dsl.StringExpression;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.PgExpressionHandler;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.DurationConstant;
import java.sql.Timestamp;

/**
 *
 * @author scf
 */
public class ConstantDurationExpression implements TimeExpression {

    final StringExpression duration;

    public ConstantDurationExpression(final DurationConstant duration) {
        this.duration = new ConstantStringExpression(duration.asISO8601());
    }

    public ConstantDurationExpression(final StringExpression duration) {
        this.duration = duration;
    }

    public StringExpression getDuration() {
        return duration;
    }

    @Override
    public Object accept(Visitor vstr, Object c) {
        throw new UnsupportedOperationException("visit on ConstantDurationExpression not supported.");
    }

    @Override
    public Class getType() {
        return DurationConstant.class;
    }

    @Override
    public BooleanExpression eq(Expression<?> other) {
        if (other instanceof ConstantDurationExpression) {
            return simpleOpBool("=", other);
        }
        throw new UnsupportedOperationException("Can not compare Duration to " + other.getClass().getName());
    }

    @Override
    public BooleanExpression neq(Expression<?> other) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public BooleanExpression gt(Expression<?> other) {
        return eq(other).not();
    }

    @Override
    public BooleanExpression goe(Expression<?> other) {
        if (other instanceof ConstantDurationExpression) {
            return simpleOpBool(">=", other);
        }
        throw new UnsupportedOperationException("Can not compare Duration to " + other.getClass().getName());
    }

    @Override
    public BooleanExpression lt(Expression<?> other) {
        if (other instanceof ConstantDurationExpression) {
            return simpleOpBool("<", other);
        }
        throw new UnsupportedOperationException("Can not compare Duration to " + other.getClass().getName());
    }

    @Override
    public BooleanExpression loe(Expression<?> other) {
        if (other instanceof ConstantDurationExpression) {
            return simpleOpBool("<=", other);
        }
        throw new UnsupportedOperationException("Can not compare Duration to " + other.getClass().getName());
    }

    @Override
    public Expression<?> add(Expression<?> other) {
        return simpleOp("+", other);
    }

    @Override
    public Expression<?> sub(Expression<?> other) {
        return simpleOp("-", other);
    }

    @Override
    public Expression<?> subi(Expression<?> other) {
        return simpleOp("-", other, true);
    }

    @Override
    public Expression<?> mul(Expression<?> other) {
        return simpleOp("*", other);
    }

    @Override
    public Expression<?> div(Expression<?> other) {
        return simpleOp("/", other);
    }

    private Expression<?> simpleOp(String op, Expression<?> other) {
        return simpleOp(op, other, false);
    }

    private Expression<?> simpleOp(String op, Expression<?> other, boolean inverse) {
        if (other instanceof ConstantDurationExpression) {
            ConstantDurationExpression cd = (ConstantDurationExpression) other;
            String template = inverse ? "({1}::interval " + op + " {0}::interval)" : "({0}::interval " + op + " {1}::interval)";
            return Expressions.dateTimeTemplate(Timestamp.class, template, this.duration, cd.duration);
        }
        if (other instanceof TimeIntervalExpression) {
            TimeIntervalExpression ti = (TimeIntervalExpression) other;
            DateTimeExpression dtEnd = PgExpressionHandler.checkType(DateTimeExpression.class, ti.end, false);
            DateTimeExpression dtStart = PgExpressionHandler.checkType(DateTimeExpression.class, ti.start, false);
            String template = inverse ? "({1}::timestamp " + op + " {0}::interval)" : "({0}::interval " + op + " {1}::timestamp)";
            DateTimeTemplate<Timestamp> newStart = Expressions.dateTimeTemplate(Timestamp.class, template, duration, dtStart);
            DateTimeTemplate<Timestamp> newEnd = Expressions.dateTimeTemplate(Timestamp.class, template, duration, dtEnd);
            return new TimeIntervalExpression(newStart, newEnd);
        }
        if (other instanceof DateTimeExpression) {
            DateTimeExpression dt = (DateTimeExpression) other;
            String template = inverse ? "({1}::timestamp " + op + " {0}::interval)" : "({0}::interval " + op + " {1}::timestamp)";
            return Expressions.dateTimeTemplate(Timestamp.class, template, duration, dt);
        }
        throw new UnsupportedOperationException("Can not add, sub, mul or div with Duration and " + other.getClass().getName());
    }

    private BooleanExpression simpleOpBool(String op, Expression<?> other) {
        if (other instanceof ConstantDurationExpression) {
            ConstantDurationExpression cd = (ConstantDurationExpression) other;
            String template = "({0}::interval " + op + " {1}::interval)";
            return Expressions.booleanTemplate(template, this.duration, cd.duration);
        }
        throw new UnsupportedOperationException("Can not compare between Duration and " + other.getClass().getName());
    }

}
