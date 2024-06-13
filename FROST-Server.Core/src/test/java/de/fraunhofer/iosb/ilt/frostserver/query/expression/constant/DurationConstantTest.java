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

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.time4j.Duration;
import net.time4j.IsoUnit;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jab
 */
class DurationConstantTest {

    @Test
    void parseDurationBasic() {
        DurationConstant result = DurationConstant.parse("P12DT23H59M59.999S");
        Duration<IsoUnit> expectedResult = Duration.ofPositive()
                .days(12)
                .hours(23)
                .minutes(59)
                .seconds(59)
                .millis(999)
                .build();
        assertEquals(expectedResult, result.getValue());
    }

}
