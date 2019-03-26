package de.fraunhofer.iosb.ilt.statests.c02cud;

import de.fraunhofer.iosb.ilt.statests.TestSuite;
import de.fraunhofer.iosb.ilt.statests.ServerSettings;
import de.fraunhofer.iosb.ilt.statests.util.ControlInformation;
import de.fraunhofer.iosb.ilt.statests.util.EntityType;
import de.fraunhofer.iosb.ilt.statests.util.Extension;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods;
import de.fraunhofer.iosb.ilt.statests.util.ServiceURLBuilder;
import de.fraunhofer.iosb.ilt.statests.util.Utils;
import static de.fraunhofer.iosb.ilt.statests.util.Utils.quoteIdForJson;
import static de.fraunhofer.iosb.ilt.statests.util.Utils.quoteIdForUrl;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Includes various tests of "A.3 Create Update Delete" Conformance class.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Capability2Tests {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Capability2Tests.class);

    private static ServerSettings serverSettings;

    /**
     * The list of ids for all the Things created during test procedure (will be
     * used for clean-up)
     */
    private static final List<Object> THING_IDS = new ArrayList<>();
    /**
     * The list of ids for all the Locations created during test procedure (will
     * be used for clean-up)
     */
    private static final List<Object> LOCATION_IDS = new ArrayList<>();
    /**
     * The list of ids for all the HistoricalLocations created during test
     * procedure (will be used for clean-up)
     */
    private static final List<Object> HISTORICAL_LOCATION_IDS = new ArrayList<>();
    /**
     * The list of ids for all the Datastreams created during test procedure
     * (will be used for clean-up)
     */
    private static final List<Object> DATASTREAM_IDS = new ArrayList<>();
    /**
     * The list of ids for all the Observations created during test procedure
     * (will be used for clean-up)
     */
    private static final List<Object> OBSERVATION_IDS = new ArrayList<>();
    /**
     * The list of ids for all the Sensors created during test procedure (will
     * be used for clean-up)
     */
    private static final List<Object> SENSOR_IDS = new ArrayList<>();
    /**
     * The list of ids for all the ObservedPropeties created during test
     * procedure (will be used for clean-up)
     */
    private static final List<Object> OBSPROP_IDS = new ArrayList<>();
    /**
     * The list of ids for all the FeaturesOfInterest created during test
     * procedure (will be used for clean-up)
     */
    private static final List<Object> FOI_IDS = new ArrayList<>();

    /**
     * This method will be run before starting the test for this conformance
     * class.It cleans the database to start test.
     *
     * @throws java.net.MalformedURLException
     */
    @BeforeClass
    public static void obtainTestSubject() throws MalformedURLException {
        TestSuite suite = TestSuite.getInstance();
        serverSettings = suite.getServerSettings();
        deleteEverythings();
    }

    /**
     * This method is testing create or POST with invalid Deep Insert. It makes
     * sure that if there is any problem in the request body of Deep Insert,
     * none of the entities in that query is created. The response should be 400
     * or 409 and the entities should not be accessible using GET.
     */
    @Test
    public void test01CreateInvalidEntitiesWithDeepInsert() {
        LOGGER.info("test01CreateInvalidEntitiesWithDeepInsert");
        try {
            String urlParameters = "{\n"
                    + "  \"name\": \"Office Building\",\n"
                    + "  \"description\": \"Office Building\",\n"
                    + "  \"properties\": {\n"
                    + "    \"reference\": \"Third Floor\"\n"
                    + "  },\n"
                    + "  \"Locations\": [\n"
                    + "    {\n"
                    + "      \"name\": \"West Roof\",\n"
                    + "      \"description\": \"West Roof\",\n"
                    + "      \"location\": { \"type\": \"Point\", \"coordinates\": [-117.05, 51.05] },\n"
                    + "      \"encodingType\": \"application/vnd.geo+json\"\n"
                    + "    }\n"
                    + "  ],\n"
                    + "  \"Datastreams\": [\n"
                    + "    {\n"
                    + "      \"unitOfMeasurement\": {\n"
                    + "        \"name\": \"Lumen\",\n"
                    + "        \"symbol\": \"lm\",\n"
                    + "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#Lumen\"\n"
                    + "      },\n"
                    + "      \"name\": \"Light exposure.\",\n"
                    + "      \"description\": \"Light exposure.\",\n"
                    + "      \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                    + "      \"ObservedProperty\": {\n"
                    + "        \"name\": \"Luminous Flux\",\n"
                    + "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html#LuminousFlux\",\n"
                    + "        \"description\": \"Luminous Flux or Luminous Power is the measure of the perceived power of light.\"\n"
                    + "      }\n"
                    + "    }\n"
                    + "  ]\n"
                    + "}";
            postInvalidEntity(EntityType.THING, urlParameters);
            List<EntityType> entityTypesToCheck = new ArrayList<>();
            entityTypesToCheck.add(EntityType.THING);
            entityTypesToCheck.add(EntityType.LOCATION);
            entityTypesToCheck.add(EntityType.HISTORICAL_LOCATION);
            entityTypesToCheck.add(EntityType.DATASTREAM);
            entityTypesToCheck.add(EntityType.OBSERVED_PROPERTY);
            checkNotExisting(entityTypesToCheck);

            /* Datastream */
            urlParameters = "{"
                    + "\"name\": \"Office Building\","
                    + "\"description\": \"Office Building\""
                    + "}";
            Object thingId = postEntity(EntityType.THING, urlParameters).get("@iot.id");

            urlParameters = "{\n"
                    + "  \"unitOfMeasurement\": {\n"
                    + "    \"name\": \"Celsius\",\n"
                    + "    \"symbol\": \"degC\",\n"
                    + "    \"definition\": \"http://qudt.org/vocab/unit#DegreeCelsius\"\n"
                    + "  },\n"
                    + "  \"name\": \"test datastream.\",\n"
                    + "  \"description\": \"test datastream.\",\n"
                    + "  \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                    + "  \"Thing\": { \"@iot.id\": " + quoteIdForJson(thingId) + " },\n"
                    + "   \"ObservedProperty\": {\n"
                    + "        \"name\": \"Luminous Flux\",\n"
                    + "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html#LuminousFlux\",\n"
                    + "        \"description\": \"Luminous Flux or Luminous Power is the measure of the perceived power of light.\"\n"
                    + "   },\n"
                    + "      \"Observations\": [\n"
                    + "        {\n"
                    + "          \"phenomenonTime\": \"2015-03-01T00:10:00Z\",\n"
                    + "          \"result\": 10\n"
                    + "        }\n"
                    + "      ]"
                    + "}";
            postInvalidEntity(EntityType.DATASTREAM, urlParameters);

            urlParameters = "{\n"
                    + "  \"unitOfMeasurement\": {\n"
                    + "    \"name\": \"Celsius\",\n"
                    + "    \"symbol\": \"degC\",\n"
                    + "    \"definition\": \"http://qudt.org/vocab/unit#DegreeCelsius\"\n"
                    + "  },\n"
                    + "  \"name\": \"test datastream.\",\n"
                    + "  \"description\": \"test datastream.\",\n"
                    + "  \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                    + "  \"Thing\": { \"@iot.id\": " + quoteIdForJson(thingId) + " },\n"
                    + "   \"Sensor\": {        \n"
                    + "        \"name\": \"Acme Fluxomatic 1000\",\n"
                    + "        \"description\": \"Acme Fluxomatic 1000\",\n"
                    + "        \"encodingType\": \"application/pdf\",\n"
                    + "        \"metadata\": \"Light flux sensor\"\n"
                    + "   },\n"
                    + "      \"Observations\": [\n"
                    + "        {\n"
                    + "          \"phenomenonTime\": \"2015-03-01T00:10:00Z\",\n"
                    + "          \"result\": 10\n"
                    + "        }\n"
                    + "      ]"
                    + "}";
            postInvalidEntity(EntityType.DATASTREAM, urlParameters);

            urlParameters = "{\n"
                    + "  \"unitOfMeasurement\": {\n"
                    + "    \"name\": \"Celsius\",\n"
                    + "    \"symbol\": \"degC\",\n"
                    + "    \"definition\": \"http://qudt.org/vocab/unit#DegreeCelsius\"\n"
                    + "  },\n"
                    + "  \"name\": \"test datastream.\",\n"
                    + "  \"description\": \"test datastream.\",\n"
                    + "  \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                    + "   \"ObservedProperty\": {\n"
                    + "        \"name\": \"Luminous Flux\",\n"
                    + "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html#LuminousFlux\",\n"
                    + "        \"description\": \"Luminous Flux or Luminous Power is the measure of the perceived power of light.\"\n"
                    + "   },\n"
                    + "   \"Sensor\": {        \n"
                    + "        \"name\": \"Acme Fluxomatic 1000\",\n"
                    + "        \"description\": \"Acme Fluxomatic 1000\",\n"
                    + "        \"encodingType\": \"application/pdf\",\n"
                    + "        \"metadata\": \"Light flux sensor\"\n"
                    + "   },\n"
                    + "      \"Observations\": [\n"
                    + "        {\n"
                    + "          \"phenomenonTime\": \"2015-03-01T00:10:00Z\",\n"
                    + "          \"result\": 10\n"
                    + "        }\n"
                    + "      ]"
                    + "}";
            postInvalidEntity(EntityType.DATASTREAM, urlParameters);

            entityTypesToCheck.clear();
            entityTypesToCheck.add(EntityType.DATASTREAM);
            entityTypesToCheck.add(EntityType.SENSOR);
            entityTypesToCheck.add(EntityType.OBSERVATION);
            entityTypesToCheck.add(EntityType.FEATURE_OF_INTEREST);
            entityTypesToCheck.add(EntityType.OBSERVED_PROPERTY);
            checkNotExisting(entityTypesToCheck);

            /* Observation */
            urlParameters = "{\n"
                    + "  \"unitOfMeasurement\": {\n"
                    + "    \"name\": \"Celsius\",\n"
                    + "    \"symbol\": \"degC\",\n"
                    + "    \"definition\": \"http://qudt.org/vocab/unit#DegreeCelsius\"\n"
                    + "  },\n"
                    + "  \"name\": \"test datastream.\",\n"
                    + "  \"description\": \"test datastream.\",\n"
                    + "  \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                    + "  \"Thing\": { \"@iot.id\": " + quoteIdForJson(thingId) + " },\n"
                    + "   \"ObservedProperty\": {\n"
                    + "        \"name\": \"Luminous Flux\",\n"
                    + "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html#LuminousFlux\",\n"
                    + "        \"description\": \"Luminous Flux or Luminous Power is the measure of the perceived power of light.\"\n"
                    + "   },\n"
                    + "   \"Sensor\": {        \n"
                    + "        \"name\": \"Acme Fluxomatic 1000\",\n"
                    + "        \"description\": \"Acme Fluxomatic 1000\",\n"
                    + "        \"encodingType\": \"application/pdf\",\n"
                    + "        \"metadata\": \"Light flux sensor\"\n"
                    + "   }\n"
                    + "}";
            Object datastreamId = postEntity(EntityType.DATASTREAM, urlParameters).get("@iot.id");

            urlParameters = "{\n"
                    + "  \"phenomenonTime\": \"2015-03-01T00:00:00Z\",\n"
                    + "  \"result\": 100,\n"
                    + "  \"Datastream\":{\"@iot.id\": " + quoteIdForJson(datastreamId) + "}\n"
                    + "}";
            postInvalidEntity(EntityType.OBSERVATION, urlParameters);

            urlParameters = "{\n"
                    + "  \"phenomenonTime\": \"2015-03-01T00:00:00Z\",\n"
                    + "  \"result\": 100,\n"
                    + "  \"FeatureOfInterest\": {\n"
                    + "  \t\"name\": \"A weather station.\",\n"
                    + "  \t\"description\": \"A weather station.\",\n"
                    + "    \"feature\": {\n"
                    + "      \"type\": \"Point\",\n"
                    + "      \"coordinates\": [\n"
                    + "        -114.05,\n"
                    + "        51.05\n"
                    + "      ]\n"
                    + "    }\n"
                    + "  },\n"
                    + "  \"Datastream\":{\"@iot.id\": " + quoteIdForJson(datastreamId) + "}\n"
                    + "}";
            postInvalidEntity(EntityType.OBSERVATION, urlParameters);

            entityTypesToCheck.clear();
            entityTypesToCheck.add(EntityType.OBSERVATION);
            entityTypesToCheck.add(EntityType.FEATURE_OF_INTEREST);
            checkNotExisting(entityTypesToCheck);

            deleteEverythings();

        } catch (JSONException e) {
            LOGGER.error("Exception: ", e);
            Assert.fail("An Exception occurred during testing: " + e.getMessage());
        }
    }

    /**
     * This method is testing create or POST entities. It only tests simple
     * create, no deep insert. It makes sure that the response is 201 and use
     * simple GET to make sure the entity is added to the service.
     */
    @Test
    public void test02CreateEntities() {
        LOGGER.info("test02CreateEntities");
        try {
            /* Thing */
            String urlParameters = "{"
                    + "\"name\":\"Test Thing\","
                    + "\"description\":\"This is a Test Thing From TestNG\""
                    + "}";
            JSONObject entity = postEntity(EntityType.THING, urlParameters);
            Object thingId = entity.get(ControlInformation.ID);
            THING_IDS.add(thingId);

            /* Location */
            urlParameters = "{\n"
                    + "  \"name\": \"bow river\",\n"
                    + "  \"description\": \"bow river\",\n"
                    + "  \"encodingType\": \"application/vnd.geo+json\",\n"
                    + "  \"location\": { \"type\": \"Point\", \"coordinates\": [-114.05, 51.05] }\n"
                    + "}";
            entity = postEntity(EntityType.LOCATION, urlParameters);
            Object locationId = entity.get(ControlInformation.ID);
            LOCATION_IDS.add(locationId);
            JSONObject locationEntity = entity;

            /* Sensor */
            urlParameters = "{\n"
                    + "  \"name\": \"Fuguro Barometer\",\n"
                    + "  \"description\": \"Fuguro Barometer\",\n"
                    + "  \"encodingType\": \"application/pdf\",\n"
                    + "  \"metadata\": \"Barometer\"\n"
                    + "}";
            entity = postEntity(EntityType.SENSOR, urlParameters);
            Object sensorId = entity.get(ControlInformation.ID);
            SENSOR_IDS.add(sensorId);

            /* ObservedProperty */
            urlParameters = "{\n"
                    + "  \"name\": \"DewPoint Temperature\",\n"
                    + "  \"definition\": \"http://dbpedia.org/page/Dew_point\",\n"
                    + "  \"description\": \"The dewpoint temperature is the temperature to which the air must be cooled, at constant pressure, for dew to form. As the grass and other objects near the ground cool to the dewpoint, some of the water vapor in the atmosphere condenses into liquid water on the objects.\"\n"
                    + "}";
            entity = postEntity(EntityType.OBSERVED_PROPERTY, urlParameters);
            Object obsPropId = entity.get(ControlInformation.ID);
            OBSPROP_IDS.add(obsPropId);

            /* FeatureOfInterest */
            urlParameters = "{\n"
                    + "  \"name\": \"A weather station.\",\n"
                    + "  \"description\": \"A weather station.\",\n"
                    + "  \"encodingType\": \"application/vnd.geo+json\",\n"
                    + "  \"feature\": {\n"
                    + "    \"type\": \"Point\",\n"
                    + "    \"coordinates\": [\n"
                    + "      10,\n"
                    + "      10\n"
                    + "    ]\n"
                    + "  }\n"
                    + "}";
            entity = postEntity(EntityType.FEATURE_OF_INTEREST, urlParameters);
            Object foiId = entity.get(ControlInformation.ID);
            FOI_IDS.add(foiId);

            /* Datastream */
            urlParameters = "{\n"
                    + "  \"unitOfMeasurement\": {\n"
                    + "    \"name\": \"Celsius\",\n"
                    + "    \"symbol\": \"degC\",\n"
                    + "    \"definition\": \"http://qudt.org/vocab/unit#DegreeCelsius\"\n"
                    + "  },\n"
                    + "  \"name\": \"test datastream.\",\n"
                    + "  \"description\": \"test datastream.\",\n"
                    + "  \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                    + "  \"Thing\": { \"@iot.id\": " + quoteIdForJson(thingId) + " },\n"
                    + "  \"ObservedProperty\":{ \"@iot.id\":" + quoteIdForJson(obsPropId) + "},\n"
                    + "  \"Sensor\": { \"@iot.id\": " + quoteIdForJson(sensorId) + " }\n"
                    + "}";
            entity = postEntity(EntityType.DATASTREAM, urlParameters);
            Object datastreamId = entity.get(ControlInformation.ID);
            DATASTREAM_IDS.add(datastreamId);

            /* Observation */
            urlParameters = "{\n"
                    + "  \"phenomenonTime\": \"2015-03-01T00:40:00.000Z\",\n"
                    + "  \"result\": 8,\n"
                    + "  \"Datastream\":{\"@iot.id\": " + quoteIdForJson(datastreamId) + "},\n"
                    + "  \"FeatureOfInterest\": {\"@iot.id\": " + quoteIdForJson(foiId) + "}  \n"
                    + "}";
            entity = postEntity(EntityType.OBSERVATION, urlParameters);
            Object obsId1 = entity.get(ControlInformation.ID);
            OBSERVATION_IDS.add(obsId1);

            //POST Observation without FOI (Automatic creation of FOI)
            //Add location to the Thing
            urlParameters = "{\"Locations\":[{\"@iot.id\":" + quoteIdForJson(locationId) + "}]}";
            patchEntity(EntityType.THING, urlParameters, thingId);

            urlParameters = "{\n"
                    + "  \"phenomenonTime\": \"2015-03-01T00:00:00.000Z\",\n"
                    + "  \"resultTime\": \"2015-03-01T01:00:00.000Z\",\n"
                    + "  \"result\": 100,\n"
                    + "  \"Datastream\":{\"@iot.id\": " + quoteIdForJson(datastreamId) + "}\n"
                    + "}";
            entity = postEntity(EntityType.OBSERVATION, urlParameters);
            checkForObservationResultTime(entity, "2015-03-01T01:00:00.000Z");
            Object obsId2 = entity.get(ControlInformation.ID);
            OBSERVATION_IDS.add(obsId2);
            Object automatedFOIId = checkAutomaticInsertionOfFOI(obsId2, locationEntity, null);
            FOI_IDS.add(automatedFOIId);

            //POST another Observation to make sure it is linked to the previously created FOI
            urlParameters = "{\n"
                    + "  \"phenomenonTime\": \"2015-05-01T00:00:00.000Z\",\n"
                    + "  \"result\": 105,\n"
                    + "  \"Datastream\":{\"@iot.id\": " + quoteIdForJson(datastreamId) + "}\n"
                    + "}";
            entity = postEntity(EntityType.OBSERVATION, urlParameters);
            checkForObservationResultTime(entity, null);
            Object obsId3 = entity.get(ControlInformation.ID);
            OBSERVATION_IDS.add(obsId3);
            checkAutomaticInsertionOfFOI(obsId2, locationEntity, automatedFOIId);

            // Move the Thing to a new location, create a new observation
            // without FOI, check if a new FOI is created from this new location.
            /* Second Location */
            urlParameters = "{\n"
                    + "  \"name\": \"spear river\",\n"
                    + "  \"description\": \"spear river\",\n"
                    + "  \"encodingType\": \"application/vnd.geo+json\",\n"
                    + "  \"location\": { \"type\": \"Point\", \"coordinates\": [114.05, -51.05] }\n"
                    + "}";
            entity = postEntity(EntityType.LOCATION, urlParameters);
            Object location2Id = entity.get(ControlInformation.ID);
            LOCATION_IDS.add(location2Id);
            JSONObject location2Entity = entity;

            //Add second location to the Thing
            urlParameters = "{\"Locations\":[{\"@iot.id\":" + quoteIdForJson(location2Id) + "}]}";
            patchEntity(EntityType.THING, urlParameters, thingId);

            // Create a new Observation for Thing1 with no FoI.
            urlParameters = "{\n"
                    + "  \"phenomenonTime\": \"2015-03-01T01:00:00.000Z\",\n"
                    + "  \"resultTime\": \"2015-03-01T02:00:00.000Z\",\n"
                    + "  \"result\": 200,\n"
                    + "  \"Datastream\":{\"@iot.id\": " + quoteIdForJson(datastreamId) + "}\n"
                    + "}";
            entity = postEntity(EntityType.OBSERVATION, urlParameters);
            Object obsId4 = entity.get(ControlInformation.ID);
            OBSERVATION_IDS.add(obsId4);
            Object automatedFOI2Id = checkAutomaticInsertionOfFOI(obsId4, location2Entity, null);
            FOI_IDS.add(automatedFOI2Id);
            String message = "A new FoI should have been created, since the Thing moved.";
            Assert.assertNotEquals(message, automatedFOI2Id, automatedFOIId);

            // Create a new Thing with the same Location, create a new
            // observation without FOI, check if the same FOI is used.
            /* Thing2 */
            urlParameters = "{"
                    + "\"name\":\"Test Thing 2\","
                    + "\"description\":\"This is a second Test Thing From TestNG\","
                    + "\"Locations\":[{\"@iot.id\": " + quoteIdForJson(locationId) + "}]"
                    + "}";
            entity = postEntity(EntityType.THING, urlParameters);
            Object thing2Id = entity.get(ControlInformation.ID);
            THING_IDS.add(thing2Id);

            /* Datastream2 */
            urlParameters = "{\n"
                    + "  \"unitOfMeasurement\": {\n"
                    + "    \"name\": \"Celsius\",\n"
                    + "    \"symbol\": \"degC\",\n"
                    + "    \"definition\": \"http://qudt.org/vocab/unit#DegreeCelsius\"\n"
                    + "  },\n"
                    + "  \"name\": \"test datastream 2.\",\n"
                    + "  \"description\": \"test datastream 2.\",\n"
                    + "  \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                    + "  \"Thing\": { \"@iot.id\": " + quoteIdForJson(thing2Id) + " },\n"
                    + "  \"ObservedProperty\":{ \"@iot.id\":" + quoteIdForJson(obsPropId) + "},\n"
                    + "  \"Sensor\": { \"@iot.id\": " + quoteIdForJson(sensorId) + " }\n"
                    + "}";
            entity = postEntity(EntityType.DATASTREAM, urlParameters);
            Object datastream2Id = entity.get(ControlInformation.ID);
            DATASTREAM_IDS.add(datastream2Id);

            /* Post new Observation without FoI */
            urlParameters = "{\n"
                    + "  \"phenomenonTime\": \"2015-03-01T03:00:00.000Z\",\n"
                    + "  \"resultTime\": \"2015-03-01T04:00:00.000Z\",\n"
                    + "  \"result\": 300,\n"
                    + "  \"Datastream\":{\"@iot.id\": " + quoteIdForJson(datastream2Id) + "}\n"
                    + "}";
            entity = postEntity(EntityType.OBSERVATION, urlParameters);
            Object obsId5 = entity.get(ControlInformation.ID);
            OBSERVATION_IDS.add(obsId5);
            Object automatedFOI3Id = checkAutomaticInsertionOfFOI(obsId5, locationEntity, null);
            message = "The generated FoI should be the same as the first generated FoI, since Thing2 has the same Location.";
            Assert.assertEquals(message, automatedFOI3Id, automatedFOIId);

            /* HistoricalLocation */
            urlParameters = "{\n"
                    + "  \"time\": \"2015-03-01T00:40:00.000Z\",\n"
                    + "  \"Thing\":{\"@iot.id\": " + quoteIdForJson(thingId) + "},\n"
                    + "  \"Locations\": [{\"@iot.id\": " + quoteIdForJson(locationId) + "}]  \n"
                    + "}";
            entity = postEntity(EntityType.HISTORICAL_LOCATION, urlParameters);
            Object histLocId = entity.get(ControlInformation.ID);
            HISTORICAL_LOCATION_IDS.add(histLocId);

        } catch (JSONException e) {
            LOGGER.error("Exception: ", e);
            Assert.fail("An Exception occurred during testing: " + e.getMessage());
        }
    }

    /**
     * This method is testing create or POST in the form of Deep Insert. It
     * makes sure the response is 201. Also using GET requests, it makes sure
     * the entity and all its related entities are created and added to the
     * service.
     */
    @Test
    public void test03CreateEntitiesWithDeepInsert() {
        LOGGER.info("test03CreateEntitiesWithDeepInsert");
        try {
            /* Thing */
            String urlParameters = "{\n"
                    + "  \"name\": \"Office Building\",\n"
                    + "  \"description\": \"Office Building\",\n"
                    + "  \"properties\": {\n"
                    + "    \"reference\": \"Third Floor\"\n"
                    + "  },\n"
                    + "  \"Locations\": [\n"
                    + "    {\n"
                    + "      \"name\": \"West Roof\",\n"
                    + "      \"description\": \"West Roof\",\n"
                    + "      \"location\": { \"type\": \"Point\", \"coordinates\": [-117.05, 51.05] },\n"
                    + "      \"encodingType\": \"application/vnd.geo+json\"\n"
                    + "    }\n"
                    + "  ],\n"
                    + "  \"Datastreams\": [\n"
                    + "    {\n"
                    + "      \"unitOfMeasurement\": {\n"
                    + "        \"name\": \"Lumen\",\n"
                    + "        \"symbol\": \"lm\",\n"
                    + "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#Lumen\"\n"
                    + "      },\n"
                    + "      \"name\": \"Light exposure.\",\n"
                    + "      \"description\": \"Light exposure.\",\n"
                    + "      \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                    + "      \"ObservedProperty\": {\n"
                    + "        \"name\": \"Luminous Flux\",\n"
                    + "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html#LuminousFlux\",\n"
                    + "        \"description\": \"Luminous Flux or Luminous Power is the measure of the perceived power of light.\"\n"
                    + "      },\n"
                    + "      \"Sensor\": {        \n"
                    + "        \"name\": \"Acme Fluxomatic 1000\",\n"
                    + "        \"description\": \"Acme Fluxomatic 1000\",\n"
                    + "        \"encodingType\": \"application/pdf\",\n"
                    + "        \"metadata\": \"Light flux sensor\"\n"
                    + "      }\n"
                    + "    }\n"
                    + "  ]\n"
                    + "}";
            JSONObject entity = postEntity(EntityType.THING, urlParameters);
            Object thingId = entity.get(ControlInformation.ID);
            //Check Datastream
            JSONObject deepInsertedObj = new JSONObject("{\n"
                    + "      \"unitOfMeasurement\": {\n"
                    + "        \"name\": \"Lumen\",\n"
                    + "        \"symbol\": \"lm\",\n"
                    + "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#Lumen\"\n"
                    + "      },\n"
                    + "      \"name\": \"Light exposure.\",\n"
                    + "      \"description\": \"Light exposure.\",\n"
                    + "      \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\"\n"
                    + "    }\n");
            Object datastreamId = checkRelatedEntity(serverSettings.extensions, EntityType.THING, thingId, EntityType.DATASTREAM, deepInsertedObj);
            DATASTREAM_IDS.add(datastreamId);
            //Check Location
            deepInsertedObj = new JSONObject("{\n"
                    + "      \"name\": \"West Roof\",\n"
                    + "      \"description\": \"West Roof\",\n"
                    + "      \"location\": { \"type\": \"Point\", \"coordinates\": [-117.05, 51.05] },\n"
                    + "      \"encodingType\": \"application/vnd.geo+json\"\n"
                    + "    }\n");
            LOCATION_IDS.add(checkRelatedEntity(serverSettings.extensions, EntityType.THING, thingId, EntityType.LOCATION, deepInsertedObj));
            //Check Sensor
            deepInsertedObj = new JSONObject("{\n"
                    + "        \"name\": \"Acme Fluxomatic 1000\",\n"
                    + "        \"description\": \"Acme Fluxomatic 1000\",\n"
                    + "        \"encodingType\": \"application/pdf\",\n"
                    + "        \"metadata\": \"Light flux sensor\"\n"
                    + "      }\n");
            SENSOR_IDS.add(checkRelatedEntity(serverSettings.extensions, EntityType.DATASTREAM, datastreamId, EntityType.SENSOR, deepInsertedObj));
            //Check ObservedProperty
            deepInsertedObj = new JSONObject("{\n"
                    + "        \"name\": \"Luminous Flux\",\n"
                    + "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html#LuminousFlux\",\n"
                    + "        \"description\": \"Luminous Flux or Luminous Power is the measure of the perceived power of light.\"\n"
                    + "      },\n");
            OBSPROP_IDS.add(checkRelatedEntity(serverSettings.extensions, EntityType.DATASTREAM, datastreamId, EntityType.OBSERVED_PROPERTY, deepInsertedObj));
            THING_IDS.add(thingId);

            /* Datastream */
            urlParameters = "{\n"
                    + "  \"unitOfMeasurement\": {\n"
                    + "    \"name\": \"Celsius\",\n"
                    + "    \"symbol\": \"degC\",\n"
                    + "    \"definition\": \"http://qudt.org/vocab/unit#DegreeCelsius\"\n"
                    + "  },\n"
                    + "  \"name\": \"test datastream.\",\n"
                    + "  \"description\": \"test datastream.\",\n"
                    + "  \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                    + "  \"Thing\": { \"@iot.id\": " + quoteIdForJson(thingId) + " },\n"
                    + "   \"ObservedProperty\": {\n"
                    + "        \"name\": \"Luminous Flux\",\n"
                    + "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html#LuminousFlux\",\n"
                    + "        \"description\": \"Luminous Flux or Luminous Power is the measure of the perceived power of light.\"\n"
                    + "   },\n"
                    + "   \"Sensor\": {        \n"
                    + "        \"name\": \"Acme Fluxomatic 1000\",\n"
                    + "        \"description\": \"Acme Fluxomatic 1000\",\n"
                    + "        \"encodingType\": \"application/pdf\",\n"
                    + "        \"metadata\": \"Light flux sensor\"\n"
                    + "   },\n"
                    + "      \"Observations\": [\n"
                    + "        {\n"
                    + "          \"phenomenonTime\": \"2015-03-01T00:10:00Z\",\n"
                    + "          \"result\": 10\n"
                    + "        }\n"
                    + "      ]"
                    + "}";
            entity = postEntity(EntityType.DATASTREAM, urlParameters);
            datastreamId = entity.get(ControlInformation.ID);
            //Check Sensor
            deepInsertedObj = new JSONObject("{\n"
                    + "        \"name\": \"Acme Fluxomatic 1000\",\n"
                    + "        \"description\": \"Acme Fluxomatic 1000\",\n"
                    + "        \"encodingType\": \"application/pdf\",\n"
                    + "        \"metadata\": \"Light flux sensor\"\n"
                    + "      }\n");
            SENSOR_IDS.add(checkRelatedEntity(serverSettings.extensions, EntityType.DATASTREAM, datastreamId, EntityType.SENSOR, deepInsertedObj));
            //Check ObservedProperty
            deepInsertedObj = new JSONObject("{\n"
                    + "        \"name\": \"Luminous Flux\",\n"
                    + "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html#LuminousFlux\",\n"
                    + "        \"description\": \"Luminous Flux or Luminous Power is the measure of the perceived power of light.\"\n"
                    + "      },\n");
            OBSPROP_IDS.add(checkRelatedEntity(serverSettings.extensions, EntityType.DATASTREAM, datastreamId, EntityType.OBSERVED_PROPERTY, deepInsertedObj));
            //Check Observation
            deepInsertedObj = new JSONObject("{\n"
                    + "          \"phenomenonTime\": \"2015-03-01T00:10:00.000Z\",\n"
                    + "          \"result\": 10\n"
                    + "        }\n");
            OBSERVATION_IDS.add(checkRelatedEntity(serverSettings.extensions, EntityType.DATASTREAM, datastreamId, EntityType.OBSERVATION, deepInsertedObj));
            DATASTREAM_IDS.add(datastreamId);

            /* Observation */
            urlParameters = "{\n"
                    + "  \"phenomenonTime\": \"2015-03-01T00:00:00Z\",\n"
                    + "  \"result\": 100,\n"
                    + "  \"FeatureOfInterest\": {\n"
                    + "  \t\"name\": \"A weather station.\",\n"
                    + "  \t\"description\": \"A weather station.\",\n"
                    + "  \t\"encodingType\": \"application/vnd.geo+json\",\n"
                    + "    \"feature\": {\n"
                    + "      \"type\": \"Point\",\n"
                    + "      \"coordinates\": [\n"
                    + "        -114.05,\n"
                    + "        51.05\n"
                    + "      ]\n"
                    + "    }\n"
                    + "  },\n"
                    + "  \"Datastream\":{\"@iot.id\": " + quoteIdForJson(datastreamId) + "}\n"
                    + "}";
            entity = postEntity(EntityType.OBSERVATION, urlParameters);
            Object obsId1 = entity.get(ControlInformation.ID);
            //Check FeaturOfInterest
            deepInsertedObj = new JSONObject("{\n"
                    + "  \"name\": \"A weather station.\",\n"
                    + "  \"description\": \"A weather station.\",\n"
                    + "  \"encodingType\": \"application/vnd.geo+json\",\n"
                    + "    \"feature\": {\n"
                    + "      \"type\": \"Point\",\n"
                    + "      \"coordinates\": [\n"
                    + "        -114.05,\n"
                    + "        51.05\n"
                    + "      ]\n"
                    + "    }\n"
                    + "  }\n");
            FOI_IDS.add(checkRelatedEntity(serverSettings.extensions, EntityType.OBSERVATION, obsId1, EntityType.FEATURE_OF_INTEREST, deepInsertedObj));
            OBSERVATION_IDS.add(obsId1);

        } catch (JSONException e) {
            LOGGER.error("Exception: ", e);
            Assert.fail("An Exception occurred during testing: " + e.getMessage());
        }
    }

    /**
     * This method is testing create or POST invalid entities. The response
     * should be 400 or 409 and the entity should not be accessible using GET.
     */
    @Test
    public void test04CreateInvalidEntities() {
        LOGGER.info("test04CreateInvalidEntities");
        try {
            /* Datastream */
            // Without Sensor
            String urlParameters = "{\n"
                    + "  \"unitOfMeasurement\": {\n"
                    + "    \"name\": \"Celsius\",\n"
                    + "    \"symbol\": \"degC\",\n"
                    + "    \"definition\": \"http://qudt.org/vocab/unit#DegreeCelsius\"\n"
                    + "  },\n"
                    + "  \"name\": \"test datastream.\",\n"
                    + "  \"description\": \"test datastream.\",\n"
                    + "  \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                    + "  \"Thing\": { \"@iot.id\": " + quoteIdForJson(THING_IDS.get(0)) + " },\n"
                    + "  \"ObservedProperty\":{ \"@iot.id\":" + quoteIdForJson(OBSPROP_IDS.get(0)) + "}\n"
                    + "}";
            postInvalidEntity(EntityType.DATASTREAM, urlParameters);
            //Without ObservedProperty
            urlParameters = "{\n"
                    + "  \"unitOfMeasurement\": {\n"
                    + "    \"name\": \"Celsius\",\n"
                    + "    \"symbol\": \"degC\",\n"
                    + "    \"definition\": \"http://qudt.org/vocab/unit#DegreeCelsius\"\n"
                    + "  },\n"
                    + "  \"name\": \"test datastream.\",\n"
                    + "  \"description\": \"test datastream.\",\n"
                    + "  \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                    + "  \"Thing\": { \"@iot.id\": " + quoteIdForJson(THING_IDS.get(0)) + " },\n"
                    + "  \"Sensor\": { \"@iot.id\": " + quoteIdForJson(SENSOR_IDS.get(0)) + " }\n"
                    + "}";
            postInvalidEntity(EntityType.DATASTREAM, urlParameters);
            //Without Things
            urlParameters = "{\n"
                    + "  \"unitOfMeasurement\": {\n"
                    + "    \"name\": \"Celsius\",\n"
                    + "    \"symbol\": \"degC\",\n"
                    + "    \"definition\": \"http://qudt.org/vocab/unit#DegreeCelsius\"\n"
                    + "  },\n"
                    + "  \"name\": \"test datastream.\",\n"
                    + "  \"description\": \"test datastream.\",\n"
                    + "  \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                    + "  \"ObservedProperty\":{ \"@iot.id\":" + quoteIdForJson(OBSPROP_IDS.get(0)) + "},\n"
                    + "  \"Sensor\": { \"@iot.id\": " + quoteIdForJson(SENSOR_IDS.get(0)) + " }\n"
                    + "}";
            postInvalidEntity(EntityType.DATASTREAM, urlParameters);

            /* Observation */
            //Create Thing and Datastream
            urlParameters = "{"
                    + "\"name\":\"This is a Test Thing From TestNG\","
                    + "\"description\":\"This is a Test Thing From TestNG\""
                    + "}";
            Object thingId = postEntity(EntityType.THING, urlParameters).get(ControlInformation.ID);
            THING_IDS.add(thingId);
            urlParameters = "{\n"
                    + "  \"unitOfMeasurement\": {\n"
                    + "    \"name\": \"Celsius\",\n"
                    + "    \"symbol\": \"degC\",\n"
                    + "    \"definition\": \"http://qudt.org/vocab/unit#DegreeCelsius\"\n"
                    + "  },\n"
                    + "  \"name\": \"test datastream.\",\n"
                    + "  \"description\": \"test datastream.\",\n"
                    + "  \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                    + "  \"Thing\": { \"@iot.id\": " + quoteIdForJson(thingId) + " },\n"
                    + "  \"ObservedProperty\":{ \"@iot.id\":" + quoteIdForJson(OBSPROP_IDS.get(0)) + "},\n"
                    + "  \"Sensor\": { \"@iot.id\": " + quoteIdForJson(SENSOR_IDS.get(0)) + " }\n"
                    + "}";
            Object datastreamId = postEntity(EntityType.DATASTREAM, urlParameters).get(ControlInformation.ID);
            DATASTREAM_IDS.add(datastreamId);
            //Without Datastream
            urlParameters = "{\n"
                    + "  \"phenomenonTime\": \"2015-03-01T00:40:00.000Z\",\n"
                    + "  \"result\": 8,\n"
                    + "  \"FeatureOfInterest\": {\"@iot.id\": " + quoteIdForJson(FOI_IDS.get(0)) + "}  \n"
                    + "}";
            postInvalidEntity(EntityType.OBSERVATION, urlParameters);
            //Without FOI and without Thing's Location
            urlParameters = "{\n"
                    + "  \"phenomenonTime\": \"2015-03-01T00:00:00.000Z\",\n"
                    + "  \"result\": 100,\n"
                    + "  \"Datastream\":{\"@iot.id\": " + quoteIdForJson(datastreamId) + "}\n"
                    + "}";
            postInvalidEntity(EntityType.OBSERVATION, urlParameters);

        } catch (JSONException e) {
            LOGGER.error("Exception: ", e);
            Assert.fail("An Exception occurred during testing: " + e.getMessage());
        }

    }

    /**
     * This method is testing partial update or PATCH. The response should be
     * 200 and only the properties in the PATCH body should be updated, and the
     * rest must be unchanged.
     */
    @Test
    public void test05PatchEntities() {
        LOGGER.info("test05PatchEntities");
        try {
            /* Thing */
            Object thingId = THING_IDS.get(0);
            JSONObject entity = getEntity(EntityType.THING, thingId);
            String urlParameters = "{\"description\":\"This is a PATCHED Test Thing From TestNG\"}";
            Map<String, Object> diffs = new HashMap<>();
            diffs.put("description", "This is a PATCHED Test Thing From TestNG");
            JSONObject updatedEntity = patchEntity(EntityType.THING, urlParameters, thingId);
            checkPatch(EntityType.THING, entity, updatedEntity, diffs);

            /* Location */
            Object locationId = LOCATION_IDS.get(0);
            entity = getEntity(EntityType.LOCATION, locationId);
            urlParameters = "{\"location\": { \"type\": \"Point\", \"coordinates\": [114.05, -50] }}";
            diffs = new HashMap<>();
            diffs.put("location", new JSONObject("{ \"type\": \"Point\", \"coordinates\": [114.05, -50] }}"));
            updatedEntity = patchEntity(EntityType.LOCATION, urlParameters, locationId);
            checkPatch(EntityType.LOCATION, entity, updatedEntity, diffs);

            /* HistoricalLocation */
            Object histLocId = HISTORICAL_LOCATION_IDS.get(0);
            entity = getEntity(EntityType.HISTORICAL_LOCATION, histLocId);
            urlParameters = "{\"time\": \"2015-07-01T00:00:00.000Z\"}";
            diffs = new HashMap<>();
            diffs.put("time", "2015-07-01T00:00:00.000Z");
            updatedEntity = patchEntity(EntityType.HISTORICAL_LOCATION, urlParameters, histLocId);
            checkPatch(EntityType.HISTORICAL_LOCATION, entity, updatedEntity, diffs);

            /* Sensor */
            Object sensorId = SENSOR_IDS.get(0);
            entity = getEntity(EntityType.SENSOR, sensorId);
            urlParameters = "{\"metadata\": \"PATCHED\"}";
            diffs = new HashMap<>();
            diffs.put("metadata", "PATCHED");
            updatedEntity = patchEntity(EntityType.SENSOR, urlParameters, sensorId);
            checkPatch(EntityType.SENSOR, entity, updatedEntity, diffs);

            /* ObserverdProperty */
            Object obsPropId = OBSPROP_IDS.get(0);
            entity = getEntity(EntityType.OBSERVED_PROPERTY, obsPropId);
            urlParameters = "{\"description\":\"PATCHED\"}";
            diffs = new HashMap<>();
            diffs.put("description", "PATCHED");
            updatedEntity = patchEntity(EntityType.OBSERVED_PROPERTY, urlParameters, obsPropId);
            checkPatch(EntityType.OBSERVED_PROPERTY, entity, updatedEntity, diffs);

            /* FeatureOfInterest */
            Object foiId = FOI_IDS.get(0);
            entity = getEntity(EntityType.FEATURE_OF_INTEREST, foiId);
            urlParameters = "{\"feature\":{ \"type\": \"Point\", \"coordinates\": [114.05, -51.05] }}";
            diffs = new HashMap<>();
            diffs.put("feature", new JSONObject("{ \"type\": \"Point\", \"coordinates\": [114.05, -51.05] }"));
            updatedEntity = patchEntity(EntityType.FEATURE_OF_INTEREST, urlParameters, foiId);
            checkPatch(EntityType.FEATURE_OF_INTEREST, entity, updatedEntity, diffs);

            /* Datastream */
            Object datastreamId = DATASTREAM_IDS.get(0);
            entity = getEntity(EntityType.DATASTREAM, datastreamId);
            urlParameters = "{\"description\": \"Patched Description\"}";
            diffs = new HashMap<>();
            diffs.put("description", "Patched Description");
            updatedEntity = patchEntity(EntityType.DATASTREAM, urlParameters, datastreamId);
            checkPatch(EntityType.DATASTREAM, entity, updatedEntity, diffs);
            //Second PATCH for UOM
            entity = updatedEntity;
            urlParameters = "{ \"unitOfMeasurement\": {\n"
                    + "    \"name\": \"Entropy2\",\n"
                    + "    \"symbol\": \"S2\",\n"
                    + "    \"definition\": \"http://qudt.org/vocab/unit#Entropy2\"\n"
                    + "  } }";
            diffs = new HashMap<>();
            diffs.put("unitOfMeasurement", new JSONObject("{\"name\": \"Entropy2\",\"symbol\": \"S2\",\"definition\": \"http://qudt.org/vocab/unit#Entropy2\"}"));
            updatedEntity = patchEntity(EntityType.DATASTREAM, urlParameters, datastreamId);
            checkPatch(EntityType.DATASTREAM, entity, updatedEntity, diffs);

            /* Observation */
            Object obsId1 = OBSERVATION_IDS.get(0);
            entity = getEntity(EntityType.OBSERVATION, obsId1);
            urlParameters = "{\"phenomenonTime\": \"2015-07-01T00:40:00.000Z\"}";
            diffs = new HashMap<>();
            diffs.put("phenomenonTime", "2015-07-01T00:40:00.000Z");
            updatedEntity = patchEntity(EntityType.OBSERVATION, urlParameters, obsId1);
            checkPatch(EntityType.OBSERVATION, entity, updatedEntity, diffs);

        } catch (JSONException e) {
            LOGGER.error("Exception: ", e);
            Assert.fail("An Exception occurred during testing: " + e.getMessage());
        }
    }

    /**
     * This method is testing update or PUT. The response should be 200 and all
     * the properties in the PUT body should be updated, and the rest must be
     * restored to their default value.
     */
    @Test
    public void test06PutEntities() {
        LOGGER.info("test06PutEntities");
        try {
            /* Thing */
            Object thingId = THING_IDS.get(0);
            JSONObject entity = getEntity(EntityType.THING, thingId);
            String urlParameters = "{"
                    + "\"name\":\"This is a Updated Test Thing From TestNG\","
                    + "\"description\":\"This is a Updated Test Thing From TestNG\""
                    + "}";
            Map<String, Object> diffs = new HashMap<>();
            diffs.put("name", "This is a Updated Test Thing From TestNG");
            diffs.put("description", "This is a Updated Test Thing From TestNG");
            JSONObject updatedEntity = updateEntity(EntityType.THING, urlParameters, thingId);
            checkPut(EntityType.THING, entity, updatedEntity, diffs);

            /* Location */
            Object locationId = LOCATION_IDS.get(0);
            entity = getEntity(EntityType.LOCATION, locationId);
            urlParameters = "{"
                    + "\"encodingType\":\"application/vnd.geo+json\","
                    + "\"name\":\"UPDATED NAME\","
                    + "\"description\":\"UPDATED DESCRIPTION\","
                    + "\"location\": { \"type\": \"Point\", \"coordinates\": [-114.05, 50] }}";
            diffs = new HashMap<>();
            diffs.put("name", "UPDATED NAME");
            diffs.put("description", "UPDATED DESCRIPTION");
            diffs.put("location", new JSONObject("{ \"type\": \"Point\", \"coordinates\": [-114.05, 50] }}"));
            updatedEntity = updateEntity(EntityType.LOCATION, urlParameters, locationId);
            checkPut(EntityType.LOCATION, entity, updatedEntity, diffs);

            /* HistoricalLocation */
            Object histLocId = HISTORICAL_LOCATION_IDS.get(0);
            entity = getEntity(EntityType.HISTORICAL_LOCATION, histLocId);
            urlParameters = "{\"time\": \"2015-08-01T00:00:00.000Z\"}";
            diffs = new HashMap<>();
            diffs.put("time", "2015-08-01T00:00:00.000Z");
            updatedEntity = updateEntity(EntityType.HISTORICAL_LOCATION, urlParameters, histLocId);
            checkPut(EntityType.HISTORICAL_LOCATION, entity, updatedEntity, diffs);

            /* Sensor */
            Object sensorId = SENSOR_IDS.get(0);
            entity = getEntity(EntityType.SENSOR, sensorId);
            urlParameters = "{"
                    + "\"name\": \"UPDATED\", "
                    + "\"description\": \"UPDATED\", "
                    + "\"encodingType\":\"application/pdf\", "
                    + "\"metadata\": \"UPDATED\"}";
            diffs = new HashMap<>();
            diffs.put("name", "UPDATED");
            diffs.put("description", "UPDATED");
            diffs.put("metadata", "UPDATED");
            updatedEntity = updateEntity(EntityType.SENSOR, urlParameters, sensorId);
            checkPut(EntityType.SENSOR, entity, updatedEntity, diffs);

            /* ObserverdProperty */
            Object obsPropId = OBSPROP_IDS.get(0);
            urlParameters = "{"
                    + "\"name\":\"QWERTY\", "
                    + "\"definition\": \"ZXCVB\", "
                    + "\"name\":\"POIUYTREW\","
                    + "\"description\":\"POIUYTREW\""
                    + "}";
            diffs = new HashMap<>();
            diffs.put("name", "QWERTY");
            diffs.put("definition", "ZXCVB");
            diffs.put("description", "POIUYTREW");
            diffs.put("name", "POIUYTREW");
            updatedEntity = updateEntity(EntityType.OBSERVED_PROPERTY, urlParameters, obsPropId);
            checkPut(EntityType.OBSERVED_PROPERTY, entity, updatedEntity, diffs);

            /* FeatureOfInterest */
            Object foiId = FOI_IDS.get(0);
            entity = getEntity(EntityType.FEATURE_OF_INTEREST, foiId);
            urlParameters = "{"
                    + "\"encodingType\":\"application/vnd.geo+json\","
                    + "\"feature\":{ \"type\": \"Point\", \"coordinates\": [-114.05, 51.05] }, "
                    + "\"description\":\"POIUYTREW\","
                    + "\"name\":\"POIUYTREW\""
                    + "}";
            diffs = new HashMap<>();
            diffs.put("feature", new JSONObject("{ \"type\": \"Point\", \"coordinates\": [-114.05, 51.05] }"));
            diffs.put("name", "POIUYTREW");
            diffs.put("description", "POIUYTREW");
            updatedEntity = updateEntity(EntityType.FEATURE_OF_INTEREST, urlParameters, foiId);
            checkPut(EntityType.FEATURE_OF_INTEREST, entity, updatedEntity, diffs);

            /* Datastream */
            Object datastreamId = DATASTREAM_IDS.get(0);
            entity = getEntity(EntityType.DATASTREAM, datastreamId);
            urlParameters = "{\n"
                    + "  \"name\": \"Data coming from sensor on ISS.\",\n"
                    + "  \"description\": \"Data coming from sensor on ISS.\",\n"
                    + "  \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Observation\",\n"
                    + "  \"unitOfMeasurement\": {\n"
                    + "    \"name\": \"Entropy\",\n"
                    + "    \"symbol\": \"S\",\n"
                    + "    \"definition\": \"http://qudt.org/vocab/unit#Entropy\"\n"
                    + "  }\n"
                    + "}\n";
            diffs = new HashMap<>();
            diffs.put("name", "Data coming from sensor on ISS.");
            diffs.put("description", "Data coming from sensor on ISS.");
            diffs.put("observationType", "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Observation");
            diffs.put("unitOfMeasurement", new JSONObject("{\"name\": \"Entropy\",\"symbol\": \"S\",\"definition\": \"http://qudt.org/vocab/unit#Entropy\"}"));
            updatedEntity = updateEntity(EntityType.DATASTREAM, urlParameters, datastreamId);
            checkPut(EntityType.DATASTREAM, entity, updatedEntity, diffs);

            /* Observation */
            Object obsId1 = OBSERVATION_IDS.get(0);
            entity = getEntity(EntityType.OBSERVATION, obsId1);
            urlParameters = "{\"result\": \"99\", \"phenomenonTime\": \"2015-08-01T00:40:00.000Z\"}";
            diffs = new HashMap<>();
            diffs.put("result", "99");
            diffs.put("phenomenonTime", "2015-08-01T00:40:00.000Z");
            updatedEntity = updateEntity(EntityType.OBSERVATION, urlParameters, obsId1);
            checkPut(EntityType.OBSERVATION, entity, updatedEntity, diffs);

        } catch (JSONException e) {
            LOGGER.error("Exception: ", e);
            Assert.fail("An Exception occurred during testing: " + e.getMessage());
        }
    }

    //TODO: Add invalid PATCH test for other entities when it is implemented in the service
    /**
     * This method is testing invalid partial update or PATCH. The PATCH request
     * is invalid if the body contains related entities as inline content.
     */
    @Test
    public void test07InvalidPatchEntities() {
        LOGGER.info("test07InvalidPatchEntities");
        /**
         * Thing *
         */
        Object thingId = THING_IDS.get(0);
        String urlParameters = "{\"Locations\": [\n"
                + "    {\n"
                + "      \"name\": \"West Roof\",\n"
                + "      \"description\": \"West Roof\",\n"
                + "      \"location\": { \"type\": \"Point\", \"coordinates\": [-117.05, 51.05] },\n"
                + "      \"encodingType\": \"application/vnd.geo+json\"\n"
                + "    }\n"
                + "  ]}";
        invalidPatchEntity(EntityType.THING, urlParameters, thingId);
        urlParameters = "{\"Datastreams\": [\n"
                + "    {\n"
                + "      \"unitOfMeasurement\": {\n"
                + "        \"name\": \"Lumen\",\n"
                + "        \"symbol\": \"lm\",\n"
                + "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#Lumen\"\n"
                + "      }}]}";
        invalidPatchEntity(EntityType.THING, urlParameters, thingId);

//        /** Location **/
//        Object locationId = locationIds.get(0);
//        urlParameters = "{\"Things\":[{\"description\":\"Orange\"}]}";
//        invalidPatchEntity(EntityType.LOCATION, urlParameters, locationId);
//
//        /** HistoricalLocation **/
//        Object histLocId = historicalLocationIds.get(0);
//        urlParameters = "{\"time\": \"2015-07-01T00:00:00.000Z\"}";
//        invalidPatchEntity(EntityType.HISTORICAL_LOCATION, urlParameters, histLocId);
//
        /**
         * Sensor *
         */
        Object sensorId = SENSOR_IDS.get(0);
        urlParameters = "{\"Datastreams\": [\n"
                + "    {\n"
                + "      \"unitOfMeasurement\": {\n"
                + "        \"name\": \"Lumen\",\n"
                + "        \"symbol\": \"lm\",\n"
                + "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#Lumen\"}\n"
                + "        ,\"Thing\":{\"@iot.id\":" + quoteIdForJson(thingId) + "}"
                + "      }]}";
        invalidPatchEntity(EntityType.SENSOR, urlParameters, sensorId);

        /**
         * ObserverdProperty *
         */
        Object obsPropId = OBSPROP_IDS.get(0);
        urlParameters = "{\"Datastreams\": [\n"
                + "    {\n"
                + "      \"unitOfMeasurement\": {\n"
                + "        \"name\": \"Lumen\",\n"
                + "        \"symbol\": \"lm\",\n"
                + "        \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#Lumen\"}\n"
                + "        ,\"Thing\":{\"@iot.id\":" + quoteIdForJson(thingId) + "}"
                + "      }]}";
        invalidPatchEntity(EntityType.OBSERVED_PROPERTY, urlParameters, obsPropId);

//        /** FeatureOfInterest **/
//        Object foiId = foiIds.get(0);
//        urlParameters = "{\"feature\":{ \"type\": \"Point\", \"coordinates\": [114.05, -51.05] }}";
//        invalidPatchEntity(EntityType.FEATURE_OF_INTEREST, urlParameters, foiId);
        /**
         * Datastream *
         */
        Object datastreamId = DATASTREAM_IDS.get(0);
        urlParameters = "{\"ObservedProperty\": {\n"
                + "  \t\"name\": \"Count\",\n"
                + "\t\"definition\": \"http://qudt.org/vocab/unit#Dimensionless\",\n"
                + "\t\"name\": \"Count is a dimensionless property.\",\n"
                + "\t\"description\": \"Count is a dimensionless property.\"\n"
                + "  } }";
        invalidPatchEntity(EntityType.DATASTREAM, urlParameters, datastreamId);
        urlParameters = "{\"Sensor\": {\n"
                + "  \t\"name\": \"Acme Traffic 2000\",  \n"
                + "  \t\"description\": \"Acme Traffic 2000\",  \n"
                + "  \t\"encodingType\": \"application/pdf\",\n"
                + "  \t\"metadata\": \"Traffic counting device\"\n"
                + "  }}";
        invalidPatchEntity(EntityType.DATASTREAM, urlParameters, datastreamId);
        urlParameters = "{"
                + "\"Thing\": {"
                + "  \"name\": \"test\","
                + "  \"description\": \"test\""
                + " }"
                + "}";
        invalidPatchEntity(EntityType.DATASTREAM, urlParameters, datastreamId);
        urlParameters = "{\"Observations\": [\n"
                + "    {\n"
                + "      \"phenomenonTime\": \"2015-03-01T00:00:00Z\",\n"
                + "      \"result\": 92122,\n"
                + "      \"resultQuality\": \"High\"\n"
                + "    }\n"
                + "  ]}";
        invalidPatchEntity(EntityType.DATASTREAM, urlParameters, datastreamId);

//        /** Observation **/
//        Object obsId1 = observationIds.get(0);
//        urlParameters = "{\"phenomenonTime\": \"2015-07-01T00:40:00.000Z\"}";
//        invalidPatchEntity(EntityType.OBSERVATION, urlParameters, obsId1);
    }

    /**
     * This method is testing DELETE and its integrity constraint. The response
     * should be 200. After DELETE the GET request to that entity should return
     * 404.
     */
    @Test
    public void test08DeleteEntities() {
        LOGGER.info("test08DeleteEntities");
        for (int i = 0; i < OBSERVATION_IDS.size(); i++) {
            deleteEntity(EntityType.OBSERVATION, OBSERVATION_IDS.get(i));
        }
        for (int i = 0; i < FOI_IDS.size(); i++) {
            deleteEntity(EntityType.FEATURE_OF_INTEREST, FOI_IDS.get(i));
        }
        for (int i = 0; i < DATASTREAM_IDS.size(); i++) {
            deleteEntity(EntityType.DATASTREAM, DATASTREAM_IDS.get(i));
        }
        for (int i = 0; i < OBSPROP_IDS.size(); i++) {
            deleteEntity(EntityType.OBSERVED_PROPERTY, OBSPROP_IDS.get(i));
        }
        for (int i = 0; i < SENSOR_IDS.size(); i++) {
            deleteEntity(EntityType.SENSOR, SENSOR_IDS.get(i));
        }
        for (int i = 0; i < HISTORICAL_LOCATION_IDS.size(); i++) {
            deleteEntity(EntityType.HISTORICAL_LOCATION, HISTORICAL_LOCATION_IDS.get(i));
        }
        for (int i = 0; i < LOCATION_IDS.size(); i++) {
            deleteEntity(EntityType.LOCATION, LOCATION_IDS.get(i));
        }
        for (int i = 0; i < THING_IDS.size(); i++) {
            deleteEntity(EntityType.THING, THING_IDS.get(i));
        }

        checkDeleteIntegrityConstraint();
    }

    /**
     * This method is testing DELETE request for a nonexistent entity. The
     * response should be 404.
     */
    @Test
    public void test09DeleteNoneexistentEntities() {
        LOGGER.info("test09DeleteNoneexistentEntities");
        for (EntityType type : serverSettings.enabledEntityTypes) {
            deleteNonExsistentEntity(type);
        }
    }

    /**
     * This is helper method for checking the integrity containt of DELETE. For
     * each entity, it checks after deleting, it confirm the deletion of its
     * related entities mentioned in the integrity constraint of the
     * specification.
     */
    private void checkDeleteIntegrityConstraint() {
        //Thing
        createEntitiesForDelete();
        deleteEntity(EntityType.THING, THING_IDS.get(0));
        List<EntityType> entityTypes = new ArrayList<>();
        entityTypes.add(EntityType.THING);
        entityTypes.add(EntityType.DATASTREAM);
        entityTypes.add(EntityType.HISTORICAL_LOCATION);
        entityTypes.add(EntityType.OBSERVATION);
        checkNotExisting(entityTypes);
        entityTypes.clear();
        entityTypes.add(EntityType.LOCATION);
        entityTypes.add(EntityType.SENSOR);
        entityTypes.add(EntityType.OBSERVED_PROPERTY);
        entityTypes.add(EntityType.FEATURE_OF_INTEREST);
        checkExisting(entityTypes);

        //Datastream
        createEntitiesForDelete();
        deleteEntity(EntityType.DATASTREAM, DATASTREAM_IDS.get(0));
        entityTypes.clear();
        entityTypes.add(EntityType.DATASTREAM);
        entityTypes.add(EntityType.OBSERVATION);
        checkNotExisting(entityTypes);
        entityTypes.clear();
        entityTypes.add(EntityType.THING);
        entityTypes.add(EntityType.SENSOR);
        entityTypes.add(EntityType.OBSERVED_PROPERTY);
        entityTypes.add(EntityType.FEATURE_OF_INTEREST);
        entityTypes.add(EntityType.LOCATION);
        entityTypes.add(EntityType.HISTORICAL_LOCATION);
        checkExisting(entityTypes);

        //Loation
        createEntitiesForDelete();
        deleteEntity(EntityType.LOCATION, LOCATION_IDS.get(0));
        entityTypes.clear();
        entityTypes.add(EntityType.LOCATION);
        entityTypes.add(EntityType.HISTORICAL_LOCATION);
        checkNotExisting(entityTypes);
        entityTypes.clear();
        entityTypes.add(EntityType.THING);
        entityTypes.add(EntityType.SENSOR);
        entityTypes.add(EntityType.OBSERVED_PROPERTY);
        entityTypes.add(EntityType.FEATURE_OF_INTEREST);
        entityTypes.add(EntityType.DATASTREAM);
        entityTypes.add(EntityType.OBSERVATION);
        checkExisting(entityTypes);

        //HistoricalLoation
        createEntitiesForDelete();
        deleteEntity(EntityType.HISTORICAL_LOCATION, HISTORICAL_LOCATION_IDS.get(0));
        entityTypes.clear();
        entityTypes.add(EntityType.HISTORICAL_LOCATION);
        checkNotExisting(entityTypes);
        entityTypes.clear();
        entityTypes.add(EntityType.THING);
        entityTypes.add(EntityType.SENSOR);
        entityTypes.add(EntityType.OBSERVED_PROPERTY);
        entityTypes.add(EntityType.FEATURE_OF_INTEREST);
        entityTypes.add(EntityType.DATASTREAM);
        entityTypes.add(EntityType.OBSERVATION);
        entityTypes.add(EntityType.LOCATION);
        checkExisting(entityTypes);

        //Sensor
        createEntitiesForDelete();
        deleteEntity(EntityType.SENSOR, SENSOR_IDS.get(0));
        entityTypes.clear();
        entityTypes.add(EntityType.SENSOR);
        entityTypes.add(EntityType.DATASTREAM);
        entityTypes.add(EntityType.OBSERVATION);
        checkNotExisting(entityTypes);
        entityTypes.clear();
        entityTypes.add(EntityType.THING);
        entityTypes.add(EntityType.OBSERVED_PROPERTY);
        entityTypes.add(EntityType.FEATURE_OF_INTEREST);
        entityTypes.add(EntityType.LOCATION);
        entityTypes.add(EntityType.HISTORICAL_LOCATION);
        checkExisting(entityTypes);

        //ObservedProperty
        createEntitiesForDelete();
        deleteEntity(EntityType.OBSERVED_PROPERTY, OBSPROP_IDS.get(0));
        entityTypes.clear();
        entityTypes.add(EntityType.OBSERVED_PROPERTY);
        entityTypes.add(EntityType.DATASTREAM);
        entityTypes.add(EntityType.OBSERVATION);
        checkNotExisting(entityTypes);
        entityTypes.clear();
        entityTypes.add(EntityType.THING);
        entityTypes.add(EntityType.SENSOR);
        entityTypes.add(EntityType.FEATURE_OF_INTEREST);
        entityTypes.add(EntityType.LOCATION);
        entityTypes.add(EntityType.HISTORICAL_LOCATION);
        checkExisting(entityTypes);

        //FeatureOfInterest
        createEntitiesForDelete();
        deleteEntity(EntityType.FEATURE_OF_INTEREST, FOI_IDS.get(0));
        entityTypes.clear();
        entityTypes.add(EntityType.FEATURE_OF_INTEREST);
        entityTypes.add(EntityType.OBSERVATION);
        checkNotExisting(entityTypes);
        entityTypes.clear();
        entityTypes.add(EntityType.THING);
        entityTypes.add(EntityType.SENSOR);
        entityTypes.add(EntityType.OBSERVED_PROPERTY);
        entityTypes.add(EntityType.LOCATION);
        entityTypes.add(EntityType.HISTORICAL_LOCATION);
        entityTypes.add(EntityType.DATASTREAM);
        checkExisting(entityTypes);

        //Observation
        createEntitiesForDelete();
        deleteEntity(EntityType.OBSERVATION, OBSERVATION_IDS.get(0));
        entityTypes.clear();
        entityTypes.add(EntityType.OBSERVATION);
        checkNotExisting(entityTypes);
        entityTypes.clear();
        entityTypes.add(EntityType.THING);
        entityTypes.add(EntityType.SENSOR);
        entityTypes.add(EntityType.OBSERVED_PROPERTY);
        entityTypes.add(EntityType.FEATURE_OF_INTEREST);
        entityTypes.add(EntityType.DATASTREAM);
        entityTypes.add(EntityType.HISTORICAL_LOCATION);
        entityTypes.add(EntityType.LOCATION);
        checkExisting(entityTypes);
    }

    /**
     * This method created the URL string for the entity with specific id and
     * then send a GET request to that URL.
     *
     * @param entityType Entity type in from EntityType enum
     * @param id The id of requested entity
     * @return The requested entity in the format of JSON Object.
     */
    private JSONObject getEntity(EntityType entityType, Object id) {
        if (id == null) {
            return null;
        }
        String urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, id, null, null);
        try {
            return new JSONObject(HTTPMethods.doGet(urlString).get("response").toString());
        } catch (JSONException e) {
            LOGGER.error("Exception: ", e);
            Assert.fail("An Exception occurred during testing: " + e.getMessage());
            return null;
        }
    }

    /**
     * This method created the URL string for the entity and then POST the
     * entity with urlParameters to that URL.
     *
     * @param entityType Entity type in from EntityType enum
     * @param urlParameters POST body
     * @return The created entity in the form of JSON Object
     */
    private JSONObject postEntity(EntityType entityType, String urlParameters) {
        String urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, null, null, null);
        try {
            Map<String, Object> responseMap = HTTPMethods.doPost(urlString, urlParameters);
            int responseCode = Integer.parseInt(responseMap.get("response-code").toString());
            String message = "Error during creation of entity " + entityType.name();
            Assert.assertEquals(message, 201, responseCode);

            String response = responseMap.get("response").toString();
            Object id = response.substring(response.indexOf("(") + 1, response.indexOf(")"));

            urlString = urlString + "(" + id + ")";
            responseMap = HTTPMethods.doGet(urlString);
            responseCode = Integer.parseInt(responseMap.get("response-code").toString());
            message = "The POSTed entity is not created.";
            Assert.assertEquals(message, 200, responseCode);

            JSONObject result = new JSONObject(responseMap.get("response").toString());
            return result;
        } catch (JSONException e) {
            LOGGER.error("Exception: ", e);
            Assert.fail("An Exception occurred during testing: " + e.getMessage());
            return null;
        }
    }

    /**
     * This helper method is sending invalid POST request and confirm that the
     * response is correct based on specification.
     *
     * @param entityType Entity type in from EntityType enum
     * @param urlParameters POST body (invalid)
     */
    private void postInvalidEntity(EntityType entityType, String urlParameters) {
        String urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, null, null, null);

        Map<String, Object> responseMap = HTTPMethods.doPost(urlString, urlParameters);
        int responseCode = Integer.parseInt(responseMap.get("response-code").toString());
        String message = "The  " + entityType.name() + " should not be created due to integrity constraints.";
        Assert.assertTrue(message, responseCode == 400 || responseCode == 409);

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
        Map<String, Object> responseMap = HTTPMethods.doDelete(urlString);
        int responseCode = Integer.parseInt(responseMap.get("response-code").toString());
        String message = "DELETE does not work properly for " + entityType + " with id " + id + ". Returned with response code " + responseCode + ".";
        Assert.assertEquals(message, 200, responseCode);

        responseMap = HTTPMethods.doGet(urlString);
        responseCode = Integer.parseInt(responseMap.get("response-code").toString());
        message = "Deleted entity was not actually deleted : " + entityType + "(" + id + ").";
        Assert.assertEquals(message, 404, responseCode);
    }

    /**
     * This method create the URL string for a nonexistent entity and send the
     * DELETE request to that URL and confirm that the response is correct based
     * on specification.
     *
     * @param entityType Entity type in from EntityType enum
     */
    private void deleteNonExsistentEntity(EntityType entityType) {
        Object id = Long.MAX_VALUE;
        String urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, id, null, null);
        Map<String, Object> responseMap = HTTPMethods.doDelete(urlString);
        int responseCode = Integer.parseInt(responseMap.get("response-code").toString());
        String message = "DELETE does not work properly for nonexistent " + entityType + " with id " + id + ". Returned with response code " + responseCode + ".";
        Assert.assertEquals(message, 404, responseCode);

    }

    /**
     * This method created the URL string for the entity with specific idand
     * then PUT the entity with urlParameters to that URL.
     *
     * @param entityType Entity type in from EntityType enum
     * @param urlParameters The PUT body
     * @param id The id of requested entity
     * @return The updated entity in the format of JSON Object
     */
    private JSONObject updateEntity(EntityType entityType, String urlParameters, Object id) {
        String urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, id, null, null);
        try {
            Map<String, Object> responseMap = HTTPMethods.doPut(urlString, urlParameters);
            int responseCode = Integer.parseInt(responseMap.get("response-code").toString());
            String message = "Error during updating(PUT) of entity " + entityType.name();
            Assert.assertEquals(message, 200, responseCode);

            responseMap = HTTPMethods.doGet(urlString);
            JSONObject result = new JSONObject(responseMap.get("response").toString());
            return result;

        } catch (JSONException e) {
            LOGGER.error("Exception: ", e);
            Assert.fail("An Exception occurred during testing: " + e.getMessage());
            return null;
        }
    }

    /**
     * This method created the URL string for the entity with specific id and
     * then PATCH the entity with urlParameters to that URL.
     *
     * @param entityType Entity type in from EntityType enum
     * @param urlParameters The PATCH body
     * @param id The id of requested entity
     * @return The patched entity in the format of JSON Object
     */
    private JSONObject patchEntity(EntityType entityType, String urlParameters, Object id) {
        String urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, id, null, null);
        try {

            Map<String, Object> responseMap = HTTPMethods.doPatch(urlString, urlParameters);
            int responseCode = Integer.parseInt(responseMap.get("response-code").toString());
            String message = "Error during updating(PATCH) of entity " + entityType.name();
            Assert.assertEquals(message, 200, responseCode);
            responseMap = HTTPMethods.doGet(urlString);
            JSONObject result = new JSONObject(responseMap.get("response").toString());
            return result;

        } catch (JSONException e) {
            LOGGER.error("Exception: ", e);
            Assert.fail("An Exception occurred during testing: " + e.getMessage());
            return null;
        }
    }

    /**
     * This method created the URL string for the entity with specific id and
     * then PATCH invalid entity with urlParameters to that URL and confirms
     * that the response is correct based on specification.
     *
     * @param entityType Entity type in from EntityType enum
     * @param urlParameters The PATCH body (invalid)
     * @param id The id of requested entity
     */
    private void invalidPatchEntity(EntityType entityType, String urlParameters, Object id) {
        String urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, id, null, null);

        Map<String, Object> responseMap = HTTPMethods.doPatch(urlString, urlParameters);
        int responseCode = Integer.parseInt(responseMap.get("response-code").toString());
        String message = "Error: Patching related entities inline must be illegal for entity " + entityType.name();
        Assert.assertEquals(message, 400, responseCode);

    }

    /**
     * Check the patched entity properties are updates correctly
     *
     * @param entityType Entity type in from EntityType enum
     * @param oldEntity The old properties of the patched entity
     * @param newEntity The updated properties of the patched entity
     * @param diffs The properties that supposed to be updated besed on the
     * request due to the specification
     */
    private void checkPatch(EntityType entityType, JSONObject oldEntity, JSONObject newEntity, Map diffs) {
        try {
            for (EntityType.EntityProperty property : entityType.getProperties()) {
                if (diffs.containsKey(property.name)) {
                    String message = "PATCH was not applied correctly for " + entityType + "'s " + property.name + ".";
                    Assert.assertEquals(message, diffs.get(property.name).toString(), newEntity.get(property.name).toString());
                } else if (newEntity.has(property.name) && oldEntity.has(property.name)) {
                    String message = "PATCH was not applied correctly for " + entityType + "'s " + property.name + ".";
                    Assert.assertEquals(message, oldEntity.get(property.name).toString(), newEntity.get(property.name).toString());
                } else {
                    String message = "PATCH was not applied correctly for " + entityType + "'s " + property.name + ".";
                    Assert.assertEquals(message, oldEntity.has(property.name), newEntity.has(property.name));
                }
            }
        } catch (JSONException e) {
            LOGGER.error("Exception: ", e);
            Assert.fail("An Exception occurred during testing: " + e.getMessage());
        }
    }

    /**
     * Check the updated entity properties are updates correctly
     *
     * @param entityType Entity type in from EntityType enum
     * @param oldEntity The old properties of the updated entity
     * @param newEntity The updated properties of the updated entity
     * @param diffs The properties that supposed to be updated based on the
     * request due to the specification
     */
    private void checkPut(EntityType entityType, JSONObject oldEntity, JSONObject newEntity, Map diffs) {
        try {
            for (EntityType.EntityProperty property : entityType.getProperties()) {
                if (diffs.containsKey(property.name)) {
                    String message = "PUT was not applied correctly for " + entityType + ".";
                    Assert.assertEquals(message, diffs.get(property.name).toString(), newEntity.get(property.name).toString());
                } else {
//                    String message =  "PUT was not applied correctly for "+entityType+".";Assert.assertEquals(message, oldEntity.get(property),newEntity.get(property));
                }
            }

        } catch (JSONException e) {
            LOGGER.error("Exception: ", e);
            Assert.fail("An Exception occurred during testing: " + e.getMessage());
        }
    }

    /**
     * Check the FeatureOfInterest is created automatically correctly if not
     * inserted in Observation
     *
     * @param obsId The observation id
     * @param locationObj The Location object that the FOI is supposed to be
     * created based on that
     * @param expectedFOIId The id of the FOI linked to the Observation
     * @return The id of FOI
     */
    private Object checkAutomaticInsertionOfFOI(Object obsId, JSONObject locationObj, Object expectedFOIId) {
        String urlString = serverSettings.serviceUrl + "/Observations(" + quoteIdForUrl(obsId) + ")/FeatureOfInterest";
        try {
            Map<String, Object> responseMap = HTTPMethods.doGet(urlString);
            int responseCode = Integer.parseInt(responseMap.get("response-code").toString());
            String message = "ERROR: FeatureOfInterest was not automatically created.";
            Assert.assertEquals(message, 200, responseCode);
            JSONObject result = new JSONObject(responseMap.get("response").toString());
            Object id = result.get(ControlInformation.ID);
            if (expectedFOIId != null) {
                message = "ERROR: the Observation should have linked to FeatureOfInterest with ID: " + expectedFOIId + ", but it is linked for FeatureOfInterest with Id: " + id + ".";
                Assert.assertEquals(message, expectedFOIId, id);
            }
            message = "ERROR: Automatic created FeatureOfInterest does not match last Location of that Thing.";
            Assert.assertEquals(message, locationObj.getJSONObject("location").toString(), result.getJSONObject("feature").toString());
            return id;
        } catch (JSONException e) {
            LOGGER.error("Exception: ", e);
            Assert.fail("An Exception occurred during testing: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Check the related entity of a given entity
     *
     * @param parentEntityType The given entity type
     * @param parentId The given entity id
     * @param relationEntityType The relation entity type
     * @param relationObj The expected related entity object
     * @return The id of related object
     */
    private Object checkRelatedEntity(Set<Extension> extensions, EntityType parentEntityType, Object parentId, EntityType relationEntityType, JSONObject relationObj) {
        boolean isCollection = true;
        String urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, parentEntityType, parentId, relationEntityType, null);
        if (parentEntityType.getRelations(extensions).contains(relationEntityType.singular)) {
            isCollection = false;
        }

        try {
            Map<String, Object> responseMap = HTTPMethods.doGet(urlString);
            int responseCode = Integer.parseInt(responseMap.get("response-code").toString());
            String message = "ERROR: Deep inserted " + relationEntityType + " was not created or linked to " + parentEntityType;
            Assert.assertEquals(message, 200, responseCode);

            JSONObject result = new JSONObject(responseMap.get("response").toString());
            if (isCollection == true) {
                result = result.getJSONArray("value").getJSONObject(0);
            }
            Iterator iterator = relationObj.keys();
            while (iterator.hasNext()) {
                String key = iterator.next().toString();
                message = "ERROR: Deep inserted " + relationEntityType + " is not created correctly.";
                Assert.assertEquals(message, relationObj.get(key).toString(), result.get(key).toString());
            }
            return result.get(ControlInformation.ID);
        } catch (JSONException e) {
            LOGGER.error("Exception: ", e);
            Assert.fail("An Exception occurred during testing: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Check the Observation have the resultTime even if it is null
     *
     * @param observation The observation JSON object
     * @param resultTimeValue The expected value of resultTime
     */
    private void checkForObservationResultTime(JSONObject observation, String resultTimeValue) {
        try {
            if (resultTimeValue == null) {
                String message = "The resultTime of the Observation " + observation.get(ControlInformation.ID) + " should have been null but it is now \"" + observation.get("resultTime").toString() + "\".";
                Assert.assertEquals(message, "null", observation.get("resultTime").toString());
            } else {
                String message = "The resultTime of the Observation " + observation.get(ControlInformation.ID) + " should have been \"" + resultTimeValue + "\" but it is now \"" + observation.get("resultTime").toString() + "\".";
                Assert.assertEquals(message, resultTimeValue, observation.get("resultTime").toString());
            }
        } catch (JSONException e) {
            LOGGER.error("Exception: ", e);
            Assert.fail("An Exception occurred during testing: " + e.getMessage());
        }
    }

    /**
     * Check the database is empty of certain entity types
     *
     * @param entityTypes List of entity types
     */
    private void checkNotExisting(List<EntityType> entityTypes) {
        for (EntityType entityType : entityTypes) {
            String urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, null, null, null);
            Map<String, Object> responseMap = HTTPMethods.doGet(urlString);
            try {
                JSONObject result = new JSONObject(responseMap.get("response").toString());
                JSONArray array = result.getJSONArray("value");
                String message = entityType + " is created although it shouldn't.";
                Assert.assertEquals(message, 0, array.length());
            } catch (JSONException e) {
                LOGGER.error("Exception: ", e);
                Assert.fail("An Exception occurred during testing: " + e.getMessage());
            }
        }
    }

    /**
     * Check there are some entityes for certain entity types
     *
     * @param entityTypes List of entity types
     */
    private void checkExisting(List<EntityType> entityTypes) {
        for (EntityType entityType : entityTypes) {
            String urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, entityType, null, null, null);
            Map<String, Object> responseMap = HTTPMethods.doGet(urlString);
            try {
                JSONObject result = new JSONObject(responseMap.get("response").toString());
                JSONArray array = result.getJSONArray("value");
                String message = entityType + " is created although it shouldn't.";
                Assert.assertTrue(message, array.length() > 0);
            } catch (JSONException e) {
                LOGGER.error("Exception: ", e);
                Assert.fail("An Exception occurred during testing: " + e.getMessage());
            }
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
        if (serverSettings.hasActuation) {
            deleteEntityType(EntityType.ACTUATOR);
            deleteEntityType(EntityType.TASK);
            deleteEntityType(EntityType.TASKING_CAPABILITY);
        }
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
                Map<String, Object> responseMap = HTTPMethods.doGet(urlString);
                int responseCode = Integer.parseInt(responseMap.get("response-code").toString());
                JSONObject result = new JSONObject(responseMap.get("response").toString());
                array = result.getJSONArray("value");
                for (int i = 0; i < array.length(); i++) {
                    Object id = array.getJSONObject(i).get(ControlInformation.ID);
                    deleteEntity(entityType, id);
                }
            } catch (JSONException e) {
                LOGGER.error("Exception: ", e);
                Assert.fail("An Exception occurred during testing: " + e.getMessage());
            }
        } while (array.length() > 0);
    }

    /**
     * Create entities as a pre-process for testing DELETE.
     */
    private void createEntitiesForDelete() {
        try {

            deleteEverythings();
            THING_IDS.clear();
            LOCATION_IDS.clear();
            HISTORICAL_LOCATION_IDS.clear();
            DATASTREAM_IDS.clear();
            SENSOR_IDS.clear();
            OBSERVATION_IDS.clear();
            OBSPROP_IDS.clear();
            FOI_IDS.clear();

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
                    + "                \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/unit/Instances.html#Lumen\"\n"
                    + "            },\n"
                    + "            \"name\": \"datastream 1\",\n"
                    + "            \"description\": \"datastream 1\",\n"
                    + "            \"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                    + "            \"ObservedProperty\": {\n"
                    + "                \"name\": \"Luminous Flux\",\n"
                    + "                \"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html#LuminousFlux\",\n"
                    + "                \"description\": \"observedProperty 1\"\n"
                    + "            },\n"
                    + "            \"Sensor\": {\n"
                    + "                \"name\": \"sensor 1\",\n"
                    + "                \"description\": \"sensor 1\",\n"
                    + "                \"encodingType\": \"application/pdf\",\n"
                    + "                \"metadata\": \"Light flux sensor\"\n"
                    + "            }\n"
                    + "        }\n"
                    + "    ]\n"
                    + "}";
            String urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.THING, null, null, null);
            Map<String, Object> responseMap = HTTPMethods.doPost(urlString, urlParameters);
            String response = responseMap.get("response").toString();
            THING_IDS.add(Utils.idObjectFromPostResult(response));

            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.THING, THING_IDS.get(0), EntityType.LOCATION, null);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.get("response").toString();
            JSONArray array = new JSONObject(response).getJSONArray("value");
            LOCATION_IDS.add(array.getJSONObject(0).get(ControlInformation.ID));

            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.THING, THING_IDS.get(0), EntityType.DATASTREAM, null);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.get("response").toString();
            array = new JSONObject(response).getJSONArray("value");
            DATASTREAM_IDS.add(array.getJSONObject(0).get(ControlInformation.ID));

            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.DATASTREAM, DATASTREAM_IDS.get(0), EntityType.SENSOR, null);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.get("response").toString();
            SENSOR_IDS.add(new JSONObject(response).get(ControlInformation.ID));
            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.DATASTREAM, DATASTREAM_IDS.get(0), EntityType.OBSERVED_PROPERTY, null);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.get("response").toString();
            OBSPROP_IDS.add(new JSONObject(response).get(ControlInformation.ID));

            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.THING, THING_IDS.get(0), EntityType.HISTORICAL_LOCATION, null);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.get("response").toString();
            array = new JSONObject(response).getJSONArray("value");
            HISTORICAL_LOCATION_IDS.add(array.getJSONObject(0).get(ControlInformation.ID));

            //Observations
            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.DATASTREAM, DATASTREAM_IDS.get(0), EntityType.OBSERVATION, null);
            urlParameters = "{\n"
                    + "  \"phenomenonTime\": \"2015-03-01T00:00:00Z\",\n"
                    + "  \"result\": 1 \n"
                    + "   }";
            responseMap = HTTPMethods.doPost(urlString, urlParameters);
            response = responseMap.get("response").toString();
            OBSERVATION_IDS.add(Utils.idObjectFromPostResult(response));

            //FeatureOfInterest
            urlString = ServiceURLBuilder.buildURLString(serverSettings.serviceUrl, EntityType.OBSERVATION, OBSERVATION_IDS.get(0), EntityType.FEATURE_OF_INTEREST, null);
            responseMap = HTTPMethods.doGet(urlString);
            response = responseMap.get("response").toString();
            FOI_IDS.add(new JSONObject(response).get(ControlInformation.ID));

        } catch (JSONException e) {
            LOGGER.error("Exception: ", e);
            Assert.fail("An Exception occurred during testing: " + e.getMessage());
        }

    }
}
