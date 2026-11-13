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
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_COUNT;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_DESCRIPTION;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_ENTITY_SET;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_ENUM;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_EXPAND;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_FILTER;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_ITEMS;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_MINIMUM;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_ORDERBY;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_PROPERTIES;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_REQUIRED;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_SELECT;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_SKIP;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_TOP;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_TYPE;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.KEY_VERSION;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.TYPE_ARRAY;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.TYPE_BOOLEAN;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.TYPE_INTEGER;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.TYPE_OBJECT;
import static de.fraunhofer.iosb.ilt.frostserver.mcp.tool.FrostToolHandler.TYPE_STRING;

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
import java.util.Optional;

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
        properties.put(KEY_ENTITY_SET, Map.of(
                KEY_TYPE, TYPE_STRING,
                KEY_ENUM, handler.entitySetNames(),
                KEY_DESCRIPTION, "The entity set to query."));
        properties.put(KEY_FILTER, Map.of(
                KEY_TYPE, TYPE_STRING,
                KEY_DESCRIPTION, "STA $filter expression, without the '$filter=' prefix. Example: 'properties/temperature gt 20 and phenomenonTime gt 2026-01-01T00:00:00Z'."));
        properties.put(KEY_SELECT, Map.of(
                KEY_TYPE, TYPE_ARRAY,
                KEY_ITEMS, Map.of(
                        KEY_TYPE, TYPE_STRING),
                KEY_DESCRIPTION, "Properties for $select; '@id' allowed."));
        properties.put(KEY_EXPAND, Map.of(
                KEY_TYPE, TYPE_ARRAY,
                KEY_ITEMS, Map.of(
                        KEY_TYPE, TYPE_STRING),
                KEY_DESCRIPTION, "$expand paths, e.g. 'Datastream', 'Things($select=id,name)'."));
        properties.put(KEY_ORDERBY, Map.of(
                KEY_TYPE, TYPE_STRING,
                KEY_DESCRIPTION, "STA $orderby expression, e.g. 'phenomenonTime desc'."));
        properties.put(KEY_TOP, Map.of(
                KEY_TYPE, TYPE_INTEGER,
                KEY_MINIMUM, 1,
                KEY_DESCRIPTION, "Page size. Clamped to the MCP maximum (mcp.maxTop)."));
        properties.put(KEY_SKIP, Map.of(
                KEY_TYPE, TYPE_INTEGER,
                KEY_MINIMUM, 0));
        properties.put(KEY_COUNT, Map.of(
                KEY_TYPE, TYPE_BOOLEAN,
                KEY_DESCRIPTION, "Request @iot.count total."));
        properties.put(KEY_VERSION, Map.of(
                KEY_TYPE, TYPE_STRING,
                KEY_DESCRIPTION, "STA version part, e.g. 'v1.1'. Default: the server's default version."));
        return Map.of(KEY_TYPE, TYPE_OBJECT,
                KEY_REQUIRED, List.of(KEY_ENTITY_SET),
                KEY_PROPERTIES, properties,
                KEY_ADDITIONAL_PROPERTIES, false);
    }

    /**
     * Builds the tool registration for frost_query.
     *
     * @return the sync tool specification.
     */
    @Override
    public McpServerFeatures.SyncToolSpecification specification() {
        final McpSchema.Tool tool = McpSchema.Tool.builder(NAME, inputSchema())
                .title("Query an entity set")
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
        final String entitySet = FrostToolGet.requireStringArg(arguments, KEY_ENTITY_SET);
        final String filter = FrostToolGet.stringArg(arguments, KEY_FILTER);
        final List<String> select = FrostToolGet.stringListArg(arguments, KEY_SELECT);
        final List<String> expand = FrostToolGet.stringListArg(arguments, KEY_EXPAND);
        final String orderby = FrostToolGet.stringArg(arguments, KEY_ORDERBY);
        final Optional<Integer> top = FrostToolGet.intArg(arguments, KEY_TOP);
        final Optional<Integer> skip = FrostToolGet.intArg(arguments, KEY_SKIP);
        final Optional<Boolean> count = FrostToolGet.boolArg(arguments, KEY_COUNT);
        final String versionName = FrostToolGet.stringArg(arguments, KEY_VERSION);

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
    String assembleQuery(String filter, List<String> select, List<String> expand,
            String orderby, Optional<Integer> top, Optional<Integer> skip, Optional<Boolean> count) {
        final StringBuilder sb = new StringBuilder();
        appendOption(sb, "$filter", filter);
        appendOption(sb, "$select", joinList(select));
        appendOption(sb, "$expand", joinList(expand));
        appendOption(sb, "$orderby", orderby);
        if (top.isPresent()) {
            appendOption(sb, "$top", top.get().toString());
        }
        if (skip.isPresent()) {
            appendOption(sb, "$skip", skip.get().toString());
        }
        if (count.isPresent()) {
            appendOption(sb, "$count", count.get().toString());
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
