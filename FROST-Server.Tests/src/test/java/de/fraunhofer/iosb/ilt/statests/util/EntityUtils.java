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
package de.fraunhofer.iosb.ilt.statests.util;

import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.EP_PROPERTIES;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing.EP_PARAMETERS;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing.EP_RESULTQUALITY;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing.EP_VALIDTIME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.JsonNode;
import de.fraunhofer.iosb.ilt.frostclient.SensorThingsService;
import de.fraunhofer.iosb.ilt.frostclient.dao.Dao;
import de.fraunhofer.iosb.ilt.frostclient.exception.NotFoundException;
import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.exception.StatusCodeException;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.model.EntitySet;
import de.fraunhofer.iosb.ilt.frostclient.model.ModelRegistry;
import de.fraunhofer.iosb.ilt.frostclient.model.PkValue;
import de.fraunhofer.iosb.ilt.frostclient.model.property.EntityPropertyMain;
import de.fraunhofer.iosb.ilt.frostclient.model.property.NavigationPropertyEntity;
import de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.MapValue;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostclient.utils.CollectionsHelper;
import de.fraunhofer.iosb.ilt.statests.StaService;
import de.fraunhofer.iosb.ilt.statests.f01auth.AuthTestHelper;
import de.fraunhofer.iosb.ilt.statests.util.model.EntityType;
import de.fraunhofer.iosb.ilt.statests.util.model.Expand;
import de.fraunhofer.iosb.ilt.statests.util.model.PathElement;
import de.fraunhofer.iosb.ilt.statests.util.model.Query;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for comparing results and cleaning the service.
 *
 * @author Hylke van der Schaaf
 */
