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
package de.fraunhofer.iosb.ilt.frostserver.parser.query;

import de.fraunhofer.iosb.ilt.frostserver.query.expression.function.Function;
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

    private final Map<String, Function<?>> functions = new HashMap<>();

    public void registerFunction(Function<?> function) {
        String name = function.getFunctionName().toLowerCase(Locale.ROOT);
        if (functions.put(name, function) != null) {
            LOGGER.warn("    Replaced the definition of Function {}", name);
        } else {
            LOGGER.debug("    Registered Function {}", name);
        }
    }

    public Function<?> getFunction(String name, boolean admin) {
        final Function<?> function = functions.get(name);
        if (function == null) {
            throw new IllegalArgumentException("Unknown function name: " + name);
        }
        if (function.isAdminOnly() && !admin) {
            throw new IllegalArgumentException("Unknown function name: " + name);
        }
        return function.newInstance();
    }

}
