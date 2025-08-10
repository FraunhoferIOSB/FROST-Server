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

import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import java.text.ParseException;
import net.time4j.range.MomentInterval;

/**
 * A constant time interval.
 */
public class IntervalConstant extends Constant<IntervalConstant, MomentInterval> {

    public static final String EXPR_NAME_INTERVALCONSTANT = "intervalconstant";

    public IntervalConstant() {
        super(EXPR_NAME_INTERVALCONSTANT);
    }

    public IntervalConstant(MomentInterval value) {
        super(EXPR_NAME_INTERVALCONSTANT, value);
    }

    public IntervalConstant(String value) throws ParseException {
        super(EXPR_NAME_INTERVALCONSTANT, MomentInterval.parseISO(value));
    }

    @Override
    public String toUrl() {
        return StringHelper.FORMAT_INTERVAL.print(getValue());
    }

    public static IntervalConstant parse(String value) {
        try {
            return new IntervalConstant(value);
        } catch (ParseException ex) {
            throw new IllegalArgumentException("Failed to parse MomentInterval " + StringHelper.cleanForLogging(value), ex);
        }
    }

    @Override
    public IntervalConstant getSelf() {
        return this;
    }

}
