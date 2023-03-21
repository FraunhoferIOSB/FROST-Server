/*
 * Copyright (C) 2023 Fraunhofer Institut IOSB, Fraunhoferstr. 1, D 76131
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

import static de.fraunhofer.iosb.ilt.statests.util.Utils.getFromList;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.Location;
import de.fraunhofer.iosb.ilt.sta.model.MultiDatastream;
import de.fraunhofer.iosb.ilt.sta.model.Observation;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.ext.EntityList;
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerSettings;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods.HttpResponse;
import java.net.URI;
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

    public static class Implementation10 extends MultiDatastreamObsPropTests {

        public Implementation10() {
            super(ServerVersion.v_1_0);
        }

    }

    public static class Implementation11 extends MultiDatastreamObsPropTests {

        public Implementation11() {
            super(ServerVersion.v_1_1);
        }

    }

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiDatastreamObsPropTests.class);

    private static final List<Thing> THINGS = new ArrayList<>();
    private static final List<Location> LOCATIONS = new ArrayList<>();
    private static final List<Sensor> SENSORS = new ArrayList<>();
    private static final List<ObservedProperty> OBSERVED_PROPS = new ArrayList<>();
    private static final List<Datastream> DATASTREAMS = new ArrayList<>();
    private static final List<MultiDatastream> MULTIDATASTREAMS = new ArrayList<>();
    private static final List<Observation> OBSERVATIONS = new ArrayList<>();

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
        EntityUtils.deleteAll(version, serverSettings, service);
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
        Location location = new Location("Location 1.0", "Location of Thing 1.", "application/vnd.geo+json", new Point(8, 51));
        service.create(location);
        LOCATIONS.add(location);

        Thing thing = new Thing("Thing 1", "The first thing.");
        thing.getLocations().add(location.withOnlyId());
        service.create(thing);
        THINGS.add(thing);

        thing = new Thing("Thing 2", "The second thing.");
        thing.getLocations().add(location.withOnlyId());
        service.create(thing);
        THINGS.add(thing);

        Sensor sensor = new Sensor("Sensor 1", "The first sensor.", "text", "Some metadata.");
        service.create(sensor);
        SENSORS.add(sensor);

        sensor = new Sensor("Sensor 2", "The second sensor.", "text", "Some metadata.");
        service.create(sensor);
        SENSORS.add(sensor);

        ObservedProperty obsProp = new ObservedProperty("ObservedProperty 1", new URI("http://ucom.org/temperature"), "The temperature of the thing.");
        service.create(obsProp);
        OBSERVED_PROPS.add(obsProp);

        obsProp = new ObservedProperty("ObservedProperty 2", new URI("http://ucom.org/humidity"), "The humidity of the thing.");
        service.create(obsProp);
        OBSERVED_PROPS.add(obsProp);

        obsProp = new ObservedProperty("ObservedProperty 3", new URI("http://ucom.org/height"), "The height of the thing.");
        service.create(obsProp);
        OBSERVED_PROPS.add(obsProp);

        obsProp = new ObservedProperty("ObservedProperty 4", new URI("http://ucom.org/depth"), "The depth of the thing.");
        service.create(obsProp);
        OBSERVED_PROPS.add(obsProp);

        Datastream datastream = new Datastream("Datastream 1", "The temperature of thing 1, sensor 1.", "someType", new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
        DATASTREAMS.add(datastream);
        datastream.setThing(THINGS.get(0).withOnlyId());
        datastream.setSensor(SENSORS.get(0).withOnlyId());
        datastream.setObservedProperty(OBSERVED_PROPS.get(0).withOnlyId());
        service.create(datastream);

        datastream = new Datastream("Datastream 2", "The temperature of thing 2, sensor 2.", "someType", new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
        DATASTREAMS.add(datastream);
        datastream.setThing(THINGS.get(1).withOnlyId());
        datastream.setSensor(SENSORS.get(1).withOnlyId());
        datastream.setObservedProperty(OBSERVED_PROPS.get(0).withOnlyId());
        service.create(datastream);

    }

    private void checkResult(String test, EntityUtils.ResultTestResult result) {
        assertTrue(result.testOk, test + " " + result.message);
    }

    private void checkObservedPropertiesFor(MultiDatastream md, ObservedProperty... expectedObservedProps) throws ServiceFailureException {
        ObservedProperty[] fetchedObservedProps2 = md.observedProperties().query().list().toArray(new ObservedProperty[0]);
        String message = "Incorrect Observed Properties returned.";
        assertArrayEquals(expectedObservedProps, fetchedObservedProps2, message);
    }

    @Test
    void test01CreateMultiDatastreams() throws ServiceFailureException {
        LOGGER.info("  test01MultiDatastream");
        // Create a MultiDatastream with one ObservedProperty.
        MultiDatastream md1 = new MultiDatastream();
        md1.setName("MultiDatastream 1");
        md1.setDescription("The first test MultiDatastream.");
        md1.addUnitOfMeasurement(new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));

        List<String> dataTypes1 = new ArrayList<>();
        dataTypes1.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        md1.setMultiObservationDataTypes(dataTypes1);

        md1.setThing(THINGS.get(0).withOnlyId());
        md1.setSensor(SENSORS.get(0).withOnlyId());

        List<ObservedProperty> observedProperties = new ArrayList<>();
        observedProperties.add(OBSERVED_PROPS.get(0).withOnlyId());
        md1.setObservedProperties(observedProperties);

        service.create(md1);
        MULTIDATASTREAMS.add(md1);

        // Create a MultiDatastream with two different ObservedProperties.
        MultiDatastream md2 = new MultiDatastream();
        md2.setName("MultiDatastream 2");
        md2.setDescription("The second test MultiDatastream.");
        md2.addUnitOfMeasurement(new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
        md2.addUnitOfMeasurement(new UnitOfMeasurement("percent", "%", "ucum:%"));
        md2.addUnitOfMeasurement(new UnitOfMeasurement("Metre", "m", "ucum:m"));
        md2.addUnitOfMeasurement(new UnitOfMeasurement("Metre", "m", "ucum:m"));

        List<String> dataTypes2 = new ArrayList<>();
        dataTypes2.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        dataTypes2.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        dataTypes2.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        dataTypes2.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        md2.setMultiObservationDataTypes(dataTypes2);

        md2.setThing(THINGS.get(0).withOnlyId());
        md2.setSensor(SENSORS.get(0).withOnlyId());

        List<ObservedProperty> observedProperties2 = new ArrayList<>();
        observedProperties2.add(OBSERVED_PROPS.get(0).withOnlyId());
        observedProperties2.add(OBSERVED_PROPS.get(1).withOnlyId());
        observedProperties2.add(OBSERVED_PROPS.get(2).withOnlyId());
        observedProperties2.add(OBSERVED_PROPS.get(3).withOnlyId());
        md2.setObservedProperties(observedProperties2);

        service.create(md2);
        MULTIDATASTREAMS.add(md2);

        // Create a MultiDatastream with two different ObservedProperties, in the opposite order.
        MultiDatastream md3 = new MultiDatastream();
        md3.setName("MultiDatastream 3");
        md3.setDescription("The third test MultiDatastream.");
        md3.addUnitOfMeasurement(new UnitOfMeasurement("percent", "%", "ucum:%"));
        md3.addUnitOfMeasurement(new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));

        List<String> dataTypes3 = new ArrayList<>();
        dataTypes3.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        dataTypes3.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        md3.setMultiObservationDataTypes(dataTypes3);

        md3.setThing(THINGS.get(0).withOnlyId());
        md3.setSensor(SENSORS.get(0).withOnlyId());

        List<ObservedProperty> observedProperties3 = new ArrayList<>();
        observedProperties3.add(OBSERVED_PROPS.get(1).withOnlyId());
        observedProperties3.add(OBSERVED_PROPS.get(0).withOnlyId());
        md3.setObservedProperties(observedProperties3);

        service.create(md3);
        MULTIDATASTREAMS.add(md3);

        // Create a MultiDatastream with two of the same ObservedProperties.
        MultiDatastream md4 = new MultiDatastream();
        md4.setName("MultiDatastream 4");
        md4.setDescription("The fourth test MultiDatastream.");
        md4.addUnitOfMeasurement(new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
        md4.addUnitOfMeasurement(new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));

        List<String> dataTypes4 = new ArrayList<>();
        dataTypes4.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        dataTypes4.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        md4.setMultiObservationDataTypes(dataTypes4);

        md4.setThing(THINGS.get(0).withOnlyId());
        md4.setSensor(SENSORS.get(1).withOnlyId());

        List<ObservedProperty> observedProperties4 = new ArrayList<>();
        observedProperties4.add(OBSERVED_PROPS.get(0).withOnlyId());
        observedProperties4.add(OBSERVED_PROPS.get(0).withOnlyId());
        md4.setObservedProperties(observedProperties4);

        service.create(md4);
        MULTIDATASTREAMS.add(md4);
        assertEquals(4, MULTIDATASTREAMS.size());
    }

    @Test
    void test02MultiDatastreamObservedProperties1() throws ServiceFailureException {
        LOGGER.info("  test07MultiDatastreamObservedProperties1");
        // Check if all Datastreams and MultiDatastreams are linked to ObservedProperty 1.
        ObservedProperty fetchedObservedProp = service.observedProperties().find(OBSERVED_PROPS.get(0).getId());
        EntityList<Datastream> fetchedDatastreams = fetchedObservedProp.datastreams().query().list();
        checkResult(
                "Check Datastreams linked to ObservedProperty 1.",
                EntityUtils.resultContains(fetchedDatastreams, getFromList(DATASTREAMS, 0, 1)));
        EntityList<MultiDatastream> fetchedMultiDatastreams = fetchedObservedProp.multiDatastreams().query().list();
        checkResult(
                "Check MultiDatastreams linked to ObservedProperty 1.",
                EntityUtils.resultContains(fetchedMultiDatastreams, new ArrayList<>(MULTIDATASTREAMS)));
    }

    @Test
    void test03MultiDatastreamObservedProperties2() throws ServiceFailureException {
        LOGGER.info("  test08MultiDatastreamObservedProperties2");
        // Check if MultiDatastreams 2 and 3 are linked to ObservedProperty 2.
        ObservedProperty fetchedObservedProp = service.observedProperties().find(OBSERVED_PROPS.get(1).getId());
        EntityList<Datastream> fetchedDatastreams = fetchedObservedProp.datastreams().query().list();
        checkResult(
                "Check Datastreams linked to ObservedProperty 2.",
                EntityUtils.resultContains(fetchedDatastreams, new ArrayList<>()));
        EntityList<MultiDatastream> fetchedMultiDatastreams = fetchedObservedProp.multiDatastreams().query().list();
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
        MultiDatastream md2 = MULTIDATASTREAMS.get(1);
        ObservedProperty op2 = OBSERVED_PROPS.get(1);
        ObservedProperty op3 = OBSERVED_PROPS.get(2);
        String md2Id = md2.getId().getUrl();
        String op2Id = op2.getId().getUrl();
        String op3Id = op3.getId().getUrl();

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
