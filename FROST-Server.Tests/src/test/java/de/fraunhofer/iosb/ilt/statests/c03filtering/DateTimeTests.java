package de.fraunhofer.iosb.ilt.statests.c03filtering;

import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.StatusCodeException;
import de.fraunhofer.iosb.ilt.sta.dao.BaseDao;
import de.fraunhofer.iosb.ilt.sta.dao.ObservationDao;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.Entity;
import de.fraunhofer.iosb.ilt.sta.model.Location;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.TimeObject;
import de.fraunhofer.iosb.ilt.sta.model.ext.EntityList;
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.sta.service.SensorThingsService;
import de.fraunhofer.iosb.ilt.statests.ServerSettings;
import de.fraunhofer.iosb.ilt.statests.TestSuite;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.geojson.Point;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.extra.Interval;

/**
 * Tests date and time functions.
 *
 * @author Hylke van der Schaaf
 */
public class DateTimeTests {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DateTimeTests.class);

    private static ServerSettings serverSettings;
    private static SensorThingsService service;

    private static final List<Thing> THINGS = new ArrayList<>();
    private static final List<Observation> OBSERVATIONS = new ArrayList<>();
    private static ZonedDateTime T2015;
    private static ZonedDateTime T600;
    private static ZonedDateTime T659;
    private static ZonedDateTime T700;
    private static ZonedDateTime T701;
    private static ZonedDateTime T759;
    private static ZonedDateTime T800;
    private static ZonedDateTime T801;
    private static ZonedDateTime T900;
    private static ZonedDateTime T2017;
    private static Interval I2015;
    private static Interval I600_659;
    private static Interval I600_700;
    private static Interval I600_701;
    private static Interval I700_800;
    private static Interval I701_759;
    private static Interval I759_900;
    private static Interval I800_900;
    private static Interval I801_900;
    private static Interval I659_801;
    private static Interval I700_759;
    private static Interval I700_801;
    private static Interval I659_800;
    private static Interval I701_800;
    private static Interval I2017;

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

        T2015 = ZonedDateTime.parse("2015-01-01T06:00:00.000Z");
        T600 = ZonedDateTime.parse("2016-01-01T06:00:00.000Z");
        T659 = ZonedDateTime.parse("2016-01-01T06:59:00.000Z");
        T700 = ZonedDateTime.parse("2016-01-01T07:00:00.000Z");
        T701 = ZonedDateTime.parse("2016-01-01T07:01:00.000Z");
        T759 = ZonedDateTime.parse("2016-01-01T07:59:00.000Z");
        T800 = ZonedDateTime.parse("2016-01-01T08:00:00.000Z");
        T801 = ZonedDateTime.parse("2016-01-01T08:01:00.000Z");
        T900 = ZonedDateTime.parse("2016-01-01T09:00:00.000Z");
        T2017 = ZonedDateTime.parse("2017-01-01T09:00:00.000Z");

        I2015 = Interval.of(T2015.toInstant(), T2015.plus(1, ChronoUnit.HOURS).toInstant());
        I600_659 = Interval.of(T600.toInstant(), T659.toInstant());
        I600_700 = Interval.of(T600.toInstant(), T700.toInstant());
        I600_701 = Interval.of(T600.toInstant(), T701.toInstant());
        I700_800 = Interval.of(T700.toInstant(), T800.toInstant());
        I701_759 = Interval.of(T701.toInstant(), T759.toInstant());
        I759_900 = Interval.of(T759.toInstant(), T900.toInstant());
        I800_900 = Interval.of(T800.toInstant(), T900.toInstant());
        I801_900 = Interval.of(T801.toInstant(), T900.toInstant());
        I659_801 = Interval.of(T659.toInstant(), T801.toInstant());
        I700_759 = Interval.of(T700.toInstant(), T759.toInstant());
        I700_801 = Interval.of(T700.toInstant(), T801.toInstant());
        I659_800 = Interval.of(T659.toInstant(), T800.toInstant());
        I701_800 = Interval.of(T701.toInstant(), T800.toInstant());
        I2017 = Interval.of(T2017.toInstant(), T2017.plus(1, ChronoUnit.HOURS).toInstant());

        createObservation(0, datastream, T600, T600, null); // 0
        createObservation(1, datastream, T659, T659, null); // 1
        createObservation(2, datastream, T700, T700, null); // 2
        createObservation(3, datastream, T701, T701, null); // 3
        createObservation(4, datastream, T759, T759, null); // 4
        createObservation(5, datastream, T800, T800, null); // 5
        createObservation(6, datastream, T801, T801, null); // 6
        createObservation(7, datastream, T900, T900, null); // 7

        createObservation(8, datastream, I600_659, null, I600_659); // 8
        createObservation(9, datastream, I600_700, null, I600_700); // 9
        createObservation(10, datastream, I600_701, null, I600_701); // 10
        createObservation(11, datastream, I700_800, null, I700_800); // 11
        createObservation(12, datastream, I701_759, null, I701_759); // 12
        createObservation(13, datastream, I759_900, null, I759_900); // 13
        createObservation(14, datastream, I800_900, null, I800_900); // 14
        createObservation(15, datastream, I801_900, null, I801_900); // 15

        createObservation(16, datastream, I659_801, null, I659_801); // 16
        createObservation(17, datastream, I700_759, null, I700_759); // 17
        createObservation(18, datastream, I700_801, null, I700_801); // 18
        createObservation(19, datastream, I659_800, null, I659_800); // 19
        createObservation(20, datastream, I701_800, null, I701_800); // 20

        createObservation(21, datastream, T2015, T2015, null); // 21
        createObservation(22, datastream, T2017, T2017, null); // 22
        createObservation(23, datastream, I2015, null, I2015); // 23
        createObservation(24, datastream, I2017, null, I2017); // 24
    }

    private static void createObservation(double result, Datastream ds, Interval pt, ZonedDateTime rt, Interval vt) throws ServiceFailureException {
        createObservation(result, ds, new TimeObject(pt), rt, vt);
    }

    private static void createObservation(double result, Datastream ds, ZonedDateTime pt, ZonedDateTime rt, Interval vt) throws ServiceFailureException {
        createObservation(result, ds, new TimeObject(pt), rt, vt);
    }

    private static void createObservation(double result, Datastream ds, TimeObject pt, ZonedDateTime rt, Interval vt) throws ServiceFailureException {
        Observation o = new Observation(result, ds);
        o.setPhenomenonTime(pt);
        o.setResultTime(rt);
        o.setValidTime(vt);
        service.create(o);
        OBSERVATIONS.add(o);
    }

    public void filterAndCheck(BaseDao doa, String filter, List<? extends Entity> expected) {
        try {
            EntityList<Observation> result = doa.query().filter(filter).list();
            EntityUtils.resultTestResult check = EntityUtils.resultContains(result, expected);
            String msg = "Failed on filter: " + filter + " Cause: " + check.message;
            Assert.assertTrue(msg, check.testOk);
        } catch (ServiceFailureException ex) {
            LOGGER.error("Exception:", ex);
            Assert.fail("Failed to call service: " + ex.getMessage());
        }
    }

    public void filterForException(BaseDao doa, String filter, int expectedCode) {
        try {
            doa.query().filter(filter).list();
        } catch (StatusCodeException e) {
            String msg = "Filter " + filter + " did not respond with " + expectedCode + " Bad Request, but with " + e.getStatusCode() + ".";
            Assert.assertEquals(msg, expectedCode, e.getStatusCode());
            return;
        } catch (ServiceFailureException ex) {
            LOGGER.error("Exception:", ex);
            Assert.fail("Failed to call service for filter " + filter + " " + ex);
        }
        Assert.fail("Filter " + filter + " did not respond with 400 Bad Request.");
    }

    @Test
    public void testLt() throws ServiceFailureException {
        ObservationDao doa = service.observations();
        filterAndCheck(doa, String.format("resultTime lt %s", T700), getFromList(OBSERVATIONS, 0, 1, 21));
        filterAndCheck(doa, String.format("validTime lt %s", T700), getFromList(OBSERVATIONS, 8, 9, 23));
        filterAndCheck(doa, String.format("phenomenonTime lt %s", T700), getFromList(OBSERVATIONS, 0, 1, 8, 9, 21, 23));

        filterAndCheck(doa, String.format("resultTime lt %s", I700_800), getFromList(OBSERVATIONS, 0, 1, 21));
        filterAndCheck(doa, String.format("validTime lt %s", I700_800), getFromList(OBSERVATIONS, 8, 9, 23));
        filterAndCheck(doa, String.format("phenomenonTime lt %s", I700_800), getFromList(OBSERVATIONS, 0, 1, 8, 9, 21, 23));

        filterAndCheck(doa, String.format("%s lt resultTime", T800), getFromList(OBSERVATIONS, 6, 7, 22));
        filterAndCheck(doa, String.format("%s lt validTime", T800), getFromList(OBSERVATIONS, 15, 24));
        filterAndCheck(doa, String.format("%s lt phenomenonTime", T800), getFromList(OBSERVATIONS, 6, 7, 15, 22, 24));

        filterAndCheck(doa, String.format("%s lt resultTime", I700_800), getFromList(OBSERVATIONS, 5, 6, 7, 22));
        filterAndCheck(doa, String.format("%s lt validTime", I700_800), getFromList(OBSERVATIONS, 14, 15, 24));
        filterAndCheck(doa, String.format("%s lt phenomenonTime", I700_800), getFromList(OBSERVATIONS, 5, 6, 7, 14, 15, 22, 24));
    }

    @Test
    public void testGt() throws ServiceFailureException {
        ObservationDao doa = service.observations();
        filterAndCheck(doa, String.format("resultTime gt %s", T800), getFromList(OBSERVATIONS, 6, 7, 22));
        filterAndCheck(doa, String.format("validTime gt %s", T800), getFromList(OBSERVATIONS, 15, 24));
        filterAndCheck(doa, String.format("phenomenonTime gt %s", T800), getFromList(OBSERVATIONS, 6, 7, 15, 22, 24));

        filterAndCheck(doa, String.format("resultTime gt %s", I700_800), getFromList(OBSERVATIONS, 5, 6, 7, 22));
        filterAndCheck(doa, String.format("validTime gt %s", I700_800), getFromList(OBSERVATIONS, 14, 15, 24));
        filterAndCheck(doa, String.format("phenomenonTime gt %s", I700_800), getFromList(OBSERVATIONS, 5, 6, 7, 14, 15, 22, 24));

        filterAndCheck(doa, String.format("%s gt resultTime", T700), getFromList(OBSERVATIONS, 0, 1, 21));
        filterAndCheck(doa, String.format("%s gt validTime", T700), getFromList(OBSERVATIONS, 8, 9, 23));
        filterAndCheck(doa, String.format("%s gt phenomenonTime", T700), getFromList(OBSERVATIONS, 0, 1, 8, 9, 21, 23));

        filterAndCheck(doa, String.format("%s gt resultTime", I700_800), getFromList(OBSERVATIONS, 0, 1, 21));
        filterAndCheck(doa, String.format("%s gt validTime", I700_800), getFromList(OBSERVATIONS, 8, 9, 23));
        filterAndCheck(doa, String.format("%s gt phenomenonTime", I700_800), getFromList(OBSERVATIONS, 0, 1, 8, 9, 21, 23));
    }

    @Test
    public void testLe() throws ServiceFailureException {
        ObservationDao doa = service.observations();
        filterAndCheck(doa, String.format("resultTime le %s", T700), getFromList(OBSERVATIONS, 0, 1, 2, 21));
        filterAndCheck(doa, String.format("validTime le %s", T700), getFromList(OBSERVATIONS, 8, 9, 23));
        filterAndCheck(doa, String.format("phenomenonTime le %s", T700), getFromList(OBSERVATIONS, 0, 1, 2, 8, 9, 21, 23));

        filterAndCheck(doa, String.format("resultTime le %s", I700_800), getFromList(OBSERVATIONS, 0, 1, 2, 21));
        filterAndCheck(doa, String.format("validTime le %s", I700_800), getFromList(OBSERVATIONS, 8, 9, 10, 11, 17, 19, 23));
        filterAndCheck(doa, String.format("phenomenonTime le %s", I700_800), getFromList(OBSERVATIONS, 0, 1, 2, 8, 9, 10, 11, 17, 19, 21, 23));

        filterAndCheck(doa, String.format("%s le resultTime", T800), getFromList(OBSERVATIONS, 5, 6, 7, 22));
        filterAndCheck(doa, String.format("%s le validTime", T800), getFromList(OBSERVATIONS, 14, 15, 24));
        filterAndCheck(doa, String.format("%s le phenomenonTime", T800), getFromList(OBSERVATIONS, 5, 6, 7, 14, 15, 22, 24));

        filterAndCheck(doa, String.format("%s le resultTime", I700_800), getFromList(OBSERVATIONS, 5, 6, 7, 22));
        filterAndCheck(doa, String.format("%s le validTime", I700_800), getFromList(OBSERVATIONS, 11, 13, 14, 15, 18, 20, 24));
        filterAndCheck(doa, String.format("%s le phenomenonTime", I700_800), getFromList(OBSERVATIONS, 5, 6, 7, 11, 13, 14, 15, 18, 20, 22, 24));
    }

    @Test
    public void testGe() throws ServiceFailureException {
        ObservationDao doa = service.observations();
        filterAndCheck(doa, String.format("resultTime ge %s", T800), getFromList(OBSERVATIONS, 5, 6, 7, 22));
        filterAndCheck(doa, String.format("validTime ge %s", T800), getFromList(OBSERVATIONS, 14, 15, 24));
        filterAndCheck(doa, String.format("phenomenonTime ge %s", T800), getFromList(OBSERVATIONS, 5, 6, 7, 14, 15, 22, 24));

        filterAndCheck(doa, String.format("resultTime ge %s", I700_800), getFromList(OBSERVATIONS, 5, 6, 7, 22));
        filterAndCheck(doa, String.format("validTime ge %s", I700_800), getFromList(OBSERVATIONS, 11, 13, 14, 15, 18, 20, 24));
        filterAndCheck(doa, String.format("phenomenonTime ge %s", I700_800), getFromList(OBSERVATIONS, 5, 6, 7, 11, 13, 14, 15, 18, 20, 22, 24));

        filterAndCheck(doa, String.format("%s ge resultTime", T700), getFromList(OBSERVATIONS, 0, 1, 2, 21));
        filterAndCheck(doa, String.format("%s ge validTime", T700), getFromList(OBSERVATIONS, 8, 9, 23));
        filterAndCheck(doa, String.format("%s ge phenomenonTime", T700), getFromList(OBSERVATIONS, 0, 1, 2, 8, 9, 21, 23));

        filterAndCheck(doa, String.format("%s ge resultTime", I700_800), getFromList(OBSERVATIONS, 0, 1, 2, 21));
        filterAndCheck(doa, String.format("%s ge validTime", I700_800), getFromList(OBSERVATIONS, 8, 9, 10, 11, 17, 19, 23));
        filterAndCheck(doa, String.format("%s ge phenomenonTime", I700_800), getFromList(OBSERVATIONS, 0, 1, 2, 8, 9, 10, 11, 17, 19, 21, 23));
    }

    @Test
    public void testEq() throws ServiceFailureException {
        ObservationDao doa = service.observations();
        filterAndCheck(doa, String.format("resultTime eq %s", T800), getFromList(OBSERVATIONS, 5));
        filterAndCheck(doa, String.format("validTime eq %s", T800), getFromList(OBSERVATIONS));
        filterAndCheck(doa, String.format("phenomenonTime eq %s", T800), getFromList(OBSERVATIONS, 5));

        filterAndCheck(doa, String.format("resultTime eq %s", I700_800), getFromList(OBSERVATIONS));
        filterAndCheck(doa, String.format("validTime eq %s", I700_800), getFromList(OBSERVATIONS, 11));
        filterAndCheck(doa, String.format("phenomenonTime eq %s", I700_800), getFromList(OBSERVATIONS, 11));

        filterAndCheck(doa, String.format("%s eq resultTime", T700), getFromList(OBSERVATIONS, 2));
        filterAndCheck(doa, String.format("%s eq validTime", T700), getFromList(OBSERVATIONS));
        filterAndCheck(doa, String.format("%s eq phenomenonTime", T700), getFromList(OBSERVATIONS, 2));

        filterAndCheck(doa, String.format("%s eq resultTime", I700_800), getFromList(OBSERVATIONS));
        filterAndCheck(doa, String.format("%s eq validTime", I700_800), getFromList(OBSERVATIONS, 11));
        filterAndCheck(doa, String.format("%s eq phenomenonTime", I700_800), getFromList(OBSERVATIONS, 11));
    }

    @Test
    public void testBefore() throws ServiceFailureException {
        ObservationDao doa = service.observations();
        filterAndCheck(doa, String.format("before(resultTime,%s)", T700), getFromList(OBSERVATIONS, 0, 1, 21));
        filterAndCheck(doa, String.format("before(validTime,%s)", T700), getFromList(OBSERVATIONS, 8, 9, 23));
        filterAndCheck(doa, String.format("before(phenomenonTime,%s)", T700), getFromList(OBSERVATIONS, 0, 1, 8, 9, 21, 23));

        filterAndCheck(doa, String.format("before(resultTime,%s)", I700_800), getFromList(OBSERVATIONS, 0, 1, 21));
        filterAndCheck(doa, String.format("before(validTime,%s)", I700_800), getFromList(OBSERVATIONS, 8, 9, 23));
        filterAndCheck(doa, String.format("before(phenomenonTime,%s)", I700_800), getFromList(OBSERVATIONS, 0, 1, 8, 9, 21, 23));

        filterAndCheck(doa, String.format("before(%s,resultTime)", T800), getFromList(OBSERVATIONS, 6, 7, 22));
        filterAndCheck(doa, String.format("before(%s,validTime)", T800), getFromList(OBSERVATIONS, 15, 24));
        filterAndCheck(doa, String.format("before(%s,phenomenonTime)", T800), getFromList(OBSERVATIONS, 6, 7, 15, 22, 24));

        filterAndCheck(doa, String.format("before(%s,resultTime)", I700_800), getFromList(OBSERVATIONS, 5, 6, 7, 22));
        filterAndCheck(doa, String.format("before(%s,validTime)", I700_800), getFromList(OBSERVATIONS, 14, 15, 24));
        filterAndCheck(doa, String.format("before(%s,phenomenonTime)", I700_800), getFromList(OBSERVATIONS, 5, 6, 7, 14, 15, 22, 24));
    }

    @Test
    public void testAfter() throws ServiceFailureException {
        ObservationDao doa = service.observations();
        filterAndCheck(doa, String.format("after(resultTime,%s)", T800), getFromList(OBSERVATIONS, 6, 7, 22));
        filterAndCheck(doa, String.format("after(validTime,%s)", T800), getFromList(OBSERVATIONS, 15, 24));
        filterAndCheck(doa, String.format("after(phenomenonTime,%s)", T800), getFromList(OBSERVATIONS, 6, 7, 15, 22, 24));

        filterAndCheck(doa, String.format("after(resultTime,%s)", I700_800), getFromList(OBSERVATIONS, 5, 6, 7, 22));
        filterAndCheck(doa, String.format("after(validTime,%s)", I700_800), getFromList(OBSERVATIONS, 14, 15, 24));
        filterAndCheck(doa, String.format("after(phenomenonTime,%s)", I700_800), getFromList(OBSERVATIONS, 5, 6, 7, 14, 15, 22, 24));

        filterAndCheck(doa, String.format("after(%s,resultTime)", T700), getFromList(OBSERVATIONS, 0, 1, 21));
        filterAndCheck(doa, String.format("after(%s,validTime)", T700), getFromList(OBSERVATIONS, 8, 9, 23));
        filterAndCheck(doa, String.format("after(%s,phenomenonTime)", T700), getFromList(OBSERVATIONS, 0, 1, 8, 9, 21, 23));

        filterAndCheck(doa, String.format("after(%s,resultTime)", I700_800), getFromList(OBSERVATIONS, 0, 1, 21));
        filterAndCheck(doa, String.format("after(%s,validTime)", I700_800), getFromList(OBSERVATIONS, 8, 9, 23));
        filterAndCheck(doa, String.format("after(%s,phenomenonTime)", I700_800), getFromList(OBSERVATIONS, 0, 1, 8, 9, 21, 23));
    }

    @Test
    public void testMeets() throws ServiceFailureException {
        ObservationDao doa = service.observations();
        filterAndCheck(doa, String.format("meets(resultTime,%s)", T700), getFromList(OBSERVATIONS, 2));
        filterAndCheck(doa, String.format("meets(validTime,%s)", T700), getFromList(OBSERVATIONS, 9, 11, 17, 18));
        filterAndCheck(doa, String.format("meets(phenomenonTime,%s)", T700), getFromList(OBSERVATIONS, 2, 9, 11, 17, 18));

        filterAndCheck(doa, String.format("meets(resultTime,%s)", I700_800), getFromList(OBSERVATIONS, 2, 5));
        filterAndCheck(doa, String.format("meets(validTime,%s)", I700_800), getFromList(OBSERVATIONS, 9, 14));
        filterAndCheck(doa, String.format("meets(phenomenonTime,%s)", I700_800), getFromList(OBSERVATIONS, 2, 5, 9, 14));

        filterAndCheck(doa, String.format("meets(%s,resultTime)", T700), getFromList(OBSERVATIONS, 2));
        filterAndCheck(doa, String.format("meets(%s,validTime)", T700), getFromList(OBSERVATIONS, 9, 11, 17, 18));
        filterAndCheck(doa, String.format("meets(%s,phenomenonTime)", T700), getFromList(OBSERVATIONS, 2, 9, 11, 17, 18));

        filterAndCheck(doa, String.format("meets(%s,resultTime)", I700_800), getFromList(OBSERVATIONS, 2, 5));
        filterAndCheck(doa, String.format("meets(%s,validTime)", I700_800), getFromList(OBSERVATIONS, 9, 14));
        filterAndCheck(doa, String.format("meets(%s,phenomenonTime)", I700_800), getFromList(OBSERVATIONS, 2, 5, 9, 14));
    }

    @Test
    public void testDuring() throws ServiceFailureException {
        ObservationDao doa = service.observations();
        filterForException(doa, String.format("during(resultTime,%s)", T700), 400);
        filterForException(doa, String.format("during(validTime,%s)", T700), 400);
        filterForException(doa, String.format("during(phenomenonTime,%s)", T700), 400);

        filterAndCheck(doa, String.format("during(resultTime,%s)", I700_800), getFromList(OBSERVATIONS, 2, 3, 4));
        filterAndCheck(doa, String.format("during(validTime,%s)", I700_800), getFromList(OBSERVATIONS, 11, 12, 17, 20));
        filterAndCheck(doa, String.format("during(phenomenonTime,%s)", I700_800), getFromList(OBSERVATIONS, 2, 3, 4, 11, 12, 17, 20));

        filterForException(doa, String.format("during(%s,resultTime)", T700), 400);
        filterAndCheck(doa, String.format("during(%s,validTime)", T700), getFromList(OBSERVATIONS, 10, 11, 16, 17, 18, 19));
        filterAndCheck(doa, String.format("during(%s,phenomenonTime)", T700), getFromList(OBSERVATIONS, 10, 11, 16, 17, 18, 19));

        filterForException(doa, String.format("during(%s,resultTime)", I700_800), 400);
        filterAndCheck(doa, String.format("during(%s,validTime)", I700_800), getFromList(OBSERVATIONS, 11, 16, 18, 19));
        filterAndCheck(doa, String.format("during(%s,phenomenonTime)", I700_800), getFromList(OBSERVATIONS, 11, 16, 18, 19));
    }

    @Test
    public void testOverlaps() throws ServiceFailureException {
        ObservationDao doa = service.observations();
        filterAndCheck(doa, String.format("overlaps(resultTime,%s)", T700), getFromList(OBSERVATIONS, 2));
        filterAndCheck(doa, String.format("overlaps(validTime,%s)", T700), getFromList(OBSERVATIONS, 10, 11, 16, 17, 18, 19));
        filterAndCheck(doa, String.format("overlaps(phenomenonTime,%s)", T700), getFromList(OBSERVATIONS, 2, 10, 11, 16, 17, 18, 19));

        filterAndCheck(doa, String.format("overlaps(resultTime,%s)", I700_800), getFromList(OBSERVATIONS, 2, 3, 4));
        filterAndCheck(doa, String.format("overlaps(validTime,%s)", I700_800), getFromList(OBSERVATIONS, 10, 11, 12, 13, 16, 17, 18, 19, 20));
        filterAndCheck(doa, String.format("overlaps(phenomenonTime,%s)", I700_800), getFromList(OBSERVATIONS, 2, 3, 4, 10, 11, 12, 13, 16, 17, 18, 19, 20));

        filterAndCheck(doa, String.format("overlaps(%s,resultTime)", T700), getFromList(OBSERVATIONS, 2));
        filterAndCheck(doa, String.format("overlaps(%s,validTime)", T700), getFromList(OBSERVATIONS, 10, 11, 16, 17, 18, 19));
        filterAndCheck(doa, String.format("overlaps(%s,phenomenonTime)", T700), getFromList(OBSERVATIONS, 2, 10, 11, 16, 17, 18, 19));

        filterAndCheck(doa, String.format("overlaps(%s,resultTime)", I700_800), getFromList(OBSERVATIONS, 2, 3, 4));
        filterAndCheck(doa, String.format("overlaps(%s,validTime)", I700_800), getFromList(OBSERVATIONS, 10, 11, 12, 13, 16, 17, 18, 19, 20));
        filterAndCheck(doa, String.format("overlaps(%s,phenomenonTime)", I700_800), getFromList(OBSERVATIONS, 2, 3, 4, 10, 11, 12, 13, 16, 17, 18, 19, 20));
    }

    @Test
    public void testStarts() throws ServiceFailureException {
        ObservationDao doa = service.observations();
        filterAndCheck(doa, String.format("starts(resultTime,%s)", T700), getFromList(OBSERVATIONS, 2));
        filterAndCheck(doa, String.format("starts(validTime,%s)", T700), getFromList(OBSERVATIONS, 11, 17, 18));
        filterAndCheck(doa, String.format("starts(phenomenonTime,%s)", T700), getFromList(OBSERVATIONS, 2, 11, 17, 18));

        filterAndCheck(doa, String.format("starts(resultTime,%s)", I700_800), getFromList(OBSERVATIONS, 2));
        filterAndCheck(doa, String.format("starts(validTime,%s)", I700_800), getFromList(OBSERVATIONS, 11, 17, 18));
        filterAndCheck(doa, String.format("starts(phenomenonTime,%s)", I700_800), getFromList(OBSERVATIONS, 2, 11, 17, 18));

        filterAndCheck(doa, String.format("starts(%s,resultTime)", T700), getFromList(OBSERVATIONS, 2));
        filterAndCheck(doa, String.format("starts(%s,validTime)", T700), getFromList(OBSERVATIONS, 11, 17, 18));
        filterAndCheck(doa, String.format("starts(%s,phenomenonTime)", T700), getFromList(OBSERVATIONS, 2, 11, 17, 18));

        filterAndCheck(doa, String.format("starts(%s,resultTime)", I700_800), getFromList(OBSERVATIONS, 2));
        filterAndCheck(doa, String.format("starts(%s,validTime)", I700_800), getFromList(OBSERVATIONS, 11, 17, 18));
        filterAndCheck(doa, String.format("starts(%s,phenomenonTime)", I700_800), getFromList(OBSERVATIONS, 2, 11, 17, 18));
    }

    @Test
    public void testFinishes() throws ServiceFailureException {
        ObservationDao doa = service.observations();
        filterAndCheck(doa, String.format("finishes(resultTime,%s)", T800), getFromList(OBSERVATIONS, 5));
        filterAndCheck(doa, String.format("finishes(validTime,%s)", T800), getFromList(OBSERVATIONS, 11, 19, 20));
        filterAndCheck(doa, String.format("finishes(phenomenonTime,%s)", T800), getFromList(OBSERVATIONS, 5, 11, 19, 20));

        filterAndCheck(doa, String.format("finishes(resultTime,%s)", I700_800), getFromList(OBSERVATIONS, 5));
        filterAndCheck(doa, String.format("finishes(validTime,%s)", I700_800), getFromList(OBSERVATIONS, 11, 19, 20));
        filterAndCheck(doa, String.format("finishes(phenomenonTime,%s)", I700_800), getFromList(OBSERVATIONS, 5, 11, 19, 20));

        filterAndCheck(doa, String.format("finishes(%s,resultTime)", T800), getFromList(OBSERVATIONS, 5));
        filterAndCheck(doa, String.format("finishes(%s,validTime)", T800), getFromList(OBSERVATIONS, 11, 19, 20));
        filterAndCheck(doa, String.format("finishes(%s,phenomenonTime)", T800), getFromList(OBSERVATIONS, 5, 11, 19, 20));

        filterAndCheck(doa, String.format("finishes(%s,resultTime)", I700_800), getFromList(OBSERVATIONS, 5));
        filterAndCheck(doa, String.format("finishes(%s,validTime)", I700_800), getFromList(OBSERVATIONS, 11, 19, 20));
        filterAndCheck(doa, String.format("finishes(%s,phenomenonTime)", I700_800), getFromList(OBSERVATIONS, 5, 11, 19, 20));
    }

    @Test
    public void testYear() throws ServiceFailureException {
        ObservationDao doa = service.observations();
        filterAndCheck(doa, String.format("year(resultTime) eq 2015"), getFromList(OBSERVATIONS, 21));
        filterAndCheck(doa, String.format("year(validTime) eq 2015"), getFromList(OBSERVATIONS, 23));
        filterAndCheck(doa, String.format("year(phenomenonTime) eq 2015"), getFromList(OBSERVATIONS, 21, 23));
    }

    @Test
    public void testDurations() throws ServiceFailureException {
        ObservationDao doa = service.observations();
        // Durations
        filterAndCheck(doa, String.format("resultTime add duration'PT1H' gt %s", T900), getFromList(OBSERVATIONS, 6, 7, 22));
        filterAndCheck(doa, String.format("validTime add duration'PT1H' gt %s", T900), getFromList(OBSERVATIONS, 15, 24));
        filterAndCheck(doa, String.format("phenomenonTime add duration'PT1H' gt %s", T900), getFromList(OBSERVATIONS, 6, 7, 15, 22, 24));

        filterAndCheck(doa, String.format("resultTime gt %s sub duration'PT1H'", T900), getFromList(OBSERVATIONS, 6, 7, 22));
        filterAndCheck(doa, String.format("validTime gt %s sub duration'PT1H'", T900), getFromList(OBSERVATIONS, 15, 24));
        filterAndCheck(doa, String.format("phenomenonTime gt %s sub duration'PT1H'", T900), getFromList(OBSERVATIONS, 6, 7, 15, 22, 24));

        filterAndCheck(doa, String.format("resultTime sub duration'PT1H' gt %s", T700), getFromList(OBSERVATIONS, 6, 7, 22));
        filterAndCheck(doa, String.format("validTime sub duration'PT1H' gt %s", T700), getFromList(OBSERVATIONS, 15, 24));
        filterAndCheck(doa, String.format("phenomenonTime sub duration'PT1H' gt %s", T700), getFromList(OBSERVATIONS, 6, 7, 15, 22, 24));

        filterAndCheck(doa, String.format("resultTime lt %s add duration'PT1H'", I600_700), getFromList(OBSERVATIONS, 0, 1, 21));
        filterAndCheck(doa, String.format("validTime lt %s add duration'PT1H'", I600_700), getFromList(OBSERVATIONS, 8, 9, 23));
        filterAndCheck(doa, String.format("phenomenonTime lt %s add duration'PT1H'", I600_700), getFromList(OBSERVATIONS, 0, 1, 8, 9, 21, 23));

        filterAndCheck(doa, String.format("resultTime gt %s sub duration'PT1H'", I800_900), getFromList(OBSERVATIONS, 5, 6, 7, 22));
        filterAndCheck(doa, String.format("validTime gt %s sub duration'PT1H'", I800_900), getFromList(OBSERVATIONS, 14, 15, 24));
        filterAndCheck(doa, String.format("phenomenonTime gt %s sub duration'PT1H'", I800_900), getFromList(OBSERVATIONS, 5, 6, 7, 14, 15, 22, 24));

        filterAndCheck(doa, String.format("phenomenonTime sub %s eq duration'PT1H'", T800), getFromList(OBSERVATIONS, 7));
    }

    @Test
    public void testAlternativeOverlaps() throws ServiceFailureException {
        ObservationDao doa = service.observations();

        filterAndCheck(doa, String.format("not resultTime lt %s and not resultTime ge %s", T700, T800), getFromList(OBSERVATIONS, 2, 3, 4));
        filterAndCheck(doa, String.format("not validTime lt %s and not validTime ge %s", T700, T800), getFromList(OBSERVATIONS, 10, 11, 12, 13, 16, 17, 18, 19, 20));
        filterAndCheck(doa, String.format("not phenomenonTime lt %s and not phenomenonTime ge %s", T700, T800), getFromList(OBSERVATIONS, 2, 3, 4, 10, 11, 12, 13, 16, 17, 18, 19, 20));
    }

    public static <T extends Entity<T>> List<T> getFromList(List<T> list, int... ids) {
        List<T> result = new ArrayList<>();
        for (int i : ids) {
            result.add(list.get(i));
        }
        return result;
    }

    public static <T extends Entity<T>> List<T> getFromListExcept(List<T> list, int... ids) {
        List<T> result = new ArrayList<>(list);
        for (int i : ids) {
            result.remove(list.get(i));
        }
        return result;
    }

    public static <T extends Entity<T>> List<T> removeFromList(List<T> sourceList, List<T> remaining, int... ids) {
        for (int i : ids) {
            remaining.remove(sourceList.get(i));
        }
        return remaining;
    }
}
