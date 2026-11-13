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
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_PROPERTIES;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_REQUIRED;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_TYPE;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_VERSION;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.TYPE_INTEGER;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.TYPE_OBJECT;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.TYPE_STRING;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolSubscribe.KEY_SUBSCRIPTION_ID;

import de.fraunhofer.iosb.ilt.frostserver.mcp.subscription.FrostSubscriptionRegistry;
import de.fraunhofer.iosb.ilt.frostserver.mcp.util.StaJsonHelper;
import de.fraunhofer.iosb.ilt.frostserver.request.Version;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponseDefault;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The frost_unsubscribe tool: removes one subscription (by id) or all
 * subscriptions of the calling session.
 */
public class FrostToolUnsubscribe implements FrostTool {

    /**
     * The tool name.
     */
    public static final String NAME = "frost_unsubscribe";

    @Override
    public String toolName() {
        return NAME;
    }

    private final FrostToolHandler handler;
    private final FrostSubscriptionRegistry registry;

    public FrostToolUnsubscribe(FrostToolHandler handler, FrostSubscriptionRegistry registry) {
        this.handler = handler;
        this.registry = registry;
    }

    /**
     * The input schema for frost_unsubscribe.
     *
     * @return the JSON schema map.
     */
    public Map<String, Object> inputSchema() {
        return Map.of(KEY_TYPE, TYPE_OBJECT,
                KEY_REQUIRED, List.of(),
                KEY_PROPERTIES, Map.of(
                        KEY_SUBSCRIPTION_ID, Map.of(
                                KEY_TYPE, TYPE_INTEGER,
                                KEY_DESCRIPTION, "The subscription id to remove, as returned by frost_subscribe. Omit to remove all subscriptions of this session."),
                        KEY_VERSION, Map.of(
                                KEY_TYPE, TYPE_STRING,
                                KEY_DESCRIPTION, "STA version part, e.g. 'v1.1'. Default: the server's default version.")),
                KEY_ADDITIONAL_PROPERTIES, false);
    }

    /**
     * Builds the tool registration for frost_unsubscribe.
     *
     * @return the sync tool specification.
     */
    @Override
    public McpServerFeatures.SyncToolSpecification specification() {
        final McpSchema.Tool tool = McpSchema.Tool.builder(NAME, inputSchema())
                .title("Unsubscribe from entity changes")
                .description(description())
                .annotations(McpSchema.ToolAnnotations.builder()
                        .readOnlyHint(Boolean.TRUE)
                        .idempotentHint(Boolean.FALSE)
                        .openWorldHint(Boolean.FALSE)
                        .build())
                .build();
        return new McpServerFeatures.SyncToolSpecification(tool, this::call);
    }

    private String description() {
        return "Remove one subscription (by subscriptionId) or, when no id is given, all subscriptions of this session. "
                + "Returns the number of removed subscriptions.";
    }

    private McpSchema.CallToolResult call(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) {
        final Map<String, Object> arguments = request.arguments() == null ? Map.of() : request.arguments();
        final PrincipalExtended principal = handler.principalFrom(exchange);
        handler.requireToolAllowed(NAME);
        final Optional<Integer> subscriptionId = FrostToolGet.intArg(arguments, KEY_SUBSCRIPTION_ID);
        final String versionName = FrostToolGet.stringArg(arguments, KEY_VERSION);
        final Version version = handler.resolveVersion(versionName);
        final int readGate = handler.checkCanRead(principal);
        if (readGate != 0) {
            return handler.readDeniedResult(NAME, null, principal, version.urlPart, readGate);
        }
        if (exchange == null || exchange.sessionId() == null || exchange.sessionId().isEmpty()) {
            throw FrostToolHandler.invalidParams("Subscriptions need a session; no session id on this exchange.");
        }
        final String sessionId = exchange.sessionId();
        if (subscriptionId.isPresent() && registry.subscriptionIds(sessionId).contains(subscriptionId.get().longValue())) {
            final int removed = registry.unsubscribe(sessionId, subscriptionId.get().longValue());
            return result(removed);
        }
        if (subscriptionId.isEmpty()) {
            return result(registry.unsubscribe(sessionId, null));
        }
        final ServiceResponseDefault response = new ServiceResponseDefault(404, "No subscription with id " + subscriptionId + " in this session.");
        return handler.toCallToolResult(NAME, null, principal, version.urlPart, response);
    }

    private McpSchema.CallToolResult result(int removed) {
        final Map<String, Object> structured = new LinkedHashMap<>();
        structured.put("removed", removed);
        return McpSchema.CallToolResult.builder()
                .structuredContent(structured)
                .addTextContent(StaJsonHelper.writeValueAsPrettyString(structured))
                .isError(false)
                .build();
    }

}
