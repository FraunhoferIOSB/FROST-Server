/*
 * Copyright (C) 2016 Fraunhofer IOSB.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq;

/**
 * The type of the result.
 *
 * @author Hylke van der Schaaf
 */
public enum ResultType {
    NUMBER,
    BOOLEAN,
    OBJECT_ARRAY,
    STRING;
    private final short sqlValue;

    private ResultType() {
        this.sqlValue = (short) ordinal();
    }

    public short sqlValue() {
        return sqlValue;
    }

    public static ResultType fromSqlValue(int sqlValue) {
        return values()[sqlValue];
    }
}
