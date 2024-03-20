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
package de.fraunhofer.iosb.ilt.statests.c08mqttsubscribe;

import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsSensingV11.EP_PROPERTIES;
import static de.fraunhofer.iosb.ilt.frostclient.utils.CollectionsHelper.propertiesBuilder;
import static de.fraunhofer.iosb.ilt.statests.util.EntityUtils.createDatastream;
import static de.fraunhofer.iosb.ilt.statests.util.EntityUtils.createObservedProperty;
import static de.fraunhofer.iosb.ilt.statests.util.EntityUtils.createSensor;

import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityHelper;
import de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.geojson.Polygon;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests non-standard MQTT extensions
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public abstract class MqttExtraTests extends AbstractTestClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttExtraTests.class.getName());

    private static final List<Entity> THINGS = new ArrayList<>();
    private static final List<Entity> LOCATIONS = new ArrayList<>();
    private static final List<Entity> SENSORS = new ArrayList<>();
    private static final List<Entity> O_PROPS = new ArrayList<>();
    private static final List<Entity> DATASTREAMS = new ArrayList<>();
    private static final List<Entity> OBSERVATIONS = new ArrayList<>();

    private static EntityHelper entityHelper;
    private static MqttHelper mqttHelper;

    public MqttExtraTests(ServerVersion serverVersion) {
        super(serverVersion);
    }

    @Override
    protected void setUpVersion() throws ServiceFailureException, URISyntaxException {
        LOGGER.info("Setting up for version {}.", version.urlPart);
        entityHelper = new EntityHelper(version, serverSettings);
        mqttHelper = new MqttHelper(version, serverSettings.getMqttUrl(), serverSettings.getMqttTimeOut());
        createEntities();
    }

    @Override
    protected void tearDownVersion() {
        entityHelper.deleteEverything();
        entityHelper = null;
        mqttHelper = null;
    }

    @AfterAll
    public static void tearDown() {
        LOGGER.info("Tearing down.");
        entityHelper.deleteEverything();
        entityHelper = null;
        mqttHelper = null;
    }

    private static void createEntities() throws ServiceFailureException, URISyntaxException {
        Entity thing = sMdl.newThing("Thing 1", "The first thing.");
        sSrvc.create(thing);
        THINGS.add(thing);

        thing = sMdl.newThing("Thing 2", "The second thing.")
                .setProperty(EP_PROPERTIES, propertiesBuilder().addItem("field", 2).build());
        sSrvc.create(thing);
        THINGS.add(thing);

        thing = sMdl.newThing("Thing 3", "The third thing.")
                .setProperty(EP_PROPERTIES, propertiesBuilder().addItem("field", 3).build());
        sSrvc.create(thing);
        THINGS.add(thing);

        thing = sMdl.newThing("Thing 4", "The fourth thing.");
        sSrvc.create(thing);
        THINGS.add(thing);

        // Locations 0
        Entity location = sMdl.newLocation("Location 1.0", "First Location of Thing 1.", "application/vnd.geo+json", new Point(8, 51))
                .setProperty(EP_PROPERTIES, propertiesBuilder().addItem("field", 1).build())
                .addNavigationEntity(sMdl.npLocationThings, THINGS.get(0));
        sSrvc.create(location);
        LOCATIONS.add(location);

        // Locations 1
        location = sMdl.newLocation("Location 1.1", "Second Location of Thing 1.", "application/vnd.geo+json", new Point(8, 52))
                .setProperty(EP_PROPERTIES, propertiesBuilder().addItem("field", 1.1).build())
                .addNavigationEntity(sMdl.npLocationThings, THINGS.get(0));
        sSrvc.create(location);
        LOCATIONS.add(location);

        // Locations 2
        location = sMdl.newLocation("Location 2", "Location of Thing 2.", "application/vnd.geo+json", new Point(8, 53))
                .setProperty(EP_PROPERTIES, propertiesBuilder().addItem("field", 2).build())
                .addNavigationEntity(sMdl.npLocationThings, THINGS.get(1));
        sSrvc.create(location);
        LOCATIONS.add(location);

        // Locations 3
        location = sMdl.newLocation("Location 3", "Location of Thing 3.", "application/vnd.geo+json", new Point(8, 54))
                .setProperty(EP_PROPERTIES, propertiesBuilder().addItem("field", 3).build())
                .addNavigationEntity(sMdl.npLocationThings, THINGS.get(2));
        sSrvc.create(location);
        LOCATIONS.add(location);

        // Locations 4
        location = sMdl.newLocation("Location 4", "Location of Thing 4.", "application/vnd.geo+json",
                new Polygon(
                        new LngLatAlt(8, 53),
                        new LngLatAlt(7, 52),
                        new LngLatAlt(7, 53),
                        new LngLatAlt(8, 53)))
                .setProperty(EP_PROPERTIES, propertiesBuilder().addItem("field", 4).build())
                .addNavigationEntity(sMdl.npLocationThings, THINGS.get(3));
        sSrvc.create(location);
        LOCATIONS.add(location);

        // Locations 5
        location = sMdl.newLocation("Location 5", "A line.", "application/vnd.geo+json",
                new LineString(
                        new LngLatAlt(5, 52),
                        new LngLatAlt(5, 53)))
                .setProperty(EP_PROPERTIES, propertiesBuilder().addItem("field", 5).build());
        sSrvc.create(location);
        LOCATIONS.add(location);

        // Locations 6
        location = sMdl.newLocation("Location 6", "A longer line.", "application/vnd.geo+json",
                new LineString(
                        new LngLatAlt(5, 52),
                        new LngLatAlt(6, 53)))
                .setProperty(EP_PROPERTIES, propertiesBuilder().addItem("field", 6).build());
        sSrvc.create(location);
        LOCATIONS.add(location);

        // Locations 7
        location = sMdl.newLocation("Location 7", "The longest line.", "application/vnd.geo+json",
                new LineString(
                        new LngLatAlt(4, 52),
                        new LngLatAlt(8, 52)))
                .setProperty(EP_PROPERTIES, propertiesBuilder().addItem("field", 7).build());
        sSrvc.create(location);
        LOCATIONS.add(location);

        createSensor(sSrvc, "Sensor 0", "The sensor with idx 0.", "text", "Some metadata.", SENSORS);
        createSensor(sSrvc, "Sensor 1", "The sensor with idx 1.", "text", "Some metadata.", SENSORS);
        createSensor(sSrvc, "Sensor 2", "The sensor with idx 2.", "text", "Some metadata.", SENSORS);
        createSensor(sSrvc, "Sensor 3", "The sensor with idx 3.", "text", "Some metadata.", SENSORS);

        createObservedProperty(sSrvc, "ObservedProperty 0", "http://ucom.org/temperature", "ObservedProperty with index 0.", O_PROPS);
        createObservedProperty(sSrvc, "ObservedProperty 1", "http://ucom.org/humidity", "ObservedProperty with index 1.", O_PROPS);
        createObservedProperty(sSrvc, "ObservedProperty 2", "http://ucom.org/pressure", "ObservedProperty with index 2.", O_PROPS);
        createObservedProperty(sSrvc, "ObservedProperty 3", "http://ucom.org/turbidity", "ObservedProperty with index 3.", O_PROPS);

        UnitOfMeasurement uomTemp = new UnitOfMeasurement("degree celcius", "°C", "ucum:T");

        createDatastream(sSrvc, "Datastream 0", "Datastream 1 of thing 0, sensor 0.", "someType", uomTemp, THINGS.get(0), SENSORS.get(0), O_PROPS.get(0), DATASTREAMS);
        createDatastream(sSrvc, "Datastream 1", "Datastream 2 of thing 0, sensor 1.", "someType", uomTemp, THINGS.get(0), SENSORS.get(1), O_PROPS.get(1), DATASTREAMS);
        createDatastream(sSrvc, "Datastream 2", "Datastream 3 of thing 0, sensor 2.", "someType", uomTemp, THINGS.get(0), SENSORS.get(2), O_PROPS.get(2), DATASTREAMS);
        createDatastream(sSrvc, "Datastream 3", "Datastream 1 of thing 1, sensor 0.", "someType", uomTemp, THINGS.get(1), SENSORS.get(0), O_PROPS.get(0), DATASTREAMS);
        createDatastream(sSrvc, "Datastream 4", "Datastream 2 of thing 1, sensor 1.", "someType", uomTemp, THINGS.get(1), SENSORS.get(1), O_PROPS.get(1), DATASTREAMS);
        createDatastream(sSrvc, "Datastream 5", "Datastream 3 of thing 1, sensor 3.", "someType", uomTemp, THINGS.get(1), SENSORS.get(3), O_PROPS.get(3), DATASTREAMS);
        createDatastream(sSrvc, "Datastream 6", "Datastream 1 of thing 2, sensor 3.", "someType", uomTemp, THINGS.get(2), SENSORS.get(1), O_PROPS.get(0), DATASTREAMS);
    }

}
