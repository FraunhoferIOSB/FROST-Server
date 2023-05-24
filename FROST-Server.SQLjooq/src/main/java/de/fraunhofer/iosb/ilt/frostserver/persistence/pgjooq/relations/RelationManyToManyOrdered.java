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
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.PostgresPersistenceManager;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.QueryBuilder;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.QueryState;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.TableRef;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.query.OrderBy;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.SelectConditionStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A relation from a source table to a target table using a link table ordered
 * by a long value.
 *
 * @author hylke
 * @param <S> The source table.
 * @param <L> The link table linking source and target entities.
 * @param <T> The target table.
 */
public class RelationManyToManyOrdered<S extends StaMainTable<S>, L extends StaTable<L>, T extends StaMainTable<T>> extends RelationManyToMany<S, L, T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RelationManyToManyOrdered.class.getName());

    /**
     * The field used for the ordering.
     */
    private FieldAccessor<L> orderFieldAcc;
    private boolean alwaysDistinct = false;
    private boolean orderOnSource = true;

    public RelationManyToManyOrdered(NavigationPropertyMain navProp, S source, L linkTable, T target, boolean orderOnSource) {
        super(navProp, source, linkTable, target);
        this.orderOnSource = orderOnSource;
    }

    public RelationManyToManyOrdered<S, L, T> setOrderFieldAcc(FieldAccessor<L> orderFieldAcc) {
        this.orderFieldAcc = orderFieldAcc;
        return this;
    }

    public RelationManyToManyOrdered<S, L, T> setAlwaysDistinct(boolean alwaysDistinct) {
        this.alwaysDistinct = alwaysDistinct;
        return this;
    }

    @Override
    public TableRef join(S source, QueryState<?> queryState, TableRef sourceRef) {
        T targetAliased = (T) getTarget().as(queryState.getNextAlias());
        L linkTableAliased = (L) getLinkTable().as(queryState.getNextAlias());
        Field<?> sourceField = getSourceFieldAcc().getField(source);
        Field<?> sourceLinkField = getSourceLinkFieldAcc().getField(linkTableAliased);
        Field<?> targetLinkField = getTargetLinkFieldAcc().getField(linkTableAliased);
        Field<?> targetField = getTargetFieldAcc().getField(targetAliased);
        queryState.setSqlFrom(queryState.getSqlFrom().leftJoin(linkTableAliased).on(((Field) sourceLinkField).eq(sourceField)));
        queryState.setSqlFrom(queryState.getSqlFrom().leftJoin(targetAliased).on(((Field) targetField).eq(targetLinkField)));
        if (alwaysDistinct || queryState.isFilter()) {
            queryState.setDistinctRequired(true);
        } else {
            Field<Integer> orderField = orderFieldAcc.getField(linkTableAliased);
            queryState.getSqlSortFields().add(orderField, OrderBy.OrderType.ASCENDING);
        }
        return QueryBuilder.createJoinedRef(sourceRef, getTargetType(), targetAliased);
    }

    @Override
    protected void link(PostgresPersistenceManager pm, Object sourceId, Object targetId) {
        final DSLContext dslContext = pm.getDslContext();
        final L linkTable = getLinkTable();
        final Field<Object> sourceLinkField = getSourceLinkFieldAcc().getField(linkTable);
        final Field<Object> targetLinkField = getTargetLinkFieldAcc().getField(linkTable);
        final Field<Integer> orderField = orderFieldAcc.getField(linkTable);
        final SelectConditionStep<Record1<Integer>> orderValue;
        if (orderOnSource) {
            orderValue = dslContext.selectCount()
                    .from(linkTable)
                    .where(sourceLinkField.equal(sourceId));
        } else {
            orderValue = dslContext.selectCount()
                    .from(linkTable)
                    .where(sourceLinkField.equal(targetId));
        }
        dslContext.insertInto(linkTable)
                .set(sourceLinkField, sourceId)
                .set(targetLinkField, targetId)
                .set(orderField, orderValue)
                .execute();
    }

    @Override
    public void unLink(PostgresPersistenceManager pm, Entity source, Entity target, NavigationPropertyMain navProp) {
        final var linkTable = getLinkTable();
        final var sourceLinkField = getSourceLinkFieldAcc().getField(linkTable);
        final var targetLinkField = getTargetLinkFieldAcc().getField(linkTable);
        Field<Integer> orderField = orderFieldAcc.getField(linkTable);

        final var sourceCondition = sourceLinkField.eq(source.getId().getValue());
        final var targetCondition = targetLinkField.eq(target.getId().getValue());
        final DSLContext dslContext = pm.getDslContext();
        int deletedOrderIdx = dslContext.deleteFrom(linkTable)
                .where(sourceCondition.and(targetCondition))
                .limit(1)
                .returning(orderField)
                .execute();
        int updated;
        if (orderOnSource) {
            updated = dslContext.update(linkTable)
                    .set(orderField, orderField.sub(1))
                    .where(sourceCondition)
                    .and(orderField.gt(deletedOrderIdx))
                    .execute();
        } else {
            updated = dslContext.update(linkTable)
                    .set(orderField, orderField.sub(1))
                    .where(targetCondition)
                    .and(orderField.gt(deletedOrderIdx))
                    .execute();
        }
        LOGGER.trace("Updated {} order entries", updated);
    }

}
