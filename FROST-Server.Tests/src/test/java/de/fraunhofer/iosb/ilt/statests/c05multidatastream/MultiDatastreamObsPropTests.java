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

import static de.fraunhofer.iosb.ilt.frostclient.utils.StringHelper.formatKeyValuesForUrl;
import static de.fraunhofer.iosb.ilt.statests.util.Utils.getFromList;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.model.EntitySet;
import de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11MultiDatastream;
import de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerSettings;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods.HttpResponse;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.geojson.Point;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some odd tests.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public abstract class MultiDatastreamObsPropTests extends AbstractTestClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiDatastreamObsPropTests.class);

    private static final List<Entity> THINGS = new ArrayList<>();
    private static final List<Entity> LOCATIONS = new ArrayList<>();
    private static final List<Entity> SENSORS = new ArrayList<>();
    private static final List<Entity> OBSERVED_PROPS = new ArrayList<>();
    private static final List<Entity> DATASTREAMS = new ArrayList<>();
    private static final List<Entity> MULTIDATASTREAMS = new ArrayList<>();
    private static final List<Entity> OBSERVATIONS = new ArrayList<>();
    private static SensorThingsV11Sensing sMdl;
    private static SensorThingsV11MultiDatastream mMdl;

    public MultiDatastreamObsPropTests(ServerVersion version) {
        super(version);
    }

    @BeforeEach
    void before() {
        assumeTrue(
                serverSettings.implementsRequirement(version, ServerSettings.MULTIDATA_REQ),
                "Conformance level 5 not checked since MultiDatastreams not listed in Service Root.");
    }

    @Override
    protected void setUpVersion() throws ServiceFailureException {
        LOGGER.info("Setting up for version {}.", version.urlPart);
        sMdl = sSrvc.getModel(SensorThingsV11Sensing.class);
        mMdl = sSrvc.getModel(SensorThingsV11MultiDatastream.class);
        assumeTrue(
                serverSettings.implementsRequirement(version, ServerSettings.MULTIDATA_REQ),
                "Conformance level 5 not checked since MultiDatastreams not listed in Service Root.");
        createEntities();
    }

    public static void cleanup() {
        EntityUtils.deleteAll(sSrvc);
        THINGS.clear();
        LOCATIONS.clear();
        SENSORS.clear();
        OBSERVED_PROPS.clear();
        DATASTREAMS.clear();
        MULTIDATASTREAMS.clear();
        OBSERVATIONS.clear();
        AbstractTestClass.cleanup();
    }

    /**
     * Creates some basic non-MultiDatastream entities.
     *
     * @throws ServiceFailureException
     * @throws URISyntaxException
     */
    private static void createEntities() throws ServiceFailureException {
        Entity location = sMdl.buildLocation()
                .setName("Location 1.0")
                .setDescription("Location of Thing 1.")
                .setEncodingType("application/vnd.geo+json")
                .setLocation(new Point(8, 51))
                .build();
        sSrvc.create(location);
        LOCATIONS.add(location);

        Entity thing = sMdl.buildThing()
                .setName("Thing 1")
                .setDescription("The first thing.")
                .build();
        thing.getProperty(sMdl.npThingLocations).add(location.withOnlyPk());
        sSrvc.create(thing);
        THINGS.add(thing);

        thing = sMdl.buildThing()
                .setName("Thing 2")
                .setDescription("The second thing.")
                .build();
        thing.getProperty(sMdl.npThingLocations).add(location.withOnlyPk());
        sSrvc.create(thing);
        THINGS.add(thing);

        Entity sensor = sMdl.buildSensor()
                .setName("Sensor 1")
                .setDescription("The first sensor.")
                .setEncodingType("text")
                .setMetadata("Some metadata.")
                .build();
        sSrvc.create(sensor);
        SENSORS.add(sensor);

        sensor = sMdl.buildSensor()
                .setName("Sensor 2")
                .setDescription("The second sensor.")
                .setEncodingType("text")
                .setMetadata("Some metadata.")
                .build();
        sSrvc.create(sensor);
        SENSORS.add(sensor);

        Entity obsProp = sMdl.buildObservedProperty()
                .setName("ObservedProperty 1")
                .setDefinition("http://ucom.org/temperature")
                .setDescription("The temperature of the thing.")
                .build();
        sSrvc.create(obsProp);
        OBSERVED_PROPS.add(obsProp);

        obsProp = sMdl.buildObservedProperty()
                .setName("ObservedProperty 2")
                .setDefinition("http://ucom.org/humidity")
                .setDescription("The humidity of the thing.")
                .build();
        sSrvc.create(obsProp);
        OBSERVED_PROPS.add(obsProp);

        obsProp = sMdl.buildObservedProperty()
                .setName("ObservedProperty 3")
                .setDefinition("http://ucom.org/height")
                .setDescription("The height of the thing.")
                .build();
        sSrvc.create(obsProp);
        OBSERVED_PROPS.add(obsProp);

        obsProp = sMdl.buildObservedProperty()
                .setName("ObservedProperty 4")
                .setDefinition("http://ucom.org/depth")
                .setDescription("The depth of the thing.")
                .build();
        sSrvc.create(obsProp);
        OBSERVED_PROPS.add(obsProp);

        Entity datastream = sMdl.buildDatastream()
                .setName("Datastream 1")
                .setDescription("The temperature of thing 1, sensor 1.")
                .setObservationType("someType")
                .setUnitOfMeasurement(new UnitOfMeasurement("degree celcius", "°C", "ucum:T"))
                .build();
        DATASTREAMS.add(datastream);
        datastream.setProperty(sMdl.npDatastreamThing, THINGS.get(0).withOnlyPk());
        datastream.setProperty(sMdl.npDatastreamSensor, SENSORS.get(0).withOnlyPk());
        datastream.setProperty(sMdl.npDatastreamObservedproperty, OBSERVED_PROPS.get(0).withOnlyPk());
        sSrvc.create(datastream);

        datastream = sMdl.buildDatastream()
                .setName("Datastream 2")
                .setDescription("The temperature of thing 2, sensor 2.")
                .setObservationType("someType")
                .setUnitOfMeasurement(new UnitOfMeasurement("degree celcius", "°C", "ucum:T"))
                .build();
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
        List<String> dataTypes1 = Arrays.asList("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        Entity md1 = mMdl.buildMultiDatastream()
                .setName("MultiDatastream 1")
                .setDescription("The first test MultiDatastream.")
                .setUnitOfMeasurements(new UnitOfMeasurement("degree celcius", "°C", "ucum:T"))
                .setMultiObservationDataTypes(dataTypes1)
                .setThing(THINGS.get(0).withOnlyPk())
                .setSensor(SENSORS.get(0).withOnlyPk())
                .addObservedProperty(OBSERVED_PROPS.get(0).withOnlyPk())
                .build();

        sSrvc.create(md1);
        MULTIDATASTREAMS.add(md1);

        // Create a MultiDatastream with four different ObservedProperties.
        List<String> dataTypes2 = Arrays.asList(
                "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement",
                "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement",
                "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement",
                "http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        Entity md2 = mMdl.buildMultiDatastream()
                .setName("MultiDatastream 2")
                .setDescription("The second test MultiDatastream.")
                .setUnitOfMeasurements(
                        new UnitOfMeasurement("degree celcius", "°C", "ucum:T"),
                        new UnitOfMeasurement("percent", "%", "ucum:%"),
                        new UnitOfMeasurement("Metre", "m", "ucum:m"),
                        new UnitOfMeasurement("Metre", "m", "ucum:m"))
                .setMultiObservationDataTypes(dataTypes2)
                .setThing(THINGS.get(0).withOnlyPk())
                .setSensor(SENSORS.get(0).withOnlyPk())
                .addObservedProperty(OBSERVED_PROPS.get(0).withOnlyPk())
                .addObservedProperty(OBSERVED_PROPS.get(1).withOnlyPk())
                .addObservedProperty(OBSERVED_PROPS.get(2).withOnlyPk())
                .addObservedProperty(OBSERVED_PROPS.get(3).withOnlyPk())
                .build();

        sSrvc.create(md2);
        MULTIDATASTREAMS.add(md2);

        // Create a MultiDatastream with two different ObservedProperties, in the opposite order.
        List<String> dataTypes3 = new ArrayList<>();
        dataTypes3.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        dataTypes3.add("http://www.opengis.net/def/observationType/OGC-OM/2.0/OM_Measurement");
        Entity md3 = mMdl.buildMultiDatastream()
                .setName("MultiDatastream 3")
                .setDescription("The third test MultiDatastream.")
                .setUnitOfMeasurements(
                        new UnitOfMeasurement("percent", "%", "ucum:%"),
                        new UnitOfMeasurement("degree celcius", "°C", "ucum:T"))
                .setMultiObservationDataTypes(dataTypes3)
                .setThing(THINGS.get(0).withOnlyPk())
                .setSensor(SENSORS.get(0).withOnlyPk())
                .addObservedProperty(OBSERVED_PROPS.get(1).withOnlyPk())
                .addObservedProperty(OBSERVED_PROPS.get(0).withOnlyPk())
                .build();

        sSrvc.create(md3);
        MULTIDATASTREAMS.add(md3);

        // Create a MultiDatastream with two of the same ObservedProperties.
        Entity md4 = mMdl.buildMultiDatastream()
                .setName("MultiDatastream 4")
                .setDescription("The fourth test MultiDatastream.")
                .setUnitOfMeasurements(
                        new UnitOfMeasurement("degree celcius", "°C", "ucum:T"),
                        new UnitOfMeasurement("degree celcius", "°C", "ucum:T"))
                .createMultiObservationDataType()
                .setThing(THINGS.get(0).withOnlyPk())
                .setSensor(SENSORS.get(1).withOnlyPk())
                .addObservedProperty(OBSERVED_PROPS.get(0).withOnlyPk())
                .addObservedProperty(OBSERVED_PROPS.get(0).withOnlyPk())
                .build();

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
        String md2Id = formatKeyValuesForUrl(md2);
        String op2Id = formatKeyValuesForUrl(op2);
        String op3Id = formatKeyValuesForUrl(op3);

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
