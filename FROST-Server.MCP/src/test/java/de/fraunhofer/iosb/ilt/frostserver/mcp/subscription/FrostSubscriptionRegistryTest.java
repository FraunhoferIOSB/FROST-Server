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
package de.fraunhofer.iosb.ilt.frostserver.mcp.subscription;

import static de.fraunhofer.iosb.ilt.frostserver.util.Constants.VALUE_ID_TYPE_LONG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.fraunhofer.iosb.ilt.frostserver.formatter.PluginResultFormatInternal;
import de.fraunhofer.iosb.ilt.frostserver.mcp.TestModelMcp;
import de.fraunhofer.iosb.ilt.frostserver.mcp.util.StaJsonHelper;
import de.fraunhofer.iosb.ilt.frostserver.messagebus.MessageBusFactory;
import de.fraunhofer.iosb.ilt.frostserver.model.DefaultEntity;
import de.fraunhofer.iosb.ilt.frostserver.model.EntityChangedMessage;
import de.fraunhofer.iosb.ilt.frostserver.model.core.PkValue;
import de.fraunhofer.iosb.ilt.frostserver.request.Version;
import de.fraunhofer.iosb.ilt.frostserver.service.PluginManager;
import de.fraunhofer.iosb.ilt.frostserver.settings.CoreSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.McpSettings;
import de.fraunhofer.iosb.ilt.frostserver.settings.PersistenceSettings;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import tools.jackson.databind.JsonNode;

/**
 * Unit tests for the MCP subscription registry and factory: gates, canonical
 * topic dedupe, the per-session cap, the per-entity visibility check,
 * unsubscribe flows, session dropping and the event path through the
 * InternalMessageBus with a capturing sink. Uses the TestModelMcp model and
 * the TestPmMcp dummy persistence manager, mirroring the core module's
 * MqttManagerTest idiom.
 */
class FrostSubscriptionRegistryTest {

    private static final String SESSION_A = "session-a";
    private static final String SESSION_B = "session-b";
    private static final String SESSION_GONE = "session-gone";

    private CoreSettings coreSettings;
    private TestModelMcp testModel;
    private CapturingSink sink;
    private FrostSubscriptionRegistry registry;
    private volatile boolean sessionPresent = true;

    @BeforeEach
    void init() {
        final Properties properties = new Properties();
        properties.put(CoreSettings.TAG_SERVICE_ROOT_URL, "http://localhost/");
        properties.put(CoreSettings.TAG_TEMP_PATH, System.getProperty("java.io.tmpdir"));
        properties.put(CoreSettings.PREFIX_PERSISTENCE + PersistenceSettings.TAG_IMPLEMENTATION_CLASS, TestPmMcp.class.getName());
        // The model plugin defines the "int" version, so it must load before
        // the result-format plugin: formatters are only registered when their
        // version is already defined.
        properties.put(CoreSettings.PREFIX_PLUGINS + PluginManager.TAG_PROVIDED_PLUGINS, TestModelMcp.class.getName() + "," + PluginResultFormatInternal.class.getName());
        properties.put(McpSettings.PREFIX_MCP + McpSettings.TAG_MAX_SUBSCRIPTIONS_PER_SESSION, "3");

        coreSettings = new CoreSettings(properties);
        testModel = coreSettings.getPluginManager().getPlugin(TestModelMcp.class);
        testModel.initModel(coreSettings.getModelRegistry(), VALUE_ID_TYPE_LONG);
        coreSettings.getModelRegistry().initFinalise();

        // The server default must not leak default $orderby entries into
        // subscription queries via Query.validate(): the gates reject any
        // $orderby.
        Assertions.assertTrue(coreSettings.getQueryDefaults().isAlwaysOrder());

        MessageBusFactory.createMessageBus(coreSettings);
        sessionPresent = true;
        sink = new CapturingSink(Set.of(SESSION_GONE));
        registry = new FrostSubscriptionRegistry(coreSettings, sink, sessionId -> sessionPresent);
    }

    @AfterEach
    void cleanup() {
        registry.shutdown();
        coreSettings.getMessageBus().stop();
    }

