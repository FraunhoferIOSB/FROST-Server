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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.relations;

import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.NOT_IMPLEMENTED_MULTI_VALUE_PK;

import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.QueryBuilder;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.QueryState;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.TableRef;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.NotImplementedException;
import org.jooq.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A relation from a source table to a target table.
 *
 * @author hylke
 * @param <S> The source table
 * @param <T> The target table
 */
public class RelationOneToMany<S extends StaMainTable<S>, T extends StaMainTable<T>> implements Relation<S> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RelationOneToMany.class.getName());

    /**
     * The navigation property this relation represents.
     */
    private final NavigationPropertyMain navProp;

    /**
     * The name of the relation. For official relations, this is the (singular)
     * entity type name.
     */
    private final String name;

    /**
     * The field on the source side that defines the relation.
     */
    private FieldAccessor<S> sourceFieldAccessor;

    /**
     * The table that is the target side of the relation.
     */
    private final T target;

    /**
     * The field on the target side that defines the relation.
     */
    private FieldAccessor<T> targetFieldAccessor;

    /**
     * Flag indicating if following this relation means the query needs to be
     * DISTINCT.
     */
    private final boolean distinctRequired;

    public RelationOneToMany(NavigationPropertyMain navProp, S source, T target) {
        this(navProp, source, target, navProp.isEntitySet());
    }

    public RelationOneToMany(NavigationPropertyMain navProp, S source, T target, boolean distinctRequired) {
        if (source == null) {
            // Source is only used for finding the generics...
            LOGGER.error("NULL source");
        }
        this.navProp = navProp;
        this.target = target;
        this.name = navProp.getName();
        this.distinctRequired = distinctRequired;
        if (source.getPkFields().size() != 1 || target.getPkFields().size() != 1) {
            throw new NotImplementedException(NOT_IMPLEMENTED_MULTI_VALUE_PK);
        }
    }

    public RelationOneToMany<S, T> setSourceFieldAccessor(FieldAccessor<S> sourceFieldAccessor) {
        this.sourceFieldAccessor = sourceFieldAccessor;
        return this;
    }

    public RelationOneToMany<S, T> setTargetFieldAccessor(FieldAccessor<T> targetFieldAccessor) {
        this.targetFieldAccessor = targetFieldAccessor;
        return this;
    }

    @Override
    public TableRef join(S joinSource, QueryState<?> queryState, TableRef sourceRef) {
        Field<?> sourceField = sourceFieldAccessor.getField(joinSource);
        T targetAliased = (T) target.asSecure(queryState.getNextAlias(), queryState.getPersistenceManager());
        Field<?> targetField = targetFieldAccessor.getField(targetAliased);
        queryState.setSqlFrom(queryState.getSqlFrom().leftJoin(targetAliased).on(((Field) targetField).eq(sourceField)));
        if (distinctRequired) {
            queryState.setDistinctRequired(distinctRequired);
        }
        // When a query filters or orders on the target field, it should instead do so on the source field
        Map<Field, Field> joinEquals = new HashMap<>();
        joinEquals.put(targetField, sourceField);
        return QueryBuilder.createJoinedRef(sourceRef, navProp, targetAliased)
                .setJoinEquals(joinEquals);
    }

    @Override
    public void semiJoinTo(S joinSource, StaMainTable joinTarget, QueryState<?> queryState) {
        Field sourceField = sourceFieldAccessor.getField(joinSource);
        Field targetField = targetFieldAccessor.getField((T) joinTarget);
        queryState.setSqlWhere(queryState.getSqlWhere().and(targetField.eq(sourceField)));
    }

    @Override
    public void link(JooqPersistenceManager pm, Entity source, EntitySet targets, NavigationPropertyMain navProp) throws NoSuchEntityException, IncompleteEntityException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void link(JooqPersistenceManager pm, Entity source, Entity target, NavigationPropertyMain navProp) throws IncompleteEntityException, NoSuchEntityException {
        if (!distinctRequired) {
            throw new IllegalStateException("Trying to update a one-to-many relation from the wrong side.");
        }
        EntityFactories entityFactories = pm.getEntityFactories();
        if (entityFactories.entityExists(pm, target, true)) {
            link(pm, source.getPrimaryKeyValues().get(0), target.getPrimaryKeyValues().get(0));
        } else {
            throw new NoSuchEntityException("Linked Entity with no id.");
        }
    }

    /**
     * Re-links the one-to-many relation to a different entity. Updates the
     * targetField to sourceId on TargetTable where TargetTable.getId =
     * targetId.
     *
     * @param pm The PersistenceManager for queries.
     * @param sourceId The source id of the link.
     * @param targetId The target id of the link.
     */
    protected void link(JooqPersistenceManager pm, Object sourceId, Object targetId) {
        if (!distinctRequired) {
            throw new IllegalStateException("Trying to update a one-to-many relation from the wrong side.");
        }
        int count = pm.getDslContext().update(target)
                .set(targetFieldAccessor.getField(target), sourceId)
                .where(target.getPkFields().get(0).eq(targetId))
                .execute();
        if (count != 1) {
            LOGGER.error("Executing query did not result in an update!");
        }
    }

    @Override
    public void unLink(JooqPersistenceManager pm, Entity source, Entity target, NavigationPropertyMain navProp) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * The name of the relation. For official relations, this is the (singular)
     * entity type name.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return name;
    }

}
