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
package de.fraunhofer.iosb.ilt.sta.persistence.pgjooq;

import de.fraunhofer.iosb.ilt.sta.model.core.Id;
import de.fraunhofer.iosb.ilt.sta.path.CustomPropertyArrayIndex;
import de.fraunhofer.iosb.ilt.sta.path.CustomPropertyPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityProperty;
import de.fraunhofer.iosb.ilt.sta.path.EntitySetPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityType;
import de.fraunhofer.iosb.ilt.sta.path.NavigationProperty;
import de.fraunhofer.iosb.ilt.sta.path.Property;
import de.fraunhofer.iosb.ilt.sta.path.PropertyPathElement;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePath;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePathElement;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePathVisitor;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableActuators;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableFeatures;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableLocationsHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableMultiDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableMultiDatastreamsObsProperties;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableObsProperties;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableObservations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableSensors;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableTaskingCapabilities;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableTasks;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableThings;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.AbstractTableThingsLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.StaTable;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.tables.TableCollection;
import de.fraunhofer.iosb.ilt.sta.query.Expand;
import de.fraunhofer.iosb.ilt.sta.query.OrderBy;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.query.expression.Expression;
import de.fraunhofer.iosb.ilt.sta.settings.PersistenceSettings;
import java.util.ArrayList;
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
    private final TableRef<J> lastPath = new TableRef<>();
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
    private List<OrderField> sqlSortFields;

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
            selectStep = dslContext.selectDistinct(sqlSelectFields);
        } else {
            selectStep = dslContext.select(sqlSelectFields);
        }
        SelectConditionStep<Record> whereStep = selectStep.from(sqlFrom)
                .where(sqlWhere);

        final List<OrderField> sortFields = getSqlSortFields();
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
            LOGGER.trace("Generated SQL:\n{}", limit.getSQL(ParamType.INDEXED));
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
            LOGGER.trace("Generated SQL:\n{}", query.getSQL(ParamType.INDEXED));
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
            LOGGER.trace("Generated SQL:\n{}", delete.getSQL(ParamType.INDEXED));
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

            parseFilter(staQuery, settings);
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
        queryEntityType(requestedEntityType, requestedId, lastPath);
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

    private void parseOrder(Query query, PersistenceSettings settings) {
        if (query != null) {
            PgExpressionHandler handler = new PgExpressionHandler(this, mainTable.copy());
            for (OrderBy ob : query.getOrderBy()) {
                handler.addOrderbyToQuery(ob, getSqlSortFields());
            }
            if (settings.getAlwaysOrderbyId()) {
                getSqlSortFields().add(mainTable.getIdField().asc());
            }
        }
    }

    public void parseFilter(Query query, PersistenceSettings settings) {
        if (query != null) {
            isFilter = true;
            PgExpressionHandler handler = new PgExpressionHandler(this, mainTable.copy());
            Expression filter = query.getFilter();
            if (filter != null) {
                sqlWhere = handler.addFilterToWhere(filter, sqlWhere);
            }
        }
    }

    @Override
    public void visit(EntityPathElement element) {
        queryEntityType(element.getEntityType(), element.getId(), lastPath);
    }

    @Override
    public void visit(EntitySetPathElement element) {
        queryEntityType(element.getEntityType(), null, lastPath);
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

    public void queryEntityType(EntityType type, Id targetId, TableRef last) {
        J id = null;
        if (targetId != null) {
            if (targetId.getBasicPersistenceType() != propertyResolver.getBasicPersistenceType()) {
                throw new IllegalArgumentException("This implementation expects " + propertyResolver.getBasicPersistenceType() + " ids, not " + targetId.getBasicPersistenceType());
            }
            id = (J) targetId.asBasicPersistenceType();
        }

        switch (type) {
            case ACTUATOR:
                queryActuator(id, last);
                break;

            case DATASTREAM:
                queryDatastreams(id, last);
                break;

            case MULTIDATASTREAM:
                queryMultiDatastreams(id, last);
                break;

            case FEATUREOFINTEREST:
                queryFeatures(id, last);
                break;

            case HISTORICALLOCATION:
                queryHistLocations(id, last);
                break;

            case LOCATION:
                queryLocations(id, last);
                break;

            case OBSERVATION:
                queryObservations(id, last);
                break;

            case OBSERVEDPROPERTY:
                queryObsProperties(id, last);
                break;

            case SENSOR:
                querySensors(id, last);
                break;

            case TASK:
                queryTasks(id, last);
                break;

            case TASKINGCAPABILITY:
                queryTaskingCapabilities(id, last);
                break;

            case THING:
                queryThings(id, last);
                break;

            default:
                LOGGER.error("Unknown entity type {}!?", type);
                throw new IllegalStateException("Unknown entity type " + type);
        }
        if (mainTable == null && !last.isEmpty()) {
            mainTable = new TableRef(last);
        }

    }

    public PropertyResolver<J> getPropertyResolver() {
        return propertyResolver;
    }

    private void queryActuator(J entityId, TableRef last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractTableActuators<J> tableActuators = tableCollection.tableActuators.as(alias);
        boolean added = true;
        if (last.getType() == null) {
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
            last.setType(EntityType.ACTUATOR);
            last.setTable(tableActuators);
            last.setIdField(tableActuators.getId());
        }
        if (entityId != null) {
            sqlWhere = sqlWhere.and(tableActuators.getId().eq(entityId));
        }
    }

    private void queryDatastreams(J entityId, TableRef last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractTableDatastreams<J> tableDatastreams = tableCollection.tableDatastreams.as(alias);
        boolean added = true;
        if (last.getType() == null) {
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
            last.setType(EntityType.DATASTREAM);
            last.setTable(tableDatastreams);
            last.setIdField(tableDatastreams.getId());
        }
        if (entityId != null) {
            sqlWhere = sqlWhere.and(tableDatastreams.getId().eq(entityId));
        }
    }

    private void queryMultiDatastreams(J entityId, TableRef last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractTableMultiDatastreams<J> tableMultiDataStreams = tableCollection.tableMultiDatastreams.as(alias);
        boolean added = true;
        if (last.getType() == null) {
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
                        getSqlSortFields().add(tMdOp.rank.asc());
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
            last.setType(EntityType.MULTIDATASTREAM);
            last.setTable(tableMultiDataStreams);
            last.setIdField(tableMultiDataStreams.getId());
        }
        if (entityId != null) {
            sqlWhere = sqlWhere.and(tableMultiDataStreams.getId().eq(entityId));
        }
    }

    private void queryTasks(J entityId, TableRef last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractTableTasks<J> tableTasks = tableCollection.tableTasks.as(alias);
        boolean added = true;
        if (last.getType() == null) {
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
            last.setType(EntityType.TASK);
            last.setTable(tableTasks);
            last.setIdField(tableTasks.getId());
        }
        if (entityId != null) {
            sqlWhere = sqlWhere.and(tableTasks.getId().eq(entityId));
        }
    }

    private void queryTaskingCapabilities(J entityId, TableRef last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractTableTaskingCapabilities<J> tableTaskingCaps = tableCollection.tableTaskingCapabilities.as(alias);
        boolean added = true;
        if (last.getType() == null) {
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
            last.setType(EntityType.TASKINGCAPABILITY);
            last.setTable(tableTaskingCaps);
            last.setIdField(tableTaskingCaps.getId());
        }
        if (entityId != null) {
            sqlWhere = sqlWhere.and(tableTaskingCaps.getId().eq(entityId));
        }
    }

    private void queryThings(J entityId, TableRef last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractTableThings<J> tableThings = tableCollection.tableThings.as(alias);
        boolean added = true;
        if (last.getType() == null) {
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
            last.setType(EntityType.THING);
            last.setTable(tableThings);
            last.setIdField(tableThings.getId());
        }
        if (entityId != null) {
            sqlWhere = sqlWhere.and(tableThings.getId().eq(entityId));
        }
    }

    private void queryFeatures(J entityId, TableRef last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractTableFeatures<J> tableFeatures = tableCollection.tableFeatures.as(alias);
        boolean added = true;
        if (last.getType() == null) {
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
            last.setType(EntityType.FEATUREOFINTEREST);
            last.setTable(tableFeatures);
            last.setIdField(tableFeatures.getId());
        }
        if (entityId != null) {
            sqlWhere = sqlWhere.and(tableFeatures.getId().eq(entityId));
        }
    }

    private void queryHistLocations(J entityId, TableRef last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractTableHistLocations<J> tHistLocations = tableCollection.tableHistLocations.as(alias);
        boolean added = true;
        if (last.getType() == null) {
            sqlSelectFields = propertyResolver.getFieldsForProperties(tHistLocations, selectedProperties);
            sqlFrom = tHistLocations;
            sqlMainIdField = tHistLocations.getId();
        } else {
            switch (last.getType()) {
                case THING:
                    AbstractTableThings<J> tThings = (AbstractTableThings<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tHistLocations).on(tThings.getId().eq(tHistLocations.getThingId()));
                    needsDistinct = true;
                    break;

                case LOCATION:
                    AbstractTableLocations<J> tLocations = (AbstractTableLocations<J>) last.getTable();
                    AbstractTableLocationsHistLocations<J> tLHL = tableCollection.tableLocationsHistLocations.as(alias + "j1");
                    sqlFrom = sqlFrom.innerJoin(tLHL).on(tLocations.getId().eq(tLHL.getLocationId()));
                    sqlFrom = sqlFrom.innerJoin(tHistLocations).on(tHistLocations.getId().eq(tLHL.getHistLocationId()));
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
            last.setType(EntityType.HISTORICALLOCATION);
            last.setTable(tHistLocations);
            last.setIdField(tHistLocations.getId());
        }
        if (entityId != null) {
            sqlWhere = sqlWhere.and(tHistLocations.getId().eq(entityId));
        }
    }

    private void queryLocations(J entityId, TableRef last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractTableLocations<J> tLocations = tableCollection.tableLocations.as(alias);
        boolean added = true;
        if (last.getType() == null) {
            sqlSelectFields = propertyResolver.getFieldsForProperties(tLocations, selectedProperties);
            sqlFrom = tLocations;
            sqlMainIdField = tLocations.getId();
        } else {
            switch (last.getType()) {
                case THING:
                    AbstractTableThings<J> tThings = (AbstractTableThings<J>) last.getTable();
                    AbstractTableThingsLocations<J> tTL = tableCollection.tableThingsLocations.as(alias + "j1");
                    sqlFrom = sqlFrom.innerJoin(tTL).on(tThings.getId().eq(tTL.getThingId()));
                    sqlFrom = sqlFrom.innerJoin(tLocations).on(tLocations.getId().eq(tTL.getLocationId()));
                    needsDistinct = true;
                    break;

                case HISTORICALLOCATION:
                    AbstractTableHistLocations<J> tHistLocations = (AbstractTableHistLocations<J>) last.getTable();
                    AbstractTableLocationsHistLocations<J> tLHL = tableCollection.tableLocationsHistLocations.as(alias + "j1");
                    sqlFrom = sqlFrom.innerJoin(tLHL).on(tHistLocations.getId().eq(tLHL.getHistLocationId()));
                    sqlFrom = sqlFrom.innerJoin(tLocations).on(tLocations.getId().eq(tLHL.getLocationId()));
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
            last.setType(EntityType.LOCATION);
            last.setTable(tLocations);
            last.setIdField(tLocations.getId());
        }
        if (entityId != null) {
            sqlWhere = sqlWhere.and(tLocations.getId().eq(entityId));
        }
    }

    private void querySensors(J entityId, TableRef last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractTableSensors<J> tSensors = tableCollection.tableSensors.as(alias);
        boolean added = true;
        if (last.getType() == null) {
            sqlSelectFields = propertyResolver.getFieldsForProperties(tSensors, selectedProperties);
            sqlFrom = tSensors;
            sqlMainIdField = tSensors.getId();
        } else {
            switch (last.getType()) {
                case DATASTREAM:
                    AbstractTableDatastreams<J> tDatastreams = (AbstractTableDatastreams<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tSensors).on(tSensors.getId().eq(tDatastreams.getSensorId()));
                    break;

                case MULTIDATASTREAM:
                    AbstractTableMultiDatastreams<J> tMultiDatastreams = (AbstractTableMultiDatastreams<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tSensors).on(tSensors.getId().eq(tMultiDatastreams.getSensorId()));
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
            last.setType(EntityType.SENSOR);
            last.setTable(tSensors);
            last.setIdField(tSensors.getId());
        }
        if (entityId != null) {
            sqlWhere = sqlWhere.and(tSensors.getId().eq(entityId));
        }
    }

    private void queryObservations(J entityId, TableRef last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractTableObservations<J> tObservations = tableCollection.tableObservations.as(alias);
        boolean added = true;
        if (last.getType() == null) {
            sqlSelectFields = propertyResolver.getFieldsForProperties(tObservations, selectedProperties);
            sqlFrom = tObservations;
            sqlMainIdField = tObservations.getId();
        } else {
            switch (last.getType()) {
                case FEATUREOFINTEREST:
                    AbstractTableFeatures<J> tFeatures = (AbstractTableFeatures<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tObservations).on(tFeatures.getId().eq(tObservations.getFeatureId()));
                    needsDistinct = true;
                    break;

                case DATASTREAM:
                    AbstractTableDatastreams<J> tDatastreams = (AbstractTableDatastreams<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tObservations).on(tDatastreams.getId().eq(tObservations.getDatastreamId()));
                    needsDistinct = true;
                    break;

                case MULTIDATASTREAM:
                    AbstractTableMultiDatastreams<J> tMultiDatastreams = (AbstractTableMultiDatastreams<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tObservations).on(tMultiDatastreams.getId().eq(tObservations.getMultiDatastreamId()));
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
            last.setType(EntityType.OBSERVATION);
            last.setTable(tObservations);
            last.setIdField(tObservations.getId());
        }
        if (entityId != null) {
            sqlWhere = sqlWhere.and(tObservations.getId().eq(entityId));
        }
    }

    private void queryObsProperties(J entityId, TableRef last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractTableObsProperties<J> tObsProperties = tableCollection.tableObsProperties.as(alias);
        boolean added = true;
        if (last.getType() == null) {
            sqlSelectFields = propertyResolver.getFieldsForProperties(tObsProperties, selectedProperties);
            sqlFrom = tObsProperties;
            sqlMainIdField = tObsProperties.getId();
        } else {
            switch (last.getType()) {
                case MULTIDATASTREAM:
                    AbstractTableMultiDatastreams<J> tMultiDatastreams = (AbstractTableMultiDatastreams<J>) last.getTable();
                    AbstractTableMultiDatastreamsObsProperties<J> tMdOp = tableCollection.tableMultiDatastreamsObsProperties.as(alias + "j1");
                    sqlFrom = sqlFrom.innerJoin(tMdOp).on(tMultiDatastreams.getId().eq(tMdOp.getMultiDatastreamId()));
                    sqlFrom = sqlFrom.innerJoin(tObsProperties).on(tObsProperties.getId().eq(tMdOp.getObsPropertyId()));
                    needsDistinct = true;
                    break;

                case DATASTREAM:
                    AbstractTableDatastreams<J> tDatastreams = (AbstractTableDatastreams<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tObsProperties).on(tObsProperties.getId().eq(tDatastreams.getObsPropertyId()));
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
            last.setType(EntityType.OBSERVEDPROPERTY);
            last.setTable(tObsProperties);
            last.setIdField(tObsProperties.getId());
        }
        if (entityId != null) {
            sqlWhere = sqlWhere.and(tObsProperties.getId().eq(entityId));
        }
    }

    private List<OrderField> getSqlSortFields() {
        if (sqlSortFields == null) {
            sqlSortFields = new ArrayList<>();
        }
        return sqlSortFields;
    }

}
