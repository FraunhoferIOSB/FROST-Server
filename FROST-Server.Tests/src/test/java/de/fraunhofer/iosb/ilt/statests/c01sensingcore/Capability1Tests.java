package de.fraunhofer.iosb.ilt.statests.c01sensingcore;

import de.fraunhofer.iosb.ilt.statests.TestSuite;
import de.fraunhofer.iosb.ilt.statests.ServerSettings;
import de.fraunhofer.iosb.ilt.statests.util.ControlInformation;
import de.fraunhofer.iosb.ilt.statests.util.EntityType;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods;
import de.fraunhofer.iosb.ilt.statests.util.ServiceURLBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 * Includes various tests of "A.1 Sensing Core" Conformance class.
 */
public class Capability1Tests {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Capability1Tests.class);

    /**
     * The variable that defines to which recursive level the resource path
     * should be tested
     */
    private final int resourcePathLevel = 4;

    private static ServerSettings serverSettings;

    /**
     * This method will be run before starting the test for this conformance
     * class.
     */
    @BeforeClass
    public static void setUp() {
        LOGGER.info("Setting up...");
        TestSuite suite = TestSuite.getInstance();
        serverSettings = suite.getServerSettings();

        TestEntityCreator.maybeCreateTestEntities(serverSettings);
    }

    @AfterClass
    public static void tearDown() {
        LOGGER.info("Tearing down...");
    }

    /**
     * This method is testing GET entities. It should return 200. Then the
     * response entities are tested for control information, mandatory
     * properties, and mandatory related entities.
     */
    @Test
    public void readEntitiesAndCheckResponse() {
        for (EntityType entityType : serverSettings.enabledEntityTypes) {
            String response = getEntities(entityType);
            checkEntitiesAllAspectsForResponse(entityType, response);
        }
    }

    /**
     * This method is testing GET when requesting a nonexistent entity. The
     * response should be 404.
     */
    @Test
    public void readNonexistentEntity() {
        for (EntityType entityType : serverSettings.enabledEntityTypes) {
            readNonexistentEntityWithEntityType(entityType);
        }
    }

    /**
     * This method is testing GET for a specific entity with its id. It checks
     * the control information, mandatory properties and mandatory related
     * entities for the response entity.
     */
    @Test
    public void readEntityAndCheckResponse() {
        for (EntityType entityType : serverSettings.enabledEntityTypes) {
            String response = readEntityWithEntityType(entityType);
            checkEntityAllAspectsForResponse(entityType, response);
        }
    }

    /**
     * This method is testing GET for a property of an entity.
     */
    @Test
    public void readPropertyOfEntityAndCheckResponse() {
        for (EntityType entityType : serverSettings.enabledEntityTypes) {
            readPropertyOfEntityWithEntityType(entityType);
        }
    }

    /**
     * This helper method is testing property and property/$value for single
     * entity of a given entity type
     *
     * @param entityType Entity type from EntityType enum list
     */
    private void readPropertyOfEntityWithEntityType(EntityType entityType) {
        try {
            String response = getEntities(entityType);
            Object id = new JSONObject(response).getJSONArray("value").getJSONObject(0).get(ControlInformation.ID);
            for (EntityType.EntityProperty property : entityType.getProperties()) {
                checkGetPropertyOfEntity(entityType, id, property);
                checkGetPropertyValueOfEntity(entityType, id, property);
            }
        } catch (JSONException e) {
            LOGGER.error("Exception:", e);
            Assert.fail("An Exception occurred during testing!:\n" + e.getMessage());
        }
    }

    /**
     * This helper method sending GET request for requesting a property and
     * check the response is 200.
     *
     * @param entityType Entity type from EntityType enum list
     * @param id The id of the entity
     * @param property The property to get requested
     */
    private void checkGetPropertyOfEntity(EntityType entityType, Object id, EntityType.EntityProperty property) {
        try {
            Map<String, Object> responseMap = getEntity(entityType, id, property.name);
            int responseCode = Integer.parseInt(responseMap.get("response-code").toString());
            if (responseCode == 204) {
                // 204 is the proper response for NULL properties.
                return;
            }
            String message = "Reading property \"" + property.name + "\" of the existing " + entityType.name() + " with id " + id + " failed.";
            Assert.assertEquals(message, 200, responseCode);
            String response = responseMap.get("response").toString();
            JSONObject entity;
            entity = new JSONObject(response);
            try {
                message = "Reading property \"" + property.name + "\"of \"" + entityType + "\" fails.";
                Assert.assertNotNull(message, entity.get(property.name));
            } catch (JSONException e) {
                Assert.fail("Reading property \"" + property.name + "\"of \"" + entityType + "\" fails.");
            }
            message = "The response for getting property " + property.name + " of a " + entityType + " returns more properties!";
            Assert.assertEquals(message, 1, entity.length());
        } catch (JSONException e) {
            LOGGER.error("Exception:", e);
            Assert.fail("An Exception occurred during testing!:\n" + e.getMessage());
        }
    }

    /**
     * This helper method sending GET request for requesting a property $value
     * and check the response is 200.
     *
     * @param entityType Entity type from EntityType enum list
     * @param id The id of the entity
     * @param property The property to get requested
     */
    private void checkGetPropertyValueOfEntity(EntityType entityType, Object id, EntityType.EntityProperty property) {
        Map<String, Object> responseMap = getEntity(entityType, id, property.name + "/$value");
        int responseCode = Integer.parseInt(responseMap.get("response-code").toString());
        if (responseCode != 200 && property.optional) {
            // The property is optional, and probably not present.
            return;
        }
        if (responseCode == 204) {
            // 204 is the proper response for NULL properties.
            return;
        }
        String message = "Reading property value of \"" + property + "\" of the exitixting " + entityType.name() + " with id " + id + " failed.";
        Assert.assertEquals(message, 200, responseCode);
        String response = responseMap.get("response").toString();
        if ("object".equalsIgnoreCase(property.jsonType)) {
            message = "Reading property value of \"" + property + "\" of \"" + entityType + "\" fails.";
            Assert.assertEquals(message, 0, response.indexOf("{"));
        } else {
            message = "Reading property value of \"" + property + "\" of \"" + entityType + "\" fails.";
            Assert.assertEquals(message, -1, response.indexOf("{"));
        }
    }

    /**
     * This method is testing the resource paths based on specification to the
     * specified level.
     */
    @Test
    public void checkResourcePaths() {
        for (EntityType entityType : serverSettings.enabledEntityTypes) {
            readRelatedEntityOfEntityWithEntityType(entityType);
        }
    }

    /**
     * This helper method is the start point for testing resource path. It adds
     * the entity type to be tested to resource path chain and call the other
     * method to test the chain.
     *
     * @param entityType Entity type from EntityType enum list
     */
    private void readRelatedEntityOfEntityWithEntityType(EntityType entityType) {
        List<String> entityTypes = new ArrayList<>();
        List<Object> ids = new ArrayList<>();
        entityTypes.add(entityType.plural);
        readRelatedEntity(entityTypes, ids);
    }

    /**
     * This helper method is testing the chain to the specified level. It
     * confirms that the response is 200.
     *
     * @param entityTypes List of entity type from EntityType enum list for the
     * chain
     * @param ids List of ids for the chain
     */
    private void readRelatedEntity(List<String> entityTypes, List<Object> ids) {
        if (entityTypes.size() > resourcePathLevel) {
            return;
        }
        String urlString = null;
        try {
            String headName = entityTypes.get(entityTypes.size() - 1);
            EntityType headEntity = EntityType.getForRelation(headName);
            boolean isPlural = EntityType.isPlural(headName);
            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityTypes, ids, null);
            Map<String, Object> responseMap = HTTPMethods.doGet(urlString);
            int code = Integer.valueOf(responseMap.get("response-code").toString());

            String message = "Reading relation of the entity failed: " + entityTypes.toString();
            Assert.assertEquals(message, 200, code);

            String response = responseMap.get("response").toString();
            Object id;
            if (isPlural) {
                id = new JSONObject(response).getJSONArray("value").getJSONObject(0).get(ControlInformation.ID);
            } else {
                id = new JSONObject(response).get(ControlInformation.ID);
            }

            //check $ref
            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityTypes, ids, "$ref");
            responseMap = HTTPMethods.doGet(urlString);
            code = Integer.valueOf(responseMap.get("response-code").toString());

            message = "Reading relation of the entity failed: " + entityTypes.toString();
            Assert.assertEquals(message, 200, code);
            response = responseMap.get("response").toString();
            checkAssociationLinks(response, entityTypes, ids);

            if (entityTypes.size() == resourcePathLevel) {
                return;
            }
            if (EntityType.isPlural(headName)) {
                ids.add(id);
            } else {
                ids.add(null);
            }
            for (String relation : headEntity.getRelations(serverSettings.extensions)) {
                entityTypes.add(relation);
                readRelatedEntity(entityTypes, ids);
                entityTypes.remove(entityTypes.size() - 1);
            }
            ids.remove(ids.size() - 1);
        } catch (JSONException e) {
            LOGGER.error("Failed to parse response for " + urlString, e);
            Assert.fail("An Exception occurred during testing!:\n" + e.getMessage());
        }

    }

    /**
     * This method is checking the response for the request of Association Link.
     * It confirms that it contains a list of selfLinks.
     *
     * @param response The response for GET association link request
     * @param entityTypes List of entity type from EntityType enum list for the
     * chain
     * @param ids List of ids for the chain
     */
    private void checkAssociationLinks(String response, List<String> entityTypes, List<Object> ids) {

        try {
            if (EntityType.isPlural(entityTypes.get(entityTypes.size() - 1))) {
                String message = "The GET entities Association Link response does not match SensorThings API : missing \"value\" in response.: " + entityTypes.toString() + ids.toString();
                Assert.assertTrue(message, response.contains("value"));
                JSONArray value = new JSONObject(response).getJSONArray("value");
                int count = 0;
                for (int i = 0; i < value.length() && count < 2; i++) {
                    count++;
                    JSONObject obj = value.getJSONObject(i);
                    try {
                        message = "The Association Link does not contain self-links.: " + entityTypes.toString() + ids.toString();
                        Assert.assertNotNull(message, obj.get(ControlInformation.SELF_LINK));
                    } catch (JSONException e) {
                        Assert.fail("The Association Link does not contain self-links.: " + entityTypes.toString() + ids.toString());
                    }
                    message = "The Association Link contains properties other than self-link.: " + entityTypes.toString() + ids.toString();
                    Assert.assertEquals(message, 1, obj.length());
                }
            } else {
                JSONObject obj = new JSONObject(response);
                try {
                    String message = "The Association Link does not contain self-links.: " + entityTypes.toString() + ids.toString();
                    Assert.assertNotNull(message, obj.get(ControlInformation.SELF_LINK));
                } catch (JSONException e) {
                    Assert.fail("The Association Link does not contain self-links.: " + entityTypes.toString() + ids.toString());
                }
                String message = "The Association Link contains properties other than self-link.: " + entityTypes.toString() + ids.toString();
                Assert.assertEquals(message, 1, obj.length());
            }
        } catch (JSONException e) {
            LOGGER.error("Exception:", e);
            Assert.fail("An Exception occurred during testing!:\n" + e.getMessage());
        }
    }

    /**
     * This method is reading a specific entity and return it as a string.
     *
     * @param entityType Entity type from EntityType enum list
     * @return The entity response as a string
     */
    private String readEntityWithEntityType(EntityType entityType) {
        try {
            String response = getEntities(entityType);
            Object id = new JSONObject(response).getJSONArray("value").getJSONObject(0).get(ControlInformation.ID);
            Map<String, Object> responseMap = getEntity(entityType, id, null);
            int responseCode = Integer.parseInt(responseMap.get("response-code").toString());

            String message = "Reading existing " + entityType.name() + " with id " + id + " failed.";
            Assert.assertEquals(message, 200, responseCode);

            response = responseMap.get("response").toString();
            return response;
        } catch (JSONException e) {
            LOGGER.error("Exception:", e);
            Assert.fail("An Exception occurred during testing!:\n" + e.getMessage());
            return null;
        }
    }

    /**
     * This method is check the response of sending a GET request to
     * m=nonexistent entity is 404.
     *
     * @param entityType Entity type from EntityType enum list
     */
    private void readNonexistentEntityWithEntityType(EntityType entityType) {
        long id = Long.MAX_VALUE;
        int responseCode = Integer.parseInt(getEntity(entityType, id, null).get("response-code").toString());
        String message = "Reading non-existing " + entityType.name() + " with id " + id + " failed.";
        Assert.assertEquals(message, 404, responseCode);
    }

    /**
     * This method is testing the root URL of the service under test. It
     * basically checks the first page.
     */
    @Test
    public void checkServiceRootUri() {
        try {
            String response = getEntities(null);
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray entities = jsonResponse.getJSONArray("value");
            Map<String, Boolean> addedLinks = new HashMap<>();
            addedLinks.put("Things", false);
            addedLinks.put("Locations", false);
            addedLinks.put("HistoricalLocations", false);
            addedLinks.put("Datastreams", false);
            addedLinks.put("Sensors", false);
            addedLinks.put("Observations", false);
            addedLinks.put("ObservedProperties", false);
            addedLinks.put("FeaturesOfInterest", false);
            if (serverSettings.hasMultiDatastream) {
                addedLinks.put("MultiDatastreams", false);
            }
            if (serverSettings.hasActuation) {
                addedLinks.put("Actuators", false);
                addedLinks.put("TaskingCapabilities", false);
                addedLinks.put("Tasks", false);
            }
            for (int i = 0; i < entities.length(); i++) {
                JSONObject entity = entities.getJSONObject(i);
                if (!entity.has("name") || !entity.has("url")) {
                    Assert.fail("Service root URI component does not have proper JSON keys: name and value.");
                }
                String name = entity.getString("name");
                String nameUrl = entity.getString("url");
                addedLinks.put(name, true);
                if ("MultiDatastreams".equals(name)) {
                    // TODO: MultiDatastreams are not in the entity list yet.
                    String message = "The URL for MultiDatastreams in Service Root URI is not compliant to SensorThings API.";
                    Assert.assertEquals(message, serverSettings.serviceUrl + "/MultiDatastreams", nameUrl);
                } else {
                    try {
                        EntityType entityType = EntityType.getForRelation(name);
                        String message = "The URL for " + entityType.plural + " in Service Root URI is not compliant to SensorThings API.";
                        Assert.assertEquals(message, serverSettings.serviceUrl + "/" + entityType.plural, nameUrl);
                    } catch (IllegalArgumentException exc) {
                        Assert.fail("There is a component in Service Root URI response that is not in SensorThings API : " + name);
                    }
                }
            }
            for (String key : addedLinks.keySet()) {
                String message = "The Service Root URI response does not contain " + key;
                Assert.assertTrue(message, addedLinks.get(key));
            }

        } catch (Exception e) {
            LOGGER.error("An Exception occurred during testing!", e);
            Assert.fail("An Exception occurred during testing!:\n" + e.getMessage());
        }
    }

    /**
     * This helper method is sending GET request to a collection of entities.
     *
     * @param entityType Entity type from EntityType enum list
     * @return The response of GET request in string format.
     */
    private String getEntities(EntityType entityType) {
        String urlString = serverSettings.serviceUrl;
        if (entityType != null) {
            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, null, null, null);
        }
        Map<String, Object> responseMap = HTTPMethods.doGet(urlString);
        String response = responseMap.get("response").toString();
        int responseCode = Integer.parseInt(responseMap.get("response-code").toString());

        String message = "Error during getting entities: " + ((entityType != null) ? entityType.name() : "root URI");
        Assert.assertEquals(message, 200, responseCode);

        if (entityType != null) {
            message = "The GET entities response for entity type \"" + entityType + "\" does not match SensorThings API : missing \"value\" in response.";
            Assert.assertTrue(message, response.contains("value"));
        } else { // GET Service Base URI
            message = "The GET entities response for service root URI does not match SensorThings API : missing \"value\" in response.";
            Assert.assertTrue(message, response.contains("value"));
        }
        return response;
    }

    /**
     * This helper method is sending Get request to a specific entity
     *
     * @param entityType Entity type from EntityType enum list
     * @param id The if of the specific entity
     * @param property The requested property of the entity
     * @return The response-code and response (body) of the request in Map
     * format.
     */
    private Map<String, Object> getEntity(EntityType entityType, Object id, String property) {
        if (id == null) {
            return null;
        }
        String urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, id, null, property);
        return HTTPMethods.doGet(urlString);
    }

    /**
     * This helper method is the start point for checking the response for a
     * collection in all aspects.
     *
     * @param entityType Entity type from EntityType enum list
     * @param response The response of the GET request to be checked
     */
    private void checkEntitiesAllAspectsForResponse(EntityType entityType, String response) {
        checkEntitiesControlInformation(response);
        checkEntitiesProperties(entityType, response);
        checkEntitiesRelations(entityType, response);
    }

    /**
     * This helper method is the start point for checking the response for a
     * specific entity in all aspects.
     *
     * @param entityType Entity type from EntityType enum list
     * @param response The response of the GET request to be checked
     */
    private void checkEntityAllAspectsForResponse(EntityType entityType, String response) {
        checkEntityControlInformation(response);
        checkEntityProperties(entityType, response);
        checkEntityRelations(entityType, response);
    }

    /**
     * This helper method is checking the control information of the response
     * for a collection
     *
     * @param response The response of the GET request to be checked
     */
    private void checkEntitiesControlInformation(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray entities = jsonResponse.getJSONArray("value");
            int count = 0;
            for (int i = 0; i < entities.length() && count < 2; i++) {
                count++;
                JSONObject entity = entities.getJSONObject(i);
                checkEntityControlInformation(entity);
            }
        } catch (JSONException e) {
            LOGGER.error("Exception:", e);
            Assert.fail("An Exception occurred during testing!:\n" + e.getMessage());
        }
    }

    /**
     * This helper method is checking the control information of the response
     * for a specific entity
     *
     * @param response The response of the GET request to be checked
     */
    private void checkEntityControlInformation(Object response) {
        try {
            JSONObject entity = new JSONObject(response.toString());
            try {
                String message = "The entity does not have mandatory control information : " + ControlInformation.ID;
                Assert.assertNotNull(message, entity.get(ControlInformation.ID));
            } catch (JSONException e) {
                Assert.fail("The entity does not have mandatory control information : " + ControlInformation.ID);
            }
            try {
                String message = "The entity does not have mandatory control information : " + ControlInformation.SELF_LINK;
                Assert.assertNotNull(message, entity.get(ControlInformation.SELF_LINK));
            } catch (JSONException e) {
                Assert.fail("The entity does not have mandatory control information : " + ControlInformation.SELF_LINK);
            }
        } catch (JSONException e) {
            LOGGER.error("Exception:", e);
            Assert.fail("An Exception occurred during testing!:\n" + e.getMessage());
        }
    }

    /**
     * This helper method is checking the mandatory properties of the response
     * for a collection
     *
     * @param entityType Entity type from EntityType enum list
     * @param response The response of the GET request to be checked
     */
    private void checkEntitiesProperties(EntityType entityType, String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray entities = jsonResponse.getJSONArray("value");
            int count = 0;
            for (int i = 0; i < entities.length() && count < 2; i++) {
                count++;
                JSONObject entity = entities.getJSONObject(i);
                checkEntityProperties(entityType, entity);
            }

        } catch (JSONException e) {
            LOGGER.error("Exception:", e);
            Assert.fail("An Exception occurred during testing!:\n" + e.getMessage());
        }

    }

    /**
     * This helper method is checking the mandatory properties of the response
     * for a specific entity
     *
     * @param entityType Entity type from EntityType enum list
     * @param response The response of the GET request to be checked
     */
    private void checkEntityProperties(EntityType entityType, Object response) {
        try {
            JSONObject entity = new JSONObject(response.toString());
            for (EntityType.EntityProperty property : entityType.getProperties()) {
                if (property.optional) {
                    continue;
                }
                try {
                    String message = "Entity type \"" + entityType + "\" does not have mandatory property: \"" + property + "\".";
                    Assert.assertNotNull(message, entity.get(property.name));
                } catch (JSONException e) {
                    Assert.fail("Entity type \"" + entityType + "\" does not have mandatory property: \"" + property + "\".");
                }
            }

        } catch (JSONException e) {
            LOGGER.error("Exception:", e);
            Assert.fail("An Exception occurred during testing!:\n" + e.getMessage());
        }

    }

    /**
     * This helper method is checking the mandatory relations of the response
     * for a collection
     *
     * @param entityType Entity type from EntityType enum list
     * @param response The response of the GET request to be checked
     */
    private void checkEntitiesRelations(EntityType entityType, String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray entities = jsonResponse.getJSONArray("value");
            int count = 0;
            for (int i = 0; i < entities.length() && count < 2; i++) {
                count++;
                JSONObject entity = entities.getJSONObject(i);
                checkEntityRelations(entityType, entity);
            }
        } catch (JSONException e) {
            LOGGER.error("Exception:", e);
            Assert.fail("An Exception occurred during testing!:\n" + e.getMessage());
        }

    }

    /**
     * This helper method is checking the mandatory relations of the response
     * for a specific entity
     *
     * @param entityType Entity type from EntityType enum list
     * @param response The response of the GET request to be checked
     */
    private void checkEntityRelations(EntityType entityType, Object response) {
        try {
            JSONObject entity = new JSONObject(response.toString());
            for (String relation : entityType.getRelations(serverSettings.extensions)) {
                try {
                    String message = "Entity type \"" + entityType + "\" does not have mandatory relation: \"" + relation + "\".";
                    Assert.assertNotNull(message, entity.get(relation + ControlInformation.NAVIGATION_LINK));
                } catch (JSONException e) {
                    Assert.fail("Entity type \"" + entityType + "\" does not have mandatory relation: \"" + relation + "\".");
                }
            }
        } catch (JSONException e) {
            LOGGER.error("Exception:", e);
            Assert.fail("An Exception occurred during testing!:\n" + e.getMessage());
        }
    }

}
