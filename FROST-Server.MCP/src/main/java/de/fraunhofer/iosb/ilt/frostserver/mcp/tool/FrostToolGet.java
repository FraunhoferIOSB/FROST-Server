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

import de.fraunhofer.iosb.ilt.frostserver.query.Query;
import de.fraunhofer.iosb.ilt.frostserver.query.QueryDefaults;
import de.fraunhofer.iosb.ilt.frostserver.request.ServiceContext;
import de.fraunhofer.iosb.ilt.frostserver.request.ServiceRequest;
import de.fraunhofer.iosb.ilt.frostserver.request.Version;
import de.fraunhofer.iosb.ilt.frostserver.service.RequestTypeUtils;
import de.fraunhofer.iosb.ilt.frostserver.service.ServiceResponseDefault;
import de.fraunhofer.iosb.ilt.frostserver.util.user.PrincipalExtended;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.List;
import java.util.Map;

/**
 * The frost_get tool: reads any STA path with optional raw query options.
 * The path/query are validated with the STA parsers (parse failures surface
 * as MCP protocol errors), the MCP paging/count caps are applied, and the
 * request is executed against the FROST core Service.
 */
public class FrostToolGet implements FrostTool {

    /**
     * The tool name.
     */
    public static final String NAME = "frost_get";

    @Override
    public String toolName() {
        return NAME;
    }

    private static final String PATH_DESCRIPTION = "STA path, relative to the version root. Examples: 'Things', 'Things(1)', 'Things(1)/Datastreams', 'Things(1)/name', 'Things(1)/name/$value', 'Datastreams(7)/Observations'. An empty path fetches the service index document.";

    private final FrostToolHandler handler;

    public FrostToolGet(FrostToolHandler handler) {
        this.handler = handler;
    }

    /**
     * The input schema for frost_get.
     *
     * @return the JSON schema map.
     */
    public Map<String, Object> inputSchema() {
        return Map.of(
                "type", "object",
                "required", List.of("path"),
                "properties", Map.of(
                        "path", Map.of("type", "string", "description", PATH_DESCRIPTION),
                        "query", Map.of(
                                "type", "string",
                                "description", "Raw STA query options, without leading '?'. Example: '$filter=properties/temperature gt 20 and phenomenonTime gt 2026-01-01T00:00:00Z&$orderby=phenomenonTime desc&$top=5&$select=id,result,phenomenonTime'. Validated by the STA query parser.",
                                "default", ""),
                        "version", Map.of(
                                "type", "string",
                                "description", "STA version part, e.g. 'v1.1'. Default: the server's default version.")),
                "additionalProperties", false);
    }

    /**
     * Builds the tool registration for frost_get.
     *
     * @return the sync tool specification.
     */
    public McpServerFeatures.SyncToolSpecification specification() {
        final McpSchema.Tool tool = McpSchema.Tool.builder()
                .name(NAME)
                .title("Read a STA resource")
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
        return "Read any SensorThings API path (entity, entity set, property, $value, index document). "
                + handler.entitySetCheatSheet() + " "
                + "Navigation syntax: 'Things(1)/Datastreams'. "
                + "Optionally pass raw STA query options ($filter, $select, $expand, $orderby, $top, $skip, $count); "
                + "entity sets are paged via @iot.nextLink. "
                + "For structured, typed arguments use frost_query instead. "
                + "Model details: frost://model resource.";
    }

    private McpSchema.CallToolResult call(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) {
        final Map<String, Object> arguments = request.arguments() == null ? Map.of() : request.arguments();
        final PrincipalExtended principal = handler.principalFrom(exchange);
        handler.requireToolAllowed(NAME);
        final String path = stringArg(arguments, "path");
        final String rawQuery = stringArg(arguments, "query");
        final String versionName = stringArg(arguments, "version");
        final Version version = handler.resolveVersion(versionName);
        final int readGate = handler.checkCanRead(principal);
        if (readGate != 0) {
            return handler.readDeniedResult(NAME, path, principal, version.urlPart, readGate);
        }
        final QueryDefaults queryDefaults = handler.getCoreSettings().getQueryDefaults().copy();
        final ServiceContext context = handler.buildContext(queryDefaults, version);
        final Query query = handler.parseAndValidateQuery(context, version, principal, path, rawQuery);
        final String urlQuery = query == null ? null : handler.queryStringFrom(query);
        final ServiceRequest serviceRequest = new ServiceRequest()
                .setContext(context)
                .setVersion(version)
                .setRequestType(RequestTypeUtils.READ)
                .setUrlPath(path == null ? "" : path)
                .setUrlQuery(urlQuery)
                .setParameterMap(handler.parameterMapFrom(urlQuery))
                .setUserPrincipal(principal);
        final ServiceResponseDefault response = handler.execute(serviceRequest);
        return handler.toCallToolResult(NAME, path, principal, version.urlPart, response);
    }

    static String stringArg(Map<String, Object> arguments, String name) {
        final Object value = arguments.get(name);
        return value == null ? null : String.valueOf(value);
    }

    static String requireStringArg(Map<String, Object> arguments, String name) {
        final String value = stringArg(arguments, name);
        if (value == null || value.isBlank()) {
            throw FrostToolHandler.invalidParams("Missing required argument '" + name + "'.");
        }
        return value;
    }

    static List<String> stringListArg(Map<String, Object> arguments, String name) {
        final Object value = arguments.get(name);
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        if (value == null) {
            return List.of();
        }
        if (value instanceof String string && !string.isBlank()) {
            return List.of(string);
        }
        throw FrostToolHandler.invalidParams("Argument '" + name + "' must be an array of strings.");
    }

    static Integer intArg(Map<String, Object> arguments, String name) {
        final Object value = arguments.get(name);
        if (value == null) {
            return null;
        }
        if (value instanceof Integer number) {
            return number;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.valueOf(String.valueOf(value));
        } catch (NumberFormatException ex) {
            throw FrostToolHandler.invalidParams("Argument '" + name + "' must be an integer.");
        }
    }

    static Boolean boolArg(Map<String, Object> arguments, String name) {
        final Object value = arguments.get(name);
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        return Boolean.valueOf(String.valueOf(value));
    }

}
