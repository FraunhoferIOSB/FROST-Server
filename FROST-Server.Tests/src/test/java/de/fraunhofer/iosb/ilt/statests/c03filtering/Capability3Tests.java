/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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
package de.fraunhofer.iosb.ilt.statests.c03filtering;

import static de.fraunhofer.iosb.ilt.statests.util.Utils.quoteForJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.ControlInformation;
import de.fraunhofer.iosb.ilt.statests.util.EntityCounts;
import de.fraunhofer.iosb.ilt.statests.util.EntityPropertiesSampleValue;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods.HttpResponse;
import de.fraunhofer.iosb.ilt.statests.util.Request;
import de.fraunhofer.iosb.ilt.statests.util.ServiceUrlHelper;
import de.fraunhofer.iosb.ilt.statests.util.Utils;
import de.fraunhofer.iosb.ilt.statests.util.model.EntityType;
import de.fraunhofer.iosb.ilt.statests.util.model.Expand;
import de.fraunhofer.iosb.ilt.statests.util.model.PathElement;
import de.fraunhofer.iosb.ilt.statests.util.model.Query;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Includes various tests of "A.2 Filtering Extension" Conformance class.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public abstract class Capability3Tests extends AbstractTestClass {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Capability3Tests.class);

    private enum Compare {
        LT("lt"),
        LE("le"),
        EQ("eq"),
        GE("ge"),
        GT("gt"),
        NE("ne");

        public final String urlOperator;

        private Compare(String urlOperator) {
            this.urlOperator = urlOperator;
        }

    }

    private static Object thingId1, thingId2,
            datastreamId1, datastreamId2, datastreamId3, datastreamId4,
            locationId1, locationId2, historicalLocationId1,
            historicalLocationId2, historicalLocationId3, historicalLocationId4,
            sensorId1, sensorId2, sensorId3, sensorId4,
            observedPropertyId1, observedPropertyId2, observedPropertyId3,
            observationId1, observationId7,
            featureOfInterestId1, featureOfInterestId2;

    private static final EntityCounts ENTITYCOUNTS = new EntityCounts();

    public Capability3Tests(ServerVersion version) {
        super(version);
    }

    @Override
    protected void setUpVersion() {
        LOGGER.info("Setting up for version {}.", version.urlPart);
        createEntities();
    }

    @Override
    protected void tearDownVersion() throws ServiceFailureException {
        EntityUtils.deleteAll(service);
        ENTITYCOUNTS.clear();
    }

    /**
     * This method is run after all the tests of this class is run and clean the
     * database.
     *
     * @throws
     * de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException
     */
    @AfterAll
    public static void tearDown() throws ServiceFailureException {
        LOGGER.info("Tearing down.");
        EntityUtils.deleteAll(service);
        ENTITYCOUNTS.clear();
    }

    /**
     * This method is testing $select query option. It tests $select for
     * collection of entities with 1 level and 2 levels resource path. It also
     * tests $select for one or more properties.
     */
    @Test
    void readEntitiesWithSelectQO() {
        LOGGER.info("  readEntitiesWithSelectQO");
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
    void readEntitiesWithExpand() {
        LOGGER.info("  readEntitiesWithExpand");
        checkExpandForEntityType(EntityType.THING);
        checkExpandForEntityType(EntityType.LOCATION);
        checkExpandForEntityType(EntityType.HISTORICAL_LOCATION);
        checkExpandForEntityType(EntityType.DATASTREAM);
        checkExpandForEntityType(EntityType.SENSOR);
        checkExpandForEntityType(EntityType.OBSERVED_PROPERTY);
        checkExpandForEntityType(EntityType.OBSERVATION);
        checkExpandForEntityType(EntityType.FEATURE_OF_INTEREST);
    }

    @Test
    void readEntitiesWithExpandRelations() {
        LOGGER.info("  readEntitiesWithExpandRelations");
        checkExpandForEntityTypeRelations(EntityType.THING, thingId1);
        checkExpandForEntityTypeRelations(EntityType.LOCATION, locationId1);
        checkExpandForEntityTypeRelations(EntityType.HISTORICAL_LOCATION, historicalLocationId1);
        checkExpandForEntityTypeRelations(EntityType.DATASTREAM, datastreamId1);
        checkExpandForEntityTypeRelations(EntityType.SENSOR, sensorId1);
        checkExpandForEntityTypeRelations(EntityType.OBSERVED_PROPERTY, observedPropertyId1);
        checkExpandForEntityTypeRelations(EntityType.OBSERVATION, observationId1);
        checkExpandForEntityTypeRelations(EntityType.FEATURE_OF_INTEREST, featureOfInterestId1);
    }

    @Test
    void readEntitiesWithExpandMultilevel() {
        LOGGER.info("  readEntitiesWithExpandMultilevel");
        checkExpandForEntityTypeMultilevel(EntityType.THING);
        checkExpandForEntityTypeMultilevel(EntityType.LOCATION);
        checkExpandForEntityTypeMultilevel(EntityType.HISTORICAL_LOCATION);
        checkExpandForEntityTypeMultilevel(EntityType.DATASTREAM);
        checkExpandForEntityTypeMultilevel(EntityType.SENSOR);
        checkExpandForEntityTypeMultilevel(EntityType.OBSERVED_PROPERTY);
        checkExpandForEntityTypeMultilevel(EntityType.OBSERVATION);
        checkExpandForEntityTypeMultilevel(EntityType.FEATURE_OF_INTEREST);
    }

    @Test
    void readEntitiesWithExpandMultilevelRelations() {
        LOGGER.info("  readEntitiesWithExpandMultilevelRelations");
        checkExpandForEntityTypeMultilevelRelations(EntityType.THING, thingId1);
        checkExpandForEntityTypeMultilevelRelations(EntityType.LOCATION, locationId1);
        checkExpandForEntityTypeMultilevelRelations(EntityType.HISTORICAL_LOCATION, historicalLocationId1);
        checkExpandForEntityTypeMultilevelRelations(EntityType.DATASTREAM, datastreamId1);
        checkExpandForEntityTypeMultilevelRelations(EntityType.SENSOR, sensorId1);
        checkExpandForEntityTypeMultilevelRelations(EntityType.OBSERVED_PROPERTY, observedPropertyId1);
        checkExpandForEntityTypeMultilevelRelations(EntityType.OBSERVATION, observationId1);
        checkExpandForEntityTypeMultilevelRelations(EntityType.FEATURE_OF_INTEREST, featureOfInterestId1);
    }

    @Test
    void readEntitiesWithExpandNested() {
        LOGGER.info("  readEntitiesWithExpandNested");
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
    void readEntitiesWithTopQO() {
        LOGGER.info("  readEntitiesWithTopQO");
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
    void readEntitiesWithSkipQO() {
        LOGGER.info("  readEntitiesWithSkipQO");
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
    void readEntitiesWithOrderbyQO() {
        LOGGER.info("  readEntitiesWithOrderbyQO");
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
    void readEntitiesWithCountQO() {
        LOGGER.info("  readEntitiesWithCountQO");
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
    void readEntitiesWithFilterQO() throws UnsupportedEncodingException {
        LOGGER.info("  readEntitiesWithFilterQO");
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
    void checkQueriesPriorityOrdering() {
        LOGGER.info("  checkQueriesPriorityOrdering");
        try {
            String urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.OBSERVATION, null, null, "?$count=true&$top=1&$skip=2&$orderby=phenomenonTime%20asc&$filter=result%20gt%20'3'");
            HttpResponse responseMap = HTTPMethods.doGet(urlString);

            String message = "There is problem for GET Observations using multiple Query Options! HTTP status code: " + responseMap.code;
            assertEquals(200, responseMap.code, message);

            String response = responseMap.response;
            JsonNode array = Utils.MAPPER.readTree(response).get("value");

            message = "The query order of execution is not correct. The expected count is 6. The service returned " + Utils.MAPPER.readTree(response).get("@iot.count").asLong();
            assertEquals(6, Utils.MAPPER.readTree(response).get("@iot.count").asLong(), message);

            message = "The query asked for top 1. The service rerurned " + array.size() + " entities.";
            assertEquals(1, array.size(), message);

            message = "The query order of execution is not correct. The expected Observation result is 6. It is " + array.get(0).get("result").toString();
            assertEquals("6", array.get(0).get("result").toString(), message);
        } catch (IOException e) {
            LOGGER.error("Exception: ", e);
            fail("An Exception occurred during testing!:\n" + e.getMessage());
        }
    }

    /**
     * This method is testing the operator precedence of the AND and OR
     * operators and parenthesis.
     *
     * @throws java.io.UnsupportedEncodingException Should not happen for UTF-8.
     */
    @Test
    void checkAndOrPrecendece() throws UnsupportedEncodingException {
        LOGGER.info("  checkAndOrPrecendece");
        String filter = "$filter=result eq 2 and result eq 1 or result eq 1";
        String fetchError = "There is problem for GET Observations using " + filter;
        String error = filter + "  should return all Observations with a result of 1.";
        String urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.OBSERVATION, null, null, "?" + URLEncoder.encode(filter, "UTF-8"));
        checkResults(urlString, 1, "1", fetchError, error);

        filter = "$filter=(result eq 2 and result eq 1) or result eq 1";
        fetchError = "There is problem for GET Observations using " + filter;
        error = filter + "  should return all Observations with a result of 1.";
        urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.OBSERVATION, null, null, "?" + URLEncoder.encode(filter, "UTF-8"));
        checkResults(urlString, 1, "1", fetchError, error);

        filter = "$filter=result eq 2 and (result eq 1 or result eq 1)";
        fetchError = "There is problem for GET Observations using " + filter;
        error = filter + "  should return no results.";
        urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.OBSERVATION, null, null, "?" + URLEncoder.encode(filter, "UTF-8"));
        checkResults(urlString, 0, "1", fetchError, error);

        filter = "$filter=not result lt 1 and not result gt 1";
        fetchError = "There is problem for GET Observations using " + filter;
        error = filter + "  should return all Observations with a result of 1.";
        urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.OBSERVATION, null, null, "?" + URLEncoder.encode(filter, "UTF-8"));
        checkResults(urlString, 1, "1", fetchError, error);
    }

    /**
     * This method is testing the operator precedence of the ADD, SUB, MUL, DIV
     * and MOD operators and parenthesis.
     *
     * @throws java.io.UnsupportedEncodingException Should not happen for UTF-8.
     */
    @Test
    void checkArithmeticPrecendece() throws UnsupportedEncodingException {
        LOGGER.info("  checkArithmeticPrecendece");
        String filter = "$filter=1 add result mul 2 sub -1 eq 4";
        String fetchError = "There is problem for GET Observations using " + filter;
        String error = filter + "  should return all Observations with a result of 1.";
        String urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.OBSERVATION, null, null, "?" + URLEncoder.encode(filter, "UTF-8"));
        checkResults(urlString, 1, "1", fetchError, error);

        filter = "$filter=6 div 2 sub result eq 2";
        fetchError = "There is problem for GET Observations using " + filter;
        error = filter + "  should return all Observations with a result of 1.";
        urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.OBSERVATION, null, null, "?" + URLEncoder.encode(filter, "UTF-8"));
        checkResults(urlString, 1, "1", fetchError, error);

        filter = "$filter=1 add 2.0 mod (result add 1) eq 1";
        fetchError = "There is problem for GET Observations using " + filter;
        error = filter + "  should return all Observations with a result of 1.";
        urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.OBSERVATION, null, null, "?" + URLEncoder.encode(filter, "UTF-8"));
        checkResults(urlString, 1, "1", fetchError, error);

        filter = "$filter=14 div (result add 1) mod 3 mul 3 eq 3";
        fetchError = "There is problem for GET Observations using " + filter;
        error = filter + "  should return all Observations with a result of 1.";
        urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.OBSERVATION, null, null, "?" + URLEncoder.encode(filter, "UTF-8"));
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
            assertEquals(200, responseMap.code, message);

            String response = responseMap.response;
            JsonNode array = Utils.MAPPER.readTree(response).get("value");
            int length = array.size();

            message = resultError + " Expected " + expectedCount + " Observations. got " + length + ".";
            assertEquals(expectedCount, length, message);

            for (int i = 0; i < length; i++) {
                JsonNode obs = array.get(i);
                String result = obs.get("result").toString();
                String msg = resultError + " The expected Observation result is " + expectedResult + ", but the given result is " + result;
                assertEquals(expectedResult, result, msg);
            }
        } catch (IOException e) {
            LOGGER.error("Exception: ", e);
            fail("An Exception occurred during testing!:\n" + e.getMessage());
        }
    }

    /**
     * This helper method is checking $orderby for 2 level of entities.
     *
     * @param entityType Entity type from EntityType enum list
     */
    private void checkOrderbyForEntityTypeRelations(EntityType entityType) {
        List<String> relations = entityType.getRelations(serverSettings.getExtensions());
        try {
            String urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), entityType, null, null, null);
            HttpResponse responseMap = HTTPMethods.doGet(urlString);
            String response = responseMap.response;
            JsonNode array = Utils.MAPPER.readTree(response).get("value");
            if (array.size() == 0) {
                return;
            }
            Object id = array.get(0).get(ControlInformation.ID);

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
                    urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), entityType, id, relationEntityType, "?$orderby=" + property.name);
                    responseMap = HTTPMethods.doGet(urlString);
                    response = responseMap.response;
                    array = Utils.MAPPER.readTree(response).get("value");
                    for (int i = 1; i < array.size(); i++) {
                        String message = "The ordering is not correct for EntityType " + entityType + " orderby property " + property;
                        compareWithPrevious(i, array, property.name, Compare.LE, message);
                    }
                    urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), entityType, id, relationEntityType, "?$orderby=" + property.name + "%20asc");
                    responseMap = HTTPMethods.doGet(urlString);
                    response = responseMap.response;
                    array = Utils.MAPPER.readTree(response).get("value");
                    for (int i = 1; i < array.size(); i++) {
                        String message = "The ordering is not correct for EntityType " + entityType + " orderby asc property " + property;
                        compareWithPrevious(i, array, property.name, Compare.LE, message);
                    }
                    urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), entityType, id, relationEntityType, "?$orderby=" + property.name + "%20desc");
                    responseMap = HTTPMethods.doGet(urlString);
                    response = responseMap.response;
                    array = Utils.MAPPER.readTree(response).get("value");
                    for (int i = 1; i < array.size(); i++) {
                        String message = "The ordering is not correct for EntityType " + entityType + " orderby desc property " + property;
                        compareWithPrevious(i, array, property.name, Compare.GE, message);
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
                    urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), entityType, id, relationEntityType, orderby);
                    responseMap = HTTPMethods.doGet(urlString);
                    response = responseMap.response;
                    array = Utils.MAPPER.readTree(response).get("value");
                    for (int i = 1; i < array.size(); i++) {
                        for (String orderProperty : orderbyPropeties) {
                            String message = "The ordering is not correct for EntityType " + entityType + " orderby property " + orderProperty;
                            int compare = compareWithPrevious(i, array, orderProperty, Compare.LE, message);
                            if (compare != 0) {
                                break;
                            }
                        }
                    }
                    if (orderbyAsc.charAt(orderbyAsc.length() - 1) != '=') {
                        orderbyAsc += ",";
                    }
                    orderbyAsc += property + "%20asc";
                    urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), entityType, id, relationEntityType, orderbyAsc);
                    responseMap = HTTPMethods.doGet(urlString);
                    response = responseMap.response;
                    array = Utils.MAPPER.readTree(response).get("value");
                    for (int i = 1; i < array.size(); i++) {
                        for (String orderProperty : orderbyPropeties) {
                            String message = "The ordering is not correct for EntityType " + entityType + " orderby asc property " + orderProperty;
                            int compare = compareWithPrevious(i, array, orderProperty, Compare.LE, message);
                            if (compare != 0) {
                                break;
                            }
                        }
                    }
                    if (orderbyDesc.charAt(orderbyDesc.length() - 1) != '=') {
                        orderbyDesc += ",";
                    }
                    orderbyDesc += property + "%20desc";
                    urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), entityType, id, relationEntityType, orderbyDesc);
                    responseMap = HTTPMethods.doGet(urlString);
                    response = responseMap.response;
                    array = Utils.MAPPER.readTree(response).get("value");
                    for (int i = 1; i < array.size(); i++) {
                        for (String orderProperty : orderbyPropeties) {
                            String message = "The ordering is not correct for EntityType " + entityType + " orderby desc property " + orderProperty;
                            int compare = compareWithPrevious(i, array, orderProperty, Compare.GE, message);
                            if (compare != 0) {
                                break;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Exception: ", e);
            fail("An Exception occurred during testing!:\n" + e.getMessage());
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
                String urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), entityType, null, null, "?$orderby=" + property.name);
                HttpResponse responseMap = HTTPMethods.doGet(urlString);
                String response = responseMap.response;
                JsonNode array = Utils.MAPPER.readTree(response).get("value");
                for (int i = 1; i < array.size(); i++) {
                    String msg = "The default ordering is not correct for EntityType " + entityType + " orderby property " + property.name;
                    compareWithPrevious(i, array, property.name, Compare.LE, msg);
                }
                urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), entityType, null, null, "?$orderby=" + property.name + "%20asc");
                responseMap = HTTPMethods.doGet(urlString);
                response = responseMap.response;
                array = Utils.MAPPER.readTree(response).get("value");
                for (int i = 1; i < array.size(); i++) {
                    String msg = "The ascending ordering is not correct for EntityType " + entityType + " orderby asc property " + property.name;
                    compareWithPrevious(i, array, property.name, Compare.LE, msg);
                }
                urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), entityType, null, null, "?$orderby=" + property.name + "%20desc");
                responseMap = HTTPMethods.doGet(urlString);
                response = responseMap.response;
                array = Utils.MAPPER.readTree(response).get("value");
                for (int i = 1; i < array.size(); i++) {
                    String msg = "The descending ordering is not correct for EntityType " + entityType + " orderby desc property " + property.name;
                    compareWithPrevious(i, array, property.name, Compare.GE, msg);
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
                String urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), entityType, null, null, orderby);
                HttpResponse responseMap = HTTPMethods.doGet(urlString);
                String response = responseMap.response;
                JsonNode array = Utils.MAPPER.readTree(response).get("value");
                for (int i = 1; i < array.size(); i++) {
                    for (String orderProperty : orderbyPropeties) {
                        String message = "The ordering is not correct for EntityType " + entityType + " orderby property " + orderProperty;
                        int compare = compareWithPrevious(i, array, orderProperty, Compare.LE, message);
                        if (compare != 0) {
                            break;
                        }
                    }
                }
                if (orderbyAsc.charAt(orderbyAsc.length() - 1) != '=') {
                    orderbyAsc += ",";
                }
                orderbyAsc += property + "%20asc";
                urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), entityType, null, null, orderbyAsc);
                responseMap = HTTPMethods.doGet(urlString);
                response = responseMap.response;
                array = Utils.MAPPER.readTree(response).get("value");
                for (int i = 1; i < array.size(); i++) {
                    for (String orderProperty : orderbyPropeties) {
                        String message = "The ordering is not correct for EntityType " + entityType + " orderby asc property " + orderProperty;
                        int compare = compareWithPrevious(i, array, orderProperty, Compare.LE, message);
                        if (compare != 0) {
                            break;
                        }
                    }
                }
                if (orderbyDesc.charAt(orderbyDesc.length() - 1) != '=') {
                    orderbyDesc += ",";
                }
                orderbyDesc += property + "%20desc";
                urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), entityType, null, null, orderbyDesc);
                responseMap = HTTPMethods.doGet(urlString);
                response = responseMap.response;
                array = Utils.MAPPER.readTree(response).get("value");
                for (int i = 1; i < array.size(); i++) {
                    for (String orderProperty : orderbyPropeties) {
                        String message = "The ordering is not correct for EntityType " + entityType + " orderby desc property " + orderProperty;
                        int compare = compareWithPrevious(i, array, orderProperty, Compare.GE, message);
                        if (compare != 0) {
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Exception: ", e);
            fail("An Exception occurred during testing " + entityType + ":\n" + e.getMessage());
        }

    }

    private int compareWithPrevious(int idx, JsonNode array, String property, Compare order, String message) throws IOException {
        JsonNode jObj1 = array.get(idx - 1);
        JsonNode jObj2 = array.get(idx);
        int result = compareObjects(property, jObj1, jObj2, order, message);
        return result;
    }

    private int compareObjects(String property, JsonNode jObj1, JsonNode jObj2, Compare order, String message) throws IOException {
        int result;
        Object o1 = null;
        Object o2 = null;
        if (jObj1.has(property)) {
            o1 = Utils.MAPPER.treeToValue(jObj1.get(property), Object.class);
        }
        if (jObj2.has(property)) {
            o2 = Utils.MAPPER.treeToValue(jObj2.get(property), Object.class);
        }
        if (o1 == null || o2 == null || o1 instanceof NullNode || o2 instanceof NullNode) {
            // One of the two does not have the property, or undefined?
            result = 0;
        } else {
            result = compareForOrder(property, o1, o2);
        }
        String fullMessage = message + " Checking: '" + Objects.toString(o1) + "' " + order + " '" + Objects.toString(o2) + "'";
        switch (order) {
            case LT:
                assertTrue(result < 0, fullMessage);
                break;
            case LE:
                assertTrue(result <= 0, fullMessage);
                break;
            case EQ:
                assertEquals(0, result, fullMessage);
                break;
            case GE:
                assertTrue(result >= 0, fullMessage);
                break;
            case GT:
                assertTrue(result > 0, fullMessage);
                break;
            default:
                throw new AssertionError(order.name());
        }
        return result;
    }

    private int compareForOrder(String property, Object o1, Object o2) {
        if (property.toLowerCase().endsWith("time")) {
            String t1s = Objects.toString(o1);
            String t2s = Objects.toString(o2);
            if (t1s.contains("/")) {
                String[] t1Parts = t1s.split("/");
                String[] t2Parts = t2s.split("/");
                int result = ZonedDateTime.parse(t1Parts[0]).compareTo(ZonedDateTime.parse(t2Parts[0]));
                if (result != 0) {
                    return result;
                }
                return ZonedDateTime.parse(t1Parts[1]).compareTo(ZonedDateTime.parse(t2Parts[1]));
            } else {
                final ZonedDateTime t1 = ZonedDateTime.parse(t1s);
                final ZonedDateTime t2 = ZonedDateTime.parse(t2s);
                return t1.compareTo(t2);
            }
        }
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
        Request request = new Request(serverSettings.getServiceUrl(version));
        request.addElement(new PathElement(entityType.plural));
        // in case an implementation returns fewer than 12 entities by default we request 12.
        request.getQuery()
                .setTop(12L)
                .setSkip(1L);
        EntityUtils.checkResponse(serverSettings.getExtensions(), request.executeGet(), request, ENTITYCOUNTS);

        request.getQuery().setSkip(2L);
        EntityUtils.checkResponse(serverSettings.getExtensions(), request.executeGet(), request, ENTITYCOUNTS);

        request.getQuery().setSkip(3L);
        EntityUtils.checkResponse(serverSettings.getExtensions(), request.executeGet(), request, ENTITYCOUNTS);

        request.getQuery().setSkip(4L);
        EntityUtils.checkResponse(serverSettings.getExtensions(), request.executeGet(), request, ENTITYCOUNTS);

        request.getQuery().setSkip(12L);
        EntityUtils.checkResponse(serverSettings.getExtensions(), request.executeGet(), request, ENTITYCOUNTS);
    }

    /**
     * This helper method is checking $skip for 2 level of entities.
     *
     * @param entityType Entity type from EntityType enum list
     */
    private void checkSkipForEntityTypeRelation(EntityType entityType, Object entityId) {
        List<String> relations = entityType.getRelations(serverSettings.getExtensions());
        for (String relation : relations) {
            if (!EntityType.isPlural(relation)) {
                continue;
            }
            Request request = new Request(serverSettings.getServiceUrl(version));
            request.addElement(new PathElement(entityType.plural, entityId))
                    .addElement(new PathElement(relation));
            // in case an implementation returns fewer than 12 entities by default we request 12.
            request.getQuery()
                    .setTop(12L)
                    .setSkip(1L);
            JsonNode response = request.executeGet();
            EntityUtils.checkResponse(serverSettings.getExtensions(), response, request, ENTITYCOUNTS);
        }
    }

    /**
     * This helper method is checking $top for a collection.
     *
     * @param entityType Entity type from EntityType enum list
     */
    private void checkTopForEntityType(EntityType entityType) {
        Request request = new Request(serverSettings.getServiceUrl(version));
        request.addElement(new PathElement(entityType.plural));
        request.getQuery().setTop(1L);
        EntityUtils.checkResponse(serverSettings.getExtensions(), request.executeGet(), request, ENTITYCOUNTS);

        request.getQuery().setTop(2L);
        EntityUtils.checkResponse(serverSettings.getExtensions(), request.executeGet(), request, ENTITYCOUNTS);

        request.getQuery().setTop(3L);
        EntityUtils.checkResponse(serverSettings.getExtensions(), request.executeGet(), request, ENTITYCOUNTS);

        request.getQuery().setTop(4L);
        EntityUtils.checkResponse(serverSettings.getExtensions(), request.executeGet(), request, ENTITYCOUNTS);

        request.getQuery().setTop(5L);
        EntityUtils.checkResponse(serverSettings.getExtensions(), request.executeGet(), request, ENTITYCOUNTS);

        request.getQuery().setTop(12L);
        EntityUtils.checkResponse(serverSettings.getExtensions(), request.executeGet(), request, ENTITYCOUNTS);

        request.getQuery().setTop(13L);
        EntityUtils.checkResponse(serverSettings.getExtensions(), request.executeGet(), request, ENTITYCOUNTS);
    }

    /**
     * This helper method is checking $top for 2 level of entities.
     *
     * @param entityType Entity type from EntityType enum list
     */
    private void checkTopForEntityTypeRelation(EntityType entityType, Object entityId) {
        List<String> relations = entityType.getRelations(serverSettings.getExtensions());
        for (String relation : relations) {
            if (!EntityType.isPlural(relation)) {
                continue;
            }
            Request request = new Request(serverSettings.getServiceUrl(version));
            request.addElement(new PathElement(entityType.plural, entityId))
                    .addElement(new PathElement(relation));
            Query query = request.getQuery();
            query.setTop(3L);
            JsonNode response = request.executeGet();
            EntityUtils.checkResponse(serverSettings.getExtensions(), response, request, ENTITYCOUNTS);
        }
    }

    /**
     * This helper method is checking $select for a collection.
     *
     * @param entityType Entity type from EntityType enum list
     */
    private void checkSelectForEntityType(EntityType entityType) {
        List<EntityType.EntityProperty> properties = entityType.getProperties();
        checkSelectSingleProperty(entityType, properties);

        checkSelectMultipleProperties(entityType, properties);

        checkSelectNavigationProperty(entityType);
    }

    private void checkSelectSingleProperty(EntityType entityType, List<EntityType.EntityProperty> properties) {
        for (EntityType.EntityProperty property : properties) {
            Request request = new Request(serverSettings.getServiceUrl(version));
            request.addElement(new PathElement(entityType.plural));
            request.getQuery().addSelect(property.name);
            JsonNode response = request.executeGet();
            EntityUtils.checkResponse(serverSettings.getExtensions(), response, request, ENTITYCOUNTS);
        }
    }

    private void checkSelectMultipleProperties(EntityType entityType, List<EntityType.EntityProperty> properties) {
        Request request = new Request(serverSettings.getServiceUrl(version));
        request.addElement(new PathElement(entityType.plural));
        for (EntityType.EntityProperty property : properties) {
            request.getQuery().addSelect(property.name);
            JsonNode response = request.executeGet();
            EntityUtils.checkResponse(serverSettings.getExtensions(), response, request, ENTITYCOUNTS);
        }
    }

    private void checkSelectNavigationProperty(EntityType entityType) {
        // Check only navigation links
        List<String> relations = entityType.getRelations(serverSettings.getExtensions());
        for (String relation : relations) {
            Request request = new Request(serverSettings.getServiceUrl(version));
            request.addElement(new PathElement(entityType.plural));
            request.getQuery().addSelect(relation);
            JsonNode response = request.executeGet();
            EntityUtils.checkResponse(serverSettings.getExtensions(), response, request, ENTITYCOUNTS);
        }
    }

    /**
     * This helper method is checking $select for 2 level of entities.
     *
     * @param entityType Entity type from EntityType enum list
     */
    private void checkSelectForEntityTypeRelations(EntityType entityType, Object entityId) {
        List<String> parentRelations = entityType.getRelations(serverSettings.getExtensions());
        for (String parentRelation : parentRelations) {
            EntityType relationEntityType = EntityType.getForRelation(parentRelation);
            List<EntityType.EntityProperty> properties = relationEntityType.getProperties();
            for (EntityType.EntityProperty property : properties) {
                Request request = new Request(serverSettings.getServiceUrl(version));
                request.addElement(new PathElement(entityType.plural, entityId));
                request.addElement(new PathElement(parentRelation));

                request.getQuery().addSelect(property.name);
                JsonNode response = request.executeGet();
                EntityUtils.checkResponse(serverSettings.getExtensions(), response, request, ENTITYCOUNTS);
            }

            Request request = new Request(serverSettings.getServiceUrl(version));
            request.addElement(new PathElement(entityType.plural, entityId));
            request.addElement(new PathElement(parentRelation));
            for (EntityType.EntityProperty property : properties) {
                request.getQuery().addSelect(property.name);
                JsonNode response = request.executeGet();
                EntityUtils.checkResponse(serverSettings.getExtensions(), response, request, ENTITYCOUNTS);
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
        List<String> relations = entityType.getRelations(serverSettings.getExtensions());
        for (String relation : relations) {
            Request request = new Request(serverSettings.getServiceUrl(version));
            request.addElement(new PathElement(entityType.plural));
            request.getQuery().addExpand(new Expand().addElement(new PathElement(relation)));
            JsonNode response = request.executeGet();
            EntityUtils.checkResponse(serverSettings.getExtensions(), response, request, ENTITYCOUNTS);
        }

        Request request = new Request(serverSettings.getServiceUrl(version));
        request.addElement(new PathElement(entityType.plural));
        for (String relation : relations) {
            request.getQuery().addExpand(new Expand().addElement(new PathElement(relation)));
            JsonNode response = request.executeGet();
            EntityUtils.checkResponse(serverSettings.getExtensions(), response, request, ENTITYCOUNTS);
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
        List<String> parentRelations = entityType.getRelations(serverSettings.getExtensions());
        for (String parentRelation : parentRelations) {
            EntityType parentRelationEntityType = EntityType.getForRelation(parentRelation);
            PathElement parentRelationPathElement = new PathElement(parentRelation);
            List<String> relations = parentRelationEntityType.getRelations(serverSettings.getExtensions());
            for (String relation : relations) {
                Request request = new Request(serverSettings.getServiceUrl(version));
                request.addElement(entityPathElement);
                request.addElement(parentRelationPathElement);
                request.getQuery().addExpand(new Expand().addElement(new PathElement(relation)));
                JsonNode response = request.executeGet();
                EntityUtils.checkResponse(serverSettings.getExtensions(), response, request, ENTITYCOUNTS);
            }

            Request request = new Request(serverSettings.getServiceUrl(version));
            request.addElement(entityPathElement);
            request.addElement(parentRelationPathElement);
            for (String relation : relations) {
                request.getQuery().addExpand(new Expand().addElement(new PathElement(relation)));
                JsonNode response = request.executeGet();
                EntityUtils.checkResponse(serverSettings.getExtensions(), response, request, ENTITYCOUNTS);
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
        List<String> parentRelations = entityType.getRelations(serverSettings.getExtensions());
        for (String parentRelation : parentRelations) {
            EntityType parentRelationEntityType = EntityType.getForRelation(parentRelation);
            PathElement parentRelationPathElement = new PathElement(parentRelation);

            List<String> relations = parentRelationEntityType.getRelations(serverSettings.getExtensions());
            for (String relation : relations) {
                EntityType relationType = EntityType.getForRelation(relation);
                List<String> secondLevelRelations = relationType.getRelations(serverSettings.getExtensions());
                for (String secondLevelRelation : secondLevelRelations) {
                    Request request = new Request(serverSettings.getServiceUrl(version));
                    request.addElement(entityPathElement);
                    request.addElement(parentRelationPathElement);
                    Expand expand = new Expand()
                            .addElement(new PathElement(relation))
                            .addElement(new PathElement(secondLevelRelation));
                    request.getQuery().addExpand(expand);
                    JsonNode response = request.executeGet();
                    request.reNest();
                    EntityUtils.checkResponse(serverSettings.getExtensions(), response, request, ENTITYCOUNTS);
                }
            }
            Request request = new Request(serverSettings.getServiceUrl(version));
            request.addElement(entityPathElement);
            request.addElement(parentRelationPathElement);

            for (String relation : relations) {
                EntityType relationType = EntityType.getForRelation(relation);
                List<String> secondLevelRelations = relationType.getRelations(serverSettings.getExtensions());
                for (String secondLevelRelation : secondLevelRelations) {
                    Expand expand = new Expand()
                            .addElement(new PathElement(relation))
                            .addElement(new PathElement(secondLevelRelation));
                    request.getQuery().addExpand(expand);
                    JsonNode response = request.executeGet();
                    EntityUtils.checkResponse(serverSettings.getExtensions(), response, request.clone().reNest(), ENTITYCOUNTS);
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
        List<String> relations = entityType.getRelations(serverSettings.getExtensions());
        for (String relation : relations) {
            EntityType relationType = EntityType.getForRelation(relation);
            List<String> secondLevelRelations = relationType.getRelations(serverSettings.getExtensions());

            for (String secondLevelRelation : secondLevelRelations) {
                Request request = new Request(serverSettings.getServiceUrl(version));
                request.addElement(new PathElement(entityType.plural));
                Expand expand = new Expand()
                        .addElement(new PathElement(relation))
                        .addElement(new PathElement(secondLevelRelation));
                request.getQuery().addExpand(expand);
                JsonNode response = request.executeGet();
                request.reNest();
                EntityUtils.checkResponse(serverSettings.getExtensions(), response, request, ENTITYCOUNTS);
            }
        }

        Request request = new Request(serverSettings.getServiceUrl(version));
        request.addElement(new PathElement(entityType.plural));
        for (String relation : relations) {
            EntityType relationType = EntityType.getForRelation(relation);
            List<String> secondLevelRelations = relationType.getRelations(serverSettings.getExtensions());
            for (String secondLevelRelation : secondLevelRelations) {
                Expand expand = new Expand()
                        .addElement(new PathElement(relation))
                        .addElement(new PathElement(secondLevelRelation));
                request.getQuery().addExpand(expand);
                JsonNode response = request.executeGet();
                EntityUtils.checkResponse(serverSettings.getExtensions(), response, request.clone().reNest(), ENTITYCOUNTS);
            }
        }
    }

    /**
     * This helper method is checking nested expands two levels deep including
     * select, top, skip and count options. For instance:
     *
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
        Request request2 = new Request(serverSettings.getServiceUrl(version));
        request2.addElement(collectionPathElement);
        boolean even = true;
        long skip = 0;

        List<String> parentRelations = entityType.getRelations(serverSettings.getExtensions());
        for (String parentRelation : parentRelations) {
            EntityType parentRelationEntityType = EntityType.getForRelation(parentRelation);
            List<String> childRelations = parentRelationEntityType.getRelations(serverSettings.getExtensions());
            for (String childRelation : childRelations) {
                EntityType childRelationEntityType = EntityType.getForRelation(childRelation);
                Request request = new Request(serverSettings.getServiceUrl(version));
                request.addElement(entityPathElement);
                Query query = request.getQuery();
                entityType.getHalfPropertiesRelations(serverSettings.getExtensions(), query.getSelect(), even);
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
                parentRelationEntityType.getHalfPropertiesRelations(serverSettings.getExtensions(), query.getSelect(), even);
                expand = new Expand()
                        .addElement(new PathElement(childRelation));
                query.addExpand(expand);
                even = !even;
                skip = 1 - skip;

                query = expand.getQuery();
                childRelationEntityType.getHalfPropertiesRelations(serverSettings.getExtensions(), query.getSelect(), even);
                query.setCount(even);
                query.setTop(2L);
                query.setSkip(skip);
                even = !even;
                skip = 1 - skip;

                JsonNode response = request.executeGet();
                EntityUtils.checkResponse(serverSettings.getExtensions(), response, request, ENTITYCOUNTS);

                request.getPath().clear();
                request.addElement(collectionPathElement);
                response = request.executeGet();
                EntityUtils.checkResponse(serverSettings.getExtensions(), response, request, ENTITYCOUNTS);
            }

            Query query1 = request2.getQuery();
            Expand expand = new Expand()
                    .addElement(new PathElement(parentRelation));
            query1.addExpand(expand);
            entityType.getHalfPropertiesRelations(serverSettings.getExtensions(), query1.getSelect(), even);
            query1.setCount(even);
            even = !even;

            Query query2 = expand.getQuery();
            for (String childRelation : childRelations) {
                parentRelationEntityType.getHalfPropertiesRelations(serverSettings.getExtensions(), query2.getSelect(), even);
                query2.setCount(even);
                EntityType childRelationEntityType = EntityType.getForRelation(childRelation);
                expand = new Expand()
                        .addElement(new PathElement(childRelation));
                query2.addExpand(expand);
                even = !even;

                Query query3 = expand.getQuery();
                childRelationEntityType.getHalfPropertiesRelations(serverSettings.getExtensions(), query3.getSelect(), even);
                query3.setCount(even);
                even = !even;

                JsonNode response = request2.executeGet();
                EntityUtils.checkResponse(serverSettings.getExtensions(), response, request2, ENTITYCOUNTS);
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
        Request request = new Request(serverSettings.getServiceUrl(version));
        request.addElement(new PathElement(entityType.plural));
        request.getQuery()
                .setCount(true)
                .setTop(1l);
        final JsonNode response = request.executeGet();
        String nextLink = EntityUtils.checkResponse(serverSettings.getExtensions(), response, request, ENTITYCOUNTS);

        Assertions.assertNotNull(nextLink, "NextLink should not have been null for " + entityType);

        Request nextRequest = new Request(nextLink);
        nextRequest.setEntityType(request.getEntityType());
        EntityUtils.checkResponse(serverSettings.getExtensions(), nextRequest.executeGet(), nextRequest, ENTITYCOUNTS);

        request.getQuery().setCount(false);
        EntityUtils.checkResponse(serverSettings.getExtensions(), request.executeGet(), request, ENTITYCOUNTS);
    }

    /**
     * This helper method is checking $count for 2 level of entities.
     *
     * @param entityType Entity type from EntityType enum list
     */
    private void checkCountForEntityTypeRelations(EntityType entityType, Object entityId) {
        List<String> relations = entityType.getRelations(serverSettings.getExtensions());
        for (String relation : relations) {
            if (!EntityType.isPlural(relation)) {
                continue;
            }
            Request request = new Request(serverSettings.getServiceUrl(version));
            request.addElement(new PathElement(entityType.plural, entityId))
                    .addElement(new PathElement(relation));
            Query query = request.getQuery();
            query.setCount(true);
            EntityUtils.checkResponse(serverSettings.getExtensions(), request.executeGet(), request, ENTITYCOUNTS);

            query.setCount(false);
            EntityUtils.checkResponse(serverSettings.getExtensions(), request.executeGet(), request, ENTITYCOUNTS);
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

            for (Compare operator : Compare.values()) {
                String urlString = ServiceUrlHelper.buildURLString(
                        serverSettings.getServiceUrl(version),
                        entityType, null, null,
                        "?$filter=" + property.name + "%20" + operator.urlOperator + "%20" + propertyValue);
                HttpResponse responseMap = HTTPMethods.doGet(urlString);
                String response = responseMap.response;
                checkPropertiesForFilter(response, filteredProperties, samplePropertyValues, operator);
            }

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
        List<String> relations = entityType.getRelations(serverSettings.getExtensions());
        String urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), entityType, null, null, null);
        HttpResponse responseMap = HTTPMethods.doGet(urlString);
        String response = responseMap.response;
        JsonNode array = null;
        try {
            array = Utils.MAPPER.readTree(response).get("value");
        } catch (IOException e) {
            LOGGER.error("Exception: ", e);
            fail("An Exception occurred during testing!:\n" + e.getMessage());
        }
        if (array.size() == 0) {
            return;
        }
        Object id;
        id = array.get(0).get(ControlInformation.ID);

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

                for (Compare operator : Compare.values()) {
                    urlString = ServiceUrlHelper.buildURLString(
                            serverSettings.getServiceUrl(version),
                            entityType, id, relationEntityType,
                            "?$filter=" + property.name + "%20" + operator.urlOperator + "%20" + propertyValue);
                    responseMap = HTTPMethods.doGet(urlString);
                    response = responseMap.response;
                    checkPropertiesForFilter(response, filteredProperties, samplePropertyValues, operator);
                }
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
    private void checkPropertiesForFilter(String response, List<String> properties, List<Comparable> values, Compare operator) {
        try {
            JsonNode entities = Utils.MAPPER.readTree(response);
            JsonNode entityArray = entities.get("value");
            for (JsonNode entity : entityArray) {
                for (int j = 0; j < properties.size(); j++) {
                    JsonNode propertyNode = entity.get(properties.get(j));
                    if (propertyNode == null) {
                        fail("The entity has null value for property " + properties.get(j));
                    }
                    Object propertyValue = Utils.MAPPER.treeToValue(propertyNode, Object.class);
                    Comparable value = values.get(j);
                    if (value instanceof String && ((String) value).charAt(0) == '\'') {
                        String sValue = (String) value;
                        value = sValue.substring(1, sValue.length() - 1);
                        if (!(propertyValue instanceof String)) {
                            propertyValue = propertyValue.toString();
                        }
                    } else if (value instanceof ZonedDateTime) {
                        propertyValue = ZonedDateTime.parse(propertyValue.toString());
                    }

                    int result = value.compareTo(propertyValue);
                    switch (operator) {
                        case NE:
                            String message = properties.get(j) + " should not be equal to " + value + ". But the property value is " + propertyValue;
                            assertNotEquals(0, result, message);
                            break;
                        case LT:
                            message = properties.get(j) + " should be less than " + value + ". But the property value is " + propertyValue;
                            assertTrue(result > 0, message);
                            break;
                        case LE:
                            message = properties.get(j) + " should be less than or equal to " + value + ". But the property value is " + propertyValue;
                            assertTrue(result >= 0, message);
                            break;
                        case EQ:
                            message = properties.get(j) + " should be equal to " + value + ". But the property value is " + propertyValue;
                            assertEquals(0, result, message);
                            break;
                        case GE:
                            message = properties.get(j) + " should be greate than or equal to " + value + ". But the property value is " + propertyValue;
                            assertTrue(result <= 0, message);
                            break;
                        case GT:
                            message = properties.get(j) + " should be greater than " + value + ". But the property value is " + propertyValue;
                            assertTrue(result < 0, message);
                            break;
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Exception: ", e);
            fail("An Exception occurred during testing " + properties + ":\n" + e.getMessage());
        }
    }

    private static Object postAndGetId(String urlString, String postContent) {
        HttpResponse responseMap = HTTPMethods.doPost(urlString, postContent);
        String response = responseMap.response;
        return Utils.pkFromPostResult(response)[0];
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
            String urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.THING, null, null, null);
            thingId1 = postAndGetId(urlString, urlParameters);

            urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.THING, thingId1, EntityType.LOCATION, null);
            HttpResponse responseMap = HTTPMethods.doGet(urlString);
            String response = responseMap.response;
            JsonNode array = Utils.MAPPER.readTree(response).get("value");
            locationId1 = array.get(0).get(ControlInformation.ID);

            urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.THING, thingId1, EntityType.DATASTREAM, null);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.response;
            array = Utils.MAPPER.readTree(response).get("value");
            // We can not assume the Datastreams are returned in the order we expect.
            if ("datastream 1".equals(array.get(0).get("name").asText())) {
                datastreamId1 = array.get(0).get(ControlInformation.ID);
                datastreamId2 = array.get(1).get(ControlInformation.ID);
            } else {
                datastreamId1 = array.get(1).get(ControlInformation.ID);
                datastreamId2 = array.get(0).get(ControlInformation.ID);
            }
            urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.DATASTREAM, datastreamId1, EntityType.SENSOR, null);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.response;
            sensorId1 = Utils.MAPPER.readTree(response).get(ControlInformation.ID);
            urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.DATASTREAM, datastreamId1, EntityType.OBSERVED_PROPERTY, null);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.response;
            observedPropertyId1 = Utils.MAPPER.readTree(response).get(ControlInformation.ID);

            urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.DATASTREAM, datastreamId2, EntityType.SENSOR, null);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.response;
            sensorId2 = Utils.MAPPER.readTree(response).get(ControlInformation.ID);
            urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.DATASTREAM, datastreamId2, EntityType.OBSERVED_PROPERTY, null);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.response;
            observedPropertyId2 = Utils.MAPPER.readTree(response).get(ControlInformation.ID);

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
                    + "            \"name\": \"datastream 4\",\n"
                    + "            \"description\": \"datastream 4\",\n"
                    + "            \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                    + "            \"ObservedProperty\": {\n"
                    + "                \"@iot.id\": " + quoteForJson(observedPropertyId2) + "\n"
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
            urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.THING, null, null, null);
            thingId2 = postAndGetId(urlString, urlParameters);

            urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.THING, thingId2, EntityType.LOCATION, null);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.response;
            array = Utils.MAPPER.readTree(response).get("value");
            locationId2 = array.get(0).get(ControlInformation.ID);

            urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.THING, thingId2, EntityType.DATASTREAM, null);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.response;
            array = Utils.MAPPER.readTree(response).get("value");
            // We can not assume the Datastreams are returned in the order we expect.
            if ("datastream 3".equals(array.get(0).get("name").asText())) {
                datastreamId3 = array.get(0).get(ControlInformation.ID);
                datastreamId4 = array.get(1).get(ControlInformation.ID);
            } else {
                datastreamId4 = array.get(0).get(ControlInformation.ID);
                datastreamId3 = array.get(1).get(ControlInformation.ID);
            }
            urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.DATASTREAM, datastreamId3, EntityType.SENSOR, null);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.response;
            sensorId3 = Utils.MAPPER.readTree(response).get(ControlInformation.ID);
            urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.DATASTREAM, datastreamId3, EntityType.OBSERVED_PROPERTY, null);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.response;
            observedPropertyId3 = Utils.MAPPER.readTree(response).get(ControlInformation.ID);

            urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.DATASTREAM, datastreamId4, EntityType.SENSOR, null);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.response;
            sensorId4 = Utils.MAPPER.readTree(response).get(ControlInformation.ID);

            //HistoricalLocations
            urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.THING, thingId1, null, null);
            urlParameters = "{\"Locations\": [\n"
                    + "    {\n"
                    + "      \"@iot.id\": " + quoteForJson(locationId2) + "\n"
                    + "    }\n"
                    + "  ]}";
            HTTPMethods.doPatch(urlString, urlParameters);

            urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.THING, thingId2, null, null);
            urlParameters = "{\"Locations\": [\n"
                    + "    {\n"
                    + "      \"@iot.id\": " + quoteForJson(locationId1) + "\n"
                    + "    }\n"
                    + "  ]}";
            HTTPMethods.doPatch(urlString, urlParameters);

            urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.THING, thingId1, EntityType.HISTORICAL_LOCATION, null);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.response;
            array = Utils.MAPPER.readTree(response).get("value");
            historicalLocationId1 = array.get(0).get(ControlInformation.ID);
            historicalLocationId2 = array.get(1).get(ControlInformation.ID);

            urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.THING, thingId2, EntityType.HISTORICAL_LOCATION, null);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.response;
            array = Utils.MAPPER.readTree(response).get("value");
            historicalLocationId3 = array.get(0).get(ControlInformation.ID);
            historicalLocationId4 = array.get(1).get(ControlInformation.ID);

            //Observations
            urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.DATASTREAM, datastreamId1, EntityType.OBSERVATION, null);
            urlParameters = """
                    {
                        "phenomenonTime": "2015-03-01T00:00:00Z",
                        "result": 1
                    }""";
            observationId1 = postAndGetId(urlString, urlParameters);
            urlParameters = """
                    {
                        "phenomenonTime": "2015-03-02T00:00:00Z",
                        "result": 2
                    }""";
            postAndGetId(urlString, urlParameters);
            urlParameters = """
                    {
                        "phenomenonTime": "2015-03-03T00:00:00Z",
                        "result": 3
                    }""";
            postAndGetId(urlString, urlParameters);

            urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.DATASTREAM, datastreamId2, EntityType.OBSERVATION, null);
            urlParameters = """
                    {
                        "phenomenonTime": "2015-03-04T00:00:00Z",
                        "result": 4
                    }""";
            postAndGetId(urlString, urlParameters);
            urlParameters = """
                    {
                        "phenomenonTime": "2015-03-05T00:00:00Z",
                        "result": 5
                    }""";
            postAndGetId(urlString, urlParameters);
            urlParameters = """
                    {
                        "phenomenonTime": "2015-03-06T00:00:00Z",
                        "result": 6
                    }""";
            postAndGetId(urlString, urlParameters);

            urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.DATASTREAM, datastreamId3, EntityType.OBSERVATION, null);
            urlParameters = """
                    {
                        "phenomenonTime": "2015-03-07T00:00:00Z",
                        "result": 7
                    }""";
            observationId7 = postAndGetId(urlString, urlParameters);
            urlParameters = """
                    {
                        "phenomenonTime": "2015-03-08T00:00:00Z",
                        "result": 8
                    }""";
            postAndGetId(urlString, urlParameters);
            urlParameters = """
                    {
                        "phenomenonTime": "2015-03-09T00:00:00Z",
                        "result": 9
                    }""";
            postAndGetId(urlString, urlParameters);

            urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.DATASTREAM, datastreamId4, EntityType.OBSERVATION, null);
            urlParameters = """
                    {
                        "phenomenonTime": "2015-03-10T00:00:00Z",
                       "result": 10
                    }""";
            postAndGetId(urlString, urlParameters);
            urlParameters = """
                    {
                        "phenomenonTime": "2015-03-11T00:00:00Z",
                        "result": 11
                    }""";
            postAndGetId(urlString, urlParameters);
            urlParameters = """
                    {
                        "phenomenonTime": "2015-03-12T00:00:00Z",
                        "result": 12
                    }""";
            postAndGetId(urlString, urlParameters);

            //FeatureOfInterest
            urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.OBSERVATION, observationId1, EntityType.FEATURE_OF_INTEREST, null);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.response;
            featureOfInterestId1 = Utils.MAPPER.readTree(response).get(ControlInformation.ID);

            urlString = ServiceUrlHelper.buildURLString(serverSettings.getServiceUrl(version), EntityType.OBSERVATION, observationId7, EntityType.FEATURE_OF_INTEREST, null);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.response;
            featureOfInterestId2 = Utils.MAPPER.readTree(response).get(ControlInformation.ID);

            ENTITYCOUNTS.setGlobalCount(EntityType.DATASTREAM, 4);
            ENTITYCOUNTS.setGlobalCount(EntityType.FEATURE_OF_INTEREST, 2);
            ENTITYCOUNTS.setGlobalCount(EntityType.HISTORICAL_LOCATION, 4);
            ENTITYCOUNTS.setGlobalCount(EntityType.LOCATION, 2);
            ENTITYCOUNTS.setGlobalCount(EntityType.OBSERVATION, 12);
            ENTITYCOUNTS.setGlobalCount(EntityType.OBSERVED_PROPERTY, 3);
            ENTITYCOUNTS.setGlobalCount(EntityType.SENSOR, 4);
            ENTITYCOUNTS.setGlobalCount(EntityType.THING, 2);

            ENTITYCOUNTS.setCount(EntityType.THING, thingId1, EntityType.LOCATION, 1);
            ENTITYCOUNTS.setCount(EntityType.THING, thingId2, EntityType.LOCATION, 1);
            ENTITYCOUNTS.setCount(EntityType.THING, thingId1, EntityType.HISTORICAL_LOCATION, 2);
            ENTITYCOUNTS.setCount(EntityType.THING, thingId2, EntityType.HISTORICAL_LOCATION, 2);
            ENTITYCOUNTS.setCount(EntityType.THING, thingId1, EntityType.DATASTREAM, 2);
            ENTITYCOUNTS.setCount(EntityType.THING, thingId2, EntityType.DATASTREAM, 2);
            ENTITYCOUNTS.setCount(EntityType.LOCATION, locationId1, EntityType.THING, 1);
            ENTITYCOUNTS.setCount(EntityType.LOCATION, locationId2, EntityType.THING, 1);
            ENTITYCOUNTS.setCount(EntityType.LOCATION, locationId1, EntityType.HISTORICAL_LOCATION, 2);
            ENTITYCOUNTS.setCount(EntityType.LOCATION, locationId2, EntityType.HISTORICAL_LOCATION, 2);
            ENTITYCOUNTS.setCount(EntityType.HISTORICAL_LOCATION, historicalLocationId1, EntityType.LOCATION, 1);
            ENTITYCOUNTS.setCount(EntityType.HISTORICAL_LOCATION, historicalLocationId2, EntityType.LOCATION, 1);
            ENTITYCOUNTS.setCount(EntityType.HISTORICAL_LOCATION, historicalLocationId3, EntityType.LOCATION, 1);
            ENTITYCOUNTS.setCount(EntityType.HISTORICAL_LOCATION, historicalLocationId4, EntityType.LOCATION, 1);
            ENTITYCOUNTS.setCount(EntityType.DATASTREAM, datastreamId1, EntityType.OBSERVATION, 3);
            ENTITYCOUNTS.setCount(EntityType.DATASTREAM, datastreamId2, EntityType.OBSERVATION, 3);
            ENTITYCOUNTS.setCount(EntityType.DATASTREAM, datastreamId3, EntityType.OBSERVATION, 3);
            ENTITYCOUNTS.setCount(EntityType.DATASTREAM, datastreamId4, EntityType.OBSERVATION, 3);
            ENTITYCOUNTS.setCount(EntityType.SENSOR, sensorId1, EntityType.DATASTREAM, 1);
            ENTITYCOUNTS.setCount(EntityType.SENSOR, sensorId2, EntityType.DATASTREAM, 1);
            ENTITYCOUNTS.setCount(EntityType.SENSOR, sensorId3, EntityType.DATASTREAM, 1);
            ENTITYCOUNTS.setCount(EntityType.SENSOR, sensorId4, EntityType.DATASTREAM, 1);
            ENTITYCOUNTS.setCount(EntityType.OBSERVED_PROPERTY, observedPropertyId1, EntityType.DATASTREAM, 1);
            ENTITYCOUNTS.setCount(EntityType.OBSERVED_PROPERTY, observedPropertyId2, EntityType.DATASTREAM, 2);
            ENTITYCOUNTS.setCount(EntityType.OBSERVED_PROPERTY, observedPropertyId3, EntityType.DATASTREAM, 1);
            ENTITYCOUNTS.setCount(EntityType.FEATURE_OF_INTEREST, featureOfInterestId1, EntityType.OBSERVATION, 6);
            ENTITYCOUNTS.setCount(EntityType.FEATURE_OF_INTEREST, featureOfInterestId2, EntityType.OBSERVATION, 6);

        } catch (IOException e) {
            LOGGER.error("Exception: ", e);
            fail("An Exception occurred during testing!:\n" + e.getMessage());
        }

    }

}
