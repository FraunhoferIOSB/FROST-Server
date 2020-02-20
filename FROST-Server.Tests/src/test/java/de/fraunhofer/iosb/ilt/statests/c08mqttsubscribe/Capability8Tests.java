/*
 * Copyright 2016 Open Geospatial Consortium.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.iosb.ilt.statests.c08mqttsubscribe;

import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityHelper;
import de.fraunhofer.iosb.ilt.statests.util.EntityType;
import static de.fraunhofer.iosb.ilt.statests.util.EntityType.DATASTREAM;
import static de.fraunhofer.iosb.ilt.statests.util.EntityType.FEATURE_OF_INTEREST;
import static de.fraunhofer.iosb.ilt.statests.util.EntityType.HISTORICAL_LOCATION;
import static de.fraunhofer.iosb.ilt.statests.util.EntityType.LOCATION;
import static de.fraunhofer.iosb.ilt.statests.util.EntityType.OBSERVATION;
import static de.fraunhofer.iosb.ilt.statests.util.EntityType.OBSERVED_PROPERTY;
import static de.fraunhofer.iosb.ilt.statests.util.EntityType.SENSOR;
import static de.fraunhofer.iosb.ilt.statests.util.EntityType.THING;
import de.fraunhofer.iosb.ilt.statests.util.Utils;
import de.fraunhofer.iosb.ilt.statests.util.mqtt.DeepInsertInfo;
import de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttBatchResult;
import de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper;
import static de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper.WAIT_AFTER_CLEANUP;
import static de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper.waitMillis;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.Callable;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jab
 */
