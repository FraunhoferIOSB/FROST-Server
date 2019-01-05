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
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableFeatures;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableLocationsHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableMultiDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableMultiDatastreamsObsProperties;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableObsProperties;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableObservations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableSensors;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableThings;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.AbstractTableThingsLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.QCollection;
import de.fraunhofer.iosb.ilt.sta.persistence.pgjooq.relationalpaths.StaTable;
import de.fraunhofer.iosb.ilt.sta.query.Expand;
import de.fraunhofer.iosb.ilt.sta.query.OrderBy;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.query.expression.Expression;
import de.fraunhofer.iosb.ilt.sta.settings.PersistenceSettings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
    private final QCollection<J> qCollection;
    private Query staQuery;

    private Set<Property> selectedProperties;
    private final TableRef<J> lastPath = new TableRef<>();
    private TableRef<J> mainTable;
    private int aliasNr = 0;
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
        this.qCollection = propertyResolver.qCollection;
    }

    public ResultQuery<Record> buildSelect() {
        gatherData();

        if (sqlSelectFields == null) {
            sqlSelectFields = Collections.emptySet();
        }

        DSLContext dslContext = pm.createDdslContext();
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
        int count = 1;
        if (single) {
            count = 2;
        } else if (staQuery != null) {
            count = staQuery.getTopOrDefault() + 1;
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
     * Build a count query. Only use after requesting a select query using
     * buildSelect.
     *
     * @return the count query.
     */
    public ResultQuery<Record1<Integer>> buildCount() {
        gatherData();

        DSLContext dslContext = pm.createDdslContext();
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

        DSLContext dslContext = pm.createDdslContext();
        final StaTable<J, ?> table = qCollection.tablesByType.get(set.getEntityType());
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
        if (!lastPath.isEmpty()) {
            throw new IllegalStateException("QueryBuilder already used.");
        }
        selectedProperties = Collections.emptySet();
        queryEntityType(entityType, id, lastPath);
        single = true;
        return this;
    }

    public QueryBuilder<J> forPath(ResourcePath path) {
        if (!lastPath.isEmpty()) {
            throw new IllegalStateException("QueryBuilder already used.");
        }
        selectedProperties = new LinkedHashSet<>();
        int count = path.size();
        for (int i = count - 1; i >= 0; i--) {
            ResourcePathElement element = path.get(i);
            element.visit(this);
        }
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
            parseFilter(staQuery, settings);
            parseOrder(staQuery, settings);
        }
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

    private void queryDatastreams(J entityId, TableRef last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractTableDatastreams<J> tableDatastreams = qCollection.qDatastreams.as(alias);
        boolean added = true;
        if (last.getType() == null) {
            sqlSelectFields = propertyResolver.getExpressions(tableDatastreams, selectedProperties);
            sqlFrom = tableDatastreams;
            sqlMainIdField = tableDatastreams.getId();
        } else {
            switch (last.getType()) {
                case THING:
                    AbstractTableThings<J> qThings = (AbstractTableThings<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tableDatastreams).on(tableDatastreams.getThingId().eq(qThings.getId()));
                    needsDistinct = true;
                    break;

                case OBSERVATION:
                    AbstractTableObservations<J> qObservations = (AbstractTableObservations<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tableDatastreams).on(tableDatastreams.getId().eq(qObservations.getDatastreamId()));
                    break;

                case SENSOR:
                    AbstractTableSensors<J> qSensors = (AbstractTableSensors<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tableDatastreams).on(tableDatastreams.getSensorId().eq(qSensors.getId()));
                    needsDistinct = true;
                    break;

                case OBSERVEDPROPERTY:
                    AbstractTableObsProperties<J> qObsProperties = (AbstractTableObsProperties<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tableDatastreams).on(tableDatastreams.getObsPropertyId().eq(qObsProperties.getId()));
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
        AbstractTableMultiDatastreams<J> tableMultiDataStreams = qCollection.qMultiDatastreams.as(alias);
        boolean added = true;
        if (last.getType() == null) {
            sqlSelectFields = propertyResolver.getExpressions(tableMultiDataStreams, selectedProperties);
            sqlFrom = tableMultiDataStreams;
            sqlMainIdField = tableMultiDataStreams.getId();
        } else {
            switch (last.getType()) {
                case THING:
                    AbstractTableThings<J> qThings = (AbstractTableThings<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tableMultiDataStreams).on(tableMultiDataStreams.getThingId().eq(qThings.getId()));
                    needsDistinct = true;
                    break;

                case OBSERVATION:
                    AbstractTableObservations<J> qObservations = (AbstractTableObservations<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tableMultiDataStreams).on(tableMultiDataStreams.getId().eq(qObservations.getMultiDatastreamId()));
                    break;

                case SENSOR:
                    AbstractTableSensors<J> qSensors = (AbstractTableSensors<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tableMultiDataStreams).on(tableMultiDataStreams.getSensorId().eq(qSensors.getId()));
                    needsDistinct = true;
                    break;

                case OBSERVEDPROPERTY:
                    AbstractTableObsProperties<J> qObsProperties = (AbstractTableObsProperties<J>) last.getTable();
                    AbstractTableMultiDatastreamsObsProperties<J> qMdOp = qCollection.qMultiDatastreamsObsProperties.as(alias + "j1");
                    sqlFrom = sqlFrom.innerJoin(qMdOp).on(qObsProperties.getId().eq(qMdOp.getObsPropertyId()));
                    sqlFrom = sqlFrom.innerJoin(tableMultiDataStreams).on(tableMultiDataStreams.getId().eq(qMdOp.getMultiDatastreamId()));
                    if (!isFilter) {
                        sqlSortFields.add(qMdOp.rank.asc());
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

    private void queryThings(J entityId, TableRef last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractTableThings<J> tableThings = qCollection.qThings.as(alias);
        boolean added = true;
        if (last.getType() == null) {
            sqlSelectFields = propertyResolver.getExpressions(tableThings, selectedProperties);
            sqlFrom = tableThings;
            sqlMainIdField = tableThings.getId();
        } else {
            switch (last.getType()) {
                case DATASTREAM:
                    AbstractTableDatastreams<J> qDatastreams = (AbstractTableDatastreams<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tableThings).on(tableThings.getId().eq(qDatastreams.getThingId()));
                    break;

                case MULTIDATASTREAM:
                    AbstractTableMultiDatastreams<J> qMultiDatastreams = (AbstractTableMultiDatastreams<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tableThings).on(tableThings.getId().eq(qMultiDatastreams.getThingId()));
                    break;

                case HISTORICALLOCATION:
                    AbstractTableHistLocations<J> qHistLocations = (AbstractTableHistLocations<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tableThings).on(tableThings.getId().eq(qHistLocations.getThingId()));
                    break;

                case LOCATION:
                    AbstractTableLocations<J> qLocations = (AbstractTableLocations<J>) last.getTable();
                    AbstractTableThingsLocations<J> qTL = qCollection.qThingsLocations.as(alias + "j1");
                    sqlFrom = sqlFrom.innerJoin(qTL).on(qLocations.getId().eq(qTL.getLocationId()));
                    sqlFrom = sqlFrom.innerJoin(tableThings).on(tableThings.getId().eq(qTL.getThingId()));
                    needsDistinct = true;
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
        AbstractTableFeatures<J> tableFeatures = qCollection.qFeatures.as(alias);
        boolean added = true;
        if (last.getType() == null) {
            sqlSelectFields = propertyResolver.getExpressions(tableFeatures, selectedProperties);
            sqlFrom = tableFeatures;
            sqlMainIdField = tableFeatures.getId();
        } else {
            switch (last.getType()) {
                case OBSERVATION:
                    AbstractTableObservations<J> qObservations = (AbstractTableObservations<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(tableFeatures).on(tableFeatures.getId().eq(qObservations.getFeatureId()));
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
        AbstractTableHistLocations<J> qHistLocations = qCollection.qHistLocations.as(alias);
        boolean added = true;
        if (last.getType() == null) {
            sqlSelectFields = propertyResolver.getExpressions(qHistLocations, selectedProperties);
            sqlFrom = qHistLocations;
            sqlMainIdField = qHistLocations.getId();
        } else {
            switch (last.getType()) {
                case THING:
                    AbstractTableThings<J> qThings = (AbstractTableThings<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(qHistLocations).on(qThings.getId().eq(qHistLocations.getThingId()));
                    needsDistinct = true;
                    break;

                case LOCATION:
                    AbstractTableLocations<J> qLocations = (AbstractTableLocations<J>) last.getTable();
                    AbstractTableLocationsHistLocations<J> qLHL = qCollection.qLocationsHistLocations.as(alias + "j1");
                    sqlFrom = sqlFrom.innerJoin(qLHL).on(qLocations.getId().eq(qLHL.getLocationId()));
                    sqlFrom = sqlFrom.innerJoin(qHistLocations).on(qHistLocations.getId().eq(qLHL.getHistLocationId()));
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
            last.setTable(qHistLocations);
            last.setIdField(qHistLocations.getId());
        }
        if (entityId != null) {
            sqlWhere = sqlWhere.and(qHistLocations.getId().eq(entityId));
        }
    }

    private void queryLocations(J entityId, TableRef last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractTableLocations<J> qLocations = qCollection.qLocations.as(alias);
        boolean added = true;
        if (last.getType() == null) {
            sqlSelectFields = propertyResolver.getExpressions(qLocations, selectedProperties);
            sqlFrom = qLocations;
            sqlMainIdField = qLocations.getId();
        } else {
            switch (last.getType()) {
                case THING:
                    AbstractTableThings<J> qThings = (AbstractTableThings<J>) last.getTable();
                    AbstractTableThingsLocations<J> qTL = qCollection.qThingsLocations.as(alias + "j1");
                    sqlFrom = sqlFrom.innerJoin(qTL).on(qThings.getId().eq(qTL.getThingId()));
                    sqlFrom = sqlFrom.innerJoin(qLocations).on(qLocations.getId().eq(qTL.getLocationId()));
                    needsDistinct = true;
                    break;

                case HISTORICALLOCATION:
                    AbstractTableHistLocations<J> qHistLocations = (AbstractTableHistLocations<J>) last.getTable();
                    AbstractTableLocationsHistLocations<J> qLHL = qCollection.qLocationsHistLocations.as(alias + "j1");
                    sqlFrom = sqlFrom.innerJoin(qLHL).on(qHistLocations.getId().eq(qLHL.getHistLocationId()));
                    sqlFrom = sqlFrom.innerJoin(qLocations).on(qLocations.getId().eq(qLHL.getLocationId()));
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
            last.setTable(qLocations);
            last.setIdField(qLocations.getId());
        }
        if (entityId != null) {
            sqlWhere = sqlWhere.and(qLocations.getId().eq(entityId));
        }
    }

    private void querySensors(J entityId, TableRef last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractTableSensors<J> qSensors = qCollection.qSensors.as(alias);
        boolean added = true;
        if (last.getType() == null) {
            sqlSelectFields = propertyResolver.getExpressions(qSensors, selectedProperties);
            sqlFrom = qSensors;
            sqlMainIdField = qSensors.getId();
        } else {
            switch (last.getType()) {
                case DATASTREAM:
                    AbstractTableDatastreams<J> qDatastreams = (AbstractTableDatastreams<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(qSensors).on(qSensors.getId().eq(qDatastreams.getSensorId()));
                    break;

                case MULTIDATASTREAM:
                    AbstractTableMultiDatastreams<J> qMultiDatastreams = (AbstractTableMultiDatastreams<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(qSensors).on(qSensors.getId().eq(qMultiDatastreams.getSensorId()));
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
            last.setTable(qSensors);
            last.setIdField(qSensors.getId());
        }
        if (entityId != null) {
            sqlWhere = sqlWhere.and(qSensors.getId().eq(entityId));
        }
    }

    private void queryObservations(J entityId, TableRef last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractTableObservations<J> qObservations = qCollection.qObservations.as(alias);
        boolean added = true;
        if (last.getType() == null) {
            sqlSelectFields = propertyResolver.getExpressions(qObservations, selectedProperties);
            sqlFrom = qObservations;
            sqlMainIdField = qObservations.getId();
        } else {
            switch (last.getType()) {
                case FEATUREOFINTEREST:
                    AbstractTableFeatures<J> qFeatures = (AbstractTableFeatures<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(qObservations).on(qFeatures.getId().eq(qObservations.getFeatureId()));
                    needsDistinct = true;
                    break;

                case DATASTREAM:
                    AbstractTableDatastreams<J> qDatastreams = (AbstractTableDatastreams<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(qObservations).on(qDatastreams.getId().eq(qObservations.getDatastreamId()));
                    needsDistinct = true;
                    break;

                case MULTIDATASTREAM:
                    AbstractTableMultiDatastreams<J> qMultiDatastreams = (AbstractTableMultiDatastreams<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(qObservations).on(qMultiDatastreams.getId().eq(qObservations.getMultiDatastreamId()));
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
            last.setTable(qObservations);
            last.setIdField(qObservations.getId());
        }
        if (entityId != null) {
            sqlWhere = sqlWhere.and(qObservations.getId().eq(entityId));
        }
    }

    private void queryObsProperties(J entityId, TableRef last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractTableObsProperties<J> qObsProperties = qCollection.qObsProperties.as(alias);
        boolean added = true;
        if (last.getType() == null) {
            sqlSelectFields = propertyResolver.getExpressions(qObsProperties, selectedProperties);
            sqlFrom = qObsProperties;
            sqlMainIdField = qObsProperties.getId();
        } else {
            switch (last.getType()) {
                case MULTIDATASTREAM:
                    AbstractTableMultiDatastreams<J> qMultiDatastreams = (AbstractTableMultiDatastreams<J>) last.getTable();
                    AbstractTableMultiDatastreamsObsProperties<J> qMdOp = qCollection.qMultiDatastreamsObsProperties.as(alias + "j1");
                    sqlFrom = sqlFrom.innerJoin(qMdOp).on(qMultiDatastreams.getId().eq(qMdOp.getMultiDatastreamId()));
                    sqlFrom = sqlFrom.innerJoin(qObsProperties).on(qObsProperties.getId().eq(qMdOp.getObsPropertyId()));
                    needsDistinct = true;
                    break;

                case DATASTREAM:
                    AbstractTableDatastreams<J> qDatastreams = (AbstractTableDatastreams<J>) last.getTable();
                    sqlFrom = sqlFrom.innerJoin(qObsProperties).on(qObsProperties.getId().eq(qDatastreams.getObsPropertyId()));
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
            last.setTable(qObsProperties);
            last.setIdField(qObsProperties.getId());
        }
        if (entityId != null) {
            sqlWhere = sqlWhere.and(qObsProperties.getId().eq(entityId));
        }
    }

    private List<OrderField> getSqlSortFields() {
        if (sqlSortFields == null) {
            sqlSortFields = new ArrayList<>();
        }
        return sqlSortFields;
    }

}
