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
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DateConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DateTimeConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DoubleConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DurationConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.IntegerConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.StringConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.Function;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.FunctionTypeBinding;

/**
 *
 * @author jab
 */
public abstract class ComparisonFunction extends Function {

    protected ComparisonFunction() {
        super();
    }

    protected ComparisonFunction(Expression... parameters) {
        super(parameters);
    }

    @Override
    protected void initAllowedTypeBindings() {
        allowedTypeBindings.add(new FunctionTypeBinding(BooleanConstant.class, DateConstant.class, DateConstant.class));
        allowedTypeBindings.add(new FunctionTypeBinding(BooleanConstant.class, DateTimeConstant.class, DateTimeConstant.class));
        allowedTypeBindings.add(new FunctionTypeBinding(BooleanConstant.class, DoubleConstant.class, DoubleConstant.class));
        allowedTypeBindings.add(new FunctionTypeBinding(BooleanConstant.class, DurationConstant.class, DurationConstant.class));
        allowedTypeBindings.add(new FunctionTypeBinding(BooleanConstant.class, IntegerConstant.class, IntegerConstant.class));
        allowedTypeBindings.add(new FunctionTypeBinding(BooleanConstant.class, StringConstant.class, StringConstant.class));
        allowedTypeBindings.add(new FunctionTypeBinding(BooleanConstant.class, IntegerConstant.class, IntegerConstant.class));
    }

}
