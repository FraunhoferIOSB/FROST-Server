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

import de.fraunhofer.iosb.ilt.frostserver.model.core.Id;
import de.fraunhofer.iosb.ilt.frostserver.path.CustomPropertyArrayIndex;
import de.fraunhofer.iosb.ilt.frostserver.path.CustomPropertyPathElement;
import de.fraunhofer.iosb.ilt.frostserver.path.EntityPathElement;
import de.fraunhofer.iosb.ilt.frostserver.path.EntityProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.EntitySetPathElement;
import de.fraunhofer.iosb.ilt.frostserver.path.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.path.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.Property;
import de.fraunhofer.iosb.ilt.frostserver.path.PropertyPathElement;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePathElement;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePathVisitor;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableActuators;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableDatastreams;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableFeatures;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableHistLocations;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableLocations;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableLocationsHistLocations;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableMultiDatastreams;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableMultiDatastreamsObsProperties;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableObsProperties;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableObservations;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableSensors;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableTaskingCapabilities;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableTasks;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableThings;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.AbstractTableThingsLocations;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.StaTable;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.tables.TableCollection;
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
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Delete;
import org.jooq.DeleteConditionStep;
import org.jooq.Field;
import org.jooq.OrderField;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.ResultQuery;
import org.jooq.SelectConditionStep;
import org.jooq.SelectSeekStepN;
import org.jooq.SelectSelectStep;
import org.jooq.SelectWithTiesAfterOffsetStep;
import org.jooq.Table;
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

    private static final String DO_NOT_KNOW_HOW_TO_JOIN = "Do not know how to join";
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
    private int aliasNr = 0;

    private boolean forPath = false;
    private ResourcePath requestedPath;
    private boolean forTypeAndId = false;
    private EntityType requestedEntityType;
    private Id requestedId;

    private boolean isFilter = false;
    private boolean needsDistinct = false;
    private boolean forUpdate = false;
    private boolean single = false;
    private boolean parsed = false;

    private Set<Field> sqlSelectFields;
    private Field<J> sqlMainIdField;
    private Table<?> sqlFrom;
    private Condition sqlWhere = DSL.trueCondition();
    private Utils.SortSelectFields sqlSortFields;

    public QueryBuilder(PostgresPersistenceManager<J> pm, PersistenceSettings settings, PropertyResolver<J> propertyResolver) {
        this.pm = pm;
        this.settings = settings;
        this.propertyResolver = propertyResolver;
        this.tableCollection = propertyResolver.getTableCollection();
    }

    public ResultQuery<Record> buildSelect() {
        gatherData();

        if (sqlSelectFields == null) {
            sqlSelectFields = Collections.emptySet();
        }

        DSLContext dslContext = pm.getDslContext();
        SelectSelectStep<Record> selectStep;
        if (needsDistinct) {
            addOrderPropertiesToSelected();
            selectStep = dslContext.selectDistinct(sqlSelectFields);
        } else {
            selectStep = dslContext.select(sqlSelectFields);
        }
        SelectConditionStep<Record> whereStep = selectStep.from(sqlFrom)
                .where(sqlWhere);

        final List<OrderField> sortFields = getSqlSortFields().getSqlSortFields();
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
        if (needsDistinct) {
            count = DSL.countDistinct(sqlMainIdField);
        } else {
            count = DSL.count(sqlMainIdField);
        }
        SelectConditionStep<Record1<Integer>> query = dslContext.select(count)
                .from(sqlFrom)
                .where(sqlWhere);

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(GENERATED_SQL, query.getSQL(ParamType.INDEXED));
        }
        return query;
    }

    public Delete buildDelete(EntitySetPathElement set) {
        gatherData();

        DSLContext dslContext = pm.getDslContext();
        final StaTable<J> table = tableCollection.tablesByType.get(set.getEntityType());
        if (table == null) {
            throw new AssertionError("Don't know how to delete" + set.getEntityType().name(), new IllegalArgumentException("Unknown type for delete"));
        }

        SelectConditionStep<Record1<J>> idSelect = DSL.select(sqlMainIdField)
                .from(sqlFrom)
                .where(sqlWhere);

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
            ResourcePathElement element = requestedPath.get(i);
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
                List<NavigationProperty> expandPath = expand.getPath();
                if (!expandPath.isEmpty()) {
                    selectedProperties.add(expandPath.get(0));
                }
            }
        }
    }

    private void addOrderPropertiesToSelected() {
        sqlSelectFields.addAll(getSqlSortFields().getSqlSortSelectFields());
    }

    private void parseOrder(Query query, PersistenceSettings settings) {
        if (query != null) {
            PgExpressionHandler handler = new PgExpressionHandler(this, mainTable);
            for (OrderBy ob : query.getOrderBy()) {
                handler.addOrderbyToQuery(ob, getSqlSortFields());
            }
            if (settings.getAlwaysOrderbyId()) {
                getSqlSortFields().add(mainTable.getIdField(), OrderBy.OrderType.ASCENDING);
            }
        }
    }

    public void parseFilter(Query query) {
        if (query != null) {
            isFilter = true;
            PgExpressionHandler handler = new PgExpressionHandler(this, mainTable);
            Expression filter = query.getFilter();
            if (filter != null) {
                sqlWhere = handler.addFilterToWhere(filter, sqlWhere);
            }
        }
    }

    @Override
    public void visit(EntityPathElement element) {
        lastPath = queryEntityType(element.getEntityType(), element.getId(), lastPath);
    }

    @Override
    public void visit(EntitySetPathElement element) {
        lastPath = queryEntityType(element.getEntityType(), null, lastPath);
    }

    @Override
    public void visit(PropertyPathElement element) {
        selectedProperties.add(element.getProperty());
        selectedProperties.add(EntityProperty.ID);
    }

    @Override
    public void visit(CustomPropertyPathElement element) {
        // noting to do for custom properties.
    }

    @Override
    public void visit(CustomPropertyArrayIndex element) {
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
            if (targetId.getBasicPersistenceType() != propertyResolver.getBasicPersistenceType()) {
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

        switch (type) {
            case ACTUATOR:
                last = queryActuator(id, last);
                break;

            case DATASTREAM:
                last = queryDatastreams(id, last);
                break;

            case MULTIDATASTREAM:
                last = queryMultiDatastreams(id, last);
                break;

            case FEATUREOFINTEREST:
                last = queryFeatures(id, last);
                break;

            case HISTORICALLOCATION:
                last = queryHistLocations(id, last);
                break;

            case LOCATION:
                last = queryLocations(id, last);
                break;

            case OBSERVATION:
                last = queryObservations(id, last);
                break;

            case OBSERVEDPROPERTY:
                last = queryObsProperties(id, last);
                break;

            case SENSOR:
                last = querySensors(id, last);
                break;

            case TASK:
                last = queryTasks(id, last);
                break;

            case TASKINGCAPABILITY:
                last = queryTaskingCapabilities(id, last);
                break;

            case THING:
                last = queryThings(id, last);
                break;

            default:
                LOGGER.error("Unknown entity type {}!?", type);
                throw new IllegalStateException("Unknown entity type " + type);
        }
        if (mainTable == null) {
            mainTable = last;
        }
        return last;
    }

    public PropertyResolver<J> getPropertyResolver() {
        return propertyResolver;
    }

    private TableRef<J> createJoinedRef(TableRef<J> base, EntityType type, Table table, Field<J> idField) {
        TableRef<J> newRef = new TableRef(type, table, idField);
        if (base != null) {
            base.addJoin(type, newRef);
        }
        return newRef;
    }

    private TableRef<J> queryActuator(J entityId, TableRef<J> last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractTableActuators<J> tableActuators = tableCollection.tableActuators.as(alias);
        boolean added = true;
        if (last == null) {
            sqlSelectFields = propertyResolver.getFieldsForProperties(tableActuators, selectedProperties);
            sqlFrom = tableActuators;
            sqlMainIdField = tableActuators.getId();
        } else {
            switch (last.getType()) {
                case TASKINGCAPABILITY:
                    AbstractTableTaskingCapabilities<J> qTaskingCaps = (AbstractTableTaskingCapabilities<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tableActuators).on(tableActuators.getId().eq(qTaskingCaps.getActuatorId()));
                    break;

                case ACTUATOR:
                    added = false;
                    break;

                default:
                    LOGGER.error("Do not know how to join {} onto Actuators.", last.getType());
                    throw new IllegalStateException(DO_NOT_KNOW_HOW_TO_JOIN);
            }
        }
        if (added) {
            last = createJoinedRef(last, EntityType.ACTUATOR, tableActuators, tableActuators.getId());
        }
        if (entityId != null) {
            sqlWhere = sqlWhere.and(tableActuators.getId().eq(entityId));
        }
        return last;
    }

    private TableRef<J> queryDatastreams(J entityId, TableRef last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractTableDatastreams<J> tableDatastreams = tableCollection.tableDatastreams.as(alias);
        boolean added = true;
        if (last == null) {
            sqlSelectFields = propertyResolver.getFieldsForProperties(tableDatastreams, selectedProperties);
            sqlFrom = tableDatastreams;
            sqlMainIdField = tableDatastreams.getId();
        } else {
            switch (last.getType()) {
                case THING:
                    AbstractTableThings<J> tThings = (AbstractTableThings<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tableDatastreams).on(tableDatastreams.getThingId().eq(tThings.getId()));
                    needsDistinct = true;
                    break;

                case OBSERVATION:
                    AbstractTableObservations<J> tObservations = (AbstractTableObservations<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tableDatastreams).on(tableDatastreams.getId().eq(tObservations.getDatastreamId()));
                    break;

                case SENSOR:
                    AbstractTableSensors<J> tSensors = (AbstractTableSensors<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tableDatastreams).on(tableDatastreams.getSensorId().eq(tSensors.getId()));
                    needsDistinct = true;
                    break;

                case OBSERVEDPROPERTY:
                    AbstractTableObsProperties<J> tObsProperties = (AbstractTableObsProperties<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tableDatastreams).on(tableDatastreams.getObsPropertyId().eq(tObsProperties.getId()));
                    needsDistinct = true;
                    break;

                case DATASTREAM:
                    added = false;
                    break;

                default:
                    LOGGER.error("Do not know how to join {} onto Datastreams.", last.getType());
                    throw new IllegalStateException(DO_NOT_KNOW_HOW_TO_JOIN);
            }
        }
        if (added) {
            last = createJoinedRef(last, EntityType.DATASTREAM, tableDatastreams, tableDatastreams.getId());
        }
        if (entityId != null) {
            sqlWhere = sqlWhere.and(tableDatastreams.getId().eq(entityId));
        }
        return last;
    }

    private TableRef<J> queryMultiDatastreams(J entityId, TableRef<J> last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractTableMultiDatastreams<J> tableMultiDataStreams = tableCollection.tableMultiDatastreams.as(alias);
        boolean added = true;
        if (last == null) {
            sqlSelectFields = propertyResolver.getFieldsForProperties(tableMultiDataStreams, selectedProperties);
            sqlFrom = tableMultiDataStreams;
            sqlMainIdField = tableMultiDataStreams.getId();
        } else {
            switch (last.getType()) {
                case THING:
                    AbstractTableThings<J> tThings = (AbstractTableThings<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tableMultiDataStreams).on(tableMultiDataStreams.getThingId().eq(tThings.getId()));
                    needsDistinct = true;
                    break;

                case OBSERVATION:
                    AbstractTableObservations<J> tObservations = (AbstractTableObservations<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tableMultiDataStreams).on(tableMultiDataStreams.getId().eq(tObservations.getMultiDatastreamId()));
                    break;

                case SENSOR:
                    AbstractTableSensors<J> tSensors = (AbstractTableSensors<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tableMultiDataStreams).on(tableMultiDataStreams.getSensorId().eq(tSensors.getId()));
                    needsDistinct = true;
                    break;

                case OBSERVEDPROPERTY:
                    AbstractTableObsProperties<J> tObsProperties = (AbstractTableObsProperties<J>) last.getTable();
                    AbstractTableMultiDatastreamsObsProperties<J> tMdOp = tableCollection.tableMultiDatastreamsObsProperties.as(alias + "j1");
                    sqlFrom = sqlFrom.innerJoin(tMdOp).on(tObsProperties.getId().eq(tMdOp.getObsPropertyId()));
                    sqlFrom = sqlFrom.innerJoin(tableMultiDataStreams).on(tableMultiDataStreams.getId().eq(tMdOp.getMultiDatastreamId()));
                    if (!isFilter) {
                        getSqlSortFields().add(tMdOp.rank, OrderBy.OrderType.ASCENDING);
                    } else {
                        needsDistinct = true;
                    }
                    break;

                case MULTIDATASTREAM:
                    added = false;
                    break;

                default:
                    LOGGER.error("Do not know how to join {} onto Datastreams.", last.getType());
                    throw new IllegalStateException(DO_NOT_KNOW_HOW_TO_JOIN);
            }
        }
        if (added) {
            last = createJoinedRef(last, EntityType.MULTIDATASTREAM, tableMultiDataStreams, tableMultiDataStreams.getId());
        }
        if (entityId != null) {
            sqlWhere = sqlWhere.and(tableMultiDataStreams.getId().eq(entityId));
        }
        return last;
    }

    private TableRef<J> queryTasks(J entityId, TableRef<J> last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractTableTasks<J> tableTasks = tableCollection.tableTasks.as(alias);
        boolean added = true;
        if (last == null) {
            sqlSelectFields = propertyResolver.getFieldsForProperties(tableTasks, selectedProperties);
            sqlFrom = tableTasks;
            sqlMainIdField = tableTasks.getId();
        } else {
            switch (last.getType()) {
                case TASKINGCAPABILITY:
                    AbstractTableTaskingCapabilities<J> qTaskincCaps = (AbstractTableTaskingCapabilities<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tableTasks).on(qTaskincCaps.getId().eq(tableTasks.getTaskingCapabilityId()));
                    needsDistinct = true;
                    break;

                case TASK:
                    added = false;
                    break;

                default:
                    LOGGER.error("Do not know how to join {} onto Tasks.", last.getType());
                    throw new IllegalStateException(DO_NOT_KNOW_HOW_TO_JOIN);
            }
        }
        if (added) {
            last = createJoinedRef(last, EntityType.TASK, tableTasks, tableTasks.getId());
        }
        if (entityId != null) {
            sqlWhere = sqlWhere.and(tableTasks.getId().eq(entityId));
        }
        return last;
    }

    private TableRef<J> queryTaskingCapabilities(J entityId, TableRef<J> last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractTableTaskingCapabilities<J> tableTaskingCaps = tableCollection.tableTaskingCapabilities.as(alias);
        boolean added = true;
        if (last == null) {
            sqlSelectFields = propertyResolver.getFieldsForProperties(tableTaskingCaps, selectedProperties);
            sqlFrom = tableTaskingCaps;
            sqlMainIdField = tableTaskingCaps.getId();
        } else {
            switch (last.getType()) {
                case TASK:
                    AbstractTableTasks<J> qTasks = (AbstractTableTasks<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tableTaskingCaps).on(tableTaskingCaps.getId().eq(qTasks.getTaskingCapabilityId()));
                    break;

                case THING:
                    AbstractTableThings<J> qThings = (AbstractTableThings<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tableTaskingCaps).on(tableTaskingCaps.getThingId().eq(qThings.getId()));
                    needsDistinct = true;
                    break;

                case ACTUATOR:
                    AbstractTableActuators<J> qActuators = (AbstractTableActuators<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tableTaskingCaps).on(tableTaskingCaps.getActuatorId().eq(qActuators.getId()));
                    needsDistinct = true;
                    break;

                case TASKINGCAPABILITY:
                    added = false;
                    break;

                default:
                    LOGGER.error("Do not know how to join {} onto TaskingCapabilities.", last.getType());
                    throw new IllegalStateException(DO_NOT_KNOW_HOW_TO_JOIN);
            }
        }
        if (added) {
            last = createJoinedRef(last, EntityType.TASKINGCAPABILITY, tableTaskingCaps, tableTaskingCaps.getId());
        }
        if (entityId != null) {
            sqlWhere = sqlWhere.and(tableTaskingCaps.getId().eq(entityId));
        }
        return last;
    }

    private TableRef<J> queryThings(J entityId, TableRef<J> last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractTableThings<J> tableThings = tableCollection.tableThings.as(alias);
        boolean added = true;
        if (last == null) {
            sqlSelectFields = propertyResolver.getFieldsForProperties(tableThings, selectedProperties);
            sqlFrom = tableThings;
            sqlMainIdField = tableThings.getId();
        } else {
            switch (last.getType()) {
                case DATASTREAM:
                    AbstractTableDatastreams<J> tDatastreams = (AbstractTableDatastreams<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tableThings).on(tableThings.getId().eq(tDatastreams.getThingId()));
                    break;

                case MULTIDATASTREAM:
                    AbstractTableMultiDatastreams<J> tMultiDatastreams = (AbstractTableMultiDatastreams<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tableThings).on(tableThings.getId().eq(tMultiDatastreams.getThingId()));
                    break;

                case HISTORICALLOCATION:
                    AbstractTableHistLocations<J> tHistLocations = (AbstractTableHistLocations<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tableThings).on(tableThings.getId().eq(tHistLocations.getThingId()));
                    break;

                case LOCATION:
                    AbstractTableLocations<J> tLocations = (AbstractTableLocations<J>) last.getTable();
                    AbstractTableThingsLocations<J> tTL = tableCollection.tableThingsLocations.as(alias + "j1");
                    sqlFrom = sqlFrom.innerJoin(tTL).on(tLocations.getId().eq(tTL.getLocationId()));
                    sqlFrom = sqlFrom.innerJoin(tableThings).on(tableThings.getId().eq(tTL.getThingId()));
                    needsDistinct = true;
                    break;

                case TASKINGCAPABILITY:
                    AbstractTableTaskingCapabilities<J> tTskCaps = (AbstractTableTaskingCapabilities<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tableThings).on(tableThings.getId().eq(tTskCaps.getThingId()));
                    break;

                case THING:
                    added = false;
                    break;

                default:
                    LOGGER.error("Do not know how to join {} onto Things.", last.getType());
                    throw new IllegalStateException(DO_NOT_KNOW_HOW_TO_JOIN);
            }
        }
        if (added) {
            last = createJoinedRef(last, EntityType.THING, tableThings, tableThings.getId());
        }
        if (entityId != null) {
            sqlWhere = sqlWhere.and(tableThings.getId().eq(entityId));
        }
        return last;
    }

    private TableRef<J> queryFeatures(J entityId, TableRef<J> last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractTableFeatures<J> tableFeatures = tableCollection.tableFeatures.as(alias);
        boolean added = true;
        if (last == null) {
            sqlSelectFields = propertyResolver.getFieldsForProperties(tableFeatures, selectedProperties);
            sqlFrom = tableFeatures;
            sqlMainIdField = tableFeatures.getId();
        } else {
            switch (last.getType()) {
                case OBSERVATION:
                    AbstractTableObservations<J> tObservations = (AbstractTableObservations<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tableFeatures).on(tableFeatures.getId().eq(tObservations.getFeatureId()));
                    break;

                case FEATUREOFINTEREST:
                    added = false;
                    break;

                default:
                    LOGGER.error("Do not know how to join {} onto Features.", last.getType());
                    throw new IllegalStateException(DO_NOT_KNOW_HOW_TO_JOIN);
            }
        }
        if (added) {
            last = createJoinedRef(last, EntityType.FEATUREOFINTEREST, tableFeatures, tableFeatures.getId());
        }
        if (entityId != null) {
            sqlWhere = sqlWhere.and(tableFeatures.getId().eq(entityId));
        }
        return last;
    }

    private TableRef<J> queryHistLocations(J entityId, TableRef<J> last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractTableHistLocations<J> tableHistLocations = tableCollection.tableHistLocations.as(alias);
        boolean added = true;
        if (last == null) {
            sqlSelectFields = propertyResolver.getFieldsForProperties(tableHistLocations, selectedProperties);
            sqlFrom = tableHistLocations;
            sqlMainIdField = tableHistLocations.getId();
        } else {
            switch (last.getType()) {
                case THING:
                    AbstractTableThings<J> tThings = (AbstractTableThings<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tableHistLocations).on(tThings.getId().eq(tableHistLocations.getThingId()));
                    needsDistinct = true;
                    break;

                case LOCATION:
                    AbstractTableLocations<J> tLocations = (AbstractTableLocations<J>) last.getTable();
                    AbstractTableLocationsHistLocations<J> tLHL = tableCollection.tableLocationsHistLocations.as(alias + "j1");
                    sqlFrom = sqlFrom.innerJoin(tLHL).on(tLocations.getId().eq(tLHL.getLocationId()));
                    sqlFrom = sqlFrom.innerJoin(tableHistLocations).on(tableHistLocations.getId().eq(tLHL.getHistLocationId()));
                    needsDistinct = true;
                    break;

                case HISTORICALLOCATION:
                    added = false;
                    break;

                default:
                    LOGGER.error("Do not know how to join {} onto HistLocations.", last.getType());
                    throw new IllegalStateException(DO_NOT_KNOW_HOW_TO_JOIN);
            }
        }
        if (added) {
            last = createJoinedRef(last, EntityType.HISTORICALLOCATION, tableHistLocations, tableHistLocations.getId());
        }
        if (entityId != null) {
            sqlWhere = sqlWhere.and(tableHistLocations.getId().eq(entityId));
        }
        return last;
    }

    private TableRef<J> queryLocations(J entityId, TableRef<J> last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractTableLocations<J> tableLocations = tableCollection.tableLocations.as(alias);
        boolean added = true;
        if (last == null) {
            sqlSelectFields = propertyResolver.getFieldsForProperties(tableLocations, selectedProperties);
            sqlFrom = tableLocations;
            sqlMainIdField = tableLocations.getId();
        } else {
            switch (last.getType()) {
                case THING:
                    AbstractTableThings<J> tThings = (AbstractTableThings<J>) last.getTable();
                    AbstractTableThingsLocations<J> tTL = tableCollection.tableThingsLocations.as(alias + "j1");
                    sqlFrom = sqlFrom.innerJoin(tTL).on(tThings.getId().eq(tTL.getThingId()));
                    sqlFrom = sqlFrom.innerJoin(tableLocations).on(tableLocations.getId().eq(tTL.getLocationId()));
                    needsDistinct = true;
                    break;

                case HISTORICALLOCATION:
                    AbstractTableHistLocations<J> tHistLocations = (AbstractTableHistLocations<J>) last.getTable();
                    AbstractTableLocationsHistLocations<J> tLHL = tableCollection.tableLocationsHistLocations.as(alias + "j1");
                    sqlFrom = sqlFrom.innerJoin(tLHL).on(tHistLocations.getId().eq(tLHL.getHistLocationId()));
                    sqlFrom = sqlFrom.innerJoin(tableLocations).on(tableLocations.getId().eq(tLHL.getLocationId()));
                    needsDistinct = true;
                    break;

                case LOCATION:
                    added = false;
                    break;

                default:
                    LOGGER.error("Do not know how to join {} onto Locations.", last.getType());
                    throw new IllegalStateException(DO_NOT_KNOW_HOW_TO_JOIN);
            }
        }
        if (added) {
            last = createJoinedRef(last, EntityType.LOCATION, tableLocations, tableLocations.getId());
        }
        if (entityId != null) {
            sqlWhere = sqlWhere.and(tableLocations.getId().eq(entityId));
        }
        return last;
    }

    private TableRef<J> querySensors(J entityId, TableRef<J> last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractTableSensors<J> tableSensors = tableCollection.tableSensors.as(alias);
        boolean added = true;
        if (last == null) {
            sqlSelectFields = propertyResolver.getFieldsForProperties(tableSensors, selectedProperties);
            sqlFrom = tableSensors;
            sqlMainIdField = tableSensors.getId();
        } else {
            switch (last.getType()) {
                case DATASTREAM:
                    AbstractTableDatastreams<J> tDatastreams = (AbstractTableDatastreams<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tableSensors).on(tableSensors.getId().eq(tDatastreams.getSensorId()));
                    break;

                case MULTIDATASTREAM:
                    AbstractTableMultiDatastreams<J> tMultiDatastreams = (AbstractTableMultiDatastreams<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tableSensors).on(tableSensors.getId().eq(tMultiDatastreams.getSensorId()));
                    break;

                case SENSOR:
                    added = false;
                    break;

                default:
                    LOGGER.error("Do not know how to join {} onto Sensors.", last.getType());
                    throw new IllegalStateException(DO_NOT_KNOW_HOW_TO_JOIN);
            }
        }
        if (added) {
            last = createJoinedRef(last, EntityType.SENSOR, tableSensors, tableSensors.getId());
        }
        if (entityId != null) {
            sqlWhere = sqlWhere.and(tableSensors.getId().eq(entityId));
        }
        return last;
    }

    private TableRef<J> queryObservations(J entityId, TableRef<J> last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractTableObservations<J> tableObservations = tableCollection.tableObservations.as(alias);
        boolean added = true;
        if (last == null) {
            sqlSelectFields = propertyResolver.getFieldsForProperties(tableObservations, selectedProperties);
            sqlFrom = tableObservations;
            sqlMainIdField = tableObservations.getId();
        } else {
            switch (last.getType()) {
                case FEATUREOFINTEREST:
                    AbstractTableFeatures<J> tFeatures = (AbstractTableFeatures<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tableObservations).on(tFeatures.getId().eq(tableObservations.getFeatureId()));
                    needsDistinct = true;
                    break;

                case DATASTREAM:
                    AbstractTableDatastreams<J> tDatastreams = (AbstractTableDatastreams<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tableObservations).on(tDatastreams.getId().eq(tableObservations.getDatastreamId()));
                    needsDistinct = true;
                    break;

                case MULTIDATASTREAM:
                    AbstractTableMultiDatastreams<J> tMultiDatastreams = (AbstractTableMultiDatastreams<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tableObservations).on(tMultiDatastreams.getId().eq(tableObservations.getMultiDatastreamId()));
                    needsDistinct = true;
                    break;

                case OBSERVATION:
                    added = false;
                    break;

                default:
                    LOGGER.error("Do not know how to join {} onto Observations.", last.getType());
                    throw new IllegalStateException(DO_NOT_KNOW_HOW_TO_JOIN);
            }
        }
        if (added) {
            last = createJoinedRef(last, EntityType.OBSERVATION, tableObservations, tableObservations.getId());
        }
        if (entityId != null) {
            sqlWhere = sqlWhere.and(tableObservations.getId().eq(entityId));
        }
        return last;
    }

    private TableRef<J> queryObsProperties(J entityId, TableRef<J> last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractTableObsProperties<J> tableObsProperties = tableCollection.tableObsProperties.as(alias);
        boolean added = true;
        if (last == null) {
            sqlSelectFields = propertyResolver.getFieldsForProperties(tableObsProperties, selectedProperties);
            sqlFrom = tableObsProperties;
            sqlMainIdField = tableObsProperties.getId();
        } else {
            switch (last.getType()) {
                case MULTIDATASTREAM:
                    AbstractTableMultiDatastreams<J> tMultiDatastreams = (AbstractTableMultiDatastreams<J>) last.getTable();
                    AbstractTableMultiDatastreamsObsProperties<J> tMdOp = tableCollection.tableMultiDatastreamsObsProperties.as(alias + "j1");
                    sqlFrom = sqlFrom.innerJoin(tMdOp).on(tMultiDatastreams.getId().eq(tMdOp.getMultiDatastreamId()));
                    sqlFrom = sqlFrom.innerJoin(tableObsProperties).on(tableObsProperties.getId().eq(tMdOp.getObsPropertyId()));
                    needsDistinct = true;
                    break;

                case DATASTREAM:
                    AbstractTableDatastreams<J> tDatastreams = (AbstractTableDatastreams<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tableObsProperties).on(tableObsProperties.getId().eq(tDatastreams.getObsPropertyId()));
                    break;
                case OBSERVEDPROPERTY:
                    added = false;
                    break;

                default:
                    LOGGER.error("Do not know how to join {} onto ObsProperties.", last.getType());
                    throw new IllegalStateException(DO_NOT_KNOW_HOW_TO_JOIN);
            }
        }
        if (added) {
            last = createJoinedRef(last, EntityType.OBSERVEDPROPERTY, tableObsProperties, tableObsProperties.getId());
        }
        if (entityId != null) {
            sqlWhere = sqlWhere.and(tableObsProperties.getId().eq(entityId));
        }
        return last;
    }

    private Utils.SortSelectFields getSqlSortFields() {
        if (sqlSortFields == null) {
            sqlSortFields = new Utils.SortSelectFields();
        }
        return sqlSortFields;
    }

}
