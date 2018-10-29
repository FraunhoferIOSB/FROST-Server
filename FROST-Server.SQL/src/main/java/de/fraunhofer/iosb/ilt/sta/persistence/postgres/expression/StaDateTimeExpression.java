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
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringTemplate;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.PgExpressionHandler;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.Utils.INTERVAL_1;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.Utils.TIMESTAMP_0;
import static de.fraunhofer.iosb.ilt.sta.persistence.postgres.Utils.TIMESTAMP_1;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.DurationConstant;
import java.sql.Timestamp;

/**
 * @author scf
 */
public class StaDateTimeExpression implements TimeExpression {

    public static DateTimeExpression createDateTimeExpression(final Expression<Timestamp> source) {
        return new DateTimeExpression<Timestamp>(source) {

            @Override
            public <R, C> R accept(Visitor<R, C> v, C context) {
                return this.mixin.accept(v, context);
            }
        };
    }
    /**
     * Flag indicating that the original time given was in utc.
     */
    private boolean utc = true;
    private final DateTimeExpression<Timestamp> mixin;

    /**
     *
     * @param ts The constant timestamp.
     * @param utc Flag indicating that the original time given was in utc.
     */
    public StaDateTimeExpression(final Timestamp ts, boolean utc) {
        mixin = createDateTimeExpression(ConstantImpl.create(ts));
        this.utc = utc;
    }

    public StaDateTimeExpression(DateTimeExpression mixin) {
        this.mixin = mixin;
    }

    /**
     * @return Flag indicating that the original time given was in utc.
     */
    @Override
    public boolean isUtc() {
        return utc;
    }

    @Override
    public DateTimeExpression<Timestamp> getDateTime() {
        return mixin;
    }

    @Override
    public Class getType() {
        return DurationConstant.class;
    }

    @Override
    public Object accept(Visitor vstr, Object c) {
        return mixin.accept(vstr, c);
    }

    private Expression<?> specificOp(String op, StaDurationExpression other) {
        switch (op) {
            case "+":
            case "-":
                String template = "(" + TIMESTAMP_0 + " " + op + " " + INTERVAL_1 + ")";
                DateTimeTemplate<Timestamp> expression = Expressions.dateTimeTemplate(Timestamp.class, template, mixin, other.getDuration());
                return new StaDateTimeExpression(expression);

            default:
                throw new UnsupportedOperationException("Can not mul or div a DateTime with a " + other.getClass().getName());
        }
    }

    private Expression<?> specificOp(String op, StaDateTimeExpression other) {
        if ("-".equals(op)) {
            String template = "(" + TIMESTAMP_0 + " " + op + " " + TIMESTAMP_1 + ")";
            StringTemplate expression = Expressions.stringTemplate(template, mixin, other.getDateTime());
            return new StaDurationExpression(expression);
        } else {
            throw new UnsupportedOperationException("Can not add, mul or div two DateTimes.");
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
        throw new UnsupportedOperationException("Can not add, sub, mul or div a DateTime with a " + other.getClass().getName());
    }

    private BooleanExpression specificOpBool(String op, StaDateTimeExpression other) {
        DateTimeExpression<Timestamp> t1 = mixin;
        DateTimeExpression<Timestamp> t2 = other.mixin;
        switch (op) {
            case "=":
                return t1.eq(t2);

            case ">":
                return t1.gt(t2);

            case ">=":
                return t1.goe(t2);

            case "<":
                return t1.lt(t2);

            case "<=":
                return t1.loe(t2);

            case "a":
                return t1.gt(t2);

            case "b":
                return t1.lt(t2);

            case "c":
                throw new UnsupportedOperationException("First parameter of contains must be an interval.");

            case "m":
                return t1.eq(t2);

            case "o":
                return t1.eq(t2);

            case "s":
                return t1.eq(t2);

            case "f":
                return t1.eq(t2);

            default:
                throw new UnsupportedOperationException("Unknown boolean operation: " + op);
        }
    }

    private BooleanExpression specificOpBool(String op, StaTimeIntervalExpression other) {
        DateTimeExpression<Timestamp> t1 = mixin;
        DateTimeExpression s2 = PgExpressionHandler.checkType(DateTimeExpression.class, other.getStart(), false);
        DateTimeExpression e2 = PgExpressionHandler.checkType(DateTimeExpression.class, other.getEnd(), false);
        switch (op) {
            case "=":
                return t1.eq(s2).and(t1.eq(e2));

            case ">":
                return t1.goe(e2).and(t1.gt(s2));

            case ">=":
                return t1.goe(e2);

            case "<":
                return t1.lt(s2);

            case "<=":
                return t1.loe(s2);

            case "a":
                return t1.goe(e2).and(t1.gt(s2));

            case "b":
                return t1.lt(s2);

            case "c":
                throw new UnsupportedOperationException("First parameter of contains must be an interval.");

            case "m":
                return t1.eq(s2).or(t1.eq(e2));

            case "o":
                return t1.eq(s2).or(s2.loe(t1).and(e2.gt(t1)));

            case "s":
                return t1.eq(s2);

            case "f":
                return t1.eq(e2);

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

    @Override
    public NumberExpression<Integer> year() {
        return getDateTime().year();
    }

}
