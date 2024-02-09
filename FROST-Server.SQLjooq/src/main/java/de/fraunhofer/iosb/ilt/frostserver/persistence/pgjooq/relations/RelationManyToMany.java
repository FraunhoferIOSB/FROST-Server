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
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.JooqPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.QueryBuilder;
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
     * The navigation property this relation represents.
     */
    private final NavigationPropertyMain navProp;

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

    /**
     * If the relation is symmetrical, on insert both [A,B] and [B,A] tuples are
     * inserted, and on delete both are deleted.
     */
    private boolean symmetrical;

    public RelationManyToMany(NavigationPropertyMain navProp, S source, L linkTable, T target) {
        this(navProp, source, linkTable, target, false);
    }

    public RelationManyToMany(NavigationPropertyMain navProp, S source, L linkTable, T target, boolean symmetrical) {
        if (source == null) {
            // Source is only used for finding the generics...
            LOGGER.error("NULL source");
        }
        this.navProp = navProp;
        this.linkTable = linkTable;
        this.target = target;
        this.targetType = navProp.getEntityType();
        this.name = navProp.getName();
        this.symmetrical = symmetrical;
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

    public NavigationPropertyMain getNavProp() {
        return navProp;
    }

    public EntityType getTargetType() {
        return targetType;
    }

    @Override
    public TableRef join(S source, QueryState<?> queryState, TableRef sourceRef) {
        L linkTableAliased = (L) linkTable.as(queryState.getNextAlias());
        T targetAliased = (T) target.asSecure(queryState.getNextAlias(), queryState.getPersistenceManager());
        Field<Object> sourceField = sourceFieldAcc.getField(source);
        Field<Object> sourceLinkField = sourceLinkFieldAcc.getField(linkTableAliased);
        Field<Object> targetLinkField = targetLinkFieldAcc.getField(linkTableAliased);
        Field<Object> targetField = targetFieldAcc.getField(targetAliased);
        queryState.setSqlFrom(queryState.getSqlFrom().leftJoin(linkTableAliased).on(sourceLinkField.eq(sourceField)));
        queryState.setSqlFrom(queryState.getSqlFrom().leftJoin(targetAliased).on(targetField.eq(targetLinkField)));
        queryState.setDistinctRequired(true);
        return QueryBuilder.createJoinedRef(sourceRef, navProp, targetAliased);
    }

    @Override
    public void link(JooqPersistenceManager pm, Entity source, EntitySet targets, NavigationPropertyMain navProp) throws NoSuchEntityException, IncompleteEntityException {
        final Object sourceId = source.getId().getValue();
        int count = pm.getDslContext().deleteFrom(linkTable)
                .where(sourceLinkFieldAcc.getField(linkTable).eq(sourceId))
                .execute();
        LOGGER.debug("Removed {} relations from {}", count, linkTable.getName());
        for (Entity targetEntity : targets) {
            link(pm, sourceId, targetEntity.getId().getValue());
        }

    }

    @Override
    public void link(JooqPersistenceManager pm, Entity source, Entity target, NavigationPropertyMain navProp) throws NoSuchEntityException, IncompleteEntityException {
        link(pm, source.getId().getValue(), target.getId().getValue());
    }

    protected void link(JooqPersistenceManager pm, Object sourceId, Object targetId) {
        pm.getDslContext().insertInto(linkTable)
                .set(sourceLinkFieldAcc.getField(linkTable), sourceId)
                .set(targetLinkFieldAcc.getField(linkTable), targetId)
                .onConflictDoNothing()
                .execute();
        if (symmetrical && !sourceId.equals(targetId)) {
            pm.getDslContext().insertInto(linkTable)
                    .set(sourceLinkFieldAcc.getField(linkTable), targetId)
                    .set(targetLinkFieldAcc.getField(linkTable), sourceId)
                    .onConflictDoNothing()
                    .execute();
        }
    }

    @Override
    public void unLink(JooqPersistenceManager pm, Entity source, Entity target, NavigationPropertyMain navProp) {
        final Object sourceId = source.getId().getValue();
        final Object targetId = target.getId().getValue();
        final Condition sourceCondition = sourceLinkFieldAcc.getField(linkTable).eq(sourceId);
        final Condition targetCondition = targetLinkFieldAcc.getField(linkTable).eq(targetId);
        pm.getDslContext().deleteFrom(linkTable)
                .where(sourceCondition.and(targetCondition))
                .limit(1)
                .execute();
        if (symmetrical) {
            final Condition sourceConditionInv = sourceLinkFieldAcc.getField(linkTable).eq(targetId);
            final Condition targetConditionInv = targetLinkFieldAcc.getField(linkTable).eq(sourceId);
            pm.getDslContext().deleteFrom(linkTable)
                    .where(sourceConditionInv.and(targetConditionInv))
                    .limit(1)
                    .execute();
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
