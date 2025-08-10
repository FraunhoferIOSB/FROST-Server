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
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.ConstantList;

/**
 * The in function.
 */
public class In extends ComparisonFunction<In> {

    public In() {
        super("in");
    }

    public In(Expression... parameters) {
        this();
        addParameters(parameters);
    }

    public BooleanConstant eval(Constant p1, ConstantList p2) {
        return new BooleanConstant(p2.contains(p1));
    }

    @Override
    public In newInstance() {
        return new In()
                .setAllowedTypeBindings(getAllowedTypeBindings());
    }

    @Override
    public In getSelf() {
        return this;
    }

}
