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

import de.fraunhofer.iosb.ilt.frostserver.util.TestHelper;
import org.geojson.LineString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jab
 */
public class LineStringConstantTest {

    @Test
    public void testParseFromString2D() {
        final LineString expected = TestHelper.getLine(new Integer[]{30, 10}, new Integer[]{10, 30}, new Integer[]{40, 40});
        String text = "LINESTRING (30 10, 10 30, 40 40)";
        LineStringConstant result = new LineStringConstant(text);
        assertEquals(expected, result.getValue());

        text = "LINESTRING(30 10,10 30,40 40)";
        result = new LineStringConstant(text);
        assertEquals(expected, result.getValue());

        text = "LINESTRING  (30 10 , 10 30 , 40 40)";
        result = new LineStringConstant(text);
        assertEquals(expected, result.getValue());

        text = "LINESTRING      (30             10                 ,                    10                 30                ,           40             40        )         ";
        result = new LineStringConstant(text);
        assertEquals(expected, result.getValue());
    }

    @Test
    public void testParseFromStringDecimal() {
        final LineString expected = TestHelper.getLine(new Double[]{30.1, 10.2}, new Double[]{0.1, .1}, new Double[]{40.0, 40.0});
        String text = "LINESTRING (30.1 10.2, 0.1 .1, 40.0 40.0)";
        LineStringConstant result = new LineStringConstant(text);
        assertEquals(expected, result.getValue());
    }

    @Test
    public void testParseFromString3D() {
        final LineString expected = TestHelper.getLine(new Integer[]{30, 10, 10}, new Integer[]{10, 30, 10}, new Integer[]{40, 40, 40});
        String text = "LINESTRING (30 10 10, 10 30 10, 40 40 40)";
        LineStringConstant result = new LineStringConstant(text);
        assertEquals(expected, result.getValue());

        text = "LINESTRING(30 10 10,10 30 10,40 40 40)";
        result = new LineStringConstant(text);
        assertEquals(expected, result.getValue());

        text = "LINESTRING  (30 10 10 , 10 30 10 , 40 40 40)";
        result = new LineStringConstant(text);
        assertEquals(expected, result.getValue());
    }

    @Test
    public void testParseFromStringWithMixedDimensions() {
        assertThrows(IllegalArgumentException.class, () -> {
            String text = "LINESTRING (30 10, 10 30 40)";
            LineStringConstant lineStringConstant = new LineStringConstant(text);
        });
    }

}
