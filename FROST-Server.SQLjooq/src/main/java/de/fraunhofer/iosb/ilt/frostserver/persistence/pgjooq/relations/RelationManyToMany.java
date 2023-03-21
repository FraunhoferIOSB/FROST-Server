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

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.QueryBuilder;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.factories.EntityFactories;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.QueryState;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.TableRef;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.IncompleteEntityException;
import de.fraunhofer.iosb.ilt.frostserver.util.exception.NoSuchEntityException;
import org.jooq.Condition;
import org.jooq.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A relation from a source table to a target table.
 *
 * @author hylke
 * @param <S> The source table.
 * @param <L> The link table linking source and target entities.
 * @param <T> The target table.
 */
public class RelationManyToMany<S extends StaMainTable<S>, L extends StaTable<L>, T extends StaMainTable<T>> implements Relation<S> {

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
    private FieldAccessor<S> sourceFieldAcc;

    private final L linkTable;
    private FieldAccessor<L> sourceLinkFieldAcc;
    private FieldAccessor<L> targetLinkFieldAcc;

    /**
     * The table that is the target side of the relation.
     */
    private final T target;

    /**
     * The field on the target side that defines the relation.
     */
    private FieldAccessor<T> targetFieldAcc;

    public RelationManyToMany(NavigationPropertyMain navProp, S source, L linkTable, T target) {
        if (source == null) {
            // Source is only used for finding the generics...
            LOGGER.error("NULL source");
        }
        this.linkTable = linkTable;
        this.target = target;
        this.targetType = navProp.getEntityType();
        this.name = navProp.getName();
    }

    public FieldAccessor<S> getSourceFieldAcc() {
        return sourceFieldAcc;
    }

    public RelationManyToMany<S, L, T> setSourceFieldAcc(FieldAccessor<S> sourceFieldAcc) {
        this.sourceFieldAcc = sourceFieldAcc;
        return this;
    }

    public L getLinkTable() {
        return linkTable;
    }

    public FieldAccessor<L> getSourceLinkFieldAcc() {
        return sourceLinkFieldAcc;
    }

    public RelationManyToMany<S, L, T> setSourceLinkFieldAcc(FieldAccessor<L> sourceLinkFieldAcc) {
        this.sourceLinkFieldAcc = sourceLinkFieldAcc;
        return this;
    }

    public FieldAccessor<L> getTargetLinkFieldAcc() {
        return targetLinkFieldAcc;
    }

    public RelationManyToMany<S, L, T> setTargetLinkFieldAcc(FieldAccessor<L> targetLinkFieldAcc) {
        this.targetLinkFieldAcc = targetLinkFieldAcc;
        return this;
    }

    public FieldAccessor<T> getTargetFieldAcc() {
        return targetFieldAcc;
    }

    public RelationManyToMany<S, L, T> setTargetFieldAcc(FieldAccessor<T> targetFieldAcc) {
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
    public TableRef join(S source, QueryState<?> queryState, TableRef sourceRef) {
        L linkTableAliased = (L) linkTable.as(queryState.getNextAlias());
        T targetAliased = (T) target.as(queryState.getNextAlias());
        Field<Object> sourceField = sourceFieldAcc.getField(source);
        Field<Object> sourceLinkField = sourceLinkFieldAcc.getField(linkTableAliased);
        Field<Object> targetLinkField = targetLinkFieldAcc.getField(linkTableAliased);
        Field<Object> targetField = targetFieldAcc.getField(targetAliased);
        queryState.setSqlFrom(queryState.getSqlFrom().innerJoin(linkTableAliased).on(sourceLinkField.eq(sourceField)));
        queryState.setSqlFrom(queryState.getSqlFrom().innerJoin(targetAliased).on(targetField.eq(targetLinkField)));
        queryState.setDistinctRequired(true);
        return QueryBuilder.createJoinedRef(sourceRef, targetType, targetAliased);
    }

    @Override
    public void link(PostgresPersistenceManager pm, Entity source, Entity target, NavigationPropertyMain navProp, boolean forInsert) throws NoSuchEntityException, IncompleteEntityException {
        EntityFactories entityFactories = pm.getEntityFactories();
        if (forInsert) {
            entityFactories.entityExistsOrCreate(pm, target);
        } else if (!entityFactories.entityExists(pm, target)) {
            throw new NoSuchEntityException("Linked Entity with no id.");
        }
        link(pm, source.getId().getValue(), target.getId().getValue());
    }

    protected void link(PostgresPersistenceManager pm, Object sourceId, Object targetId) {
        pm.getDslContext().insertInto(linkTable)
                .set(sourceLinkFieldAcc.getField(linkTable), sourceId)
                .set(targetLinkFieldAcc.getField(linkTable), targetId)
                .execute();
    }

    @Override
    public void unLink(PostgresPersistenceManager pm, Entity source, Entity target, NavigationPropertyMain navProp) {
        final Condition sourceCondition = sourceLinkFieldAcc.getField(linkTable).eq(source.getId().getValue());
        final Condition targetCondition = targetLinkFieldAcc.getField(linkTable).eq(target.getId().getValue());
        pm.getDslContext().deleteFrom(linkTable)
                .where(sourceCondition.and(targetCondition))
                .limit(1)
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
