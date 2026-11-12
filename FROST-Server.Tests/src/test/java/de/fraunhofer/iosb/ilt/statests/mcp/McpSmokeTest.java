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
package de.fraunhofer.iosb.ilt.statests.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.fraunhofer.iosb.ilt.frostclient.json.SimpleJsonMapper;
import de.fraunhofer.iosb.ilt.frostclient.utils.TypeReferencesHelper;
import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.TestCore;
import de.fraunhofer.iosb.ilt.statests.util.HTTPMethods;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.ResourceContents;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;

/**
 * Smoke test for the FROST MCP server: boots the stand-alone FrostMcpServer
 * alongside the regular HTTP/MQTT test servers (same database, no bus of its
 * own) and drives it with the MCP Java SDK client over the streamable-HTTP
 * transport: initialize handshake, tools/list, a full CRUD round-trip through
 * the frost_* tools, error mapping, the frost:// resources, and a subscription
 * round-trip with a logging-notification consumer.
 */
public class McpSmokeTest extends AbstractTestClass {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(McpSmokeTest.class);

    private static final Set<String> EXPECTED_TOOLS = Set.of(
            "frost_get", "frost_query", "frost_create",
            "frost_update", "frost_replace", "frost_delete",
            "frost_subscribe", "frost_unsubscribe");

    private static final ConcurrentLinkedQueue<LoggingMessageNotification> loggingNotifications = new ConcurrentLinkedQueue<>();

    private static McpSyncClient mcpClient;

    public McpSmokeTest() {
        super(ServerVersion.V_1_1, Map.of(
                TestCore.KEY_MCP_ENABLED, "true",
                "auth.allowAnonymousRead", "true"));
    }

    @Override
    protected void setUpVersion() {
        // The servers are up (the constructor booted them); connect now.
        assertNotNull(serverSettings, "Server settings must be initialised.");
        String mcpUrl = serverSettings.getMcpUrl();
        assertFalse(mcpUrl.isEmpty(), "The MCP server must have been started.");
        LOGGER.info("Connecting MCP client to {}", mcpUrl);
        mcpClient = McpClient.sync(
                HttpClientStreamableHttpTransport.builder(mcpUrl).build())
                .clientInfo(McpSchema.Implementation.builder("FROST-MCP-SmokeTest", "1.0.0").build())
                .loggingConsumer(loggingNotifications::add)
                .build();
        mcpClient.initialize();
    }

    @AfterAll
    static void stats() {
        cleanup();
        HTTPMethods.expectStats("McpSmokeTest", 0, 1, 0, 0, 0);
    }

    public static void cleanup() {
        if (mcpClient != null) {
            mcpClient.close();
            mcpClient = null;
        }
        loggingNotifications.clear();
    }

    @Test
    public void testInitializeHandshake() {
        LOGGER.info("  testInitializeHandshake");
        // initialize() ran during setUpVersion; a working tools/list proves it.
        assertNotNull(mcpClient, "Client must be initialised.");
        McpSchema.ListToolsResult tools = mcpClient.listTools();
        assertNotNull(tools, "Server must answer tools/list after initialize.");
    }

    @Test
    public void testToolsList() {
        LOGGER.info("  testToolsList");
        List<Tool> tools = mcpClient.listTools().tools();
        Set<String> names = tools.stream().map(Tool::name).collect(Collectors.toSet());
        assertEquals(EXPECTED_TOOLS, names, "All eight generic tools must be listed.");
        for (Tool tool : tools) {
            assertTrue(tool.description() != null && !tool.description().isBlank(),
                    "Tool " + tool.name() + " must carry a description.");
            Map<String, Object> schema = tool.inputSchema();
            assertEquals("object", schema.get("type"), "Tool " + tool.name() + " input schema must be an object.");
        }
    }

