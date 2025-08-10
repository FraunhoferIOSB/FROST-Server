/*
 * Copyright (C) 2024 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.query.expression.function.logical;

import de.fraunhofer.iosb.ilt.frostserver.query.expression.Expression;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.BooleanConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.Function;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.FunctionTypeBinding;

/**
 * The and operator.
 */
public class And extends Function<And> {

    public And() {
        super("and");
    }

    public And(Expression... parameters) {
        this();
        addParameters(parameters);
    }

    protected BooleanConstant eval(BooleanConstant p1, BooleanConstant p2) {
        return new BooleanConstant(p1.getValue() && p2.getValue());
    }

    @Override
    protected void initAllowedTypeBindings() {
        addAllowedTypeBinding(new FunctionTypeBinding(BooleanConstant.class, BooleanConstant.class, BooleanConstant.class));
    }

    @Override
    public And newInstance() {
        return new And()
                .setAllowedTypeBindings(getAllowedTypeBindings());
    }

    @Override
    public And getSelf() {
        return this;
    }
}
