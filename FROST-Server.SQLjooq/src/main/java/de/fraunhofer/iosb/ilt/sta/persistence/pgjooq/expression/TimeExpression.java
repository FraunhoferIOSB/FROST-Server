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

import java.time.OffsetDateTime;
import org.jooq.Condition;
import org.jooq.Field;

/**
 *
 * @author scf
 */
public interface TimeExpression extends FieldWrapper {

    public Field<OffsetDateTime> getDateTime();

    public boolean isUtc();

    public default Condition eq(Object other) {
        return simpleOpBool("=", other);
    }

    public default Condition neq(Object other) {
        return simpleOpBool("!=", other);
    }

    public default Condition gt(Object other) {
        return simpleOpBool(">", other);
    }

    public default Condition goe(Object other) {
        return simpleOpBool(">=", other);
    }

    public default Condition lt(Object other) {
        return simpleOpBool("<", other);
    }

    public default Condition loe(Object other) {
        return simpleOpBool("<=", other);
    }

    public default Condition after(Object other) {
        return simpleOpBool("a", other);
    }

    public default Condition before(Object other) {
        return simpleOpBool("b", other);
    }

    public default Condition meets(Object other) {
        return simpleOpBool("m", other);
    }

    public default Condition contains(Object other) {
        return simpleOpBool("c", other);
    }

    public default Condition overlaps(Object other) {
        return simpleOpBool("o", other);
    }

    public default Condition starts(Object other) {
        return simpleOpBool("s", other);
    }

    public default Condition finishes(Object other) {
        return simpleOpBool("f", other);
    }

    public default Object add(Object other) {
        return simpleOp("+", other);
    }

    public default Object sub(Object other) {
        return simpleOp("-", other);
    }

    public default Object mul(Object other) {
        return simpleOp("*", other);
    }

    public default Object div(Object other) {
        return simpleOp("/", other);
    }

    public Object simpleOp(String op, Object other);

    public Condition simpleOpBool(String op, Object other);

}
