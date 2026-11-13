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
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_DEFAULT;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_DESCRIPTION;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_ENUM;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_PATH;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_PROPERTIES;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_REQUIRED;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_TYPE;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_VERSION;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.TYPE_OBJECT;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.TYPE_STRING;

import de.fraunhofer.iosb.ilt.frostserver.mcp.util.StaJsonHelper;
import de.fraunhofer.iosb.ilt.frostserver.request.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.request.Version;
import de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponseDefault;
import de.fraunhofer.iosb.ilt.frostserver.util.Constants;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The frost_update tool: PATCH or JSON-Patch (changeset) an existing entity,
 * both changing only the addressed properties. Full replace (PUT) is a separate
 * tool, frost_replace (FrostToolReplace), because MCP tool annotations are
 * static per registration and only a separate tool can carry
 * destructiveHint=true for the full-replace semantics.
 */
public class FrostToolUpdate implements FrostTool {

    /**
     * The tool name.
     */
    public static final String NAME = "frost_update";

    public static final String KEY_MODE = "mode";
    public static final String KEY_CHANGES = "changes";

    /**
     * The supported update modes.
     */
    public static final String MODE_PATCH = "patch";
    public static final String MODE_CHANGESET = "changeset";

    @Override
    public String toolName() {
        return NAME;
    }

    private final FrostToolHandler handler;

    public FrostToolUpdate(FrostToolHandler handler) {
        this.handler = handler;
    }

    /**
     * The input schema for frost_update.
     *
     * @return the JSON schema map.
     */
    public Map<String, Object> inputSchema() {
        final Map<String, Object> properties = new LinkedHashMap<>();
        properties.put(KEY_PATH, Map.of(
                KEY_TYPE, TYPE_STRING,
                KEY_DESCRIPTION, "Path to the entity (or property) to update, e.g. 'Things(1)' or 'Things(1)/name'."));
        properties.put(KEY_CHANGES, Map.of(
                KEY_DESCRIPTION, "'patch': an STA JSON object (entity or single property value). 'changeset': a JSON Patch array of operations."));
        properties.put(KEY_MODE, Map.of(
                KEY_TYPE, TYPE_STRING,
                KEY_ENUM, List.of(MODE_PATCH, MODE_CHANGESET),
                KEY_DEFAULT, MODE_PATCH,
                KEY_DESCRIPTION, "patch: PATCH semantics, only listed properties change (UPDATE_CHANGES). changeset: RFC 6902 JSON Patch (UPDATE_CHANGESET)."));
        properties.put(KEY_VERSION, Map.of(
                KEY_TYPE, TYPE_STRING,
                KEY_DESCRIPTION, "STA version part, e.g. 'v1.1'. Default: the server's default version."));
        return Map.of(KEY_TYPE, TYPE_OBJECT,
                KEY_REQUIRED, List.of(KEY_PATH, KEY_CHANGES),
                KEY_PROPERTIES, properties,
                KEY_ADDITIONAL_PROPERTIES, false);
    }

    /**
     * Builds the tool registration for frost_update.
     *
     * @return the sync tool specification.
     */
    @Override
    public McpServerFeatures.SyncToolSpecification specification() {
        final McpSchema.Tool tool = McpSchema.Tool.builder(NAME, inputSchema())
                .title("Update an existing entity")
                .description(description())
                .annotations(McpSchema.ToolAnnotations.builder()
                        .destructiveHint(Boolean.FALSE)
                        .idempotentHint(Boolean.TRUE)
                        .openWorldHint(Boolean.FALSE)
                        .build())
                .build();
        return new McpServerFeatures.SyncToolSpecification(tool, this::call);
    }

    private String description() {
        return "Update an existing SensorThings entity: 'patch' changes only the listed properties, "
                + "'changeset' applies an RFC 6902 JSON Patch. For a full replace (PUT semantics) use frost_replace. "
                + handler.entitySetCheatSheet() + " "
                + "Property details: frost://model resource.";
    }

    private McpSchema.CallToolResult call(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) {
        final Map<String, Object> arguments = request.arguments() == null ? Map.of() : request.arguments();
        final PrincipalExtended principal = handler.principalFrom(exchange);
        handler.requireToolAllowed(NAME);
        final String path = FrostToolGet.requireStringArg(arguments, KEY_PATH);
        final Object changes = arguments.get(KEY_CHANGES);
        if (changes == null) {
            throw FrostToolHandler.invalidParams("Missing required argument 'changes'.");
        }
        final String mode = FrostToolGet.stringArg(arguments, KEY_MODE);
        final String versionName = FrostToolGet.stringArg(arguments, KEY_VERSION);

        final String resolvedMode = mode == null || mode.isEmpty() ? MODE_PATCH : mode;
        final String requestType;
        final boolean changeset;
        switch (resolvedMode) {
            case MODE_PATCH -> {
                requestType = RequestTypeUtils.UPDATE_CHANGES;
                changeset = false;
            }
            case MODE_CHANGESET -> {
                requestType = RequestTypeUtils.UPDATE_CHANGESET;
                changeset = true;
            }
            default ->
                throw FrostToolHandler.invalidParams(
                        "Unknown mode '" + resolvedMode + "'. Known modes: patch, changeset. (Full replace is a separate tool: frost_replace.)");
        }
        if (!changeset && !(changes instanceof Map)) {
            throw FrostToolHandler.invalidParams("Argument 'changes' must be a JSON object for mode 'patch'.");
        }
        if (changeset && !(changes instanceof List)) {
            throw FrostToolHandler.invalidParams("Argument 'changes' must be a JSON Patch array for mode 'changeset'.");
        }
        final String changesJson = StaJsonHelper.writeValueAsString(changes);

        final Version version = handler.resolveVersion(versionName);
        final int readGate = handler.checkCanRead(principal);
        if (readGate != 0) {
            return handler.readDeniedResult(NAME, path, principal, version.urlPart, readGate);
        }
        final ServiceRequest serviceRequest = new ServiceRequest()
                .setContext(handler.buildContext(handler.getCoreSettings().getQueryDefaults().copy(), version))
                .setVersion(version)
                .setRequestType(requestType)
                .setUrlPath(path)
                .setContent(changesJson)
                .setContentType(changeset
                        ? Constants.CONTENT_TYPE_APPLICATION_JSONPATCH
                        : Constants.CONTENT_TYPE_APPLICATION_JSON)
                .setUserPrincipal(principal);
        final ServiceResponseDefault response = handler.execute(serviceRequest);
        return handler.toCallToolResult(NAME, path, principal, version.urlPart, response);
    }

}
