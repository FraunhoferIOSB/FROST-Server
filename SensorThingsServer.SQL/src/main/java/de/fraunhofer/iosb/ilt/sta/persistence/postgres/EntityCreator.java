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
import com.querydsl.sql.SQLQuery;
import de.fraunhofer.iosb.ilt.sta.model.core.Entity;
import de.fraunhofer.iosb.ilt.sta.model.core.EntitySet;
import de.fraunhofer.iosb.ilt.sta.model.core.NavigableElement;
import de.fraunhofer.iosb.ilt.sta.path.CustomPropertyPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntityPathElement;
import de.fraunhofer.iosb.ilt.sta.path.EntitySetPathElement;
import de.fraunhofer.iosb.ilt.sta.path.NavigationProperty;
import de.fraunhofer.iosb.ilt.sta.path.PropertyPathElement;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePath;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePathElement;
import de.fraunhofer.iosb.ilt.sta.path.ResourcePathVisitor;
import de.fraunhofer.iosb.ilt.sta.persistence.PersistenceManager;
import de.fraunhofer.iosb.ilt.sta.persistence.QDatastreams;
import de.fraunhofer.iosb.ilt.sta.persistence.QFeatures;
import de.fraunhofer.iosb.ilt.sta.persistence.QHistLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.QLocations;
import de.fraunhofer.iosb.ilt.sta.persistence.QObsProperties;
import de.fraunhofer.iosb.ilt.sta.persistence.QObservations;
import de.fraunhofer.iosb.ilt.sta.persistence.QSensors;
import de.fraunhofer.iosb.ilt.sta.persistence.QThings;
import de.fraunhofer.iosb.ilt.sta.query.Expand;
import de.fraunhofer.iosb.ilt.sta.query.Query;
import de.fraunhofer.iosb.ilt.sta.util.UrlHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author scf
 */