public class EntityUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityUtils.class.getName());

    private EntityUtils() {
        // Helper Class.
    }

    /**
     * Class returned by checks on results. Encapsulates the result of the
     * check, and the message.
     */
    public static class ResultTestResult {

        public final boolean testOk;
        public final String message;

        public ResultTestResult(boolean testOk, String message) {
            this.testOk = testOk;
            this.message = message;
        }

    }

    public static ResultTestResult resultContains(EntitySet result, Entity... entities) {
        return resultContains(result, new ArrayList(Arrays.asList(entities)));
    }

    /**
     * Checks if the list contains all the given entities exactly once.
     *
     * @param result the result to check.
     * @param expected the expected entities.
     * @return the result of the comparison.
     */
    public static ResultTestResult resultContains(EntitySet result, List<Entity> expected) {
        long count = result.getCount();
        if (count != -1 && count != expected.size()) {
            LOGGER.info("Result count ({}) not equal to expected count ({})", count, expected.size());
            return new ResultTestResult(false, "Result count " + count + " not equal to expected count (" + expected.size() + ")");
        }
        List<Entity> testExpectedList = new ArrayList<>(expected);
        Iterator<Entity> resultIt;
        for (resultIt = result.iterator(); resultIt.hasNext();) {
            Entity nextResult = resultIt.next();
            Entity inExpectedList = findEntityIn(nextResult, testExpectedList);
            if (!testExpectedList.remove(inExpectedList)) {
                LOGGER.info("Entity with pk {} found in result that is not expected.", nextResult.getPrimaryKeyValues());
                return new ResultTestResult(false, "Entity with pk " + nextResult.getPrimaryKeyValues() + " found in result that is not expected.");
            }
        }
        if (!testExpectedList.isEmpty()) {
            LOGGER.info("Expected entity not found in result.");
            return new ResultTestResult(false, testExpectedList.size() + " expected entities not in result.");
        }
        return new ResultTestResult(true, "Check ok.");
    }

    /**
     * Finds the given entity in the given list, based on its primary key.
     *
     * @param entity The List of Entities to search in.
     * @param entities The entity to find by primary key.
     * @return The first entity from the list that has the correct primary key,
     * or null.
     */
    public static Entity findEntityIn(Entity entity, List<Entity> entities) {
        PkValue pk = entity.getPrimaryKeyValues();
        for (Entity inList : entities) {
            if (inList.getPrimaryKeyValues().equals(pk)) {
                return inList;
            }
        }
        return null;
    }

    public static void deleteAll(StaService sts) throws ServiceFailureException {
        deleteAll(sts.service);
    }

    public static void deleteAll(SensorThingsService service) throws ServiceFailureException {
        ModelRegistry mr = service.getModelRegistry();
        if (mr.getEntityTypeForName("Thing") != null) {
            // First delete Things, for efficiency
            deleteAll(service.dao(mr.getEntityTypeForName("Thing")));
        }
        for (de.fraunhofer.iosb.ilt.frostclient.model.EntityType et : mr.getEntityTypes()) {
            if ("user".equalsIgnoreCase(et.entityName)) {
                // Can't usually delete users.
                continue;
            }
            try {
                deleteAll(service.dao(et));
            } catch (NotFoundException exc) {
                // the model has entity types that dont exist on the server.
            }
        }
    }

    public static void deleteAll(Dao doa) throws ServiceFailureException {
        boolean more = true;
        int count = 0;
        while (more) {
            EntitySet entities = doa.query().list();
            if (entities.getCount() > 0) {
                LOGGER.debug("{} to go.", entities.getCount());
            } else {
                more = false;
            }
            for (Entity entity : entities) {
                doa.delete(entity);
                count++;
            }
        }
        LOGGER.debug("Deleted {} using {}.", count, doa.getClass().getName());
    }

    /**
     * Find the expected count value for the given request. Can not determine
     * the count for paths like /Datastreams(xxx)/Thing/Locations since the id
     * of the Thing can not be determined from the path.
     *
     * @param request The request to determine the count for.
     * @param entityCounts The object holding the entity counts.
     * @return The expected count for the given request.
     */
    public static long findCountForRequest(Request request, EntityCounts entityCounts) {
        Object parentId = -1;
        long count = -1;
        EntityType parentType = null;
        for (PathElement element : request.getPath()) {
            EntityType elementType = element.getEntityType();
            if (element.getId() != null) {
                parentId = element.getId();
                parentType = elementType;
                count = -1;
            } else if (parentType == null) {
                if (!element.isCollection()) {
                    throw new IllegalArgumentException("Non-collection requested without parent.");
                }
                count = entityCounts.getCount(elementType);
            } else if (element.isCollection()) {
                count = entityCounts.getCount(parentType, parentId, elementType);
                parentType = null;
                parentId = -1;
            } else {
                count = -1;
                // Can not determine the id of this single-entity.
            }
        }

        return count;
    }

    /**
     * Checks the given response against the given request.
     *
     * @param extensions The server extensions that are enabled.
     * @param response The response object to check.
     * @param request The request to check the response against.
     * @param entityCounts The object with the expected entity counts.
     * @return the nextLink, or null.
     */
    public static String checkResponse(Set<Extension> extensions, JsonNode response, Request request, EntityCounts entityCounts) {
        String nextLink = null;
        if (request.isCollection()) {
            checkCollection(extensions, response.get("value"), request, entityCounts);

            // check count for request
            Query expandQuery = request.getQuery();
            Boolean count = expandQuery.getCount();
            String countProperty = "@iot.count";
            if (count != null) {
                if (count) {
                    String message = "Response should have property " + countProperty + " for request: '" + request.toString() + "'";
                    assertTrue(response.has(countProperty), message);
                } else {
                    String message = "Response should not have property " + countProperty + " for request: '" + request.toString() + "'";
                    assertFalse(response.has(countProperty), message);
                }
            }

            long expectedCount = findCountForRequest(request, entityCounts);
            if (response.has(countProperty) && expectedCount != -1) {
                long foundCount = response.get(countProperty).asLong();
                String message = "Incorrect count for collection of " + request.getEntityType() + " for request: '" + request.toString() + "'";
                assertEquals(expectedCount, foundCount, message);
            }
            Long top = expandQuery.getTop();
            String nextLinkProperty = "@iot.nextLink";
            if (response.has(nextLinkProperty)) {
                nextLink = response.get(nextLinkProperty).textValue();
            }
            if (top != null && expectedCount != -1) {
                int foundNumber = response.get("value").size();
                long skip = expandQuery.getSkip() == null ? 0 : expandQuery.getSkip();

                long expectedNumber = Math.max(0, Math.min(expectedCount - skip, top));
                if (foundNumber != expectedNumber) {
                    fail("Requested " + top + " of " + expectedCount + ", expected " + expectedNumber + " with skip of " + skip + " but received " + foundNumber);
                }

                if (foundNumber + skip < expectedCount) {
                    // should have nextLink
                    String message = "Entity should have " + nextLinkProperty + " for request: '" + request.toString() + "'";
                    assertTrue(response.has(nextLinkProperty), message);
                } else {
                    // should not have nextLink
                    String message = "Entity should not have " + nextLinkProperty + " for request: '" + request.toString() + "'";
                    assertFalse(response.has(nextLinkProperty), message);
                }

            }

        } else {
            checkEntity(extensions, response, request, entityCounts);
        }
        return nextLink;
    }

    /**
     * Check a collection from a response, against the given expand as present
     * in the request.
     *
     * @param extensions Extensions that may affect the entity.
     * @param collection The collection of items to check.
     * @param expand The expand that led to the collection.
     * @param entityCounts The object with the expected entity counts.
     */
    public static void checkCollection(Set<Extension> extensions, JsonNode collection, Expand expand, EntityCounts entityCounts) {
        // Check entities
        for (int i = 0; i < collection.size(); i++) {
            checkEntity(extensions, collection.get(i), expand, entityCounts);
        }
        // todo: check orderby
        // todo: check filter
    }

    /**
     * Check the given entity from a response against the given expand.
     *
     * @param extensions Extensions that may affect the entity.
     * @param entity The entity to check.
     * @param expand The expand that led to the entity.
     * @param entityCounts The object with the expected entity counts.
     */
    public static void checkEntity(Set<Extension> extensions, JsonNode entity, Expand expand, EntityCounts entityCounts) {
        EntityType entityType = expand.getEntityType();
        Query query = expand.getQuery();

        // Check properties & select
        List<String> select = new ArrayList<>(query.getSelect());
        if (select.isEmpty()) {
            select.add("id");
            select.addAll(entityType.getPropertyNames());
            if (expand.isToplevel()) {
                select.addAll(entityType.getRelations(extensions));
            }
        }
        if (select.contains("id")) {
            String message = "Entity should have property @iot.id for request: '" + expand.toString() + "'";
            assertTrue(entity.has("@iot.id"), message);
        } else {
            String message = "Entity should not have property @iot.id for request: '" + expand.toString() + "'";
            assertFalse(entity.has("@iot.id"), message);
        }
        for (EntityType.EntityProperty property : entityType.getProperties()) {
            if (select.contains(property.name)) {
                String message = "Entity should have property " + property.name + " for request: '" + expand.toString() + "'";
                assertTrue(entity.has(property.name) || property.optional, message);
            } else {
                String message = "Entity should not have property " + property.name + " for request: '" + expand.toString() + "'";
                assertFalse(entity.has(property.name), message);
            }
        }
        for (String relationName : entityType.getRelations(extensions)) {
            String propertyName = relationName + "@iot.navigationLink";
            if (select.contains(relationName)) {
                String message = "Entity should have property " + propertyName + " for request: '" + expand.toString() + "'";
                assertTrue(entity.has(propertyName), message);
            } else {
                String message = "Entity should not have property " + propertyName + " for request: '" + expand.toString() + "'";
                assertFalse(entity.has(propertyName), message);
            }
        }

        // Entity id in case we need to check counts.
        Object entityId = entity.get("@iot.id");

        // Check expand
        List<String> relations = new ArrayList<>(entityType.getRelations(extensions));
        for (Expand subExpand : query.getExpand()) {
            PathElement path = subExpand.getPath().get(0);
            String propertyName = path.getPropertyName();
            if (!entity.has(propertyName)) {
                fail("Entity should have expanded " + propertyName + " for request: '" + expand.toString() + "'");
            }

            // Check the expanded items
            if (subExpand.isCollection()) {
                checkCollection(extensions, entity.get(propertyName), subExpand, entityCounts);
            } else {
                checkEntity(extensions, entity.get(propertyName), subExpand, entityCounts);
            }
            relations.remove(propertyName);

            // For expanded collections, check count, top, skip
            if (subExpand.isCollection()) {
                // Check count
                Query expandQuery = subExpand.getQuery();
                Boolean count = expandQuery.getCount();
                String countProperty = propertyName + "@iot.count";
                boolean hasCountProperty = entity.has(countProperty);
                if (count != null) {
                    if (count) {
                        String message = "Entity should have property " + countProperty + " for request: '" + expand.toString() + "'";
                        assertTrue(hasCountProperty, message);
                    } else {
                        String message = "Entity should not have property " + countProperty + " for request: '" + expand.toString() + "'";
                        assertFalse(hasCountProperty, message);
                    }
                }

                long expectedCount = entityCounts.getCount(entityType, entityId, EntityType.getForRelation(propertyName));
                if (hasCountProperty && expectedCount != -1) {
                    long foundCount = entity.get(countProperty).asLong();
                    String message = "Found incorrect count for " + countProperty;
                    assertEquals(expectedCount, foundCount, message);
                }

                Long top = expandQuery.getTop();
                if (top != null && expectedCount != -1) {
                    int foundNumber = entity.get(propertyName).size();
                    long skip = expandQuery.getSkip() == null ? 0 : expandQuery.getSkip();

                    long expectedNumber = Math.min(expectedCount - skip, top);
                    if (foundNumber != expectedNumber) {
                        fail("Requested " + top + " of " + expectedCount + ", expected " + expectedNumber + " with skip of " + skip + " but received " + foundNumber);
                    }

                    String nextLinkProperty = propertyName + "@iot.nextLink";
                    if (foundNumber + skip < expectedCount) {
                        // should have nextLink
                        String message = "Entity should have " + nextLinkProperty + " for expand " + subExpand.toString();
                        assertTrue(entity.has(nextLinkProperty), message);
                    } else {
                        // should not have nextLink
                        String message = "Entity should have " + nextLinkProperty + " for expand " + subExpand.toString();
                        assertFalse(entity.has(nextLinkProperty), message);
                    }

                }

            }
        }
        for (String propertyName : relations) {
            if (entity.has(propertyName)) {
                fail("Entity should not have expanded " + propertyName + " for request: '" + expand.toString() + "'");
            }
        }
    }

    public static String listEntities(List<Entity> list) {
        StringBuilder result = new StringBuilder();
        for (Entity item : list) {
            result.append(item.getPrimaryKeyValues().toString());
            result.append(", ");
        }
        if (result.length() == 0) {
            return "";
        }
        return result.substring(0, result.length() - 2);
    }

    public static void compareEntityWithRemote(SensorThingsService service, Entity expected, EntityPropertyMain... properties) {
        compareEntityWithRemote(service, expected, Arrays.asList(properties));
    }

    public static void compareEntityWithRemote(SensorThingsService service, Entity expected, List<EntityPropertyMain> properties) {
        try {
            final de.fraunhofer.iosb.ilt.frostclient.model.EntityType entityType = expected.getEntityType();
            final Entity found = service.dao(entityType).find(expected.getPrimaryKeyValues());
            final List<EntityPropertyMain> toCheck = new ArrayList<>(properties);
            if (toCheck.isEmpty()) {
                toCheck.addAll(entityType.getEntityProperties());
            }
            for (EntityPropertyMain property : toCheck) {
                if (property.isReadOnly()) {
                    continue;
                }
                assertEquals(expected.getProperty(property), found.getProperty(property), () -> "property: " + property.getName());
            }

        } catch (ServiceFailureException ex) {
            LOGGER.error("Failed to fetch entity to compare", ex);
            Assertions.fail("Failed to fetch entity to compare");
        }

    }

    public static void compareEntityWithRemote(SensorThingsService service, Entity expected, NavigationPropertyEntity property) {
        try {
            final de.fraunhofer.iosb.ilt.frostclient.model.EntityType entityType = expected.getEntityType();
            final Entity found = service.dao(entityType).find(expected.getPrimaryKeyValues());
            final Entity linkedExpected = expected.getProperty(property);
            final Entity linkedFound = found.getProperty(property);
            for (EntityPropertyMain keyProp : property.getEntityType().getPrimaryKey().getKeyProperties()) {
                assertEquals(linkedExpected.getProperty(keyProp), linkedFound.getProperty(keyProp));
            }
        } catch (ServiceFailureException ex) {
            LOGGER.error("Failed to fetch entity to compare", ex);
            Assertions.fail("Failed to fetch entity to compare");
        }

    }

    public static void testFilterResults(SensorThingsService service, de.fraunhofer.iosb.ilt.frostclient.model.EntityType type, String filter, List<Entity> expected) {
        testFilterResults("-", service, type, filter, expected);
    }

    public static void testFilterResults(String user, SensorThingsService service, de.fraunhofer.iosb.ilt.frostclient.model.EntityType type, String filter, List<Entity> expected) {
        testFilterResults(user, service.dao(type), filter, expected);
    }

    public static void testFilterResults(Dao doa, String filter, List<Entity> expected) {
        testFilterResults("-", doa, filter, expected);
    }

    public static void testFilterResults(String user, Dao doa, String filter, List<Entity> expected) {
        try {
            EntitySet result = doa.query().filter(filter).list();
            EntityUtils.ResultTestResult check = EntityUtils.resultContains(result, expected);
            String message = "Failed for " + user + " on filter: " + filter + " Cause: " + check.message;
            if (!check.testOk) {
                LOGGER.info("Failed for {} on filter: {}\nexpected {},\n     got {}.",
                        user,
                        filter,
                        EntityUtils.listEntities(expected),
                        EntityUtils.listEntities(result.toList()));
            }
            assertTrue(check.testOk, message);
        } catch (ServiceFailureException ex) {
            LOGGER.error("Exception filtering doa {} using {} :", doa, filter, ex);
            fail("Failed to call service: " + ex.getMessage());
        }
    }

    public static void filterForException(SensorThingsService service, de.fraunhofer.iosb.ilt.frostclient.model.EntityType type, String filter, int... expectedCode) {
        filterForException("-", service, type, filter, expectedCode);
    }

    public static void filterForException(String user, SensorThingsService service, de.fraunhofer.iosb.ilt.frostclient.model.EntityType type, String filter, int... expectedCode) {
        filterForException(user, service.dao(type), filter, expectedCode);
    }

    public static void filterForException(Dao doa, String filter, int... expectedCode) {
        filterForException("-", doa, filter, expectedCode);
    }

    public static void filterForException(String user, Dao doa, String filter, int... expectedCode) {
        try {
            doa.query().filter(filter).list();
        } catch (StatusCodeException e) {
            String message = "User " + user + ", Filter " + filter + " did not respond with one of " + Arrays.toString(expectedCode) + ", but with " + e.getStatusCode() + ".";
            AuthTestHelper.expectStatusCodeException(message, e, expectedCode);
            return;
        } catch (ServiceFailureException ex) {
            LOGGER.error("Exception:", ex);
            fail("Failed to call service for filter User " + user + ", " + filter + " " + ex);
        }
        fail("User " + user + ", Filter " + filter + " did not respond with " + Arrays.toString(expectedCode) + ".");
    }

    public static Entity createSensor(SensorThingsService srvc, String name, String desc, String type, String metadata, List<Entity> registry) throws ServiceFailureException {
        int idx = registry.size();
        MapValue properties = CollectionsHelper.propertiesBuilder()
                .addItem("idx", idx)
                .build();
        SensorThingsV11Sensing sMdl = srvc.getModel(SensorThingsV11Sensing.class);
        Entity sensor = sMdl.newSensor(name, desc, type, metadata)
                .setProperty(EP_PROPERTIES, properties);
        srvc.create(sensor);
        registry.add(sensor);
        return sensor;
    }

    public static Entity createDatastream(SensorThingsService srvc, String name, String desc, String type, UnitOfMeasurement uom, Entity thing, Entity sensor, Entity op, List<Entity> registry) throws ServiceFailureException {
        int idx = registry.size();
        MapValue properties = CollectionsHelper.propertiesBuilder()
                .addItem("idx", idx)
                .build();
        SensorThingsV11Sensing sMdl = srvc.getModel(SensorThingsV11Sensing.class);
        Entity ds = sMdl.newDatastream(name, desc, type, uom)
                .setProperty(EP_PROPERTIES, properties)
                .setProperty(sMdl.npDatastreamThing, thing)
                .setProperty(sMdl.npDatastreamSensor, sensor)
                .setProperty(sMdl.npDatastreamObservedproperty, op);
        srvc.create(ds);
        registry.add(ds);
        return ds;
    }

    public static Entity createObservedProperty(SensorThingsService srvc, String name, String definition, String description, List<Entity> registry) throws ServiceFailureException {
        int idx = registry.size();
        MapValue properties = CollectionsHelper.propertiesBuilder()
                .addItem("idx", idx)
                .build();
        SensorThingsV11Sensing sMdl = srvc.getModel(SensorThingsV11Sensing.class);
        Entity obsProp = sMdl.newObservedProperty(name, definition, description)
                .setProperty(EP_PROPERTIES, properties);
        srvc.create(obsProp);
        registry.add(obsProp);
        return obsProp;
    }

    public static void createObservationSet(SensorThingsService srvc, Entity datastream, long resultStart, ZonedDateTime phenomenonTimeStart, TimeInterval validTimeStart, long count, List<Entity> registry) throws ServiceFailureException {
        for (int i = 0; i < count; i++) {
            ZonedDateTime phenTime = phenomenonTimeStart.plus(i, ChronoUnit.HOURS);
            TimeInterval validTime = TimeInterval.create(
                    validTimeStart.getStart().plus(count, TimeUnit.HOURS),
                    validTimeStart.getEnd().plus(count, TimeUnit.HOURS));
            createObservation(srvc, datastream, resultStart + i, phenTime, validTime, registry);
        }
    }

    public static Entity createObservation(SensorThingsService srvc, Entity datastream, long result, ZonedDateTime phenomenonTime, TimeInterval validTime, List<Entity> registry) throws ServiceFailureException {
        int idx = registry.size();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("idx", idx);
        SensorThingsV11Sensing sMdl = srvc.getModel(SensorThingsV11Sensing.class);
        Entity obs = sMdl.newObservation(result, phenomenonTime, datastream)
                .setProperty(EP_VALIDTIME, validTime)
                .setProperty(EP_PARAMETERS, parameters);
        if (idx % 2 == 0) {
            obs.setProperty(EP_RESULTQUALITY, idx);
        } else {
            obs.setProperty(EP_RESULTQUALITY, "number-" + idx);
        }
        srvc.create(obs);
        registry.add(obs);
        return obs;
    }

    public static Entity createObservation(SensorThingsService srvc, Entity datastream, long result, ZonedDateTime phenomenonTime, List<Entity> registry) throws ServiceFailureException {
        SensorThingsV11Sensing sMdl = srvc.getModel(SensorThingsV11Sensing.class);
        Entity obs = sMdl.newObservation(result, phenomenonTime, datastream);
        srvc.create(obs);
        registry.add(obs);
        return obs;
    }

}