    @Timeout(60)
    @Test
    void testEntitySetSubscriptionDeliversCreateUpdateDelete() throws Exception {
        final FrostSubscriptionResult result = subscribe(SESSION_A, "Rooms", null, null);
        assertFalse(result.alreadyExisted());
        assertTrue(result.sessionActive());
        assertEquals(1, registry.registrationCount());

        sendMessage(creEvent(7, null, EntityChangedMessage.Type.CREATE));
        awaitCount(1);
        assertEnvelope(sink.getNotifications().peek(), result.subscriptionId(), "create", "Rooms");
        assertTrue(sink.getNotifications().peek().notification().data().contains("\"truncated\":false"));

        sink.clear();
        sendMessage(creEvent(7, null, EntityChangedMessage.Type.UPDATE));
        awaitCount(1);
        assertEnvelope(sink.getNotifications().peek(), result.subscriptionId(), "update", "Rooms");
        assertTrue(sink.getNotifications().peek().notification().data().contains("\"entity\""));

        sink.clear();
        sendMessage(creEvent(7, null, EntityChangedMessage.Type.DELETE));
        awaitCount(1);
        assertEnvelope(sink.getNotifications().peek(), result.subscriptionId(), "delete", "Rooms");
    }

    @Timeout(60)
    @Test
    void testBareSetIgnoresOtherTypes() throws Exception {
        subscribe(SESSION_A, "Houses", null, null);
        sendMessage(entityEvent(testModel.etHouse, 7, EntityChangedMessage.Type.CREATE));
        awaitCount(1, "A House event must notify a Houses subscription.");

        sink.clear();
        sendMessage(creEvent(8, 2L, EntityChangedMessage.Type.CREATE));
        awaitQuiesce();
        assertEquals(0, sink.size(), "A Room event must not notify a Houses subscription.");
    }

    @Timeout(60)
    @Test
    void testParentedSetMatchesOnlyItsParent() throws Exception {
        subscribe(SESSION_A, "Houses(1)/Rooms", null, null);
        sendMessage(creEvent(7, 1L, EntityChangedMessage.Type.CREATE));
        awaitCount(1, "Room of House 1 must notify.");

        sink.clear();
        sendMessage(creEvent(8, 2L, EntityChangedMessage.Type.CREATE));
        awaitQuiesce();
        assertEquals(0, sink.size(), "Room of House 2 must not notify.");
    }

    @Timeout(60)
    @Test
    void testEntitySubscriptionPkMatcherAndVisibility() throws Exception {
        final FrostSubscriptionResult ok = subscribe(SESSION_A, "Rooms(1)", null, null);
        assertFalse(ok.alreadyExisted());
        assertEquals("int/Rooms(1)", ok.topic(), "The entity subscription topic carries no query.");

        // Entity -1 is absent from the dummy store → not visible → 404.
        final FrostSubscriptionException notFound = assertThrows(
                FrostSubscriptionException.class, () -> subscribe(SESSION_A, "Rooms(-1)", null, null));
        assertEquals(404, notFound.getCode());
        assertTrue(notFound.getMessage().contains("not found"), notFound.getMessage());

        sendMessage(creEvent(1, 1L, EntityChangedMessage.Type.CREATE));
        awaitCount(1);
        assertEnvelope(sink.getNotifications().peek(), ok.subscriptionId(), "create", "Rooms");

        sink.clear();
        sendMessage(creEvent(2, 1L, EntityChangedMessage.Type.CREATE));
        awaitQuiesce();
        assertEquals(0, sink.size(), "Another Room must not notify the entity subscription.");
    }

    @Timeout(60)
    @Test
    void testDedupeSessionsAndSelect() throws Exception {
        final FrostSubscriptionResult first = subscribe(SESSION_A, "Rooms", null, null);
        final FrostSubscriptionResult duplicate = subscribe(SESSION_A, "Rooms", null, null);
        assertTrue(duplicate.alreadyExisted(), "Identical subscription must dedupe.");
        assertEquals(first.subscriptionId(), duplicate.subscriptionId());
        assertEquals(1, registry.registrationCount());

        // Different select → different canonical topic → extra registration.
        subscribe(SESSION_A, "Rooms", null, List.of("id", "name"));
        assertEquals(2, registry.registrationCount());

        // Same subscription in another session is separate.
        subscribe(SESSION_B, "Rooms", null, null);
        assertEquals(3, registry.registrationCount());

        sendMessage(creEvent(7, 1L, EntityChangedMessage.Type.CREATE));
        // The bare-set subscriptions of SESSION_A and SESSION_B match
        // cheaply; the $select variant of SESSION_A also matches ($select
        // does not build a match expression). The $filter subscriptions
        // would need the persistence manager and never match in this test.
        awaitCount(3);
    }

