/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.query.expression;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.path.ParserContext;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.logical.And;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.logical.Or;

/**
 *
 * @author jab
 */
public interface Expression {

    public default void addParameter(Expression parameter) {
        throw new IllegalArgumentException("Expression of type " + getClass().getName() + " does not accept parameters.");
    }

    public Expression compress();

    public <O> O accept(ExpressionVisitor<O> visitor);

    /**
     * Validate the raw elements in this Expression against the given context
     * and EntityType, turning it into a usable Expression.
     *
     * @param context The parser context.
     * @param type The starting point of this Path, or null to validate against
     * the service Root.
     */
    public void validate(ParserContext context, EntityType type);

    /**
     * get the filter as it is expected to appear in a URL.
     *
     * @return The filter as it is expected to appear in a URL.
     */
    public String toUrl();

    public default Expression and(Expression that) {
        return new And(this, that);
    }

    public default Expression or(Expression that) {
        return new Or(this, that);
    }

}
