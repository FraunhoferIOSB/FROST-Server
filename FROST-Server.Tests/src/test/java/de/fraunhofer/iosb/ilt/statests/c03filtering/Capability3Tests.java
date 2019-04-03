package de.fraunhofer.iosb.ilt.statests.c03filtering;

import de.fraunhofer.iosb.ilt.sta.service.SensorThingsService;
import de.fraunhofer.iosb.ilt.statests.ServerSettings;
import de.fraunhofer.iosb.ilt.statests.TestSuite;
import de.fraunhofer.iosb.ilt.statests.util.ControlInformation;
import de.fraunhofer.iosb.ilt.statests.util.EntityCounts;
import de.fraunhofer.iosb.ilt.statests.util.EntityPropertiesSampleValue;
import de.fraunhofer.iosb.ilt.statests.util.EntityType;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import de.fraunhofer.iosb.ilt.statests.util.Expand;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods.HttpResponse;
import de.fraunhofer.iosb.ilt.statests.util.PathElement;
import de.fraunhofer.iosb.ilt.statests.util.Query;
import de.fraunhofer.iosb.ilt.statests.util.Request;
import de.fraunhofer.iosb.ilt.statests.util.ServiceURLBuilder;
import de.fraunhofer.iosb.ilt.statests.util.Utils;
import static de.fraunhofer.iosb.ilt.statests.util.Utils.quoteIdForJson;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Includes various tests of "A.2 Filtering Extension" Conformance class.
 */
