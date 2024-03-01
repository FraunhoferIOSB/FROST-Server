/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.QueryState;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.TableRef;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;

/**
 * The interface for table-to-table relations.
 *
 * @author hylke
 * @param <S> the type of the table linked from.
 */
public interface Relation<S extends StaMainTable<S>> {

    /**
     * The name of the relation. For official relations, this is the (singular)
     * entity type name.
     *
     * @return the name
     */
    public String getName();

    /**
     * Create a table join across this relation.
     *
     * @param source The (aliased) source table to join on.
     * @param queryState The query state to register the join on.
     * @param sourceRef The table tree to add the join to.
     * @return A new table tree leaf-entry.
     */
    public TableRef join(S source, QueryState<?> queryState, TableRef sourceRef);

    /**
     * Create a semi-join, useful for sub-queries that link to the main query.
     *
     * @param joinSource The source (subquery) table.
     * @param joinTarget The target (outer) query table.
     * @param queryState The query state to register the join on.
     */
    public void semiJoinTo(S joinSource, StaMainTable joinTarget, QueryState<?> queryState);

    /**
     * Create a link between the given source and target. This is not
     * necessarily supported for all implementations, in all directions. For
     * some types of relations this means creating new entries in a link table,
     * for other implementations it may mean existing relations are changed.
     *
     * @param pm The persistence manager to use for accessing the database.
     * @param source The source entity of the link.
     * @param target The target entity of the link.
     * @param navProp The navigation property of the relation.
     * @throws NoSuchEntityException if the target entity does not exist yet and
     * can not be created.
     * @throws IncompleteEntityException if the target entity can not be created
     * because it is not complete.
     */
    public void link(JooqPersistenceManager pm, Entity source, Entity target, NavigationPropertyMain navProp) throws NoSuchEntityException, IncompleteEntityException;

    /**
     * Make the links between the given source and targets, removing links that
     * are not in the given set. This is not necessarily supported for all
     * implementations, in all directions. For some types of relations this
     * means creating new entries in a link table, for other implementations it
     * may mean existing relations are changed.
     *
     * @param pm The persistence manager to use for accessing the database.
     * @param source The source entity of the link.
     * @param targets The target entity of the link.
     * @param navProp The navigation property of the relation.
     * @throws NoSuchEntityException if the target entity does not exist yet and
     * can not be created.
     * @throws IncompleteEntityException if the target entity can not be created
     * because it is not complete.
     */
    public void link(JooqPersistenceManager pm, Entity source, EntitySet targets, NavigationPropertyMain navProp) throws NoSuchEntityException, IncompleteEntityException;

    public void unLink(JooqPersistenceManager pm, Entity source, Entity target, NavigationPropertyMain navProp);
}
