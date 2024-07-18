/*
 * Copyright (C) 2024 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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

import com.fasterxml.jackson.databind.node.ArrayNode;
import de.fraunhofer.iosb.ilt.frostserver.model.ComplexValue;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostserver.model.core.NavigableElement;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PkValue;
import de.fraunhofer.iosb.ilt.frostserver.path.CustomLinksHelper;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementArrayIndex;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementCustomProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntity;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElementProperty;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePath;
import de.fraunhofer.iosb.ilt.frostserver.path.ResourcePathVisitor;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.DataSize;
import de.fraunhofer.iosb.ilt.frostserver.persistence.pgjooq.utils.QueryState;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyCustom;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostserver.property.NavigationPropertyMain.NavigationPropertyEntitySet;
import de.fraunhofer.iosb.ilt.frostserver.query.Expand;
import de.fraunhofer.iosb.ilt.frostserver.query.Metadata;
import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.PersistenceSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.PersistenceSettings.CountMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jooq.Cursor;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.ResultQuery;
import org.jooq.conf.ParamType;
import org.jooq.exception.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Turns the sqlQuery into the model instances to be returned to the client.
 *
 * @author scf
 */
public class ResultBuilder implements ResourcePathVisitor {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ResultBuilder.class);
    private static final String ESTIMATE_COUNT = "Estimate: {}, Count: {}";

    private final JooqPersistenceManager pm;
    private final PersistenceSettings persistenceSettings;
    private final ResourcePath path;
    private final Query staQuery;
    private final QueryBuilder sqlQueryBuilder;
    private final ResultQuery<Record> sqlQuery;
    private final CustomLinksHelper customLinksHelper;
    private final DataSize dataSize;
    private final int estimateTreshold;
    private final CountMode countMode;

    private Object resultObject;
    /**
     * If resultObject is a property or sub-property, and we are not using
     * $value, then the resultObject is encapsulated in a Map, using this key.
     */
    private String entityName;

    /**
     *
     * @param pm The persistence manager.
     * @param path The path leading to the items.
     * @param query The query parameters to use when fetching expanded items.
     * @param sqlQueryBuilder The configured sql query builder to use for
     * @param dataSize The DataSize instance to add size information to.
     * generating select and count queries.
     */
    public ResultBuilder(JooqPersistenceManager pm, ResourcePath path, Query query, QueryBuilder sqlQueryBuilder, DataSize dataSize) {
        this.pm = pm;
        this.path = path;
        this.staQuery = query;
        this.sqlQueryBuilder = sqlQueryBuilder;
        this.sqlQuery = sqlQueryBuilder.buildSelect();
        this.dataSize = dataSize;
        final CoreSettings coreSettings = pm.getCoreSettings();
        this.persistenceSettings = coreSettings.getPersistenceSettings();
        this.customLinksHelper = coreSettings.getCustomLinksHelper();
        countMode = persistenceSettings.getCountMode();
        estimateTreshold = persistenceSettings.getEstimateCountThreshold();
    }

    public Object getEntity() {
        return resultObject;
    }

    /**
     * If resultObject is a property or sub-property, and we are not using
     * $value, then the resultObject is encapsulated in a Map, using this key.
     *
     * @return The entityName of the resultObject in the map.
     */
    public String getEntityName() {
        return entityName;
    }

    public Query getStaQuery() {
        return staQuery;
    }

    public ResourcePath getPath() {
        return path;
    }

    public DataSize getDataSize() {
        return dataSize;
    }

    @Override
    public void visit(PathElementEntity element) {
        Result<Record> results = sqlQuery.fetch();
        if (results.size() > 1) {
            throw new IllegalStateException("Expecting an element, yet more than 1 result. Got " + results.size() + " results.");
        }
        if (results.isEmpty()) {
            return;
        }

        QueryState<?> queryState = sqlQueryBuilder.getQueryState();
        Entity entity = queryState.entityFromRecord(
                results.get(0),
                new DataSize(pm.getCoreSettings().getDataSizeMax()),
                staQuery);

        if (entity == null) {
            throw new IllegalStateException("Failed to create an entity from result set.");
        }
        expandEntity(entity, staQuery);
        resultObject = entity;
    }

    public void expandEntity(Entity entity, Query query) {
        if (query == null) {
            return;
        }
        if (query.getMetadata() == Metadata.FULL) {
            customLinksHelper.expandCustomLinks(query, entity, path);
        }
        for (Expand expand : query.getExpand()) {
            addExpandToEntity(entity, expand);
        }
    }

    private void addExpandToEntity(Entity entity, Expand expand) {
        NavigationProperty firstNp = expand.getPath();
        NavigableElement existing = null;
        Object o = entity.getProperty(firstNp);
        if (o instanceof NavigableElement navigableElement) {
            existing = navigableElement;
        } else if (firstNp instanceof NavigationPropertyCustom firstNpCust) {
            PkValue id = firstNpCust.getTargetIdFrom(entity);
            if (id == null) {
                return;
            }
            existing = loadEntity(firstNp.getEntityType(), id, expand);
            if (existing == null) {
                return;
            }
            firstNpCust.setElementOn(entity, existing);
        }

        Query subQuery = expand.getSubQuery();
        if (existing == null || existing.isEmpty()) {
            createExpandedElement(entity, firstNp, subQuery);
        } else if (existing instanceof EntitySet subEntitySet) {
            expandEntitySet(subEntitySet, subQuery);
        } else if (existing instanceof Entity subEntity) {
            expandEntity(subEntity, subQuery);
        }
    }

    private Entity loadEntity(EntityType type, PkValue pkValues, Expand expand) {
        try {
            return pm.get(type, pkValues, expand.getSubQuery());
        } catch (IllegalArgumentException ex) {
            // not a valid id.
        }
        return null;
    }

    private void createExpandedElement(Entity entity, NavigationProperty firstNp, Query subQuery) {
        PathElementEntitySet parentCollection = new PathElementEntitySet(entity.getEntityType());
        PathElementEntity parent = new PathElementEntity(entity.getPrimaryKeyValues(), entity.getEntityType(), parentCollection);
        ResourcePath ePath = new ResourcePath(path.getServiceRootUrl(), path.getVersion(), null);
        ePath.addPathElement(parentCollection, false, false);
        ePath.addPathElement(parent, false, true);

        if (firstNp.isEntitySet()) {
            PathElementEntitySet childPe = new PathElementEntitySet((NavigationPropertyEntitySet) firstNp, parent);
            ePath.addPathElement(childPe, true, false);
        } else {
            PathElementEntity childPe;
            if (firstNp instanceof NavigationPropertyEntity npe) {
                childPe = new PathElementEntity(npe, parent);
            } else {
                childPe = new PathElementEntity(firstNp.getEntityType(), parent);
            }
            ePath.addPathElement(childPe, true, false);
        }
        Object child = pm.get(ePath, subQuery);
        entity.setProperty(firstNp, child);
    }

    private void expandEntitySet(EntitySet entitySet, Query subQuery) {
        for (Entity subEntity : entitySet) {
            expandEntity(subEntity, subQuery);
        }
    }

    private <R extends Record> Cursor<R> timeQuery(ResultQuery<R> query) {
        if (persistenceSettings.isTimeoutQueries()) {
            query.queryTimeout(persistenceSettings.getQueryTimeout());
        }
        if (!persistenceSettings.isLogSlowQueries()) {
            return query.fetchLazy();
        }
        long start = System.currentTimeMillis();
        Cursor<R> result;
        try {
            result = query.fetchLazy();
        } catch (DataAccessException exc) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.info("Failed to run query:\n{}", query.getSQL(ParamType.INLINED));
            }
            throw new IllegalStateException("Failed to run query: " + exc.getMessage());
        }
        long end = System.currentTimeMillis();
        long duration = end - start;
        if (LOGGER.isInfoEnabled() && duration > persistenceSettings.getSlowQueryThreshold()) {
            LOGGER.info("Slow Query executed in {} ms:\n{}", duration, query.getSQL(ParamType.INLINED));
        }
        return result;
    }

    private int timeCountQueryRecord(ResultQuery<Record> query) {
        try (Cursor<Record> countCursor = timeQuery(query)) {
            return countCursor
                    .fetchNext()
                    .get(0, Integer.class);
        }
    }

    private int timeCountQuery(ResultQuery<Record1<Integer>> query) {
        try (Cursor<Record1<Integer>> countCursor = timeQuery(query)) {
            return countCursor
                    .fetchNext()
                    .component1();
        }
    }

    @Override
    public void visit(PathElementEntitySet element) {
        final EntitySet entitySet;
        if (staQuery.getTopOrDefault() > 0) {
            final Cursor<Record> results = timeQuery(sqlQuery);
            entitySet = sqlQueryBuilder
                    .getQueryState()
                    .createSetFromRecords(results, this);
        } else {
            entitySet = new EntitySetImpl(sqlQueryBuilder.getQueryState().getMainTable().getEntityType());
        }

        if (entitySet == null) {
            throw new IllegalStateException("Empty set!");
        }

        fetchAndAddCount(entitySet);

        resultObject = entitySet;
    }

    private void fetchAndAddCount(EntitySet entitySet) {
        if (LOGGER.isTraceEnabled()) {
            final int estimate = timeCountQuery(sqlQueryBuilder.buildEstimateCountExplain());
            final int sample = timeCountQueryRecord(sqlQueryBuilder.buildEstimateCountSample().countQuery) * 100;
            final int limit = timeCountQuery(sqlQueryBuilder.buildCount(estimateTreshold));
            final int full = timeCountQuery(sqlQueryBuilder.buildCount());
            LOGGER.trace("Estimate: {}, Sample: {}, Limit: {}, Full: {}", estimate, sample, limit, full);
        }
        if (staQuery.isCountOrDefault()) {
            switch (countMode) {
                case FULL:
                    entitySet.setCount(timeCountQuery(sqlQueryBuilder.buildCount()));
                    return;

                case LIMIT_ESTIMATE:
                    countLimitEstimate(entitySet);
                    return;

                case ESTIMATE_LIMIT:
                    countEstimateLimit(entitySet);
                    return;

                case LIMIT_SAMPLE:
                    countLimitSample(entitySet);
                    return;

                case SAMPLE_LIMIT:
                    countSampleLimit(entitySet);
                    return;

                default:
                    throw new AssertionError(countMode.name());

            }
        }
    }

    public void countSampleLimit(EntitySet entitySet) {
        final var csr = sqlQueryBuilder.buildEstimateCountSample();
        final int estimate = (int) (timeCountQueryRecord(csr.countQuery) * Math.pow(100, csr.sampledTables));
        if (estimate < estimateTreshold) {
            final int count = timeCountQuery(sqlQueryBuilder.buildCount());
            entitySet.setCount(count);
            LOGGER.debug(ESTIMATE_COUNT, estimate, count);
        } else {
            entitySet.setCount(estimate);
        }
    }

    public void countLimitSample(EntitySet entitySet) {
        final int count = timeCountQuery(sqlQueryBuilder.buildCount(estimateTreshold));
        if (count < estimateTreshold) {
            entitySet.setCount(count);
        } else {
            final var csr = sqlQueryBuilder.buildEstimateCountSample();
            final int estimate = (int) (timeCountQueryRecord(csr.countQuery) * Math.pow(100, csr.sampledTables));
            entitySet.setCount(Math.max(count, estimate));
            LOGGER.debug(ESTIMATE_COUNT, estimate, count);
        }
    }

    public void countEstimateLimit(EntitySet entitySet) {
        final int estimate = timeCountQuery(sqlQueryBuilder.buildEstimateCountExplain());
        if (estimate < estimateTreshold) {
            final int count = timeCountQuery(sqlQueryBuilder.buildCount(estimateTreshold));
            entitySet.setCount(count);
            LOGGER.debug(ESTIMATE_COUNT, estimate, count);
        } else {
            entitySet.setCount(estimate);
        }
    }

    public void countLimitEstimate(EntitySet entitySet) {
        final int count = timeCountQuery(sqlQueryBuilder.buildCount(estimateTreshold));
        if (count < estimateTreshold) {
            entitySet.setCount(count);
        } else {
            final int estimate = timeCountQuery(sqlQueryBuilder.buildEstimateCountExplain());
            entitySet.setCount(Math.max(count, estimate));
            LOGGER.debug(ESTIMATE_COUNT, estimate, count);
        }
    }

    @Override
    public void visit(PathElementProperty element) {
        element.getParent().visit(this);
        if (Entity.class.isAssignableFrom(resultObject.getClass())) {
            Object propertyValue = ((Entity) resultObject).getProperty(element.getProperty());
            Map<String, Object> entityMap = new HashMap<>();
            entityName = element.getProperty().getName();
            entityMap.put(entityName, propertyValue);
            resultObject = entityMap;
        }
    }

    @Override
    public void visit(PathElementCustomProperty element) {
        element.getParent().visit(this);
        String name = element.getName();
        if (resultObject instanceof Map map) {
            Object inner = map.get(entityName);
            if (inner instanceof Map innerMap) {
                map = innerMap;
                if (map.containsKey(name)) {
                    Object propertyValue = map.get(name);
                    Map<String, Object> entityMap = new HashMap<>();
                    entityName = name;
                    entityMap.put(entityName, propertyValue);
                    resultObject = entityMap;
                    return;
                }
            } else if (inner instanceof ComplexValue cv) {
                Object propertyValue = cv.getProperty(name);
                Map<String, Object> entityMap = new HashMap<>();
                entityName = name;
                entityMap.put(entityName, propertyValue);
                resultObject = entityMap;
                return;
            }
        }

        resultObject = null;
        entityName = null;
    }

    @Override
    public void visit(PathElementArrayIndex element) {
        element.getParent().visit(this);
        int index = element.getIndex();
        if (resultObject instanceof Map map) {
            Object inner = map.get(entityName);
            Object propertyValue = null;
            if (inner instanceof ArrayNode innerArray && innerArray.size() > index) {
                propertyValue = innerArray.get(index);
            }
            if (inner instanceof List innerList && innerList.size() > index) {
                propertyValue = innerList.get(index);
            }
            if (propertyValue != null) {
                Map<String, Object> entityMap = new HashMap<>();
                entityName = entityName + "[" + Integer.toString(index) + "]";
                entityMap.put(entityName, propertyValue);
                resultObject = entityMap;
                return;
            }
        }

        resultObject = null;
        entityName = null;
    }

}
