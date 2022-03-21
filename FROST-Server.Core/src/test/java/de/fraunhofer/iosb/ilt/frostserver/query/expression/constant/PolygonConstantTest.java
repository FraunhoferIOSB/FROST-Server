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
import org.geojson.Polygon;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jab
 */
class PolygonConstantTest {

    @Test
    void testParseFromString2DOnlyExterior() {
        String text = "POLYGON ((30 10, 10 30, 40 40))";
        PolygonConstant result = PolygonConstant.parse(text);
        assertEquals(
                TestHelper.getPolygon(
                        2,
                        30, 10,
                        10, 30,
                        40, 40),
                result.getValue());
    }

    @Test
    void testParseFromString2DWithInteriorRing() {
        String text = "POLYGON ((30 10, 10 30, 40 40), (29 29, 29 30, 30 29))";
        PolygonConstant result = PolygonConstant.parse(text);
        Polygon polygon = TestHelper.getPolygon(
                2,
                30, 10,
                10, 30,
                40, 40);
        polygon.addInteriorRing(TestHelper.getPointList(
                2,
                29, 29,
                29, 30,
                30, 29));
        assertEquals(polygon, result.getValue());
    }

    @Test
    void testParseFromString2DWithMultpleInteriorRings() {
        String text = "POLYGON ((30 10, 10 30, 40 40), (29 29, 29 30, 30 29), (21 21, 21 22, 22 21))";
        PolygonConstant result = PolygonConstant.parse(text);
        Polygon polygon = TestHelper.getPolygon(
                2,
                30, 10,
                10, 30,
                40, 40);
        polygon.addInteriorRing(TestHelper.getPointList(
                2,
                29, 29,
                29, 30,
                30, 29));
        polygon.addInteriorRing(TestHelper.getPointList(
                2,
                21, 21,
                21, 22,
                22, 21));
        assertEquals(polygon, result.getValue());
    }

    @Test
    void testParseFromString3DOnlyExterior() {
        String text = "POLYGON Z((30 10 1, 10 30 1, 40 40 1))";
        PolygonConstant result = PolygonConstant.parse(text);
        assertEquals(
                TestHelper.getPolygon(
                        3,
                        30, 10, 1,
                        10, 30, 1,
                        40, 40, 1),
                result.getValue());
    }

    @Test
    void testParseFromString3DWithInteriorRing() {
        String text = "POLYGONZ ((30 10 1, 10 30 1, 40 40 1), (29 29 1, 29 30 1, 30 29 1))";
        PolygonConstant result = PolygonConstant.parse(text);
        Polygon polygon = TestHelper.getPolygon(
                3,
                30, 10, 1,
                10, 30, 1,
                40, 40, 1);
        polygon.addInteriorRing(TestHelper.getPointList(
                3,
                29, 29, 1,
                29, 30, 1,
                30, 29, 1));
        assertEquals(polygon, result.getValue());
    }

    @Test
    void testParseFromString3DWithMultpleInteriorRings() {
        String text = "POLYGON Z ((30 10 1, 10 30 1, 40 40 1), (29 29 1, 29 30 1, 30 29 1), (21 21 1, 21 22 1, 22 21 1))";
        PolygonConstant result = PolygonConstant.parse(text);
        Polygon polygon = TestHelper.getPolygon(
                3,
                30, 10, 1,
                10, 30, 1,
                40, 40, 1);
        polygon.addInteriorRing(TestHelper.getPointList(
                3,
                29, 29, 1,
                29, 30, 1,
                30, 29, 1));
        polygon.addInteriorRing(TestHelper.getPointList(
                3,
                21, 21, 1,
                21, 22, 1,
                22, 21, 1));
        assertEquals(polygon, result.getValue());
    }

    @Test
    void testParseFromStringWithMixedDimensions() {
        String text = "POLYGON ((30 10, 10 30 40))";
        assertThrows(IllegalArgumentException.class, () -> PolygonConstant.parse(text));
    }
}
