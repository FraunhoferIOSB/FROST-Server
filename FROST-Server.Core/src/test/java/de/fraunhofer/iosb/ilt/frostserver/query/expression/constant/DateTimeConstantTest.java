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
package de.fraunhofer.iosb.ilt.frostserver.query.expression.constant;

import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DateTimeConstant;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

/**
 *
 * @author jab
 */
public class DateTimeConstantTest {

    public DateTimeConstantTest() {
    }

    @Test
    public void parseDateTime_Basic_Success() {
        DateTimeConstant result = new DateTimeConstant("2012-12-03T07:16:23.999Z");
        DateTime expectedResult = new DateTime()
                .withZone(DateTimeZone.UTC)
                .withYear(2012)
                .withMonthOfYear(12)
                .withDayOfMonth(03)
                .withHourOfDay(7)
                .withMinuteOfHour(16)
                .withSecondOfMinute(23)
                .withMillisOfSecond(999);
        assert (result.getValue().equals(expectedResult));
    }

}
