package de.fraunhofer.iosb.ilt.statests.c02cud;

import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.dao.ObservationDao;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.Entity;
import de.fraunhofer.iosb.ilt.sta.model.Location;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.sta.service.SensorThingsService;
import de.fraunhofer.iosb.ilt.statests.TestSuite;
import de.fraunhofer.iosb.ilt.statests.ServerSettings;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.geojson.Point;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests date and time functions.
 *
 * @author Hylke van der Schaaf
 */
public class ResultTypesTests {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ResultTypesTests.class);

    private static ServerSettings serverSettings;
    private static SensorThingsService service;

    private static final List<Thing> THINGS = new ArrayList<>();
    private static final List<Datastream> DATASTREAMS = new ArrayList<>();
    private static final List<Observation> OBSERVATIONS = new ArrayList<>();

    public ResultTypesTests() {
    }

    @BeforeClass
    public static void setUp() throws MalformedURLException, ServiceFailureException, URISyntaxException {
        LOGGER.info("Setting up class.");
        TestSuite suite = TestSuite.getInstance();
        serverSettings = suite.getServerSettings();
        service = new SensorThingsService(new URL(serverSettings.serviceUrl));
        createEntities();
    }

    @AfterClass
    public static void tearDown() {
        LOGGER.info("tearing down class.");
        try {
            EntityUtils.deleteAll(service);
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

    @Test
    public void testBooleanResult() throws ServiceFailureException {
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

    @Test
    public void testStringResult() throws ServiceFailureException {
        ObservationDao doa = service.observations();
        Observation b1 = new Observation("fourty two", DATASTREAMS.get(0));
        doa.create(b1);
        OBSERVATIONS.add(b1);

        Observation found;
        found = doa.find(b1.getId());
        String message = "Expected result to be a String.";
        Assert.assertEquals(message, b1.getResult(), found.getResult());
    }

    @Test
    public void testNumericResult() throws ServiceFailureException {
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

    @Test
    public void testObjectResult() throws ServiceFailureException {
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

    @Test
    public void testArrayResult() throws ServiceFailureException {
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

    @Test
    public void testNullResult() throws ServiceFailureException {
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

    public static <T extends Entity<T>> List<T> getFromList(List<T> list, int... ids) {
        List<T> result = new ArrayList<>();
        for (int i : ids) {
            result.add(list.get(i));
        }
        return result;
    }
}
