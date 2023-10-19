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

import static de.fraunhofer.iosb.ilt.statests.util.EntityType.DATASTREAM;
import static de.fraunhofer.iosb.ilt.statests.util.EntityType.FEATURE_OF_INTEREST;
import static de.fraunhofer.iosb.ilt.statests.util.EntityType.HISTORICAL_LOCATION;
import static de.fraunhofer.iosb.ilt.statests.util.EntityType.LOCATION;
import static de.fraunhofer.iosb.ilt.statests.util.EntityType.OBSERVATION;
import static de.fraunhofer.iosb.ilt.statests.util.EntityType.OBSERVED_PROPERTY;
import static de.fraunhofer.iosb.ilt.statests.util.EntityType.SENSOR;
import static de.fraunhofer.iosb.ilt.statests.util.EntityType.THING;
import static de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper.WAIT_AFTER_CLEANUP;
import static de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper.waitMillis;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.util.EntityHelper;
import de.fraunhofer.iosb.ilt.statests.util.EntityType;
import de.fraunhofer.iosb.ilt.statests.util.Utils;
import de.fraunhofer.iosb.ilt.statests.util.mqtt.DeepInsertInfo;
import de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttBatchResult;
import de.fraunhofer.iosb.ilt.statests.util.mqtt.MqttHelper;
import java.net.URI;
import java.text.ParseException;
import java.time.ZonedDateTime;
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
import net.time4j.range.MomentInterval;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TestMethodOrder(MethodOrderer.MethodName.class)
public abstract class Capability8Tests extends AbstractTestClass {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Capability8Tests.class);

    // TODO: Add Actuation & MultiDatastream
    private static final List<EntityType> ENTITY_TYPES_FOR_CREATE = Arrays.asList(
            THING,
            LOCATION,
            SENSOR,
            OBSERVED_PROPERTY,
            FEATURE_OF_INTEREST,
            DATASTREAM,
            OBSERVATION,
            HISTORICAL_LOCATION);
    private static final List<EntityType> ENTITY_TYPES_FOR_DEEP_INSERT = Arrays.asList(
            THING,
            DATASTREAM,
            OBSERVATION);
    private static final Map<EntityType, Object> IDS = new HashMap<>();

    private static EntityHelper entityHelper;
    private static MqttHelper mqttHelper;

    public Capability8Tests(ServerVersion version) {
        super(version);
    }

    @Override
    protected void setUpVersion() {
        LOGGER.info("Setting up for version {}.", version.urlPart);
        entityHelper = new EntityHelper(version, serverSettings);
        mqttHelper = new MqttHelper(version, serverSettings.getMqttUrl(), serverSettings.getMqttTimeOut());
    }

    @Override
    protected void tearDownVersion() {
        entityHelper.deleteEverything();
        entityHelper = null;
        mqttHelper = null;
        IDS.clear();
    }

    /**
     * This method is run after all the tests of this class is run and clean the
     * database.
     */
    @AfterAll
    public static void tearDown() {
        LOGGER.info("Tearing down.");
        entityHelper.deleteEverything();
        entityHelper = null;
        mqttHelper = null;
        IDS.clear();
    }

    @Test
    void check01SubscribeToEntitySetInsert() {
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
    void check02SubscribeToEntitySetUpdatePATCH() {
        LOGGER.info("  checkSubscribeToEntitySetUpdatePATCH");
        deleteCreatedEntities();
        createEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        ENTITY_TYPES_FOR_CREATE.stream().forEach((entityType) -> {
            LOGGER.info("    {}", entityType);
            MqttBatchResult<JsonNode> result = mqttHelper.executeRequests(getUpdatePatchEntityAction(entityType), mqttHelper.getTopic(entityType));
            assertJsonEqualsWithLinkResolving(result.getActionResult(), result.getMessages().values().iterator().next(), mqttHelper.getTopic(entityType));
        });
    }

    @Test
    void check03SubscribeToEntitySetUpdatePUT() {
        LOGGER.info("  checkSubscribeToEntitySetUpdatePUT");
        deleteCreatedEntities();
        createEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        ENTITY_TYPES_FOR_CREATE.stream().forEach((entityType) -> {
            LOGGER.info("    {}", entityType);
            MqttBatchResult<JsonNode> result = mqttHelper.executeRequests(getUpdatePutEntityAction(entityType), mqttHelper.getTopic(entityType));
            assertJsonEqualsWithLinkResolving(result.getActionResult(), result.getMessages().values().iterator().next(), mqttHelper.getTopic(entityType));
        });
    }

    @Test
    void check04SubscribeToEntitySetWithMultipleSelectInsert() {
        LOGGER.info("  checkSubscribeToEntitySetWithMultipleSelectInsert");
        deleteCreatedEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        ENTITY_TYPES_FOR_CREATE.stream().forEach((entityType) -> {
            LOGGER.info("    {}", entityType);

            List<String> selectedProperties = getSelectedProperties(entityType, true);
            checkSubscribeSelectInsert(entityType, selectedProperties);
            selectedProperties = getSelectedProperties(entityType, false);
            checkSubscribeSelectInsert(entityType, selectedProperties);
        });
    }

    @Test
    void check05SubscribeToEntitySetWithMultipleSelectUpdatePATCH() {
        LOGGER.info("  checkSubscribeToEntitySetWithMultipleSelectUpdatePATCH");
        deleteCreatedEntities();
        createEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        ENTITY_TYPES_FOR_CREATE.stream().forEach((entityType) -> {
            LOGGER.info("    {}", entityType);
            List<String> selectedProperties = getSelectedProperties(entityType, true);
            checkSubscribePatch(entityType, selectedProperties);
            selectedProperties = getSelectedProperties(entityType, false);
            checkSubscribePatch(entityType, selectedProperties);
        });
    }

    @Test
    void check06SubscribeToEntitySetWithMultipleSelectUpdatePUT() {
        LOGGER.info("  checkSubscribeToEntitySetWithMultipleSelectUpdatePUT");
        deleteCreatedEntities();
        createEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        ENTITY_TYPES_FOR_CREATE.stream().forEach((entityType) -> {
            LOGGER.info("    {}", entityType);
            List<String> selectedProperties = getSelectedProperties(entityType, true);
            checkSubscribePut(entityType, selectedProperties);
            selectedProperties = getSelectedProperties(entityType, false);
            checkSubscribePut(entityType, selectedProperties);
        });
    }

    @Test
    void check07SubscribeToEntitySetWithRelativeTopicUpdatePUT() {
        LOGGER.info("  checkSubscribeToEntitySetWithRelativeTopicUpdatePUT");
        deleteCreatedEntities();
        createEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        ENTITY_TYPES_FOR_CREATE.stream().forEach((entityType) -> {
            LOGGER.info("    {}", entityType);
            List<String> relativeTopics = mqttHelper.getRelativeTopicsForEntitySet(entityType, IDS);
            if (!(relativeTopics.isEmpty())) {
                MqttBatchResult<JsonNode> result = mqttHelper.executeRequests(
                        getUpdatePutEntityAction(entityType),
                        relativeTopics.toArray(new String[relativeTopics.size()]));
                result.getMessages().entrySet().stream().forEach((entry) -> {
                    // coudl return multiple results so make sure we only get the latest
                    Object lastestId = entityHelper.getLastestEntityId(entityType);
                    String filter = "id%20eq%20" + Utils.quoteForUrl(lastestId);
                    JsonNode expectedResult = entityHelper.getEntity(entry.getKey() + "?$filter=" + filter).get("value").get(0);
                    assertJsonEqualsWithLinkResolving(expectedResult, entry.getValue(), entry.getKey());
                });
            }
        });
    }

    @Test
    void check08SubscribeToEntitySetsWithDeepInsert() {
        LOGGER.info("  checkSubscribeToEntitySetsWithDeepInsert");
        deleteCreatedEntities();

        ENTITY_TYPES_FOR_DEEP_INSERT.stream().forEach((EntityType entityType) -> {
            // Give the server a second to send out all the messages created by the setup or previous call.
            waitMillis(WAIT_AFTER_CLEANUP);

            LOGGER.info("    {}", entityType);
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
            JsonNode entity = entityHelper.getEntity(deepInsertInfo.getEntityType(), result.getActionResult());
            Optional<JsonNode> rootResult = result.getMessages().entrySet().stream().filter(x -> x.getKey().equals(mqttHelper.getTopic(deepInsertInfo.getEntityType()))).map(x -> x.getValue()).findFirst();
            if (!rootResult.isPresent()) {
                fail("Deep insert MQTT result is missing root entity");
            }
            assertJsonEqualsWithLinkResolving(entity, rootResult.get(), mqttHelper.getTopic(deepInsertInfo.getEntityType()));
            deepInsertInfo.getSubEntityTypes().stream().forEach((subType) -> {
                JsonNode subEntity = getSubEntityByRoot(deepInsertInfo.getEntityType(), result.getActionResult(), subType);
                Optional<JsonNode> subResult = result.getMessages().entrySet().stream().filter(x -> x.getKey().equals(mqttHelper.getTopic(subType))).map(x -> x.getValue()).findFirst();
                if (!subResult.isPresent()) {
                    fail("Deep insert MQTT result is missing entity " + subEntity.toString());
                }
                assertJsonEqualsWithLinkResolving(subEntity, subResult.get(), mqttHelper.getTopic(subType));
            });
        });
    }

    @Test
    void check09SubscribeToEntityUpdatePATCH() {
        LOGGER.info("  checkSubscribeToEntityUpdatePATCH");
        deleteCreatedEntities();
        createEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        ENTITY_TYPES_FOR_CREATE.stream().forEach((entityType) -> {
            LOGGER.info("    {}", entityType);
            MqttBatchResult<JsonNode> result = mqttHelper.executeRequests(getUpdatePatchEntityAction(entityType), mqttHelper.getTopic(entityType, IDS.get(entityType)));
            assertJsonEqualsWithLinkResolving(result.getActionResult(), result.getMessages().values().iterator().next(), mqttHelper.getTopic(entityType, IDS.get(entityType)));
        });
    }

    @Test
    void check10SubscribeToEntityUpdatePUT() {
        LOGGER.info("  checkSubscribeToEntityUpdatePUT");
        deleteCreatedEntities();
        createEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        ENTITY_TYPES_FOR_CREATE.stream().forEach((entityType) -> {
            LOGGER.info("    {}", entityType);
            MqttBatchResult<JsonNode> result = mqttHelper.executeRequests(getUpdatePutEntityAction(entityType), mqttHelper.getTopic(entityType, IDS.get(entityType)));
            assertJsonEqualsWithLinkResolving(result.getActionResult(), result.getMessages().values().iterator().next(), mqttHelper.getTopic(entityType, IDS.get(entityType)));
        });
    }

    @Test
    void check11SubscribeToEntityWithRelativeTopicUpdatePUT() {
        LOGGER.info("  checkSubscribeToEntityWithRelativeTopicUpdatePUT");
        deleteCreatedEntities();
        createEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        ENTITY_TYPES_FOR_CREATE.stream().forEach((entityType) -> {
            LOGGER.info("    {}", entityType);
            List<String> relativeTopics = mqttHelper.getRelativeTopicsForEntity(entityType, IDS);
            if (!(relativeTopics.isEmpty())) {
                MqttBatchResult<JsonNode> result = mqttHelper.executeRequests(
                        getUpdatePutEntityAction(entityType),
                        relativeTopics.toArray(new String[relativeTopics.size()]));
                result.getMessages().entrySet().stream().forEach((entry) -> {
                    JsonNode expectedResult = entityHelper.getEntity(entry.getKey());
                    assertJsonEqualsWithLinkResolving(expectedResult, entry.getValue(), entry.getKey());
                });
            }
        });
    }

    @Test
    void check12SubscribeToPropertyUpdatePATCH() {
        LOGGER.info("  checkSubscribeToPropertyUpdatePATCH");
        deleteCreatedEntities();
        createEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        ENTITY_TYPES_FOR_CREATE.stream().forEach((entityType) -> {
            LOGGER.info("    {}", entityType);
            Map<String, Object> changes = entityHelper.getEntityChanges(entityType);
            for (String property : entityType.getPropertyNames()) {
                Map<String, Object> propertyChange = new HashMap<>(0);
                Object change = changes.get(property);
                if (change == null) {
                    // No change prepared for this property.
                    continue;
                }
                propertyChange.put(property, change);
                MqttBatchResult<JsonNode> result = mqttHelper.executeRequests(
                        () -> {
                            return entityHelper.patchEntity(entityType, propertyChange, IDS.get(entityType));
                        },
                        mqttHelper.getTopic(entityType, IDS.get(entityType), property));
                assertJsonEqualsWithLinkResolving(
                        Utils.MAPPER.valueToTree(propertyChange),
                        result.getMessages().values().iterator().next(),
                        mqttHelper.getTopic(entityType, IDS.get(entityType), property));
            }
        });
    }

    @Test
    void check13SubscribeToPropertyUpdatePUT() {
        LOGGER.info("  checkSubscribeToPropertyUpdatePUT");
        deleteCreatedEntities();
        createEntities();
        // Give the server a second to send out the messages created by the setup.
        waitMillis(WAIT_AFTER_CLEANUP);

        ENTITY_TYPES_FOR_CREATE.stream().forEach((entityType) -> {
            LOGGER.info("    {}", entityType);
            Map<String, Object> changes = entityHelper.getEntityChanges(entityType);
            for (String property : entityType.getPropertyNames()) {
                Map<String, Object> propertyChange = new HashMap<>(0);
                Object change = changes.get(property);
                if (change == null) {
                    // No change prepared for this property.
                    continue;
                }
                propertyChange.put(property, change);
                MqttBatchResult<JsonNode> result = mqttHelper.executeRequests(
                        () -> {
                            return entityHelper.putEntity(entityType, propertyChange, IDS.get(entityType));
                        },
                        mqttHelper.getTopic(entityType, IDS.get(entityType), property));
                assertJsonEqualsWithLinkResolving(
                        Utils.MAPPER.valueToTree(propertyChange),
                        result.getMessages().values().iterator().next(),
                        mqttHelper.getTopic(entityType, IDS.get(entityType), property));
            }
        });
    }

    @Test
    void check14SubscribeToHistoricalLocationSetUpdateThingLocations() {
        LOGGER.info("  checkSubscribeToHistoricalLocationSetUpdateThingLocations");
        deleteCreatedEntities();
        createEntities();

        Callable<JsonNode> updateLocationOfThing;
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
            MqttBatchResult<JsonNode> result = mqttHelper.executeRequests(updateLocationOfThing, mqttHelper.getTopic(HISTORICAL_LOCATION));
            JsonNode lastHistLoc = entityHelper.getAnyEntity(HISTORICAL_LOCATION, "$orderby=time%20desc", 10);
            assertJsonEqualsWithLinkResolving(lastHistLoc, result.getMessages().values().iterator().next(), mqttHelper.getTopic(HISTORICAL_LOCATION));
        } catch (Exception ex) {
            fail("Could not create second Location: " + ex.getMessage());
        }

    }

    private void createEntities() {
        ENTITY_TYPES_FOR_CREATE.stream().forEach((entityType) -> {
            try {
                IDS.put(entityType, getInsertEntityAction(entityType).call());
            } catch (Exception ex) {
                fail("Could not create entities");
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
        JsonNode entity = entityHelper.getEntity(entityType, result.getActionResult());
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
        MqttBatchResult<JsonNode> result = mqttHelper.executeRequests(
                () -> {
                    return entityHelper.putEntity(entityType, changes, IDS.get(entityType));
                },
                mqttHelper.getTopic(entityType, selectedProperties));
        assertJsonEqualsWithLinkResolving(
                Utils.MAPPER.valueToTree(changes),
                result.getMessages().values().iterator().next(),
                mqttHelper.getTopic(entityType, selectedProperties));
    }

    private void checkSubscribePatch(EntityType entityType, List<String> selectedProperties) {
        if (selectedProperties.isEmpty()) {
            // can't test with no selected properties.
            return;
        }
        Map<String, Object> changes = entityHelper.getEntityChanges(entityType, selectedProperties);
        prunePropertiesToChanges(selectedProperties, changes);
        MqttBatchResult<JsonNode> result = mqttHelper.executeRequests(
                () -> {
                    return entityHelper.patchEntity(entityType, changes, IDS.get(entityType));
                },
                mqttHelper.getTopic(entityType, selectedProperties));
        assertJsonEqualsWithLinkResolving(
                Utils.MAPPER.valueToTree(changes),
                result.getMessages().values().iterator().next(),
                mqttHelper.getTopic(entityType, selectedProperties));
    }

    private JsonNode filterEntity(JsonNode entity, List<String> selectedProperties) {
        Iterator iterator = entity.fieldNames();
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
                    return entityHelper.createDatastreamWithDeepInsert(IDS.get(THING));
                case OBSERVATION:
                    return entityHelper.createObservationWithDeepInsert(IDS.get(DATASTREAM));
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
                    return entityHelper.createDatastream(IDS.get(THING), IDS.get(OBSERVED_PROPERTY), IDS.get(SENSOR));
                case FEATURE_OF_INTEREST:
                    return entityHelper.createFeatureOfInterest();
                case HISTORICAL_LOCATION:
                    return entityHelper.createHistoricalLocation(IDS.get(THING), IDS.get(LOCATION));
                case LOCATION:
                    return entityHelper.createLocation(IDS.get(THING));
                case OBSERVATION:
                    if (IDS.get(FEATURE_OF_INTEREST) == null) {
                        return entityHelper.createObservation(IDS.get(DATASTREAM));
                    } else {
                        return entityHelper.createObservation(IDS.get(DATASTREAM), IDS.get(FEATURE_OF_INTEREST));
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
                    return currentElement.path + (currentElement.path.isEmpty() ? relation : "/" + relation);
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

    private JsonNode getSubEntityByRoot(EntityType rootEntityType, Object rootId, EntityType subtEntityType) {
        String path = getPathToRelatedEntity(subtEntityType, rootEntityType);
        path = "/" + subtEntityType.getRootEntitySet() + "?$count=true&$filter=" + path + "/id%20eq%20" + Utils.quoteForUrl(rootId);
        JsonNode result = entityHelper.getEntity(path);
        if (result.get("@iot.count").asInt() != 1) {
            fail("Invalid result with size != 1");
        }
        JsonNode subEntity = result.get("value").get(0);
        //helper.clearLinks(subEntity);
        return subEntity;
    }

    private Callable<JsonNode> getUpdatePatchEntityAction(EntityType entityType) {
        return () -> {
            return entityHelper.updateEntitywithPATCH(entityType, IDS.get(entityType));
        };
    }

    private Callable<JsonNode> getUpdatePutEntityAction(EntityType entityType) {
        return () -> {
            return entityHelper.updateEntitywithPUT(entityType, IDS.get(entityType));
        };
    }

    private static void assertJsonEqualsWithLinkResolving(JsonNode expected, JsonNode received, String topic) {
        String message = "";
        boolean equals = jsonEqualsWithLinkResolving(expected, received, topic);
        if (!equals) {
            message = "Expected " + expected.toString() + " got " + received.toString() + " for topic " + topic;
        }
        assertTrue(equals, message);
    }

    private static boolean jsonEqualsWithLinkResolving(JsonNode node1, JsonNode node2, String topic) {
        if (node1 instanceof ObjectNode obj1 && node2 instanceof ObjectNode obj2) {
            return jsonEqualsWithLinkResolving(obj1, obj2, topic);
        }
        if (node1 instanceof ArrayNode arr1 && node2 instanceof ArrayNode arr2) {
            return jsonEqualsWithLinkResolving(arr1, arr2, topic);
        }
        return false;
    }

    private static boolean jsonEqualsWithLinkResolving(ArrayNode arr1, ArrayNode arr2, String topic) {
        if (arr1.size() != arr2.size()) {
            return false;
        }
        for (int i = 0; i < arr1.size(); i++) {
            Object val1 = arr1.get(i);
            if (val1 instanceof ObjectNode) {
                if (!jsonEqualsWithLinkResolving((ObjectNode) val1, (ObjectNode) arr2.get(i), topic)) {
                    return false;
                }
            } else if (val1 instanceof ArrayNode) {
                if (!jsonEqualsWithLinkResolving((ArrayNode) val1, (ArrayNode) arr2.get(i), topic)) {
                    return false;
                }
            } else if (!val1.equals(arr2.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean jsonEqualsWithLinkResolving(ObjectNode obj1, ObjectNode obj2, String topic) {
        if (obj1 == obj2) {
            return true;
        }
        if (obj1 == null) {
            return false;
        }
        if (obj1.getClass() != obj2.getClass()) {
            return false;
        }
        if (obj1.size() != obj2.size()) {
            return false;
        }
        Iterator<String> iterator = obj1.fieldNames();
        while (iterator.hasNext()) {
            String key = iterator.next();
            if (!obj2.has(key)) {
                return false;
            }
            JsonNode val1 = obj1.get(key);
            if (val1 == null) {
                return obj2.get(key) == null;
            } else if (val1 instanceof ObjectNode) {
                if (!jsonEqualsWithLinkResolving((ObjectNode) val1, (ObjectNode) obj2.get(key), topic)) {
                    return false;
                }
            } else if (val1 instanceof ArrayNode) {
                ArrayNode arr1 = (ArrayNode) val1;
                ArrayNode arr2 = (ArrayNode) obj2.get(key);
                if (!jsonEqualsWithLinkResolving(arr1, arr2, topic)) {
                    return false;
                }
            } else if (key.toLowerCase().endsWith("time")) {
                if (!checkTimeEquals(val1.textValue(), obj2.get(key).textValue())) {
                    return false;
                }
            } else if (topic != null && !topic.isEmpty() && key.endsWith("@iot.navigationLink")) {
                String version = topic.substring(0, topic.indexOf("/"));

                String selfLink1 = obj1.get("@iot.selfLink").textValue();
                URI baseUri1 = URI.create(selfLink1.substring(0, selfLink1.indexOf(version))).resolve(topic);
                String navLink1 = obj1.get(key).textValue();
                String absoluteUri1 = baseUri1.resolve(navLink1).toString();

                String selfLink2 = obj2.get("@iot.selfLink").textValue();
                URI baseUri2 = URI.create(selfLink2.substring(0, selfLink2.indexOf(version))).resolve(topic);
                String navLink2 = obj2.get(key).textValue();
                String absoluteUri2 = baseUri2.resolve(navLink2).toString();
                if (!absoluteUri1.equals(absoluteUri2)) {
                    return false;
                }

            } else if (!val1.equals(obj2.get(key))) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkTimeEquals(String val1, String val2) {
        if (val1 == null) {
            return val2 == null;
        }
        if (val1.equals(val2)) {
            return true;
        }

        try {
            ZonedDateTime dateTime1 = ZonedDateTime.parse(val1);
            ZonedDateTime dateTime2 = ZonedDateTime.parse(val2);
            return dateTime1.isEqual(dateTime2);
        } catch (Exception ex) {
            // do nothing
        }
        try {
            MomentInterval interval1 = MomentInterval.parseISO(val1);
            MomentInterval interval2 = MomentInterval.parseISO(val2);
            return interval1.equals(interval2);
        } catch (RuntimeException | ParseException ex) {
            fail("time properies could neither be parsed as time nor as interval");
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
