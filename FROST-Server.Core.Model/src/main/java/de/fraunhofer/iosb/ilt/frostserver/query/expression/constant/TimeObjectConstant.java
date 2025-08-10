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
package de.fraunhofer.iosb.ilt.frostserver.query.expression.constant;

import de.fraunhofer.iosb.ilt.frostserver.model.ext.TimeObject;

/**
 * A constant time object.
 */
public class TimeObjectConstant extends Constant<TimeObjectConstant, TimeObject> {

    public static final String EXPR_NAME_TIMEOBJECTCONSTANT = "timeobjectconstant";

    public TimeObjectConstant() {
        super(EXPR_NAME_TIMEOBJECTCONSTANT);
    }

    public TimeObjectConstant(TimeObject value) {
        super(EXPR_NAME_TIMEOBJECTCONSTANT, value);
    }

    @Override
    public String toUrl() {
        return value.asISO8601();
    }

    @Override
    public TimeObjectConstant getSelf() {
        return this;
    }
}
