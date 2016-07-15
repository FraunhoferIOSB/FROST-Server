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
package de.fraunhofer.iosb.ilt.sta.query.expression.function.arithmetic;

import de.fraunhofer.iosb.ilt.sta.query.expression.Expression;
import de.fraunhofer.iosb.ilt.sta.query.expression.ExpressionVisitor;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.DoubleConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.IntegerConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.NumericConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.Function;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.FunctionTypeBinding;

/**
 *
 * @author jab
 */
public class Multiply extends Function {

    public Multiply() {
    }

    public Multiply(Expression... parameters) {
        super(parameters);
    }

    protected NumericConstant eval(NumericConstant p1, NumericConstant p2) {
        Number n1 = p1.getValue();
        Number n2 = p2.getValue();
        if (n1 instanceof Double || n2 instanceof Double) {
            return new DoubleConstant(n1.doubleValue() * n2.doubleValue());
        } else {
            return new IntegerConstant(n1.intValue() * n2.intValue());
        }
    }

    @Override
    protected void initAllowedTypeBindings() {
        allowedTypeBindings.add(new FunctionTypeBinding(IntegerConstant.class, IntegerConstant.class, IntegerConstant.class));
        allowedTypeBindings.add(new FunctionTypeBinding(DoubleConstant.class, DoubleConstant.class, DoubleConstant.class));
        allowedTypeBindings.add(new FunctionTypeBinding(DoubleConstant.class, IntegerConstant.class, DoubleConstant.class));
        allowedTypeBindings.add(new FunctionTypeBinding(DoubleConstant.class, DoubleConstant.class, IntegerConstant.class));
    }

    @Override
    public String toUrl() {
        return "(" + parameters.get(0).toUrl() + " mul " + parameters.get(1).toUrl() + ")";
    }

    @Override
    public <O> O accept(ExpressionVisitor<O> visitor) {
        return visitor.visit(this);
    }

}
