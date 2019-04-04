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
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.JsonPointerException;
import com.github.fge.jsonpatch.AddOperation;
import com.github.fge.jsonpatch.CopyOperation;
import com.github.fge.jsonpatch.JsonPatchOperation;
import com.github.fge.jsonpatch.MoveOperation;
import de.fraunhofer.iosb.ilt.sta.ServiceFailureException;
import de.fraunhofer.iosb.ilt.sta.model.Datastream;
import de.fraunhofer.iosb.ilt.sta.model.Location;
import de.fraunhofer.iosb.ilt.sta.model.ObservedProperty;
import de.fraunhofer.iosb.ilt.sta.model.Sensor;
import de.fraunhofer.iosb.ilt.sta.model.Thing;
import de.fraunhofer.iosb.ilt.sta.model.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.sta.service.SensorThingsService;
import de.fraunhofer.iosb.ilt.statests.TestSuite;
import de.fraunhofer.iosb.ilt.statests.ServerSettings;
import de.fraunhofer.iosb.ilt.statests.util.Utils;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.geojson.Point;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Hylke van der Schaaf
 */
public class JsonPatchTests {

    private static final List<Thing> THINGS = new ArrayList<>();
    private static final List<Location> LOCATIONS = new ArrayList<>();
    private static final List<Sensor> SENSORS = new ArrayList<>();
    private static final List<ObservedProperty> OPROPS = new ArrayList<>();
    private static final List<Datastream> DATASTREAMS = new ArrayList<>();

    private static ServerSettings serverSettings;
    private static SensorThingsService service;

    /**
     * This method will be run before starting the test for this conformance
     * class.It cleans the database to start test.
     *
     * @throws java.net.MalformedURLException If the url passed to the test is
     * bad.
     * @throws de.fraunhofer.iosb.ilt.sta.ServiceFailureException If the service
     * has an issue.
     * @throws java.net.URISyntaxException If the url passed to the test is bad.
     */
    @BeforeClass
    public static void setUp() throws MalformedURLException, ServiceFailureException, URISyntaxException {
        TestSuite suite = TestSuite.getInstance();
        serverSettings = suite.getServerSettings();
        service = new SensorThingsService(new URL(serverSettings.serviceUrl));
        deleteEverything();
        createEntities();
    }

    /**
     * This method is run after all the tests of this class is run and clean the
     * database.
     *
     * @throws de.fraunhofer.iosb.ilt.sta.ServiceFailureException
     */
    @AfterClass
    public static void deleteEverything() throws ServiceFailureException {
        Utils.deleteAll(service);
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
     * Tests if JSON-Patch is working.
     *
     * @throws ServiceFailureException if the service connection fails.
     */
    @Test
    public void jsonPatchTest() throws ServiceFailureException, JsonPointerException, IOException {
        Thing thingOnlyId = THINGS.get(0).withOnlyId();
        List<JsonPatchOperation> operations = new ArrayList<>();
        operations.add(new AddOperation(new JsonPointer("/properties"), new ObjectMapper().readTree("{\"key1\": 1}")));
        service.patch(thingOnlyId, operations);
        Thing updatedThing = service.things().find(thingOnlyId.getId());

        String message = "properties/key1 was not added correctly.";
        Assert.assertEquals(message, (Integer) 1, (Integer) updatedThing.getProperties().get("key1"));

        operations.clear();
        operations.add(new CopyOperation(new JsonPointer("/properties/key1"), new JsonPointer("/properties/keyCopy1")));
        operations.add(new MoveOperation(new JsonPointer("/properties/key1"), new JsonPointer("/properties/key2")));
        service.patch(thingOnlyId, operations);
        updatedThing = service.things().find(thingOnlyId.getId());

        message = "properties/keyCopy1 does not exist after copy.";
        Assert.assertEquals(message, (Integer) 1, (Integer) updatedThing.getProperties().get("keyCopy1"));
        message = "properties/key1 still exists after move.";
        Assert.assertEquals(message, null, updatedThing.getProperties().get("key1"));
        message = "properties/key2 does not exist after move.";
        Assert.assertEquals(message, (Integer) 1, (Integer) updatedThing.getProperties().get("key2"));

    }

}
