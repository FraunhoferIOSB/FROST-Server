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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jab
 */
class PointConstantTest {

    @Test
    void testparseFromStringSpaces() {
        String text = "POINT                     (      30                                              10    )";
        PointConstant result = PointConstant.parse(text);
        assertEquals(TestHelper.getPoint(30, 10), result.getValue());
    }

    @Test
    void testparseFromString2D() {
        String text = "POINT (30 10)";
        PointConstant result = PointConstant.parse(text);
        assertEquals(TestHelper.getPoint(30, 10), result.getValue());
    }

    @Test
    void testparseFromString3D() {
        String text = "POINT Z (30 10 10)";
        PointConstant result = PointConstant.parse(text);
        assertEquals(TestHelper.getPoint(30, 10, 10), result.getValue());
    }

    @Test
    void testparseFromStringWithWrongDimension1D() {
        String text = "POINT (10)";
        assertThrows(IllegalArgumentException.class, () -> PointConstant.parse(text));
    }

    @Test
    void testparseFromStringWithWrongDimension4D() {
        String text = "POINT (10 10 10 10)";
        assertThrows(IllegalArgumentException.class, () -> PointConstant.parse(text));
    }

}