public class Capability3Tests {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Capability3Tests.class);

    private static ServerSettings serverSettings;
    private static SensorThingsService service;

    private static Object thingId1, thingId2,
            datastreamId1, datastreamId2, datastreamId3, datastreamId4,
            locationId1, locationId2, historicalLocationId1,
            historicalLocationId2, historicalLocationId3, historicalLocationId4,
            sensorId1, sensorId2, sensorId3, sensorId4,
            observedPropertyId1, observedPropertyId2, observedPropertyId3,
            observationId1, observationId2, observationId3, observationId4, observationId5, observationId6, observationId7, observationId8, observationId9, observationId10, observationId11, observationId12,
            featureOfInterestId1, featureOfInterestId2;

    private static EntityCounts entityCounts = new EntityCounts();

    /**
     * This method will be run before starting the test for this conformance
     * class. It creates a set of entities to start testing query options.
     *
     */
    @BeforeClass
    public static void setUp() throws MalformedURLException {
        LOGGER.info("Setting up class.");
        TestSuite suite = TestSuite.getInstance();
        serverSettings = suite.getServerSettings();
        service = new SensorThingsService(new URL(serverSettings.serviceUrl));
        createEntities();
    }

    /**
     * This method is testing $select query option. It tests $select for
     * collection of entities with 1 level and 2 levels resource path. It also
     * tests $select for one or more properties.
     */
    @Test
    public void readEntitiesWithSelectQO() {
        checkSelectForEntityType(EntityType.THING);
        checkSelectForEntityType(EntityType.LOCATION);
        checkSelectForEntityType(EntityType.HISTORICAL_LOCATION);
        checkSelectForEntityType(EntityType.DATASTREAM);
        checkSelectForEntityType(EntityType.SENSOR);
        checkSelectForEntityType(EntityType.OBSERVED_PROPERTY);
        checkSelectForEntityType(EntityType.OBSERVATION);
        checkSelectForEntityType(EntityType.FEATURE_OF_INTEREST);
        checkSelectForEntityTypeRelations(EntityType.THING, thingId1);
        checkSelectForEntityTypeRelations(EntityType.LOCATION, locationId1);
        checkSelectForEntityTypeRelations(EntityType.HISTORICAL_LOCATION, historicalLocationId1);
        checkSelectForEntityTypeRelations(EntityType.DATASTREAM, datastreamId1);
        checkSelectForEntityTypeRelations(EntityType.SENSOR, sensorId1);
        checkSelectForEntityTypeRelations(EntityType.OBSERVED_PROPERTY, observedPropertyId1);
        checkSelectForEntityTypeRelations(EntityType.OBSERVATION, observationId1);
        checkSelectForEntityTypeRelations(EntityType.FEATURE_OF_INTEREST, featureOfInterestId1);

    }

    /**
     * This method is testing $expand query option. It tests $expand for
     * collection of entities with 1 level and 2 levels resource path. It also
     * tests $expand for one or more collections, and also tests multilevel
     * $expand.
     */
    @Test
    public void readEntitiesWithExpandQO() {
        checkExpandForEntityType(EntityType.THING);
        checkExpandForEntityType(EntityType.LOCATION);
        checkExpandForEntityType(EntityType.HISTORICAL_LOCATION);
        checkExpandForEntityType(EntityType.DATASTREAM);
        checkExpandForEntityType(EntityType.SENSOR);
        checkExpandForEntityType(EntityType.OBSERVED_PROPERTY);
        checkExpandForEntityType(EntityType.OBSERVATION);
        checkExpandForEntityType(EntityType.FEATURE_OF_INTEREST);
        checkExpandForEntityTypeRelations(EntityType.THING, thingId1);
        checkExpandForEntityTypeRelations(EntityType.LOCATION, locationId1);
        checkExpandForEntityTypeRelations(EntityType.HISTORICAL_LOCATION, historicalLocationId1);
        checkExpandForEntityTypeRelations(EntityType.DATASTREAM, datastreamId1);
        checkExpandForEntityTypeRelations(EntityType.SENSOR, sensorId1);
        checkExpandForEntityTypeRelations(EntityType.OBSERVED_PROPERTY, observedPropertyId1);
        checkExpandForEntityTypeRelations(EntityType.OBSERVATION, observationId1);
        checkExpandForEntityTypeRelations(EntityType.FEATURE_OF_INTEREST, featureOfInterestId1);
        checkExpandForEntityTypeMultilevel(EntityType.THING);
        checkExpandForEntityTypeMultilevel(EntityType.LOCATION);
        checkExpandForEntityTypeMultilevel(EntityType.HISTORICAL_LOCATION);
        checkExpandForEntityTypeMultilevel(EntityType.DATASTREAM);
        checkExpandForEntityTypeMultilevel(EntityType.SENSOR);
        checkExpandForEntityTypeMultilevel(EntityType.OBSERVED_PROPERTY);
        checkExpandForEntityTypeMultilevel(EntityType.OBSERVATION);
        checkExpandForEntityTypeMultilevel(EntityType.FEATURE_OF_INTEREST);
        checkExpandForEntityTypeMultilevelRelations(EntityType.THING, thingId1);
        checkExpandForEntityTypeMultilevelRelations(EntityType.LOCATION, locationId1);
        checkExpandForEntityTypeMultilevelRelations(EntityType.HISTORICAL_LOCATION, historicalLocationId1);
        checkExpandForEntityTypeMultilevelRelations(EntityType.DATASTREAM, datastreamId1);
        checkExpandForEntityTypeMultilevelRelations(EntityType.SENSOR, sensorId1);
        checkExpandForEntityTypeMultilevelRelations(EntityType.OBSERVED_PROPERTY, observedPropertyId1);
        checkExpandForEntityTypeMultilevelRelations(EntityType.OBSERVATION, observationId1);
        checkExpandForEntityTypeMultilevelRelations(EntityType.FEATURE_OF_INTEREST, featureOfInterestId1);

        checkNestedExpandForEntity(EntityType.THING, thingId1);
        checkNestedExpandForEntity(EntityType.LOCATION, locationId1);
        checkNestedExpandForEntity(EntityType.HISTORICAL_LOCATION, historicalLocationId1);
        checkNestedExpandForEntity(EntityType.DATASTREAM, datastreamId1);
        checkNestedExpandForEntity(EntityType.SENSOR, sensorId1);
        checkNestedExpandForEntity(EntityType.OBSERVED_PROPERTY, observedPropertyId1);
        checkNestedExpandForEntity(EntityType.OBSERVATION, observationId1);
        checkNestedExpandForEntity(EntityType.FEATURE_OF_INTEREST, featureOfInterestId1);
    }

    /**
     * This method is testing $top query option. It tests $top for collection of
     * entities with 1 level and 2 levels resource path. It also tests
     * {@literal @iot.nextLink} with regard to $top.
     */
    @Test
    public void readEntitiesWithTopQO() {
        checkTopForEntityType(EntityType.THING);
        checkTopForEntityType(EntityType.LOCATION);
        checkTopForEntityType(EntityType.HISTORICAL_LOCATION);
        checkTopForEntityType(EntityType.DATASTREAM);
        checkTopForEntityType(EntityType.SENSOR);
        checkTopForEntityType(EntityType.OBSERVED_PROPERTY);
        checkTopForEntityType(EntityType.OBSERVATION);
        checkTopForEntityType(EntityType.FEATURE_OF_INTEREST);
        checkTopForEntityTypeRelation(EntityType.THING, thingId1);
        checkTopForEntityTypeRelation(EntityType.LOCATION, locationId1);
        checkTopForEntityTypeRelation(EntityType.HISTORICAL_LOCATION, historicalLocationId1);
        checkTopForEntityTypeRelation(EntityType.DATASTREAM, datastreamId1);
        checkTopForEntityTypeRelation(EntityType.SENSOR, sensorId1);
        checkTopForEntityTypeRelation(EntityType.OBSERVED_PROPERTY, observedPropertyId1);
        checkTopForEntityTypeRelation(EntityType.OBSERVATION, observationId1);
        checkTopForEntityTypeRelation(EntityType.FEATURE_OF_INTEREST, featureOfInterestId1);
    }

    /**
     * This method is testing $skip query option. It tests $skip for collection
     * of entities with 1 level and 2 levels resource path. It also tests
     * {@literal @iot.nextLink} with regard to $skip.
     */
    @Test
    public void readEntitiesWithSkipQO() {
        checkSkipForEntityType(EntityType.THING);
        checkSkipForEntityType(EntityType.LOCATION);
        checkSkipForEntityType(EntityType.HISTORICAL_LOCATION);
        checkSkipForEntityType(EntityType.DATASTREAM);
        checkSkipForEntityType(EntityType.SENSOR);
        checkSkipForEntityType(EntityType.OBSERVED_PROPERTY);
        checkSkipForEntityType(EntityType.OBSERVATION);
        checkSkipForEntityType(EntityType.FEATURE_OF_INTEREST);
        checkSkipForEntityTypeRelation(EntityType.THING, thingId1);
        checkSkipForEntityTypeRelation(EntityType.LOCATION, locationId1);
        checkSkipForEntityTypeRelation(EntityType.HISTORICAL_LOCATION, historicalLocationId1);
        checkSkipForEntityTypeRelation(EntityType.DATASTREAM, datastreamId1);
        checkSkipForEntityTypeRelation(EntityType.SENSOR, sensorId1);
        checkSkipForEntityTypeRelation(EntityType.OBSERVED_PROPERTY, observedPropertyId1);
        checkSkipForEntityTypeRelation(EntityType.OBSERVATION, observationId1);
        checkSkipForEntityTypeRelation(EntityType.FEATURE_OF_INTEREST, featureOfInterestId1);

    }

    /**
     * This method is testing $orderby query option. It tests $orderby for
     * collection of entities with 1 level and 2 levels resource path. It also
     * tests $orderby for one or more properties, and ascending and descending
     * sorting.
     */
    @Test
    public void readEntitiesWithOrderbyQO() {
        checkOrderbyForEntityType(EntityType.THING);
        checkOrderbyForEntityType(EntityType.LOCATION);
        checkOrderbyForEntityType(EntityType.HISTORICAL_LOCATION);
        checkOrderbyForEntityType(EntityType.DATASTREAM);
        checkOrderbyForEntityType(EntityType.SENSOR);
        checkOrderbyForEntityType(EntityType.OBSERVED_PROPERTY);
        checkOrderbyForEntityType(EntityType.OBSERVATION);
        checkOrderbyForEntityType(EntityType.FEATURE_OF_INTEREST);
        checkOrderbyForEntityTypeRelations(EntityType.THING);
        checkOrderbyForEntityTypeRelations(EntityType.LOCATION);
        checkOrderbyForEntityTypeRelations(EntityType.HISTORICAL_LOCATION);
        checkOrderbyForEntityTypeRelations(EntityType.DATASTREAM);
        checkOrderbyForEntityTypeRelations(EntityType.SENSOR);
        checkOrderbyForEntityTypeRelations(EntityType.OBSERVED_PROPERTY);
        checkOrderbyForEntityTypeRelations(EntityType.OBSERVATION);
        checkOrderbyForEntityTypeRelations(EntityType.FEATURE_OF_INTEREST);
    }

    /**
     * This method is testing $count query option. It tests $count for
     * collection of entities with 1 level and 2 levels resource path.
     */
    @Test
    public void readEntitiesWithCountQO() {
        checkCountForEntityType(EntityType.THING);
        checkCountForEntityType(EntityType.LOCATION);
        checkCountForEntityType(EntityType.HISTORICAL_LOCATION);
        checkCountForEntityType(EntityType.DATASTREAM);
        checkCountForEntityType(EntityType.SENSOR);
        checkCountForEntityType(EntityType.OBSERVED_PROPERTY);
        checkCountForEntityType(EntityType.OBSERVATION);
        checkCountForEntityType(EntityType.FEATURE_OF_INTEREST);
        checkCountForEntityTypeRelations(EntityType.THING, thingId1);
        checkCountForEntityTypeRelations(EntityType.LOCATION, locationId1);
        checkCountForEntityTypeRelations(EntityType.HISTORICAL_LOCATION, historicalLocationId1);
        checkCountForEntityTypeRelations(EntityType.DATASTREAM, datastreamId1);
        checkCountForEntityTypeRelations(EntityType.SENSOR, sensorId1);
        checkCountForEntityTypeRelations(EntityType.OBSERVED_PROPERTY, observedPropertyId1);
        checkCountForEntityTypeRelations(EntityType.OBSERVATION, observationId1);
        checkCountForEntityTypeRelations(EntityType.FEATURE_OF_INTEREST, featureOfInterestId1);
    }

    /**
     * This method is testing $filter query option for
     * {@literal <, <=, =, >=, >} on properties. It tests $filter for collection
     * of entities with 1 level and 2 levels resource path.
     *
     * @throws java.io.UnsupportedEncodingException Should not happen for UTF-8.
     */
    @Test
    public void readEntitiesWithFilterQO() throws UnsupportedEncodingException {
        checkFilterForEntityType(EntityType.THING);
        checkFilterForEntityType(EntityType.LOCATION);
        checkFilterForEntityType(EntityType.HISTORICAL_LOCATION);
        checkFilterForEntityType(EntityType.DATASTREAM);
        checkFilterForEntityType(EntityType.SENSOR);
        checkFilterForEntityType(EntityType.OBSERVED_PROPERTY);
        checkFilterForEntityType(EntityType.OBSERVATION);
        checkFilterForEntityType(EntityType.FEATURE_OF_INTEREST);
        checkFilterForEntityTypeRelations(EntityType.THING);
        checkFilterForEntityTypeRelations(EntityType.LOCATION);
        checkFilterForEntityTypeRelations(EntityType.HISTORICAL_LOCATION);
        checkFilterForEntityTypeRelations(EntityType.DATASTREAM);
        checkFilterForEntityTypeRelations(EntityType.SENSOR);
        checkFilterForEntityTypeRelations(EntityType.OBSERVED_PROPERTY);
        checkFilterForEntityTypeRelations(EntityType.OBSERVATION);
        checkFilterForEntityTypeRelations(EntityType.FEATURE_OF_INTEREST);
    }

    /**
     * This method is testing the correct priority of the query options. It uses
     * $count, $top, $skip, $orderby, and $filter togther and check the priority
     * in result.
     */
    @Test
    public void checkQueriesPriorityOrdering() {
        try {
            String urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.OBSERVATION, null, null, "?$count=true&$top=1&$skip=2&$orderby=phenomenonTime%20asc&$filter=result%20gt%20'3'");
            HttpResponse responseMap = HTTPMethods.doGet(urlString);

            String message = "There is problem for GET Observations using multiple Query Options! HTTP status code: " + responseMap.code;
            Assert.assertEquals(message, 200, responseMap.code);

            String response = responseMap.response;
            JSONArray array = new JSONObject(response).getJSONArray("value");

            message = "The query order of execution is not correct. The expected count is 6. The service returned " + new JSONObject(response).getLong("@iot.count");
            Assert.assertEquals(message, 6, new JSONObject(response).getLong("@iot.count"));

            message = "The query asked for top 1. The service rerurned " + array.length() + " entities.";
            Assert.assertEquals(message, 1, array.length());

            message = "The query order of execution is not correct. The expected Observation result is 6. It is " + array.getJSONObject(0).get("result").toString();
            Assert.assertEquals(message, "6", array.getJSONObject(0).get("result").toString());
        } catch (JSONException e) {
            LOGGER.error("Exception: ", e);
            Assert.fail("An Exception occurred during testing!:\n" + e.getMessage());
        }
    }

    /**
     * This method is testing the operator precedence of the AND and OR
     * operators and parenthesis.
     *
     * @throws java.io.UnsupportedEncodingException Should not happen for UTF-8.
     */
    @Test
    public void checkAndOrPrecendece() throws UnsupportedEncodingException {
        String filter = "$filter=result eq 2 and result eq 1 or result eq 1";
        String fetchError = "There is problem for GET Observations using " + filter;
        String error = filter + "  should return all Observations with a result of 1.";
        String urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.OBSERVATION, null, null, "?" + URLEncoder.encode(filter, "UTF-8"));
        checkResults(urlString, 1, "1", fetchError, error);

        filter = "$filter=(result eq 2 and result eq 1) or result eq 1";
        fetchError = "There is problem for GET Observations using " + filter;
        error = filter + "  should return all Observations with a result of 1.";
        urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.OBSERVATION, null, null, "?" + URLEncoder.encode(filter, "UTF-8"));
        checkResults(urlString, 1, "1", fetchError, error);

        filter = "$filter=result eq 2 and (result eq 1 or result eq 1)";
        fetchError = "There is problem for GET Observations using " + filter;
        error = filter + "  should return no results.";
        urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.OBSERVATION, null, null, "?" + URLEncoder.encode(filter, "UTF-8"));
        checkResults(urlString, 0, "1", fetchError, error);

        filter = "$filter=not result lt 1 and not result gt 1";
        fetchError = "There is problem for GET Observations using " + filter;
        error = filter + "  should return all Observations with a result of 1.";
        urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.OBSERVATION, null, null, "?" + URLEncoder.encode(filter, "UTF-8"));
        checkResults(urlString, 1, "1", fetchError, error);
    }

    /**
     * This method is testing the operator precedence of the ADD, SUB, MUL, DIV
     * and MOD operators and parenthesis.
     *
     * @throws java.io.UnsupportedEncodingException Should not happen for UTF-8.
     */
    @Test
    public void checkArithmeticPrecendece() throws UnsupportedEncodingException {
        String filter = "$filter=1 add result mul 2 sub -1 eq 4";
        String fetchError = "There is problem for GET Observations using " + filter;
        String error = filter + "  should return all Observations with a result of 1.";
        String urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.OBSERVATION, null, null, "?" + URLEncoder.encode(filter, "UTF-8"));
        checkResults(urlString, 1, "1", fetchError, error);

        filter = "$filter=6 div 2 sub result eq 2";
        fetchError = "There is problem for GET Observations using " + filter;
        error = filter + "  should return all Observations with a result of 1.";
        urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.OBSERVATION, null, null, "?" + URLEncoder.encode(filter, "UTF-8"));
        checkResults(urlString, 1, "1", fetchError, error);

        filter = "$filter=1 add 2.0 mod (result add 1) eq 1";
        fetchError = "There is problem for GET Observations using " + filter;
        error = filter + "  should return all Observations with a result of 1.";
        urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.OBSERVATION, null, null, "?" + URLEncoder.encode(filter, "UTF-8"));
        checkResults(urlString, 1, "1", fetchError, error);

        filter = "$filter=14 div (result add 1) mod 3 mul 3 eq 3";
        fetchError = "There is problem for GET Observations using " + filter;
        error = filter + "  should return all Observations with a result of 1.";
        urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.OBSERVATION, null, null, "?" + URLEncoder.encode(filter, "UTF-8"));
        checkResults(urlString, 1, "1", fetchError, error);
    }

    /**
     * Checks the results in the given response. The expectedResult is a string,
     * since any result can be represented as a String.
     *
     * @param urlString The url to call.
     * @param expectedCount The expected number of results.
     * @param expectedResult The expected result as a String.
     * @param fetchError The message to use when the GET response is not 200.
     * The actual response is appended to the message.
     * @param resultError The message to use when the count or result is not the
     * expected value. The actual count or result is appended to the message.
     */
    private void checkResults(String urlString, int expectedCount, String expectedResult, String fetchError, String resultError) {
        try {
            HttpResponse responseMap = HTTPMethods.doGet(urlString);

            String message = fetchError + ": " + responseMap.code;
            Assert.assertEquals(message, 200, responseMap.code);

            String response = responseMap.response;
            JSONArray array = new JSONObject(response).getJSONArray("value");
            int length = array.length();

            message = resultError + " Expected " + expectedCount + " Observations. got " + length + ".";
            Assert.assertTrue(message, length == expectedCount);

            for (int i = 0; i < length; i++) {
                JSONObject obs = array.getJSONObject(i);
                String result = obs.get("result").toString();
                String msg = resultError + " The expected Observation result is " + expectedResult + ", but the given result is " + result;
                Assert.assertEquals(msg, expectedResult, result);
            }
        } catch (JSONException e) {
            LOGGER.error("Exception: ", e);
            Assert.fail("An Exception occurred during testing!:\n" + e.getMessage());
        }
    }

    /**
     * This helper method is checking $orderby for 2 level of entities.
     *
     * @param entityType Entity type from EntityType enum list
     */
    private void checkOrderbyForEntityTypeRelations(EntityType entityType) {
        List<String> relations = entityType.getRelations(serverSettings.extensions);
        try {
            String urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, null, null, null);
            HttpResponse responseMap = HTTPMethods.doGet(urlString);
            String response = responseMap.response;
            JSONArray array = new JSONObject(response).getJSONArray("value");
            if (array.length() == 0) {
                return;
            }
            Object id = array.getJSONObject(0).get(ControlInformation.ID);

            for (String relation : relations) {
                if (!EntityType.isPlural(relation)) {
                    continue;
                }
                EntityType relationEntityType = EntityType.getForRelation(relation);
                List<EntityType.EntityProperty> properties = relationEntityType.getProperties();
                //single orderby
                for (EntityType.EntityProperty property : properties) {
                    if (!property.canSort) {
                        continue;
                    }
                    urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, id, relationEntityType, "?$orderby=" + property.name);
                    responseMap = HTTPMethods.doGet(urlString);
                    response = responseMap.response;
                    array = new JSONObject(response).getJSONArray("value");
                    for (int i = 1; i < array.length(); i++) {
                        String message = "The ordering is not correct for EntityType " + entityType + " orderby property " + property;
                        Assert.assertTrue(message, compareWithPrevious(i, array, property.name) <= 0);
                    }
                    urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, id, relationEntityType, "?$orderby=" + property.name + "%20asc");
                    responseMap = HTTPMethods.doGet(urlString);
                    response = responseMap.response;
                    array = new JSONObject(response).getJSONArray("value");
                    for (int i = 1; i < array.length(); i++) {
                        String message = "The ordering is not correct for EntityType " + entityType + " orderby asc property " + property;
                        Assert.assertTrue(message, compareWithPrevious(i, array, property.name) <= 0);
                    }
                    urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, id, relationEntityType, "?$orderby=" + property.name + "%20desc");
                    responseMap = HTTPMethods.doGet(urlString);
                    response = responseMap.response;
                    array = new JSONObject(response).getJSONArray("value");
                    for (int i = 1; i < array.length(); i++) {
                        String message = "The ordering is not correct for EntityType " + entityType + " orderby desc property " + property;
                        Assert.assertTrue(message, compareWithPrevious(i, array, property.name) >= 0);
                    }
                }

                //multiple orderby
                List<String> orderbyPropeties = new ArrayList<>();
                String orderby = "?$orderby=";
                String orderbyAsc = "?$orderby=";
                String orderbyDesc = "?$orderby=";
                for (EntityType.EntityProperty property : properties) {
                    if (!property.canSort) {
                        continue;
                    }
                    if (orderby.charAt(orderby.length() - 1) != '=') {
                        orderby += ",";
                    }
                    orderby += property.name;
                    orderbyPropeties.add(property.name);
                    urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, id, relationEntityType, orderby);
                    responseMap = HTTPMethods.doGet(urlString);
                    response = responseMap.response;
                    array = new JSONObject(response).getJSONArray("value");
                    for (int i = 1; i < array.length(); i++) {
                        for (String orderProperty : orderbyPropeties) {
                            int compare = compareWithPrevious(i, array, orderProperty);
                            String message = "The ordering is not correct for EntityType " + entityType + " orderby property " + orderProperty;
                            Assert.assertTrue(message, compare <= 0);
                            if (compare != 0) {
                                break;
                            }
                        }
                    }
                    if (orderbyAsc.charAt(orderbyAsc.length() - 1) != '=') {
                        orderbyAsc += ",";
                    }
                    orderbyAsc += property + "%20asc";
                    urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, id, relationEntityType, orderbyAsc);
                    responseMap = HTTPMethods.doGet(urlString);
                    response = responseMap.response;
                    array = new JSONObject(response).getJSONArray("value");
                    for (int i = 1; i < array.length(); i++) {
                        for (String orderProperty : orderbyPropeties) {
                            int compare = compareWithPrevious(i, array, orderProperty);
                            String message = "The ordering is not correct for EntityType " + entityType + " orderby asc property " + orderProperty;
                            Assert.assertTrue(message, compare <= 0);
                            if (compare != 0) {
                                break;
                            }
                        }
                    }
                    if (orderbyDesc.charAt(orderbyDesc.length() - 1) != '=') {
                        orderbyDesc += ",";
                    }
                    orderbyDesc += property + "%20desc";
                    urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, id, relationEntityType, orderbyDesc);
                    responseMap = HTTPMethods.doGet(urlString);
                    response = responseMap.response;
                    array = new JSONObject(response).getJSONArray("value");
                    for (int i = 1; i < array.length(); i++) {
                        for (String orderProperty : orderbyPropeties) {
                            int compare = compareWithPrevious(i, array, orderProperty);
                            String message = "The ordering is not correct for EntityType " + entityType + " orderby desc property " + orderProperty;
                            Assert.assertTrue(message, compare >= 0);
                            if (compare != 0) {
                                break;
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            LOGGER.error("Exception: ", e);
            Assert.fail("An Exception occurred during testing!:\n" + e.getMessage());
        }

    }

    /**
     * This helper method is checking $orderby for a collection.
     *
     * @param entityType Entity type from EntityType enum list
     */
    private void checkOrderbyForEntityType(EntityType entityType) {
        List<EntityType.EntityProperty> properties = entityType.getProperties();
        try {
            //single orderby
            for (EntityType.EntityProperty property : properties) {
                if (!property.canSort) {
                    continue;
                }
                String urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, null, null, "?$orderby=" + property.name);
                HttpResponse responseMap = HTTPMethods.doGet(urlString);
                String response = responseMap.response;
                JSONArray array = new JSONObject(response).getJSONArray("value");
                for (int i = 1; i < array.length(); i++) {
                    String msg = "The default ordering is not correct for EntityType " + entityType + " orderby property " + property.name;
                    Assert.assertTrue(msg, compareWithPrevious(i, array, property.name) <= 0);
                }
                urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, null, null, "?$orderby=" + property.name + "%20asc");
                responseMap = HTTPMethods.doGet(urlString);
                response = responseMap.response;
                array = new JSONObject(response).getJSONArray("value");
                for (int i = 1; i < array.length(); i++) {
                    String msg = "The ascending ordering is not correct for EntityType " + entityType + " orderby asc property " + property.name;
                    Assert.assertTrue(msg, compareWithPrevious(i, array, property.name) <= 0);
                }
                urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, null, null, "?$orderby=" + property.name + "%20desc");
                responseMap = HTTPMethods.doGet(urlString);
                response = responseMap.response;
                array = new JSONObject(response).getJSONArray("value");
                for (int i = 1; i < array.length(); i++) {
                    String msg = "The descending ordering is not correct for EntityType " + entityType + " orderby desc property " + property.name;
                    Assert.assertTrue(msg, compareWithPrevious(i, array, property.name) >= 0);
                }
            }

            //multiple orderby
            List<String> orderbyPropeties = new ArrayList<>();
            String orderby = "?$orderby=";
            String orderbyAsc = "?$orderby=";
            String orderbyDesc = "?$orderby=";
            for (EntityType.EntityProperty property : properties) {
                if (!property.canSort) {
                    continue;
                }
                if (orderby.charAt(orderby.length() - 1) != '=') {
                    orderby += ",";
                }
                orderby += property.name;
                orderbyPropeties.add(property.name);
                String urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, null, null, orderby);
                HttpResponse responseMap = HTTPMethods.doGet(urlString);
                String response = responseMap.response;
                JSONArray array = new JSONObject(response).getJSONArray("value");
                for (int i = 1; i < array.length(); i++) {
                    for (String orderProperty : orderbyPropeties) {
                        int compare = compareWithPrevious(i, array, orderProperty);
                        String message = "The ordering is not correct for EntityType " + entityType + " orderby property " + orderProperty;
                        Assert.assertTrue(message, compare <= 0);
                        if (compare != 0) {
                            break;
                        }
                    }
                }
                if (orderbyAsc.charAt(orderbyAsc.length() - 1) != '=') {
                    orderbyAsc += ",";
                }
                orderbyAsc += property + "%20asc";
                urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, null, null, orderbyAsc);
                responseMap = HTTPMethods.doGet(urlString);
                response = responseMap.response;
                array = new JSONObject(response).getJSONArray("value");
                for (int i = 1; i < array.length(); i++) {
                    for (String orderProperty : orderbyPropeties) {
                        int compare = compareWithPrevious(i, array, orderProperty);
                        String message = "The ordering is not correct for EntityType " + entityType + " orderby asc property " + orderProperty;
                        Assert.assertTrue(message, compare <= 0);
                        if (compare != 0) {
                            break;
                        }
                    }
                }
                if (orderbyDesc.charAt(orderbyDesc.length() - 1) != '=') {
                    orderbyDesc += ",";
                }
                orderbyDesc += property + "%20desc";
                urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, null, null, orderbyDesc);
                responseMap = HTTPMethods.doGet(urlString);
                response = responseMap.response;
                array = new JSONObject(response).getJSONArray("value");
                for (int i = 1; i < array.length(); i++) {
                    for (String orderProperty : orderbyPropeties) {
                        int compare = compareWithPrevious(i, array, orderProperty);
                        String message = "The ordering is not correct for EntityType " + entityType + " orderby desc property " + orderProperty;
                        Assert.assertTrue(message, compare >= 0);
                        if (compare != 0) {
                            break;
                        }
                    }
                }
            }
        } catch (JSONException e) {
            LOGGER.error("Exception: ", e);
            Assert.fail("An Exception occurred during testing " + entityType + ":\n" + e.getMessage());
        }

    }

    private int compareWithPrevious(int idx, JSONArray array, String property) throws JSONException {
        JSONObject jObj1 = array.getJSONObject(idx - 1);
        JSONObject jObj2 = array.getJSONObject(idx);
        if (!jObj1.has(property) && !jObj2.has(property)) {
            // Neither has the property, they are the same.
            return 0;
        }
        if (!jObj1.has(property) || !jObj2.has(property)) {
            // One of the two does not have the property, oder undefined?
            return 0;
        }
        Object o1 = jObj1.get(property);
        Object o2 = jObj2.get(property);
        return compareForOrder(o1, o2);
    }

    private int compareForOrder(Object o1, Object o2) {
        if (o1 instanceof Comparable && o2 instanceof Comparable) {
            if (o1.getClass().isAssignableFrom(o2.getClass())) {
                return ((Comparable) o1).compareTo(o2);
            } else if (o2.getClass().isAssignableFrom(o1.getClass())) {
                return -((Comparable) o2).compareTo(o1);
            }
        }
        return o1.toString().compareTo(o2.toString());
    }

    /**
     * This helper method is checking $skip for s collection.
     *
     * @param entityType Entity type from EntityType enum list
     */
    private void checkSkipForEntityType(EntityType entityType) {
        Request request = new Request(serverSettings.serviceUrl);
        request.addElement(new PathElement(entityType.plural));
        // in case an implementation returns fewer than 12 entities by default we request 12.
        request.getQuery()
                .setTop(12L)
                .setSkip(1L);
        EntityUtils.checkResponse(serverSettings.extensions, request.executeGet(), request, entityCounts);

        request.getQuery().setSkip(2L);
        EntityUtils.checkResponse(serverSettings.extensions, request.executeGet(), request, entityCounts);

        request.getQuery().setSkip(3L);
        EntityUtils.checkResponse(serverSettings.extensions, request.executeGet(), request, entityCounts);

        request.getQuery().setSkip(4L);
        EntityUtils.checkResponse(serverSettings.extensions, request.executeGet(), request, entityCounts);

        request.getQuery().setSkip(12L);
        EntityUtils.checkResponse(serverSettings.extensions, request.executeGet(), request, entityCounts);
    }

    /**
     * This helper method is checking $skip for 2 level of entities.
     *
     * @param entityType Entity type from EntityType enum list
     */
    private void checkSkipForEntityTypeRelation(EntityType entityType, Object entityId) {
        List<String> relations = entityType.getRelations(serverSettings.extensions);
        for (String relation : relations) {
            if (!EntityType.isPlural(relation)) {
                continue;
            }
            Request request = new Request(serverSettings.serviceUrl);
            request.addElement(new PathElement(entityType.plural, entityId))
                    .addElement(new PathElement(relation));
            // in case an implementation returns fewer than 12 entities by default we request 12.
            request.getQuery()
                    .setTop(12L)
                    .setSkip(1L);
            JSONObject response = request.executeGet();
            EntityUtils.checkResponse(serverSettings.extensions, response, request, entityCounts);
        }
    }

    /**
     * This helper method is checking $top for a collection.
     *
     * @param entityType Entity type from EntityType enum list
     */
    private void checkTopForEntityType(EntityType entityType) {
        Request request = new Request(serverSettings.serviceUrl);
        request.addElement(new PathElement(entityType.plural));
        request.getQuery().setTop(1L);
        EntityUtils.checkResponse(serverSettings.extensions, request.executeGet(), request, entityCounts);

        request.getQuery().setTop(2L);
        EntityUtils.checkResponse(serverSettings.extensions, request.executeGet(), request, entityCounts);

        request.getQuery().setTop(3L);
        EntityUtils.checkResponse(serverSettings.extensions, request.executeGet(), request, entityCounts);

        request.getQuery().setTop(4L);
        EntityUtils.checkResponse(serverSettings.extensions, request.executeGet(), request, entityCounts);

        request.getQuery().setTop(5L);
        EntityUtils.checkResponse(serverSettings.extensions, request.executeGet(), request, entityCounts);

        request.getQuery().setTop(12L);
        EntityUtils.checkResponse(serverSettings.extensions, request.executeGet(), request, entityCounts);

        request.getQuery().setTop(13L);
        EntityUtils.checkResponse(serverSettings.extensions, request.executeGet(), request, entityCounts);
    }

    /**
     * This helper method is checking $top for 2 level of entities.
     *
     * @param entityType Entity type from EntityType enum list
     */
    private void checkTopForEntityTypeRelation(EntityType entityType, Object entityId) {
        List<String> relations = entityType.getRelations(serverSettings.extensions);
        for (String relation : relations) {
            if (!EntityType.isPlural(relation)) {
                continue;
            }
            Request request = new Request(serverSettings.serviceUrl);
            request.addElement(new PathElement(entityType.plural, entityId))
                    .addElement(new PathElement(relation));
            Query query = request.getQuery();
            query.setTop(3L);
            JSONObject response = request.executeGet();
            EntityUtils.checkResponse(serverSettings.extensions, response, request, entityCounts);
        }
    }

    /**
     * This helper method is checking $select for a collection.
     *
     * @param entityType Entity type from EntityType enum list
     */
    private void checkSelectForEntityType(EntityType entityType) {
        List<EntityType.EntityProperty> properties = entityType.getProperties();
        for (EntityType.EntityProperty property : properties) {
            Request request = new Request(serverSettings.serviceUrl);
            request.addElement(new PathElement(entityType.plural));
            request.getQuery().addSelect(property.name);
            JSONObject response = request.executeGet();
            EntityUtils.checkResponse(serverSettings.extensions, response, request, entityCounts);
        }

        Request request = new Request(serverSettings.serviceUrl);
        request.addElement(new PathElement(entityType.plural));
        for (EntityType.EntityProperty property : properties) {
            request.getQuery().addSelect(property.name);
            JSONObject response = request.executeGet();
            EntityUtils.checkResponse(serverSettings.extensions, response, request, entityCounts);
        }
    }

    /**
     * This helper method is checking $select for 2 level of entities.
     *
     * @param entityType Entity type from EntityType enum list
     */
    private void checkSelectForEntityTypeRelations(EntityType entityType, Object entityId) {
        List<String> parentRelations = entityType.getRelations(serverSettings.extensions);
        for (String parentRelation : parentRelations) {
            EntityType relationEntityType = EntityType.getForRelation(parentRelation);
            List<EntityType.EntityProperty> properties = relationEntityType.getProperties();
            for (EntityType.EntityProperty property : properties) {
                Request request = new Request(serverSettings.serviceUrl);
                request.addElement(new PathElement(entityType.plural, entityId));
                request.addElement(new PathElement(parentRelation));

                request.getQuery().addSelect(property.name);
                JSONObject response = request.executeGet();
                EntityUtils.checkResponse(serverSettings.extensions, response, request, entityCounts);
            }

            Request request = new Request(serverSettings.serviceUrl);
            request.addElement(new PathElement(entityType.plural, entityId));
            request.addElement(new PathElement(parentRelation));
            for (EntityType.EntityProperty property : properties) {
                request.getQuery().addSelect(property.name);
                JSONObject response = request.executeGet();
                EntityUtils.checkResponse(serverSettings.extensions, response, request, entityCounts);
            }
        }
    }

    /**
     * This helper method is checking $expand for a collection. for instance:
     * /Things?$expand=Datastreams,HistoricalLocations
     *
     * @param entityType Entity type from EntityType enum list
     */
    private void checkExpandForEntityType(EntityType entityType) {
        List<String> relations = entityType.getRelations(serverSettings.extensions);
        for (String relation : relations) {
            Request request = new Request(serverSettings.serviceUrl);
            request.addElement(new PathElement(entityType.plural));
            request.getQuery().addExpand(new Expand().addElement(new PathElement(relation)));
            JSONObject response = request.executeGet();
            EntityUtils.checkResponse(serverSettings.extensions, response, request, entityCounts);
        }

        Request request = new Request(serverSettings.serviceUrl);
        request.addElement(new PathElement(entityType.plural));
        for (String relation : relations) {
            request.getQuery().addExpand(new Expand().addElement(new PathElement(relation)));
            JSONObject response = request.executeGet();
            EntityUtils.checkResponse(serverSettings.extensions, response, request, entityCounts);
        }
    }

    /**
     * This helper method is checking $expand for entities with relations. For
     * instance: /Things(709)/Datastreams?$expand=Thing,Sensor
     *
     * @param entityType Entity type from EntityType enum list
     */
    private void checkExpandForEntityTypeRelations(EntityType entityType, Object entityId) {
        PathElement entityPathElement = new PathElement(entityType.plural, entityId);
        List<String> parentRelations = entityType.getRelations(serverSettings.extensions);
        for (String parentRelation : parentRelations) {
            EntityType parentRelationEntityType = EntityType.getForRelation(parentRelation);
            PathElement parentRelationPathElement = new PathElement(parentRelation);
            List<String> relations = parentRelationEntityType.getRelations(serverSettings.extensions);
            for (String relation : relations) {
                Request request = new Request(serverSettings.serviceUrl);
                request.addElement(entityPathElement);
                request.addElement(parentRelationPathElement);
                request.getQuery().addExpand(new Expand().addElement(new PathElement(relation)));
                JSONObject response = request.executeGet();
                EntityUtils.checkResponse(serverSettings.extensions, response, request, entityCounts);
            }

            Request request = new Request(serverSettings.serviceUrl);
            request.addElement(entityPathElement);
            request.addElement(parentRelationPathElement);
            for (String relation : relations) {
                request.getQuery().addExpand(new Expand().addElement(new PathElement(relation)));
                JSONObject response = request.executeGet();
                EntityUtils.checkResponse(serverSettings.extensions, response, request, entityCounts);
            }
        }
    }

    /**
     * This helper method is checking multilevel $expand for 2 level of
     * entities. For instance:
     * /Things(709)/Datastreams?$expand=Thing/Datastreams,Thing/HistoricalLocations
     *
     * @param entityType Entity type from EntityType enum list
     */
    private void checkExpandForEntityTypeMultilevelRelations(EntityType entityType, Object entityId) {
        PathElement entityPathElement = new PathElement(entityType.plural, entityId);
        List<String> parentRelations = entityType.getRelations(serverSettings.extensions);
        for (String parentRelation : parentRelations) {
            EntityType parentRelationEntityType = EntityType.getForRelation(parentRelation);
            PathElement parentRelationPathElement = new PathElement(parentRelation);

            List<String> relations = parentRelationEntityType.getRelations(serverSettings.extensions);
            for (String relation : relations) {
                EntityType relationType = EntityType.getForRelation(relation);
                List<String> secondLevelRelations = relationType.getRelations(serverSettings.extensions);
                for (String secondLevelRelation : secondLevelRelations) {
                    Request request = new Request(serverSettings.serviceUrl);
                    request.addElement(entityPathElement);
                    request.addElement(parentRelationPathElement);
                    Expand expand = new Expand()
                            .addElement(new PathElement(relation))
                            .addElement(new PathElement(secondLevelRelation));
                    request.getQuery().addExpand(expand);
                    JSONObject response = request.executeGet();
                    request.reNest();
                    EntityUtils.checkResponse(serverSettings.extensions, response, request, entityCounts);
                }
            }
            Request request = new Request(serverSettings.serviceUrl);
            request.addElement(entityPathElement);
            request.addElement(parentRelationPathElement);

            for (String relation : relations) {
                EntityType relationType = EntityType.getForRelation(relation);
                List<String> secondLevelRelations = relationType.getRelations(serverSettings.extensions);
                for (String secondLevelRelation : secondLevelRelations) {
                    Expand expand = new Expand()
                            .addElement(new PathElement(relation))
                            .addElement(new PathElement(secondLevelRelation));
                    request.getQuery().addExpand(expand);
                    JSONObject response = request.executeGet();
                    EntityUtils.checkResponse(serverSettings.extensions, response, request.clone().reNest(), entityCounts);
                }
            }
        }
    }

    /**
     * This helper method is checking multilevel $expand for a collection. For
     * instance: /Things?$expand=Datastreams/Thing,Datastreams/Sensor
     *
     * @param entityType Entity type from EntityType enum list
     */
    private void checkExpandForEntityTypeMultilevel(EntityType entityType) {
        List<String> relations = entityType.getRelations(serverSettings.extensions);
        for (String relation : relations) {
            EntityType relationType = EntityType.getForRelation(relation);
            List<String> secondLevelRelations = relationType.getRelations(serverSettings.extensions);

            for (String secondLevelRelation : secondLevelRelations) {
                Request request = new Request(serverSettings.serviceUrl);
                request.addElement(new PathElement(entityType.plural));
                Expand expand = new Expand()
                        .addElement(new PathElement(relation))
                        .addElement(new PathElement(secondLevelRelation));
                request.getQuery().addExpand(expand);
                JSONObject response = request.executeGet();
                request.reNest();
                EntityUtils.checkResponse(serverSettings.extensions, response, request, entityCounts);
            }
        }

        Request request = new Request(serverSettings.serviceUrl);
        request.addElement(new PathElement(entityType.plural));
        for (String relation : relations) {
            EntityType relationType = EntityType.getForRelation(relation);
            List<String> secondLevelRelations = relationType.getRelations(serverSettings.extensions);
            for (String secondLevelRelation : secondLevelRelations) {
                Expand expand = new Expand()
                        .addElement(new PathElement(relation))
                        .addElement(new PathElement(secondLevelRelation));
                request.getQuery().addExpand(expand);
                JSONObject response = request.executeGet();
                EntityUtils.checkResponse(serverSettings.extensions, response, request.clone().reNest(), entityCounts);
            }
        }
    }

    /**
     * This helper method is checking nested expands two levels deep including
     * select, top, skip and count options. For instance:
     * <pre>
     * ObservedProperties(722)?
     *   $select=name,description&
     *   $expand=Datastreams(
     *     $select=name,unitOfMeasurement,Thing,ObservedProperty;
     *     $expand=Thing(
     *       $select=name,Datastreams,Locations
     *     ),
     *     Sensor(
     *       $select=description,metadata
     *     ),
     *     ObservedProperty(
     *       $select=name,description
     *     ),
     *     Observations(
     *       $select=result,Datastream;
     *       $count=false
     *     );
     *     $count=true
     *   )
     * </pre>
     *
     * @param entityType Entity type from EntityType enum list
     */
    private void checkNestedExpandForEntity(EntityType entityType, Object entityId) {
        PathElement collectionPathElement = new PathElement(entityType.plural);
        PathElement entityPathElement = new PathElement(entityType.plural, entityId);
        Request request2 = new Request(serverSettings.serviceUrl);
        request2.addElement(collectionPathElement);
        boolean even = true;
        long skip = 0;

        List<String> parentRelations = entityType.getRelations(serverSettings.extensions);
        for (String parentRelation : parentRelations) {
            EntityType parentRelationEntityType = EntityType.getForRelation(parentRelation);
            List<String> childRelations = parentRelationEntityType.getRelations(serverSettings.extensions);
            for (String childRelation : childRelations) {
                EntityType childRelationEntityType = EntityType.getForRelation(childRelation);
                Request request = new Request(serverSettings.serviceUrl);
                request.addElement(entityPathElement);
                Query query = request.getQuery();
                entityType.getHalfPropertiesRelations(serverSettings.extensions, query.getSelect(), even);
                query.setCount(even);
                query.setTop(2L);
                query.setSkip(skip);
                Expand expand = new Expand()
                        .addElement(new PathElement(parentRelation));
                query.addExpand(expand);
                even = !even;
                skip = 1 - skip;

                query = expand.getQuery();
                query.setCount(even);
                query.setTop(2L);
                query.setSkip(skip);
                parentRelationEntityType.getHalfPropertiesRelations(serverSettings.extensions, query.getSelect(), even);
                expand = new Expand()
                        .addElement(new PathElement(childRelation));
                query.addExpand(expand);
                even = !even;
                skip = 1 - skip;

                query = expand.getQuery();
                childRelationEntityType.getHalfPropertiesRelations(serverSettings.extensions, query.getSelect(), even);
                query.setCount(even);
                query.setTop(2L);
                query.setSkip(skip);
                even = !even;
                skip = 1 - skip;

                JSONObject response = request.executeGet();
                EntityUtils.checkResponse(serverSettings.extensions, response, request, entityCounts);

                request.getPath().clear();
                request.addElement(collectionPathElement);
                response = request.executeGet();
                EntityUtils.checkResponse(serverSettings.extensions, response, request, entityCounts);
            }

            Query query1 = request2.getQuery();
            Expand expand = new Expand()
                    .addElement(new PathElement(parentRelation));
            query1.addExpand(expand);
            entityType.getHalfPropertiesRelations(serverSettings.extensions, query1.getSelect(), even);
            query1.setCount(even);
            even = !even;

            Query query2 = expand.getQuery();
            for (String childRelation : childRelations) {
                parentRelationEntityType.getHalfPropertiesRelations(serverSettings.extensions, query2.getSelect(), even);
                query2.setCount(even);
                EntityType childRelationEntityType = EntityType.getForRelation(childRelation);
                expand = new Expand()
                        .addElement(new PathElement(childRelation));
                query2.addExpand(expand);
                even = !even;

                Query query3 = expand.getQuery();
                childRelationEntityType.getHalfPropertiesRelations(serverSettings.extensions, query3.getSelect(), even);
                query3.setCount(even);
                even = !even;

                JSONObject response = request2.executeGet();
                EntityUtils.checkResponse(serverSettings.extensions, response, request2, entityCounts);
                even = !even;
            }
        }
    }

    /**
     * This helper method is checking $count for a collection.
     *
     * @param entityType Entity type from EntityType enum list
     */
    private void checkCountForEntityType(EntityType entityType) {
        Request request = new Request(serverSettings.serviceUrl);
        request.addElement(new PathElement(entityType.plural));
        request.getQuery().setCount(true);
        EntityUtils.checkResponse(serverSettings.extensions, request.executeGet(), request, entityCounts);

        request.getQuery().setCount(false);
        EntityUtils.checkResponse(serverSettings.extensions, request.executeGet(), request, entityCounts);
    }

    /**
     * This helper method is checking $count for 2 level of entities.
     *
     * @param entityType Entity type from EntityType enum list
     */
    private void checkCountForEntityTypeRelations(EntityType entityType, Object entityId) {
        List<String> relations = entityType.getRelations(serverSettings.extensions);
        for (String relation : relations) {
            if (!EntityType.isPlural(relation)) {
                continue;
            }
            Request request = new Request(serverSettings.serviceUrl);
            request.addElement(new PathElement(entityType.plural, entityId))
                    .addElement(new PathElement(relation));
            Query query = request.getQuery();
            query.setCount(true);
            EntityUtils.checkResponse(serverSettings.extensions, request.executeGet(), request, entityCounts);

            query.setCount(false);
            EntityUtils.checkResponse(serverSettings.extensions, request.executeGet(), request, entityCounts);
        }
    }

    /**
     * This helper method is checking $filter for a collection.
     *
     * @param entityType Entity type from EntityType enum list
     * @throws java.io.UnsupportedEncodingException Should not happen, UTF-8
     * should always be supported.
     */
    private void checkFilterForEntityType(EntityType entityType) throws UnsupportedEncodingException {
        List<EntityType.EntityProperty> properties = entityType.getProperties();
        List<String> filteredProperties;
        List<Comparable> samplePropertyValues;
        for (int i = 0; i < properties.size(); i++) {
            EntityType.EntityProperty property = properties.get(i);
            filteredProperties = new ArrayList<>();
            samplePropertyValues = new ArrayList<>();
            // TODO: Do we need a canFilter here, or are those sets the same?
            if (!property.canSort) {
                continue;
            }
            filteredProperties.add(property.name);
            Comparable propertyValue = EntityPropertiesSampleValue.getPropertyValueFor(entityType, i);
            if (propertyValue == null) {
                // No sample value available.
                continue;
            }
            samplePropertyValues.add(propertyValue);

            propertyValue = URLEncoder.encode(propertyValue.toString(), "UTF-8");
            String urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, null, null, "?$filter=" + property.name + "%20lt%20" + propertyValue);
            HttpResponse responseMap = HTTPMethods.doGet(urlString);
            String response = responseMap.response;
            checkPropertiesForFilter(response, filteredProperties, samplePropertyValues, -2);

            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, null, null, "?$filter=" + property.name + "%20le%20" + propertyValue);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.response;
            checkPropertiesForFilter(response, filteredProperties, samplePropertyValues, -1);

            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, null, null, "?$filter=" + property.name + "%20eq%20" + propertyValue);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.response;
            checkPropertiesForFilter(response, filteredProperties, samplePropertyValues, 0);

            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, null, null, "?$filter=" + property.name + "%20ne%20" + propertyValue);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.response;
            checkPropertiesForFilter(response, filteredProperties, samplePropertyValues, -3);

            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, null, null, "?$filter=" + property.name + "%20ge%20" + propertyValue);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.response;
            checkPropertiesForFilter(response, filteredProperties, samplePropertyValues, 1);

            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, null, null, "?$filter=" + property.name + "%20gt%20" + propertyValue);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.response;
            checkPropertiesForFilter(response, filteredProperties, samplePropertyValues, 2);
        }
    }

    /**
     * This helper method is checking $filter for 2 level of entities.
     *
     * @param entityType Entity type from EntityType enum list
     * @throws java.io.UnsupportedEncodingException Should not happen, UTF-8
     * should always be supported.
     */
    private void checkFilterForEntityTypeRelations(EntityType entityType) throws UnsupportedEncodingException {
        List<String> relations = entityType.getRelations(serverSettings.extensions);
        String urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, null, null, null);
        HttpResponse responseMap = HTTPMethods.doGet(urlString);
        String response = responseMap.response;
        JSONArray array = null;
        try {
            array = new JSONObject(response).getJSONArray("value");
        } catch (JSONException e) {
            LOGGER.error("Exception: ", e);
            Assert.fail("An Exception occurred during testing!:\n" + e.getMessage());
        }
        if (array.length() == 0) {
            return;
        }
        Object id = null;
        try {
            id = array.getJSONObject(0).get(ControlInformation.ID);
        } catch (JSONException e) {
            LOGGER.error("Exception: ", e);
            Assert.fail("An Exception occurred during testing!:\n" + e.getMessage());
        }

        for (String relation : relations) {
            if (!EntityType.isPlural(relation)) {
                return;
            }
            EntityType relationEntityType = EntityType.getForRelation(relation);

            List<EntityType.EntityProperty> properties = relationEntityType.getProperties();
            List<String> filteredProperties;
            List<Comparable> samplePropertyValues;
            for (int i = 0; i < properties.size(); i++) {
                filteredProperties = new ArrayList<>();
                samplePropertyValues = new ArrayList<>();
                EntityType.EntityProperty property = properties.get(i);
                if (!property.canSort) {
                    continue;
                }
                filteredProperties.add(property.name);
                Comparable propertyValue = EntityPropertiesSampleValue.getPropertyValueFor(relationEntityType, i);
                if (propertyValue == null) {
                    continue;
                }
                samplePropertyValues.add(propertyValue);

                propertyValue = URLEncoder.encode(propertyValue.toString(), "UTF-8");
                urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, id, relationEntityType, "?$filter=" + property.name + "%20lt%20" + propertyValue);
                responseMap = HTTPMethods.doGet(urlString);
                response = responseMap.response;
                checkPropertiesForFilter(response, filteredProperties, samplePropertyValues, -2);

                urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, id, relationEntityType, "?$filter=" + property.name + "%20le%20" + propertyValue);
                responseMap = HTTPMethods.doGet(urlString);
                response = responseMap.response;
                checkPropertiesForFilter(response, filteredProperties, samplePropertyValues, -1);

                urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, id, relationEntityType, "?$filter=" + property.name + "%20eq%20" + propertyValue);
                responseMap = HTTPMethods.doGet(urlString);
                response = responseMap.response;
                checkPropertiesForFilter(response, filteredProperties, samplePropertyValues, 0);

                urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, id, relationEntityType, "?$filter=" + property.name + "%20ne%20" + propertyValue);
                responseMap = HTTPMethods.doGet(urlString);
                response = responseMap.response;
                checkPropertiesForFilter(response, filteredProperties, samplePropertyValues, -3);

                urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, id, relationEntityType, "?$filter=" + property.name + "%20ge%20" + propertyValue);
                responseMap = HTTPMethods.doGet(urlString);
                response = responseMap.response;
                checkPropertiesForFilter(response, filteredProperties, samplePropertyValues, 1);

                urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, id, relationEntityType, "?$filter=" + property.name + "%20gt%20" + propertyValue);
                responseMap = HTTPMethods.doGet(urlString);
                response = responseMap.response;
                checkPropertiesForFilter(response, filteredProperties, samplePropertyValues, 2);
            }
        }
    }

    /**
     * This method is checking the properties of the filtered collection
     *
     * @param response The response to be checked
     * @param properties List of filtered properties
     * @param values List of values for filtered properties
     * @param operator The operator of the filter
     */
    private void checkPropertiesForFilter(String response, List<String> properties, List<Comparable> values, int operator) {
        try {
            JSONObject entities = new JSONObject(response);
            JSONArray entityArray = entities.getJSONArray("value");
            for (int i = 0; i < entityArray.length(); i++) {
                JSONObject entity = entityArray.getJSONObject(i);
                for (int j = 0; j < properties.size(); j++) {
                    Object propertyValue = "";
                    try {
                        propertyValue = entity.get(properties.get(j));
                    } catch (JSONException e) {
                        Assert.fail("The entity does not have property " + properties.get(j));
                    }
                    if (propertyValue == null) {
                        Assert.fail("The entity has null value for property " + properties.get(j));
                    }
                    Comparable value = values.get(j);
                    if (value instanceof String && ((String) value).charAt(0) == '\'') {
                        String sValue = (String) value;
                        value = sValue.substring(1, sValue.length() - 1);
                        if (!(propertyValue instanceof String)) {
                            propertyValue = propertyValue.toString();
                        }
                    } else if (value instanceof DateTime) {
                        propertyValue = ISODateTimeFormat.dateTime().parseDateTime(propertyValue.toString());
                    }

                    int result = value.compareTo(propertyValue);
                    switch (operator) {
                        case -3:
                            String message = properties.get(j) + " should not be equal to " + value + ". But the property value is " + propertyValue;
                            Assert.assertTrue(message, result != 0);
                            break;
                        case -2:
                            message = properties.get(j) + " should be less than " + value + ". But the property value is " + propertyValue;
                            Assert.assertTrue(message, result > 0);
                            break;
                        case -1:
                            message = properties.get(j) + " should be less than or equal to " + value + ". But the property value is " + propertyValue;
                            Assert.assertTrue(message, result >= 0);
                            break;
                        case 0:
                            message = properties.get(j) + " should be equal to than " + value + ". But the property value is " + propertyValue;
                            Assert.assertTrue(message, result == 0);
                            break;
                        case 1:
                            message = properties.get(j) + " should be greate than or equal to " + value + ". But the property value is " + propertyValue;
                            Assert.assertTrue(message, result <= 0);
                            break;
                        case 2:
                            message = properties.get(j) + " should be greater than " + value + ". But the property value is " + propertyValue;
                            Assert.assertTrue(message, result < 0);
                            break;
                    }
                }
            }
        } catch (JSONException e) {
            LOGGER.error("Exception: ", e);
            Assert.fail("An Exception occurred during testing " + properties + ":\n" + e.getMessage());
        }
    }

    private static Object postAndGetId(String urlString, String postContent) {
        HttpResponse responseMap = HTTPMethods.doPost(urlString, postContent);
        String response = responseMap.response;
        return Utils.idObjectFromPostResult(response);
    }

    /**
     * Create entities as a pre-process for testing query options.
     */
    private static void createEntities() {
        try {
            //First Thing
            String urlParameters = "{\n"
                    + "    \"name\": \"thing 1\",\n"
                    + "    \"description\": \"thing 1\",\n"
                    + "    \"properties\": {\n"
                    + "        \"reference\": \"first\"\n"
                    + "    },\n"
                    + "    \"Locations\": [\n"
                    + "        {\n"
                    + "            \"name\": \"location 1\",\n"
                    + "            \"description\": \"location 1\",\n"
                    + "            \"location\": {\n"
                    + "                \"type\": \"Point\",\n"
                    + "                \"coordinates\": [\n"
                    + "                    -117.05,\n"
                    + "                    51.05\n"
                    + "                ]\n"
                    + "            },\n"
                    + "            \"encodingType\": \"application/vnd.geo+json\"\n"
                    + "        }\n"
                    + "    ],\n"
                    + "    \"Datastreams\": [\n"
                    + "        {\n"
                    + "            \"unitOfMeasurement\": {\n"
                    + "                \"name\": \"Lumen\",\n"
                    + "                \"symbol\": \"lm\",\n"
                    + "                \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html/Lumen\"\n"
                    + "            },\n"
                    + "            \"name\": \"datastream 1\",\n"
                    + "            \"description\": \"datastream 1\",\n"
                    + "            \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                    + "            \"ObservedProperty\": {\n"
                    + "                \"name\": \"Luminous Flux\",\n"
                    + "                \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html/LuminousFlux\",\n"
                    + "                \"description\": \"observedProperty 1\"\n"
                    + "            },\n"
                    + "            \"Sensor\": {\n"
                    + "                \"name\": \"sensor 1\",\n"
                    + "                \"description\": \"sensor 1\",\n"
                    + "                \"encodingType\": \"application/pdf\",\n"
                    + "                \"metadata\": \"Light flux sensor\"\n"
                    + "            }\n"
                    + "        },\n"
                    + "        {\n"
                    + "            \"unitOfMeasurement\": {\n"
                    + "                \"name\": \"Centigrade\",\n"
                    + "                \"symbol\": \"C\",\n"
                    + "                \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html/Lumen\"\n"
                    + "            },\n"
                    + "            \"name\": \"datastream 2\",\n"
                    + "            \"description\": \"datastream 2\",\n"
                    + "            \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                    + "            \"ObservedProperty\": {\n"
                    + "                \"name\": \"Tempretaure\",\n"
                    + "                \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html/Tempreture\",\n"
                    + "                \"description\": \"observedProperty 2\"\n"
                    + "            },\n"
                    + "            \"Sensor\": {\n"
                    + "                \"name\": \"sensor 2\",\n"
                    + "                \"description\": \"sensor 2\",\n"
                    + "                \"encodingType\": \"application/pdf\",\n"
                    + "                \"metadata\": \"Tempreture sensor\"\n"
                    + "            }\n"
                    + "        }\n"
                    + "    ]\n"
                    + "}";
            String urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.THING, null, null, null);
            thingId1 = postAndGetId(urlString, urlParameters);

            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.THING, thingId1, EntityType.LOCATION, null);
            HttpResponse responseMap = HTTPMethods.doGet(urlString);
            String response = responseMap.response;
            JSONArray array = new JSONObject(response).getJSONArray("value");
            locationId1 = array.getJSONObject(0).get(ControlInformation.ID);

            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.THING, thingId1, EntityType.DATASTREAM, null);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.response;
            array = new JSONObject(response).getJSONArray("value");
            // We can not assume the Datastreams are returned in the order we expect.
            if ("datastream 1".equals(array.getJSONObject(0).get("name"))) {
                datastreamId1 = array.getJSONObject(0).get(ControlInformation.ID);
                datastreamId2 = array.getJSONObject(1).get(ControlInformation.ID);
            } else {
                datastreamId1 = array.getJSONObject(1).get(ControlInformation.ID);
                datastreamId2 = array.getJSONObject(0).get(ControlInformation.ID);
            }
            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.DATASTREAM, datastreamId1, EntityType.SENSOR, null);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.response;
            sensorId1 = new JSONObject(response).get(ControlInformation.ID);
            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.DATASTREAM, datastreamId1, EntityType.OBSERVED_PROPERTY, null);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.response;
            observedPropertyId1 = new JSONObject(response).get(ControlInformation.ID);

            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.DATASTREAM, datastreamId2, EntityType.SENSOR, null);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.response;
            sensorId2 = new JSONObject(response).get(ControlInformation.ID);
            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.DATASTREAM, datastreamId2, EntityType.OBSERVED_PROPERTY, null);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.response;
            observedPropertyId2 = new JSONObject(response).get(ControlInformation.ID);

            //Second Thing
            urlParameters = "{\n"
                    + "    \"name\": \"thing 2\",\n"
                    + "    \"description\": \"thing 2\",\n"
                    + "    \"properties\": {\n"
                    + "        \"reference\": \"second\"\n"
                    + "    },\n"
                    + "    \"Locations\": [\n"
                    + "        {\n"
                    + "            \"name\": \"location 2\",\n"
                    + "            \"description\": \"location 2\",\n"
                    + "            \"location\": {\n"
                    + "                \"type\": \"Point\",\n"
                    + "                \"coordinates\": [\n"
                    + "                    -100.05,\n"
                    + "                    50.05\n"
                    + "                ]\n"
                    + "            },\n"
                    + "            \"encodingType\": \"application/vnd.geo+json\"\n"
                    + "        }\n"
                    + "    ],\n"
                    + "    \"Datastreams\": [\n"
                    + "        {\n"
                    + "            \"unitOfMeasurement\": {\n"
                    + "                \"name\": \"Lumen\",\n"
                    + "                \"symbol\": \"lm\",\n"
                    + "                \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html/Lumen\"\n"
                    + "            },\n"
                    + "            \"name\": \"datastream 3\",\n"
                    + "            \"description\": \"datastream 3\",\n"
                    + "            \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                    + "            \"ObservedProperty\": {\n"
                    + "                \"name\": \"Second Luminous Flux\",\n"
                    + "                \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html/LuminousFlux\",\n"
                    + "                \"description\": \"observedProperty 3\"\n"
                    + "            },\n"
                    + "            \"Sensor\": {\n"
                    + "                \"name\": \"sensor 3\",\n"
                    + "                \"description\": \"sensor 3\",\n"
                    + "                \"encodingType\": \"application/pdf\",\n"
                    + "                \"metadata\": \"Second Light flux sensor\"\n"
                    + "            }\n"
                    + "        },\n"
                    + "        {\n"
                    + "            \"unitOfMeasurement\": {\n"
                    + "                \"name\": \"Centigrade\",\n"
                    + "                \"symbol\": \"C\",\n"
                    + "                \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html/Lumen\"\n"
                    + "            },\n"
                    + "            \"name\": \"datastream 2\",\n"
                    + "            \"description\": \"datastream 2\",\n"
                    + "            \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                    + "            \"ObservedProperty\": {\n"
                    + "                \"@iot.id\": " + quoteIdForJson(observedPropertyId2) + "\n"
                    + "            },\n"
                    + "            \"Sensor\": {\n"
                    + "                \"name\": \"sensor 4 \",\n"
                    + "                \"description\": \"sensor 4 \",\n"
                    + "                \"encodingType\": \"application/pdf\",\n"
                    + "                \"metadata\": \"Second Tempreture sensor\"\n"
                    + "            }\n"
                    + "        }\n"
                    + "    ]\n"
                    + "}";
            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.THING, null, null, null);
            thingId2 = postAndGetId(urlString, urlParameters);

            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.THING, thingId2, EntityType.LOCATION, null);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.response;
            array = new JSONObject(response).getJSONArray("value");
            locationId2 = array.getJSONObject(0).get(ControlInformation.ID);

            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.THING, thingId2, EntityType.DATASTREAM, null);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.response;
            array = new JSONObject(response).getJSONArray("value");
            // We can not assume the Datastreams are returned in the order we expect.
            if ("datastream 3".equals(array.getJSONObject(0).get("name"))) {
                datastreamId3 = array.getJSONObject(0).get(ControlInformation.ID);
                datastreamId4 = array.getJSONObject(1).get(ControlInformation.ID);
            } else {
                datastreamId4 = array.getJSONObject(0).get(ControlInformation.ID);
                datastreamId3 = array.getJSONObject(1).get(ControlInformation.ID);
            }
            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.DATASTREAM, datastreamId3, EntityType.SENSOR, null);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.response;
            sensorId3 = new JSONObject(response).get(ControlInformation.ID);
            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.DATASTREAM, datastreamId3, EntityType.OBSERVED_PROPERTY, null);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.response;
            observedPropertyId3 = new JSONObject(response).get(ControlInformation.ID);

            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.DATASTREAM, datastreamId4, EntityType.SENSOR, null);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.response;
            sensorId4 = new JSONObject(response).get(ControlInformation.ID);

            //HistoricalLocations
            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.THING, thingId1, null, null);
            urlParameters = "{\"Locations\": [\n"
                    + "    {\n"
                    + "      \"@iot.id\": " + quoteIdForJson(locationId2) + "\n"
                    + "    }\n"
                    + "  ]}";
            HTTPMethods.doPatch(urlString, urlParameters);

            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.THING, thingId2, null, null);
            urlParameters = "{\"Locations\": [\n"
                    + "    {\n"
                    + "      \"@iot.id\": " + quoteIdForJson(locationId1) + "\n"
                    + "    }\n"
                    + "  ]}";
            HTTPMethods.doPatch(urlString, urlParameters);

            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.THING, thingId1, EntityType.HISTORICAL_LOCATION, null);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.response;
            array = new JSONObject(response).getJSONArray("value");
            historicalLocationId1 = array.getJSONObject(0).get(ControlInformation.ID);
            historicalLocationId2 = array.getJSONObject(1).get(ControlInformation.ID);

            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.THING, thingId2, EntityType.HISTORICAL_LOCATION, null);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.response;
            array = new JSONObject(response).getJSONArray("value");
            historicalLocationId3 = array.getJSONObject(0).get(ControlInformation.ID);
            historicalLocationId4 = array.getJSONObject(1).get(ControlInformation.ID);

            //Observations
            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.DATASTREAM, datastreamId1, EntityType.OBSERVATION, null);
            urlParameters = "{\n"
                    + "  \"phenomenonTime\": \"2015-03-01T00:00:00Z\",\n"
                    + "  \"result\": 1 \n"
                    + "   }";
            observationId1 = postAndGetId(urlString, urlParameters);
            urlParameters = "{\n"
                    + "  \"phenomenonTime\": \"2015-03-02T00:00:00Z\",\n"
                    + "  \"result\": 2 \n"
                    + "   }";
            observationId2 = postAndGetId(urlString, urlParameters);
            urlParameters = "{\n"
                    + "  \"phenomenonTime\": \"2015-03-03T00:00:00Z\",\n"
                    + "  \"result\": 3 \n"
                    + "   }";
            observationId3 = postAndGetId(urlString, urlParameters);

            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.DATASTREAM, datastreamId2, EntityType.OBSERVATION, null);
            urlParameters = "{\n"
                    + "  \"phenomenonTime\": \"2015-03-04T00:00:00Z\",\n"
                    + "  \"result\": 4 \n"
                    + "   }";
            observationId4 = postAndGetId(urlString, urlParameters);
            urlParameters = "{\n"
                    + "  \"phenomenonTime\": \"2015-03-05T00:00:00Z\",\n"
                    + "  \"result\": 5 \n"
                    + "   }";
            observationId5 = postAndGetId(urlString, urlParameters);
            urlParameters = "{\n"
                    + "  \"phenomenonTime\": \"2015-03-06T00:00:00Z\",\n"
                    + "  \"result\": 6 \n"
                    + "   }";
            observationId6 = postAndGetId(urlString, urlParameters);

            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.DATASTREAM, datastreamId3, EntityType.OBSERVATION, null);
            urlParameters = "{\n"
                    + "  \"phenomenonTime\": \"2015-03-07T00:00:00Z\",\n"
                    + "  \"result\": 7 \n"
                    + "   }";
            observationId7 = postAndGetId(urlString, urlParameters);
            urlParameters = "{\n"
                    + "  \"phenomenonTime\": \"2015-03-08T00:00:00Z\",\n"
                    + "  \"result\": 8 \n"
                    + "   }";
            observationId8 = postAndGetId(urlString, urlParameters);
            urlParameters = "{\n"
                    + "  \"phenomenonTime\": \"2015-03-09T00:00:00Z\",\n"
                    + "  \"result\": 9 \n"
                    + "   }";
            observationId9 = postAndGetId(urlString, urlParameters);

            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.DATASTREAM, datastreamId4, EntityType.OBSERVATION, null);
            urlParameters = "{\n"
                    + "  \"phenomenonTime\": \"2015-03-10T00:00:00Z\",\n"
                    + "  \"result\": 10 \n"
                    + "   }";
            observationId10 = postAndGetId(urlString, urlParameters);
            urlParameters = "{\n"
                    + "  \"phenomenonTime\": \"2015-03-11T00:00:00Z\",\n"
                    + "  \"result\": 11 \n"
                    + "   }";
            observationId11 = postAndGetId(urlString, urlParameters);
            urlParameters = "{\n"
                    + "  \"phenomenonTime\": \"2015-03-12T00:00:00Z\",\n"
                    + "  \"result\": 12 \n"
                    + "   }";
            observationId12 = postAndGetId(urlString, urlParameters);

            //FeatureOfInterest
            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.OBSERVATION, observationId1, EntityType.FEATURE_OF_INTEREST, null);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.response;
            featureOfInterestId1 = new JSONObject(response).get(ControlInformation.ID);

            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.OBSERVATION, observationId7, EntityType.FEATURE_OF_INTEREST, null);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.response;
            featureOfInterestId2 = new JSONObject(response).get(ControlInformation.ID);

            entityCounts.setGlobalCount(EntityType.DATASTREAM, 4);
            entityCounts.setGlobalCount(EntityType.FEATURE_OF_INTEREST, 2);
            entityCounts.setGlobalCount(EntityType.HISTORICAL_LOCATION, 4);
            entityCounts.setGlobalCount(EntityType.LOCATION, 2);
            entityCounts.setGlobalCount(EntityType.OBSERVATION, 12);
            entityCounts.setGlobalCount(EntityType.OBSERVED_PROPERTY, 3);
            entityCounts.setGlobalCount(EntityType.SENSOR, 4);
            entityCounts.setGlobalCount(EntityType.THING, 2);

            entityCounts.setCount(EntityType.THING, thingId1, EntityType.LOCATION, 1);
            entityCounts.setCount(EntityType.THING, thingId2, EntityType.LOCATION, 1);
            entityCounts.setCount(EntityType.THING, thingId1, EntityType.HISTORICAL_LOCATION, 2);
            entityCounts.setCount(EntityType.THING, thingId2, EntityType.HISTORICAL_LOCATION, 2);
            entityCounts.setCount(EntityType.THING, thingId1, EntityType.DATASTREAM, 2);
            entityCounts.setCount(EntityType.THING, thingId2, EntityType.DATASTREAM, 2);
            entityCounts.setCount(EntityType.LOCATION, locationId1, EntityType.THING, 1);
            entityCounts.setCount(EntityType.LOCATION, locationId2, EntityType.THING, 1);
            entityCounts.setCount(EntityType.LOCATION, locationId1, EntityType.HISTORICAL_LOCATION, 2);
            entityCounts.setCount(EntityType.LOCATION, locationId2, EntityType.HISTORICAL_LOCATION, 2);
            entityCounts.setCount(EntityType.HISTORICAL_LOCATION, historicalLocationId1, EntityType.LOCATION, 1);
            entityCounts.setCount(EntityType.HISTORICAL_LOCATION, historicalLocationId2, EntityType.LOCATION, 1);
            entityCounts.setCount(EntityType.HISTORICAL_LOCATION, historicalLocationId3, EntityType.LOCATION, 1);
            entityCounts.setCount(EntityType.HISTORICAL_LOCATION, historicalLocationId4, EntityType.LOCATION, 1);
            entityCounts.setCount(EntityType.DATASTREAM, datastreamId1, EntityType.OBSERVATION, 3);
            entityCounts.setCount(EntityType.DATASTREAM, datastreamId2, EntityType.OBSERVATION, 3);
            entityCounts.setCount(EntityType.DATASTREAM, datastreamId3, EntityType.OBSERVATION, 3);
            entityCounts.setCount(EntityType.DATASTREAM, datastreamId4, EntityType.OBSERVATION, 3);
            entityCounts.setCount(EntityType.SENSOR, sensorId1, EntityType.DATASTREAM, 1);
            entityCounts.setCount(EntityType.SENSOR, sensorId2, EntityType.DATASTREAM, 1);
            entityCounts.setCount(EntityType.SENSOR, sensorId3, EntityType.DATASTREAM, 1);
            entityCounts.setCount(EntityType.SENSOR, sensorId4, EntityType.DATASTREAM, 1);
            entityCounts.setCount(EntityType.OBSERVED_PROPERTY, observedPropertyId1, EntityType.DATASTREAM, 1);
            entityCounts.setCount(EntityType.OBSERVED_PROPERTY, observedPropertyId2, EntityType.DATASTREAM, 2);
            entityCounts.setCount(EntityType.OBSERVED_PROPERTY, observedPropertyId3, EntityType.DATASTREAM, 1);
            entityCounts.setCount(EntityType.FEATURE_OF_INTEREST, featureOfInterestId1, EntityType.OBSERVATION, 6);
            entityCounts.setCount(EntityType.FEATURE_OF_INTEREST, featureOfInterestId2, EntityType.OBSERVATION, 6);

        } catch (JSONException e) {
            LOGGER.error("Exception: ", e);
            Assert.fail("An Exception occurred during testing!:\n" + e.getMessage());
        }

    }

    /**
     * This method is run after all the tests of this class is run and clean the
     * database.
     */
    @AfterClass
    public static void deleteEverythings() {
        deleteEntityType(EntityType.OBSERVATION);
        deleteEntityType(EntityType.FEATURE_OF_INTEREST);
        deleteEntityType(EntityType.DATASTREAM);
        deleteEntityType(EntityType.SENSOR);
        deleteEntityType(EntityType.OBSERVED_PROPERTY);
        deleteEntityType(EntityType.HISTORICAL_LOCATION);
        deleteEntityType(EntityType.LOCATION);
        deleteEntityType(EntityType.THING);
    }

    /**
     * Delete all the entities of a certain entity type
     *
     * @param entityType The entity type from EntityType enum
     */
    private static void deleteEntityType(EntityType entityType) {
        JSONArray array = null;
        do {
            try {
                String urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, null, null, null);
                HttpResponse responseMap = HTTPMethods.doGet(urlString);
                int responseCode = responseMap.code;
                JSONObject result = new JSONObject(responseMap.response);
                array = result.getJSONArray("value");
                for (int i = 0; i < array.length(); i++) {
                    Object id = array.getJSONObject(i).get(ControlInformation.ID);
                    deleteEntity(entityType, id);
                }
            } catch (JSONException e) {
                LOGGER.error("Exception: ", e);
                Assert.fail("An Exception occurred during testing!:\n" + e.getMessage());
            }
        } while (array.length() > 0);
    }

    /**
     * This method created the URL string for the entity with specific id and
     * then send DELETE request to that URl.
     *
     * @param entityType Entity type in from EntityType enum
     * @param id The id of requested entity
     */
    private static void deleteEntity(EntityType entityType, Object id) {
        String urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, id, null, null);
        HttpResponse responseMap = HTTPMethods.doDelete(urlString);
        int responseCode = responseMap.code;
        String message = "DELETE does not work properly for " + entityType + " with id " + id + ". Returned with response code " + responseCode + ".";
        Assert.assertEquals(message, 200, responseCode);

        responseMap = HTTPMethods.doGet(urlString);
        responseCode = responseMap.code;
        message = "Deleted entity was not actually deleted : " + entityType + "(" + id + ").";
        Assert.assertEquals(message, 404, responseCode);
    }

}
