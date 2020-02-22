package de.fraunhofer.iosb.ilt.statests.c03filtering;

import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.StatusCodeException;
import de.fraunhofer.iosb.ilt.sta.dao.BaseDao;
import de.fraunhofer.iosb.ilt.sta.dao.DatastreamDao;
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
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.geojson.Point;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.extra.Interval;

/**
 * Tests date and time functions.
 *
 * @author Hylke van der Schaaf
 */
public class DateTimeTests extends AbstractTestClass {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DateTimeTests.class);

    private static final List<Thing> THINGS = new ArrayList<>();
    private static final List<Observation> OBSERVATIONS = new ArrayList<>();
    private static final List<Datastream> DATASTREAMS = new ArrayList<>();
    private static final ZonedDateTime T2014 = ZonedDateTime.parse("2014-01-01T06:00:00.000Z");
    private static final ZonedDateTime T2015 = ZonedDateTime.parse("2015-01-01T06:00:00.000Z");
    private static final ZonedDateTime T600 = ZonedDateTime.parse("2016-01-01T06:00:00.000Z");
    private static final ZonedDateTime T659 = ZonedDateTime.parse("2016-01-01T06:59:00.000Z");
    private static final ZonedDateTime T700 = ZonedDateTime.parse("2016-01-01T07:00:00.000Z");
    private static final ZonedDateTime T701 = ZonedDateTime.parse("2016-01-01T07:01:00.000Z");
    private static final ZonedDateTime T759 = ZonedDateTime.parse("2016-01-01T07:59:00.000Z");
    private static final ZonedDateTime T800 = ZonedDateTime.parse("2016-01-01T08:00:00.000Z");
    private static final ZonedDateTime T801 = ZonedDateTime.parse("2016-01-01T08:01:00.000Z");
    private static final ZonedDateTime T900 = ZonedDateTime.parse("2016-01-01T09:00:00.000Z");
    private static final ZonedDateTime T2017 = ZonedDateTime.parse("2017-01-01T09:00:00.000Z");
    private static final ZonedDateTime T2017_2 = T2017.plus(1, ChronoUnit.HOURS);
    private static final ZonedDateTime T2018 = ZonedDateTime.parse("2018-01-01T09:00:00.000Z");
    private static final Interval I2015 = Interval.of(T2015.toInstant(), T2015.plus(1, ChronoUnit.HOURS).toInstant());
    private static final Interval I600_659 = Interval.of(T600.toInstant(), T659.toInstant());
    private static final Interval I600_700 = Interval.of(T600.toInstant(), T700.toInstant());
    private static final Interval I600_701 = Interval.of(T600.toInstant(), T701.toInstant());
    private static final Interval I700_800 = Interval.of(T700.toInstant(), T800.toInstant());
    private static final Interval I701_759 = Interval.of(T701.toInstant(), T759.toInstant());
    private static final Interval I759_900 = Interval.of(T759.toInstant(), T900.toInstant());
    private static final Interval I800_900 = Interval.of(T800.toInstant(), T900.toInstant());
    private static final Interval I801_900 = Interval.of(T801.toInstant(), T900.toInstant());
    private static final Interval I659_801 = Interval.of(T659.toInstant(), T801.toInstant());
    private static final Interval I700_759 = Interval.of(T700.toInstant(), T759.toInstant());
    private static final Interval I700_801 = Interval.of(T700.toInstant(), T801.toInstant());
    private static final Interval I659_800 = Interval.of(T659.toInstant(), T800.toInstant());
    private static final Interval I701_800 = Interval.of(T701.toInstant(), T800.toInstant());
    private static final Interval I2017 = Interval.of(T2017.toInstant(), T2017_2.toInstant());
    private static final Interval I2014_2015 = Interval.of(T2014.toInstant(), T2015.toInstant());
    private static final Interval I2014_2017_2 = Interval.of(T2014.toInstant(), T2017_2.toInstant());
    private static final Interval I2014_2018 = Interval.of(T2014.toInstant(), T2018.toInstant());
    private static final Interval I2015_2017_2 = Interval.of(T2015.toInstant(), T2017_2.toInstant());
    private static final Interval I2015_2018 = Interval.of(T2015.toInstant(), T2018.toInstant());
    private static final Interval I2017_2_2018 = Interval.of(T2017_2.toInstant(), T2018.toInstant());

    public DateTimeTests(ServerVersion version) throws ServiceFailureException, URISyntaxException {
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

    @AfterClass
    public static void tearDown() throws ServiceFailureException {
        LOGGER.info("Tearing down.");
        cleanup();
    }

    private static void cleanup() throws ServiceFailureException {
        EntityUtils.deleteAll(service);
        THINGS.clear();
        DATASTREAMS.clear();
        OBSERVATIONS.clear();
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
        createObservation(23, datastream, I2015, T2015, I2015); // 23
        createObservation(24, datastream, I2017, T2017.plus(1, ChronoUnit.HOURS), I2017); // 24

        // A second Datastream, with no observations.
        Datastream datastream2 = new Datastream("Datastream 2", "The second temperature of thing 1, sensor 1.", "someType", new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
        datastream2.setThing(thing);
        datastream2.setSensor(sensor);
        datastream2.setObservedProperty(obsProp);
        service.create(datastream2);
        DATASTREAMS.add(datastream2);
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

    public void filterAndCheckDs(BaseDao doa, String filter, List<? extends Entity> expected) {
        if (expected == null) {
            return;
        }
        filterAndCheck(doa, filter.replace("{}", "phenomenonTime"), expected);
        filterAndCheck(doa, filter.replace("{}", "resultTime"), expected);
    }

    public void filterAndCheck(BaseDao doa, String filter, List<? extends Entity> expected) {
        try {
            EntityList<Observation> result = doa.query().filter(filter).list();
            EntityUtils.ResultTestResult check = EntityUtils.resultContains(result, expected);
            String msg = "Failed on filter: " + filter + " Cause: " + check.message;
            if (!check.testOk) {
                LOGGER.info("Failed filter: {}\nexpected {},\n     got {}.",
                        filter,
                        EntityUtils.listEntities(expected),
                        EntityUtils.listEntities(result.toList()));
            }
            Assert.assertTrue(msg, check.testOk);
        } catch (ServiceFailureException ex) {
            LOGGER.error("Exception on filter: " + filter, ex);
            Assert.fail("Failed to call service: " + filter + " " + ex.getMessage());
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

    public void testDsTpl(String tpl,
            List<? extends Entity> t2014,
            List<? extends Entity> t2015,
            List<? extends Entity> t700,
            List<? extends Entity> t2017_2,
            List<? extends Entity> t2018,
            List<? extends Entity> i78,
            List<? extends Entity> i2014_2015,
            List<? extends Entity> i2014_2017_2,
            List<? extends Entity> i2014_2018,
            List<? extends Entity> i2015_2017_2,
            List<? extends Entity> i2015_2018,
            List<? extends Entity> i2017_2_2018) {
        DatastreamDao dsDoa = service.datastreams();
        filterAndCheckDs(dsDoa, String.format(tpl, T2014), t2014);
        filterAndCheckDs(dsDoa, String.format(tpl, T2015), t2015);
        filterAndCheckDs(dsDoa, String.format(tpl, T700), t700);
        filterAndCheckDs(dsDoa, String.format(tpl, T2017_2), t2017_2);
        filterAndCheckDs(dsDoa, String.format(tpl, T2018), t2018);
        filterAndCheckDs(dsDoa, String.format(tpl, I700_800), i78);
        filterAndCheckDs(dsDoa, String.format(tpl, I2014_2015), i2014_2015);
        filterAndCheckDs(dsDoa, String.format(tpl, I2014_2017_2), i2014_2017_2);
        filterAndCheckDs(dsDoa, String.format(tpl, I2014_2018), i2014_2018);
        filterAndCheckDs(dsDoa, String.format(tpl, I2015_2017_2), i2015_2017_2);
        filterAndCheckDs(dsDoa, String.format(tpl, I2015_2018), i2015_2018);
        filterAndCheckDs(dsDoa, String.format(tpl, I2017_2_2018), i2017_2_2018);
    }

    public void testTimeValue(String tpl,
            List<? extends Entity> rtOpT7,
            List<? extends Entity> vtOpT7,
            List<? extends Entity> ptOpT7,
            List<? extends Entity> rtOpT78,
            List<? extends Entity> vtOpT78,
            List<? extends Entity> ptOpT78) {
        ObservationDao doa = service.observations();
        filterAndCheck(doa, String.format(tpl, "resultTime", T700), rtOpT7);
        filterAndCheck(doa, String.format(tpl, "validTime", T700), vtOpT7);
        filterAndCheck(doa, String.format(tpl, "phenomenonTime", T700), ptOpT7);

        filterAndCheck(doa, String.format(tpl, "resultTime", I700_800), rtOpT78);
        filterAndCheck(doa, String.format(tpl, "validTime", I700_800), vtOpT78);
        filterAndCheck(doa, String.format(tpl, "phenomenonTime", I700_800), ptOpT78);
    }

    public void testValueTime(String tpl,
            List<? extends Entity> rtOpT7,
            List<? extends Entity> vtOpT7,
            List<? extends Entity> ptOpT7,
            List<? extends Entity> rtOpT78,
            List<? extends Entity> vtOpT78,
            List<? extends Entity> ptOpT78) {
        ObservationDao doa = service.observations();
        filterAndCheck(doa, String.format(tpl, T700, "resultTime"), rtOpT7);
        filterAndCheck(doa, String.format(tpl, T700, "validTime"), vtOpT7);
        filterAndCheck(doa, String.format(tpl, T700, "phenomenonTime"), ptOpT7);

        filterAndCheck(doa, String.format(tpl, I700_800, "resultTime"), rtOpT78);
        filterAndCheck(doa, String.format(tpl, I700_800, "validTime"), vtOpT78);
        filterAndCheck(doa, String.format(tpl, I700_800, "phenomenonTime"), ptOpT78);
    }

    public void testTimeOpValue(String op,
            List<? extends Entity> rtOpT7,
            List<? extends Entity> vtOpT7,
            List<? extends Entity> ptOpT7,
            List<? extends Entity> rtOpT78,
            List<? extends Entity> vtOpT78,
            List<? extends Entity> ptOpT78) {
        testTimeValue("%s " + op + " %s", rtOpT7, vtOpT7, ptOpT7, rtOpT78, vtOpT78, ptOpT78);
    }

    public void testValueOpTime(String op,
            List<? extends Entity> rtOpT7,
            List<? extends Entity> vtOpT7,
            List<? extends Entity> ptOpT7,
            List<? extends Entity> rtOpT78,
            List<? extends Entity> vtOpT78,
            List<? extends Entity> ptOpT78) {
        testValueTime("%s " + op + " %s", rtOpT7, vtOpT7, ptOpT7, rtOpT78, vtOpT78, ptOpT78);
    }

    @Test
    public void testLt() throws ServiceFailureException {
        LOGGER.info("  testLt");
        String op = "lt";
        testTimeOpValue(op,
                getFromList(OBSERVATIONS, 0, 1, 21, 23),
                getFromList(OBSERVATIONS, 8, 9, 23),
                getFromList(OBSERVATIONS, 0, 1, 8, 9, 21, 23),
                getFromList(OBSERVATIONS, 0, 1, 21, 23),
                getFromList(OBSERVATIONS, 8, 9, 23),
                getFromList(OBSERVATIONS, 0, 1, 8, 9, 21, 23));

        testValueOpTime(op,
                getFromList(OBSERVATIONS, 3, 4, 5, 6, 7, 22, 24),
                getFromList(OBSERVATIONS, 12, 13, 14, 15, 20, 24),
                getFromList(OBSERVATIONS, 3, 4, 5, 6, 7, 12, 13, 14, 15, 20, 22, 24),
                getFromList(OBSERVATIONS, 5, 6, 7, 22, 24),
                getFromList(OBSERVATIONS, 14, 15, 24),
                getFromList(OBSERVATIONS, 5, 6, 7, 14, 15, 22, 24));

        String tpl = "{} " + op + " %s";
        testDsTpl(tpl,
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS, 0));
    }

    @Test
    public void testGt() throws ServiceFailureException {
        LOGGER.info("  testGt");
        String op = "gt";
        testTimeOpValue(op,
                getFromList(OBSERVATIONS, 3, 4, 5, 6, 7, 22, 24),
                getFromList(OBSERVATIONS, 12, 13, 14, 15, 20, 24),
                getFromList(OBSERVATIONS, 3, 4, 5, 6, 7, 12, 13, 14, 15, 20, 22, 24),
                getFromList(OBSERVATIONS, 5, 6, 7, 22, 24),
                getFromList(OBSERVATIONS, 14, 15, 24),
                getFromList(OBSERVATIONS, 5, 6, 7, 14, 15, 22, 24));

        testValueOpTime(op,
                getFromList(OBSERVATIONS, 0, 1, 21, 23),
                getFromList(OBSERVATIONS, 8, 9, 23),
                getFromList(OBSERVATIONS, 0, 1, 8, 9, 21, 23),
                getFromList(OBSERVATIONS, 0, 1, 21, 23),
                getFromList(OBSERVATIONS, 8, 9, 23),
                getFromList(OBSERVATIONS, 0, 1, 8, 9, 21, 23));

        String tpl = "{} " + op + " %s";
        testDsTpl(tpl,
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS));
    }

    @Test
    public void testLe() throws ServiceFailureException {
        LOGGER.info("  testLe");
        String op = "le";
        testTimeOpValue(op,
                getFromList(OBSERVATIONS, 0, 1, 2, 21, 23),
                getFromList(OBSERVATIONS, 8, 9, 23),
                getFromList(OBSERVATIONS, 0, 1, 2, 8, 9, 21, 23),
                getFromList(OBSERVATIONS, 0, 1, 2, 21, 23),
                getFromList(OBSERVATIONS, 8, 9, 10, 11, 17, 19, 23),
                getFromList(OBSERVATIONS, 0, 1, 2, 8, 9, 10, 11, 17, 19, 21, 23));

        testValueOpTime(op,
                getFromList(OBSERVATIONS, 2, 3, 4, 5, 6, 7, 22, 24),
                getFromList(OBSERVATIONS, 11, 12, 13, 14, 15, 17, 18, 20, 24),
                getFromList(OBSERVATIONS, 2, 3, 4, 5, 6, 7, 11, 12, 13, 14, 15, 17, 18, 20, 22, 24),
                getFromList(OBSERVATIONS, 5, 6, 7, 22, 24),
                getFromList(OBSERVATIONS, 11, 13, 14, 15, 18, 20, 24),
                getFromList(OBSERVATIONS, 5, 6, 7, 11, 13, 14, 15, 18, 20, 22, 24));

        String tpl = "{} " + op + " %s";
        testDsTpl(tpl,
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS, 0));
    }

    @Test
    public void testGe() throws ServiceFailureException {
        LOGGER.info("  testGe");
        String op = "ge";
        testTimeOpValue(op,
                getFromList(OBSERVATIONS, 2, 3, 4, 5, 6, 7, 22, 24),
                getFromList(OBSERVATIONS, 11, 12, 13, 14, 15, 17, 18, 20, 24),
                getFromList(OBSERVATIONS, 2, 3, 4, 5, 6, 7, 11, 12, 13, 14, 15, 17, 18, 20, 22, 24),
                getFromList(OBSERVATIONS, 5, 6, 7, 22, 24),
                getFromList(OBSERVATIONS, 11, 13, 14, 15, 18, 20, 24),
                getFromList(OBSERVATIONS, 5, 6, 7, 11, 13, 14, 15, 18, 20, 22, 24));

        testValueOpTime(op,
                getFromList(OBSERVATIONS, 0, 1, 2, 21, 23),
                getFromList(OBSERVATIONS, 8, 9, 23),
                getFromList(OBSERVATIONS, 0, 1, 2, 8, 9, 21, 23),
                getFromList(OBSERVATIONS, 0, 1, 2, 21, 23),
                getFromList(OBSERVATIONS, 8, 9, 10, 11, 17, 19, 23),
                getFromList(OBSERVATIONS, 0, 1, 2, 8, 9, 10, 11, 17, 19, 21, 23));

        String tpl = "{} " + op + " %s";
        testDsTpl(tpl,
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS));
    }

    @Test
    public void testEq() throws ServiceFailureException {
        LOGGER.info("  testEq");
        String op = "eq";
        testTimeOpValue(op,
                getFromList(OBSERVATIONS, 2),
                getFromList(OBSERVATIONS),
                getFromList(OBSERVATIONS, 2),
                getFromList(OBSERVATIONS),
                getFromList(OBSERVATIONS, 11),
                getFromList(OBSERVATIONS, 11));

        testValueOpTime(op,
                getFromList(OBSERVATIONS, 2),
                getFromList(OBSERVATIONS),
                getFromList(OBSERVATIONS, 2),
                getFromList(OBSERVATIONS),
                getFromList(OBSERVATIONS, 11),
                getFromList(OBSERVATIONS, 11));

        String tpl = "{} " + op + " %s";
        testDsTpl(tpl,
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS));
    }

    @Test
    public void testBefore() throws ServiceFailureException {
        LOGGER.info("  testBefore");
        String tpl = "before(%s,%s)";
        testTimeValue(tpl,
                getFromList(OBSERVATIONS, 0, 1, 21, 23),
                getFromList(OBSERVATIONS, 8, 9, 23),
                getFromList(OBSERVATIONS, 0, 1, 8, 9, 21, 23),
                getFromList(OBSERVATIONS, 0, 1, 21, 23),
                getFromList(OBSERVATIONS, 8, 9, 23),
                getFromList(OBSERVATIONS, 0, 1, 8, 9, 21, 23));

        testValueTime(tpl,
                getFromList(OBSERVATIONS, 3, 4, 5, 6, 7, 22, 24),
                getFromList(OBSERVATIONS, 12, 13, 14, 15, 20, 24),
                getFromList(OBSERVATIONS, 3, 4, 5, 6, 7, 12, 13, 14, 15, 20, 22, 24),
                getFromList(OBSERVATIONS, 5, 6, 7, 22, 24),
                getFromList(OBSERVATIONS, 14, 15, 24),
                getFromList(OBSERVATIONS, 5, 6, 7, 14, 15, 22, 24));

        tpl = "before({}, %s)";
        testDsTpl(tpl,
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS, 0));
    }

    @Test
    public void testAfter() throws ServiceFailureException {
        LOGGER.info("  testAfter");
        String tpl = "after(%s,%s)";
        testTimeValue(tpl,
                getFromList(OBSERVATIONS, 3, 4, 5, 6, 7, 22, 24),
                getFromList(OBSERVATIONS, 12, 13, 14, 15, 20, 24),
                getFromList(OBSERVATIONS, 3, 4, 5, 6, 7, 12, 13, 14, 15, 20, 22, 24),
                getFromList(OBSERVATIONS, 5, 6, 7, 22, 24),
                getFromList(OBSERVATIONS, 14, 15, 24),
                getFromList(OBSERVATIONS, 5, 6, 7, 14, 15, 22, 24));

        testValueTime(tpl,
                getFromList(OBSERVATIONS, 0, 1, 21, 23),
                getFromList(OBSERVATIONS, 8, 9, 23),
                getFromList(OBSERVATIONS, 0, 1, 8, 9, 21, 23),
                getFromList(OBSERVATIONS, 0, 1, 21, 23),
                getFromList(OBSERVATIONS, 8, 9, 23),
                getFromList(OBSERVATIONS, 0, 1, 8, 9, 21, 23));

        tpl = "after({}, %s)";
        testDsTpl(tpl,
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS));
    }

    @Test
    public void testMeets() throws ServiceFailureException {
        LOGGER.info("  testMeets");
        String tpl = "meets(%s,%s)";
        testTimeValue(tpl,
                getFromList(OBSERVATIONS, 2),
                getFromList(OBSERVATIONS, 9, 11, 17, 18),
                getFromList(OBSERVATIONS, 2, 9, 11, 17, 18),
                getFromList(OBSERVATIONS, 2, 5),
                getFromList(OBSERVATIONS, 9, 14),
                getFromList(OBSERVATIONS, 2, 5, 9, 14));

        testValueTime(tpl,
                getFromList(OBSERVATIONS, 2),
                getFromList(OBSERVATIONS, 9, 11, 17, 18),
                getFromList(OBSERVATIONS, 2, 9, 11, 17, 18),
                getFromList(OBSERVATIONS, 2, 5),
                getFromList(OBSERVATIONS, 9, 14),
                getFromList(OBSERVATIONS, 2, 5, 9, 14));

        tpl = "meets({}, %s)";
        testDsTpl(tpl,
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS, 0));
    }

    @Test
    public void testDuring() throws ServiceFailureException {
        LOGGER.info("  testDuring");
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

        String tpl = "during({}, %s)";
        testDsTpl(tpl,
                null,
                null,
                null,
                null,
                null,
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS));
    }

    @Test
    public void testOverlaps() throws ServiceFailureException {
        LOGGER.info("  testOverlaps");
        String tpl = "overlaps(%s,%s)";
        testTimeValue(tpl,
                getFromList(OBSERVATIONS, 2),
                getFromList(OBSERVATIONS, 10, 11, 16, 17, 18, 19),
                getFromList(OBSERVATIONS, 2, 10, 11, 16, 17, 18, 19),
                getFromList(OBSERVATIONS, 2, 3, 4),
                getFromList(OBSERVATIONS, 10, 11, 12, 13, 16, 17, 18, 19, 20),
                getFromList(OBSERVATIONS, 2, 3, 4, 10, 11, 12, 13, 16, 17, 18, 19, 20));

        testValueTime(tpl,
                getFromList(OBSERVATIONS, 2),
                getFromList(OBSERVATIONS, 10, 11, 16, 17, 18, 19),
                getFromList(OBSERVATIONS, 2, 10, 11, 16, 17, 18, 19),
                getFromList(OBSERVATIONS, 2, 3, 4),
                getFromList(OBSERVATIONS, 10, 11, 12, 13, 16, 17, 18, 19, 20),
                getFromList(OBSERVATIONS, 2, 3, 4, 10, 11, 12, 13, 16, 17, 18, 19, 20));

        tpl = "overlaps({}, %s)";
        testDsTpl(tpl,
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS));
    }

    @Test
    public void testStarts() throws ServiceFailureException {
        LOGGER.info("  testStarts");
        String tpl = "starts(%s,%s)";
        testTimeValue(tpl,
                getFromList(OBSERVATIONS, 2),
                getFromList(OBSERVATIONS, 11, 17, 18),
                getFromList(OBSERVATIONS, 2, 11, 17, 18),
                getFromList(OBSERVATIONS, 2),
                getFromList(OBSERVATIONS, 11, 17, 18),
                getFromList(OBSERVATIONS, 2, 11, 17, 18));

        testValueTime(tpl,
                getFromList(OBSERVATIONS, 2),
                getFromList(OBSERVATIONS, 11, 17, 18),
                getFromList(OBSERVATIONS, 2, 11, 17, 18),
                getFromList(OBSERVATIONS, 2),
                getFromList(OBSERVATIONS, 11, 17, 18),
                getFromList(OBSERVATIONS, 2, 11, 17, 18));

        tpl = "starts({}, %s)";
        testDsTpl(tpl,
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS));
    }

    @Test
    public void testFinishes() throws ServiceFailureException {
        LOGGER.info("  testFinishes");
        String tpl = "finishes(%s,%s)";
        testTimeValue(tpl,
                getFromList(OBSERVATIONS, 2),
                getFromList(OBSERVATIONS, 9),
                getFromList(OBSERVATIONS, 2, 9),
                getFromList(OBSERVATIONS, 5),
                getFromList(OBSERVATIONS, 11, 19, 20),
                getFromList(OBSERVATIONS, 5, 11, 19, 20));

        testValueTime(tpl,
                getFromList(OBSERVATIONS, 2),
                getFromList(OBSERVATIONS, 9),
                getFromList(OBSERVATIONS, 2, 9),
                getFromList(OBSERVATIONS, 5),
                getFromList(OBSERVATIONS, 11, 19, 20),
                getFromList(OBSERVATIONS, 5, 11, 19, 20));

        tpl = "finishes({}, %s)";
        testDsTpl(tpl,
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS, 0),
                getFromList(DATASTREAMS),
                getFromList(DATASTREAMS));
    }

    @Test
    public void testYear() throws ServiceFailureException {
        LOGGER.info("  testYear");
        ObservationDao doa = service.observations();
        filterAndCheck(doa, String.format("year(resultTime) eq 2015"), getFromList(OBSERVATIONS, 21, 23));
        filterAndCheck(doa, String.format("year(validTime) eq 2015"), getFromList(OBSERVATIONS, 23));
        filterAndCheck(doa, String.format("year(phenomenonTime) eq 2015"), getFromList(OBSERVATIONS, 21, 23));
    }

    @Test
    public void testDurations() throws ServiceFailureException {
        LOGGER.info("  testDurations");
        ObservationDao doa = service.observations();
        // Durations
        filterAndCheck(doa, String.format("resultTime add duration'PT1H' gt %s", T900), getFromList(OBSERVATIONS, 6, 7, 22, 24));
        filterAndCheck(doa, String.format("validTime add duration'PT1H' gt %s", T900), getFromList(OBSERVATIONS, 15, 24));
        filterAndCheck(doa, String.format("phenomenonTime add duration'PT1H' gt %s", T900), getFromList(OBSERVATIONS, 6, 7, 15, 22, 24));

        filterAndCheck(doa, String.format("resultTime gt %s sub duration'PT1H'", T900), getFromList(OBSERVATIONS, 6, 7, 22, 24));
        filterAndCheck(doa, String.format("validTime gt %s sub duration'PT1H'", T900), getFromList(OBSERVATIONS, 15, 24));
        filterAndCheck(doa, String.format("phenomenonTime gt %s sub duration'PT1H'", T900), getFromList(OBSERVATIONS, 6, 7, 15, 22, 24));

        filterAndCheck(doa, String.format("resultTime sub duration'PT1H' gt %s", T700), getFromList(OBSERVATIONS, 6, 7, 22, 24));
        filterAndCheck(doa, String.format("validTime sub duration'PT1H' gt %s", T700), getFromList(OBSERVATIONS, 15, 24));
        filterAndCheck(doa, String.format("phenomenonTime sub duration'PT1H' gt %s", T700), getFromList(OBSERVATIONS, 6, 7, 15, 22, 24));

        filterAndCheck(doa, String.format("resultTime lt %s add duration'PT1H'", I600_700), getFromList(OBSERVATIONS, 0, 1, 21, 23));
        filterAndCheck(doa, String.format("validTime lt %s add duration'PT1H'", I600_700), getFromList(OBSERVATIONS, 8, 9, 23));
        filterAndCheck(doa, String.format("phenomenonTime lt %s add duration'PT1H'", I600_700), getFromList(OBSERVATIONS, 0, 1, 8, 9, 21, 23));

        filterAndCheck(doa, String.format("resultTime gt %s sub duration'PT1H'", I800_900), getFromList(OBSERVATIONS, 5, 6, 7, 22, 24));
        filterAndCheck(doa, String.format("validTime gt %s sub duration'PT1H'", I800_900), getFromList(OBSERVATIONS, 14, 15, 24));
        filterAndCheck(doa, String.format("phenomenonTime gt %s sub duration'PT1H'", I800_900), getFromList(OBSERVATIONS, 5, 6, 7, 14, 15, 22, 24));

        filterAndCheck(doa, String.format("phenomenonTime sub %s eq duration'PT1H'", T800), getFromList(OBSERVATIONS, 7));
    }

    @Test
    public void testAlternativeOverlaps() throws ServiceFailureException {
        LOGGER.info("  testAlternativeOverlaps");
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
