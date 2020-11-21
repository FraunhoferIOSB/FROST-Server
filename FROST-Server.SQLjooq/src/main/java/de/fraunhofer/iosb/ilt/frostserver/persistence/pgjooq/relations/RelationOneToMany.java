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

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.QueryBuilder;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.QueryState;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.TableRef;
import org.jooq.Record;
import org.jooq.TableField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A relation from a source table to a target table.
 *
 * @author hylke
 * @param <J> The ID type
 * @param <S> The source table
 * @param <T> The target table
 */
public class RelationOneToMany<J extends Comparable, S extends StaMainTable<J, S>, T extends StaMainTable<J, T>> implements Relation<J, S> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RelationOneToMany.class.getName());
    /**
     * The target entity type of the relation.
     */
    private final EntityType targetType;
    /**
     * The name of the relation. For official relations, this is the (singular)
     * entity type name.
     */
    private final String name;

    /**
     * The field on the source side that defines the relation.
     */
    private FieldAccessor<J, S> sourceFieldAccessor;

    /**
     * The table that is the target side of the relation.
     */
    private final T target;

    /**
     * The field on the target side that defines the relation.
     */
    private FieldAccessor<J, T> targetFieldAccessor;

    /**
     * Flag indicating if following this relation means the query needs to be
     * DISTINCT.
     */
    private final boolean distinctRequired;

    public RelationOneToMany(S source, T target, EntityType targetType) {
        this(source, target, targetType, false);
    }

    public RelationOneToMany(S source, T target, EntityType targetType, boolean distinctRequired) {
        if (source == null) {
            // Source is only used for finding the generics...
            LOGGER.error("NULL source");
        }
        this.target = target;
        this.targetType = targetType;
        this.name = targetType.entityName;
        this.distinctRequired = distinctRequired;
    }

    public RelationOneToMany<J, S, T> setSourceFieldAccessor(FieldAccessor<J, S> sourceFieldAccessor) {
        this.sourceFieldAccessor = sourceFieldAccessor;
        return this;
    }

    public RelationOneToMany<J, S, T> setTargetFieldAccessor(FieldAccessor<J, T> targetFieldAccessor) {
        this.targetFieldAccessor = targetFieldAccessor;
        return this;
    }

    @Override
    public TableRef<J> join(S joinSource, QueryState<J, ?> queryState, TableRef<J> sourceRef) {
        TableField<Record, J> sourceField = sourceFieldAccessor.getField(joinSource);
        T targetAliased = (T) target.as(queryState.getNextAlias());
        TableField<Record, J> targetField = targetFieldAccessor.getField(targetAliased);
        queryState.setSqlFrom(queryState.getSqlFrom().innerJoin(targetAliased).on(targetField.eq(sourceField)));
        if (distinctRequired) {
            queryState.setDistinctRequired(distinctRequired);
        }
        return QueryBuilder.createJoinedRef(sourceRef, targetType, targetAliased);
    }

    /**
     * Re-links the one-to-many relation to a different entity. Updates the
     * targetField to sourceId on TargetTable where TargetTable.getId =
     * targetId.
     *
     * @param pm
     * @param sourceId
     * @param targetId
     */
    @Override
    public void link(PostgresPersistenceManager<J> pm, J sourceId, J targetId) {
        if (!distinctRequired) {
            throw new IllegalStateException("Trying to update a one-to-many relation from the wrong side.");
        }
        int count = pm.getDslContext().update(target)
                .set(targetFieldAccessor.getField(target), sourceId)
                .where(target.getId().eq(targetId))
                .execute();
        if (count != 1) {
            LOGGER.error("Executing query did not result in an update!");
        }
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
