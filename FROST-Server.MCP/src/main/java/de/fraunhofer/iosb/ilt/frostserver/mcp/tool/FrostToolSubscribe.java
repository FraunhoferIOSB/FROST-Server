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
package de.fraunhofer.iosb.ilt.frostserver.mcp.tool;

import de.fraunhofer.iosb.ilt.frostserver.mcp.subscription.FrostSubscriptionException;
import de.fraunhofer.iosb.ilt.frostserver.mcp.subscription.FrostSubscriptionRegistry;
import de.fraunhofer.iosb.ilt.frostserver.request.Version;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponseDefault;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The frost_subscribe tool: registers a session for change notifications on
 * an entity set (optionally narrowed with $filter and $select) or a single
 * entity. The notifications arrive as MCP logging notifications (logger
 * "frost", level INFO).
 */
public class FrostToolSubscribe implements FrostTool {

    /**
     * The tool name.
     */
    public static final String NAME = "frost_subscribe";

    @Override
    public String toolName() {
        return NAME;
    }

    private final FrostToolHandler handler;
    private final FrostSubscriptionRegistry registry;

    public FrostToolSubscribe(FrostToolHandler handler, FrostSubscriptionRegistry registry) {
        this.handler = handler;
        this.registry = registry;
    }

    /**
     * The input schema for frost_subscribe.
     *
     * @return the JSON schema map.
     */
    public Map<String, Object> inputSchema() {
        return Map.of(
                "type", "object",
                "required", List.of("path"),
                "properties", Map.of(
                        "path", Map.of(
                                "type", "string",
                                "description", "STA path to subscribe to, relative to the version root. Entity sets and single entities; navigation syntax allowed. Examples: 'Things', 'Things(1)', 'Things(1)/Datastreams', 'Datastreams(7)/Observations'."),
                        "filter", Map.of(
                                "type", "string",
                                "description", "STA $filter expression, without the '$filter=' prefix. Only allowed for entity-set subscriptions. Example: 'properties/temperature gt 20'."),
                        "select", Map.of(
                                "type", "array",
                                "items", Map.of("type", "string"),
                                "description", "Properties for $select in notifications; '@id' allowed. Only allowed for entity-set subscriptions."),
                        "version", Map.of(
                                "type", "string",
                                "description", "STA version part, e.g. 'v1.1'. Default: the server's default version.")),
                "additionalProperties", false);
    }

    /**
     * Builds the tool registration for frost_subscribe.
     *
     * @return the sync tool specification.
     */
    public McpServerFeatures.SyncToolSpecification specification() {
        final McpSchema.Tool tool = McpSchema.Tool.builder()
                .name(NAME)
                .title("Subscribe to entity changes")
                .description(description())
                .inputSchema(inputSchema())
                .annotations(McpSchema.ToolAnnotations.builder()
                        .readOnlyHint(Boolean.TRUE)
                        .idempotentHint(Boolean.TRUE)
                        .openWorldHint(Boolean.FALSE)
                        .build())
                .build();
        return new McpServerFeatures.SyncToolSpecification(tool, this::call);
    }

    private String description() {
        return "Subscribe to SensorThings change notifications on an entity set (optionally narrowed with filter and select) or a single entity. "
                + handler.entitySetCheatSheet() + " "
                + "Change events (create, update, delete) arrive as MCP logging notifications (logger 'frost', level INFO) "
                + "carrying subscriptionId, eventType, entityType and the entity. "
                + "Identical repeated subscriptions return the existing subscriptionId. "
                + "End subscriptions with frost_unsubscribe or by closing the session.";
    }

    private McpSchema.CallToolResult call(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) {
        final Map<String, Object> arguments = request.arguments() == null ? Map.of() : request.arguments();
        final PrincipalExtended principal = handler.principalFrom(exchange);
        handler.requireToolAllowed(NAME);
        final String path = FrostToolGet.requireStringArg(arguments, "path");
        final String filter = FrostToolGet.stringArg(arguments, "filter");
        final List<String> select = FrostToolGet.stringListArg(arguments, "select");
        final String versionName = FrostToolGet.stringArg(arguments, "version");
        final Version version = handler.resolveVersion(versionName);
        final int readGate = handler.checkCanRead(principal);
        if (readGate != 0) {
            return handler.readDeniedResult(NAME, path, principal, version.urlPart, readGate);
        }
        if (exchange == null || exchange.sessionId() == null || exchange.sessionId().isEmpty()) {
            throw FrostToolHandler.invalidParams("Subscriptions need a session; no session id on this exchange.");
        }
        final String sessionId = exchange.sessionId();
        try {
            final var result = registry.subscribe(sessionId, principal, version, path, filter, select);
            final Map<String, Object> structured = new LinkedHashMap<>(result.toStructuredContent());
            return mcpResult(structured);
        } catch (IllegalArgumentException ex) {
            throw FrostToolHandler.invalidParams(ex.getMessage());
        } catch (FrostSubscriptionException ex) {
            final ServiceResponseDefault response = new ServiceResponseDefault(ex.getCode(), ex.getMessage());
            return handler.toCallToolResult(NAME, path, principal, version.urlPart, response);
        }
    }

    private McpSchema.CallToolResult mcpResult(Object structured) {
        return McpSchema.CallToolResult.builder()
                .structuredContent(structured)
                .addTextContent(de.fraunhofer.iosb.ilt.frostserver.mcp.util.StaJsonHelper.writeValueAsPrettyString(structured))
                .isError(false)
                .build();
    }

}
