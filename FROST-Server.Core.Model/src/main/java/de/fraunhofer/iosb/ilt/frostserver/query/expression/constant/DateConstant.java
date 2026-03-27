/*
 * Copyright (C) 2024 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.query.expression.constant;

import net.time4j.PlainDate;
import net.time4j.format.expert.Iso8601Format;

/**
 * A constant Date.
 */
public class DateConstant extends Constant<DateConstant, PlainDate> {

    public static final String EXPR_NAME_DATECONSTANT = "dateconstant";

    public DateConstant() {
        super(EXPR_NAME_DATECONSTANT);
    }

    public DateConstant(PlainDate value) {
        super(EXPR_NAME_DATECONSTANT, value);
    }

    public DateConstant(String value) {
        super(EXPR_NAME_DATECONSTANT);
        if (value.lastIndexOf('-') <= 0) {
            // We do not want simple integers be interpreted as a year.
            throw new IllegalArgumentException("Not a date: " + value);
        }
        this.value = PlainDate.parse(value, Iso8601Format.EXTENDED_CALENDAR_DATE);
    }

    @Override
    public String toUrl() {
        return getValue().toString();
    }

    public static DateConstant parse(String value) {
        return new DateConstant(value);
    }
}
