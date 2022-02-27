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

import de.fraunhofer.iosb.ilt.frostserver.query.expression.Expression;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.BooleanConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DoubleConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.IntegerConstant;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jab
 */
public class GreaterEqualTest {

    @Test
    public void testIntIntGreater() {
        Expression expectedResult = new BooleanConstant(true);
        Expression result = new GreaterEqual(new IntegerConstant(2), new IntegerConstant(1)).compress();
        assertEquals(expectedResult, result);
    }

    @Test
    public void testIntIntSmaller() {
        Expression expectedResult = new BooleanConstant(false);
        Expression result = new GreaterEqual(new IntegerConstant(1), new IntegerConstant(2)).compress();
        assertEquals(expectedResult, result);
    }

    @Test
    public void testIntIntEqual() {
        Expression expectedResult = new BooleanConstant(true);
        Expression result = new GreaterEqual(new IntegerConstant(1), new IntegerConstant(1)).compress();
        assertEquals(expectedResult, result);
    }

    @Test
    public void testDoubleDoubleGreater() {
        Expression expectedResult = new BooleanConstant(true);
        Expression result = new GreaterEqual(new DoubleConstant(2.5), new DoubleConstant(1.5)).compress();
        assertEquals(expectedResult, result);
    }

    @Test
    public void testDoubleDoubleSmaller() {
        Expression expectedResult = new BooleanConstant(false);
        Expression result = new GreaterEqual(new DoubleConstant(1.5), new DoubleConstant(2.5)).compress();
        assertEquals(expectedResult, result);
    }

    @Test
    public void testDoubleDoubleEqual() {
        Expression expectedResult = new BooleanConstant(true);
        Expression result = new GreaterEqual(new DoubleConstant(1.5), new DoubleConstant(1.5)).compress();
        assertEquals(expectedResult, result);
    }

    @Test
    public void testIntDoubleGreater() {
        Expression expectedResult = new BooleanConstant(true);
        Expression result = new GreaterEqual(new IntegerConstant(2), new DoubleConstant(1.5)).compress();
        assertEquals(expectedResult, result);
    }

    @Test
    public void testIntDoubleSmaller() {
        Expression expectedResult = new BooleanConstant(false);
        Expression result = new GreaterEqual(new IntegerConstant(1), new DoubleConstant(2.5)).compress();
        assertEquals(expectedResult, result);
    }

    @Test
    public void testIntDoubleEqual() {
        Expression expectedResult = new BooleanConstant(true);
        Expression result = new GreaterEqual(new IntegerConstant(1), new DoubleConstant(1.0)).compress();
        assertEquals(expectedResult, result);
    }

    @Test
    public void testDoubleIntGreater() {
        Expression expectedResult = new BooleanConstant(true);
        Expression result = new GreaterEqual(new DoubleConstant(2.5), new IntegerConstant(1)).compress();
        assertEquals(expectedResult, result);
    }

    @Test
    public void testDoubleIntSmaller() {
        Expression expectedResult = new BooleanConstant(false);
        Expression result = new GreaterEqual(new DoubleConstant(1.5), new IntegerConstant(2)).compress();
        assertEquals(expectedResult, result);
    }

    @Test
    public void testDoubleIntEqual() {
        Expression expectedResult = new BooleanConstant(true);
        Expression result = new GreaterEqual(new DoubleConstant(1.0), new IntegerConstant(1)).compress();
        assertEquals(expectedResult, result);
    }
}
