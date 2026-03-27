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
package de.fraunhofer.iosb.ilt.frostserver.query.expression.function.date;

import de.fraunhofer.iosb.ilt.frostserver.query.expression.Expression;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DoubleConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DurationConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.StringConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.Function;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.FunctionTypeBinding;

/**
 * The totaloffsetminutes function.
 */
public class TotalOffsetMinutes extends Function<TotalOffsetMinutes> {

    public TotalOffsetMinutes() {
        super("totaloffsetminutes");
    }

    public TotalOffsetMinutes(Expression... parameters) {
        this();
        addParameters(parameters);
    }

    @Override
    protected void initAllowedTypeBindings() {
        addAllowedTypeBinding(new FunctionTypeBinding(DoubleConstant.class, DurationConstant.class));
        addAllowedTypeBinding(new FunctionTypeBinding(DoubleConstant.class, DurationConstant.class, StringConstant.class));
    }

    @Override
    public TotalOffsetMinutes newInstance() {
        return new TotalOffsetMinutes()
                .setAllowedTypeBindings(getAllowedTypeBindings());
    }

}
