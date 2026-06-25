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

import de.fraunhofer.iosb.ilt.frostserver.query.PropertyPlaceholder;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.Function;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads available functions.
 */
public class FunctionRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(FunctionRegistry.class);

    private final Map<String, Expression<?>> expressionsByName = new HashMap<>();
    private final Map<Class<? extends Expression>, Expression<?>> expressionsByClass = new HashMap<>();
    // Cached path, since it is much used.
    private Path rootPath;

    public FunctionRegistry() {
        LOGGER.info("Initialising a new FunctionRegistry.");
    }

    public void registerExpression(Expression<?> function) {
        expressionsByClass.put(function.getClass(), function);
        String name = function.getName().toLowerCase(Locale.ROOT);
        if (expressionsByName.put(name, function) != null) {
            LOGGER.warn("    Replaced the definition of Function {}", name);
        } else {
            LOGGER.debug("    Registered Function {}", name);
        }
        if (function instanceof Path p) {
            rootPath = p;
        }
    }

    public Collection<Expression<?>> getExpressions() {
        return Collections.unmodifiableCollection(expressionsByName.values());
    }

    public <T extends Expression<T>> T getExpression(Class<T> clazz) {
        final Expression<?> expression = expressionsByClass.get(clazz);
        if (expression == null) {
            LOGGER.error("Unknown expression class: {}", clazz);
        }
        return (T) expression;
    }

    public Expression<?> getExpression(String name, boolean admin) {
        final Expression<?> expression = expressionsByName.get(name);
        if (expression == null) {
            throw new IllegalArgumentException("Unknown function name: " + name);
        }
        if (expression.isAdminOnly() && !admin) {
            throw new IllegalArgumentException("Unknown function name: " + name);
        }
        return expression.newInstance();
    }

    public Function<?> getFunction(String name, boolean admin) {
        final Expression<?> expression = expressionsByName.get(name);
        if ((expression instanceof Function<?> f)) {
            if (f.isAdminOnly() && !admin) {
                throw new IllegalArgumentException("Unknown function name: " + name);
            }
            return f.newInstance();
        }
        if (expression == null) {
            throw new IllegalArgumentException("Unknown function name: " + name);
        }
        throw new IllegalArgumentException("Not a function: " + name);
    }

    public Path newPath() {
        return rootPath.newInstance();
    }

    public Path newPath(PropertyPlaceholder rawElements) {
        return rootPath.newInstance(rawElements);
    }

}
