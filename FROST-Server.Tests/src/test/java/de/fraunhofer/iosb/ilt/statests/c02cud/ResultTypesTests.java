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
package de.fraunhofer.iosb.ilt.statests.c02cud;

import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsSensingV11.EP_RESULT;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsSensingV11.EP_RESULTQUALITY;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.frostclient.dao.Dao;
import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.json.SimpleJsonMapper;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.geojson.Point;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests Observation result.
 *
 * @author Hylke van der Schaaf
 */
public abstract class ResultTypesTests extends AbstractTestClass {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ResultTypesTests.class);

    private static final List<Entity> THINGS = new ArrayList<>();
    private static final List<Entity> DATASTREAMS = new ArrayList<>();
    private static final List<Entity> OBSERVATIONS = new ArrayList<>();

    public ResultTypesTests(ServerVersion version) {
        super(version);
    }

    @Override
    protected void setUpVersion() throws ServiceFailureException, URISyntaxException {
        LOGGER.info("Setting up for version {}.", version.urlPart);
        createEntities();
    }

    @Override
    protected void tearDownVersion() throws ServiceFailureException {
        cleanup();
    }

    private static void cleanup() throws ServiceFailureException {
        EntityUtils.deleteAll(version, serverSettings, service);
        THINGS.clear();
        DATASTREAMS.clear();
        DATASTREAMS.clear();
    }

    @AfterAll
    public static void tearDown() {
        LOGGER.info("Tearing down.");
        try {
            cleanup();
        } catch (ServiceFailureException ex) {
            LOGGER.error("Failed to clean database.", ex);
        }
    }

    private static void createEntities() throws ServiceFailureException, URISyntaxException {
        Entity thing = sMdl.newThing("Thing 1", "The first thing.");
        THINGS.add(thing);
        Entity location = sMdl.newLocation("Location 1.0", "Location of Thing 1.", "application/vnd.geo+json", new Point(8, 51));
        thing.getProperty(sMdl.npThingLocations).add(location);
        sSrvc.create(thing);

        Entity sensor = sMdl.newSensor("Sensor 1", "The first sensor.", "text", "Some metadata.");
        Entity obsProp = sMdl.newObservedProperty("Temperature", "http://ucom.org/temperature", "The temperature of the thing.");
        Entity datastream = sMdl.newDatastream("Datastream 1", "The temperature of thing 1, sensor 1.", "someType", new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
        datastream.setProperty(sMdl.npDatastreamThing, thing);
        datastream.setProperty(sMdl.npDatastreamSensor, sensor);
        datastream.setProperty(sMdl.npDatastreamObservedproperty, obsProp);
        sSrvc.create(datastream);
        DATASTREAMS.add(datastream);
    }

    /**
     * Tests if Boolean result values are stored and retrieved correctly.
     *
     * @throws ServiceFailureException if the service connection fails.
     */
    @Test
    void testBooleanResult() throws ServiceFailureException {
        LOGGER.info("  testBooleanResult");
        Entity b1 = sMdl.newObservation(Boolean.TRUE, DATASTREAMS.get(0));
        sSrvc.create(b1);
        OBSERVATIONS.add(b1);

        Entity b2 = sMdl.newObservation(Boolean.FALSE, DATASTREAMS.get(0));
        sSrvc.create(b2);
        OBSERVATIONS.add(b2);

        Dao doa = sSrvc.dao(sMdl.etObservation);
        Entity found;
        found = doa.find(b1.getPrimaryKeyValues());
        String message = "Expected result to be a Boolean.";
        assertEquals(b1.getProperty(EP_RESULT), found.getProperty(EP_RESULT), message);
        found = doa.find(b2.getPrimaryKeyValues());
        message = "Expected result to be a Boolean.";
        assertEquals(b2.getProperty(EP_RESULT), found.getProperty(EP_RESULT), message);
    }

    /**
     * Tests if String result values are stored and retrieved correctly.
     *
     * @throws ServiceFailureException if the service connection fails.
     */
    @Test
    void testStringResult() throws ServiceFailureException {
        LOGGER.info("  testStringResult");
        Entity b1 = sMdl.newObservation("fourty two", DATASTREAMS.get(0));
        sSrvc.create(b1);
        OBSERVATIONS.add(b1);

        Dao doa = sSrvc.dao(sMdl.etObservation);
        Entity found;
        found = doa.find(b1.getPrimaryKeyValues());
        String message = "Expected result to be a String.";
        assertEquals(b1.getProperty(EP_RESULT), found.getProperty(EP_RESULT), message);
    }

    /**
     * Tests if Numeric result values are stored and retrieved correctly.
     *
     * @throws ServiceFailureException if the service connection fails.
     */
    @Test
    void testNumericResult() throws ServiceFailureException {
        LOGGER.info("  testNumericResult");
        Entity b1 = sMdl.newObservation(1L, DATASTREAMS.get(0));
        sSrvc.create(b1);
        OBSERVATIONS.add(b1);

        Dao doa = sSrvc.dao(sMdl.etObservation);
        Entity found1 = doa.find(b1.getPrimaryKeyValues());
        String message = "Expected result to be a Number.";
        assertEquals(b1.getProperty(EP_RESULT), found1.getProperty(EP_RESULT), message);

        Entity b2 = sMdl.newObservation(BigDecimal.valueOf(1.23), DATASTREAMS.get(0));
        doa.create(b2);
        OBSERVATIONS.add(b2);

        Entity found2 = doa.find(b2.getPrimaryKeyValues());
        message = "Expected result to be a Number.";
        assertEquals(b2.getProperty(EP_RESULT), found2.getProperty(EP_RESULT), message);
    }

    /**
     * Tests if Object result values are stored and retrieved correctly.
     *
     * @throws ServiceFailureException if the service connection fails.
     */
    @Test
    void testObjectResult() throws ServiceFailureException {
        LOGGER.info("  testObjectResult");
        Dao doa = sSrvc.dao(sMdl.etObservation);
        Map<String, Object> result = new HashMap<>();
        result.put("number", BigDecimal.valueOf(1.23));
        result.put("string", "One comma twentythree");
        result.put("boolean", Boolean.TRUE);
        Entity o1 = sMdl.newObservation(result, DATASTREAMS.get(0));
        doa.create(o1);
        OBSERVATIONS.add(o1);

        Entity found = doa.find(o1.getPrimaryKeyValues());
        String message = "Expected result Maps are not equal.";
        assertEquals(o1.getProperty(EP_RESULT), found.getProperty(EP_RESULT), message);
    }

    /**
     * Tests if Array result values are stored and retrieved correctly.
     *
     * @throws ServiceFailureException if the service connection fails.
     */
    @Test
    void testArrayResult() throws ServiceFailureException {
        LOGGER.info("  testArrayResult");
        Dao doa = sSrvc.dao(sMdl.etObservation);
        List<Object> result = new ArrayList<>();
        result.add(BigDecimal.valueOf(1.23));
        result.add("One comma twentythree");
        result.add(Boolean.TRUE);
        Entity o1 = sMdl.newObservation(result, DATASTREAMS.get(0));
        doa.create(o1);
        OBSERVATIONS.add(o1);

        Entity found = doa.find(o1.getPrimaryKeyValues());
        String message = "Expected result Arrays are not equal.";
        assertEquals(o1.getProperty(EP_RESULT), found.getProperty(EP_RESULT), message);
    }

    /**
     * Tests if NULL result values are stored and retrieved correctly.
     *
     * @throws ServiceFailureException if the service connection fails.
     */
    @Test
    void testNullResult() throws ServiceFailureException {
        LOGGER.info("  testNullResult");
        Dao doa = sSrvc.dao(sMdl.etObservation);
        Entity o1 = sMdl.newObservation(null, DATASTREAMS.get(0));
        doa.create(o1);
        OBSERVATIONS.add(o1);

        Entity found;
        found = doa.find(o1.getPrimaryKeyValues());
        String message = "Expected result to be Null.";
        assertEquals(o1.getProperty(EP_RESULT), found.getProperty(EP_RESULT), message);

        Entity o2 = sMdl.newObservation(BigDecimal.valueOf(1.23), DATASTREAMS.get(0));
        doa.create(o2);
        OBSERVATIONS.add(o2);

        o2 = o2.withOnlyPk();
        o2.setProperty(EP_RESULT, null);
        doa.update(o2);

        found = doa.find(o2.getPrimaryKeyValues());
        message = "Expected result to be Null.";
        assertEquals(o2.getProperty(EP_RESULT), found.getProperty(EP_RESULT), message);
    }

    /**
     * Tests if resultQuality can have arbitrary json.
     *
     * @throws ServiceFailureException if the service connection fails.
     */
    @Test
    void testResultQualityObject() throws ServiceFailureException, IOException {
        LOGGER.info("  testResultQualityObject");
        Dao doa = sSrvc.dao(sMdl.etObservation);
        Entity o1 = sMdl.newObservation(1.0, DATASTREAMS.get(0));
        ObjectMapper mapper = SimpleJsonMapper.getSimpleObjectMapper();
        String resultQualityString = ""
                + "{\"DQ_Status\":{"
                + "  \"code\": \"http://id.eaufrance.fr/nsa/446#2\","
                + "  \"label\": \"Niveau 1\",\n"
                + "  \"comment\": \"Donnée contrôlée niveau 1 (données contrôlées)\""
                + "}}";
        o1.setProperty(EP_RESULTQUALITY, mapper.readTree(resultQualityString));
        doa.create(o1);
        OBSERVATIONS.add(o1);

        Entity found;
        found = doa.find(o1.getPrimaryKeyValues());
        String message = "resultQuality not stored correctly.";
        assertEquals(o1.getProperty(EP_RESULTQUALITY), mapper.valueToTree(found.getProperty(EP_RESULTQUALITY)), message);
    }

    /**
     * Tests if resultQuality can have arbitrary json.
     *
     * @throws ServiceFailureException if the service connection fails.
     */
    @Test
    void testResultQualityArray() throws ServiceFailureException, IOException {
        LOGGER.info("  testResultQualityArray");
        Dao doa = sSrvc.dao(sMdl.etObservation);
        Entity o1 = sMdl.newObservation(1.0, DATASTREAMS.get(0));
        ObjectMapper mapper = SimpleJsonMapper.getSimpleObjectMapper();
        String resultQualityString = "[\n"
                + "    {\n"
                + "        \"nameOfMeasure\": \"DQ_Status\",\n"
                + "        \"DQ_Result\": {\n"
                + "            \"code\": \"http://id.eaufrance.fr/nsa/446#2\",\n"
                + "            \"label\": \"Niveau 1\",\n"
                + "            \"comment\": \"Donnée contrôlée niveau 1 (données contrôlées)\"\n"
                + "        }\n"
                + "    },\n"
                + "    {\n"
                + "        \"nameOfMeasure\": \"DQ_Qualification\",\n"
                + "        \"DQ_Result\": {\n"
                + "            \"code\": \"http://id.eaufrance.fr/nsa/414#1\",\n"
                + "            \"label\": \"Correcte\",\n"
                + "            \"comment\": \"Correcte\"\n"
                + "        }\n"
                + "    }\n"
                + "\n"
                + "]";
        o1.setProperty(EP_RESULTQUALITY, mapper.readTree(resultQualityString));
        doa.create(o1);
        OBSERVATIONS.add(o1);

        Entity found = doa.find(o1.getPrimaryKeyValues());
        String message = "resultQuality not stored correctly.";
        assertEquals(o1.getProperty(EP_RESULTQUALITY), mapper.valueToTree(found.getProperty(EP_RESULTQUALITY)), message);
    }

}
