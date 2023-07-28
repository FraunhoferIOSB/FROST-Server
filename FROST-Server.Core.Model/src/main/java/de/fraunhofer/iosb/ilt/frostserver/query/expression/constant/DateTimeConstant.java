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
package de.fraunhofer.iosb.ilt.frostserver.query.expression.constant;

import static net.time4j.tz.ZonalOffset.UTC;

import de.fraunhofer.iosb.ilt.frostserver.query.expression.ExpressionVisitor;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import net.time4j.ZonalDateTime;
import net.time4j.engine.ChronoException;
import net.time4j.format.expert.Iso8601Format;
import net.time4j.tz.Timezone;

/**
 *
 * @author jab
 */
public class DateTimeConstant extends Constant<ZonalDateTime> {

    public static final Timezone TIMEZONE_UTC = Timezone.of(UTC);

    public DateTimeConstant(ZonalDateTime value) {
        super(value);
    }

    public DateTimeConstant(String value) {
        if (value.lastIndexOf('-') <= 0) {
            // We do not want simple integers be interpreted as a year.
            throw new IllegalArgumentException("Not a date: " + value);
        }
        this.value = ZonalDateTime.parse(value, Iso8601Format.EXTENDED_DATE_TIME_OFFSET);
    }

    @Override
    public String toUrl() {
        return StringHelper.FORMAT_MOMENT.print(getValue().toMoment());
    }

    @Override
    public <O> O accept(ExpressionVisitor<O> visitor) {
        return visitor.visit(this);
    }

    public static DateTimeConstant parse(String value) {
        try {
            return new DateTimeConstant(value);
        } catch (ChronoException ex) {
            throw new IllegalArgumentException("Failed to parse PlainTimestamp " + StringHelper.cleanForLogging(value), ex);
        }
    }

}
