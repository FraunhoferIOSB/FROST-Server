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

import static net.time4j.tz.ZonalOffset.UTC;

import de.fraunhofer.iosb.ilt.frostserver.query.expression.Expression;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DateConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DateTimeConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.Function;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.FunctionTypeBinding;

/**
 * The date function.
 */
public class Date extends Function<Date> {

    public Date() {
        super("date");
    }

    public Date(Expression... parameters) {
        this();
        addParameters(parameters);
    }

    protected DateConstant eval(DateTimeConstant p1) {
        return new DateConstant(p1.getValue().toMoment().toZonalTimestamp(UTC).getCalendarDate());
    }

    @Override
    protected void initAllowedTypeBindings() {
        addAllowedTypeBinding(new FunctionTypeBinding(DateTimeConstant.class, DateTimeConstant.class));
    }

    @Override
    public Date newInstance() {
        return new Date()
                .setAllowedTypeBindings(getAllowedTypeBindings());
    }

    @Override
    public Date getSelf() {
        return this;
    }

}
