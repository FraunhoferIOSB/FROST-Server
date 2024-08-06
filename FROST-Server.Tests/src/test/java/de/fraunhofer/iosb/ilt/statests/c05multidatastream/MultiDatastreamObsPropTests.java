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
package de.fraunhofer.iosb.ilt.statests.c05multidatastream;

import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11MultiDatastream.EP_MULTIOBSERVATIONDATATYPES;
import static de.fraunhofer.iosb.ilt.frostclient.utils.ParserUtils.formatKeyValuesForUrl;
import static de.fraunhofer.iosb.ilt.statests.util.Utils.getFromList;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.model.EntitySet;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerSettings;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods.HttpResponse;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.geojson.Point;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some odd tests.
 *
 * @author Hylke van der Schaaf
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public abstract class MultiDatastreamObsPropTests extends AbstractTestClass {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiDatastreamObsPropTests.class);

    private static final List<Entity> THINGS = new ArrayList<>();
    private static final List<Entity> LOCATIONS = new ArrayList<>();
    private static final List<Entity> SENSORS = new ArrayList<>();
    private static final List<Entity> OBSERVED_PROPS = new ArrayList<>();
    private static final List<Entity> DATASTREAMS = new ArrayList<>();
    private static final List<Entity> MULTIDATASTREAMS = new ArrayList<>();
    private static final List<Entity> OBSERVATIONS = new ArrayList<>();

    public MultiDatastreamObsPropTests(ServerVersion version) {
        super(version);
    }

    @BeforeEach
    public void before() {
        assumeTrue(
                serverSettings.implementsRequirement(version, ServerSettings.MULTIDATA_REQ),
                "Conformance level 5 not checked since MultiDatastreams not listed in Service Root.");
    }

    @Override
    protected void setUpVersion() throws ServiceFailureException, URISyntaxException {
        LOGGER.info("Setting up for version {}.", version.urlPart);
        assumeTrue(
                serverSettings.implementsRequirement(version, ServerSettings.MULTIDATA_REQ),
                "Conformance level 5 not checked since MultiDatastreams not listed in Service Root.");
        createEntities();
    }

    @Override
    protected void tearDownVersion() throws ServiceFailureException {
        cleanup();
    }

    @AfterAll
    public static void tearDown() throws ServiceFailureException {
        LOGGER.info("Tearing down.");
        cleanup();
    }

    private static void cleanup() throws ServiceFailureException {
        EntityUtils.deleteAll(service);
        THINGS.clear();
        LOCATIONS.clear();
        SENSORS.clear();
        OBSERVED_PROPS.clear();
        DATASTREAMS.clear();
        MULTIDATASTREAMS.clear();
        OBSERVATIONS.clear();
    }

    /**
     * Creates some basic non-MultiDatastream entities.
     *
     * @throws ServiceFailureException
     * @throws URISyntaxException
     */
    private static void createEntities() throws ServiceFailureException, URISyntaxException {
        Entity location = sMdl.newLocation("Location 1.0", "Location of Thing 1.", "application/vnd.geo+json", new Point(8, 51));
        sSrvc.create(location);
        LOCATIONS.add(location);

        Entity thing = sMdl.newThing("Thing 1", "The first thing.");
        thing.getProperty(sMdl.npThingLocations).add(location.withOnlyPk());
        sSrvc.create(thing);
        THINGS.add(thing);

        thing = sMdl.newThing("Thing 2", "The second thing.");
        thing.getProperty(sMdl.npThingLocations).add(location.withOnlyPk());
        sSrvc.create(thing);
        THINGS.add(thing);

        Entity sensor = sMdl.newSensor("Sensor 1", "The first sensor.", "text", "Some metadata.");
        sSrvc.create(sensor);
        SENSORS.add(sensor);

        sensor = sMdl.newSensor("Sensor 2", "The second sensor.", "text", "Some metadata.");
        sSrvc.create(sensor);
        SENSORS.add(sensor);

        Entity obsProp = sMdl.newObservedProperty("ObservedProperty 1", "http://ucom.org/temperature", "The temperature of the thing.");
        sSrvc.create(obsProp);
        OBSERVED_PROPS.add(obsProp);

        obsProp = sMdl.newObservedProperty("ObservedProperty 2", "http://ucom.org/humidity", "The humidity of the thing.");
        sSrvc.create(obsProp);
        OBSERVED_PROPS.add(obsProp);

        obsProp = sMdl.newObservedProperty("ObservedProperty 3", "http://ucom.org/height", "The height of the thing.");
        sSrvc.create(obsProp);
        OBSERVED_PROPS.add(obsProp);

        obsProp = sMdl.newObservedProperty("ObservedProperty 4", "http://ucom.org/depth", "The depth of the thing.");
        sSrvc.create(obsProp);
        OBSERVED_PROPS.add(obsProp);

        Entity datastream = sMdl.newDatastream("Datastream 1", "The temperature of thing 1, sensor 1.", "someType", new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
        DATASTREAMS.add(datastream);
        datastream.setProperty(sMdl.npDatastreamThing, THINGS.get(0).withOnlyPk());
        datastream.setProperty(sMdl.npDatastreamSensor, SENSORS.get(0).withOnlyPk());
        datastream.setProperty(sMdl.npDatastreamObservedproperty, OBSERVED_PROPS.get(0).withOnlyPk());
        sSrvc.create(datastream);

        datastream = sMdl.newDatastream("Datastream 2", "The temperature of thing 2, sensor 2.", "someType", new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
        DATASTREAMS.add(datastream);
        datastream.setProperty(sMdl.npDatastreamThing, THINGS.get(1).withOnlyPk());
        datastream.setProperty(sMdl.npDatastreamSensor, SENSORS.get(1).withOnlyPk());
        datastream.setProperty(sMdl.npDatastreamObservedproperty, OBSERVED_PROPS.get(0).withOnlyPk());
        sSrvc.create(datastream);

    }

    private void checkResult(String test, EntityUtils.ResultTestResult result) {
        assertTrue(result.testOk, test + " " + result.message);
    }

    private void checkObservedPropertiesFor(Entity md, Entity... expectedObservedProps) throws ServiceFailureException {
        Entity[] fetchedObservedProps2 = md.query(mMdl.npMultidatastreamObservedproperties).list().toList().toArray(Entity[]::new);
        String message = "Incorrect Observed Properties returned.";
        assertArrayEquals(expectedObservedProps, fetchedObservedProps2, message);
    }

    @Test
    void test01CreateMultiDatastreams() throws ServiceFailureException {
        LOGGER.info("  test01MultiDatastream");
        // Create a MultiDatastream with one ObservedProperty.
        Entity md1 = mMdl.newMultiDatastream("MultiDatastream 1", "The first test MultiDatastream.", new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));

        List<String> dataTypes1 = new ArrayList<>();
        dataTypes1.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        md1.setProperty(EP_MULTIOBSERVATIONDATATYPES, dataTypes1);

        md1.setProperty(sMdl.npDatastreamThing, THINGS.get(0).withOnlyPk());
        md1.setProperty(sMdl.npDatastreamSensor, SENSORS.get(0).withOnlyPk());
        md1.addNavigationEntity(mMdl.npMultidatastreamObservedproperties, OBSERVED_PROPS.get(0).withOnlyPk());

        sSrvc.create(md1);
        MULTIDATASTREAMS.add(md1);

        // Create a MultiDatastream with two different ObservedProperties.
        Entity md2 = mMdl.newMultiDatastream("MultiDatastream 2", "The second test MultiDatastream.",
                new UnitOfMeasurement("degree celcius", "°C", "ucum:T"),
                new UnitOfMeasurement("percent", "%", "ucum:%"),
                new UnitOfMeasurement("Metre", "m", "ucum:m"),
                new UnitOfMeasurement("Metre", "m", "ucum:m"));

        List<String> dataTypes2 = new ArrayList<>();
        dataTypes2.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        dataTypes2.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        dataTypes2.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        dataTypes2.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        md2.setProperty(EP_MULTIOBSERVATIONDATATYPES, dataTypes2);

        md2.setProperty(sMdl.npDatastreamThing, THINGS.get(0).withOnlyPk());
        md2.setProperty(sMdl.npDatastreamSensor, SENSORS.get(0).withOnlyPk());

        md2.addNavigationEntity(mMdl.npMultidatastreamObservedproperties, OBSERVED_PROPS.get(0).withOnlyPk());
        md2.addNavigationEntity(mMdl.npMultidatastreamObservedproperties, OBSERVED_PROPS.get(1).withOnlyPk());
        md2.addNavigationEntity(mMdl.npMultidatastreamObservedproperties, OBSERVED_PROPS.get(2).withOnlyPk());
        md2.addNavigationEntity(mMdl.npMultidatastreamObservedproperties, OBSERVED_PROPS.get(3).withOnlyPk());

        sSrvc.create(md2);
        MULTIDATASTREAMS.add(md2);

        // Create a MultiDatastream with two different ObservedProperties, in the opposite order.
        Entity md3 = mMdl.newMultiDatastream("MultiDatastream 3", "The third test MultiDatastream.",
                new UnitOfMeasurement("percent", "%", "ucum:%"),
                new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));

        List<String> dataTypes3 = new ArrayList<>();
        dataTypes3.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        dataTypes3.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        md3.setProperty(EP_MULTIOBSERVATIONDATATYPES, dataTypes3);

        md3.setProperty(sMdl.npDatastreamThing, THINGS.get(0).withOnlyPk());
        md3.setProperty(sMdl.npDatastreamSensor, SENSORS.get(0).withOnlyPk());

        md3.addNavigationEntity(mMdl.npMultidatastreamObservedproperties, OBSERVED_PROPS.get(1).withOnlyPk());
        md3.addNavigationEntity(mMdl.npMultidatastreamObservedproperties, OBSERVED_PROPS.get(0).withOnlyPk());

        sSrvc.create(md3);
        MULTIDATASTREAMS.add(md3);

        // Create a MultiDatastream with two of the same ObservedProperties.
        Entity md4 = mMdl.newMultiDatastream("MultiDatastream 4", "The fourth test MultiDatastream.",
                new UnitOfMeasurement("degree celcius", "°C", "ucum:T"),
                new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));

        md4.setProperty(sMdl.npDatastreamThing, THINGS.get(0).withOnlyPk());
        md4.setProperty(sMdl.npDatastreamSensor, SENSORS.get(1).withOnlyPk());

        md4.addNavigationEntity(mMdl.npMultidatastreamObservedproperties, OBSERVED_PROPS.get(0).withOnlyPk());
        md4.addNavigationEntity(mMdl.npMultidatastreamObservedproperties, OBSERVED_PROPS.get(0).withOnlyPk());

        sSrvc.create(md4);
        MULTIDATASTREAMS.add(md4);
        assertEquals(4, MULTIDATASTREAMS.size());
    }

    @Test
    void test02MultiDatastreamObservedProperties1() throws ServiceFailureException {
        LOGGER.info("  test07MultiDatastreamObservedProperties1");
        // Check if all Datastreams and MultiDatastreams are linked to ObservedProperty 1.
        Entity fetchedObservedProp = sSrvc.dao(sMdl.etObservedProperty).find(OBSERVED_PROPS.get(0).getPrimaryKeyValues());
        EntitySet fetchedDatastreams = fetchedObservedProp.query(sMdl.npObspropDatastreams).list();
        checkResult(
                "Check Datastreams linked to ObservedProperty 1.",
                EntityUtils.resultContains(fetchedDatastreams, getFromList(DATASTREAMS, 0, 1)));
        EntitySet fetchedMultiDatastreams = fetchedObservedProp.query(mMdl.npObspropMultidatastreams).list();
        checkResult(
                "Check MultiDatastreams linked to ObservedProperty 1.",
                EntityUtils.resultContains(fetchedMultiDatastreams, new ArrayList<>(MULTIDATASTREAMS)));
    }

    @Test
    void test03MultiDatastreamObservedProperties2() throws ServiceFailureException {
        LOGGER.info("  test08MultiDatastreamObservedProperties2");
        // Check if MultiDatastreams 2 and 3 are linked to ObservedProperty 2.
        Entity fetchedObservedProp = sSrvc.dao(sMdl.etObservedProperty).find(OBSERVED_PROPS.get(1).getPrimaryKeyValues());
        EntitySet fetchedDatastreams = fetchedObservedProp.query(sMdl.npObspropDatastreams).list();
        checkResult(
                "Check Datastreams linked to ObservedProperty 2.",
                EntityUtils.resultContains(fetchedDatastreams, new ArrayList<>()));
        EntitySet fetchedMultiDatastreams = fetchedObservedProp.query(mMdl.npObspropMultidatastreams).list();
        checkResult(
                "Check MultiDatastreams linked to ObservedProperty 2.",
                EntityUtils.resultContains(fetchedMultiDatastreams, getFromList(MULTIDATASTREAMS, 1, 2)));
    }

    @Test
    void test04ObservedPropertyOrder() throws ServiceFailureException {
        LOGGER.info("  test11ObservedPropertyOrder");
        // Check if the MultiDatastreams have the correct ObservedProperties in the correct order.
        checkObservedPropertiesFor(MULTIDATASTREAMS.get(0), OBSERVED_PROPS.get(0));
        checkObservedPropertiesFor(MULTIDATASTREAMS.get(1), OBSERVED_PROPS.get(0), OBSERVED_PROPS.get(1), OBSERVED_PROPS.get(2), OBSERVED_PROPS.get(3));
        checkObservedPropertiesFor(MULTIDATASTREAMS.get(2), OBSERVED_PROPS.get(1), OBSERVED_PROPS.get(0));
        checkObservedPropertiesFor(MULTIDATASTREAMS.get(3), OBSERVED_PROPS.get(0), OBSERVED_PROPS.get(0));
    }

    @Test
    void test05UnLinkObsProp1() throws ServiceFailureException {
        LOGGER.info("  test12IncorrectObservation");
        // Try to delete the first ObservedProperty of MD 2.
        Entity md2 = MULTIDATASTREAMS.get(1);
        Entity op2 = OBSERVED_PROPS.get(1);
        Entity op3 = OBSERVED_PROPS.get(2);
        String md2Id = formatKeyValuesForUrl(md2.getPrimaryKeyValues());
        String op2Id = formatKeyValuesForUrl(op2.getPrimaryKeyValues());
        String op3Id = formatKeyValuesForUrl(op3.getPrimaryKeyValues());

        HttpResponse response = HTTPMethods.doDelete(serverSettings.getServiceUrl(version) + "/MultiDatastreams(" + md2Id + ")/ObservedProperties/$ref?$id=../../Observations(" + op2Id + ")");
        assertEquals(400, response.code);
        checkObservedPropertiesFor(MULTIDATASTREAMS.get(1), OBSERVED_PROPS.get(0), OBSERVED_PROPS.get(1), OBSERVED_PROPS.get(2), OBSERVED_PROPS.get(3));

        response = HTTPMethods.doDelete(serverSettings.getServiceUrl(version) + "/MultiDatastreams(" + md2Id + ")/ObservedProperties(" + op2Id + ")/$ref");
        assertEquals(204, response.code);
        checkObservedPropertiesFor(MULTIDATASTREAMS.get(1), OBSERVED_PROPS.get(0), OBSERVED_PROPS.get(2), OBSERVED_PROPS.get(3));

        response = HTTPMethods.doDelete(serverSettings.getServiceUrl(version) + "/MultiDatastreams(" + md2Id + ")/ObservedProperties/$ref?$id=../../ObservedProperties(" + op3Id + ")");
        assertEquals(204, response.code);
        checkObservedPropertiesFor(MULTIDATASTREAMS.get(1), OBSERVED_PROPS.get(0), OBSERVED_PROPS.get(3));

        response = HTTPMethods.doDelete(serverSettings.getServiceUrl(version) + "/MultiDatastreams(" + md2Id + ")/ObservedProperties/$ref?$id=../../ObservedProperties(-1)");
        assertEquals(404, response.code);
        checkObservedPropertiesFor(MULTIDATASTREAMS.get(1), OBSERVED_PROPS.get(0), OBSERVED_PROPS.get(3));

    }

}
