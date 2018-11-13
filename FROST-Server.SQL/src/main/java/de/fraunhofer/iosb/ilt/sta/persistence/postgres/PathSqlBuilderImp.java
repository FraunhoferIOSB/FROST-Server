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
package de.fraunhofer.iosb.ilt.sta.persistence.postgres;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.sql.SQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import com.querydsl.sql.dml.SQLDeleteClause;
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
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQFeatures;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQLocationsHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQMultiDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQMultiDatastreamsObsProperties;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQObsProperties;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQObservations;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQSensors;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQThings;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.AbstractQThingsLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.relationalpaths.QCollection;
import de.fraunhofer.iosb.ilt.sta.query.Expand;
import de.fraunhofer.iosb.ilt.sta.query.OrderBy;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.settings.PersistenceSettings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds a path for a query. Should not be re-used.
 *
 * @author scf
 * @param <I> The type of path used for the ID fields.
 * @param <J> The type of the ID fields.
 */
public class PathSqlBuilderImp<I extends ComparableExpressionBase<J> & Path<J>, J extends Comparable> implements PathSqlBuilder {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PathSqlBuilderImp.class);
    private static final String DO_NOT_KNOW_HOW_TO_JOIN = "Do not know how to join";
    /**
     * The prefix used for table aliases. The main entity is always
     * &lt;PREFIX&gt;1.
     */
    public static final String ALIAS_PREFIX = "e";

    private final PropertyResolver<I, J> propertyResolver;
    private final QCollection<I, J> qCollection;

    private SQLQuery<Tuple> sqlQuery;
    private Set<Property> selectedProperties;
    private final TableRef<I, J> lastPath = new TableRef<>();
    private TableRef<I, J> mainTable;
    private int aliasNr = 0;
    private boolean isFilter = false;
    private boolean needsDistinct = false;

    public PathSqlBuilderImp(PropertyResolver<I, J> propertyResolver) {
        this.propertyResolver = propertyResolver;
        this.qCollection = propertyResolver.qCollection;
    }

    @Override
    public synchronized SQLQuery<Tuple> buildFor(EntityType entityType, Id id, SQLQueryFactory sqlQueryFactory, PersistenceSettings settings) {
        selectedProperties = Collections.emptySet();
        sqlQuery = sqlQueryFactory.select();
        lastPath.clear();
        aliasNr = 0;
        queryEntityType(entityType, id, lastPath);
        return sqlQuery;
    }

    @Override
    public synchronized SQLQuery<Tuple> buildFor(ResourcePath path, Query query, SQLQueryFactory sqlQueryFactory, PersistenceSettings settings) {
        findSelectedProperties(query);

        sqlQuery = sqlQueryFactory.select();
        lastPath.clear();
        aliasNr = 0;
        List<ResourcePathElement> elements = new ArrayList<>(path.getPathElements());

        int count = elements.size();
        for (int i = count - 1; i >= 0; i--) {
            ResourcePathElement element = elements.get(i);
            element.visit(this);
        }

        addOrderAndFilter(query, settings);

        return sqlQuery;
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

    private void addOrderAndFilter(Query query, PersistenceSettings settings) {
        if (query != null) {
            PgExpressionHandler handler = new PgExpressionHandler(this, mainTable.copy());
            for (OrderBy ob : query.getOrderBy()) {
                handler.addOrderbyToQuery(ob, sqlQuery);
            }
            isFilter = true;
            de.fraunhofer.iosb.ilt.sta.query.expression.Expression filter = query.getFilter();
            if (filter != null) {
                handler.addFilterToQuery(filter, sqlQuery);
            }
            if (settings.getAlwaysOrderbyId()) {
                sqlQuery.orderBy(mainTable.getIdPath().asc());
            }
            if (needsDistinct) {
                sqlQuery.distinct();
            }
        }
    }

    public SQLDeleteClause createDelete(EntitySetPathElement set, SQLQueryFactory sqlQueryFactory, SubQueryExpression idSelect) {
        switch (set.getEntityType()) {
            case DATASTREAM:
                return sqlQueryFactory.delete(qCollection.qDatastreams).where(qCollection.qDatastreams.getId().in(idSelect));

            case MULTIDATASTREAM:
                return sqlQueryFactory.delete(qCollection.qMultiDatastreams).where(qCollection.qMultiDatastreams.getId().in(idSelect));

            case FEATUREOFINTEREST:
                return sqlQueryFactory.delete(qCollection.qFeatures).where(qCollection.qFeatures.getId().in(idSelect));

            case HISTORICALLOCATION:
                return sqlQueryFactory.delete(qCollection.qHistLocations).where(qCollection.qHistLocations.getId().in(idSelect));

            case LOCATION:
                return sqlQueryFactory.delete(qCollection.qLocations).where(qCollection.qLocations.getId().in(idSelect));

            case OBSERVATION:
                return sqlQueryFactory.delete(qCollection.qObservations).where(qCollection.qObservations.getId().in(idSelect));

            case OBSERVEDPROPERTY:
                return sqlQueryFactory.delete(qCollection.qObsProperties).where(qCollection.qObsProperties.getId().in(idSelect));

            case SENSOR:
                return sqlQueryFactory.delete(qCollection.qSensors).where(qCollection.qSensors.getId().in(idSelect));

            case THING:
                return sqlQueryFactory.delete(qCollection.qThings).where(qCollection.qThings.getId().in(idSelect));

            default:
                throw new AssertionError("Don't know how to delete" + set.getEntityType().name(), new IllegalArgumentException("Unknown type for delete"));
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

    @Override
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

    @Override
    public Map<String, Expression> expressionsForProperty(EntityProperty property, Path<?> qPath, Map<String, Expression> target) {
        return propertyResolver.expressionsForProperty(property, qPath, target);
    }

    private void queryDatastreams(J entityId, TableRef last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractQDatastreams<? extends AbstractQDatastreams, I, J> qDataStreams = qCollection.qDatastreams.newWithAlias(alias);
        boolean added = true;
        if (last.getType() == null) {
            sqlQuery.select(propertyResolver.getExpressions(qDataStreams, selectedProperties));
            sqlQuery.from(qDataStreams);
        } else {
            switch (last.getType()) {
                case THING:
                    AbstractQThings<?, I, J> qThings = (AbstractQThings<?, I, J>) last.getqPath();
                    sqlQuery.innerJoin(qDataStreams).on(qDataStreams.getThingId().eq(qThings.getId()));
                    needsDistinct = true;
                    break;

                case OBSERVATION:
                    AbstractQObservations<?, I, J> qObservations = (AbstractQObservations<?, I, J>) last.getqPath();
                    sqlQuery.innerJoin(qDataStreams).on(qDataStreams.getId().eq(qObservations.getDatastreamId()));
                    break;

                case SENSOR:
                    AbstractQSensors<?, I, J> qSensors = (AbstractQSensors<?, I, J>) last.getqPath();
                    sqlQuery.innerJoin(qDataStreams).on(qDataStreams.getSensorId().eq(qSensors.getId()));
                    needsDistinct = true;
                    break;

                case OBSERVEDPROPERTY:
                    AbstractQObsProperties<?, I, J> qObsProperties = (AbstractQObsProperties<?, I, J>) last.getqPath();
                    sqlQuery.innerJoin(qDataStreams).on(qDataStreams.getObsPropertyId().eq(qObsProperties.getId()));
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
            last.setqPath(qDataStreams);
            last.setIdPath(qDataStreams.getId());
        }
        if (entityId != null) {
            sqlQuery.where(qDataStreams.getId().eq(entityId));
        }
    }

    private void queryMultiDatastreams(J entityId, TableRef last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractQMultiDatastreams<? extends AbstractQMultiDatastreams, I, J> qMultiDataStreams = qCollection.qMultiDatastreams.newWithAlias(alias);
        boolean added = true;
        if (last.getType() == null) {
            sqlQuery.select(propertyResolver.getExpressions(qMultiDataStreams, selectedProperties));
            sqlQuery.from(qMultiDataStreams);
        } else {
            switch (last.getType()) {
                case THING:
                    AbstractQThings<?, I, J> qThings = (AbstractQThings<?, I, J>) last.getqPath();
                    sqlQuery.innerJoin(qMultiDataStreams).on(qMultiDataStreams.getThingId().eq(qThings.getId()));
                    needsDistinct = true;
                    break;

                case OBSERVATION:
                    AbstractQObservations<?, I, J> qObservations = (AbstractQObservations<?, I, J>) last.getqPath();
                    sqlQuery.innerJoin(qMultiDataStreams).on(qMultiDataStreams.getId().eq(qObservations.getMultiDatastreamId()));
                    break;

                case SENSOR:
                    AbstractQSensors<?, I, J> qSensors = (AbstractQSensors<?, I, J>) last.getqPath();
                    sqlQuery.innerJoin(qMultiDataStreams).on(qMultiDataStreams.getSensorId().eq(qSensors.getId()));
                    needsDistinct = true;
                    break;

                case OBSERVEDPROPERTY:
                    AbstractQObsProperties<?, I, J> qObsProperties = (AbstractQObsProperties<?, I, J>) last.getqPath();
                    AbstractQMultiDatastreamsObsProperties<?, I, J> qMdOp = qCollection.qMultiDatastreamsObsProperties.newWithAlias(alias + "j1");
                    sqlQuery.innerJoin(qMdOp).on(qObsProperties.getId().eq(qMdOp.getObsPropertyId()));
                    sqlQuery.innerJoin(qMultiDataStreams).on(qMultiDataStreams.getId().eq(qMdOp.getMultiDatastreamId()));
                    if (!isFilter) {
                        sqlQuery.orderBy(qMdOp.rank.asc());
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
            last.setqPath(qMultiDataStreams);
            last.setIdPath(qMultiDataStreams.getId());
        }
        if (entityId != null) {
            sqlQuery.where(qMultiDataStreams.getId().eq(entityId));
        }
    }

    private void queryThings(J entityId, TableRef last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractQThings<?, I, J> qThings = qCollection.qThings.newWithAlias(alias);
        boolean added = true;
        if (last.getType() == null) {
            sqlQuery.select(propertyResolver.getExpressions(qThings, selectedProperties));
            sqlQuery.from(qThings);
        } else {
            switch (last.getType()) {
                case DATASTREAM:
                    AbstractQDatastreams<?, I, J> qDatastreams = (AbstractQDatastreams<?, I, J>) last.getqPath();
                    sqlQuery.innerJoin(qThings).on(qThings.getId().eq(qDatastreams.getThingId()));
                    break;

                case MULTIDATASTREAM:
                    AbstractQMultiDatastreams<?, I, J> qMultiDatastreams = (AbstractQMultiDatastreams<?, I, J>) last.getqPath();
                    sqlQuery.innerJoin(qThings).on(qThings.getId().eq(qMultiDatastreams.getThingId()));
                    break;

                case HISTORICALLOCATION:
                    AbstractQHistLocations<?, I, J> qHistLocations = (AbstractQHistLocations<?, I, J>) last.getqPath();
                    sqlQuery.innerJoin(qThings).on(qThings.getId().eq(qHistLocations.getThingId()));
                    break;

                case LOCATION:
                    AbstractQLocations<?, I, J> qLocations = (AbstractQLocations<?, I, J>) last.getqPath();
                    AbstractQThingsLocations<?, I, J> qTL = qCollection.qThingsLocations.newWithAlias(alias + "j1");
                    sqlQuery.innerJoin(qTL).on(qLocations.getId().eq(qTL.getLocationId()));
                    sqlQuery.innerJoin(qThings).on(qThings.getId().eq(qTL.getThingId()));
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
            last.setqPath(qThings);
            last.setIdPath(qThings.getId());
        }
        if (entityId != null) {
            sqlQuery.where(qThings.getId().eq(entityId));
        }
    }

    private void queryFeatures(J entityId, TableRef last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractQFeatures<?, I, J> qFeatures = qCollection.qFeatures.newWithAlias(alias);
        boolean added = true;
        if (last.getType() == null) {
            sqlQuery.select(propertyResolver.getExpressions(qFeatures, selectedProperties));
            sqlQuery.from(qFeatures);
        } else {
            switch (last.getType()) {
                case OBSERVATION:
                    AbstractQObservations<?, I, J> qObservations = (AbstractQObservations<?, I, J>) last.getqPath();
                    sqlQuery.innerJoin(qFeatures).on(qFeatures.getId().eq(qObservations.getFeatureId()));
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
            last.setqPath(qFeatures);
            last.setIdPath(qFeatures.getId());
        }
        if (entityId != null) {
            sqlQuery.where(qFeatures.getId().eq(entityId));
        }
    }

    private void queryHistLocations(J entityId, TableRef last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractQHistLocations<?, I, J> qHistLocations = qCollection.qHistLocations.newWithAlias(alias);
        boolean added = true;
        if (last.getType() == null) {
            sqlQuery.select(propertyResolver.getExpressions(qHistLocations, selectedProperties));
            sqlQuery.from(qHistLocations);
        } else {
            switch (last.getType()) {
                case THING:
                    AbstractQThings<?, I, J> qThings = (AbstractQThings<?, I, J>) last.getqPath();
                    sqlQuery.innerJoin(qHistLocations).on(qThings.getId().eq(qHistLocations.getThingId()));
                    needsDistinct = true;
                    break;

                case LOCATION:
                    AbstractQLocations<?, I, J> qLocations = (AbstractQLocations<?, I, J>) last.getqPath();
                    AbstractQLocationsHistLocations<?, I, J> qLHL = qCollection.qLocationsHistLocations.newWithAlias(alias + "j1");
                    sqlQuery.innerJoin(qLHL).on(qLocations.getId().eq(qLHL.getLocationId()));
                    sqlQuery.innerJoin(qHistLocations).on(qHistLocations.getId().eq(qLHL.getHistLocationId()));
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
            last.setqPath(qHistLocations);
            last.setIdPath(qHistLocations.getId());
        }
        if (entityId != null) {
            sqlQuery.where(qHistLocations.getId().eq(entityId));
        }
    }

    private void queryLocations(J entityId, TableRef last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractQLocations<?, I, J> qLocations = qCollection.qLocations.newWithAlias(alias);
        boolean added = true;
        if (last.getType() == null) {
            sqlQuery.select(propertyResolver.getExpressions(qLocations, selectedProperties));
            sqlQuery.from(qLocations);
        } else {
            switch (last.getType()) {
                case THING:
                    AbstractQThings<?, I, J> qThings = (AbstractQThings<?, I, J>) last.getqPath();
                    AbstractQThingsLocations<?, I, J> qTL = qCollection.qThingsLocations.newWithAlias(alias + "j1");
                    sqlQuery.innerJoin(qTL).on(qThings.getId().eq(qTL.getThingId()));
                    sqlQuery.innerJoin(qLocations).on(qLocations.getId().eq(qTL.getLocationId()));
                    needsDistinct = true;
                    break;

                case HISTORICALLOCATION:
                    AbstractQHistLocations<?, I, J> qHistLocations = (AbstractQHistLocations<?, I, J>) last.getqPath();
                    AbstractQLocationsHistLocations<?, I, J> qLHL = qCollection.qLocationsHistLocations.newWithAlias(alias + "j1");
                    sqlQuery.innerJoin(qLHL).on(qHistLocations.getId().eq(qLHL.getHistLocationId()));
                    sqlQuery.innerJoin(qLocations).on(qLocations.getId().eq(qLHL.getLocationId()));
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
            last.setqPath(qLocations);
            last.setIdPath(qLocations.getId());
        }
        if (entityId != null) {
            sqlQuery.where(qLocations.getId().eq(entityId));
        }
    }

    private void querySensors(J entityId, TableRef last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractQSensors<? extends AbstractQSensors, I, J> qSensors = qCollection.qSensors.newWithAlias(alias);
        boolean added = true;
        if (last.getType() == null) {
            sqlQuery.select(propertyResolver.getExpressions(qSensors, selectedProperties));
            sqlQuery.from(qSensors);
        } else {
            switch (last.getType()) {
                case DATASTREAM:
                    AbstractQDatastreams<?, I, J> qDatastreams = (AbstractQDatastreams<?, I, J>) last.getqPath();
                    sqlQuery.innerJoin(qSensors).on(qSensors.getId().eq(qDatastreams.getSensorId()));
                    break;

                case MULTIDATASTREAM:
                    AbstractQMultiDatastreams<?, I, J> qMultiDatastreams = (AbstractQMultiDatastreams<?, I, J>) last.getqPath();
                    sqlQuery.innerJoin(qSensors).on(qSensors.getId().eq(qMultiDatastreams.getSensorId()));
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
            last.setqPath(qSensors);
            last.setIdPath(qSensors.getId());
        }
        if (entityId != null) {
            sqlQuery.where(qSensors.getId().eq(entityId));
        }
    }

    private void queryObservations(J entityId, TableRef last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractQObservations<? extends AbstractQObservations, I, J> qObservations = qCollection.qObservations.newWithAlias(alias);
        boolean added = true;
        if (last.getType() == null) {
            sqlQuery.select(propertyResolver.getExpressions(qObservations, selectedProperties));
            sqlQuery.from(qObservations);
        } else {
            switch (last.getType()) {
                case FEATUREOFINTEREST:
                    AbstractQFeatures<?, I, J> qFeatures = (AbstractQFeatures<?, I, J>) last.getqPath();
                    sqlQuery.innerJoin(qObservations).on(qFeatures.getId().eq(qObservations.getFeatureId()));
                    needsDistinct = true;
                    break;

                case DATASTREAM:
                    AbstractQDatastreams<?, I, J> qDatastreams = (AbstractQDatastreams<?, I, J>) last.getqPath();
                    sqlQuery.innerJoin(qObservations).on(qDatastreams.getId().eq(qObservations.getDatastreamId()));
                    needsDistinct = true;
                    break;

                case MULTIDATASTREAM:
                    AbstractQMultiDatastreams<?, I, J> qMultiDatastreams = (AbstractQMultiDatastreams<?, I, J>) last.getqPath();
                    sqlQuery.innerJoin(qObservations).on(qMultiDatastreams.getId().eq(qObservations.getMultiDatastreamId()));
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
            last.setqPath(qObservations);
            last.setIdPath(qObservations.getId());
        }
        if (entityId != null) {
            sqlQuery.where(qObservations.getId().eq(entityId));
        }
    }

    private void queryObsProperties(J entityId, TableRef last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        AbstractQObsProperties<?, I, J> qObsProperties = qCollection.qObsProperties.newWithAlias(alias);
        boolean added = true;
        if (last.getType() == null) {
            sqlQuery.select(propertyResolver.getExpressions(qObsProperties, selectedProperties));
            sqlQuery.from(qObsProperties);
        } else {
            switch (last.getType()) {
                case MULTIDATASTREAM:
                    AbstractQMultiDatastreams<?, I, J> qMultiDatastreams = (AbstractQMultiDatastreams<?, I, J>) last.getqPath();
                    AbstractQMultiDatastreamsObsProperties<?, I, J> qMdOp = qCollection.qMultiDatastreamsObsProperties.newWithAlias(alias + "j1");
                    sqlQuery.innerJoin(qMdOp).on(qMultiDatastreams.getId().eq(qMdOp.getMultiDatastreamId()));
                    sqlQuery.innerJoin(qObsProperties).on(qObsProperties.getId().eq(qMdOp.getObsPropertyId()));
                    needsDistinct = true;
                    break;

                case DATASTREAM:
                    AbstractQDatastreams<?, I, J> qDatastreams = (AbstractQDatastreams<?, I, J>) last.getqPath();
                    sqlQuery.innerJoin(qObsProperties).on(qObsProperties.getId().eq(qDatastreams.getObsPropertyId()));
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
            last.setqPath(qObsProperties);
            last.setIdPath(qObsProperties.getId());
        }
        if (entityId != null) {
            sqlQuery.where(qObsProperties.getId().eq(entityId));
        }
    }

}
