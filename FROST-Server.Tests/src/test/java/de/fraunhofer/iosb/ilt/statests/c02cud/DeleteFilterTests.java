package de.fraunhofer.iosb.ilt.statests.c02cud;

import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.dao.BaseDao;
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
import de.fraunhofer.iosb.ilt.statests.util.Utils;
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
 * Tests additional details not part of the official tests.
 *
 * @author Hylke van der Schaaf
 */
public class DeleteFilterTests extends AbstractTestClass {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteFilterTests.class);

    private static final List<Thing> things = new ArrayList<>();
    private static final List<Datastream> datastreams = new ArrayList<>();
    private static final List<Observation> observations = new ArrayList<>();
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

    public DeleteFilterTests(ServerVersion version) throws Exception {
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
        things.add(thing);
        Location location = new Location("Location 1.0", "Location of Thing 1.", "application/vnd.geo+json", new Point(8, 51));
        thing.getLocations().add(location);
        service.create(thing);

        Sensor sensor = new Sensor("Sensor 1", "The first sensor.", "text", "Some metadata.");
        ObservedProperty obsProp = new ObservedProperty("Temperature", new URI("http://ucom.org/temperature"), "The temperature of the thing.");
        {
            Datastream datastream = new Datastream("Datastream 1", "The temperature of thing 1, sensor 1.", "someType", new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
            datastream.setThing(thing);
            datastream.setSensor(sensor);
            datastream.setObservedProperty(obsProp);
            service.create(datastream);
            datastreams.add(datastream);
        }
        {
            Datastream datastream = new Datastream("Datastream 2", "The alternate temperature of thing 1, sensor 1.", "someType", new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
            datastream.setThing(thing);
            datastream.setSensor(sensor);
            datastream.setObservedProperty(obsProp);
            service.create(datastream);
            datastreams.add(datastream);
        }
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
    }

    private void recreateObservations() throws ServiceFailureException {
        EntityUtils.deleteAll(service.observations());
        observations.clear();
        recreateObservations(datastreams.get(0));
        recreateObservations(datastreams.get(1));
    }

    private void recreateObservations(Datastream datastream) throws ServiceFailureException {
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

    private void createObservation(double result, Datastream ds, Interval pt, ZonedDateTime rt, Interval vt) throws ServiceFailureException {
        createObservation(result, ds, new TimeObject(pt), rt, vt);
    }

    private void createObservation(double result, Datastream ds, ZonedDateTime pt, ZonedDateTime rt, Interval vt) throws ServiceFailureException {
        createObservation(result, ds, new TimeObject(pt), rt, vt);
    }

    private void createObservation(double result, Datastream ds, TimeObject pt, ZonedDateTime rt, Interval vt) throws ServiceFailureException {
        Observation o = new Observation(result, ds);
        o.setPhenomenonTime(pt);
        o.setResultTime(rt);
        o.setValidTime(vt);
        service.create(o);
        observations.add(o);
    }

    public void deleteAndCheck(BaseDao doa, String filter, List<? extends Entity> expected) {
        try {
            doa.query().filter(filter).delete();

            EntityList<Observation> result = service.observations().query().list();
            EntityUtils.resultTestResult check = EntityUtils.resultContains(result, expected);
            String message = "Failed on filter: " + filter + " Cause: " + check.message;
            Assert.assertTrue(message, check.testOk);
        } catch (ServiceFailureException ex) {
            LOGGER.error("Failed to call service.", ex);
            Assert.fail("Failed to call service." + ex.getMessage());
        }
    }

    /**
     * Tests if filtered deletes are working, when filtering by resultTime,
     * validTime or phenomenonTime.
     *
     * @throws ServiceFailureException if the service connection fails.
     */
    @Test
    public void testDeleteByTime() throws ServiceFailureException {
        LOGGER.info("  testDeleteByTime");
        recreateObservations();
        Datastream ds1 = datastreams.get(0);
        BaseDao<Observation> doaDs1 = ds1.observations();

        List<Observation> remaining = Utils.getFromListExcept(observations, 0, 1, 21);
        deleteAndCheck(doaDs1, String.format("resultTime lt %s", T700), remaining);
        deleteAndCheck(doaDs1, String.format("validTime lt %s", T700), Utils.removeFromList(observations, remaining, 8, 9, 23));
        deleteAndCheck(doaDs1, String.format("%s lt phenomenonTime", T800), Utils.removeFromList(observations, remaining, 6, 7, 15, 22, 24));
    }

    private static void cleanup() throws ServiceFailureException {
        EntityUtils.deleteAll(service);
        things.clear();
        datastreams.clear();
        observations.clear();
    }
}
