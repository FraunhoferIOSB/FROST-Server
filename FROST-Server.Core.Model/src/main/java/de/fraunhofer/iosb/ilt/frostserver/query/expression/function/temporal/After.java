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
package de.fraunhofer.iosb.ilt.frostserver.query.expression.function.temporal;

import de.fraunhofer.iosb.ilt.frostserver.query.expression.Expression;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.ExpressionVisitor;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.BooleanConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DateTimeConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.IntervalConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.Function;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.FunctionTypeBinding;

/**
 *
 * @author jab
 */
public class After extends Function {

    public After() {
        // Parameters added later...
    }

    public After(Expression... parameters) {
        super(parameters);
    }

    protected BooleanConstant eval(IntervalConstant p1, IntervalConstant p2) {
        return new BooleanConstant(p1.getValue().isAfter(p2.getValue()));
    }

    protected BooleanConstant eval(DateTimeConstant p1, IntervalConstant p2) {
        return new BooleanConstant(p1.getValue().toMoment().isAfter(p2.getValue().getStartAsMoment()));
    }

    protected BooleanConstant eval(IntervalConstant p1, DateTimeConstant p2) {
        return new BooleanConstant(p1.getValue().isAfter(p2.getValue().toMoment()));
    }

    protected BooleanConstant eval(DateTimeConstant p1, DateTimeConstant p2) {
        return new BooleanConstant(p1.getValue().toMoment().isAfter(p2.getValue()));
    }

    @Override
    protected void initAllowedTypeBindings() {
        allowedTypeBindings.add(new FunctionTypeBinding(BooleanConstant.class, IntervalConstant.class, IntervalConstant.class));
        allowedTypeBindings.add(new FunctionTypeBinding(BooleanConstant.class, DateTimeConstant.class, IntervalConstant.class));
        allowedTypeBindings.add(new FunctionTypeBinding(BooleanConstant.class, IntervalConstant.class, DateTimeConstant.class));
        allowedTypeBindings.add(new FunctionTypeBinding(BooleanConstant.class, DateTimeConstant.class, DateTimeConstant.class));
    }

    @Override
    public <O> O accept(ExpressionVisitor<O> visitor) {
        return visitor.visit(this);
    }

}
