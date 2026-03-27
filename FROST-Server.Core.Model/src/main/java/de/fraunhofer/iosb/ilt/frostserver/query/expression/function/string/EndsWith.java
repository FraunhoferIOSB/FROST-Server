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
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.BooleanConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.StringConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.Function;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.FunctionTypeBinding;

/**
 * The EndsWith function.
 */
public class EndsWith extends Function<EndsWith> {

    public EndsWith() {
        super("endswith");
    }

    public EndsWith(Expression... parameters) {
        this();
        addParameters(parameters);
    }

    protected BooleanConstant eval(StringConstant p1, StringConstant p2) {
        return new BooleanConstant(p1.getValue().endsWith(p2.getValue()));
    }

    @Override
    protected void initAllowedTypeBindings() {
        addAllowedTypeBinding(new FunctionTypeBinding(BooleanConstant.class, StringConstant.class, StringConstant.class));
    }

    @Override
    public EndsWith newInstance() {
        return new EndsWith()
                .setAllowedTypeBindings(getAllowedTypeBindings());
    }

}
