package de.fraunhofer.iosb.ilt.statests.c01sensingcore;

import de.fraunhofer.iosb.ilt.statests.ServerSettings;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityType;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods.HttpResponse;
import de.fraunhofer.iosb.ilt.statests.util.ServiceUrlHelper;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates entities for tests of "A.1 Sensing Core" Conformance class.
 */
public class TestEntityCreator {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TestEntityCreator.class);

    public static void maybeCreateTestEntities(ServerSettings serverSettings, ServerVersion version) {
        String rootUri;
        rootUri = serverSettings.getServiceUrl(version);

        // Check if there is data to test on. We check Observation and
        // HistoricalLocation, since if those exist, all other entities should
        // also exist.
        String responseObservations = getEntities(rootUri, EntityType.OBSERVATION);
        String responseHistLocations = getEntities(rootUri, EntityType.HISTORICAL_LOCATION);
        int countObservations = countEntitiesInResponse(responseObservations);
        int countHistLocations = countEntitiesInResponse(responseHistLocations);
        if (countHistLocations == 0 || countObservations == 0) {
            // No data found, insert test data.
            createTestEntities(rootUri, serverSettings.implementsRequirement(version, ServerSettings.TASKING_REQ));
        }
    }

    private static void createTestEntities(String rootUri, boolean actuation) {
        String urlParameters = getEntitiesJson();
        String urlString = ServiceUrlHelper.buildURLString(rootUri, EntityType.THING, null, null, null);
        HttpResponse responseMap = HTTPMethods.doPost(urlString, urlParameters);
        String response = responseMap.response;
        Object id = HTTPMethods.idFromSelfLink(response);
        if (actuation) {
            String postContent = getActuationJson();
            urlString = ServiceUrlHelper.buildURLString(rootUri, EntityType.THING, id, EntityType.TASKING_CAPABILITY, null);
            HTTPMethods.doPost(urlString, postContent);
        }
    }

    private static String getEntitiesJson() {
        return pathToString("src/test/resources/entitiesDefault.json");
    }

    private static String getActuationJson() {
        return pathToString("src/test/resources/entitiesDefaultActuation.json");
    }

    private static String pathToString(String path) throws RuntimeException {
        File file = new File(path);
        try {
            return FileUtils.readFileToString(file, "UTF-8");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * This helper method is sending GET request to a collection of entities.
     *
     * @param entityType Entity type from EntityType enum list
     * @return The response of GET request in string format.
     */
    private static String getEntities(String rootUri, EntityType entityType) {
        String urlString = rootUri;
        if (entityType != null) {
            urlString = ServiceUrlHelper.buildURLString(rootUri, entityType, null, null, null);
        }
        HttpResponse responseMap = HTTPMethods.doGet(urlString);
        String response = responseMap.response;
        int responseCode = responseMap.code;
        Assert.assertEquals("Error during getting entities: " + ((entityType != null) ? entityType.name() : "root URI"), 200, responseCode);
        if (entityType != null) {
            Assert.assertTrue("The GET entities response for entity type \"" + entityType + "\" does not match SensorThings API : missing \"value\" in response.", response.contains("value"));
        } else { // GET Service Base URI
            Assert.assertTrue("The GET entities response for service root URI does not match SensorThings API : missing \"value\" in response.", response.contains("value"));
        }
        return response;
    }

    private static int countEntitiesInResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray entities = jsonResponse.getJSONArray("value");
            return entities.length();
        } catch (JSONException e) {
            LOGGER.error("Exception: ", e);
            Assert.fail("An Exception occurred during testing!:\n" + e.getMessage());
        }
        return 0;
    }

}