    @Test
    public void testCrudRoundTrip() {
        LOGGER.info("  testCrudRoundTrip");
        // CREATE a Thing
        Map<String, Object> thing = new LinkedHashMap<>();
        thing.put("name", "MCP Smoke Test Thing");
        thing.put("description", "Created by the MCP smoke test.");
        thing.put("properties", Map.of("source", "mcp-smoke-test"));
        CallToolResult created = callTool("frost_create", Map.of(
                "path", "Things",
                "entity", thing,
                "version", "v1.1"));
        assertFalse(isError(created), "Creating a Thing must succeed: " + textOf(created));
        long thingId = idFrom(created);
        assertTrue(thingId > 0, "Created entity must carry a positive @iot.id.");

        // GET it back
        CallToolResult fetched = callTool("frost_get", Map.of(
                "path", "Things(" + thingId + ")"));
        assertFalse(isError(fetched));
        Map<String, Object> fetchedEntity = structuredMap(fetched);
        assertEquals("MCP Smoke Test Thing", fetchedEntity.get("name"));

        // QUERY with typed arguments
        CallToolResult queried = callTool("frost_query", Map.of(
                "entitySet", "Things",
                "filter", "name eq 'MCP Smoke Test Thing'",
                "select", List.of("id", "name"),
                "count", true));
        assertFalse(isError(queried));
        Map<String, Object> queryResult = structuredMap(queried);
        assertEquals(1, ((Number) queryResult.get("@iot.count")).intValue(), "Query must find the created Thing.");

        // UPDATE with a patch
        CallToolResult patched = callTool("frost_update", Map.of(
                "path", "Things(" + thingId + ")",
                "mode", "patch",
                "changes", Map.of("description", "Patched by the MCP smoke test.")));
        assertFalse(isError(patched), "Patching must succeed: " + textOf(patched));

        // UPDATE with a JSON Patch
        CallToolResult changeset = callTool("frost_update", Map.of(
                "path", "Things(" + thingId + ")",
                "mode", "changeset",
                "changes", List.of(Map.of("op", "replace", "path", "/name", "value", "MCP Smoke Test Thing v2"))));
        assertFalse(isError(changeset), "JSON-Patching must succeed: " + textOf(changeset));

        // REPLACE the whole entity
        CallToolResult replaced = callTool("frost_replace", Map.of(
                "path", "Things(" + thingId + ")",
                "entity", Map.of(
                        "name", "MCP Smoke Test Thing v3",
                        "description", "Replaced by the MCP smoke test.")));
        assertFalse(isError(replaced), "Replacing must succeed: " + textOf(replaced));
        CallToolResult refetched = callTool("frost_get", Map.of("path", "Things(" + thingId + ")"));
        assertEquals("MCP Smoke Test Thing v3", structuredMap(refetched).get("name"));

        // DELETE it
        CallToolResult deleted = callTool("frost_delete", Map.of(
                "path", "Things(" + thingId + ")"));
        assertFalse(isError(deleted), "Deleting must succeed: " + textOf(deleted));

        // GET must now 404 as an isError result carrying the statusCode
        CallToolResult gone = callTool("frost_get", Map.of("path", "Things(" + thingId + ")"));
        assertTrue(isError(gone), "Reading a deleted entity must be an isError result.");
        Map<String, Object> goneError = structuredMap(gone);
        assertEquals(404, ((Number) goneError.get("statusCode")).intValue(),
                "The error must carry the STA status code.");
    }

