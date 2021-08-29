package de.fraunhofer.iosb.ilt.statests.c05multidatastream;

import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.dao.BaseDao;
import de.fraunhofer.iosb.ilt.sta.dao.MultiDatastreamDao;
import de.fraunhofer.iosb.ilt.sta.dao.ObservationDao;
import de.fraunhofer.iosb.ilt.sta.model.Entity;
import de.fraunhofer.iosb.ilt.sta.model.Location;
import de.fraunhofer.iosb.ilt.sta.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.TimeObject;
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import static de.fraunhofer.iosb.ilt.statests.util.EntityUtils.filterAndCheck;
import static de.fraunhofer.iosb.ilt.statests.util.EntityUtils.filterForException;
import static de.fraunhofer.iosb.ilt.statests.util.Utils.getFromList;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.geojson.Point;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.extra.Interval;

/**
 * Tests date and time functions.
 *
 * @author Hylke van der Schaaf
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DateTimeTests extends AbstractTestClass {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DateTimeTests.class);

    private static final List<Thing> THINGS = new ArrayList<>();
    private static final List<Observation> OBSERVATIONS = new ArrayList<>();
    private static final List<MultiDatastream> MULTI_DATASTREAMS = new ArrayList<>();
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
        MULTI_DATASTREAMS.clear();
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
        MultiDatastream datastream = new MultiDatastream(
                "Datastream 1",
                "The temperature of thing 1, sensor 1.",
                Arrays.asList("someType"),
                Arrays.asList(new UnitOfMeasurement("degree celcius", "°C", "ucum:T")));
        datastream.setThing(thing);
        datastream.setSensor(sensor);
        datastream.getObservedProperties().add(obsProp);
        service.create(datastream);
        MULTI_DATASTREAMS.add(datastream);

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
        MultiDatastream datastream2 = new MultiDatastream(
                "Datastream 2",
                "The second temperature of thing 1, sensor 1.",
                Arrays.asList("someType"),
                Arrays.asList(new UnitOfMeasurement("degree celcius", "°C", "ucum:T")));
        datastream2.setThing(thing);
        datastream2.setSensor(sensor);
        datastream2.getObservedProperties().add(obsProp);
        service.create(datastream2);
        MULTI_DATASTREAMS.add(datastream2);
    }

    private static void createObservation(double result, MultiDatastream ds, Interval pt, ZonedDateTime rt, Interval vt) throws ServiceFailureException {
        createObservation(result, ds, new TimeObject(pt), rt, vt);
    }

    private static void createObservation(double result, MultiDatastream ds, ZonedDateTime pt, ZonedDateTime rt, Interval vt) throws ServiceFailureException {
        createObservation(result, ds, new TimeObject(pt), rt, vt);
    }

    private static void createObservation(double result, MultiDatastream ds, TimeObject pt, ZonedDateTime rt, Interval vt) throws ServiceFailureException {
        Observation o = new Observation(new double[]{result}, ds);
        o.setPhenomenonTime(pt);
        o.setResultTime(rt);
        o.setValidTime(vt);
        service.create(o);
        OBSERVATIONS.add(o);
    }

    public <T extends Entity<T>> void filterAndCheckDs(BaseDao<T> doa, String filter, List<T> expected) {
        if (expected == null) {
            return;
        }
        filterAndCheck(doa, filter.replace("{}", "phenomenonTime"), expected);
        filterAndCheck(doa, filter.replace("{}", "resultTime"), expected);
    }

    public void testDsTpl(String tpl,
            List<MultiDatastream> t2014,
            List<MultiDatastream> t2015,
            List<MultiDatastream> t700,
            List<MultiDatastream> t2017_2,
            List<MultiDatastream> t2018,
            List<MultiDatastream> i78,
            List<MultiDatastream> i2014_2015,
            List<MultiDatastream> i2014_2017_2,
            List<MultiDatastream> i2014_2018,
            List<MultiDatastream> i2015_2017_2,
            List<MultiDatastream> i2015_2018,
            List<MultiDatastream> i2017_2_2018) {
        MultiDatastreamDao dsDoa = service.multiDatastreams();
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
            List<Observation> rtOpT7,
            List<Observation> vtOpT7,
            List<Observation> ptOpT7,
            List<Observation> rtOpT78,
            List<Observation> vtOpT78,
            List<Observation> ptOpT78) {
        ObservationDao doa = service.observations();
        filterAndCheck(doa, String.format(tpl, "resultTime", T700), rtOpT7);
        filterAndCheck(doa, String.format(tpl, "validTime", T700), vtOpT7);
        filterAndCheck(doa, String.format(tpl, "phenomenonTime", T700), ptOpT7);

        filterAndCheck(doa, String.format(tpl, "resultTime", I700_800), rtOpT78);
        filterAndCheck(doa, String.format(tpl, "validTime", I700_800), vtOpT78);
        filterAndCheck(doa, String.format(tpl, "phenomenonTime", I700_800), ptOpT78);
    }

    public void testValueTime(String tpl,
            List<Observation> rtOpT7,
            List<Observation> vtOpT7,
            List<Observation> ptOpT7,
            List<Observation> rtOpT78,
            List<Observation> vtOpT78,
            List<Observation> ptOpT78) {
        ObservationDao doa = service.observations();
        filterAndCheck(doa, String.format(tpl, T700, "resultTime"), rtOpT7);
        filterAndCheck(doa, String.format(tpl, T700, "validTime"), vtOpT7);
        filterAndCheck(doa, String.format(tpl, T700, "phenomenonTime"), ptOpT7);

        filterAndCheck(doa, String.format(tpl, I700_800, "resultTime"), rtOpT78);
        filterAndCheck(doa, String.format(tpl, I700_800, "validTime"), vtOpT78);
        filterAndCheck(doa, String.format(tpl, I700_800, "phenomenonTime"), ptOpT78);
    }

    public void testTimeOpValue(String op,
            List<Observation> rtOpT7,
            List<Observation> vtOpT7,
            List<Observation> ptOpT7,
            List<Observation> rtOpT78,
            List<Observation> vtOpT78,
            List<Observation> ptOpT78) {
        testTimeValue("%s " + op + " %s", rtOpT7, vtOpT7, ptOpT7, rtOpT78, vtOpT78, ptOpT78);
    }

    public void testValueOpTime(String op,
            List<Observation> rtOpT7,
            List<Observation> vtOpT7,
            List<Observation> ptOpT7,
            List<Observation> rtOpT78,
            List<Observation> vtOpT78,
            List<Observation> ptOpT78) {
        testValueTime("%s " + op + " %s", rtOpT7, vtOpT7, ptOpT7, rtOpT78, vtOpT78, ptOpT78);
    }

    @Test
    public void test01Lt() throws ServiceFailureException {
        LOGGER.info("  test01Lt");
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
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS, 0));
    }

    @Test
    public void test02Gt() throws ServiceFailureException {
        LOGGER.info("  test02Gt");
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
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS));
    }

    @Test
    public void test03Le() throws ServiceFailureException {
        LOGGER.info("  test03Le");
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
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS, 0));
    }

    @Test
    public void test04Ge() throws ServiceFailureException {
        LOGGER.info("  test04Ge");
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
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS));
    }

    @Test
    public void test05Eq() throws ServiceFailureException {
        LOGGER.info("  test05Eq");
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
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS));
    }

    @Test
    public void test06Before() throws ServiceFailureException {
        LOGGER.info("  test06Before");
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
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS, 0));
    }

    @Test
    public void test07After() throws ServiceFailureException {
        LOGGER.info("  test07After");
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
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS));
    }

    @Test
    public void test08Meets() throws ServiceFailureException {
        LOGGER.info("  test08Meets");
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
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS, 0));
    }

    @Test
    public void test09During() throws ServiceFailureException {
        LOGGER.info("  test09During");
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
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS));
    }

    @Test
    public void test10Overlaps() throws ServiceFailureException {
        LOGGER.info("  test10Overlaps");
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
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS));
    }

    @Test
    public void test11Starts() throws ServiceFailureException {
        LOGGER.info("  test11Starts");
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
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS));
    }

    @Test
    public void test12Finishes() throws ServiceFailureException {
        LOGGER.info("  test12Finishes");
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
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS, 0),
                getFromList(MULTI_DATASTREAMS),
                getFromList(MULTI_DATASTREAMS));
    }

    @Test
    public void test13Year() throws ServiceFailureException {
        LOGGER.info("  test13Year");
        ObservationDao doa = service.observations();
        filterAndCheck(doa, String.format("year(resultTime) eq 2015"), getFromList(OBSERVATIONS, 21, 23));
        filterAndCheck(doa, String.format("year(validTime) eq 2015"), getFromList(OBSERVATIONS, 23));
        filterAndCheck(doa, String.format("year(phenomenonTime) eq 2015"), getFromList(OBSERVATIONS, 21, 23));
    }

    @Test
    public void test14Durations() throws ServiceFailureException {
        LOGGER.info("  test14Durations");
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
    public void test15AlternativeOverlaps() throws ServiceFailureException {
        LOGGER.info("  test15AlternativeOverlaps");
        ObservationDao doa = service.observations();
        filterAndCheck(doa, String.format("not resultTime lt %s and not resultTime ge %s", T700, T800), getFromList(OBSERVATIONS, 2, 3, 4));
        filterAndCheck(doa, String.format("not validTime lt %s and not validTime ge %s", T700, T800), getFromList(OBSERVATIONS, 10, 11, 12, 13, 16, 17, 18, 19, 20));
        filterAndCheck(doa, String.format("not phenomenonTime lt %s and not phenomenonTime ge %s", T700, T800), getFromList(OBSERVATIONS, 2, 3, 4, 10, 11, 12, 13, 16, 17, 18, 19, 20));
    }

    @Test
    public void test19PhenomenonTimeAfterDelete() throws ServiceFailureException {
        LOGGER.info("  test19PhenomenonTimeAfterDelete");
        EntityUtils.deleteAll(service.observations());
        MultiDatastream ds1 = service.multiDatastreams().find(MULTI_DATASTREAMS.get(0).getId());
        Assert.assertNull("phenomenonTime should be null", ds1.getPhenomenonTime());
        MultiDatastream ds2 = service.multiDatastreams().find(MULTI_DATASTREAMS.get(1).getId());
        Assert.assertNull("phenomenonTime should be null", ds2.getPhenomenonTime());
    }
}
