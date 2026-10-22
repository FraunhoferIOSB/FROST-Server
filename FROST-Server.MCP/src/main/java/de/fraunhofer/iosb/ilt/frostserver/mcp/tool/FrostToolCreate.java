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

import de.fraunhofer.iosb.ilt.frostserver.mcp.util.StaJsonHelper;
import de.fraunhofer.iosb.ilt.frostserver.request.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.request.Version;
import de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponseDefault;
import de.fraunhofer.iosb.ilt.frostserver.util.Constants;
import de.fraunhofer.iosb.ilt.frostserver.util.StringHelper;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The frost_create tool: POSTs a new entity (or deep insert) to an entity set.
 * The prefer argument maps to the STA Prefer header semantics: 'minimal'
 * returns only the new @iot.id, 'representation' the full created entity
 * ( honoured by STA v2; v1 always creates with an empty body, like HTTP).
 */
public class FrostToolCreate implements FrostTool {

    /**
     * The tool name.
     */
    public static final String NAME = "frost_create";

    @Override
    public String toolName() {
        return NAME;
    }

    private final FrostToolHandler handler;

    public FrostToolCreate(FrostToolHandler handler) {
        this.handler = handler;
    }

    /**
     * The input schema for frost_create.
     *
     * @return the JSON schema map.
     */
    public Map<String, Object> inputSchema() {
        final Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("path", Map.of(
                "type", "string",
                "description", "Target entity set, optionally nested under an entity. Examples: 'Things', 'Things(1)/HistoricalLocations', 'Datastreams(3)/Observations'."));
        properties.put("entity", Map.of(
                "type", "object",
                "description", "The entity to create, as STA JSON. Required/nullable/read-only rules and navigation by @id link are validated by the server (EntityType.validateCreate)."));
        properties.put("prefer", Map.of(
                "type", "string",
                "enum", List.of(Constants.VALUE_RETURN_REPRESENTATION, Constants.VALUE_RETURN_MINIMAL),
                "default", Constants.VALUE_RETURN_REPRESENTATION,
                "description", "'minimal' returns only the new @iot.id; 'representation' returns the full created entity."));
        properties.put("version", Map.of(
                "type", "string",
                "description", "STA version part, e.g. 'v1.1'. Default: the server's default version."));
        return Map.of(
                "type", "object",
                "required", List.of("path", "entity"),
                "properties", properties,
                "additionalProperties", false);
    }

    /**
     * Builds the tool registration for frost_create.
     *
     * @return the sync tool specification.
     */
    public McpServerFeatures.SyncToolSpecification specification() {
        final McpSchema.Tool tool = McpSchema.Tool.builder()
                .name(NAME)
                .title("Create a new entity")
                .description(description())
                .inputSchema(inputSchema())
                .annotations(McpSchema.ToolAnnotations.builder()
                        .destructiveHint(Boolean.FALSE)
                        .idempotentHint(Boolean.FALSE)
                        .openWorldHint(Boolean.FALSE)
                        .build())
                .build();
        return new McpServerFeatures.SyncToolSpecification(tool, this::call);
    }

    private String description() {
        return "Create a new entity (or a deep insert creating related entities inline) in a SensorThings entity set. "
                + "The entity is STA JSON; navigation properties may use @iot.id links. "
                + handler.entitySetCheatSheet() + " "
                + "Property details: frost://model resource.";
    }

    private McpSchema.CallToolResult call(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) {
        final Map<String, Object> arguments = request.arguments() == null ? Map.of() : request.arguments();
        final PrincipalExtended principal = handler.principalFrom(exchange);
        handler.requireToolAllowed(NAME);
        final String path = FrostToolGet.requireStringArg(arguments, "path");
        final Object entity = arguments.get("entity");
        if (!(entity instanceof Map)) {
            throw FrostToolHandler.invalidParams("Missing required argument 'entity' (must be a JSON object).");
        }
        final String prefer = FrostToolGet.stringArg(arguments, "prefer");
        final String versionName = FrostToolGet.stringArg(arguments, "version");
        final String entityJson = StaJsonHelper.writeValueAsString(entity);

        final Version version = handler.resolveVersion(versionName);
        final int readGate = handler.checkCanRead(principal);
        if (readGate != 0) {
            return handler.readDeniedResult(NAME, path, principal, version.urlPart, readGate);
        }
        final ServiceRequest serviceRequest = new ServiceRequest()
                .setContext(handler.buildContext(handler.getCoreSettings().getQueryDefaults().copy(), version))
                .setVersion(version)
                .setRequestType(RequestTypeUtils.CREATE)
                .setUrlPath(path)
                .setContent(entityJson)
                .setContentType(Constants.CONTENT_TYPE_APPLICATION_JSON)
                .setUserPrincipal(principal);
        if (!StringHelper.isNullOrEmpty(prefer)) {
            serviceRequest.setParameter(Constants.TAG_PREFER_RETURN, prefer);
        }
        final ServiceResponseDefault response = handler.execute(serviceRequest);
        return handler.toCallToolResult(NAME, path, principal, version.urlPart, response);
    }

}
