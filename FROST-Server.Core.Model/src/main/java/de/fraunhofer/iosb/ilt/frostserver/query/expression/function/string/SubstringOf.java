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
 * The
 * <a href="https://docs.ogc.org/is/18-088/18-088.html#requirement-request-data-built-in-query-functions">STA
 * substringof</a> function. This is the STA defined inverse of
 * <a
 * href="https://docs.oasis-open.org/odata/odata/v4.01/os/part2-url-conventions/odata-v4.01-os-part2-url-conventions.html#sec_contains">OData
 * contains</a>
 */
public class SubstringOf extends Function<SubstringOf> {

    public SubstringOf() {
        super("substringof");
    }

    public SubstringOf(Expression... parameters) {
        this();
        addParameters(parameters);
    }

    protected BooleanConstant eval(StringConstant p1, StringConstant p2) {
        return new BooleanConstant(p2.getValue().contains(p1.getValue()));
    }

    @Override
    protected void initAllowedTypeBindings() {
        addAllowedTypeBinding(new FunctionTypeBinding(BooleanConstant.class, StringConstant.class, StringConstant.class));
    }

    @Override
    public SubstringOf newInstance() {
        return new SubstringOf()
                .setAllowedTypeBindings(getAllowedTypeBindings());
    }

    @Override
    public SubstringOf getSelf() {
        return this;
    }

}
