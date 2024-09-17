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
package de.fraunhofer.iosb.ilt.statests.c02cud;

import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.EP_PROPERTIES;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.node.IntNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.JsonPointerException;
import com.github.fge.jsonpatch.AddOperation;
import com.github.fge.jsonpatch.CopyOperation;
import com.github.fge.jsonpatch.JsonPatchOperation;
import com.github.fge.jsonpatch.MoveOperation;
import com.github.fge.jsonpatch.ReplaceOperation;
import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.MapValue;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import de.fraunhofer.iosb.ilt.statests.util.Utils;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.geojson.Point;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hylke van der Schaaf
 */
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

    public JsonPatchTests(ServerVersion version) {
        super(version);
    }

    @Override
    protected void setUpVersion() throws ServiceFailureException, URISyntaxException {
        LOGGER.info("Setting up for version {}.", version.urlPart);
        EntityUtils.deleteAll(service);
        createEntities();
    }

    @Override
    protected void tearDownVersion() throws ServiceFailureException {
        cleanup();
    }

    private static void cleanup() throws ServiceFailureException {
        EntityUtils.deleteAll(service);
        THINGS.clear();
        LOCATIONS.clear();
        SENSORS.clear();
        OPROPS.clear();
        DATASTREAMS.clear();
    }

    /**
     * This method is run after all the tests of this class is run and clean the
     * database.
     *
     * @throws
     * de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException
     */
    @AfterAll
    public static void deleteEverything() throws ServiceFailureException {
        LOGGER.info("Tearing down.");
        cleanup();
    }

    private static void createEntities() throws ServiceFailureException, URISyntaxException {
        {
            Entity thing = sMdl.newThing("Thing 1", "The first thing.");
            sSrvc.create(thing);
            THINGS.add(thing);
        }
        {
            Entity location = sMdl.newLocation("Location Des Dings von ILT", "First Location of Thing 1.", "application/vnd.geo+json", new Point(8, 49));
            location.addNavigationEntity(sMdl.npLocationThings, THINGS.get(0));
            sSrvc.create(location);
            LOCATIONS.add(location);
        }
        {
            Entity sensor1 = sMdl.newSensor("Sensor 1", "The first sensor.", "text", "Some metadata.");
            sSrvc.create(sensor1);
            SENSORS.add(sensor1);
        }
        {
            Entity sensor2 = sMdl.newSensor("Sensor 2", "The second sensor", "text", "Some metadata.");
            sSrvc.create(sensor2);
            SENSORS.add(sensor2);
        }
        {
            Entity obsProp1 = sMdl.newObservedProperty("Temperature", "http://ucom.org/temperature", "The temperature of the thing.");
            sSrvc.create(obsProp1);
            OPROPS.add(obsProp1);
        }
        {
            Entity obsProp2 = sMdl.newObservedProperty("Humidity", "http://ucom.org/humidity", "The humidity of the thing.");
            sSrvc.create(obsProp2);
            OPROPS.add(obsProp2);
        }
        {
            Entity datastream1 = sMdl.newDatastream("Datastream Temp", "The temperature of thing 1, sensor 1.", "someType", new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
            datastream1.setProperty(sMdl.npDatastreamThing, THINGS.get(0).withOnlyPk());
            datastream1.setProperty(sMdl.npDatastreamSensor, SENSORS.get(0).withOnlyPk());
            datastream1.setProperty(sMdl.npDatastreamObservedproperty, OPROPS.get(0).withOnlyPk());
            sSrvc.create(datastream1);
            DATASTREAMS.add(datastream1);
        }
        {
            Entity datastream2 = sMdl.newDatastream("Datastream LF", "The humidity of thing 1, sensor 2.", "someType", new UnitOfMeasurement("relative humidity", "%", "ucum:Humidity"));
            datastream2.setProperty(sMdl.npDatastreamThing, THINGS.get(0).withOnlyPk());
            datastream2.setProperty(sMdl.npDatastreamSensor, SENSORS.get(1).withOnlyPk());
            datastream2.setProperty(sMdl.npDatastreamObservedproperty, OPROPS.get(1).withOnlyPk());
            sSrvc.create(datastream2);
            DATASTREAMS.add(datastream2);
        }
    }

    /**
     * Tests if JSON-Patch is working on Things.
     *
     * @throws ServiceFailureException if the service connection fails.
     * @throws JsonPointerException if the patch is invalid.
     * @throws IOException if the patch is invalid.
     */
    @Test
    void jsonPatchThingTest() throws ServiceFailureException, JsonPointerException, IOException {
        LOGGER.info("  jsonPatchThingTest");
        Entity thingOnlyId = THINGS.get(0).withOnlyPk();
        List<JsonPatchOperation> operations = new ArrayList<>();
        operations.add(new AddOperation(new JsonPointer("/properties"), Utils.MAPPER.readTree("{\"key1\": 1}")));
        sSrvc.patch(thingOnlyId, operations);
        Entity updatedThing = sSrvc.dao(sMdl.etThing).find(thingOnlyId.getPrimaryKeyValues());

        String message = "properties/key1 was not added correctly.";
        assertEquals(1L, (Long) updatedThing.getProperty(EP_PROPERTIES).get("key1"), message);

        operations.clear();
        operations.add(new CopyOperation(new JsonPointer("/properties/key1"), new JsonPointer("/properties/keyCopy1")));
        operations.add(new MoveOperation(new JsonPointer("/properties/key1"), new JsonPointer("/properties/key2")));
        sSrvc.patch(thingOnlyId, operations);
        updatedThing = sSrvc.dao(sMdl.etThing).find(thingOnlyId.getPrimaryKeyValues());

        final MapValue updatedProperties = updatedThing.getProperty(EP_PROPERTIES);
        message = "properties/keyCopy1 does not exist after copy.";
        assertEquals(1L, (Long) updatedProperties.get("keyCopy1"), message);
        message = "properties/key1 still exists after move.";
        assertEquals(null, updatedProperties.get("key1"), message);
        message = "properties/key2 does not exist after move.";
        assertEquals(1L, (Long) updatedProperties.get("key2"), message);
    }

    @Test
    void jsonPatchThingNoOpTest() throws ServiceFailureException, JsonPointerException, IOException {
        LOGGER.info("  jsonPatchThingTest");
        Entity thingOnlyId = THINGS.get(0).withOnlyPk();
        List<JsonPatchOperation> operations = new ArrayList<>();
        operations.add(new AddOperation(new JsonPointer("/properties"), Utils.MAPPER.readTree("{\"key1\": 2}")));
        sSrvc.patch(thingOnlyId, operations);
        Entity updatedThing = sSrvc.dao(sMdl.etThing).find(thingOnlyId.getPrimaryKeyValues());

        String message = "properties/key1 was not added correctly.";
        assertEquals(2L, (Long) updatedThing.getProperty(EP_PROPERTIES).get("key1"), message);

        // This patch should result in no change.
        operations.clear();
        operations.add(new ReplaceOperation(new JsonPointer("/properties/key1"), new IntNode(2)));
        sSrvc.patch(thingOnlyId, operations);
        updatedThing = sSrvc.dao(sMdl.etThing).find(thingOnlyId.getPrimaryKeyValues());

        final MapValue updatedProperties = updatedThing.getProperty(EP_PROPERTIES);
        message = "properties/key1 does not have the correct value.";
        assertEquals(2L, updatedProperties.get("key1"), message);
    }

    /**
     * Tests if JSON-Patch is working on Datastreams.
     *
     * @throws ServiceFailureException if the service connection fails.
     * @throws JsonPointerException if the patch is invalid.
     * @throws IOException if the patch is invalid.
     */
    @Test
    void jsonPatchDatastreamTest() throws ServiceFailureException, JsonPointerException, IOException {
        LOGGER.info("  jsonPatchDatastreamTest");
        Entity dsOnlyId = DATASTREAMS.get(0).withOnlyPk();
        List<JsonPatchOperation> operations = new ArrayList<>();
        operations.add(new AddOperation(new JsonPointer("/properties"), Utils.MAPPER.readTree("{\"key1\": 1}")));
        sSrvc.patch(dsOnlyId, operations);
        Entity updatedDs = sSrvc.dao(sMdl.etDatastream).find(dsOnlyId.getPrimaryKeyValues());

        String message = "properties/key1 was not added correctly.";
        assertEquals(1L, (Long) updatedDs.getProperty(EP_PROPERTIES).get("key1"), message);

        operations.clear();
        operations.add(new CopyOperation(new JsonPointer("/properties/key1"), new JsonPointer("/properties/keyCopy1")));
        operations.add(new MoveOperation(new JsonPointer("/properties/key1"), new JsonPointer("/properties/key2")));
        sSrvc.patch(dsOnlyId, operations);
        updatedDs = sSrvc.dao(sMdl.etDatastream).find(dsOnlyId.getPrimaryKeyValues());

        final MapValue updatedProperties = updatedDs.getProperty(EP_PROPERTIES);
        message = "properties/keyCopy1 does not exist after copy.";
        assertEquals(1L, (Long) updatedProperties.get("keyCopy1"), message);
        message = "properties/key1 still exists after move.";
        assertEquals(null, updatedProperties.get("key1"), message);
        message = "properties/key2 does not exist after move.";
        assertEquals(1L, (Long) updatedProperties.get("key2"), message);
    }

}
