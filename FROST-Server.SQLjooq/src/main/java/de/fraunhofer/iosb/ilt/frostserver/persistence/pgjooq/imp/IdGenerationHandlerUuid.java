/*
 * Copyright (C) 2016 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
 * Karlsruhe, Germany.
 * Copyright (C) 2018 KIT TECO, Vincenz-Prießnitz-Str. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.imp;

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.IdGenerationHandler;

/**
 *
 * Class for handling the persistence setting "idGenerationMode".
 *
 * @author koepke, scf
 */
public class IdGenerationHandlerUuid extends IdGenerationHandler {

    /**
     * Constructor for IdGenerationHandler.
     *
     * @param entity Entity for which idGenerationMode should be
     * checked/applied.
     */
    public IdGenerationHandlerUuid(Entity entity) {
        super(entity);
    }

    /**
     *
     * Modify the entity id.
     *
     */
    @Override
    public void modifyClientSuppliedId() {
        // Nothing to do for now.
    }

    /**
     *
     * Checks if a client generated id is valid.
     *
     * @return true if client generated id is valid.
     */
    @Override
    protected boolean validateClientSuppliedId() {
        return getIdValue() != null;
    }
}
