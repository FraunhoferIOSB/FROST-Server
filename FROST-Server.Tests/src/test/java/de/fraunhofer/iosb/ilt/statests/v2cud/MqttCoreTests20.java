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
package de.fraunhofer.iosb.ilt.statests.v2cud;

import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.NAME_DATASTREAM;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.NAME_FEATURE;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.NAME_HISTORICALLOCATION;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.NAME_LOCATION;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.NAME_OBSERVATION;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.NAME_OBSERVEDPROPERTY;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.NAME_SENSOR;
import static de.fraunhofer.iosb.ilt.frostclient.models.CommonProperties.NAME_THING;
import static de.fraunhofer.iosb.ilt.frostclient.utils.SpecialNames.AT_CONTEXT;
import static de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper11.WAIT_AFTER_CLEANUP;
import static de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper11.waitMillis;

import de.fraunhofer.iosb.ilt.frostclient.SensorThingsService;
import de.fraunhofer.iosb.ilt.frostclient.exception.ServiceFailureException;
import de.fraunhofer.iosb.ilt.frostclient.exception.StatusCodeException;
import de.fraunhofer.iosb.ilt.frostclient.model.Entity;
import de.fraunhofer.iosb.ilt.frostclient.model.EntitySet;
import de.fraunhofer.iosb.ilt.frostclient.model.EntityType;
import de.fraunhofer.iosb.ilt.frostclient.model.property.NavigationProperty;
import de.fraunhofer.iosb.ilt.frostclient.models.SensorThingsV20Core;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityHelper20;
import de.fraunhofer.iosb.ilt.statests.util.EntityHelperAbstract.EntityCreateInfo;
import de.fraunhofer.iosb.ilt.statests.util.EntityHelperAbstract.StringModifier;
import de.fraunhofer.iosb.ilt.statests.util.EntityUtils;
import de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper11;
import de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper11.MqttAction;
import de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper11.TestSubscription;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.Strings;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;

