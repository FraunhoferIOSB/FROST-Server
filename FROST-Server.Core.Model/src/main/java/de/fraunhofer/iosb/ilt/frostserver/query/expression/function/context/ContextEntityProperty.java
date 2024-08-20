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
package de.fraunhofer.iosb.ilt.frostserver.query.expression.function.context;

import de.fraunhofer.iosb.ilt.frostserver.query.expression.Expression;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.ExpressionVisitor;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.constant.StringConstant;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.Function;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.FunctionTypeBinding;
import org.apache.commons.lang3.NotImplementedException;

/**
 * A function that returns a property from the entity in the DynamicContext.
 *
 * TODO: Work in progress.
 */
public class ContextEntityProperty extends Function {

    public ContextEntityProperty() {
        // Parameters added later...
    }

    public ContextEntityProperty(Expression... parameters) {
        super(parameters);
    }

    public String getValue() {
        throw new NotImplementedException("ContextEntityProperty is work in progress.");
    }

    @Override
    protected void initAllowedTypeBindings() {
        allowedTypeBindings.add(new FunctionTypeBinding(StringConstant.class));
    }

    @Override
    public <O> O accept(ExpressionVisitor<O> visitor) {
        return visitor.visit(this);
    }

}
