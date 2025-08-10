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

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.path.ParserContext;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.ExpressionHandler;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.ExpressionHelper;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Value;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The abstract class for constant values.
 *
 * @param <T> The exact type of the implementing class.
 * @param <V> The type of the constant value.
 */
public abstract class Constant<T extends Constant<T, V>, V> implements Value<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Constant.class.getName());

    private String name;
    private ExpressionHandler handler;

    protected V value;

    protected Constant(String name) {
        this.name = name;
    }

    protected Constant(String name, V value) {
        this(name);
        this.value = value;
    }

    public V getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean hasHandler() {
        return handler != null;
    }

    @Override
    public ExpressionHandler getHandler() {
        return handler;
    }

    @Override
    public <R> T setHandler(ExpressionHandler<T, ExpressionHelper<R>, R> handler) {
        this.handler = handler;
        return getSelf();
    }

    @Override
    public <R> R handle(ExpressionHelper<R> h) {
        if (!hasHandler()) {
            LOGGER.error("No hanlder for {} ({})", getName(), getClass());
        }
        return (R) handler.handle(this, h);
    }

    @Override
    public void validate(ParserContext context, EntityType type) {
        // Nothing to validate by default.
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final T other = (T) obj;
        return Objects.equals(this.value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public T newInstance() {
        throw new UnsupportedOperationException("Can not instantiate constants using newInstance");
    }

}
