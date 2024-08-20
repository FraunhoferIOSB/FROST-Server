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

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import java.util.Map;
import org.jooq.Field;

/**
 * A hook that can be registered on a table and will get executed before an
 * insert happens.
 *
 * @author hylke
 */
public interface HookPreInsert extends JooqPmHook {

    /**
     * The fase the pre-insert hook is running in.
     */
    public enum Phase {
        /**
         * Phase before relations are loaded and maybe created.
         */
        PRE_RELATIONS,
        /**
         * Phase after relations are loaded / created but before the entity
         * itself is created.
         */
        POST_RELATIONS,
        /**
         * Phase after the insert has been done.
         */
        POST_INSERT
    }

    /**
     *
     * @param fase The current insert-fase.
     * @param pm The Persistence Manager to use for database queries.
     * @param entity The entity that is being inserted.
     * @param insertFields The fields being inserted.
     * @return true if the insert can continue, false if the insert should be
     * skipped without error.
     * @throws NoSuchEntityException If a related entity does not exist.
     * @throws IncompleteEntityException If the entity is not complete.
     */
    public boolean preInsertIntoDatabase(Phase fase, JooqPersistenceManager pm, Entity entity, Map<Field, Object> insertFields) throws NoSuchEntityException, IncompleteEntityException;

}
