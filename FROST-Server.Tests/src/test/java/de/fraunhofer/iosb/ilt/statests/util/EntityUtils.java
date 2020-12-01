package de.fraunhofer.iosb.ilt.statests.util;

import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.StatusCodeException;
import de.fraunhofer.iosb.ilt.sta.dao.BaseDao;
import de.fraunhofer.iosb.ilt.sta.model.Entity;
import de.fraunhofer.iosb.ilt.sta.model.Id;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.ext.EntityList;
import de.fraunhofer.iosb.ilt.sta.service.SensorThingsService;
import de.fraunhofer.iosb.ilt.statests.ServerSettings;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
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

    public static ResultTestResult resultContains(EntityList<? extends Entity> result, Entity... entities) {
        return resultContains(result, new ArrayList(Arrays.asList(entities)));
    }

    /**
     * Checks if the list contains all the given entities exactly once.
     *
     * @param result the result to check.
     * @param expected the expected entities.
     * @return the result of the comparison.
     */
    public static ResultTestResult resultContains(EntityList<? extends Entity> result, List<? extends Entity> expected) {
        long count = result.getCount();
        if (count != -1 && count != expected.size()) {
            LOGGER.info("Result count ({}) not equal to expected count ({})", count, expected.size());
            return new ResultTestResult(false, "Result count " + count + " not equal to expected count (" + expected.size() + ")");
        }
        List<? extends Entity> testList = new ArrayList<>(expected);
        Iterator<? extends Entity> it;
        for (it = result.fullIterator(); it.hasNext();) {
            Entity next = it.next();
            Entity inList = findEntityIn(next, testList);
            if (!testList.remove(inList)) {
                LOGGER.info("Entity with id {} found in result that is not expected.", next.getId());
                return new ResultTestResult(false, "Entity with id " + next.getId() + " found in result that is not expected.");
            }
        }
        if (!testList.isEmpty()) {
            LOGGER.info("Expected entity not found in result.");
            return new ResultTestResult(false, testList.size() + " expected entities not in result.");
        }
        return new ResultTestResult(true, "Check ok.");
    }

    public static Entity findEntityIn(Entity entity, List<? extends Entity> entities) {
        Id id = entity.getId();
        for (Entity inList : entities) {
            if (Objects.equals(inList.getId(), id)) {
                return inList;
            }
        }
        return null;
    }

    public static void deleteAll(ServerVersion version, ServerSettings serverSettings, SensorThingsService sts) throws ServiceFailureException {
        deleteAll(sts.things());
        deleteAll(sts.locations());
        deleteAll(sts.sensors());
        deleteAll(sts.featuresOfInterest());
        deleteAll(sts.observedProperties());
        deleteAll(sts.observations());
        if (serverSettings.implementsRequirement(version, serverSettings.TASKING_REQ)) {
            deleteAll(sts.actuators());
            deleteAll(sts.taskingCapabilities());
            deleteAll(sts.tasks());
        }
    }

    public static <T extends Entity<T>> void deleteAll(BaseDao<T> doa) throws ServiceFailureException {
        boolean more = true;
        int count = 0;
        while (more) {
            EntityList<T> entities = doa.query().list();
            if (entities.getCount() > 0) {
                LOGGER.debug("{} to go.", entities.getCount());
            } else {
                more = false;
            }
            for (T entity : entities) {
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
     */
    public static void checkResponse(Set<Extension> extensions, JSONObject response, Request request, EntityCounts entityCounts) {
        try {
            if (request.isCollection()) {
                checkCollection(extensions, response.getJSONArray("value"), request, entityCounts);

                // check count for request
                Query expandQuery = request.getQuery();
                Boolean count = expandQuery.getCount();
                String countProperty = "@iot.count";
                if (count != null) {
                    if (count) {
                        String message = "Response should have property " + countProperty + " for request: '" + request.toString() + "'";
                        Assert.assertTrue(message, response.has(countProperty));
                    } else {
                        String message = "Response should not have property " + countProperty + " for request: '" + request.toString() + "'";
                        Assert.assertFalse(message, response.has(countProperty));
                    }
                }

                long expectedCount = findCountForRequest(request, entityCounts);
                if (response.has(countProperty) && expectedCount != -1) {
                    long foundCount = response.getLong(countProperty);
                    String message = "Incorrect count for collection of " + request.getEntityType() + " for request: '" + request.toString() + "'";
                    Assert.assertEquals(message, expectedCount, foundCount);
                }
                Long top = expandQuery.getTop();
                if (top != null && expectedCount != -1) {
                    int foundNumber = response.getJSONArray("value").length();
                    long skip = expandQuery.getSkip() == null ? 0 : expandQuery.getSkip();

                    long expectedNumber = Math.max(0, Math.min(expectedCount - skip, top));
                    if (foundNumber != expectedNumber) {
                        Assert.fail("Requested " + top + " of " + expectedCount + ", expected " + expectedNumber + " with skip of " + skip + " but received " + foundNumber);
                    }

                    String nextLinkProperty = "@iot.nextLink";
                    if (foundNumber + skip < expectedCount) {
                        // should have nextLink
                        String message = "Entity should have " + nextLinkProperty + " for request: '" + request.toString() + "'";
                        Assert.assertTrue(message, response.has(nextLinkProperty));
                    } else {
                        // should not have nextLink
                        String message = "Entity should not have " + nextLinkProperty + " for request: '" + request.toString() + "'";
                        Assert.assertFalse(message, response.has(nextLinkProperty));
                    }

                }

            } else {
                checkEntity(extensions, response, request, entityCounts);
            }
        } catch (JSONException ex) {
            String message = "Failure when checking response of query '" + request.getLastUrl() + "'";
            LOGGER.error(message, ex);
            Assert.fail(message);
        }
    }

    /**
     * Check a collection from a response, against the given expand as present
     * in the request.
     *
     * @param extensions Extensions that may affect the entity.
     * @param collection The collection of items to check.
     * @param expand The expand that led to the collection.
     * @param entityCounts The object with the expected entity counts.
     * @throws JSONException if there is a problem with the json.
     */
    public static void checkCollection(Set<Extension> extensions, JSONArray collection, Expand expand, EntityCounts entityCounts) throws JSONException {
        // Check entities
        for (int i = 0; i < collection.length(); i++) {
            checkEntity(extensions, collection.getJSONObject(i), expand, entityCounts);
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
     * @throws JSONException if there is a problem with the json.
     */
    public static void checkEntity(Set<Extension> extensions, JSONObject entity, Expand expand, EntityCounts entityCounts) throws JSONException {
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
            Assert.assertTrue(message, entity.has("@iot.id"));
        } else {
            String message = "Entity should not have property @iot.id for request: '" + expand.toString() + "'";
            Assert.assertFalse(message, entity.has("@iot.id"));
        }
        for (EntityType.EntityProperty property : entityType.getProperties()) {
            if (select.contains(property.name)) {
                String message
                        = "Entity should have property " + property.name + " for request: '" + expand.toString() + "'";
                Assert.assertTrue(message,
                        entity.has(property.name) || property.optional);
            } else {
                String message = "Entity should not have property " + property.name + " for request: '" + expand.toString() + "'";
                Assert.assertFalse(message, entity.has(property.name));
            }
        }
        for (String relationName : entityType.getRelations(extensions)) {
            String propertyName = relationName + "@iot.navigationLink";
            if (select.contains(relationName)) {
                String message = "Entity should have property " + propertyName + " for request: '" + expand.toString() + "'";
                Assert.assertTrue(message, entity.has(propertyName));
            } else {
                String message = "Entity should not have property " + propertyName + " for request: '" + expand.toString() + "'";
                Assert.assertFalse(message, entity.has(propertyName));
            }
        }

        // Entity id in case we need to check counts.
        Object entityId = entity.opt("@iot.id");

        // Check expand
        List<String> relations = new ArrayList<>(entityType.getRelations(extensions));
        for (Expand subExpand : query.getExpand()) {
            PathElement path = subExpand.getPath().get(0);
            String propertyName = path.getPropertyName();
            if (!entity.has(propertyName)) {
                Assert.fail("Entity should have expanded " + propertyName + " for request: '" + expand.toString() + "'");
            }

            // Check the expanded items
            if (subExpand.isCollection()) {
                checkCollection(extensions, entity.getJSONArray(propertyName), subExpand, entityCounts);
            } else {
                checkEntity(extensions, entity.getJSONObject(propertyName), subExpand, entityCounts);
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
                        Assert.assertTrue(message, hasCountProperty);
                    } else {
                        String message = "Entity should not have property " + countProperty + " for request: '" + expand.toString() + "'";
                        Assert.assertFalse(message, hasCountProperty);
                    }
                }

                long expectedCount = entityCounts.getCount(entityType, entityId, EntityType.getForRelation(propertyName));
                if (hasCountProperty && expectedCount != -1) {
                    long foundCount = entity.getLong(countProperty);
                    String message = "Found incorrect count for " + countProperty;
                    Assert.assertEquals(message, expectedCount, foundCount);
                }

                Long top = expandQuery.getTop();
                if (top != null && expectedCount != -1) {
                    int foundNumber = entity.getJSONArray(propertyName).length();
                    long skip = expandQuery.getSkip() == null ? 0 : expandQuery.getSkip();

                    long expectedNumber = Math.min(expectedCount - skip, top);
                    if (foundNumber != expectedNumber) {
                        Assert.fail("Requested " + top + " of " + expectedCount + ", expected " + expectedNumber + " with skip of " + skip + " but received " + foundNumber);
                    }

                    String nextLinkProperty = propertyName + "@iot.nextLink";
                    if (foundNumber + skip < expectedCount) {
                        // should have nextLink
                        String message = "Entity should have " + nextLinkProperty + " for expand " + subExpand.toString();
                        Assert.assertTrue(message, entity.has(nextLinkProperty));
                    } else {
                        // should not have nextLink
                        String message = "Entity should have " + nextLinkProperty + " for expand " + subExpand.toString();
                        Assert.assertFalse(message, entity.has(nextLinkProperty));
                    }

                }

            }
        }
        for (String propertyName : relations) {
            if (entity.has(propertyName)) {
                Assert.fail("Entity should not have expanded " + propertyName + " for request: '" + expand.toString() + "'");
            }
        }
    }

    public static String listEntities(List<? extends Entity> list) {
        StringBuilder result = new StringBuilder();
        for (Entity item : list) {
            if (item instanceof Observation) {
                result.append(((Observation) item).getResult());
            } else {
                result.append(item.getId());
            }
            result.append(", ");
        }
        if (result.length() == 0) {
            return "";
        }
        return result.substring(0, result.length() - 2);
    }

    public static <T extends Entity<T>> void filterAndCheck(BaseDao<T> doa, String filter, List<T> expected) {
        try {
            EntityList<T> result = doa.query().filter(filter).list();
            EntityUtils.ResultTestResult check = EntityUtils.resultContains(result, expected);
            String msg = "Failed on filter: " + filter + " Cause: " + check.message;
            if (!check.testOk) {
                LOGGER.info("Failed filter: {}\nexpected {},\n     got {}.",
                        filter,
                        EntityUtils.listEntities(expected),
                        EntityUtils.listEntities(result.toList()));
            }
            Assert.assertTrue(msg, check.testOk);
        } catch (ServiceFailureException ex) {
            LOGGER.error("Exception:", ex);
            Assert.fail("Failed to call service: " + ex.getMessage());
        }
    }

    public static void filterForException(BaseDao doa, String filter, int expectedCode) {
        try {
            doa.query().filter(filter).list();
        } catch (StatusCodeException e) {
            String msg = "Filter " + filter + " did not respond with " + expectedCode + ", but with " + e.getStatusCode() + ".";
            Assert.assertEquals(msg, expectedCode, e.getStatusCode());
            return;
        } catch (ServiceFailureException ex) {
            LOGGER.error("Exception:", ex);
            Assert.fail("Failed to call service for filter " + filter + " " + ex);
        }
        Assert.fail("Filter " + filter + " did not respond with " + expectedCode + ".");
    }
}
