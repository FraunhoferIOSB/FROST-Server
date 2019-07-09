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

import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.PolygonConstant;
import de.fraunhofer.iosb.ilt.frostserver.util.TestHelper;
import org.geojson.Polygon;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 *
 * @author jab
 */
public class PolygonConstantTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    public PolygonConstantTest() {
    }

    @Test
    public void testConstructor_parseFromString2DOnlyExterior_Success() {
        String text = "POLYGON ((30 10, 10 30, 40 40))";
        PolygonConstant result = new PolygonConstant(text);
        Assert.assertEquals(
                TestHelper.getPolygon(
                        2,
                        30, 10,
                        10, 30,
                        40, 40),
                result.getValue());
    }

    @Test
    public void testConstructor_parseFromString2DWithInteriorRing_Success() {
        String text = "POLYGON ((30 10, 10 30, 40 40), (29 29, 29 30, 30 29))";
        PolygonConstant result = new PolygonConstant(text);
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
        Assert.assertEquals(polygon, result.getValue());
    }

    @Test
    public void testConstructor_parseFromString2DWithMultpleInteriorRings_Success() {
        String text = "POLYGON ((30 10, 10 30, 40 40), (29 29, 29 30, 30 29), (21 21, 21 22, 22 21))";
        PolygonConstant result = new PolygonConstant(text);
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
        Assert.assertEquals(polygon, result.getValue());
    }

    @Test
    public void testConstructor_parseFromString3DOnlyExterior_Success() {
        String text = "POLYGON ((30 10 1, 10 30 1, 40 40 1))";
        PolygonConstant result = new PolygonConstant(text);
        Assert.assertEquals(
                TestHelper.getPolygon(
                        3,
                        30, 10, 1,
                        10, 30, 1,
                        40, 40, 1),
                result.getValue());
    }

    @Test
    public void testConstructor_parseFromString3DWithInteriorRing_Success() {
        String text = "POLYGON ((30 10 1, 10 30 1, 40 40 1), (29 29 1, 29 30 1, 30 29 1))";
        PolygonConstant result = new PolygonConstant(text);
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
        Assert.assertEquals(polygon, result.getValue());
    }

    @Test
    public void testConstructor_parseFromString3DWithMultpleInteriorRings_Success() {
        String text = "POLYGON ((30 10 1, 10 30 1, 40 40 1), (29 29 1, 29 30 1, 30 29 1), (21 21 1, 21 22 1, 22 21 1))";
        PolygonConstant result = new PolygonConstant(text);
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
        Assert.assertEquals(polygon, result.getValue());
    }

    @Test
    public void testConstructor_parseFromStringWithMixedDimensions_Exception() {
        String text = "POLYGON ((30 10, 10 30 40))";
        exception.expect(IllegalArgumentException.class);
        PolygonConstant polygonConstant = new PolygonConstant(text);
        Assert.fail("Should have thrown an exception, but got a value: " + polygonConstant.toString());
    }
}
