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

import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.PointConstant;
import de.fraunhofer.iosb.ilt.frostserver.util.TestHelper;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 *
 * @author jab
 */
public class PointConstantTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    public PointConstantTest() {
    }

    @Test
    public void testConstructor_parseFromString2D_Success() {
        String text = "POINT (30 10)";
        PointConstant result = new PointConstant(text);
        Assert.assertEquals(TestHelper.getPoint(30, 10), result.getValue());
    }

    @Test
    public void testConstructor_parseFromString3D_Success() {
        String text = "POINT (30 10 10)";
        PointConstant result = new PointConstant(text);
        Assert.assertEquals(TestHelper.getPoint(30, 10, 10), result.getValue());
    }

    @Test
    public void testConstructor_parseFromStringWithWrongDimension1D_Exception() {
        String text = "POINT (10)";
        exception.expect(IllegalArgumentException.class);
        PointConstant pointConstant = new PointConstant(text);
        Assert.fail("Should have thrown an exception, but got a value: " + pointConstant.toString());
    }

    @Test
    public void testConstructor_parseFromStringWithWrongDimension4D_Exception() {
        String text = "POINT (10 10 10 10)";
        exception.expect(IllegalArgumentException.class);
        PointConstant pointConstant = new PointConstant(text);
        Assert.fail("Should have thrown an exception, but got a value: " + pointConstant.toString());
    }

}
