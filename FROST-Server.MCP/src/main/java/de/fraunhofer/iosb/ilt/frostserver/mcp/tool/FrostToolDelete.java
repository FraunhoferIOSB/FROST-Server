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

import de.fraunhofer.iosb.ilt.frostserver.request.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.request.Version;
import de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponseDefault;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The frost_delete tool: DELETEs an entity (or a $ref navigation link).
 */
public class FrostToolDelete implements FrostTool {

    /**
     * The tool name.
     */
    public static final String NAME = "frost_delete";

    @Override
    public String toolName() {
        return NAME;
    }

    private final FrostToolHandler handler;

    public FrostToolDelete(FrostToolHandler handler) {
        this.handler = handler;
    }

    /**
     * The input schema for frost_delete.
     *
     * @return the JSON schema map.
     */
    public Map<String, Object> inputSchema() {
        final Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("path", Map.of(
                "type", "string",
                "description", "Path to the entity to delete, e.g. 'Observations(4711)'."));
        properties.put("version", Map.of(
                "type", "string",
                "description", "STA version part, e.g. 'v1.1'. Default: the server's default version."));
        return Map.of(
                "type", "object",
                "required", List.of("path"),
                "properties", properties,
                "additionalProperties", false);
    }

    /**
     * Builds the tool registration for frost_delete.
     *
     * @return the sync tool specification.
     */
    public McpServerFeatures.SyncToolSpecification specification() {
        final McpSchema.Tool tool = McpSchema.Tool.builder()
                .name(NAME)
                .title("Delete an entity")
                .description(description())
                .inputSchema(inputSchema())
                .annotations(McpSchema.ToolAnnotations.builder()
                        .destructiveHint(Boolean.TRUE)
                        .idempotentHint(Boolean.TRUE)
                        .openWorldHint(Boolean.FALSE)
                        .build())
                .build();
        return new McpServerFeatures.SyncToolSpecification(tool, this::call);
    }

    private String description() {
        return "Delete a SensorThings entity by path. "
                + handler.entitySetCheatSheet();
    }

    private McpSchema.CallToolResult call(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) {
        final Map<String, Object> arguments = request.arguments() == null ? Map.of() : request.arguments();
        final PrincipalExtended principal = handler.principalFrom(exchange);
        handler.requireToolAllowed(NAME);
        final String path = FrostToolGet.requireStringArg(arguments, "path");
        final String versionName = FrostToolGet.stringArg(arguments, "version");

        final Version version = handler.resolveVersion(versionName);
        final int readGate = handler.checkCanRead(principal);
        if (readGate != 0) {
            return handler.readDeniedResult(NAME, path, principal, version.urlPart, readGate);
        }
        final ServiceRequest serviceRequest = new ServiceRequest()
                .setContext(handler.buildContext(handler.getCoreSettings().getQueryDefaults().copy(), version))
                .setVersion(version)
                .setRequestType(RequestTypeUtils.DELETE)
                .setUrlPath(path)
                .setUserPrincipal(principal);
        final ServiceResponseDefault response = handler.execute(serviceRequest);
        return handler.toCallToolResult(NAME, path, principal, version.urlPart, response);
    }

}
