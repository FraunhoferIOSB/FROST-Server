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
import java.time.OffsetDateTime;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;

/**
 * @author scf
 */
public class StaDateTimeExpression implements TimeExpression {

    /**
     * Flag indicating that the original time given was in utc.
     */
    private boolean utc = true;
    private final Field<OffsetDateTime> mixin;

    /**
     *
     * @param ts The constant timestamp.
     * @param utc Flag indicating that the original time given was in utc.
     */
    public StaDateTimeExpression(final OffsetDateTime ts, boolean utc) {
        mixin = DSL.inline(ts);
        this.utc = utc;
    }

    public StaDateTimeExpression(Field<OffsetDateTime> mixin) {
        this.mixin = mixin;
    }

    @Override
    public Field getDefaultField() {
        return mixin;
    }

    /**
     * @return Flag indicating that the original time given was in utc.
     */
    @Override
    public boolean isUtc() {
        return utc;
    }

    @Override
    public Field<OffsetDateTime> getDateTime() {
        return mixin;
    }

    private Object specificOp(String op, StaDurationExpression other) {
        switch (op) {
            case "+":
                return new StaDateTimeExpression(mixin.add(other.getDuration()));

            case "-":
                return new StaDateTimeExpression(mixin.sub(other.getDuration()));

            default:
                throw new UnsupportedOperationException("Can not mul or div a DateTime with a " + other.getClass().getName());
        }
    }

    private Object specificOp(String op, StaDateTimeExpression other) {
        if ("-".equals(op)) {
            return new StaDurationExpression(mixin, other.getDateTime());
        }
        throw new UnsupportedOperationException("Can not add, mul or div two DateTimes.");
    }

    @Override
    public Object simpleOp(String op, Object other) {
        if (other instanceof StaDurationExpression) {
            return specificOp(op, (StaDurationExpression) other);
        }
        if (other instanceof StaDateTimeExpression) {
            return specificOp(op, (StaDateTimeExpression) other);
        }
        throw new UnsupportedOperationException("Can not add, sub, mul or div a DateTime with a " + other.getClass().getName());
    }

    private Condition specificOpBool(String op, StaDateTimeExpression other) {
        Field<OffsetDateTime> t1 = mixin;
        Field<OffsetDateTime> t2 = other.mixin;
        switch (op) {
            case "=":
                return t1.equal(t2);

            case "!=":
                return t1.notEqual(t2);

            case ">":
                return t1.greaterThan(t2);

            case ">=":
                return t1.greaterOrEqual(t2);

            case "<":
                return t1.lessThan(t2);

            case "<=":
                return t1.lessOrEqual(t2);

            case "a":
                return t1.greaterThan(t2);

            case "b":
                return t1.lessThan(t2);

            case "c":
                throw new UnsupportedOperationException("First parameter of contains must be an interval.");

            case "m":
                return t1.equal(t2);

            case "o":
                return t1.equal(t2);

            case "s":
                return t1.equal(t2);

            case "f":
                return t1.equal(t2);

            default:
                throw new UnsupportedOperationException("Unknown boolean operation: " + op);
        }
    }

    private Condition specificOpBool(String op, StaTimeIntervalExpression other) {
        Field<OffsetDateTime> t1 = mixin;
        Field<OffsetDateTime> s2 = PgExpressionHandler.checkType(OffsetDateTime.class, other.getStart(), false);
        Field<OffsetDateTime> e2 = PgExpressionHandler.checkType(OffsetDateTime.class, other.getEnd(), false);
        switch (op) {
            case "=":
                return t1.equal(s2).and(t1.equal(e2));

            case ">":
                return t1.greaterOrEqual(e2).and(t1.greaterThan(s2));

            case ">=":
                return t1.greaterOrEqual(e2);

            case "<":
                return t1.lessThan(s2);

            case "<=":
                return t1.lessOrEqual(s2);

            case "a":
                return t1.greaterOrEqual(e2).and(t1.greaterThan(s2));

            case "b":
                return t1.lessThan(s2);

            case "c":
                throw new UnsupportedOperationException("First parameter of contains must be an interval.");

            case "m":
                return t1.equal(s2).or(t1.equal(e2));

            case "o":
                return t1.equal(s2).or(s2.lessOrEqual(t1).and(e2.greaterThan(t1)));

            case "s":
                return t1.equal(s2);

            case "f":
                return t1.equal(e2);

            default:
                throw new UnsupportedOperationException("Unknown boolean operation: " + op);
        }
    }

    @Override
    public Condition simpleOpBool(String op, Object other) {
        if (other instanceof StaDateTimeExpression) {
            return specificOpBool(op, (StaDateTimeExpression) other);
        }
        if (other instanceof StaTimeIntervalExpression) {
            return specificOpBool(op, (StaTimeIntervalExpression) other);
        }
        throw new UnsupportedOperationException("Can not compare between Duration and " + other.getClass().getName());
    }

}
