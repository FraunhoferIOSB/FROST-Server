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
package de.fraunhofer.iosb.ilt.statests.c02cud;

import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.EP_PROPERTIES;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.MapValue;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.frostclient.utils.CollectionsHelper;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import de.fraunhofer.iosb.ilt.statests.util.Utils;
import java.util.ArrayList;
import java.util.List;
import org.geojson.Point;
import org.junit.jupiter.api.Test;
import org.opentmf.commons.patch.JsonPatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.node.LongNode;
import tools.jackson.databind.node.StringNode;

public abstract class JsonPatchTests extends AbstractTestClass {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonPatchTests.class);

    private static final List<Entity> THINGS = new ArrayList<>();
    private static final List<Entity> LOCATIONS = new ArrayList<>();
    private static final List<Entity> SENSORS = new ArrayList<>();
    private static final List<Entity> OPROPS = new ArrayList<>();
    private static final List<Entity> DATASTREAMS = new ArrayList<>();

    private static SensorThingsV11Sensing sMdl;

    public JsonPatchTests(ServerVersion version) {
        super(version);
    }

    @Override
    protected void setUpVersion() throws ServiceFailureException {
        LOGGER.info("Setting up for version {}.", version.urlPart);
        sMdl = sSrvc.getModel(SensorThingsV11Sensing.class);
        EntityUtils.deleteAll(sSrvc);
        createEntities();
    }

    public static void cleanup() {
        EntityUtils.deleteAll(sSrvc);
        THINGS.clear();
        LOCATIONS.clear();
        SENSORS.clear();
        OPROPS.clear();
        DATASTREAMS.clear();
        AbstractTestClass.cleanup();
    }

    private static void createEntities() throws ServiceFailureException {
        {
            Entity thing = sMdl.buildThing()
                    .setName("Thing 1")
                    .setDescription("The first thing.")
                    .setProperties(CollectionsHelper.propertiesBuilder().addItem("key0", "zero").build())
                    .build();
            sSrvc.create(thing);
            THINGS.add(thing);
        }
        {
            Entity location = sMdl.buildLocation()
                    .setName("Location Des Dings von ILT")
                    .setDescription("First Location of Thing 1.")
                    .setEncodingType("application/vnd.geo+json")
                    .setLocation(new Point(8, 49))
                    .addThing(THINGS.get(0))
                    .build();
            sSrvc.create(location);
            LOCATIONS.add(location);
        }
        {
            Entity sensor1 = sMdl.buildSensor()
                    .setName("Sensor 1")
                    .setDescription("The first sensor.")
                    .setEncodingType("text")
                    .setMetadata("Some metadata.")
                    .build();
            sSrvc.create(sensor1);
            SENSORS.add(sensor1);
        }
        {
            Entity sensor2 = sMdl.buildSensor()
                    .setName("Sensor 2")
                    .setDescription("The second sensor")
                    .setEncodingType("text")
                    .setMetadata("Some metadata.")
                    .build();
            sSrvc.create(sensor2);
            SENSORS.add(sensor2);
        }
        {
            Entity obsProp1 = sMdl.buildObservedProperty()
                    .setName("Temperature")
                    .setDefinition("http://ucom.org/temperature")
                    .setDescription("The temperature of the thing.")
                    .build();
            sSrvc.create(obsProp1);
            OPROPS.add(obsProp1);
        }
        {
            Entity obsProp2 = sMdl.buildObservedProperty()
                    .setName("Humidity")
                    .setDefinition("http://ucom.org/humidity")
                    .setDescription("The humidity of the thing.")
                    .build();
            sSrvc.create(obsProp2);
            OPROPS.add(obsProp2);
        }
        {
            Entity datastream1 = sMdl.buildDatastream()
                    .setName("Datastream Temp")
                    .setDescription("The temperature of thing 1, sensor 1.")
                    .setObservationType("someType")
                    .setUnitOfMeasurement(new UnitOfMeasurement("degree celcius", "°C", "ucum:T"))
                    .setThing(THINGS.get(0).withOnlyPk())
                    .setSensor(SENSORS.get(0).withOnlyPk())
                    .setObservedProperty(OPROPS.get(0).withOnlyPk())
                    .build();
            sSrvc.create(datastream1);
            DATASTREAMS.add(datastream1);
        }
        {
            Entity datastream2 = sMdl.buildDatastream()
                    .setName("Datastream LF")
                    .setDescription("The humidity of thing 1, sensor 2.")
                    .setObservationType("someType")
                    .setUnitOfMeasurement(new UnitOfMeasurement("relative humidity", "%", "ucum:Humidity"))
                    .setThing(THINGS.get(0).withOnlyPk())
                    .setSensor(SENSORS.get(1).withOnlyPk())
                    .setObservedProperty(OPROPS.get(1).withOnlyPk())
                    .build();
            sSrvc.create(datastream2);
            DATASTREAMS.add(datastream2);
        }
    }

    /**
     * Tests if JSON-Patch is working on Things.
     *
     * @throws ServiceFailureException if the sSrvc connection fails.
     */
    @Test
    void jsonPatchThingTest() throws ServiceFailureException {
        LOGGER.info("  jsonPatchThingTest");
        Entity thingOnlyId = THINGS.get(0).withOnlyPk();
        JsonPatch patch = JsonPatch.builder()
                .add("/properties/key1", 1)
                .build();
        sSrvc.patch(thingOnlyId, patch);
        Entity updatedThing = sSrvc.dao(sMdl.etThing).find(thingOnlyId.getPrimaryKeyValues());

        String message = "properties/key1 was not added correctly.";
        assertEquals(LongNode.valueOf(1), updatedThing.getProperty(EP_PROPERTIES).get("key1"), message);
        message = "properties/key0 was changed.";
        assertEquals(StringNode.valueOf("zero"), updatedThing.getProperty(EP_PROPERTIES).get("key0"), message);
        patch = JsonPatch.builder()
                .copy("/properties/key1", "/properties/keyCopy1")
                .move("/properties/key1", "/properties/key2")
                .build();
        sSrvc.patch(thingOnlyId, patch);
        updatedThing = sSrvc.dao(sMdl.etThing).find(thingOnlyId.getPrimaryKeyValues());

        final MapValue updatedProperties = updatedThing.getProperty(EP_PROPERTIES);
        message = "properties/key0 was changed.";
        assertEquals(StringNode.valueOf("zero"), updatedThing.getProperty(EP_PROPERTIES).get("key0"), message);
        message = "properties/keyCopy1 does not exist after copy.";
        assertEquals(LongNode.valueOf(1), updatedProperties.get("keyCopy1"), message);
        message = "properties/key1 still exists after move.";
        assertEquals(null, updatedProperties.get("key1"), message);
        message = "properties/key2 does not exist after move.";
        assertEquals(LongNode.valueOf(1), updatedProperties.get("key2"), message);
    }

    @Test
    void jsonPatchThingNoOpTest() throws ServiceFailureException {
        LOGGER.info("  jsonPatchThingTest");
        Entity thingOnlyId = THINGS.get(0).withOnlyPk();
        JsonPatch patch = JsonPatch.builder()
                .add("/properties", Utils.MAPPER.readTree("{\"key1\": 2}"))
                .build();
        sSrvc.patch(thingOnlyId, patch);
        Entity updatedThing = sSrvc.dao(sMdl.etThing).find(thingOnlyId.getPrimaryKeyValues());

        String message = "properties/key1 was not added correctly.";
        assertEquals(new LongNode(2), updatedThing.getProperty(EP_PROPERTIES).get("key1"), message);

        // This patch should result in no change.
        patch = JsonPatch.builder()
                .replace("/properties/key1", 2)
                .build();
        sSrvc.patch(thingOnlyId, patch);
        updatedThing = sSrvc.dao(sMdl.etThing).find(thingOnlyId.getPrimaryKeyValues());

        final MapValue updatedProperties = updatedThing.getProperty(EP_PROPERTIES);
        message = "properties/key1 does not have the correct value.";
        assertEquals(LongNode.valueOf(2), updatedProperties.get("key1"), message);
    }

    /**
     * Tests if JSON-Patch is working on Datastreams.
     *
     * @throws ServiceFailureException if the sSrvc connection fails.
     */
    @Test
    void jsonPatchDatastreamTest() throws ServiceFailureException {
        LOGGER.info("  jsonPatchDatastreamTest");
        Entity dsOnlyId = DATASTREAMS.get(0).withOnlyPk();
        JsonPatch patch = JsonPatch.builder()
                .add("/properties", Utils.MAPPER.readTree("{\"key1\": 1}"))
                .build();
        sSrvc.patch(dsOnlyId, patch);
        Entity updatedDs = sSrvc.dao(sMdl.etDatastream).find(dsOnlyId.getPrimaryKeyValues());

        String message = "properties/key1 was not added correctly.";
        assertEquals(LongNode.valueOf(1), updatedDs.getProperty(EP_PROPERTIES).get("key1"), message);

        patch = JsonPatch.builder()
                .copy("/properties/key1", "/properties/keyCopy1")
                .move("/properties/key1", "/properties/key2")
                .build();
        sSrvc.patch(dsOnlyId, patch);
        updatedDs = sSrvc.dao(sMdl.etDatastream).find(dsOnlyId.getPrimaryKeyValues());

        final MapValue updatedProperties = updatedDs.getProperty(EP_PROPERTIES);
        message = "properties/keyCopy1 does not exist after copy.";
        assertEquals(LongNode.valueOf(1), updatedProperties.get("keyCopy1"), message);
        message = "properties/key1 still exists after move.";
        assertEquals(null, updatedProperties.get("key1"), message);
        message = "properties/key2 does not exist after move.";
        assertEquals(LongNode.valueOf(1), updatedProperties.get("key2"), message);
    }

}
