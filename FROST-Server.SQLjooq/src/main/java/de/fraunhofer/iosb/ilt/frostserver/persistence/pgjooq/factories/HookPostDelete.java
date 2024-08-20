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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories;

import de.fraunhofer.iosb.ilt.frostserver.model.core.PkValue;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;

/**
 * A hook that can be registered on a table and will get executed after a
 * postDelete has happened.
 *
 * @author hylke
 */
public interface HookPostDelete extends JooqPmHook {

    /**
     *
     * @param pm The PersistenceManager for database access.
     * @param entityId The entity ID that is going to be deleted.
     * @throws NoSuchEntityException if something is wrong. This will cancel the
     * action.
     */
    public void postDelete(JooqPersistenceManager pm, PkValue entityId) throws NoSuchEntityException;
}
