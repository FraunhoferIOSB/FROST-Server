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
package de.fraunhofer.iosb.ilt.statests.c01sensingcore;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
        HttpResponse response = HTTPMethods.doPost(urlString, urlParameters);
        if (response.code != 201) {
            LOGGER.error("Failed to create entities: {}, {}", response.code, response.response);
        }
        String data = response.response;
        Object id = HTTPMethods.idFromSelfLink(data);
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
        assertEquals(200, responseCode, "Error during getting entities: " + ((entityType != null) ? entityType.name() : "root URI"));
        if (entityType != null) {
            assertTrue(response.contains("value"), "The GET entities response for entity type \"" + entityType + "\" does not match SensorThings API : missing \"value\" in response.");
        } else { // GET Service Base URI
            assertTrue(response.contains("value"), "The GET entities response for service root URI does not match SensorThings API : missing \"value\" in response.");
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
            fail("An Exception occurred during testing!:\n" + e.getMessage());
        }
        return 0;
    }

}
