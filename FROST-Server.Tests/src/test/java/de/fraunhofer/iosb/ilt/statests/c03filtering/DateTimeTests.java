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
package de.fraunhofer.iosb.ilt.statests.c03filtering;

import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing.EP_PHENOMENONTIME;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing.EP_RESULTTIME;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing.EP_VALIDTIME;
import static de.fraunhofer.iosb.ilt.statests.util.EntityUtils.filterForException;
import static de.fraunhofer.iosb.ilt.statests.util.EntityUtils.testFilterResults;
import static de.fraunhofer.iosb.ilt.statests.util.Utils.getFromList;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.fraunhofer.iosb.ilt.frostclient.dao.Dao;
import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.TimeInstant;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.TimeInterval;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.TimeValue;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.geojson.Point;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests date and time functions.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public abstract class DateTimeTests extends AbstractTestClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(DateTimeTests.class);

    private static final List<Entity> THINGS = new ArrayList<>();
    private static final List<Entity> OBSERVATIONS = new ArrayList<>();
    private static final List<Entity> DATASTREAMS = new ArrayList<>();
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
    private static final ZonedDateTime T2017A = ZonedDateTime.parse("2017-01-01T09:00:00.000Z");
    private static final ZonedDateTime T2017B = T2017A.plus(1, ChronoUnit.HOURS);
    private static final ZonedDateTime T2018 = ZonedDateTime.parse("2018-01-01T09:00:00.000Z");
    private static final TimeInterval I2015 = TimeInterval.create(T2015.toInstant(), T2015.plus(1, ChronoUnit.HOURS).toInstant());
    private static final TimeInterval I600_659 = TimeInterval.create(T600.toInstant(), T659.toInstant());
    private static final TimeInterval I600_700 = TimeInterval.create(T600.toInstant(), T700.toInstant());
    private static final TimeInterval I600_701 = TimeInterval.create(T600.toInstant(), T701.toInstant());
    private static final TimeInterval I700_800 = TimeInterval.create(T700.toInstant(), T800.toInstant());
    private static final TimeInterval I701_759 = TimeInterval.create(T701.toInstant(), T759.toInstant());
    private static final TimeInterval I759_900 = TimeInterval.create(T759.toInstant(), T900.toInstant());
    private static final TimeInterval I800_900 = TimeInterval.create(T800.toInstant(), T900.toInstant());
    private static final TimeInterval I801_900 = TimeInterval.create(T801.toInstant(), T900.toInstant());
    private static final TimeInterval I659_801 = TimeInterval.create(T659.toInstant(), T801.toInstant());
    private static final TimeInterval I700_759 = TimeInterval.create(T700.toInstant(), T759.toInstant());
    private static final TimeInterval I700_801 = TimeInterval.create(T700.toInstant(), T801.toInstant());
    private static final TimeInterval I659_800 = TimeInterval.create(T659.toInstant(), T800.toInstant());
    private static final TimeInterval I701_800 = TimeInterval.create(T701.toInstant(), T800.toInstant());
    private static final TimeInterval I2017 = TimeInterval.create(T2017A.toInstant(), T2017B.toInstant());
    private static final TimeInterval I2014_2015 = TimeInterval.create(T2014.toInstant(), T2015.toInstant());
    private static final TimeInterval I2014_2017B = TimeInterval.create(T2014.toInstant(), T2017B.toInstant());
    private static final TimeInterval I2014_2018 = TimeInterval.create(T2014.toInstant(), T2018.toInstant());
    private static final TimeInterval I2015_2017B = TimeInterval.create(T2015.toInstant(), T2017B.toInstant());
    private static final TimeInterval I2015_2018 = TimeInterval.create(T2015.toInstant(), T2018.toInstant());
    private static final TimeInterval I2017B_2018 = TimeInterval.create(T2017B.toInstant(), T2018.toInstant());

    private static final ZonedDateTime T2020_00 = ZonedDateTime.parse("2020-01-01T00:00:00.000Z");
    private static final ZonedDateTime T2020_01 = ZonedDateTime.parse("2020-01-01T01:00:00.000Z");
    private static final ZonedDateTime T2020_02 = ZonedDateTime.parse("2020-01-01T02:00:00.000Z");
    private static final ZonedDateTime T2020_03 = ZonedDateTime.parse("2020-01-01T03:00:00.000Z");
    private static final ZonedDateTime T2020_04 = ZonedDateTime.parse("2020-01-01T04:00:00.000Z");
    private static final ZonedDateTime T2020_05 = ZonedDateTime.parse("2020-01-01T05:00:00.000Z");
    private static final ZonedDateTime T2020_06 = ZonedDateTime.parse("2020-01-01T06:00:00.000Z");
    private static final ZonedDateTime T2020_07 = ZonedDateTime.parse("2020-01-01T07:00:00.000Z");
    private static final ZonedDateTime T2020_08 = ZonedDateTime.parse("2020-01-01T08:00:00.000Z");
    private static final ZonedDateTime T2020_09 = ZonedDateTime.parse("2020-01-01T09:00:00.000Z");
    private static final ZonedDateTime T2020_10 = ZonedDateTime.parse("2020-01-01T10:00:00.000Z");
    private static final ZonedDateTime T2020_11 = ZonedDateTime.parse("2020-01-01T11:00:00.000Z");
    private static final ZonedDateTime T2020_12 = ZonedDateTime.parse("2020-01-01T12:00:00.000Z");
    private static final ZonedDateTime T2020_13 = ZonedDateTime.parse("2020-01-01T13:00:00.000Z");
    private static final ZonedDateTime T2020_14 = ZonedDateTime.parse("2020-01-01T14:00:00.000Z");
    private static final ZonedDateTime T2020_15 = ZonedDateTime.parse("2020-01-01T15:00:00.000Z");
    private static final ZonedDateTime T2020_16 = ZonedDateTime.parse("2020-01-01T16:00:00.000Z");
    private static final ZonedDateTime T2020_17 = ZonedDateTime.parse("2020-01-01T17:00:00.000Z");
    private static final ZonedDateTime T2020_18 = ZonedDateTime.parse("2020-01-01T18:00:00.000Z");
    private static final ZonedDateTime T2020_19 = ZonedDateTime.parse("2020-01-01T19:00:00.000Z");
    private static final ZonedDateTime T2020_20 = ZonedDateTime.parse("2020-01-01T20:00:00.000Z");
    private static final ZonedDateTime T2020_21 = ZonedDateTime.parse("2020-01-01T21:00:00.000Z");
    private static final ZonedDateTime T2020_22 = ZonedDateTime.parse("2020-01-01T22:00:00.000Z");
    private static final ZonedDateTime T2020_23 = ZonedDateTime.parse("2020-01-01T23:00:00.000Z");
    private static final ZonedDateTime T2020_24 = ZonedDateTime.parse("2020-01-02T00:00:00.000Z");
    private static SensorThingsV11Sensing sMdl;

    public DateTimeTests(ServerVersion version) {
        super(version);
    }

    @Override
    protected void setUpVersion() throws ServiceFailureException {
        LOGGER.info("Setting up for version {}.", version.urlPart);
        sMdl = sSrvc.getModel(SensorThingsV11Sensing.class);
        createEntities();
    }

    @AfterAll
    public static void tearDown() throws ServiceFailureException {
        LOGGER.info("Tearing down.");
        cleanup();
    }

    private static void cleanup() throws ServiceFailureException {
        EntityUtils.deleteAll(sSrvc);
        THINGS.clear();
        DATASTREAMS.clear();
        OBSERVATIONS.clear();
    }

    private static void createEntities() throws ServiceFailureException {
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

        int r = 0;
        createObservation(r++, datastream, T600, T600, null); // 0
        createObservation(r++, datastream, T659, T659, null); // 1
        createObservation(r++, datastream, T700, T700, null); // 2
        createObservation(r++, datastream, T701, T701, null); // 3
        createObservation(r++, datastream, T759, T759, null); // 4
        createObservation(r++, datastream, T800, T800, null); // 5
        createObservation(r++, datastream, T801, T801, null); // 6
        createObservation(r++, datastream, T900, T900, null); // 7

        createObservation(r++, datastream, I600_659, null, I600_659); // 8
        createObservation(r++, datastream, I600_700, null, I600_700); // 9
        createObservation(r++, datastream, I600_701, null, I600_701); // 10
        createObservation(r++, datastream, I700_800, null, I700_800); // 11
        createObservation(r++, datastream, I701_759, null, I701_759); // 12
        createObservation(r++, datastream, I759_900, null, I759_900); // 13
        createObservation(r++, datastream, I800_900, null, I800_900); // 14
        createObservation(r++, datastream, I801_900, null, I801_900); // 15

        createObservation(r++, datastream, I659_801, null, I659_801); // 16
        createObservation(r++, datastream, I700_759, null, I700_759); // 17
        createObservation(r++, datastream, I700_801, null, I700_801); // 18
        createObservation(r++, datastream, I659_800, null, I659_800); // 19
        createObservation(r++, datastream, I701_800, null, I701_800); // 20

        createObservation(r++, datastream, T2015, T2015, null); // 21
        createObservation(r++, datastream, T2017A, T2017A, null); // 22
        createObservation(r++, datastream, I2015, T2015, I2015); // 23
        createObservation(r++, datastream, I2017, T2017A.plus(1, ChronoUnit.HOURS), I2017); // 24
        Assertions.assertEquals(25, r);

        // A second Datastream, with no observations.
        Entity datastream2 = sMdl.newDatastream("Datastream 2", "The second temperature of thing 1, sensor 1.", "someType", new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
        datastream2.setProperty(sMdl.npDatastreamThing, thing);
        datastream2.setProperty(sMdl.npDatastreamSensor, sensor);
        datastream2.setProperty(sMdl.npDatastreamObservedproperty, obsProp);
        sSrvc.create(datastream2);
        DATASTREAMS.add(datastream2);

        // A third Datastream, with a days worth of observations.
        Entity datastream3 = sMdl.newDatastream("Datastream 3", "The third temperature of thing 1, sensor 1.", "someType", new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
        datastream3.setProperty(sMdl.npDatastreamThing, thing);
        datastream3.setProperty(sMdl.npDatastreamSensor, sensor);
        datastream3.setProperty(sMdl.npDatastreamObservedproperty, obsProp);
        sSrvc.create(datastream3);
        DATASTREAMS.add(datastream3);

        createObservation(r++, datastream3, T2020_00, T2020_00, null); // 25
        createObservation(r++, datastream3, T2020_01, T2020_01, null); // 26
        createObservation(r++, datastream3, T2020_02, T2020_02, null); // 27
        createObservation(r++, datastream3, T2020_03, T2020_03, null); // 28
        createObservation(r++, datastream3, T2020_04, T2020_04, null); // 29
        createObservation(r++, datastream3, T2020_05, T2020_05, null); // 30
        createObservation(r++, datastream3, T2020_06, T2020_06, null); // 31
        createObservation(r++, datastream3, T2020_07, T2020_07, null); // 32
        createObservation(r++, datastream3, T2020_08, T2020_08, null); // 33
        createObservation(r++, datastream3, T2020_09, T2020_09, null); // 34
        createObservation(r++, datastream3, T2020_10, T2020_10, null); // 35
        createObservation(r++, datastream3, T2020_11, T2020_11, null); // 36
        createObservation(r++, datastream3, T2020_12, T2020_12, null); // 37
        createObservation(r++, datastream3, T2020_13, T2020_13, null); // 38
        createObservation(r++, datastream3, T2020_14, T2020_14, null); // 39
        createObservation(r++, datastream3, T2020_15, T2020_15, null); // 40
        createObservation(r++, datastream3, T2020_16, T2020_16, null); // 41
        createObservation(r++, datastream3, T2020_17, T2020_17, null); // 42
        createObservation(r++, datastream3, T2020_18, T2020_18, null); // 43
        createObservation(r++, datastream3, T2020_19, T2020_19, null); // 44
        createObservation(r++, datastream3, T2020_20, T2020_20, null); // 45
        createObservation(r++, datastream3, T2020_21, T2020_21, null); // 46
        createObservation(r++, datastream3, T2020_22, T2020_22, null); // 47
        createObservation(r++, datastream3, T2020_23, T2020_23, null); // 48
        createObservation(r++, datastream3, T2020_24, T2020_24, null); // 49
        LOGGER.debug("Created {} Observations", r);
    }

    private static void createObservation(double result, Entity ds, TimeInterval pt, ZonedDateTime rt, TimeInterval vt) throws ServiceFailureException {
        createObservation(result, ds, TimeValue.create(pt), TimeInstant.create(rt), vt);
    }

    private static void createObservation(double result, Entity ds, ZonedDateTime pt, ZonedDateTime rt, TimeInterval vt) throws ServiceFailureException {
        createObservation(result, ds, TimeValue.create(pt), TimeInstant.create(rt), vt);
    }

    private static void createObservation(double result, Entity ds, TimeValue pt, TimeInstant rt, TimeInterval vt) throws ServiceFailureException {
        Entity o = sMdl.newObservation(result, pt, ds)
                .setProperty(EP_RESULTTIME, rt)
                .setProperty(EP_VALIDTIME, vt);
        sSrvc.create(o);
        OBSERVATIONS.add(o);
    }

    public void testFilterResultsDs(Dao doa, String filter, List<Entity> expected) {
        if (expected == null) {
            return;
        }
        testFilterResults(doa, filter.replace("{}", "phenomenonTime"), expected);
        testFilterResults(doa, filter.replace("{}", "resultTime"), expected);
    }

    public void testDsTpl(String tpl,
            List<Entity> t2014,
            List<Entity> t2015,
            List<Entity> t700,
            List<Entity> t2017b,
            List<Entity> t2018,
            List<Entity> i78,
            List<Entity> i2014x2015,
            List<Entity> i2014x2017b,
            List<Entity> i2014x2018,
            List<Entity> i2015x2017b,
            List<Entity> i2015x2018,
            List<Entity> i2017bx2018) {
        Dao dsDoa = sSrvc.dao(sMdl.etDatastream);
        testFilterResultsDs(dsDoa, String.format(tpl, T2014), t2014);
        testFilterResultsDs(dsDoa, String.format(tpl, T2015), t2015);
        testFilterResultsDs(dsDoa, String.format(tpl, T700), t700);
        testFilterResultsDs(dsDoa, String.format(tpl, T2017B), t2017b);
        testFilterResultsDs(dsDoa, String.format(tpl, T2018), t2018);
        testFilterResultsDs(dsDoa, String.format(tpl, I700_800), i78);
        testFilterResultsDs(dsDoa, String.format(tpl, I2014_2015), i2014x2015);
        testFilterResultsDs(dsDoa, String.format(tpl, I2014_2017B), i2014x2017b);
        testFilterResultsDs(dsDoa, String.format(tpl, I2014_2018), i2014x2018);
        testFilterResultsDs(dsDoa, String.format(tpl, I2015_2017B), i2015x2017b);
        testFilterResultsDs(dsDoa, String.format(tpl, I2015_2018), i2015x2018);
        testFilterResultsDs(dsDoa, String.format(tpl, I2017B_2018), i2017bx2018);
    }

    public void testTimeValue(String tpl,
            List<Entity> rtOpT7,
            List<Entity> vtOpT7,
            List<Entity> ptOpT7,
            List<Entity> rtOpT78,
            List<Entity> vtOpT78,
            List<Entity> ptOpT78) {
        Dao doa = DATASTREAMS.get(0).dao(sMdl.npDatastreamObservations);
        testFilterResults(doa, String.format(tpl, "resultTime", T700), rtOpT7);
        testFilterResults(doa, String.format(tpl, "validTime", T700), vtOpT7);
        testFilterResults(doa, String.format(tpl, "phenomenonTime", T700), ptOpT7);

        testFilterResults(doa, String.format(tpl, "resultTime", I700_800), rtOpT78);
        testFilterResults(doa, String.format(tpl, "validTime", I700_800), vtOpT78);
        testFilterResults(doa, String.format(tpl, "phenomenonTime", I700_800), ptOpT78);
    }

    public void testValueTime(String tpl,
            List<Entity> rtOpT7,
            List<Entity> vtOpT7,
            List<Entity> ptOpT7,
            List<Entity> rtOpT78,
            List<Entity> vtOpT78,
            List<Entity> ptOpT78) {
        Dao doa = DATASTREAMS.get(0).dao(sMdl.npDatastreamObservations);
        testFilterResults(doa, String.format(tpl, T700, "resultTime"), rtOpT7);
        testFilterResults(doa, String.format(tpl, T700, "validTime"), vtOpT7);
        testFilterResults(doa, String.format(tpl, T700, "phenomenonTime"), ptOpT7);

        testFilterResults(doa, String.format(tpl, I700_800, "resultTime"), rtOpT78);
        testFilterResults(doa, String.format(tpl, I700_800, "validTime"), vtOpT78);
        testFilterResults(doa, String.format(tpl, I700_800, "phenomenonTime"), ptOpT78);
    }

    public void testTimeOpValue(String op,
            List<Entity> rtOpT7,
            List<Entity> vtOpT7,
            List<Entity> ptOpT7,
            List<Entity> rtOpT78,
            List<Entity> vtOpT78,
            List<Entity> ptOpT78) {
        testTimeValue("%s " + op + " %s", rtOpT7, vtOpT7, ptOpT7, rtOpT78, vtOpT78, ptOpT78);
    }

    public void testValueOpTime(String op,
            List<Entity> rtOpT7,
            List<Entity> vtOpT7,
            List<Entity> ptOpT7,
            List<Entity> rtOpT78,
            List<Entity> vtOpT78,
            List<Entity> ptOpT78) {
        testValueTime("%s " + op + " %s", rtOpT7, vtOpT7, ptOpT7, rtOpT78, vtOpT78, ptOpT78);
    }

    @Test
    void test01Lt() {
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
    void test02Gt() {
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
                getFromList(DATASTREAMS, 0, 2),
                getFromList(DATASTREAMS, 2),
                getFromList(DATASTREAMS, 2),
                getFromList(DATASTREAMS, 2),
                getFromList(DATASTREAMS, 2),
                getFromList(DATASTREAMS, 2),
                getFromList(DATASTREAMS, 0, 2),
                getFromList(DATASTREAMS, 2),
                getFromList(DATASTREAMS, 2),
                getFromList(DATASTREAMS, 2),
                getFromList(DATASTREAMS, 2),
                getFromList(DATASTREAMS, 2));
    }

    @Test
    void test03Le() {
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
    void test04Ge() {
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
                getFromList(DATASTREAMS, 0, 2),
                getFromList(DATASTREAMS, 0, 2),
                getFromList(DATASTREAMS, 2),
                getFromList(DATASTREAMS, 2),
                getFromList(DATASTREAMS, 2),
                getFromList(DATASTREAMS, 2),
                getFromList(DATASTREAMS, 0, 2),
                getFromList(DATASTREAMS, 0, 2),
                getFromList(DATASTREAMS, 2),
                getFromList(DATASTREAMS, 0, 2),
                getFromList(DATASTREAMS, 2),
                getFromList(DATASTREAMS, 2));
    }

    @Test
    void test05Eq() {
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
    void test06Before() {
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
    void test07After() {
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
                getFromList(DATASTREAMS, 0, 2),
                getFromList(DATASTREAMS, 2),
                getFromList(DATASTREAMS, 2),
                getFromList(DATASTREAMS, 2),
                getFromList(DATASTREAMS, 2),
                getFromList(DATASTREAMS, 2),
                getFromList(DATASTREAMS, 0, 2),
                getFromList(DATASTREAMS, 2),
                getFromList(DATASTREAMS, 2),
                getFromList(DATASTREAMS, 2),
                getFromList(DATASTREAMS, 2),
                getFromList(DATASTREAMS, 2));
    }

    @Test
    void test08Meets() {
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
    void test09During() {
        LOGGER.info("  test09During");
        Dao doa = sSrvc.dao(sMdl.etObservation);
        filterForException(doa, String.format("during(resultTime,%s)", T700), 400);
        filterForException(doa, String.format("during(validTime,%s)", T700), 400);
        filterForException(doa, String.format("during(phenomenonTime,%s)", T700), 400);

        testFilterResults(doa, String.format("during(resultTime,%s)", I700_800), getFromList(OBSERVATIONS, 2, 3, 4));
        testFilterResults(doa, String.format("during(validTime,%s)", I700_800), getFromList(OBSERVATIONS, 11, 12, 17, 20));
        testFilterResults(doa, String.format("during(phenomenonTime,%s)", I700_800), getFromList(OBSERVATIONS, 2, 3, 4, 11, 12, 17, 20));

        filterForException(doa, String.format("during(%s,resultTime)", T700), 400);
        testFilterResults(doa, String.format("during(%s,validTime)", T700), getFromList(OBSERVATIONS, 10, 11, 16, 17, 18, 19));
        testFilterResults(doa, String.format("during(%s,phenomenonTime)", T700), getFromList(OBSERVATIONS, 10, 11, 16, 17, 18, 19));

        filterForException(doa, String.format("during(%s,resultTime)", I700_800), 400);
        testFilterResults(doa, String.format("during(%s,validTime)", I700_800), getFromList(OBSERVATIONS, 11, 16, 18, 19));
        testFilterResults(doa, String.format("during(%s,phenomenonTime)", I700_800), getFromList(OBSERVATIONS, 11, 16, 18, 19));

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
    void test10Overlaps() {
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
    void test11Starts() {
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
    void test12Finishes() {
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
    void test13Year() {
        LOGGER.info("  test13Year");
        Dao doa = DATASTREAMS.get(0).dao(sMdl.npDatastreamObservations);
        testFilterResults(doa, "year(resultTime) eq 2015", getFromList(OBSERVATIONS, 21, 23));
        testFilterResults(doa, "year(validTime) eq 2015", getFromList(OBSERVATIONS, 23));
        testFilterResults(doa, "year(phenomenonTime) eq 2015", getFromList(OBSERVATIONS, 21, 23));
    }

    @Test
    void test14Durations() {
        LOGGER.info("  test14Durations");
        Dao doa = DATASTREAMS.get(0).dao(sMdl.npDatastreamObservations);
        // Durations
        testFilterResults(doa, String.format("resultTime add duration'PT1H' gt %s", T900), getFromList(OBSERVATIONS, 6, 7, 22, 24));
        testFilterResults(doa, String.format("validTime add duration'PT1H' gt %s", T900), getFromList(OBSERVATIONS, 15, 24));
        testFilterResults(doa, String.format("phenomenonTime add duration'PT1H' gt %s", T900), getFromList(OBSERVATIONS, 6, 7, 15, 22, 24));

        testFilterResults(doa, String.format("resultTime gt %s sub duration'PT1H'", T900), getFromList(OBSERVATIONS, 6, 7, 22, 24));
        testFilterResults(doa, String.format("validTime gt %s sub duration'PT1H'", T900), getFromList(OBSERVATIONS, 15, 24));
        testFilterResults(doa, String.format("phenomenonTime gt %s sub duration'PT1H'", T900), getFromList(OBSERVATIONS, 6, 7, 15, 22, 24));

        testFilterResults(doa, String.format("resultTime sub duration'PT1H' gt %s", T700), getFromList(OBSERVATIONS, 6, 7, 22, 24));
        testFilterResults(doa, String.format("validTime sub duration'PT1H' gt %s", T700), getFromList(OBSERVATIONS, 15, 24));
        testFilterResults(doa, String.format("phenomenonTime sub duration'PT1H' gt %s", T700), getFromList(OBSERVATIONS, 6, 7, 15, 22, 24));

        testFilterResults(doa, String.format("resultTime lt %s add duration'PT1H'", I600_700), getFromList(OBSERVATIONS, 0, 1, 21, 23));
        testFilterResults(doa, String.format("validTime lt %s add duration'PT1H'", I600_700), getFromList(OBSERVATIONS, 8, 9, 23));
        testFilterResults(doa, String.format("phenomenonTime lt %s add duration'PT1H'", I600_700), getFromList(OBSERVATIONS, 0, 1, 8, 9, 21, 23));

        testFilterResults(doa, String.format("resultTime gt %s sub duration'PT1H'", I800_900), getFromList(OBSERVATIONS, 5, 6, 7, 22, 24));
        testFilterResults(doa, String.format("validTime gt %s sub duration'PT1H'", I800_900), getFromList(OBSERVATIONS, 14, 15, 24));
        testFilterResults(doa, String.format("phenomenonTime gt %s sub duration'PT1H'", I800_900), getFromList(OBSERVATIONS, 5, 6, 7, 14, 15, 22, 24));

        testFilterResults(doa, String.format("phenomenonTime sub %s eq duration'PT1H'", T800), getFromList(OBSERVATIONS, 7));
    }

    @Test
    void test15AlternativeOverlaps() {
        LOGGER.info("  test15AlternativeOverlaps");
        Dao doa = sSrvc.dao(sMdl.etObservation);
        testFilterResults(doa, String.format("not resultTime lt %s and not resultTime ge %s", T700, T800), getFromList(OBSERVATIONS, 2, 3, 4));
        testFilterResults(doa, String.format("not validTime lt %s and not validTime ge %s", T700, T800), getFromList(OBSERVATIONS, 10, 11, 12, 13, 16, 17, 18, 19, 20));
        testFilterResults(doa, String.format("not phenomenonTime lt %s and not phenomenonTime ge %s", T700, T800), getFromList(OBSERVATIONS, 2, 3, 4, 10, 11, 12, 13, 16, 17, 18, 19, 20));
    }

    @Test
    void test20DateWithTimezone() {
        LOGGER.info("  test20DateWithTimezone");
        Dao doa = DATASTREAMS.get(2).dao(sMdl.npDatastreamObservations);
        testFilterResults(doa, "date(resultTime, 'Europe/berlin') eq 2020-01-01", getFromList(OBSERVATIONS, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47));
        testFilterResults(doa, "date(phenomenonTime, 'Europe/Berlin') eq 2020-01-01", getFromList(OBSERVATIONS, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47));

        testFilterResults(doa, "date(resultTime, 'UTC') eq 2020-01-01", getFromList(OBSERVATIONS, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48));
        testFilterResults(doa, "date(phenomenonTime, 'UTC') eq 2020-01-01", getFromList(OBSERVATIONS, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48));

        testFilterResults(doa, "date(resultTime, 'PST') eq 2020-01-01", getFromList(OBSERVATIONS, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49));
        testFilterResults(doa, "date(phenomenonTime, 'PST') eq 2020-01-01", getFromList(OBSERVATIONS, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49));

        testFilterResults(doa, "date(resultTime, '-08:00') eq 2020-01-01", getFromList(OBSERVATIONS, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49));
        testFilterResults(doa, "date(resultTime, '-08') eq 2020-01-01", getFromList(OBSERVATIONS, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49));
        testFilterResults(doa, "date(phenomenonTime, '-8') eq 2020-01-01", getFromList(OBSERVATIONS, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49));

        testFilterResults(doa, "date(resultTime, '-11:00:00') eq 2020-01-01", getFromList(OBSERVATIONS, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49));
        testFilterResults(doa, "date(resultTime, '-11:00') eq 2020-01-01", getFromList(OBSERVATIONS, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49));
        testFilterResults(doa, "date(resultTime, '-11') eq 2020-01-01", getFromList(OBSERVATIONS, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49));

        testFilterResults(doa, "date(resultTime, '+11:00:00') eq 2020-01-01", getFromList(OBSERVATIONS, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37));
        testFilterResults(doa, "date(resultTime, '11:00:00') eq 2020-01-01", getFromList(OBSERVATIONS, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37));
        testFilterResults(doa, "date(resultTime, '+11:00') eq 2020-01-01", getFromList(OBSERVATIONS, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37));
        testFilterResults(doa, "date(resultTime, '11:00') eq 2020-01-01", getFromList(OBSERVATIONS, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37));
        testFilterResults(doa, "date(resultTime, '+11') eq 2020-01-01", getFromList(OBSERVATIONS, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37));
        testFilterResults(doa, "date(resultTime, '11') eq 2020-01-01", getFromList(OBSERVATIONS, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37));
    }

    @Test
    void test99PhenomenonTimeAfterDelete() throws ServiceFailureException {
        LOGGER.info("  test99PhenomenonTimeAfterDelete");
        EntityUtils.deleteAll(sSrvc.dao(sMdl.etObservation));
        Entity ds1 = sSrvc.dao(sMdl.etDatastream).find(DATASTREAMS.get(0).getPrimaryKeyValues());
        assertNull(ds1.getProperty(EP_PHENOMENONTIME), "phenomenonTime should be null");
        Entity ds2 = sSrvc.dao(sMdl.etDatastream).find(DATASTREAMS.get(1).getPrimaryKeyValues());
        assertNull(ds2.getProperty(EP_PHENOMENONTIME), "phenomenonTime should be null");
    }
}
