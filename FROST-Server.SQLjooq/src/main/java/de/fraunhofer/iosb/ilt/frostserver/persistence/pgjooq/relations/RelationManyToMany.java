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
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTable;
import org.jooq.Record;
import org.jooq.TableField;

/**
 * A relation from a source table to a target table.
 *
 * @author hylke
 * @param <J>
 * @param <S> The source table.
 * @param <L> The link table linking source and target entities.
 * @param <T> The target table.
 */
public class RelationManyToMany<J extends Comparable, S extends StaMainTable<J>, L extends StaTable<J>, T extends StaMainTable<J>> implements Relation<J> {

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
    private FieldAccessor<J, S> sourceFieldAcc;

    private final L linkTable;
    private FieldAccessor<J, L> sourceLinkFieldAcc;
    private FieldAccessor<J, L> targetLinkFieldAcc;

    /**
     * The table that is the target side of the relation.
     */
    private final T target;

    /**
     * The field on the target side that defines the relation.
     */
    private FieldAccessor<J, T> targetFieldAcc;

    public RelationManyToMany(
            S source,
            L linkTable,
            T target,
            EntityType targetType) {
        this.source = source;
        this.linkTable = linkTable;
        this.target = target;
        this.targetType = targetType;
        this.name = targetType.entityName;
    }

    public RelationManyToMany<J, S, L, T> setSourceFieldAcc(FieldAccessor<J, S> sourceFieldAcc) {
        this.sourceFieldAcc = sourceFieldAcc;
        return this;
    }

    public RelationManyToMany<J, S, L, T> setSourceLinkFieldAcc(FieldAccessor<J, L> sourceLinkFieldAcc) {
        this.sourceLinkFieldAcc = sourceLinkFieldAcc;
        return this;
    }

    public RelationManyToMany<J, S, L, T> setTargetLinkFieldAcc(FieldAccessor<J, L> targetLinkFieldAcc) {
        this.targetLinkFieldAcc = targetLinkFieldAcc;
        return this;
    }

    public RelationManyToMany<J, S, L, T> setTargetFieldAcc(FieldAccessor<J, T> targetFieldAcc) {
        this.targetFieldAcc = targetFieldAcc;
        return this;
    }

    @Override
    public TableRef<J> join(QueryState<J> queryState, TableRef<J> sourceRef) {
        L linkTableAliased = (L) linkTable.as(queryState.getNextAlias());
        T targetAliased = (T) target.as(queryState.getNextAlias());
        TableField<Record, J> sourceField = sourceFieldAcc.getField(source);
        TableField<Record, J> sourceLinkField = sourceLinkFieldAcc.getField(linkTableAliased);
        TableField<Record, J> targetLinkField = targetLinkFieldAcc.getField(linkTableAliased);
        TableField<Record, J> targetField = targetFieldAcc.getField(targetAliased);
        queryState.setSqlFrom(queryState.getSqlFrom().innerJoin(linkTableAliased).on(sourceLinkField.eq(sourceField)));
        queryState.setSqlFrom(queryState.getSqlFrom().innerJoin(targetAliased).on(targetField.eq(targetLinkField)));
        queryState.setDistinctRequired(true);
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
