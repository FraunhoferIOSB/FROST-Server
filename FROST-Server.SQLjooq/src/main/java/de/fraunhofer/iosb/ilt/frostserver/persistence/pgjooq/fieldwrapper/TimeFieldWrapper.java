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

import net.time4j.Moment;
import org.jooq.Field;

/**
 * @author scf
 */
public interface TimeFieldWrapper extends FieldWrapper {

    public Field<Moment> getDateTime();

    public boolean isUtc();

    public default FieldWrapper eq(FieldWrapper other) {
        return simpleOpBool("=", other);
    }

    public default FieldWrapper neq(FieldWrapper other) {
        return simpleOpBool("!=", other);
    }

    public default FieldWrapper gt(FieldWrapper other) {
        return simpleOpBool(">", other);
    }

    public default FieldWrapper goe(FieldWrapper other) {
        return simpleOpBool(">=", other);
    }

    public default FieldWrapper lt(FieldWrapper other) {
        return simpleOpBool("<", other);
    }

    public default FieldWrapper loe(FieldWrapper other) {
        return simpleOpBool("<=", other);
    }

    public default FieldWrapper after(FieldWrapper other) {
        return simpleOpBool("a", other);
    }

    public default FieldWrapper before(FieldWrapper other) {
        return simpleOpBool("b", other);
    }

    public default FieldWrapper meets(FieldWrapper other) {
        return simpleOpBool("m", other);
    }

    public default FieldWrapper contains(FieldWrapper other) {
        return simpleOpBool("c", other);
    }

    public default FieldWrapper overlaps(FieldWrapper other) {
        return simpleOpBool("o", other);
    }

    public default FieldWrapper starts(FieldWrapper other) {
        return simpleOpBool("s", other);
    }

    public default FieldWrapper finishes(FieldWrapper other) {
        return simpleOpBool("f", other);
    }

    public default FieldWrapper add(FieldWrapper other) {
        return simpleOp("+", other);
    }

    public default FieldWrapper sub(FieldWrapper other) {
        return simpleOp("-", other);
    }

    public default FieldWrapper mul(FieldWrapper other) {
        return simpleOp("*", other);
    }

    public default FieldWrapper div(FieldWrapper other) {
        return simpleOp("/", other);
    }

    public FieldWrapper simpleOp(String op, FieldWrapper other);

    public FieldWrapper simpleOpBool(String op, FieldWrapper other);

}
