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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.HookPreDelete;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.HookPreInsert;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.HookPreUpdate;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations.Relation;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.PropertyFieldRegistry;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.QueryState;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.TableRef;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.validator.SecurityTableWrapper;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyCustomSelect;
import de.fraunhofer.iosb.ilt.frostserver.service.UpdateMode;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import org.jooq.Field;
import org.jooq.Name;
import org.jooq.Record;

/**
 *
 * @author Hylke van der Schaaf
 * @param <T> The exact type of the implementing class.
 */
public interface StaMainTable<T extends StaMainTable<T>> extends StaTable<T> {

    public abstract Field getId();

    @Override
    public abstract T as(Name as);

    @Override
    public abstract T as(String name);

    /**
     * Return an aliased version of the table, with security joins active.
     *
     * @param name The alias to use.
     * @param pm The PersistenceManager to use for any queries.
     * @return The secured, aliased table.
     */
    public abstract StaMainTable<T> asSecure(String name, JooqPersistenceManager pm);

    /**
     * Get the SecurityTableWrapper for this table, if any is defined.
     *
     * @return The SecurityTableWrapper for this table, or null.
     */
    public abstract SecurityTableWrapper getSecurityWrapper();

    /**
     * Set the SecurityTableWrapper for this table.
     *
     * @param securityWrapper The SecurityTableWrapper to set.
     */
    public void setSecurityWrapper(SecurityTableWrapper securityWrapper);

    public void initRelations();

    public void initProperties(EntityFactories entityFactories);

    public Relation<T> findRelation(String name);

    public void registerRelation(Relation<T> relation);

    public TableRef createJoin(String name, QueryState<?> queryState, TableRef sourceRef);

    public <U extends StaMainTable<U>> void createSemiJoin(String name, U targetTable, QueryState queryState);

    public PropertyFieldRegistry<T> getPropertyFieldRegistry();

    public PropertyFieldRegistry.PropertyFields<T> handleEntityPropertyCustomSelect(final EntityPropertyCustomSelect epCustomSelect);

    public EntityType getEntityType();

    public EntitySet newSet();

    public Entity entityFromQuery(Record tuple, QueryState<T> state, DataSize dataSize);

    public boolean insertIntoDatabase(JooqPersistenceManager pm, Entity entity, UpdateMode updateMode) throws NoSuchEntityException, IncompleteEntityException;

    public EntityChangedMessage updateInDatabase(JooqPersistenceManager pm, Entity entity, Id entityId, UpdateMode updateMode) throws NoSuchEntityException, IncompleteEntityException;

    public void delete(JooqPersistenceManager pm, Id entityId) throws NoSuchEntityException;

    /**
     * Add a hook that runs pre-insert.
     *
     * @param priority The priority. Lower priority hooks run first. This is a
     * double to make sure it is always possible to squeeze in between two other
     * hooks.
     * @param hook The hook
     */
    public void registerHookPreInsert(double priority, HookPreInsert hook);

    /**
     * Add a hook that runs pre-update.
     *
     * @param priority The priority. Lower priority hooks run first. This is a
     * double to make sure it is always possible to squeeze in between two other
     * hooks.
     * @param hook The hook
     */
    public void registerHookPreUpdate(double priority, HookPreUpdate hook);

    /**
     * Add a hook that runs pre-delete.
     *
     * @param priority The priority. Lower priority hooks run first. This is a
     * double to make sure it is always possible to squeeze in between two other
     * hooks.
     * @param hook The hook
     */
    public void registerHookPreDelete(double priority, HookPreDelete hook);

}
