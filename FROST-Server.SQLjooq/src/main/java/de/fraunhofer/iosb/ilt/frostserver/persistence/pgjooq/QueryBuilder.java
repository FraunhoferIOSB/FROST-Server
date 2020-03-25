/*
 * Copyright (C) 2016 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElement;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementArrayIndex;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementCustomProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePathVisitor;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.query.Expand;
import de.fraunhofer.iosb.ilt.frostserver.query.OrderBy;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Expression;
import de.fraunhofer.iosb.ilt.frostserver.settings.PersistenceSettings;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jooq.AggregateFunction;
import org.jooq.DSLContext;
import org.jooq.Delete;
import org.jooq.DeleteConditionStep;
import org.jooq.OrderField;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.ResultQuery;
import org.jooq.SelectConditionStep;
import org.jooq.SelectSeekStepN;
import org.jooq.SelectSelectStep;
import org.jooq.SelectWithTiesAfterOffsetStep;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds a path for a query. Should not be re-used.
 *
 * @author scf
 * @param <J> The type of the ID fields.
 */
public class QueryBuilder<J extends Comparable> implements ResourcePathVisitor {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryBuilder.class);

    private static final String GENERATED_SQL = "Generated SQL:\n{}";

    /**
     * The prefix used for table aliases. The main entity is always
     * &lt;PREFIX&gt;1.
     */
    public static final String ALIAS_PREFIX = "e";

    private final PostgresPersistenceManager<J> pm;
    private final PersistenceSettings settings;
    private final PropertyResolver<J> propertyResolver;
    private final TableCollection<J> tableCollection;
    private Query staQuery;

    private Set<Property> selectedProperties;
    private TableRef<J> lastPath;
    private TableRef<J> mainTable;

    private boolean forPath = false;
    private ResourcePath requestedPath;
    private boolean forTypeAndId = false;
    private EntityType requestedEntityType;
    private Id requestedId;

    private boolean forUpdate = false;
    private boolean single = false;
    private boolean parsed = false;

    private final QueryState<J> queryState = new QueryState<>();

    public QueryBuilder(PostgresPersistenceManager<J> pm, PersistenceSettings settings, PropertyResolver<J> propertyResolver) {
        this.pm = pm;
        this.settings = settings;
        this.propertyResolver = propertyResolver;
        this.tableCollection = propertyResolver.getTableCollection();
    }

    public ResultQuery<Record> buildSelect() {
        gatherData();

        if (queryState.getSqlSelectFields() == null) {
            queryState.setSqlSelectFields(Collections.emptySet());
        }

        DSLContext dslContext = pm.getDslContext();
        SelectSelectStep<Record> selectStep;
        if (queryState.isDistinctRequired()) {
            addOrderPropertiesToSelected();
            selectStep = dslContext.selectDistinct(queryState.getSqlSelectFields());
        } else {
            selectStep = dslContext.select(queryState.getSqlSelectFields());
        }
        SelectConditionStep<Record> whereStep = selectStep.from(queryState.getSqlFrom())
                .where(queryState.getSqlWhere());

        final List<OrderField> sortFields = queryState.getSqlSortFields().getSqlSortFields();
        SelectSeekStepN<Record> orderByStep = whereStep.orderBy(sortFields.toArray(new OrderField[sortFields.size()]));

        int skip = 0;
        int count;
        if (single) {
            count = 2;
        } else if (staQuery != null) {
            count = staQuery.getTopOrDefault() + 1;
            skip = staQuery.getSkip(0);
        } else {
            count = 1;
        }
        SelectWithTiesAfterOffsetStep<Record> limit = orderByStep.limit(skip, count);

        if (forUpdate) {
            return limit.forUpdate();
        }

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(GENERATED_SQL, limit.getSQL(ParamType.INDEXED));
        }
        return limit;
    }

    /**
     * Build a count query.
     *
     * @return the count query.
     */
    public ResultQuery<Record1<Integer>> buildCount() {
        gatherData();

        DSLContext dslContext = pm.getDslContext();
        AggregateFunction<Integer> count;
        if (queryState.isDistinctRequired()) {
            count = DSL.countDistinct(queryState.getSqlMainIdField());
        } else {
            count = DSL.count(queryState.getSqlMainIdField());
        }
        SelectConditionStep<Record1<Integer>> query = dslContext.select(count)
                .from(queryState.getSqlFrom())
                .where(queryState.getSqlWhere());

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(GENERATED_SQL, query.getSQL(ParamType.INDEXED));
        }
        return query;
    }

    public Delete buildDelete(PathElementEntitySet set) {
        gatherData();

        DSLContext dslContext = pm.getDslContext();
        final StaMainTable<J> table = tableCollection.tablesByType.get(set.getEntityType());
        if (table == null) {
            throw new AssertionError("Don't know how to delete" + set.getEntityType().name(), new IllegalArgumentException("Unknown type for delete"));
        }

        SelectConditionStep<Record1<J>> idSelect = DSL.select(queryState.getSqlMainIdField())
                .from(queryState.getSqlFrom())
                .where(queryState.getSqlWhere());

        DeleteConditionStep<? extends Record> delete = dslContext
                .deleteFrom(table)
                .where(
                        table.getId().in(idSelect)
                );

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(GENERATED_SQL, delete.getSQL(ParamType.INDEXED));
        }
        return delete;
    }

    public QueryBuilder<J> forTypeAndId(EntityType entityType, Id id) {
        if (forPath || forTypeAndId) {
            throw new IllegalStateException("QueryBuilder already used.");
        }
        forTypeAndId = true;
        requestedEntityType = entityType;
        requestedId = id;
        return this;
    }

    public QueryBuilder<J> forPath(ResourcePath path) {
        if (forPath || forTypeAndId) {
            throw new IllegalStateException("QueryBuilder already used.");
        }
        forPath = true;
        requestedPath = path;
        return this;
    }

    public QueryBuilder<J> forUpdate(boolean forUpdate) {
        this.forUpdate = forUpdate;
        return this;
    }

    public QueryBuilder<J> usingQuery(Query query) {
        this.staQuery = query;
        return this;
    }

    private void gatherData() {
        if (!parsed) {
            parsed = true;

            findSelectedProperties(staQuery);

            if (forPath) {
                parsePath();
            }
            if (forTypeAndId) {
                parseTypeAndId();
            }

            // Joins created when generating the path should not be merged with
            // joins generated for the filter or orderby.
            mainTable.clearJoins();

            parseFilter(staQuery);
            parseOrder(staQuery, settings);
        }
    }

    private void parsePath() {
        int count = requestedPath.size();
        for (int i = count - 1; i >= 0; i--) {
            PathElement element = requestedPath.get(i);
            element.visit(this);
        }
    }

    private void parseTypeAndId() {
        lastPath = queryEntityType(requestedEntityType, requestedId, lastPath);
        single = true;
    }

    private void findSelectedProperties(Query query) {
        selectedProperties = new HashSet<>();
        if (query == null) {
            return;
        }
        for (Property property : query.getSelect()) {
            selectedProperties.add(property);
        }
        if (!query.getExpand().isEmpty() && !selectedProperties.isEmpty()) {
            // If we expand, and there is a $select, make sure we load the ID and the navigation properties.
            // If no $select, then we already load everything.
            selectedProperties.add(EntityProperty.ID);
            for (Expand expand : query.getExpand()) {
                NavigationProperty expandPath = expand.getPath();
                if (expandPath != null) {
                    selectedProperties.add(expandPath);
                }
            }
        }
    }

    private void addOrderPropertiesToSelected() {
        queryState.getSqlSelectFields().addAll(queryState.getSqlSortFields().getSqlSortSelectFields());
    }

    private void parseOrder(Query query, PersistenceSettings settings) {
        if (query != null) {
            PgExpressionHandler handler = new PgExpressionHandler(this, mainTable);
            for (OrderBy ob : query.getOrderBy()) {
                handler.addOrderbyToQuery(ob, queryState.getSqlSortFields());
            }
            if (settings.getAlwaysOrderbyId()) {
                queryState.getSqlSortFields().add(queryState.getSqlMainIdField(), OrderBy.OrderType.ASCENDING);
            }
        }
    }

    public void parseFilter(Query query) {
        if (query != null) {
            queryState.setFilter(true);
            PgExpressionHandler handler = new PgExpressionHandler(this, mainTable);
            Expression filter = query.getFilter();
            if (filter != null) {
                queryState.setSqlWhere(handler.addFilterToWhere(filter, queryState.getSqlWhere()));
            }
        }
    }

    @Override
    public void visit(PathElementEntity element) {
        lastPath = queryEntityType(element.getEntityType(), element.getId(), lastPath);
    }

    @Override
    public void visit(PathElementEntitySet element) {
        lastPath = queryEntityType(element.getEntityType(), null, lastPath);
    }

    @Override
    public void visit(PathElementProperty element) {
        selectedProperties.add(element.getProperty());
        selectedProperties.add(EntityProperty.ID);
    }

    @Override
    public void visit(PathElementCustomProperty element) {
        // noting to do for custom properties.
    }

    @Override
    public void visit(PathElementArrayIndex element) {
        // noting to do for custom properties.
    }

    /**
     * Queries the given entity type, as relation to the given table reference
     * and returns a new table reference. Effectively, this generates a join.
     *
     * @param type The type of entity to query
     * @param targetId The id of the requested entity
     * @param last The table the requested entity is related to.
     * @return The table reference of the requested entity.
     */
    public TableRef queryEntityType(EntityType type, Id targetId, TableRef last) {
        J id = null;
        if (targetId != null) {
            if (!targetId.getBasicPersistenceType().equals(propertyResolver.getBasicPersistenceType())) {
                throw new IllegalArgumentException("This implementation expects " + propertyResolver.getBasicPersistenceType() + " ids, not " + targetId.getBasicPersistenceType());
            }
            id = (J) targetId.asBasicPersistenceType();
        }
        if (last != null) {
            TableRef existingJoin = last.getJoin(type);
            if (existingJoin != null) {
                return existingJoin;
            }
        }

        if (last == null) {
            StaMainTable<J> tableForType = tableCollection.getTableForType(type);
            queryState.startQuery(tableForType, propertyResolver.getFieldsForProperties(tableForType, selectedProperties));
            last = createJoinedRef(null, type, tableForType);
        } else {
            if (!type.equals(last.getType())) {
                last = last.createJoin(type.entityName, queryState);
            }
        }

        if (id != null) {
            queryState.setSqlWhere(queryState.getSqlWhere().and(last.getTable().getId().eq(id)));
        }

        if (mainTable == null) {
            mainTable = last;
        }
        return last;
    }

    public PropertyResolver<J> getPropertyResolver() {
        return propertyResolver;
    }

    public static <J extends Comparable> TableRef<J> createJoinedRef(TableRef<J> base, EntityType type, StaMainTable<J> table) {
        TableRef<J> newRef = new TableRef(type, table);
        if (base != null) {
            base.addJoin(type, newRef);
        }
        return newRef;
    }

}
