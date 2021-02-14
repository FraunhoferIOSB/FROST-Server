/*
 * Copyright (C) 2020 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
 * Karlsruhe, Germany.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.Relation;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.QueryState;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.TableRef;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyCustomSelect;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.impl.DSL;

/**
 *
 * @author Hylke van der Schaaf
 * @param <J> The type of the ID fields.
 * @param <T> The exact type of the implementing class.
 */
public interface StaMainTable<J extends Comparable, T extends StaMainTable<J, T>> extends StaTable<J, T> {

    public abstract Field<J> getId();

    @Override
    public abstract T as(Name as);

    @Override
    public default StaMainTable<J, T> as(String name) {
        return as(DSL.name(name));
    }

    public void initRelations();

    public void initProperties(EntityFactories<J> entityFactories);

    public Relation<J, T> findRelation(String name);

    public TableRef<J> createJoin(String name, QueryState<J, ?> queryState, TableRef<J> sourceRef);

    public PropertyFieldRegistry<J, T> getPropertyFieldRegistry();

    public PropertyFieldRegistry.PropertyFields<T> handleEntityPropertyCustomSelect(final EntityPropertyCustomSelect epCustomSelect);

    public EntityType getEntityType();

    public EntitySet newSet();

    public Entity entityFromQuery(Record tuple, QueryState<J, T> state, DataSize dataSize);

    public boolean insertIntoDatabase(PostgresPersistenceManager<J> pm, Entity entity) throws NoSuchEntityException, IncompleteEntityException;

    public EntityChangedMessage updateInDatabase(PostgresPersistenceManager<J> pm, Entity entity, J dsId) throws NoSuchEntityException, IncompleteEntityException;

    public void delete(PostgresPersistenceManager<J> pm, J entityId) throws NoSuchEntityException;
}