class EntityCreator implements ResourcePathVisitor {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityCreator.class);
    private final PersistenceManager pm;
    private final ResourcePath path;
    private final Query query;
    private final SQLQuery<Tuple> sqlQuery;
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
     * @param sqlQuery The sql query to use for fetching items.
     */
    public EntityCreator(PersistenceManager pm, ResourcePath path, Query query, SQLQuery<Tuple> sqlQuery) {
        this.pm = pm;
        this.path = path;
        this.query = query;
        this.sqlQuery = sqlQuery;
    }

    public Object getEntity() {
        return resultObject;
    }

    /**
     * If resultObject is a property or sub-property, and we are not using
     * $value, then the resultObject is encapsulated in a Map, using this key.
     *
     * @return The name of the resultObject in the map.
     */
    public String getEntityName() {
        return entityName;
    }

    @Override
    public void visit(EntityPathElement element) {
        List<Tuple> results = sqlQuery.fetch();
        if (results.size() > 1) {
            throw new IllegalStateException("Expecting an element, yet more than 1 result. Got " + results.size() + " results.");
        }
        if (results.isEmpty()) {
            return;
        }
        Entity entity = null;
        switch (element.getEntityType()) {
            case Datastream:
                entity = PropertyHelper.createDatastreamFromTuple(results.get(0), new QDatastreams(PathSqlBuilder.ALIAS_PREFIX + "1"));
                break;

            case FeatureOfInterest:
                entity = PropertyHelper.createFeatureOfInterestFromTuple(results.get(0), new QFeatures(PathSqlBuilder.ALIAS_PREFIX + "1"));
                break;

            case HistoricalLocation:
                entity = PropertyHelper.createHistoricalLocationFromTuple(results.get(0), new QHistLocations(PathSqlBuilder.ALIAS_PREFIX + "1"));
                break;

            case Location:
                entity = PropertyHelper.createLocationFromTuple(results.get(0), new QLocations(PathSqlBuilder.ALIAS_PREFIX + "1"));
                break;

            case Observation:
                entity = PropertyHelper.createObservationFromTuple(results.get(0), new QObservations(PathSqlBuilder.ALIAS_PREFIX + "1"));
                break;

            case ObservedProperty:
                entity = PropertyHelper.createObservedPropertyFromTuple(results.get(0), new QObsProperties(PathSqlBuilder.ALIAS_PREFIX + "1"));
                break;

            case Sensor:
                entity = PropertyHelper.createSensorFromTuple(results.get(0), new QSensors(PathSqlBuilder.ALIAS_PREFIX + "1"));
                break;

            case Thing:
                entity = PropertyHelper.createThingFromTuple(results.get(0), new QThings(PathSqlBuilder.ALIAS_PREFIX + "1"));
                break;

            default:
                throw new AssertionError(element.getEntityType().name());
        }
        if (entity == null) {
            throw new IllegalStateException("Failed to create an entity from result set.");
        }
        if (entity.getId() != null) {
            entity.setSelfLink(UrlHelper.generateSelfLink(path, entity));
        }
        expandEntity(entity, query);
        resultObject = entity;
    }

    private void expandEntity(Entity e, Query query) {
        if (query == null) {
            return;
        }
        for (Expand expand : query.getExpand()) {
            ResourcePath ePath = new ResourcePath(path.getServiceRootUrl(), null);
            ResourcePathElement parentCollection = new EntitySetPathElement(e.getEntityType(), null);
            ePath.addPathElement(parentCollection, false, false);
            ResourcePathElement parent = new EntityPathElement(e.getId(), e.getEntityType(), parentCollection);
            ePath.addPathElement(parent, false, true);

            NavigationProperty firstNp = expand.getPath().get(0);
            NavigableElement existing = null;
            {
                Object o = e.getProperty(firstNp);
                if (o instanceof NavigableElement) {
                    existing = (NavigableElement) o;
                }
            }

            if (firstNp.isSet) {
                EntitySetPathElement child = new EntitySetPathElement(firstNp.type, parent);
                ePath.addPathElement(child, true, false);
            } else {
                EntityPathElement child = new EntityPathElement(null, firstNp.type, parent);
                ePath.addPathElement(child, true, false);
            }

            Object child;
            Query subQuery;
            if (expand.getPath().size() == 1) {
                // This was the last element in the expand path. The query is for this element.
                subQuery = expand.getSubQuery();
                if (subQuery == null) {
                    subQuery = new Query(query.getSettings());
                }
            } else {
                // This is not the last element in the expand path. The query is not for this element.
                subQuery = new Query(query.getSettings());
                Expand subExpand = new Expand();
                subExpand.getPath().addAll(expand.getPath());
                subExpand.getPath().remove(0);
                subExpand.setSubQuery(expand.getSubQuery());
                subQuery.addExpand(subExpand);
                if (query.getCount().isPresent()) {
                    subQuery.setCount(query.isCountOrDefault());
                }
            }

            if (existing == null || !existing.isExportObject()) {
                child = pm.get(ePath, subQuery);
                e.setProperty(firstNp, child);
            } else if (existing instanceof EntitySet) {
                EntitySet entitySet = (EntitySet) existing;
                for (Object subEntity : entitySet) {
                    if (subEntity instanceof Entity) {
                        Entity entity = (Entity) subEntity;
                        expandEntity(entity, subQuery);
                    }
                }
            } else if (existing instanceof Entity) {
                Entity entity = (Entity) existing;
                expandEntity(entity, subQuery);
            }
        }
    }

    @Override
    public void visit(EntitySetPathElement element) {

        int top = query.getTopOrDefault();
        sqlQuery.limit(top);

        int skip = 0;
        if (query.getSkip().isPresent()) {
            skip = query.getSkip().get();
            sqlQuery.offset(skip);
        }
        List<Tuple> results = sqlQuery.fetch();

        EntitySet<? extends Entity> entitySet = null;
        switch (element.getEntityType()) {
            case Datastream:
                entitySet = PropertyHelper.createDatastreamsFromTuples(results, new QDatastreams(PathSqlBuilder.ALIAS_PREFIX + "1"));
                break;

            case FeatureOfInterest:
                entitySet = PropertyHelper.createFeaturesOfInterestFromTuples(results, new QFeatures(PathSqlBuilder.ALIAS_PREFIX + "1"));
                break;

            case HistoricalLocation:
                entitySet = PropertyHelper.createHistoricalLocationsFromTuples(results, new QHistLocations(PathSqlBuilder.ALIAS_PREFIX + "1"));
                break;

            case Location:
                entitySet = PropertyHelper.createLocationsFromTuples(results, new QLocations(PathSqlBuilder.ALIAS_PREFIX + "1"));
                break;

            case Observation:
                entitySet = PropertyHelper.createObservationsFromTuples(results, new QObservations(PathSqlBuilder.ALIAS_PREFIX + "1"));
                break;

            case ObservedProperty:
                entitySet = PropertyHelper.createObservedPropertiesFromTuples(results, new QObsProperties(PathSqlBuilder.ALIAS_PREFIX + "1"));
                break;

            case Sensor:
                entitySet = PropertyHelper.createSensorsFromTuples(results, new QSensors(PathSqlBuilder.ALIAS_PREFIX + "1"));
                break;

            case Thing:
                entitySet = PropertyHelper.createThingsFromTuples(results, new QThings(PathSqlBuilder.ALIAS_PREFIX + "1"));
                break;

            default:
                throw new AssertionError(element.getEntityType().name());
        }
        if (entitySet == null) {
            throw new IllegalStateException("Empty set!");
        }

        int count = -1;
        if (query.isCountOrDefault()) {
            count = (int) sqlQuery.fetchCount();
            entitySet.setCount(count);
        }
        if (results.size() == top) {
            if (count == -1) {
                count = (int) sqlQuery.fetchCount();
            }
            if (results.size() + skip < count) {
                entitySet.setNextLink(UrlHelper.generateNextLink(path, query));
            }
        }
        for (Entity e : entitySet) {
            expandEntity(e, query);
        }
        resultObject = entitySet;
    }

    @Override
    public void visit(PropertyPathElement element) {
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
    public void visit(CustomPropertyPathElement element) {
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

}
