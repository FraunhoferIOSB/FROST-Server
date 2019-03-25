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
package de.fraunhofer.iosb.ilt.frostserver.query.expression.function.comparison;

import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.comparison.GreaterEqual;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Expression;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.BooleanConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DoubleConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.IntegerConstant;
import org.junit.Test;

/**
 *
 * @author jab
 */
public class GreaterEqualTest {

    public GreaterEqualTest() {
    }

    @Test
    public void testCompress_IntIntGreater_Success() {
        Expression expectedResult = new BooleanConstant(true);
        Expression result = new GreaterEqual(new IntegerConstant(2), new IntegerConstant(1)).compress();
        assert (result.equals(expectedResult));
    }

    @Test
    public void testCompress_IntIntSmaller_Success() {
        Expression expectedResult = new BooleanConstant(false);
        Expression result = new GreaterEqual(new IntegerConstant(1), new IntegerConstant(2)).compress();
        assert (result.equals(expectedResult));
    }

    @Test
    public void testCompress_IntIntEqual_Success() {
        Expression expectedResult = new BooleanConstant(true);
        Expression result = new GreaterEqual(new IntegerConstant(1), new IntegerConstant(1)).compress();
        assert (result.equals(expectedResult));
    }

    @Test
    public void testCompress_DoubleDoubleGreater_Success() {
        Expression expectedResult = new BooleanConstant(true);
        Expression result = new GreaterEqual(new DoubleConstant(2.5), new DoubleConstant(1.5)).compress();
        assert (result.equals(expectedResult));
    }

    @Test
    public void testCompress_DoubleDoubleSmaller_Success() {
        Expression expectedResult = new BooleanConstant(false);
        Expression result = new GreaterEqual(new DoubleConstant(1.5), new DoubleConstant(2.5)).compress();
        assert (result.equals(expectedResult));
    }

    @Test
    public void testCompress_DoubleDoubleEqual_Success() {
        Expression expectedResult = new BooleanConstant(true);
        Expression result = new GreaterEqual(new DoubleConstant(1.5), new DoubleConstant(1.5)).compress();
        assert (result.equals(expectedResult));
    }

    @Test
    public void testCompress_IntDoubleGreater_Success() {
        Expression expectedResult = new BooleanConstant(true);
        Expression result = new GreaterEqual(new IntegerConstant(2), new DoubleConstant(1.5)).compress();
        assert (result.equals(expectedResult));
    }

    @Test
    public void testCompress_IntDoubleSmaller_Success() {
        Expression expectedResult = new BooleanConstant(false);
        Expression result = new GreaterEqual(new IntegerConstant(1), new DoubleConstant(2.5)).compress();
        assert (result.equals(expectedResult));
    }

    @Test
    public void testCompress_IntDoubleEqual_Success() {
        Expression expectedResult = new BooleanConstant(true);
        Expression result = new GreaterEqual(new IntegerConstant(1), new DoubleConstant(1.0)).compress();
        assert (result.equals(expectedResult));
    }

    @Test
    public void testCompress_DoubleIntGreater_Success() {
        Expression expectedResult = new BooleanConstant(true);
        Expression result = new GreaterEqual(new DoubleConstant(2.5), new IntegerConstant(1)).compress();
        assert (result.equals(expectedResult));
    }

    @Test
    public void testCompress_DoubleIntSmaller_Success() {
        Expression expectedResult = new BooleanConstant(false);
        Expression result = new GreaterEqual(new DoubleConstant(1.5), new IntegerConstant(2)).compress();
        assert (result.equals(expectedResult));
    }

    @Test
    public void testCompress_DoubleIntEqual_Success() {
        Expression expectedResult = new BooleanConstant(true);
        Expression result = new GreaterEqual(new DoubleConstant(1.0), new IntegerConstant(1)).compress();
        assert (result.equals(expectedResult));
    }
}