    @Timeout(60)
    @Test
    void testSelectTopicRoundTrip() throws Exception {
        final FrostSubscriptionResult selected = subscribe(SESSION_A, "Rooms", null, List.of("id", "name"));
        assertTrue(selected.topic().startsWith("int/Rooms?"), "Topic: " + selected.topic());
        assertTrue(selected.topic().contains("$select=@iot.id,name"), "Topic: " + selected.topic());
        assertFalse(selected.topic().contains("$orderby"), "Default orderby must not leak into the topic: " + selected.topic());
        assertEquals(1, registry.registrationCount());

        final FrostSubscriptionResult filtered = subscribe(SESSION_B, "Rooms", "name eq 'n7'", null);
        assertTrue(filtered.topic().contains("$filter=(name"), "Topic: " + filtered.topic());
        assertFalse(filtered.topic().contains("$orderby"), "Default orderby must not leak into the topic: " + filtered.topic());
    }

    @Timeout(60)
    @Test
    void testGates() throws Exception {
        // Entity path with query options.
        assertInvalidSubscribe(() -> subscribe(SESSION_A, "Rooms(1)", "name eq 'x'", null));
        // Property path.
        assertInvalidSubscribe(() -> subscribe(SESSION_A, "Rooms(1)/name", null, null));
        // Unknown entity set.
        assertInvalidSubscribe(() -> subscribe(SESSION_A, "Wombats", null, null));
        // Empty and null paths.
        assertInvalidSubscribe(() -> subscribe(SESSION_A, "", null, null));
        assertInvalidSubscribe(() -> subscribe(SESSION_A, null, null, null));
        assertEquals(0, registry.registrationCount(), "Failed subscribes must not register.");
    }

    @Timeout(60)
    @Test
    void testCap() throws Exception {
        subscribe(SESSION_A, "Rooms", null, null);
        subscribe(SESSION_A, "Houses", null, null);
        subscribe(SESSION_A, "Houses(1)/Rooms", null, null);
        final FrostSubscriptionException capped = assertThrows(
                FrostSubscriptionException.class, () -> subscribe(SESSION_A, "Rooms(2)", null, null));
        assertEquals(429, capped.getCode());
        assertTrue(capped.getMessage().contains("mcp.maxSubscriptionsPerSession"), capped.getMessage());

        // Dedupe happens before the cap: an identical subscription is fine...
        subscribe(SESSION_A, "Rooms", null, null);
        assertEquals(3, registry.registrationCount());

        // ... and other sessions have their own cap.
        subscribe(SESSION_B, "Rooms", null, null);
        assertEquals(4, registry.registrationCount());
    }

    @Timeout(60)
    @Test
    void testUnsubscribe() throws Exception {
        final FrostSubscriptionResult one = subscribe(SESSION_A, "Rooms", null, null);
        subscribe(SESSION_A, "Houses", null, null);
        assertEquals(2, registry.registrationCount());

        assertEquals(1, registry.unsubscribe(SESSION_A, one.subscriptionId()));
        assertEquals(1, registry.registrationCount());
        assertEquals(0, registry.unsubscribe(SESSION_A, one.subscriptionId()), "Unknown id must no-op.");
        assertEquals(0, registry.unsubscribe(SESSION_B, one.subscriptionId()), "Other sessions are not affected.");
        assertEquals(1, registry.unsubscribe(SESSION_A, null), "Null id removes all remaining.");
        assertEquals(0, registry.registrationCount());
        assertEquals(0, registry.unsubscribe(SESSION_A, null), "Unsubscribing an empty session no-ops.");
    }

    @Timeout(60)
    @Test
    void testEventsStopAfterUnsubscribe() throws Exception {
        subscribe(SESSION_A, "Rooms", null, null);
        sendMessage(creEvent(7, 1L, EntityChangedMessage.Type.CREATE));
        awaitCount(1);
        assertEquals(1, registry.unsubscribe(SESSION_A, null));

        sink.clear();
        sendMessage(creEvent(8, 1L, EntityChangedMessage.Type.CREATE));
        awaitQuiesce();
        assertEquals(0, sink.size(), "No notifications after unsubscribe.");
    }

