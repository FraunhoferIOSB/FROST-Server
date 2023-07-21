/*
 * Copyright (C) 2016 Hylke van der Schaaf.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library. If not, see <http://www.gnu.org/licenses/>.
 */
package de.fraunhofer.iosb.ilt.statests.c02cud;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.IntNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.JsonPointerException;
import com.github.fge.jsonpatch.AddOperation;
import com.github.fge.jsonpatch.CopyOperation;
import com.github.fge.jsonpatch.JsonPatchOperation;
import com.github.fge.jsonpatch.MoveOperation;
import com.github.fge.jsonpatch.ReplaceOperation;
import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.Location;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.geojson.Point;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hylke van der Schaaf
 */
public abstract class JsonPatchTests extends AbstractTestClass {

    public static class Implementation10 extends JsonPatchTests {

        public Implementation10() {
            super(ServerVersion.v_1_0);
        }

    }

    public static class Implementation11 extends JsonPatchTests {

        public Implementation11() {
            super(ServerVersion.v_1_1);
        }

    }

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonPatchTests.class);

    private static final List<Thing> THINGS = new ArrayList<>();
    private static final List<Location> LOCATIONS = new ArrayList<>();
    private static final List<Sensor> SENSORS = new ArrayList<>();
    private static final List<ObservedProperty> OPROPS = new ArrayList<>();
    private static final List<Datastream> DATASTREAMS = new ArrayList<>();

    public JsonPatchTests(ServerVersion version) {
        super(version);
    }

    @Override
    protected void setUpVersion() throws ServiceFailureException, URISyntaxException {
        LOGGER.info("Setting up for version {}.", version.urlPart);
        EntityUtils.deleteAll(version, serverSettings, service);
        createEntities();
    }

    @Override
    protected void tearDownVersion() throws ServiceFailureException {
        cleanup();
    }

    private static void cleanup() throws ServiceFailureException {
        EntityUtils.deleteAll(version, serverSettings, service);
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
     * @throws de.fraunhofer.iosb.ilt.sta.ServiceFailureException
     */
    @AfterAll
    public static void deleteEverything() throws ServiceFailureException {
        LOGGER.info("Tearing down.");
        cleanup();
    }

    private static void createEntities() throws ServiceFailureException, URISyntaxException {
        {
            Thing thing = new Thing("Thing 1", "The first thing.");
            service.create(thing);
            THINGS.add(thing);
        }
        {
            Location location = new Location("Location Des Dings von ILT", "First Location of Thing 1.", "application/vnd.geo+json", new Point(8, 49));
            location.getThings().add(THINGS.get(0));
            service.create(location);
            LOCATIONS.add(location);
        }
        {
            Sensor sensor1 = new Sensor("Sensor 1", "The first sensor.", "text", "Some metadata.");
            service.create(sensor1);
            SENSORS.add(sensor1);
        }
        {
            Sensor sensor2 = new Sensor("Sensor 2", "The second sensor", "text", "Some metadata.");
            service.create(sensor2);
            SENSORS.add(sensor2);
        }
        {
            ObservedProperty obsProp1 = new ObservedProperty("Temperature", new URI("http://ucom.org/temperature"), "The temperature of the thing.");
            service.create(obsProp1);
            OPROPS.add(obsProp1);
        }
        {
            ObservedProperty obsProp2 = new ObservedProperty("Humidity", new URI("http://ucom.org/humidity"), "The humidity of the thing.");
            service.create(obsProp2);
            OPROPS.add(obsProp2);
        }
        {
            Datastream datastream1 = new Datastream("Datastream Temp", "The temperature of thing 1, sensor 1.", "someType", new UnitOfMeasurement("degree celcius", "°C", "ucum:T"));
            datastream1.setThing(THINGS.get(0).withOnlyId());
            datastream1.setSensor(SENSORS.get(0).withOnlyId());
            datastream1.setObservedProperty(OPROPS.get(0).withOnlyId());
            service.create(datastream1);
            DATASTREAMS.add(datastream1);
        }
        {
            Datastream datastream2 = new Datastream("Datastream LF", "The humidity of thing 1, sensor 2.", "someType", new UnitOfMeasurement("relative humidity", "%", "ucum:Humidity"));
            datastream2.setThing(THINGS.get(0).withOnlyId());
            datastream2.setSensor(SENSORS.get(1).withOnlyId());
            datastream2.setObservedProperty(OPROPS.get(1).withOnlyId());
            service.create(datastream2);
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
        Thing thingOnlyId = THINGS.get(0).withOnlyId();
        List<JsonPatchOperation> operations = new ArrayList<>();
        operations.add(new AddOperation(new JsonPointer("/properties"), new ObjectMapper().readTree("{\"key1\": 1}")));
        service.patch(thingOnlyId, operations);
        Thing updatedThing = service.things().find(thingOnlyId.getId());

        String message = "properties/key1 was not added correctly.";
        assertEquals((Integer) 1, (Integer) updatedThing.getProperties().get("key1"), message);

        operations.clear();
        operations.add(new CopyOperation(new JsonPointer("/properties/key1"), new JsonPointer("/properties/keyCopy1")));
        operations.add(new MoveOperation(new JsonPointer("/properties/key1"), new JsonPointer("/properties/key2")));
        service.patch(thingOnlyId, operations);
        updatedThing = service.things().find(thingOnlyId.getId());

        message = "properties/keyCopy1 does not exist after copy.";
        assertEquals((Integer) 1, (Integer) updatedThing.getProperties().get("keyCopy1"), message);
        message = "properties/key1 still exists after move.";
        assertEquals(null, updatedThing.getProperties().get("key1"), message);
        message = "properties/key2 does not exist after move.";
        assertEquals((Integer) 1, (Integer) updatedThing.getProperties().get("key2"), message);
    }

    @Test
    void jsonPatchThingNoOpTest() throws ServiceFailureException, JsonPointerException, IOException {
        LOGGER.info("  jsonPatchThingNoOpTest");
        Thing thingOnlyId = THINGS.get(0).withOnlyId();
        List<JsonPatchOperation> operations = new ArrayList<>();
        operations.add(new AddOperation(new JsonPointer("/properties"), new ObjectMapper().readTree("{\"key1\": 2}")));
        service.patch(thingOnlyId, operations);
        Thing updatedThing = service.things().find(thingOnlyId.getId());

        String message = "properties/key1 was not added correctly.";
        assertEquals((Integer) 2, (Integer) updatedThing.getProperties().get("key1"), message);

        // This patch should result in no change.
        operations.clear();
        operations.add(new ReplaceOperation(new JsonPointer("/properties/key1"), new IntNode(2)));
        service.patch(thingOnlyId, operations);
        updatedThing = service.things().find(thingOnlyId.getId());

        message = "properties/key1 does not have the correct value.";
        assertEquals((Integer) 2, (Integer) updatedThing.getProperties().get("key1"), message);
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
        Datastream dsOnlyId = DATASTREAMS.get(0).withOnlyId();
        List<JsonPatchOperation> operations = new ArrayList<>();
        operations.add(new AddOperation(new JsonPointer("/properties"), new ObjectMapper().readTree("{\"key1\": 1}")));
        service.patch(dsOnlyId, operations);
        Datastream updatedDs = service.datastreams().find(dsOnlyId.getId());

        String message = "properties/key1 was not added correctly.";
        assertEquals((Integer) 1, (Integer) updatedDs.getProperties().get("key1"), message);

        operations.clear();
        operations.add(new CopyOperation(new JsonPointer("/properties/key1"), new JsonPointer("/properties/keyCopy1")));
        operations.add(new MoveOperation(new JsonPointer("/properties/key1"), new JsonPointer("/properties/key2")));
        service.patch(dsOnlyId, operations);
        updatedDs = service.datastreams().find(dsOnlyId.getId());

        message = "properties/keyCopy1 does not exist after copy.";
        assertEquals((Integer) 1, (Integer) updatedDs.getProperties().get("keyCopy1"), message);
        message = "properties/key1 still exists after move.";
        assertEquals(null, updatedDs.getProperties().get("key1"), message);
        message = "properties/key2 does not exist after move.";
        assertEquals((Integer) 1, (Integer) updatedDs.getProperties().get("key2"), message);
    }

}
