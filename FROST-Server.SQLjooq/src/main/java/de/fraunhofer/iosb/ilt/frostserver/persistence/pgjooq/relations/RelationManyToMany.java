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
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.QueryState;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.TableRef;
import org.jooq.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A relation from a source table to a target table.
 *
 * @author hylke
 * @param <J>
 * @param <S> The source table.
 * @param <L> The link table linking source and target entities.
 * @param <T> The target table.
 */
public class RelationManyToMany<J extends Comparable, S extends StaMainTable<J, S>, L extends StaTable<J, L>, T extends StaMainTable<J, T>> implements Relation<J, S> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RelationManyToMany.class.getName());

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
        if (source == null) {
            // Source is only used for finding the generics...
            LOGGER.error("NULL source");
        }
        this.linkTable = linkTable;
        this.target = target;
        this.targetType = targetType;
        this.name = targetType.entityName;
    }

    public FieldAccessor<J, S> getSourceFieldAcc() {
        return sourceFieldAcc;
    }

    public RelationManyToMany<J, S, L, T> setSourceFieldAcc(FieldAccessor<J, S> sourceFieldAcc) {
        this.sourceFieldAcc = sourceFieldAcc;
        return this;
    }

    public L getLinkTable() {
        return linkTable;
    }

    public FieldAccessor<J, L> getSourceLinkFieldAcc() {
        return sourceLinkFieldAcc;
    }

    public RelationManyToMany<J, S, L, T> setSourceLinkFieldAcc(FieldAccessor<J, L> sourceLinkFieldAcc) {
        this.sourceLinkFieldAcc = sourceLinkFieldAcc;
        return this;
    }

    public FieldAccessor<J, L> getTargetLinkFieldAcc() {
        return targetLinkFieldAcc;
    }

    public RelationManyToMany<J, S, L, T> setTargetLinkFieldAcc(FieldAccessor<J, L> targetLinkFieldAcc) {
        this.targetLinkFieldAcc = targetLinkFieldAcc;
        return this;
    }

    public FieldAccessor<J, T> getTargetFieldAcc() {
        return targetFieldAcc;
    }

    public RelationManyToMany<J, S, L, T> setTargetFieldAcc(FieldAccessor<J, T> targetFieldAcc) {
        this.targetFieldAcc = targetFieldAcc;
        return this;
    }

    public T getTarget() {
        return target;
    }

    public EntityType getTargetType() {
        return targetType;
    }

    @Override
    public TableRef<J> join(S source, QueryState<J, ?> queryState, TableRef<J> sourceRef) {
        L linkTableAliased = (L) linkTable.as(queryState.getNextAlias());
        T targetAliased = (T) target.as(queryState.getNextAlias());
        Field<J> sourceField = sourceFieldAcc.getField(source);
        Field<J> sourceLinkField = sourceLinkFieldAcc.getField(linkTableAliased);
        Field<J> targetLinkField = targetLinkFieldAcc.getField(linkTableAliased);
        Field<J> targetField = targetFieldAcc.getField(targetAliased);
        queryState.setSqlFrom(queryState.getSqlFrom().innerJoin(linkTableAliased).on(sourceLinkField.eq(sourceField)));
        queryState.setSqlFrom(queryState.getSqlFrom().innerJoin(targetAliased).on(targetField.eq(targetLinkField)));
        queryState.setDistinctRequired(true);
        return QueryBuilder.createJoinedRef(sourceRef, targetType, targetAliased);
    }

    public void link(PostgresPersistenceManager<J> pm, J sourceId, J targetId) {
        pm.getDslContext().insertInto(linkTable)
                .set(sourceLinkFieldAcc.getField(linkTable), sourceId)
                .set(targetLinkFieldAcc.getField(linkTable), targetId)
                .execute();
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