    @Timeout(60)
    @Test
    void testDeliveryFailureDropsSession() throws Exception {
        // SESSION_GONE deliveries fail in the sink → the registry drops that
        // session's registrations; SESSION_A keeps receiving.
        subscribe(SESSION_GONE, "Rooms", null, null);
        subscribe(SESSION_A, "Rooms", null, null);
        sendMessage(creEvent(7, 1L, EntityChangedMessage.Type.CREATE));
        awaitCount(1);
        assertEquals(SESSION_A, sink.getNotifications().peek().sessionId());
        awaitQuiesce();
        assertEquals(1, registry.registrationCount(), "The failing session's subscriptions must be dropped.");
        assertEquals(List.of(), registry.subscriptionIds(SESSION_GONE));
    }

    @Timeout(60)
    @Test
    void testMissingSessionDropsRegistrations() throws Exception {
        subscribe(SESSION_A, "Rooms", null, null);
        assertEquals(1, registry.registrationCount());
        sessionPresent = false;
        sendMessage(creEvent(7, 1L, EntityChangedMessage.Type.CREATE));
        awaitQuiesce();
        assertEquals(0, registry.registrationCount(), "Subscriptions of gone sessions must be dropped.");
    }

    private void assertInvalidSubscribe(SubscriptionCall call) {
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, call::run);
        assertNotNull(ex.getMessage());
    }

    @FunctionalInterface
    private interface SubscriptionCall {

        void run() throws Exception;

    }

    private FrostSubscriptionResult subscribe(String sessionId, String path, String filter, List<String> select) throws FrostSubscriptionException {
        return registry.subscribe(sessionId, PrincipalExtended.ANONYMOUS_PRINCIPAL, Version.INTERNAL, path, filter, select);
    }

    private void sendMessage(EntityChangedMessage message) {
        coreSettings.getMessageBus().sendMessage(message);
    }

    private EntityChangedMessage creEvent(long id, Long parentId, EntityChangedMessage.Type eventType) {
        final DefaultEntity entity = new DefaultEntity(testModel.etRoom, PkValue.of(id))
                .setProperty(testModel.epName, "n" + id)
                .setProperty(testModel.epValue, 5L);
        if (parentId != null) {
            entity.setProperty(testModel.npRoomHouse, new DefaultEntity(testModel.etHouse, PkValue.of(parentId)));
        }
        return new EntityChangedMessage()
                .setEventType(eventType)
                .setEntity(entity);
    }

    private EntityChangedMessage entityEvent(de.fraunhofer.iosb.ilt.frostserver.model.EntityType entityType, long id, EntityChangedMessage.Type eventType) {
        final DefaultEntity entity = new DefaultEntity(entityType, PkValue.of(id))
                .setProperty(testModel.epName, "n" + id);
        return new EntityChangedMessage()
                .setEventType(eventType)
                .setEntity(entity);
    }

    private void awaitCount(int count) throws InterruptedException {
        awaitCount(count, "");
    }

    private void awaitCount(int count, String message) throws InterruptedException {
        final long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30);
        while (sink.size() < count && System.currentTimeMillis() < deadline) {
            Thread.sleep(50);
        }
        assertEquals(count, sink.size(), message);
    }

    private void awaitQuiesce() throws InterruptedException {
        Thread.sleep(500);
    }

    private void assertEnvelope(CapturingSink.Captured captured, long subscriptionId, String eventType, String entityType) {
        assertEquals(SESSION_A, captured.sessionId());
        assertEquals(FrostSubscriptionRegistry.LOGGER_NAME, captured.notification().logger());
        assertEquals(McpSchema.LoggingLevel.INFO, captured.notification().level());
        final JsonNode envelope = StaJsonHelper.parseOrNull(captured.notification().data());
        assertNotNull(envelope, "The envelope must be valid JSON.");
        assertEquals(subscriptionId, envelope.get("subscriptionId").asLong());
        assertEquals(eventType, envelope.get("eventType").asText());
        assertEquals(entityType, envelope.get("entityType").asText());
    }

}
