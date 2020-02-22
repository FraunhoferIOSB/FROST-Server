package de.fraunhofer.iosb.ilt.statests.c03filtering;

import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.dao.BaseDao;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.Entity;
import de.fraunhofer.iosb.ilt.sta.model.FeatureOfInterest;
import de.fraunhofer.iosb.ilt.sta.model.Location;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.ext.EntityList;
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.geojson.Polygon;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.extra.Interval;

/**
 * Tests for the geospatial functions.
 *
 * @author Hylke van der Schaaf
 */
public class GeoTests extends AbstractTestClass {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GeoTests.class);

    private static final List<Datastream> DATASTREAMS = new ArrayList<>();
    private static final List<FeatureOfInterest> FEATURESOFINTEREST = new ArrayList<>();
    private static final List<Location> LOCATIONS = new ArrayList<>();
    private static final List<Observation> OBSERVATIONS = new ArrayList<>();
    private static final List<ObservedProperty> O_PROPS = new ArrayList<>();
    private static final List<Sensor> SENSORS = new ArrayList<>();
    private static final List<Thing> THINGS = new ArrayList<>();

    public GeoTests(ServerVersion version) throws ServiceFailureException, URISyntaxException {
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
        FEATURESOFINTEREST.clear();
        LOCATIONS.clear();
        SENSORS.clear();
        O_PROPS.clear();
        DATASTREAMS.clear();
        OBSERVATIONS.clear();
    }

    private static void createEntities() throws ServiceFailureException, URISyntaxException {
        createThings();
        createSensor();
        createObsProp();
        createDatastreams();
        createLocation0();
        createLocation1();
        createLocation2();
        createLocation3();
        createLocation4();
        createLocation5();
        createLocation6();
        createLocation7();
    }

    private static void createThings() throws ServiceFailureException {
        Thing thing = new Thing("Thing 1", "The first thing.");
        service.create(thing);
        THINGS.add(thing);

        thing = new Thing("Thing 2", "The second thing.");
        service.create(thing);
        THINGS.add(thing);

        thing = new Thing("Thing 3", "The third thing.");
        service.create(thing);
        THINGS.add(thing);

        thing = new Thing("Thing 4", "The fourt thing.");
        service.create(thing);
        THINGS.add(thing);
    }

    private static void createSensor() throws ServiceFailureException {
        Sensor sensor = new Sensor("Sensor 1", "The first sensor.", "text", "Some metadata.");
        service.create(sensor);
        SENSORS.add(sensor);
    }

    private static void createObsProp() throws ServiceFailureException, URISyntaxException {
        ObservedProperty obsProp = new ObservedProperty("Temperature", new URI("http://ucom.org/temperature"), "The temperature of the thing.");
        service.create(obsProp);
        O_PROPS.add(obsProp);
    }

    private static void createDatastreams() throws ServiceFailureException {
        Datastream datastream = new Datastream("Datastream 1", "The temperature of thing 1, sensor 1.", "someType", new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
        datastream.setThing(THINGS.get(0));
        datastream.setSensor(SENSORS.get(0));
        datastream.setObservedProperty(O_PROPS.get(0));
        service.create(datastream);
        DATASTREAMS.add(datastream);

        datastream = new Datastream("Datastream 2", "The temperature of thing 2, sensor 1.", "someType", new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
        datastream.setThing(THINGS.get(1));
        datastream.setSensor(SENSORS.get(0));
        datastream.setObservedProperty(O_PROPS.get(0));
        service.create(datastream);
        DATASTREAMS.add(datastream);

        datastream = new Datastream("Datastream 3", "The temperature of thing 3, sensor 1.", "someType", new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
        datastream.setThing(THINGS.get(2));
        datastream.setSensor(SENSORS.get(0));
        datastream.setObservedProperty(O_PROPS.get(0));
        service.create(datastream);
        DATASTREAMS.add(datastream);
    }

    private static void createLocation0() throws ServiceFailureException {
        // Locations 0
        Point gjo = new Point(8, 51);
        Location location = new Location("Location 1.0", "First Location of Thing 1.", "application/vnd.geo+json", gjo);
        location.getThings().add(THINGS.get(0));
        service.create(location);
        LOCATIONS.add(location);

        FeatureOfInterest featureOfInterest = new FeatureOfInterest("FoI 0", "This should be FoI #0.", "application/geo+json", gjo);
        service.create(featureOfInterest);
        FEATURESOFINTEREST.add(featureOfInterest);

        Observation o = new Observation(1, DATASTREAMS.get(0));
        o.setFeatureOfInterest(featureOfInterest);
        o.setPhenomenonTimeFrom(ZonedDateTime.parse("2016-01-01T01:01:01.000Z"));
        o.setValidTime(Interval.of(Instant.parse("2016-01-01T01:01:01.000Z"), Instant.parse("2016-01-01T23:59:59.999Z")));
        service.create(o);
        OBSERVATIONS.add(o);
    }

    private static void createLocation1() throws ServiceFailureException {
        // Locations 1
        Point gjo = new Point(8, 52);
        Location location = new Location("Location 1.1", "Second Location of Thing 1.", "application/vnd.geo+json", gjo);
        location.getThings().add(THINGS.get(0));
        service.create(location);
        LOCATIONS.add(location);

        FeatureOfInterest featureOfInterest = new FeatureOfInterest("FoI 1", "This should be FoI #1.", "application/geo+json", gjo);
        service.create(featureOfInterest);
        FEATURESOFINTEREST.add(featureOfInterest);

        Observation o = new Observation(2, DATASTREAMS.get(0));
        o.setFeatureOfInterest(featureOfInterest);
        o.setPhenomenonTimeFrom(ZonedDateTime.parse("2016-01-02T01:01:01.000Z"));
        o.setValidTime(Interval.of(Instant.parse("2016-01-02T01:01:01.000Z"), Instant.parse("2016-01-02T23:59:59.999Z")));
        service.create(o);
        OBSERVATIONS.add(o);
    }

    private static void createLocation2() throws ServiceFailureException {
        // Locations 2
        Point gjo = new Point(8, 53);
        Location location = new Location("Location 2", "Location of Thing 2.", "application/vnd.geo+json", gjo);
        location.getThings().add(THINGS.get(1));
        service.create(location);
        LOCATIONS.add(location);

        FeatureOfInterest featureOfInterest = new FeatureOfInterest("FoI 2", "This should be FoI #2.", "application/geo+json", gjo);
        service.create(featureOfInterest);
        FEATURESOFINTEREST.add(featureOfInterest);

        Observation o = new Observation(3, DATASTREAMS.get(1));
        o.setFeatureOfInterest(featureOfInterest);
        o.setPhenomenonTimeFrom(ZonedDateTime.parse("2016-01-03T01:01:01.000Z"));
        o.setValidTime(Interval.of(Instant.parse("2016-01-03T01:01:01.000Z"), Instant.parse("2016-01-03T23:59:59.999Z")));
        service.create(o);
        OBSERVATIONS.add(o);
    }

    private static void createLocation3() throws ServiceFailureException {
        // Locations 3
        Point gjo = new Point(8, 54);
        Location location = new Location("Location 3", "Location of Thing 3.", "application/vnd.geo+json", gjo);
        location.getThings().add(THINGS.get(2));
        service.create(location);
        LOCATIONS.add(location);

        FeatureOfInterest featureOfInterest = new FeatureOfInterest("FoI 3", "This should be FoI #3.", "application/geo+json", gjo);
        service.create(featureOfInterest);
        FEATURESOFINTEREST.add(featureOfInterest);

        Observation o = new Observation(4, DATASTREAMS.get(2));
        o.setFeatureOfInterest(featureOfInterest);
        o.setPhenomenonTimeFrom(ZonedDateTime.parse("2016-01-04T01:01:01.000Z"));
        o.setValidTime(Interval.of(Instant.parse("2016-01-04T01:01:01.000Z"), Instant.parse("2016-01-04T23:59:59.999Z")));
        service.create(o);
        OBSERVATIONS.add(o);
    }

    private static void createLocation4() throws ServiceFailureException {
        // Locations 4
        Polygon gjo = new Polygon(
                new LngLatAlt(8, 53),
                new LngLatAlt(7, 52),
                new LngLatAlt(7, 53),
                new LngLatAlt(8, 53));
        Location location = new Location("Location 4", "Location of Thing 4.", "application/vnd.geo+json", gjo);
        location.getThings().add(THINGS.get(3));
        service.create(location);
        LOCATIONS.add(location);

        FeatureOfInterest featureOfInterest = new FeatureOfInterest("FoI 4", "This should be FoI #4.", "application/geo+json", gjo);
        service.create(featureOfInterest);
        FEATURESOFINTEREST.add(featureOfInterest);
    }

    private static void createLocation5() throws ServiceFailureException {
        // Locations 5
        LineString gjo = new LineString(
                new LngLatAlt(5, 52),
                new LngLatAlt(5, 53));
        Location location = new Location("Location 5", "A line.", "application/vnd.geo+json", gjo);
        service.create(location);
        LOCATIONS.add(location);

        FeatureOfInterest featureOfInterest = new FeatureOfInterest("FoI 5", "This should be FoI #5.", "application/geo+json", gjo);
        service.create(featureOfInterest);
        FEATURESOFINTEREST.add(featureOfInterest);
    }

    private static void createLocation6() throws ServiceFailureException {
        // Locations 6
        LineString gjo = new LineString(
                new LngLatAlt(5, 52),
                new LngLatAlt(6, 53));
        Location location = new Location("Location 6", "A longer line.", "application/vnd.geo+json", gjo);
        service.create(location);
        LOCATIONS.add(location);

        FeatureOfInterest featureOfInterest = new FeatureOfInterest("FoI 6", "This should be FoI #6.", "application/geo+json", gjo);
        service.create(featureOfInterest);
        FEATURESOFINTEREST.add(featureOfInterest);
    }

    private static void createLocation7() throws ServiceFailureException {
        // Locations 7
        LineString gjo = new LineString(
                new LngLatAlt(4, 52),
                new LngLatAlt(8, 52));
        Location location = new Location("Location 7", "The longest line.", "application/vnd.geo+json",
                gjo);
        service.create(location);
        LOCATIONS.add(location);

        FeatureOfInterest featureOfInterest = new FeatureOfInterest("FoI 7", "This should be FoI #7.", "application/geo+json", gjo);
        service.create(featureOfInterest);
        FEATURESOFINTEREST.add(featureOfInterest);
    }

    public static void filterAndCheck(BaseDao doa, String filter, List<? extends Entity> expected) {
        try {
            EntityList<Observation> result = doa.query().filter(filter).list();
            EntityUtils.ResultTestResult check = EntityUtils.resultContains(result, expected);
            String msg = "Failed on filter: " + filter + " Cause: " + check.message;
            Assert.assertTrue(msg, check.testOk);
        } catch (ServiceFailureException ex) {
            Assert.fail("Failed to call service: " + ex.getMessage());
        }
    }

    /**
     * Test the geo.distance filter function.
     *
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    public void testGeoDistance() throws ServiceFailureException {
        LOGGER.info("  testGeoDistance");
        filterAndCheck(service.locations(), "geo.distance(location, geography'POINT(8 54.1)') lt 1", getFromList(LOCATIONS, 3));
        filterAndCheck(service.locations(), "geo.distance(location, geography'POINT(8 54.1)') gt 1", getFromList(LOCATIONS, 0, 1, 2, 4, 5, 6, 7));
        filterAndCheck(service.observations(), "geo.distance(FeatureOfInterest/feature, geography'POINT(8 54.1)') lt 1", getFromList(OBSERVATIONS, 3));
        filterAndCheck(service.observations(), "geo.distance(FeatureOfInterest/feature, geography'POINT(8 54.1)') gt 1", getFromList(OBSERVATIONS, 0, 1, 2));
    }

    /**
     * Test the geo.intersects filter function.
     *
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    public void testGeoIntersects() throws ServiceFailureException {
        LOGGER.info("  testGeoIntersects");
        filterAndCheck(service.locations(), "geo.intersects(location, geography'LINESTRING(7.5 51, 7.5 54)')", getFromList(LOCATIONS, 4, 7));
        filterAndCheck(service.featuresOfInterest(), "geo.intersects(feature, geography'LINESTRING(7.5 51, 7.5 54)')", getFromList(FEATURESOFINTEREST, 4, 7));
        filterAndCheck(service.datastreams(),
                "geo.intersects(observedArea, geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))')",
                getFromList(DATASTREAMS, 0, 1));
    }

    /**
     * Test the geo.length filter function.
     *
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    public void testGeoLength() throws ServiceFailureException {
        LOGGER.info("  testGeoLength");
        filterAndCheck(service.locations(), "geo.length(location) gt 1", getFromList(LOCATIONS, 6, 7));
        filterAndCheck(service.locations(), "geo.length(location) ge 1", getFromList(LOCATIONS, 5, 6, 7));
        filterAndCheck(service.locations(), "geo.length(location) eq 1", getFromList(LOCATIONS, 5));
        filterAndCheck(service.locations(), "geo.length(location) ne 1", getFromList(LOCATIONS, 0, 1, 2, 3, 4, 6, 7));
        filterAndCheck(service.locations(), "geo.length(location) le 4", getFromList(LOCATIONS, 0, 1, 2, 3, 4, 5, 6, 7));
        filterAndCheck(service.locations(), "geo.length(location) lt 4", getFromList(LOCATIONS, 0, 1, 2, 3, 4, 5, 6));
        filterAndCheck(service.featuresOfInterest(), "geo.length(feature) gt 1", getFromList(FEATURESOFINTEREST, 6, 7));
        filterAndCheck(service.featuresOfInterest(), "geo.length(feature) ge 1", getFromList(FEATURESOFINTEREST, 5, 6, 7));
        filterAndCheck(service.featuresOfInterest(), "geo.length(feature) eq 1", getFromList(FEATURESOFINTEREST, 5));
        filterAndCheck(service.featuresOfInterest(), "geo.length(feature) ne 1", getFromList(FEATURESOFINTEREST, 0, 1, 2, 3, 4, 6, 7));
        filterAndCheck(service.featuresOfInterest(), "geo.length(feature) le 4", getFromList(FEATURESOFINTEREST, 0, 1, 2, 3, 4, 5, 6, 7));
        filterAndCheck(service.featuresOfInterest(), "geo.length(feature) lt 4", getFromList(FEATURESOFINTEREST, 0, 1, 2, 3, 4, 5, 6));
    }

    /**
     * Test the st_contains filter function.
     *
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    public void testStContains() throws ServiceFailureException {
        LOGGER.info("  testStContains");
        filterAndCheck(service.locations(),
                "st_contains(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', location)",
                getFromList(LOCATIONS, 1, 2));
        filterAndCheck(service.observations(),
                "st_contains(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', FeatureOfInterest/feature)",
                getFromList(OBSERVATIONS, 1, 2));
        filterAndCheck(service.datastreams(),
                "st_contains(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', observedArea)",
                getFromList(DATASTREAMS, 1));
    }

    /**
     * Test the st_crosses filter function.
     *
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    public void testStCrosses() throws ServiceFailureException {
        LOGGER.info("  testStCrosses");
        filterAndCheck(service.locations(), "st_crosses(geography'LINESTRING(7.5 51.5, 7.5 53.5)', location)", getFromList(LOCATIONS, 4, 7));
        filterAndCheck(service.featuresOfInterest(), "st_crosses(geography'LINESTRING(7.5 51.5, 7.5 53.5)', feature)", getFromList(FEATURESOFINTEREST, 4, 7));
    }

    /**
     * Test the st_disjoint filter function.
     *
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    public void testStDisjoint() throws ServiceFailureException {
        LOGGER.info("  testStDisjoint");
        filterAndCheck(service.locations(),
                "st_disjoint(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', location)",
                getFromList(LOCATIONS, 0, 3, 5, 6));
        filterAndCheck(service.featuresOfInterest(),
                "st_disjoint(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', feature)",
                getFromList(FEATURESOFINTEREST, 0, 3, 5, 6));
    }

    /**
     * Test the st_equals filter function.
     *
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    public void testStEquals() throws ServiceFailureException {
        LOGGER.info("  testStEquals");
        filterAndCheck(service.locations(), "st_equals(location, geography'POINT(8 53)')", getFromList(LOCATIONS, 2));
        filterAndCheck(service.featuresOfInterest(), "st_equals(feature, geography'POINT(8 53)')", getFromList(FEATURESOFINTEREST, 2));
    }

    /**
     * Test the st_intersects filter function.
     *
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    public void testStIntersects() throws ServiceFailureException {
        LOGGER.info("  testStIntersects");
        filterAndCheck(service.locations(), "st_intersects(location, geography'LINESTRING(7.5 51, 7.5 54)')", getFromList(LOCATIONS, 4, 7));
        filterAndCheck(service.featuresOfInterest(), "st_intersects(feature, geography'LINESTRING(7.5 51, 7.5 54)')", getFromList(FEATURESOFINTEREST, 4, 7));
        filterAndCheck(service.datastreams(),
                "st_intersects(observedArea, geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))')",
                getFromList(DATASTREAMS, 0, 1));
    }

    /**
     * Test the st_overlaps filter function.
     *
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    public void testStOverlaps() throws ServiceFailureException {
        LOGGER.info("  testStOverlaps");
        filterAndCheck(service.locations(),
                "st_overlaps(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', location)",
                getFromList(LOCATIONS, 4));
        filterAndCheck(service.featuresOfInterest(),
                "st_overlaps(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', feature)",
                getFromList(FEATURESOFINTEREST, 4));
    }

    /**
     * Test the st_relate filter function.
     *
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    public void testStRelate() throws ServiceFailureException {
        LOGGER.info("  testStRelate");
        filterAndCheck(service.locations(),
                "st_relate(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', location, 'T********')",
                getFromList(LOCATIONS, 1, 2, 4, 7));
        filterAndCheck(service.featuresOfInterest(),
                "st_relate(geography'POLYGON((7.5 51.5, 7.5 53.5, 8.5 53.5, 8.5 51.5, 7.5 51.5))', feature, 'T********')",
                getFromList(FEATURESOFINTEREST, 1, 2, 4, 7));
    }

    /**
     * Test the st_touches filter function.
     *
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    public void testStTouches() throws ServiceFailureException {
        LOGGER.info("  testStTouches");
        filterAndCheck(service.locations(), "st_touches(geography'POLYGON((8 53, 7.5 54.5, 8.5 54.5, 8 53))', location)", getFromList(LOCATIONS, 2, 4));
        filterAndCheck(service.featuresOfInterest(), "st_touches(geography'POLYGON((8 53, 7.5 54.5, 8.5 54.5, 8 53))', feature)", getFromList(FEATURESOFINTEREST, 2, 4));
    }

    /**
     * Test the st_within filter function.
     *
     * @throws ServiceFailureException If the service doesn't respond.
     */
    @Test
    public void testStWithin() throws ServiceFailureException {
        LOGGER.info("  testStWithin");
        filterAndCheck(service.locations(), "st_within(geography'POINT(7.5 52.75)', location)", getFromList(LOCATIONS, 4));
        filterAndCheck(service.featuresOfInterest(), "st_within(geography'POINT(7.5 52.75)', feature)", getFromList(FEATURESOFINTEREST, 4));
    }

    public static <T extends Entity<T>> List<T> getFromList(List<T> list, int... ids) {
        List<T> result = new ArrayList<>();
        for (int i : ids) {
            result.add(list.get(i));
        }
        return result;
    }
}
