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

import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsSensingV11.NAME_DATASTREAM;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsSensingV11.NAME_FEATUREOFINTEREST;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsSensingV11.NAME_HISTORICALLOCATION;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsSensingV11.NAME_LOCATION;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsSensingV11.NAME_OBSERVATION;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsSensingV11.NAME_OBSERVEDPROPERTY;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsSensingV11.NAME_SENSOR;
import static de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsSensingV11.NAME_THING;
import static de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper2.WAIT_AFTER_CLEANUP;
import static de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper2.waitMillis;

import com.fasterxml.jackson.databind.JsonNode;
import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.model.EntitySet;
import de.fraunhofer.iosb.ilt.frostclient.model.EntitySetImpl;
import de.fraunhofer.iosb.ilt.frostclient.model.EntityType;
import de.fraunhofer.iosb.ilt.frostclient.model.property.NavigationProperty;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityHelper2;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper2;
import de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper2.MqttAction;
import de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper2.TestSubscription;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test the standard MQTT functionality.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public class MqttCoreTests extends AbstractTestClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttCoreTests.class);

    private static List<EntityType> entityTypesForCreate;

    private static EntityHelper2 eh2;
    private static MqttHelper2 mqttHelper;

    public MqttCoreTests() {
        super(ServerVersion.v_1_1);
    }

    @Override
    protected void setUpVersion() throws ServiceFailureException, URISyntaxException {
        LOGGER.info("Setting up for version {}.", version.urlPart);
        eh2 = new EntityHelper2(sSrvc);
        mqttHelper = new MqttHelper2(sSrvc, serverSettings.getMqttUrl(), serverSettings.getMqttTimeOutMs());
        entityTypesForCreate = Arrays.asList(
                sMdl.etThing,
                sMdl.etLocation,
                sMdl.etSensor,
                sMdl.etObservedProperty,
                sMdl.etFeatureOfInterest,
                sMdl.etDatastream,
                sMdl.etObservation,
                sMdl.etHistoricalLocation);
    }

    @Override
    protected void tearDownVersion() throws ServiceFailureException {
        cleanup();
    }

    /**
     * This method is run after all the tests of this class is run and clean the
     * database.
     *
     * @throws ServiceFailureException if cleaning up fails,
     */
    @AfterAll
    public static void tearDown() throws ServiceFailureException {
        LOGGER.info("Tearing down.");
        cleanup();
    }

    public static void cleanup() throws ServiceFailureException {
        EntityUtils.deleteAll(service);
        eh2.clearCaches();
        eh2 = null;
        mqttHelper = null;

    }

    private void deleteCreatedEntities() throws ServiceFailureException {
        EntityUtils.deleteAll(service);
        eh2.clearCaches();
    }

    @Test
    void check01a_SubscribeToEntitySetInsert() throws ServiceFailureException {
        LOGGER.info("  check01a_SubscribeToEntitySetInsert");
        deleteCreatedEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        for (var entityType : entityTypesForCreate) {
            LOGGER.info("    {}", entityType);
            final QueryJsonFuture future = QueryJsonFuture.build();
            final Callable<Object> insertAction = getInsertAction(entityType, future);
            final TestSubscription testSubscription = new TestSubscription(mqttHelper, "v1.1/" + entityType.mainContainer)
                    .addExpectedJson(future.getFuture())
                    .createReceivedListener(entityType);
            final MqttAction mqttAction = new MqttAction(insertAction)
                    .add(testSubscription);
            mqttHelper.executeRequest(mqttAction);
        }
    }

    @Test
    void check01b_SubscribeAutoFeatureCreation() throws ServiceFailureException {
        LOGGER.info("  check01b_SubscribeAutoFeatureCreation");

        // Now check if an Observation insert creates a new FoI and posts it over MQTT.
        LOGGER.debug("    FoI creation");
        EntityUtils.deleteAll(sSrvc.dao(sMdl.etFeatureOfInterest));
        EntityUtils.deleteAll(sSrvc.dao(sMdl.etObservation));
        eh2.getCache(sMdl.etObservation).clear();

        final CompletableFuture<JsonNode> futureObs = new CompletableFuture<>();
        final CompletableFuture<JsonNode> futureFoi = new CompletableFuture<>();
        final Callable<Object> insertAction = getInsertActionObs(futureObs, futureFoi);

        final TestSubscription testSubscription1 = new TestSubscription(mqttHelper, "v1.1/Observations")
                .addExpectedJson(futureObs)
                .createReceivedListener(sMdl.etObservation);

        final TestSubscription testSubscription2 = new TestSubscription(mqttHelper, "v1.1/FeaturesOfInterest")
                .addExpectedJson(futureFoi)
                .createReceivedListener(sMdl.etFeatureOfInterest);

        final MqttAction mqttAction = new MqttAction(insertAction)
                .add(testSubscription1)
                .add(testSubscription2);
        mqttHelper.executeRequest(mqttAction);

    }

    @Test
    void check02_SubscribeToEntitySetUpdatePATCH() throws ServiceFailureException {
        LOGGER.info("  check02SubscribeToEntitySetUpdatePATCH");
        deleteCreatedEntities();
        createEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        for (var entityType : entityTypesForCreate) {
            LOGGER.info("    {}", entityType);
            final QueryJsonFuture future = QueryJsonFuture.build();
            final Callable<Object> updateAction = getPatchUpdateAction(entityType, future);
            final TestSubscription testSubscription = new TestSubscription(mqttHelper, "v1.1/" + entityType.mainContainer)
                    .addExpectedJson(future.getFuture())
                    .createReceivedListener(entityType);
            final MqttAction mqttAction = new MqttAction(updateAction)
                    .add(testSubscription);
            mqttHelper.executeRequest(mqttAction);
        }
    }

    @Test
    void check03_SubscribeToEntitySetUpdatePUT() throws ServiceFailureException {
        LOGGER.info("  check03_SubscribeToEntitySetUpdatePUT");
        deleteCreatedEntities();
        createEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        for (var entityType : entityTypesForCreate) {
            LOGGER.info("    {}", entityType);
            final QueryJsonFuture future = QueryJsonFuture.build();
            final Callable<Object> updateAction = getPutUpdateAction(entityType, future);
            final TestSubscription testSubscription = new TestSubscription(mqttHelper, "v1.1/" + entityType.mainContainer)
                    .addExpectedJson(future.getFuture())
                    .createReceivedListener(entityType);
            final MqttAction mqttAction = new MqttAction(updateAction)
                    .add(testSubscription);
            mqttHelper.executeRequest(mqttAction);
        }
    }

    @Test
    void check04_SubscribeToEntitySetWithMultipleSelectInsert() throws ServiceFailureException {
        LOGGER.info("  check04_SubscribeToEntitySetWithMultipleSelectInsert");
        deleteCreatedEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        for (var entityType : entityTypesForCreate) {
            LOGGER.info("    {}", entityType);

            final QueryJsonFuture futureEven = QueryJsonFuture.build()
                    .setSelect(eh2.getSelectedProperties(entityType, true));
            final QueryJsonFuture futureOdd = QueryJsonFuture.build()
                    .setSelect(eh2.getSelectedProperties(entityType, false));
            final Callable<Object> insertAction = getInsertAction(entityType, futureEven, futureOdd);
            final TestSubscription evenSubscription = new TestSubscription(mqttHelper)
                    .setTopic("v1.1/" + entityType.mainContainer + '?' + eh2.createSelect(futureEven.getSelect()))
                    .addExpectedJson(futureEven.getFuture())
                    .createReceivedListener(entityType);
            final TestSubscription oddSubscription = new TestSubscription(mqttHelper)
                    .setTopic("v1.1/" + entityType.mainContainer + '?' + eh2.createSelect(futureOdd.getSelect()))
                    .addExpectedJson(futureOdd.getFuture())
                    .createReceivedListener(entityType);
            final MqttAction mqttAction = new MqttAction(insertAction)
                    .add(evenSubscription)
                    .add(oddSubscription);
            mqttHelper.executeRequest(mqttAction);
        }
    }

    @Test
    void check05_SubscribeToEntitySetWithMultipleSelectUpdatePATCH() throws ServiceFailureException {
        LOGGER.info("  check05_SubscribeToEntitySetWithMultipleSelectUpdatePATCH");
        deleteCreatedEntities();
        createEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        for (var entityType : entityTypesForCreate) {
            LOGGER.info("    {}", entityType);

            final QueryJsonFuture futureEven = QueryJsonFuture.build()
                    .setSelect(eh2.getSelectedProperties(entityType, true));
            final QueryJsonFuture futureOdd = QueryJsonFuture.build()
                    .setSelect(eh2.getSelectedProperties(entityType, false));
            final Callable<Object> patchAction = getPatchUpdateAction(entityType, futureEven, futureOdd);
            final TestSubscription evenSubscription = new TestSubscription(mqttHelper)
                    .setTopic("v1.1/" + entityType.mainContainer + '?' + eh2.createSelect(futureEven.getSelect()))
                    .addExpectedJson(futureEven.getFuture())
                    .createReceivedListener(entityType);
            final TestSubscription oddSubscription = new TestSubscription(mqttHelper)
                    .setTopic("v1.1/" + entityType.mainContainer + '?' + eh2.createSelect(futureOdd.getSelect()))
                    .addExpectedJson(futureOdd.getFuture())
                    .createReceivedListener(entityType);
            final MqttAction mqttAction = new MqttAction(patchAction)
                    .add(evenSubscription)
                    .add(oddSubscription);
            mqttHelper.executeRequest(mqttAction);
        }
    }

    @Test
    void check06_SubscribeToEntitySetWithMultipleSelectUpdatePUT() throws ServiceFailureException {
        LOGGER.info("  check06_SubscribeToEntitySetWithMultipleSelectUpdatePUT");
        deleteCreatedEntities();
        createEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        for (var entityType : entityTypesForCreate) {
            LOGGER.info("    {}", entityType);

            final QueryJsonFuture futureEven = QueryJsonFuture.build()
                    .setSelect(eh2.getSelectedProperties(entityType, true));
            final QueryJsonFuture futureOdd = QueryJsonFuture.build()
                    .setSelect(eh2.getSelectedProperties(entityType, false));
            final Callable<Object> putAction = getPutUpdateAction(entityType, futureEven, futureOdd);
            final TestSubscription evenSubscription = new TestSubscription(mqttHelper)
                    .setTopic("v1.1/" + entityType.mainContainer + '?' + eh2.createSelect(futureEven.getSelect()))
                    .addExpectedJson(futureEven.getFuture())
                    .createReceivedListener(entityType);
            final TestSubscription oddSubscription = new TestSubscription(mqttHelper)
                    .setTopic("v1.1/" + entityType.mainContainer + '?' + eh2.createSelect(futureOdd.getSelect()))
                    .addExpectedJson(futureOdd.getFuture())
                    .createReceivedListener(entityType);
            final MqttAction mqttAction = new MqttAction(putAction)
                    .add(evenSubscription)
                    .add(oddSubscription);
            mqttHelper.executeRequest(mqttAction);
        }
    }

    @Test
    void check07_SubscribeToEntitySetWithRelativeTopicUpdatePUT() throws ServiceFailureException {
        LOGGER.info("  check07_SubscribeToEntitySetWithRelativeTopicUpdatePUT");
        deleteCreatedEntities();
        createEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        int totalPaths = 0;
        for (var entityType : entityTypesForCreate) {
            LOGGER.info("    {}", entityType);

            final QueryJsonFuture future = QueryJsonFuture.build();
            final Callable<Object> updateAction = getPutUpdateAction(entityType, future);
            final List<TestSubscription> subs = new ArrayList<>();
            final List<List<NavigationProperty>> paths = eh2.findPathsTo(entityType, true, 4);
            LOGGER.debug("      {} paths", paths.size());
            for (var path : paths) {
                totalPaths++;
                var finalNp = path.get(path.size() - 1);
                String topic = eh2.createUrl(eh2.getCache(finalNp.getEntityType(), 0));
                for (int idx = path.size() - 1; idx >= 0; idx--) {
                    NavigationProperty nextNp = path.get(idx);
                    topic = topic + '/' + nextNp.getInverse();
                }
                final TestSubscription testSub = new TestSubscription(mqttHelper)
                        .setTopic(topic)
                        .addExpectedJson(future.getFuture())
                        .createReceivedListener(entityType);
                subs.add(testSub);
                LOGGER.debug("      {}", topic);
            }
            if (!subs.isEmpty()) {
                final MqttAction mqttAction = new MqttAction(updateAction)
                        .addAll(subs);
                mqttHelper.executeRequest(mqttAction);
            }
        }
        Assertions.assertTrue(totalPaths >= 6, "Did not find enough paths, something is amiss.");
    }

    @Test
    void check08a_SubscribeToEntitySetsWithDeepInsertThing() throws ServiceFailureException {
        LOGGER.info("  check08a_SubscribeToEntitySetsWithDeepInsertThing");
        deleteCreatedEntities();

        final var futureThing = QueryJsonFuture.build();
        final var futureLocation = QueryJsonFuture.build();
        final var futureDatastream = QueryJsonFuture.build();
        final var futureSensor = QueryJsonFuture.build();
        final var futureObservedProperty = QueryJsonFuture.build();
        final var futureHistLoc = QueryJsonFuture.build();
        final Callable<Object> updateAction = () -> {
            Entity thing = eh2.newThing();
            Entity location = eh2.newLocation();
            Entity sensor = eh2.newSensor();
            Entity obsProp = eh2.newObservedProperty();
            Entity datastream = eh2.newDatastream(obsProp, sensor);

            thing.addNavigationEntity(sMdl.npThingLocations, location);
            thing.addNavigationEntity(sMdl.npThingDatastreams, datastream);

            sSrvc.create(thing);

            Entity tempThing = sSrvc.dao(thing.getEntityType()).find(thing.getPrimaryKeyValues());
            Entity tempDs = thing.query(sMdl.npThingDatastreams).first();

            futureThing.complete(eh2.getEntity(thing));
            futureLocation.complete(eh2.getEntity(tempThing.query(sMdl.npThingLocations).first()));
            futureHistLoc.complete(eh2.getEntity(tempThing.query(sMdl.npThingHistoricallocations).first()));
            futureDatastream.complete(eh2.getEntity(tempDs));
            futureSensor.complete(eh2.getEntity(tempDs.getProperty(sMdl.npDatastreamSensor, true)));
            futureObservedProperty.complete(eh2.getEntity(tempDs.getProperty(sMdl.npDatastreamObservedproperty, true)));
            return null;
        };

        final var tsThing = new TestSubscription(mqttHelper, "v1.1/" + sMdl.etThing.mainContainer)
                .addExpectedJson(futureThing.getFuture())
                .createReceivedListener(sMdl.etThing);
        final var tsDatastream = new TestSubscription(mqttHelper, "v1.1/" + sMdl.etDatastream.mainContainer)
                .addExpectedJson(futureDatastream.getFuture())
                .createReceivedListener(sMdl.etDatastream);
        final var tsSensor = new TestSubscription(mqttHelper, "v1.1/" + sMdl.etSensor.mainContainer)
                .addExpectedJson(futureSensor.getFuture())
                .createReceivedListener(sMdl.etSensor);
        final var tsObsProp = new TestSubscription(mqttHelper, "v1.1/" + sMdl.etObservedProperty.mainContainer)
                .addExpectedJson(futureObservedProperty.getFuture())
                .createReceivedListener(sMdl.etObservedProperty);
        final var tsLoc = new TestSubscription(mqttHelper, "v1.1/" + sMdl.etLocation.mainContainer)
                .addExpectedJson(futureLocation.getFuture())
                .createReceivedListener(sMdl.etLocation);
        final var tsHistLoc = new TestSubscription(mqttHelper, "v1.1/" + sMdl.etHistoricalLocation.mainContainer)
                .addExpectedJson(futureHistLoc.getFuture())
                .createReceivedListener(sMdl.etHistoricalLocation);
        final MqttAction mqttAction = new MqttAction(updateAction)
                .add(tsThing)
                .add(tsDatastream)
                .add(tsSensor)
                .add(tsObsProp)
                .add(tsHistLoc)
                .add(tsLoc);
        mqttHelper.executeRequest(mqttAction);
    }

    @Test
    void check08b_SubscribeToEntitySetsWithDeepInsertObservation() throws ServiceFailureException {
        LOGGER.info("  check08b_SubscribeToEntitySetsWithDeepInsertObservation");
        deleteCreatedEntities();

        final var futureObservation = QueryJsonFuture.build();
        final var futureFeature = QueryJsonFuture.build();
        final var futureThing = QueryJsonFuture.build();
        final var futureLocation = QueryJsonFuture.build();
        final var futureDatastream = QueryJsonFuture.build();
        final var futureSensor = QueryJsonFuture.build();
        final var futureObservedProperty = QueryJsonFuture.build();
        final var futureHistLoc = QueryJsonFuture.build();
        final Callable<Object> updateAction = () -> {
            Entity observation;
            Entity sensor = eh2.newSensor();
            Entity obsProp = eh2.newObservedProperty();
            Entity thing = eh2.newThing();
            Entity location = eh2.newLocation();
            thing.addNavigationEntity(sMdl.npThingLocations, location);
            Entity datastream = eh2.newDatastream(thing, obsProp, sensor);
            Entity feature = eh2.newFeatureOfInterest();
            observation = eh2.newObservation(datastream, feature);

            sSrvc.create(observation);

            Entity tempObs = sSrvc.dao(observation.getEntityType()).find(observation.getPrimaryKeyValues());
            Entity tempFeature = tempObs.getProperty(sMdl.npObservationFeatureofinterest);
            Entity tempDs = tempObs.getProperty(sMdl.npObservationDatastream);
            Entity tempThing = tempDs.getProperty(sMdl.npDatastreamThing);

            futureObservation.complete(eh2.getEntity(tempObs));
            futureFeature.complete(eh2.getEntity(tempFeature));
            futureThing.complete(eh2.getEntity(tempThing));
            futureLocation.complete(eh2.getEntity(tempThing.query(sMdl.npThingLocations).first()));
            futureHistLoc.complete(eh2.getEntity(tempThing.query(sMdl.npThingHistoricallocations).first()));
            // The generated fields are not coming though in the MQTT message.
            futureDatastream.complete(eh2.getEntity(tempDs, Arrays.asList(
                    "@iot.selfLink",
                    "@iot.id",
                    "name",
                    "description",
                    "observationType",
                    "unitOfMeasurement",
                    "Thing", "Sensor", "ObservedProperty", "Observations")));
            futureSensor.complete(eh2.getEntity(tempDs.getProperty(sMdl.npDatastreamSensor, true)));
            futureObservedProperty.complete(eh2.getEntity(tempDs.getProperty(sMdl.npDatastreamObservedproperty, true)));
            return null;
        };

        final var tsThing = new TestSubscription(mqttHelper, "v1.1/" + sMdl.etThing.mainContainer)
                .addExpectedJson(futureThing.getFuture())
                .createReceivedListener(sMdl.etThing);
        final var tsFeature = new TestSubscription(mqttHelper, "v1.1/" + sMdl.etFeatureOfInterest.mainContainer)
                .addExpectedJson(futureFeature.getFuture())
                .createReceivedListener(sMdl.etFeatureOfInterest);
        final var tsDatastream = new TestSubscription(mqttHelper, "v1.1/" + sMdl.etDatastream.mainContainer)
                .addExpectedJson(futureDatastream.getFuture())
                .createReceivedListener(sMdl.etDatastream);
        final var tsSensor = new TestSubscription(mqttHelper, "v1.1/" + sMdl.etSensor.mainContainer)
                .addExpectedJson(futureSensor.getFuture())
                .createReceivedListener(sMdl.etSensor);
        final var tsObsProp = new TestSubscription(mqttHelper, "v1.1/" + sMdl.etObservedProperty.mainContainer)
                .addExpectedJson(futureObservedProperty.getFuture())
                .createReceivedListener(sMdl.etObservedProperty);
        final var tsLoc = new TestSubscription(mqttHelper, "v1.1/" + sMdl.etLocation.mainContainer)
                .addExpectedJson(futureLocation.getFuture())
                .createReceivedListener(sMdl.etLocation);
        final var tsHistLoc = new TestSubscription(mqttHelper, "v1.1/" + sMdl.etHistoricalLocation.mainContainer)
                .addExpectedJson(futureHistLoc.getFuture())
                .createReceivedListener(sMdl.etHistoricalLocation);
        final var tsObservation = new TestSubscription(mqttHelper, "v1.1/" + sMdl.etObservation.mainContainer)
                .addExpectedJson(futureObservation.getFuture())
                .createReceivedListener(sMdl.etObservation);
        final MqttAction mqttAction = new MqttAction(updateAction)
                .add(tsThing)
                .add(tsDatastream)
                .add(tsSensor)
                .add(tsObsProp)
                .add(tsHistLoc)
                .add(tsFeature)
                .add(tsObservation)
                .add(tsLoc);
        mqttHelper.executeRequest(mqttAction);
    }

    @Test
    void check09_SubscribeToEntityUpdatePATCH() throws ServiceFailureException {
        LOGGER.info("  check09_SubscribeToEntityUpdatePATCH");
        deleteCreatedEntities();
        createEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        for (var entityType : entityTypesForCreate) {
            LOGGER.info("    {}", entityType);
            final Entity entity = eh2.getCache(entityType, 0);
            final QueryJsonFuture future = QueryJsonFuture.build();
            final Callable<Object> updateAction = getPatchUpdateAction(entityType, future);
            final TestSubscription testSubscription = new TestSubscription(mqttHelper)
                    .setTopic(eh2.createUrl(entity))
                    .addExpectedJson(future.getFuture())
                    .createReceivedListener(entityType);
            final MqttAction mqttAction = new MqttAction(updateAction)
                    .add(testSubscription);
            mqttHelper.executeRequest(mqttAction);
        }
    }

    @Test
    void check10_SubscribeToEntityUpdatePUT() throws ServiceFailureException {
        LOGGER.info("  check10_SubscribeToEntityUpdatePUT");
        deleteCreatedEntities();
        createEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        for (var entityType : entityTypesForCreate) {
            LOGGER.info("    {}", entityType);
            final Entity entity = eh2.getCache(entityType, 0);
            final QueryJsonFuture future = QueryJsonFuture.build();
            final Callable<Object> updateAction = getPutUpdateAction(entityType, future);
            final TestSubscription testSubscription = new TestSubscription(mqttHelper)
                    .setTopic(eh2.createUrl(entity))
                    .addExpectedJson(future.getFuture())
                    .createReceivedListener(entityType);
            final MqttAction mqttAction = new MqttAction(updateAction)
                    .add(testSubscription);
            mqttHelper.executeRequest(mqttAction);
        }
    }

    @Test
    void check11_SubscribeToEntityWithRelativeTopicUpdatePUT() throws ServiceFailureException {
        LOGGER.info("  check11_SubscribeToEntityWithRelativeTopicUpdatePUT");
        deleteCreatedEntities();
        createEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        int totalPaths = 0;
        for (var entityType : entityTypesForCreate) {
            LOGGER.info("    {}", entityType);

            final QueryJsonFuture future = QueryJsonFuture.build();
            final Callable<Object> updateAction = getPutUpdateAction(entityType, future);
            final List<TestSubscription> subs = new ArrayList<>();
            final List<List<NavigationProperty>> paths = eh2.findPathsTo(entityType, false, 4);
            LOGGER.debug("      {} paths", paths.size());
            for (var path : paths) {
                totalPaths++;
                var finalNp = path.get(path.size() - 1);
                String topic = eh2.createUrl(eh2.getCache(finalNp.getEntityType(), 0));
                for (int idx = path.size() - 1; idx >= 0; idx--) {
                    NavigationProperty nextNp = path.get(idx);
                    topic = topic + '/' + nextNp.getInverse();
                }
                final TestSubscription testSub = new TestSubscription(mqttHelper)
                        .setTopic(topic)
                        .addExpectedJson(future.getFuture())
                        .createReceivedListener(entityType);
                subs.add(testSub);
                LOGGER.debug("      {}", topic);
            }
            if (!subs.isEmpty()) {
                final MqttAction mqttAction = new MqttAction(updateAction)
                        .addAll(subs);
                mqttHelper.executeRequest(mqttAction);
            }
        }
        Assertions.assertTrue(totalPaths >= 6, "Did not find enough paths, something is amiss.");
    }

    @Test
    void check12_SubscribeToPropertyUpdatePATCH() throws ServiceFailureException {
        LOGGER.info("  check12_SubscribeToPropertyUpdatePATCH");
        deleteCreatedEntities();
        createEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);
        boolean patch = true;
        SubscribeToPropertyUpdate(patch);
    }

    @Test
    void check13_SubscribeToPropertyUpdatePUT() throws ServiceFailureException {
        LOGGER.info("  check13_SubscribeToPropertyUpdatePUT");
        deleteCreatedEntities();
        createEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);
        boolean patch = false;
        SubscribeToPropertyUpdate(patch);
    }

    @Test
    void check14SubscribeToHistoricalLocationSetUpdateThingLocations() throws ServiceFailureException {
        LOGGER.info("  checkSubscribeToHistoricalLocationSetUpdateThingLocations");
        deleteCreatedEntities();
        createEntities();
        // Create a second location
        eh2.createLocation();
        createEntity(sMdl.etLocation);
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        final CompletableFuture<JsonNode> future = new CompletableFuture<>();
        final TestSubscription sub = new TestSubscription(mqttHelper, "v1.1/HistoricalLocations")
                .addExpectedJson(future)
                .createReceivedListener(sMdl.etHistoricalLocation);
        final Callable<Object> updateAction = () -> {
            Entity thing = eh2.getCache(sMdl.etThing, 0);
            Entity loc2 = eh2.getCache(sMdl.etLocation, 1);
            EntitySet thingLocs = new EntitySetImpl(sMdl.npThingLocations);
            thing.setProperty(sMdl.npThingLocations, thingLocs);
            thingLocs.add(loc2.withOnlyPk());
            sSrvc.update(thing);
            JsonNode result = eh2.getEntity(thing, sMdl.npThingHistoricallocations, null, null, "id%20desc");
            future.complete(result);
            return null;
        };
        final MqttAction mqttAction = new MqttAction(updateAction)
                .add(sub);
        mqttHelper.executeRequest(mqttAction);

    }

    private void SubscribeToPropertyUpdate(boolean patch) throws IllegalArgumentException {
        for (var entityType : entityTypesForCreate) {
            LOGGER.info("    {}", entityType);
            Entity entity = eh2.getCache(entityType, 0);
            final Entity copy = entity.withOnlyPk();
            final List<String> changedProps = eh2.changeEntity(copy);
            final List<TestSubscription> subs = new ArrayList<>();
            final List<QueryJsonFuture> futures = new ArrayList<>();
            for (String prop : changedProps) {
                final QueryJsonFuture future = QueryJsonFuture.build();
                final TestSubscription sub = new TestSubscription(mqttHelper)
                        .setTopic(eh2.createUrl(entity) + '/' + prop)
                        .addExpectedJson(future.getFuture())
                        .createReceivedListener(entityType);
                subs.add(sub);
                futures.add(future);
                LOGGER.debug("      {}", sub.getTopic());
            }
            final Callable<Object> updateAction = () -> {
                if (patch) {
                    sSrvc.update(copy);
                } else {
                    eh2.sendHttpPutEntity(copy);
                }
                int idx = 0;
                for (var future : futures) {
                    JsonNode expected = eh2.getEntity(copy, Arrays.asList(changedProps.get(idx)));
                    future.complete(expected);
                    idx++;
                }
                return null;
            };
            final MqttAction mqttAction = new MqttAction(updateAction)
                    .addAll(subs);
            mqttHelper.executeRequest(mqttAction);
        }
    }

    private static class QueryJsonFuture {

        private CompletableFuture<JsonNode> future = new CompletableFuture<>();
        private List<String> select;

        public CompletableFuture<JsonNode> getFuture() {
            return future;
        }

        public QueryJsonFuture setFuture(CompletableFuture<JsonNode> future) {
            this.future = future;
            return this;
        }

        public boolean complete(JsonNode jsonNode) {
            return future.complete(jsonNode);
        }

        public List<String> getSelect() {
            return select;
        }

        public QueryJsonFuture setSelect(List<String> select) {
            this.select = select;
            return this;
        }

        public static QueryJsonFuture build() {
            return new QueryJsonFuture();
        }

    }

    private Callable<Object> getInsertAction(final EntityType et, final QueryJsonFuture... futures) {
        return () -> {
            Entity entity = createEntity(et);
            for (var future : futures) {
                JsonNode jsonNode = eh2.getEntity(entity, future.getSelect());
                future.complete(jsonNode);
            }
            return null;
        };
    }

    private void createEntities() throws ServiceFailureException {
        for (var et : entityTypesForCreate) {
            createEntity(et);
        }
    }

    /**
     * Returns a new entity after creating it on the server. The entity has a
     * primary key, and is added to the entity cache.
     *
     * @param et The type of entity to create.
     * @return a new entity of the given type.
     * @throws ServiceFailureException if there is a problem sending the entity
     * to the server.
     */
    private Entity createEntity(EntityType et) throws ServiceFailureException {
        final Entity entity = newEntity(et);
        sSrvc.create(entity);
        return entity;
    }

    /**
     * Returns a new entity without creating it on the server. The entity has no
     * primary key, but it is added to the entity cache.
     *
     * @param et The type of entity to create.
     * @return a new entity of the given type.
     */
    private Entity newEntity(EntityType et) {
        switch (et.entityName) {
            case NAME_THING:
                return eh2.newThing();

            case NAME_SENSOR:
                return eh2.newSensor();

            case NAME_LOCATION:
                return eh2.newLocation(eh2.getCache(sMdl.etThing, 0));

            case NAME_OBSERVEDPROPERTY:
                return eh2.newObservedProperty();

            case NAME_FEATUREOFINTEREST:
                return eh2.newFeatureOfInterest();

            case NAME_DATASTREAM:
                return eh2.newDatastream(
                        eh2.getCache(sMdl.etThing, 0),
                        eh2.getCache(sMdl.etObservedProperty, 0),
                        eh2.getCache(sMdl.etSensor, 0));

            case NAME_OBSERVATION:
                if (eh2.getCache(et).isEmpty()) {
                    return eh2.newObservation(
                            eh2.getCache(sMdl.etDatastream, 0),
                            eh2.getCache(sMdl.etFeatureOfInterest, 0));
                }
                return eh2.newObservation(eh2.getCache(sMdl.etDatastream, 0));

            case NAME_HISTORICALLOCATION:
                return eh2.newHistoricalLocation(
                        eh2.getCache(sMdl.etThing, 0),
                        eh2.getCache(sMdl.etLocation, 0));

            default:
                throw new IllegalArgumentException("Don't know how to create a " + et);
        }
    }

    private Callable<Object> getPatchUpdateAction(final EntityType et, final QueryJsonFuture... futures) {
        return () -> {
            Entity entity = patchEntity(et);
            for (var future : futures) {
                JsonNode jsonNode = eh2.getEntity(entity, future.getSelect());
                future.getFuture().complete(jsonNode);
            }
            return null;
        };
    }

    private Entity patchEntity(EntityType et) throws ServiceFailureException {
        Entity original = eh2.getCache(et, 0);
        return eh2.patchEntity(original);
    }

    private Callable<Object> getPutUpdateAction(final EntityType et, final QueryJsonFuture... futures) {
        return () -> {
            Entity entity = putEntity(et);
            for (var future : futures) {
                JsonNode jsonNode = eh2.getEntity(entity, future.getSelect());
                future.getFuture().complete(jsonNode);
            }
            return null;
        };
    }

    private Entity putEntity(EntityType et) throws ServiceFailureException {
        Entity original = eh2.getCache(et, 0);
        return eh2.putEntity(original);
    }

    private Callable<Object> getInsertActionObs(final CompletableFuture<JsonNode> futureObs, final CompletableFuture<JsonNode> futureFoi) {
        return () -> {
            Entity obs = eh2.createObservation(eh2.getCache(sMdl.etDatastream, 0));
            JsonNode jsonNode1 = eh2.getEntity(obs);
            futureObs.complete(jsonNode1);
            JsonNode jsonNode2 = eh2.getEntity(obs, sMdl.npObservationFeatureofinterest);
            futureFoi.complete(jsonNode2);
            return null;
        };

    }
}
