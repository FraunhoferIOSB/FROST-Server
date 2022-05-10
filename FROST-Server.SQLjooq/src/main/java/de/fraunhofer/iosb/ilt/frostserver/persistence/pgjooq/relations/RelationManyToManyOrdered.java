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

    /**
     * The field used for the ordering.
     */
    private FieldAccessor<L> orderFieldAcc;
    private boolean alwaysDistinct = false;

    public RelationManyToManyOrdered(NavigationPropertyMain navProp, S source, L linkTable, T target) {
        super(navProp, source, linkTable, target);
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
        queryState.setSqlFrom(queryState.getSqlFrom().innerJoin(linkTableAliased).on(((Field) sourceLinkField).eq(sourceField)));
        queryState.setSqlFrom(queryState.getSqlFrom().innerJoin(targetAliased).on(((Field) targetField).eq(targetLinkField)));
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
        dslContext.insertInto(linkTable)
                .set(sourceLinkField, sourceId)
                .set(targetLinkField, targetId)
                .set(
                        orderField,
                        dslContext.selectCount()
                                .from(linkTable)
                                .where(sourceLinkField.equal(sourceId))
                )
                .execute();
    }

}
