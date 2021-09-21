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

import com.fasterxml.jackson.databind.node.ArrayNode;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityType;
import de.fraunhofer.iosb.ilt.frostserver.model.core.Entity;
import de.fraunhofer.iosb.ilt.frostserver.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.frostserver.model.core.NavigableElement;
import de.fraunhofer.iosb.ilt.frostserver.path.CustomLinksHelper;
import de.fraunhofer.iosb.ilt.frostserver.path.PathElement;
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
 * @param <J> The type of the EP_ID fields.
 */
public class ResultBuilder<J extends Comparable> implements ResourcePathVisitor {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ResultBuilder.class);
    private final PostgresPersistenceManager<J> pm;
    private final PersistenceSettings persistenceSettings;
    private final ResourcePath path;
    private final Query staQuery;
    private final QueryBuilder<J> sqlQueryBuilder;
    private final ResultQuery<Record> sqlQuery;
    private final CustomLinksHelper customLinksHelper;
    private final DataSize dataSize;

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
     * generating select and count queries.
     */
    public ResultBuilder(PostgresPersistenceManager<J> pm, ResourcePath path, Query query, QueryBuilder<J> sqlQueryBuilder, DataSize dataSize) {
        this.pm = pm;
        this.path = path;
        this.staQuery = query;
        this.sqlQueryBuilder = sqlQueryBuilder;
        this.sqlQuery = sqlQueryBuilder.buildSelect();
        this.dataSize = dataSize;
        final CoreSettings coreSettings = pm.getCoreSettings();
        this.persistenceSettings = coreSettings.getPersistenceSettings();
        this.customLinksHelper = coreSettings.getCustomLinksHelper();
    }

    public Object getEntity() {
        return resultObject;
    }

    /**
     * If resultObject is a property or sub-property, and we are not using
     * $value, then the resultObject is encapsulated in a Map, using this key.
     *
     * @return The entitiyName of the resultObject in the map.
     */
    public String getEntityName() {
        return entityName;
    }

    public Query getStaQuery() {
        return staQuery;
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

        QueryState<J, ?> queryState = sqlQueryBuilder.getQueryState();
        Entity entity = queryState.entityFromQuery(results.get(0), new DataSize(pm.getCoreSettings().getDataSizeMax()));

        if (entity == null) {
            throw new IllegalStateException("Failed to create an entity from result set.");
        }
        entity.setQuery(staQuery);
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
        if (o instanceof NavigableElement) {
            existing = (NavigableElement) o;
        } else if (firstNp instanceof NavigationPropertyCustom) {
            NavigationPropertyCustom firstNpCust = (NavigationPropertyCustom) firstNp;
            Object id = firstNpCust.getTargetIdFrom(entity);
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
        } else if (existing instanceof EntitySet) {
            expandEntitySet((EntitySet) existing, subQuery);
        } else if (existing instanceof Entity) {
            expandEntity((Entity) existing, subQuery);
        }
    }

    private Entity loadEntity(EntityType type, Object id, Expand expand) {
        try {
            return pm.get(type, pm.getIdManager().fromObject(id), expand.getSubQuery());
        } catch (IllegalArgumentException ex) {
            // not a valid id.
        }
        return null;
    }

    private void createExpandedElement(Entity entity, NavigationProperty firstNp, Query subQuery) {
        PathElement parentCollection = new PathElementEntitySet(entity.getEntityType());
        PathElement parent = new PathElementEntity(entity.getId(), entity.getEntityType(), parentCollection);
        ResourcePath ePath = new ResourcePath(path.getServiceRootUrl(), path.getVersion(), null);
        ePath.addPathElement(parentCollection, false, false);
        ePath.addPathElement(parent, false, true);

        if (firstNp.isEntitySet()) {
            PathElementEntitySet childPe = new PathElementEntitySet((NavigationPropertyEntitySet) firstNp, parent);
            ePath.addPathElement(childPe, true, false);
        } else {
            PathElementEntity childPe;
            if (firstNp instanceof NavigationPropertyEntity) {
                childPe = new PathElementEntity((NavigationPropertyEntity) firstNp, parent);
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
            if (subEntity instanceof Entity) {
                expandEntity((Entity) subEntity, subQuery);
            }
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

    @Override
    public void visit(PathElementEntitySet element) {
        Cursor<Record> results = timeQuery(sqlQuery);
        EntitySet entitySet = sqlQueryBuilder
                .getQueryState()
                .createSetFromRecords(results, this);

        if (entitySet == null) {
            throw new IllegalStateException("Empty set!");
        }

        fetchAndAddCount(entitySet);

        resultObject = entitySet;
    }

    private void fetchAndAddCount(EntitySet entitySet) {
        if (staQuery.isCountOrDefault()) {
            ResultQuery<Record1<Integer>> countQuery = sqlQueryBuilder.buildCount();
            try (Cursor<Record1<Integer>> countCursor = timeQuery(countQuery)) {
                Integer count = countCursor
                        .fetchNext()
                        .component1();
                entitySet.setCount(count);
            }
        }
    }

    @Override
    public void visit(PathElementProperty element) {
        element.getParent().visit(this);
        if (Entity.class.isAssignableFrom(resultObject.getClass())) {
            Object propertyValue = ((Entity) resultObject).getProperty(element.getProperty());
            Map<String, Object> entityMap = new HashMap<>();
            entityName = element.getProperty().name;
            entityMap.put(entityName, propertyValue);
            resultObject = entityMap;
        }
    }

    @Override
    public void visit(PathElementCustomProperty element) {
        element.getParent().visit(this);
        String name = element.getName();
        if (resultObject instanceof Map) {
            Map map = (Map) resultObject;
            Object inner = map.get(entityName);
            if (inner instanceof Map) {
                map = (Map) inner;
                if (map.containsKey(name)) {
                    Object propertyValue = map.get(name);
                    Map<String, Object> entityMap = new HashMap<>();
                    entityName = name;
                    entityMap.put(entityName, propertyValue);
                    resultObject = entityMap;
                    return;
                }
            }
        }

        resultObject = null;
        entityName = null;
    }

    @Override
    public void visit(PathElementArrayIndex element) {
        element.getParent().visit(this);
        int index = element.getIndex();
        if (resultObject instanceof Map) {
            Map map = (Map) resultObject;
            Object inner = map.get(entityName);
            Object propertyValue = null;
            if (inner instanceof ArrayNode && ((ArrayNode) inner).size() > index) {
                propertyValue = ((ArrayNode) inner).get(index);
            }
            if (inner instanceof List && ((List) inner).size() > index) {
                propertyValue = ((List) inner).get(index);
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
