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

import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_ADDITIONAL_PROPERTIES;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_DESCRIPTION;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_FILTER;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_ITEMS;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_PATH;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_PROPERTIES;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_REQUIRED;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_SELECT;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_TYPE;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_VERSION;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.TYPE_ARRAY;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.TYPE_OBJECT;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.TYPE_STRING;

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
 * The frost_subscribe tool: registers a session for change notifications on an
 * entity set (optionally narrowed with $filter and $select) or a single entity.
 * The notifications arrive as MCP logging notifications (logger "frost", level
 * INFO).
 */
public class FrostToolSubscribe implements FrostTool {

    public static final String KEY_SUBSCRIPTION_ID = "subscriptionId";

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
        return Map.of(KEY_TYPE, TYPE_OBJECT,
                KEY_REQUIRED, List.of(KEY_PATH),
                KEY_PROPERTIES, Map.of(KEY_PATH, Map.of(
                        KEY_TYPE, TYPE_STRING,
                        KEY_DESCRIPTION, "STA path to subscribe to, relative to the version root. Entity sets and single entities; navigation syntax allowed. Examples: 'Things', 'Things(1)', 'Things(1)/Datastreams', 'Datastreams(7)/Observations'."),
                        KEY_FILTER, Map.of(
                                KEY_TYPE, TYPE_STRING,
                                KEY_DESCRIPTION, "STA $filter expression, without the '$filter=' prefix. Only allowed for entity-set subscriptions. Example: 'properties/temperature gt 20'."),
                        KEY_SELECT, Map.of(
                                KEY_TYPE, TYPE_ARRAY,
                                KEY_ITEMS, Map.of(
                                        KEY_TYPE, TYPE_STRING),
                                KEY_DESCRIPTION, "Properties for $select in notifications; '@id' allowed. Only allowed for entity-set subscriptions."),
                        KEY_VERSION, Map.of(
                                KEY_TYPE, TYPE_STRING,
                                KEY_DESCRIPTION, "STA version part, e.g. 'v1.1'. Default: the server's default version.")),
                KEY_ADDITIONAL_PROPERTIES, false);
    }

    /**
     * Builds the tool registration for frost_subscribe.
     *
     * @return the sync tool specification.
     */
    @Override
    public McpServerFeatures.SyncToolSpecification specification() {
        final McpSchema.Tool tool = McpSchema.Tool.builder(NAME, inputSchema())
                .title("Subscribe to entity changes")
                .description(description())
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
        final String path = FrostToolGet.requireStringArg(arguments, KEY_PATH);
        final String filter = FrostToolGet.stringArg(arguments, KEY_FILTER);
        final List<String> select = FrostToolGet.stringListArg(arguments, KEY_SELECT);
        final String versionName = FrostToolGet.stringArg(arguments, KEY_VERSION);
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
