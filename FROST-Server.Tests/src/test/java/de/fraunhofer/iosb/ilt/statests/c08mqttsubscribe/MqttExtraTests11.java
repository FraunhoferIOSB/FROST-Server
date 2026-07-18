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
package de.fraunhofer.iosb.ilt.statests.c08mqttsubscribe;

import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.EP_PROPERTIES;
import static de.fraunhofer.iosb.ilt.frostclient.utils.CollectionsHelper.propertiesBuilder;
import static de.fraunhofer.iosb.ilt.statests.util.EntityUtils.createDatastream;
import static de.fraunhofer.iosb.ilt.statests.util.EntityUtils.createObservedProperty;
import static de.fraunhofer.iosb.ilt.statests.util.EntityUtils.createSensor;

import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.exception.StatusCodeException;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV11Sensing;
import de.fraunhofer.iosb.ilt.frostclient.models.ext.UnitOfMeasurement;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityHelper11;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper11;
import de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper11.MqttAction;
import de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper11.TestSubscription;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import org.geojson.LineString;
import org.geojson.LngLatAlt;
import org.geojson.Point;
import org.geojson.Polygon;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;

/**
 * Tests non-standard MQTT extensions.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public class MqttExtraTests11 extends AbstractTestClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttExtraTests11.class.getName());

    private static EntityHelper11 eh;
    private static MqttHelper11 mqttHelper;
    private static SensorThingsV11Sensing sMdl;

    public MqttExtraTests11() {
        super(ServerVersion.V_1_1);
    }

    @Override
    protected void setUpVersion() throws URISyntaxException, ServiceFailureException {
        LOGGER.info("Setting up for version {}.", version.urlPart);
        sMdl = sSrvc.getModel(SensorThingsV11Sensing.class);
        eh = new EntityHelper11(sSrvc);
        mqttHelper = new MqttHelper11(sSrvc, serverSettings.getMqttUrl(), serverSettings.getMqttTimeOutMs());
        try {
            createEntities();
        } catch (StatusCodeException ex) {
            LOGGER.error("Failed to create test entities: {}\n{}", ex.getStatusCode(), ex.getReturnedContent());
            Assertions.fail("Failed to create test entities");
        }
    }

    @AfterAll
    public static void tearDown() throws ServiceFailureException {
        LOGGER.info("Tearing down.");
        EntityUtils.deleteAll(sSrvc);
        eh.clearCaches();
        eh = null;
        mqttHelper = null;
    }

    private static void createEntities() throws ServiceFailureException, URISyntaxException {
        Entity thing = sMdl.newThing("Thing 1", "The first thing.");
        sSrvc.create(thing);
        eh.cache(thing);

        thing = sMdl.newThing("Thing 2", "The second thing.")
                .setProperty(EP_PROPERTIES, propertiesBuilder().addItem("field", 2).build());
        sSrvc.create(thing);
        eh.cache(thing);

        thing = sMdl.newThing("Thing 3", "The third thing.")
                .setProperty(EP_PROPERTIES, propertiesBuilder().addItem("field", 3).build());
        sSrvc.create(thing);
        eh.cache(thing);

        thing = sMdl.newThing("Thing 4", "The fourth thing.");
        sSrvc.create(thing);
        eh.cache(thing);

        // Locations 0
        Entity location = sMdl.newLocation("Location 1.0", "First Location of Thing 1.", "application/geo+json", new Point(8, 51))
                .setProperty(EP_PROPERTIES, propertiesBuilder().addItem("field", 1).build())
                .addNavigationEntity(sMdl.npLocationThings, eh.getCache(sMdl.etThing, 0));
        sSrvc.create(location);
        eh.cache(location);

        // Locations 1
        location = sMdl.newLocation("Location 1.1", "Second Location of Thing 1.", "application/geo+json", new Point(8, 52))
                .setProperty(EP_PROPERTIES, propertiesBuilder().addItem("field", 1.1).build())
                .addNavigationEntity(sMdl.npLocationThings, eh.getCache(sMdl.etThing, 0));
        sSrvc.create(location);
        eh.cache(location);

        // Locations 2
        location = sMdl.newLocation("Location 2", "Location of Thing 2.", "application/geo+json", new Point(8, 53))
                .setProperty(EP_PROPERTIES, propertiesBuilder().addItem("field", 2).build())
                .addNavigationEntity(sMdl.npLocationThings, eh.getCache(sMdl.etThing, 1));
        sSrvc.create(location);
        eh.cache(location);

        // Locations 3
        location = sMdl.newLocation("Location 3", "Location of Thing 3.", "application/geo+json", new Point(8, 54))
                .setProperty(EP_PROPERTIES, propertiesBuilder().addItem("field", 3).build())
                .addNavigationEntity(sMdl.npLocationThings, eh.getCache(sMdl.etThing, 2));
        sSrvc.create(location);
        eh.cache(location);

        // Locations 4
        location = sMdl.newLocation("Location 4", "Location of Thing 4.", "application/geo+json",
                new Polygon(
                        new LngLatAlt(8, 53),
                        new LngLatAlt(7, 52),
                        new LngLatAlt(7, 53),
                        new LngLatAlt(8, 53)))
                .setProperty(EP_PROPERTIES, propertiesBuilder().addItem("field", 4).build())
                .addNavigationEntity(sMdl.npLocationThings, eh.getCache(sMdl.etThing, 3));
        sSrvc.create(location);
        eh.cache(location);

        // Locations 5
        location = sMdl.newLocation("Location 5", "A line.", "application/geo+json",
                new LineString(
                        new LngLatAlt(5, 52),
                        new LngLatAlt(5, 53)))
                .setProperty(EP_PROPERTIES, propertiesBuilder().addItem("field", 5).build());
        sSrvc.create(location);
        eh.cache(location);

        // Locations 6
        location = sMdl.newLocation("Location 6", "A longer line.", "application/geo+json",
                new LineString(
                        new LngLatAlt(5, 52),
                        new LngLatAlt(6, 53)))
                .setProperty(EP_PROPERTIES, propertiesBuilder().addItem("field", 6).build());
        sSrvc.create(location);
        eh.cache(location);

        // Locations 7
        location = sMdl.newLocation("Location 7", "The longest line.", "application/geo+json",
                new LineString(
                        new LngLatAlt(4, 52),
                        new LngLatAlt(8, 52)))
                .setProperty(EP_PROPERTIES, propertiesBuilder().addItem("field", 7).build());
        sSrvc.create(location);
        eh.cache(location);

        final List<Entity> sensors = eh.getCache(sMdl.etSensor);
        createSensor(sSrvc, "Sensor 0", "The sensor with idx 0.", "text", "Some metadata.", sensors);
        createSensor(sSrvc, "Sensor 1", "The sensor with idx 1.", "text", "Some metadata.", sensors);
        createSensor(sSrvc, "Sensor 2", "The sensor with idx 2.", "text", "Some metadata.", sensors);
        createSensor(sSrvc, "Sensor 3", "The sensor with idx 3.", "text", "Some metadata.", sensors);

        final List<Entity> oProps = eh.getCache(sMdl.etObservedProperty);
        createObservedProperty(sSrvc, "ObservedProperty 0", "http://ucom.org/temperature", "ObservedProperty with index 0.", oProps);
        createObservedProperty(sSrvc, "ObservedProperty 1", "http://ucom.org/humidity", "ObservedProperty with index 1.", oProps);
        createObservedProperty(sSrvc, "ObservedProperty 2", "http://ucom.org/pressure", "ObservedProperty with index 2.", oProps);
        createObservedProperty(sSrvc, "ObservedProperty 3", "http://ucom.org/turbidity", "ObservedProperty with index 3.", oProps);

        final List<Entity> things = eh.getCache(sMdl.etThing);
        final List<Entity> datastreams = eh.getCache(sMdl.etDatastream);
        UnitOfMeasurement uomTemp = new UnitOfMeasurement("degree celcius", "°C", "ucum:T");
        createDatastream(sSrvc, "Datastream 0", "Datastream 1 of thing 0, sensor 0.", "someType", uomTemp, things.get(0), sensors.get(0), oProps.get(0), datastreams);
        createDatastream(sSrvc, "Datastream 1", "Datastream 2 of thing 0, sensor 1.", "someType", uomTemp, things.get(0), sensors.get(1), oProps.get(1), datastreams);
        createDatastream(sSrvc, "Datastream 2", "Datastream 3 of thing 0, sensor 2.", "someType", uomTemp, things.get(0), sensors.get(2), oProps.get(2), datastreams);
        createDatastream(sSrvc, "Datastream 3", "Datastream 1 of thing 1, sensor 0.", "someType", uomTemp, things.get(1), sensors.get(0), oProps.get(0), datastreams);
        createDatastream(sSrvc, "Datastream 4", "Datastream 2 of thing 1, sensor 1.", "someType", uomTemp, things.get(1), sensors.get(1), oProps.get(1), datastreams);
        createDatastream(sSrvc, "Datastream 5", "Datastream 3 of thing 1, sensor 3.", "someType", uomTemp, things.get(1), sensors.get(3), oProps.get(3), datastreams);
        createDatastream(sSrvc, "Datastream 6", "Datastream 1 of thing 2, sensor 3.", "someType", uomTemp, things.get(2), sensors.get(1), oProps.get(0), datastreams);
    }

    @Test
    void test01SubscribeObservationBase() {
        LOGGER.info("  test01SubscribeObservationBase");
        final CompletableFuture<Entity> obsFuture = new CompletableFuture<>();
        final Callable<Object> insertAction = () -> {
            Entity obs = EntityUtils.createObservation(
                    sSrvc,
                    eh.getCache(sMdl.etDatastream, 0),
                    0,
                    ZonedDateTime.parse("2016-01-01T01:00:00.000Z"),
                    eh.getCache(sMdl.etObservation));
            obsFuture.complete(obs);
            return null;
        };
        final TestSubscription testSubscription = new TestSubscription(mqttHelper, "v1.1/Observations")
                .addExpectedEntity(obsFuture)
                .createReceivedListener(sMdl.etObservation);
        MqttAction mqttAction = new MqttAction(insertAction)
                .add(testSubscription);
        mqttHelper.executeRequest(mqttAction);
        Assertions.assertTrue(mqttAction.isAllOk());
    }

    @Test
    void test02SubscribeObservationResultFilter() {
        LOGGER.info("  test02SubscribeObservationResultFilter");
        final CompletableFuture<Entity> obsFuture1 = new CompletableFuture<>();
        final CompletableFuture<Entity> obsFuture2 = new CompletableFuture<>();
        final CompletableFuture<Entity> obsFuture3 = new CompletableFuture<>();
        final Callable<Object> insertAction = () -> {
            Entity obs1 = EntityUtils.createObservation(sSrvc,
                    eh.getCache(sMdl.etDatastream, 0),
                    10,
                    ZonedDateTime.parse("2016-01-01T01:00:00.000Z"),
                    eh.getCache(sMdl.etObservation));
            obsFuture1.complete(obs1);
            Entity obs2 = EntityUtils.createObservation(sSrvc,
                    eh.getCache(sMdl.etDatastream, 0),
                    5,
                    ZonedDateTime.parse("2016-01-01T01:00:00.000Z"),
                    eh.getCache(sMdl.etObservation));
            obsFuture2.complete(obs2);
            Entity obs3 = EntityUtils.createObservation(sSrvc,
                    eh.getCache(sMdl.etDatastream, 0),
                    0,
                    ZonedDateTime.parse("2016-01-01T01:00:00.000Z"),
                    eh.getCache(sMdl.etObservation));
            obsFuture3.complete(obs3);
            return null;
        };
        final TestSubscription testSubscription1 = new TestSubscription(mqttHelper, "v1.1/Observations?$filter=result gt 4")
                .addExpectedEntity(obsFuture1)
                .addExpectedEntity(obsFuture2)
                .createReceivedListener(sMdl.etObservation);
        final TestSubscription testSubscription2 = new TestSubscription(mqttHelper, "v1.1/Observations?$filter=result gt 5")
                .addExpectedEntity(obsFuture1)
                .createReceivedListener(sMdl.etObservation);
        final TestSubscription testSubscription3 = new TestSubscription(mqttHelper, "v1.1/Observations?$filter=result ge 5")
                .addExpectedEntity(obsFuture1)
                .addExpectedEntity(obsFuture2)
                .createReceivedListener(sMdl.etObservation);
        MqttAction mqttAction = new MqttAction(insertAction)
                .add(testSubscription1)
                .add(testSubscription2)
                .add(testSubscription3);
        mqttHelper.executeRequest(mqttAction);
        Assertions.assertTrue(mqttAction.isAllOk());
    }

    @Test
    void test03SubscribeObservationFoIFilter() {
        LOGGER.info("  test03SubscribeObservationFoIFilter");
        final CompletableFuture<Entity> obsFuture1 = new CompletableFuture<>();
        final CompletableFuture<Entity> obsFuture2 = new CompletableFuture<>();
        final CompletableFuture<Entity> obsFuture3 = new CompletableFuture<>();
        final Callable<Object> insertAction = () -> {
            Entity obs1 = EntityUtils.createObservation(sSrvc, eh.getCache(sMdl.etDatastream, 0),
                    1, ZonedDateTime.parse("2016-01-01T01:00:00.000Z"),
                    eh.getCache(sMdl.etObservation));
            obsFuture1.complete(obs1);
            Entity obs2 = EntityUtils.createObservation(sSrvc, eh.getCache(sMdl.etDatastream, 3),
                    2, ZonedDateTime.parse("2016-01-01T01:00:00.000Z"),
                    eh.getCache(sMdl.etObservation));
            obsFuture2.complete(obs2);
            Entity obs3 = EntityUtils.createObservation(sSrvc, eh.getCache(sMdl.etDatastream, 6),
                    3, ZonedDateTime.parse("2016-01-01T01:00:00.000Z"),
                    eh.getCache(sMdl.etObservation));
            obsFuture3.complete(obs3);
            return null;
        };
        final TestSubscription testSubscription1 = new TestSubscription(mqttHelper, "v1.1/Observations?$filter=startswith(FeatureOfInterest/name,'Location 1')")
                .addExpectedEntity(obsFuture1)
                .createReceivedListener(sMdl.etObservation);
        final TestSubscription testSubscription2 = new TestSubscription(mqttHelper, "v1.1/Observations?$filter=startswith(FeatureOfInterest/name,'Location 2')")
                .addExpectedEntity(obsFuture2)
                .createReceivedListener(sMdl.etObservation);
        final TestSubscription testSubscription3 = new TestSubscription(mqttHelper, "v1.1/Observations?$filter=startswith(FeatureOfInterest/name,'Location 3')")
                .addExpectedEntity(obsFuture3)
                .createReceivedListener(sMdl.etObservation);
        MqttAction mqttAction = new MqttAction(insertAction)
                .add(testSubscription1)
                .add(testSubscription2)
                .add(testSubscription3);
        mqttHelper.executeRequest(mqttAction);
        Assertions.assertTrue(mqttAction.isAllOk());
    }

    @Test
    void test04SubscribeExpand() {
        LOGGER.info("  test04SubscribeExpand");
        final CompletableFuture<JsonNode> obsFuture1 = new CompletableFuture<>();
        final CompletableFuture<JsonNode> obsFuture2 = new CompletableFuture<>();
        final CompletableFuture<JsonNode> obsFuture3 = new CompletableFuture<>();
        final CompletableFuture<JsonNode> obsFuture4 = new CompletableFuture<>();
        final Callable<Object> insertAction = () -> {
            Entity obs = EntityUtils.createObservation(
                    sSrvc,
                    eh.getCache(sMdl.etDatastream, 0),
                    0,
                    ZonedDateTime.parse("2016-01-01T01:00:00.000Z"),
                    eh.getCache(sMdl.etObservation));
            JsonNode entityJson1 = eh.getEntityJson(
                    sMdl.etObservation,
                    obs.getPrimaryKeyValues(),
                    eh.selectAllWithId(sMdl.etObservation),
                    "Datastream($select=description)");
            obsFuture1.complete(entityJson1);
            JsonNode entityJson2 = eh.getEntityJson(
                    sMdl.etObservation,
                    obs.getPrimaryKeyValues(),
                    eh.selectAllWithId(sMdl.etObservation),
                    "Datastream");
            obsFuture2.complete(entityJson2);
            JsonNode entityJson3 = eh.getEntityJson(
                    sMdl.etObservation,
                    obs.getPrimaryKeyValues(),
                    Arrays.asList(new String[]{"id", "result"}),
                    "FeatureOfInterest($select=feature)");
            obsFuture3.complete(entityJson3);
            JsonNode entityJson4 = eh.getEntityJson(sMdl.etObservation, obs.getPrimaryKeyValues(), null, Arrays.asList("result"), "FeatureOfInterest($select=feature)", null);
            obsFuture4.complete(entityJson4);
            return null;
        };
        final TestSubscription testSubscription1 = new TestSubscription(mqttHelper, "v1.1/Observations?$expand=Datastream($select=description)")
                .addExpectedJson(obsFuture1)
                .createReceivedListener(sMdl.etObservation);
        final TestSubscription testSubscription2 = new TestSubscription(mqttHelper, "v1.1/Observations?$expand=Datastream")
                .addExpectedJson(obsFuture2)
                .createReceivedListener(sMdl.etObservation);
        final TestSubscription testSubscription3 = new TestSubscription(mqttHelper, "v1.1/Observations?$select=id,result&$expand=FeatureOfInterest($select=feature)")
                .addExpectedJson(obsFuture3)
                .createReceivedListener(sMdl.etObservation);
        final TestSubscription testSubscription4 = new TestSubscription(mqttHelper, "v1.1/Observations?$select=result&$expand=FeatureOfInterest($select=feature)")
                .addExpectedJson(obsFuture4)
                .createReceivedListener(sMdl.etObservation);
        MqttAction mqttAction = new MqttAction(insertAction)
                .add(testSubscription1)
                .add(testSubscription2)
                .add(testSubscription3)
                .add(testSubscription4);
        mqttHelper.executeRequest(mqttAction);
        Assertions.assertTrue(mqttAction.isAllOk());
    }

}
