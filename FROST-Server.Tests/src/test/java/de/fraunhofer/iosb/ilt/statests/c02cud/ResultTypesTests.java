package de.fraunhofer.iosb.ilt.statests.c02cud;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.dao.ObservationDao;
import de.fraunhofer.iosb.ilt.sta.jackson.ObjectMapperFactory;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.Location;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.Utils;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.geojson.Point;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests Observation result.
 *
 * @author Hylke van der Schaaf
 */
public class ResultTypesTests extends AbstractTestClass {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ResultTypesTests.class);

    private static final List<Thing> THINGS = new ArrayList<>();
    private static final List<Datastream> DATASTREAMS = new ArrayList<>();
    private static final List<Observation> OBSERVATIONS = new ArrayList<>();

    public ResultTypesTests(ServerVersion version) throws ServiceFailureException, URISyntaxException, Exception {
        super(version);
    }

    @Override
    protected void setUpVersion() throws ServiceFailureException, URISyntaxException {
        LOGGER.info("Setting up for version {}.", version.urlPart);
        createEntities();
    }

    @Override
    protected void tearDownVersion() throws Exception {
        cleanup();
    }

    private static void cleanup() throws ServiceFailureException {
        Utils.deleteAll(service);
        THINGS.clear();
        DATASTREAMS.clear();
        DATASTREAMS.clear();
    }

    @AfterClass
    public static void tearDown() {
        LOGGER.info("Tearing down.");
        try {
            cleanup();
        } catch (ServiceFailureException ex) {
            LOGGER.error("Failed to clean database.", ex);
        }
    }

    private static void createEntities() throws ServiceFailureException, URISyntaxException {
        Thing thing = new Thing("Thing 1", "The first thing.");
        THINGS.add(thing);
        Location location = new Location("Location 1.0", "Location of Thing 1.", "application/vnd.geo+json", new Point(8, 51));
        thing.getLocations().add(location);
        service.create(thing);

        Sensor sensor = new Sensor("Sensor 1", "The first sensor.", "text", "Some metadata.");
        ObservedProperty obsProp = new ObservedProperty("Temperature", new URI("http://ucom.org/temperature"), "The temperature of the thing.");
        Datastream datastream = new Datastream("Datastream 1", "The temperature of thing 1, sensor 1.", "someType", new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
        datastream.setThing(thing);
        datastream.setSensor(sensor);
        datastream.setObservedProperty(obsProp);
        service.create(datastream);
        DATASTREAMS.add(datastream);
    }

    /**
     * Tests if Boolean result values are stored and retrieved correctly.
     *
     * @throws ServiceFailureException if the service connection fails.
     */
    @Test
    public void testBooleanResult() throws ServiceFailureException {
        LOGGER.info("  testBooleanResult");
        ObservationDao doa = service.observations();
        Observation b1 = new Observation(Boolean.TRUE, DATASTREAMS.get(0));
        doa.create(b1);
        OBSERVATIONS.add(b1);

        Observation b2 = new Observation(Boolean.FALSE, DATASTREAMS.get(0));
        doa.create(b2);
        OBSERVATIONS.add(b2);

        Observation found;
        found = doa.find(b1.getId());
        String message = "Expected result to be a Boolean.";
        Assert.assertEquals(message, b1.getResult(), found.getResult());
        found = doa.find(b2.getId());
        message = "Expected result to be a Boolean.";
        Assert.assertEquals(message, b2.getResult(), found.getResult());
    }

    /**
     * Tests if String result values are stored and retrieved correctly.
     *
     * @throws ServiceFailureException if the service connection fails.
     */
    @Test
    public void testStringResult() throws ServiceFailureException {
        LOGGER.info("  testStringResult");
        ObservationDao doa = service.observations();
        Observation b1 = new Observation("fourty two", DATASTREAMS.get(0));
        doa.create(b1);
        OBSERVATIONS.add(b1);

        Observation found;
        found = doa.find(b1.getId());
        String message = "Expected result to be a String.";
        Assert.assertEquals(message, b1.getResult(), found.getResult());
    }

    /**
     * Tests if Numeric result values are stored and retrieved correctly.
     *
     * @throws ServiceFailureException if the service connection fails.
     */
    @Test
    public void testNumericResult() throws ServiceFailureException {
        LOGGER.info("  testNumericResult");
        ObservationDao doa = service.observations();
        Observation b1 = new Observation(1, DATASTREAMS.get(0));
        doa.create(b1);
        OBSERVATIONS.add(b1);

        Observation found;
        found = doa.find(b1.getId());
        String message = "Expected result to be a Number.";
        Assert.assertEquals(message, b1.getResult(), found.getResult());

        Observation b2 = new Observation(BigDecimal.valueOf(1.23), DATASTREAMS.get(0));
        doa.create(b2);
        OBSERVATIONS.add(b2);

        found = doa.find(b2.getId());
        message = "Expected result to be a Number.";
        Assert.assertEquals(message, b2.getResult(), found.getResult());
    }

    /**
     * Tests if Object result values are stored and retrieved correctly.
     *
     * @throws ServiceFailureException if the service connection fails.
     */
    @Test
    public void testObjectResult() throws ServiceFailureException {
        LOGGER.info("  testObjectResult");
        ObservationDao doa = service.observations();
        Map<String, Object> result = new HashMap<>();
        result.put("number", BigDecimal.valueOf(1.23));
        result.put("string", "One comma twentythree");
        result.put("boolean", Boolean.TRUE);
        Observation o1 = new Observation(result, DATASTREAMS.get(0));
        doa.create(o1);
        OBSERVATIONS.add(o1);

        Observation found;
        found = doa.find(o1.getId());
        String message = "Expected result Maps are not equal.";
        Assert.assertEquals(message, o1.getResult(), found.getResult());
    }

    /**
     * Tests if Array result values are stored and retrieved correctly.
     *
     * @throws ServiceFailureException if the service connection fails.
     */
    @Test
    public void testArrayResult() throws ServiceFailureException {
        LOGGER.info("  testArrayResult");
        ObservationDao doa = service.observations();
        List<Object> result = new ArrayList<>();
        result.add(BigDecimal.valueOf(1.23));
        result.add("One comma twentythree");
        result.add(Boolean.TRUE);
        Observation o1 = new Observation(result, DATASTREAMS.get(0));
        doa.create(o1);
        OBSERVATIONS.add(o1);

        Observation found;
        found = doa.find(o1.getId());
        String message = "Expected result Arrays are not equal.";
        Assert.assertEquals(message, o1.getResult(), found.getResult());
    }

    /**
     * Tests if NULL result values are stored and retrieved correctly.
     *
     * @throws ServiceFailureException if the service connection fails.
     */
    @Test
    public void testNullResult() throws ServiceFailureException {
        LOGGER.info("  testNullResult");
        ObservationDao doa = service.observations();
        Observation o1 = new Observation(null, DATASTREAMS.get(0));
        doa.create(o1);
        OBSERVATIONS.add(o1);

        Observation found;
        found = doa.find(o1.getId());
        String message = "Expected result to be Null.";
        Assert.assertEquals(message, o1.getResult(), found.getResult());

        Observation o2 = new Observation(BigDecimal.valueOf(1.23), DATASTREAMS.get(0));
        doa.create(o2);
        OBSERVATIONS.add(o2);

        o2 = o2.withOnlyId();
        o2.setResult(null);
        doa.update(o2);

        found = doa.find(o2.getId());
        message = "Expected result to be Null.";
        Assert.assertEquals(message, o2.getResult(), found.getResult());
    }

    /**
     * Tests if resultQuality can have arbitrary json.
     *
     * @throws ServiceFailureException if the service connection fails.
     */
    @Test
    public void testResultQualityObject() throws ServiceFailureException, IOException {
        LOGGER.info("  testResultQualityObject");
        ObservationDao doa = service.observations();
        Observation o1 = new Observation(1.0, DATASTREAMS.get(0));
        ObjectMapper mapper = ObjectMapperFactory.get();
        String resultQualityString = ""
                + "{\"DQ_Status\":{"
                + "  \"code\": \"http://id.eaufrance.fr/nsa/446#2\","
                + "  \"label\": \"Niveau 1\",\n"
                + "  \"comment\": \"Donnée contrôlée niveau 1 (données contrôlées)\""
                + "}}";
        o1.setResultQuality(mapper.readTree(resultQualityString));
        doa.create(o1);
        OBSERVATIONS.add(o1);

        Observation found;
        found = doa.find(o1.getId());
        String message = "resultQuality not stored correctly.";
        Assert.assertEquals(message, o1.getResultQuality(), mapper.valueToTree(found.getResultQuality()));
    }

    /**
     * Tests if resultQuality can have arbitrary json.
     *
     * @throws ServiceFailureException if the service connection fails.
     */
    @Test
    public void testResultQualityArray() throws ServiceFailureException, IOException {
        LOGGER.info("  testResultQualityArray");
        ObservationDao doa = service.observations();
        Observation o1 = new Observation(1.0, DATASTREAMS.get(0));
        ObjectMapper mapper = ObjectMapperFactory.get();
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
        o1.setResultQuality(mapper.readTree(resultQualityString));
        doa.create(o1);
        OBSERVATIONS.add(o1);

        Observation found;
        found = doa.find(o1.getId());
        String message = "resultQuality not stored correctly.";
        Assert.assertEquals(message, o1.getResultQuality(), mapper.valueToTree(found.getResultQuality()));
    }

}
