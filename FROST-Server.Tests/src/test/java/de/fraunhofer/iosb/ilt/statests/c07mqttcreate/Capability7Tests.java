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
package de.fraunhofer.iosb.ilt.statests.c07mqttcreate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.JsonNode;
import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.ControlInformation;
import de.fraunhofer.iosb.ilt.statests.util.EntityHelper2;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import de.fraunhofer.iosb.ilt.statests.util.Utils;
import de.fraunhofer.iosb.ilt.statests.util.model.EntityType;
import de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper2;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TestMethodOrder(MethodOrderer.MethodName.class)
public abstract class Capability7Tests extends AbstractTestClass {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Capability7Tests.class);

    private static MqttHelper2 mqttHelper;
    private static EntityHelper2 entityHelper;

    public Capability7Tests(ServerVersion version) {
        super(version);
    }

    @Override
    protected void setUpVersion() {
        LOGGER.info("Setting up for version {}.", version.urlPart);

        long mqttTimeout = serverSettings.getMqttTimeOutMs();
        entityHelper = new EntityHelper2(sSrvc);
        mqttHelper = new MqttHelper2(sSrvc, serverSettings.getMqttUrl(), mqttTimeout);
    }

    @Override
    protected void tearDownVersion() throws ServiceFailureException {
        EntityUtils.deleteAll(service);
        entityHelper = null;
        mqttHelper = null;
    }

    @AfterAll
    public static void tearDown() throws ServiceFailureException {
        LOGGER.info("Tearing down.");
        EntityUtils.deleteAll(service);
        entityHelper = null;
        mqttHelper = null;
    }

    @Test
    void check01_CreateObservationDirect() throws ServiceFailureException {
        LOGGER.info("  checkCreateObservationDirect");
        EntityUtils.deleteAll(sSrvc.dao(sMdl.etObservation));
        JsonNode createdObservation = getObservation();
        mqttHelper.publish(entityHelper.createUrl(sMdl.etObservation), createdObservation.toString());

        JsonNode latestObservation = entityHelper.getEntityJsonWithRetry(
                sMdl.etObservation,
                "Datastream($select=id),FeatureOfInterest($select=id)&$select=result,phenomenonTime,validTime,parameters",
                10);
        assertTrue(Utils.jsonEquals(latestObservation, createdObservation));
    }

    @Test
    void check02_CreateObservationViaDatastream() throws ServiceFailureException {
        LOGGER.info("  checkCreateObservationViaDatastream");
        EntityUtils.deleteAll(sSrvc.dao(sMdl.etObservation));
        JsonNode createdObservation = getObservation();
        Object datastreamId;
        datastreamId = createdObservation.get("Datastream").get(ControlInformation.ID);
        mqttHelper.publish(entityHelper.createUrl(sMdl.etDatastream, datastreamId, "/Observations"), createdObservation.toString());

        JsonNode latestObservation = entityHelper.getEntityJsonWithRetry(
                sMdl.etObservation,
                "Datastream($select=id),FeatureOfInterest($select=id)&$select=result,phenomenonTime,validTime,parameters",
                10);
        assertTrue(Utils.jsonEquals(latestObservation, createdObservation));
    }

    @Test
    void check03_CreateObservationViaFeatureOfInterest() throws ServiceFailureException {
        LOGGER.info("  checkCreateObservationViaFeatureOfInterest");
        EntityUtils.deleteAll(sSrvc.dao(sMdl.etObservation));
        JsonNode createdObservation = getObservation();
        Object featureOfInterestId = -1;
        featureOfInterestId = createdObservation.get("FeatureOfInterest").get(ControlInformation.ID);
        mqttHelper.publish(entityHelper.createUrl(sMdl.etFeatureOfInterest, featureOfInterestId, "/Observations"), createdObservation.toString());

        JsonNode latestObservation = entityHelper.getEntityJsonWithRetry(
                sMdl.etObservation,
                "Datastream($select=id),FeatureOfInterest($select=id)&$select=result,phenomenonTime,validTime,parameters",
                10);
        assertTrue(Utils.jsonEquals(latestObservation, createdObservation));
    }

    @Test
    void check04_CreateObservationWithDeepInsert() throws ServiceFailureException {
        LOGGER.info("  checkCreateObservationWithDeepInsert");
        EntityUtils.deleteAll(sSrvc.dao(sMdl.etObservation));
        JsonNode createdObservation = getObservationWithDeepInsert();
        mqttHelper.publish(entityHelper.createUrl(sMdl.etObservation), createdObservation.toString());

        JsonNode latestObservation = entityHelper.getEntityJsonWithRetry(
                sMdl.etObservation,
                expandQueryFromJsonObject(createdObservation),
                10);
        assertTrue(Utils.jsonEquals(latestObservation, createdObservation));
    }

    private String expandQueryFromJsonObject(JsonNode expectedResult) {
        return expandQueryFromJsonObject(expectedResult, "&");
    }

    private String expandQueryFromJsonObject(JsonNode expectedResult, String seperator) {
        String result = "";
        List<String> selects = new ArrayList<>();
        List<String> expands = new ArrayList<>();
        Iterator iterator = expectedResult.fieldNames();
        while (iterator.hasNext()) {
            String key = iterator.next().toString();
            EntityType relationType = null;
            try {
                relationType = EntityType.getForRelation(key);
            } catch (IllegalArgumentException ex) {

            }
            // check if navigationLink or simple property
            if (relationType != null) {
                expands.add(key + "(" + expandQueryFromJsonObject(expectedResult.get(key), ";") + ")");
            } else {
                selects.add(key);
            }
        }
        if (!selects.isEmpty()) {
            result += "$select=" + selects.stream().collect(Collectors.joining(","));
        }
        if (!expands.isEmpty()) {
            if (!result.isEmpty()) {
                result += seperator;
            }
            result += "$expand=" + expands.stream().collect(Collectors.joining(","));
        }
        return result;
    }

    private JsonNode getObservation() throws ServiceFailureException {
        long value = new Random().nextLong();
        Entity thing = entityHelper.createThing();
        Entity observedProperty = entityHelper.createObservedProperty();
        Entity sensor = entityHelper.createSensor();
        Entity datastream = entityHelper.createDatastream(thing, observedProperty, sensor);
        Entity featureOfInterest = entityHelper.createFeatureOfInterest();
        try {
            return Utils.MAPPER.readTree("{\n"
                    + "  \"phenomenonTime\": \"2015-03-01T02:40:00+02:00\",\n"
                    + "  \"validTime\": \"2016-01-01T01:01:01.000Z/2016-01-01T23:59:59.000Z\",\n"
                    + "  \"result\": " + value + ",\n"
                    + "  \"parameters\":{\"param1\": \"some value1\", \"param2\": \"some value2\"},\n"
                    + "  \"Datastream\":{\"@iot.id\": " + Utils.quoteForJson(datastream.getPrimaryKeyValues().get(0)) + "},\n"
                    + "  \"FeatureOfInterest\": {\"@iot.id\": " + Utils.quoteForJson(featureOfInterest.getPrimaryKeyValues().get(0)) + "}  \n"
                    + "}");
        } catch (IOException ex) {
            LOGGER.error("Exception:", ex);
            fail("error converting obsveration to JSON: " + ex.getMessage());
        }
        return null;
    }

    private JsonNode getObservationWithDeepInsert() {
        long value = new Random().nextLong();
        try {
            return Utils.MAPPER.readTree("{\n"
                    + "	\"phenomenonTime\": \"2015-03-01T00:00:00.000Z\",\n"
                    + "	\"result\": " + value + ",\n"
                    + "	\"FeatureOfInterest\": {\n"
                    + "		\"name\": \"A weather station.\",\n"
                    + "		\"description\": \"A weather station for " + value + ".\",\n"
                    + "		\"encodingType\": \"application/vnd.geo+json\",\n"
                    + "		\"feature\": {\n"
                    + "			\"type\": \"Point\",\n"
                    + "			\"coordinates\": [\n"
                    + "				-114.05,\n"
                    + "				51.05\n"
                    + "			]\n"
                    + "		}\n"
                    + "	},\n"
                    + "	\"Datastream\": {\n"
                    + "		\"unitOfMeasurement\": {\n"
                    + "			\"name\": \"Celsius\",\n"
                    + "			\"symbol\": \"degC\",\n"
                    + "			\"definition\": \"http://qudt.org/vocab/unit#DegreeCelsius\"\n"
                    + "		},\n"
                    + "		\"name\": \"test datastream.\",\n"
                    + "		\"description\": \"test datastream for " + value + ".\",\n"
                    + "		\"observationType\": \"http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement\",\n"
                    + "		\"Thing\": {\n"
                    + "			\"name\": \"Test Thing\",\n"
                    + "			\"description\": \"This is a Test Thing for " + value + "\"\n"
                    + "		},\n"
                    + "		\"ObservedProperty\": {\n"
                    + "			\"name\": \"Luminous Flux\",\n"
                    + "			\"definition\": \"http://www.qudt.org/qudt/owl/1.0.0/quantity/Instances.html#LuminousFlux\",\n"
                    + "			\"description\": \"Luminous Flux for " + value + ".\"\n"
                    + "		},\n"
                    + "		\"Sensor\": {        \n"
                    + "			\"name\": \"Acme Fluxomatic 1000\",\n"
                    + "			\"description\": \"Acme Fluxomatic for " + value + "\",\n"
                    + "			\"encodingType\": \"http://schema.org/description\",\n"
                    + "			\"metadata\": \"Light flux sensor\"\n"
                    + "		}\n"
                    + "	}\n"
                    + "}\n"
                    + "");
        } catch (IOException ex) {
            LOGGER.error("Exception:", ex);
            fail("error converting obsveration to JSON: " + ex.getMessage());
        }
        return null;
    }
}
