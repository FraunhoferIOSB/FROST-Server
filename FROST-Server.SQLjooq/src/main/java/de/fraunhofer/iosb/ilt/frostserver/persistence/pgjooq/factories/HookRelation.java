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
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationProperty;

/**
 * Hook that is called when a relation is created or deleted.
 */
public interface HookRelation {

    public void preCreate(JooqPersistenceManager pm, NavigationProperty np, Entity source, Entity target);

    public void postDelete(JooqPersistenceManager pm, NavigationProperty np, Entity source, Entity target);

    public static HookRelation of(RelationCall createCall, RelationCall deleteCall) {
        return new HookRelation() {
            @Override
            public void postDelete(JooqPersistenceManager pm, NavigationProperty np, Entity source, Entity target) {
                deleteCall.call(pm, np, source, target);
            }

            @Override
            public void preCreate(JooqPersistenceManager pm, NavigationProperty np, Entity source, Entity target) {
                createCall.call(pm, np, source, target);
            }
        };
    }

    public static interface RelationCall {

        public void call(JooqPersistenceManager pm, NavigationProperty np, Entity source, Entity target);
    }
}
