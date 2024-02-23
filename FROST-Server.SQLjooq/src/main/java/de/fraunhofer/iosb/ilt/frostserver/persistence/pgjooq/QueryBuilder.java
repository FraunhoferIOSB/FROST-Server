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
package de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq;

import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElement;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementArrayIndex;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementCustomProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntityType;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePathVisitor;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaMainTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.QueryState;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.TableRef;
import de.fraunhofer.iosb.ilt.frostserver.property.EntityPropertyCustomLink;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain;
import de.fraunhofer.iosb.ilt.frostserver.property.Property;
import de.fraunhofer.iosb.ilt.frostserver.query.Expand;
import de.fraunhofer.iosb.ilt.frostserver.query.OrderBy;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.query.expression.Expression;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.PersistenceSettings;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.lang3.RegExUtils;
import org.jooq.AggregateFunction;
import org.jooq.DSLContext;
import org.jooq.Delete;
import org.jooq.DeleteConditionStep;
import org.jooq.Field;
import org.jooq.OrderField;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.ResultQuery;
import org.jooq.SelectConditionStep;
import org.jooq.SelectIntoStep;
import org.jooq.SelectSelectStep;
import org.jooq.conf.ParamType;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds a path for a query. Should not be re-used.
 *
 * @author scf
 */
