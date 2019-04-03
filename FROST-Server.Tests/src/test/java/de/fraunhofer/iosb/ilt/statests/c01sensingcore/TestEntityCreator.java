package de.fraunhofer.iosb.ilt.statests.c01sensingcore;

import de.fraunhofer.iosb.ilt.statests.ServerSettings;
import de.fraunhofer.iosb.ilt.statests.util.EntityType;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods.HttpResponse;
import de.fraunhofer.iosb.ilt.statests.util.ServiceURLBuilder;
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

    public static void maybeCreateTestEntities(ServerSettings serverSettings) {
        String rootUri;
        rootUri = serverSettings.serviceUrl;

        // Check if there is data to test on. We check Observation and
        // HistoricalLocation, since if those exist, all other entities should
        // also exist.
        String responseObservations = getEntities(rootUri, EntityType.OBSERVATION);
        String responseHistLocations = getEntities(rootUri, EntityType.HISTORICAL_LOCATION);
        int countObservations = countEntitiesInResponse(responseObservations);
        int countHistLocations = countEntitiesInResponse(responseHistLocations);
        if (countHistLocations == 0 || countObservations == 0) {
            // No data found, insert test data.
            createTestEntities(rootUri, serverSettings.hasActuation);
        }
    }

    private static void createTestEntities(String rootUri, boolean actuation) {
        String urlParameters = "{\n"
                + "  \"description\": \"thing 1\",\n"
                + "  \"name\": \"thing name 1\",\n"
                + "  \"properties\": {\n"
                + "    \"reference\": \"firstThing\"\n"
                + "  },\n"
                + "  \"Locations\": [{\n"
                + "      \"description\": \"location 1\",\n"
                + "      \"name\": \"location name 1\",\n"
                + "      \"properties\": {\n"
                + "        \"reference\": \"firstLocation\"\n"
                + "      },\n"
                + "      \"location\": {\n"
                + "        \"type\": \"Point\",\n"
                + "        \"coordinates\": [-117.05, 51.05]\n"
                + "      },\n"
                + "      \"encodingType\": \"application/vnd.geo+json\"\n"
                + "    }],\n"
                + "  \"Datastreams\": [{\n"
                + "      \"unitOfMeasurement\": {\n"
                + "        \"name\": \"Lumen\",\n"
                + "        \"symbol\": \"lm\",\n"
                + "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html/Lumen\"\n"
                + "      },\n"
                + "      \"description\": \"datastream 1\",\n"
                + "      \"name\": \"datastream name 1\",\n"
                + "      \"properties\": {\n"
                + "        \"reference\": \"firstDatastream\"\n"
                + "      },\n"
                + "      \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                + "      \"ObservedProperty\": {\n"
                + "        \"name\": \"Luminous Flux\",\n"
                + "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html/LuminousFlux\",\n"
                + "        \"description\": \"observedProperty 1\"\n"
                + "      },\n"
                + "      \"Sensor\": {\n"
                + "        \"description\": \"sensor 1\",\n"
                + "        \"name\": \"sensor name 1\",\n"
                + "        \"properties\": {\n"
                + "          \"reference\": \"firstSensor\"\n"
                + "        },\n"
                + "        \"encodingType\": \"application/pdf\",\n"
                + "        \"metadata\": \"Light flux sensor\"\n"
                + "      },\n"
                + "      \"Observations\": [{\n"
                + "          \"phenomenonTime\": \"2015-03-03T00:00:00Z\",\n"
                + "          \"result\": 3\n"
                + "        }, {\n"
                + "          \"phenomenonTime\": \"2015-03-04T00:00:00Z\",\n"
                + "          \"result\": 4\n"
                + "        }]\n"
                + "    }, {\n"
                + "      \"unitOfMeasurement\": {\n"
                + "        \"name\": \"Centigrade\",\n"
                + "        \"symbol\": \"C\",\n"
                + "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html/Lumen\"\n"
                + "      },\n"
                + "      \"description\": \"datastream 2\",\n"
                + "      \"name\": \"datastream name 2\",\n"
                + "      \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                + "      \"ObservedProperty\": {\n"
                + "        \"name\": \"Tempretaure\",\n"
                + "        \"properties\": {\n"
                + "          \"reference\": \"firstObservedProperty\"\n"
                + "        },\n"
                + "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html/Tempreture\",\n"
                + "        \"description\": \"observedProperty 2\"\n"
                + "      },\n"
                + "      \"Sensor\": {\n"
                + "        \"description\": \"sensor 2\",\n"
                + "        \"name\": \"sensor name 2\",\n"
                + "        \"properties\": {\n"
                + "          \"reference\": \"secondSensor\"\n"
                + "        },\n"
                + "        \"encodingType\": \"application/pdf\",\n"
                + "        \"metadata\": \"Tempreture sensor\"\n"
                + "      },\n"
                + "      \"Observations\": [{\n"
                + "          \"phenomenonTime\": \"2015-03-05T00:00:00Z\",\n"
                + "          \"result\": 5\n"
                + "        }, {\n"
                + "          \"phenomenonTime\": \"2015-03-06T00:00:00Z\",\n"
                + "          \"result\": 6\n"
                + "        }]\n"
                + "    }]\n"
                + "}\n"
                + "";
        String urlString = ServiceURLBuilder.buildURLString(rootUri, EntityType.THING, null, null, null);
        HttpResponse responseMap = HTTPMethods.doPost(urlString, urlParameters);
        String response = responseMap.response;
        Object id = HTTPMethods.idFromSelfLink(response);
        if (actuation) {
            String postContent = "{\n"
                    + "	\"name\": \"TaskingCapability 1\",\n"
                    + "	\"description\": \"This is a tasking capability\",\n"
                    + "	\"properties\": {\n"
                    + "		\"cool\": true\n"
                    + "	},\n"
                    + "	\"taskingParameters\": {\n"
                    + "		\"todo\": \"yes\"\n"
                    + "	},\n"
                    + "	\"Actuator\": {\n"
                    + "		\"description\": \"actuator 1\",\n"
                    + "		\"name\": \"actuator name 1\",\n"
                    + "		\"properties\": {\n"
                    + "			\"reference\": \"firstActuator \"\n"
                    + "		},\n"
                    + "		\"encodingType\": \"application/pdf\",\n"
                    + "		\"metadata\": \"Window opener\"\n"
                    + "	},\n"
                    + "	\"Tasks\": [\n"
                    + "		{\n"
                    + "			\"creationTime\": \"2015-03-05T00:00:00Z\",\n"
                    + "			\"taskingParameters\": {\n"
                    + "				\"todo\": \"yes\"\n"
                    + "			}\n"
                    + "		},\n"
                    + "		{\n"
                    + "			\"creationTime\": \"2015-03-05T00:00:00Z\",\n"
                    + "			\"taskingParameters\": {\n"
                    + "				\"todo\": \"no\"\n"
                    + "			}\n"
                    + "		}\n"
                    + "	]\n"
                    + "}";
            urlString = ServiceURLBuilder.buildURLString(rootUri, EntityType.THING, id, EntityType.TASKING_CAPABILITY, null);
            HTTPMethods.doPost(urlString, postContent);
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
            urlString = ServiceURLBuilder.buildURLString(rootUri, entityType, null, null, null);
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
