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
package de.fraunhofer.iosb.ilt.sta.persistence.postgres.stringid;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.RelationalPathBase;
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
import de.fraunhofer.iosb.ilt.sta.persistence.BasicPersistenceType;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.PathSqlBuilder;
import de.fraunhofer.iosb.ilt.sta.persistence.postgres.PgExpressionHandler;
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
 *
 * @author scf
 */
public class PathSqlBuilderString implements PathSqlBuilder {

    public static class TableRefString implements TableRef {

        public EntityType type;
        public RelationalPathBase<?> qPath;
        public StringPath idPath;

        public TableRefString() {
        }

        public TableRefString(TableRefString source) {
            type = source.type;
            qPath = source.qPath;
            idPath = source.idPath;
        }

        @Override
        public EntityType getType() {
            return type;
        }

        @Override
        public RelationalPathBase<?> getqPath() {
            return qPath;
        }

        @Override
        public void clear() {
            type = null;
            qPath = null;
            idPath = null;
        }

        @Override
        public TableRef copy() {
            TableRefString copy = new TableRefString(this);
            return copy;
        }

        @Override
        public boolean isEmpty() {
            return type == null && qPath == null;
        }
    }
    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PathSqlBuilder.class);
    /**
     * The prefix used for table aliases. The main entity is always
     * &lt;PREFIX&gt;1.
     */
    public static final String ALIAS_PREFIX = "e";
    private SQLQuery<Tuple> sqlQuery;
    private Set<Property> selectedProperties;
    private final TableRefString lastPath = new TableRefString();
    private TableRefString mainTable;
    private int aliasNr = 0;
    private boolean isFilter = false;
    private boolean needsDistinct = false;

    @Override
    public synchronized SQLQuery<Tuple> buildFor(EntityType entityType, Id id, SQLQueryFactory sqlQueryFactory, PersistenceSettings settings) {
        selectedProperties = Collections.EMPTY_SET;
        sqlQuery = sqlQueryFactory.select(new Expression<?>[]{});
        lastPath.clear();
        aliasNr = 0;
        queryEntityType(entityType, id, lastPath);
        return sqlQuery;
    }

    @Override
    public synchronized SQLQuery<Tuple> buildFor(ResourcePath path, Query query, SQLQueryFactory sqlQueryFactory, PersistenceSettings settings) {
        selectedProperties = new HashSet<>();
        if (query != null) {
            for (Property property : query.getSelect()) {
                selectedProperties.add(property);
            }
            if (!query.getExpand().isEmpty() && !selectedProperties.isEmpty()) {
                // If we expand, and there is a $select, make sure we load the ID and the navigation properties.
                // If no $select, then we already load everything.
                selectedProperties.add(EntityProperty.Id);
                for (Expand expand : query.getExpand()) {
                    List<NavigationProperty> expandPath = expand.getPath();
                    if (expandPath.size() > 0) {
                        selectedProperties.add(expandPath.get(0));
                    }
                }
            }
        }
        sqlQuery = sqlQueryFactory.select(new Expression<?>[]{});
        lastPath.clear();
        aliasNr = 0;
        List<ResourcePathElement> elements = new ArrayList<>(path.getPathElements());

        int count = elements.size();
        for (int i = count - 1; i >= 0; i--) {
            ResourcePathElement element = elements.get(i);
            element.visit(this);
        }

        if (query != null) {
            boolean distict = false;
            PgExpressionHandler handler = new PgExpressionHandler(this, mainTable.copy());
            for (OrderBy ob : query.getOrderBy()) {
                handler.addOrderbyToQuery(ob, sqlQuery);
            }
            if (needsDistinct) {
                sqlQuery.distinct();
                distict = true;
            }
            isFilter = true;
            needsDistinct = false;
            de.fraunhofer.iosb.ilt.sta.query.expression.Expression filter = query.getFilter();
            if (filter != null) {
                handler.addFilterToQuery(filter, sqlQuery);
            }
            if (settings.getAlwaysOrderbyId()) {
                sqlQuery.orderBy(mainTable.idPath.asc());
            }
            if (needsDistinct && !distict) {
                sqlQuery.distinct();
            }
        }

        return sqlQuery;
    }

    public SQLDeleteClause createDelete(EntitySetPathElement set, SQLQueryFactory sqlQueryFactory, SubQueryExpression idSelect) {
        switch (set.getEntityType()) {
            case Datastream:
                return sqlQueryFactory.delete(QDatastreams.datastreams).where(QDatastreams.datastreams.id.in(idSelect));

            case MultiDatastream:
                return sqlQueryFactory.delete(QMultiDatastreams.multiDatastreams).where(QMultiDatastreams.multiDatastreams.id.in(idSelect));

            case FeatureOfInterest:
                return sqlQueryFactory.delete(QFeatures.features).where(QFeatures.features.id.in(idSelect));

            case HistoricalLocation:
                return sqlQueryFactory.delete(QHistLocations.histLocations).where(QHistLocations.histLocations.id.in(idSelect));

            case Location:
                return sqlQueryFactory.delete(QLocations.locations).where(QLocations.locations.id.in(idSelect));

            case Observation:
                return sqlQueryFactory.delete(QObservations.observations).where(QObservations.observations.id.in(idSelect));

            case ObservedProperty:
                return sqlQueryFactory.delete(QObsProperties.obsProperties).where(QObsProperties.obsProperties.id.in(idSelect));

            case Sensor:
                return sqlQueryFactory.delete(QSensors.sensors).where(QSensors.sensors.id.in(idSelect));

            case Thing:
                return sqlQueryFactory.delete(QThings.things).where(QThings.things.id.in(idSelect));

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
        selectedProperties.add(EntityProperty.Id);
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
    public void queryEntityType(EntityType type, Id targetId, TableRef lastRef) {
        if (!(lastRef instanceof TableRefString)) {
            throw new IllegalArgumentException("This implementation expect a TableRefString");
        }
        TableRefString last = (TableRefString) lastRef;

        String id = null;
        if (targetId != null) {
            if (targetId.getBasicPersistenceType() != BasicPersistenceType.String) {
                throw new IllegalArgumentException("This implementation expects String ids, not " + targetId.getBasicPersistenceType());
            }
            id = (String) targetId.asBasicPersistenceType();
        }

        switch (type) {
            case Datastream:
                queryDatastreams(id, last);
                break;

            case MultiDatastream:
                queryMultiDatastreams(id, last);
                break;

            case FeatureOfInterest:
                queryFeatures(id, last);
                break;

            case HistoricalLocation:
                queryHistLocations(id, last);
                break;

            case Location:
                queryLocations(id, last);
                break;

            case Observation:
                queryObservations(id, last);
                break;

            case ObservedProperty:
                queryObsProperties(id, last);
                break;

            case Sensor:
                querySensors(id, last);
                break;

            case Thing:
                queryThings(id, last);
                break;

            default:
                LOGGER.error("Unknown entity type {}!?", type);
                throw new IllegalStateException("Unknown entity type " + type);
        }
        if (mainTable == null && !last.isEmpty()) {
            mainTable = new TableRefString(last);
        }

    }

    @Override
    public Map<String, Expression<?>> expressionsForProperty(EntityProperty property, Path<?> qPath, Map<String, Expression<?>> target) {
        return PropertyResolver.expressionsForProperty(property, qPath, target);
    }

    private void queryDatastreams(String entityId, TableRefString last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        QDatastreams qDataStreams = new QDatastreams(alias);
        boolean added = true;
        if (last.type == null) {
            sqlQuery.select(PropertyHelper.getExpressions(qDataStreams, selectedProperties));
            sqlQuery.from(qDataStreams);
        } else {
            switch (last.type) {
                case Thing:
                    QThings qThings = (QThings) last.qPath;
                    sqlQuery.innerJoin(qDataStreams).on(qDataStreams.thingId.eq(qThings.id));
                    needsDistinct = true;
                    break;

                case Observation:
                    QObservations qObservations = (QObservations) last.qPath;
                    sqlQuery.innerJoin(qDataStreams).on(qDataStreams.id.eq(qObservations.datastreamId));
                    break;

                case Sensor:
                    QSensors qSensors = (QSensors) last.qPath;
                    sqlQuery.innerJoin(qDataStreams).on(qDataStreams.sensorId.eq(qSensors.id));
                    needsDistinct = true;
                    break;

                case ObservedProperty:
                    QObsProperties qObsProperties = (QObsProperties) last.qPath;
                    sqlQuery.innerJoin(qDataStreams).on(qDataStreams.obsPropertyId.eq(qObsProperties.id));
                    needsDistinct = true;
                    break;

                case Datastream:
                    added = false;
                    break;

                default:
                    LOGGER.error("Do not know how to join {} onto Datastreams.", last.type);
                    throw new IllegalStateException("Do not know how to join");
            }
        }
        if (added) {
            last.type = EntityType.Datastream;
            last.qPath = qDataStreams;
            last.idPath = qDataStreams.id;
        }
        if (entityId != null) {
            sqlQuery.where(qDataStreams.id.eq(entityId));
        }
    }

    private void queryMultiDatastreams(String entityId, TableRefString last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        QMultiDatastreams qMultiDataStreams = new QMultiDatastreams(alias);
        boolean added = true;
        if (last.type == null) {
            sqlQuery.select(PropertyHelper.getExpressions(qMultiDataStreams, selectedProperties));
            sqlQuery.from(qMultiDataStreams);
        } else {
            switch (last.type) {
                case Thing:
                    QThings qThings = (QThings) last.qPath;
                    sqlQuery.innerJoin(qMultiDataStreams).on(qMultiDataStreams.thingId.eq(qThings.id));
                    needsDistinct = true;
                    break;

                case Observation:
                    QObservations qObservations = (QObservations) last.qPath;
                    sqlQuery.innerJoin(qMultiDataStreams).on(qMultiDataStreams.id.eq(qObservations.multiDatastreamId));
                    break;

                case Sensor:
                    QSensors qSensors = (QSensors) last.qPath;
                    sqlQuery.innerJoin(qMultiDataStreams).on(qMultiDataStreams.sensorId.eq(qSensors.id));
                    needsDistinct = true;
                    break;

                case ObservedProperty:
                    QObsProperties qObsProperties = (QObsProperties) last.qPath;
                    QMultiDatastreamsObsProperties qMdOp = new QMultiDatastreamsObsProperties(alias + "j1");
                    sqlQuery.innerJoin(qMdOp).on(qObsProperties.id.eq(qMdOp.obsPropertyId));
                    sqlQuery.innerJoin(qMultiDataStreams).on(qMultiDataStreams.id.eq(qMdOp.multiDatastreamId));
                    if (!isFilter) {
                        sqlQuery.orderBy(qMdOp.rank.asc());
                    } else {
                        needsDistinct = true;
                    }
                    break;

                case MultiDatastream:
                    added = false;
                    break;

                default:
                    LOGGER.error("Do not know how to join {} onto Datastreams.", last.type);
                    throw new IllegalStateException("Do not know how to join");
            }
        }
        if (added) {
            last.type = EntityType.MultiDatastream;
            last.qPath = qMultiDataStreams;
            last.idPath = qMultiDataStreams.id;
        }
        if (entityId != null) {
            sqlQuery.where(qMultiDataStreams.id.eq(entityId));
        }
    }

    private void queryThings(String entityId, TableRefString last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        QThings qThings = new QThings(alias);
        boolean added = true;
        if (last.type == null) {
            sqlQuery.select(PropertyHelper.getExpressions(qThings, selectedProperties));
            sqlQuery.from(qThings);
        } else {
            switch (last.type) {
                case Datastream:
                    QDatastreams qDatastreams = (QDatastreams) last.qPath;
                    sqlQuery.innerJoin(qThings).on(qThings.id.eq(qDatastreams.thingId));
                    break;

                case MultiDatastream:
                    QMultiDatastreams qMultiDatastreams = (QMultiDatastreams) last.qPath;
                    sqlQuery.innerJoin(qThings).on(qThings.id.eq(qMultiDatastreams.thingId));
                    break;

                case HistoricalLocation:
                    QHistLocations qHistLocations = (QHistLocations) last.qPath;
                    sqlQuery.innerJoin(qThings).on(qThings.id.eq(qHistLocations.thingId));
                    break;

                case Location:
                    QLocations qLocations = (QLocations) last.qPath;
                    QThingsLocations qTL = new QThingsLocations(alias + "j1");
                    sqlQuery.innerJoin(qTL).on(qLocations.id.eq(qTL.locationId));
                    sqlQuery.innerJoin(qThings).on(qThings.id.eq(qTL.thingId));
                    needsDistinct = true;
                    break;

                case Thing:
                    added = false;
                    break;

                default:
                    LOGGER.error("Do not know how to join {} onto Things.", last.type);
                    throw new IllegalStateException("Do not know how to join");
            }
        }
        if (added) {
            last.type = EntityType.Thing;
            last.qPath = qThings;
            last.idPath = qThings.id;
        }
        if (entityId != null) {
            sqlQuery.where(qThings.id.eq(entityId));
        }
    }

    private void queryFeatures(String entityId, TableRefString last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        QFeatures qFeatures = new QFeatures(alias);
        boolean added = true;
        if (last.type == null) {
            sqlQuery.select(PropertyHelper.getExpressions(qFeatures, selectedProperties));
            sqlQuery.from(qFeatures);
        } else {
            switch (last.type) {
                case Observation:
                    QObservations qObservations = (QObservations) last.qPath;
                    sqlQuery.innerJoin(qFeatures).on(qFeatures.id.eq(qObservations.featureId));
                    break;

                case FeatureOfInterest:
                    added = false;
                    break;

                default:
                    LOGGER.error("Do not know how to join {} onto Features.", last.type);
                    throw new IllegalStateException("Do not know how to join");
            }
        }
        if (added) {
            last.type = EntityType.FeatureOfInterest;
            last.qPath = qFeatures;
            last.idPath = qFeatures.id;
        }
        if (entityId != null) {
            sqlQuery.where(qFeatures.id.eq(entityId));
        }
    }

    private void queryHistLocations(String entityId, TableRefString last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        QHistLocations qHistLocations = new QHistLocations(alias);
        boolean added = true;
        if (last.type == null) {
            sqlQuery.select(PropertyHelper.getExpressions(qHistLocations, selectedProperties));
            sqlQuery.from(qHistLocations);
        } else {
            switch (last.type) {
                case Thing:
                    QThings qThings = (QThings) last.qPath;
                    sqlQuery.innerJoin(qHistLocations).on(qThings.id.eq(qHistLocations.thingId));
                    needsDistinct = true;
                    break;

                case Location:
                    QLocations qLocations = (QLocations) last.qPath;
                    QLocationsHistLocations qLHL = new QLocationsHistLocations(alias + "j1");
                    sqlQuery.innerJoin(qLHL).on(qLocations.id.eq(qLHL.locationId));
                    sqlQuery.innerJoin(qHistLocations).on(qHistLocations.id.eq(qLHL.histLocationId));
                    needsDistinct = true;
                    break;

                case HistoricalLocation:
                    added = false;
                    break;

                default:
                    LOGGER.error("Do not know how to join {} onto HistLocations.", last.type);
                    throw new IllegalStateException("Do not know how to join");
            }
        }
        if (added) {
            last.type = EntityType.HistoricalLocation;
            last.qPath = qHistLocations;
            last.idPath = qHistLocations.id;
        }
        if (entityId != null) {
            sqlQuery.where(qHistLocations.id.eq(entityId));
        }
    }

    private void queryLocations(String entityId, TableRefString last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        QLocations qLocations = new QLocations(alias);
        boolean added = true;
        if (last.type == null) {
            sqlQuery.select(PropertyHelper.getExpressions(qLocations, selectedProperties));
            sqlQuery.from(qLocations);
        } else {
            switch (last.type) {
                case Thing:
                    QThings qThings = (QThings) last.qPath;
                    QThingsLocations qTL = new QThingsLocations(alias + "j1");
                    sqlQuery.innerJoin(qTL).on(qThings.id.eq(qTL.thingId));
                    sqlQuery.innerJoin(qLocations).on(qLocations.id.eq(qTL.locationId));
                    needsDistinct = true;
                    break;

                case HistoricalLocation:
                    QHistLocations qHistLocations = (QHistLocations) last.qPath;
                    QLocationsHistLocations qLHL = new QLocationsHistLocations(alias + "j1");
                    sqlQuery.innerJoin(qLHL).on(qHistLocations.id.eq(qLHL.histLocationId));
                    sqlQuery.innerJoin(qLocations).on(qLocations.id.eq(qLHL.locationId));
                    needsDistinct = true;
                    break;

                case Location:
                    added = false;
                    break;

                default:
                    LOGGER.error("Do not know how to join {} onto Locations.", last.type);
                    throw new IllegalStateException("Do not know how to join");
            }
        }
        if (added) {
            last.type = EntityType.Location;
            last.qPath = qLocations;
            last.idPath = qLocations.id;
        }
        if (entityId != null) {
            sqlQuery.where(qLocations.id.eq(entityId));
        }
    }

    private void querySensors(String entityId, TableRefString last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        QSensors qSensors = new QSensors(alias);
        boolean added = true;
        if (last.type == null) {
            sqlQuery.select(PropertyHelper.getExpressions(qSensors, selectedProperties));
            sqlQuery.from(qSensors);
        } else {
            switch (last.type) {
                case Datastream:
                    QDatastreams qDatastreams = (QDatastreams) last.qPath;
                    sqlQuery.innerJoin(qSensors).on(qSensors.id.eq(qDatastreams.sensorId));
                    break;

                case MultiDatastream:
                    QMultiDatastreams qMultiDatastreams = (QMultiDatastreams) last.qPath;
                    sqlQuery.innerJoin(qSensors).on(qSensors.id.eq(qMultiDatastreams.sensorId));
                    break;

                case Sensor:
                    added = false;
                    break;

                default:
                    LOGGER.error("Do not know how to join {} onto Sensors.", last.type);
                    throw new IllegalStateException("Do not know how to join");
            }
        }
        if (added) {
            last.type = EntityType.Sensor;
            last.qPath = qSensors;
            last.idPath = qSensors.id;
        }
        if (entityId != null) {
            sqlQuery.where(qSensors.id.eq(entityId));
        }
    }

    private void queryObservations(String entityId, TableRefString last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        QObservations qObservations = new QObservations(alias);
        boolean added = true;
        if (last.type == null) {
            sqlQuery.select(PropertyHelper.getExpressions(qObservations, selectedProperties));
            sqlQuery.from(qObservations);
        } else {
            switch (last.type) {
                case FeatureOfInterest:
                    QFeatures qFeatures = (QFeatures) last.qPath;
                    sqlQuery.innerJoin(qObservations).on(qFeatures.id.eq(qObservations.featureId));
                    needsDistinct = true;
                    break;

                case Datastream:
                    QDatastreams qDatastreams = (QDatastreams) last.qPath;
                    sqlQuery.innerJoin(qObservations).on(qDatastreams.id.eq(qObservations.datastreamId));
                    needsDistinct = true;
                    break;

                case MultiDatastream:
                    QMultiDatastreams qMultiDatastreams = (QMultiDatastreams) last.qPath;
                    sqlQuery.innerJoin(qObservations).on(qMultiDatastreams.id.eq(qObservations.multiDatastreamId));
                    needsDistinct = true;
                    break;

                case Observation:
                    added = false;
                    break;

                default:
                    LOGGER.error("Do not know how to join {} onto Observations.", last.type);
                    throw new IllegalStateException("Do not know how to join");
            }
        }
        if (added) {
            last.type = EntityType.Observation;
            last.qPath = qObservations;
            last.idPath = qObservations.id;
        }
        if (entityId != null) {
            sqlQuery.where(qObservations.id.eq(entityId));
        }
    }

    private void queryObsProperties(String entityId, TableRefString last) {
        int nr = ++aliasNr;
        String alias = ALIAS_PREFIX + nr;
        QObsProperties qObsProperties = new QObsProperties(alias);
        boolean added = true;
        if (last.type == null) {
            sqlQuery.select(PropertyHelper.getExpressions(qObsProperties, selectedProperties));
            sqlQuery.from(qObsProperties);
        } else {
            switch (last.type) {
                case MultiDatastream:
                    QMultiDatastreams qMultiDatastreams = (QMultiDatastreams) last.qPath;
                    QMultiDatastreamsObsProperties qMdOp = new QMultiDatastreamsObsProperties(alias + "j1");
                    sqlQuery.innerJoin(qMdOp).on(qMultiDatastreams.id.eq(qMdOp.multiDatastreamId));
                    sqlQuery.innerJoin(qObsProperties).on(qObsProperties.id.eq(qMdOp.obsPropertyId));
                    needsDistinct = true;
                    needsDistinct = true;
                    break;

                case Datastream:
                    QDatastreams qDatastreams = (QDatastreams) last.qPath;
                    sqlQuery.innerJoin(qObsProperties).on(qObsProperties.id.eq(qDatastreams.obsPropertyId));
                    break;
                case ObservedProperty:
                    added = false;
                    break;

                default:
                    LOGGER.error("Do not know how to join {} onto ObsProperties.", last.type);
                    throw new IllegalStateException("Do not know how to join");
            }
        }
        if (added) {
            last.type = EntityType.ObservedProperty;
            last.qPath = qObsProperties;
            last.idPath = qObsProperties.id;
        }
        if (entityId != null) {
            sqlQuery.where(qObsProperties.id.eq(entityId));
        }
    }

}
