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
package de.fraunhofer.iosb.ilt.frostserver.query.expression.function.string;

import de.fraunhofer.iosb.ilt.frostserver.query.expression.Expression;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.IntegerConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.StringConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.Function;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.FunctionTypeBinding;

/**
 * The Substring function.
 */
public class Substring extends Function<Substring> {

    public Substring() {
        super("substring");
    }

    public Substring(Expression... parameters) {
        this();
        addParameters(parameters);
    }

    protected StringConstant eval(StringConstant p1, IntegerConstant p2) {
        return new StringConstant(p1.getValue().substring(p2.getValue().intValue()));
    }

    protected StringConstant eval(StringConstant p1, IntegerConstant p2, IntegerConstant p3) {
        return new StringConstant(p1.getValue().substring(p2.getValue().intValue(), p3.getValue().intValue()));
    }

    @Override
    protected void initAllowedTypeBindings() {
        addAllowedTypeBinding(new FunctionTypeBinding(IntegerConstant.class, StringConstant.class, IntegerConstant.class));
        addAllowedTypeBinding(new FunctionTypeBinding(IntegerConstant.class, StringConstant.class, IntegerConstant.class, IntegerConstant.class));
    }

    @Override
    public Substring newInstance() {
        return new Substring()
                .setAllowedTypeBindings(getAllowedTypeBindings());
    }

    @Override
    public Substring getSelf() {
        return this;
    }

}