    @Test
    public void testResources() {
        LOGGER.info("  testResources");
        ReadResourceResult index = mcpClient.readResource(
                McpSchema.ReadResourceRequest.builder("frost://index").build());
        assertNotNull(index);
        assertFalse(index.contents().isEmpty(), "frost://index must yield content.");
        assertTrue(textOf(index).contains("Things"),
                "The index resource must mention the Things entity set.");
        String indexJson = textOf(index);
        assertTrue(indexJson.contains("MCP-Server"),
                "The index document must list the MCP conformance URL.");
        assertTrue(indexJson.contains("\"endpoints\""),
                "The index document must carry an endpoints map for MCP.");
        assertTrue(indexJson.contains(serverSettings.getMcpUrl()),
                "The index document must advertise the MCP endpoint: " + serverSettings.getMcpUrl());

        ReadResourceResult model = mcpClient.readResource(
                McpSchema.ReadResourceRequest.builder("frost://model").build());
        assertNotNull(model);
        assertTrue(textOf(model).contains("Things"), "The model resource must describe Things.");

        ReadResourceResult modelThings = mcpClient.readResource(
                McpSchema.ReadResourceRequest.builder("frost://model/Things").build());
        assertNotNull(modelThings);
        assertTrue(textOf(modelThings).contains("name"), "The Things model resource must list the name property.");

        ReadResourceResult sta = mcpClient.readResource(
                McpSchema.ReadResourceRequest.builder("frost://sta/Things?$top=1").build());
        assertNotNull(sta);
        assertFalse(sta.contents().isEmpty(), "frost://sta/Things must yield content.");
    }

    @Test
    public void testInvalidPathBecomesProtocolError() {
        LOGGER.info("  testInvalidPathBecomesProtocolError");
        // Invalid entity set name; rejected by the STA parser surfaces as an
        // MCP protocol error (-32602) instead of a tool result.
        Assertions.assertThrows(
                McpError.class,
                () -> mcpClient.callTool(CallToolRequest.builder("frost_get")
                        .arguments(Map.of("path", "NotAnEntitySet")).build()),
                "Calling with an invalid path must raise an MCP error.");
    }

    @Test
    public void testSubscriptionRoundTrip() throws InterruptedException {
        LOGGER.info("  testSubscriptionRoundTrip");
        loggingNotifications.clear();

        // Subscribe to Thing creations.
        CallToolResult subscribed = callTool("frost_subscribe", Map.of("path", "Things"));
        assertFalse(isError(subscribed), "Subscribing must succeed: " + textOf(subscribed));
        final Map<String, Object> subResult = structuredMap(subscribed);
        final Number subscriptionId = (Number) subResult.get("subscriptionId");
        assertNotNull(subscriptionId, "The subscribe result must carry a subscriptionId.");
        assertEquals(Boolean.TRUE, subResult.get("sessionActive"), "The session must be active.");
        assertEquals("v1.1/Things", subResult.get("topic"), "The topic must be the canonical path.");

        // Subscribing identically again dedupes to the same id.
        CallToolResult again = callTool("frost_subscribe", Map.of("path", "Things"));
        assertFalse(isError(again));
        final Map<String, Object> subAgain = structuredMap(again);
        assertEquals(subscriptionId.longValue(), ((Number) subAgain.get("subscriptionId")).longValue(),
                "Identical subscriptions must dedupe to the existing id.");
        assertNotNull(subAgain.get("note"), "The deduped subscribe result must carry a note.");

        // Create a Thing; the notification must arrive on the logging stream.
        Map<String, Object> thing = new LinkedHashMap<>();
        thing.put("name", "MCP Subscription Thing");
        thing.put("description", "For the MCP subscription smoke test.");
        CallToolResult created = callTool("frost_create", Map.of(
                "path", "Things",
                "entity", thing,
                "version", "v1.1"));
        assertFalse(isError(created), "Creating a Thing must succeed: " + textOf(created));
        final Map<String, Object> envelope = awaitNotification(subscriptionId.longValue());
        assertEquals("create", envelope.get("eventType"), "The notification must be a create event.");
        assertEquals("Things", envelope.get("entityType"));
        assertEquals(Boolean.FALSE, envelope.get("truncated"), "The envelope must not be truncated.");
        final Map<String, Object> entity = entityOf(envelope);
        assertEquals("MCP Subscription Thing", entity.get("name"), "The envelope must carry the entity.");

        // Unsubscribing by id must report one removed subscription.
        CallToolResult removed = callTool("frost_unsubscribe", Map.of("subscriptionId", subscriptionId));
        assertFalse(isError(removed), "Unsubscribing must succeed: " + textOf(removed));
        assertEquals(1, ((Number) structuredMap(removed).get("removed")).intValue());

        // Unsubscribing the id again finds nothing: an isError result
        // carrying the 404, like reading a deleted entity.
        CallToolResult removedAgain = callTool("frost_unsubscribe", Map.of("subscriptionId", subscriptionId));
        assertTrue(isError(removedAgain), "Unsubscribing an unknown id must be an isError result.");
        assertEquals(404, ((Number) structuredMap(removedAgain).get("statusCode")).intValue(),
                "Unsubscribing an unknown id must report the STA status code.");

        // An invalid subscription path is an MCP protocol error, like the
        // other tools.
        Assertions.assertThrows(
                McpError.class,
                () -> mcpClient.callTool(CallToolRequest.builder("frost_subscribe")
                        .arguments(Map.of("path", "NotAnEntitySet")).build()),
                "Subscribing to an invalid path must raise an MCP error.");
    }

