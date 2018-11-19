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
package de.fraunhofer.iosb.ilt.sta.query.expression.function.date;

import de.fraunhofer.iosb.ilt.sta.query.expression.Expression;
import de.fraunhofer.iosb.ilt.sta.query.expression.ExpressionVisitor;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.DateTimeConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.constant.TimeConstant;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.Function;
import de.fraunhofer.iosb.ilt.sta.query.expression.function.FunctionTypeBinding;

/**
 *
 * @author jab
 */
public class Time extends Function {

    public Time() {
        // Parameters added later...
    }

    public Time(Expression... parameters) {
        super(parameters);
    }

    protected TimeConstant eval(DateTimeConstant p1) {
        return new TimeConstant(p1.getValue().toLocalTime());
    }

    @Override
    protected void initAllowedTypeBindings() {
        allowedTypeBindings.add(new FunctionTypeBinding(TimeConstant.class, DateTimeConstant.class));
    }

    @Override
    public <O> O accept(ExpressionVisitor<O> visitor) {
        return visitor.visit(this);
    }

}