/**
 * Test the standard MQTT functionality.
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
public class MqttCoreTests20 extends AbstractTestClass {

    private static final Logger LOGGER = LoggerFactory.getLogger(MqttCoreTests20.class);

    private final StringModifier urlsHttpToMqtt = (s) -> Strings.CS.replace(s, serverSettings.getServiceRootUrl() + '/', "");

    private static List<EntityCreateInfo> entityTypesForCreate;

    private static EntityHelper20 eh;
    private static MqttHelper11 mqttHelper;
    private static SensorThingsV20Core sMdl;

    private static final Map<String, String> SERVER_PROPERTIES = new LinkedHashMap<>();

    static {
        SERVER_PROPERTIES.put("plugins.modelLoader.enable", "true");
        SERVER_PROPERTIES.put("plugins.coreModel.enable", "false");
        SERVER_PROPERTIES.put("plugins.coreModel.idType", "LONG");
        SERVER_PROPERTIES.put("plugins.coreService.enable", "true");
        SERVER_PROPERTIES.put("plugins.coreModelV2.enable", "true");
    }

    public MqttCoreTests20() {
        super(ServerVersion.v_2_0, SERVER_PROPERTIES);
    }

    @Override
    protected void setUpVersion() throws ServiceFailureException, URISyntaxException {
        LOGGER.info("Setting up for version {}.", version.urlPart);
        sMdl = sSrvc.getModel(SensorThingsV20Core.class);
        eh = new EntityHelper20(sSrvc);
        mqttHelper = new MqttHelper11(sSrvc, serverSettings.getMqttUrl(), serverSettings.getMqttTimeOutMs());
        entityTypesForCreate = Arrays.asList(
                EntityCreateInfo.of(sMdl.etThing, 1),
                EntityCreateInfo.of(sMdl.etLocation, 1),
                EntityCreateInfo.of(sMdl.etSensor, 1),
                EntityCreateInfo.of(sMdl.etObservedProperty, 1),
                EntityCreateInfo.of(sMdl.etFeature, 2),
                EntityCreateInfo.of(sMdl.etDatastream, 1),
                EntityCreateInfo.of(sMdl.etObservation, 1),
                EntityCreateInfo.of(sMdl.etHistoricalLocation, 1));
    }

    @Override
    protected SensorThingsService createService() throws MalformedURLException, URISyntaxException {
        return new SensorThingsService(new SensorThingsV20Core())
                .setBaseUrl(new URI(serverSettings.getServiceUrl(version)).toURL())
                .init();
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
        EntityUtils.deleteAll(sSrvc);
        eh.clearCaches();
        eh = null;
        mqttHelper = null;

    }

    private void deleteCreatedEntities() throws ServiceFailureException {
        EntityUtils.deleteAll(sSrvc);
        eh.clearCaches();
    }

    @Test
    void check01_SubscribeToEntitySetInsert() throws ServiceFailureException {
        LOGGER.info("  check01a_SubscribeToEntitySetInsert");
        deleteCreatedEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        for (var eci : entityTypesForCreate) {
            EntityType entityType = eci.et;
            LOGGER.info("    {}", entityType);
            final QueryJsonFuture future = QueryJsonFuture.build()
                    .setSelect(eh.selectAllWithId(eci.et));
            final Callable<Object> insertAction = getInsertAction(entityType, future);
            final TestSubscription testSubscription = new TestSubscription(mqttHelper, "v2.0/" + entityType.mainSet)
                    .addExpectedJson(future.getFuture())
                    .createReceivedListener(entityType);
            final MqttAction mqttAction = new MqttAction(insertAction)
                    .add(testSubscription);
            mqttHelper.executeRequest(mqttAction);

            for (int i = 1; i <= eci.count; i++) {
                createEntity(entityType, i);
            }
        }
    }

    @Test
    void check02_SubscribeToEntitySetUpdatePATCH() throws ServiceFailureException {
        LOGGER.info("  check02SubscribeToEntitySetUpdatePATCH");
        deleteCreatedEntities();
        createEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        for (var eci : entityTypesForCreate) {
            EntityType entityType = eci.et;
            LOGGER.info("    {}", entityType);
            final QueryJsonFuture future = QueryJsonFuture.build()
                    .setSelect(eh.selectAllWithId(eci.et));
            final Callable<Object> updateAction = getPatchUpdateAction(entityType, future);
            final TestSubscription testSubscription = new TestSubscription(mqttHelper, "v2.0/" + entityType.mainSet)
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

        for (var eci : entityTypesForCreate) {
            EntityType entityType = eci.et;
            LOGGER.info("    {}", entityType);
            final Entity original = eh.getCache(entityType, 0);
            final QueryJsonFuture future = QueryJsonFuture.build()
                    .setSelect(eh.selectAllWithId(eci.et));
            final Callable<Object> updateAction = getPutUpdateAction(original, future);
            final TestSubscription testSubscription = new TestSubscription(mqttHelper, "v2.0/" + entityType.mainSet)
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

        for (var eci : entityTypesForCreate) {
            EntityType entityType = eci.et;
            LOGGER.info("    {}", entityType);

            final QueryJsonFuture futureEven = QueryJsonFuture.build()
                    .setSelect(eh.getSelectedProperties(entityType, true));
            final QueryJsonFuture futureOdd = QueryJsonFuture.build()
                    .setSelect(eh.getSelectedProperties(entityType, false));
            final Callable<Object> insertAction = getInsertAction(entityType, futureEven, futureOdd);
            final TestSubscription evenSubscription = new TestSubscription(mqttHelper)
                    .setTopic("v2.0/" + entityType.mainSet + '?' + eh.createSelect(futureEven.getSelect()))
                    .addExpectedJson(futureEven.getFuture())
                    .createReceivedListener(entityType);
            final TestSubscription oddSubscription = new TestSubscription(mqttHelper)
                    .setTopic("v2.0/" + entityType.mainSet + '?' + eh.createSelect(futureOdd.getSelect()))
                    .addExpectedJson(futureOdd.getFuture())
                    .createReceivedListener(entityType);
            final MqttAction mqttAction = new MqttAction(insertAction)
                    .add(evenSubscription)
                    .add(oddSubscription);
            mqttHelper.executeRequest(mqttAction);

            for (int i = 1; i <= eci.count; i++) {
                createEntity(entityType, i);
            }
        }
    }

    @Test
    void check05_SubscribeToEntitySetWithMultipleSelectUpdatePATCH() throws ServiceFailureException {
        LOGGER.info("  check05_SubscribeToEntitySetWithMultipleSelectUpdatePATCH");
        deleteCreatedEntities();
        createEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        for (var eci : entityTypesForCreate) {
            EntityType entityType = eci.et;
            LOGGER.info("    {}", entityType);

            final QueryJsonFuture futureEven = QueryJsonFuture.build()
                    .setSelect(eh.getSelectedProperties(entityType, true));
            final QueryJsonFuture futureOdd = QueryJsonFuture.build()
                    .setSelect(eh.getSelectedProperties(entityType, false));
            final Callable<Object> patchAction = getPatchUpdateAction(entityType, futureEven, futureOdd);
            final TestSubscription evenSubscription = new TestSubscription(mqttHelper)
                    .setTopic("v2.0/" + entityType.mainSet + '?' + eh.createSelect(futureEven.getSelect()))
                    .addExpectedJson(futureEven.getFuture())
                    .createReceivedListener(entityType);
            final TestSubscription oddSubscription = new TestSubscription(mqttHelper)
                    .setTopic("v2.0/" + entityType.mainSet + '?' + eh.createSelect(futureOdd.getSelect()))
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

        for (var eci : entityTypesForCreate) {
            EntityType entityType = eci.et;
            LOGGER.info("    {}", entityType);
            final Entity original = eh.getCache(entityType, 0);
            final QueryJsonFuture futureEven = QueryJsonFuture.build()
                    .setSelect(eh.getSelectedProperties(entityType, true));
            final QueryJsonFuture futureOdd = QueryJsonFuture.build()
                    .setSelect(eh.getSelectedProperties(entityType, false));
            final Callable<Object> putAction = getPutUpdateAction(original, futureEven, futureOdd);
            final TestSubscription evenSubscription = new TestSubscription(mqttHelper)
                    .setTopic("v2.0/" + entityType.mainSet + '?' + eh.createSelect(futureEven.getSelect()))
                    .addExpectedJson(futureEven.getFuture())
                    .createReceivedListener(entityType);
            final TestSubscription oddSubscription = new TestSubscription(mqttHelper)
                    .setTopic("v2.0/" + entityType.mainSet + '?' + eh.createSelect(futureOdd.getSelect()))
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
        for (var eci : entityTypesForCreate) {
            EntityType entityType = eci.et;
            LOGGER.info("    {}", entityType);
            final Entity original = eh.getCache(entityType, 0);
            final QueryJsonFuture future = QueryJsonFuture.build()
                    .setSelect(eh.selectAllWithId(eci.et));
            final Callable<Object> updateAction = getPutUpdateAction(original, future);
            final List<TestSubscription> subs = new ArrayList<>();
            final List<List<String>> paths = eh.findPathsTo(original, true, 4);
            LOGGER.debug("      {} paths", paths.size());
            for (var path : paths) {
                totalPaths++;
                String topic = version.urlPart;
                for (int idx = path.size() - 1; idx >= 0; idx--) {
                    topic += '/' + path.get(idx);
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

        Entity obsProp = eh.createObservedProperty();

        final var futureThing = QueryJsonFuture.build()
                .setSelect(eh.selectAllWithId(sMdl.etThing));
        final var futureLocation = QueryJsonFuture.build()
                .setSelect(eh.selectAllWithId(sMdl.etLocation));
        final var futureDatastream = QueryJsonFuture.build()
                .setSelect(eh.selectAllWithId(sMdl.etDatastream));
        final var futureSensor = QueryJsonFuture.build()
                .setSelect(eh.selectAllWithId(sMdl.etSensor));
        final var futureHistLoc = QueryJsonFuture.build()
                .setSelect(eh.selectAllWithId(sMdl.etHistoricalLocation));
        final Callable<Object> updateAction = () -> {
            Entity thing = eh.newThing();
            Entity location = eh.newLocation();
            Entity sensor = eh.newSensor();
            Entity datastream = eh.newDatastream(obsProp, sensor);

            thing.addNavigationEntity(sMdl.npThingLocations, location);
            thing.addNavigationEntity(sMdl.npThingDatastreams, datastream);

            sSrvc.create(thing);

            Entity tempThing = sSrvc.dao(thing.getType()).find(thing.getPrimaryKeyValues());
            Entity tempDs = thing.query(sMdl.npThingDatastreams).first();

            futureThing.complete(getEntity(thing, eh.selectAllWithId(sMdl.etThing)));
            futureLocation.complete(getEntity(
                    tempThing.query(sMdl.npThingLocations).first(),
                    eh.selectAllWithId(sMdl.etLocation)));
            futureHistLoc.complete(getEntity(
                    tempThing.query(sMdl.npThingHistoricallocations).first(),
                    eh.selectAllWithId(sMdl.etHistoricalLocation)));
            futureDatastream.complete(getEntity(
                    tempDs,
                    eh.selectAllWithId(sMdl.etDatastream)));
            futureSensor.complete(getEntity(
                    tempDs.getProperty(sMdl.npDatastreamSensor, true),
                    eh.selectAllWithId(sMdl.etSensor)));
            return null;
        };

        final var tsThing = new TestSubscription(mqttHelper, "v2.0/" + sMdl.etThing.mainSet)
                .addExpectedJson(futureThing.getFuture())
                .createReceivedListener(sMdl.etThing);
        final var tsDatastream = new TestSubscription(mqttHelper, "v2.0/" + sMdl.etDatastream.mainSet)
                .addExpectedJson(futureDatastream.getFuture())
                .createReceivedListener(sMdl.etDatastream);
        final var tsSensor = new TestSubscription(mqttHelper, "v2.0/" + sMdl.etSensor.mainSet)
                .addExpectedJson(futureSensor.getFuture())
                .createReceivedListener(sMdl.etSensor);
        final var tsLoc = new TestSubscription(mqttHelper, "v2.0/" + sMdl.etLocation.mainSet)
                .addExpectedJson(futureLocation.getFuture())
                .createReceivedListener(sMdl.etLocation);
        final var tsHistLoc = new TestSubscription(mqttHelper, "v2.0/" + sMdl.etHistoricalLocation.mainSet)
                .addExpectedJson(futureHistLoc.getFuture())
                .createReceivedListener(sMdl.etHistoricalLocation);
        final MqttAction mqttAction = new MqttAction(updateAction)
                .add(tsThing)
                .add(tsDatastream)
                .add(tsSensor)
                .add(tsHistLoc)
                .add(tsLoc);
        mqttHelper.executeRequest(mqttAction);
    }

    @Test
    void check08b_SubscribeToEntitySetsWithDeepInsertObservation() throws ServiceFailureException {
        LOGGER.info("  check08b_SubscribeToEntitySetsWithDeepInsertObservation");
        deleteCreatedEntities();

        Entity obsProp = eh.createObservedProperty();

        final var futureObservation = QueryJsonFuture.build()
                .setSelect(eh.selectAllWithId(sMdl.etObservation));
        final var futureFeature = QueryJsonFuture.build()
                .setSelect(eh.selectAllWithId(sMdl.etFeature));
        final var futureThing = QueryJsonFuture.build()
                .setSelect(eh.selectAllWithId(sMdl.etThing));
        final var futureLocation = QueryJsonFuture.build()
                .setSelect(eh.selectAllWithId(sMdl.etLocation));
        final var futureDatastream = QueryJsonFuture.build()
                .setSelect(eh.selectAllWithId(sMdl.etDatastream));
        final var futureSensor = QueryJsonFuture.build()
                .setSelect(eh.selectAllWithId(sMdl.etSensor));
        final var futureHistLoc = QueryJsonFuture.build()
                .setSelect(eh.selectAllWithId(sMdl.etHistoricalLocation));
        final Callable<Object> updateAction = () -> {
            Entity observation;
            Entity sensor = eh.newSensor();
            Entity thing = eh.newThing();
            Entity location = eh.newLocation();
            thing.addNavigationEntity(sMdl.npThingLocations, location);
            Entity datastream = eh.newDatastream(thing, obsProp, sensor);
            Entity feature = eh.newFeatureOfInterest(1);
            observation = eh.newObservation(datastream, feature);

            sSrvc.create(observation);

            Entity tempObs = sSrvc.dao(observation.getType()).find(observation.getPrimaryKeyValues());
            Entity tempFeature = tempObs.getProperty(sMdl.npObservationProximateFoi);
            Entity tempDs = tempObs.getProperty(sMdl.npObservationDatastream);
            Entity tempThing = tempDs.getProperty(sMdl.npDatastreamThing);

            futureObservation.complete(getEntity(tempObs, eh.selectAllWithId(sMdl.etObservation)));
            futureFeature.complete(getEntity(tempFeature, eh.selectAllWithId(sMdl.etFeature)));
            futureThing.complete(getEntity(tempThing, eh.selectAllWithId(sMdl.etThing)));
            futureLocation.complete(getEntity(
                    tempThing.query(sMdl.npThingLocations).first(),
                    eh.selectAllWithId(sMdl.etLocation)));
            futureHistLoc.complete(getEntity(
                    tempThing.query(sMdl.npThingHistoricallocations).first(),
                    eh.selectAllWithId(sMdl.etHistoricalLocation)));
            // The generated fields are not coming though in the MQTT message.
            futureDatastream.complete(getEntity(tempDs, Arrays.asList(
                    "@id",
                    "id",
                    "name",
                    "description",
                    "resultType",
                    "resultEncoding")));
            futureSensor.complete(getEntity(
                    tempDs.getProperty(sMdl.npDatastreamSensor, true),
                    eh.selectAllWithId(sMdl.etSensor)));
            return null;
        };

        final var tsThing = new TestSubscription(mqttHelper, "v2.0/" + sMdl.etThing.mainSet)
                .addExpectedJson(futureThing.getFuture())
                .createReceivedListener(sMdl.etThing);
        final var tsFeature = new TestSubscription(mqttHelper, "v2.0/" + sMdl.etFeature.mainSet)
                .addExpectedJson(futureFeature.getFuture())
                .createReceivedListener(sMdl.etFeature);
        final var tsDatastream = new TestSubscription(mqttHelper, "v2.0/" + sMdl.etDatastream.mainSet)
                .addExpectedJson(futureDatastream.getFuture())
                .createReceivedListener(sMdl.etDatastream);
        final var tsSensor = new TestSubscription(mqttHelper, "v2.0/" + sMdl.etSensor.mainSet)
                .addExpectedJson(futureSensor.getFuture())
                .createReceivedListener(sMdl.etSensor);
        final var tsLoc = new TestSubscription(mqttHelper, "v2.0/" + sMdl.etLocation.mainSet)
                .addExpectedJson(futureLocation.getFuture())
                .createReceivedListener(sMdl.etLocation);
        final var tsHistLoc = new TestSubscription(mqttHelper, "v2.0/" + sMdl.etHistoricalLocation.mainSet)
                .addExpectedJson(futureHistLoc.getFuture())
                .createReceivedListener(sMdl.etHistoricalLocation);
        final var tsObservation = new TestSubscription(mqttHelper, "v2.0/" + sMdl.etObservation.mainSet)
                .addExpectedJson(futureObservation.getFuture())
                .createReceivedListener(sMdl.etObservation);
        final MqttAction mqttAction = new MqttAction(updateAction)
                .add(tsThing)
                .add(tsDatastream)
                .add(tsSensor)
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

        for (var eci : entityTypesForCreate) {
            EntityType entityType = eci.et;
            LOGGER.info("    {}", entityType);
            final Entity entity = eh.getCache(entityType, 0);
            final QueryJsonFuture future = QueryJsonFuture.build()
                    .setSelect(eh.selectAllWithId(eci.et));
            final Callable<Object> updateAction = getPatchUpdateAction(entityType, future);
            final TestSubscription testSubscription = new TestSubscription(mqttHelper)
                    .setTopic(eh.createUrl(entity))
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

        for (var eci : entityTypesForCreate) {
            EntityType entityType = eci.et;
            LOGGER.info("    {}", entityType);
            final Entity entity = eh.getCache(entityType, 0);
            final QueryJsonFuture future = QueryJsonFuture.build()
                    .setSelect(eh.selectAllWithId(eci.et));
            final Callable<Object> updateAction = getPutUpdateAction(entity, future);
            final TestSubscription testSubscription = new TestSubscription(mqttHelper)
                    .setTopic(eh.createUrl(entity))
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
        for (var eci : entityTypesForCreate) {
            EntityType entityType = eci.et;
            LOGGER.info("    {}", entityType);

            final Entity original = eh.getCache(entityType, 0);
            final QueryJsonFuture future = QueryJsonFuture.build()
                    .setSelect(eh.selectAllWithId(eci.et));
            final Callable<Object> updateAction = getPutUpdateAction(original, future);
            final List<TestSubscription> subs = new ArrayList<>();
            final List<List<String>> paths = eh.findPathsTo(original, false, 4);
            LOGGER.debug("      {} paths", paths.size());
            for (var path : paths) {
                totalPaths++;
                String topic = version.urlPart;
                for (int idx = path.size() - 1; idx >= 0; idx--) {
                    topic += '/' + path.get(idx);
                }
                final TestSubscription testSub = new TestSubscription(mqttHelper)
                        .setTopic(topic)
                        .addExpectedJson(future.getFuture())
                        .createReceivedListener(entityType);
                subs.add(testSub);
                LOGGER.info("      {}", topic);
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
        eh.createLocation();
        createEntity(sMdl.etLocation, 9);
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        final CompletableFuture<JsonNode> future = new CompletableFuture<>();
        final TestSubscription sub = new TestSubscription(mqttHelper, "v2.0/HistoricalLocations")
                .addExpectedJson(future)
                .createReceivedListener(sMdl.etHistoricalLocation);
        final Callable<Object> updateAction = () -> {
            Entity thing = eh.getCache(sMdl.etThing, 0);
            Entity loc2 = eh.getCache(sMdl.etLocation, 1);
            EntitySet thingLocs = new EntitySet(thing, sMdl.npThingLocations);
            thing.setProperty(sMdl.npThingLocations, thingLocs);
            thingLocs.add(loc2.withOnlyPk());
            sSrvc.update(thing);
            JsonNode result = eh.getEntity(thing, sMdl.npThingHistoricallocations, Arrays.asList("@id", "id", "time"), null, "id%20desc", urlsHttpToMqtt);
            future.complete(result);
            return null;
        };
        final MqttAction mqttAction = new MqttAction(updateAction)
                .add(sub);
        mqttHelper.executeRequest(mqttAction);

    }

    private void SubscribeToPropertyUpdate(boolean patch) throws IllegalArgumentException {
        for (var eci : entityTypesForCreate) {
            EntityType entityType = eci.et;
            LOGGER.info("    {}", entityType);
            Entity entity = eh.getCache(entityType, 0);
            final Entity copy = entity.withOnlyPk();
            final List<String> changedProps = eh.changeEntity(copy);
            final List<TestSubscription> subs = new ArrayList<>();
            final List<QueryJsonFuture> futures = new ArrayList<>();
            for (String prop : changedProps) {
                final QueryJsonFuture future = QueryJsonFuture.build();
                final TestSubscription sub = new TestSubscription(mqttHelper)
                        .setTopic(eh.createUrl(entity) + '/' + prop)
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
                    eh.sendHttpPutEntity(copy);
                }
                int idx = 0;
                for (var future : futures) {
                    JsonNode expected = getEntity(copy, Arrays.asList(changedProps.get(idx)));
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
            Entity entity = createEntity(et, 10);
            for (var future : futures) {
                JsonNode jsonNode = getEntity(entity, future.getSelect());
                future.complete(jsonNode);
            }
            return null;
        };
    }

    private void createEntities() throws ServiceFailureException {
        for (var eci : entityTypesForCreate) {
            LOGGER.debug("Creating {}", eci);
            for (int i = 0; i < eci.count; i++) {
                createEntity(eci.et, i);
            }
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
    private Entity createEntity(EntityType et, int idx) throws ServiceFailureException {
        final Entity entity = newEntity(et, idx);
        try {
            sSrvc.create(entity);
        } catch (StatusCodeException ex) {
            LOGGER.warn("Failed to create: {} {} \n{}", ex.getStatusCode(), ex.getUrl(), ex.getReturnedContent());
            LOGGER.info("Exception", ex);
        } catch (RuntimeException ex) {
            LOGGER.info("Failed to create entity {} of type {}", idx, et, ex);
        }
        return entity;
    }

    private Entity newEntity(EntityType et, int idx) {
        switch (et.getName()) {
            case NAME_THING:
                return eh.newThing();

            case NAME_SENSOR:
                return eh.newSensor();

            case NAME_LOCATION:
                return eh.newLocation(eh.getCache(sMdl.etThing, 0));

            case NAME_OBSERVEDPROPERTY:
                return eh.newObservedProperty();

            case NAME_FEATURE:
                return eh.newFeatureOfInterest(idx);

            case NAME_DATASTREAM:
                return eh.newDatastream(
                        eh.getCache(sMdl.etThing, 0),
                        eh.getCache(sMdl.etObservedProperty, 0),
                        eh.getCache(sMdl.etSensor, 0))
                        .setProperty(sMdl.npDatastreamProximateFoi, eh.getCache(sMdl.etFeature, 0))
                        .addNavigationEntity(sMdl.npDatastreamUltimateFois, eh.getCache(sMdl.etFeature, 1));

            case NAME_OBSERVATION:
                if (eh.getCache(et).isEmpty()) {
                    return eh.newObservation(
                            eh.getCache(sMdl.etDatastream, 0),
                            eh.getCache(sMdl.etFeature, 0));
                }
                return eh.newObservation(eh.getCache(sMdl.etDatastream, 0));

            case NAME_HISTORICALLOCATION:
                return eh.newHistoricalLocation(
                        eh.getCache(sMdl.etThing, 0),
                        eh.getCache(sMdl.etLocation, 0));

            default:
                throw new IllegalArgumentException("Don't know how to create a " + et);
        }
    }

    private Callable<Object> getPatchUpdateAction(final EntityType et, final QueryJsonFuture... futures) {
        return () -> {
            Entity entity = patchEntity(et);
            for (var future : futures) {
                JsonNode jsonNode = getEntity(entity, future.getSelect());
                future.getFuture().complete(jsonNode);
            }
            return null;
        };
    }

    private Entity patchEntity(EntityType et) throws ServiceFailureException {
        Entity original = eh.getCache(et, 0);
        return eh.patchEntity(original);
    }

    private Callable<Object> getPutUpdateAction(final Entity original, final QueryJsonFuture... futures) {
        return () -> {
            Entity entity = eh.putEntity(original);
            for (var future : futures) {
                JsonNode jsonNode = getEntity(entity, future.getSelect());
                future.getFuture().complete(jsonNode);
            }
            return null;
        };
    }

    private Callable<Object> getInsertActionObs(final CompletableFuture<JsonNode> futureObs, final CompletableFuture<JsonNode> futureFoi) {
        return () -> {
            Entity obs = eh.createObservation(eh.getCache(sMdl.etDatastream, 0));
            JsonNode jsonNode1 = getEntity(obs);
            futureObs.complete(jsonNode1);
            JsonNode jsonNode2 = getEntity(obs, sMdl.npObservationProximateFoi);
            futureFoi.complete(jsonNode2);
            return null;
        };

    }

    public JsonNode getEntity(Entity entity) {
        final JsonNode jsonNode = eh.getEntity(entity, urlsHttpToMqtt);
        jsonNode.asObject().remove(AT_CONTEXT);
        return jsonNode;
    }

    public final JsonNode getEntity(Entity entity, List<String> select) {
        JsonNode jsonNode = eh.getEntity(entity, select, urlsHttpToMqtt);
        jsonNode.asObject().remove(AT_CONTEXT);
        return jsonNode;
    }

    public JsonNode getEntity(Entity entity, NavigationProperty np) {
        final JsonNode jsonNode = eh.getEntity(entity, np, urlsHttpToMqtt);
        jsonNode.asObject().remove(AT_CONTEXT);
        return jsonNode;
    }
}
