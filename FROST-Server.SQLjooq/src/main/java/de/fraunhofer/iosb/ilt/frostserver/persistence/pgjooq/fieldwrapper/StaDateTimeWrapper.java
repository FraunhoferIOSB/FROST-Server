/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.fieldwrapper;

import static de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.Utils.INTERVAL_PARAM;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.bindings.MomentBinding;
import net.time4j.Moment;
import org.jooq.Condition;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author scf
 */
public class StaDateTimeWrapper implements TimeFieldWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(StaDateTimeWrapper.class.getName());

    /**
     * Flag indicating that the original time given was in utc.
     */
    private boolean utc = true;

    private final Field<Moment> field;

    /**
     *
     * @param ts The constant timestamp.
     * @param utc Flag indicating that the original time given was in utc.
     */
    public StaDateTimeWrapper(final Moment ts, boolean utc) {
        field = DSL.inline(ts, MomentBinding.dataType());
        this.utc = utc;
    }

    public StaDateTimeWrapper(Field<Moment> mixin) {
        this.field = mixin;
    }

    @Override
    public Field getDefaultField() {
        return getDateTime();
    }

    @Override
    public <T> Field<T> getFieldAsType(Class<T> expectedClazz, boolean canCast) {
        Class<Moment> fieldType = field.getType();
        if (expectedClazz.isAssignableFrom(fieldType)) {
            return (Field<T>) field;
        }
        if (canCast && expectedClazz == String.class) {
            return (Field<T>) field.cast(String.class);
        }
        LOGGER.debug("Not a {}: {} ({} -- {})", expectedClazz.getName(), field, field.getClass().getName(), fieldType.getName());
        return null;
    }

    /**
     * @return Flag indicating that the original time given was in utc.
     */
    @Override
    public boolean isUtc() {
        return utc;
    }

    @Override
    public Field<Moment> getDateTime() {
        return field;
    }

    private FieldWrapper specificOp(String op, StaDurationWrapper other) {
        switch (op) {
            case "+":
            case "-":
                String template = "(? " + op + " " + INTERVAL_PARAM + ")";
                Field<Moment> expression = DSL.field(template, MomentBinding.dataType(), field, other.getDuration());
                return new StaDateTimeWrapper(expression);

            default:
                throw new UnsupportedOperationException("Can not mul or div a DateTime with a " + other.getClass().getName());
        }
    }

    private FieldWrapper specificOp(String op, StaDateTimeWrapper other) {
        if ("-".equals(op)) {
            return new StaDurationWrapper(field, other.getDateTime());
        }
        throw new UnsupportedOperationException("Can not add, mul or div two DateTimes.");
    }

    @Override
    public FieldWrapper simpleOp(String op, FieldWrapper other) {
        if (other instanceof StaDurationWrapper staDurationWrapper) {
            return specificOp(op, staDurationWrapper);
        }
        if (other instanceof StaDateTimeWrapper staDateTimeWrapper) {
            return specificOp(op, staDateTimeWrapper);
        }
        throw new UnsupportedOperationException("Can not add, sub, mul or div a DateTime with a " + other.getClass().getName());
    }

    private Condition specificOpBool(String op, StaDateTimeWrapper other) {
        Field<Moment> t1 = field;
        Field<Moment> t2 = other.field;
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

    private Condition specificOpBool(String op, StaTimeIntervalWrapper other) {
        Field<Moment> t1 = field;
        Field<Moment> s2 = other.getStart();
        Field<Moment> e2 = other.getEnd();
        switch (op) {
            case "=":
                return t1.equal(s2).and(t1.equal(e2));

            case "!=":
                return t1.notEqual(s2).and(t1.notEqual(e2));

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
    public FieldWrapper simpleOpBool(String op, FieldWrapper other) {
        if (other instanceof StaDateTimeWrapper staDateTimeWrapper) {
            return new SimpleFieldWrapper(
                    specificOpBool(op, staDateTimeWrapper));
        }
        if (other instanceof StaTimeIntervalWrapper staTimeIntervalWrapper) {
            return new SimpleFieldWrapper(
                    specificOpBool(op, staTimeIntervalWrapper));
        }
        throw new UnsupportedOperationException("Can not compare between Duration and " + other.getClass().getName());
    }

}