    private Map<String, Object> awaitNotification(long subscriptionId) throws InterruptedException {
        final long deadline = System.currentTimeMillis() + 30_000;
        while (loggingNotifications.isEmpty() && System.currentTimeMillis() < deadline) {
            Thread.sleep(100);
        }
        assertFalse(loggingNotifications.isEmpty(), "A subscription notification must arrive.");
        LoggingMessageNotification notification = loggingNotifications.peek();
        assertEquals("frost", notification.logger(), "The notification must carry the frost logger.");
        final Map<String, Object> envelope = parseEnvelope(notification.data());
        assertEquals(subscriptionId, ((Number) envelope.get("subscriptionId")).longValue());
        return envelope;
    }

    private static Map<String, Object> parseEnvelope(String json) {
        try {
            return SimpleJsonMapper.getSimpleObjectMapper().readValue(
                    json,
                    TypeReferencesHelper.TYPE_REFERENCE_MAP);
        } catch (JacksonException ex) {
            throw new AssertionError("The notification envelope must be valid JSON: " + json, ex);
        }
    }

    private static Map<String, Object> entityOf(Map<String, Object> envelope) {
        Object entity = envelope.get("entity");
        assertNotNull(entity, "The envelope must carry an entity.");
        assertTrue(entity instanceof Map, "The envelope entity must be a JSON object.");
        Map<String, Object> map = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) entity).entrySet()) {
            map.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return map;
    }

    private CallToolResult callTool(String name, Map<String, Object> arguments) {
        return mcpClient.callTool(CallToolRequest.builder(name).arguments(arguments).build());
    }

    private static boolean isError(CallToolResult result) {
        return Boolean.TRUE.equals(result.isError());
    }

    private static String textOf(CallToolResult result) {
        if (result.content() == null || result.content().isEmpty()) {
            return "";
        }
        return String.valueOf(result.content().get(0));
    }

    private static String textOf(ReadResourceResult result) {
        ResourceContents contents = result.contents().get(0);
        if (contents instanceof TextResourceContents text) {
            return text.text();
        }
        return String.valueOf(contents);
    }

    private static Map<String, Object> structuredMap(CallToolResult result) {
        Object structured = result.structuredContent();
        assertNotNull(structured, "The result must carry structuredContent.");
        assertTrue(structured instanceof Map, "The structuredContent must be a JSON object.");
        Map<String, Object> map = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) structured).entrySet()) {
            map.put(String.valueOf(entry.getKey()), entry.getValue());
        }
        return map;
    }

    private static long idFrom(CallToolResult result) {
        Map<String, Object> entity = structuredMap(result);
        Object id = entity.get("@iot.id");
        assertNotNull(id, "Created entity must carry @iot.id: " + textOf(result));
        return ((Number) id).longValue();
    }
}
