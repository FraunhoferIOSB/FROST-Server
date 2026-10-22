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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The frost_query tool: a structured, LLM-friendly entity-set query. Typed
 * arguments are assembled into an STA query string, parsed and validated with
 * the STA QueryParser (so filter expressions keep full STA expressiveness),
 * then executed on the same code path as frost_get.
 */
public class FrostToolQuery implements FrostTool {

    /**
     * The tool name.
     */
    public static final String NAME = "frost_query";

    @Override
    public String toolName() {
        return NAME;
    }

    private final FrostToolHandler handler;

    public FrostToolQuery(FrostToolHandler handler) {
        this.handler = handler;
    }

    /**
     * The input schema for frost_query. The entitySet enum is generated from
     * the live model registry.
     *
     * @return the JSON schema map.
     */
    public Map<String, Object> inputSchema() {
        final Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("entitySet", Map.of(
                "type", "string",
                "enum", handler.entitySetNames(),
                "description", "The entity set to query."));
        properties.put("filter", Map.of(
                "type", "string",
                "description", "STA $filter expression, without the '$filter=' prefix. Example: 'properties/temperature gt 20 and phenomenonTime gt 2026-01-01T00:00:00Z'."));
        properties.put("select", Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "Properties for $select; '@id' allowed."));
        properties.put("expand", Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "description", "$expand paths, e.g. 'Datastream', 'Things($select=id,name)'."));
        properties.put("orderby", Map.of(
                "type", "string",
                "description", "STA $orderby expression, e.g. 'phenomenonTime desc'."));
        properties.put("top", Map.of(
                "type", "integer",
                "minimum", 1,
                "description", "Page size. Clamped to the MCP maximum (mcp.maxTop)."));
        properties.put("skip", Map.of(
                "type", "integer",
                "minimum", 0));
        properties.put("count", Map.of(
                "type", "boolean",
                "description", "Request @iot.count total."));
        properties.put("version", Map.of(
                "type", "string",
                "description", "STA version part, e.g. 'v1.1'. Default: the server's default version."));
        return Map.of(
                "type", "object",
                "required", List.of("entitySet"),
                "properties", properties,
                "additionalProperties", false);
    }

    /**
     * Builds the tool registration for frost_query.
     *
     * @return the sync tool specification.
     */
    public McpServerFeatures.SyncToolSpecification specification() {
        final McpSchema.Tool tool = McpSchema.Tool.builder()
                .name(NAME)
                .title("Query an entity set")
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
        return "Query a SensorThings entity set with typed arguments "
                + "(filter, select, expand, orderby, top, skip, count); "
                + handler.entitySetCheatSheet() + " "
                + "For pages beyond the first, read @iot.nextLink from the result. "
                + "For full STA expressiveness (deep paths, $value, function calls) use frost_get.";
    }

    private McpSchema.CallToolResult call(McpSyncServerExchange exchange, McpSchema.CallToolRequest request) {
        final Map<String, Object> arguments = request.arguments() == null ? Map.of() : request.arguments();
        final PrincipalExtended principal = handler.principalFrom(exchange);
        handler.requireToolAllowed(NAME);
        final String entitySet = FrostToolGet.requireStringArg(arguments, "entitySet");
        final String filter = FrostToolGet.stringArg(arguments, "filter");
        final List<String> select = FrostToolGet.stringListArg(arguments, "select");
        final List<String> expand = FrostToolGet.stringListArg(arguments, "expand");
        final String orderby = FrostToolGet.stringArg(arguments, "orderby");
        final Integer top = FrostToolGet.intArg(arguments, "top");
        final Integer skip = FrostToolGet.intArg(arguments, "skip");
        final Boolean count = FrostToolGet.boolArg(arguments, "count");
        final String versionName = FrostToolGet.stringArg(arguments, "version");

        final Version version = handler.resolveVersion(versionName);
        final int readGate = handler.checkCanRead(principal);
        if (readGate != 0) {
            return handler.readDeniedResult(NAME, entitySet, principal, version.urlPart, readGate);
        }
        final String decodedQuery = assembleQuery(filter, select, expand, orderby, top, skip, count);
        final QueryDefaults queryDefaults = handler.getCoreSettings().getQueryDefaults().copy();
        final ServiceContext context = handler.buildContext(queryDefaults, version);
        final Query query = handler.parseAndValidateQuery(context, version, principal, entitySet, decodedQuery);
        final String urlQuery = handler.queryStringFrom(query);
        final ServiceRequest serviceRequest = new ServiceRequest()
                .setContext(context)
                .setVersion(version)
                .setRequestType(RequestTypeUtils.READ)
                .setUrlPath(entitySet)
                .setUrlQuery(urlQuery)
                .setParameterMap(handler.parameterMapFrom(urlQuery))
                .setUserPrincipal(principal);
        final ServiceResponseDefault response = handler.execute(serviceRequest);
        return handler.toCallToolResult(NAME, entitySet, principal, version.urlPart, response);
    }

    /**
     * Assembles the raw, decoded STA query string from the typed arguments.
     * Values join with '&'; select/expand lists join with ','. The string is
     * then parsed by the STA QueryParser, so the full grammar validates it.
     *
     * @param filter The $filter expression, without prefix. May be null.
     * @param select The $select items. May be null or empty.
     * @param expand The $expand items. May be null or empty.
     * @param orderby The $orderby expression, without prefix. May be null.
     * @param top The $top value. May be null.
     * @param skip The $skip value. May be null.
     * @param count The $count value. May be null.
     * @return the query string, or null when no options are present.
     */
    private String assembleQuery(String filter, List<String> select, List<String> expand,
            String orderby, Integer top, Integer skip, Boolean count) {
        final StringBuilder sb = new StringBuilder();
        appendOption(sb, "$filter", filter);
        appendOption(sb, "$select", joinList(select));
        appendOption(sb, "$expand", joinList(expand));
        appendOption(sb, "$orderby", orderby);
        if (top != null) {
            appendOption(sb, "$top", String.valueOf(top));
        }
        if (skip != null) {
            appendOption(sb, "$skip", String.valueOf(skip));
        }
        if (count != null) {
            appendOption(sb, "$count", String.valueOf(count));
        }
        return sb.isEmpty() ? null : sb.toString();
    }

    private String joinList(List<String> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        return String.join(",", items);
    }

    private void appendOption(StringBuilder sb, String name, String value) {
        if (value == null || value.isEmpty()) {
            return;
        }
        if (!sb.isEmpty()) {
            sb.append('&');
        }
        sb.append(name).append('=').append(value);
    }

}
