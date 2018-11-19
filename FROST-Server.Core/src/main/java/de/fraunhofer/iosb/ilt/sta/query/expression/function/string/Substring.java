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
package de.fraunhofer.iosb.ilt.sta.query.expression.function.string;

import de.fraunhofer.iosb.ilt.sta.query.expression.Expression;
import de.fraunhofer.iosb.ilt.sta.query.expression.ExpressionVisitor;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.IntegerConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.StringConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.Function;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.FunctionTypeBinding;

/**
 *
 * @author jab
 */
public class Substring extends Function {

    public Substring() {
        // Parameters added later...
    }

    public Substring(Expression... parameters) {
        super(parameters);
    }

    protected StringConstant eval(StringConstant p1, IntegerConstant p2) {
        return new StringConstant(p1.getValue().substring(p2.getValue()));
    }

    protected StringConstant eval(StringConstant p1, IntegerConstant p2, IntegerConstant p3) {
        return new StringConstant(p1.getValue().substring(p2.getValue(), p3.getValue()));
    }

    @Override
    protected void initAllowedTypeBindings() {
        allowedTypeBindings.add(new FunctionTypeBinding(IntegerConstant.class, StringConstant.class, IntegerConstant.class));
        allowedTypeBindings.add(new FunctionTypeBinding(IntegerConstant.class, StringConstant.class, IntegerConstant.class, IntegerConstant.class));
    }

    @Override
    public <O> O accept(ExpressionVisitor<O> visitor) {
        return visitor.visit(this);
    }

}
