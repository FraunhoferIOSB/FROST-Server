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
package de.fraunhofer.iosb.ilt.frostserver.query.expression;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.path.ParserContext;

/**
 * The core interface of expressions (Operators and Functions).
 *
 * @param <T> The exact type of the implementing class.
 */
public interface Expression<T extends Expression<T>> {

    public default T addParameter(Expression parameter) {
        throw new IllegalArgumentException("Expression of type " + getClass().getName() + " does not accept parameters.");
    }

    public Expression compress();

    /**
     * Get the (type) name of the expression.
     *
     * @return the name of the expression.
     */
    public String getName();

    /**
     * AdminOnly expressions can only be used by Administrators.
     *
     * @return true if this expression is only for admin use.
     */
    public boolean isAdminOnly();

    public boolean hasHandler();

    public ExpressionHandler getHandler();

    public <R> T setHandler(ExpressionHandler<T, ExpressionHelper<R>, R> handler);

    public <R> R handle(ExpressionHelper<R> h);

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

    /**
     * The context to use when rendering the expression.
     *
     * @param context the context to set
     * @return this.
     */
    public T setContext(DynamicContext context);

    /**
     * Create an new instance of this Expression.
     *
     * @return A new instance of this Expression without any parameters.
     */
    public T newInstance();

    public default T getSelf() {
        return (T) this;
    }

}
