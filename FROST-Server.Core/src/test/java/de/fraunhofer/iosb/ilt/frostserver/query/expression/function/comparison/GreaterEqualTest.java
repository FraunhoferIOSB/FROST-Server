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
package de.fraunhofer.iosb.ilt.frostserver.query.expression.function.comparison;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.fraunhofer.iosb.ilt.frostserver.query.expression.Expression;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.BooleanConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DoubleConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.IntegerConstant;
import org.junit.jupiter.api.Test;

/**
 *
 * @author jab
 */
class GreaterEqualTest {

    @Test
    void testIntIntGreater() {
        Expression expectedResult = new BooleanConstant(true);
        Expression result = new GreaterEqual(new IntegerConstant(2), new IntegerConstant(1)).compress();
        assertEquals(expectedResult, result);
    }

    @Test
    void testIntIntSmaller() {
        Expression expectedResult = new BooleanConstant(false);
        Expression result = new GreaterEqual(new IntegerConstant(1), new IntegerConstant(2)).compress();
        assertEquals(expectedResult, result);
    }

    @Test
    void testIntIntEqual() {
        Expression expectedResult = new BooleanConstant(true);
        Expression result = new GreaterEqual(new IntegerConstant(1), new IntegerConstant(1)).compress();
        assertEquals(expectedResult, result);
    }

    @Test
    void testDoubleDoubleGreater() {
        Expression expectedResult = new BooleanConstant(true);
        Expression result = new GreaterEqual(new DoubleConstant(2.5), new DoubleConstant(1.5)).compress();
        assertEquals(expectedResult, result);
    }

    @Test
    void testDoubleDoubleSmaller() {
        Expression expectedResult = new BooleanConstant(false);
        Expression result = new GreaterEqual(new DoubleConstant(1.5), new DoubleConstant(2.5)).compress();
        assertEquals(expectedResult, result);
    }

    @Test
    void testDoubleDoubleEqual() {
        Expression expectedResult = new BooleanConstant(true);
        Expression result = new GreaterEqual(new DoubleConstant(1.5), new DoubleConstant(1.5)).compress();
        assertEquals(expectedResult, result);
    }

    @Test
    void testIntDoubleGreater() {
        Expression expectedResult = new BooleanConstant(true);
        Expression result = new GreaterEqual(new IntegerConstant(2), new DoubleConstant(1.5)).compress();
        assertEquals(expectedResult, result);
    }

    @Test
    void testIntDoubleSmaller() {
        Expression expectedResult = new BooleanConstant(false);
        Expression result = new GreaterEqual(new IntegerConstant(1), new DoubleConstant(2.5)).compress();
        assertEquals(expectedResult, result);
    }

    @Test
    void testIntDoubleEqual() {
        Expression expectedResult = new BooleanConstant(true);
        Expression result = new GreaterEqual(new IntegerConstant(1), new DoubleConstant(1.0)).compress();
        assertEquals(expectedResult, result);
    }

    @Test
    void testDoubleIntGreater() {
        Expression expectedResult = new BooleanConstant(true);
        Expression result = new GreaterEqual(new DoubleConstant(2.5), new IntegerConstant(1)).compress();
        assertEquals(expectedResult, result);
    }

    @Test
    void testDoubleIntSmaller() {
        Expression expectedResult = new BooleanConstant(false);
        Expression result = new GreaterEqual(new DoubleConstant(1.5), new IntegerConstant(2)).compress();
        assertEquals(expectedResult, result);
    }

    @Test
    void testDoubleIntEqual() {
        Expression expectedResult = new BooleanConstant(true);
        Expression result = new GreaterEqual(new DoubleConstant(1.0), new IntegerConstant(1)).compress();
        assertEquals(expectedResult, result);
    }
}
