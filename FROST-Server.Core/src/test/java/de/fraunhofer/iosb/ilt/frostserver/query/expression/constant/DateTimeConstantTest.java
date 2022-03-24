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

import net.time4j.ClockUnit;
import net.time4j.PlainTimestamp;
import net.time4j.ZonalDateTime;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jab
 */
class DateTimeConstantTest {

    @Test
    void parseDateTimeBasic() {
        DateTimeConstant result = DateTimeConstant.parse("2012-12-03T07:16:23.999Z");
        ZonalDateTime expectedResult = PlainTimestamp.of(2012, 12, 3, 7, 16, 23)
                .plus(999, ClockUnit.MILLIS).inZonalView(DateTimeConstant.TIMEZONE_UTC);
        assertEquals(expectedResult, result.getValue());
    }

}
