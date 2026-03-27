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
package de.fraunhofer.iosb.ilt.frostserver.query.expression.function.comparison;

import de.fraunhofer.iosb.ilt.frostserver.query.expression.Expression;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.BooleanConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.Constant;

/**
 * The eq operator.
 */
public class Equal extends ComparisonFunction<Equal> {

    public Equal() {
        super("eq");
    }

    public Equal(Expression... parameters) {
        this();
        addParameters(parameters);
    }

    public BooleanConstant eval(Constant p1, Constant p2) {
        return new BooleanConstant(p1.equals(p2));
    }

    @Override
    public Equal newInstance() {
        return new Equal()
                .setAllowedTypeBindings(getAllowedTypeBindings());
    }

}
