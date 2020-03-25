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
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.QueryBuilder;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.QueryState;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.TableRef;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import org.jooq.Record;
import org.jooq.TableField;

/**
 * A relation from a source table to a target table.
 *
 * @author hylke
 * @param <J>
 * @param <S> The source table
 * @param <T> The target table
 */
public class RelationOneToMany<J extends Comparable, S extends StaMainTable<J>, T extends StaMainTable<J>> implements Relation<J> {

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
     * The table that is the source side of the relation.
     */
    private final S source;

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
        this.source = source;
        this.target = target;
        this.targetType = targetType;
        this.name = targetType.entityName;
        this.distinctRequired = false;
    }

    public RelationOneToMany(S source, T target, EntityType targetType, boolean distinctRequired) {
        this.source = source;
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
    public TableRef<J> join(QueryState<J> queryState, TableRef<J> sourceRef) {
        TableField<Record, J> sourceField = sourceFieldAccessor.getField(source);
        T targetAliased = (T) target.as(queryState.getNextAlias());
        TableField<Record, J> targetField = targetFieldAccessor.getField(targetAliased);
        queryState.setSqlFrom(queryState.getSqlFrom().innerJoin(targetAliased).on(targetField.eq(sourceField)));
        if (distinctRequired) {
            queryState.setDistinctRequired(distinctRequired);
        }
        return QueryBuilder.createJoinedRef(sourceRef, targetType, targetAliased);
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
