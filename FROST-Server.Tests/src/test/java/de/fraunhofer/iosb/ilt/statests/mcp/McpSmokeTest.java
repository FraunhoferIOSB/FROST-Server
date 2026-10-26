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

import de.fraunhofer.iosb.ilt.statests.AbstractTestClass;
import de.fraunhofer.iosb.ilt.statests.ServerVersion;
import de.fraunhofer.iosb.ilt.statests.TestCore;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.ResourceContents;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Smoke test for the FROST MCP server: boots the stand-alone FrostMcpServer
 * alongside the regular HTTP/MQTT test servers (same database, no bus of its
 * own) and drives it with the MCP Java SDK client over the streamable-HTTP
 * transport: initialize handshake, tools/list, a full CRUD round-trip through
 * the frost_* tools, error mapping and the frost:// resources.
 */
public class McpSmokeTest extends AbstractTestClass {

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(McpSmokeTest.class);

    private static final Set<String> EXPECTED_TOOLS = Set.of(
            "frost_get", "frost_query", "frost_create",
            "frost_update", "frost_replace", "frost_delete");

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
                .clientInfo(new McpSchema.Implementation("FROST-MCP-SmokeTest", "1.0.0"))
                .build();
        mcpClient.initialize();
    }

    @AfterAll
    public static void disconnect() {
        if (mcpClient != null) {
            mcpClient.close();
            mcpClient = null;
        }
    }

    @Test
    public void testInitializeHandshake() {
        // initialize() ran during setUpVersion; a working tools/list proves it.
        assertNotNull(mcpClient, "Client must be initialised.");
        McpSchema.ListToolsResult tools = mcpClient.listTools();
        assertNotNull(tools, "Server must answer tools/list after initialize.");
    }

    @Test
    public void testToolsList() {
        List<Tool> tools = mcpClient.listTools().tools();
        Set<String> names = tools.stream().map(Tool::name).collect(Collectors.toSet());
        assertEquals(EXPECTED_TOOLS, names, "All six generic tools must be listed.");
        for (Tool tool : tools) {
            assertTrue(tool.description() != null && !tool.description().isBlank(),
                    "Tool " + tool.name() + " must carry a description.");
            Map<String, Object> schema = tool.inputSchema();
            assertEquals("object", schema.get("type"), "Tool " + tool.name() + " input schema must be an object.");
        }
    }

    @Test
    public void testCrudRoundTrip() {
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
        ReadResourceResult index = mcpClient.readResource(
                new McpSchema.ReadResourceRequest("frost://index"));
        assertNotNull(index);
        assertFalse(index.contents().isEmpty(), "frost://index must yield content.");
        assertTrue(textOf(index).contains("Things"),
                "The index resource must mention the Things entity set.");

        ReadResourceResult model = mcpClient.readResource(
                new McpSchema.ReadResourceRequest("frost://model"));
        assertNotNull(model);
        assertTrue(textOf(model).contains("Things"), "The model resource must describe Things.");

        ReadResourceResult modelThings = mcpClient.readResource(
                new McpSchema.ReadResourceRequest("frost://model/Things"));
        assertNotNull(modelThings);
        assertTrue(textOf(modelThings).contains("name"), "The Things model resource must list the name property.");

        ReadResourceResult sta = mcpClient.readResource(
                new McpSchema.ReadResourceRequest("frost://sta/Things?$top=1"));
        assertNotNull(sta);
        assertFalse(sta.contents().isEmpty(), "frost://sta/Things must yield content.");
    }

    @Test
    public void testInvalidPathBecomesProtocolError() {
        // Invalid entity set name; rejected by the STA parser surfaces as an
        // MCP protocol error (-32602) instead of a tool result.
        try {
            mcpClient.callTool(new CallToolRequest("frost_get", Map.of("path", "NotAnEntitySet")));
            throw new AssertionError("Calling with an invalid path must raise an MCP error.");
        } catch (McpError ex) {
            LOGGER.info("Expected MCP error for invalid path: {}", ex.getMessage());
        }
    }

    private CallToolResult callTool(String name, Map<String, Object> arguments) {
        return mcpClient.callTool(new CallToolRequest(name, arguments));
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