public class Capability8Tests extends AbstractTestClass {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Capability8Tests.class);

    // TODO: Add Actuation & MultiDatastream
    private static final List<EntityType> ENTITY_TYPES_FOR_CREATE = Arrays.asList(
            EntityType.THING,
            EntityType.LOCATION,
            EntityType.SENSOR,
            EntityType.OBSERVED_PROPERTY,
            EntityType.FEATURE_OF_INTEREST,
            EntityType.DATASTREAM,
            EntityType.OBSERVATION,
            EntityType.HISTORICAL_LOCATION);
    private static final List<EntityType> ENTITY_TYPES_FOR_DEEP_INSERT = Arrays.asList(
            EntityType.THING,
            EntityType.DATASTREAM,
            EntityType.OBSERVATION);
    private static final List<EntityType> ENTITY_TYPES_FOR_DELETE = Arrays.asList(
            EntityType.OBSERVATION,
            EntityType.FEATURE_OF_INTEREST,
            EntityType.DATASTREAM,
            EntityType.SENSOR,
            EntityType.OBSERVED_PROPERTY,
            EntityType.HISTORICAL_LOCATION,
            EntityType.LOCATION,
            EntityType.THING);
    private static final Map<EntityType, Object> IDS = new HashMap<>();

    private static EntityHelper entityHelper;
    private static MqttHelper mqttHelper;

    public Capability8Tests(ServerVersion version) throws Exception {
        super(version);
    }

    @Override
    protected void setUpVersion() {
        LOGGER.info("Setting up for version {}.", version.urlPart);
        entityHelper = new EntityHelper(serverSettings.getServiceUrl(version));
        mqttHelper = new MqttHelper(version, serverSettings.getMqttUrl(), serverSettings.getMqttTimeOut());
    }

    @Override
    protected void tearDownVersion() throws Exception {
        entityHelper.deleteEverything();
        entityHelper = null;
        mqttHelper = null;
        IDS.clear();
    }

    /**
     * This method is run after all the tests of this class is run and clean the
     * database.
     */
    @AfterClass
    public static void tearDown() {
        LOGGER.info("Tearing down.");
        entityHelper.deleteEverything();
        entityHelper = null;
        mqttHelper = null;
        IDS.clear();
    }

    @Test
    public void checkSubscribeToEntitySetInsert() {
        LOGGER.info("  checkSubscribeToEntitySetInsert");
        deleteCreatedEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        ENTITY_TYPES_FOR_CREATE.stream().forEach((entityType) -> {
            LOGGER.debug("    {}", entityType);
            MqttBatchResult<Object> result = mqttHelper.executeRequests(getInsertEntityAction(entityType), mqttHelper.getTopic(entityType));
            IDS.put(entityType, result.getActionResult());
            assertJsonEqualsWithLinkResolving(
                    entityHelper.getEntity(entityType, result.getActionResult()),
                    result.getMessages().values().iterator().next(),
                    mqttHelper.getTopic(entityType));
        });

        // Now check if an Observation insert creates a new FoI and posts it over MQTT.
        LOGGER.debug("    FoI creation");
        entityHelper.deleteEntityType(OBSERVATION);
        IDS.remove(OBSERVATION);
        entityHelper.deleteEntityType(FEATURE_OF_INTEREST);
        IDS.remove(FEATURE_OF_INTEREST);

        MqttBatchResult<Object> result = mqttHelper.executeRequests(
                getInsertEntityAction(OBSERVATION),
                mqttHelper.getTopic(OBSERVATION),
                mqttHelper.getTopic(FEATURE_OF_INTEREST));
        IDS.put(OBSERVATION, result.getActionResult());
    }

    @Test
    public void checkSubscribeToEntitySetUpdatePATCH() {
        LOGGER.info("  checkSubscribeToEntitySetUpdatePATCH");
        deleteCreatedEntities();
        createEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        ENTITY_TYPES_FOR_CREATE.stream().forEach((entityType) -> {
            LOGGER.debug("    {}", entityType);
            MqttBatchResult<JSONObject> result = mqttHelper.executeRequests(getUpdatePatchEntityAction(entityType), mqttHelper.getTopic(entityType));
            assertJsonEqualsWithLinkResolving(result.getActionResult(), result.getMessages().values().iterator().next(), mqttHelper.getTopic(entityType));
        });
    }

    @Test
    public void checkSubscribeToEntitySetUpdatePUT() {
        LOGGER.info("  checkSubscribeToEntitySetUpdatePUT");
        deleteCreatedEntities();
        createEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        ENTITY_TYPES_FOR_CREATE.stream().forEach((entityType) -> {
            LOGGER.debug("    {}", entityType);
            MqttBatchResult<JSONObject> result = mqttHelper.executeRequests(getUpdatePutEntityAction(entityType), mqttHelper.getTopic(entityType));
            assertJsonEqualsWithLinkResolving(result.getActionResult(), result.getMessages().values().iterator().next(), mqttHelper.getTopic(entityType));
        });
    }

    @Test
    public void checkSubscribeToEntitySetWithMultipleSelectInsert() {
        LOGGER.info("  checkSubscribeToEntitySetWithMultipleSelectInsert");
        deleteCreatedEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        ENTITY_TYPES_FOR_CREATE.stream().forEach((entityType) -> {
            LOGGER.debug("    {}", entityType);

            List<String> selectedProperties = getSelectedProperties(entityType, true);
            checkSubscribeSelectInsert(entityType, selectedProperties);
            selectedProperties = getSelectedProperties(entityType, false);
            checkSubscribeSelectInsert(entityType, selectedProperties);
        });
    }

    @Test
    public void checkSubscribeToEntitySetWithMultipleSelectUpdatePATCH() {
        LOGGER.info("  checkSubscribeToEntitySetWithMultipleSelectUpdatePATCH");
        deleteCreatedEntities();
        createEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        ENTITY_TYPES_FOR_CREATE.stream().forEach((entityType) -> {
            LOGGER.debug("    {}", entityType);
            List<String> selectedProperties = getSelectedProperties(entityType, true);
            checkSubscribePatch(entityType, selectedProperties);
            selectedProperties = getSelectedProperties(entityType, false);
            checkSubscribePatch(entityType, selectedProperties);
        });
    }

    @Test
    public void checkSubscribeToEntitySetWithMultipleSelectUpdatePUT() {
        LOGGER.info("  checkSubscribeToEntitySetWithMultipleSelectUpdatePUT");
        deleteCreatedEntities();
        createEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        ENTITY_TYPES_FOR_CREATE.stream().forEach((entityType) -> {
            LOGGER.debug("    {}", entityType);
            List<String> selectedProperties = getSelectedProperties(entityType, true);
            checkSubscribePut(entityType, selectedProperties);
            selectedProperties = getSelectedProperties(entityType, false);
            checkSubscribePut(entityType, selectedProperties);
        });
    }

    @Test
    public void checkSubscribeToEntitySetWithRelativeTopicUpdatePUT() {
        LOGGER.info("  checkSubscribeToEntitySetWithRelativeTopicUpdatePUT");
        deleteCreatedEntities();
        createEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        ENTITY_TYPES_FOR_CREATE.stream().forEach((entityType) -> {
            LOGGER.debug("    {}", entityType);
            List<String> relativeTopics = mqttHelper.getRelativeTopicsForEntitySet(entityType, IDS);
            if (!(relativeTopics.isEmpty())) {
                MqttBatchResult<JSONObject> result = mqttHelper.executeRequests(
                        getUpdatePutEntityAction(entityType),
                        relativeTopics.toArray(new String[relativeTopics.size()]));
                result.getMessages().entrySet().stream().forEach((entry) -> {
                    try {
                        // coudl return multiple results so make sure we only get the latest
                        Object lastestId = entityHelper.getLastestEntityId(entityType);
                        String filter = "id%20eq%20" + Utils.quoteIdForUrl(lastestId);
                        JSONObject expectedResult = entityHelper.getEntity(entry.getKey() + "?$filter=" + filter).getJSONArray("value").getJSONObject(0);
                        assertJsonEqualsWithLinkResolving(expectedResult, entry.getValue(), entry.getKey());
                    } catch (JSONException ex) {
                        LOGGER.error("Exception:", ex);
                        Assert.fail("Could not get expected result for MQTT subscription from server: " + ex.getMessage());
                    }
                });
            }
        });
    }

    @Test
    public void checkSubscribeToEntitySetsWithDeepInsert() {
        LOGGER.info("  checkSubscribeToEntitySetsWithDeepInsert");
        deleteCreatedEntities();

        ENTITY_TYPES_FOR_DEEP_INSERT.stream().forEach((EntityType entityType) -> {
            // Give the server a second to send out all the messages created by the setup or previous call.
            waitMillis(WAIT_AFTER_CLEANUP);

            LOGGER.debug("    {}", entityType);
            DeepInsertInfo deepInsertInfo = entityHelper.getDeepInsertInfo(entityType);
            List<String> topics = new ArrayList<>(deepInsertInfo.getSubEntityTypes().size() + 1);
            topics.add(mqttHelper.getTopic(deepInsertInfo.getEntityType()));
            deepInsertInfo.getSubEntityTypes().stream().forEach((subType) -> {
                topics.add(mqttHelper.getTopic(subType));
            });

            MqttBatchResult<Object> result = mqttHelper.executeRequests(
                    getDeepInsertEntityAction(entityType),
                    topics.toArray(new String[topics.size()]));
            IDS.put(entityType, result.getActionResult());
            JSONObject entity = entityHelper.getEntity(deepInsertInfo.getEntityType(), result.getActionResult());
            Optional<JSONObject> rootResult = result.getMessages().entrySet().stream().filter(x -> x.getKey().equals(mqttHelper.getTopic(deepInsertInfo.getEntityType()))).map(x -> x.getValue()).findFirst();
            if (!rootResult.isPresent()) {
                Assert.fail("Deep insert MQTT result is missing root entity");
            }
            assertJsonEqualsWithLinkResolving(entity, rootResult.get(), mqttHelper.getTopic(deepInsertInfo.getEntityType()));
            deepInsertInfo.getSubEntityTypes().stream().forEach((subType) -> {
                JSONObject subEntity = getSubEntityByRoot(deepInsertInfo.getEntityType(), result.getActionResult(), subType);
                Optional<JSONObject> subResult = result.getMessages().entrySet().stream().filter(x -> x.getKey().equals(mqttHelper.getTopic(subType))).map(x -> x.getValue()).findFirst();
                if (!subResult.isPresent()) {
                    Assert.fail("Deep insert MQTT result is missing entity " + subEntity.toString());
                }
                assertJsonEqualsWithLinkResolving(subEntity, subResult.get(), mqttHelper.getTopic(subType));
            });
        });
    }

    @Test
    public void checkSubscribeToEntityUpdatePATCH() {
        LOGGER.info("  checkSubscribeToEntityUpdatePATCH");
        deleteCreatedEntities();
        createEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        ENTITY_TYPES_FOR_CREATE.stream().forEach((entityType) -> {
            LOGGER.debug("    {}", entityType);
            MqttBatchResult<JSONObject> result = mqttHelper.executeRequests(getUpdatePatchEntityAction(entityType), mqttHelper.getTopic(entityType, IDS.get(entityType)));
            assertJsonEqualsWithLinkResolving(result.getActionResult(), result.getMessages().values().iterator().next(), mqttHelper.getTopic(entityType, IDS.get(entityType)));
        });
    }

    @Test
    public void checkSubscribeToEntityUpdatePUT() {
        LOGGER.info("  checkSubscribeToEntityUpdatePUT");
        deleteCreatedEntities();
        createEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        ENTITY_TYPES_FOR_CREATE.stream().forEach((entityType) -> {
            LOGGER.debug("    {}", entityType);
            MqttBatchResult<JSONObject> result = mqttHelper.executeRequests(getUpdatePutEntityAction(entityType), mqttHelper.getTopic(entityType, IDS.get(entityType)));
            assertJsonEqualsWithLinkResolving(result.getActionResult(), result.getMessages().values().iterator().next(), mqttHelper.getTopic(entityType, IDS.get(entityType)));
        });
    }

    @Test
    public void checkSubscribeToEntityWithRelativeTopicUpdatePUT() {
        LOGGER.info("  checkSubscribeToEntityWithRelativeTopicUpdatePUT");
        deleteCreatedEntities();
        createEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        ENTITY_TYPES_FOR_CREATE.stream().forEach((entityType) -> {
            LOGGER.debug("    {}", entityType);
            List<String> relativeTopics = mqttHelper.getRelativeTopicsForEntity(entityType, IDS);
            if (!(relativeTopics.isEmpty())) {
                MqttBatchResult<JSONObject> result = mqttHelper.executeRequests(
                        getUpdatePutEntityAction(entityType),
                        relativeTopics.toArray(new String[relativeTopics.size()]));
                result.getMessages().entrySet().stream().forEach((entry) -> {
                    JSONObject expectedResult = entityHelper.getEntity(entry.getKey());
                    assertJsonEqualsWithLinkResolving(expectedResult, entry.getValue(), entry.getKey());
                });
            }
        });
    }

    @Test
    public void checkSubscribeToPropertyUpdatePATCH() {
        LOGGER.info("  checkSubscribeToPropertyUpdatePATCH");
        deleteCreatedEntities();
        createEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        ENTITY_TYPES_FOR_CREATE.stream().forEach((entityType) -> {
            LOGGER.debug("    {}", entityType);
            Map<String, Object> changes = entityHelper.getEntityChanges(entityType);
            for (String property : entityType.getPropertyNames()) {
                Map<String, Object> propertyChange = new HashMap<>(0);
                Object change = changes.get(property);
                if (change == null) {
                    // No change prepared for this property.
                    continue;
                }
                propertyChange.put(property, change);
                MqttBatchResult<JSONObject> result = mqttHelper.executeRequests(
                        () -> {
                            return entityHelper.patchEntity(entityType, propertyChange, IDS.get(entityType));
                        },
                        mqttHelper.getTopic(entityType, IDS.get(entityType), property));
                assertJsonEqualsWithLinkResolving(new JSONObject(propertyChange), result.getMessages().values().iterator().next(), mqttHelper.getTopic(entityType, IDS.get(entityType), property));
            }
        });
    }

    @Test
    public void checkSubscribeToPropertyUpdatePUT() {
        LOGGER.info("  checkSubscribeToPropertyUpdatePUT");
        deleteCreatedEntities();
        createEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        ENTITY_TYPES_FOR_CREATE.stream().forEach((entityType) -> {
            LOGGER.debug("    {}", entityType);
            Map<String, Object> changes = entityHelper.getEntityChanges(entityType);
            for (String property : entityType.getPropertyNames()) {
                Map<String, Object> propertyChange = new HashMap<>(0);
                Object change = changes.get(property);
                if (change == null) {
                    // No change prepared for this property.
                    continue;
                }
                propertyChange.put(property, change);
                MqttBatchResult<JSONObject> result = mqttHelper.executeRequests(
                        () -> {
                            return entityHelper.putEntity(entityType, propertyChange, IDS.get(entityType));
                        },
                        mqttHelper.getTopic(entityType, IDS.get(entityType), property));
                assertJsonEqualsWithLinkResolving(new JSONObject(propertyChange), result.getMessages().values().iterator().next(), mqttHelper.getTopic(entityType, IDS.get(entityType), property));
            }
        });
    }

    @Test
    public void checkSubscribeToHistoricalLocationSetUpdateThingLocations() {
        LOGGER.info("  checkSubscribeToHistoricalLocationSetUpdateThingLocations");
        deleteCreatedEntities();
        createEntities();

        Callable<JSONObject> updateLocationOfThing;
        try {
            // Create a second location
            final Object locId2 = getInsertEntityAction(LOCATION).call();
            // Give the server a second to send out the messages created by the setup.
            waitMillis(WAIT_AFTER_CLEANUP);

            updateLocationOfThing = () -> {
                return entityHelper.patchEntity(
                        THING,
                        entityHelper.getThingChangesLocation(locId2),
                        IDS.get(THING));
            };
            MqttBatchResult<JSONObject> result = mqttHelper.executeRequests(updateLocationOfThing, mqttHelper.getTopic(HISTORICAL_LOCATION));
            JSONObject lastHistLoc = entityHelper.getAnyEntity(HISTORICAL_LOCATION, "$orderby=time%20desc", 10);
            assertJsonEqualsWithLinkResolving(lastHistLoc, result.getMessages().values().iterator().next(), mqttHelper.getTopic(HISTORICAL_LOCATION));
        } catch (Exception ex) {
            Assert.fail("Could not create second Location: " + ex.getMessage());
        }

    }

    private void createEntities() {
        ENTITY_TYPES_FOR_CREATE.stream().forEach((entityType) -> {
            try {
                IDS.put(entityType, getInsertEntityAction(entityType).call());
            } catch (Exception ex) {
                Assert.fail("Could not create entities");
            }
        });
    }

    private void deleteCreatedEntities() {
        entityHelper.deleteEverything();
        IDS.clear();
    }

    private void prunePropertiesToChanges(List<String> selectedProperties, Map<String, Object> changes) {
        if (changes.size() != selectedProperties.size()) {
            for (Iterator<String> it = selectedProperties.iterator(); it.hasNext();) {
                String property = it.next();
                if (!changes.containsKey(property)) {
                    it.remove();
                }
            }
        }
    }

    private void checkSubscribeSelectInsert(EntityType entityType, List<String> selectedProperties) {
        if (selectedProperties.isEmpty()) {
            // can't test with no selected properties.
            return;
        }
        MqttBatchResult<Object> result = mqttHelper.executeRequests(getInsertEntityAction(entityType), mqttHelper.getTopic(entityType, selectedProperties));
        IDS.put(entityType, result.getActionResult());
        JSONObject entity = entityHelper.getEntity(entityType, result.getActionResult());
        filterEntity(entity, selectedProperties);
        assertJsonEqualsWithLinkResolving(entity, result.getMessages().values().iterator().next(), mqttHelper.getTopic(entityType, selectedProperties));
    }

    private void checkSubscribePut(EntityType entityType, List<String> selectedProperties) {
        if (selectedProperties.isEmpty()) {
            // can't test with no selected properties.
            return;
        }
        Map<String, Object> changes = entityHelper.getEntityChanges(entityType, selectedProperties);
        prunePropertiesToChanges(selectedProperties, changes);
        MqttBatchResult<JSONObject> result = mqttHelper.executeRequests(
                () -> {
                    return entityHelper.putEntity(entityType, changes, IDS.get(entityType));
                },
                mqttHelper.getTopic(entityType, selectedProperties));
        assertJsonEqualsWithLinkResolving(new JSONObject(changes), result.getMessages().values().iterator().next(), mqttHelper.getTopic(entityType, selectedProperties));
    }

    private void checkSubscribePatch(EntityType entityType, List<String> selectedProperties) {
        if (selectedProperties.isEmpty()) {
            // can't test with no selected properties.
            return;
        }
        Map<String, Object> changes = entityHelper.getEntityChanges(entityType, selectedProperties);
        prunePropertiesToChanges(selectedProperties, changes);
        MqttBatchResult<JSONObject> result = mqttHelper.executeRequests(
                () -> {
                    return entityHelper.patchEntity(entityType, changes, IDS.get(entityType));
                },
                mqttHelper.getTopic(entityType, selectedProperties));
        assertJsonEqualsWithLinkResolving(new JSONObject(changes), result.getMessages().values().iterator().next(), mqttHelper.getTopic(entityType, selectedProperties));
    }

    private JSONObject filterEntity(JSONObject entity, List<String> selectedProperties) {
        Iterator iterator = entity.keys();
        while (iterator.hasNext()) {
            String key = iterator.next().toString();
            if (!selectedProperties.contains(key)) {
                iterator.remove();
            }
        }
        return entity;
    }

    private Callable<Object> getDeepInsertEntityAction(EntityType entityType) {
        Callable<Object> trigger = () -> {
            switch (entityType) {
                case THING:
                    return entityHelper.createThingWithDeepInsert();
                case DATASTREAM:
                    return entityHelper.createDatastreamWithDeepInsert(IDS.get(EntityType.THING));
                case OBSERVATION:
                    return entityHelper.createObservationWithDeepInsert(IDS.get(EntityType.DATASTREAM));
            }
            throw new IllegalArgumentException("Unknown EntityType '" + entityType.toString() + "'");
        };
        return trigger;
    }

    private Callable<Object> getInsertEntityAction(EntityType entityType) {
        Callable<Object> trigger = () -> {
            switch (entityType) {
                case THING:
                    return entityHelper.createThing();
                case DATASTREAM:
                    return entityHelper.createDatastream(IDS.get(EntityType.THING), IDS.get(EntityType.OBSERVED_PROPERTY), IDS.get(EntityType.SENSOR));
                case FEATURE_OF_INTEREST:
                    return entityHelper.createFeatureOfInterest();
                case HISTORICAL_LOCATION:
                    return entityHelper.createHistoricalLocation(IDS.get(EntityType.THING), IDS.get(EntityType.LOCATION));
                case LOCATION:
                    return entityHelper.createLocation(IDS.get(EntityType.THING));
                case OBSERVATION:
                    if (IDS.get(EntityType.FEATURE_OF_INTEREST) == null) {
                        return entityHelper.createObservation(IDS.get(EntityType.DATASTREAM));
                    } else {
                        return entityHelper.createObservation(IDS.get(EntityType.DATASTREAM), IDS.get(EntityType.FEATURE_OF_INTEREST));
                    }
                case OBSERVED_PROPERTY:
                    return entityHelper.createObservedProperty();
                case SENSOR:
                    return entityHelper.createSensor();
            }
            throw new IllegalArgumentException("Unknown EntityType '" + entityType.toString() + "'");
        };
        return trigger;
    }

    private String getPathToRelatedEntity(EntityType sourceEntityType, EntityType destinationEntityType) {
        Queue<BFSStructure> queue = new LinkedList<>();
        queue.offer(new BFSStructure(sourceEntityType, ""));
        while (queue.peek() != null) {
            BFSStructure currentElement = queue.poll();
            List<String> relations = currentElement.entityType.getRelations(serverSettings.getExtensions());
            for (String relation : relations) {
                EntityType relatedType = EntityType.getForRelation(relation);
                if (relatedType.equals(destinationEntityType)) {
                    return currentElement.path
                            + (currentElement.path.isEmpty()
                            ? relation
                            : "/" + relation);
                } else {
                    queue.offer(new BFSStructure(relatedType, currentElement.path + (currentElement.path.isEmpty() ? relation : "/" + relation)));
                }
            }
        }
        return "";
    }

    /**
     * Returns half of all entity properties of the given Entity Type.
     *
     * @param entityType The entity type to get the entity properties for.
     * @param even If true, return the even-half of the properties, otherwise
     * the odd-half.
     * @return a list with the property names of half of the entity properties.
     */
    private List<String> getSelectedProperties(EntityType entityType, boolean even) {
        List<String> allProperties = new ArrayList<>(entityType.getEditablePropertyNames());
        List<String> selectedProperties = new ArrayList<>(allProperties.size() / 2);
        for (int i = even ? 0 : 1; i < allProperties.size(); i += 2) {
            selectedProperties.add(allProperties.get(i));
        }
        return selectedProperties;
    }

    private JSONObject getSubEntityByRoot(EntityType rootEntityType, Object rootId, EntityType subtEntityType) {
        try {
            String path = getPathToRelatedEntity(subtEntityType, rootEntityType);
            path = "/" + subtEntityType.getRootEntitySet() + "?$count=true&$filter=" + path + "/id%20eq%20" + Utils.quoteIdForUrl(rootId);
            JSONObject result = entityHelper.getEntity(path);
            if (result.getInt("@iot.count") != 1) {
                Assert.fail("Invalid result with size != 1");
            }
            JSONObject subEntity = result.getJSONArray("value").getJSONObject(0);
            //helper.clearLinks(subEntity);
            return subEntity;
        } catch (JSONException ex) {
            LOGGER.error("Exception:", ex);
            Assert.fail("Invalid JSON: " + ex.getMessage());
        }
        throw new IllegalStateException();
    }

    private Callable<JSONObject> getUpdatePatchEntityAction(EntityType entityType) {
        return () -> {
            return entityHelper.updateEntitywithPATCH(entityType, IDS.get(entityType));
        };
    }

    private Callable<JSONObject> getUpdatePutEntityAction(EntityType entityType) {
        return () -> {
            return entityHelper.updateEntitywithPUT(entityType, IDS.get(entityType));
        };
    }

    private static void assertJsonEqualsWithLinkResolving(JSONObject expected, JSONObject received, String topic) {
        String message = "";
        boolean equals = jsonEqualsWithLinkResolving(expected, received, topic);
        if (!equals) {
            message = "Expected " + expected.toString() + " got " + received.toString() + " for topic " + topic;
        }
        Assert.assertTrue(message, equals);
    }

    private static boolean jsonEqualsWithLinkResolving(JSONArray arr1, JSONArray arr2, String topic) {
        if (arr1.length() != arr2.length()) {
            return false;
        }
        for (int i = 0; i < arr1.length(); i++) {
            Object val1 = arr1.get(i);
            if (val1 instanceof JSONObject) {
                if (!jsonEqualsWithLinkResolving((JSONObject) val1, arr2.getJSONObject(i), topic)) {
                    return false;
                }
            } else if (val1 instanceof JSONArray) {
                if (!jsonEqualsWithLinkResolving((JSONArray) val1, arr2.getJSONArray(i), topic)) {
                    return false;
                }
            } else if (!val1.equals(arr2.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean jsonEqualsWithLinkResolving(JSONObject obj1, JSONObject obj2, String topic) {
        if (obj1 == obj2) {
            return true;
        }
        if (obj1 == null) {
            return false;
        }
        if (obj1.getClass() != obj2.getClass()) {
            return false;
        }
        if (obj1.length() != obj2.length()) {
            return false;
        }
        Iterator iterator = obj1.keys();
        while (iterator.hasNext()) {
            String key = iterator.next().toString();
            if (!obj2.has(key)) {
                return false;
            }
            try {
                Object val1 = obj1.get(key);
                if (val1 == null) {
                    return obj2.get(key) == null;
                } else if (val1 instanceof JSONObject) {
                    if (!jsonEqualsWithLinkResolving((JSONObject) val1, (JSONObject) obj2.getJSONObject(key), topic)) {
                        return false;
                    }
                } else if (val1 instanceof JSONArray) {
                    JSONArray arr1 = (JSONArray) val1;
                    JSONArray arr2 = obj2.getJSONArray(key);
                    if (!jsonEqualsWithLinkResolving(arr1, arr2, topic)) {
                        return false;
                    }
                } else if (key.toLowerCase().endsWith("time")) {
                    if (!checkTimeEquals(val1.toString(), obj2.get(key).toString())) {
                        return false;
                    }
                } else if (topic != null && !topic.isEmpty() && key.endsWith("@iot.navigationLink")) {
                    String version = topic.substring(0, topic.indexOf("/"));

                    String selfLink1 = obj1.getString("@iot.selfLink");
                    URI baseUri1 = URI.create(selfLink1.substring(0, selfLink1.indexOf(version))).resolve(topic);
                    String navLink1 = obj1.getString(key);
                    String absoluteUri1 = baseUri1.resolve(navLink1).toString();

                    String selfLink2 = obj2.getString("@iot.selfLink");
                    URI baseUri2 = URI.create(selfLink2.substring(0, selfLink2.indexOf(version))).resolve(topic);
                    String navLink2 = obj2.getString(key);
                    String absoluteUri2 = baseUri2.resolve(navLink2).toString();
                    if (!absoluteUri1.equals(absoluteUri2)) {
                        return false;
                    }

                } else if (!val1.equals(obj2.get(key))) {
                    return false;
                }
            } catch (JSONException ex) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkTimeEquals(String val1, String val2) {
        if (val1.equals(val2)) {
            return true;
        }

        try {
            DateTime dateTime1 = DateTime.parse(val1);
            DateTime dateTime2 = DateTime.parse(val2);
            return dateTime1.isEqual(dateTime2);
        } catch (Exception ex) {
            // do nothing
        }
        try {
            Interval interval1 = Interval.parse(val1);
            Interval interval2 = Interval.parse(val2);
            return interval1.isEqual(interval2);
        } catch (Exception ex) {
            Assert.fail("time properies could neither be parsed as time nor as interval");
        }

        return false;
    }

    private class BFSStructure {

        EntityType entityType;
        String path;

        public BFSStructure(EntityType entityType, String path) {
            this.entityType = entityType;
            this.path = path;
        }
    }
}
