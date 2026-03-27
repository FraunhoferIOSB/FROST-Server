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

import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.text.ParseException;
import net.time4j.PlainTime;
import net.time4j.format.expert.Iso8601Format;

/**
 * A constant plain time.
 */
public class TimeConstant extends Constant<TimeConstant, PlainTime> {

    public static final String EXPR_NAME_TIMECONSTANT = "timeconstant";

    public TimeConstant() {
        super(EXPR_NAME_TIMECONSTANT);
    }

    public TimeConstant(PlainTime value) {
        super(EXPR_NAME_TIMECONSTANT, value);
    }

    public TimeConstant(String value) throws ParseException {
        super(EXPR_NAME_TIMECONSTANT, Iso8601Format.EXTENDED_WALL_TIME.parse(value));
    }

    @Override
    public String toUrl() {
        return getValue().toString();
    }

    public static TimeConstant parse(String value) {
        try {
            return new TimeConstant(value);
        } catch (ParseException ex) {
            throw new IllegalArgumentException("Failed to parse PlainTime " + StringHelper.cleanForLogging(value), ex);
        }
    }
}
