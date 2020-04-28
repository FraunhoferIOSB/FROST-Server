/*
 * Copyright (C) 2017 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.persistence;

import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;

/**
 * A interface for classes that can create ID instances. Implementations must
 * not depend on a specific instance of a persistenceManager.
 *
 * @author scf
 */
public interface IdManager {

    /**
     * Get the Id implementation used by this IdManager.
     *
     * @return The Class that implements Id.
     */
    public Class<? extends Id> getIdClass();

    /**
     * Parse the given input and generate an Id from it.
     *
     * @param input The input to parse as Id.
     * @return The Id.
     */
    public Id parseId(String input);

    /**
     * Wrap the given id object in an Id.
     *
     * @param input The id object to wrap.
     * @return an Id.
     */
    public Id fromObject(Object input);
}
