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
package de.fraunhofer.iosb.ilt.statests.c02cud;

import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing.EP_RESULT;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing.EP_RESULTQUALITY;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.fraunhofer.iosb.ilt.frostclient.dao.Dao;
import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.json.SimpleJsonMapper;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.geojson.Point;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.ObjectMapper;

/**
 * Tests Observation result.
 */
public abstract class ResultTypesTests extends AbstractTestClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResultTypesTests.class);

    private static final List<Entity> THINGS = new ArrayList<>();
    private static final List<Entity> DATASTREAMS = new ArrayList<>();
    private static final List<Entity> OBSERVATIONS = new ArrayList<>();
    private static SensorThingsV11Sensing sMdl;

    public ResultTypesTests(ServerVersion version) {
        super(version);
    }

    @Override
    protected void setUpVersion() throws ServiceFailureException {
        LOGGER.info("Setting up for version {}.", version.urlPart);
        sMdl = sSrvc.getModel(SensorThingsV11Sensing.class);
        createEntities();
    }

    public static void cleanup() {
        EntityUtils.deleteAll(sSrvc);
        THINGS.clear();
        DATASTREAMS.clear();
        DATASTREAMS.clear();
        AbstractTestClass.cleanup();
    }

    private static void createEntities() throws ServiceFailureException {
        Entity thing = sMdl.buildThing()
                .setName("Thing 1")
                .setDescription("The first thing.")
                .build();
        THINGS.add(thing);
        Entity location = sMdl.buildLocation()
                .setName("Location 1.0")
                .setDescription("Location of Thing 1.")
                .setEncodingType("application/vnd.geo+json")
                .setLocation(new Point(8, 51))
                .build();
        thing.getProperty(sMdl.npThingLocations).add(location);
        sSrvc.create(thing);

        Entity sensor = sMdl.buildSensor()
                .setName("Sensor 1")
                .setDescription("The first sensor.")
                .setEncodingType("text")
                .setMetadata("Some metadata.")
                .build();
        Entity obsProp = sMdl.buildObservedProperty()
                .setName("Temperature")
                .setDefinition("http://ucom.org/temperature")
                .setDescription("The temperature of the thing.")
                .build();
        Entity datastream = sMdl.buildDatastream()
                .setName("Datastream 1")
                .setDescription("The temperature of thing 1, sensor 1.")
                .setObservationType("someType")
                .setUnitOfMeasurement(new UnitOfMeasurement("degree celcius", "°C", "ucum:T"))
                .setThing(thing)
                .setSensor(sensor)
                .setObservedProperty(obsProp)
                .build();
        sSrvc.create(datastream);
        DATASTREAMS.add(datastream);
    }

    /**
     * Tests if Boolean result values are stored and retrieved correctly.
     *
     * @throws ServiceFailureException if the sSrvc connection fails.
     */
    @Test
    void testBooleanResult() throws ServiceFailureException {
        LOGGER.info("  testBooleanResult");
        Entity b1 = sMdl.buildObservation()
                .setResult(Boolean.TRUE)
                .setDatastream(DATASTREAMS.get(0))
                .build();
        sSrvc.create(b1);
        OBSERVATIONS.add(b1);

        Entity b2 = sMdl.buildObservation()
                .setResult(Boolean.FALSE)
                .setDatastream(DATASTREAMS.get(0))
                .build();
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
     * @throws ServiceFailureException if the sSrvc connection fails.
     */
    @Test
    void testStringResult() throws ServiceFailureException {
        LOGGER.info("  testStringResult");
        Entity b1 = sMdl.buildObservation()
                .setResult("fourty two")
                .setDatastream(DATASTREAMS.get(0))
                .build();
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
     * @throws ServiceFailureException if the sSrvc connection fails.
     */
    @Test
    void testNumericResult() throws ServiceFailureException {
        LOGGER.info("  testNumericResult");
        Entity b1 = sMdl.buildObservation()
                .setResult(1L)
                .setDatastream(DATASTREAMS.get(0))
                .build();
        sSrvc.create(b1);
        OBSERVATIONS.add(b1);

        Dao doa = sSrvc.dao(sMdl.etObservation);
        Entity found1 = doa.find(b1.getPrimaryKeyValues());
        String message = "Expected result to be a Number.";
        assertEquals(b1.getProperty(EP_RESULT), found1.getProperty(EP_RESULT), message);

        Entity b2 = sMdl.buildObservation()
                .setResult(BigDecimal.valueOf(1.23))
                .setDatastream(DATASTREAMS.get(0))
                .build();
        doa.create(b2);
        OBSERVATIONS.add(b2);

        Entity found2 = doa.find(b2.getPrimaryKeyValues());
        message = "Expected result to be a Number.";
        assertEquals(b2.getProperty(EP_RESULT), found2.getProperty(EP_RESULT), message);
    }

    /**
     * Tests if Object result values are stored and retrieved correctly.
     *
     * @throws ServiceFailureException if the sSrvc connection fails.
     */
    @Test
    void testObjectResult() throws ServiceFailureException {
        LOGGER.info("  testObjectResult");
        Dao doa = sSrvc.dao(sMdl.etObservation);
        Map<String, Object> result = new HashMap<>();
        result.put("number", BigDecimal.valueOf(1.23));
        result.put("string", "One comma twentythree");
        result.put("boolean", Boolean.TRUE);
        Entity o1 = sMdl.buildObservation()
                .setResult(result)
                .setDatastream(DATASTREAMS.get(0))
                .build();
        doa.create(o1);
        OBSERVATIONS.add(o1);

        Entity found = doa.find(o1.getPrimaryKeyValues());
        String message = "Expected result Maps are not equal.";
        assertEquals(o1.getProperty(EP_RESULT), found.getProperty(EP_RESULT), message);
    }

    /**
     * Tests if Array result values are stored and retrieved correctly.
     *
     * @throws ServiceFailureException if the sSrvc connection fails.
     */
    @Test
    void testArrayResult() throws ServiceFailureException {
        LOGGER.info("  testArrayResult");
        Dao doa = sSrvc.dao(sMdl.etObservation);
        List<Object> result = new ArrayList<>();
        result.add(BigDecimal.valueOf(1.23));
        result.add("One comma twentythree");
        result.add(Boolean.TRUE);
        Entity o1 = sMdl.buildObservation()
                .setResult(result)
                .setDatastream(DATASTREAMS.get(0))
                .build();
        doa.create(o1);
        OBSERVATIONS.add(o1);

        Entity found = doa.find(o1.getPrimaryKeyValues());
        String message = "Expected result Arrays are not equal.";
        assertEquals(o1.getProperty(EP_RESULT), found.getProperty(EP_RESULT), message);
    }

    /**
     * Tests if NULL result values are stored and retrieved correctly.
     *
     * @throws ServiceFailureException if the sSrvc connection fails.
     */
    @Test
    void testNullResult() throws ServiceFailureException {
        LOGGER.info("  testNullResult");
        Dao doa = sSrvc.dao(sMdl.etObservation);
        Entity o1 = sMdl.buildObservation()
                .setResult(null)
                .setDatastream(DATASTREAMS.get(0))
                .build();
        doa.create(o1);
        OBSERVATIONS.add(o1);

        Entity found;
        found = doa.find(o1.getPrimaryKeyValues());
        String message = "Expected result to be Null.";
        assertEquals(o1.getProperty(EP_RESULT), found.getProperty(EP_RESULT), message);

        Entity o2 = sMdl.buildObservation()
                .setResult(BigDecimal.valueOf(1.23))
                .setDatastream(DATASTREAMS.get(0))
                .build();
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
     * @throws ServiceFailureException if the sSrvc connection fails.
     */
    @Test
    void testResultQualityObject() throws ServiceFailureException {
        LOGGER.info("  testResultQualityObject");
        Dao doa = sSrvc.dao(sMdl.etObservation);
        ObjectMapper mapper = SimpleJsonMapper.getSimpleObjectMapper();
        String resultQualityString = """
                {"DQ_Status":{
                  "code": "http://id.eaufrance.fr/nsa/446#2",
                  "label": "Niveau 1",
                  "comment": "Donn\u00e9e contr\u00f4l\u00e9e niveau 1 (donn\u00e9es contr\u00f4l\u00e9es)"
                }}""";
        Entity o1 = sMdl.buildObservation()
                .setResult(1.0)
                .setDatastream(DATASTREAMS.get(0))
                .setResultQuality(mapper.readTree(resultQualityString))
                .build();
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
     * @throws ServiceFailureException if the sSrvc connection fails.
     */
    @Test
    void testResultQualityArray() throws ServiceFailureException {
        LOGGER.info("  testResultQualityArray");
        Dao doa = sSrvc.dao(sMdl.etObservation);
        ObjectMapper mapper = SimpleJsonMapper.getSimpleObjectMapper();
        String resultQualityString = """
                [
                    {
                        "nameOfMeasure": "DQ_Status",
                        "DQ_Result": {
                            "code": "http://id.eaufrance.fr/nsa/446#2",
                            "label": "Niveau 1",
                            "comment": "Donn\u00e9e contr\u00f4l\u00e9e niveau 1 (donn\u00e9es contr\u00f4l\u00e9es)"
                        }
                    },
                    {
                        "nameOfMeasure": "DQ_Qualification",
                        "DQ_Result": {
                            "code": "http://id.eaufrance.fr/nsa/414#1",
                            "label": "Correcte",
                            "comment": "Correcte"
                        }
                    }
                ]""";
        Entity o1 = sMdl.buildObservation()
                .setResult(1.0)
                .setDatastream(DATASTREAMS.get(0))
                .setResultQuality(mapper.readTree(resultQualityString))
                .build();
        doa.create(o1);
        OBSERVATIONS.add(o1);

        Entity found = doa.find(o1.getPrimaryKeyValues());
        String message = "resultQuality not stored correctly.";
        assertEquals(o1.getProperty(EP_RESULTQUALITY), mapper.valueToTree(found.getProperty(EP_RESULTQUALITY)), message);
    }

}
