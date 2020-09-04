/*
 * Copyright (C) 2020 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations;

import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.QueryState;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.TableRef;

/**
 * The interface for table-to-table relations.
 *
 * @author hylke
 * @param <J>
 */
public interface Relation<J extends Comparable> {

    /**
     * The name of the relation. For official relations, this is the (singular)
     * entity type name.
     *
     * @return the name
     */
    public String getName();

    public TableRef<J> join(QueryState<J, ?, ?> queryState, TableRef<J> sourceRef);
}