public class QueryBuilder implements ResourcePathVisitor {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryBuilder.class);

    private static final String GENERATED_SQL = "Generated SQL:\n{}";
    private static final String TABLESAMPLE_REPLACE_REGEX = "$1 tablesample system (1)";
    private static final String TABLE_SEARCH_REGEX = "(\"[A-Za-z0-9_-]+\" as \"[A-Za-z0-9]+\")";
    private static final Pattern TABLE_SEARCH_PATTERN = Pattern.compile(TABLE_SEARCH_REGEX);

    /**
     * The prefix used for table aliases. The main entity is always
     * &lt;PREFIX&gt;1.
     */
    public static final String ALIAS_PREFIX = "e";
    public static final String DEFAULT_PREFIX = QueryBuilder.ALIAS_PREFIX + "0";

    private final JooqPersistenceManager pm;
    private final CoreSettings coreSettings;
    private final PersistenceSettings settings;
    private final TableCollection tableCollection;
    private Query staQuery;

    private Set<Property> selectedProperties;
    private TableRef lastPath;
    private TableRef mainTable;
    private NavigationPropertyMain lastNavProp;

    private boolean forPath = false;
    private ResourcePath requestedPath;
    private boolean forTypeAndId = false;
    private EntityType requestedEntityType;
    private Id requestedId;

    private boolean forUpdate = false;
    private boolean single = false;
    private boolean parsed = false;

    private QueryState<?> queryState;

    public QueryBuilder(JooqPersistenceManager pm, CoreSettings coreSettings, TableCollection tableCollection) {
        this.pm = pm;
        this.coreSettings = coreSettings;
        this.settings = coreSettings.getPersistenceSettings();
        this.tableCollection = tableCollection;
    }

    public QueryState<?> getQueryState() {
        return queryState;
    }

    public ResultQuery<Record> buildSelect() {
        gatherData();

        final DSLContext dslContext = pm.getDslContext();
        final SelectIntoStep<Record> selectStep;
        if (staQuery != null && staQuery.isSelectDistinct()) {
            selectStep = dslContext.selectDistinct(queryState.getSqlSelectFields());
        } else if (queryState.isDistinctRequired()) {
            if (queryState.isSqlSortFieldsSet()) {
                if (staQuery == null || !staQuery.isPkOrder()) {
                    queryState.getSqlSortFields().add(queryState.getSqlMainIdField(), OrderBy.OrderType.ASCENDING);
                }
                selectStep = dslContext.select(queryState.getSqlSelectFields())
                        .distinctOn(queryState.getSqlSortFields().getSqlSortSelectFields());
            } else {
                selectStep = dslContext.select(queryState.getSqlSelectFields())
                        .distinctOn(queryState.getSqlMainIdField());
            }
        } else {
            selectStep = dslContext.select(queryState.getSqlSelectFields());
        }
        var whereStep = selectStep.from(queryState.getSqlFrom())
                .where(queryState.getFullSqlWhere());

        final var sortFields = queryState.getSqlSortFields().getSqlSortFields();
        final var orderByStep = whereStep.orderBy(sortFields.toArray(OrderField[]::new));

        int skip = 0;
        int count;
        if (single) {
            count = 2;
        } else if (staQuery != null) {
            count = staQuery.getTopOrDefault() + 1;
            if (staQuery.getSkipFilter() == null) {
                skip = staQuery.getSkip(0);
            }
        } else {
            count = 1;
        }
        var limit = orderByStep.limit(skip, count);
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
        if (staQuery != null && staQuery.isSelectDistinct()) {
            final Set<Field> sqlSelectFields = queryState.getSqlSelectFields();
            count = DSL.countDistinct(sqlSelectFields.toArray(Field[]::new));
        } else if (queryState.isDistinctRequired()) {
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

    public ResultQuery<Record1<Integer>> buildCount(int limit) {
        gatherData();

        DSLContext dslContext = pm.getDslContext();
        SelectSelectStep<?> subSelect;
        if (staQuery != null && staQuery.isSelectDistinct()) {
            final Set<Field> sqlSelectFields = queryState.getSqlSelectFields();
            subSelect = DSL.selectDistinct(sqlSelectFields.toArray(Field[]::new));
        } else if (queryState.isDistinctRequired()) {
            subSelect = DSL.selectDistinct(queryState.getSqlMainIdField());
        } else {
            subSelect = DSL.select(queryState.getSqlMainIdField());
        }
        var selectFromWhere = subSelect.from(queryState.getSqlFrom())
                .where(queryState.getSqlWhere())
                .limit(limit);
        var query = dslContext.selectCount().from(selectFromWhere);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(GENERATED_SQL, query.getSQL(ParamType.INDEXED));
        }
        return query;
    }

    public static final class CountSampleResult {

        public final ResultQuery<Record> countQuery;
        public final int sampledTables;

        public CountSampleResult(ResultQuery<Record> countQuery, int sampledTables) {
            this.countQuery = countQuery;
            this.sampledTables = sampledTables;
        }

    }

    public CountSampleResult buildEstimateCountSample() {
        ResultQuery<Record1<Integer>> baseQuery = buildCount();
        String queryString = baseQuery.getSQL(ParamType.INLINED);

        String extendedQuery = RegExUtils.replaceFirst(queryString, TABLE_SEARCH_PATTERN, TABLESAMPLE_REPLACE_REGEX);
        int replaces = (extendedQuery.length() - queryString.length()) / " tablesample system (1)".length();
        DSLContext dslContext = pm.getDslContext();
        final var query = dslContext.resultQuery(extendedQuery);
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(GENERATED_SQL, extendedQuery);
        }
        return new CountSampleResult(query, replaces);
    }

    public ResultQuery<Record1<Integer>> buildEstimateCountExplain() {
        gatherData();

        DSLContext dslContext = pm.getDslContext();
        SelectSelectStep<?> select;
        if (staQuery != null && staQuery.isSelectDistinct()) {
            final Set<Field> sqlSelectFields = queryState.getSqlSelectFields();
            select = dslContext.selectDistinct(sqlSelectFields.toArray(Field[]::new));
        } else if (queryState.isDistinctRequired()) {
            select = dslContext.selectDistinct(queryState.getSqlMainIdField());
        } else {
            select = dslContext.select(queryState.getSqlMainIdField());
        }
        String selectQuery = select
                .from(queryState.getSqlFrom())
                .where(queryState.getSqlWhere())
                .getSQL(ParamType.INLINED);

        Field<Integer> countField = DSL.field("count_estimate({0})", SQLDataType.INTEGER, selectQuery);
        SelectSelectStep<Record1<Integer>> countQuery = dslContext.select(countField);
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(GENERATED_SQL, countQuery.getSQL(ParamType.INDEXED));
        }
        return countQuery;
    }

    public Delete buildDelete(PathElementEntitySet set) {
        gatherData();

        DSLContext dslContext = pm.getDslContext();
        final StaMainTable<?> table = tableCollection.getTablesByType().get(set.getEntityType());
        if (table == null) {
            throw new AssertionError("Don't know how to delete" + set.getEntityType().entityName, new IllegalArgumentException("Unknown type for delete"));
        }

        SelectConditionStep idSelect = DSL.select(queryState.getSqlMainIdField())
                .from(queryState.getSqlFrom())
                .where(queryState.getSqlWhere());

        DeleteConditionStep<? extends Record> delete = dslContext
                .deleteFrom(table)
                .where(
                        table.getId().in(idSelect));

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(GENERATED_SQL, delete.getSQL(ParamType.INDEXED));
        }
        return delete;
    }

    public QueryBuilder forTypeAndId(EntityType entityType, Id id) {
        if (forPath || forTypeAndId) {
            throw new IllegalStateException("QueryBuilder already used.");
        }
        forTypeAndId = true;
        requestedEntityType = entityType;
        requestedId = id;
        return this;
    }

    public QueryBuilder forPath(ResourcePath path) {
        if (forPath || forTypeAndId) {
            throw new IllegalStateException("QueryBuilder already used.");
        }
        forPath = true;
        requestedPath = path;
        requestedEntityType = path.getMainElementType();
        return this;
    }

    public QueryBuilder forUpdate(boolean forUpdate) {
        this.forUpdate = forUpdate;
        return this;
    }

    public QueryBuilder usingQuery(Query query) {
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
            parseOrder(staQuery);
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
        lastPath = queryEntityType(new PathElementEntity(requestedEntityType, null), requestedId, lastPath);
        single = true;
    }

    private void findSelectedProperties(Query query) {
        selectedProperties = new HashSet<>();
        if (query == null) {
            return;
        }
        for (Property property : query.getSelect()) {
            if (property instanceof NavigationPropertyMain) {
                selectedProperties.add(requestedEntityType.getPrimaryKey());
            }
            selectedProperties.add(property);
        }
        if (query.isPkOrder() && !query.isSelectDistinct() && !selectedProperties.isEmpty()) {
            // We're ordering by PK, make sure we select it too, so we can build better nextLinks
            selectedProperties.add(requestedEntityType.getPrimaryKey());
        }
        if (!query.getExpand().isEmpty() && !selectedProperties.isEmpty()) {
            // If we expand, and there is a $select, make sure we load the EP_ID and the navigation properties.
            // If no $select, then we already load everything.
            selectedProperties.add(requestedEntityType.getPrimaryKey());
            for (Expand expand : query.getExpand()) {
                NavigationProperty expandPath = expand.getPath();
                if (expandPath != null) {
                    selectedProperties.add(expandPath);
                }
            }
        }
    }

    private void parseOrder(Query query) {
        if (query != null) {
            PgExpressionHandler handler = new PgExpressionHandler(coreSettings, this, mainTable);
            for (OrderBy ob : query.getOrderBy()) {
                handler.addOrderbyToQuery(ob, queryState.getSqlSortFields());
            }
        }
    }

    public void parseFilter(Query query) {
        if (query != null) {
            queryState.setFilter(true);
            final Expression filter = query.getFilter();
            final Expression skipFilter = query.getSkipFilter();
            PgExpressionHandler handler = new PgExpressionHandler(coreSettings, this, mainTable);
            if (filter != null) {
                queryState.setSqlWhere(handler.addFilterToWhere(filter, queryState.getSqlWhere()));
            }
            if (skipFilter != null) {
                queryState.setSqlSkipWhere(handler.addFilterToWhere(skipFilter, queryState.getSqlSkipWhere()));
            }
        }
    }

    @Override
    public void visit(PathElementEntity element) {
        lastPath = queryEntityType(element, element.getId(), lastPath);
    }

    @Override
    public void visit(PathElementEntitySet element) {
        lastPath = queryEntityType(element, null, lastPath);
    }

    @Override
    public void visit(PathElementProperty element) {
        selectedProperties.add(element.getProperty());
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
     * @param pe The path element to query.
     * @param targetId The id of the requested entity.
     * @param last The table the requested entity is related to.
     * @return The table reference of the requested entity.
     */
    public TableRef queryEntityType(PathElementEntityType pe, Id targetId, TableRef last) {
        final EntityType entityType = pe.getEntityType();
        if (last != null) {
            TableRef existingJoin = last.getJoin(pe.getNavigationProperty());
            if (existingJoin != null) {
                return existingJoin;
            }
        }

        TableRef result;
        if (last == null) {
            StaMainTable<?> tableForType = tableCollection.getTableForType(entityType).asSecure(DEFAULT_PREFIX, pm);
            queryState = new QueryState(pm, tableForType, tableForType.getPropertyFieldRegistry().getFieldsForProperties(selectedProperties));
            result = new TableRef(entityType, tableForType);
        } else {
            if (entityType.equals(last.getType()) && lastNavProp == null) {
                result = last;
            } else {
                result = last.createJoin(lastNavProp.getInverse().getName(), queryState);
            }
        }

        if (targetId != null) {
            final Object id = targetId.asBasicPersistenceType();
            final Field lastId = result.getTable().getId();
            final Field optimisedLastId = result.getJoinEqual(lastId);
            queryState.setSqlWhere(queryState.getSqlWhere().and(optimisedLastId.eq(id)));
        }

        if (mainTable == null) {
            mainTable = result;
        }
        lastNavProp = pe.getNavigationProperty();
        return result;
    }

    /**
     * Queries the given entity type, as relation to the given table reference
     * and returns a new table reference. Effectively, this generates a join.
     *
     * @param np The NavigationProperty to query
     * @param last The table the requested entity is related to.
     * @return The table reference of the requested entity.
     */
    public TableRef queryEntityType(NavigationProperty np, TableRef last) {
        if (mainTable == null) {
            throw new IllegalStateException("mainTable should not be null");
        }
        if (last == null) {
            throw new IllegalStateException("last result should not be null");
        }

        TableRef existingJoin = last.getJoin(np);
        if (existingJoin != null) {
            return existingJoin;
        }

        return last.createJoin(np.getName(), queryState);
    }

    /**
     * Directly query an entity type. Used for custom linking.
     *
     * @param epcl the custom link.
     * @param sourceRef The source table ref.
     * @param sourceIdField The source ID field.
     * @return A new table ref with the target entity type table joined.
     */
    public TableRef queryEntityType(EntityPropertyCustomLink epcl, TableRef sourceRef, Field sourceIdField) {
        final EntityType targetEntityType = epcl.getEntityType();
        final StaMainTable<?> target = tableCollection.getTablesByType().get(targetEntityType);
        final StaMainTable<?> targetAliased = target.asSecure(queryState.getNextAlias(), pm);
        final Field<?> targetField = targetAliased.getId();
        queryState.setSqlFrom(queryState.getSqlFrom().leftJoin(targetAliased).on(targetField.eq(sourceIdField)));
        TableRef newRef = new TableRef(targetEntityType, targetAliased);
        sourceRef.addJoin(epcl, newRef);
        return newRef;
    }

    public TableCollection getTableCollection() {
        return tableCollection;
    }

    public static TableRef createJoinedRef(TableRef base, NavigationProperty np, StaMainTable<?> table) {
        TableRef newRef = new TableRef(np.getEntityType(), table);
        if (base != null) {
            base.addJoin(np, newRef);
        }
        return newRef;
    }

}
