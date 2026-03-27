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
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.DateTimeConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.IntegerConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.StringConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.TimeConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.Function;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.FunctionTypeBinding;

/**
 * The hour function.
 */
public class Hour extends Function<Hour> {

    public Hour() {
        super("hour");
    }

    public Hour(Expression... parameters) {
        this();
        addParameters(parameters);
    }

    protected IntegerConstant eval(DateTimeConstant p1) {
        return new IntegerConstant(p1.getValue().toMoment().toZonalTimestamp(UTC).getHour());
    }

    protected IntegerConstant eval(TimeConstant p1) {
        return new IntegerConstant(p1.getValue().getHour());
    }

    @Override
    protected void initAllowedTypeBindings() {
        addAllowedTypeBinding(new FunctionTypeBinding(IntegerConstant.class, TimeConstant.class));
        addAllowedTypeBinding(new FunctionTypeBinding(IntegerConstant.class, DateTimeConstant.class));
        addAllowedTypeBinding(new FunctionTypeBinding(IntegerConstant.class, TimeConstant.class, StringConstant.class));
        addAllowedTypeBinding(new FunctionTypeBinding(IntegerConstant.class, DateTimeConstant.class, StringConstant.class));
    }

    @Override
    public Hour newInstance() {
        return new Hour()
                .setAllowedTypeBindings(getAllowedTypeBindings());
    }
}
