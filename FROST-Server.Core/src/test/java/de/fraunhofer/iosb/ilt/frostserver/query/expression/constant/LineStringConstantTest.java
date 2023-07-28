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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.fraunhofer.iosb.ilt.frostserver.util.TestHelper;
import org.geojson.LineString;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jab
 */
class LineStringConstantTest {

    @Test
    void testParseFromString2D() {
        final LineString expected = TestHelper.getLine(new Integer[]{30, 10}, new Integer[]{10, 30}, new Integer[]{40, 40});
        String text = "LINESTRING (30 10, 10 30, 40 40)";
        LineStringConstant result = LineStringConstant.parse(text);
        assertEquals(expected, result.getValue());

        text = "LINESTRING(30 10,10 30,40 40)";
        result = LineStringConstant.parse(text);
        assertEquals(expected, result.getValue());

        text = "LINESTRING  (30 10 , 10 30 , 40 40)";
        result = LineStringConstant.parse(text);
        assertEquals(expected, result.getValue());

        text = "LINESTRING      (30             10                 ,                    10                 30                ,           40             40        )         ";
        result = LineStringConstant.parse(text);
        assertEquals(expected, result.getValue());
    }

    @Test
    void testParseFromStringDecimal() {
        final LineString expected = TestHelper.getLine(new Double[]{30.1, 10.2}, new Double[]{0.1, .1}, new Double[]{40.0, 40.0});
        String text = "LINESTRING (30.1 10.2, 0.1 .1, 40.0 40.0)";
        LineStringConstant result = LineStringConstant.parse(text);
        assertEquals(expected, result.getValue());
    }

    @Test
    void testParseFromString3D() {
        final LineString expected = TestHelper.getLine(new Integer[]{30, 10, 10}, new Integer[]{10, 30, 10}, new Integer[]{40, 40, 40});
        String text = "LINESTRING  Z (30 10 10, 10 30 10, 40 40 40)";
        LineStringConstant result = LineStringConstant.parse(text);
        assertEquals(expected, result.getValue());

        text = "LINESTRINGZ(30 10 10,10 30 10,40 40 40)";
        result = LineStringConstant.parse(text);
        assertEquals(expected, result.getValue());

        text = "LINESTRINGZ  (30 10 10 , 10 30 10 , 40 40 40)";
        result = LineStringConstant.parse(text);
        assertEquals(expected, result.getValue());
    }

    @Test
    void testParseFromStringWithMixedDimensions() {
        String text = "LINESTRING (30 10, 10 30 40)";
        assertThrows(IllegalArgumentException.class, () -> LineStringConstant.parse(text));
    }

}
