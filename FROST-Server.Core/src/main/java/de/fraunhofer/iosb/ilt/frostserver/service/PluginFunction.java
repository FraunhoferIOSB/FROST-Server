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
package de.fraunhofer.iosb.ilt.frostserver.service;

import de.fraunhofer.iosb.ilt.frostserver.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.FunctionRegistry;

/**
 * The interface for plugins that expose filter functions.
 */
public interface PluginFunction extends Plugin {

    /**
     * Register new functions in the FunctionRegistry.
     *
     * @param fr the FunctionRegistry to add new functions to.
     */
    public void registerFunctions(FunctionRegistry fr);

    /**
     * Add expression handlers to functions (or other expressions) in the
     * FunctionRegestry.
     *
     * @param fr The FunctionRegistry holding all expressions.
     * @param pm The PersistenceManager that needs the handlers to execute the
     * expressions.
     */
    public void addExpressionHandlers(FunctionRegistry fr, PersistenceManager pm);
}
